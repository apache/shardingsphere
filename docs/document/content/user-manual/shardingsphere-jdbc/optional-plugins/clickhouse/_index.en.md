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
        <classifier>http</classifier>
        <version>0.6.3</version>
    </dependency>
</dependencies>
```

## Configuration example

### Start ClickHouse

Write a Docker Compose file to start ClickHouse.

```yaml
services:
  clickhouse-server:
    image: clickhouse/clickhouse-server:25.6.5.41
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
Users should consider submitting a PR containing unit tests for ShardingSphere.

### Key Generate restrictions

The column type corresponding to the Key Generate function of ClickHouse itself is `UUID`, 
and `UUID` is received as `java.util.UUID` in ClickHouse JDBC Driver,
refer to https://github.com/ClickHouse/ClickHouse/issues/56228 .
The column type corresponding to the Key Generate SPI implementation of ShardingSphere's `SNOWFLAKE` is `UInt64`,
which is received as `java.lang.Long` in ShardingSphere JDBC Driver.

When configuring ShardingSphere to connect to ClickHouse, 
if ShardingSphere is also configured to use the Key Generate SPI implementation of `SNOWFLAKE`,
the column type in the ClickHouse real database used by ShardingSphere's Key Generate function should not be set to `UUID`.

Because `com.clickhouse.jdbc.ClickHouseConnection#prepareStatement(String, int)` of `com.clickhouse:clickhouse-jdbc:0.6.3:http`
Maven module intentionally throws an exception when `autoGeneratedKeys` is `java.sql.Statement.RETURN_GENERATED_KEYS`,
to prevent ShardingSphere from proxying `com.clickhouse.jdbc.internal.ClickHouseConnectionImpl` normally,
therefore, if users need to obtain the Key generated by ShardingSphere from the JDBC business code, 
they need to set `autoGeneratedKeys` to `java.sql.Statement.NO_GENERATED_KEYS`.

A possible example is as follows,

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
                     Statement.NO_GENERATED_KEYS
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

### Limitations of ClickHouse JDBC Driver V2

Starting from the `0.8.6` milestone at https://github.com/ClickHouse/clickhouse-java/pull/2368 , 
ClickHouse JDBC Driver V2 uses `org.antlr:antlr4-maven-plugin:4.13.2`. 
This conflicts with `org.antlr:antlr4-runtime:4.10.1` used by ShardingSphere.
ShardingSphere only uses `com.clickhouse:clickhouse-jdbc:0.6.3:http` to test ClickHouse integration.
