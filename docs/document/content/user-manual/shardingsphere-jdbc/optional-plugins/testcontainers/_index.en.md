+++
title = "Testcontainers"
weight = 6
+++

ShardingSphere does not provide support for `driverClassName` of `org.testcontainers.jdbc.ContainerDatabaseDriver` by default.
To use `jdbcUrl` like `jdbc:tc:postgresql:17.1-bookworm://test-databases-postgres/demo_ds_0` for data nodes in ShardingSphere's configuration file,
the possible Maven dependencies are as follows,

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-jdbc</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-infra-database-testcontainers</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <version>1.20.3</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

At this time, you can use the jdbcURL with the prefix `jdbc:tc:postgresql:` normally in the YAML configuration file of ShardingSphere.

```yaml
dataSources:
  ds_0:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: org.testcontainers.jdbc.ContainerDatabaseDriver
    jdbcUrl: jdbc:tc:postgresql:17.1-bookworm://test-databases-postgres/demo_ds_0
  ds_1:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: org.testcontainers.jdbc.ContainerDatabaseDriver
    jdbcUrl: jdbc:tc:postgresql:17.1-bookworm://test-databases-postgres/demo_ds_1
  ds_2:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: org.testcontainers.jdbc.ContainerDatabaseDriver
    jdbcUrl: jdbc:tc:postgresql:17.1-bookworm://test-databases-postgres/demo_ds_2
```

To use the `org.apache.shardingsphere:shardingsphere-infra-database-testcontainers` module, 
the user machine always needs to have Docker Engine or alternative container runtimes that comply with https://java.testcontainers.org/supported_docker_environment/ installed.
`org.apache.shardingsphere:shardingsphere-infra-database-testcontainers` provides support for testcontainers-java style jdbcURL,
including but not limited to,

1. Maven module `org.testcontainers:clickhouse:1.20.3` that provides support for jdbcURL prefixes for `jdbc:tc:clickhouse:`
2. Maven module `org.testcontainers:postgresql:1.20.3` that provides support for jdbcURL prefixes for `jdbc:tc:postgresql:`
3. Maven module `org.testcontainers:mssqlserver:1.20.3` that provides support for jdbcURL prefixes for `jdbc:tc:sqlserver:`
4. Maven module `org.testcontainers:mariadb:1.20.3` that provides support for jdbcURL prefixes for `jdbc:tc:mariadb:`
5. Maven module `org.testcontainers:mysql:1.20.3` that provides support for jdbcURL prefixes of `jdbc:tc:mysql:` 
6. Maven modules `org.testcontainers:oracle-xe:1.20.3` and `org.testcontainers:oracle-free:1.20.3` that provide support for jdbcURL prefixes of `jdbc:tc:oracle:`
7. Maven module `org.testcontainers:tidb:1.20.3` that provides support for jdbcURL prefixes of `jdbc:tc:tidb:`
