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

Note: The example connection pool is HikariCP, which can be replaced with other connection pools according to business scenarios.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:shardingsphere="http://shardingsphere.apache.org/schema/shardingsphere/datasource"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://shardingsphere.apache.org/schema/shardingsphere/datasource
                        http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource.xsd">
    <!-- Configure actual data sources -->
    <!-- Configure the 1st data source -->
    <bean id="ds1" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/ds1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    <!-- Configure the 2nd data source -->
    <bean id="ds2" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/ds2" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>

    <!-- Please reference concentrate rule configurations -->

    <!-- Configure ShardingSphereDataSource -->
    <shardingsphere:data-source id="shardingDataSource" data-source-names="ds0, ds1" rule-refs="fooRule, barRule" />
</beans>
```

## Use ShardingSphere Data Source in Spring

Same with Spring Boot Starter.
