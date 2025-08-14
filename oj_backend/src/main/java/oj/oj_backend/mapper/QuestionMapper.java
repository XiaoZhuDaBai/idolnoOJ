package oj.oj_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import oj.oj_backend.model.Question;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface QuestionMapper extends BaseMapper<Question> {
    int insert(Question question);
    Long getQuestionsCountByTag(@Param("platform")String platform, @Param("difficulty")String difficulty,
                               @Param("resource")String resource, @Param("tags")String... tags);
    List<Question> getQuestionsByTag(@Param("platform")String platform, @Param("difficulty")String difficulty,
                                     @Param("resource")String resource, @Param("page")int page, @Param("size") int size, @Param("tags")String... tags);

    int updateCommitCountById(@Param("questionId") String questionId);
}
