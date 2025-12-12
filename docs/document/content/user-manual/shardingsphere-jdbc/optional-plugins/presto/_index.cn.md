+++
title = "Presto"
weight = 6
+++

## 背景信息

ShardingSphere 默认情况下不提供对 `com.facebook.presto.jdbc.PrestoDriver` 的 `driverClassName` 的支持。
ShardingSphere 对 Presto JDBC Driver 的支持位于可选模块中。

## 前提条件

要在 ShardingSphere 的配置文件为数据节点使用类似 `jdbc:presto://localhost:8080/iceberg/demo_ds_0` 的 `standardJdbcUrl`，
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
        <artifactId>shardingsphere-jdbc-dialect-presto</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>com.facebook.presto</groupId>
        <artifactId>presto-jdbc</artifactId>
        <version>0.292</version>
    </dependency>
</dependencies>
```

## 配置示例

### 启动 Presto

编写 Docker Compose 文件来启动 Presto。这将启动一个既为协调器又为工作节点的 Presto 节点，并为该节点配置 Iceberg 连接器。
此外，此 Iceberg 连接器将使用本地文件系统目录启动 Hive Metastore Server。

```yaml
services:
  presto:
    image: prestodb/presto:0.292
    ports:
      - "8080:8080"
    volumes:
      - ./iceberg.properties:/opt/presto-server/etc/catalog/iceberg.properties
```

同级文件夹包含文件 `iceberg.properties`，内容如下，

```properties
connector.name=iceberg
iceberg.catalog.type=hive
hive.metastore=file
hive.metastore.catalog.dir=file:/home/iceberg_data
```

### 创建业务相关的 schema 和表

通过第三方工具在 Presto 内创建业务相关的 schema 和表。
以 DBeaver Community 为例，若使用 Ubuntu 22.04.5，可通过 Snapcraft 快速安装，

```shell
sudo apt update && sudo apt upgrade -y
sudo snap install dbeaver-ce
snap run dbeaver-ce
```

在 DBeaver Community 内，使用 `jdbc:presto://localhost:8080/iceberg` 的 `standardJdbcUrl`，`test` 的`username` 连接至 Presto，
`password` 留空。
执行如下 SQL，

```sql
-- noinspection SqlNoDataSourceInspectionForFile
CREATE SCHEMA iceberg.demo_ds_0;
CREATE SCHEMA iceberg.demo_ds_1;
CREATE SCHEMA iceberg.demo_ds_2;
```

分别使用 `jdbc:presto://localhost:8080/iceberg/demo_ds_0` ，
`jdbc:presto://localhost:8080/iceberg/demo_ds_1` 和 `jdbc:presto://localhost:8080/iceberg/demo_ds_2` 的 `standardJdbcUrl` 连接至 Presto 来执行如下 SQL，

```sql
-- noinspection SqlNoDataSourceInspectionForFile
CREATE TABLE IF NOT EXISTS t_order (
    order_id BIGINT NOT NULL,
    order_type INTEGER,
    user_id INTEGER NOT NULL,
    address_id BIGINT NOT NULL,
    status VARCHAR(50)
);
truncate table t_order;
```

### 在业务项目创建 ShardingSphere 数据源

在业务项目引入`前提条件`涉及的依赖后，在业务项目的 classpath 上编写 ShardingSphere 数据源的配置文件`demo.yaml`，

```yaml
dataSources:
  ds_0:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.facebook.presto.jdbc.PrestoDriver
    standardJdbcUrl: jdbc:presto://localhost:8080/iceberg/demo_ds_0
    username: test
  ds_1:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.facebook.presto.jdbc.PrestoDriver
    standardJdbcUrl: jdbc:presto://localhost:8080/iceberg/demo_ds_1
    username: test
  ds_2:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.facebook.presto.jdbc.PrestoDriver
    standardJdbcUrl: jdbc:presto://localhost:8080/iceberg/demo_ds_2
    username: test
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
            statement.execute("DELETE FROM t_order WHERE user_id=1");
            statement.execute("DROP TABLE IF EXISTS t_order");
        }
    }
}
```

## 使用限制

### SQL 限制

ShardingSphere JDBC DataSource 尚不支持执行 Presto 的 `create table` 和 `truncate table` 语句。

### 事务限制

Presto 不支持 ShardingSphere 集成级别的本地事务，XA 事务或 Seata 的 AT 模式事务。
Presto 自身的事务支持存在问题，参考 https://github.com/prestodb/presto/issues/25204 。

### 连接器限制

受 https://github.com/prestodb/presto/issues/23226 影响，Presto Memory 连接器的健康检查存在已知问题，
不应在 ShardingSphere 的配置文件内连接至 Presto Memory 连接器。
