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

### URL 配置

- 以 `jdbc:shardingsphere:` 为前缀
- 配置文件：`xxx.yaml`，配置文件格式与 [YAML 配置](/cn/user-manual/shardingsphere-jdbc/yaml-config/)一致
- 配置文件加载规则：
  - `absolutepath:` 前缀表示从绝对路径中加载配置文件
  - `classpath:` 前缀表示从类路径中加载配置文件
  - `apollo:` 前缀表示从 apollo 中加载配置文件

## 操作步骤

1. 引入 Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

如果使用 apollo 配置方式，还需要引入 `apollo-client` 依赖：

```xml
<dependency>
    <groupId>com.ctrip.framework.apollo</groupId>
    <artifactId>apollo-client</artifactId>
    <version>${apollo-client.version}</version>
</dependency>
```

2. 使用驱动

* 使用原生驱动：

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

* 使用数据库连接池

```java
String driverClassName = "org.apache.shardingsphere.driver.ShardingSphereDriver";
String jdbcUrl = "jdbc:shardingsphere:classpath:config.yaml";

// 以 HikariCP 为例 
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

## 配置示例

加载 classpath 中 config.yaml 配置文件的 JDBC URL：
```
jdbc:shardingsphere:classpath:config.yaml
```

加载绝对路径中 config.yaml 配置文件的 JDBC URL：
```
jdbc:shardingsphere:absolutepath:/path/to/config.yaml
```

加载 apollo 指定 namespace 中的 yaml 配置文件的 JDBC URL：
```
jdbc:shardingsphere:apollo:TEST.test_namespace
```
