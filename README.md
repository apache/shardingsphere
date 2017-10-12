# Sharding-JDBC - JDBC driver for shard databases and tables 

# [Homepage](http://shardingjdbc.io/)

# [中文主页](http://shardingjdbc.io/index_zh.html)

[![Build Status](https://secure.travis-ci.org/shardingjdbc/sharding-jdbc.png?branch=master)](https://travis-ci.org/dangdangdotcom/sharding-jdbc)
[![Maven Status](https://maven-badges.herokuapp.com/maven-central/com.dangdang/sharding-jdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.dangdang/sharding-jdbc)
[![Coverage Status](https://coveralls.io/repos/shardingjdbc/sharding-jdbc/badge.svg?branch=master&service=github)](https://coveralls.io/github/shardingjdbc/sharding-jdbc?branch=master)
[![GitHub release](https://img.shields.io/github/release/shardingjdbc/sharding-jdbc.svg)](https://github.com/shardingjdbc/sharding-jdbc/releases)
[![Hex.pm](http://shardingjdbc.github.io/sharding-jdbc/img/license.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

# Overview

Sharding-JDBC is a JDBC extension, provides distributed features such as sharding, read/write splitting, BASE transaction and database orchestration.

# Features

## 1. Sharding
* Aggregation functions, group by, order by and limit SQL supported in distributed database.
* Join (inner/outer) query supported.
* Sharding operator `=`, `BETWEEN` and `IN` supported.
* Sharding algorithm customization supported.
* Hint supported.

## 2. Read/Write Splitting
* Same transaction data concurrency guarantee.
* Hint supported.

## 3. BASE Transaction
* Best efforts delivery transaction.
* Try confirm cancel transaction (TBD).

## 6. Distributed ID Generation
* Distributed Unique Time-Sequence Generation

## 5. Compatibility
* ORM self-adapting. JPA, Hibernate, Mybatis, Spring JDBC Template or JDBC supported.
* Connection-pool self-adapting. DBCP, C3P0, BoneCP, Druid supported.
* Any Database supported theoretically. Support MySQL, Oracle, SQLServer and PostgreSQL.

## 6. Configuration
* Java config
* Spring namespace
* YAML
* Inline expression

## 7. Orchestration (new feature for 2.0)
* Configuration center, can support data sources, tables and sharding strategies switch dynamically. (2.0.0.M1)
* Smart client to orchestrate data access service, can failover automatically (2.0.0.M2)
* Output apm information based on open tracing protocol (2.0.0.M3)

# Architecture

![Architecture](http://ovfotjrsi.bkt.clouddn.com/docs/img/architecture_en.png)

# [Release Notes](https://github.com/shardingjdbc/sharding-jdbc/releases)

# [Roadmap](ROADMAP.md)

# Quick Start

## Add maven dependency

```xml
<!-- import sharding-jdbc core -->
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>

<!-- import other module if need -->
```

## Rule configuration

```java
    Map<String, DataSource> dataSourceMap = new HashMap<>();
    
    BasicDataSource dataSource1 = new BasicDataSource();
    dataSource2.setDriverClassName("com.mysql.jdbc.Driver");
    dataSource1.setUrl("jdbc:mysql://localhost:3306/ds_0");
    dataSource1.setUsername("root");
    dataSource1.setPassword("");
    dataSourceMap.put("ds_0", dataSource1);
    
    BasicDataSource dataSource2 = new BasicDataSource();
    dataSource2.setDriverClassName("com.mysql.jdbc.Driver");
    dataSource2.setUrl("jdbc:mysql://localhost:3306/ds_1");
    dataSource2.setUsername("root");
    dataSource2.setPassword("");
    dataSourceMap.put("ds_1", dataSource2);
    
    TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
    orderTableRuleConfig.setLogicTable("t_order");
    orderTableRuleConfig.setActualDataNodes("ds_${0..1}.t_order_${[0, 1]}");
    
    orderTableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("user_id", "ds_${user_id % 2}"));
    orderTableRuleConfig.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id", "t_order_${order_id % 2}"));
    
    ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
    shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
    
    // config order_item table rule...
    
    DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig);
```

Or use yaml to configure:

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
        algorithmExpression: ds_${user_id % 2}
    tableStrategy: 
      inline:
        shardingColumn: order_id
        algorithmExpression: t_order_${order_id % 2}
  t_order_item: 
    actualDataNodes: ds_${0..1}.t_order_item_${0..1}
    databaseStrategy: 
      inline:
        shardingColumn: user_id
        algorithmExpression: ds_${user_id % 2}
    tableStrategy: 
      inline:
        shardingColumn: order_id
        algorithmExpression: t_order_item_${order_id % 2}
```

```java
    DataSource dataSource = ShardingDataSourceFactory.createDataSource(yamlFile);
```

## Use raw JDBC API

```java
DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig);
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

## Use spring namespace

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
