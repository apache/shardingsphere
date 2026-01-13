+++
title = "Firebird"
weight = 6
+++

## 背景信息

ShardingSphere 默认情况下不提供对 `org.firebirdsql.jdbc.FBDriver` 的 `driverClassName` 的支持。
ShardingSphere 对 Firebird JDBC Driver 的支持位于可选模块中。

## 前提条件

要在 ShardingSphere 的配置文件为数据节点使用类似 `jdbc:firebird://localhost:3050//var/lib/firebird/data/demo_ds_0.fdb` 的 `standardJdbcUrl`，
可能的 Maven 依赖关系如下，

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-jdbc-dialect-firebird</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>org.firebirdsql.jdbc</groupId>
        <artifactId>jaybird</artifactId>
        <version>5.0.10.java8</version>
    </dependency>
</dependencies>
```

## 配置示例

### 启动 Firebird

编写 Docker Compose 文件来启动 Firebird。

```yaml
services:
  firebird:
    image: firebirdsql/firebird:5.0.3
    environment:
      FIREBIRD_ROOT_PASSWORD: masterkey
      FIREBIRD_USER: alice
      FIREBIRD_PASSWORD: masterkey
      FIREBIRD_DATABASE: mirror.fdb
      FIREBIRD_DATABASE_DEFAULT_CHARSET: UTF8
    ports:
      - "3050:3050"
```

### 创建业务库

通过第三方工具在 Firebird 内创建业务库。

包括 DBeaver Community 在内的第三方工具无法为 Firebird 创建 databases，
下以 Maven 模块 `org.firebirdsql.jdbc:jaybird:5.0.10.java8` 的 Java API 为例，

```java
import org.firebirdsql.management.FBManager;
import org.firebirdsql.management.PageSizeConstants;
class Solution {
    void test() throws Exception {
        try (FBManager fbManager = new FBManager()) {
            fbManager.setServer("localhost");
            fbManager.setUserName("alice");
            fbManager.setPassword("masterkey");
            fbManager.setFileName("/var/lib/firebird/data/mirror.fdb");
            fbManager.setPageSize(PageSizeConstants.SIZE_16K);
            fbManager.setDefaultCharacterSet("UTF8");
            fbManager.setPort(3050);
            fbManager.start();
            fbManager.createDatabase("/var/lib/firebird/data/demo_ds_0.fdb", "alice", "masterkey");
            fbManager.createDatabase("/var/lib/firebird/data/demo_ds_1.fdb", "alice", "masterkey");
            fbManager.createDatabase("/var/lib/firebird/data/demo_ds_2.fdb", "alice", "masterkey");
        }
    }
}
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
    driverClassName: org.firebirdsql.jdbc.FBDriver
    standardJdbcUrl: jdbc:firebird://localhost:3050//var/lib/firebird/data/demo_ds_0.fdb
    username: alice
    password: masterkey
  ds_1:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: org.firebirdsql.jdbc.FBDriver
    standardJdbcUrl: jdbc:firebird://localhost:3050//var/lib/firebird/data/demo_ds_1.fdb
    username: alice
    password: masterkey
  ds_2:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: org.firebirdsql.jdbc.FBDriver
    standardJdbcUrl: jdbc:firebird://localhost:3050//var/lib/firebird/data/demo_ds_2.fdb
    username: alice
    password: masterkey
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
class Solution {
    void test() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:shardingsphere:classpath:demo.yaml");
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        try (HikariDataSource dataSource = new HikariDataSource(config);
             Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE t_order (order_id BIGINT generated by default as identity PRIMARY KEY, order_type INT, user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50))");
            statement.execute("INSERT INTO t_order (user_id, order_type, address_id, status) VALUES (1, 1, 1, 'INSERT_TEST')");
            statement.executeQuery("SELECT * FROM t_order");
            statement.execute("DELETE FROM t_order WHERE user_id=1");
            statement.execute("DROP TABLE t_order");
        }
    }
}
```

## 使用限制

### 事务限制

Firebird 支持 ShardingSphere 集成级别的本地事务，但不支持 XA 事务或 Seata 的 AT 模式事务。

对 XA 事务的讨论位于 https://github.com/apache/shardingsphere/issues/34973 。

对 Seata 的 AT 模式事务的处理，则应在 https://github.com/apache/incubator-seata 提交包含对应实现的 PR。
