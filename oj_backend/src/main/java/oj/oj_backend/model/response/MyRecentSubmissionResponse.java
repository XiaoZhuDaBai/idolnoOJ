package oj.oj_backend.model.response;

import lombok.Data;

import java.util.Date;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/7/21 15:04
 */
@Data
public class MyRecentSubmissionResponse {
    private String title;
    private String cnName;
    private Date createTime;
}
