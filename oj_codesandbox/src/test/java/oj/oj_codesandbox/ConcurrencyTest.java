package oj.oj_codesandbox;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 并发测试：验证 SET field = field + 1 的原子性
 */
@SpringBootTest
@ActiveProfiles("test")
public class ConcurrencyTest {

    private static final String DB_URL = "jdbc:mysql://192.168.70.1:3306/oj";
    private static final String USERNAME = "test";
    private static final String PASSWORD = "123456";

    /**
     * 测试原子更新操作
     */
    @Test
    public void testAtomicUpdate() throws Exception {
        // 准备测试数据
        setupTestData();
        
        // 并发执行100次更新
        int threadCount = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        // 记录成功次数
        AtomicInteger successCount = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    // 执行原子更新
                    boolean success = atomicUpdate();
                    if (success) {
                        successCount.incrementAndGet();
                    }
                    System.out.println("线程 " + threadId + " 执行完成");
                } catch (Exception e) {
                    System.err.println("线程 " + threadId + " 执行失败: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 等待所有线程完成
        latch.await();
        executor.shutdown();
        
        // 验证结果
        verifyResult();
    }
    
    /**
     * 原子更新操作
     */
    private boolean atomicUpdate() throws Exception {
        String sql = "UPDATE question SET ac = ac + 1 WHERE question_id = 'TEST001'";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int result = stmt.executeUpdate();
            return result > 0;
        }
    }
    
    /**
     * 准备测试数据
     */
    private void setupTestData() throws Exception {
        String sql = "INSERT INTO question (question_id, ac) VALUES ('TEST001', 0) " +
                    "ON DUPLICATE KEY UPDATE ac = 0";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }
    
    /**
     * 验证结果
     */
    private void verifyResult() throws Exception {
        String sql = "SELECT ac FROM question WHERE question_id = 'TEST001'";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                int finalAc = rs.getInt("ac");
                System.out.println("最终 ac 值: " + finalAc);
                System.out.println("期望值: 100");
                System.out.println("测试结果: " + (finalAc == 100 ? "✅ 通过" : "❌ 失败"));
            }
        }
    }
}
