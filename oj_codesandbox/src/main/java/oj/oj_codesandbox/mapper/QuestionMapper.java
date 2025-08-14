package oj.oj_codesandbox.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import oj.oj_codesandbox.model.dto.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
    int insert(Question question);
    @Update("update question set question_commits = question_commits + 1 where question_id = #{problemId}")
    int updateCommitCountById(@Param("problemId") String problemId);
    @Update("update question set ac = ac + 1 where question_id = #{problemId}")
    int updateAcCountById(@Param("problemId") String problemId);

}
