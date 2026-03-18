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

### URL Configuration and sample

Refer to [known Implementation](./known-implementation/_index.en.md).

## Procedure

1. Import Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

2. Use drive

* Use native drivers:

```java
Class.forName("org.apache.shardingsphere.driver.ShardingSphereDriver");
String standardJdbcUrl = "jdbc:shardingsphere:classpath:config.yaml";

String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=? AND o.order_id=?";
try (
        Connection conn = DriverManager.getConnection(standardJdbcUrl);
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
String standardJdbcUrl = "jdbc:shardingsphere:classpath:config.yaml";

// Take HikariCP as an example 
HikariDataSource dataSource = new HikariDataSource();
dataSource.setDriverClassName(driverClassName);
dataSource.setJdbcUrl(standardJdbcUrl);

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
