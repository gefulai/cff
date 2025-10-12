package com.cff.cache.queue.processor;

import com.cff.cache.queue.model.Block;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Queue;

@Component
public class ExampleCacheQueueProcessor<E extends Serializable> extends AbstractCacheQueueProcessor<E> {
    
    public ExampleCacheQueueProcessor() {
        // 设置业务ID
        setBizId("example_biz");
    }
    
    @Override
    public void consumeBlockTasks(Block<E> block) {
        Queue<E> taskQueue = block.getQueue();
        if (taskQueue != null) {
            while (!taskQueue.isEmpty()) {
                E task = taskQueue.poll();
                // 处理具体的任务
                System.out.println("Processing task: " + task);
                
                // 模拟任务处理时间
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}