+++
title = "ClickHouse"
weight = 6
+++

## 背景信息

ShardingSphere 默认情况下不提供对 `com.clickhouse.jdbc.ClickHouseDriver` 的 `driverClassName` 的支持。
ShardingSphere 对 ClickHouse JDBC Driver 的支持位于可选模块中。

## 前提条件

要在 ShardingSphere 的配置文件为数据节点使用类似 `jdbc:ch://localhost:8123/demo_ds_0` 的 `jdbcUrl`，
可能的 Maven 依赖关系如下，

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

## 配置示例

### 启动 ClickHouse

编写 Docker Compose 文件来启动 ClickHouse。

```yaml
services:
  clickhouse-server:
    image: clickhouse/clickhouse-server:24.11.1.2557
    ports:
      - "8123:8123"
```

### 创建业务表

通过第三方工具在 ClickHouse 内创建业务库与业务表。
以 DBeaver Community 为例，若使用 Ubuntu 22.04.4，可通过 Snapcraft 快速安装，

```shell
sudo apt update && sudo apt upgrade -y
sudo snap install dbeaver-ce
snap run dbeaver-ce
```

在 DBeaver Community 内，使用 `jdbc:ch://localhost:8123/default` 的 `jdbcUrl`，`default` 的`username` 连接至 ClickHouse，
`password` 留空。
执行如下 SQL，

```sql
-- noinspection SqlNoDataSourceInspectionForFile
CREATE DATABASE demo_ds_0;
CREATE DATABASE demo_ds_1;
CREATE DATABASE demo_ds_2;
```

分别使用 `jdbc:ch://localhost:8123/demo_ds_0` ，
`jdbc:ch://localhost:8123/demo_ds_1` 和 `jdbc:ch://localhost:8123/demo_ds_2` 的 `jdbcUrl` 连接至 ClickHouse 来执行如下 SQL，

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

### 在业务项目创建 ShardingSphere 数据源

在业务项目引入`前提条件`涉及的依赖后，在业务项目的 classpath 上编写 ShardingSphere 数据源的配置文件`demo.yaml`，

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

### 享受集成

创建 ShardingSphere 的数据源以享受集成，

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

## 使用限制

### SQL 限制

ShardingSphere JDBC DataSource 尚不支持执行 ClickHouse 的 `create table`，`truncate table` 和 `drop table` 语句。
用户应考虑为 ShardingSphere 提交包含单元测试的 PR。

### 分布式序列限制

ClickHouse 自身的，对应分布式序列功能的列类型是 `UUID`，`UUID` 在 ClickHouse JDBC Driver 中接收为 `java.util.UUID`，
参考 https://github.com/ClickHouse/ClickHouse/issues/56228 。 
而 ShardingSphere 的 `SNOWFLAKE` 的分布式序列 SPI 实现对应的列类型是 `UInt64`，
在 ShardingSphere JDBC Driver 中接收为 `java.lang.Long`。

当为 ShardingSphere 配置连接至 ClickHouse 时， 若同时配置了 ShardingSphere 使用 `SNOWFLAKE` 的分布式序列 SPI 实现，
ShardingSphere 的分布式序列功能使用的 ClickHouse 真实数据库中的列类型不应该被设置为 `UUID`。

由于 `com.clickhouse:clickhouse-jdbc:0.6.3:http` Maven 模块的 `com.clickhouse.jdbc.ClickHouseConnection#prepareStatement(String, int)`
故意在 `autoGeneratedKeys` 为 `java.sql.Statement.RETURN_GENERATED_KEYS` 时抛出异常，
以阻止 ShardingSphere 正常代理 `com.clickhouse.jdbc.internal.ClickHouseConnectionImpl`，
因此如果用户需要从 JDBC 业务代码获取 ShardingSphere 生成的分布式序列，需要将 `autoGeneratedKeys` 置为 `java.sql.Statement.NO_GENERATED_KEYS`。

一个可能的示例如下，

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

### 事务限制

ClickHouse 不支持 ShardingSphere 集成级别的 XA 事务，
因为 https://github.com/ClickHouse/clickhouse-java 未实现 `javax.sql.XADataSource` 的相关 Java 接口。

ClickHouse 不支持 ShardingSphere 集成级别的 Seata AT 模式事务，
因为 https://github.com/apache/incubator-seata 未实现 ClickHouse 的 SQL 方言解析。

ClickHouse 支持 ShardingSphere 集成级别的本地事务，但需要对 ClickHouse 进行额外配置， 
更多讨论位于 https://github.com/ClickHouse/clickhouse-docs/issues/2300 。

引入讨论，编写 Docker Compose 文件来启动 ClickHouse 和 ClickHouse Keeper。

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

`./keeper_config.xml` 的内容如下，

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

`./transactions.xml` 的内容如下，

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

在 DBeaver Community 内，使用 `jdbc:ch://localhost:8123/default` 的 `jdbcUrl`，`default` 的`username` 连接至 ClickHouse，
`password` 留空。
执行如下 SQL，

```sql
-- noinspection SqlNoDataSourceInspectionForFile
CREATE DATABASE demo_ds_0;
CREATE DATABASE demo_ds_1;
CREATE DATABASE demo_ds_2;
```

分别使用 `jdbc:ch://localhost:8123/demo_ds_0` ，
`jdbc:ch://localhost:8123/demo_ds_1` 和 `jdbc:ch://localhost:8123/demo_ds_2` 的 `jdbcUrl` 连接至 ClickHouse 来执行如下 SQL，

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

在业务项目引入`前提条件`涉及的依赖后，在业务项目的 classpath 上编写 ShardingSphere 数据源的配置文件`demo.yaml`，

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

创建 ShardingSphere 的数据源后可正常使用本地事务，

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

一旦在 ShardingSphere 的配置文件为 ClickHouse JDBC Driver 的 jdbcUrl 设置 `transactionSupport=true`，
用户在执行 `alter table` 语句前应确保没有尚未完成执行的 `insert` 语句，以避免如下 Error 的发生，

```shell
java.sql.BatchUpdateException: Code: 341. DB::Exception: Exception happened during execution of mutation 'mutation_6.txt' with part 'all_1_1_0' reason: 'Serialization error: part all_1_1_0 is locked by transaction 5672402456378293316'. This error maybe retryable or not. In case of unretryable error, mutation can be killed with KILL MUTATION query. (UNFINISHED) (version 24.10.2.80 (official build))
	at com.clickhouse.jdbc.SqlExceptionUtils.batchUpdateError(SqlExceptionUtils.java:107)
	at com.clickhouse.jdbc.internal.SqlBasedPreparedStatement.executeAny(SqlBasedPreparedStatement.java:223)
	at com.clickhouse.jdbc.internal.SqlBasedPreparedStatement.executeLargeUpdate(SqlBasedPreparedStatement.java:302)
	at com.clickhouse.jdbc.internal.AbstractPreparedStatement.executeUpdate(AbstractPreparedStatement.java:135)
```

### 嵌入式 ClickHouse 限制

嵌入式 ClickHouse `chDB` 尚未发布 Java 客户端，
ShardingSphere 不针对 SNAPSHOT 版本的 https://github.com/chdb-io/chdb-java 做集成测试。
参考 https://github.com/chdb-io/chdb/issues/243 。
