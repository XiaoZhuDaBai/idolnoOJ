package oj.oj_backend.controller;

import cn.hutool.json.JSONUtil;
import oj.oj_backend.model.Question;
import oj.oj_backend.model.vo.SearchQuestionVo;
import oj.oj_backend.service.QuestionService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/question")
public class QuestionController {
    private static final int PAGE_SIZE = 15;
    @Resource
    private QuestionService questionService;

    @GetMapping(value = "/data")
    public String getOneProblemById(@RequestParam String problemId) {
        return JSONUtil.toJsonStr(questionService.getOneQuestion(problemId));
    }

    @PostMapping(value = "/counts")
    public long getQuestionsCountByTag(@RequestBody SearchQuestionVo searchQuestionVo) {
        return questionService.getQuestionsCountByTag(
                searchQuestionVo.getPlatform(),
                searchQuestionVo.getDifficulty(),
                searchQuestionVo.getResource(),
                searchQuestionVo.getTags()
        );
    }

    @PostMapping(value = "/search")
    public String searchQuestions(@RequestBody SearchQuestionVo searchQuestionVo) {
        return JSONUtil.toJsonStr(questionService.getQuestionsByTag(
                searchQuestionVo.getPlatform(),
                searchQuestionVo.getDifficulty(),
                searchQuestionVo.getResource(),
                searchQuestionVo.getPage(),
                PAGE_SIZE,
                searchQuestionVo.getTags()
        ));
    }
}
