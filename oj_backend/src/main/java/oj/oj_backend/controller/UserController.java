package oj.oj_backend.controller;

import cn.hutool.json.JSONUtil;
import oj.oj_backend.comm.ResponseResult;
import oj.oj_backend.model.vo.LoginVo;
import oj.oj_backend.model.vo.SearchSubmissionVo;
import oj.oj_backend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;


    // 用redis存
    @PostMapping("/send-verify-code")
    public ResponseResult<String> sendVerificationCode(@RequestParam String email) {

        String verificationCode = userService.sendVerificationCode(email);

        return ResponseResult.success(200, verificationCode);
    }

    // 从redis读有效验证码
    @PostMapping("/login-or-register")
    public ResponseResult<String> loginOrRegister(@RequestBody LoginVo loginVo) {

        return userService.toLoginOrRegister(loginVo);
    }

    @GetMapping("/jwt")
    public ResponseResult<String> getJWT(@RequestParam String uuid) {
        String jwt = userService.getJWT(uuid);
        return ResponseResult.success(200, jwt);
    }

    // 个人一周的提交记录
    @GetMapping("/search-recent-commit")
    public String searchCommitCase(@RequestParam String uuid, @RequestParam int page) {
        return JSONUtil.toJsonStr(userService.getMyRecentSubmission(uuid, page)) ;
    }
    //todo
    @GetMapping("/search-recent-commit-count")
    public int searchCommitCaseCount(@RequestParam String uuid) {
        return userService.getMyRecentSubmissionCount(uuid);
    }

    // 个人一周未通过的题目记录
    @GetMapping("/search-wrong-commit")
    public String searchWrongCommitCase(@RequestParam String uuid) {
        return JSONUtil.toJsonStr(userService.getMyRecentWrongSubmission(uuid));
    }

    // 个人一周的答题总数和通过数记录
    @GetMapping("/search-my-commits-data")
    public String searchMyCommitsData(@RequestParam String uuid) {
        return JSONUtil.toJsonStr(userService.getMySubmissionData(uuid));
    }

    // 搜索提交记录
    @PostMapping("/search-commits")
    public String searchCommits(@RequestBody SearchSubmissionVo searchSubmissionVo) {
        return JSONUtil.toJsonStr(userService.getCommits(
                searchSubmissionVo.getProblemName(),
                searchSubmissionVo.getLanguage(),
                searchSubmissionVo.getStatus(),
                searchSubmissionVo.getUserType(),
                searchSubmissionVo.getUuid(),
                searchSubmissionVo.getPage()
        ));
    }
    //todo
    @PostMapping("/search-commits-count")
    public int searchCommitsCount(@RequestBody SearchSubmissionVo searchSubmissionVo) {
        return userService.getCommitsCount(
                searchSubmissionVo.getProblemName(),
                searchSubmissionVo.getLanguage(),
                searchSubmissionVo.getStatus(),
                searchSubmissionVo.getUserType(),
                searchSubmissionVo.getUuid()
        );
    }

}
