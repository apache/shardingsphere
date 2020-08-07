+++
title = "使用 Spring Boot Starter"
weight = 3
+++

## 引入 Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-orchestration-spring-boot-starter</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- 使用 ZooKeeper 时，需要引入此模块 -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-orchestration-repository-zookeeper-curator</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- 使用 Etcd 时，需要引入此模块 -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-orchestration-repository-etcd</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

## 规则配置

```properties
spring.shardingsphere.orchestration.name=orchestration-spring-boot-shardingsphere-test
spring.shardingsphere.orchestration.registryCenter.type=Zookeeper
spring.shardingsphere.orchestration.registryCenter.server-lists=localhost:2181
spring.shardingsphere.orchestration.additionalConfigCenter.type=Zookeeper
spring.shardingsphere.orchestration.additionalConfigCenter.server-lists=localhost:2182
spring.shardingsphere.orchestration.overwrite=true
```

## 在 Spring 中使用 OrchestrationShardingSphereDataSource

直接通过注入的方式即可使用 OrchestrationShardingSphereDataSource；或者将 OrchestrationShardingSphereDataSource 配置在JPA， MyBatis 等 ORM 框架中配合使用。

```java
@Resource
private DataSource dataSource;
```
