package oj.oj_codesandbox.judge.entity;

import lombok.Builder;
import lombok.Data;
import oj.oj_codesandbox.model.dto.Question;
import oj.oj_codesandbox.model.dto.UserCommit;

import java.util.List;

@Data
@Builder
public class JudgeContext {
    private String language;
    private JudgeInfo judgeInfo;
    private List<String> inputList;
    private List<String> outputList;
    private Question question;
    private List<JudgeCase> judgeCaseList;
    private UserCommit userCommit;
    private long exitCode;
}