+++
title = "Doris FE"
weight = 6
+++

## 背景信息

ShardingSphere 对 Doris FE 的 SQL 方言支持位于可选模块中。

## 前提条件

当 ShardingSphere 的配置文件通过 MySQL JDBC Driver 连接至 Doris FE 时，要为 Doris FE 的数据节点使用 Doris FE 的 SQL 方言模块，
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

## 配置示例

### 启动 Doris FE 和 Doris BE

编写 Docker Compose 文件来启动 Doris FE 和 Doris BE。

```yaml
services:
  doris:
    image: dyrnq/doris:3.0.5
    environment:
      RUN_MODE: standalone
      SKIP_CHECK_ULIMIT: true
    ports:
      - "9030:9030"
```

### 创建业务库

通过第三方工具在 Doris FE 内创建业务库。
以 DBeaver Community 为例，若使用 Ubuntu 22.04.4，可通过 Snapcraft 快速安装，

```shell
sudo apt update && sudo apt upgrade -y
sudo snap install dbeaver-ce
snap run dbeaver-ce
```

在 DBeaver Community 内，使用 `jdbc:mysql://localhost:9030/` 的 `jdbcUrl`，`root` 的 `username` 连接至 Doris FE，
`password` 留空。
执行如下 SQL，

```sql
-- noinspection SqlNoDataSourceInspectionForFile
CREATE DATABASE demo_ds_0;
CREATE DATABASE demo_ds_1;
CREATE DATABASE demo_ds_2;
```

### 在业务项目创建 ShardingSphere 数据源

在业务项目引入`前提条件`涉及的依赖后，在业务项目的 classpath 上编写 ShardingSphere 数据源的配置文件`demo.yaml`，

```yaml
dataSources:
  ds_0:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.cj.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:9030/demo_ds_0
    username: root
    password: 
  ds_1:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.cj.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:9030/demo_ds_1
    username: root
    password: 
  ds_2:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.cj.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:9030/demo_ds_2
    username: root
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
class Solution {
    void test() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:shardingsphere:classpath:demo.yaml");
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        try (HikariDataSource dataSource = new HikariDataSource(config);
             Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS t_order ( order_id BIGINT NOT NULL AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50)) PROPERTIES ('replication_num' = '1')");
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

ShardingSphere JDBC DataSource 尚不完全支持执行 Doris FE 的 `create table` 语句。
这意味着 ShardingSphere JDBC DataSource 可执行类似如下的语句,

```sql
-- noinspection SqlNoDataSourceInspectionForFile
CREATE TABLE IF NOT EXISTS t_order (
    order_id BIGINT NOT NULL AUTO_INCREMENT,
    order_type INT(11),
    user_id INT NOT NULL,
    address_id BIGINT NOT NULL,
    status VARCHAR(50)
)
PROPERTIES ('replication_num' = '1');
```

但 ShardingSphere JDBC DataSource 无法执行类似如下的语句,

```sql
-- noinspection SqlNoDataSourceInspectionForFile
CREATE TABLE IF NOT EXISTS t_order (
    order_id BIGINT NOT NULL AUTO_INCREMENT,
    order_type INT(11),
    user_id INT NOT NULL,
    address_id BIGINT NOT NULL,
    status VARCHAR(50)
)
UNIQUE KEY (order_id) DISTRIBUTED BY HASH(order_id) PROPERTIES ('replication_num' = '1');
```

### 事务限制

Doris FE 不支持 ShardingSphere 集成级别的本地事务，XA 事务或 Seata 的 AT 模式事务。

Doris FE 自身的事务支持并不完整。当单个事务单元中存在执行失败的 SQL 语句时，此事务单元中已执行成功的 SQL 语句不会回滚。
参考 https://doris.apache.org/docs/3.0/data-operate/transaction#failed-statements-within-a-transaction 。
