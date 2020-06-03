+++
title = "Use Java API"
weight = 1
+++

## Import Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

## Configure Rule

ShardingSphere-JDBC Java API consists of data sources, rules and properties configuration.
The following example is the configuration of 2 databases and 2 tables, 
whose databases take module and split according to `order_id`, tables take module and split according to `order_id`.

```java

// Configure actual data sources
Map<String, DataSource> dataSourceMap = new HashMap<>();

// Configure the first data source
BasicDataSource dataSource1 = new BasicDataSource();
dataSource1.setDriverClassName("com.mysql.jdbc.Driver");
dataSource1.setUrl("jdbc:mysql://localhost:3306/ds0");
dataSource1.setUsername("root");
dataSource1.setPassword("");
dataSourceMap.put("ds0", dataSource1);

// Configure the second data source
BasicDataSource dataSource2 = new BasicDataSource();
dataSource2.setDriverClassName("com.mysql.jdbc.Driver");
dataSource2.setUrl("jdbc:mysql://localhost:3306/ds1");
dataSource2.setUsername("root");
dataSource2.setPassword("");
dataSourceMap.put("ds1", dataSource2);

// Configure order table rule
ShardingTableRuleConfiguration orderTableRuleConfig = new ShardingTableRuleConfiguration("t_order", "ds${0..1}.t_order${0..1}");

// Configure database sharding strategy
StandardShardingAlgorithm dbShardingAlgorithm = new InlineShardingAlgorithm();
Properties dbProps = new Properties();
dbProps.setProperty(InlineShardingAlgorithm.ALGORITHM_EXPRESSION, "ds${user_id % 2}");
dbShardingAlgorithm.setProperties(dbProps);
StandardShardingStrategyConfiguration dbShardingStrategyConfig = new StandardShardingStrategyConfiguration("user_id", dbShardingAlgorithm);
orderTableRuleConfig.setDatabaseShardingStrategy(dbShardingStrategyConfig);

// Configure table sharding strategy
StandardShardingAlgorithm tableShardingAlgorithm = new InlineShardingAlgorithm();
Properties tableProps = new Properties();
tableProps.setProperty(InlineShardingAlgorithm.ALGORITHM_EXPRESSION, "t_order${order_id % 2}");
tableShardingAlgorithm.setProperties(tableProps);
StandardShardingStrategyConfiguration tableShardingStrategy = new StandardShardingStrategyConfiguration("order_id", tableShardingAlgorithm);
orderTableRuleConfig.setTableShardingStrategy(tableShardingStrategy);

// Omit t_order_item table rule configuration ...
// ...
    
// Configure sharding rule
ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
shardingRuleConfig.getTables().add(orderTableRuleConfig);

// Create ShardingSphereDataSource
DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Collections.singleton((shardingRuleConfig), new Properties());
```

## Use ShardingSphereDataSource

The ShardingSphereDataSource created by ShardingSphereDataSourceFactory implements the standard JDBC DataSource interface.
Developer can choose to use native JDBC or ORM frameworks such as JPA or MyBatis through the DataSource.

Take native JDBC usage as an example:

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
