+++
title = "Firebird"
weight = 6
+++

## Background Information

ShardingSphere does not provide support for `driverClassName` of `org.firebirdsql.jdbc.FBDriver` by default.
ShardingSphere's support for Firebird JDBC Driver is in an optional module.

## Prerequisites

To use a `jdbcUrl` like `jdbc:firebird://localhost:3050//var/lib/firebird/data/demo_ds_0.fdb` for the data node in the ShardingSphere configuration file,
the possible Maven dependencies are as follows,

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-jdbc-dialect-firebird</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>org.firebirdsql.jdbc</groupId>
        <artifactId>jaybird</artifactId>
        <version>5.0.10.java8</version>
    </dependency>
</dependencies>
```

## Configuration example

### Start Firebird

Write a Docker Compose file to start Firebird.

```yaml
services:
  firebird:
    image: firebirdsql/firebird:5.0.3
    environment:
      FIREBIRD_ROOT_PASSWORD: masterkey
      FIREBIRD_USER: alice
      FIREBIRD_PASSWORD: masterkey
      FIREBIRD_DATABASE: mirror.fdb
      FIREBIRD_DATABASE_DEFAULT_CHARSET: UTF8
    ports:
      - "3050:3050"
```

### Create business databases

Create some business databases in Firebird through third-party tools.

Third-party tools including DBeaver Community cannot create databases for Firebird.
Below is the Java API of the Maven module `org.firebirdsql.jdbc:jaybird:5.0.10.java8` as an example.

```java
import org.firebirdsql.management.FBManager;
import org.firebirdsql.management.PageSizeConstants;
class Solution {
    void test() throws Exception {
        try (FBManager fbManager = new FBManager()) {
            fbManager.setServer("localhost");
            fbManager.setUserName("alice");
            fbManager.setPassword("masterkey");
            fbManager.setFileName("/var/lib/firebird/data/mirror.fdb");
            fbManager.setPageSize(PageSizeConstants.SIZE_16K);
            fbManager.setDefaultCharacterSet("UTF8");
            fbManager.setPort(3050);
            fbManager.start();
            fbManager.createDatabase("/var/lib/firebird/data/demo_ds_0.fdb", "alice", "masterkey");
            fbManager.createDatabase("/var/lib/firebird/data/demo_ds_1.fdb", "alice", "masterkey");
            fbManager.createDatabase("/var/lib/firebird/data/demo_ds_2.fdb", "alice", "masterkey");
        }
    }
}
```

### Create ShardingSphere data source in business project

After including the dependencies related to the `Prerequisites` in the business project, add the following additional dependencies,

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

Write the ShardingSphere data source configuration file `demo.yaml` on the classpath of the business project.

```yaml
dataSources:
  ds_0:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: org.firebirdsql.jdbc.FBDriver
    jdbcUrl: jdbc:firebird://localhost:3050//var/lib/firebird/data/demo_ds_0.fdb
    username: alice
    password: masterkey
  ds_1:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: org.firebirdsql.jdbc.FBDriver
    jdbcUrl: jdbc:firebird://localhost:3050//var/lib/firebird/data/demo_ds_1.fdb
    username: alice
    password: masterkey
  ds_2:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: org.firebirdsql.jdbc.FBDriver
    jdbcUrl: jdbc:firebird://localhost:3050//var/lib/firebird/data/demo_ds_2.fdb
    username: alice
    password: masterkey
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

### Enjoy integration

Create a ShardingSphere data source to enjoy the integration,

```java
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
class Solution {
    void test() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:shardingsphere:classpath:demo.yaml");
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        try (HikariDataSource dataSource = new HikariDataSource(config);
             Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE t_order (order_id BIGINT generated by default as identity PRIMARY KEY, order_type INT, user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50))");
            statement.execute("INSERT INTO t_order (user_id, order_type, address_id, status) VALUES (1, 1, 1, 'INSERT_TEST')");
            statement.executeQuery("SELECT * FROM t_order");
            statement.execute("DELETE FROM t_order WHERE user_id=1");
            statement.execute("DROP TABLE t_order");
        }
    }
}
```

## Usage Limitations

### Transaction Limitations

Firebird supports local transactions at the ShardingSphere integration level, but does not support XA transactions or Seata's AT mode transactions.

Discussions on XA transactions are at https://github.com/apache/shardingsphere/issues/34973 .

For Seata's AT mode transactions, a PR containing the corresponding implementation should be submitted at https://github.com/apache/incubator-seata .
