package oj.oj_codesandbox.model.dto;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;
import lombok.Data;

/**
 *
 * @TableName user_commit
 */
@TableName(value ="user_commit")
@Data
public class UserCommit {
    /**
     * 做题记录ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 答题人ID
     */
    private String uid;

    /**
     * 题目ID
     */
    private String qid;

    /**
     * 提交的ID
     */
    private String commitId;

    /**
     * 提交的代码
     */
    private String code;

    /**
     * 程序语言
     */
    private String language;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date modifyTime;
}