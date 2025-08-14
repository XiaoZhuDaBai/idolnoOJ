package oj.oj_codesandbox.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import oj.oj_codesandbox.mapper.CommitCaseMapper;
import oj.oj_codesandbox.model.dto.CommitCase;
import oj.oj_codesandbox.service.CommitCaseService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CommitCaseServiceImpl extends ServiceImpl<CommitCaseMapper, CommitCase>
        implements CommitCaseService {

    @Resource
    private  CommitCaseMapper commitCaseMapper;

    @Override
    public CommitCase getByCommitId(String commitId) {
        QueryWrapper<CommitCase> queryWrapper = new QueryWrapper<CommitCase>();
        queryWrapper.eq("commit_id", commitId);
        return commitCaseMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean updateById(CommitCase commitCase) {
        UpdateWrapper<CommitCase> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("commit_id", commitCase.getCommitId());
        int update = commitCaseMapper.update(commitCase, updateWrapper);
        return update > 0;
    }

    @Override
    public int insert(CommitCase commitCase) {
        return commitCaseMapper.insert(commitCase);
    }
}