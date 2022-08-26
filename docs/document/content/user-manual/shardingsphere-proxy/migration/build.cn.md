+++
title = "运行部署"
weight = 1
+++

## 背景信息

对于使用单数据库运行的系统来说，如何安全简单地将数据迁移至水平分片的数据库上，一直以来都是一个迫切的需求。

## 前提条件

-  Proxy 采用纯  JAVA 开发，JDK 建议 1.8 或以上版本。
- 数据迁移使用集群模式，目前支持 ZooKeeper 作为注册中心。

## 操作步骤

1. 执行以下命令，编译生成 ShardingSphere-Proxy 二进制包：

```
git clone --depth 1 https://github.com/apache/shardingsphere.git
cd shardingsphere
mvn clean install -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Drat.skip=true -Djacoco.skip=true -DskipITs -DskipTests -Prelease
```

发布包：
- /shardingsphere-distribution/shardingsphere-proxy-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-proxy-bin.tar.gz

或者通过[下载页面]( https://shardingsphere.apache.org/document/current/cn/downloads/ )获取安装包。

2. 解压缩 proxy 发布包，修改配置文件 `conf/config-sharding.yaml`。详情请参见 [proxy 启动手册](/cn/user-manual/shardingsphere-proxy/startup/bin/)。

3. 修改配置文件 `conf/server.yaml`，详情请参见[模式配置](/cn/user-manual/shardingsphere-jdbc/yaml-config/mode/)。

目前 `mode` 必须是 `Cluster`，需要提前启动对应的注册中心。

配置示例：
```yaml
mode:
  type: Cluster
  repository:
    type: ZooKeeper
    props:
      namespace: governance_ds
      server-lists: localhost:2181
      retryIntervalMilliseconds: 500
      timeToLiveSeconds: 60
      maxRetries: 3
      operationTimeoutMilliseconds: 500
  overwrite: false
```

4. 引入 JDBC 驱动。

如果后端连接以下数据库，请下载相应 JDBC 驱动 jar 包，并将其放入 `${shardingsphere-proxy}/lib` 目录。

| 数据库                 | JDBC 驱动                                                                                                                                                          | 参考                                                                                             |
| --------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------ |
| MySQL                 | [mysql-connector-java-5.1.47.jar]( https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar )                              | [Connector/J Versions]( https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-versions.html ) |
| openGauss             | [opengauss-jdbc-2.0.1-compatibility.jar]( https://repo1.maven.org/maven2/org/opengauss/opengauss-jdbc/2.0.1-compatibility/opengauss-jdbc-2.0.1-compatibility.jar ) |                                                                                                  |

5. 启动 ShardingSphere-Proxy：

```
sh bin/start.sh
```

6. 查看 proxy 日志 `logs/stdout.log`，看到日志中出现：

```
[INFO ] [main] o.a.s.p.frontend.ShardingSphereProxy - ShardingSphere-Proxy start success
```

确认启动成功。

7. 按需配置迁移

7.1. 查询配置。

```sql
SHOW MIGRATION PROCESS CONFIGURATION;
```

默认配置如下：

```sql
+--------------------------------------------------------------+--------------------------------------+------------------------------------------------------+
| read                                                         | write                                | stream_channel                                       |
+--------------------------------------------------------------+--------------------------------------+------------------------------------------------------+
| {"workerThread":40,"batchSize":1000,"shardingSize":10000000} | {"workerThread":40,"batchSize":1000} | {"type":"MEMORY","props":{"block-queue-size":10000}} |
+--------------------------------------------------------------+--------------------------------------+------------------------------------------------------+
```

7.2. 新建配置（可选）。

不配置的话有默认值。

完整配置 DistSQL 示例：

```sql
CREATE MIGRATION PROCESS CONFIGURATION (
READ(
  WORKER_THREAD=40,
  BATCH_SIZE=1000,
  SHARDING_SIZE=10000000,
  RATE_LIMITER (TYPE(NAME='QPS',PROPERTIES('qps'='500')))
),
WRITE(
  WORKER_THREAD=40,
  BATCH_SIZE=1000,
  RATE_LIMITER (TYPE(NAME='TPS',PROPERTIES('tps'='2000')))
),
STREAM_CHANNEL (TYPE(NAME='MEMORY',PROPERTIES('block-queue-size'='10000')))
);
```

配置项说明：

```sql
CREATE MIGRATION PROCESS CONFIGURATION (
READ( -- 数据读取配置。如果不配置则部分参数默认生效。
  WORKER_THREAD=40, -- 从源端摄取全量数据的线程池大小。如果不配置则使用默认值。
  BATCH_SIZE=1000, -- 一次查询操作返回的最大记录数。如果不配置则使用默认值。
  SHARDING_SIZE=10000000, -- 全量数据分片大小。如果不配置则使用默认值。
  RATE_LIMITER ( -- 限流算法。如果不配置则不限流。
  TYPE( -- 算法类型。可选项：QPS
  NAME='QPS',
  PROPERTIES( -- 算法属性
  'qps'='500'
  )))
),
WRITE( -- 数据写入配置。如果不配置则部分参数默认生效。
  WORKER_THREAD=40, -- 数据写入到目标端的线程池大小。如果不配置则使用默认值。
  BATCH_SIZE=1000, -- 一次批量写入操作的最大记录数。如果不配置则使用默认值。
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
'block-queue-size'='10000' -- 属性：阻塞队列大小
)))
);
```

DistSQL 示例：配置 `READ` 限流。

```sql
CREATE MIGRATION PROCESS CONFIGURATION (
READ(
  RATE_LIMITER (TYPE(NAME='QPS',PROPERTIES('qps'='500')))
)
);
```

配置读取数据限流，其它配置使用默认值。

7.3. 修改配置。

`ALTER MIGRATION PROCESS CONFIGURATION`，内部结构和 `CREATE MIGRATION PROCESS CONFIGURATION` 一致。

DistSQL 示例：调整限流参数

```sql
ALTER MIGRATION PROCESS CONFIGURATION (
READ(
  RATE_LIMITER (TYPE(NAME='QPS',PROPERTIES('qps'='1000')))
)
);
---
ALTER MIGRATION PROCESS CONFIGURATION (
READ(
  RATE_LIMITER (TYPE(NAME='QPS',PROPERTIES('qps'='1000')))
), WRITE(
  RATE_LIMITER (TYPE(NAME='TPS',PROPERTIES('tps'='1000')))
)
);
```

7.4. 清除配置。

DistSQL 示例：清空 `READ` 配置、恢复为默认值。

```sql
DROP MIGRATION PROCESS CONFIGURATION '/READ';
```

DistSQL 示例：清空 `READ/RATE_LIMITER` 配置。

```sql
DROP MIGRATION PROCESS CONFIGURATION '/READ/RATE_LIMITER';
```
