package com.imooc.elasticlock.oversell.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.imooc.elasticlock.oversell.util.Range;
import com.imooc.elasticlock.oversell.util.annotations.QueryFilter;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
@Data
public class BaseEntity<T extends BaseEntity<T>> extends Model<T> implements Serializable {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 创建时间
     */
    @QueryFilter(Range.class)
    @TableField(fill=FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateUser;

    /**
     * 更新时间
     */

    @QueryFilter(Range.class)
    @TableField(fill=FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
