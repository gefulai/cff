# 连接池配置示例

## 1. 配置文件示例

在application.yml中添加以下配置来设置连接池参数：

```yaml
cff:
  cache:
    queue:
      config:
        host: localhost
        port: 6379
        password: your_password # 如果没有密码可以不配置
        # 连接池配置
        pool:
          maxTotal: 20          # 最大连接数
          maxIdle: 10           # 最大空闲连接数
          minIdle: 2            # 最小空闲连接数
          maxWaitMillis: 2000   # 获取连接的最大等待时间（毫秒）
          # 以下是一些可选的高级配置
          connectTimeoutMillis: 2000   # 连接超时时间（毫秒）
          keepAlive: true              # 是否保持连接
          tcpNoDelay: true             # 是否禁用Nagle算法
          timeoutMillis: 2000          # 操作超时时间（毫秒）
          ioThreadPoolSize: 4          # IO线程池大小
          computationThreadPoolSize: 4 # 计算线程池大小
      biz:
        - bizId: example_biz
          blockSize: 100
```

## 2. 配置参数说明

### 基本连接池参数

- `maxTotal`: 最大连接数，控制连接池中最多可以有多少个连接
- `maxIdle`: 最大空闲连接数，控制连接池中最多可以有多少个空闲连接
- `minIdle`: 最小空闲连接数，控制连接池中最少需要保持多少个空闲连接
- `maxWaitMillis`: 获取连接的最大等待时间，当连接池中没有可用连接时，等待获取连接的最大时间

### 高级连接参数

- `connectTimeoutMillis`: 连接超时时间，建立Redis连接的超时时间
- `keepAlive`: 是否保持连接，启用TCP keep-alive机制
- `tcpNoDelay`: 是否禁用Nagle算法，启用可降低延迟但可能增加网络流量
- `timeoutMillis`: 操作超时时间，执行Redis命令的超时时间
- `ioThreadPoolSize`: IO线程池大小，处理网络IO操作的线程数
- `computationThreadPoolSize`: 计算线程池大小，处理计算密集型任务的线程数

## 3. 配置建议

### 开发环境
```yaml
cff:
  cache:
    queue:
      config:
        host: localhost
        port: 6379
        pool:
          maxTotal: 10
          maxIdle: 5
          minIdle: 1
          maxWaitMillis: 1000
```

### 生产环境
```yaml
cff:
  cache:
    queue:
      config:
        host: your-redis-host
        port: 6379
        password: your-password
        pool:
          maxTotal: 50
          maxIdle: 20
          minIdle: 5
          maxWaitMillis: 3000
```

## 4. 监控和调优

### 监控连接池状态
可以通过以下方式监控连接池的状态：

1. **通过Redis客户端监控**：
   ```bash
   # 查看当前连接数
   INFO clients
   
   # 查看内存使用情况
   INFO memory
   ```

2. **通过应用程序日志监控**：
   在应用程序中添加连接池状态的日志输出：
   ```java
   @Autowired
   private GenericObjectPool<StatefulRedisConnection<String, String>> connectionPool;
   
   public void logPoolStatus() {
       logger.info("Active connections: {}", connectionPool.getNumActive());
       logger.info("Idle connections: {}", connectionPool.getNumIdle());
       logger.info("Total connections: {}", connectionPool.getMaxTotal());
   }
   ```

### 调优建议

1. **根据并发量调整maxTotal**：
   - 估算应用程序的最大并发请求数
   - 设置maxTotal略大于最大并发数

2. **合理设置空闲连接数**：
   - maxIdle不宜过大，避免浪费资源
   - minIdle应保证基本的连接可用性

3. **调整超时时间**：
   - maxWaitMillis根据业务容忍度设置
   - timeoutMillis根据Redis服务器性能调整

4. **监控和告警**：
   - 监控连接池的使用率
   - 当连接池使用率超过80%时发出告警