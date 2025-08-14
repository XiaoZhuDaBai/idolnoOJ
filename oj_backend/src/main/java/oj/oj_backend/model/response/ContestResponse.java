package oj.oj_backend.model.response;

import lombok.Data;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/6/26 23:51
 */
@Data
public class ContestResponse {
    private Long start_time;
    private Long end_time;
    private Long durationSeconds;
    private String name;
    private String platform;
    private String contest_url;
}
