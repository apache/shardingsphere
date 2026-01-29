+++
title = "DistSQL"
weight = 6
chapter = true
+++

# Background Information

Currently, DistSQL can be executed via ShardingSphere JDBC DataSource to dynamically modify ShardingSphere configuration.

# Configuration Example

## Prerequisites

Include the following dependencies in your business project:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-dialect-mysql</artifactId>
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
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.2.224</version>
</dependency>
```

Write the ShardingSphere data source configuration file `demo.yaml` on the classpath of your business project.

```yaml
props:
  sql-show: false
```

## Enjoy the Integration

Create a ShardingSphere data source to enjoy integration.

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
            statement.execute("register storage unit if not exists ds_0 (url='jdbc:h2:mem:local_sharding_ds_0;MODE=MYSQL;IGNORECASE=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE',user='sa',password=''), ds_1 (url='jdbc:h2:mem:local_sharding_ds_1;MODE=MYSQL;IGNORECASE=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE',user='sa',password=''), ds_2 (url='jdbc:h2:mem:local_sharding_ds_2;MODE=MYSQL;IGNORECASE=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE',user='sa',password='')");
            statement.execute("create default sharding database strategy if not exists (type='standard', sharding_column=user_id,sharding_algorithm(type(name=class_based, properties('strategy'='STANDARD', 'algorithmClassName'='org.apache.shardingsphere.test.natived.commons.algorithm.ClassBasedInlineShardingAlgorithmFixture'))))");
            statement.execute("create sharding table rule if not exists t_order (datanodes('<LITERAL>ds_0.t_order, ds_1.t_order, ds_2.t_order'), key_generate_strategy(column=order_id,type(name='SNOWFLAKE'))), t_order_item (datanodes('<LITERAL>ds_0.t_order_item, ds_1.t_order_item, ds_2.t_order_item'), key_generate_strategy(column=order_item_id,type(name='SNOWFLAKE')))");
            statement.execute("create broadcast table rule if not exists t_address");
            statement.execute("CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT NOT NULL AUTO_INCREMENT,order_type INT(11),user_id INT NOT NULL,address_id BIGINT NOT NULL,status VARCHAR(50),PRIMARY KEY (order_id))");
            statement.execute("TRUNCATE TABLE t_order");
            statement.execute("INSERT INTO t_order (user_id, order_type, address_id, status) VALUES (1, 1, 1, 'INSERT_TEST')");
            statement.execute("SELECT * FROM t_order");
            statement.execute("DELETE FROM t_order WHERE user_id=1");
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("drop broadcast table rule if exists t_address");
            statement.execute("drop sharding table rule if exists t_order, t_order_item");
            statement.execute("drop default sharding database strategy if exists");
            statement.execute("unregister storage unit if exists ds_0, ds_1, ds_2");
        }
    }
}
```

# Usage Restrictions

## YAML Restrictions

A YAML configuration file must always be created before using the ShardingSphere JDBC Driver.

## Property Restrictions

If you need to define the `props` and `databaseName` properties in the YAML configuration, you must configure them within the YAML file and cannot modify them.
A possible configuration file is as follows:

```yaml
databaseName: logic_db
props:
  sql-show: false
```

## SQL Restrictions

If the `databaseName` property is not defined in the YAML configuration file, 
the default logical database name is `logic_db`.
In this case, you cannot execute SQL statements like `CREATE DATABASE sharding_db` or `USE sharding_db` to create a new logical database against the ShardingSphere JDBC DataSource.
