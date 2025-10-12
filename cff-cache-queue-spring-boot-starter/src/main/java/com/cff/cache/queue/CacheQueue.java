package com.cff.cache.queue;

import com.cff.cache.queue.exception.CacheQueueException;
import com.cff.cache.queue.model.Batch;
import com.cff.cache.queue.model.Block;

import java.io.Serializable;
import java.util.List;

public interface CacheQueue<E extends Serializable> {

    /**
     * 获取下一个批次
     * @param bizId 业务ID
     * @return 下一个批次
     * @throws CacheQueueException 缓存队列异常
     */
    Batch<E> nextBatch(String bizId) throws CacheQueueException;

    /**
     * 获取下一个块
     * @param bizId 业务ID
     * @param batchId 批次ID
     * @return 下一个块
     * @throws CacheQueueException 缓存队列异常
     */
    Block<E> nextBlock(String bizId, String batchId) throws CacheQueueException;

    /**
     * 添加批次
     * @param bizId 业务ID
     * @param batchId 批次ID
     * @param blockSize 块大小
     * @param list 任务列表
     * @throws CacheQueueException 缓存队列异常
     */
    void addBatch(String bizId, String batchId, Integer blockSize, List<E> list) throws CacheQueueException;

    /**
     * 添加块
     * @param bizId 业务ID
     * @param batchId 批次ID
     * @param blockIndex 块索引
     * @param list 任务列表
     * @throws CacheQueueException 缓存队列异常
     */
    void addBlock(String bizId, String batchId, Integer blockIndex, List<E> list) throws CacheQueueException;
    
    /**
     * 标记块为已消费
     * @param bizId 业务ID
     * @param batchId 批次ID
     * @param blockIndex 块索引
     * @throws CacheQueueException 缓存队列异常
     */
    void markBlockConsumed(String bizId, String batchId, Integer blockIndex) throws CacheQueueException;
    
    /**
     * 获取批次信息
     * @param bizId 业务ID
     * @param batchId 批次ID
     * @return 批次信息
     * @throws CacheQueueException 缓存队列异常
     */
    Batch<E> getBatch(String bizId, String batchId) throws CacheQueueException;
    
    /**
     * 获取块信息
     * @param bizId 业务ID
     * @param batchId 批次ID
     * @param blockIndex 块索引
     * @return 块信息
     * @throws CacheQueueException 缓存队列异常
     */
    Block<E> getBlock(String bizId, String batchId, Integer blockIndex) throws CacheQueueException;
    
    /**
     * 删除批次
     * @param bizId 业务ID
     * @param batchId 批次ID
     * @throws CacheQueueException 缓存队列异常
     */
    void removeBatch(String bizId, String batchId) throws CacheQueueException;
    
    /**
     * 删除块
     * @param bizId 业务ID
     * @param batchId 批次ID
     * @param blockIndex 块索引
     * @throws CacheQueueException 缓存队列异常
     */
    void removeBlock(String bizId, String batchId, Integer blockIndex) throws CacheQueueException;
}