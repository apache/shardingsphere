+++
title = "Java API"
weight = 1
chapter = true
+++

## 简介

Java API 是 ShardingSphere-JDBC 中所有配置方式的基础，其他配置最终都将转化成为 Java API 的配置方式。

Java API 是最复杂也是最灵活的配置方式，适合需要通过编程进行动态配置的场景下使用。

## 使用方式

### 创建简单数据源

通过 ShardingSphereDataSourceFactory 工厂创建的 ShardingSphereDataSource 实现自 JDBC 的标准接口 DataSource。

```java
// 构建数据源
Map<String, DataSource> dataSourceMap = // ...

// 构建配置规则
Collection<RuleConfiguration> configurations = // ...

// 构建属性配置
Properties props = // ...

DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, configurations, props);
```

### 创建携带治理功能的数据源

通过 OrchestrationShardingSphereDataSourceFactory 工厂创建的 OrchestrationShardingSphereDataSource 实现自 JDBC 的标准接口 DataSource。

```java
// 构建数据源
Map<String, DataSource> dataSourceMap = // ...

// 构建配置规则
Collection<RuleConfiguration> configurations = // ...

// 构建属性配置
Properties props = // ...

// 构建注册中心配置对象
OrchestrationConfiguration orchestrationConfig = // ...

DataSource dataSource = OrchestrationShardingSphereDataSourceFactory.createDataSource(dataSourceMap, configurations, props, orchestrationConfig);
```

### 使用数据源

可通过 DataSource 选择使用原生 JDBC，或JPA， MyBatis 等 ORM 框架。

以原生 JDBC 使用方式为例：

```java
DataSource dataSource = // 通过Apache ShardingSphere 工厂创建的数据源
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
