+++
title = "Spring Boot Starter"
weight = 4
chapter = true
+++

## Overview

ShardingSphere-JDBC provides official Spring Boot Starter to make convenient for developers to integrate ShardingSphere-JDBC and Spring Boot.

The list of compatible SpringBoot versions is as follows:

1. SpringBoot 1.x
2. SpringBoot 2.x
3. SpringBoot 3.x (Experimental)

## Usage

### Import Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-boot-starter</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### Configure Spring Boot Properties

ShardingSphere-JDBC spring boot properties consists of database name, mode configuration, data source map, rule configurations and properties.

```properties
# JDBC logic database name. Through this parameter to connect ShardingSphere-JDBC and ShardingSphere-Proxy.
spring.shardingsphere.database.name= # logic database name, default value: logic_db
spring.shardingsphere.mode.xxx= # mode configuration
spring.shardingsphere.dataSource.xxx= # data source map
spring.shardingsphere.rules.xxx= # rule configurations
spring.shardingsphere.props= # properties
```

Please refer to [Mode Confiugration](/en/user-manual/shardingsphere-jdbc/spring-boot-starter/mode) for more mode details.

Please refer to [Data Source Confiugration](/en/user-manual/shardingsphere-jdbc/spring-boot-starter/data-source) for more data source details.

Please refer to [Rules Confiugration](/en/user-manual/shardingsphere-jdbc/spring-boot-starter/rules) for more rule details.

### Use Data Source

Developer can inject to use native JDBC or ORM frameworks such as JPA, Hibernate or MyBatis through the DataSource.

Take native JDBC usage as an example:

```java
@Resource
private DataSource dataSource;
```
