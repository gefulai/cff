package com.cff.common.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.protocol.BasicAsyncResponseConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public final class AsyncApacheHttpUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncApacheHttpUtils.class);

    // 默认超时时间（毫秒）
    private static final long DEFAULT_TIMEOUT = 60000L;

    /**
     * 代理配置类
     */
    public static class ProxyConfig {
        private final String host;
        private final Integer port;
        private final String username;
        private final String password;

        public ProxyConfig(String host, Integer port) {
            this(host, port, null, null);
        }

        public ProxyConfig(String host, Integer port, String username, String password) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
        }

        public String getHost() { return host; }
        public Integer getPort() { return port; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }

    /**
     * 客户端配置类
     */
    public static class ClientConfig {
        private final String key;
        private final boolean async;
        private final ProxyConfig proxyConfig;

        private ClientConfig(Builder builder) {
            this.key = builder.key;
            this.async = builder.async;
            this.proxyConfig = builder.proxyConfig;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String key = "default";
            private boolean async = true;
            private ProxyConfig proxyConfig;

            public Builder key(String key) {
                this.key = key;
                return this;
            }

            public Builder async(boolean async) {
                this.async = async;
                return this;
            }

            public Builder proxyConfig(ProxyConfig proxyConfig) {
                this.proxyConfig = proxyConfig;
                return this;
            }

            public ClientConfig build() {
                return new ClientConfig(this);
            }
        }
    }

    /**
     * GET请求 - 异步
     */
    public static Future<HttpResponse> getAsync(String url,
                                                Map<String, String> headers,
                                                ClientConfig clientConfig) {
        return executeRequestAsync(HttpGet.METHOD_NAME, url, null, null, headers, clientConfig);
    }

    /**
     * POST JSON请求 - 异步
     */
    public static Future<HttpResponse> postAsync(String url,
                                                 String jsonBody,
                                                 Map<String, String> headers,
                                                 ClientConfig clientConfig) {
        return executeRequestAsync(HttpPost.METHOD_NAME, url, jsonBody, ContentType.APPLICATION_JSON, headers, clientConfig);
    }

    /**
     * GET请求 - 同步
     */
    public static HttpResponse getSync(String url,
                                       Map<String, String> headers,
                                       ClientConfig clientConfig,
                                       long timeout) throws ExecutionException, InterruptedException, TimeoutException {
        Future<HttpResponse> future = getAsync(url, headers, clientConfig);
        return future.get(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * POST JSON请求 - 同步
     */
    public static HttpResponse postSync(String url,
                                        String jsonBody,
                                        Map<String, String> headers,
                                        ClientConfig clientConfig,
                                        long timeout) throws ExecutionException, InterruptedException, TimeoutException {
        Future<HttpResponse> future = postAsync(url, jsonBody, headers, clientConfig);
        return future.get(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 执行异步请求的核心方法
     */
    private static Future<HttpResponse> executeRequestAsync(String method,
                                                            String url,
                                                            Object body,
                                                            ContentType contentType,
                                                            Map<String, String> headers,
                                                            ClientConfig clientConfig) {
        // 生成客户端key
        String clientKey = generateClientKey(clientConfig);

        if (clientConfig.async) {
            // 使用异步客户端
            CloseableHttpAsyncClient asyncClient = ApacheHttpClientFactory.createAsyncClient(
                    clientKey,
                    clientConfig.proxyConfig != null ? clientConfig.proxyConfig.getHost() : null,
                    clientConfig.proxyConfig != null ? clientConfig.proxyConfig.getPort() : null,
                    clientConfig.proxyConfig != null ? clientConfig.proxyConfig.getUsername() : null,
                    clientConfig.proxyConfig != null ? clientConfig.proxyConfig.getPassword() : null
            );

            HttpRequestBase request = createHttpRequest(method, url, body, contentType, headers, clientConfig);

            HttpAsyncRequestProducer producer = HttpAsyncMethods.create(request);
            HttpAsyncResponseConsumer<HttpResponse> consumer = new BasicAsyncResponseConsumer();

            return asyncClient.execute(producer, consumer, new FutureCallback<HttpResponse>() {
                @Override
                public void completed(HttpResponse response) {
                    logRequestCompletion(request, response);
                }

                @Override
                public void failed(Exception ex) {
                    LOGGER.error("Async request failed [{} {}]: {}",
                            request.getMethod(), request.getURI(), ex.getMessage());
                }

                @Override
                public void cancelled() {
                    LOGGER.warn("Async request cancelled [{} {}]", request.getMethod(), request.getURI());
                }
            });

        } else {
            // 使用同步客户端，包装为Future
            CloseableHttpClient syncClient = ApacheHttpClientFactory.createSyncClient(
                    clientKey,
                    clientConfig.proxyConfig != null ? clientConfig.proxyConfig.getHost() : null,
                    clientConfig.proxyConfig != null ? clientConfig.proxyConfig.getPort() : null,
                    clientConfig.proxyConfig != null ? clientConfig.proxyConfig.getUsername() : null,
                    clientConfig.proxyConfig != null ? clientConfig.proxyConfig.getPassword() : null
            );

            HttpRequestBase request = createHttpRequest(method, url, body, contentType, headers, clientConfig);

            CompletableFuture<HttpResponse> future = new CompletableFuture<>();

            // 在单独线程中执行同步请求
            CompletableFuture.runAsync(() -> {
                try {
                    HttpResponse response = syncClient.execute(request);
                    future.complete(response);
                    logRequestCompletion(request, response);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                    LOGGER.error("Sync request failed [{} {}]: {}",
                            request.getMethod(), request.getURI(), e.getMessage());
                }
            });

            return future;
        }
    }

    /**
     * 创建HTTP请求对象
     */
    private static HttpRequestBase createHttpRequest(String method,
                                                     String url,
                                                     Object body,
                                                     ContentType contentType,
                                                     Map<String, String> headers,
                                                     ClientConfig clientConfig) {
        HttpRequestBase request;
        switch (method.toUpperCase()) {
            case "GET":
                request = new HttpGet(url);
                break;
            case "POST":
                request = new HttpPost(url);
                setRequestBody((HttpPost) request, body, contentType);
                break;
            case "PUT":
                request = new HttpPut(url);
                setRequestBody((HttpPut) request, body, contentType);
                break;
            case "DELETE":
                request = new HttpDelete(url);
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        // 设置自定义请求头
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request.setHeader(new BasicHeader(entry.getKey(), entry.getValue()));
            }
        }

        // 如果是表单请求且没有设置Content-Type，则设置默认值
        if (body instanceof Map && !request.containsHeader("Content-Type")) {
            request.setHeader("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.toString());
        }
        // 如果是JSON请求且没有设置Content-Type，则设置默认值
        else if (body instanceof String && contentType == ContentType.APPLICATION_JSON
                && !request.containsHeader("Content-Type")) {
            request.setHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
        }

        return request;
    }

    /**
     * 设置请求体
     */
    private static void setRequestBody(HttpEntityEnclosingRequestBase request,
                                       Object body,
                                       ContentType contentType) {
        if (body == null) {
            return;
        }

        if (body instanceof String) {
            String jsonBody = (String) body;
            if (!jsonBody.isEmpty()) {
                StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
                if (contentType != null) {
                    entity.setContentType(contentType.toString());
                }
                request.setEntity(entity);
            }
        } else if (body instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, String> formParams = (Map<String, String>) body;
            if (!formParams.isEmpty()) {
                List<NameValuePair> params = formParams.entrySet().stream()
                        .map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());
                try {
                    request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create form entity", e);
                }
            }
        }
    }

    /**
     * 生成客户端key
     */
    private static String generateClientKey(ClientConfig clientConfig) {
        StringBuilder key = new StringBuilder(clientConfig.key);

        if (clientConfig.proxyConfig != null) {
            key.append("_proxy_")
                    .append(clientConfig.proxyConfig.getHost())
                    .append("_")
                    .append(clientConfig.proxyConfig.getPort());

            if (clientConfig.proxyConfig.getUsername() != null) {
                key.append("_auth");
            }
        }

        key.append("_").append(clientConfig.async ? "async" : "sync");

        return key.toString();
    }

    /**
     * 记录请求完成日志
     */
    private static void logRequestCompletion(HttpRequestBase request, HttpResponse response) {
        try {
            int statusCode = response.getStatusLine().getStatusCode();
            LOGGER.debug("Request completed [{} {}]: Status {}",
                    request.getMethod(), request.getURI(), statusCode);

            if (statusCode >= 200 && statusCode < 300) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                    LOGGER.debug("Response body length: {} characters", responseBody.length());
                }
            } else {
                LOGGER.warn("Request completed with non-success status: {}", statusCode);
            }
        } catch (IOException e) {
            LOGGER.error("Error logging response", e);
        }
    }

    /**
     * 辅助方法：从HttpResponse中获取字符串响应体
     */
    public static String getResponseBody(HttpResponse response) throws IOException {
        if (response.getEntity() == null) {
            return null;
        }
        return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
    }

    /**
     * 辅助方法：从HttpResponse中获取状态码
     */
    public static int getStatusCode(HttpResponse response) {
        return response.getStatusLine().getStatusCode();
    }

    /**
     * 辅助方法：检查响应是否成功（2xx状态码）
     */
    public static boolean isSuccessful(HttpResponse response) {
        int statusCode = getStatusCode(response);
        return statusCode >= 200 && statusCode < 300;
    }
}
