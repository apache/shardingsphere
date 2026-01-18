+++
title = "DistSQL(Alpha)"
weight = 6
chapter = true
+++

# 背景信息

当前可以通过 ShardingSphere JDBC DataSource 执行 DistSQL，以动态修改 ShardingSphere 配置。

# 配置示例

## 前提条件

在业务项目引入如下依赖，

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-dialect-mysql</artifactId>
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
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.2.224</version>
</dependency>
```

在业务项目的 classpath 上编写 ShardingSphere 数据源的配置文件 `demo.yaml`，

```yaml
props:
  sql-show: false
```

## 享受集成

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
            statement.execute("use logic_db");
            statement.execute("register storage unit if not exists ds_0 (url='jdbc:h2:mem:local_sharding_ds_0;MODE=MYSQL;IGNORECASE=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE',user='sa',password=''), ds_1 (url='jdbc:h2:mem:local_sharding_ds_1;MODE=MYSQL;IGNORECASE=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE',user='sa',password=''), ds_2 (url='jdbc:h2:mem:local_sharding_ds_2;MODE=MYSQL;IGNORECASE=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE',user='sa',password='')");
            statement.execute("create default sharding database strategy if not exists (type='standard', sharding_column=user_id,sharding_algorithm(type(name=class_based, properties('strategy'='STANDARD', 'algorithmClassName'='org.apache.shardingsphere.test.natived.commons.algorithm.ClassBasedInlineShardingAlgorithmFixture'))))");
            statement.execute("create sharding table rule if not exists t_order (datanodes('<LITERAL>ds_0.t_order, ds_1.t_order, ds_2.t_order'), key_generate_strategy(column=order_id,type(name='SNOWFLAKE'))), t_order_item (datanodes('<LITERAL>ds_0.t_order_item, ds_1.t_order_item, ds_2.t_order_item'), key_generate_strategy(column=order_item_id,type(name='SNOWFLAKE')))");
            statement.execute("create broadcast table rule if not exists t_address");
            statement.execute("CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT NOT NULL AUTO_INCREMENT,order_type INT(11),user_id INT NOT NULL,address_id BIGINT NOT NULL,status VARCHAR(50),PRIMARY KEY (order_id))");
            statement.execute("TRUNCATE TABLE t_order");
            statement.execute("INSERT INTO t_order (user_id, order_type, address_id, status) VALUES (1, 1, 1, 'INSERT_TEST')");
            statement.execute("SELECT * FROM t_order");
            statement.execute("DELETE FROM t_order WHERE user_id=1");
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("drop broadcast table rule if exists t_address");
            statement.execute("drop sharding table rule if exists t_order, t_order_item");
            statement.execute("drop default sharding database strategy if exists");
        }
    }
}
```

# 使用限制

## YAML 限制

使用 ShardingSphere JDBC Driver 前，总是需要创建 YAML 作为配置文件。
不支持类似 `jdbc:shardingsphere:scratch:logic_db` 的 URL scheme，
即在没有预先创建 YAML 配置文件的情况下直接创建逻辑数据库。

## 属性限制

如果需要定义 YAML 配置的 `props` 属性和 `databaseName` 属性，总是需要在 YAML 文件内配置，且无法修改。
可能的配置文件如下，

```yaml
props:
  sql-show: false
```

若不在 YAML 配置文件内定义 `databaseName` 属性，则默认的逻辑数据库名为 `logic_db`。

## SQL 限制

当前 ShardingSphere JDBC Driver 已引入 `shardingsphere-infra-distsql-handler`、`shardingsphere-sharding-distsql-handler` 与 `shardingsphere-broadcast-distsql-handler`，
因此支持 RDL（资源/规则定义）、RQL（规则查询）以及在这些模块 SPI 路径下可达的 RAL/RUL Executor 子集。

以下语句目前尚未支持：

- `CREATE DATABASE <name>` / `USE <name>` —— 创建或切换逻辑数据库的功能尚未实现，因此 JDBC DataSource 只能在 YAML 中指定的逻辑数据库上操作（默认 `logic_db`）。
- `UNREGISTER STORAGE UNIT` —— 当存在 sharding / single / broadcast 规则且规则仍引用这些 storage unit 时，最后一组 storage unit 的 unregister 会抛出 `MissingRequiredShardingConfigurationException`。`ShardingRuleBuilder` 在 resource map 为空时直接拒绝重建 `ShardingRule`，即便残留的 `ShardingRuleConfiguration` 只剩下 `defaultShardingColumn`、`keyGenerators` 这类与数据源无关的字段。仅 unregister 子集 storage unit 的场景可正常工作，只有当最后一个 storage unit 被移除时才会触发。
- `PREVIEW <SQL>` —— 其 `PreviewExecutor` 会将 JDBC 的 `DatabaseConnectionManager` 强制转换为 `ProxyDatabaseConnectionManager`，并使用 Proxy 端的 Executor Statement Manager，JDBC Driver 均不提供。Driver 会在 SPI 分发之前抛出 `SQLFeatureNotSupportedException` 拒绝该语句。即便通过 `shardingsphere-test-native` 等方式把 Proxy 后端模块加入 classpath，拒绝逻辑仍先发生，因此不会出现运行时的 `ClassCastException` 或 `NullPointerException`。
