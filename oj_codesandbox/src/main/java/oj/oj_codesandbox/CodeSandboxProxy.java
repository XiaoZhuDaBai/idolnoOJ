package oj.oj_codesandbox;

import lombok.extern.slf4j.Slf4j;
import oj.oj_codesandbox.comm.ResponseResult;
import oj.oj_codesandbox.model.ExecuteCodeRequest;
import oj.oj_codesandbox.model.ExecuteCodeResponse;

/**
 * 代理模式优化
 *
 */
@Slf4j
public class CodeSandboxProxy implements CodeSandbox{
    private final CodeSandbox codeSandbox;

    public CodeSandboxProxy(CodeSandbox codeSandbox) {
        this.codeSandbox = codeSandbox;
    }
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        log.info("提交-代码沙箱请求信息：" + executeCodeRequest.toString());
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        log.info("提交-代码沙箱响应信息：" + executeCodeResponse.toString());
        return executeCodeResponse;
    }

    @Override
    public ExecuteCodeResponse userTestCode(ExecuteCodeRequest executeCodeRequest) {
        log.info("用户自测-代码沙箱请求信息：" + executeCodeRequest.toString());
        ExecuteCodeResponse executeCodeResponse = codeSandbox.userTestCode(executeCodeRequest);
        log.info("用户自测-代码沙箱响应信息：" + executeCodeResponse.toString());
        return executeCodeResponse;
    }
}
