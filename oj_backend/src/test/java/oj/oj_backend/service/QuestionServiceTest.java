package oj.oj_backend.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import oj.oj_backend.model.Question;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
public class QuestionServiceTest {
    @Resource
    QuestionService questionService;

    @Test
    public void test() {
//        Question question = new Question();
        String jsonArrStr = FileUtil.readUtf8String("D:\\code\\oj\\oj_backend\\leetcode_problems.json");
        List<Question> questions = JSONUtil.toList(jsonArrStr, Question.class);

        boolean saved = questionService.saveBatch(questions);

        System.out.println(saved);

//        boolean save = questionService.save(question);
//        System.out.println(save);
    }
}
