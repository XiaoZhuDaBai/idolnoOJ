package oj.oj_codesandbox.judge.strategy.impl;


import oj.oj_codesandbox.judge.entity.JudgeCase;
import oj.oj_codesandbox.judge.entity.JudgeContext;
import oj.oj_codesandbox.judge.entity.JudgeInfo;
import oj.oj_codesandbox.judge.myenum.CommitStatusEnum;
import oj.oj_codesandbox.judge.strategy.JudgeStrategy;
import oj.oj_codesandbox.model.dto.Question;
import oj.oj_codesandbox.utils.ErrorClassifier;

import java.util.List;

public class DefaultJudgeStrategy implements JudgeStrategy {
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();
        Long memory = judgeInfo.getMemory();
        Long time = judgeInfo.getTime();
        boolean[] correct = judgeInfo.getCorrect();
        Question question = judgeContext.getQuestion();
        long exitCode = judgeContext.getExitCode();

        JudgeInfo judgeInfoResponse = new JudgeInfo();
        judgeInfoResponse.setMemory(memory);
        judgeInfoResponse.setTime(time);

        // 判断代码是否出现错误
        List<String> errorMessages = judgeInfo.getErrorMessages();
        // 编译错误 / 运行错误
        if (exitCode != 0) {
            String s = errorMessages.get(0);
            if (ErrorClassifier.classifyError(s, exitCode).equals("COMPILE_ERROR")) {
                judgeInfoResponse.setCnMessage(CommitStatusEnum.COMPILE_ERROR.getCnMessage());
                judgeInfoResponse.setEnMessage(errorMessages.get(0));

            } else if (ErrorClassifier.classifyError(s, exitCode).equals("RUNTIME_ERROR")) {
                judgeInfoResponse.setCnMessage(CommitStatusEnum.RUNTIME_ERROR.getCnMessage());
                judgeInfoResponse.setEnMessage(errorMessages.get(0));
            } else {
                judgeInfoResponse.setCnMessage(CommitStatusEnum.NON_ZERO_ERROR.getCnMessage());
                judgeInfoResponse.setEnMessage(errorMessages.get(0));
            }
            judgeInfoResponse.setExitCode(exitCode);
            return judgeInfoResponse;
        }

        // 具体的用例错误信息
        if (!errorMessages.isEmpty()) {
            judgeInfoResponse.setErrorMessages(errorMessages);
        }

        // correct是否全对
        for (boolean c : correct) {
            if (!c) {
                judgeInfoResponse.setCnMessage(CommitStatusEnum.WRONG_ANSWER.getCnMessage());
                judgeInfoResponse.setEnMessage(CommitStatusEnum.WRONG_ANSWER.getEnMessage());
                return judgeInfoResponse;
            }
        }

        long memoryLimit = question.getMemoryLimit();
        if (memory > memoryLimit) {
            judgeInfoResponse.setCnMessage(CommitStatusEnum.MEMORY_LIMIT_EXCEEDED.getCnMessage());
            judgeInfoResponse.setEnMessage(CommitStatusEnum.MEMORY_LIMIT_EXCEEDED.getEnMessage());
            return judgeInfoResponse;
        }

        long timeLimit = question.getTimeLimit();
        if (time > timeLimit) {
            judgeInfoResponse.setCnMessage(CommitStatusEnum.TIME_OUT.getCnMessage());
            judgeInfoResponse.setEnMessage(CommitStatusEnum.TIME_OUT.getEnMessage());
            return judgeInfoResponse;
        }

        judgeInfoResponse.setCnMessage(CommitStatusEnum.ACCEPTED.getCnMessage());
        judgeInfoResponse.setEnMessage(CommitStatusEnum.ACCEPTED.getEnMessage());
        return judgeInfoResponse;
    }


    public JudgeInfo doTest(JudgeContext judgeContext) {
        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();
        Long memory = judgeInfo.getMemory();
        Long time = judgeInfo.getTime();
        Question question = judgeContext.getQuestion();

        JudgeInfo judgeInfoResponse = new JudgeInfo();
        judgeInfoResponse.setMemory(memory);
        judgeInfoResponse.setTime(time);

        // 判断代码是否出现错误
        List<String> errorMessages = judgeInfo.getErrorMessages();
        if (!errorMessages.isEmpty() && errorMessages.get(0).contains("非零异常")) {
            judgeInfoResponse.setCnMessage(errorMessages.get(0));
            judgeInfoResponse.setEnMessage(errorMessages.get(0));
            judgeInfoResponse.setErrorMessages(errorMessages);

            return judgeInfoResponse;
        }

        long memoryLimit = question.getMemoryLimit();
        if (memory > memoryLimit) {
            judgeInfoResponse.setCnMessage(CommitStatusEnum.MEMORY_LIMIT_EXCEEDED.getCnMessage());
            judgeInfoResponse.setEnMessage(CommitStatusEnum.MEMORY_LIMIT_EXCEEDED.getEnMessage());
            return judgeInfoResponse;
        }

        long timeLimit = question.getTimeLimit();
        if (time > timeLimit) {
            judgeInfoResponse.setCnMessage(CommitStatusEnum.TIME_OUT.getCnMessage());
            judgeInfoResponse.setEnMessage(CommitStatusEnum.TIME_OUT.getEnMessage());
            return judgeInfoResponse;
        }


        return judgeInfoResponse;
    }


}
