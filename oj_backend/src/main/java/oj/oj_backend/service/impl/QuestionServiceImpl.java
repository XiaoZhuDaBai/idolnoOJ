package oj.oj_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import oj.oj_backend.mapper.QuestionMapper;
import oj.oj_backend.model.Question;
import oj.oj_backend.service.QuestionService;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
        implements QuestionService {
    @Resource
    private QuestionMapper questionMapper;

    @Override
    public int addBatch() {
        return 0;
    }

    @Override
    public int addOne(Question question) {
        return questionMapper.insert(question);
    }

    @Override
    public Question getOneQuestion(String problemId) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("question_id", problemId);
        return questionMapper.selectOne(queryWrapper);
    }

    @Override
    public List<Question> getQuestionsByTag(String platform, String difficulty, String resource, int page, int size, String... tags) {
        return questionMapper.getQuestionsByTag(platform, difficulty, resource, (page - 1) * size, size, tags);
    }

    @Override
    public Long getQuestionsCountByTag(String platform, String difficulty, String resource, String... tags) {
        return questionMapper.getQuestionsCountByTag(platform, difficulty, resource, tags);
    }

    @Override
    public int updateCommitCountById(String questionId) {
        return questionMapper.updateCommitCountById(questionId);
    }
}
