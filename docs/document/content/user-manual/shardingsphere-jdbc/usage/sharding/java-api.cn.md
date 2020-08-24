+++
title = "使用 Java API"
weight = 1
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

ShardingSphere-JDBC 的 Java API 通过数据源集合、规则集合以及属性配置组成。
以下示例是根据 `user_id` 取模分库, 且根据 `order_id` 取模分表的 2 库 2 表的配置。

```java

// 配置真实数据源
Map<String, DataSource> dataSourceMap = new HashMap<>();

// 配置第 1 个数据源
BasicDataSource dataSource1 = new BasicDataSource();
dataSource1.setDriverClassName("com.mysql.jdbc.Driver");
dataSource1.setUrl("jdbc:mysql://localhost:3306/ds0");
dataSource1.setUsername("root");
dataSource1.setPassword("");
dataSourceMap.put("ds0", dataSource1);

// 配置第 2 个数据源
BasicDataSource dataSource2 = new BasicDataSource();
dataSource2.setDriverClassName("com.mysql.jdbc.Driver");
dataSource2.setUrl("jdbc:mysql://localhost:3306/ds1");
dataSource2.setUsername("root");
dataSource2.setPassword("");
dataSourceMap.put("ds1", dataSource2);

// 配置 t_order 表规则
ShardingTableRuleConfiguration orderTableRuleConfig = new ShardingTableRuleConfiguration("t_order", "ds${0..1}.t_order${0..1}");

// 配置分库策略
orderTableRuleConfig.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "dbShardingAlgorithm"));

// 配置分表策略
orderTableRuleConfig.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "tableShardingAlgorithm"));

// 省略配置 t_order_item 表规则...
// ...

// 配置分片规则
ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
shardingRuleConfig.getTables().add(orderTableRuleConfig);

// 配置分库算法
Properties dbShardingAlgorithmrProps = new Properties();
dbShardingAlgorithmrProps.setProperty("algorithm.expression", "ds${user_id % 2}");
shardingRuleConfig.getShardingAlgorithms().put("dbShardingAlgorithm", new ShardingSphereAlgorithmConfiguration("INLINE", dbShardingAlgorithmrProps));

// 配置分表算法
Properties tableShardingAlgorithmrProps = new Properties();
tableShardingAlgorithmrProps.setProperty("algorithm.expression", "t_order${order_id % 2}");
shardingRuleConfig.getShardingAlgorithms().put("tableShardingAlgorithm", new ShardingSphereAlgorithmConfiguration("INLINE", tableShardingAlgorithmrProps));

// 创建 ShardingSphereDataSource
DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Collections.singleton((shardingRuleConfig), new Properties());
```

## 使用 ShardingSphereDataSource

通过 ShardingSphereDataSourceFactory 工厂创建的 ShardingSphereDataSource 实现自 JDBC 的标准接口 DataSource。
可通过 DataSource 选择使用原生 JDBC，或JPA， MyBatis 等 ORM 框架。

以原生 JDBC 使用方式为例：

```java
DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Collections.singleton((shardingRuleConfig), new Properties());
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
