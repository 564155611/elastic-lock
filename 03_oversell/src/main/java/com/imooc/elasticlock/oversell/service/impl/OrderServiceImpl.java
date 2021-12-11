package com.imooc.elasticlock.oversell.service.impl;

import com.imooc.elasticlock.oversell.entity.Order;
import com.imooc.elasticlock.oversell.entity.OrderItem;
import com.imooc.elasticlock.oversell.entity.Product;
import com.imooc.elasticlock.oversell.mapper.OrderMapper;
import com.imooc.elasticlock.oversell.service.IOrderItemService;
import com.imooc.elasticlock.oversell.service.IOrderService;
import com.imooc.elasticlock.oversell.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>
 * 订单表  服务实现类
 * </p>
 *
 * @author fanx
 * @since 2021-12-08
 */
@Service
public class OrderServiceImpl extends BaseServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    private IOrderService orderService;
    @Autowired
    private IOrderItemService orderItemService;
    @Autowired
    private IProductService productService;
    //购买商品id
    private int purchaseProductId = 100100;
    //购买商品数量
    private int purchaseProductNum = 1;
    /**
     * 由于synchronized,reenterantLock等的锁定没有将transaction.commit这样的逻辑锁到,
     * 所以可能会导致AB两个createOrder事务的并发:由于MySQL默认采用REPEATABLE_READ隔离,
     * 所以AB在第一次的查询后都会拿到独立的快照视图,所以如果A事务在transaction commit之前
     * B事务执行了查询则此时B查询的数据是A事务更新之前的数据,这样B就能够顺利通过库存检查,
     * 就会导致库存明明只有一件,但是订单最终会被创建两次,库存最终也被减少成负数.
     */
    @Autowired
    private PlatformTransactionManager platformTransactionManager;
    @Autowired
    private TransactionDefinition transactionDefinition;

    private Lock lock = new ReentrantLock();

    /**
     * 避免库存并发的修改的异常.方案:
     * ①锁住(查询库存,修改库存)这一块逻辑+READ_UNCOMMITTED级别的声明事务.锁住的方案可以使用:synchronized方法,sychronized代码块,ReenterantLock
     * ②锁住(事务开启,查询库存,修改库存,事务提交)这一块逻辑+后面可以采用无锁补偿
     * @return
     */
//    @Transactional(isolation = Isolation.READ_UNCOMMITTED, rollbackFor = Exception.class)
    @Override
    public /*synchronized*/ Integer createOrder() {
        System.out.println(platformTransactionManager.getClass());
        Product product = null;
        try {

            lock.lock();
            TransactionStatus transaction1 = platformTransactionManager.getTransaction(transactionDefinition);
            product = productService.getById(purchaseProductId);
            if (product == null) {
                platformTransactionManager.rollback(transaction1);
                throw new RuntimeException("购买商品：" + purchaseProductId + "不存在");
            }

            //商品当前库存
            Integer currentCount = product.getCount();
            System.out.println(Thread.currentThread().getName() + "库存数：" + currentCount);
            //校验库存
            if (purchaseProductNum > currentCount) {
                platformTransactionManager.rollback(transaction1);
                throw new RuntimeException("商品" + purchaseProductId + "仅剩" + currentCount + "件，无法购买");
            }
            Map<String, Object> setParams = new LinkedHashMap<>();
            setParams.put("count=count-{0}", purchaseProductNum);
            setParams.put("create_user", "xxx");
            setParams.put("update_user", "xxx");
            Map<String, Object> filterParams = new LinkedHashMap<>();
            filterParams.put("id", product.getId());
            System.out.println("---------------------------------------------------->" + System.currentTimeMillis());
            productService.update(productService.updateWrapper(setParams, filterParams));
            System.out.println("---------------------------------------------------->" + System.currentTimeMillis());
            platformTransactionManager.commit(transaction1);
        } finally {
            lock.unlock();
        }

        TransactionStatus transaction = platformTransactionManager.getTransaction(transactionDefinition);
        Order order = new Order();
        order.setOrderAmount(product.getPrice().multiply(new BigDecimal(purchaseProductNum)));
        order.setOrderStatus(1);//待处理
        order.setReceiverName("xxx");
        order.setReceiverMobile("13311112222");
        order.setCreateUser("xxx");
        order.setUpdateUser("xxx");
        orderService.save(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setProductId(product.getId());
        orderItem.setPurchasePrice(product.getPrice());
        orderItem.setPurchaseNum(purchaseProductNum);
        orderItem.setCreateUser("xxx");
        orderItem.setUpdateUser("xxx");
        orderItemService.save(orderItem);
        platformTransactionManager.commit(transaction);
        return order.getId();

    }

}
