package com.cff.cache.queue.lettuce;

import io.lettuce.core.api.StatefulRedisConnection;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LettuceConnectionPool {

    private static final Logger logger = LoggerFactory.getLogger(LettuceConnectionPool.class);

    private final GenericObjectPool<StatefulRedisConnection<String, String>> connectionPool;

    public LettuceConnectionPool(GenericObjectPool<StatefulRedisConnection<String, String>> connectionPool) {
        this.connectionPool = connectionPool;
    }

    public StatefulRedisConnection<String, String> getConnection() throws Exception {
        return connectionPool.borrowObject();
    }

    public void close(StatefulRedisConnection<String, String> connection) {
        try {
            connectionPool.returnObject(connection);
        } catch (Exception e) {
            logger.error("Returning connection to pool error", e);
        }
    }
}
