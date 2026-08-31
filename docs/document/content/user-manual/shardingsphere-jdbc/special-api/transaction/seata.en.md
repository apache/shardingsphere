+++
title = "Seata Transaction"
weight = 7
+++

## Background

Apache ShardingSphere provides BASE transactions that integrate the Seata implementation.
All references to Seata integration in this article refer to Seata AT mode.

## Prerequisites

This document targets Apache Seata 2.6.0.
Use `org.apache.seata:seata-all:2.6.0` as the Seata Client dependency for both HotSpot VM and GraalVM Native Image.
Introduce Maven dependencies and exclude the outdated Maven dependency of `org.antlr:antlr4-runtime:4.8` in `org.apache.seata:seata-all`.

```xml
<project>
    <dependencies>
      <dependency>
         <groupId>org.apache.shardingsphere</groupId>
         <artifactId>shardingsphere-transaction-base-seata-at</artifactId>
         <version>${shardingsphere.version}</version>
      </dependency>
      <dependency>
         <groupId>org.apache.seata</groupId>
         <artifactId>seata-all</artifactId>
         <version>2.6.0</version>
         <exclusions>
            <exclusion>
               <groupId>org.antlr</groupId>
               <artifactId>antlr4-runtime</artifactId>
            </exclusion>
         </exclusions>
      </dependency>
    </dependencies>
</project>
```

The database must be supported by both ShardingSphere and Seata AT mode.

### `undo_log` table restrictions

In each real database instance involved in ShardingSphere, an `undo_log` table needs to be created.
The SQL content of each database is based on the corresponding database in https://github.com/apache/incubator-seata/tree/v2.6.0/script/client/at/db .

### Related configuration

Write the following content in the YAML configuration file of ShardingSphere of your own project, 
refer to [Distributed Transaction](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/transaction).
If Java API is used when initializing ShardingSphere JDBC DataSource, 
refer to [Distributed Transaction](/en/user-manual/shardingsphere-jdbc/java-api/rules/transaction).

```yaml
transaction:
   defaultType: BASE
   providerType: Seata
```

