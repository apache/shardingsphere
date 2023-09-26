+++
title = "Spring Boot"
weight = 4
chapter = true
+++

## 简介

ShardingSphere 提供 JDBC 驱动，开发者可以在 Spring Boot 中配置 `ShardingSphereDriver` 来使用 ShardingSphere。

## 使用步骤

### 引入 Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### 配置 Spring Boot

```properties
# 配置 DataSource Driver
spring.datasource.driver-class-name=org.apache.shardingsphere.driver.ShardingSphereDriver
# 指定 YAML 配置文件
spring.datasource.url=jdbc:shardingsphere:classpath:xxx.yaml
```

`spring.datasource.url` 中的 YAML 配置文件当前支持通过两种方式获取，绝对路径 `absolutepath:` 以及 CLASSPATH `classpath:`，具体可参考 `org.apache.shardingsphere.driver.jdbc.core.driver.ShardingSphereURLProvider` 的实现。

### 使用数据源

直接使用该数据源；或者将 ShardingSphereDataSource 配置在 JPA、Hibernate、MyBatis 等 ORM 框架中配合使用。

## 针对 Spring Boot OSS 3 的特殊处理

Spring Boot OSS 3 对 Jakarta EE 和 Java 17 进行了 “大爆炸” 升级，涉及大量复杂情况。

对于正在使用 Java EE 8 API 及其实现的 ShardingSphere JDBC 而言，如果用户希望在 Spring Boot OSS 3 等基于 Jakarta EE 9+ API 的 Web 
Framework 上使用 ShardingSphere JDBC，则需要引入 Java EE 8 的 JAXB 的实现，并指定一个特定的 SnakeYAML 版本。

这在 Maven 的 `pom.xml` 体现为如下内容。你也可以使用其他的 JAXB API 的实现。此配置同样适用于其他基于 Jakarta EE 的 Web Framework，如 
Quarkus 3，Micronaut Framework 4 和 Helidon 3。

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

如果用户是通过 https://start.spring.io/ 创建了 Spring Boot 项目，或者在 `dependencyManagement` 的 XML 标签导入了 
`org.springframework.boot:spring-boot-dependencies` 的 POM 文件，则可通过如下内容来简化配置。

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

此外，ShardingSphere 的 XA 分布式事务尚未在 Spring Boot OSS 3 上就绪。

## 针对低版本的 Spring Boot OSS 2 的特殊处理

ShardingSphere 的所有特性均可在 Spring Boot OSS 2 上使用，但低版本的 Spring Boot OSS 可能需要手动指定 SnakeYAML 的版本为 1.33 。 
这在 Maven 的 `pom.xml` 体现为如下内容。

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

如果用户是通过 https://start.spring.io/ 创建了 Spring Boot 项目，或者在 `dependencyManagement` 的 XML 标签导入了
`org.springframework.boot:spring-boot-dependencies`的 POM 文件，同样可以选择通过配置 `snakeyaml.version` 的 `properties` 
来简化内容。
