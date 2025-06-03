+++
title = "运行部署"
weight = 1
+++

## 背景信息

ShardingSphere CDC 分为两个部分，一个是 CDC Server，另一个是 CDC Client。 CDC Server 和 ShardingSphere-Proxy 目前是一同部署的。

用户可以在自己的项目中引入 CDC Client，实现数据的消费逻辑。

## 约束条件

- 纯 JAVA 开发，JDK 建议 1.8 或以上版本。
- CDC Server 要求 ShardingSphere-Proxy 使用集群模式，目前支持 ZooKeeper 作为注册中心。
- CDC 只同步数据，不会同步表结构，目前也不支持 DDL 的语句同步。
- CDC 增量阶段会按照分库事务的维度输出数据， 如果要开启 XA 事务的兼容，则 openGauss 和 ShardingSphere-Proxy 都需要 GLT 模块

## CDC Server 部署步骤

这里以 openGauss 数据库为例，介绍 CDC Server 的部署步骤。

由于 CDC Server 内置于 ShardingSphere-Proxy，所以需要获取 ShardingSphere-Proxy。详情请参见 [proxy 启动手册](/cn/user-manual/shardingsphere-proxy/startup/bin/)。

### 配置 GLT 模块（可选）

官网发布的二进制包默认不包含 GLT 模块，如果使用的是包含 GLT 功能的 openGauss 数据库，则可以额外引入 GLT 模块，保证 XA 事务的完整性。

目前有两种方式引入 GLT 模块，并且需要在 global.yaml 中也进行相应的配置。

#### 1. 源码编译安装

