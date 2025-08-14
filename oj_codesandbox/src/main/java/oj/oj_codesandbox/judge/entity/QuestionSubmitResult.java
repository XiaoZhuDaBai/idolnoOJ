package oj.oj_codesandbox.judge.entity;

import lombok.Data;
import oj.oj_codesandbox.model.dto.CommitCase;
import oj.oj_codesandbox.model.dto.UserCommit;


@Data
public class QuestionSubmitResult {
    private CommitCase commitCase;
    private UserCommit userCommit;
}
