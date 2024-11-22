+++
title = "HiveServer2"
weight = 6
+++

## Background Information

ShardingSphere does not provide support for `driverClassName` of `org.apache.hive.jdbc.HiveDriver` by default.

ShardingSphere's support for HiveServer2 JDBC Driver is in the optional module.

## Prerequisites

To use a `jdbcUrl` like `jdbc:hive2://localhost:10000/` for the data node in the ShardingSphere configuration file,
The possible Maven dependencies are as follows.

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-jdbc</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-infra-database-hive</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-parser-sql-hive</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.hive</groupId>
        <artifactId>hive-jdbc</artifactId>
        <version>4.0.1</version>
    </dependency>
    <dependency>
        <groupId>org.apache.hive</groupId>
        <artifactId>hive-service</artifactId>
        <version>4.0.1</version>
    </dependency>
</dependencies>
```

### Optional shortcut to resolve dependency conflicts

Using `org.apache.hive:hive-jdbc:4.0.1` directly will cause a large number of dependency conflicts.
If users do not want to manually resolve potentially thousands of lines of dependency conflicts, 
they can use a third-party build of the HiveServer2 JDBC Driver Thin JAR.
The following is an example of a possible configuration,

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-jdbc</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-infra-database-hive</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-parser-sql-hive</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>io.github.linghengqian</groupId>
        <artifactId>hive-server2-jdbc-driver-thin</artifactId>
        <version>1.5.0</version>
        <exclusions>
            <exclusion>
                <groupId>com.fasterxml.woodstox</groupId>
                <artifactId>woodstox-core</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
</dependencies>
```

## Configuration Example

### Start HiveServer2

Write a Docker Compose file to start HiveServer2.

```yaml
services:
  hive-server2:
    image: apache/hive:4.0.1
    environment:
      SERVICE_NAME: hiveserver2
    ports:
      - "10000:10000"
```

### Create business tables

Use a third-party tool to create a business database and business table in HiveServer2.
Taking DBeaver Community as an example, if you use Ubuntu 22.04.4, you can quickly install it through Snapcraft.

```shell
sudo apt update && sudo apt upgrade -y
sudo snap install dbeaver-ce
snap run dbeaver-ce
```

In DBeaver Community, use the `jdbcUrl` of `jdbc:hive2://localhost:10000/` to connect to HiveServer2, 
and leave `username` and `password` blank.
Execute the following SQL,

```sql
-- noinspection SqlNoDataSourceInspectionForFile
CREATE DATABASE demo_ds_0;
CREATE DATABASE demo_ds_1;
CREATE DATABASE demo_ds_2;
```

Use the `jdbcUrl` of `jdbc:hive2://localhost:10000/demo_ds_0`, 
`jdbc:hive2://localhost:10000/demo_ds_1` and `jdbc:hive2://localhost:10000/demo_ds_2` to connect to HiveServer2 to execute the following SQL,

```sql
-- noinspection SqlNoDataSourceInspectionForFile
set iceberg.mr.schema.auto.conversion=true;

CREATE TABLE IF NOT EXISTS t_order
(
    order_id   BIGINT,
    order_type INT,
    user_id    INT    NOT NULL,
    address_id BIGINT NOT NULL,
    status     VARCHAR(50),
    PRIMARY KEY (order_id) disable novalidate
) STORED BY ICEBERG STORED AS ORC TBLPROPERTIES ('format-version' = '2');

TRUNCATE TABLE t_order;
```

### Create ShardingSphere data source in business projects

After the business project introduces the dependencies involved in `prerequisites`, 
write the ShardingSphere data source configuration file `demo.yaml` on the classpath of the business project.

