+++
title = "ClickHouse"
weight = 6
+++

## Background Information

ShardingSphere does not provide support for `driverClassName` of `com.clickhouse.jdbc.ClickHouseDriver` by default.
ShardingSphere's support for ClickHouse JDBC Driver is in the optional module.

## Prerequisites

To use a `jdbcUrl` like `jdbc:ch://localhost:8123/demo_ds_0` for the data node in the ShardingSphere configuration file,
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
        <artifactId>shardingsphere-parser-sql-clickhouse</artifactId>
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
    image: clickhouse/clickhouse-server:24.11.1.2557
    ports:
      - "8123:8123"
```

### Create business tables

Use a third-party tool to create a business database and business table in ClickHouse.
Taking DBeaver Community as an example, if you use Ubuntu 22.04.4, you can quickly install it through Snapcraft.

```shell
sudo apt update && sudo apt upgrade -y
sudo snap install dbeaver-ce
snap run dbeaver-ce
```

In DBeaver Community, use `jdbcUrl` of `jdbc:ch://localhost:8123/default`, `username` of `default` to connect to ClickHouse, 
and leave `password` blank.
Execute the following SQL,

```sql
-- noinspection SqlNoDataSourceInspectionForFile
CREATE DATABASE demo_ds_0;
CREATE DATABASE demo_ds_1;
CREATE DATABASE demo_ds_2;
```

Use `jdbcUrl` of `jdbc:ch://localhost:8123/demo_ds_0`, 
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
        jdbcUrl: jdbc:ch://localhost:8123/demo_ds_0
        username: default
        password:
    ds_1:
        dataSourceClassName: com.zaxxer.hikari.HikariDataSource
        driverClassName: com.clickhouse.jdbc.ClickHouseDriver
        jdbcUrl: jdbc:ch://localhost:8123/demo_ds_1
        username: default
        password:
    ds_2:
        dataSourceClassName: com.zaxxer.hikari.HikariDataSource
        driverClassName: com.clickhouse.jdbc.ClickHouseDriver
        jdbcUrl: jdbc:ch://localhost:8123/demo_ds_2
        username: default
        password:
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
            statement.execute("alter table t_order delete where order_id=1");
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

ClickHouse does not support XA transactions at the ShardingSphere integration level,
because https://github.com/ClickHouse/clickhouse-java does not implement the relevant Java interface of `javax.sql.XADataSource`.

ClickHouse does not support Seata AT mode transactions at the ShardingSphere integration level,
because https://github.com/apache/incubator-seata does not implement ClickHouse's SQL dialect parsing.

ClickHouse supports local transactions at the ShardingSphere integration level, but additional configuration of ClickHouse is required,
For more discussion, please visit https://github.com/ClickHouse/clickhouse-docs/issues/2300 .

Introduce the discussion of writing a Docker Compose file to start ClickHouse and ClickHouse Keeper.

```yaml
services:
  clickhouse-keeper-01:
    image: clickhouse/clickhouse-keeper:24.11.1.2557
    volumes:
      - ./keeper_config.xml:/etc/clickhouse-keeper/keeper_config.xml
  clickhouse-server:
    image: clickhouse/clickhouse-server:24.11.1.2557
    depends_on:
      - clickhouse-keeper-01
    ports:
      - "8123:8123"
    volumes:
      - ./transactions.xml:/etc/clickhouse-server/config.d/transactions.xml
```

The content of `./keeper_config.xml` is as follows,

```xml
<clickhouse replace="true">
    <listen_host>0.0.0.0</listen_host>
    <keeper_server>
        <tcp_port>9181</tcp_port>
        <server_id>1</server_id>
        <snapshot_storage_path>/var/lib/clickhouse/coordination/snapshots</snapshot_storage_path>
        <raft_configuration>
            <server>
                <id>1</id>
                <hostname>clickhouse-keeper-01</hostname>
                <port>9234</port>
            </server>
        </raft_configuration>
    </keeper_server>
</clickhouse>
```

The content of `./transactions.xml` is as follows,

```xml
<clickhouse>
    <allow_experimental_transactions>1</allow_experimental_transactions>
    <zookeeper>
        <node index="1">
            <host>clickhouse-keeper-01</host>
            <port>9181</port>
        </node>
    </zookeeper>
</clickhouse>
```

