package oj.oj_codesandbox.docker;

import com.github.dockerjava.api.DockerClient;
import oj.oj_codesandbox.CodeSandboxTemplate;
import oj.oj_codesandbox.comm.ResponseResult;
import oj.oj_codesandbox.model.ExecuteCodeResponse;
import oj.oj_codesandbox.model.ExecuteMessage;

import java.io.File;
import java.util.List;

public class JavaDockerCodeSandbox extends CodeSandboxTemplate {
    @Override
    public File saveCodeToFile(String code, String globalCodeDirName) {
        return super.saveCodeToFile(code, globalCodeDirName);
    }

    @Override
    public ResponseResult<ExecuteMessage> compileFile(File userCodeFile, String compileCommand) {
        return super.compileFile(userCodeFile, compileCommand);
    }

    @Override
    public List<ExecuteMessage> runFile(File userCodeFile, String problemId, String imageName, String languageCmd) {
        return super.runFile(userCodeFile, problemId, imageName, languageCmd);
    }

    @Override
    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList) {
        return super.getOutputResponse(executeMessageList);
    }

    @Override
    public void PullDockerImage(DockerClient dockerClient, String imageName) {
        super.PullDockerImage(dockerClient, imageName);
    }


}