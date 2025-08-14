package oj.oj_codesandbox.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import oj.oj_codesandbox.model.dto.Question;
import oj.oj_codesandbox.model.dto.UserCommit;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author LJ_Wang
 * @description 针对表【user_commit】的数据库操作Mapper
 * @createDate 2025-04-24 00:34:18
 * @Entity generator.entity.UserCommit
 */
@Mapper
public interface UserCommitMapper extends BaseMapper<UserCommit> {
    int insert(UserCommit userCommit);
}