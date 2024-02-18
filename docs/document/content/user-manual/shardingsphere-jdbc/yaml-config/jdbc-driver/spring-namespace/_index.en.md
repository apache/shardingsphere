+++
title = "Spring Namespace"
weight = 4
chapter = true
+++

## Overview

ShardingSphere provides a JDBC driver. To use ShardingSphere, developers can configure `ShardingSphereDriver` in Spring.

## Operation

### Import Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### Configure Spring Bean

#### Configuration Item Explanation

| *Name*            | *Type*      | *Description*                                     |
|-------------------|-------------|---------------------------------------------------|
| driverClass       | Attribute   | Database Driver, need to use ShardingSphereDriver |
| url               | Attribute   | YAML configuration file path                      |

#### Example

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                           http://www.springframework.org/schema/beans/spring-beans.xsd">
    
    <bean id="shardingDataSource" class="org.springframework.jdbc.datasource.SimpleDriverDataSource">
        <property name="driverClass" value="org.apache.shardingsphere.driver.ShardingSphereDriver" />
        <property name="url" value="jdbc:shardingsphere:classpath:xxx.yaml" />
    </bean>
</beans>
```

### Use Data Source

Same with Spring Boot.