```yaml
dataSources:
    ds_0:
        dataSourceClassName: com.zaxxer.hikari.HikariDataSource
        driverClassName: org.apache.hive.jdbc.HiveDriver
        jdbcUrl: jdbc:hive2://localhost:10000/demo_ds_0
    ds_1:
        dataSourceClassName: com.zaxxer.hikari.HikariDataSource
        driverClassName: org.apache.hive.jdbc.HiveDriver
        jdbcUrl: jdbc:hive2://localhost:10000/demo_ds_1
    ds_2:
        dataSourceClassName: com.zaxxer.hikari.HikariDataSource
        driverClassName: org.apache.hive.jdbc.HiveDriver
        jdbcUrl: jdbc:hive2://localhost:10000/demo_ds_2
rules:
- !SHARDING
    tables:
      t_order:
        actualDataNodes:
        keyGenerateStrategy:
          column: order_id
          keyGeneratorName: snowflake
    defaultDatabaseStrategy:
      standard:
        shardingColumn: user_id
        shardingAlgorithmName: inline
    shardingAlgorithms:
      inline:
        type: INLINE
        props:
          algorithm-expression: ds_${user_id % 2}
    keyGenerators:
      snowflake:
        type: SNOWFLAKE
```

### Enjoy the integration

Create a ShardingSphere data source to enjoy the integration.

```java
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
public class ExampleUtils {
    void test() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:shardingsphere:classpath:demo.yaml");
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        try (HikariDataSource dataSource = new HikariDataSource(config);
             Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO t_order (user_id, order_type, address_id, status) VALUES (1, 1, 1, 'INSERT_TEST')");
            statement.executeQuery("SELECT * FROM t_order");
            statement.execute("DELETE FROM t_order WHERE order_id=1");
        }
    }
}
```

## External Integration

### Connect to HiveServer2 with ZooKeeper Service Discovery enabled

`jdbcUrl` in the ShardingSphere configuration file can be configured to connect to HiveServer2 with ZooKeeper Service Discovery enabled.

For discussion, assume that there is the following Docker Compose file to start HiveServer2 with ZooKeeper Service Discovery.

```yaml
name: test-1
services:
  zookeeper:
    image: zookeeper:3.9.3-jre-17
    ports:
      - "2181:2181"
  apache-hive-1:
    image: apache/hive:4.0.1
    depends_on:
      - zookeeper
    environment:
      SERVICE_NAME: hiveserver2
      SERVICE_OPTS: >-
        -Dhive.server2.support.dynamic.service.discovery=true
        -Dhive.zookeeper.quorum=zookeeper:2181
        -Dhive.server2.thrift.bind.host=0.0.0.0
        -Dhive.server2.thrift.port=10000
    ports:
      - "10000:10000"
```

In DBeaver Community,
use `jdbcUrl` of `jdbc:hive2://127.0.0.1:2181/;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2` to connect to HiveServer2,
leave `username` and `password` blank.
Execute the following SQL,

```sql
-- noinspection SqlNoDataSourceInspectionForFile
CREATE DATABASE demo_ds_0;
CREATE DATABASE demo_ds_1;
CREATE DATABASE demo_ds_2;
```

Use `jdbcUrl` of `jdbc:hive2://127.0.0.1:2181/demo_ds_0;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2`,
`jdbc:hive2://127.0.0.1:2181/demo_ds_1;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2`
and `jdbc:hive2://127.0.0.1:2181/demo_ds_2;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2`
to connect to HiveServer2 and execute the following SQL,

```sql
-- noinspection SqlNoDataSourceInspectionForFile
set iceberg.mr.schema.auto.conversion=true;

CREATE TABLE IF NOT EXISTS t_order
(
    order_id   BIGINT,
    order_type INT,
    user_id    INT    NOT NULL,
    address_id BIGINT NOT NULL,
    status     VARCHAR(50),
    PRIMARY KEY (order_id) disable novalidate
) STORED BY ICEBERG STORED AS ORC TBLPROPERTIES ('format-version' = '2');

TRUNCATE TABLE t_order;
```

After the business project introduces the dependencies involved in the `prerequisites`,
write the ShardingSphere data source configuration file `demo.yaml` on the classpath of the business project.

