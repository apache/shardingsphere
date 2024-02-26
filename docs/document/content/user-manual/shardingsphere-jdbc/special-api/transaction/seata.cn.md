+++
title = "Seata 事务"
weight = 7
+++

## 背景信息

Apache ShardingSphere 提供 BASE 事务，集成了 Seata 的实现。本文所指 Seata 集成均指向 Seata AT 模式。

## 前提条件

引入 Maven 依赖，并排除 `io.seata:seata-all` 中过时的 `org.antlr:antlr4-runtime:4.8` 的 Maven 依赖。

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

## 操作步骤

1. 启动 Seata Server
2. 创建日志表
3. 添加 Seata 配置

## 配置示例

### 启动 Seata Server

按照 [seata-fescar-workshop](https://github.com/seata/fescar-workshop) 或 https://hub.docker.com/r/seataio/seata-server 中的步骤，
下载并启动 Seata 服务器。

### 创建 undo_log 表

在每一个分片数据库实例中执创建 `undo_log` 表（以 MySQL 为例）。
SQL 的内容以 https://github.com/apache/incubator-seata/tree/v2.0.0/script/client/at/db 内对应的数据库为准。

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

### 修改配置

在 classpath 的根目录中增加 `seata.conf` 文件， 
配置文件格式参考 `io.seata.config.FileConfiguration` 的 [JavaDoc](https://github.com/apache/incubator-seata/blob/v2.0.0/config/seata-config-core/src/main/java/io/seata/config/FileConfiguration.java)。

`seata.conf` 存在四个属性，

1. `sharding.transaction.seata.at.enable`，当此值为`true`时，开启 ShardingSphere 的 Seata AT 集成，存在默认值为 `true`
2. `sharding.transaction.seata.tx.timeout`，全局事务超时（秒），存在默认值为 `60`
3. `client.application.id`，应用唯一主键
4. `client.transaction.service.group`，所属事务组，存在默认值为 `default`

一个完全配置的 `seata.conf` 如下，

```conf
sharding.transaction.seata.at.enable = true
sharding.transaction.seata.tx.timeout = 60

client {
    application.id = example
    transaction.service.group = default_tx_group
}
```

根据实际场景修改 Seata 的 `file.conf` 和 `registry.conf` 文件。

## 使用限制

ShardingSphere 的 Seata 集成不支持隔离级别。

ShardingSphere 的 Seata 集成将获取到的 Seata 全局事务置入线程的局部变量。
而 `org.apache.seata.spring.annotation.GlobalTransactionScanner` 则是采用 Dynamic Proxy 的方式对方法进行增强。
这意味着用户始终不应该针对 ShardingSphere 的 DataSource 使用 `io.seata:seata-all` 的 Java 注解。
即在使用 ShardingSphere 的 Seata 集成时，用户应避免使用 `io.seata:seata-all` 的 Java API。

针对 ShardingSphere 数据源，讨论 6 种情况，

1. 手动获取从 ShardingSphere 数据源创建的 `java.sql.Connection` 实例，
并手动调用 `setAutoCommit()`, `commit()` 和 `rollback()` 方法，这是被允许的。

2. 在函数上使用 Jakarta EE 8 的 `javax.transaction.Transactional` 注解，这是被允许的。

3. 在函数上使用 Jakarta EE 9/10 的 `jakarta.transaction.Transactional` 注解，这是被允许的。

4. 在函数上使用 Spring Framework 的 `org.springframework.transaction.annotation.Transactional` 注解，这是被允许的。

5. 在函数上使用 `io.seata.spring.annotation.GlobalTransactional` 注解，这是**不被允许的**。

6. 手动从 `io.seata.tm.api.GlobalTransactionContext ` 创建 `io.seata.tm.api.GlobalTransaction` 实例，
调用 `io.seata.tm.api.GlobalTransaction` 实例的 `begin()`, `commit()` 和 `rollback()` 方法，这是**不被允许的**。

对于Seata Server 2.0.0，
Seata Server 不会为同一 **transaction group** 的所有已连接的 Seata Client 实例传递 `io.seata.core.context.RootContext.getXID()` 的返回值，
参考 https://seata.apache.org/docs/user/api/ 。
这需要讨论两种情况，

1. 在使用 ShardingSphere JDBC 的场景下，
   跨多个微服务的事务场景需要考虑在起点微服务的上下文使用 `io.seata.core.context.RootContext.getXID()` 获取 Seata XID 后通过 RPC 传递给终点微服务，
   并在终点微服务的业务函数中调用 `io.seata.core.context.RootContext.bind(rpcXid)`。

2. 在使用 ShardingSphere Proxy 的场景下，多个微服务均对着 ShardingSphere Proxy 的逻辑数据源操作本地事务，
   这将在 ShardingSphere Proxy 的服务端来转化为对分布式事务的操作，不需要考虑额外的 Seata XID。

在使用 Spring Boot OSS 的实际情景中，
`com.alibaba.cloud:spring-cloud-starter-alibaba-seata` 和 `io.seata:seata-spring-boot-starter` 常常被其他 Maven 依赖传递引入。
为了避开事务冲突，你需要手动关闭 Seata 的自动配置类，
并在 Spring Boot OSS 的配置文件中将 `seata.enable-auto-data-source-proxy` 的属性置为 `false`。一个可能的依赖关系如下。

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

对应的 Spring Boot OSS 启动类可能如下。

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

classpath 下对应的 `application.yml` 需要包含以下配置。
在此情况下，在 Spring Boot OSS 的 `application.yaml` 内定义  Seata 的 `registry.conf` 的等价配置不一定有效。
这取决于 Seata Client。
当下游项目使用 `org.apache.shardingsphere:shardingsphere-transaction-base-seata-at` 的 Maven 模块时，
总是被鼓励使用 `registry.conf` 配置 Seata Client。

```yaml
seata:
  enable-auto-data-source-proxy: false
```
