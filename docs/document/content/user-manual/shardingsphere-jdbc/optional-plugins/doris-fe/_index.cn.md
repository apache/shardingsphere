+++
title = "Doris FE"
weight = 6
+++

## 背景信息

ShardingSphere 默认情况下不提供对 `com.mysql.cj.jdbc.Driver` 的 `driverClassName` 的支持。
ShardingSphere 对 Doris FE 的支持位于可选模块中。

## 前提条件

要在 ShardingSphere 的配置文件为数据节点使用类似 `jdbc:mysql://localhost:9030/demo_ds_0` 的 `jdbcUrl`，
可能的 Maven 依赖关系如下，

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-jdbc-dialect-mysql</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>9.4.0</version>
    </dependency>
</dependencies>
```

## 配置示例

### 启动 Doris FE 和 Doris BE

编写 Docker Compose 文件来启动 Doris FE 和 Doris BE。

```yaml
services:
  doris:
    image: dyrnq/doris:4.0.0
    environment:
      RUN_MODE: standalone
      SKIP_CHECK_ULIMIT: true
    ports:
      - "9030:9030"
```

### 创建业务相关的库和表

通过第三方工具在 Doris FE 内创建业务相关的库和表。
以 DBeaver Community 为例，若使用 Ubuntu 24.04，可通过 Snapcraft 快速安装，

```shell
sudo apt update && sudo apt upgrade -y
sudo snap install dbeaver-ce --classic
snap run dbeaver-ce
```

在 DBeaver Community 内，使用 `jdbc:mysql://localhost:9030/` 的 `jdbcUrl`，`root` 的`username` 连接至 Doris FE，
`password` 留空。
执行如下 SQL，

```sql
-- noinspection SqlNoDataSourceInspectionForFile
CREATE DATABASE demo_ds_0;
CREATE DATABASE demo_ds_1;
CREATE DATABASE demo_ds_2;
```

分别使用 `jdbc:mysql://localhost:9030/demo_ds_0` ，
`jdbc:mysql://localhost:9030/demo_ds_1` 和 `jdbc:mysql://localhost:9030/demo_ds_2` 的 `jdbcUrl` 连接至 Doris FE 来执行如下 SQL，

```sql
-- noinspection SqlNoDataSourceInspectionForFile
CREATE TABLE IF NOT EXISTS t_order (
    order_id BIGINT NOT NULL AUTO_INCREMENT,
    order_type INT(11),
    user_id INT NOT NULL,
    address_id BIGINT NOT NULL,
    status VARCHAR(50)
) UNIQUE KEY (order_id) DISTRIBUTED BY HASH(order_id) PROPERTIES ('replication_num' = '1');
```

### 在业务项目创建 ShardingSphere 数据源

在业务项目引入`前提条件`涉及的依赖后，额外引入如下依赖，

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-infra-data-source-pool-hikari</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-infra-url-classpath</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-standalone-mode-repository-memory</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-sharding-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-authority-simple</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

在业务项目的 classpath 上编写 ShardingSphere 数据源的配置文件 `demo.yaml`，

```yaml
dataSources:
  ds_0:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.cj.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:9030/demo_ds_0
    username: root
  ds_1:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.cj.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:9030/demo_ds_1
    username: root
  ds_2:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.cj.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:9030/demo_ds_2
    username: root
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
            statement.execute("TRUNCATE TABLE t_order");
            statement.execute("INSERT INTO t_order (user_id, order_type, address_id, status) VALUES (1, 1, 1, 'INSERT_TEST')");
            statement.executeQuery("SELECT * FROM t_order");
            statement.execute("DELETE FROM t_order WHERE user_id=1");
            statement.execute("DROP TABLE IF EXISTS t_order");
        }
    }
}
```

## 使用限制

### SQL 限制

ShardingSphere JDBC DataSource 尚不支持执行 Doris FE 的 `create table` 语句。

### 事务限制

Doris FE 不支持 ShardingSphere 集成级别的本地事务，XA 事务或 Seata 的 AT 模式事务。
Doris FE 自身的事务支持存在问题，参考 https://doris.apache.org/docs/4.x/data-operate/transaction#failed-statements-within-a-transaction 。
对于 Doris FE，当事务中的某个语句执行失败时，这个操作已经自动回滚。然而，事务中其它执行成功的语句不会被自动回滚。

### 实验性模块限制

ShardingSphere 存在可选模块为 `org.apache.shardingsphere:shardingsphere-jdbc-dialect-doris`，用于支持 Doris FE 的特有数据库方言。可能的 Maven 依赖关系如下，

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-jdbc-dialect-doris</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>9.4.0</version>
    </dependency>
</dependencies>
```

`org.apache.shardingsphere:shardingsphere-jdbc-dialect-doris` 并未完全支持 Doris 数据库方言。对于未支持的 SQL 语法，以 https://github.com/apache/shardingsphere/issues?q=is%3Aissue%20state%3Aopen%20label%3A%22db%3A%20Doris%22%20label%3A%22in%3A%20SQL%20parse%22 的未关闭 issue 为准。

受 https://github.com/apache/shardingsphere/issues/36081 影响，对于同一 Maven 模块，若同时引入 `org.apache.shardingsphere:shardingsphere-jdbc-dialect-doris` 和 `org.apache.shardingsphere:shardingsphere-jdbc-dialect-mysql`，则向 Doris FE 执行的所有 SQL 均通过 MySQL 数据库方言解析。这将导致特定于 Doris 数据库方言的语法无法在 ShardingSphere 的逻辑数据库下使用。
