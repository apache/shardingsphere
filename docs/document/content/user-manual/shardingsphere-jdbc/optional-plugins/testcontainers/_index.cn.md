+++
title = "Testcontainers"
weight = 6
+++

ShardingSphere 默认情况下不提供对 `org.testcontainers.jdbc.ContainerDatabaseDriver` 的 `driverClassName` 的支持。
要在 ShardingSphere 的配置文件为数据节点使用类似 `jdbc:tc:postgresql:17.1-bookworm://test-databases-postgres/demo_ds_0` 的 `jdbcUrl`，
可能的 Maven 依赖关系如下，

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

要使用 `org.apache.shardingsphere:shardingsphere-infra-database-testcontainers` 模块，
用户设备总是需要安装 Docker Engine 或符合 https://java.testcontainers.org/supported_docker_environment/ 要求的 alternative container runtimes。
此时可在 ShardingSphere 的 YAML 配置文件正常使用 `jdbc:tc:postgresql:` 前缀的 jdbcURL。

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

`org.apache.shardingsphere:shardingsphere-infra-database-testcontainers` 为 testcontainers-java 风格的 jdbcURL 提供支持，
包括但不限于，

1. 为 `jdbc:tc:clickhouse:` 的 jdbcURL 前缀提供支持的 Maven 模块 `org.testcontainers:clickhouse:1.20.3`
2. 为 `jdbc:tc:postgresql:` 的 jdbcURL 前缀提供支持的 Maven 模块 `org.testcontainers:postgresql:1.20.3`
3. 为 `jdbc:tc:sqlserver:` 的 jdbcURL 前缀提供支持的 Maven 模块 `org.testcontainers:mssqlserver:1.20.3`
4. 为 `jdbc:tc:mariadb:` 的 jdbcURL 前缀提供支持的 Maven 模块 `org.testcontainers:mariadb:1.20.3`
5. 为 `jdbc:tc:mysql:` 的 jdbcURL 前缀提供支持的 Maven 模块 `org.testcontainers:mysql:1.20.3`
6. 为 `jdbc:tc:oracle:` 的 jdbcURL 前缀提供支持的 Maven 模块 `org.testcontainers:oracle-xe:1.20.3` 和 `org.testcontainers:oracle-free:1.20.3`
7. 为 `jdbc:tc:tidb:` 的 jdbcURL 前缀提供支持的 Maven 模块 `org.testcontainers:tidb:1.20.3`
