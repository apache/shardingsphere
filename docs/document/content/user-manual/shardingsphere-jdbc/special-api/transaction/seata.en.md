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

Follow the steps in one of the links below to download and start Seata Server.

The proper way to start Seata Server is to instantiate it through the Docker Image of `seataio/seata-server` in Docker Hub.
For `apache/incubator-seata:v2.0.0` and earlier Seata versions, `seataio/seata-server` from Docker Hub should be used.
Otherwise, `apache/seata-server` from Docker Hub should be used.

- [seata-fescar-workshop](https://github.com/seata/fescar-workshop)
- https://hub.docker.com/r/seataio/seata-server
- https://hub.docker.com/r/apache/seata-server

### Create undo_log table

Create the `undo_log` table in each real database instance involved in ShardingSphere.
The SQL content is based on the corresponding database in https://github.com/apache/incubator-seata/tree/v2.0.0/script/client/at/db .
The following content takes MySQL as an example.
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
The configuration file format refers to the [JavaDoc](https://github.com/apache/incubator-seata/blob/v2.0.0/config/seata-config-core/src/main/java/io/seata/config/FileConfiguration.java) of `io.seata.config.FileConfiguration`.

There are four properties in `seata.conf`,

1. `shardingsphere.transaction.seata.at.enable`, when this value is `true`, enable ShardingSphere's Seata AT integration. The default value is `true`
2. `shardingsphere.transaction.seata.tx.timeout`, global transaction timeout (seconds). The default value is `60`
3. `client.application.id`, application unique primary key, used to set `applicationId` of Seata Transaction Manager Client and Seata Resource Manager Client
4. `client.transaction.service.group`, transaction group, used to set `transactionServiceGroup` of Seata Transaction Manager Client and Seata Resource Manager Client.
The default value is `default`

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
In `seata.conf` managed by ShardingSphere, the default value of `client.transaction.service.group` is set to `default` for historical reasons.
Assuming that in the `registry.conf` of Seata Server and Seata Client used by the user, `registry.type` and `config.type` are both `file`,
then for the `.conf` file configured by `config.file.name` of `registry.conf`, 
the default value of the transaction group name is `default_tx_group` after `apache/incubator-seata:v1.5.1`, otherwise it is `my_test_tx_group`.

```conf
client.application.id = example
```

Modify the `registry.conf` file of Seata as required.

## Usage restrictions

ShardingSphere's Seata integration does not support isolation levels.

ShardingSphere's Seata integration places the obtained Seata global transaction into the thread's local variables.
And `org.apache.seata.spring.annotation.GlobalTransactionScanner` uses Dynamic Proxy to enhance the method.
This means that when using ShardingSphere's Seata integration, users should avoid using the Java API of `io.seata:seata-all`, 
unless the user is mixing ShardingSphere's Seata integration with the TCC mode feature of Seata Client.

For ShardingSphere data source, discuss 6 situations,

1. Manually obtain the `java.sql.Connection` instance created from the ShardingSphere data source,
and manually calling the `setAutoCommit()`, `commit()` and `rollback()` methods is allowed.

2. Using the Jakarta EE 8 `javax.transaction.Transactional` annotation on the function is allowed.

3. Using Jakarta EE 9/10’s `jakarta.transaction.Transactional` annotation on functions is allowed.

4. Using Spring Framework’s `org.springframework.transaction.annotation.Transactional` annotation on functions is allowed.

5. Using the `io.seata.spring.annotation.GlobalTransactional` annotation on the function is **not allowed**.

6. Manually create `io.seata.tm.api.GlobalTransaction` instance from `io.seata.tm.api.GlobalTransactionContext`,
calling the `begin()`, `commit()` and `rollback()` methods of an `io.seata.tm.api.GlobalTransaction` instance is **not allowed**.

In actual scenarios where Spring Boot is used, 
`com.alibaba.cloud:spring-cloud-starter-alibaba-seata` and `io.seata:seata-spring-boot-starter` are often transitively imported by other Maven dependencies.
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
you can instantiate a non-proxy ordinary TCC interface implementation class, and then use `io.seata.integration.tx.api.util.ProxyUtil` to create a proxy TCC interface class,
and call the functions corresponding to the three stages of the TCC interface implementation class `Try`, `Confirm`, and `Cancel`.

For the `io.seata.spring.annotation.GlobalTransactional` annotation introduced by the Seata TCC mode or the business functions involved in the Seata TCC mode that need to interact with the database instance, 
ShardingSphere JDBC DataSource should not be used in the business functions marked by this annotation. Instead, 
a `javax.sql.DataSource` instance should be created manually or obtained from a custom Spring Bean.

### Transactional propagation across service calls

Transactional propagationn in cross-service call scenarios is not as out-of-the-box as transaction operations within a single microservice.
For Seata Server, transactional propagation in cross-service call scenarios requires passing XID to the service provider through service calls and binding it to `io.seata.core.context.RootContext`.
Refer to https://seata.apache.org/docs/user/api/ . This requires discussing two situations,

1. In the scenario of using ShardingSphere JDBC, 
transaction scenarios across multiple microservices need to consider using `io.seata.core.context.RootContext.getXID()` to obtain Seata XID in the context of the starting microservice,
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
and the `io.seata.core.context.RootContext` of the microservice instance `a-service` is not bound to the Seata XID of the business function `bMethod` of the microservice instance `b-service`,
so the changes to the MySQL database instance `a-mysql` in the business function will not be rolled back.

In order to achieve that when the business function `bMethod` of the microservice instance `b-service` throws an exception, 
the changes to the MySQL database instances `a-mysql` and `b-mysql` in the business function are rolled back normally,
discuss the common processing solutions in different scenarios.

1. The microservice instances `a-service` and `b-service` are both Spring Boot 2 microservices based on Jakarta EE 8.
Users can use `org.springframework.web.client.RestTemplate` in the business function `bMethod` of the microservice instance `b-service` to pass the XID to the microservice instance `a-service` through the service call.
The possible transformation logic is as follows.

```java
import io.seata.core.context.RootContext;
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

At this time, custom `org.springframework.web.servlet.config.annotation.WebMvcConfigurer` implementations need to be added to the microservice instances `a-service` and `b-service`.

```java
import io.seata.integration.http.TransactionPropagationInterceptor;
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
import io.seata.core.context.RootContext;
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

At this time, custom `org.springframework.web.servlet.config.annotation.WebMvcConfigurer` implementations need to be added to the microservice instances `a-service` and `b-service`.

```java
import io.seata.integration.http.JakartaTransactionPropagationInterceptor;
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
Refer to https://github.com/apache/incubator-seata/tree/v2.0.0/integration .

4. The microservice instances `a-service` and `b-service` are both microservices such as Quarkus, Micronaut Framework and Helidon. 
In this case, Spring WebMVC HandlerInterceptor cannot be used.
You can refer to the following Spring Boot 3 custom WebMvcConfigurer implementation to implement Filter.

```java
import io.seata.common.util.StringUtils;
import io.seata.core.context.RootContext;
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

6. The Seata Client used by the microservice instances `a-service` and `b-service` is `org.apache.seata:seata-all`, not `io.seata:seata-all`.
Change all calls to the `io.seata` package to the `org.apache.seata` package.
