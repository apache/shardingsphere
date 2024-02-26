+++
title = "Seata Transaction"
weight = 7
+++

## Background

Apache ShardingSphere provides BASE transactions that integrate the Seata implementation.
All references to Seata integration in this article refer to Seata AT mode.

## Prerequisites

Introduce Maven dependencies and exclude the outdated Maven dependencies of `org.antlr:antlr4-runtime:4.8` in `io.seata:seata-all`.

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
         <groupId>io.seata</groupId>
         <artifactId>seata-all</artifactId>
         <version>2.0.0</version>
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

## Procedure

1. Start Seata Server
2. Create the log table
3. Add the Seata configuration

## Sample

### Start Seata Server

Follow the steps in [seata-fescar-workshop](https://github.com/seata/fescar-workshop) or https://hub.docker.com/r/seataio/seata-server ,
download and start the Seata server.

### Create undo_log table

Create the `undo_log` table in each shard database instance (take MySQL as an example).
The content of SQL is subject to the corresponding database in https://github.com/apache/incubator-seata/tree/v2.0.0/script/client/at/db .

```sql
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
```

### Modify configuration

Add the `seata.conf` file to the root directory of the classpath.
The configuration file format refers to the [JavaDoc](https://github.com/apache/incubator-seata/blob/v2.0.0/config/seata-config-core/src/main/java/io/seata/config/FileConfiguration.java) of `io.seata.config.FileConfiguration`.

There are four properties in `seata.conf`,

1. `sharding.transaction.seata.at.enable`, when this value is `true`, ShardingSphere's Seata AT integration is enabled, there is a default value of `true`
2. `sharding.transaction.seata.tx.timeout`, global transaction timeout in SECONDS, there is a default value of `60`
3. `client.application.id`, apply the only primary key
4. `client.transaction.service.group`, the transaction group it belongs to, there is a default value of `default`

A fully configured `seata.conf` is as follows,

```conf
sharding.transaction.seata.at.enable = true
sharding.transaction.seata.tx.timeout = 60

client {
    application.id = example
    transaction.service.group = default_tx_group
}
```

Modify the `file.conf` and `registry.conf` files of Seata as required.

## Usage restrictions

ShardingSphere's Seata integration does not support isolation levels.

ShardingSphere's Seata integration places the obtained Seata global transaction into the thread's local variables.
And `org.apache.seata.spring.annotation.GlobalTransactionScanner` uses Dynamic Proxy to enhance the method.
This means that users should never use the `io.seata:seata-all` Java annotation for ShardingSphere's DataSource.
That is, when using ShardingSphere's Seata integration, users should avoid using the Java API of `io.seata:seata-all`.

For ShardingSphere data source, discuss 6 situations,

1. Manually obtain the `java.sql.Connection` instance created from the ShardingSphere data source,
   and manually calling the `setAutoCommit()`, `commit()` and `rollback()` methods is allowed.

2. Using the Jakarta EE 8 `javax.transaction.Transactional` annotation on the function is allowed.

3. Using Jakarta EE 9/10’s `jakarta.transaction.Transactional` annotation on functions is allowed.

4. Using Spring Framework’s `org.springframework.transaction.annotation.Transactional` annotation on functions is allowed.

5. Using the `io.seata.spring.annotation.GlobalTransactional` annotation on the function is **not allowed**.

6. Manually create `io.seata.tm.api.GlobalTransaction` instance from `io.seata.tm.api.GlobalTransactionContext`,
calling the `begin()`, `commit()` and `rollback()` methods of an `io.seata.tm.api.GlobalTransaction` instance is **not allowed**.

For Seata Server 2.0.0,
Seata Server does not pass the return value of `io.seata.core.context.RootContext.getXID()` to all connected Seata Client instances of the same **transaction group**,
Reference https://seata.apache.org/docs/user/api/ .
This requires discussing two situations,

1. In the scenario of using ShardingSphere JDBC,
   transaction scenarios across multiple microservices need to consider using `io.seata.core.context.RootContext.getXID()` in the context of the starting microservice to obtain the Seata XID and then pass it to the ending microservice through RPC,
   and call `io.seata.core.context.RootContext.bind(rpcXid)` in the business function of the endpoint microservice.

2. In the scenario of using ShardingSphere Proxy,
   Multiple microservices operate local transactions against the logical dataSource of ShardingSphere Proxy,
   this will be converted into distributed transaction operations on the server side of ShardingSphere Proxy,
   there are no additional Seata XIDs to consider.

In the actual scenario of using Spring Boot OSS,
`com.alibaba.cloud:spring-cloud-starter-alibaba-seata` and `io.seata:seata-spring-boot-starter` are often introduced transitively by other Maven dependencies.
In order to avoid transaction conflicts, users need to manually turn off Seata's auto-config class.
And set the `seata.enable-auto-data-source-proxy` property to `false` in the Spring Boot OSS configuration file. 
A possible dependency is as follows.

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
          <groupId>io.seata</groupId>
          <artifactId>seata-spring-boot-starter</artifactId>
          <version>2.0.0</version>
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

The corresponding Spring Boot OSS bootstrap class may be as follows.

```java
import io.seata.spring.boot.autoconfigure.SeataAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = SeataAutoConfiguration.class)
public class ExampleApplication {

     public static void main(String[] args) {
         SpringApplication.run(ShardingsphereSeataSpringBootTestApplication.class, args);
     }

}
```

The corresponding `application.yml` under the classpath needs to contain the following configuration.
In this case, the equivalent configuration of Seata's `registry.conf` defined in `application.yaml` of Spring Boot OSS may not be valid.
This depends on the Seata Client.
When a downstream project uses the Maven module `org.apache.shardingsphere:shardingsphere-transaction-base-seata-at`,
users are always encouraged to configure Seata Client using `registry.conf`.

```yaml
seata:
   enable-auto-data-source-proxy: false
```
