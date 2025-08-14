package oj.oj_codesandbox.model.vo;

import lombok.Data;

import java.util.List;

/**
 *  返回与前端提交结果
 */
@Data
public class CommitResultVo {
    private List<String> output;
    private String message;
    private long time;
    private long memory;
    private String errorMessages;
    private String commitId;
    private boolean processing;
}
