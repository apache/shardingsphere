+++
title = "使用 Java API"
weight = 1
+++

## 数据分片

数据分片是 Apache ShardingSphere 的基础能力，本节以数据分片的使用举例。
除数据分片之外，读写分离、多副本、数据加密、影子库压测等功能的使用方法完全一致，只要配置相应的规则即可。多规则可以叠加配置。

详情请参见[配置手册](/cn/user-manual/shardingsphere-jdbc/configuration/config-java/)。

### 引入 Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### 规则配置

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
TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration("t_order", "ds${0..1}.t_order${0..1}");

// 配置分库策略
StandardShardingAlgorithm dbShardingAlgorithm = new InlineShardingAlgorithm();
Properties dbProps = new Properties();
dbProps.setProperty(InlineShardingAlgorithm.ALGORITHM_EXPRESSION, "ds${user_id % 2}");
dbShardingAlgorithm.setProperties(dbProps);
StandardShardingStrategyConfiguration dbShardingStrategyConfig = new StandardShardingStrategyConfiguration("user_id", dbShardingAlgorithm);
orderTableRuleConfig.setDatabaseShardingStrategyConfig(dbShardingStrategyConfig);

// 配置分表策略
StandardShardingAlgorithm tableShardingAlgorithm = new InlineShardingAlgorithm();
Properties tableProps = new Properties();
tableProps.setProperty(InlineShardingAlgorithm.ALGORITHM_EXPRESSION, "t_order${order_id % 2}");
tableShardingAlgorithm.setProperties(tableProps);
StandardShardingStrategyConfiguration tableShardingStrategyConfig = new StandardShardingStrategyConfiguration("order_id", tableShardingAlgorithm);
orderTableRuleConfig.setTableShardingStrategyConfig(tableShardingStrategyConfig);

// 省略配置 t_order_item 表规则...
// ...

// 配置分片规则
ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
    
// 创建 ShardingSphereDataSource
DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Collections.singleton((shardingRuleConfig), new Properties());
```

### 使用 ShardingSphereDataSource

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
    ps.setInt(2, 1001);
    try (ResultSet rs = preparedStatement.executeQuery()) {
        while(rs.next()) {
            // ...
        }
    }
}
```

## 分布式事务

通过 Apache ShardingSphere 使用分布式事务，与本地事务并无区别。
除了透明化分布式事务的使用之外，Apache ShardingSphere 还能够在每次数据库访问时切换分布式事务类型。
支持的事务类型包括 本地事务、XA事务 和 柔性事务。可在创建数据库连接之前设置，缺省为 Apache ShardingSphere 启动时的默认事务类型。

### 引入 Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- 使用XA事务时，需要引入此模块 -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-transaction-xa-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- 使用BASE事务时，需要引入此模块 -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-transaction-base-seata-at</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### 使用分布式事务

```java
TransactionTypeHolder.set(TransactionType.XA); // 支持 TransactionType.LOCAL, TransactionType.XA, TransactionType.BASE
try (Connection conn = dataSource.getConnection()) { // 使用 ShardingSphereDataSource
    conn.setAutoCommit(false);
    PreparedStatement ps = conn.prepareStatement("INSERT INTO t_order (user_id, status) VALUES (?, ?)");
    ps.setObject(1, 1000);
    ps.setObject(2, "init");
    ps.executeUpdate();
    conn.commit();
}
```
