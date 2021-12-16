package com.imooc.elasticlock.distributelock.mapper;

import com.imooc.elasticlock.distributelock.entity.DistributeLock;
import com.imooc.elasticlock.distributelock.entity.DistributeLockExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DistributeLockMapper {
    long countByExample(DistributeLockExample example);

    int deleteByExample(DistributeLockExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(DistributeLock record);

    int insertSelective(DistributeLock record);

    List<DistributeLock> selectByExample(DistributeLockExample example);

    DistributeLock selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") DistributeLock record, @Param("example") DistributeLockExample example);

    int updateByExample(@Param("record") DistributeLock record, @Param("example") DistributeLockExample example);

    int updateByPrimaryKeySelective(DistributeLock record);

    int updateByPrimaryKey(DistributeLock record);

    DistributeLock selectLock(@Param("businessCode") String businessCode);
}