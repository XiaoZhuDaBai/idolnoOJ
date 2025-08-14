package oj.oj_codesandbox.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

import oj.oj_codesandbox.mapper.QuestionMapper;
import org.springframework.stereotype.Service;
import oj.oj_codesandbox.model.dto.Question;
import oj.oj_codesandbox.service.QuestionService;
import javax.annotation.Resource;

@Service
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
        implements QuestionService {
    @Resource
    private QuestionMapper questionMapper;

    @Override
    public Question getOneQuestion(String id) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("question_id", id);
        return questionMapper.selectOne(queryWrapper);
    }

    @Override
    public int updateCommitCountById(String problemId) {
        return questionMapper.updateCommitCountById(problemId);
    }

    @Override
    public int updateAcCountById(String problemId) {
        return questionMapper.updateAcCountById(problemId);
    }


}
