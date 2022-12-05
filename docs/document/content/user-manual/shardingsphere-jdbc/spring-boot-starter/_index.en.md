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

#### Driver Class Name

`org.apache.shardingsphere.driver.ShardingSphereDriver`

#### URL Configuration Instructions

- Prefixed with `jdbc:shardingsphere:`
- Configuration file: `xxx.yaml`, the configuration file format is consistent with [YAML Configuration](/en/user-manual/shardingsphere-jdbc/yaml-config)
- Configuration file loading rules:
  - No prefix means to load the configuration file from the specified path
  - The `classpath:` prefix means to load configuration files from the classpath

```properties
# Configuring DataSource Drivers
spring.datasource.driver-class-name=org.apache.shardingsphere.driver.ShardingSphereDriver
# Specify a YAML configuration file
spring.datasource.url=jdbc:shardingsphere:classpath:xxx.yaml
```

### Use Data Source

Use this data source directly; or configure ShardingSphereDataSource to be used in conjunction with ORM frameworks such as JPA, Hibernate, and MyBatis.
