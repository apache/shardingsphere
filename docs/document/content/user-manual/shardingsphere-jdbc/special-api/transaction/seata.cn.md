+++
title = "Seata 事务"
weight = 7
+++

## 背景信息

Apache ShardingSphere 提供 BASE 事务，集成了 Seata 的实现。本文所指 Seata 集成均指向 Seata AT 模式。

## 前提条件

ShardingSphere 的 Seata 集成仅在 `apache/incubator-seata:v2.5.0` 或更高版本可用。
对于 `org.apache.seata:seata-all` Maven 模块对应的 Seata Client，此限制同时作用于 HotSpot VM 和 GraalVM Native Image。
引入 Maven 依赖，并排除 `org.apache.seata:seata-all` 中过时的 `org.antlr:antlr4-runtime:4.8` 的 Maven 依赖。

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

使用 ShardingSphere 的 Seata 集成模块时，ShardingSphere 连接的数据库实例应同时实现 ShardingSphere 的方言解析支持与 Seata AT 模式的方言解析支持。
这类数据库包括但不限于 `mysql`，`gvenzl/oracle-free`，`gvenzl/oracle-xe`，`postgres`，`mcr.microsoft.com/mssql/server` 等 Docker Image。

### `undo_log` 表限制

在每一个 ShardingSphere 涉及的真实数据库实例中均需要创建 `undo_log` 表。
每种数据库的 SQL 的内容以 https://github.com/apache/incubator-seata/tree/v2.5.0/script/client/at/db 内对应的数据库为准。

### 相关配置

在自有项目的 ShardingSphere 的 YAML 配置文件写入如下内容，参考 [分布式事务](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/transaction)。
若初始化 ShardingSphere JDBC DataSource 时使用的是 Java API，参考 [分布式事务](/cn/user-manual/shardingsphere-jdbc/java-api/rules/transaction)。

```yaml
transaction:
   defaultType: BASE
   providerType: Seata
```

