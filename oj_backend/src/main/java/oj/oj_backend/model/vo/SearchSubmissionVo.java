package oj.oj_backend.model.vo;

import lombok.Data;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/7/21 18:43
 */
@Data
public class SearchSubmissionVo {
    private String uuid;
    private String problemName;
    private String language;
    private String status;
    private String userType;
    private int page;
}
