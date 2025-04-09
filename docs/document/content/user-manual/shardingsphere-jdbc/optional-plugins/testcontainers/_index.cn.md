+++
title = "Testcontainers"
weight = 6
+++

## 背景信息

ShardingSphere 默认情况下不提供对 `org.testcontainers.jdbc.ContainerDatabaseDriver` 的 `driverClassName` 的支持。

## 前提条件

要在 ShardingSphere 的配置文件为数据节点使用类似 `jdbc:tc:postgresql:17.2-bookworm://test/demo_ds_0` 的 `jdbcUrl`，
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

## 配置示例

要使用 `org.apache.shardingsphere:shardingsphere-infra-database-testcontainers` 模块，
用户设备总是需要安装 Docker Engine 或符合 https://java.testcontainers.org/supported_docker_environment/ 要求的 alternative container runtimes。
此时可在 ShardingSphere 的 YAML 配置文件正常使用 `jdbc:tc:postgresql:` 前缀的 jdbcURL。

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

`org.apache.shardingsphere:shardingsphere-infra-database-testcontainers` 为 testcontainers-java 风格的 jdbcURL 提供支持，
包括但不限于，

1. 为 `jdbc:tc:clickhouse:` 的 jdbcURL 前缀提供支持的 Maven 模块 `org.testcontainers:clickhouse:1.20.3`
2. 为 `jdbc:tc:postgresql:` 的 jdbcURL 前缀提供支持的 Maven 模块 `org.testcontainers:postgresql:1.20.3`
3. 为 `jdbc:tc:sqlserver:` 的 jdbcURL 前缀提供支持的 Maven 模块 `org.testcontainers:mssqlserver:1.20.3`
4. 为 `jdbc:tc:mariadb:` 的 jdbcURL 前缀提供支持的 Maven 模块 `org.testcontainers:mariadb:1.20.3`
5. 为 `jdbc:tc:mysql:` 的 jdbcURL 前缀提供支持的 Maven 模块 `org.testcontainers:mysql:1.20.3`
6. 为 `jdbc:tc:oracle:` 的 jdbcURL 前缀提供支持的 Maven 模块 `org.testcontainers:oracle-xe:1.20.3` 和 `org.testcontainers:oracle-free:1.20.3`
7. 为 `jdbc:tc:tidb:` 的 jdbcURL 前缀提供支持的 Maven 模块 `org.testcontainers:tidb:1.20.3`
8. 为 `jdbc:tc:firebird:` 的 jdbcURL 前缀提供支持的 Maven 模块 `org.firebirdsql:firebird-testcontainers-java:1.4.0`

## 使用限制

### 生命周期限制

如果像如下所示在 ShardingSphere 配置文件内定义通过 testcontainers-java 创建 Docker Container 的逻辑，

```yaml
dataSources:
  ds_0:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: org.testcontainers.jdbc.ContainerDatabaseDriver
    jdbcUrl: jdbc:tc:postgresql:17.2-bookworm://test/demo_ds_0
```

testcontainers 默认情况下仅在对 `jdbc:tc:postgresql:17.2-bookworm://test/demo_ds_0` 的最后一个 `java.sql.Connection` 关闭后，
停止`jdbc:tc:postgresql:17.2-bookworm://test/demo_ds_0`创建的 Docker Container。
但 ShardingSphere 的内部类会缓存 `java.sql.Connection`。这导致直到 JVM 关闭，
`jdbc:tc:postgresql:17.2-bookworm://test/demo_ds_0` 创建的 Docker Container 才会被关闭。
若有避免 Container 被长期开启的必要，
`org.testcontainers.jdbc.ContainerDatabaseDriver` 存在可用方法来在单元测试中快速关闭相关 Container，
示例如下，

```java
import org.testcontainers.jdbc.ContainerDatabaseDriver;
public class ExampleUtils {
    void test() {
        ContainerDatabaseDriver.killContainers();
    }
}
```

`org.testcontainers.jdbc.ContainerDatabaseDriver#killContainers()`
将立刻销毁所有由 `org.testcontainers.jdbc.ContainerDatabaseDriver` 创建的 Docker Container。
默认情况下，通过 Junit 5 创建的单元测试是串行执行的，因此这一般不会造成问题。


### host-less URIs 限制

对于大多数 testcontainers-java 模块，
ShardingSphere 配置文件的 `jdbcUrl` 可以使用类似 `jdbc:tc:postgresql:17.2-bookworm:///databasename` 的 host-less URIs。

但对于特定 Maven 模块 `org.testcontainers:mssqlserver`，无法使用类似 `jdbc:tc:sqlserver:2022-CU16-ubuntu-22.04:///databasename` 的 host-less URIs，
仅可使用类似 `jdbc:tc:sqlserver:2022-CU16-ubuntu-22.04://test;databaseName=databasename` 的 JDBC Url。
