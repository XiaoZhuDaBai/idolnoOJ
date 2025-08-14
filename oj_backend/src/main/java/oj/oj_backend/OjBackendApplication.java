package oj.oj_backend;

import cn.hutool.json.JSONUtil;
import oj.oj_backend.model.response.ContestResponse;
import oj.oj_backend.util.ProcessTask;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class })
@MapperScan("oj.oj_backend.mapper")
@EnableScheduling
@EnableAsync
public class OjBackendApplication {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(OjBackendApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(OjBackendApplication.class, args);
    }

    @Scheduled(fixedRateString = "${schedule.contest.time}")
    @Async
    public void fetchContests() {
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

        logger.info("开始执行竞赛数据抓取任务...");
        long startTime = System.currentTimeMillis();

        try {
            BoundZSetOperations<String, String> zSetKey = stringRedisTemplate.boundZSetOps("oj:contest:all");

            // 使用CompletableFuture并发获取结果
            logger.info("开始并发获取各平台竞赛数据...");
            List<CompletableFuture<List<ContestResponse>>> futures = Stream.of(submit1, submit2, submit3)
                    .map(f -> CompletableFuture.supplyAsync(() -> {
                        try {
                            logger.debug("开始执行Python爬取任务...");
                            String result = f.get(60, TimeUnit.SECONDS);
                            logger.debug("Python爬取任务完成，返回结果长度: {}", result.length());

                            List<ContestResponse> contests = JSONUtil.toList(result, ContestResponse.class);
                            logger.info("成功解析平台数据，获取竞赛数量: {}", contests.size());
                            return contests;
                        } catch (Exception e) {
                            logger.error("处理Python爬取任务时发生异常", e);
                            throw new CompletionException(e);
                        }
                    }))
                    .collect(Collectors.toList());
            // 合并所有结果
            logger.info("开始合并各平台数据...");
            List<ContestResponse> allContests = futures.stream()
                    .map(future -> {
                        try {
                            return future.join();
                        } catch (CompletionException e) {
                            logger.warn("处理平台数据时遇到异常，跳过该平台", e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            logger.info("合并完成，总竞赛数量: {}", allContests.size());
            // 批量插入Redis
            if (!allContests.isEmpty()) {
                // 清空原有数据
                long removedCount = zSetKey.removeRange(0, -1);
                logger.debug("已清空Redis中原有竞赛数据，移除条目数: {}", removedCount);

                logger.debug("开始准备Redis批量插入数据...");
                Map<String, Double> contestMap = allContests.stream()
                        .collect(Collectors.toMap(
                                JSONUtil::toJsonStr,
                                c -> c.getStart_time().doubleValue()
                        ));

                logger.info("开始执行Redis批量插入，数据量: {}", contestMap.size());
                long pipelineStart = System.currentTimeMillis();

                stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                    contestMap.forEach((json, score) -> {
                        byte[] key = "oj:contest:all".getBytes(StandardCharsets.UTF_8);
                        byte[] value = json.getBytes(StandardCharsets.UTF_8);
                        connection.zSetCommands().zAdd(key, score, value);
                    });
                    return null;
                });

                long pipelineTime = System.currentTimeMillis() - pipelineStart;
                logger.info("Redis批量插入完成，耗时: {}ms", pipelineTime);
            } else {
                logger.warn("未获取到任何竞赛数据，跳过Redis插入操作");
            }

        } catch (CompletionException e) {
            if (e.getCause() instanceof TimeoutException) {
                logger.error("进程响应超时，已强制中断", e);
            } else {
                logger.error("子进程执行过程中出现严重错误", e);
            }
        } finally {
            executor.shutdownNow();
            long totalTime = System.currentTimeMillis() - startTime;
            logger.info("竞赛数据抓取任务完成，总耗时: {}ms", totalTime);
        }
    }
}
