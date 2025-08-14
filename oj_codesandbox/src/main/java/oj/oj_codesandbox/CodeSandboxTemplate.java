package oj.oj_codesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.Statistics;
import oj.oj_codesandbox.comm.ResponseResult;
import oj.oj_codesandbox.model.ExecuteCodeResponse;
import oj.oj_codesandbox.model.ExecuteMessage;
import oj.oj_codesandbox.judge.entity.JudgeInfo;
import oj.oj_codesandbox.utils.DockerUtils;
import oj.oj_codesandbox.utils.ProcessUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

/**
 *  代码沙箱模板
 */
public abstract class CodeSandboxTemplate{
    private static final Logger log = LoggerFactory.getLogger(CodeSandboxTemplate.class);
    private static final int MAX_RUN_TIME = 10000; // 单位毫秒
    private static final String USER_DIR  = System.getProperty("user.dir");
    private static final String GLOBAL_CODE_DIR_NAME = "UserCode";
    private static final String[] CMD = {"sh", "-c", ""};

    /**
     * 保存成文件
     * @param code
     * @param codeFileName
     * @return
     */
     public File saveCodeToFile(String code, String codeFileName) {
        // 使用 File.separator 防止系统地址符号不兼容
        String globalCodeDirPath = USER_DIR + File.separator + GLOBAL_CODE_DIR_NAME;
        // 创建目录
        if (!FileUtil.exist(globalCodeDirPath)) {
            FileUtil.mkdir(globalCodeDirPath);
        }

        String userCodeFilePath = globalCodeDirPath + File.separator + UUID.randomUUID();
        String userCodePath = userCodeFilePath + File.separator + codeFileName;
        // 保存代码到文件
         return FileUtil.writeUtf8String(code, userCodePath);
    }

