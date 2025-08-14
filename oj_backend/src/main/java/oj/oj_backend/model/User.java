package oj.oj_backend.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 *
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User {
    /**
     * 用户ID
     */
    @TableId
    private String uuid;

    /**
     * 微信唯一标识
     */
    private String openid;

    /**
     * 微信开放平台统一ID
     */
    private String unionid;

    /**
     * 用户权限，admin， user， tourist
     */
    private String auth;

    /**
     * 用户名称
     */
    private String nickname;

    /**
     * 学校
     */
    private String school;

    /**
     * 专业
     */
    private String course;


    /**
     * 邮箱
     */
    private String email;

    /**
     * 性别， 0 女 ，1 男
     */
    private Integer gender;

    /**
     * 头像图片地址
     */
    private String avatar;

    /**
     * 个性签名
     */
    private String signature;

    /**
     * github地址
     */
    private String github;

    /**
     * 账号状态，0 注销， 1 未注销
     */
    private Integer status;

    /**
     * 竞赛网站
     */
    private String rateWebsite;

    /**
     * 竞赛分数
     */
    private Integer rateScore;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date modifyTime;
}