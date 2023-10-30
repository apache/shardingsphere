+++
title = "使用手册"
weight = 2
+++

## CDC 功能介绍

### CDC 协议介绍

CDC 协议使用 Protobuf，对应的 Protobuf 类型是根据 Java 中的类型来映射的。

这里以 openGauss 为例，CDC 协议的数据类型和数据库类型的映射关系如下

| openGauss 类型                             | Java 数据类型          | CDC 对应的 protobuf 类型 | 备注             |
|------------------------------------------|--------------------|---------------------|----------------|
| INT1、INT2、INT4                           | Integer            | int32               |                |
| INT8                                     | Long               | int64               |                |
| NUMERIC                                  | BigDecimal         | string              |                |
| FLOAT4                                   | Float              | float               |                |
| FLOAT8                                   | Double             | double              |                |
| BOOLEAN                                  | Boolean            | bool                |                |
| CHAR、VARCHAR、TEXT、CLOB                   | String             | string              |                |
| BLOB、RAW、BYTEA                           | byte[]             | bytes               |                |
| DATE、TIMESTAMP，TIMESTAMPTZ、SMALLDATETIME | java.sql.Timestamp | Timestamp           | 不带时区信息         |
| TIME，TIMETZ                              | java.sql.Time      | int64               | 代表当天的纳秒数（时区无关） |
| INTERVAL、reltime、abstime                 | String             | string              |                |
| point、lseg、box、path、polygon、circle       | String             | string              |                |
| cidr、inet、macaddr                        | String             | string              |                |
| tsvector                                 | String             | string              |                |
| UUID                                     | String             | string              |                |
| JSON、JSONB                               | String             | string              |                |
| HLL                                      | String             | string              |                |
| 范围类型（int4range等）                         | String             | string              |                |
| HASH16、HASH32                            | String             | string              |                |

> 需要注意对时间类型的处理，为了屏蔽时区的差异，CDC 返回的数据都是时区无关的

## openGauss 使用手册

### 环境要求

支持的 openGauss 版本：2.x ~ 3.x。

### 权限要求

1. 调整源端 WAL 配置。

`postgresql.conf` 示例配置：
```
wal_level = logical
max_wal_senders = 10
max_replication_slots = 10
wal_sender_timeout = 0
max_connections = 600
```

