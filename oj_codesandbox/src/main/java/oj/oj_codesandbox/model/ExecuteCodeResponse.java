package oj.oj_codesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import oj.oj_codesandbox.judge.entity.JudgeInfo;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteCodeResponse {
    private List<String> outputList;
    /**
     * 接口信息
     */
    private String message;
    private String status;
    private JudgeInfo judgeInfo;
    // 非零表示异常
    private long exitCode;
}
