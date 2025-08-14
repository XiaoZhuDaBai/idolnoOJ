//package oj.oj_codesandbox.deprecated;
//
//import com.github.dockerjava.api.DockerClient;
//import com.github.dockerjava.api.async.ResultCallback;
//import com.github.dockerjava.api.command.*;
//import com.github.dockerjava.api.model.*;
//import oj.oj_codesandbox.deprecated.JavaCodeSandboxTemplate;
//import oj.oj_codesandbox.model.ExecuteCodeResponse;
//import oj.oj_codesandbox.model.ExecuteMessage;
//import oj.oj_codesandbox.judge.entity.JudgeInfo;
//import oj.oj_codesandbox.utils.DockerUtils;
//import org.springframework.util.StopWatch;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;
//import java.util.stream.Stream;
//
///**
// *  单镜像多容器
// */
//public class JavaDockerCodeSandboxPlus extends JavaCodeSandboxTemplate {
//
//    private static final String USER_DIR  = System.getProperty("user.dir");
//    private static final String javaCmd = "java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main < %s";
//    private static final String[] CMD = {"sh", "-c", ""};
//    private static final long TIME_OUT = 10000L;
//    @Override
//    public List<ExecuteMessage> runFile(File userCodeFile, String problemId) {
//        String userCodeDir = userCodeFile.getParentFile().getAbsolutePath();
//        String imageName = "openjdk:17-jdk-alpine";
//        // 获取Docker Client
//        DockerClient dockerClient = DockerUtils.createDockerClient();
//        boolean imageExists = DockerUtils.imageExists(dockerClient, imageName);
//        if (!imageExists) {
//            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(imageName);
//            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
//                @Override
//                public void onNext(PullResponseItem item) {
//                    String status = item.getStatus();
//                    String progress = item.getProgress();
//                    System.out.printf("[DOCKER PULL] Status: %s, Progress: %s, ID: %s%n",
//                            status,
//                            progress,
//                            item.getId()
//                    );
//                    super.onNext(item);
//                }
//            };
//            try {
//                pullImageCmd
//                        .exec(pullImageResultCallback)
//                        .awaitCompletion();
//            } catch (InterruptedException e) {
//                System.out.println("拉取镜像异常");
//                throw new RuntimeException(e);
//            }
//        }
//
//        System.out.println(imageExists ? "镜像存在" : "下载完成");
//
//        // 执行命令
//        List<ExecuteMessage> executeMessages = new ArrayList<>();
//        String inputFileParentPath = Paths.get(USER_DIR, "Problems", problemId, "input").toString();
//        long inputFileCount = getDirChildFileCount(inputFileParentPath);
//
//        String MainPath = "/app" + File.separator + userCodeDir.substring(USER_DIR.length() + 1);
//        for (int i = 1; i <= inputFileCount; i++) {
//            // 构造执行命令
//            String newJavaCmd = String.format(javaCmd, MainPath, Paths.get("/app","Problems", problemId, "input", String.format("input%d.txt", i)));
//            CMD[CMD.length - 1] = newJavaCmd;
//
//            ExecuteMessage message = new ExecuteMessage();
//
//            // 获取容器
//            CreateContainerResponse response = DockerUtils.createResponsePlus(dockerClient, imageName, CMD, USER_DIR);
//            String containerId = response.getId();
//            System.out.printf("[CONTAINER CREATE] ID: %s, Image: %s%n",
//                    containerId,
//                    imageName
//            );
//
//            dockerClient.startContainerCmd(containerId).exec();
//            // 获取占用内存
//            Long[] maxMemoryUsage = {0L};
//            dockerClient.statsCmd(containerId).withNoStream(false).exec(new ResultCallback.Adapter<Statistics>() {
//                @Override
//                public void onNext(Statistics statistics) {
//                    // Docker stats API 在容器刚启动或刚结束时，部分字段（如 usage）可能为 null
//                    Long usage = null;
//                    if (statistics.getMemoryStats() != null) {
//                        usage = statistics.getMemoryStats().getUsage();
//                    }
//                    if (usage != null) {
//                        if (usage > maxMemoryUsage[0]) {
//                            maxMemoryUsage[0] = usage;
//                        }
//                        System.out.println("内存消耗：" + (usage / 1024 / 1024) + "MB");
//                    } else {
//                        System.out.println("内存消耗：未知");
//                    }
//
//                    if (statistics.getCpuStats() != null && statistics.getCpuStats().getCpuUsage() != null) {
//                        System.out.println("cpu：" + statistics.getCpuStats().getCpuUsage());
//                    } else {
//                        System.out.println("cpu：未知");
//                    }
//                    super.onNext(statistics);
//                }
//            });
//
//
//            try {
//                // 启动容器并计时
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
//                } finally {
//                    stopWatch.stop();
//                    message.setMemory(maxMemoryUsage[0] / 1024 / 1024);
//                    message.setTime(stopWatch.getLastTaskTimeMillis());
//                    System.out.println("执行时间：" + message.getTime() + " ms");
//                    System.out.println("占用内存：" + message.getMemory() + " MB");
//                }
//
//                //  收集容器状态
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
//                String result = output.toString().replaceAll("\r", "");
//                message.setMessage(result);
//                System.out.println(result);
//
//                // 检验结果
//                String answerFilePath = Paths.get(USER_DIR, "Problems", problemId, "answer", "answer" + i + ".txt").toString();
//                boolean isCorrect = check(result, answerFilePath);
//                message.setCorrect(isCorrect);
//
//
//            } catch (Exception e) {
//                message.setExitValue(-1L);
//                message.setErrorMessage("容器异常: "+e.getMessage());
//            } finally {
//
//                // 10. 清理容器
//                dockerClient.removeContainerCmd(containerId)
//                        .withForce(true)
//                        .exec();
//
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
