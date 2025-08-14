package oj.oj_codesandbox;


import oj.oj_codesandbox.comm.ResponseResult;
import oj.oj_codesandbox.model.ExecuteCodeRequest;
import oj.oj_codesandbox.model.ExecuteCodeResponse;

public interface CodeSandbox {
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
    ExecuteCodeResponse userTestCode(ExecuteCodeRequest executeCodeRequest);
}
