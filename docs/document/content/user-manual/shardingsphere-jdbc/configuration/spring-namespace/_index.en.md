+++
title = "Spring Namespace Configuration"
weight = 4
+++

## Introduction

ShardingSphere-JDBC provides official Spring namespace to make convenient for developers to integrate ShardingSphere-JDBC and Spring Framework.

## Spring Namespace Configuration Item

### Configuration Example

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:shardingsphere="http://shardingsphere.apache.org/schema/shardingsphere/datasource"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                           http://www.springframework.org/schema/beans/spring-beans.xsd 
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource.xsd
                           ">
    <bean id="ds0" class="org.apache.commons.dbcp2.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="ds1" class="org.apache.commons.dbcp2.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <!-- Rule configurations, please refer to specific rule configuration for more details. -->
    <!-- ... -->
    
    <shardingsphere:data-source id="shardingDataSource" data-source-names="ds0,ds1" rule-refs="..." >
        <props>
            <prop key="xxx.xxx">${xxx.xxx}</prop>
        </props>
    </shardingsphere:data-source>
</beans>
```

### Configuration Item Explanation

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource-5.0.0.xsd)

\<shardingsphere:data-source />

| *Name*            | *Type*    | *Description*                                                        |
| ----------------- | --------- | -------------------------------------------------------------------- |
| id                | Attribute | Spring Bean Id                                                       |
| data-source-names | Attribute | Data source name, multiple data source names are separated by commas |
| rule-refs         | Attribute | Rule name, multiple rule names are separated by commas               |
| props (?)         | Tag       | Properties configuration, Please refer to [Properties Configuration](/en/user-manual/shardingsphere-jdbc/configuration/props) for more details |
