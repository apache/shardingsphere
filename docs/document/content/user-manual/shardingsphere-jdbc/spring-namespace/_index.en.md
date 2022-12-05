+++
title = "Spring Namespace"
weight = 4
chapter = true
+++

## Overview

ShardingSphere provides a JDBC driver, and developers can configure `ShardingSphereDriver` in Spring to use ShardingSphere.

## Usage

### Import Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### Configure Spring Bean


#### Configuration Item Explanation

| *Name*            | *Type*      | *Description*                                     |
|-------------------|-------------|---------------------------------------------------|
| driverClass       | Attribute   | Database Driver, need to use ShardingSphereDriver |
| url               | Attribute   | YAML configuration file path                      |

#### Driver Class Name

`org.apache.shardingsphere.driver.ShardingSphereDriver`

#### URL Configuration Instructions

- Prefixed with `jdbc:shardingsphere:`
- Configuration file: `xxx.yaml`, the configuration file format is consistent with [YAML Configuration](/en/user-manual/shardingsphere-jdbc/yaml-config)
- Configuration file loading rules:
  - No prefix means to load the configuration file from the specified path
  - The `classpath:` prefix means to load configuration files from the classpath

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

Same with Spring Boot Starter.
