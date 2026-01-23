+++
title = "Manual"
weight = 2
+++

## Introduction to CDC Function

CDC only synchronizes data, it does not synchronize table structures, and currently does not support the synchronization of DDL statements.

### Introduction to CDC Protocol

The CDC protocol uses Protobuf, and the corresponding Protobuf types are mapped based on the types in Java.

Here, taking openGauss as an example, the mapping relationship between the data types of the CDC protocol and the database types is as follows.

| openGauss type                           | Java data type     | CDC corresponding protobuf type | Remarks                                                                                                       |
|------------------------------------------|--------------------|---------------------------------|---------------------------------------------------------------------------------------------------------------|
| tinyint, smallint, integer               | Integer            | int32                           |                                                                                                               |
| bigint                                   | Long               | int64                           |                                                                                                               |
| numeric                                  | BigDecimal         | string                          |                                                                                                               |
| real, float4                             | Float              | float                           |                                                                                                               |
| binary_double, double precision          | Double             | double                          |                                                                                                               |
| boolean                                  | Boolean            | bool                            |                                                                                                               |
| char, varchar, text, clob                | String             | string                          |                                                                                                               |
| blob, bytea, raw                         | byte[]             | bytes                           |                                                                                                               |
| date, timestamp, timestamptz, smalldatetime | java.sql.Timestamp | Timestamp                       | The Timestamp type of protobuf only contains seconds and nanoseconds, so it is irrelevant to the time zone |
| time, timetz                             | java.sql.Time       | int64                           | Represents the number of nanoseconds of the day, irrelevant to the time zone                                  |
| interval, reltime, abstime               | String             | string                          |                                                                                                               |
| point, lseg, box, path, polygon, circle  | String             | string                          |                                                                                                               |
| cidr, inet, macaddr                      | String             | string                          |                                                                                                               |
| tsvector                                 | String             | string                          |                                                                                                               |
| tsquery                                  | String             | String                          |                                                                                                               |
| uuid                                     | String             | string                          |                                                                                                               |
| json, jsonb                              | String             | string                          |                                                                                                               |
| hll                                      | String             | string                          |                                                                                                               |
| int4range, daterange, tsrange, tstzrange | String             | string                          |                                                                                                               |
| hash16, hash32                           | String             | string                          |                                                                                                               |
| bit, bit varying                         | String             | string                          | Returns Boolean type when bit(1)                                                                              |

## openGauss User Manual

### Environmental Requirements

Supported openGauss versions: 2.x ~ 3.x.

### Permission Requirements

1. Adjust the source end WAL configuration.

Example configuration for `postgresql.conf`:
```
wal_level = logical
max_wal_senders = 10
max_replication_slots = 10
wal_sender_timeout = 0
max_connections = 600
```

