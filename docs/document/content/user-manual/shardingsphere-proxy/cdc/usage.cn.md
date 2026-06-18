+++
title = "使用手册"
weight = 2
+++

## CDC 功能介绍

CDC 只会同步数据，不会同步表结构，目前也不支持 DDL 的语句同步。

### CDC 协议介绍

CDC 协议使用 Protobuf，对应的 Protobuf 类型是根据 Java 中的类型来映射的。

这里以 openGauss 为例，CDC 协议的数据类型和数据库类型的映射关系如下

| openGauss 类型                             | Java 数据类型          | CDC 对应的 protobuf 类型 | 备注                                     |
|------------------------------------------|--------------------|---------------------|----------------------------------------|
| tinyint、smallint、integer                 | Integer            | int32               |                                        |
| bigint                                   | Long               | int64               |                                        |
| numeric                                  | BigDecimal         | string              |                                        |
| real、float4                              | Float              | float               |                                        |
| binary_double、double precision           | Double             | double              |                                        |
| boolean                                  | Boolean            | bool                |                                        |
| char、varchar、text、clob                   | String             | string              |                                        |
| blob、bytea、raw                           | byte[]             | bytes               |                                        |
| date、timestamp，timestamptz、smalldatetime | java.sql.Timestamp | Timestamp           | protobuf 的 Timestamp 类型只包含秒和纳秒，所以和时区无关 |
| time、timetz                              | java.sql.Time      | int64               | 代表当天的纳秒数，和时区无关                         |
| interval、reltime、abstime                 | String             | string              |                                        |
| point、lseg、box、path、polygon、circle       | String             | string              |                                        |
| cidr、inet、macaddr                        | String             | string              |                                        |
| tsvector                                 | String             | string              |                                        |
| tsquery                                  | String             | String              |                                        |
| uuid                                     | String             | string              |                                        |
| json、jsonb                               | String             | string              |                                        |
| hll                                      | String             | string              |                                        |
| int4range、daterange、tsrange、tstzrange    | String             | string              |                                        |
| hash16、hash32                            | String             | string              |                                        |
| bit、bit varying                          | String             | string              | bit(1) 的时候返回 Boolean 类型                |

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

还需要账号对订阅的表和 schema 具备访问权限，以 test schema 下的 t_order 表为例。

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

#### 配置 CDC Server

1. 创建逻辑库。

```sql
CREATE DATABASE sharding_db;

\c sharding_db
```
2. 注册存储单元。

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

3. 创建分片规则。

```sql
CREATE SHARDING TABLE RULE t_order(
STORAGE_UNITS(ds_0,ds_1),
SHARDING_COLUMN=order_id,
TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="2")),
KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME="snowflake"))
);
```

4. 创建表

在 proxy 执行建表语句。

```sql
CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));
```

#### 启动 CDC Client

目前 CDC Client 只提供了 Java API，用户需要自行实现数据的消费逻辑。

下面是一个简单的启动 CDC Client 的示例。

```java
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.cdc.client.CDCClient;
import org.apache.shardingsphere.data.pipeline.cdc.client.config.CDCClientConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.client.handler.RetryStreamingExceptionHandler;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.CDCLoginParameter;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.StartStreamingParameter;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody.SchemaTable;

import java.util.Collections;

@Slf4j
public final class Bootstrap {
    
    @SneakyThrows(InterruptedException.class)
    public static void main(final String[] args) {
        String address = "127.0.0.1";
        // 构造 CDCClient，传入 CDCClientConfiguration，CDCClientConfiguration 中包含了 CDC Server 的地址和端口，以及超时时间
        try (CDCClient cdcClient = new CDCClient(new CDCClientConfiguration(address, 33071, 10000))) {
            // 先调用 connect 连接到 CDC Server，需要传入 1. 数据的消费处理逻辑 2. 消费时候的异常处理逻辑 3. 服务端错误的异常处理逻辑
            cdcClient.connect(records -> log.info("records: {}", records), new RetryStreamingExceptionHandler(cdcClient, 5, 5000),
                    (ctx, result) -> log.error("Server error: {}", result.getErrorMessage()));
            cdcClient.login(new CDCLoginParameter("root", "root"));
            // 开始 CDC 数据同步，返回的 streamingId 是这次 CDC 任务的唯一标识，CDC Server 生成唯一标识的依据是 订阅的数据库名称 + 订阅的表 + 是否是全量同步
            String streamingId = cdcClient.startStreaming(new StartStreamingParameter("sharding_db", Collections.singleton(SchemaTable.newBuilder().setTable("t_order").build()), true));
            log.info("Streaming id={}", streamingId);
            // 防止 main 主线程退出
            cdcClient.await();
        }
    }
}
```