详情请参见 [Write Ahead Log](https://docs.opengauss.org/en/docs/2.0.1/docs/Developerguide/settings.html) 和 [Replication](https://docs.opengauss.org/en/docs/2.0.1/docs/Developerguide/sending-server.html)。

2. 赋予源端 openGauss 账号 replication 权限。

`pg_hba.conf` 示例配置：

```
host replication repl_acct 0.0.0.0/0 md5
# 0.0.0.0/0 表示允许任意 IP 地址访问，可以根据实际情况调整成 CDC Server 的 IP 地址
```

详情请参见 [Configuring Client Access Authentication](https://docs.opengauss.org/en/docs/2.0.1/docs/Developerguide/configuring-client-access-authentication.html) 和 [Example: Logic Replication Code](https://docs.opengauss.org/en/docs/2.0.1/docs/Developerguide/example-logic-replication-code.html)。

3. 赋予 openGauss 账号 DDL DML 权限。

如果使用非超级管理员账号，要求该账号在用到的数据库上，具备 CREATE 和 CONNECT 的权限。

示例：
```sql
GRANT CREATE, CONNECT ON DATABASE source_ds TO cdc_user;
```

还需要账号对迁移的表和 schema 具备访问权限，以 test schema 下的 t_order 表为例。

```sql
\c source_ds

GRANT USAGE ON SCHEMA test TO GROUP cdc_user;
GRANT SELECT ON TABLE test.t_order TO cdc_user;
```

openGauss 有 OWNER 的概念，如果是数据库，SCHEMA，表的 OWNER，则可以省略对应的授权步骤。

openGauss 不允许普通账户在 public schema 下操作。所以如果迁移的表在 public schema 下，需要额外授权。

```sql
GRANT ALL PRIVILEGES TO cdc_user;
```

详情请参见 [openGauss GRANT](https://docs.opengauss.org/zh/docs/2.0.1/docs/Developerguide/GRANT.html)

### 完整流程示例

#### 前提条件

1. 准备好 CDC 源端的库、表、数据。

```sql
DROP DATABASE IF EXISTS ds_0;
CREATE DATABASE ds_0;

DROP DATABASE IF EXISTS ds_1;
CREATE DATABASE ds_1;
```

#### 操作步骤

1. 在 `server.yaml` 中开启 CDC 功能。

```yaml
mode:
  type: Cluster
  repository:
    type: ZooKeeper
    props:
      namespace: cdc
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

#开启 GLT 功能参考 CDC 部署手册
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
  system-log-level: INFO
  check-table-metadata-enabled: false
  proxy-default-port: 3307 # Proxy default port.
  cdc-server-port: 33071 # CDC server port
  proxy-frontend-database-protocol-type: openGauss
```

2. 在 proxy 新建逻辑数据库并配置好存储单元和规则。

2.1. 创建逻辑库。

```sql
CREATE DATABASE sharding_db;

\c sharding_db
```
2.2. 注册存储单元。

```sql
REGISTER STORAGE UNIT ds_0 (
    URL="jdbc:opengauss://127.0.0.1:5432/ds_0",
    USER="gaussdb",
    PASSWORD="Root@123",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
), ds_1 (
    URL="jdbc:opengauss://127.0.0.1:5432/ds_1",
    USER="gaussdb",
    PASSWORD="Root@123",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
);
```

2.3. 创建分片规则。

```sql
CREATE SHARDING TABLE RULE t_order(
STORAGE_UNITS(ds_0,ds_1),
SHARDING_COLUMN=order_id,
TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="2")),
KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME="snowflake"))
);
```

2.4. 创建表和初始化数据

在 proxy 执行建表语句。

```sql
CREATE TABLE t_order (id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (id));

INSERT INTO t_order (id, user_id, status) VALUES (1,1,'ok1'),(2,2,'ok2'),(3,3,'ok3');
```

3. 启动 CDC Client

先引入 CDC Client 依赖，在代码

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

4. 通过 DistSQL 查看 CDC 任务状态

CDC 任务的启动和停止目前只能通过 CDC Client 控制，可以在 proxy 中执行对应的 DistSQL 查看 CDC 任务的运行情况

1. 查看 CDC 任务列表

SHOW STREAMING LIST;

运行结果

```
sharding_db=> SHOW STREAMING LIST;
                     id                     |  database   | tables  | job_item_count | active |     create_time     | stop_time 
--------------------------------------------+-------------+---------+----------------+--------+---------------------+-----------
 j0302p0000702a83116fcee83f70419ca5e2993791 | sharding_db | t_order | 1              | true   | 2023-10-27 22:01:27 | 
(1 row)
```

2. 查看 CDC 任务详情

SHOW STREAMING STATUS j0302p0000702a83116fcee83f70419ca5e2993791;

运行结果

```
sharding_db=> SHOW STREAMING STATUS j0302p0000702a83116fcee83f70419ca5e2993791;
 item | data_source |          status          | active | processed_records_count | inventory_finished_percentage | incremental_idle_seconds | error_message
------+-------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------
 0    | ds_0        | EXECUTE_INCREMENTAL_TASK | true   | 1                       | 100                           | 101                      |
 1    | ds_1        | EXECUTE_INCREMENTAL_TASK | true   | 2                       | 100                           | 100                      |
(2 rows)
```

3. 删除 CDC 任务

DROP STREAMING j0302p0000702a83116fcee83f70419ca5e2993791;

此时也会删除 openGauss 物理库上的 replication slots

```
sharding_db=> DROP STREAMING j0302p0000702a83116fcee83f70419ca5e2993791;
SUCCESS
```