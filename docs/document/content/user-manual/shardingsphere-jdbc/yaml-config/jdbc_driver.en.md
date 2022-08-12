+++
title = "JDBC Driver"
weight = 5
chapter = true
+++

## Background

ShardingSphere-JDBC provides a JDBC Driver, which can be used only through configuration changes without rewriting the code.

## Parameters

### Driver Class Name

`org.apache.shardingsphere.driver.ShardingSphereDriver`

### URL Configuration

- Use jdbc:shardingsphere: as prefix
- Configuration file: xxx.yaml, keep consist format with [YAML Configuration](/en/user-manual/shardingsphere-jdbc/yaml-config/)
- Configuration file loading rule:
  - No prefix means that the configuration file is loaded from the absolute path
  - `classpath:` prefix indicates that the configuration file is loaded from the classpath

## Procedure

1. Import Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

2. Use drive

* Use native drivers:

```java
Class.forName("org.apache.shardingsphere.driver.ShardingSphereDriver");
String jdbcUrl = "jdbc:shardingsphere:classpath:config.yaml";

String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=? AND o.order_id=?";
try (
        Connection conn = DriverManager.getConnection(jdbcUrl);
        PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setInt(1, 10);
    ps.setInt(2, 1000);
    try (ResultSet rs = preparedStatement.executeQuery()) {
        while(rs.next()) {
            // ...
        }
    }
}
```

* Use database connection pool:

```java
String driverClassName = "org.apache.shardingsphere.driver.ShardingSphereDriver";
String jdbcUrl = "jdbc:shardingsphere:classpath:config.yaml";

// Take HikariCP as an example 
HikariDataSource dataSource = new HikariDataSource();
dataSource.setDriverClassName(driverClassName);
dataSource.setJdbcUrl(jdbcUrl);

String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=? AND o.order_id=?";
try (
        Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setInt(1, 10);
    ps.setInt(2, 1000);
    try (ResultSet rs = preparedStatement.executeQuery()) {
        while(rs.next()) {
            // ...
        }
    }
}
```

## Sample

Load JDBC URL of config.yaml profile in classpath:
```
jdbc:shardingsphere:classpath:config.yaml
```

Load JDBC URL of config.yaml profile in absolute path
```
jdbc:shardingsphere:/path/to/config.yaml
```