在 classpath 的根目录中增加 `seata.conf` 文件，
配置文件格式参考 `org.apache.seata.config.FileConfiguration` 的 [JavaDoc](https://github.com/apache/incubator-seata/blob/v2.5.0/config/seata-config-core/src/main/java/org/apache/seata/config/FileConfiguration.java)。

`seata.conf` 存在四个属性，

1. `shardingsphere.transaction.seata.at.enable`，当此值为`true`时，开启 ShardingSphere 的 Seata AT 集成。存在默认值为 `true`
2. `shardingsphere.transaction.seata.tx.timeout`，全局事务超时（秒）。存在默认值为 `60`
3. `client.application.id`，应用唯一主键，用于设置 Seata Transaction Manager Client 和 Seata Resource Manager Client 的 `applicationId`
4. `client.transaction.service.group`，所属事务组， 用于设置 Seata Transaction Manager Client 和 Seata Resource Manager Client 的 `transactionServiceGroup`。
   存在默认值为 `default`

一个完全配置的 `seata.conf` 如下，

```conf
shardingsphere.transaction.seata.at.enable = true
shardingsphere.transaction.seata.tx.timeout = 60

client {
    application.id = example
    transaction.service.group = default_tx_group
}
```

一个最小配置的 `seata.conf` 如下。
由 ShardingSphere 管理的 `seata.conf` 中， `client.transaction.service.group` 的默认值为 `default` 是出于历史原因。
假设用户使用的 Seata Server 和 Seata Client 的 `registry.conf` 中，`registry.type` 和 `config.type` 均为 `file`，
则对于 `registry.conf` 的 `config.file.name` 配置的 `.conf` 文件中，事务分组名在 `apache/incubator-seata:v1.5.1` 及之后默认值为 `default_tx_group`，
在 `apache/incubator-seata:v1.5.1` 之前则为 `my_test_tx_group`。

```conf
client.application.id = example
```

根据实际场景修改 Seata 的 `registry.conf` 文件。

## 操作步骤

1. 启动 Seata Server
2. 创建 `undo_log` 表
3. 添加 Seata 配置

## 配置示例

### 启动 Seata Server 和 MySQL Server

编写 Docker Compose 文件来启动 Seata Server 和 MySQL Server。

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

`./docker-entrypoint-initdb.d` 文件夹包含文件为 `init.sh`，内容如下，

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

### 在业务项目的 classpath 创建 `seata.conf`

在业务项目的 classpath 创建 `seata.conf`，内容如下，

```
service {
    default.grouplist = "127.0.0.1:8091"
    vgroupMapping.default_tx_group = "default"
}
```

### 在业务项目的 classpath 创建 `file.conf`

在业务项目的 classpath 创建 `file.conf`，内容如下，

```
client {
    application.id = test
    transaction.service.group = default_tx_group
}
```

### 在业务项目的 classpath 创建 `registry.conf`

在业务项目的 classpath 创建 `registry.conf`，内容如下，

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

### 在业务项目添加 JDBC Driver 和创建 ShardingSphere 配置文件

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
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>9.4.0</version>
</dependency>
```

在业务项目的 classpath 上编写 ShardingSphere 数据源的配置文件`demo.yaml`，

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

### 享受集成

在 ShardingSphere 的数据源上可开始享受集成，

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

## 使用限制

ShardingSphere 的 Seata 集成不支持隔离级别。

ShardingSphere 的 Seata 集成将获取到的 Seata 全局事务置入线程的局部变量。
而 `org.apache.seata.spring.annotation.GlobalTransactionScanner` 则是采用 Dynamic Proxy 的方式对方法进行增强。
这意味着用户在使用 ShardingSphere 的 Seata 集成时，用户应避免使用 `org.apache.seata:seata-all` 的 Java API，
除非用户正在混合使用 ShardingSphere 的 Seata 集成与 Seata Client 的 TCC 模式特性。

针对 ShardingSphere 数据源，讨论 7 种情况，

1. 手动获取从 ShardingSphere 数据源创建的 `java.sql.Connection` 实例，并手动调用 `setAutoCommit()`, `commit()` 和 `rollback()` 方法，
这是被允许的。

2. 在函数上使用 Jakarta EE 8 的 `javax.transaction.Transactional` 注解，这是被允许的。

3. 在函数上使用 Jakarta EE 9/10 的 `jakarta.transaction.Transactional` 注解，这是被允许的。

4. 在函数上使用 Spring Framework 的 `org.springframework.transaction.annotation.Transactional` 注解，这是被允许的。

5. 手动获取从 `org.springframework.transaction.PlatformTransactionManager` 实例创建的 `org.springframework.transaction.support.TransactionTemplate` 实例，
并使用 `org.springframework.transaction.support.TransactionTemplate#execute(org.springframework.transaction.support.TransactionCallback)`，
这是被允许的。

6. 在函数上使用 `org.apache.seata.spring.annotation.GlobalTransactional` 注解，这是**不被允许的**。

7. 手动从 `org.apache.seata.tm.api.GlobalTransactionContext ` 创建 `org.apache.seata.tm.api.GlobalTransaction` 实例，
调用 `org.apache.seata.tm.api.GlobalTransaction` 实例的 `begin()`, `commit()` 和 `rollback()` 方法，这是**不被允许的**。

在使用 Spring Boot 的实际情景中，
`com.alibaba.cloud:spring-cloud-starter-alibaba-seata` 和 `org.apache.seata:seata-spring-boot-starter` 常常被其他 Maven 依赖传递引入。
为了避开事务冲突，用户需要在 Spring Boot 的配置文件中将 `seata.enable-auto-data-source-proxy` 的属性置为 `false`。一个可能的依赖关系如下。

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

classpath 下对应的 `application.yml` 需要包含以下配置。
在此情况下，在 Spring Boot 的 `application.yaml` 内定义 Seata 的 `registry.conf` 的等价配置依然有效。
当下游项目使用 `org.apache.shardingsphere:shardingsphere-transaction-base-seata-at` 的 Maven 模块时，总是被鼓励使用 `registry.conf` 配置 Seata Client。

```yaml
seata:
  enable-auto-data-source-proxy: false
```

### 与 Seata TCC 模式特性混合使用

对于设置开启 ShardingSphere 的 Seata 集成的情况下，
在与 ShardingSphere JDBC DataSource 无关的业务函数中，如需在业务函数使用 Seata Client 的 Seata TCC 模式相关的特性，
可实例化一个未代理的普通 TCC 接口实现类， 然后使用 `org.apache.seata.integration.tx.api.util.ProxyUtil` 创建一个代理的TCC接口类，
并调用 TCC 接口实现类 `Try`，`Confirm`，`Cancel` 三个阶段对应的函数。

对于由 Seata TCC 模式而引入的 `org.apache.seata.spring.annotation.GlobalTransactional` 注解或 Seata TCC 模式涉及的业务函数中需要与数据库实例交互，
此注解标记的业务函数内不应使用 ShardingSphere JDBC DataSource，
而是应该手动创建`javax.sql.DataSource` 实例，或从自定义的 Spring Bean 中获取 `javax.sql.DataSource` 实例。

### 跨服务调用的事务传播

跨服务调用场景下的事务传播，并不像单个微服务内的事务操作一样开箱即用。
对于 Seata Server，跨服务调用场景下的事务传播，要把 XID 通过服务调用传递到服务提供方，并绑定到 `org.apache.seata.core.context.RootContext` 中去。
参考 https://seata.apache.org/docs/user/api/ 。这需要讨论两种情况，

1. 在使用 ShardingSphere JDBC 的场景下，跨多个微服务的事务场景需要考虑在起点微服务的上下文使用 `org.apache.seata.core.context.RootContext.getXID()` 获取 Seata XID 后，
   通过 HTTP 或 RPC 等手段传递给终点微服务，并在终点微服务的 Filter 或 Spring WebMVC HandlerInterceptor 中处理。
   Spring WebMVC HandlerInterceptor 仅适用于 Spring Boot 微服务，对 Quarkus，Micronaut Framework 和 Helidon 无效。

2. 在使用 ShardingSphere Proxy 的场景下，多个微服务均对着 ShardingSphere Proxy 的逻辑数据源操作本地事务，
   这将在 ShardingSphere Proxy 的服务端来转化为对分布式事务的操作，不需要考虑额外的 Seata XID。

引入简单场景来继续讨论在使用 ShardingSphere JDBC 的场景下，跨服务调用的事务传播。假设存在以下已知微服务和中间件的 Docker Image 实例。

1. MySQL 数据库实例 `a-mysql`，所有 database 均已创建 `UNDO_LOG` 表和业务表。
2. MySQL 数据库实例 `b-mysql`，所有 database 均已创建 `UNDO_LOG` 表和业务表。
3. 使用 `file` 作为配置中心和注册中心的 Seata Server 实例 `a-seata-server`。
4. 微服务实例 `a-service`。此微服务创建仅配置数据库实例 `a-mysql` 的 ShardingSphere JDBC DataSource。
此 ShardingSphere JDBC DataSource 配置使用连接到 Seata Server 实例 `a-seata-server` 的 Seata AT 集成，其 Seata Application Id 为 `service-a`，
其 Seata 事务分组为 `default_tx_group`，其 `Virtual Group Mapping` 指向的 Seata Transaction Coordinator 集群分组为 `default`。
此微服务实例 `a-service` 暴露单个 Restful API 的 GET 端点为 `/hello`，此 Restful API 端点的业务函数 `aMethod` 使用了普通的本地事务注解。
若此微服务基于 Spring Boot 2，

```java
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {
   @Transactional
   @GetMapping("/hello")
   public String aMethod() {
      // ... 对数据库实例 `a-mysql` 做 UPDATE 操作
      return "Hello World!";
   }
}
```

5. 微服务实例 `b-service`。此微服务创建仅配置数据库实例 `b-mysql` 的 ShardingSphere JDBC DataSource。
此 ShardingSphere JDBC DataSource 配置使用连接到 Seata Server 实例 `a-seata-server` 的 Seata AT 集成，其 Seata Application Id 为 `service-b`，
其 Seata 事务分组为 `default_tx_group`，其 `Virtual Group Mapping` 指向的 Seata Transaction Coordinator 集群分组为 `default`。
此微服务实例 `b-service` 的业务函数 `bMethod` 使用普通的本地事务注解，并在 `bMethod` 通过 HTTP Client 调用微服务实例 `a-service` 的 `/hello` Restful API 端点。
若此微服务基于 Spring Boot 2，

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
      // ... 对数据库实例 `b-mysql` 做 UPDATE 操作
   }
}
```

对于此简单场景，此时存在单个 Seata Server Cluster，其包含单个 `Virtual Group` 为 `default`。 此 `Virtual Group` 包含单个 Seata Server 实例为 `a-seata-server`。

讨论单服务调用的事务传播。当微服务实例 `a-service` 的业务函数 `aMethod` 抛出异常，在业务函数内对 MySQL 数据库实例 `a-mysql` 的更改将被正常回滚。

讨论跨服务调用的事务传播。当微服务实例 `b-service` 的业务函数 `bMethod` 抛出异常，在业务函数内对 MySQL 数据库实例 `b-mysql` 的更改将被正常回滚，
而微服务实例 `a-service` 的 `org.apache.seata.core.context.RootContext` 未绑定微服务实例 `b-service` 的业务函数 `bMethod` 的 Seata XID，
因此在业务函数内对 MySQL 数据库实例 `a-mysql` 的更改将不会被回滚。

为了实现当微服务实例 `b-service` 的业务函数 `bMethod` 抛出异常，在业务函数内对 MySQL 数据库实例 `a-mysql` 和 `b-mysql` 的更改均被正常回滚，
讨论不同场景下的常见处理方案。

1. 微服务实例 `a-service` 和 `b-service` 均为基于 Jakarta EE 8 的 Spring Boot 2 微服务。
用户可在微服务实例 `b-service` 的业务函数 `bMethod` 使用 `org.springframework.web.client.RestTemplate` 把 XID 通过服务调用传递到微服务实例 `a-service`。
可能的改造逻辑如下。

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
        // ... 对数据库实例 `b-mysql` 做 UPDATE 操作
    }
}
```

