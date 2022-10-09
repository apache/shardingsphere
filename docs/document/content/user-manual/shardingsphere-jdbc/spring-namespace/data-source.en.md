+++
title = "Data Source"
weight = 2
chapter = true
+++

## Background

Any data source object configured as Spring bean can be used with the Spring namespace of ShardingSphere-JDBC Data Planning.

The database driver in the example is MySQL and the connection pool is HikariCP, both of which can be replaced by other database drivers and connection pools.
When using ShardingSphere JDBC, the property names of the JDBC pools depend on the definition of JDBC pools themselves respectively, rather than being rigidly defined by ShardingSphere. For relevant processing, you can see reference class `org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator`. As for Alibaba Druid 1.2.9, using `url` instead of `jdbcUrl` as in the following example is the expected behavior.

## Configuration Examples

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:shardingsphere="http://shardingsphere.apache.org/schema/shardingsphere/datasource"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                           http://www.springframework.org/schema/beans/spring-beans.xsd 
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource.xsd
                           ">
    <bean id="ds1" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/ds1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="ds2" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/ds2" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <shardingsphere:data-source id="ds" database-name="foo_schema" data-source-names="ds1,ds2" rule-refs="..." />
</beans>
```
