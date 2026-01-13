+++
title = "Presto"
weight = 6
+++

## Background Information

ShardingSphere does not provide support for `driverClassName` of `com.facebook.presto.jdbc.PrestoDriver` by default.
ShardingSphere's support for Presto JDBC Driver is in an optional module.

## Prerequisites

To use a `standardJdbcUrl` like `jdbc:presto://localhost:8080/iceberg/demo_ds_0` for the data node in the ShardingSphere configuration file,
Possible Maven dependencies are as follows,

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-jdbc-dialect-presto</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>com.facebook.presto</groupId>
        <artifactId>presto-jdbc</artifactId>
        <version>0.296</version>
    </dependency>
</dependencies>
```

## Configuration Example

### Start Presto

Write a Docker Compose file to start Presto. 
This will start a Presto node that is both a coordinator and a worker node, and configure the Iceberg Connector for the node.
In addition, this Iceberg Connector will start a Hive Metastore Server using a local file system directory.

```yaml
services:
  presto:
    image: prestodb/presto:0.296
    ports:
      - "8080:8080"
    volumes:
      - ./iceberg.properties:/opt/presto-server/etc/catalog/iceberg.properties
```

The same folder contains the file `iceberg.properties`, the contents are as follows,

```properties
connector.name=iceberg
iceberg.catalog.type=hive
hive.metastore=file
hive.metastore.catalog.dir=file:/home/iceberg_data
```

### Create business-related schemas and tables

Use third-party tools to create business-related schemas and tables in Presto.
Taking DBeaver Community as an example, if you use Ubuntu 24.04, you can quickly install it through Snapcraft.

```shell
sudo apt update && sudo apt upgrade -y
sudo snap install dbeaver-ce --classic
snap run dbeaver-ce
```

In DBeaver Community, use `standardJdbcUrl` of `jdbc:presto://localhost:8080/iceberg`, `username` of `test` to connect to Presto, and leave `password` blank.
Execute the following SQL,

```sql
-- noinspection SqlNoDataSourceInspectionForFile
CREATE SCHEMA iceberg.demo_ds_0;
CREATE SCHEMA iceberg.demo_ds_1;
CREATE SCHEMA iceberg.demo_ds_2;
```

Use the `standardJdbcUrl` of `jdbc:presto://localhost:8080/iceberg/demo_ds_0`, 
`jdbc:presto://localhost:8080/iceberg/demo_ds_1` and `jdbc:presto://localhost:8080/iceberg/demo_ds_2` to connect to Presto and execute the following SQL,

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

### Create ShardingSphere data source in business project

After including the dependencies related to the `Prerequisites` in the business project, add the following additional dependencies,

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


After including the dependencies related to the `Prerequisites` in the business project, add the following additional dependencies,

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

Write the ShardingSphere data source configuration file `demo.yaml` on the classpath of the business project.

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
            statement.execute("DELETE FROM t_order WHERE user_id=1");
            statement.execute("DROP TABLE IF EXISTS t_order");
        }
    }
}
```

## Usage Limitations

### SQL Limitations

ShardingSphere JDBC DataSource does not yet support the execution of Presto's `create table` and `truncate table` statements.

### Transaction Limitations

Presto does not support local transactions, XA transactions, or Seata's AT mode transactions at the ShardingSphere integration level.
There are bugs with Presto's own transaction support, see https://github.com/prestodb/presto/issues/25204 .

### Connector Limitations

Affected by https://github.com/prestodb/presto/issues/23226 , there are known issues with the health check of the Presto Memory connector,
developers should not connect to the Presto Memory connector in the ShardingSphere configuration file.
