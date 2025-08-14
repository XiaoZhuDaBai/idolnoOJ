package oj.oj_codesandbox.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import oj.oj_codesandbox.mapper.UserCommitMapper;
import oj.oj_codesandbox.model.dto.Question;
import oj.oj_codesandbox.model.dto.UserCommit;
import oj.oj_codesandbox.service.UserCommitService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserCommitServiceImpl extends ServiceImpl<UserCommitMapper, UserCommit>
        implements UserCommitService {

    @Resource
    private UserCommitMapper userCommitMapper;


    @Override
    public int insert(UserCommit userCommit) {
        return userCommitMapper.insert(userCommit);
    }

    @Override
    public UserCommit getByCommitId(String commitId) {
        QueryWrapper<UserCommit> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("commit_id", commitId);
        return userCommitMapper.selectOne(queryWrapper);
    }
}