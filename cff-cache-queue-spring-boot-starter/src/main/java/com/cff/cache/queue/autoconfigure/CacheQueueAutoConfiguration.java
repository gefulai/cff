package com.cff.cache.queue.autoconfigure;

import com.cff.cache.queue.CacheQueue;
import com.cff.cache.queue.lettuce.LettuceCacheQueue;
import com.cff.cache.queue.lettuce.LettuceConnectionPool;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.io.Serializable;
import java.time.Duration;

@AutoConfiguration
@ConditionalOnClass({RedisClient.class})
@EnableConfigurationProperties(CacheQueueProperties.class)
public class CacheQueueAutoConfiguration {

    @Autowired
    private CacheQueueProperties cacheQueueProperties;

    @Bean
    @ConditionalOnMissingBean
    public RedisClient redisClient() {
        CacheQueueProperties.Config config = cacheQueueProperties.getConfig();
        if (config == null) {
            config = new CacheQueueProperties.Config();
        }

        RedisURI.Builder builder = RedisURI.Builder
                .redis(config.getHost(), config.getPort());
        
        if (config.getPassword() != null && !config.getPassword().isEmpty()) {
            builder.withPassword(config.getPassword().toCharArray());
        }

        CacheQueueProperties.Pool pool = config.getPool();

        DefaultClientResources clientResources = DefaultClientResources.builder()
                .ioThreadPoolSize(pool.getIoThreadPoolSize())
                .computationThreadPoolSize(pool.getComputationThreadPoolSize())
                .build();
        RedisClient redisClient = RedisClient.create(clientResources, builder.build());

        // 全局设置ClientOptions
        // 创建 SocketOptions
        SocketOptions socketOptions = SocketOptions.builder()
                // 连接超时
                .connectTimeout(Duration.ofMillis(pool.getConnectTimeoutMillis()))
                .keepAlive(pool.isKeepAlive())
                .tcpNoDelay(pool.isTcpNoDelay())
                .build();

        // 创建 ClientOptions 并设置 SocketOptions
        ClientOptions clientOptions = ClientOptions.builder()
                .socketOptions(socketOptions)
                // 自动重连
                .autoReconnect(true)
                .pingBeforeActivateConnection(true)
                //.. Redis 命令设置统一的超时控制机制
                .timeoutOptions(TimeoutOptions.enabled(Duration.ofMillis(pool.getTimeoutMillis())))
                .build();

        redisClient.setOptions(clientOptions);
        return redisClient;
    }

    @Bean
    @ConditionalOnMissingBean
    public LettuceConnectionPool lettuceConnectionPool(RedisClient redisClient) {
        // 创建连接池配置
        GenericObjectPoolConfig<StatefulRedisConnection<String, String>> poolConfig = 
            new GenericObjectPoolConfig<>();
        
        // 获取配置中的连接池设置
        CacheQueueProperties.Config config = cacheQueueProperties.getConfig();
        if (config != null && config.getPool() != null) {
            CacheQueueProperties.Pool pool = config.getPool();
            
            // 设置连接池参数
            if (pool.getMaxTotal() > 0) {
                poolConfig.setMaxTotal(pool.getMaxTotal());
            }
            if (pool.getMaxIdle() > 0) {
                poolConfig.setMaxIdle(pool.getMaxIdle());
            }
            if (pool.getMinIdle() >= 0) {
                poolConfig.setMinIdle(pool.getMinIdle());
            }
            if (pool.getMaxWaitMillis() > 0) {
                poolConfig.setMaxWait(Duration.ofMillis(pool.getMaxWaitMillis()));
            }
        }

        GenericObjectPool<StatefulRedisConnection<String, String>> pool =
                ConnectionPoolSupport.createGenericObjectPool(() -> redisClient.connect(StringCodec.UTF8), poolConfig);
        return new LettuceConnectionPool(pool);
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheQueue<Serializable> lettuceCacheQueue(LettuceConnectionPool connectionPool) {
        return new LettuceCacheQueue<>(connectionPool);
    }
}