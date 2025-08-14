package oj.oj_codesandbox.service;

import com.baomidou.mybatisplus.extension.service.IService;

import oj.oj_codesandbox.model.dto.Question;
import org.apache.ibatis.annotations.Param;


public interface QuestionService extends IService<Question> {
    Question getOneQuestion(String id);
    int updateCommitCountById(String problemId);
    int updateAcCountById(String problemId);

}