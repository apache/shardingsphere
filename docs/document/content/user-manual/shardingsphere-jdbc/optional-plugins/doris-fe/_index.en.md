+++
title = "Doris FE"
weight = 6
+++

## Background Information

ShardingSphere's SQL dialect support for Doris FE is in the optional module.

## Prerequisites

When ShardingSphere's configuration file is connected to Doris FE via MySQL JDBC Driver, 
the SQL dialect module of Doris FE should be used for the data node of Doris FE.
The possible Maven dependencies are as follows,

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

## Configuration example

### Start Doris FE and Doris BE

Write a Docker Compose file to start Doris FE and Doris BE.

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

### Create a business library

Use third-party tools to create business databases in Doris FE.
Taking DBeaver Community as an example, if you use Ubuntu 22.04.4, you can quickly install it through Snapcraft.

```shell
sudo apt update && sudo apt upgrade -y
sudo snap install dbeaver-ce
snap run dbeaver-ce
```

In DBeaver Community, use `jdbcUrl` of `jdbc:mysql://localhost:9030/`, `username` of `root` to connect to Doris FE, 
and leave `password` blank. Execute the following SQL,

```sql
-- noinspection SqlNoDataSourceInspectionForFile
CREATE DATABASE demo_ds_0;
CREATE DATABASE demo_ds_1;
CREATE DATABASE demo_ds_2;
```

### Create ShardingSphere data source in business project

After the business project introduces the dependencies involved in `Prerequisites`, 
write the ShardingSphere data source configuration file `demo.yaml` on the classpath of the business project.

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

### Enjoy integration

Create a ShardingSphere data source to enjoy integration,

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

## Usage Limitations

### SQL Limitations

ShardingSphere JDBC DataSource does not fully support the execution of Doris FE's `create table` statement.
This means that ShardingSphere JDBC DataSource can execute statements like the following,

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

But ShardingSphere JDBC DataSource cannot execute statements like the following:

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

### Transaction Limitations

Doris FE does not support ShardingSphere integration-level local transactions, XA transactions, or Seata's AT mode transactions.

Doris FE's own transaction support is not complete. 
When there is a failed SQL statement in a single transaction unit, 
the successfully executed SQL statements in this transaction unit will not be rolled back.
Refer to https://doris.apache.org/docs/3.0/data-operate/transaction#failed-statements-within-a-transaction .
