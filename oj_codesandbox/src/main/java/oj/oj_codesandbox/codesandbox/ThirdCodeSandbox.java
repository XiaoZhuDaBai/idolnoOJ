package oj.oj_codesandbox.codesandbox;


import oj.oj_codesandbox.CodeSandbox;
import oj.oj_codesandbox.comm.ResponseResult;
import oj.oj_codesandbox.model.ExecuteCodeRequest;
import oj.oj_codesandbox.model.ExecuteCodeResponse;

public class ThirdCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return null;
    }

    @Override
    public ExecuteCodeResponse userTestCode(ExecuteCodeRequest executeCodeRequest) {
        return null;
    }
}