+++
title = "MS SQL Server"
weight = 6
+++

## Background Information

ShardingSphere does not provide support for `driverClassName` of `com.microsoft.sqlserver.jdbc.SQLServerDriver` by default.

ShardingSphere support for MS SQL Server is located in an optional module.

## Prerequisites

To use a `jdbcUrl` like `jdbc:sqlserver://localhost:1433;databaseName=demo_ds_0;encrypt=false;` for data nodes in the ShardingSphere configuration file, the following Maven dependencies are required:

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

## Configuration Example

### Starting MS SQL Server

Write a Docker Compose file to start MS SQL Server.

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

### Creating Business Database

Create business database within MS SQL Server using a third-party tool.
Taking DBeaver Community as an example, if using Ubuntu 24.04, it can be quickly installed via Snapcraft:

```shell
sudo apt update && sudo apt upgrade -y
sudo snap install dbeaver-ce --classic
snap run dbeaver-ce
```

Within DBeaver Community, connect to MS SQL Server using `jdbc:sqlserver://localhost:1433;encrypt=false;`'s `jdbcUrl`, `sa`'s `username`, and `A_Str0ng_Required_Password`'s `password`.
Execute the following SQL:

```sql
-- noinspection SqlNoDataSourceInspectionForFile
CREATE DATABASE demo_ds_0;
CREATE DATABASE demo_ds_1;
CREATE DATABASE demo_ds_2;
```

### Creating ShardingSphere Data Source in a Business Project

After including the dependencies involved in the `Prerequisites` section in the business project, additionally include the following dependencies:

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

Write the ShardingSphere data source configuration file `demo.yaml` on the classpath of your business project.

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

### Enjoy Integration

Create ShardingSphere data source to enjoy integration.

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

## Usage Restrictions

### SQL Restrictions

ShardingSphere JDBC DataSource supports executing the MS SQL Server `DROP TABLE` statement,
but does not yet support executing the MS SQL Server `DROP TABLE IF EXISTS` statement.
