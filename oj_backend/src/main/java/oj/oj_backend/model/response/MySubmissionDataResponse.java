package oj.oj_backend.model.response;

import lombok.Data;

import java.util.Date;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/7/21 18:24
 */
@Data
public class MySubmissionDataResponse {
    private long commitCount;
    private long acCount;
    private Date time;
}
