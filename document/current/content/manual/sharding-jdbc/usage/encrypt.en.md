+++
toc = true
title = "数据脱敏"
weight = 6
+++

This chapter mainly introduce how to use the feather of Data Masking. On one hand User can use Data Masking and Sharding together, which will
create ShardingDataSource, On another hand, when user only adopt the feather of Data Masking, ShardingSphere will create EncryptDataSource.

## Not Use Spring

### Introduce Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>
```

### Rule Configuration Based on Java

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
       
       // 配置Order表规则 + 脱敏规则
       TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
       orderTableRuleConfig.setLogicTable("t_order");
       orderTableRuleConfig.setActualDataNodes("ds${0..1}.t_order${0..1}");
       orderTableRuleConfig.setEncryptorConfig(new EncryptorConfiguration("MD5", "status", new Properties()));
       
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

### Rule Configuration Based on Yaml

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
      encryptor:
        type: MD5
        columns: status
    t_order_encrypt:
       encryptor:
        type: QUERY
        columns: encrypt_id
        assistedQueryColumns: query_id
  bindingTables:
    - t_order,t_order_item,t_order_encrypt
```

```java
    DataSource dataSource = YamlOrchestrationShardingDataSourceFactory.createDataSource(yamlFile);
```

## Use Native JDBC

### Introduce Maven Dependency

```xml
<!-- for spring boot -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-jdbc-spring-boot-starter</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>

<!-- for spring namespace -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-jdbc-spring-namespace</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>
```

### Rule Configuration Based on Spring Boot

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
sharding.jdbc.config.sharding.tables.t_order_item.encryptor.type=MD5
sharding.jdbc.config.sharding.tables.t_order_item.encryptor.columns=status

sharding.jdbc.config.sharding.tables.t_order_encrypt.actual-data-nodes=ds_$->{0..1}.t_order_encrypt_$->{0..1}
sharding.jdbc.config.sharding.tables.t_order_encrypt.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t_order_encrypt.table-strategy.inline.algorithm-expression=t_order_encrypt_$->{order_id % 2}
sharding.jdbc.config.sharding.tables.t_order_encrypt.encryptor.type=QUERY
sharding.jdbc.config.sharding.tables.t_order_encrypt.encryptor.columns=encrypt_id
sharding.jdbc.config.sharding.tables.t_order_encrypt.encryptor.assistedQueryColumns=query_id
```

### Rule Configuration Based on Spring Name Space

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:sharding="http://shardingsphere.apache.org/schema/shardingsphere/sharding" 
    xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://shardingsphere.apache.org/schema/shardingsphere/sharding 
                        http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding.xsd 
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
    <sharding:inline-strategy id="orderEncryptTableStrategy" sharding-column="order_id" algorithm-expression="t_order_encrypt_${order_id % 2}" />

    <sharding:encryptor id="md5" type="MD5" columns="status" />
    <sharding:encryptor id="query" type="QUERY" columns="encrypt_id" assisted-query-columns="query_id" />
    
    <sharding:data-source id="shardingDataSource">
        <sharding:sharding-rule data-source-names="ds0,ds1">
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="ds$->{0..1}.t_order$->{0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderTableStrategy" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="ds$->{0..1}.t_order_item$->{0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderItemTableStrategy" encryptor-ref="md5" />
                <sharding:table-rule logic-table="t_order_encrypt" actual-data-nodes="demo_ds_${0..1}.t_order_encrypt_${0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderEncryptTableStrategy" encryptor-ref="query" />
            </sharding:table-rules>
        </sharding:sharding-rule>
    </sharding:data-source>
</beans>
```

### Use DataSource in Spring

直接通过注入的方式即可使用DataSource，或者将DataSource配置在JPA、Hibernate或MyBatis中使用。

```java
@Resource
private DataSource dataSource;
```