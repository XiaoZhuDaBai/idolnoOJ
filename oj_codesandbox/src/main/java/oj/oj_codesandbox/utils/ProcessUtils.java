package oj.oj_codesandbox.utils;

import oj.oj_codesandbox.model.ExecuteMessage;
import org.springframework.util.StopWatch;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * 进程工具类
 */
public class ProcessUtils {

    /**
     * 执行进程并获取信息
     *
     * @param runProcess
     * @param opName
     * @return
     */
    public static ExecuteMessage runProcessAndGetMessage(Process runProcess, String opName) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            // 等待程序执行，获取错误码
            long exitValue = runProcess.waitFor();
            executeMessage.setExitValue(exitValue);
            // 正常退出
            if (exitValue == 0) {
                System.out.println(opName + "成功");
                // 分批获取进程的正常输出
                executeMessage.setMessage(getProcessNormalOutput(runProcess));
            } else {
                // 异常退出
                System.out.println(opName + "失败，错误码： " + exitValue);
                // 分批获取进程的正常输出
                executeMessage.setMessage(getProcessNormalOutput(runProcess));
                // 分批获取进程的错误输出
                executeMessage.setErrorMessage(getProcessErrorOutput(runProcess));
            }
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return executeMessage;
    }

    private static String getProcessNormalOutput(Process runProcess) throws IOException {
        // 分批获取进程的正常输出
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
        StringBuilder compileOutputStringBuilder = new StringBuilder();
        // 逐行读取
        String compileOutputLine;
        while ((compileOutputLine = bufferedReader.readLine()) != null) {
            compileOutputStringBuilder.append(compileOutputLine).append("\n");
        }
        return compileOutputStringBuilder.toString();
    }
    private static String getProcessErrorOutput(Process runProcess) throws IOException {
        BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
        StringBuilder errorCompileOutputStringBuilder = new StringBuilder();
        // 逐行读取
        String errorCompileOutputLine;
        while ((errorCompileOutputLine = errorBufferedReader.readLine()) != null) {
            errorCompileOutputStringBuilder.append(errorCompileOutputLine);
        }
        return errorCompileOutputStringBuilder.toString();
    }


    // 封装的进程执行方法
    public static ExecuteMessage runProcessWithProcessBuilder(String[] command, String inputFile, String outputFile, long timeoutMs) {
        ExecuteMessage message = new ExecuteMessage();
        StopWatch stopWatch = new StopWatch();
        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectInput(new File(inputFile));
            pb.redirectOutput(new File(outputFile));
            pb.redirectError(ProcessBuilder.Redirect.PIPE);

            // 启动计时
            stopWatch.start();
            process = pb.start();

            // 异步读取错误流
            StringBuilder errorOutput = new StringBuilder();
            Thread errorThread = getThread(process, errorOutput);
            errorThread.start();

            // 等待进程结束或超时
            boolean exited = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
            // 关键修复：立即停止计时器
            stopWatch.stop();

            if (exited) {
                message.setExitValue((long)process.exitValue());
                message.setTime(stopWatch.getLastTaskTimeMillis()); // 安全获取时间
                message.setMessage(Files.readString(Paths.get(outputFile)));
                message.setErrorMessage(errorOutput.toString());
            } else {
                process.destroyForcibly();
                message.setExitValue(-1L);
                message.setErrorMessage("Timeout after " + timeoutMs + "ms");
            }
            System.out.println("运行" + (exited ? "成功" : "失败"));
        } catch (IOException | InterruptedException e) {
            message.setExitValue(-1L);
            message.setErrorMessage("Execution failed: " + e.getMessage());
        } finally {
            // 确保计时器停止（防止未捕获异常导致计时未停）
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
        return message;
    }

    private static Thread getThread(Process process, StringBuilder errorOutput) {
        Thread errorThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
            } catch (IOException e) {
                errorOutput.append("Error reading stderr: ").append(e.getMessage());
            }
        });
        return errorThread;
    }

}