+++
title = "MS SQL Server"
weight = 6
+++

## 背景信息

ShardingSphere 默认情况下不提供对 `com.microsoft.sqlserver.jdbc.SQLServerDriver` 的 `driverClassName` 的支持。
ShardingSphere 对 MS SQL Server 的支持位于可选模块中。

## 前提条件

要在 ShardingSphere 的配置文件为数据节点使用类似 `jdbc:sqlserver://localhost:1433;databaseName=demo_ds_0;encrypt=false;` 的 `jdbcUrl`，
可能的 Maven 依赖关系如下，

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-jdbc-dialect-sqlserver</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>com.microsoft.sqlserver</groupId>
        <artifactId>mssql-jdbc</artifactId>
        <version>12.10.2.jre8</version>
    </dependency>
</dependencies>
```

## 配置示例

### 启动 MS SQL Server

编写 Docker Compose 文件来启动 MS SQL Server。

```yaml
services:
  ms-sql-server:
    image: mcr.microsoft.com/mssql/server:2025-RTM-ubuntu-22.04
    environment:
      ACCEPT_EULA: Y
      MSSQL_SA_PASSWORD: A_Str0ng_Required_Password
    ports:
      - "1433:1433"
```

### 创建业务库

通过第三方工具在 MS SQL Server 内创建业务库。
以 DBeaver Community 为例，若使用 Ubuntu 24.04，可通过 Snapcraft 快速安装，

```shell
sudo apt update && sudo apt upgrade -y
sudo snap install dbeaver-ce --classic
snap run dbeaver-ce
```

在 DBeaver Community 内，使用 `jdbc:sqlserver://localhost:1433;encrypt=false;` 的 `jdbcUrl`，`sa` 的`username`，`A_Str0ng_Required_Password` 的 `password` 连接至 MS SQL Server。
执行如下 SQL，

```sql
-- noinspection SqlNoDataSourceInspectionForFile
CREATE DATABASE demo_ds_0;
CREATE DATABASE demo_ds_1;
CREATE DATABASE demo_ds_2;
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
    driverClassName: com.microsoft.sqlserver.jdbc.SQLServerDriver
    jdbcUrl: jdbc:sqlserver://localhost:1433;databaseName=demo_ds_0;encrypt=false;
    username: sa
    password: A_Str0ng_Required_Password
  ds_1:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.microsoft.sqlserver.jdbc.SQLServerDriver
    jdbcUrl: jdbc:sqlserver://localhost:1433;databaseName=demo_ds_1;encrypt=false;
    username: sa
    password: A_Str0ng_Required_Password
  ds_2:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.microsoft.sqlserver.jdbc.SQLServerDriver
    jdbcUrl: jdbc:sqlserver://localhost:1433;databaseName=demo_ds_2;encrypt=false;
    username: sa
    password: A_Str0ng_Required_Password
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
            statement.execute("CREATE TABLE [t_order] (order_id bigint NOT NULL,order_type int,user_id int NOT NULL,address_id bigint NOT NULL,status varchar(50),PRIMARY KEY (order_id))");
            statement.execute("TRUNCATE TABLE t_order");
            statement.execute("INSERT INTO t_order (user_id, order_type, address_id, status) VALUES (1, 1, 1, 'INSERT_TEST')");
            statement.executeQuery("SELECT * FROM t_order");
            statement.execute("DELETE FROM t_order WHERE user_id=1");
            statement.execute("DROP TABLE t_order");
        }
    }
}
```

## 使用限制

### SQL 限制

ShardingSphere JDBC DataSource 支持执行 MS SQL Server 的 `DROP TABLE` 语句，
但尚不支持执行 MS SQL Server 的 `DROP TABLE IF EXISTS` 语句。
