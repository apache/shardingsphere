# Sharding-JDBC - 为分库分表而生的数据库访问层微服务框架 

[![Build Status](https://secure.travis-ci.org/dangdangdotcom/sharding-jdbc.png?branch=master)](https://travis-ci.org/dangdangdotcom/sharding-jdbc)
[![Maven Status](https://maven-badges.herokuapp.com/maven-central/com.dangdang/sharding-jdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.dangdang/sharding-jdbc)
[![Coverage Status](https://coveralls.io/repos/dangdangdotcom/sharding-jdbc/badge.svg?branch=master&service=github)](https://coveralls.io/github/dangdangdotcom/sharding-jdbc?branch=master)
[![GitHub release](https://img.shields.io/github/release/dangdangdotcom/sharding-jdbc.svg)](https://github.com/dangdangdotcom/sharding-jdbc/releases)
[![Hex.pm](http://dangdangdotcom.github.io/sharding-jdbc/img/license.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

# 概述

Sharding-JDBC定位为轻量级java框架，使用客户端直连数据库，以jar包形式提供服务，未使用中间层，无需额外部署，无其他依赖，DBA也无需改变原有的运维方式，可理解为增强版的JDBC驱动，旧代码迁移成本几乎为零。

# 功能列表

## 1. 分库分表
* SQL解析功能完善，支持聚合，分组，排序，LIMIT，OR等查询，并且支持级联表以及笛卡尔积的表查询
* 支持内、外连接查询
* 分片策略灵活，可支持=，BETWEEN，IN等多维度分片，也可支持多分片键共用，以及自定义分片策略
* 基于Hint的强制分库分表路由

## 2. 读写分离
* 一主多从的读写分离配置，可配合分库分表使用
* 基于Hint的强制主库路由

## 3. 分布式事务
* 最大努力送达型事务
* TCC型事务(TBD)

## 4. 兼容性
* 可适用于任何基于java的ORM框架，如：JPA, Hibernate, Mybatis, Spring JDBC Template或直接使用JDBC
* 可基于任何第三方的数据库连接池，如：DBCP, C3P0, BoneCP, Druid等
* 理论上可支持任意实现JDBC规范的数据库。目前支持MySQL，Oracle，SQLServer和PostgreSQL

## 5. 灵活多样的配置
* Java
* Spring命名空间
* YAML
* Inline表达式

## 6. 分布式生成全局主键
* 统一的分布式基于时间序列的ID生成器

# Architecture

![Architecture](http://dangdangdotcom.github.io/sharding-jdbc/img/architecture.png)

# [Release Notes](https://github.com/dangdangdotcom/sharding-jdbc/releases)

# [Roadmap](ROADMAP.md)


# 快速入门

## 引入maven依赖

```xml
<!-- 引入sharding-jdbc核心模块 -->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

## 规则配置
Sharding-JDBC的分库分表通过规则配置描述，请简单浏览配置全貌：

```java
ShardingRule shardingRule = ShardingRule.builder()
        .dataSourceRule(dataSourceRule)
        .tableRules(tableRuleList)
        .databaseShardingStrategy(new DatabaseShardingStrategy("sharding_column", new XXXShardingAlgorithm()))
        .tableShardingStrategy(new TableShardingStrategy("sharding_column", new XXXShardingAlgorithm())))
        .build();
```

规则配置包括数据源配置、表规则配置、分库策略和分表策略组成。这只是最简单的配置方式，实际使用可更加灵活，如：多分片键，分片策略直接和表规则配置绑定等。

## 使用原生JDBC接口
通过ShardingDataSourceFactory工厂和规则配置对象获取ShardingDataSource，ShardingDataSource实现自JDBC的标准接口DataSource。然后可通过DataSource选择使用原生JDBC开发，或者使用JPA, MyBatis等ORM工具。
以JDBC原生实现为例：

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

## 使用Spring命名空间配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:context="http://www.springframework.org/schema/context"
    xmlns:rdb="http://www.dangdang.com/schema/ddframe/rdb" 
    xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.springframework.org/schema/context 
                        http://www.springframework.org/schema/context/spring-context.xsd 
                        http://www.dangdang.com/schema/ddframe/rdb 
                        http://www.dangdang.com/schema/ddframe/rdb/rdb.xsd 
                        ">
    <context:property-placeholder location="classpath:conf/rdb/conf.properties" ignore-unresolvable="true"/>
    
    <bean id="dbtbl_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_0"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    <bean id="dbtbl_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_1"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>

    <rdb:strategy id="orderTableStrategy" sharding-columns="order_id" algorithm-expression="t_order_${order_id.longValue() % 4}"/>
    <rdb:strategy id="orderItemTableStrategy" sharding-columns="order_id" algorithm-expression="t_order_item_${order_id.longValue() % 4}"/>
    <rdb:data-source id="shardingDataSource">
        <rdb:sharding-rule data-sources="dbtbl_0,dbtbl_1">
            <rdb:table-rules>
                <rdb:table-rule logic-table="t_order" actual-tables="t_order_${0..3}" table-strategy="orderTableStrategy"/>
                <rdb:table-rule logic-table="t_order_item" actual-tables="t_order_item_${0..3}" table-strategy="orderItemTableStrategy"/>
            </rdb:table-rules>
            <rdb:default-database-strategy sharding-columns="none" algorithm-class="com.dangdang.ddframe.rdb.sharding.api.strategy.database.NoneDatabaseShardingAlgorithm"/>
        </rdb:sharding-rule>
    </rdb:data-source>
</beans>
```
