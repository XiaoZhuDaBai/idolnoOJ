package oj.oj_codesandbox.service;

import com.baomidou.mybatisplus.extension.service.IService;
import oj.oj_codesandbox.model.dto.CommitCase;


/**
 * @author LJ_Wang
 * @description 针对表【commit_case】的数据库操作Service
 * @createDate 2025-04-24 09:17:01
 */
public interface CommitCaseService extends IService<CommitCase> {
    CommitCase getByCommitId(String commitId);
    boolean updateById(CommitCase commitCase);

    int insert(CommitCase commitCase) ;
}
