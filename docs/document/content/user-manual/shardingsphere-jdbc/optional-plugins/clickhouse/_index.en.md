+++
title = "ClickHouse"
weight = 6
+++

## Background Information

ShardingSphere does not provide support for `driverClassName` of `com.clickhouse.jdbc.ClickHouseDriver` by default.
ShardingSphere's support for ClickHouse JDBC Driver is in the optional module.

## Prerequisites

To use a `standardJdbcUrl` like `jdbc:ch://localhost:8123/demo_ds_0` for the data node in the ShardingSphere configuration file,
the possible Maven dependencies are as follows,

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-jdbc</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-jdbc-dialect-clickhouse</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>com.clickhouse</groupId>
        <artifactId>clickhouse-jdbc</artifactId>
        <classifier>all</classifier>
        <version>0.9.4</version>
    </dependency>
</dependencies>
```

## Configuration example

### Start ClickHouse

Write a Docker Compose file to start ClickHouse.

```yaml
services:
  clickhouse-server:
    image: clickhouse/clickhouse-server:25.10.3.100
    environment:
      CLICKHOUSE_SKIP_USER_SETUP: "1"
    ports:
      - "8123:8123"
```

### Create business tables

Use a third-party tool to create some business databases and business tables in ClickHouse.
Taking DBeaver Community as an example, if you use Ubuntu 22.04.4, you can quickly install it through Snapcraft.

```shell
sudo apt update && sudo apt upgrade -y
sudo snap install dbeaver-ce
snap run dbeaver-ce
```

In DBeaver Community, use `standardJdbcUrl` of `jdbc:ch://localhost:8123/default`, `username` of `default` to connect to ClickHouse, 
and leave `password` blank.
Execute the following SQL,

```sql
-- noinspection SqlNoDataSourceInspectionForFile
CREATE DATABASE demo_ds_0;
CREATE DATABASE demo_ds_1;
CREATE DATABASE demo_ds_2;
```

Use `standardJdbcUrl` of `jdbc:ch://localhost:8123/demo_ds_0`, 
`jdbc:ch://localhost:8123/demo_ds_1` and `jdbc:ch://localhost:8123/demo_ds_2`
to connect to ClickHouse and execute the following SQL.

```sql
-- noinspection SqlNoDataSourceInspectionForFile
create table IF NOT EXISTS t_order (
    order_id   Int64 NOT NULL,
    order_type Int32,
    user_id    Int32 NOT NULL,
    address_id Int64 NOT NULL,
    status     VARCHAR(50)
) engine = MergeTree
    primary key (order_id)
    order by (order_id);

TRUNCATE TABLE t_order;
```

### Create ShardingSphere data source in business project

After the business project introduces the dependencies involved in `prerequisites`, 
write the ShardingSphere data source configuration file `demo.yaml` on the classpath of the business project.

```yaml
dataSources:
    ds_0:
        dataSourceClassName: com.zaxxer.hikari.HikariDataSource
        driverClassName: com.clickhouse.jdbc.ClickHouseDriver
        standardJdbcUrl: jdbc:ch://localhost:8123/demo_ds_0
        username: default
        password:
    ds_1:
        dataSourceClassName: com.zaxxer.hikari.HikariDataSource
        driverClassName: com.clickhouse.jdbc.ClickHouseDriver
        standardJdbcUrl: jdbc:ch://localhost:8123/demo_ds_1
        username: default
        password:
    ds_2:
        dataSourceClassName: com.zaxxer.hikari.HikariDataSource
        driverClassName: com.clickhouse.jdbc.ClickHouseDriver
        standardJdbcUrl: jdbc:ch://localhost:8123/demo_ds_2
        username: default
        password:
rules:
- !SHARDING
    tables:
      t_order:
        actualDataNodes: <LITERAL>ds_0.t_order, ds_1.t_order, ds_2.t_order
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

### Enjoy integration

Create a ShardingSphere data source to enjoy integration,

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
            statement.execute("alter table t_order delete where user_id=1");
        }
    }
}
```

## Usage Limitations

### SQL Limitations

ShardingSphere JDBC DataSource does not yet support executing ClickHouse's `create table`, `truncate table`,
and `drop table` statements.
The current ShardingSphere parsing of ClickHouse's `INNER JOIN` syntax has shortcomings, 
and it may return incorrect query results for SQL statements such as `SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id`.

### Key Generate restrictions

Due to the issue mentioned in https://github.com/ClickHouse/ClickHouse/issues/21697 ,
because ClickHouse does not support the `INSERT ... RETURNING` syntax,
developers cannot obtain distributed sequences after executing `INSERT` SQL into ShardingSphere's logical data source. 
Specifically, the following operations are not allowed:

```java
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.*;
public class ExampleTest {
    long test() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:shardingsphere:classpath:demo.yaml");
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        try (HikariDataSource dataSource = new HikariDataSource(config);
             Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO t_order (user_id, order_type, address_id, status) VALUES (1, 1, 1, 'INSERT_TEST')",
                     Statement.RETURN_GENERATED_KEYS
             )) {
            preparedStatement.executeUpdate();
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
                throw new RuntimeException();
            }
        }
    }
}
```

### Transaction Limitations

ClickHouse does not support local transactions at the ShardingSphere integration level, XA transactions, or AT mode transactions for Seata,
More discussion is at https://github.com/ClickHouse/clickhouse-docs/issues/2300 .

This has nothing to do with the `Transactions, Commit, and Rollback` feature provided by https://clickhouse.com/docs/en/guides/developer/transactional for ClickHouse,
but only with `com.clickhouse.jdbc.ConnectionImpl` not implementing `java.sql.Connection#rollback()`.
See https://github.com/ClickHouse/clickhouse-java/issues/2023 .

### Embedded ClickHouse Limitations

The embedded ClickHouse `chDB` Java client has not been released yet.
ShardingSphere does not do integration testing for the SNAPSHOT version of https://github.com/chdb-io/chdb-java .
Refer to https://github.com/chdb-io/chdb/issues/243 .
