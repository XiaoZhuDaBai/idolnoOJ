package oj.oj_backend.model.response;

import lombok.Data;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/6/15 22:50
 */
@Data
public class MyRecentWrongSubmissionResponse {
    private String problemId;
    private String problemName;
    private int tryCount;
}
