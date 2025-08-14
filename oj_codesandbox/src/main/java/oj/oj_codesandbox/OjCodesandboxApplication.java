package oj.oj_codesandbox;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@SpringBootApplication
@MapperScan("oj.oj_codesandbox.mapper")
@EnableAsync
public class OjCodesandboxApplication {

    public static void main(String[] args) {
        SpringApplication.run(OjCodesandboxApplication.class, args);
    }

    /**
     * 自定义异步线程池配置
     * 专门用于处理判题任务
     */
    @Bean("judgeTaskExecutor")
    public Executor judgeTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：判题任务的核心处理线程
        executor.setCorePoolSize(20);
        
        // 最大线程数：最大可创建的线程数
        executor.setMaxPoolSize(50);
        
        // 队列容量：等待执行的任务队列大小
        executor.setQueueCapacity(100);
        
        // 线程名前缀：便于监控和调试
        executor.setThreadNamePrefix("judge-task-");
        
        // 线程空闲时间：超过核心线程数的线程空闲多久后被回收
        executor.setKeepAliveSeconds(60);
        
        // 拒绝策略：队列满且线程池满时的处理策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }
}
