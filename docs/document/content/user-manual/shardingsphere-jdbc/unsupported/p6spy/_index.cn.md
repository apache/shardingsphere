+++
title = "P6Spy"
weight = 6
+++

## 背景信息

ShardingSphere 通过 `org.apache.shardingsphere:shardingsphere-infra-database-p6spy` 模块，
对 `com.p6spy.engine.spy.P6SpyDriver` 提供部分支持。

## 前提条件

要在 ShardingSphere 的配置文件为 MySQL Server 数据节点使用 P6Spy， 可能的 Maven 依赖关系如下，

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

## 配置示例

### 启动 MySQL Server

编写 Docker Compose 文件来启动 MySQL Server。

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

### 在业务项目创建 ShardingSphere 数据源

在业务项目引入前提条件涉及的依赖后，在业务项目的 classpath 上编写 ShardingSphere 数据源的配置文件`demo.yaml`，

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

### 享受集成

创建 ShardingSphere 的数据源以享受集成，

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

## 使用限制

目前仅支持为 MySQL JDBC Driver 配置 P6Spy。
