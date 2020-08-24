+++
title = "使用 YAML 配置"
weight = 2
+++

## 引入 Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

## 规则配置

ShardingSphere-JDBC 的 YAML 配置文件 通过数据源集合、规则集合以及属性配置组成。
以下示例是根据 `user_id` 取模分库, 且根据 `order_id` 取模分表的 2 库 2 表的配置。

```yaml
# 配置真实数据源
dataSources:
  # 配置第 1 个数据源
  ds0: !!org.apache.commons.dbcp2.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds0
    username: root
    password:
  # 配置第 2 个数据源
  ds1: !!org.apache.commons.dbcp2.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds1
    username: root
    password: 

rules:
# 配置分片规则
- !SHARDING
  tables:
    # 配置 t_order 表规则
    t_order: 
      actualDataNodes: ds${0..1}.t_order${0..1}
      # 配置分库策略
      databaseStrategy:
        standard:
          shardingColumn: user_id
          shardingAlgorithmName: database_inline
      # 配置分表策略
      tableStrategy:
        standard:
          shardingColumn: order_id
          shardingAlgorithmName: table_inline
    t_order_item: 
    # 省略配置 t_order_item 表规则...
    # ...
    
  # 配置分片算法
  shardingAlgorithms:
    database_inline:
      type: INLINE
      props:
        algorithm.expression: ds${user_id % 2}
    table_inline:
      type: INLINE
      props:
        algorithm.expression: t_order_${order_id % 2}
```

```java
// 创建 ShardingSphereDataSource
DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
```

## 使用 ShardingSphereDataSource

通过 YamlShardingSphereDataSourceFactory 工厂创建的 ShardingSphereDataSource 实现自 JDBC 的标准接口 DataSource。
可通过 DataSource 选择使用原生 JDBC，或JPA， MyBatis 等 ORM 框架。

```java
DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
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
