+++
title = "H2"
weight = 6
+++

## Background Information

ShardingSphere does not provide support for `driverClassName` of `org.h2.Driver` by default.

ShardingSphere support for H2 JDBC Drivers is located in an optional module.

## Prerequisites

To use a `jdbcUrl` like `jdbc:h2:mem:demo_ds_0;MODE=MYSQL;IGNORECASE=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE` for data nodes in your ShardingSphere configuration file, the following Maven dependencies are required:

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-jdbc-dialect-mysql</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>2.2.224</version>
    </dependency>
</dependencies>
```

## Configuration Example

### Creating a ShardingSphere Data Source in a Business Project

After including the dependencies involved in the `Prerequisites` section in the business project, additionally include the following dependencies:

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
```

Write the ShardingSphere data source configuration file `demo.yaml` on the classpath of your business project.

```yaml
dataSources:
  ds_0:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: org.h2.Driver
    jdbcUrl: jdbc:h2:mem:demo_ds_0;MODE=MYSQL;IGNORECASE=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE
    username: sa
    password:
  ds_1:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: org.h2.Driver
    jdbcUrl: jdbc:h2:mem:demo_ds_1;MODE=MYSQL;IGNORECASE=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE
    username: sa
    password:
  ds_2:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: org.h2.Driver
    jdbcUrl: jdbc:h2:mem:demo_ds_2;MODE=MYSQL;IGNORECASE=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE
    username: sa
    password:
rules:
  - !SHARDING
    tables:
      t_order:
        actualDataNodes: <LITERAL>ds_0.t_order, ds_1.t_order, ds_2.t_order
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

### Enjoy Integration

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
            statement.execute("CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT NOT NULL AUTO_INCREMENT,order_type INT(11),user_id INT NOT NULL,address_id BIGINT NOT NULL,status VARCHAR(50),PRIMARY KEY (order_id))");
            statement.execute("TRUNCATE TABLE t_order");
            statement.execute("INSERT INTO t_order (user_id, order_type, address_id, status) VALUES (1, 1, 1, 'INSERT_TEST')");
            statement.executeQuery("SELECT * FROM t_order");
            statement.execute("DELETE FROM t_order WHERE user_id=1");
            statement.execute("DROP TABLE IF EXISTS t_order");
        }
    }
}
```

## Usage Restrictions

### Database Dialect Restrictions

Because the SPI implementation of `org.apache.shardingsphere:shardingsphere-database-connector-h2` routes SQL executed via the H2 JDBC Driver to the MySQL dialect, the parameter `;MODE=MYSQL;IGNORECASE=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE` needs to be added to the JDBC URL of the H2 JDBC Driver to use H2's MySQL Compatibility Mode.

If developers need to use H2's native SQL dialect, or use H2's Compatibility Mode for other databases, they should exclude `org.apache.shardingsphere:shardingsphere-database-connector-h2` from all Maven dependencies and implement the missing SPI themselves.

### Functionality Limitations

Using ShardingSphere's `!SHARDING` and `!READWRITE_SPLITTING` features simultaneously with an H2 database will cause ShardingSphere to resolve incorrect database metadata, resulting in incorrect results when executing some SQL statements.

If you need to use ShardingSphere's `!SHARDING` and `!READWRITE_SPLITTING` features simultaneously, you should switch to a database such as MySQL. A live MySQL database can be started in a test environment using `testcontainers-java`.
