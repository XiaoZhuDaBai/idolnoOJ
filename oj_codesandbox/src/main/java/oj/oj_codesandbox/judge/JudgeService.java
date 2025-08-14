package oj.oj_codesandbox.judge;


import oj.oj_codesandbox.comm.ResponseResult;
import oj.oj_codesandbox.judge.entity.QuestionSubmitResult;
import oj.oj_codesandbox.model.ExecuteCodeRequest;
import oj.oj_codesandbox.model.vo.CommitResultVo;

public interface JudgeService {
    ResponseResult<CommitResultVo> doJudge(ExecuteCodeRequest executeCodeRequest);
    ResponseResult<CommitResultVo> getJudgeResult(String commitId);
    ResponseResult<CommitResultVo> userTest(ExecuteCodeRequest executeCodeRequest);
}