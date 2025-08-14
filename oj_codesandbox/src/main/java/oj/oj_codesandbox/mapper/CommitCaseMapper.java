package oj.oj_codesandbox.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import oj.oj_codesandbox.model.dto.CommitCase;
import oj.oj_codesandbox.model.dto.UserCommit;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author LJ_Wang
 * @description 针对表【commit_case】的数据库操作Mapper
 * @createDate 2025-04-24 09:17:01
 * @Entity generator.entity.CommitCase
 */
@Mapper
public interface CommitCaseMapper extends BaseMapper<CommitCase> {
    int insert(CommitCase commitCase);
}