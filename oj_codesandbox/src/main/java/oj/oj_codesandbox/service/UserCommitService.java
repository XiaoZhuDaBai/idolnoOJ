package oj.oj_codesandbox.service;


import oj.oj_codesandbox.model.dto.UserCommit;

public interface UserCommitService {
    int insert(UserCommit userCommit);
    UserCommit getByCommitId(String commitId);
}
