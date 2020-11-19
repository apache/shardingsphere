+++
title = "Use Spring Namespace"
weight = 4
+++

## Import Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-namespace</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

## Configure Rule

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
    <!-- Configure actual data sources -->
    <!-- Configure the first data source -->
    <bean id="ds0" class="org.apache.commons.dbcp2.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    <!-- Configure the second data source -->
    <bean id="ds1" class="org.apache.commons.dbcp2.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <!-- Configure database sharding strategy -->
    <sharding:sharding-algorithm id="dbShardingAlgorithm" type="INLINE">
           <props>
               <prop key="algorithm-expression">ds$->{user_id % 2}</prop>
           </props>
    </sharding:sharding-algorithm>
    <sharding:standard-strategy id="dbStrategy" sharding-column="user_id" algorithm-ref="dbShardingAlgorithm" />
       
    <!-- Configure table sharding strategy -->
    <sharding:sharding-algorithm id="tableShardingAlgorithm" type="INLINE">
           <props>
               <prop key="algorithm-expression">t_order$->{order_id % 2}</prop>
           </props>
    </sharding:sharding-algorithm>
    <sharding:standard-strategy id="tableStrategy" sharding-column="user_id" algorithm-ref="tableShardingAlgorithm" />
   
    <!-- Configure distributed key-generate strategy -->
    <sharding:key-generate-algorithm id="snowflakeAlgorithm" type="SNOWFLAKE">
              <props>
                  <prop key="worker-id">123</prop>
              </props>
    </sharding:key-generate-algorithm>   
    <sharding:key-generate-strategy id="orderKeyGenerator" column="order_id" algorithm-ref="snowflakeAlgorithm" />
   
    <!-- Configure sharding rule -->
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
       
    <!-- Configure ShardingSphereDataSource -->
    <shardingsphere:data-source id="shardingDataSource" data-source-names="ds0,ds1" rule-refs="shardingRule">
           <props>
               <prop key="sql-show">false</prop>
           </props>
    </shardingsphere:data-source>     

</beans>
```

## Use ShardingSphereDataSource in Spring

ShardingSphereDataSource can be used directly by injection; 
or configure ShardingSphereDataSource in ORM frameworks such as JPA or MyBatis.

```java
@Resource
private DataSource dataSource;
```
