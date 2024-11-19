+++
title = "Testcontainers"
weight = 6
+++

ShardingSphere does not provide support for `driverClassName` of `org.testcontainers.jdbc.ContainerDatabaseDriver` by default.
To use `jdbcUrl` like `jdbc:tc:postgresql:17.1-bookworm://test-native-databases-postgres/demo_ds_0` for data nodes in ShardingSphere's configuration file,
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

`org.apache.shardingsphere:shardingsphere-infra-database-testcontainers` provides support for jdbcURL in the testcontainers-java partition, 
including but not limited to,

1. Maven module `org.testcontainers:clickhouse:1.20.3` that provides support for jdbcURL prefixes for `jdbc:tc:clickhouse:`
2. Maven module `org.testcontainers:postgresql:1.20.3` that provides support for jdbcURL prefixes for `jdbc:tc:postgresql:`
3. Maven module `org.testcontainers:mssqlserver:1.20.3` that provides support for jdbcURL prefixes for `jdbc:tc:sqlserver:`
4. Maven module `org.testcontainers:mariadb:1.20.3` that provides support for jdbcURL prefixes for `jdbc:tc:mariadb:`
5. Maven module `org.testcontainers:mysql:1.20.3` that provides support for jdbcURL prefixes of `jdbc:tc:mysql:` 
6. Maven modules `org.testcontainers:oracle-xe:1.20.3` and `org.testcontainers:oracle-free:1.20.3` that provide support for jdbcURL prefixes of `jdbc:tc:oracle:`
7. Maven module `org.testcontainers:tidb:1.20.3` that provides support for jdbcURL prefixes of `jdbc:tc:tidb:`
