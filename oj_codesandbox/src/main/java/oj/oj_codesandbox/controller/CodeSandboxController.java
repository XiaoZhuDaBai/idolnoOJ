package oj.oj_codesandbox.controller;

import oj.oj_codesandbox.comm.ResponseResult;
import oj.oj_codesandbox.judge.JudgeService;
import oj.oj_codesandbox.model.ExecuteCodeRequest;
import oj.oj_codesandbox.model.vo.CommitResultVo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/codesandbox/oj")
public class CodeSandboxController {
    @Resource
    private JudgeService judgeService;

    @PostMapping("/test")
    private ResponseResult<CommitResultVo> userTest(@RequestBody ExecuteCodeRequest executeCodeRequest) {
        return judgeService.userTest(executeCodeRequest);
    }

    @PostMapping("/submit")
    private ResponseResult<CommitResultVo> submit(@RequestBody ExecuteCodeRequest executeCodeRequest) {
        return judgeService.doJudge(executeCodeRequest);
    }

    // 前段轮询接口找到提交结果
    @GetMapping("/result/{commitId}")
    public ResponseResult<CommitResultVo> getJudgeResult(@PathVariable String commitId) {
        return judgeService.getJudgeResult(commitId);
    }
}
