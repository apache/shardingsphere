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

## Special handling for earlier versions of Spring Boot OSS 2

Earlier versions of Spring Boot OSS may require manually specifying version 2.2 for SnakeYAML.
This is reflected in Maven's `pom.xml` as follows.

```xml
<project>
    <dependencies>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-jdbc</artifactId>
            <version>${shardingsphere.version}</version>
        </dependency>
        <dependency>
            <groupId>org.yaml</groupId>
            <artifactId>snakeyaml</artifactId>
            <version>2.2</version>
        </dependency>
    </dependencies>
</project>
```

If the user created the Spring Boot project from https://start.spring.io/, users can simplify configuration by
following things.

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

In addition, ShardingSphere's XA distributed transactions cannot be integrated with Spring's own annotations on Spring Boot OSS 2.x.
