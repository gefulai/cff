package com.cff.cache.queue.processor;

import com.cff.cache.queue.CacheQueue;
import com.cff.cache.queue.model.Batch;
import com.cff.cache.queue.model.Block;

import java.io.Serializable;
import java.util.List;

public interface CacheQueueProcessor<E extends Serializable> {

    /**
     * 获取业务ID
     * @return 业务ID
     */
    String getBizId();

    /**
     * 初始化处理器
     * @param list 任务列表
     * @param blockSize 块大小
     */
    void init(List<E> list, int blockSize);

    /**
     * 处理任务
     */
    void process();

    /**
     * 处理批次
     * @param batch 批次
     */
    void processBatch(Batch<E> batch);

    /**
     * 处理块
     * @param block 块
     */
    void processBlock(Block<E> block);

    /**
     * 设置缓存队列
     * @param cacheQueue 缓存队列
     */
    void setCacheQueue(CacheQueue<E> cacheQueue);

    /**
     * 获取缓存队列
     * @return 缓存队列
     */
    CacheQueue<E> getCacheQueue();
    
    /**
     * 消费块中的任务
     * @param block 块
     */
    void consumeBlockTasks(Block<E> block);
}