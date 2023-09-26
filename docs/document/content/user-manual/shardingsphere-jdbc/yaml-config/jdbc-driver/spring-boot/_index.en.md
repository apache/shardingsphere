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

The YAML configuration file in 'spring.datasource.url' currently support in two ways, the absolute path 'absolutepath:' and CLASSPATH 'classpath:', which can be referred to `org.apache.shardingsphere.driver.jdbc.core.driver.ShardingSphereURLProvider`'s implementation for details.

### Use Data Source

Use this data source directly; or configure ShardingSphereDataSource to be used in conjunction with ORM frameworks such as JPA, Hibernate, and MyBatis.

## Special handling for Spring Boot OSS 3

Spring Boot OSS 3 has made a "big bang" upgrade to Jakarta EE and Java 17, with all complications involved.

For ShardingSphere JDBC that is using the Java EE 8 API and its implementation, if you want to use ShardingSphere JDBC 
on a Jakarta EE 9+ API-based web framework such as Spring Boot OSS 3, you need to introduce a JAXB implementation of 
Java EE 8 and specify a specific version of SnakeYAML.

This is reflected in Maven's `pom.xml` as follows. You can also use other JAXB API implementations. This configuration 
also applies to other Jakarta EE-based Web Frameworks, such as Quarkus 3, Micronaut Framework 4 and Helidon 3.

```xml
<project>
    <dependencies>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-jdbc-core</artifactId>
            <version>${shardingsphere.version}</version>
        </dependency>
        <dependency>
            <groupId>org.yaml</groupId>
            <artifactId>snakeyaml</artifactId>
            <version>1.33</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jaxb</groupId>
            <artifactId>jaxb-runtime</artifactId>
            <version>2.3.8</version>
        </dependency>
    </dependencies>
</project>
```

If the user created the Spring Boot project from https://start.spring.io/, or the `dependencyManagement` XML tag was 
imported POM file for `org.springframework.boot:spring-boot-dependencies`, users can simplify configuration by 
following things.

```xml
<project>
    <properties>
        <snakeyaml.version>1.33</snakeyaml.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-jdbc-core</artifactId>
            <version>${shardingsphere.version}</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jaxb</groupId>
            <artifactId>jaxb-runtime</artifactId>
            <version>2.3.8</version>
        </dependency>
    </dependencies>
</project>
```

In addition, ShardingSphere's XA distributed transactions are not yet ready on Spring Boot OSS 3.

## Special handling for earlier versions of Spring Boot OSS 2

All features of ShardingSphere are available on Spring Boot OSS 2, but earlier versions of Spring Boot OSS may require 
manually specifying version 1.33 for SnakeYAML.
This is reflected in Maven's `pom.xml` as follows.

```xml
<project>
    <dependencies>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-jdbc-core</artifactId>
            <version>${shardingsphere.version}</version>
        </dependency>
        <dependency>
            <groupId>org.yaml</groupId>
            <artifactId>snakeyaml</artifactId>
            <version>1.33</version>
        </dependency>
    </dependencies>
</project>
```
If the user created the Spring Boot project from https://start.spring.io/, or the `dependencyManagement` XML tag was
imported POM file for `org.springframework.boot:spring-boot-dependencies`, users can also choose to simplify the content
by configuring `properties` for `snakeyaml.version`.
