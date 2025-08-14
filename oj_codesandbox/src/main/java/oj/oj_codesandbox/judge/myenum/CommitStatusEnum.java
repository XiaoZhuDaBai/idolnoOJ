package oj.oj_codesandbox.judge.myenum;

import lombok.Getter;

@Getter
public enum CommitStatusEnum {
    SUCCESS("提交成功", "success"),
    FAIL("提交失败", "fail"),
    WAITING("等待中", "waiting"),
    SUBMITTING("提交中", "submitting"),
    RUNNING("运行中", "running"),
    TIME_OUT("提交超时", "time_out"),
    MEMORY_LIMIT_EXCEEDED("内存超限", "memory_limit_exceeded"),
    RUNTIME_ERROR("运行错误", "runtime_error"),
    COMPILE_ERROR("编译错误", "compile_error"),
    ACCEPTED("通过", "accepted"),
    WRONG_ANSWER("答案错误", "wrong_answer"),
    NON_ZERO_ERROR("非零错误", "non_zero_error");

    private final String cnMessage;
    private final String enMessage;

    CommitStatusEnum(String cnMessage, String enMessage) {
        this.cnMessage = cnMessage;
        this.enMessage = enMessage;
    }

}
