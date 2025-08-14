package oj.oj_codesandbox.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 *
 * @TableName question
 */
@TableName(value ="question")
@Data
public class Question {
    /**
     * 题目ID
     */
    @TableId
    private String questionId;

    /**
     * 题目名称
     */
    private String questionName;

    /**
     * 题目类型， 0  ACM, 1  OI
     */
    private Integer questionType;

    /**
     * 题目添加时间
     */
    private Date addTime;

    /**
     * 题目创作者
     */
    private String author;

    /**
     * 题目难度，0简单，1中等，2困难
     */
    private Integer difficulty;

    /**
     * 题目标签
     */
    private String questionTag;

    /**
     * 时间限制
     */
    private Long timeLimit;

    /**
     * 内存限制
     */
    private Long memoryLimit;

    /**
     * 栈空间限制
     */
    private Long stackLimit;

    /**
     * 题目描述
     */
    // todo 修改单词
    private String questionDescription;

    /**
     * 输入描述
     */
    private String questionInputDescription;

    /**
     * 输出描述
     */
    private String questionOutputDescription;

    /**
     * 样例描述
     */
    private String questionExamples;

    /**
     * 题目来源（Leetcode，NowCoder等等）
     */
    private String questionSource;

    /**
     * 提交总数
     */
    private Integer questionCommits;

    /**
     * 通过数
     */
    private Integer ac;

    /**
     * 题目修改时间
     */
    private Date modifyTime;

    /**
     * 题目状态，0 删除， 1保留
     */
    private Integer status;
}