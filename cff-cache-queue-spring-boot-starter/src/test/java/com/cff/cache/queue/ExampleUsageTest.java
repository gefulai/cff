package com.cff.cache.queue;

import com.cff.cache.queue.processor.ExampleCacheQueueProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class ExampleUsageTest {

    @Test
    public void testExampleUsage() {
        // 创建示例处理器
        ExampleCacheQueueProcessor<String> processor = new ExampleCacheQueueProcessor<>();
        
        // 创建示例任务列表
        List<String> tasks = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            tasks.add("Task-" + i);
        }
        
        // 初始化处理器（在实际使用中，需要注入CacheQueue）
        // processor.setCacheQueue(cacheQueue);
        // processor.init(tasks, 10); // 块大小为10
        // processor.process();
        
        System.out.println("Example usage test completed");
    }
}