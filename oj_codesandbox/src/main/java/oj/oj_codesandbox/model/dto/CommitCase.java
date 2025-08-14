package oj.oj_codesandbox.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 *
 * @TableName commit_case
 */
@TableName(value ="commit_case")
@Data
public class CommitCase {
    /**
     * 评测结果ID
     */
    @TableId
    private String commitId;

    /**
     * 评测结果名
     */
    private String cnName;

    /**
     * 评测结果英文名
     */
    private String englishName;



    /**
     * 输入案例
     */
    private String input;

    /**
     * 输出结果
     */
    private String output;

    private long time;
    private long memory;
}