Add the `seata.conf` file to the root directory of the classpath.
For the configuration file format, refer to the [JavaDoc](https://github.com/apache/incubator-seata/blob/v2.6.0/config/seata-config-core/src/main/java/org/apache/seata/config/FileConfiguration.java) of `org.apache.seata.config.FileConfiguration`.

`seata.conf` has four properties,

1. `shardingsphere.transaction.seata.at.enable`, when this value is `true`, enable ShardingSphere's Seata AT integration.
The default value is `true`

2. `shardingsphere.transaction.seata.tx.timeout`, global transaction timeout (seconds). The default value is `60`

3. `client.application.id`, application unique primary key, 
used to set `applicationId` of Seata Transaction Manager Client and Seata Resource Manager Client

4. `client.transaction.service.group`, transaction group, 
used to set `transactionServiceGroup` of Seata Transaction Manager Client and Seata Resource Manager Client. The default value is `default`

A fully configured `seata.conf` is as follows,

```conf
shardingsphere.transaction.seata.at.enable = true
shardingsphere.transaction.seata.tx.timeout = 60

client {
    application.id = example
    transaction.service.group = default_tx_group
}
```

A minimally configured `seata.conf` is as follows.
In `seata.conf` managed by ShardingSphere, the default value of `client.transaction.service.group` is `default` for historical reasons.
Assuming that `registry.type` and `config.type` are both `file` in `registry.conf` of Seata Server and Seata Client used by the user,
then for `registry.file.name` of `registry.conf`, 
the transaction group name in the `.conf` file configured by `config.file.name` is `default_tx_group` in `apache/incubator-seata:v1.5.1` and later, 
and `my_test_tx_group` before `apache/incubator-seata:v1.5.1`.

```conf
client.application.id = example
```

Modify Seata's `registry.conf` file according to the actual scenario.

## Operation steps

1. Start Seata Server
2. Create `undo_log` table
3. Add Seata configuration

## Configuration Example

### Start Seata Server and MySQL Server

Write Docker Compose file to start Seata Server and MySQL Server.

```yaml
services:
   apache-seata-server:
      image: apache/seata-server:2.6.0
      ports:
         - "8091:8091"
   mysql:
      image: mysql:9.4.0
      environment:
         MYSQL_ROOT_PASSWORD: example
      volumes:
         - ./docker-entrypoint-initdb.d:/docker-entrypoint-initdb.d
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
CREATE TABLE IF NOT EXISTS `undo_log`
(
    `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
    `log_status`    INT(11)      NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`   DATETIME(6)  NOT NULL COMMENT 'create datetime',
    `log_modified`  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT ='AT transaction mode undo table';
ALTER TABLE `undo_log` ADD INDEX `ix_log_created` (`log_created`);

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

### Create `seata.conf` in the classpath of the business project

Create `seata.conf` in the classpath of the business project, with the following content,

```
service {
    default.grouplist = "127.0.0.1:8091"
    vgroupMapping.default_tx_group = "default"
}
```

### Create `file.conf` in the classpath of the business project

Create `file.conf` in the classpath of the business project, with the following content,

```
client {
    application.id = test
    transaction.service.group = default_tx_group
}
```

### Create `registry.conf` in the classpath of the business project

Create a `registry.conf` in the classpath of the business project with the following content:

```
registry {
  type = "file"
  file {
    name = "file.conf"
  }
}
config {
  type = "file"
  file {
    name = "file.conf"
  }
}
```

### Add JDBC Driver to the business project and create ShardingSphere configuration file

After including the dependencies listed in the `Prerequisites` section in the business project, add the following additional dependencies,

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
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>9.4.0</version>
</dependency>
```

Write the ShardingSphere data source configuration file `demo.yaml` on the classpath of the business project.

```yaml
dataSources:
   ds_0:
      dataSourceClassName: com.zaxxer.hikari.HikariDataSource
      driverClassName: com.mysql.cj.jdbc.Driver
      jdbcUrl: jdbc:mysql://localhost:3306/demo_ds_0?sslMode=REQUIRED
      username: root
      password: example
   ds_1:
      dataSourceClassName: com.zaxxer.hikari.HikariDataSource
      driverClassName: com.mysql.cj.jdbc.Driver
      jdbcUrl: jdbc:mysql://localhost:3306/demo_ds_1?sslMode=REQUIRED
      username: root
      password: example
   ds_2:
      dataSourceClassName: com.zaxxer.hikari.HikariDataSource
      driverClassName: com.mysql.cj.jdbc.Driver
      jdbcUrl: jdbc:mysql://localhost:3306/demo_ds_2?sslMode=REQUIRED
      username: root
      password: example
rules:
   - !SHARDING
      tables:
         t_order:
            actualDataNodes: ds_$->{0..2}.t_order
      defaultDatabaseStrategy:
         standard:
            shardingColumn: user_id
            shardingAlgorithmName: inline
      keyGenerateStrategies:
         t_order_order_id:
            keyGenerateType: column
            keyGeneratorName: snowflake
            logicTable: t_order
            keyGenerateColumn: order_id
      shardingAlgorithms:
         inline:
            type: INLINE
            props:
               algorithm-expression: ds_${user_id % 2}
      keyGenerators:
         snowflake:
            type: SNOWFLAKE
transaction:
   defaultType: BASE
   providerType: Seata
```

### Verify transaction rollback

The following example verifies that an unsuccessful transaction is rolled back on the ShardingSphere data source.

```java
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@SuppressWarnings("SqlNoDataSourceInspection")
public class ExampleTest {
    void test() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:shardingsphere:classpath:demo.yaml");
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        try (HikariDataSource dataSource = new HikariDataSource(config)) {
            try (Connection conn = dataSource.getConnection(); Statement statement = conn.createStatement()) {
                conn.setAutoCommit(false);
                statement.executeUpdate("INSERT INTO t_order (user_id, order_type, address_id, status) VALUES (2024, 1, 2024, 'INSERT_TEST')");
                try {
                    statement.executeUpdate("INSERT INTO t_order_does_not_exist (test_id_does_not_exist) VALUES (2024)");
                    conn.commit();
                } catch (final SQLException ignored) {
                    conn.rollback();
                } finally {
                    conn.setAutoCommit(true);
                }
            }
            try (Connection conn = dataSource.getConnection(); Statement statement = conn.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT * FROM t_order WHERE user_id = 2024")) {
                if (resultSet.next()) {
                    throw new IllegalStateException("Transaction rollback failed.");
                }
            }
        }
    }
}
```

## Usage restrictions

ShardingSphere's Seata integration does not support isolation levels.

ShardingSphere manages the Seata global transaction for its JDBC data source.
Do not use another global transaction entry point to manage transactions on the same data source.

The following rules apply to ShardingSphere data sources.

1. Manually obtain the `java.sql.Connection` instance created from the ShardingSphere data source and manually call the `setAutoCommit()`, `commit()` and `rollback()` methods.
This is allowed.

2. Use the `javax.transaction.Transactional` annotation of Jakarta EE 8 on the function. This is allowed.

3. Use the `jakarta.transaction.Transactional` annotation of Jakarta EE 9/10 on the function. This is allowed.

4. Use the `org.springframework.transaction.annotation.Transactional` annotation of Spring Framework on the function.
This is allowed.

5. Manually obtain an `org.springframework.transaction.support.TransactionTemplate` instance created from an `org.springframework.transaction.PlatformTransactionManager` instance,
   and use `org.springframework.transaction.support.TransactionTemplate#execute(org.springframework.transaction.support.TransactionCallback)`,
   which is allowed.

6. Use the `org.apache.seata.spring.annotation.GlobalTransactional` annotation on a function, which is **not allowed**.

7. Manually create an `org.apache.seata.tm.api.GlobalTransaction` instance from an `org.apache.seata.tm.api.GlobalTransactionContext`,
   and call the `begin()`, `commit()`, and `rollback()` methods of the `org.apache.seata.tm.api.GlobalTransaction` instance, 
   which is **not allowed**.

In actual scenarios where Spring Boot is used, 
`com.alibaba.cloud:spring-cloud-starter-alibaba-seata` and `org.apache.seata:seata-spring-boot-starter` are often transitively imported by other Maven dependencies.
To avoid transaction conflicts, users need to set the property `seata.enable-auto-data-source-proxy` to `false` in the Spring Boot configuration file. 
A possible dependency relationship is as follows.

```xml
<project>
     <dependencies>
       <dependency>
          <groupId>org.apache.shardingsphere</groupId>
          <artifactId>shardingsphere-jdbc</artifactId>
          <version>${shardingsphere.version}</version>
       </dependency>
       <dependency>
          <groupId>org.apache.shardingsphere</groupId>
          <artifactId>shardingsphere-transaction-base-seata-at</artifactId>
          <version>${shardingsphere.version}</version>
       </dependency>
       <dependency>
          <groupId>org.apache.seata</groupId>
          <artifactId>seata-spring-boot-starter</artifactId>
          <version>2.6.0</version>
          <exclusions>
             <exclusion>
                <groupId>org.antlr</groupId>
                <artifactId>antlr4-runtime</artifactId>
             </exclusion>
          </exclusions>
       </dependency>
     </dependencies>
</project>
```

The corresponding `application.yml` under classpath needs to contain the following configuration.
In this case, the equivalent configuration of Seata's `registry.conf` defined in Spring Boot's `application.yaml` is still valid.
When downstream projects use the Maven module of `org.apache.shardingsphere:shardingsphere-transaction-base-seata-at`, 
it is always encouraged to use `registry.conf` to configure Seata Client.

```yaml
seata:
   enable-auto-data-source-proxy: false
```

### Mixing with other Seata transaction modes

ShardingSphere's Seata integration supports AT mode only.

If an application uses Seata TCC, use a separate data source that is not managed by ShardingSphere JDBC and follow the Apache Seata documentation for TCC configuration and APIs.

### Transaction propagation across service calls

ShardingSphere JDBC's Seata AT integration makes database operations on the current ShardingSphere data source participate in a Seata global transaction.
It does not propagate the Seata XID between services.

When a transaction crosses an HTTP, RPC, messaging, or other service boundary, the application framework and Seata Client are responsible for propagating, binding, and clearing the XID.
Follow the Apache Seata documentation for the Seata version and communication framework in use.

ShardingSphere does not prescribe transport headers, framework filters or interceptors, or manual XID context operations.
