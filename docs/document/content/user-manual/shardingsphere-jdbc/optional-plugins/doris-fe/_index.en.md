+++
title = "Doris FE"
weight = 6
+++

## Background Information

ShardingSphere does not provide support for `driverClassName` of `com.mysql.cj.jdbc.Driver` by default.
ShardingSphere's support for Doris FE is located in an optional module.

## Prerequisites

To use a `jdbcUrl` like `jdbc:mysql://localhost:9030/demo_ds_0` for data nodes in the ShardingSphere configuration file, the possible Maven dependencies are as follows:

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

## Configuration Example

### Start Doris FE and Doris BE

Write a Docker Compose file to start Doris FE and Doris BE.

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

### Create business-related databases and tables

Create business-related databases and tables within Doris FE using third-party tools.
For example, DBeaver Community can be quickly installed via Snapcraft on Ubuntu 24.04.

```shell
sudo apt update && sudo apt upgrade -y
sudo snap install dbeaver-ce --classic
snap run dbeaver-ce
```

Within the DBeaver Community, use `jdbcUrl` (`jdbc:mysql://localhost:9030/`) and `username` (`root`) to connect to Doris FE, leaving the `password` blank.

Execute the following SQL:

```sql
-- noinspection SqlNoDataSourceInspectionForFile
CREATE DATABASE demo_ds_0;
CREATE DATABASE demo_ds_1;
CREATE DATABASE demo_ds_2;
```

Connect to Doris FE using `jdbcUrl` from `jdbc:mysql://localhost:9030/demo_ds_0`, `jdbc:mysql://localhost:9030/demo_ds_1`, and `jdbc:mysql://localhost:9030/demo_ds_2` respectively to execute the following SQL:

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

### Creating ShardingSphere data source in business project

After including the dependencies related to `Prerequisites` in the business project, add the following dependencies:

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
            statement.execute("TRUNCATE TABLE t_order");
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

ShardingSphere JDBC DataSource does not yet support executing `create table` statements in Doris FE.

### Transaction Limitations

Doris FE does not support ShardingSphere integration-level native transactions, XA transactions, or Seata AT mode transactions.

Doris FE itself has issues with transaction support; see https://doris.apache.org/docs/4.x/data-operate/transaction#failed-statements-within-a-transaction .

For Doris FE, when a statement in a transaction fails, the operation is automatically rolled back. However, other statements in the transaction that execute successfully are not automatically rolled back.

### Experimental Module Limitations

ShardingSphere has an optional module, `org.apache.shardingsphere:shardingsphere-jdbc-dialect-doris`, to support Doris FE's specific database dialect. Possible Maven dependencies are as follows:

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

`org.apache.shardingsphere:shardingsphere-jdbc-dialect-doris` does not fully support the Doris database dialect. For unsupported SQL syntax, refer to the open issue at https://github.com/apache/shardingsphere/issues?q=is%3Aissue%20state%3Aopen%20label%3A%22db%3A%20Doris%22%20label%3A%22in%3A%20SQL%20parse%22 .

Due to the issue mentioned in https://github.com/apache/shardingsphere/issues/36081 , if both `org.apache.shardingsphere:shardingsphere-jdbc-dialect-doris` and `org.apache.shardingsphere:shardingsphere-jdbc-dialect-mysql` are included in the same Maven module, all SQL executed towards Doris FE will be parsed using the MySQL database dialect. This will cause Doris-specific database dialect syntax to be unusable in ShardingSphere's logical databases.
