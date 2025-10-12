package com.cff.cache.queue.lettuce;

import com.cff.cache.queue.CacheQueue;
import com.cff.cache.queue.exception.CacheQueueException;
import com.cff.cache.queue.model.Batch;
import com.cff.cache.queue.model.Block;
import com.cff.cache.queue.util.CacheQueueConstants;
import com.cff.cache.queue.util.SerializationUtil;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Component
public class LettuceCacheQueue<E extends Serializable> implements CacheQueue<E> {

    private final LettuceConnectionPool connectionPool;

    public LettuceCacheQueue(LettuceConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public Batch<E> nextBatch(String bizId) throws CacheQueueException {
        StatefulRedisConnection<String, String> connection = null;
        try {
            connection = connectionPool.getConnection();
            RedisCommands<String, String> commands = connection.sync();
            String batchKey = String.format(CacheQueueConstants.BATCHES_KEY_TEMPLATE, bizId);
            String batchId = commands.lpop(batchKey);

            if (batchId == null) {
                return null;
            }

            return getBatch(bizId, batchId);
        } catch (Exception e) {
            throw new CacheQueueException("Failed to get next batch", e);
        } finally {
            if (connection != null) {
                try {
                    connectionPool.close(connection);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    @Override
    public Block<E> nextBlock(String bizId, String batchId) throws CacheQueueException {
        StatefulRedisConnection<String, String> connection = null;
        try {
            connection = connectionPool.getConnection();
            RedisCommands<String, String> commands = connection.sync();
            
            String blockKey = String.format(CacheQueueConstants.BLOCKS_KEY_TEMPLATE, bizId, batchId);
            String blockIndexStr = commands.lpop(blockKey);
            
            if (blockIndexStr == null) {
                return null;
            }
            
            Integer blockIndex = Integer.valueOf(blockIndexStr);
            return getBlock(bizId, batchId, blockIndex);
        } catch (Exception e) {
            throw new CacheQueueException("Failed to get next block", e);
        } finally {
            if (connection != null) {
                try {
                    connectionPool.close(connection);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    @Override
    public void addBatch(String bizId, String batchId, Integer blockSize, List<E> list) throws CacheQueueException {
        StatefulRedisConnection<String, String> connection = null;
        try {
            connection = connectionPool.getConnection();
            RedisCommands<String, String> commands = connection.sync();
            // 计算块数量
            int blockCount = (int) Math.ceil((double) list.size() / blockSize);

            // 保存批次信息
            String batchInfoKey = String.format(CacheQueueConstants.BATCH_INFO_KEY_TEMPLATE, bizId, batchId);
            commands.hset(batchInfoKey, CacheQueueConstants.BATCH_BIZ_ID, bizId);
            commands.hset(batchInfoKey, CacheQueueConstants.BATCH_BATCH_ID, batchId);
            commands.hset(batchInfoKey, CacheQueueConstants.BATCH_BLOCK_SIZE, String.valueOf(blockSize));
            commands.hset(batchInfoKey, CacheQueueConstants.BATCH_BLOCK_COUNT, String.valueOf(blockCount));
            commands.hset(batchInfoKey, CacheQueueConstants.BATCH_CONSUMED_BLOCK_COUNT, "0");

            // 将任务列表拆分为块并保存
            for (int i = 0; i < blockCount; i++) {
                int startIndex = i * blockSize;
                int endIndex = Math.min(startIndex + blockSize, list.size());
                List<E> subList = list.subList(startIndex, endIndex);

                addBlock(bizId, batchId, i, subList);
            }

            // 将批次ID添加到批次队列中
            String batchKey = String.format(CacheQueueConstants.BATCHES_KEY_TEMPLATE, bizId);
            commands.rpush(batchKey, batchId);
        } catch (Exception e) {
            throw new CacheQueueException("Failed to add batch", e);
        } finally {
            if (connection != null) {
                try {
                    connectionPool.close(connection);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    @Override
    public void addBlock(String bizId, String batchId, Integer blockIndex, List<E> list) throws CacheQueueException {
        StatefulRedisConnection<String, String> connection = null;
        try {
            connection = connectionPool.getConnection();
            RedisCommands<String, String> commands = connection.sync();
            
            // 保存块信息
            String blockInfoKey = String.format(CacheQueueConstants.BLOCK_INFO_KEY_TEMPLATE, bizId, batchId, blockIndex);
            commands.hset(blockInfoKey, CacheQueueConstants.BLOCK_INDEX, String.valueOf(blockIndex));
            commands.hset(blockInfoKey, CacheQueueConstants.BLOCK_CONSUMED, "false");
            
            // 保存块中的任务列表
            String blockDataKey = String.format(CacheQueueConstants.BLOCK_DATA_KEY_TEMPLATE, bizId, batchId, blockIndex);
            for (E item : list) {
                try {
                    String serializedItem = SerializationUtil.serializeToString(item);
                    commands.rpush(blockDataKey, serializedItem);
                } catch (Exception e) {
                    throw new CacheQueueException("Failed to serialize item", e);
                }
            }
            
            // 将块索引添加到块队列中
            String blockKey = String.format(CacheQueueConstants.BLOCKS_KEY_TEMPLATE, bizId, batchId);
            commands.rpush(blockKey, String.valueOf(blockIndex));
        } catch (CacheQueueException e) {
            throw e;
        } catch (Exception e) {
            throw new CacheQueueException("Failed to add block", e);
        } finally {
            if (connection != null) {
                try {
                    connectionPool.close(connection);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }
    
    @Override
    public void markBlockConsumed(String bizId, String batchId, Integer blockIndex) throws CacheQueueException {
        StatefulRedisConnection<String, String> connection = null;
        try {
            connection = connectionPool.getConnection();
            RedisCommands<String, String> commands = connection.sync();
            
            // 更新块的消费状态
            String blockInfoKey = String.format(CacheQueueConstants.BLOCK_INFO_KEY_TEMPLATE, bizId, batchId, blockIndex);
            commands.hset(blockInfoKey, CacheQueueConstants.BLOCK_CONSUMED, "true");
            
            // 增加批次的已消费块计数
            String batchInfoKey = String.format(CacheQueueConstants.BATCH_INFO_KEY_TEMPLATE, bizId, batchId);
            String consumedBlockCountStr = commands.hget(batchInfoKey, CacheQueueConstants.BATCH_CONSUMED_BLOCK_COUNT);
            int consumedBlockCount = consumedBlockCountStr != null ? Integer.parseInt(consumedBlockCountStr) : 0;
            commands.hset(batchInfoKey, CacheQueueConstants.BATCH_CONSUMED_BLOCK_COUNT, String.valueOf(consumedBlockCount + 1));
        } catch (Exception e) {
            throw new CacheQueueException("Failed to mark block consumed", e);
        } finally {
            if (connection != null) {
                try {
                    connectionPool.close(connection);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }
    
    @Override
    public Batch<E> getBatch(String bizId, String batchId) throws CacheQueueException {
        StatefulRedisConnection<String, String> connection = null;
        try {
            connection = connectionPool.getConnection();
            RedisCommands<String, String> commands = connection.sync();
            
            String batchInfoKey = String.format(CacheQueueConstants.BATCH_INFO_KEY_TEMPLATE, bizId, batchId);
            String blockSizeStr = commands.hget(batchInfoKey, CacheQueueConstants.BATCH_BLOCK_SIZE);
            String blockCountStr = commands.hget(batchInfoKey, CacheQueueConstants.BATCH_BLOCK_COUNT);
            String consumedBlockCountStr = commands.hget(batchInfoKey, CacheQueueConstants.BATCH_CONSUMED_BLOCK_COUNT);
            
            if (blockSizeStr == null || blockCountStr == null) {
                return null;
            }
            
            Integer blockSize = Integer.valueOf(blockSizeStr);
            Integer blockCount = Integer.valueOf(blockCountStr);
            Integer consumedBlockCount = consumedBlockCountStr != null ? Integer.valueOf(consumedBlockCountStr) : 0;
            
            Batch<E> batch = new Batch<>(bizId, batchId, blockSize, blockCount);
            batch.setConsumedBlockCount(consumedBlockCount);
            return batch;
        } catch (Exception e) {
            throw new CacheQueueException("Failed to get batch", e);
        } finally {
            if (connection != null) {
                try {
                    connectionPool.close(connection);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }
    
    @Override
    public Block<E> getBlock(String bizId, String batchId, Integer blockIndex) throws CacheQueueException {
        StatefulRedisConnection<String, String> connection = null;
        try {
            connection = connectionPool.getConnection();
            RedisCommands<String, String> commands = connection.sync();
            
            String blockInfoKey = String.format(CacheQueueConstants.BLOCK_INFO_KEY_TEMPLATE, bizId, batchId, blockIndex);
            String consumedStr = commands.hget(blockInfoKey, CacheQueueConstants.BLOCK_CONSUMED);
            
            Block<E> block = new Block<>(blockIndex);
            if (consumedStr != null) {
                block.setConsumed(Boolean.parseBoolean(consumedStr));
            }
            
            // 获取块中的任务列表
            String blockDataKey = String.format(CacheQueueConstants.BLOCK_DATA_KEY_TEMPLATE, bizId, batchId, blockIndex);
            List<String> serializedTasks = commands.lrange(blockDataKey, 0, -1);
            if (serializedTasks != null && !serializedTasks.isEmpty()) {
                Queue<E> taskQueue = new LinkedList<>();
                for (String serializedTask : serializedTasks) {
                    try {
                        @SuppressWarnings("unchecked")
                        E task = (E) SerializationUtil.deserializeFromString(serializedTask);
                        taskQueue.add(task);
                    } catch (Exception e) {
                        throw new CacheQueueException("Failed to deserialize task", e);
                    }
                }
                block.setQueue(taskQueue);
            }
            
            return block;
        } catch (Exception e) {
            throw new CacheQueueException("Failed to get block", e);
        } finally {
            if (connection != null) {
                try {
                    connectionPool.close(connection);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }
    
    @Override
    public void removeBatch(String bizId, String batchId) throws CacheQueueException {
        StatefulRedisConnection<String, String> connection = null;
        try {
            connection = connectionPool.getConnection();
            RedisCommands<String, String> commands = connection.sync();
            
            // 删除批次信息
            String batchInfoKey = String.format(CacheQueueConstants.BATCH_INFO_KEY_TEMPLATE, bizId, batchId);
            commands.del(batchInfoKey);
            
            // 删除批次下的所有块信息
            Batch<E> batch = getBatch(bizId, batchId);
            if (batch != null) {
                for (int i = 0; i < batch.getBlockCount(); i++) {
                    removeBlock(bizId, batchId, i);
                }
            }
            
            // 从批次队列中移除批次ID
            String batchKey = String.format(CacheQueueConstants.BATCHES_KEY_TEMPLATE, bizId);
            commands.lrem(batchKey, 1, batchId);
        } catch (Exception e) {
            throw new CacheQueueException("Failed to remove batch", e);
        } finally {
            if (connection != null) {
                try {
                    connectionPool.close(connection);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }
    
    @Override
    public void removeBlock(String bizId, String batchId, Integer blockIndex) throws CacheQueueException {
        StatefulRedisConnection<String, String> connection = null;
        try {
            connection = connectionPool.getConnection();
            RedisCommands<String, String> commands = connection.sync();
            
            // 删除块信息
            String blockInfoKey = String.format(CacheQueueConstants.BLOCK_INFO_KEY_TEMPLATE, bizId, batchId, blockIndex);
            commands.del(blockInfoKey);
            
            // 删除块数据
            String blockDataKey = String.format(CacheQueueConstants.BLOCK_DATA_KEY_TEMPLATE, bizId, batchId, blockIndex);
            commands.del(blockDataKey);
            
            // 从块队列中移除块索引
            String blockKey = String.format(CacheQueueConstants.BLOCKS_KEY_TEMPLATE, bizId, batchId);
            commands.lrem(blockKey, 1, String.valueOf(blockIndex));
        } catch (Exception e) {
            throw new CacheQueueException("Failed to remove block", e);
        } finally {
            if (connection != null) {
                try {
                    connectionPool.close(connection);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }
}