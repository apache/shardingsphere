+++
title = "使用 Spring 命名空间"
weight = 4
+++

## 引入 Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-namespace</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

## 规则配置

注：示例的数据库连接池为 HikariCP，可根据业务场景更换为其他数据库连接池。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:shardingsphere="http://shardingsphere.apache.org/schema/shardingsphere/datasource"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://shardingsphere.apache.org/schema/shardingsphere/datasource
                        http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource.xsd">
    <!-- 配置真实数据源 -->
    <!-- 配置第 1 个数据源 -->
    <bean id="ds1" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/ds1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    <!-- 配置第 2 个数据源 -->
    <bean id="ds2" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/ds2" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <!-- 规则请参见具体配置 -->
    
    <!-- 配置 ShardingSphereDataSource -->
    <shardingsphere:data-source id="shardingDataSource" data-source-names="ds1, ds2" rule-refs="fooRule, barRule" />
</beans>
```

## 在 Spring 中使用 ShardingSphere 数据源

使用方式同 Spring Boot Starter。
