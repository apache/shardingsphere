# [Sharding-JDBC - 分布式数据库中间层](http://shardingjdbc.io/index_zh.html)

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

[![Maven Status](https://maven-badges.herokuapp.com/maven-central/io.shardingjdbc/sharding-jdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.shardingjdbc/sharding-jdbc)
[![GitHub release](https://img.shields.io/github/release/shardingjdbc/sharding-jdbc.svg)](https://github.com/shardingjdbc/sharding-jdbc/releases)
[![Download](https://img.shields.io/badge/release-download-orange.svg)](https://github.com/shardingjdbc/sharding-jdbc-doc/raw/master/dist/sharding-jdbc-server-2.1.0-SNAPSHOT-assembly.tar.gz)

[![Build Status](https://secure.travis-ci.org/shardingjdbc/sharding-jdbc.png?branch=master)](https://travis-ci.org/shardingjdbc/sharding-jdbc)
[![Coverage Status](https://codecov.io/github/shardingjdbc/sharding-jdbc/coverage.svg?branch=master)](https://codecov.io/github/shardingjdbc/sharding-jdbc?branch=master)
[![Gitter](https://badges.gitter.im/Sharding-JDBC/shardingjdbc.svg)](https://gitter.im/Sharding-JDBC/shardingjdbc?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![OpenTracing-1.0 Badge](https://img.shields.io/badge/OpenTracing--1.0-enabled-blue.svg)](http://opentracing.io)
[![Skywalking Tracing](https://img.shields.io/badge/Skywalking%20Tracing-enable-brightgreen.svg)](https://github.com/OpenSkywalking/skywalking)


# 概述

Sharding-JDBC是一个开源的分布式数据库中间件解决方案。它在Java的JDBC层以对业务应用零侵入的方式额外提供数据分片，读写分离，柔性事务和分布式治理能力。并在其基础上提供封装了MySQL协议的服务端版本，用于完成对异构语言的支持。

基于JDBC的客户端版本定位为轻量级Java框架，使用客户端直连数据库，以jar包形式提供服务，无需额外部署和依赖，可理解为增强版的JDBC驱动，完全兼容JDBC和各种ORM框架。

封装了MySQL协议的服务端版本定位为透明化的MySQL代理端，可以使用任何兼容MySQL协议的访问客户端(如：MySQL Command Client, MySQL Workbench等)操作数据，对DBA更加友好。

# 文档

[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](http://shardingjdbc.io/docs_cn/00-overview/)
[![Roadmap](https://img.shields.io/badge/roadmap-english-blue.svg)](ROADMAP.md)

# 功能列表

## 1. 数据分片
* 支持分库 + 分表
* 支持聚合，分组，排序，分页，关联查询等复杂查询语句
* 支持常见的DML，DDL，TCL以及数据库管理语句
* 支持=，BETWEEN，IN的分片操作符
* 自定义的灵活分片策略，支持多分片键共用，支持inline表达式
* 基于Hint的强制路由
* 支持分布式主键

## 2. 读写分离
* 支持一主多从的读写分离
* 支持同一线程内的数据一致性
* 支持分库分表与读写分离共同使用
* 支持基于Hint的强制主库路由

## 3. 柔性事务
* 最大努力送达型事务
* TCC型事务(TBD)

## 4. 分布式治理
* 支持配置中心，可动态修改配置
* 支持客户端熔断和失效转移
* 支持Open Tracing协议

# 部署架构

## Sharding-JDBC-Driver

通过客户端分片的方式由应用程序直连数据库，减少二次转发成本，性能最高，适合线上程序使用。

* 可适用于任何基于Java的ORM框架，如：JPA, Hibernate, Mybatis, Spring JDBC Template或直接使用JDBC
* 可基于任何第三方的数据库连接池，如：DBCP, C3P0, BoneCP, Druid等
* 可支持任意实现JDBC规范的数据库。目前支持MySQL，Oracle，SQLServer和PostgreSQL

## Sharding-JDBC-Server

通过代理服务器连接数据库(目前仅支持MySQL)，适合其他开发语言或MySQL客户端操作数据。

* 向应用程序完全透明，可直接当做MySQL使用
* 可适用于任何兼容MySQL协议的的客户端

## Sharding-JDBC-Sidecar(TBD)

通过sidecar分片的方式，由IPC代替RPC，自动代理SQL分片，适合与Kubernetes或Mesos配合使用。

# Architecture

![Architecture](http://ovfotjrsi.bkt.clouddn.com/docs/img/architecture_v2.png)

# [Roadmap](ROADMAP.md)


# 快速入门

## 引入maven依赖

```xml
<!-- 引入sharding-jdbc核心模块 -->
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

## 规则配置
Sharding-JDBC的分库分表通过规则配置描述，请简单浏览配置全貌：

```java
    // 配置真实数据源
    Map<String, DataSource> dataSourceMap = new HashMap<>();
    
    // 配置第一个数据源
    BasicDataSource dataSource1 = new BasicDataSource();
    dataSource1.setDriverClassName("com.mysql.jdbc.Driver");
    dataSource1.setUrl("jdbc:mysql://localhost:3306/ds_0");
    dataSource1.setUsername("root");
    dataSource1.setPassword("");
    dataSourceMap.put("ds_0", dataSource1);
    
    // 配置第二个数据源
    BasicDataSource dataSource2 = new BasicDataSource();
    dataSource2.setDriverClassName("com.mysql.jdbc.Driver");
    dataSource2.setUrl("jdbc:mysql://localhost:3306/ds_1");
    dataSource2.setUsername("root");
    dataSource2.setPassword("");
    dataSourceMap.put("ds_1", dataSource2);
    
    // 配置Order表规则
    TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
    orderTableRuleConfig.setLogicTable("t_order");
    orderTableRuleConfig.setActualDataNodes("ds_${0..1}.t_order_${[0, 1]}");
    
    // 配置分库策略
    orderTableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("user_id", "ds_${user_id % 2}"));
    
    // 配置分表策略
    orderTableRuleConfig.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id", "t_order_${order_id % 2}"));
    
    // 配置分片规则
    ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
    shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
    
    // 省略配置order_item表规则...
    
    // 获取数据源对象
    DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig);
```

或通过YAML方式配置，与以上配置等价：

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

规则配置包括数据源配置、表规则配置、分库策略和分表策略组成。这只是最简单的配置方式，实际使用可更加灵活，如：多分片键，分片策略直接和表规则配置绑定等。

## 使用原生JDBC接口
通过ShardingDataSourceFactory工厂和规则配置对象获取ShardingDataSource，ShardingDataSource实现自JDBC的标准接口DataSource。然后可通过DataSource选择使用原生JDBC开发，或者使用JPA, MyBatis等ORM工具。
以JDBC原生实现为例：

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

## 使用Spring命名空间配置

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
