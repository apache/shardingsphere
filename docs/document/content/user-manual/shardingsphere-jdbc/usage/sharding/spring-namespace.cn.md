+++
title = "使用 Spring 命名空间"
weight = 4
+++

## 引入Maven依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-namespace</artifactId>
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
        <props>
            <prop key="algorithm-expression">ds$->{user_id % 2}</prop>
        </props>
    </sharding:sharding-algorithm>
    <sharding:standard-strategy id="dbStrategy" sharding-column="user_id" algorithm-ref="dbShardingAlgorithm" />
    
    <!-- 配置分表策略 -->
    <sharding:sharding-algorithm id="tableShardingAlgorithm" type="INLINE">
        <props>
            <prop key="algorithm-expression">t_order$->{order_id % 2}</prop>
        </props>
    </sharding:sharding-algorithm>
    <sharding:standard-strategy id="tableStrategy" sharding-column="user_id" algorithm-ref="tableShardingAlgorithm" />

    <!-- 配置分布式id生成策略 -->
    <sharding:key-generate-algorithm id="snowflakeAlgorithm" type="SNOWFLAKE">
           <props>
               <prop key="worker-id">123</prop>
           </props>
    </sharding:key-generate-algorithm>   
    <sharding:key-generate-strategy id="orderKeyGenerator" column="order_id" algorithm-ref="snowflakeAlgorithm" />

    <!-- 配置sharding策略 -->
    <sharding:rule id="shardingRule">
        <sharding:table-rules>
            <sharding:table-rule logic-table="t_order" actual-data-nodes="ds${0..1}.t_order_${0..1}" database-strategy-ref="dbStrategy" table-strategy-ref="tableStrategy" key-generate-strategy-ref="orderKeyGenerator" />
        </sharding:table-rules>
        <sharding:binding-table-rules>
            <sharding:binding-table-rule logic-tables="t_order,t_order_item"/>
        </sharding:binding-table-rules>
        <sharding:broadcast-table-rules>
            <sharding:broadcast-table-rule table="t_address"/>
        </sharding:broadcast-table-rules>
    </sharding:rule>
    
    <!-- 配置ShardingSphereDataSource -->
    <shardingsphere:data-source id="shardingDataSource" data-source-names="ds0, ds1" rule-refs="shardingRule">
        <props>
            <prop key="sql-show">false</prop>
        </props>
    </shardingsphere:data-source>
    
</beans>
```

## 在 Spring 中使用 ShardingSphereDataSource

直接通过注入的方式即可使用 ShardingSphereDataSource；或者将 ShardingSphereDataSource 配置在JPA， MyBatis 等 ORM 框架中配合使用。

```java
@Resource
private DataSource dataSource;
```
