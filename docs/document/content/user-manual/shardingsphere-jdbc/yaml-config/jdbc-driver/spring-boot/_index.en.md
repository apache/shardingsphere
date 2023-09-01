+++
title = "Spring Boot"
weight = 4
chapter = true
+++

## Overview

ShardingSphere provides a JDBC driver, and developers can configure `ShardingSphereDriver` in Spring Boot to use ShardingSphere.

## Usage

### Import Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### Configure Spring Boot Properties

```properties
# Configuring DataSource Drivers
spring.datasource.driver-class-name=org.apache.shardingsphere.driver.ShardingSphereDriver
# Specify a YAML configuration file
spring.datasource.url=jdbc:shardingsphere:classpath:xxx.yaml
```

The YAML configuration file in 'spring.datasource.url' currently support in three ways, the absolute path 'absolutepath:', Apollo configuration center 'apollo:', and CLASSPATH 'classpath:', which can be referred to `org.apache.shardingsphere.driver.jdbc.core.driver.ShardingSphereURLProvider`'s implementation for details.

### Use Data Source

Use this data source directly; or configure ShardingSphereDataSource to be used in conjunction with ORM frameworks such as JPA, Hibernate, and MyBatis.
