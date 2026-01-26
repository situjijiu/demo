package com.example.mybatis.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户详情信息实体类
 * 对应表名：tb_user_info
 */
@Data
@TableName("tb_user_info")
public class UserInfo {

    /**
     * 主键，用户id
     */
    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;

    /**
     * 城市名称
     */
    @TableField("city")
    private String city;

    /**
     * 个人介绍，不要超过128个字符
     */
    @TableField("introduce")
    private String introduce;

    /**
     * 粉丝数量
     */
    @TableField("fans")
    private Integer fans;

    /**
     * 关注的人的数量
     */
    @TableField("followee")
    private Integer followee;

    /**
     * 性别，0：男，1：女
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 生日
     */
    @TableField("birthday")
    private LocalDate birthday;

    /**
     * 积分
     */
    @TableField("credits")
    private Integer credits;

    /**
     * 会员级别，0~9级,0代表未开通会员
     */
    @TableField("level")
    private Integer level;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}