# Sharding-JDBC - 为分库分表而生的数据库访问层微服务框架 

[![Build Status](https://secure.travis-ci.org/dangdangdotcom/sharding-jdbc.png?branch=master)](https://travis-ci.org/dangdangdotcom/sharding-jdbc)
[![Maven Status](https://maven-badges.herokuapp.com/maven-central/com.dangdang/sharding-jdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.dangdang/sharding-jdbc)
[![Coverage Status](https://coveralls.io/repos/shardingjdbc/sharding-jdbc/badge.svg?branch=master&service=github)](https://coveralls.io/github/shardingjdbc/sharding-jdbc?branch=master)
[![GitHub release](https://img.shields.io/github/release/shardingjdbc/sharding-jdbc.svg)](https://github.com/shardingjdbc/sharding-jdbc/releases)
[![Hex.pm](http://shardingjdbc.github.io/sharding-jdbc/img/license.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)


# [中文主页](http://shardingjdbc.io/index_zh.html)

# 概述

Sharding-JDBC定位为轻量级java框架，使用客户端直连数据库，以jar包形式提供服务，未使用中间层，无需额外部署，无其他依赖，DBA也无需改变原有的运维方式，可理解为增强版的JDBC驱动，旧代码迁移成本几乎为零。

# 功能列表

## 1. 分库分表
* SQL解析功能完善，支持聚合，分组，排序，LIMIT，TOP等查询，并且支持级联表以及笛卡尔积的表查询
* 支持内、外连接查询
* 分片策略灵活，可支持=，BETWEEN，IN等多维度分片，也可支持多分片键共用，以及自定义分片策略
* 基于Hint的强制分库分表路由

## 2. 读写分离
* 一主多从的读写分离配置，可配合分库分表使用
* 基于Hint的强制主库路由

## 3. 柔性事务
* 最大努力送达型事务
* TCC型事务(TBD)

## 4. 分布式主键
* 统一的分布式基于时间序列的ID生成器

## 5. 兼容性
* 可适用于任何基于java的ORM框架，如：JPA, Hibernate, Mybatis, Spring JDBC Template或直接使用JDBC
* 可基于任何第三方的数据库连接池，如：DBCP, C3P0, BoneCP, Druid等
* 理论上可支持任意实现JDBC规范的数据库。目前支持MySQL，Oracle，SQLServer和PostgreSQL

## 6. 灵活多样的配置
* Java
* YAML
* Inline表达式
* Spring命名空间
* Spring boot starter

## 7. 分布式治理能力 (2.0新功能)
* 配置集中化与动态化，可支持数据源、表与分片策略的动态切换(2.0.0.M1)
* 客户端的数据库治理，数据源失效自动切换(2.0.0.M2)
* 基于Open Tracing协议的APM信息输出(2.0.0.M3)

# Architecture

![Architecture](http://ovfotjrsi.bkt.clouddn.com/docs/img/architecture.png)

# [Release Notes](https://github.com/shardingjdbc/sharding-jdbc/releases)

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
    dataSource2.setDriverClassName("com.mysql.jdbc.Driver");
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
