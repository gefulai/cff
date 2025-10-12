package com.cff.cache.queue.processor;

import com.cff.cache.queue.CacheQueue;
import com.cff.cache.queue.exception.CacheQueueException;
import com.cff.cache.queue.model.Batch;
import com.cff.cache.queue.model.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public abstract class AbstractCacheQueueProcessor<E extends Serializable> implements CacheQueueProcessor<E> {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractCacheQueueProcessor.class);

    private CacheQueue<E> cacheQueue;
    
    private String bizId;
    
    private int blockSize = 100; // 默认块大小

    @Override
    public void init(List<E> list, int blockSize) {
        this.blockSize = blockSize;
        String batchId = generateBatchId();
        
        try {
            cacheQueue.addBatch(bizId, batchId, blockSize, list);
            logger.info("Batch {} initialized with {} tasks, block size: {}", batchId, list.size(), blockSize);
        } catch (CacheQueueException e) {
            logger.error("Failed to initialize batch", e);
            throw e;
        }
    }

    @Override
    public void process() {
        try {
            while (true) {
                Batch<E> batch = cacheQueue.nextBatch(bizId);
                if (batch == null) {
                    logger.info("No more batches to process for bizId: {}", bizId);
                    break;
                }
                
                logger.info("Processing batch: {}", batch.getBatchId());
                processBatch(batch);
                
                // 检查批次是否完成
                if (batch.isCompleted()) {
                    cacheQueue.removeBatch(bizId, batch.getBatchId());
                    logger.info("Batch {} completed and removed", batch.getBatchId());
                }
            }
        } catch (CacheQueueException e) {
            logger.error("Error processing batches", e);
        }
    }

    @Override
    public void processBatch(Batch<E> batch) {
        try {
            while (true) {
                Block<E> block = cacheQueue.nextBlock(bizId, batch.getBatchId());
                if (block == null) {
                    logger.info("No more blocks to process for batch: {}", batch.getBatchId());
                    break;
                }
                
                if (block.isConsumed()) {
                    logger.info("Block {} already consumed, skipping", block.getBlockIndex());
                    continue;
                }
                
                logger.info("Processing block: {} in batch: {}", block.getBlockIndex(), batch.getBatchId());
                processBlock(block);
                
                // 标记块为已消费
                cacheQueue.markBlockConsumed(bizId, batch.getBatchId(), block.getBlockIndex());
                batch.incrementConsumedBlockCount();
                
                logger.info("Block {} consumed, consumed count: {}", block.getBlockIndex(), batch.getConsumedBlockCount());
            }
        } catch (CacheQueueException e) {
            logger.error("Error processing blocks for batch: " + batch.getBatchId(), e);
        }
    }

    @Override
    public void processBlock(Block<E> block) {
        consumeBlockTasks(block);
    }

    @Override
    public void setCacheQueue(CacheQueue<E> cacheQueue) {
        this.cacheQueue = cacheQueue;
    }

    @Override
    public CacheQueue<E> getCacheQueue() {
        return cacheQueue;
    }
    
    @Override
    public String getBizId() {
        return bizId;
    }
    
    public void setBizId(String bizId) {
        this.bizId = bizId;
    }
    
    protected String generateBatchId() {
        return "batch_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    // 抽象方法，由具体实现类提供块任务消费逻辑
    public abstract void consumeBlockTasks(Block<E> block);
}