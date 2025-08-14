package oj.oj_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import oj.oj_backend.model.Question;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface QuestionService extends IService<Question> {
    int addBatch();
    int addOne(Question question);
    Question getOneQuestion(String id);
    List<Question> getQuestionsByTag(String platform, String difficulty, String resource, int page, int size, String... tags);
    Long getQuestionsCountByTag(String platform, String difficulty, String resource, String... tags);

    int updateCommitCountById(@Param("questionId") String questionId);
}
