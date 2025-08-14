package oj.oj_codesandbox.codesandbox;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import oj.oj_codesandbox.CodeSandbox;
import oj.oj_codesandbox.CodeSandboxTemplate;
import oj.oj_codesandbox.docker.CppDockerCodeSandbox;
import oj.oj_codesandbox.docker.JavaDockerCodeSandbox;
import oj.oj_codesandbox.model.ExecuteCodeRequest;
import oj.oj_codesandbox.model.ExecuteCodeResponse;
import oj.oj_codesandbox.model.ExecuteMessage;

import java.io.File;
import java.util.List;


/**
 * 远程代码沙箱
 */
@Slf4j
public class RemoteCodeSandbox implements CodeSandbox {

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        String problemId = executeCodeRequest.getProblemId();

        String codeFileName = "Main.";
        String compileCmd = "";
        String imageName = "";
        String languageCmd = "";
        CodeSandboxTemplate codeSandboxTemplate;
        switch (language) {
            case "java":
                codeSandboxTemplate = new JavaDockerCodeSandbox();
                imageName = "openjdk:17-jdk";
                compileCmd = "javac -encoding UTF-8 %s";
                languageCmd = "java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main < %s";
                break;
            case "cpp":
                codeSandboxTemplate = new CppDockerCodeSandbox();

                break;
            default:
                throw new RuntimeException("没有实现这个语言");
        }
        codeFileName += language;

        // 保存代码
        File userCodeFile = codeSandboxTemplate.saveCodeToFile(code, codeFileName);

        // 开始 编译和执行
        List<ExecuteMessage> executeMessageList = null;
        try {
            executeMessageList = codeSandboxTemplate.compileAndRunFile(userCodeFile, problemId, imageName, compileCmd,languageCmd, codeFileName);
        } catch (Exception e) {
            System.out.println("运行文件出错");
            throw new RuntimeException(e);
        } finally {
            boolean deletedFile = deleteFile(userCodeFile);
            if (!deletedFile) {
                log.error("删除文件失败");
            }
        }

        // 返回运行结果
        return codeSandboxTemplate.getOutputResponse(executeMessageList);
    }

    @Override
    public ExecuteCodeResponse userTestCode(ExecuteCodeRequest executeCodeRequest) {
        String code = executeCodeRequest.getCode();
        String input = executeCodeRequest.getUserInput();
        String language = executeCodeRequest.getLanguage();

        String codeFileName = "Main.";
        String compileCmd = "";
        String imageName = "";
        String languageCmd = "";
        CodeSandboxTemplate codeSandboxTemplate = null;
        boolean needCompile = true;
        switch (language) {
            case "java":
                codeSandboxTemplate = new JavaDockerCodeSandbox();
                imageName = "openjdk:17-jdk";
                compileCmd = "javac -encoding UTF-8 %s";
                languageCmd = "java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main < %s";
                break;
            case "cpp":
                codeSandboxTemplate = new CppDockerCodeSandbox();

                break;
            case "javascript":
                needCompile = false;
                break;

            case "python":
                needCompile = false;
                break;
            default:
                throw new RuntimeException("没有实现这个语言");
        }
        codeFileName += language;


        if (codeSandboxTemplate == null) {
            throw new RuntimeException("沙箱非法操作");
        }
        // 保存代码和输入
        File userCodeFile = codeSandboxTemplate.saveCodeToFile(code, codeFileName);
        File userInputFile = codeSandboxTemplate.saveCodeToFile(input, "input.txt");

        // 开始 编译和执行
        List<ExecuteMessage> executeMessageList = null;
        try {
            executeMessageList = needCompile ? codeSandboxTemplate.compileAndRunFileWithInput(userCodeFile, userInputFile, imageName, compileCmd, languageCmd, codeFileName)
                        :
                    codeSandboxTemplate.runFileWithInput(userCodeFile, userInputFile, imageName,languageCmd);

        } catch (Exception e) {
            System.out.println("运行文件出错");
            throw new RuntimeException(e);
        } finally {
            boolean codeFile = deleteFile(userCodeFile);
            boolean inputFile = deleteFile(userInputFile);
            if (!(codeFile && inputFile)) {
                log.error("删除文件失败");
            }
        }

        // 返回运行结果
        return codeSandboxTemplate.getOutputResponse(executeMessageList);
    }

    public boolean deleteFile(File userCodeFile) {
        if (userCodeFile.getParentFile() != null) {
            String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
            final boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
            return del;
        }
        return true;
    }
}