此时在微服务实例 `a-service` 需要添加自定义的 `org.springframework.web.servlet.config.annotation.WebMvcConfigurer` 实现。

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

此时，当微服务实例 `b-service` 的业务函数 `bMethod` 抛出异常，在业务函数内对 MySQL 数据库实例 `a-mysql` 和 `b-mysql` 的更改均被正常回滚。

2. 微服务实例 `a-service` 和 `b-service` 均为基于 Jakarta EE 9/10 的 Spring Boot 3 微服务。
用户可在微服务实例 `b-service` 的业务函数 `bMethod` 使用 `org.springframework.web.client.RestClient` 把 XID 通过服务调用传递到微服务实例 `a-service`。
可能的改造逻辑如下。

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
        // ... 对数据库实例 `b-mysql` 做 UPDATE 操作
    }
}
```

此时在微服务实例 `a-service` 需要添加自定义的 `org.springframework.web.servlet.config.annotation.WebMvcConfigurer` 实现。

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

此时，当微服务实例 `b-service` 的业务函数 `bMethod` 抛出异常，在业务函数内对 MySQL 数据库实例 `a-mysql` 和 `b-mysql` 的更改均被正常回滚。

3. 微服务实例 `a-service` 和 `b-service` 均为 Spring Boot 微服务，但使用的 API 网关中间件阻断了所有包含 `TX_XID` 的 HTTP Header 的 HTTP 请求。
用户需要考虑更改把 XID 通过服务调用传递到微服务实例 `a-service` 使用的 HTTP Header，或使用 RPC 框架把 XID 通过服务调用传递到微服务实例 `a-service`。
参考 https://github.com/apache/incubator-seata/tree/v2.5.0/integration 。

4. 微服务实例 `a-service` 和 `b-service` 均为 Quarkus，Micronaut Framework 和 Helidon 等微服务。
此情况下无法使用 Spring WebMVC HandlerInterceptor。
可参考如下 Spring Boot 3 的自定义 WebMvcConfigurer 实现，来实现 Filter。

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

5. 微服务实例 `a-service` 和 `b-service` 均为 Spring Boot 微服务，但使用的组件是 Spring WebFlux 而非 Spring WebMVC。
在反应式编程 API 下 ShardingSphere JDBC 无法处理 R2DBC DataSource，仅可处理 JDBC DataSource。
在使用 WebFlux 组件的 Spring Boot 微服务中应避免创建 ShardingSphere JDBC DataSource。

### Log 配置

在业务项目启动 Seata Client 后，可能看到如下的 Error Log。

```shell
[ERROR] 2024-12-20 11:46:43.878 [ForkJoinPool.commonPool-worker-1] o.a.s.config.ConfigurationFactory - failed to load non-spring configuration :not found service provider for : org.apache.seata.config.ConfigurationProvider
org.apache.seata.common.loader.EnhancedServiceNotFoundException: not found service provider for : org.apache.seata.config.ConfigurationProvider
```

根据 https://github.com/apache/incubator-seata/issues/6886 ，抛出此异常是 Seata Client 的预期行为。
若使用 logback 作为 SLF4J 的实现，用户可通过在业务项目的 classpath 放置 `logback.xml` 对 Seata Client 的日志加以配置。