In DBeaver Community, use `jdbcUrl` of `jdbc:ch://localhost:8123/default`, `username` of `default` to connect to ClickHouse, 
and leave `password` blank. Execute the following SQL,

```sql
-- noinspection SqlNoDataSourceInspectionForFile
CREATE DATABASE demo_ds_0;
CREATE DATABASE demo_ds_1;
CREATE DATABASE demo_ds_2;
```

Use `jdbcUrl` of `jdbc:ch://localhost:8123/demo_ds_0`, `jdbc:ch://localhost:8123/demo_ds_1`
and `jdbc:ch://localhost:8123/demo_ds_2` to connect to ClickHouse and execute the following SQL.

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

After the business project introduces the dependencies involved in the `prerequisites`, 
write the ShardingSphere data source configuration file `demo.yaml` on the classpath of the business project.

```yaml
dataSources:
    ds_0:
        dataSourceClassName: com.zaxxer.hikari.HikariDataSource
        driverClassName: com.clickhouse.jdbc.ClickHouseDriver
        jdbcUrl: jdbc:ch://localhost:8123/demo_ds_0?transactionSupport=true
        username: default
        password:
    ds_1:
        dataSourceClassName: com.zaxxer.hikari.HikariDataSource
        driverClassName: com.clickhouse.jdbc.ClickHouseDriver
        jdbcUrl: jdbc:ch://localhost:8123/demo_ds_1?transactionSupport=true
        username: default
        password:
    ds_2:
        dataSourceClassName: com.zaxxer.hikari.HikariDataSource
        driverClassName: com.clickhouse.jdbc.ClickHouseDriver
        jdbcUrl: jdbc:ch://localhost:8123/demo_ds_2?transactionSupport=true
        username: default
        password:
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

After creating the ShardingSphere data source, local transactions can be used normally.

```java
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
@SuppressWarnings({"SqlNoDataSourceInspection", "AssertWithSideEffects"})
public class ExampleUtils {
    void test() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:shardingsphere:classpath:demo.yaml");
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        try (HikariDataSource dataSource = new HikariDataSource(config); Connection connection = dataSource.getConnection()) {
            try {
                connection.setAutoCommit(false);
                connection.createStatement().executeUpdate("INSERT INTO t_order (user_id, order_type, address_id, status) VALUES (2024, 0, 2024, 'INSERT_TEST')");
                connection.createStatement().executeUpdate("INSERT INTO t_order_does_not_exist (test_id_does_not_exist) VALUES (2024)");
                connection.commit();
            } catch (final SQLException ignored) {
                connection.rollback();
            } finally {
                connection.setAutoCommit(true);
            }
            try (Connection conn = dataSource.getConnection()) {
                assert !conn.createStatement().executeQuery("SELECT * FROM t_order WHERE user_id = 2024").next();
            }
        }
    }
}
```

Once `transactionSupport=true` is set for the jdbcUrl of ClickHouse JDBC Driver in the ShardingSphere configuration file,
users should ensure that there are no unfinished `insert` statements before executing the `alter table` statement to avoid the following Error.

```shell
java.sql.BatchUpdateException: Code: 341. DB::Exception: Exception happened during execution of mutation 'mutation_6.txt' with part 'all_1_1_0' reason: 'Serialization error: part all_1_1_0 is locked by transaction 5672402456378293316'. This error maybe retryable or not. In case of unretryable error, mutation can be killed with KILL MUTATION query. (UNFINISHED) (version 24.10.2.80 (official build))
	at com.clickhouse.jdbc.SqlExceptionUtils.batchUpdateError(SqlExceptionUtils.java:107)
	at com.clickhouse.jdbc.internal.SqlBasedPreparedStatement.executeAny(SqlBasedPreparedStatement.java:223)
	at com.clickhouse.jdbc.internal.SqlBasedPreparedStatement.executeLargeUpdate(SqlBasedPreparedStatement.java:302)
	at com.clickhouse.jdbc.internal.AbstractPreparedStatement.executeUpdate(AbstractPreparedStatement.java:135)
```

### Embedded ClickHouse Limitations

The embedded ClickHouse `chDB` Java client has not been released yet.
ShardingSphere does not do integration testing for the SNAPSHOT version of https://github.com/chdb-io/chdb-java .
Refer to https://github.com/chdb-io/chdb/issues/243 .
