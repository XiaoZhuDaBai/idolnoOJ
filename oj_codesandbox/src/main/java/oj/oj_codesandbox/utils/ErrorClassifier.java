package oj.oj_codesandbox.utils;

import java.util.Arrays;
import java.util.List;

public class ErrorClassifier {

    // 编译错误特征
    private static final List<String> COMPILE_ERROR_KEYWORDS = Arrays.asList(
            "error:", "cannot find symbol", "class", "should be declared",
            "incompatible types", "method", "constructor", "package does not exist"
    );

    // 运行时错误特征
    private static final List<String> RUNTIME_ERROR_KEYWORDS = Arrays.asList(
            "Exception", "Error", "at java.", "at sun.", "Caused by:",
            "NullPointerException", "ArrayIndexOutOfBoundsException"
    );

    public static String classifyError(String errorOutput, long exitCode) {
        // 检查是否包含编译错误关键词
        for (String keyword : COMPILE_ERROR_KEYWORDS) {
            if (errorOutput.contains(keyword)) {
                return "COMPILE_ERROR";
            }
        }

        // 检查是否包含运行时错误关键词
        for (String keyword : RUNTIME_ERROR_KEYWORDS) {
            if (errorOutput.contains(keyword)) {
                return "RUNTIME_ERROR";
            }
        }

        // 根据退出码判断
        if (exitCode == 1 && errorOutput.contains("error")) {
            return "COMPILE_ERROR";
        }

        return "RUNTIME_ERROR";
    }
}