```yaml
dataSources:
    ds_0:
        dataSourceClassName: com.zaxxer.hikari.HikariDataSource
        driverClassName: org.apache.hive.jdbc.HiveDriver
        jdbcUrl: jdbc:hive2://127.0.0.1:2181/demo_ds_0;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2
    ds_1:
        dataSourceClassName: com.zaxxer.hikari.HikariDataSource
        driverClassName: org.apache.hive.jdbc.HiveDriver
        jdbcUrl: jdbc:hive2://127.0.0.1:2181/demo_ds_1;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2
    ds_2:
        dataSourceClassName: com.zaxxer.hikari.HikariDataSource
        driverClassName: org.apache.hive.jdbc.HiveDriver
        jdbcUrl: jdbc:hive2://127.0.0.1:2181/demo_ds_2;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2
rules:
- !SHARDING
    tables:
      t_order:
        actualDataNodes:
        keyGenerateStrategy:
          column: order_id
          keyGeneratorName: snowflake
    defaultDatabaseStrategy:
      standard:
        shardingColumn: user_id
        shardingAlgorithmName: inline
    shardingAlgorithms:
      inline:
        type: INLINE
        props:
          algorithm-expression: ds_${user_id % 2}
    keyGenerators:
      snowflake:
        type: SNOWFLAKE
```

At this point, you can create the ShardingSphere data source normally and execute logical SQL on the virtual data source.

```java
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
public class ExampleUtils {
    void test() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:shardingsphere:classpath:demo.yaml");
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        try (HikariDataSource dataSource = new HikariDataSource(config);
             Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO t_order (user_id, order_type, address_id, status) VALUES (1, 1, 1, 'INSERT_TEST')");
            statement.executeQuery("SELECT * FROM t_order");
            statement.execute("DELETE FROM t_order WHERE order_id=1");
        }
    }
}
```

At this point, if the HiveServer2 example with the `service` named `apache-hive-1` is manually destroyed,
start the second HiveServer2 instance through another Docker Compose file with the following content,

```yaml
name: test-2
services:
  apache-hive-2:
    image: apache/hive:4.0.1
    environment:
      SERVICE_NAME: hiveserver2
      SERVICE_OPTS: >-
        -Dhive.server2.support.dynamic.service.discovery=true
        -Dhive.zookeeper.quorum=zookeeper:2181
        -Dhive.server2.thrift.bind.host=0.0.0.0
        -Dhive.server2.thrift.port=20000
    ports:
      - "20000:20000"
    networks:
      - test-1_default
networks:
  test-1_default:
    external: true
```

In DBeaver Community,
use `jdbcUrl` of `jdbc:hive2://127.0.0.1:2181/;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2` to connect to HiveServer2,
leave `username` and `password` blank.
Execute the following SQL,

```sql
-- noinspection SqlNoDataSourceInspectionForFile
CREATE DATABASE demo_ds_0;
CREATE DATABASE demo_ds_1;
CREATE DATABASE demo_ds_2;
```

Use `jdbcUrl` of `jdbc:hive2://127.0.0.1:2181/demo_ds_0;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2`,
`jdbc:hive2://127.0.0.1:2181/demo_ds_1;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2`
and `jdbc:hive2://127.0.0.1:2181/demo_ds_2;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2`
to connect to HiveServer2 and execute the following SQL,

```sql
-- noinspection SqlNoDataSourceInspectionForFile
set iceberg.mr.schema.auto.conversion=true;

CREATE TABLE IF NOT EXISTS t_order
(
    order_id   BIGINT,
    order_type INT,
    user_id    INT    NOT NULL,
    address_id BIGINT NOT NULL,
    status     VARCHAR(50),
    PRIMARY KEY (order_id) disable novalidate
) STORED BY ICEBERG STORED AS ORC TBLPROPERTIES ('format-version' = '2');

TRUNCATE TABLE t_order;
```

At this point,
the old ShardingSphere JDBC DataSource can still be switched to the HiveServer2 instance named `apache-hive-2` in the `service` to execute the logical SQL without recreating the JDBC DataSource.

```java
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
public class ExampleUtils {
    void test(HikariDataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO t_order (user_id, order_type, address_id, status) VALUES (1, 1, 1, 'INSERT_TEST')");
            statement.executeQuery("SELECT * FROM t_order");
            statement.execute("DELETE FROM t_order WHERE order_id=1");
        }
    }
}
```

## Usage Restrictions

### Version Restrictions

