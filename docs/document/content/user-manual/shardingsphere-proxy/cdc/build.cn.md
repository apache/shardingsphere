+++
title = "运行部署"
weight = 1
+++

## 背景信息

ShardingSphere CDC 分为两个部分，一个是 CDC Server，另一个是 CDC Client。 CDC Server 和 ShardingSphere-Proxy 目前是一同部署的。

用户可以在自己的项目中引入 CDC Client，实现数据的消费逻辑。

## 约束条件

- 纯 JAVA 开发，JDK 建议 1.8 或以上版本。
- CDC Server 要求 SharingSphere-Proxy 使用集群模式，目前支持 ZooKeeper 作为注册中心。
- CDC 只同步数据，不会同步表结构，目前也不支持 DDL 的语句同步。
- CDC 增量会按照事务的维度输出数据， 如果要开启 XA 事务的兼容，则 openGauss 和 ShardingSphere-Proxy 都需要 GLT 模块

## CDC 功能介绍

CDC 服务端的逻辑可以参考 https://github.com/apache/shardingsphere/tree/master/kernel/data-pipeline/scenario/cdc/core 中的实现。

### CDC 协议

CDC 协议使用 Protobuf，对应的 Protobuf 类型是根据 Java 中的类型来映射的，CDC 的数据类型和 openGauss 之间的映射关系如下

CDC 协议的数据类型和 openGauss 之间的映射关系如下

| openGauss 类型                        | Java 数据类型          | CDC 对应的 protobuf 类型 | 备注                       |
|-------------------------------------|--------------------|---------------------|--------------------------|
| INT1、INT2、INT4                      | Integer            | int32               |                          |
| INT8                                | Long               | int64               |                          |
| NUMERIC                             | BigDecimal         | string              |                          |
| FLOAT4                              | Float              | float               |                          |
| FLOAT8                              | Double             | double              |                          |
| BOOLEAN                             | Boolean            | bool                |                          |
| CHAR、VARCHAR、TEXT、CLOB              | String             | string              |                          |
| BLOB、RAW、BYTEA                      | byte[]             | bytes               |                          |
| DATE                                | java.util.Date     | Timestamp           |                          |
| DATE                                | java.sql.Date      | int64               | 这种情况下返回从1970-01-01 以来的天数 |
| TIMESTAMP，TIMESTAMPTZ、SMALLDATETIME | java.sql.Timestamp | Timestamp           | 不带时区信息                   |
| TIME，TIMETZ                         | java.sql.Time      | int64               | 代表当天的纳秒数（时区无关）           |
| INTERVAL、reltime、abstime            | String             | string              |                          |
| point、lseg、box、path、polygon、circle  | String             | string              |                          |
| cidr、inet、macaddr                   | String             | string              |                          |
| tsvector                            | String             | string              |                          |
| UUID                                | String             | string              |                          |
| JSON、JSONB                          | String             | string              |                          |
| HLL                                 | String             | string              |                          |
| 范围类型（int4range等）                    | String             | string              |                          |
| HASH16、HASH32                       | String             | string              |                          |

> 需要注意对时间类型的处理，为了屏蔽时区的差异，CDC 返回的数据都是时区无关的

