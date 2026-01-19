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
    <artifactId>shardingsphere-jdbc</artifactId>
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

The YAML configuration file in 'spring.datasource.url' currently support in multiple ways, refer to [Known Implementation](../known-implementation/).

### Use Data Source

Use this data source directly; or configure ShardingSphereDataSource to be used in conjunction with ORM frameworks such as JPA, Hibernate, and MyBatis.

## Handling for Spring Boot 3+

ShardingSphere's XA distributed transactions are not yet ready for Spring Boot 3+. 
This limitation also applies to other Jakarta EE 9+ based web frameworks, 
such as Quarkus 3, Micronaut Framework 4, and Helidon 3+.

Users only need to configure the following.

```xml
<project>
    <dependencies>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-jdbc</artifactId>
            <version>${shardingsphere.version}</version>
        </dependency>
    </dependencies>
</project>
```

## Special Handling for Lower Versions of Spring Boot 2

Aside from ShardingSphere Agent, all ShardingSphere JDBC features are available in Spring Boot 2.
However, lower versions of Spring Boot may require manually specifying the SnakeYAML version as `2.2`.
This is reflected in the Maven `pom.xml` as follows.

```xml
<project>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.yaml</groupId>
                <artifactId>snakeyaml</artifactId>
                <version>2.2</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-jdbc</artifactId>
            <version>${shardingsphere.version}</version>
        </dependency>
    </dependencies>
</project>
```

If a user created a Spring Boot project via https://start.spring.io/ , 
the configuration can be simplified as follows.

```xml
<project>
    <properties>
        <snakeyaml.version>2.2</snakeyaml.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-jdbc</artifactId>
            <version>${shardingsphere.version}</version>
        </dependency>
    </dependencies>
</project>
```

For Spring Boot 2, if developers are managing logs via SLF4J or Logback,
they may encounter the issue described in https://github.com/spring-projects/spring-boot/issues/34708 .
Developers should consider maintaining the Spring Boot 2 source code themselves.
