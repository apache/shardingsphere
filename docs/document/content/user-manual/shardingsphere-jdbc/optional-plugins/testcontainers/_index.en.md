+++
title = "Testcontainers"
weight = 6
+++

## Background Information

ShardingSphere does not provide support for `driverClassName` of `org.testcontainers.jdbc.ContainerDatabaseDriver` by default.

## Prerequisites

To use `jdbcUrl` like `jdbc:tc:postgresql:17.2-bookworm://test/demo_ds_0` for data nodes in ShardingSphere's configuration file,
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
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>42.7.5</version>
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

## Configuration Example

To use the `org.apache.shardingsphere:shardingsphere-infra-database-testcontainers` module,
the user machine always needs to have Docker Engine or alternative container runtimes that comply with https://java.testcontainers.org/supported_docker_environment/ installed.
`org.apache.shardingsphere:shardingsphere-infra-database-testcontainers` provides support for testcontainers-java style jdbcURL,
including but not limited to,

```yaml
dataSources:
  ds_0:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: org.testcontainers.jdbc.ContainerDatabaseDriver
    jdbcUrl: jdbc:tc:postgresql:17.2-bookworm://test/demo_ds_0
  ds_1:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: org.testcontainers.jdbc.ContainerDatabaseDriver
    jdbcUrl: jdbc:tc:postgresql:17.2-bookworm://test/demo_ds_1
  ds_2:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: org.testcontainers.jdbc.ContainerDatabaseDriver
    jdbcUrl: jdbc:tc:postgresql:17.2-bookworm://test/demo_ds_2
```

1. Maven module `org.testcontainers:clickhouse:1.20.3` that provides support for jdbcURL prefixes for `jdbc:tc:clickhouse:`
2. Maven module `org.testcontainers:postgresql:1.20.3` that provides support for jdbcURL prefixes for `jdbc:tc:postgresql:`
3. Maven module `org.testcontainers:mssqlserver:1.20.3` that provides support for jdbcURL prefixes for `jdbc:tc:sqlserver:`
4. Maven module `org.testcontainers:mariadb:1.20.3` that provides support for jdbcURL prefixes for `jdbc:tc:mariadb:`
5. Maven module `org.testcontainers:mysql:1.20.3` that provides support for jdbcURL prefixes of `jdbc:tc:mysql:` 
6. Maven modules `org.testcontainers:oracle-xe:1.20.3` and `org.testcontainers:oracle-free:1.20.3` that provide support for jdbcURL prefixes of `jdbc:tc:oracle:`
7. Maven module `org.testcontainers:tidb:1.20.3` that provides support for jdbcURL prefixes of `jdbc:tc:tidb:`
8. Maven module `org.firebirdsql:firebird-testcontainers-java:1.4.0` that provides support for jdbcURL prefixes of `jdbc:tc:firebird:`

## Usage restrictions

### Lifecycle restrictions

If the logic of creating a Docker Container through testcontainers-java is defined in the ShardingSphere configuration file as shown below,

```yaml
dataSources:
  ds_0:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: org.testcontainers.jdbc.ContainerDatabaseDriver
    jdbcUrl: jdbc:tc:postgresql:17.2-bookworm://test/demo_ds_0
```

testcontainers, by default, 
stops the Docker Container created by `jdbc:tc:postgresql:17.2-bookworm://test/demo_ds_0` only after the last `java.sql.Connection` of `jdbc:tc:postgresql:17.2-bookworm://test/demo_ds_0` is closed.
But ShardingSphere's internal class will cache `java.sql.Connection`.
As a result, the Docker Container created by `jdbc:tc:postgresql:17.2-bookworm://test/demo_ds_0` will not be closed until the JVM is closed.
If it is necessary to prevent the Container from being opened for a long time, `org.testcontainers.jdbc.ContainerDatabaseDriver` has a method available to quickly close the relevant Container in the unit test.
The example is as follows,

```java
import org.testcontainers.jdbc.ContainerDatabaseDriver;
public class ExampleUtils {
    void test() {
        ContainerDatabaseDriver.killContainers();
    }
}
```

`org.testcontainers.jdbc.ContainerDatabaseDriver#killContainers()`
will immediately destroy all Docker Containers created by `org.testcontainers.jdbc.ContainerDatabaseDriver`.
By default, unit tests created by JUnit 5 are executed serially, so this is generally not a problem.

### host-less URIs restrictions

For most testcontainers-java modules, 
the `jdbcUrl` of ShardingSphere configuration file can use host-less URIs like `jdbc:tc:postgresql:17.2-bookworm:///databasename`.

But for the specific Maven module `org.testcontainers:mssqlserver`, host-less URIs like `jdbc:tc:sqlserver:2022-CU16-ubuntu-22.04:///databasename` cannot be used,
only JDBC Url like `jdbc:tc:sqlserver:2022-CU16-ubuntu-22.04://test;databaseName=databasename` can be used.
