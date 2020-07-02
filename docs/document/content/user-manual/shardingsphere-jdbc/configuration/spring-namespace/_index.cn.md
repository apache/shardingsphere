+++
title = "Spring 命名空间配置"
weight = 4
+++

## 简介

ShardingSphere-JDBC 提供官方的 Spring 命名空间配置，使开发者可以非常便捷的整合 ShardingSphere-JDBC 和 Spring 框架。

## Spring 命名空间配置项

### 配置示例

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
    
    <!-- 配置规则，更多详细配置请参见具体的规则配置部分。 -->
    <!-- ... -->
    
    <shardingsphere:data-source id="shardingDataSource" data-source-names="ds0,ds1" rule-refs="..." >
        <props>
            <prop key="xxx.xxx">${xxx.xxx}</prop>
        </props>
    </shardingsphere:data-source>
</beans>
```

### 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource-5.0.0.xsd)

\<shardingsphere:data-source />

| *名称*            | *类型* | *说明*                       |
| ----------------- | ----- | --------------------------- |
| id                | 属性  | Spring Bean Id               |
| data-source-names | 标签  | 数据源名称，多个数据源以逗号分隔 |
| rule-refs         | 标签  | 规则名称，多个规则以逗号分隔     |
| props (?)         | 标签  | 属性配置，详情请参见[属性配置](/cn/user-manual/shardingsphere-jdbc/configuration/props) |
