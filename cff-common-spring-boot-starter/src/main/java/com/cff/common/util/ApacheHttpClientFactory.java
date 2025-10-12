package com.cff.common.util;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ApacheHttpClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApacheHttpClientFactory.class);

    // 同步HTTP客户端缓存
    private static final Map<String, CloseableHttpClient> SYNC_CLIENTS = new ConcurrentHashMap<>();
    // 异步HTTP客户端缓存
    private static final Map<String, CloseableHttpAsyncClient> ASYNC_CLIENTS = new ConcurrentHashMap<>();
    // 默认连接池配置
    private static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 1000;
    private static final int DEFAULT_MAX_ROUTE_CONNECTIONS = 500;
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    private static final int DEFAULT_SOCKET_TIMEOUT = 60000;
    private static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT = 5000;

    private ApacheHttpClientFactory() {
    }

    /**
     * 创建同步HTTP客户端
     *
     * @param key           客户端唯一标识
     * @param proxyHost     代理主机
     * @param proxyPort     代理端口
     * @param proxyUsername 代理用户名
     * @param proxyPassword 代理密码
     * @return CloseableHttpClient实例
     */
    public static CloseableHttpClient createSyncClient(String key,
                                                       String proxyHost, Integer proxyPort,
                                                       String proxyUsername, String proxyPassword) {
        return SYNC_CLIENTS.computeIfAbsent(key, k -> {
            try {
                // 创建连接池管理器
                PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
                connectionManager.setMaxTotal(DEFAULT_MAX_TOTAL_CONNECTIONS);
                connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_ROUTE_CONNECTIONS);

                // 创建请求配置
                RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                        .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
                        .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT)
                        .setConnectionRequestTimeout(DEFAULT_CONNECTION_REQUEST_TIMEOUT);

                // 配置代理
                if (proxyHost != null && proxyPort != null) {
                    HttpHost proxy = new HttpHost(proxyHost, proxyPort);
                    requestConfigBuilder.setProxy(proxy);
                }
                RequestConfig requestConfig = requestConfigBuilder.build();

                // 创建认证提供者
                BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                // 配置代理认证
                if (proxyHost != null && proxyPort != null && proxyUsername != null && proxyPassword != null) {
                    credentialsProvider.setCredentials(
                            new AuthScope(proxyHost, proxyPort),
                            new UsernamePasswordCredentials(proxyUsername, proxyPassword)
                    );
                }

                // 构建HTTP客户端
                CloseableHttpClient httpClient = HttpClientBuilder.create()
                        .setConnectionManager(connectionManager)
                        .setDefaultRequestConfig(requestConfig)
                        .setDefaultCredentialsProvider(credentialsProvider)
                        .evictExpiredConnections() // 定期清理过期连接
                        .evictIdleConnections(60, TimeUnit.SECONDS) // 清理空闲连接
                        .build();

                LOGGER.info("Created sync HTTP client with key: {}", key);
                return httpClient;
            } catch (Exception e) {
                LOGGER.error("Failed to create sync HTTP client with key: {}", key, e);
                throw new RuntimeException("Failed to create HTTP client", e);
            }
        });
    }

    /**
     * 创建异步HTTP客户端
     *
     * @param key           客户端唯一标识
     * @param proxyHost     代理主机
     * @param proxyPort     代理端口
     * @param proxyUsername 代理用户名
     * @param proxyPassword 代理密码
     * @return CloseableHttpAsyncClient实例
     */
    public static CloseableHttpAsyncClient createAsyncClient(String key,
                                                             String proxyHost, Integer proxyPort,
                                                             String proxyUsername, String proxyPassword) {
        return ASYNC_CLIENTS.computeIfAbsent(key, k -> {
            try {
                // 创建I/O反应器
                IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                        .setIoThreadCount(Runtime.getRuntime().availableProcessors())
                        .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
                        .setSoTimeout(DEFAULT_SOCKET_TIMEOUT)
                        .build();
                ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);

                // 创建异步连接池管理器
                PoolingNHttpClientConnectionManager connectionManager =
                        new PoolingNHttpClientConnectionManager(ioReactor);
                connectionManager.setMaxTotal(DEFAULT_MAX_TOTAL_CONNECTIONS);
                connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_ROUTE_CONNECTIONS);

                // 创建请求配置
                RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                        .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
                        .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT)
                        .setConnectionRequestTimeout(DEFAULT_CONNECTION_REQUEST_TIMEOUT);

                // 配置代理
                if (proxyHost != null && proxyPort != null) {
                    HttpHost proxy = new HttpHost(proxyHost, proxyPort);
                    requestConfigBuilder.setProxy(proxy);
                }

                RequestConfig requestConfig = requestConfigBuilder.build();

                // 创建认证提供者
                BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                // 配置代理认证
                if (proxyHost != null && proxyPort != null && proxyUsername != null && proxyPassword != null) {
                    credentialsProvider.setCredentials(
                            new AuthScope(proxyHost, proxyPort),
                            new UsernamePasswordCredentials(proxyUsername, proxyPassword)
                    );
                }

                // 构建异步HTTP客户端
                CloseableHttpAsyncClient httpClient = HttpAsyncClientBuilder.create()
                        .setConnectionManager(connectionManager)
                        .setDefaultRequestConfig(requestConfig)
                        .setDefaultCredentialsProvider(credentialsProvider)
                        .build();

                // 启动客户端
                httpClient.start();
                LOGGER.info("Created async HTTP client with key: {}", key);
                return httpClient;

            } catch (IOReactorException e) {
                LOGGER.error("Failed to create async HTTP client with key: {}", key, e);
                throw new RuntimeException("Failed to create async HTTP client", e);
            }
        });
    }

    /**
     * 关闭所有同步HTTP客户端
     */
    public static void shutdownAllSyncClients() {
        for (Map.Entry<String, CloseableHttpClient> entry : SYNC_CLIENTS.entrySet()) {
            try {
                entry.getValue().close();
                LOGGER.info("Closed sync HTTP client with key: {}", entry.getKey());
            } catch (Exception e) {
                LOGGER.error("Failed to close sync HTTP client with key: {}", entry.getKey(), e);
            }
        }
        SYNC_CLIENTS.clear();
    }

    /**
     * 关闭所有异步HTTP客户端
     */
    public static void shutdownAllAsyncClients() {
        for (Map.Entry<String, CloseableHttpAsyncClient> entry : ASYNC_CLIENTS.entrySet()) {
            try {
                entry.getValue().close();
                LOGGER.info("Closed async HTTP client with key: {}", entry.getKey());
            } catch (Exception e) {
                LOGGER.error("Failed to close async HTTP client with key: {}", entry.getKey(), e);
            }
        }
        ASYNC_CLIENTS.clear();
    }

    /**
     * 关闭所有客户端
     */
    public static void shutdownAll() {
        shutdownAllSyncClients();
        shutdownAllAsyncClients();
    }
}
