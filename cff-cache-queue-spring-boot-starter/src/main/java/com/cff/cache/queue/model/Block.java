package com.cff.cache.queue.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

public class Block<E extends Serializable> implements Serializable {

    private static final long serialVersionUID = -278244762226213149L;

    private final Integer blockIndex;
    
    private final Long createTime;

    private Queue<E> queue;
    
    private boolean consumed = false;

    public Block(Integer blockIndex) {
        this.blockIndex = blockIndex;
        this.createTime = System.currentTimeMillis();
    }

    public Integer getBlockIndex() {
        return blockIndex;
    }

    public Queue<E> getQueue() {
        return queue;
    }

    public void setQueue(Queue<E> queue) {
        this.queue = queue;
    }
    
    public boolean isConsumed() {
        return consumed;
    }
    
    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }
    
    public Long getCreateTime() {
        return createTime;
    }
    
    public void addTask(E task) {
        if (queue == null) {
            queue = new LinkedList<>();
        }
        queue.add(task);
    }
}