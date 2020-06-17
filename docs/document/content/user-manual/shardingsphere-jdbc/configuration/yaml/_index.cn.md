+++
title = "YAML 配置"
weight = 2
+++

## 简介

YAML 提供通过配置文件的方式与 ShardingSphere-JDBC 交互。配合治理模块一同使用时，持久化在配置中心的配置均为 YAML 格式。

YAML 配置是最常见的配置方式，可以省略编程的复杂度，简化用户配置。

## 使用方式

### 创建简单数据源

通过 YamlShardingSphereDataSourceFactory 工厂创建的 ShardingSphereDataSource 实现自 JDBC 的标准接口 DataSource。

```java
// 指定 YAML 文件路径
File yamlFile = // ...

DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
```

### 创建携带治理功能的数据源

通过 YamlOrchestrationShardingSphereDataSourceFactory 工厂创建的 OrchestrationShardingSphereDataSource 实现自 JDBC 的标准接口 DataSource。

```java
// 指定 YAML 文件路径
File yamlFile = // ...

DataSource dataSource = YamlOrchestrationShardingSphereDataSourceFactory.createDataSource(yamlFile);
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

## YAML 语法说明

`!!` 表示实例化该类

`!` 表示自定义别名

`-` 表示可以包含一个或多个

`[]` 表示数组，可以与减号相互替换使用