主要有4个步骤
1. 构造 CDCClient，传入 CDCClientConfiguration
2. 调用 CDCClient.connect，这一步是和 CDC Server 建立连接
3. 调用 CDCClient.login，使用 global.yaml 中配置好的用户名和密码登录
4. 调用 CDCClient.startStreaming，开启订阅，需要保证订阅的库和表在 ShardingSphere-Proxy 存在，否则会报错。

> CDCClient.await 是阻塞主线程，非必需的步骤，用其他方式也可以，只要保证 CDC 线程一直在工作就行。

如果需要更复杂数据消费的实现，例如写入到数据库，可以参考 `DataSourceRecordConsumer.java`。

#### 写入数据

通过 proxy 写入数据，此时 CDC Client 会收到数据变更的通知。

```
INSERT INTO t_order (order_id, user_id, status) VALUES (1,1,'ok1'),(2,2,'ok2'),(3,3,'ok3');
UPDATE t_order SET status='updated' WHERE order_id = 1;
DELETE FROM t_order WHERE order_id = 2;
```

Bootstrap 会输出类似的日志

```
  records: [before {
  name: "order_id"
  value {
    type_url: "type.googleapis.com/google.protobuf.Empty"
  }
  ......
```

#### 查看 CDC 任务运行情况

CDC 任务的启动和停止目前只能通过 CDC Client 控制，可以通过在 proxy 中执行 DistSQL 查看 CDC 任务状态

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
 item | data_source |          status          | active | processed_records_count | inventory_finished_percentage | incremental_idle_seconds | confirmed_position | current_position | error_message
------+-------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+--------------------+------------------+---------------
 0    | ds_0        | EXECUTE_INCREMENTAL_TASK | false  | 2                       | 100                           | 115                      | 5/597E43D0         | 5/597E4810       |
 1    | ds_1        | EXECUTE_INCREMENTAL_TASK | false  | 3                       | 100                           | 115                      | 5/597E4450         | 5/597E4810       |
(2 rows)
```

3. 删除 CDC 任务

DROP STREAMING j0302p0000702a83116fcee83f70419ca5e2993791;

只有当 CDC 任务没有订阅的时候才可以删除，此时也会删除 openGauss 物理库上的 replication slots

```
sharding_db=> DROP STREAMING j0302p0000702a83116fcee83f70419ca5e2993791;
SUCCESS
```

# 注意事项

## 增量数据推送的说明

1. CDC 增量推送目前是按照事务维度的，物理库的事务不会被拆分，所以如果一个事务中有多个表的数据变更，那么这些数据变更会被一起推送。
如果要支持 XA 事务（目前只支持 openGauss），则 openGauss 和 Proxy 都需要 GLT 模块。
2. 满足推送的条件是满足了一定大小的数据量或者到了一定的时间间隔（目前是 300ms），在处理 XA 事务时，收到的多个分库增量事件超过了 300ms，可能会导致 XA 事务被拆开推送。

## 超大事务的处理

目前是将大事务完整解析，这样可能会导致 CDC Server 进程 OOM，后续可能会考虑强制截断。

## 建议的配置

CDC 的性能目前没有一个固定的值，可以关注配置中读/写的 batchSize，以及内存队列的大小，根据实际情况进行调优。
