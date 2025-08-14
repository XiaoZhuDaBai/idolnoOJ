package oj.oj_backend.model.response;

import lombok.Data;

import java.util.Date;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/7/22 0:05
 */
@Data
public class SubmissionResponse {
    private String problemName;
    private String avatar;
    private String nickname;
    private String commitCase;
    private long time;
    private long memory;
    private Date createTime;
    private String code;
    private String language;
}
