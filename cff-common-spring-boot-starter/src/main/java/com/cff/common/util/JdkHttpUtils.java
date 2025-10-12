package com.cff.common.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class JdkHttpUtils {

    private JdkHttpUtils() {
    }

    private static final long DEFAULT_CONNECT_TIMEOUT = 10L;
    private static final long DEFAULT_TIMEOUT = 30L;

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(DEFAULT_CONNECT_TIMEOUT)) // 连接超时时间
            .followRedirects(HttpClient.Redirect.NORMAL) // 跟随重定向
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    public static String get(String url) throws Exception {
        return get(url, null);
    }

    public static String get(String url, Map<String, String> headers) throws Exception {
        return get(url, headers, DEFAULT_TIMEOUT);
    }

    /**
     * GET同步请求（带请求头）
     *
     * @param url     请求URL
     * @param headers 请求头
     * @param timeout 请求超时时间
     * @return 响应字符串
     * @throws Exception 请求异常
     */
    public static String get(String url, Map<String, String> headers, long timeout) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofSeconds(timeout))
                .GET();
        // 添加请求头
        if (headers != null) {
            headers.forEach(builder::header);
        }

        return sendAndGetBody(builder);
    }

    /**
     * GET异步请求
     *
     * @param url     请求URL
     * @param headers 请求头
     * @param timeout 请求超时时间
     * @return CompletableFuture<String>
     */
    public static CompletableFuture<String> getAsync(String url, Map<String, String> headers, long timeout) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofSeconds(timeout))
                .GET();
        // 添加请求头
        if (headers != null) {
            headers.forEach(builder::header);
        }

        return sendAsyncAndGetBody(builder);
    }

    public static String post(String url, String jsonBody) throws Exception {
        return post(url, jsonBody, null, DEFAULT_TIMEOUT);
    }

    /**
     * POST同步请求（JSON格式）
     *
     * @param url      请求URL
     * @param jsonBody JSON请求体
     * @param timeout  请求超时时间
     * @return 响应字符串
     * @throws Exception 请求异常
     */
    public static String post(String url, String jsonBody, Map<String, String> headers, long timeout) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofSeconds(timeout))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

        if (headers != null) {
            headers.forEach(builder::header);
        }

        return sendAndGetBody(builder);
    }

    public static CompletableFuture<String> postAsync(String url, String jsonBody) {
        return postAsync(url, jsonBody, null, DEFAULT_TIMEOUT);
    }

    /**
     * POST异步请求（JSON格式）
     *
     * @param url      请求URL
     * @param jsonBody JSON请求体
     * @param timeout  请求超时时间
     * @return CompletableFuture<String>
     */
    public static CompletableFuture<String> postAsync(String url, String jsonBody, Map<String, String> headers,
                                                      long timeout) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofSeconds(timeout))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

        if (headers != null) {
            headers.forEach(builder::header);
        }

        return sendAsyncAndGetBody(builder);
    }

    public static HttpResponse<String> send(HttpRequest.Builder builder) throws Exception {
        HttpRequest request = builder.version(HttpClient.Version.HTTP_1_1).build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    /**
     * 自定义同步请求
     *
     * @param builder 请求构建器
     * @return 响应字符串
     * @throws Exception 请求异常
     */
    public static String sendAndGetBody(HttpRequest.Builder builder) throws Exception {
        return send(builder).body();
    }

    public static CompletableFuture<HttpResponse<String>> sendAsync(HttpRequest.Builder builder) {
        HttpRequest request = builder.version(HttpClient.Version.HTTP_1_1).build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    /**
     * 自定义异步请求
     *
     * @param builder 请求构建器
     * @return CompletableFuture<String>
     */
    public static CompletableFuture<String> sendAsyncAndGetBody(HttpRequest.Builder builder) {
        return sendAsync(builder).thenApply(HttpResponse::body);
    }
}
