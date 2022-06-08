+++
title = "JDBC Driver"
weight = 3
chapter = true
+++

## Overview

ShardingSphere-JDBC provides JDBC driver, it permits user using ShardingSphere by configuration updating only, without any code changes.

## Usage

### Import Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### Driver Usage

#### Native Driver Usage

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

#### Database Connection Pool Usage

```java
String driverClassName = "org.apache.shardingsphere.driver.ShardingSphereDriver";
String jdbcUrl = "jdbc:shardingsphere:classpath:config.yaml";

// Use HikariCP as sample 
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

### Configuration Explanation

#### Driver Class Name

`org.apache.shardingsphere.driver.ShardingSphereDriver`

#### URL Configuration Explanation

- Use `jdbc:shardingsphere:` as prefix
- Configuration file: `xxx.yaml`, keep consist format with [YAML Configuration](/en/user-manual/yaml-config/)
- Configuration file loading rule:
  - No prefix for loading from absolute path
  - Prefix with `classpath:` for loading from java class path
