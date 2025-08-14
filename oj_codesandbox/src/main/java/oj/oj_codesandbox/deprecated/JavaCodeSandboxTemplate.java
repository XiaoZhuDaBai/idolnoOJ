//package oj.oj_codesandbox.deprecated;
//
//import cn.hutool.core.io.FileUtil;
//import cn.hutool.core.lang.UUID;
//import lombok.extern.slf4j.Slf4j;
//import oj.oj_codesandbox.CodeSandbox;
//import oj.oj_codesandbox.model.ExecuteCodeRequest;
//import oj.oj_codesandbox.model.ExecuteCodeResponse;
//import oj.oj_codesandbox.model.ExecuteMessage;
//import oj.oj_codesandbox.judge.entity.JudgeInfo;
//import oj.oj_codesandbox.utils.ProcessUtils;
//import org.springframework.stereotype.Service;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Stream;
//
//@Slf4j
//@Service
//public class JavaCodeSandboxTemplate implements CodeSandbox {
//    private static final String GLOBAL_CODE_DIR_NAME = "UserCode";
//    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";
//    private static final int MAX_RUN_TIME = 10000; // 单位毫秒
//    private static final String USER_DIR  = System.getProperty("user.dir");
//    /**
//     * 保存代码到文件
//     * @param code
//     * @return
//     */
//    public File saveCodeToFile(String code) {
//        // 使用 File.separator 防止系统地址符号不兼容
//        String globalCodeDirPath = USER_DIR + File.separator + GLOBAL_CODE_DIR_NAME;
//        // 创建目录
//        if (!FileUtil.exist(globalCodeDirPath)) {
//            FileUtil.mkdir(globalCodeDirPath);
//        }
//
//        String userCodeFilePath = globalCodeDirPath + File.separator + UUID.randomUUID();
//        String userCodePath = userCodeFilePath + File.separator + GLOBAL_JAVA_CLASS_NAME;
//        // 保存代码到文件
//        File userCodeFile = FileUtil.writeUtf8String(code, userCodePath);
//        return userCodeFile;
//    }
//
//    /**
//     * 编译代码文件
//     * @param userCodeFile
//     * @return
//     */
//    public ExecuteMessage compileFile(File userCodeFile) {
//        String compileCommand = String.format("javac -encoding UTF-8 %s", userCodeFile.getAbsolutePath());
//        try {
//            Process compileProcess = Runtime.getRuntime().exec(compileCommand);
//            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
//            System.out.println(executeMessage);
//            if (executeMessage.getExitValue() != 0) {
//                throw new RuntimeException("编译错误");
//            }
//            return executeMessage;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    /**
//     * 运行代码文件
//     * @param userCodeFile
//     * @return
//     */
//    public List<ExecuteMessage> runFile(File userCodeFile, String problemId) {
//        String userCodeDir = userCodeFile.getParentFile().getAbsolutePath();
//        String inputFileParentPath = Paths.get(USER_DIR, "Problems", problemId, "input").toString();
//        long inputFileCount = getDirChildFileCount(inputFileParentPath);
//
//        List<ExecuteMessage> executeMessages = new ArrayList<>();
//        for (int i = 1; i <= inputFileCount; i++) {
//            // 动态生成输入和输出文件路径，避免覆盖
//            String inputFilePath = Paths.get(inputFileParentPath, "input" + i + ".txt").toString();
//            String outputFilePath = Paths.get(userCodeDir, "output_" + i + ".txt").toString();
//            // 构造执行命令
//            String[] command = {
//                    "java",
//                    "-Xmx256m",
//                    "-Dfile.encoding=UTF-8",
//                    "-cp", userCodeDir,
//                    "Main"
//            };
//            // 调用封装方法执行进程
//            ExecuteMessage result =  ProcessUtils.runProcessWithProcessBuilder(
//                    command,
//                    inputFilePath,
//                    outputFilePath,
//                    MAX_RUN_TIME
//            );
//            result.setOutputFilePath(outputFilePath);
//            executeMessages.add(result);
//        }
//        return executeMessages;
//    }
//
//    private long getDirChildFileCount(String inputFileParentPath) {
//        Path path = Paths.get(inputFileParentPath);
//        try (Stream<Path> walk = Files.walk(path)) {
//            return walk.filter(Files::isRegularFile)
//                    .count();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    //todo 完善一下
//    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList) {
//        ExecuteCodeResponse response = new ExecuteCodeResponse();
//        List<String> outputList = new ArrayList<>();
//        List<String> errorMessages = new ArrayList<>(); // 收集所有错误信息
//        long maxTime = 0;
//        String problemId = "L0001"; // 可根据实际需求动态获取
//        for (int i = 0; i < executeMessageList.size(); i++) {
//            ExecuteMessage message = executeMessageList.get(i);
//            int caseNumber = i + 1;
//            // 检查进程执行是否失败（如超时、编译错误）
//            if (message.getExitValue() == null || message.getExitValue() != 0) {
//                errorMessages.add(String.format("用例 %d 执行失败: %s", caseNumber, message.getErrorMessage()));
//                continue;
//            }
//            // 获取输出文件路径
//            String outputFilePath = message.getOutputFilePath(); // 假设 ExecuteMessage 中存储了输出文件路径
//            String answerFilePath = Paths.get(USER_DIR, "Problems", problemId, "answer", "answer" + caseNumber + ".txt").toString();
//            // 验证输出与答案是否一致
//            boolean isCorrect = check(outputFilePath, answerFilePath);
//            if (!isCorrect) {
//                errorMessages.add(String.format("用例 %d 答案错误", caseNumber));
//            }
//            // 记录输出内容和最大耗时
//            try {
//                String outputContent = Files.readString(Paths.get(outputFilePath));
//                outputList.add(outputContent);
//                maxTime = Math.max(maxTime, message.getTime());
//            } catch (IOException e) {
//                errorMessages.add(String.format("用例 %d 读取输出文件失败: %s", caseNumber, e.getMessage()));
//            }
//        }
//        // 设置最终状态和错误信息
//        if (!errorMessages.isEmpty()) {
//            response.setStatus("Error");
//            response.setMessage(String.join("; ", errorMessages));
//        } else {
//            response.setStatus("SUCCESS");
//        }
//        // 封装判题信息
//        JudgeInfo judgeInfo = new JudgeInfo();
//        judgeInfo.setTime(maxTime);
//        response.setJudgeInfo(judgeInfo);
//        response.setOutputList(outputList);
//        return response;
//    }
//    private boolean check(String outputFilePath, String answerFilePath) {
//        try {
//            List<String> output = Files.readAllLines(Paths.get(outputFilePath));
//            List<String> answer = Files.readAllLines(Paths.get(answerFilePath));
//            if (output.size() != answer.size()) {
//                return false;
//            }
//            for (int i = 0; i < output.size(); i++) {
//                if (!output.get(i).equals(answer.get(i))) {
//                    return false;
//                }
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return true;
//    }
//
//
//    public boolean deleteFile(File userCodeFile) {
//        if (userCodeFile.getParentFile() != null) {
//            String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
//            final boolean del = FileUtil.del(userCodeParentPath);
//            System.out.println("删除" + (del ? "成功" : "失败"));
//            return del;
//        }
//        return true;
//    }
//
//    /**
//     *  执行代码完整流程
//     * @param executeCodeRequest
//     * @return
//     */
//    @Override
//    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
//        // 保存代码
//        File userCodeFile = saveCodeToFile(executeCodeRequest.getCode());
//
//        // 编译
//        ExecuteMessage compileMessage = compileFile(userCodeFile);
//        System.out.println(compileMessage);
//
//        //todo 执行
//        List<ExecuteMessage> executeMessageList = null;
//        try {
//            executeMessageList = runFile(userCodeFile, "L0001");
//        } catch (Exception e) {
//            System.out.println("运行文件出错");
//            throw new RuntimeException(e);
//        } finally {
//            boolean deletedFile = deleteFile(userCodeFile);
//            if (!deletedFile) {
//                log.error("删除文件失败");
//            }
//        }
//
//        // 返回运行结果
//        return getOutputResponse(executeMessageList);
//    }
//
//
//}
