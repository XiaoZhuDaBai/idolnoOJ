package oj.oj_backend.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import oj.oj_backend.model.CommitCase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author LJ_Wang
* @description 针对表【commit_case】的数据库操作Mapper
* @createDate 2025-04-24 09:17:01
* @Entity generator.entity.CommitCase
*/
@Mapper
public interface CommitCaseMapper extends BaseMapper<CommitCase> {
    List<CommitCase> getAllCommitCaseByPage(@Param("page")int page, @Param("size")int size);
    List<CommitCase> getUserCommitCase(@Param("uid")String uid);
}




