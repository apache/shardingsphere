+++
title = "使用 Spring 命名空间"
weight = 4
+++

## 引入Maven依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-spring-namespace</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

## 规则配置

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
    <!-- 配置真实数据源 -->
    <!-- 配置第 1 个数据源 -->
    <bean id="ds0" class="org.apache.commons.dbcp2.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    <!-- 配置第 2 个数据源 -->
    <bean id="ds1" class="org.apache.commons.dbcp2.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <!-- 配置分库策略 -->
    <sharding:sharding-algorithm id="dbShardingAlgorithm" type="INLINE">
        <properties>
            <prop key="algorithm.expression">ds$->{user_id % 2}</prop>
        </properties>
    </sharding:sharding-algorithm>
    <sharding:standard-strategy id="dbStrategy" sharding-column="user_id" algorithm-ref="dbShardingAlgorithm" />
    
    <!-- 配置分表策略 -->
    <sharding:sharding-algorithm id="tableShardingAlgorithm" type="INLINE">
        <properties>
            <prop key="algorithm.expression">t_order$->{order_id % 2}</prop>
        </properties>
    </sharding:sharding-algorithm>
    <sharding:standard-strategy id="tableStrategy" sharding-column="user_id" algorithm-ref="tableShardingAlgorithm" />
    
    <!-- 配置ShardingSphereDataSource -->
    <sharding:data-source id="shardingDataSource">
        <!-- 配置分片规则 -->
        <sharding:sharding-rule data-source-names="ds0,ds1">
            <sharding:table-rules>
                <!-- 配置 t_order 表规则 -->
                <sharding:table-rule logic-table="t_order" actual-data-nodes="ds$->{0..1}.t_order$->{0..1}" database-strategy-ref="dbStrategy" table-strategy-ref="tableStrategy" />
                <!-- 省略配置 t_order_item 表规则... -->
                <!-- ... -->
            </sharding:table-rules>
        </sharding:sharding-rule>
    </sharding:data-source>
</beans>
```

## 在 Spring 中使用 ShardingSphereDataSource

直接通过注入的方式即可使用 ShardingSphereDataSource；或者将 ShardingSphereDataSource 配置在JPA， MyBatis 等 ORM 框架中配合使用。

```java
@Resource
private DataSource dataSource;
```
