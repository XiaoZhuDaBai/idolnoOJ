package oj.oj_codesandbox.model.vo;

import lombok.Data;

@Data
public class UserTestResultVo {
    private String input;
    private String output;
    private long time;
    private long memory;
}
