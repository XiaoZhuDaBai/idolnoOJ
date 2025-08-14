package oj.oj_backend.service.impl;

import cn.hutool.json.JSONUtil;
import oj.oj_backend.model.response.ContestResponse;
import oj.oj_backend.service.FetchService;
import oj.oj_backend.util.ProcessTask;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;


/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/6/14 21:19
 */
@Service
@Deprecated
public class FetchServiceImpl implements FetchService {
    @Override
    //todo 解决方案：加缓存，接口只返回缓存数据，抓取任务定时异步执行
    public String fetchContests() {
        ProcessBuilder pb1 = new ProcessBuilder(
                "python",
                Paths.get(System.getProperty("user.dir"), "python", "atcoder_contest.py").toString()
        );
        ProcessBuilder pb2 = new ProcessBuilder(
                "python",
                Paths.get(System.getProperty("user.dir"), "python", "codeforces_contest.py").toString()
        );
        ProcessBuilder pb3 = new ProcessBuilder(
                "python",
                Paths.get(System.getProperty("user.dir"), "python", "nowcoder_contest.py").toString()
        );

        ExecutorService executor = Executors.newFixedThreadPool(3);

        Future<String> submit1 = executor.submit(new ProcessTask(pb1));
        Future<String> submit2 = executor.submit(new ProcessTask(pb2));
        Future<String> submit3 = executor.submit(new ProcessTask(pb3));

        List<ContestResponse> contests = new ArrayList<>();
        try {
            // 等待任务结束（设置超时避免进程卡顿）
            String result1 = submit1.get(60, TimeUnit.SECONDS);
            String result2 = submit2.get(60, TimeUnit.SECONDS);
            String result3 = submit3.get(60, TimeUnit.SECONDS);

            contests.addAll(JSONUtil.toList(result1, ContestResponse.class));
            System.out.println("任务-1结果:" + result1);
            contests.addAll(JSONUtil.toList(result2, ContestResponse.class));
            System.out.println("任务-2结果:" + result2);
            contests.addAll(JSONUtil.toList(result3, ContestResponse.class));
            System.out.println("任务-3结果:" + result3);

        } catch (TimeoutException e) {
            System.err.println("进程响应超时，被强制中断");
        } catch (ExecutionException e) {
            System.err.println("子进程执行过程中出错: " + e.getCause().getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("任务被中断");
        } finally {
            executor.shutdownNow();  //立即终止所有线程
        }

        contests.sort(Comparator.comparing(ContestResponse::getStart_time));

        return JSONUtil.toJsonStr(contests);
    }
}