For details, please refer to [Write Ahead Log](https://docs.opengauss.org/en/docs/2.0.1/docs/Developerguide/settings.html) and [Replication](https://docs.opengauss.org/en/docs/2.0.1/docs/Developerguide/sending-server.html).

2. Grant replication permission to the source end openGauss account.

Example configuration for `pg_hba.conf`:

```
host replication repl_acct 0.0.0.0/0 md5
# 0.0.0.0/0 means allowing access from any IP address, which can be adjusted to the IP address of the CDC Server according to the actual situation
```

For details, please refer to [Configuring Client Access Authentication](https://docs.opengauss.org/en/docs/2.0.1/docs/Developerguide/configuring-client-access-authentication.html) and [Example: Logic Replication Code](https://docs.opengauss.org/en/docs/2.0.1/docs/Developerguide/example-logic-replication-code.html).

3. Grant DDL DML permissions to the openGauss account.

If a non-super administrator account is used, it is required that this account has CREATE and CONNECT permissions on the database used.

Example:
```sql
GRANT CREATE, CONNECT ON DATABASE source_ds TO cdc_user;
```

The account also needs to have access permissions to the table and schema to be subscribed, taking the t_order table under the test schema as an example.

```sql
\c source_ds

GRANT USAGE ON SCHEMA test TO GROUP cdc_user;
GRANT SELECT ON TABLE test.t_order TO cdc_user;
```

openGauss has the concept of OWNER. If it is the OWNER of the database, SCHEMA, or table, the corresponding authorization steps can be omitted.

openGauss does not allow ordinary accounts to operate under the public schema. So if the table to be migrated is under the public schema, additional authorization is needed.

```sql
GRANT ALL PRIVILEGES TO cdc_user;
```

For details, please refer to [openGauss GRANT](https://docs.opengauss.org/zh/docs/2.0.1/docs/Developerguide/GRANT.html)

### Complete Process Example

#### Prerequisites

1. Prepare the database, table, and data of the CDC source end.

```sql
DROP DATABASE IF EXISTS ds_0;
CREATE DATABASE ds_0;

DROP DATABASE IF EXISTS ds_1;
CREATE DATABASE ds_1;
```

#### Configure CDC Server

1. Create a logical database.

```sql
CREATE DATABASE sharding_db;

\c sharding_db
```
2. Register storage unit.

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

3. Create sharding rules.

```sql
CREATE SHARDING TABLE RULE t_order(
STORAGE_UNITS(ds_0,ds_1),
SHARDING_COLUMN=order_id,
TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="2")),
KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME="snowflake"))
);
```

4. Create tables

Execute the creation table statement in the proxy.

```sql
CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));
```

#### Start CDC Client

Currently, the CDC Client only provides a Java API, and users need to implement the data consumption themselves.

Below is a simple example of starting the CDC Client.

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
        // Construct CDCClient, pass in CDCClientConfiguration, CDCClientConfiguration contains the address and port of the CDC Server, as well as the timeout time
        try (CDCClient cdcClient = new CDCClient(new CDCClientConfiguration(address, 33071, 10000))) {
            // First call connect to the CDC Server, you need to pass in 1. Data consumption processing logic 2. Exception handling logic during consumption 3. Server error exception handling logic
            cdcClient.connect(records -> log.info("records: {}", records), new RetryStreamingExceptionHandler(cdcClient, 5, 5000),
                    (ctx, result) -> log.error("Server error: {}", result.getErrorMessage()));
            cdcClient.login(new CDCLoginParameter("root", "root"));
            // Start CDC data synchronization, the returned streamingId is the unique identifier of this CDC task, the basis for the CDC Server to generate a unique identifier is the name of the subscribed database + the subscribed table + whether it is full synchronization
            String streamingId = cdcClient.startStreaming(new StartStreamingParameter("sharding_db", Collections.singleton(SchemaTable.newBuilder().setTable("t_order").build()), true));
            log.info("Streaming id={}", streamingId);
            // Prevent the main thread from exiting
            cdcClient.await();
        }
    }
}
```

There are mainly 4 steps
1. Construct CDCClient, pass in CDCClientConfiguration
2. Call CDCClient.connect(), this step is to establish a connection with the CDC Server
3. Call CDCClient.login(), log in with the username and password configured in global.yaml
4. Call CDCClient.startStreaming(), start subscribing, you need to ensure that the subscribed database and table exist in ShardingSphere-Proxy, otherwise an error will be reported

> CDCClient.await is to block the main thread, it is not a necessary step, other methods can also be used, as long as the CDC thread is always working.

If you need more complex data consumption implementation, such as writing to the database, you can refer to `DataSourceRecordConsumer.java`.

#### Write Data

When write data through a proxy, the CDC Client is notified of the data change.

```
INSERT INTO t_order (order_id, user_id, status) VALUES (1,1,'ok1'),(2,2,'ok2'),(3,3,'ok3');
UPDATE t_order SET status='updated' WHERE order_id = 1;
DELETE FROM t_order WHERE order_id = 2;
```

Bootstrap will output a similar log.

```
  records: [before {
  name: "order_id"
  value {
    type_url: "type.googleapis.com/google.protobuf.Empty"
  }
  ......
```

#### View the Running Status of the CDC Task

The start and stop of the CDC task can only be controlled by the CDC Client. You can view the status of the CDC task by executing DistSQL in the proxy

1. View the CDC task list

SHOW STREAMING LIST;

Running result

```
sharding_db=> SHOW STREAMING LIST;
                     id                     |  database   | tables  | job_item_count | active |     create_time     | stop_time
--------------------------------------------+-------------+---------+----------------+--------+---------------------+-----------
 j0302p0000702a83116fcee83f70419ca5e2993791 | sharding_db | t_order | 1              | true   | 2023-10-27 22:01:27 |
(1 row)
```

2. View the details of the CDC task

SHOW STREAMING STATUS j0302p0000702a83116fcee83f70419ca5e2993791;

Running result

```
sharding_db=> SHOW STREAMING STATUS j0302p0000702a83116fcee83f70419ca5e2993791;
 item | data_source |          status          | active | processed_records_count | inventory_finished_percentage | incremental_idle_seconds | confirmed_position | current_position | error_message
------+-------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+--------------------+------------------+---------------
 0    | ds_0        | EXECUTE_INCREMENTAL_TASK | false  | 2                       | 100                           | 115                      | 5/597E43D0         | 5/597E4810       |
 1    | ds_1        | EXECUTE_INCREMENTAL_TASK | false  | 3                       | 100                           | 115                      | 5/597E4450         | 5/597E4810       |
(2 rows)
```

3. Drop CDC task

DROP STREAMING j0302p0000702a83116fcee83f70419ca5e2993791;

The CDC task can only be deleted when there are no subscriptions. At this time, the replication slots on the openGauss physical database will also be deleted.

```
sharding_db=> DROP STREAMING j0302p0000702a83116fcee83f70419ca5e2993791;
SUCCESS
```

# Precautions

## Explanation of incremental data push

1. The CDC incremental push is currently transactional, and the transactions of the physical database will not be split. Therefore, if there are data changes in multiple tables in a transaction, these data changes will be pushed together.
If you want to support XA transactions (currently only supports openGauss), both openGauss and Proxy need the GLT module.
2. The conditions for push are met when a certain amount of data is met or a certain time interval is reached (currently 300ms). When processing XA transactions, if the received multiple physical database incremental events exceed 300ms, it may cause the XA transaction to be split and pushed.

## Handling of large transactions

Currently, large transactions are fully parsed, which may cause the CDC Server process to OOM. In the future, forced truncation may be considered.

## Recommended configuration

There is no fixed value for the performance of CDC, you can focus on the batchSize of read/write in the configuration, and the size of the memory queue, and tune it according to the actual situation.
