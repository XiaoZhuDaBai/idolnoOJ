package oj.oj_backend.model.vo;

import lombok.Data;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/6/15 22:51
 */
@Data
public class SearchQuestionVo {
    private String platform;
    private String difficulty;
    private String resource;
    private String[] tags;
    private int page;
}
