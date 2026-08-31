+++
title = "Seata 事务"
weight = 7
+++

## 背景信息

Apache ShardingSphere 提供 BASE 事务，集成了 Seata 的实现。本文所指 Seata 集成均指向 Seata AT 模式。

## 前提条件

本文以 Apache Seata 2.6.0 为目标版本。
HotSpot VM 和 GraalVM Native Image 均使用 `org.apache.seata:seata-all:2.6.0` 作为 Seata Client 依赖。
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

数据库必须同时受到 ShardingSphere 和 Seata AT 模式支持。

### `undo_log` 表限制

在每一个 ShardingSphere 涉及的真实数据库实例中均需要创建 `undo_log` 表。
每种数据库的 SQL 的内容以 https://github.com/apache/incubator-seata/tree/v2.6.0/script/client/at/db 内对应的数据库为准。

### 相关配置

在自有项目的 ShardingSphere 的 YAML 配置文件写入如下内容，参考 [分布式事务](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/transaction)。
若初始化 ShardingSphere JDBC DataSource 时使用的是 Java API，参考 [分布式事务](/cn/user-manual/shardingsphere-jdbc/java-api/rules/transaction)。

```yaml
transaction:
   defaultType: BASE
   providerType: Seata
```

在 classpath 的根目录中增加 `seata.conf` 文件，
配置文件格式参考 `org.apache.seata.config.FileConfiguration` 的 [JavaDoc](https://github.com/apache/incubator-seata/blob/v2.6.0/config/seata-config-core/src/main/java/org/apache/seata/config/FileConfiguration.java)。

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

### 验证事务回滚

以下示例验证执行失败的事务会在 ShardingSphere 数据源上回滚。

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

## 使用限制

ShardingSphere 的 Seata 集成不支持隔离级别。

ShardingSphere 负责管理其 JDBC 数据源对应的 Seata 全局事务。
不要使用其他全局事务入口管理同一个数据源上的事务。

以下规则适用于 ShardingSphere 数据源。

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

classpath 下对应的 `application.yml` 需要包含以下配置。
在此情况下，在 Spring Boot 的 `application.yaml` 内定义 Seata 的 `registry.conf` 的等价配置依然有效。
当下游项目使用 `org.apache.shardingsphere:shardingsphere-transaction-base-seata-at` 的 Maven 模块时，总是被鼓励使用 `registry.conf` 配置 Seata Client。

```yaml
seata:
  enable-auto-data-source-proxy: false
```

### 与其他 Seata 事务模式混用

ShardingSphere 的 Seata 集成仅支持 AT 模式。

如果应用使用 Seata TCC，请使用不由 ShardingSphere JDBC 管理的独立数据源，并参考 Apache Seata 官方文档配置和调用 TCC API。

### 跨服务调用的事务传播

ShardingSphere JDBC 的 Seata AT 集成负责使当前 ShardingSphere 数据源上的数据库操作参与 Seata 全局事务。
该集成不负责在服务之间传播 Seata XID。

当事务跨越 HTTP、RPC、消息或其他服务边界时，XID 的传递、绑定和清理由应用框架及 Seata Client 负责。
请根据使用的 Seata 版本和通信框架参考 Apache Seata 官方文档。

ShardingSphere 不规定传输 Header、框架过滤器或拦截器，也不要求应用手动操作 XID 上下文。
