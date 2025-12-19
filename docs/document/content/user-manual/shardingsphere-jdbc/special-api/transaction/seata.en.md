+++
title = "Seata Transaction"
weight = 7
+++

## Background

Apache ShardingSphere provides BASE transactions that integrate the Seata implementation.
All references to Seata integration in this article refer to Seata AT mode.

## Prerequisites

ShardingSphere's Seata integration is only available in `apache/incubator-seata:v2.5.0` or higher.
For Seata Client corresponding to the `org.apache.seata:seata-all` Maven module, this limitation applies to both HotSpot VM and GraalVM Native Image.
Introduce Maven dependencies and exclude the outdated Maven dependency of `org.antlr:antlr4-runtime:4.8` in `org.apache.seata:seata-all`.

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
         <artifactId>seata-all</artifactId>
         <version>2.5.0</version>
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

When using ShardingSphere's Seata integration module, 
the database instance connected to ShardingSphere should implement both ShardingSphere's dialect parsing support and Seata AT mode's dialect parsing support.
This type of database includes but is not limited to `mysql`, `gvenzl/oracle-free`, `gvenzl/oracle-xe`, `postgres`, 
`mcr.microsoft.com/mssql/server` and other Docker Images.

### `undo_log` table restrictions

In each real database instance involved in ShardingSphere, an `undo_log` table needs to be created.
The SQL content of each database is based on the corresponding database in https://github.com/apache/incubator-seata/tree/v2.5.0/script/client/at/db .

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
For the configuration file format, refer to the [JavaDoc](https://github.com/apache/incubator-seata/blob/v2.5.0/config/seata-config-core/src/main/java/org/apache/seata/config/FileConfiguration.java) of `org.apache.seata.config.FileConfiguration`.

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
      image: apache/seata-server:2.5.0
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

After the business project introduces the dependencies involved in the prerequisites, 
add the Maven dependency of MySQL JDBC Driver.

```xml
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
      standardJdbcUrl: jdbc:mysql://localhost:3306/demo_ds_0?sslMode=REQUIRED
      username: root
      password: example
   ds_1:
      dataSourceClassName: com.zaxxer.hikari.HikariDataSource
      driverClassName: com.mysql.cj.jdbc.Driver
      standardJdbcUrl: jdbc:mysql://localhost:3306/demo_ds_1?sslMode=REQUIRED
      username: root
      password: example
   ds_2:
      dataSourceClassName: com.zaxxer.hikari.HikariDataSource
      driverClassName: com.mysql.cj.jdbc.Driver
      standardJdbcUrl: jdbc:mysql://localhost:3306/demo_ds_2?sslMode=REQUIRED
      username: root
      password: example
rules:
   - !SHARDING
      tables:
         t_order:
            actualDataNodes: ds_$->{0..2}.t_order
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
transaction:
   defaultType: BASE
   providerType: Seata
```

### Enjoy integration

You can start enjoying integration on ShardingSphere’s data source.

```java
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
@SuppressWarnings({"SqlNoDataSourceInspection", "AssertWithSideEffects"})
public class ExampleTest {
    void test() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:shardingsphere:classpath:demo.yaml");
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        try (HikariDataSource dataSource = new HikariDataSource(config)) {
            try (Connection conn = dataSource.getConnection()) {
                try {
                    conn.setAutoCommit(false);
                    conn.createStatement().executeUpdate("INSERT INTO t_order (user_id, order_type, address_id, status) VALUES (2024, 1, 2024, 'INSERT_TEST')");
                    conn.createStatement().executeUpdate("INSERT INTO t_order_does_not_exist (test_id_does_not_exist) VALUES (2024)");
                    conn.commit();
                } catch (final SQLException ignored) {
                    conn.rollback();
                } finally {
                    conn.setAutoCommit(true);
                }
            }
            try (Connection conn = dataSource.getConnection()) {
                assert !conn.createStatement().executeQuery("SELECT * FROM t_order_item WHERE user_id = 2024").next();
            }
        }
    }
}
```

## Usage restrictions

ShardingSphere's Seata integration does not support isolation levels.

ShardingSphere's Seata integration places the obtained Seata global transaction into the thread's local variables.
And `org.apache.seata.spring.annotation.GlobalTransactionScanner` uses Dynamic Proxy to enhance the method.
This means that when using ShardingSphere's Seata integration, users should avoid using the Java API of `org.apache.seata:seata-all`, 
unless the user is mixing ShardingSphere's Seata integration with the TCC mode feature of Seata Client.

For ShardingSphere data source, 7 situations are discussed.

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
          <version>2.5.0</version>
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

### Mixed use with Seata TCC mode features

For the case of setting up ShardingSphere's Seata integration,
In business functions unrelated to ShardingSphere JDBC DataSource, if you need to use Seata Client's Seata TCC mode-related features in business functions,
you can instantiate a non-proxy ordinary TCC interface implementation class, and then use `org.apache.integration.tx.api.util.ProxyUtil` to create a proxy TCC interface class,
and call the functions corresponding to the three stages of the TCC interface implementation class `Try`, `Confirm`, and `Cancel`.

For the `org.apache.seata.spring.annotation.GlobalTransactional` annotation introduced by the Seata TCC mode or the business functions involved in the Seata TCC mode that need to interact with the database instance, 
ShardingSphere JDBC DataSource should not be used in the business functions marked by this annotation. Instead, 
a `javax.sql.DataSource` instance should be created manually or obtained from a custom Spring Bean.

### Transactional propagation across service calls

Transactional propagationn in cross-service call scenarios is not as out-of-the-box as transaction operations within a single microservice.
For Seata Server, transactional propagation in cross-service call scenarios requires passing XID to the service provider through service calls and binding it to `org.apache.seata.core.context.RootContext`.
Refer to https://seata.apache.org/docs/user/api/ . This requires discussing two situations,

1. In the scenario of using ShardingSphere JDBC, 
transaction scenarios across multiple microservices need to consider using `org.apache.seata.core.context.RootContext.getXID()` to obtain Seata XID in the context of the starting microservice,
and passing it to the end microservice through HTTP or RPC, and processing it in the Filter or Spring WebMVC HandlerInterceptor of the end microservice.
Spring WebMVC HandlerInterceptor is only applicable to Spring Boot microservices and is invalid for Quarkus, Micronaut Framework and Helidon.

2. In the scenario of using ShardingSphere Proxy, multiple microservices operate local transactions against the logical data source of ShardingSphere Proxy.
This will be converted into distributed transaction operations on the server side of ShardingSphere Proxy, without considering additional Seata XID.

Introduce a simple scenario to continue discussing the transactional propagation across service calls in the scenario of using ShardingSphere JDBC.

1. MySQL database instance `a-mysql`, all databases have created `UNDO_LOG` table and business table.
2. MySQL database instance `b-mysql`, all databases have created `UNDO_LOG` table and business table.
3. Seata Server instance `a-seata-server` using `file` as configuration center and registration center.
4. Microservice instance `a-service`. This microservice creates a ShardingSphere JDBC DataSource that only configures the database instance `a-mysql`.
This ShardingSphere JDBC DataSource configuration uses the Seata AT integration connected to the Seata Server instance `a-seata-server`, 
whose Seata Application Id is `service-a`, whose Seata transaction group is `default_tx_group`, 
and the Seata Transaction Coordinator cluster group pointed to by its `Virtual Group Mapping` is `default`.
This microservice instance `a-service` exposes a single Restful API GET endpoint as `/hello`,
and the business function `aMethod` of this Restful API endpoint uses a common local transaction annotation.
If this microservice is based on Spring Boot 2,

```java
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {
   @Transactional
   @GetMapping("/hello")
   public String aMethod() {
      // ... Perform an UPDATE operation on the database instance `a-mysql`
      return "Hello World!";
   }
}
```

5. Microservice instance `b-service`. This microservice creates a ShardingSphere JDBC DataSource that only configures the database instance `b-mysql`.
This ShardingSphere JDBC DataSource configuration uses the Seata AT integration connected to the Seata Server instance `a-seata-server`, 
whose Seata Application Id is `service-b`, whose Seata transaction group is `default_tx_group`, 
and whose `Virtual Group Mapping` points to the Seata Transaction Coordinator cluster group as `default`.
The business function `bMethod` of this microservice instance `b-service` uses a normal local transaction annotation, 
and calls the `/hello` Restful API endpoint of the microservice instance `a-service` through the HTTP Client in `bMethod`.
If this microservice is based on Spring Boot 2,

```java
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class DemoService {
   @Transactional
   public void bMethod() {
      RestTemplate restTemplate = new RestTemplateBuilder().build();
      restTemplate.getForEntity("http://a-service/hello", String.class);
      // ... Perform an UPDATE operation on the database instance `b-mysql`
   }
}
```

For this simple scenario, there is a single Seata Server Cluster, which contains a single `Virtual Group` as `default`. 
This `Virtual Group` contains a single Seata Server instance as `a-seata-server`.

Discuss transaction propagation for single service calls. When the business function `aMethod` of the microservice instance `a-service` throws an exception, 
the changes to the MySQL database instance `a-mysql` in the business function will be rolled back normally.

Discuss transaction propagation for cross-service calls. When the business function `bMethod` of the microservice instance `b-service` throws an exception, 
the changes to the MySQL database instance `b-mysql` in the business function will be rolled back normally,
and the `org.apache.seata.core.context.RootContext` of the microservice instance `a-service` is not bound to the Seata XID of the business function `bMethod` of the microservice instance `b-service`,
so the changes to the MySQL database instance `a-mysql` in the business function will not be rolled back.

In order to achieve that when the business function `bMethod` of the microservice instance `b-service` throws an exception, 
the changes to the MySQL database instances `a-mysql` and `b-mysql` in the business function are rolled back normally,
discuss the common processing solutions in different scenarios.

1. The microservice instances `a-service` and `b-service` are both Spring Boot 2 microservices based on Jakarta EE 8.
Users can use `org.springframework.web.client.RestTemplate` in the business function `bMethod` of the microservice instance `b-service` to pass the XID to the microservice instance `a-service` through the service call.
The possible transformation logic is as follows.

```java
import org.apache.seata.core.context.RootContext;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class DemoService {
    @Transactional
    public void bMethod() {
        RestTemplate restTemplate = new RestTemplateBuilder().additionalInterceptors((request, body, execution) -> {
                    String xid = RootContext.getXID();
                    if (null != xid) {
                        request.getHeaders().add(RootContext.KEY_XID, xid);
                    }
                    return execution.execute(request, body);
                })
                .build();
        restTemplate.getForEntity("http://a-service/hello", String.class);
        // ... Perform an UPDATE operation on the database instance `b-mysql`
    }
}
```

At this time, a custom `org.springframework.web.servlet.config.annotation.WebMvcConfigurer` implementation needs to be added to the microservice instance `a-service`.

```java
import org.apache.seata.integration.http.TransactionPropagationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CustomWebMvcConfigurer implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TransactionPropagationInterceptor());
    }
}
```

At this time, when the business function `bMethod` of the microservice instance `b-service` throws an exception, 
the changes to the MySQL database instances `a-mysql` and `b-mysql` in the business function are rolled back normally.

2. The microservice instances `a-service` and `b-service` are both Spring Boot 3 microservices based on Jakarta EE 9/10.
Users can use `org.springframework.web.client.RestClient` in the business function `bMethod` of the microservice instance `b-service` to pass the XID to the microservice instance `a-service` through a service call.
The possible transformation logic is as follows.

```java
import org.apache.seata.core.context.RootContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
public class DemoService {
    @Transactional
    public void bMethod() {
        RestClient restClient = RestClient.builder().requestInterceptor((request, body, execution) -> {
                    String xid = RootContext.getXID();
                    if (null != xid) {
                        request.getHeaders().add(RootContext.KEY_XID, xid);
                    }
                    return execution.execute(request, body);
                })
                .build();
        restClient.get().uri("http://a-service/hello").retrieve().body(String.class);
        // ... Perform an UPDATE operation on the database instance `b-mysql`
    }
}
```

At this time, 
a custom `org.springframework.web.servlet.config.annotation.WebMvcConfigurer` implementation needs to be added to the microservice instance `a-service`.

```java
import org.apache.seata.integration.http.JakartaTransactionPropagationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CustomWebMvcConfigurer implements WebMvcConfigurer {

   @Override
   public void addInterceptors(InterceptorRegistry registry) {
      registry.addInterceptor(new JakartaTransactionPropagationInterceptor());
   }
}
```

At this time, when the business function `bMethod` of the microservice instance `b-service` throws an exception, 
the changes to the MySQL database instances `a-mysql` and `b-mysql` in the business function are rolled back normally.

3. The microservice instances `a-service` and `b-service` are both Spring Boot microservices, 
but the API gateway middleware used blocks all HTTP requests containing the HTTP Header of `TX_XID`.
The user needs to consider changing the HTTP Header used to pass XID to the microservice instance `a-service` through service calls, 
or use the RPC framework to pass XID to the microservice instance `a-service` through service calls.
Refer to https://github.com/apache/incubator-seata/tree/v2.5.0/integration .

4. The microservice instances `a-service` and `b-service` are both microservices such as Quarkus, 
Micronaut Framework and Helidon. In this case, Spring WebMVC HandlerInterceptor cannot be used.
You can refer to the following Spring Boot 3 custom WebMvcConfigurer implementation to implement Filter.

```java
import org.apache.seata.common.util.StringUtils;
import org.apache.seata.core.context.RootContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class CustomWebMvcConfigurer implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
                String rpcXid = request.getHeader(RootContext.KEY_XID);
                String xid = RootContext.getXID();
                if (StringUtils.isBlank(xid) && StringUtils.isNotBlank(rpcXid)) {
                    RootContext.bind(rpcXid);
                }
                return true;
            }
            @Override
            public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
                if (RootContext.inGlobalTransaction()) {
                    String rpcXid = request.getHeader(RootContext.KEY_XID);
                    String xid = RootContext.getXID();
                    if (StringUtils.isNotBlank(xid)) {
                        String unbindXid = RootContext.unbind();
                        if (!StringUtils.equalsIgnoreCase(rpcXid, unbindXid)) {
                            if (StringUtils.isNotBlank(unbindXid)) {
                                RootContext.bind(unbindXid);
                            }
                        }
                    }
                }
            }
        });
    }
}
```

5. Both microservice instances `a-service` and `b-service` are Spring Boot microservices, but the components used are Spring WebFlux instead of Spring WebMVC.
ShardingSphere JDBC cannot handle R2DBC DataSource under the reactive programming API, only JDBC DataSource.
Avoid creating ShardingSphere JDBC DataSource in Spring Boot microservices using WebFlux components.

### Log Configuration

After starting Seata Client in a business project, you may see the following Error Log.

```shell
[ERROR] 2024-12-20 11:46:43.878 [ForkJoinPool.commonPool-worker-1] o.a.s.config.ConfigurationFactory - failed to load non-spring configuration :not found service provider for : org.apache.seata.config.ConfigurationProvider
org.apache.seata.common.loader.EnhancedServiceNotFoundException: not found service provider for : org.apache.seata.config.ConfigurationProvider
```

According to https://github.com/apache/incubator-seata/issues/6886 , throwing this exception is the expected behavior of Seata Client.
Users can configure the log of Seata Client by placing `logback.xml` in the classpath of the business project.
