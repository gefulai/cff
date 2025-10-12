package com.cff.cache.queue;

import com.cff.cache.queue.model.Batch;
import com.cff.cache.queue.model.Block;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CacheQueueTest {

    @Test
    public void testBatchCreation() {
        String bizId = "testBiz";
        String batchId = "testBatch";
        Integer blockSize = 10;
        Integer blockCount = 5;
        
        Batch<String> batch = new Batch<>(bizId, batchId, blockSize, blockCount);
        
        assertEquals(bizId, batch.getBizId());
        assertEquals(batchId, batch.getBatchId());
        assertEquals(blockSize, batch.getBlockSize());
        assertEquals(blockCount, batch.getBlockCount());
        assertEquals(0, batch.getConsumedBlockCount());
        assertFalse(batch.isCompleted());
    }
    
    @Test
    public void testBlockCreation() {
        Integer blockIndex = 1;
        Block<String> block = new Block<>(blockIndex);
        
        assertEquals(blockIndex, block.getBlockIndex());
        assertFalse(block.isConsumed());
        assertNotNull(block.getCreateTime());
    }
    
    @Test
    public void testBatchConsumption() {
        String bizId = "testBiz";
        String batchId = "testBatch";
        Integer blockSize = 10;
        Integer blockCount = 3;
        
        Batch<String> batch = new Batch<>(bizId, batchId, blockSize, blockCount);
        
        // 模拟消费过程
        for (int i = 0; i < blockCount; i++) {
            batch.incrementConsumedBlockCount();
        }
        
        assertTrue(batch.isCompleted());
        assertEquals(blockCount, batch.getConsumedBlockCount());
    }
    
    @Test
    public void testBlockTaskAddition() {
        Block<String> block = new Block<>(0);
        
        block.addTask("Task 1");
        block.addTask("Task 2");
        block.addTask("Task 3");
        
        assertNotNull(block.getQueue());
        assertEquals(3, block.getQueue().size());
    }
}