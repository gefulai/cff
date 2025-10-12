# 使用示例

## 1. 添加依赖

在你的Spring Boot项目中添加以下依赖：

```xml
<dependency>
    <groupId>com.cff</groupId>
    <artifactId>cff-cache-queue-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 2. 配置文件

在application.yml中添加以下配置：

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
      biz:
        - bizId: example_biz
          blockSize: 100
```

## 3. 创建自定义任务类型

首先创建一个自定义的任务类型：

```java
public class MyTask implements Serializable {
    private String taskId;
    private String taskData;
    
    // 构造函数、getter和setter
    public MyTask(String taskId, String taskData) {
        this.taskId = taskId;
        this.taskData = taskData;
    }
    
    // getter和setter方法
    public String getTaskId() {
        return taskId;
    }
    
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    
    public String getTaskData() {
        return taskData;
    }
    
    public void setTaskData(String taskData) {
        this.taskData = taskData;
    }
    
    @Override
    public String toString() {
        return "MyTask{taskId='" + taskId + "', taskData='" + taskData + "'}";
    }
}
```

## 4. 创建自定义处理器

创建一个自定义的处理器类继承AbstractCacheQueueProcessor：

```java
@Component
public class MyCacheQueueProcessor extends AbstractCacheQueueProcessor<MyTask> {
    
    private static final Logger logger = LoggerFactory.getLogger(MyCacheQueueProcessor.class);
    
    public MyCacheQueueProcessor() {
        setBizId("example_biz");
    }
    
    @Override
    public void consumeBlockTasks(Block<MyTask> block) {
        Queue<MyTask> taskQueue = block.getQueue();
        if (taskQueue != null) {
            while (!taskQueue.isEmpty()) {
                MyTask task = taskQueue.poll();
                // 处理具体的任务
                logger.info("Processing task: {}", task);
                
                // 在这里实现你的业务逻辑
                processTask(task);
            }
        }
    }
    
    private void processTask(MyTask task) {
        // 实现你的任务处理逻辑
        try {
            // 模拟任务处理时间
            Thread.sleep(100);
            logger.info("Task {} processed successfully", task.getTaskId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Task processing interrupted", e);
        }
    }
}
```

## 5. 在服务中使用处理器

在你的服务中注入并使用处理器：

```java
@Service
public class MyTaskService {
    
    @Autowired
    private MyCacheQueueProcessor processor;
    
    @Autowired
    private CacheQueue<MyTask> cacheQueue;
    
    public void processTasks(List<MyTask> tasks) {
        // 设置缓存队列
        processor.setCacheQueue(cacheQueue);
        
        // 初始化处理器，将任务列表拆分为块
        processor.init(tasks, 10); // 块大小为10
        
        // 开始处理任务
        processor.process();
    }
    
    public void startProcessing() {
        // 启动一个后台线程来处理任务
        new Thread(() -> {
            processor.setCacheQueue(cacheQueue);
            processor.process();
        }).start();
    }
}
```

## 6. 在Controller中使用

```java
@RestController
@RequestMapping("/tasks")
public class TaskController {
    
    @Autowired
    private MyTaskService taskService;
    
    @PostMapping("/process")
    public ResponseEntity<String> processTasks(@RequestBody List<MyTask> tasks) {
        try {
            taskService.processTasks(tasks);
            return ResponseEntity.ok("Tasks processing started");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to process tasks: " + e.getMessage());
        }
    }
}
```

## 7. 完整的Spring Boot应用示例

```java
@SpringBootApplication
public class MyApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
    
    @Bean
    public CommandLineRunner commandLineRunner(MyTaskService taskService) {
        return args -> {
            // 创建示例任务
            List<MyTask> tasks = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                tasks.add(new MyTask("task-" + i, "data-" + i));
            }
            
            // 处理任务
            taskService.processTasks(tasks);
        };
    }
}
```

## 8. 运行应用

确保你已经安装并运行了Redis服务器，然后启动你的Spring Boot应用。处理器将自动处理队列中的任务。

## 9. 监控和管理

你可以通过Redis客户端直接查看队列状态：

```bash
# 查看批次队列
LRANGE cache_queue:example_biz:batches 0 -1

# 查看批次信息
HGETALL cache_queue:example_biz:batch:{batchId}:info

# 查看块队列
LRANGE cache_queue:example_biz:batch:{batchId}:blocks 0 -1

# 查看块信息
HGETALL cache_queue:example_biz:batch:{batchId}:block:{blockIndex}:info
```

## 10. 连接池优势

使用连接池操作Redis具有以下优势：

1. **性能提升**：避免了频繁创建和销毁连接的开销
2. **资源管理**：通过连接池可以更好地管理Redis连接资源
3. **并发支持**：连接池可以支持更高的并发访问
4. **稳定性**：连接池提供了连接的复用和管理机制，提高了系统的稳定性

连接池的配置参数说明：
- `maxTotal`: 最大连接数，控制连接池中最多可以有多少个连接
- `maxIdle`: 最大空闲连接数，控制连接池中最多可以有多少个空闲连接
- `minIdle`: 最小空闲连接数，控制连接池中最少需要保持多少个空闲连接
- `maxWaitMillis`: 获取连接的最大等待时间，当连接池中没有可用连接时，等待获取连接的最大时间

## 11. 连接池配置最佳实践

### 开发环境配置
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

### 生产环境配置
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

这个框架确保了任务的可靠处理，即使在处理过程中出现故障，也不会丢失任务或重复处理任务。