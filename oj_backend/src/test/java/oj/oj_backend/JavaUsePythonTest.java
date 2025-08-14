package oj.oj_backend;


import cn.hutool.json.JSONUtil;
import oj.oj_backend.model.response.ContestResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/6/24 21:03
 */
@SpringBootTest
public class JavaUsePythonTest {

    @Test
    public void test() throws Exception{
        ProcessBuilder pb1 = new ProcessBuilder(
                "python",
                Paths.get(System.getProperty("user.dir"), "python", "nowcoder_contest.py").toString()
        );
        ProcessBuilder pb2 = new ProcessBuilder(
                "python",
                Paths.get(System.getProperty("user.dir"), "python", "nowcoder_contest.py").toString()
        );

        pb1.redirectErrorStream(true); // 合并错误输出到标准输出流
        pb1.environment().put("PYTHONIOENCODING", "utf-8");
        Process process = pb1.start();
// 读取Python输出
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        );
        String line;
        StringBuilder builder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            System.out.println(line);
        }

        List<ContestResponse> list = JSONUtil.toList(builder.toString(), ContestResponse.class);

        System.out.println(list);
        System.out.println(JSONUtil.toJsonStr( list));
        int exitCode = process.waitFor();
        System.out.println("Exit Code: " + exitCode);
    }


}
