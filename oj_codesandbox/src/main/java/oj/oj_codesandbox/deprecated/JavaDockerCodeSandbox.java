//package oj.oj_codesandbox.deprecated;
//
//import com.github.dockerjava.api.DockerClient;
//import com.github.dockerjava.api.async.ResultCallback;
//import com.github.dockerjava.api.command.*;
//import com.github.dockerjava.api.exception.NotFoundException;
//import com.github.dockerjava.api.model.*;
//import oj.oj_codesandbox.model.ExecuteCodeResponse;
//import oj.oj_codesandbox.model.ExecuteMessage;
//import oj.oj_codesandbox.judge.entity.JudgeInfo;
//import oj.oj_codesandbox.utils.DockerUtils;
//import org.springframework.util.StopWatch;
//
//import java.io.*;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;
//import java.util.stream.Stream;
//
///**
// * 使用 Dockerfile 执行命令
// *  单镜像单容器，存在镜像无法及时删除，开销比较大
// */
//public class JavaDockerCodeSandbox extends JavaCodeSandboxTemplate {
//
//    private static final String USER_DIR  = System.getProperty("user.dir");
//    private static final String javaCmd = "java -Xmx256m -Dfile.encoding=UTF-8 -cp /app Main < %s";
//    private static final String[] CMD = {"sh", "-c", ""};
//    private static final long TIME_OUT = 10000L;
//    @Override
//    public List<ExecuteMessage> runFile(File userCodeFile, String problemId) {
//
//        // 获取Docker Client
//        DockerClient dockerClient = DockerUtils.createDockerClient();
//
//        // 执行命令
//        List<ExecuteMessage> executeMessages = new ArrayList<>();
//        String inputFileParentPath = Paths.get(USER_DIR, "Problems", problemId).toString();
//        String userCodeDir = userCodeFile.getParentFile().getAbsolutePath().substring(USER_DIR.length() + 1);
//        long inputFileCount = getDirChildFileCount(inputFileParentPath + File.separator + "input");
//        for (int i = 1; i <= inputFileCount; i++) {
//            // 构造执行命令
//            String newJavaCmd = String.format(javaCmd, Paths.get("input", String.format("input%d.txt", i)));
//            CMD[CMD.length - 1] = newJavaCmd;
//
//            ExecuteMessage message = new ExecuteMessage();
//
//            // 获取容器
//            File dockerfile = createDockerfile(inputFileParentPath.substring(USER_DIR.length() + 1), userCodeDir);
//            System.out.println(dockerfile);
//            CreateContainerResponse response = DockerUtils.createResponse(dockerClient);
//            String containerId = response.getId();
//            try {
//                // 启动容器
//                Long[] maxMemoryUsage = {0L};
//                dockerClient.startContainerCmd(containerId).exec();
//                dockerClient.statsCmd(containerId).withNoStream(false).exec(new ResultCallback.Adapter<Statistics>() {
//                    @Override
//                    public void onNext(Statistics statistics) {
//                        // Docker stats API 在容器刚启动或刚结束时，部分字段（如 usage）可能为 null
//                        Long usage = null;
//                        if (statistics.getMemoryStats() != null) {
//                            usage = statistics.getMemoryStats().getUsage();
//                        }
//                        if (usage != null) {
//                            if (usage > maxMemoryUsage[0]) {
//                                maxMemoryUsage[0] = usage;
//                            }
//                            System.out.println("内存消耗：" + (usage / 1024 / 1024) + "MB");
//                        } else {
//                            System.out.println("内存消耗：未知");
//                        }
//
//                        if (statistics.getCpuStats() != null && statistics.getCpuStats().getCpuUsage() != null) {
//                            System.out.println("cpu：" + statistics.getCpuStats().getCpuUsage());
//                        } else {
//                            System.out.println("cpu：未知");
//                        }
//                        super.onNext(statistics);
//                    }
//                });
//
//                // 获取执行时间
//                StopWatch stopWatch = new StopWatch();
//                stopWatch.start();
//                // 异步等待容器退出（带超时）
//                CompletableFuture<Void> completion = CompletableFuture.runAsync(() -> {
//                    try {
//                        dockerClient.waitContainerCmd(containerId)
//                                .exec(new WaitContainerResultCallback())
//                                .awaitCompletion();
//                    } catch (InterruptedException e) {
//                        Thread.currentThread().interrupt();
//                    }
//                });
//                try {
//                    completion.get(TIME_OUT, TimeUnit.MILLISECONDS);
//                } catch (TimeoutException e) {
//                    dockerClient.killContainerCmd(containerId).exec();
//                    message.setExitValue(-1L);
//                    message.setErrorMessage("执行超时");
//                } catch (ExecutionException e) {
//                    throw new RuntimeException(e);
//                } finally {
//                    stopWatch.stop();
//                    message.setTime(stopWatch.getLastTaskTimeMillis());
//                    message.setMemory(maxMemoryUsage[0] / 1024 / 1024);
//                    System.out.println("执行时间：" + message.getTime() + " ms");
//                    System.out.println("占用内存：" + message.getMemory() + " MB");
//                }
//
//                // 记录容器退出码
//                try {
//                    InspectContainerResponse inspect = dockerClient.inspectContainerCmd(containerId).exec();
//                    if (inspect != null && inspect.getState() != null) {
//                        Long exitCode = inspect.getState().getExitCodeLong();
//                        message.setExitValue(exitCode);
//                        Boolean oomKilled = inspect.getState().getOOMKilled();
//                        String error = inspect.getState().getError();
//                        String status = inspect.getState().getStatus();
//                        System.out.println("容器退出码: " + exitCode);
//                        System.out.println("OOMKilled: " + oomKilled);
//                        System.out.println("Error: " + error);
//                        System.out.println("Status: " + status);
//                    } else {
//                        message.setExitValue(-1L);
//                        message.setErrorMessage("无法获取容器状态");
//                    }
//                } catch (Exception e) {
//                    message.setExitValue(-1L);
//                    message.setErrorMessage("inspectContainerCmd异常: " + e.getMessage());
//                    throw new RuntimeException("容器异常退出");
//                }
//
//                // 获取执行结果
//                StringBuilder output = new StringBuilder();
//                dockerClient.logContainerCmd(containerId)
//                        .withStdOut(true)
//                        .withStdErr(true)
//                        .withFollowStream(false)
//                        .exec(new ResultCallback.Adapter<Frame>() {
//                            @Override
//                            public void onNext(Frame frame) {
//                                output.append(new String(frame.getPayload()));
//                            }
//                        }).awaitCompletion();
//                String result = output.toString();
//                message.setMessage(result);
//                System.out.println(result);
//
//                // 检验结果
//                String answerFilePath = Paths.get(USER_DIR, "Problems", problemId, "answer", "answer" + i + ".txt").toString();
//                boolean isCorrect = check(result, answerFilePath);
//                message.setCorrect(isCorrect);
//
//
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            } finally {
//                // todo 清理镜像容器
//                try {
//                    dockerClient.removeContainerCmd(containerId)
//                            .withForce(true)
//                            .exec();
//                } catch (NotFoundException ignored) {
//
//                }
//                executeMessages.add(message);
//            }
//        }
//
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
//    private static File createDockerfile (String problemId, String Main) {
//        //todo 只用在oj下的那些文件
//        File dockerfile = new File("Dockerfile");
//        try {
//            // 确保文件不存在时再创建
//            if (dockerfile.exists()) {
//                Files.delete(dockerfile.toPath());
//            }
//            Files.createFile(dockerfile.toPath());
//            try (BufferedWriter writer = new BufferedWriter(new FileWriter(dockerfile))) {
//                writer.write("FROM openjdk:17-jdk-alpine");
//                writer.newLine();
//
//                writer.write("WORKDIR /app");
//                writer.newLine();
//
//                // 确保只复制指定的文件
//                writer.write("COPY " + problemId + " /app");
//                writer.newLine();
//                writer.write("COPY " + Main + File.separator + "Main.class" + " /app");
//                writer.newLine();
//                // 确保 CMD 参数正确传入（避免数组越界）
//                if (CMD == null) {
//                    throw new IllegalArgumentException("CMD cannot be null or empty");
//                }
//                // 用 StringBuilder 拼接 CMD
//                StringBuilder cmdBuilder = new StringBuilder("CMD [");
//                for (int i = 0; i < CMD.length; i++) {
//                    if (i > 0) cmdBuilder.append(", ");
//                    cmdBuilder.append("\"").append(CMD[i]).append("\"");
//                }
//                cmdBuilder.append("]");
//                writer.write(cmdBuilder.toString());
//                writer.flush();
//                return dockerfile;
//            }
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to create Dockerfile: " + e.getMessage(), e);
//        }
//    }
//
//    static String problemId = "L0001";
//    @Override
//    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList) {
//        ExecuteCodeResponse response = new ExecuteCodeResponse();
//        List<String> outputList = new ArrayList<>();
//        List<String> errorMessages = new ArrayList<>(); // 收集所有错误信息
//        long maxTime = 0;
//        long maxMemory = 0;
//        for (int i = 0; i < executeMessageList.size(); i++) {
//            ExecuteMessage message = executeMessageList.get(i);
//            int caseNumber = i + 1;
//            // 检查进程执行是否失败（如超时、编译错误）
//            if (message.getExitValue() != 0) {
//                errorMessages.add(String.format("用例 %d 执行失败: %s", caseNumber, message.getErrorMessage()));
//                continue;
//            }
//
//            if (!message.isCorrect()) {
//                errorMessages.add(String.format("用例 %d 答案错误", caseNumber));
//            }
//            // 记录输出内容和最大耗时
//            try {
//                outputList.add(message.getMessage());
//                maxTime = Math.max(maxTime, message.getTime());
//                maxMemory = Math.max(maxMemory, message.getMemory());
//            } catch (Exception e) {
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
//        judgeInfo.setMemory(maxMemory);
//        response.setJudgeInfo(judgeInfo);
//        response.setOutputList(outputList);
//        return response;
//    }
//
//    private boolean check(String outputStr, String answerFilePath) {
//        try {
//            List<String> output = List.of(outputStr.split("\n"));
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
//}
