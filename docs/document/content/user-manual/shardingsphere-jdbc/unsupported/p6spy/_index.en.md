+++
title = "P6Spy"
weight = 6
+++

## Background Information

ShardingSphere provides partial support for `com.p6spy.engine.spy.P6SpyDriver` through the
`org.apache.shardingsphere:shardingsphere-infra-database-p6spy` module.

## Prerequisites

To use P6Spy for MySQL Server data nodes in ShardingSphere's configuration file, 
the possible Maven dependencies are as follows,

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-jdbc</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>p6spy</groupId>
        <artifactId>p6spy</artifactId>
        <version>3.9.1</version>
    </dependency>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>9.1.0</version>
    </dependency>
</dependencies>
```

## Configuration Example

### Start MySQL Server

Write a Docker Compose file to start MySQL Server.

```yaml
services:
   mysql:
      image: mysql:9.1.0
      environment:
         MYSQL_ROOT_PASSWORD: example
      volumes:
         - ./mysql/docker-entrypoint-initdb.d:/docker-entrypoint-initdb.d
      ports:
         - "3306:3306"
```

The `./docker-entrypoint-initdb.d` folder contains the file `init.sh`, the content is as follows,

```shell
#!/bin/bash
set -e

mysql -uroot -p"$MYSQL_ROOT_PASSWORD" <<EOSQL
CREATE DATABASE demo_ds_0;
CREATE DATABASE demo_ds_1;
CREATE DATABASE demo_ds_2;
EOSQL

for i in "demo_ds_0" "demo_ds_1" "demo_ds_2"
do
mysql -uroot -p"$MYSQL_ROOT_PASSWORD" "$i" <<'EOSQL'
CREATE TABLE IF NOT EXISTS t_order (
   order_id BIGINT NOT NULL AUTO_INCREMENT,
   order_type INT(11),
   user_id INT NOT NULL,
   address_id BIGINT NOT NULL,
   status VARCHAR(50),
   PRIMARY KEY (order_id)
);
EOSQL
done
```

### Create ShardingSphere data source in business project

After the business project introduces the dependencies involved in the prerequisites, 
write the ShardingSphere data source configuration file `demo.yaml` on the classpath of the business project.

```yaml
dataSources:
  ds_0:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.p6spy.engine.spy.P6SpyDriver
    jdbcUrl: jdbc:p6spy:mysql://localhost:3306/demo_ds_0?sslMode=REQUIRED
    username: root
    password: example
  ds_1:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.p6spy.engine.spy.P6SpyDriver
    jdbcUrl: jdbc:p6spy:mysql://localhost:3306/demo_ds_1?sslMode=REQUIRED
    username: root
    password: example
  ds_2:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.p6spy.engine.spy.P6SpyDriver
    jdbcUrl: jdbc:p6spy:mysql://localhost:3306/demo_ds_2?sslMode=REQUIRED
    username: root
    password: example
rules:
- !SHARDING
    tables:
      t_order:
        actualDataNodes:
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
            statement.execute("DELETE FROM t_order WHERE order_id=1");
        }
    }
}
```

## Usage Limitations

Currently only supports configuring P6Spy for MySQL JDBC Driver.