1.1 准备代码环境，提前下载或者使用 Git clone，从 Github 下载 [ShardingSphere](https://github.com/apache/shardingsphere.git) 源码。

1.2 删除 kernel/global-clock/type/tso/core/pom.xml 中 shardingsphere-global-clock-tso-provider-redis 依赖的 `<scope>provided</scope>` 标签和 kernel/global-clock/type/tso/provider/redis/pom.xml 中 jedis
   的 `<scope>provided</scope>` 标签

1.3 编译 ShardingSphere-Proxy，具体编译步骤请参考 [ShardingSphere 编译手册](https://github.com/apache/shardingsphere/wiki#build-apache-shardingsphere)。

#### 2. 直接引入 GLT 依赖

可以从 maven 仓库中引入

2.1. [shardingsphere-global-clock-tso-provider-redis](https://repo1.maven.org/maven2/org/apache/shardingsphere/shardingsphere-global-clock-tso-provider-redis)，下载和 ShardingSphere-Proxy 同名版本

2.2. [jedis-4.3.1](https://repo1.maven.org/maven2/redis/clients/jedis/4.3.1/jedis-4.3.1.jar)

### CDC Server 使用手册

修改配置文件 `conf/global.yaml`，打开 CDC 功能。 目前 `mode` 必须是 `Cluster`，需要提前启动对应的注册中心。如果 GLT provider 使用 Redis，需要提前启动 Redis。

配置示例：

1. 在 `global.yaml` 中开启 CDC 功能。

```yaml
mode:
  type: Cluster
  repository:
    type: ZooKeeper
    props:
      namespace: cdc_demo
      server-lists: localhost:2181
      retryIntervalMilliseconds: 500
      timeToLiveSeconds: 60
      maxRetries: 3
      operationTimeoutMilliseconds: 500

authority:
  users:
    - user: root@%
      password: root
  privilege:
    type: ALL_PERMITTED

# 使用 GLT 的时候也需要开启分布式事务，目前 GLT 只有 openGauss 数据库支持
#transaction:
#  defaultType: XA
#  providerType: Atomikos
#
#globalClock:
#  enabled: true
#  type: TSO
#  provider: redis
#  props:
#    host: 127.0.0.1
#    port: 6379

props:
  proxy-default-port: 3307 # Proxy default port
  cdc-server-port: 33071 # CDC Server 端口，必须配置
  proxy-frontend-database-protocol-type: openGauss # 和后端数据库的类型一致
```

2. 引入 JDBC 驱动。

proxy 已包含 PostgreSQL JDBC 和 openGauss JDBC 驱动。

如果后端连接以下数据库，请下载相应 JDBC 驱动 jar 包，并将其放入 `${shardingsphere-proxy}/ext-lib` 目录。

| 数据库       | JDBC 驱动                                                                                                                        |
|-----------|--------------------------------------------------------------------------------------------------------------------------------|
| MySQL     | [mysql-connector-j-8.3.0.jar](https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/)                            |

4. 启动 ShardingSphere-Proxy：

```
sh bin/start.sh
```

5. 查看 proxy 日志 `logs/stdout.log`，看到日志中出现：

```
[INFO ] [main] o.a.s.p.frontend.ShardingSphereProxy - ShardingSphere-Proxy Cluster mode started successfully
```

确认启动成功。

6. 按需配置 CDC 任务同步配置

6.1. 查询配置。

```sql
SHOW STREAMING RULE;
```

默认配置如下：

```
+--------------------------------------------------------------+--------------------------------------+-------------------------------------------------------+
| read                                                         | write                                | stream_channel                                        |
+--------------------------------------------------------------+--------------------------------------+-------------------------------------------------------+
| {"workerThread":20,"batchSize":1000,"shardingSize":10000000} | {"workerThread":20,"batchSize":1000} | {"type":"MEMORY","props":{"block-queue-size":"2000"}} |
+--------------------------------------------------------------+--------------------------------------+-------------------------------------------------------+
```

6.2. 修改配置（可选）。

因 streaming rule 具有默认值，无需创建，仅提供 ALTER 语句。

完整配置 DistSQL 示例：

```sql
ALTER STREAMING RULE (
READ(
  WORKER_THREAD=20,
  BATCH_SIZE=1000,
  SHARDING_SIZE=10000000,
  RATE_LIMITER (TYPE(NAME='QPS',PROPERTIES('qps'='500')))
),
WRITE(
  WORKER_THREAD=20,
  BATCH_SIZE=1000,
  RATE_LIMITER (TYPE(NAME='TPS',PROPERTIES('tps'='2000')))
),
STREAM_CHANNEL (TYPE(NAME='MEMORY',PROPERTIES('block-queue-size'='2000')))
);
```

配置项说明：

```sql
ALTER STREAMING RULE (
READ( -- 数据读取配置。如果不配置则部分参数默认生效。
  WORKER_THREAD=20, -- 影响全量、增量任务，从源端摄取数据的线程池大小，不配置则使用默认值，需要确保该值不低于分库的数量。
  BATCH_SIZE=1000, -- 影响全量、增量任务，一次查询操作返回的最大记录数。如果一个事务中的数据量大于该值，增量情况下可能超过设定的值。
  SHARDING_SIZE=10000000, -- 影响全量任务，存量数据分片大小。如果不配置则使用默认值。
  RATE_LIMITER ( -- 影响全量、增量任务，限流算法。如果不配置则不限流。
  TYPE( -- 算法类型。可选项：QPS
  NAME='QPS',
  PROPERTIES( -- 算法属性
  'qps'='500'
  )))
),
WRITE( -- 数据写入配置。如果不配置则部分参数默认生效。
  WORKER_THREAD=20, -- 影响全量、增量任务，数据写入到目标端的线程池大小。如果不配置则使用默认值。
  BATCH_SIZE=1000, -- 影响全量、增量任务，存量任务一次批量写入操作的最大记录数。如果不配置则使用默认值。如果一个事务中的数据量大于该值，增量情况下可能超过设定的值。
  RATE_LIMITER ( -- 限流算法。如果不配置则不限流。
  TYPE( -- 算法类型。可选项：TPS
  NAME='TPS',
  PROPERTIES( -- 算法属性
  'tps'='2000'
  )))
),
STREAM_CHANNEL ( -- 数据通道，连接生产者和消费者，用于 read 和 write 环节。如果不配置则默认使用 MEMORY 类型。
TYPE( -- 算法类型。可选项：MEMORY
NAME='MEMORY',
PROPERTIES( -- 算法属性
'block-queue-size'='2000' -- 属性：阻塞队列大小
)))
);
```

## CDC Client 手册

CDC Client 不需要额外部署，只需要通过 maven 引入 CDC Client 的依赖就可以在项目中使用。用户可以通过 CDC Client 和服务端进行交互。

如果有需要，用户也可以自行实现一个 CDC Client，进行数据的消费和 ACK。

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-data-pipeline-cdc-client</artifactId>
    <version>${version}</version>
</dependency>
```

### CDC Client 介绍

`org.apache.shardingsphere.data.pipeline.cdc.client.CDCClient` 是 CDC Client 的入口类，用户可以通过该类和 CDC Server 进行交互。主要的和新方法如下。

| 方法名                                                                                                                         | 返回值         | 说明                                                                                                           |
|-----------------------------------------------------------------------------------------------------------------------------|-------------|--------------------------------------------------------------------------------------------------------------|
| connect(Consumer<List<Record>> dataConsumer, ExceptionHandler exceptionHandler, ServerErrorResultHandler errorResultHandler | void        | 和服务端进行连接，连接的时候需要指定 <br/>1. 数据的消费处理逻辑 <br/>2. 消费时候的异常处理逻辑 <br/>3. 服务端错误的异常处理逻辑                                |
| login(CDCLoginParameter parameter)                                                                                          | void        | CDC登陆，参数 <br/>username：用户名 <br/>password：密码                                                                  |
| startStreaming(StartStreamingParameter parameter)                                                                           | streamingId | 开启 CDC 订阅 <br/> StartStreamingParameter 参数 <br/> database：逻辑库名称 <br/> schemaTables：订阅的表名 <br/> full：是否订阅全量数据 |
| restartStreaming(String streamingId)                                                                                        | void        | 重启订阅                                                                                                         |
| stopStreaming(String streamingId)                                                                                           | void        | 停止订阅                                                                                                         |
| dropStreaming(String streamingId)                                                                                           | void        | 删除订阅                                                                                                         |
| await()                                                                                                                     | void        | 阻塞 CDC 线程，等待 channel 关闭                                                                                      |
| close()                                                                                                                     | void        | 关闭 channel，流程结束                                                                                              |
