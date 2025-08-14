package oj.oj_codesandbox.model;
import lombok.Data;

/**
 * 进程执行信息
 */
@Data
public class ExecuteMessage {
    private Long exitValue;

    private String message;

    private String errorMessage;

    private String outputFilePath;

    private Long time;
    private Long Memory;

    private boolean correct;
}