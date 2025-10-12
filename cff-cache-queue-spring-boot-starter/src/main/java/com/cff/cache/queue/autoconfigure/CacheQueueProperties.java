package com.cff.cache.queue.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "cff.cache.queue")
public class CacheQueueProperties {

    private Config config;

    private List<Biz> biz;

    public static class Config {

        private String host = "localhost";

        private int port = 6379;

        private String password;

        private Pool pool;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Pool getPool() {
            return pool;
        }

        public void setPool(Pool pool) {
            this.pool = pool;
        }
    }

    public static class Pool {

        private int maxTotal;

        private int maxIdle;

        private int minIdle;

        private long maxWaitMillis;

        private long connectTimeoutMillis;

        private boolean keepAlive;

        private boolean tcpNoDelay;

        private long timeoutMillis;

        private int ioThreadPoolSize;

        private int computationThreadPoolSize;

        public int getMaxTotal() {
            return maxTotal;
        }

        public void setMaxTotal(int maxTotal) {
            this.maxTotal = maxTotal;
        }

        public int getMaxIdle() {
            return maxIdle;
        }

        public void setMaxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
        }

        public int getMinIdle() {
            return minIdle;
        }

        public void setMinIdle(int minIdle) {
            this.minIdle = minIdle;
        }

        public long getMaxWaitMillis() {
            return maxWaitMillis;
        }

        public void setMaxWaitMillis(long maxWaitMillis) {
            this.maxWaitMillis = maxWaitMillis;
        }

        public long getConnectTimeoutMillis() {
            return connectTimeoutMillis;
        }

        public void setConnectTimeoutMillis(long connectTimeoutMillis) {
            this.connectTimeoutMillis = connectTimeoutMillis;
        }

        public boolean isKeepAlive() {
            return keepAlive;
        }

        public void setKeepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
        }

        public boolean isTcpNoDelay() {
            return tcpNoDelay;
        }

        public void setTcpNoDelay(boolean tcpNoDelay) {
            this.tcpNoDelay = tcpNoDelay;
        }

        public long getTimeoutMillis() {
            return timeoutMillis;
        }

        public void setTimeoutMillis(long timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
        }

        public int getIoThreadPoolSize() {
            return ioThreadPoolSize;
        }

        public void setIoThreadPoolSize(int ioThreadPoolSize) {
            this.ioThreadPoolSize = ioThreadPoolSize;
        }

        public int getComputationThreadPoolSize() {
            return computationThreadPoolSize;
        }

        public void setComputationThreadPoolSize(int computationThreadPoolSize) {
            this.computationThreadPoolSize = computationThreadPoolSize;
        }
    }

    public static class Biz {

        private String bizId;

        private Integer blockSize;

        public String getBizId() {
            return bizId;
        }

        public void setBizId(String bizId) {
            this.bizId = bizId;
        }

        public Integer getBlockSize() {
            return blockSize;
        }

        public void setBlockSize(Integer blockSize) {
            this.blockSize = blockSize;
        }
    }
    
    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public List<Biz> getBiz() {
        return biz;
    }

    public void setBiz(List<Biz> biz) {
        this.biz = biz;
    }
}