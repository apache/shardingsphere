+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "快速入门"
weight = 1
prev = "/01-start/index"
next = "/01-start/faq"

+++

## 使用API配置

### Maven安装 

```xml
<!-- 引入sharding-jdbc核心模块 -->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>${sharding-jdbc.version}</version>
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

>详细的规则配置请参考[分库分表](/02-guide/sharding)

## 使用基于ShardingDataSource的JDBC接口

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

## 使用Spring

### Maven安装

```xml
<!-- 引入sharding-jdbc核心模块 -->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>sharding-jdbc-config-spring</artifactId>
    <version>${sharding-jdbc.version}</version>
</dependency>
```
### Spring命名空间

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
>详细的规则配置请参考[配置手册](/02-guide/configuration)