    /**
     *  编译代码文件
     * @param userCodeFile
     * @return
     */
    @Deprecated
    public ResponseResult<ExecuteMessage> compileFile(File userCodeFile, String compileCommand) {
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCommand);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            log.info(executeMessage.toString());
            if (executeMessage.getExitValue() != 0) {
                return ResponseResult.fail(1,"编译失败", executeMessage);
            }
            return ResponseResult.success(0,"编译成功", executeMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解释型语言所用
     * @param userCodeFile
     * @param problemId
     * @param imageName
     * @param languageCmd
     * @return
     */
    public List<ExecuteMessage> runFile(File userCodeFile, String problemId, String imageName, String languageCmd) {
        String userCodeDir = userCodeFile.getParentFile().getAbsolutePath();
        // 获取Docker Client
        DockerClient dockerClient = DockerUtils.createDockerClient();
        // 拉取镜像
        this.PullDockerImage(dockerClient, imageName);

        // 执行命令
        List<ExecuteMessage> executeMessages = new ArrayList<>();
        String inputFileParentPath = Paths.get(USER_DIR, "Problems", problemId, "input").toString();
        long inputFileCount = getDirChildFileCount(inputFileParentPath);
        String MainPath = "/app" + File.separator + userCodeDir.substring(USER_DIR.length() + 1);

        for (int i = 1; i <= inputFileCount; i++) {
            // 构造执行命令
            String newLanguageCmd = String.format(languageCmd, MainPath, Paths.get("/app","Problems", problemId, "input", String.format("input%d.txt", i)));
            CMD[CMD.length - 1] = newLanguageCmd;

            ExecuteMessage message = new ExecuteMessage();

            // 获取容器
            CreateContainerResponse response = DockerUtils.createResponsePlus(dockerClient, imageName, CMD, USER_DIR);
            String containerId = response.getId();
            log.info("[CONTAINER CREATE] ID: {}, Image: {}",
                    containerId,
                    imageName
            );

            dockerClient.startContainerCmd(containerId).exec();
            // 获取占用内存
            Long[] maxMemoryUsage = {0L};
            dockerClient.statsCmd(containerId).withNoStream(false).exec(new ResultCallback.Adapter<Statistics>() {
                @Override
                public void onNext(Statistics statistics) {
                    // Docker stats API 在容器刚启动或刚结束时，部分字段（如 usage）可能为 null
                    Long usage = null;
                    if (statistics.getMemoryStats() != null) {
                        usage = statistics.getMemoryStats().getUsage();
                    }
                    if (usage != null) {
                        if (usage > maxMemoryUsage[0]) {
                            maxMemoryUsage[0] = usage;
                        }
                        log.info("内存消耗：" + usage + "KB");
                    } else {
                        log.info("内存消耗：未知");
                    }

                    if (statistics.getCpuStats() != null && statistics.getCpuStats().getCpuUsage() != null) {
                        log.info("cpu：" + statistics.getCpuStats().getCpuUsage());
                    } else {
                        log.info("cpu：未知");
                    }
                    super.onNext(statistics);
                }
            });


            try {
                // 启动容器并计时
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                // 异步等待容器退出（带超时）
                CompletableFuture<Void> completion = CompletableFuture.runAsync(() -> {
                    try {
                        dockerClient.waitContainerCmd(containerId)
                                .exec(new WaitContainerResultCallback())
                                .awaitCompletion();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                try {
                    completion.get(MAX_RUN_TIME, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    dockerClient.killContainerCmd(containerId).exec();
                    message.setExitValue(-1L);
                    message.setErrorMessage("执行超时");
                } finally {
                    stopWatch.stop();
                    message.setMemory(maxMemoryUsage[0]);
                    message.setTime(stopWatch.getLastTaskTimeMillis());
                    log.info("执行时间：" + message.getTime() + " ms");
                    log.info("占用内存：" + message.getMemory() + " B");
                }

                //  收集容器状态
                try {
                    InspectContainerResponse inspect = dockerClient.inspectContainerCmd(containerId).exec();
                    if (inspect != null && inspect.getState() != null) {
                        Long exitCode = inspect.getState().getExitCodeLong();
                        message.setExitValue(exitCode);
                        Boolean oomKilled = inspect.getState().getOOMKilled();
                        String error = inspect.getState().getError();
                        String status = inspect.getState().getStatus();
                        log.info("容器退出码: " + exitCode);
                        message.setExitValue(exitCode);
                        log.info("OOMKilled: " + oomKilled);
                        log.info("Error: " + error);
                        message.setErrorMessage(error);
                        log.info("Status: " + status);
                    } else {
                        message.setExitValue(-1L);
                        message.setErrorMessage("无法获取容器状态");
                    }
                } catch (Exception e) {
                    message.setExitValue(-1L);
                    message.setErrorMessage("inspectContainerCmd异常: " + e.getMessage());
                    throw new RuntimeException("容器异常退出");
                }

                // 获取执行结果
                StringBuilder output = new StringBuilder();
                dockerClient.logContainerCmd(containerId)
                        .withStdOut(true)
                        .withStdErr(true)
                        .withFollowStream(false)
                        .exec(new ResultCallback.Adapter<Frame>() {
                            @Override
                            public void onNext(Frame frame) {
                                output.append(new String(frame.getPayload()));
                            }
                        }).awaitCompletion();
                String result = output.toString().replaceAll("\r", "");
                // 鉴定异常
                if (message.getExitValue() != 0) {
                    message.setErrorMessage(result.substring(result.indexOf("error")));
                } else {
                    message.setMessage(result);
                }

                log.info(result);

                // 检验结果
                String answerFilePath = Paths.get(USER_DIR, "Problems", problemId, "answer", "answer" + i + ".txt").toString();
                boolean isCorrect = check(result, answerFilePath);
                message.setCorrect(isCorrect);

            } catch (Exception e) {
                message.setExitValue(-1L);
                message.setErrorMessage("容器异常: "+e.getMessage());
            } finally {

                // 10. 清理容器
                dockerClient.removeContainerCmd(containerId)
                        .withForce(true)
                        .exec();

                executeMessages.add(message);
            }
        }

        return executeMessages;
    }
    public List<ExecuteMessage> runFileWithInput(File userCodeFile, File userInputFile,  String imageName, String languageCmd) {
        String userCodeDir = userCodeFile.getParentFile().getAbsolutePath();
        String userInputDir = userInputFile.getAbsolutePath();

        // 获取Docker Client
        DockerClient dockerClient = DockerUtils.createDockerClient();
        // 拉取镜像
        this.PullDockerImage(dockerClient, imageName);

        // 执行命令
        List<ExecuteMessage> executeMessages = new ArrayList<>();

        String MainPath = "/app" + File.separator + userCodeDir.substring(USER_DIR.length() + 1);
        String InputPath = "/app" + File.separator + userInputDir.substring(USER_DIR.length() + 1);


        // 用户输入测试
        String newLanguageCmd = String.format(languageCmd, MainPath, InputPath);
        CMD[CMD.length - 1] = newLanguageCmd;

        ExecuteMessage message = new ExecuteMessage();

        // 获取容器
        CreateContainerResponse response = DockerUtils.createResponsePlus(dockerClient, imageName, CMD, USER_DIR);
        String containerId = response.getId();
        log.info("[CONTAINER CREATE] ID: {}, Image: {}",
                containerId,
                imageName
        );

        dockerClient.startContainerCmd(containerId).exec();
        // 获取占用内存
        Long[] maxMemoryUsage = {0L};
        dockerClient.statsCmd(containerId).withNoStream(false).exec(new ResultCallback.Adapter<Statistics>() {
            @Override
            public void onNext(Statistics statistics) {
                // Docker stats API 在容器刚启动或刚结束时，部分字段（如 usage）可能为 null
                Long usage = null;
                if (statistics.getMemoryStats() != null) {
                    usage = statistics.getMemoryStats().getUsage();
                }
                if (usage != null) {
                    if (usage > maxMemoryUsage[0]) {
                        maxMemoryUsage[0] = usage;
                    }
                    log.info("内存消耗：" + usage + "KB");
                } else {
                    log.info("内存消耗：未知");
                }

                if (statistics.getCpuStats() != null && statistics.getCpuStats().getCpuUsage() != null) {
                    log.info("cpu：" + statistics.getCpuStats().getCpuUsage());
                } else {
                    log.info("cpu：未知");
                }
                super.onNext(statistics);
            }
        });


        try {
            // 启动容器并计时
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            // 异步等待容器退出（带超时）
            CompletableFuture<Void> completion = CompletableFuture.runAsync(() -> {
                try {
                    dockerClient.waitContainerCmd(containerId)
                            .exec(new WaitContainerResultCallback())
                            .awaitCompletion();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            try {
                completion.get(MAX_RUN_TIME, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                dockerClient.killContainerCmd(containerId).exec();
                message.setExitValue(-1L);
                message.setErrorMessage("执行超时");
            } finally {
                stopWatch.stop();
                message.setMemory(maxMemoryUsage[0]);
                message.setTime(stopWatch.getLastTaskTimeMillis());
                log.info("执行时间：" + message.getTime() + " ms");
                log.info("占用内存：" + message.getMemory() + " B");
            }

            //  收集容器状态
            try {
                InspectContainerResponse inspect = dockerClient.inspectContainerCmd(containerId).exec();
                if (inspect != null && inspect.getState() != null) {
                    Long exitCode = inspect.getState().getExitCodeLong();
                    message.setExitValue(exitCode);
                    Boolean oomKilled = inspect.getState().getOOMKilled();
                    String error = inspect.getState().getError();
                    String status = inspect.getState().getStatus();
                    log.info("容器退出码: " + exitCode);
                    message.setExitValue(exitCode);
                    log.info("OOMKilled: " + oomKilled);
                    log.info("Error: " + error);
                    message.setErrorMessage(error);
                    log.info("Status: " + status);
                } else {
                    message.setExitValue(-1L);
                    message.setErrorMessage("无法获取容器状态");
                }
            } catch (Exception e) {
                message.setExitValue(-1L);
                message.setErrorMessage("inspectContainerCmd异常: " + e.getMessage());
                throw new RuntimeException("容器异常退出");
            }

            // 获取执行结果
            StringBuilder output = new StringBuilder();
            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(false)
                    .exec(new ResultCallback.Adapter<Frame>() {
                        @Override
                        public void onNext(Frame frame) {
                            output.append(new String(frame.getPayload()));
                        }
                    }).awaitCompletion();
            String result = output.toString().replaceAll("\r", "");
            // 鉴定异常
            if (message.getExitValue() != 0) {
                message.setErrorMessage(result.substring(result.indexOf("error")));
            } else {
                message.setMessage(result);
            }

            log.info(result);

        } catch (Exception e) {
            message.setExitValue(-1L);
            message.setErrorMessage("容器异常: "+e.getMessage());
        } finally {

            // 10. 清理容器
            dockerClient.removeContainerCmd(containerId)
                    .withForce(true)
                    .exec();

            executeMessages.add(message);
        }


        return executeMessages;
    }
    /**
     * 编译语言所用
     * @param userCodeFile
     * @param problemId
     * @param imageName
     * @param compileCmd
     * @param languageCmd
     * @param codeFileName
     * @return
     */
    public List<ExecuteMessage> compileAndRunFile(File userCodeFile, String problemId, String imageName,String compileCmd, String languageCmd, String codeFileName) {
        String userCodeDir = userCodeFile.getParentFile().getAbsolutePath();
        // 获取Docker Client
        DockerClient dockerClient = DockerUtils.createDockerClient();
        // 拉取镜像
        this.PullDockerImage(dockerClient, imageName);

        // 执行命令
        List<ExecuteMessage> executeMessages = new ArrayList<>();
        String inputFileParentPath = Paths.get(USER_DIR, "Problems", problemId, "input").toString();
        long inputFileCount = getDirChildFileCount(inputFileParentPath);
        String MainPath = "/app" + File.separator + userCodeDir.substring(USER_DIR.length() + 1);

        // 编译命令
        String newCompileCmd = String.format(compileCmd, MainPath + File.separator + codeFileName);
        for (int i = 1; i <= inputFileCount; i++) {
            // 构造执行命令
            String newLanguageCmd = String.format(languageCmd, MainPath, Paths.get("/app","Problems", problemId, "input", String.format("input%d.txt", i)));
            CMD[CMD.length - 1] = newCompileCmd + " && " + newLanguageCmd;

            ExecuteMessage message = new ExecuteMessage();

            // 获取容器
            CreateContainerResponse response = DockerUtils.createResponsePlus(dockerClient, imageName, CMD, USER_DIR);
            String containerId = response.getId();
            log.info("[CONTAINER CREATE] ID: {}, Image: {}",
                    containerId,
                    imageName
            );

            dockerClient.startContainerCmd(containerId).exec();
            // 获取占用内存
            Long[] maxMemoryUsage = {0L};
            dockerClient.statsCmd(containerId).withNoStream(false).exec(new ResultCallback.Adapter<Statistics>() {
                @Override
                public void onNext(Statistics statistics) {
                    // Docker stats API 在容器刚启动或刚结束时，部分字段（如 usage）可能为 null
                    Long usage = null;
                    if (statistics.getMemoryStats() != null) {
                        usage = statistics.getMemoryStats().getUsage();
                    }
                    if (usage != null) {
                        if (usage > maxMemoryUsage[0]) {
                            maxMemoryUsage[0] = usage;
                        }
                        log.info("内存消耗：" + usage + "KB");
                    } else {
                        log.info("内存消耗：未知");
                    }

                    if (statistics.getCpuStats() != null && statistics.getCpuStats().getCpuUsage() != null) {
                        log.info("cpu：" + statistics.getCpuStats().getCpuUsage());
                    } else {
                        log.info("cpu：未知");
                    }
                    super.onNext(statistics);
                }
            });


            try {
                // 启动容器并计时
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                // 异步等待容器退出（带超时）
                CompletableFuture<Void> completion = CompletableFuture.runAsync(() -> {
                    try {
                        dockerClient.waitContainerCmd(containerId)
                                .exec(new WaitContainerResultCallback())
                                .awaitCompletion();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                try {
                    completion.get(MAX_RUN_TIME, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    dockerClient.killContainerCmd(containerId).exec();
                    message.setExitValue(-1L);
                    message.setErrorMessage("执行超时");
                } finally {
                    stopWatch.stop();
                    message.setMemory(maxMemoryUsage[0]);
                    message.setTime(stopWatch.getLastTaskTimeMillis());
                    log.info("执行时间：" + message.getTime() + " ms");
                    log.info("占用内存：" + message.getMemory() + " B");
                }

                //  收集容器状态
                try {
                    InspectContainerResponse inspect = dockerClient.inspectContainerCmd(containerId).exec();
                    if (inspect != null && inspect.getState() != null) {
                        Long exitCode = inspect.getState().getExitCodeLong();
                        message.setExitValue(exitCode);
                        Boolean oomKilled = inspect.getState().getOOMKilled();
                        String error = inspect.getState().getError();
                        String status = inspect.getState().getStatus();
                        log.info("容器退出码: " + exitCode);
                        message.setExitValue(exitCode);
                        log.info("OOMKilled: " + oomKilled);
                        log.info("Error: " + error);
                        message.setErrorMessage(error);
                        log.info("Status: " + status);
                    } else {
                        message.setExitValue(-1L);
                        message.setErrorMessage("无法获取容器状态");
                    }
                } catch (Exception e) {
                    message.setExitValue(-1L);
                    message.setErrorMessage("inspectContainerCmd异常: " + e.getMessage());
                    throw new RuntimeException("容器异常退出");
                }

                // 获取执行结果
                StringBuilder output = new StringBuilder();
                dockerClient.logContainerCmd(containerId)
                        .withStdOut(true)
                        .withStdErr(true)
                        .withFollowStream(false)
                        .exec(new ResultCallback.Adapter<Frame>() {
                            @Override
                            public void onNext(Frame frame) {
                                output.append(new String(frame.getPayload()));
                            }
                        }).awaitCompletion();
                String result = output.toString().replaceAll("\r", "");
                // 鉴定异常
                if (message.getExitValue() != 0) {
                    message.setErrorMessage(result.substring(result.indexOf("error")));
                } else {
                    message.setMessage(result);
                }

                log.info("输出结果： " + result);

                // 检验结果
                String answerFilePath = Paths.get(USER_DIR, "Problems", problemId, "answer", "answer" + i + ".txt").toString();
                boolean isCorrect = check(result, answerFilePath);
                message.setCorrect(isCorrect);

            } catch (Exception e) {
                message.setExitValue(-1L);
                message.setErrorMessage("容器异常: "+e.getMessage());
            } finally {

                // 10. 清理容器
                dockerClient.removeContainerCmd(containerId)
                        .withForce(true)
                        .exec();

                executeMessages.add(message);
            }
        }

        return executeMessages;
    }

    public List<ExecuteMessage> compileAndRunFileWithInput(File userCodeFile, File userInputFile, String imageName,String compileCmd, String languageCmd, String codeFileName) {
        String userCodeDir = userCodeFile.getParentFile().getAbsolutePath();
        String userInputDir = userInputFile.getAbsolutePath();

        // 获取Docker Client
        DockerClient dockerClient = DockerUtils.createDockerClient();
        // 拉取镜像
        this.PullDockerImage(dockerClient, imageName);

        // 执行命令
        List<ExecuteMessage> executeMessages = new ArrayList<>();

        String MainPath = "/app" + File.separator + userCodeDir.substring(USER_DIR.length() + 1);
        String InputPath = "/app" + File.separator + userInputDir.substring(USER_DIR.length() + 1);

        // 编译命令
        String newCompileCmd = String.format(compileCmd, MainPath + File.separator + codeFileName);

        // 用户输入测试
        String newLanguageCmd = String.format(languageCmd, MainPath, InputPath);
        CMD[CMD.length - 1] = newCompileCmd + " && " + newLanguageCmd;

        ExecuteMessage message = new ExecuteMessage();

        // 获取容器
        CreateContainerResponse response = DockerUtils.createResponsePlus(dockerClient, imageName, CMD, USER_DIR);
        String containerId = response.getId();
        log.info("[CONTAINER CREATE] ID: {}, Image: {}",
                containerId,
                imageName
        );

        dockerClient.startContainerCmd(containerId).exec();
        // 获取占用内存
        Long[] maxMemoryUsage = {0L};
        dockerClient.statsCmd(containerId).withNoStream(false).exec(new ResultCallback.Adapter<Statistics>() {
            @Override
            public void onNext(Statistics statistics) {
                // Docker stats API 在容器刚启动或刚结束时，部分字段（如 usage）可能为 null
                Long usage = null;
                if (statistics.getMemoryStats() != null) {
                    usage = statistics.getMemoryStats().getUsage();
                }
                if (usage != null) {
                    if (usage > maxMemoryUsage[0]) {
                        maxMemoryUsage[0] = usage;
                    }
                    log.info("内存消耗：" + usage + "KB");
                } else {
                    log.info("内存消耗：未知");
                }

                if (statistics.getCpuStats() != null && statistics.getCpuStats().getCpuUsage() != null) {
                    log.info("cpu：" + statistics.getCpuStats().getCpuUsage());
                } else {
                    log.info("cpu：未知");
                }
                super.onNext(statistics);
            }
        });


        try {
            // 启动容器并计时
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            // 异步等待容器退出（带超时）
            CompletableFuture<Void> completion = CompletableFuture.runAsync(() -> {
                try {
                    dockerClient.waitContainerCmd(containerId)
                            .exec(new WaitContainerResultCallback())
                            .awaitCompletion();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            try {
                completion.get(MAX_RUN_TIME, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                dockerClient.killContainerCmd(containerId).exec();
                message.setExitValue(-1L);
                message.setErrorMessage("执行超时");
            } finally {
                stopWatch.stop();
                message.setMemory(maxMemoryUsage[0]);
                message.setTime(stopWatch.getLastTaskTimeMillis());
                log.info("执行时间：" + message.getTime() + " ms");
                log.info("占用内存：" + message.getMemory() + " B");
            }

            //  收集容器状态
            try {
                InspectContainerResponse inspect = dockerClient.inspectContainerCmd(containerId).exec();
                if (inspect != null && inspect.getState() != null) {
                    Long exitCode = inspect.getState().getExitCodeLong();
                    message.setExitValue(exitCode);
                    Boolean oomKilled = inspect.getState().getOOMKilled();
                    String error = inspect.getState().getError();
                    String status = inspect.getState().getStatus();
                    log.info("容器退出码: " + exitCode);
                    message.setExitValue(exitCode);
                    log.info("OOMKilled: " + oomKilled);
                    log.info("Error: " + error);
                    message.setErrorMessage(error);
                    log.info("Status: " + status);
                } else {
                    message.setExitValue(-1L);
                    message.setErrorMessage("无法获取容器状态");
                }
            } catch (Exception e) {
                message.setExitValue(-1L);
                message.setErrorMessage("inspectContainerCmd异常: " + e.getMessage());
                throw new RuntimeException("容器异常退出");
            }

            // 获取执行结果
            StringBuilder output = new StringBuilder();
            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(false)
                    .exec(new ResultCallback.Adapter<Frame>() {
                        @Override
                        public void onNext(Frame frame) {
                            output.append(new String(frame.getPayload()));
                        }
                    }).awaitCompletion();
            String result = output.toString().replaceAll("\r", "");
            // 鉴定异常
            if (message.getExitValue() != 0) {
                message.setErrorMessage(result.substring(result.indexOf("error")));
            } else {
                message.setMessage(result);
            }

            log.info(result);

        } catch (Exception e) {
            message.setExitValue(-1L);
            message.setErrorMessage("容器异常: "+e.getMessage());
        } finally {

            // 10. 清理容器
            dockerClient.removeContainerCmd(containerId)
                    .withForce(true)
                    .exec();

            executeMessages.add(message);
        }


        return executeMessages;
    }

    //
    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList) {
        ExecuteCodeResponse response = new ExecuteCodeResponse();

        List<String> outputList = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>(); // 收集所有错误信息
        long maxTime = 0;
        long maxMemory = 0;
        int size = executeMessageList.size();
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setCorrect(new boolean[size]);
        for (int i = 0; i < executeMessageList.size(); i++) {
            judgeInfo.getCorrect()[i] = true;
            ExecuteMessage message = executeMessageList.get(i);
            maxTime = Math.max(maxTime, message.getTime());
            maxMemory = Math.max(maxMemory, message.getMemory());
            // 检查进程执行是否失败（如超时、编译错误）
            if (message.getExitValue() != 0) {
                errorMessages.add(String.format(message.getErrorMessage()));
                response.setExitCode(message.getExitValue());
                break;
            }

            if (!message.isCorrect()) {
                judgeInfo.getCorrect()[i] = false;
            }
            outputList.add(message.getMessage());
        }

        // 封装判题信息
        judgeInfo.setTime(maxTime);
        judgeInfo.setMemory(maxMemory);
        judgeInfo.setErrorMessages(errorMessages);
        response.setJudgeInfo(judgeInfo);
        response.setOutputList(outputList);
        return response;
    }

    public boolean check(String outputStr, String answerFilePath) {
        try {
            List<String> output = List.of(outputStr.split("\n"));
            List<String> answer = Files.readAllLines(Paths.get(answerFilePath));
            if (output.size() != answer.size()) {
                return false;
            }
            for (int i = 0; i < output.size(); i++) {
                if (!output.get(i).equals(answer.get(i))) {
                    return false;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public long getDirChildFileCount(String inputFileParentPath) {
        Path path = Paths.get(inputFileParentPath);
        try (Stream<Path> walk = Files.walk(path)) {
            return walk.filter(Files::isRegularFile)
                    .count();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void PullDockerImage (DockerClient dockerClient, String imageName) {
        boolean imageExists = DockerUtils.imageExists(dockerClient, imageName);
        if (!imageExists) {
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(imageName);
            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
                @Override
                public void onNext(PullResponseItem item) {
                    String status = item.getStatus();
                    String progress = item.getProgress();
                    log.info("[DOCKER PULL] Status: {}, Progress: {}, ID: {}",
                            status,
                            progress,
                            item.getId()
                    );
                    super.onNext(item);
                }
            };
            try {
                pullImageCmd
                        .exec(pullImageResultCallback)
                        .awaitCompletion();
            } catch (InterruptedException e) {
                log.info("拉取镜像异常");
                throw new RuntimeException(e);
            }
        }

        log.info(imageExists ? "镜像存在" : "下载完成");
    }

    public ExecuteMessage executeContainer(DockerClient dockerClient, String imageName, List<ExecuteMessage> executeMessages) {

        ExecuteMessage message = new ExecuteMessage();

        // 获取容器
        CreateContainerResponse response = DockerUtils.createResponsePlus(dockerClient, imageName, CMD, USER_DIR);
        String containerId = response.getId();
        log.info("[CONTAINER CREATE] ID: {}, Image: {}",
                containerId,
                imageName
        );

        dockerClient.startContainerCmd(containerId).exec();
        // 获取占用内存
        Long[] maxMemoryUsage = {0L};
        dockerClient.statsCmd(containerId).withNoStream(false).exec(new ResultCallback.Adapter<Statistics>() {
            @Override
            public void onNext(Statistics statistics) {
                // Docker stats API 在容器刚启动或刚结束时，部分字段（如 usage）可能为 null
                Long usage = null;
                if (statistics.getMemoryStats() != null) {
                    usage = statistics.getMemoryStats().getUsage();
                }
                if (usage != null) {
                    if (usage > maxMemoryUsage[0]) {
                        maxMemoryUsage[0] = usage;
                    }
                    log.info("内存消耗：" + usage + "KB");
                } else {
                    log.info("内存消耗：未知");
                }

                if (statistics.getCpuStats() != null && statistics.getCpuStats().getCpuUsage() != null) {
                    log.info("cpu：" + statistics.getCpuStats().getCpuUsage());
                } else {
                    log.info("cpu：未知");
                }
                super.onNext(statistics);
            }
        });


        try {
            // 启动容器并计时
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            // 异步等待容器退出（带超时）
            CompletableFuture<Void> completion = CompletableFuture.runAsync(() -> {
                try {
                    dockerClient.waitContainerCmd(containerId)
                            .exec(new WaitContainerResultCallback())
                            .awaitCompletion();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            try {
                completion.get(MAX_RUN_TIME, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                dockerClient.killContainerCmd(containerId).exec();
                message.setExitValue(-1L);
                message.setErrorMessage("执行超时");
            } finally {
                stopWatch.stop();
                message.setMemory(maxMemoryUsage[0]);
                message.setTime(stopWatch.getLastTaskTimeMillis());
                log.info("执行时间：" + message.getTime() + " ms");
                log.info("占用内存：" + message.getMemory() + " B");
            }

            //  收集容器状态
            try {
                InspectContainerResponse inspect = dockerClient.inspectContainerCmd(containerId).exec();
                if (inspect != null && inspect.getState() != null) {
                    Long exitCode = inspect.getState().getExitCodeLong();
                    message.setExitValue(exitCode);
                    Boolean oomKilled = inspect.getState().getOOMKilled();
                    String error = inspect.getState().getError();
                    String status = inspect.getState().getStatus();
                    log.info("容器退出码: " + exitCode);
                    message.setExitValue(exitCode);
                    log.info("OOMKilled: " + oomKilled);
                    log.info("Error: " + error);
                    message.setErrorMessage(error);
                    log.info("Status: " + status);
                } else {
                    message.setExitValue(-1L);
                    message.setErrorMessage("无法获取容器状态");
                }
            } catch (Exception e) {
                message.setExitValue(-1L);
                message.setErrorMessage("inspectContainerCmd异常: " + e.getMessage());
                throw new RuntimeException("容器异常退出");
            }

            // 获取执行结果
            StringBuilder output = new StringBuilder();
            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(false)
                    .exec(new ResultCallback.Adapter<Frame>() {
                        @Override
                        public void onNext(Frame frame) {
                            output.append(new String(frame.getPayload()));
                        }
                    }).awaitCompletion();
            String result = output.toString().replaceAll("\r", "");
            // 鉴定异常
            if (message.getExitValue() != 0) {
                message.setErrorMessage(result.substring(result.indexOf("error")));
            } else {
                message.setMessage(result);
            }

            log.info(result);

        } catch (Exception e) {
            message.setExitValue(-1L);
            message.setErrorMessage("容器异常: "+e.getMessage());
        } finally {

            // 10. 清理容器
            dockerClient.removeContainerCmd(containerId)
                    .withForce(true)
                    .exec();

            executeMessages.add(message);
        }
        return message;
    }
}
