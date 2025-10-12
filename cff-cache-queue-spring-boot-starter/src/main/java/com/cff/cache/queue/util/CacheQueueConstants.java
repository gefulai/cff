package com.cff.cache.queue.util;

public class CacheQueueConstants {
    
    /**
     * Redis键前缀
     */
    public static final String CACHE_QUEUE_PREFIX = "cache_queue:";
    
    /**
     * 批次队列键模板
     */
    public static final String BATCHES_KEY_TEMPLATE = CACHE_QUEUE_PREFIX + "%s:batches";
    
    /**
     * 批次信息键模板
     */
    public static final String BATCH_INFO_KEY_TEMPLATE = CACHE_QUEUE_PREFIX + "%s:batch:%s:info";
    
    /**
     * 块队列键模板
     */
    public static final String BLOCKS_KEY_TEMPLATE = CACHE_QUEUE_PREFIX + "%s:batch:%s:blocks";
    
    /**
     * 块信息键模板
     */
    public static final String BLOCK_INFO_KEY_TEMPLATE = CACHE_QUEUE_PREFIX + "%s:batch:%s:block:%s:info";
    
    /**
     * 块数据键模板
     */
    public static final String BLOCK_DATA_KEY_TEMPLATE = CACHE_QUEUE_PREFIX + "%s:batch:%s:block:%s:data";
    
    /**
     * 批次信息字段名
     */
    public static final String BATCH_BIZ_ID = "bizId";
    public static final String BATCH_BATCH_ID = "batchId";
    public static final String BATCH_BLOCK_SIZE = "blockSize";
    public static final String BATCH_BLOCK_COUNT = "blockCount";
    public static final String BATCH_CONSUMED_BLOCK_COUNT = "consumedBlockCount";
    
    /**
     * 块信息字段名
     */
    public static final String BLOCK_INDEX = "blockIndex";
    public static final String BLOCK_CONSUMED = "consumed";
}