The lifecycle of HiveServer2 `2.x` and HiveServer2 `3.x` releases has ended.
Refer to https://lists.apache.org/thread/0mh4hvpllzv877bkx1f9srv1c3hlbtt9 and https://lists.apache.org/thread/mpzrv7v1hqqo4cmp0zorswnbvd7ltmbp .
ShardingSphere is only integrated tested for HiveServer2 `4.0.1`.

### Uber JAR Limitation of HiveServer2 JDBC Driver

Affected by https://issues.apache.org/jira/browse/HIVE-28445,
users should not use `org.apache.hive:hive-jdbc:4.0.1` with `classifier` as `standalone` to avoid dependency conflicts.

### Embedded HiveServer2 Limitation

Embedded HiveServer2 is no longer considered user-friendly by the Hive community, 
and users should not try to start embedded HiveServer2 through ShardingSphere's configuration file.
Users should always start HiveServer2 through HiveServer2's Docker Image `apache/hive:4.0.1`.
Reference https://issues.apache.org/jira/browse/HIVE-28418.

### Hadoop Limitations

Users can only use Hadoop `3.3.6` as the underlying Hadoop dependency of HiveServer2 JDBC Driver `4.0.1`.
HiveServer2 JDBC Driver `4.0.1` does not support Hadoop `3.4.1`,
Reference https://github.com/apache/hive/pull/5500.

### SQL Limitations

ShardingSphere JDBC DataSource does not yet support executing HiveServer2's `SET` statement, 
`CREATE TABLE` statement, and `TRUNCATE TABLE` statement.

Users should consider submitting a PR containing unit tests for ShardingSphere.

### Prerequisites for using DML SQL statements on ShardingSphere data sources

In order to be able to use DML SQL statements such as `delete`, 
users should consider using only ACID-supported tables in ShardingSphere JDBC when connecting to HiveServer2.
`apache/hive` provides multiple transaction solutions.

The first option is to use ACID tables, and the possible table creation process is as follows.
Due to its outdated catalog-based table format, 
users may have to wait before and after the execution of DML statements to allow HiveServer2 to complete inefficient DML operations.

```sql
-- noinspection SqlNoDataSourceInspectionForFile
set metastore.compactor.initiator.on=true;
set metastore.compactor.cleaner.on=true;
set metastore.compactor.worker.threads=5;

set hive.support.concurrency=true;
set hive.exec.dynamic.partition.mode=nonstrict;
set hive.txn.manager=org.apache.hadoop.hive.ql.lockmgr.DbTxnManager;

CREATE TABLE IF NOT EXISTS t_order
(
    order_id   BIGINT,
    order_type INT,
    user_id    INT    NOT NULL,
    address_id BIGINT NOT NULL,
    status     VARCHAR(50),
    PRIMARY KEY (order_id) disable novalidate
) CLUSTERED BY (order_id) INTO 2 BUCKETS STORED AS ORC TBLPROPERTIES ('transactional' = 'true');
```

The second option is to use Iceberg tables. The possible table creation process is as follows. Apache Iceberg table format is expected to replace the traditional Hive table format in the next few years.
Refer to https://blog.cloudera.com/from-hive-tables-to-iceberg-tables-hassle-free/ .

```sql
-- noinspection SqlNoDataSourceInspectionForFile
set iceberg.mr.schema.auto.conversion=true;

CREATE TABLE IF NOT EXISTS t_order
(
    order_id   BIGINT,
    order_type INT,
    user_id    INT    NOT NULL,
    address_id BIGINT NOT NULL,
    status     VARCHAR(50),
    PRIMARY KEY (order_id) disable novalidate
) STORED BY ICEBERG STORED AS ORC TBLPROPERTIES ('format-version' = '2');
```

The Iceberg table format supports relatively few Hive types. 
Setting `iceberg.mr.schema.auto.conversion` to `true` can help alleviate this problem.

### Transaction Limitations

HiveServer2 does not support local transactions at the ShardingSphere integration level, XA transactions, or Seata's AT mode transactions.
For more discussion, please visit https://cwiki.apache.org/confluence/display/Hive/Hive+Transactions.

### DBeaver Community Version Limitations

When users use DBeaver Community to connect to HiveServer2, they need to ensure that the DBeaver Community version is greater than or equal to `24.2.5`.

See https://github.com/dbeaver/dbeaver/pull/35059 .
