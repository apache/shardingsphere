+++
title = "使用 Spring Boot Starter"
weight = 3
+++

## 引入 Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-boot-starter</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- 使用 ZooKeeper 时，需要引入此模块 -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-cluster-mode-repository-zookeeper-curator</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- 使用 Etcd 时，需要引入此模块 -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-cluster-mode-repository-etcd</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

## 规则配置

```properties
spring.shardingsphere.mode.type=Cluster
spring.shardingsphere.mode.repository.type=ZooKeeper
spring.shardingsphere.mode.repository.props.namespace=governance-spring-boot-shardingsphere-test
spring.shardingsphere.mode.repository.props.server-lists=localhost:2181
spring.shardingsphere.mode.overwrite=true
```

## 在 Spring 中使用 ShardingSphereDataSource

直接通过注入的方式即可使用 ShardingSphereDataSource；或者将 ShardingSphereDataSource 配置在JPA， MyBatis 等 ORM 框架中配合使用。

```java
@Resource
private DataSource dataSource;
```
