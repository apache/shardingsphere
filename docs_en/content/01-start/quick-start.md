+++
toc = true
title = "Quick Start"
weight = 1
prev = "/01-start/"
next = "/01-start/code-demo/"

+++

## To configure by using API

### The Maven Installation

```xml
<!-- imoort the core module of sharding-jdbc -->
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>${sharding-jdbc.version}</version>
</dependency>
```

## The rule configurtion

You can implement Sharding by configuring rules for Sharding-JDBC. The following example firstly takes the module of user_id to split databases and then takes the module of order_id to split tables. At last, two tables are in each of two databases.

To configure by JAVA codes:

```java
    // To configure the actual data source
    Map<String, DataSource> dataSourceMap = new HashMap<>();
    
    // To configure the first data source
    BasicDataSource dataSource1 = new BasicDataSource();
    dataSource1.setDriverClassName("com.mysql.jdbc.Driver");
    dataSource1.setUrl("jdbc:mysql://localhost:3306/ds_0");
    dataSource1.setUsername("root");
    dataSource1.setPassword("");
    dataSourceMap.put("ds_0", dataSource1);
    
    // To configure the second data source
    BasicDataSource dataSource2 = new BasicDataSource();
    dataSource2.setDriverClassName("com.mysql.jdbc.Driver");
    dataSource2.setUrl("jdbc:mysql://localhost:3306/ds_1");
    dataSource2.setUsername("root");
    dataSource2.setPassword("");
    dataSourceMap.put("ds_1", dataSource2);
    
    // To configure the table rules for Order
    TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
    orderTableRuleConfig.setLogicTable("t_order");
    orderTableRuleConfig.setActualDataNodes("ds_${0..1}.t_order_${0..1}");
    
    // To configure the strategy for database Sharding. 
    orderTableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("user_id", "ds_${user_id % 2}"));
    
    // To configure the strategy for table Sharding.
    orderTableRuleConfig.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id", "t_order_${order_id % 2}"));
    
    // To configure the strategy rule of Sharding
    ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
    shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
    
    // To configure the table rules for order_item
    
    ...
    
    // To get the data source object
    DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, new ConcurrentHashMap(), new Properties());
```

To configure by YAML, similar with the configuration method of JAVA codes:

```yaml
dataSources:
  ds_0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_0
    username: root
    password: 
  ds_1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_1
    username: root
    password: 

tables:
  t_order: 
    actualDataNodes: ds_${0..1}.t_order_${0..1}
    databaseStrategy: 
      inline:
        shardingColumn: user_id
        algorithmInlineExpression: ds_$_${user_id % 2}
    tableStrategy: 
      inline:
        shardingColumn: order_id
        algorithmInlineExpression: t_order_${order_id % 2}
  t_order_item: 
    actualDataNodes: ds_${0..1}.t_order_item_${0..1}
    databaseStrategy: 
      inline:
        shardingColumn: user_id
        algorithmInlineExpression: ds_$_${user_id % 2}
    tableStrategy: 
      inline:
        shardingColumn: order_id
        algorithmInlineExpression: t_order_item_${order_id % 2}
```

```java
    DataSource dataSource = ShardingDataSourceFactory.createDataSource(yamlFile);
```
The rule configuration consists of data source configuration, table rule configuration, database Sharding strategy and table Sharding strategy, etc. Here is a simple configuration example, more flexible configurations can be used in product environment, e.g. multi-Sharding columns, table rules configuration directly bound with Sharding strategy.

> To learn the details of the Sharding configuration, please refer to [Sharding](/02-guide/sharding).

## To use JDBC interface based on ShardingDataSource

By using ShardingDataSourceFactory factory class and rule configuration object, we can obtain ShardingDataSource which implements the standard interface of DataSource in JDBC. Thus you can choose to use native JDBC DataSource for development, or using JPA, MyBatis ORM tools, etc.

Take native DataSource in JDBC as an example:

```java
DataSource dataSource = ShardingDataSourceFactory.createDataSource(shardingRule);
String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=? AND o.order_id=?";
try (
        Connection conn = dataSource.getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
    preparedStatement.setInt(1, 10);
    preparedStatement.setInt(2, 1001);
    try (ResultSet rs = preparedStatement.executeQuery()) {
        while(rs.next()) {
            System.out.println(rs.getInt(1));
            System.out.println(rs.getInt(2));
        }
    }
}
```

##  To configure by using Spring

### The Maven Installation

```xml
<!-- imoort the core module of sharding-jdbc -->
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-core-spring-namespace</artifactId>
    <version>${sharding-jdbc.version}</version>
</dependency>
```
### The Spring namespace

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:context="http://www.springframework.org/schema/context"
    xmlns:sharding="http://shardingjdbc.io/schema/shardingjdbc/sharding" 
    xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.springframework.org/schema/context 
                        http://www.springframework.org/schema/context/spring-context.xsd 
                        http://shardingjdbc.io/schema/shardingjdbc/sharding 
                        http://shardingjdbc.io/schema/shardingjdbc/sharding/sharding.xsd 
                        ">
    <context:property-placeholder location="classpath:conf/conf.properties" ignore-unresolvable="true" />
    
    <bean id="ds_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    <bean id="ds_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <sharding:inline-strategy id="databaseStrategy" sharding-column="user_id" algorithm-expression="ds_${user_id % 2}" />
    <sharding:inline-strategy id="orderTableStrategy" sharding-column="order_id" algorithm-expression="t_order_${order_id % 2}" />
    <sharding:inline-strategy id="orderItemTableStrategy" sharding-column="order_id" algorithm-expression="t_order_item_${order_id % 2}" />
    
    <sharding:data-source id="shardingDataSource">
        <sharding:sharding-rule data-source-names="ds_0,ds_1">
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="ds_${0..1}.t_order_${0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderTableStrategy" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="ds_${0..1}.t_order_item_${0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderItemTableStrategy" />
            </sharding:table-rules>
        </sharding:sharding-rule>
    </sharding:data-source>
</beans>
```

> To learn the details of the rule configuration, please refer to [Configuration](/02-guide/configuration).
