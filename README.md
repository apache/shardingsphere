# [Sharding-JDBC - Distributed database middleware](http://shardingjdbc.io/)

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

[![Maven Status](https://maven-badges.herokuapp.com/maven-central/io.shardingjdbc/sharding-jdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.shardingjdbc/sharding-jdbc)
[![GitHub release](https://img.shields.io/github/release/shardingjdbc/sharding-jdbc.svg)](https://github.com/shardingjdbc/sharding-jdbc/releases)
[![Download](https://img.shields.io/badge/release-download-orange.svg)](https://github.com/shardingjdbc/sharding-jdbc-doc/raw/master/dist/sharding-jdbc-server-2.1.0-SNAPSHOT-assembly.tar.gz)

[![Build Status](https://secure.travis-ci.org/shardingjdbc/sharding-jdbc.png?branch=master)](https://travis-ci.org/shardingjdbc/sharding-jdbc)
[![Coverage Status](https://codecov.io/github/shardingjdbc/sharding-jdbc/coverage.svg?branch=master)](https://codecov.io/github/shardingjdbc/sharding-jdbc?branch=master)
[![Gitter](https://badges.gitter.im/Sharding-JDBC/shardingjdbc.svg)](https://gitter.im/Sharding-JDBC/shardingjdbc?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![OpenTracing-1.0 Badge](https://img.shields.io/badge/OpenTracing--1.0-enabled-blue.svg)](http://opentracing.io)
[![Skywalking Tracing](https://img.shields.io/badge/Skywalking%20Tracing-enable-brightgreen.svg)](https://github.com/OpenSkywalking/skywalking)

# Overview

Sharding-JDBC: A data sharding, read/write splitting, BASE transaction and database orchestration middleware. It provides maximum compatibilities for applications by JDBC and MySQL protocol.

# Document

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](http://shardingjdbc.io/docs_en/00-overview/)
[![Roadmap](https://img.shields.io/badge/roadmap-English-blue.svg)](ROADMAP.md)

[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](http://shardingjdbc.io/docs_cn/00-overview/)

# Features

## 1. Data sharding
* Both databases and tables sharding supported.
* Standard aggregation functions, GROUP BY, ORDER BY, LIMIT and JOIN DQL supported.
* Standard DML, DDL, TCL and database administrator command supported.
* Sharding operator `=`, `BETWEEN` and `IN` supported.
* Sharding algorithm customization and inline expression supported.
* Route by hint supported.
* Distributed sequence supported.

## 2. Read/write splitting
* Multiple slaves replica supported. 
* Data consistency guarantee in same thread supported.
* Mix read/write splitting and data sharding supported.
* Route by hint supported.

## 3. BASE Transaction
* Best efforts delivery transaction supported.
* Try confirm cancel transaction (TBD).

## 4. Orchestration
* Configuration center supported, can refresh dynamically.
* Circuit breaker and failover supported.
* Open tracing supported.

# Architecture

## Sharding-JDBC-Driver

Use JDBC connect databases without redirect cost for java application, best performance for production.

* ORM self-adapting. JPA, Hibernate, Mybatis, Spring JDBC Template or JDBC supported.
* Connection-pool self-adapting. DBCP, C3P0, BoneCP, Druid supported.
* Any Database supported theoretically. Support MySQL, Oracle, SQLServer and PostgreSQL right now.

## Sharding-JDBC-Server

Use proxy to connect databases(only MySQL protocol for now), for other programing language or MySQL client.

* Use standard MySQL protocol, application do not care about whether proxy or real MySQL.
* Any MySQL command line and UI workbench supported.

## Sharding-JDBC-Sidecar(TBD)

Use sidecar to connect databases, best for Kubernetes or Mesos together.

# Architecture

![Architecture](http://ovfotjrsi.bkt.clouddn.com/docs/img/architecture_en_v2.png)

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
    dataSource1.setDriverClassName("com.mysql.jdbc.Driver");
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

shardingRule:
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
