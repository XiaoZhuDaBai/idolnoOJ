package oj.oj_backend.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/6/27 23:14
 */
public class ProcessTask implements Callable<String> {
    private final ProcessBuilder pb;

    public ProcessTask(ProcessBuilder pb) {
        this.pb = pb;
    }
    @Override
    public String call() throws Exception {
        pb.redirectErrorStream(true); // 合并错误输出到标准输出流
        pb.environment().put("PYTHONIOENCODING", "utf-8");
        Process process = pb.start();
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            // 逐行读取输出
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
            // 阻塞直到进程结束并返回状态码
            int exitCode = process.waitFor();
            output.append("\nExit Value: ").append(exitCode);
        }
        return output.toString();
    }
}