CDC Server 中 Java 类型转 Protobuf 类型的工具类：ColumnValueConvertUtils，[源码地址](https://github.com/apache/shardingsphere/blob/master/kernel/data-pipeline/scenario/cdc/core/src/main/java/org/apache/shardingsphere/data/pipeline/cdc/util/ColumnValueConvertUtils.java)

对应的 CDC Client 中有 Protobuf 类型转换成 Java 类型的工具类 ProtobufAnyValueConverter，[源码地址](https://github.com/apache/shardingsphere/blob/master/kernel/data-pipeline/scenario/cdc/client/src/main/java/org/apache/shardingsphere/data/pipeline/cdc/client/util/ProtobufAnyValueConverter.java)

## CDC Server 部署步骤

这里以 openGauss 数据库为例，介绍 CDC Server 的部署步骤。

由于 CDC Server 内置于 ShardingSphere-Proxy，所以需要获取 ShardingSphere-Proxy。详情请参见 [proxy 启动手册](/cn/user-manual/shardingsphere-proxy/startup/bin/)。

### 配置 GLT 模块（可选）

官网发布的二进制包默认不包含 GLT 模块，不保证跨库事务完整性，如果使用的是包含 GLT 功能的 openGauss 数据库，则可以额外引入 GLT 模块，保证跨库事务的完整性。

目前有两种方式引入 GLT 模块，并且需要在 server.yaml 中也进行相应的配置。

#### 1. 源码编译安装

1. 准备代码环境，提前下载或者使用 Git clone，从 Github 下载 [ShardingSphere](https://github.com/apache/shardingsphere.git) 源码。
2. 删除 kernel/global-clock/type/tso/core/pom.xml 中 shardingsphere-global-clock-tso-provider-redis 依赖的 `<scope>provided</scope>` 标签和 kernel/global-clock/type/tso/provider/redis/pom.xml 中 jedis 的 `<scope>provided</scope>` 标签 
3. 编译 ShardingSphere-Proxy，具体编译步骤请参考 [ShardingSphere 编译手册](https://github.com/apache/shardingsphere/wiki#build-apache-shardingsphere)。

#### 2. 直接引入 GLT 依赖

可以从 maven 仓库中引入

1. [shardingsphere-global-clock-tso-provider-redis](https://mvnrepository.com/artifact/io.github.greycode/shardingsphere-global-clock-tso-provider-redis)，需要和 ShardingSphere-Proxy 版本一致
2. [jedis](https://mvnrepository.com/artifact/redis.clients/jedis), 推荐使用 4.3.1 版本

### CDC Server 使用手册

1. 修改配置文件 `conf/server.yaml`，打开 CDC 功能。 目前 `mode` 必须是 `Cluster`，需要提前启动对应的注册中心。如果 GLT provider 使用 Redis，需要提前启动 Redis。

配置示例：

```yaml
mode:
  type: Cluster
  repository:
    type: ZooKeeper
    props:
      namespace: open_cdc
      server-lists: localhost:2181
      retryIntervalMilliseconds: 500
      timeToLiveSeconds: 60
      maxRetries: 3
      operationTimeoutMilliseconds: 500

authority:
  users:
    # 这里的用户名和密码在 CDC Client 的认证中也会用到
    - user: root@%
      password: root
    - user: proxy
      password: Proxy@123
  privilege:
    type: ALL_PERMITTED

# 开启 GLT 的时候也需要打开分布式事务
#transaction:
#  defaultType: XA
#  providerType: Atomikos

# GLT 模块配置，如果不需要 GLT 模块，可以不配置
#globalClock:
#  enabled: true
#  type: TSO
#  provider: redis
#  props:
#    host: 127.0.0.1
#    port: 6379


props:
  cdc-server-port: 33071 # CDC Server 端口，必须配置
  proxy-frontend-database-protocol-type: openGauss
  # 省略其他配置
  ......
```

2. 引入 JDBC 驱动。

proxy 已包含 PostgreSQL JDBC 驱动。

如果后端连接以下数据库，请下载相应 JDBC 驱动 jar 包，并将其放入 `${shardingsphere-proxy}/ext-lib` 目录。

| 数据库       | JDBC 驱动                                                                                                                               | 参考                                                                                               |
|-----------|---------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|
| MySQL     | [mysql-connector-java-5.1.49.jar]( https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.49/mysql-connector-java-5.1.49.jar ) | [Connector/J Versions]( https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-versions.html ) |
| openGauss | [opengauss-jdbc-3.0.0.jar]( https://repo1.maven.org/maven2/org/opengauss/opengauss-jdbc/3.0.0/opengauss-jdbc-3.0.0.jar )              |                                                                                                  |

4. 启动 ShardingSphere-Proxy：

```
sh bin/start.sh
```

5. 查看 proxy 日志 `logs/stdout.log`，看到日志中出现：

```
[INFO ] [main] o.a.s.p.frontend.ShardingSphereProxy - ShardingSphere-Proxy Cluster mode started successfully
```

确认启动成功。

## CDC Client 使用手册

用户可以通过 CDC Client 和服务端进行交互，CDC Client 的依赖很轻，只包含 netty 以及 CDC 协议相关的依赖。

有两种方式可以引入 CDC Client

1. 源码编译，CDC Client 在编译 Proxy 的时候会一同编译，在 kernel/data-pipeline/scenario/cdc/client/target 目录下可以找到编译后的 jar 文件
2. 从 maven 仓库获取，[Shardingsphere Data Pipeline CDC Client](https://mvnrepository.com/artifact/io.github.greycode/shardingsphere-data-pipeline-cdc-client)

### CDC Client 介绍

`org.apache.shardingsphere.data.pipeline.cdc.client.CDCClient` 是 CDC Client 的入口类，用户可以通过该类和 CDC Server 进行交互。主要的和新方法如下。

| 方法名                                                | 返回值 | 说明 |
|----------------------------------------------------| --- | --- |
| await()                                            | void | 阻塞 CDC 线程，await channel 关闭 |
| close()                                            | void | 关闭 channel |
| connect()                                          | void | 和服务端进行连接 |
| login (CDCLoginParameter parameter)                | void | 登陆验证 |
| startStreaming (StartStreamingParameter parameter) | java.lang.String （CDC 任务唯一标识）        | 开启 CDC 订阅 |
| restartStreaming (java.lang.String streamingId)    | void | 重启订阅 |
| stopStreaming (java.lang.String streamingId)       | void | 停止订阅 |


### CDC Client 使用示例

1. 引入 CDC Client

```xml
<dependency>
    <groupId>io.github.greycode</groupId>
    <artifactId>shardingsphere-data-pipeline-cdc-client</artifactId>
    <version>${version}</version>
</dependency>
```

2. 启动 CDC Client

这里先介绍下 `CDCClientConfiguration` 参数，构造 CDCClient 的时候需要传入该参数，该参数包含了 CDC Server 的地址，端口，以及 CDC 数据的消费逻辑。

```java
@RequiredArgsConstructor
@Getter
public final class CDCClientConfiguration {
    
    // CDC 的地址，和Proxy一致
    private final String address;
    
    // CDC 端口，和 server.yaml 的一致
    private final int port;
    
    // 数据消费的逻辑, 需要用户自行实现
    private final Consumer<List<Record>> dataConsumer;
    
    // 异常处理 handler，有个默认的实现 org.apache.shardingsphere.data.pipeline.cdc.client.handler.LoggerExceptionHandler，也可以自行实现相应的处理逻辑，比如出现错误后重连，或者停止
    private final ExceptionHandler exceptionHandler;
    
    // 超时时间，超过这个时间没收到服务器的响应，会认为请求失败。
    private final int timeoutMills;
    ......
}
```

下面是一个简单的启动 CDC Client 的示例。

```java
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.cdc.client.CDCClient;
import org.apache.shardingsphere.data.pipeline.cdc.client.config.CDCClientConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.client.handler.LoggerExceptionHandler;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.CDCLoginParameter;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.StartStreamingParameter;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody.SchemaTable;

import java.util.Collections;

@Slf4j
public final class Bootstrap {
    
    @SneakyThrows(InterruptedException.class)
    public static void main(final String[] args) {
        // TODO records 的消费逻辑需要用户自行实现，这里只是简单打印下
        CDCClientConfiguration clientConfig = new CDCClientConfiguration("127.0.0.1", 33071, records -> log.info("records: {}", records), new LoggerExceptionHandler());
        try (CDCClient cdcClient = new CDCClient(clientConfig)) {
            // 1. 先调用 connect 连接到 CDC Server
            cdcClient.connect();
            // 2. 调用登陆的逻辑，用户名密码和 server.yaml 配置文件中的一致
            cdcClient.login(new CDCLoginParameter("root", "root"));
            // 3. 开启 CDC 数据订阅，用户只需要传入逻辑库和逻辑表，不需要关注底层数据分片情况，CDC Server 会将数据聚合后推送
            String streamingId = cdcClient.startStreaming(new StartStreamingParameter("sharding_db", Collections.singleton(SchemaTable.newBuilder().setTable("t_order").build()), true));
            log.info("Streaming id={}", streamingId);
            // stopStreaming 和 restartStreaming 非必需的操作，分别表示停止订阅和重启订阅
            // cdcClient.stopStreaming(streamingId);
            // cdcClient.restartStreaming(streamingId);
            // 4. 这里是阻塞线程，确保 CDC Client 一直运行。
            cdcClient.await();
        }
    }
}
```

主要有4个步骤
1. 构造 CDCClient，传入 CDCClientConfiguration
2. 调用 CDCClient.connect，这一步是和 CDC Server 建立连接
3. 调用 CDCClient.login，使用 server.yaml 中配置好的用户名和密码登录
4. 调用 CDCClient.startStreaming，开启订阅，需要保证订阅的库和表在 ShardingSphere-Proxy 存在，否则会报错。

> CDCClient.await 是阻塞主线程，非必需的步骤，用其他方式也可以，只要保证 CDC 线程一直在工作就行。

如果需要更复杂数据消费的实现，例如写入到数据库，可以参考 [DataSourceRecordConsumer](https://github.com/apache/shardingsphere/blob/master/test/e2e/operation/pipeline/src/test/java/org/apache/shardingsphere/test/e2e/data/pipeline/cases/cdc/DataSourceRecordConsumer.java)
