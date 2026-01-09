+++
title = "ClickHouse"
weight = 6
+++

## 背景信息

ShardingSphere 默认情况下不提供对 `com.clickhouse.jdbc.ClickHouseDriver` 的 `driverClassName` 的支持。
ShardingSphere 对 ClickHouse JDBC Driver 的支持位于可选模块中。

## 前提条件

要在 ShardingSphere 的配置文件为数据节点使用类似 `jdbc:ch://localhost:8123/demo_ds_0` 的 `standardJdbcUrl`，
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
        <artifactId>shardingsphere-jdbc-dialect-clickhouse</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>com.clickhouse</groupId>
        <artifactId>clickhouse-jdbc</artifactId>
        <classifier>all</classifier>
        <version>0.9.5</version>
    </dependency>
</dependencies>
```

## 配置示例

### 启动 ClickHouse

编写 Docker Compose 文件来启动 ClickHouse。

```yaml
services:
  clickhouse-server:
    image: clickhouse/clickhouse-server:25.12.1.649
    environment:
      CLICKHOUSE_SKIP_USER_SETUP: "1"
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

在 DBeaver Community 内，使用 `jdbc:ch://localhost:8123/default` 的 `standardJdbcUrl`，`default` 的`username` 连接至 ClickHouse，
`password` 留空。
执行如下 SQL，

```sql
-- noinspection SqlNoDataSourceInspectionForFile
CREATE DATABASE demo_ds_0;
CREATE DATABASE demo_ds_1;
CREATE DATABASE demo_ds_2;
```

分别使用 `jdbc:ch://localhost:8123/demo_ds_0` ，
`jdbc:ch://localhost:8123/demo_ds_1` 和 `jdbc:ch://localhost:8123/demo_ds_2` 的 `standardJdbcUrl` 连接至 ClickHouse 来执行如下 SQL，

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
            statement.execute("alter table t_order delete where user_id=1");
        }
    }
}
```

## 使用限制

### SQL 限制

ShardingSphere JDBC DataSource 尚不支持执行 ClickHouse 的 `create table`，`truncate table` 和 `drop table` 语句。
当前 ShardingSphere 对 ClickHouse 的 `INNER JOIN` 语法解析存在不足，
对 `SELECT i.* FROM t_order o, t_order_item i WHERE o.order_id = i.order_id` 这类 SQL，它可能返回错误的查询结果。

### 分布式序列限制

受 https://github.com/ClickHouse/ClickHouse/issues/21697 影响，
由于 ClickHouse 不支持 `INSERT ... RETURNING` 语法，
开发者无法在向 ShardingSphere 的逻辑数据源执行 `INSERT` SQL 后获得分布式序列。即，如下操作是不允许的，

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

### 事务限制

ClickHouse 不支持 ShardingSphere 集成级别的本地事务，XA 事务或 Seata 的 AT 模式事务，
更多讨论位于 https://github.com/ClickHouse/clickhouse-docs/issues/2300 。

这与 https://clickhouse.com/docs/en/guides/developer/transactional 为 ClickHouse 提供的 `Transactions, Commit, and Rollback` 功能无关，
仅与 `com.clickhouse.jdbc.ConnectionImpl` 未实现 `java.sql.Connection#rollback()` 有关。
参考 https://github.com/ClickHouse/clickhouse-java/issues/2023 。

### 嵌入式 ClickHouse 限制

嵌入式 ClickHouse `chDB` 尚未发布 Java 客户端，
ShardingSphere 不针对 SNAPSHOT 版本的 https://github.com/chdb-io/chdb-java 做集成测试。
参考 https://github.com/chdb-io/chdb/issues/243 。
