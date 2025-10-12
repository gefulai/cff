package com.cff.cache.queue.model;

import java.io.Serializable;
import java.util.Queue;

public class Batch<E extends Serializable> implements Serializable {

    private static final long serialVersionUID = 6385363806011267917L;

    private final String bizId;

    private final String batchId;

    private final Long startTime;

    private final Integer blockSize;
    
    private final Integer blockCount;

    private Queue<Block<E>> blocks;
    
    private Integer consumedBlockCount = 0;

    public Batch(String bizId, String batchId, Integer blockSize, Integer blockCount) {
        this.bizId = bizId;
        this.batchId = batchId;
        this.startTime = System.currentTimeMillis();
        this.blockSize = blockSize;
        this.blockCount = blockCount;
    }

    public String getBizId() {
        return bizId;
    }

    public String getBatchId() {
        return batchId;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Queue<Block<E>> getBlocks() {
        return blocks;
    }

    public void setBlocks(Queue<Block<E>> blocks) {
        this.blocks = blocks;
    }
    
    public Integer getBlockSize() {
        return blockSize;
    }
    
    public Integer getBlockCount() {
        return blockCount;
    }
    
    public Integer getConsumedBlockCount() {
        return consumedBlockCount;
    }
    
    public void setConsumedBlockCount(Integer consumedBlockCount) {
        this.consumedBlockCount = consumedBlockCount;
    }
    
    public void incrementConsumedBlockCount() {
        this.consumedBlockCount++;
    }
    
    public boolean isCompleted() {
        return consumedBlockCount >= blockCount;
    }
}