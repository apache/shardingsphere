+++
toc = true
title = "数据分片"
weight = 1
+++

## 不使用Spring

### 引入Maven依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>
```

### 基于Java编码的规则配置

Sharding-JDBC的分库分表通过规则配置描述，以下例子是根据user_id取模分库, 且根据order_id取模分表的两库两表的配置。

```java
    // 配置真实数据源
    Map<String, DataSource> dataSourceMap = new HashMap<>();
    
    // 配置第一个数据源
    BasicDataSource dataSource1 = new BasicDataSource();
    dataSource1.setDriverClassName("com.mysql.jdbc.Driver");
    dataSource1.setUrl("jdbc:mysql://localhost:3306/ds0");
    dataSource1.setUsername("root");
    dataSource1.setPassword("");
    dataSourceMap.put("ds0", dataSource1);
    
    // 配置第二个数据源
    BasicDataSource dataSource2 = new BasicDataSource();
    dataSource2.setDriverClassName("com.mysql.jdbc.Driver");
    dataSource2.setUrl("jdbc:mysql://localhost:3306/ds1");
    dataSource2.setUsername("root");
    dataSource2.setPassword("");
    dataSourceMap.put("ds1", dataSource2);
    
    // 配置Order表规则
    TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
    orderTableRuleConfig.setLogicTable("t_order");
    orderTableRuleConfig.setActualDataNodes("ds${0..1}.t_order${0..1}");
    
    // 配置分库 + 分表策略
    orderTableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("user_id", "ds${user_id % 2}"));
    orderTableRuleConfig.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id", "t_order${order_id % 2}"));
    
    // 配置分片规则
    ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
    shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
    
    // 省略配置order_item表规则...
    // ...
    
    // 获取数据源对象
    DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, new ConcurrentHashMap(), new Properties());
```

### 基于Yaml的规则配置

或通过Yaml方式配置，与以上配置等价：

```yaml
dataSources:
  ds0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds0
    username: root
    password: 
  ds1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds1
    username: root
    password: 

tables:
  t_order: 
    actualDataNodes: ds${0..1}.t_order${0..1}
    databaseStrategy: 
      inline:
        shardingColumn: user_id
        algorithmInlineExpression: ds${user_id % 2}
    tableStrategy: 
      inline:
        shardingColumn: order_id
        algorithmInlineExpression: t_order${order_id % 2}
  t_order_item: 
    actualDataNodes: ds${0..1}.t_order_item${0..1}
    databaseStrategy: 
      inline:
        shardingColumn: user_id
        algorithmInlineExpression: ds${user_id % 2}
    tableStrategy: 
      inline:
        shardingColumn: order_id
        algorithmInlineExpression: t_order_item${order_id % 2}
```

```java
    DataSource dataSource = YamlShardingDataSourceFactory.createDataSource(yamlFile);
```

### 使用原生JDBC

通过ShardingDataSourceFactory或者YamlShardingDataSourceFactory工厂和规则配置对象获取ShardingDataSource，ShardingDataSource实现自JDBC的标准接口DataSource。然后可通过DataSource选择使用原生JDBC开发，或者使用JPA, MyBatis等ORM工具。
以JDBC原生实现为例：

```java
DataSource dataSource = YamlShardingDataSourceFactory.createDataSource(yamlFile);
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

## 使用Spring

### 引入Maven依赖

```xml
<!-- for spring boot -->
<dependency>
    <groupId>io.shardingsphere</groupId>
    <artifactId>sharding-jdbc-spring-boot-starter</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>

<!-- for spring namespace -->
<dependency>
    <groupId>io.shardingsphere</groupId>
    <artifactId>sharding-jdbc-spring-namespace</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>
```

### 基于Spring boot的规则配置

```properties
sharding.jdbc.datasource.names=ds0,ds1

sharding.jdbc.datasource.ds0.type=org.apache.commons.dbcp2.BasicDataSource
sharding.jdbc.datasource.ds0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds0.url=jdbc:mysql://localhost:3306/ds0
sharding.jdbc.datasource.ds0.username=root
sharding.jdbc.datasource.ds0.password=

sharding.jdbc.datasource.ds1.type=org.apache.commons.dbcp2.BasicDataSource
sharding.jdbc.datasource.ds1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds1.url=jdbc:mysql://localhost:3306/ds1
sharding.jdbc.datasource.ds1.username=root
sharding.jdbc.datasource.ds1.password=

sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column=user_id
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-expression=ds$->{user_id % 2}

sharding.jdbc.config.sharding.tables.t_order.actual-data-nodes=ds$->{0..1}.t_order$->{0..1}
sharding.jdbc.config.sharding.tables.t_order.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t_order.table-strategy.inline.algorithm-expression=t_order$->{order_id % 2}

sharding.jdbc.config.sharding.tables.t_order_item.actual-data-nodes=ds$->{0..1}.t_order_item$->{0..1}
sharding.jdbc.config.sharding.tables.t_order_item.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t_order_item.table-strategy.inline.algorithm-expression=t_order_item$->{order_id % 2}
```

### 基于Spring命名空间的规则配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:sharding="http://shardingsphere.io/schema/shardingsphere/sharding" 
    xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://shardingsphere.io/schema/shardingsphere/sharding 
                        http://shardingsphere.io/schema/shardingsphere/sharding/sharding.xsd 
                        ">
    <bean id="ds0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    <bean id="ds1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <sharding:inline-strategy id="databaseStrategy" sharding-column="user_id" algorithm-expression="ds$->{user_id % 2}" />
    <sharding:inline-strategy id="orderTableStrategy" sharding-column="order_id" algorithm-expression="t_order$->{order_id % 2}" />
    <sharding:inline-strategy id="orderItemTableStrategy" sharding-column="order_id" algorithm-expression="t_order_item$->{order_id % 2}" />
    
    <sharding:data-source id="shardingDataSource">
        <sharding:sharding-rule data-source-names="ds0,ds1">
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="ds$->{0..1}.t_order$->{0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderTableStrategy" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="ds$->{0..1}.t_order_item$->{0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderItemTableStrategy" />
            </sharding:table-rules>
        </sharding:sharding-rule>
    </sharding:data-source>
</beans>
```

### 在Spring中使用DataSource

直接通过注入的方式即可使用DataSource，或者将DataSource配置在JPA、Hibernate或MyBatis中使用。

```java
@Resource
private DataSource dataSource;
```

规则配置包括数据源配置、表规则配置、分库策略和分表策略组成。这只是最简单的配置方式，实际使用可更加灵活，如：多分片键，分片策略直接和表规则配置绑定等。更多的详细配置请参考[配置手册](/cn/manual/sharding-jdbc/configuration/)。
