+++
title = "JDBC 驱动"
weight = 5
chapter = true
+++

## 背景信息

ShardingSphere-JDBC 提供了 JDBC 驱动，可以仅通过配置变更即可使用，无需改写代码。

## 参数解释

### 驱动类名称

`org.apache.shardingsphere.driver.ShardingSphereDriver`

### URL 配置及配置示例

参考 [已知实现](./known-implementation/_index.cn.md) 。

## 操作步骤

1. 引入 Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

2. 使用驱动

* 使用原生驱动：

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

* 使用数据库连接池

```java
String driverClassName = "org.apache.shardingsphere.driver.ShardingSphereDriver";
String standardJdbcUrl = "jdbc:shardingsphere:classpath:config.yaml";

// 以 HikariCP 为例 
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
