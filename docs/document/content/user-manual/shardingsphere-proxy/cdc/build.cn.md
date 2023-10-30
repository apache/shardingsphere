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
- CDC 增量阶段会按照事务的维度输出数据， 如果要开启 XA 事务的兼容，则 openGauss 和 ShardingSphere-Proxy 都需要 GLT 模块

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

1. [shardingsphere-global-clock-tso-provider-redis](https://repo1.maven.org/maven2/org/apache/shardingsphere/shardingsphere-global-clock-tso-provider-redis)，下载和 ShardingSphere-Proxy 同名版本
2. [jedis-4.3.1](https://repo1.maven.org/maven2/redis/clients/jedis/4.3.1/jedis-4.3.1.jar)

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
| MySQL     | [mysql-connector-java-8.0.11.jar]( https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.49/mysql-connector-java-5.1.49.jar ) | [Connector/J Versions]( https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.11/mysql-connector-java-8.0.11.jar ) |
| openGauss | [opengauss-jdbc-3.1.1-og.jar]( https://repo1.maven.org/maven2/org/opengauss/opengauss-jdbc/3.1.1-og/opengauss-jdbc-3.1.1-og.jar )        |                                                                                                  |

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

目前 CDC Client 只提供了 Java API，用户需要自行实现数据的消费逻辑。

1. 引入 CDC Client

```xml
<dependency>
    <groupId>io.github.greycode</groupId>
    <artifactId>shardingsphere-data-pipeline-cdc-client</artifactId>
    <version>${version}</version>
</dependency>
```

2. 启动 CDC Client

参考 [Example](https://github.com/apache/shardingsphere/blob/master/kernel/data-pipeline/scenario/cdc/client/src/test/java/org/apache/shardingsphere/data/pipeline/cdc/client/example/Bootstrap.java) 启动 CDC Client。
