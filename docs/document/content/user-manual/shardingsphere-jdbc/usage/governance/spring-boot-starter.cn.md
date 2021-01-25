+++
title = "使用 Spring Boot Starter"
weight = 3
+++

## 引入 Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-governance-spring-boot-starter</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- 使用 ZooKeeper 时，需要引入此模块 -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-governance-repository-zookeeper-curator</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- 使用 Etcd 时，需要引入此模块 -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-governance-repository-etcd</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

## 规则配置

```properties
spring.shardingsphere.governance.name=governance-spring-boot-shardingsphere-test
spring.shardingsphere.governance.registry-center.type=Zookeeper
spring.shardingsphere.governance.registry-center.server-lists=localhost:2181
spring.shardingsphere.governance.additional-config-center.type=Zookeeper
spring.shardingsphere.governance.additional-config-center.server-lists=localhost:2182
spring.shardingsphere.governance.overwrite=true
```

## 在 Spring 中使用 GovernanceShardingSphereDataSource

直接通过注入的方式即可使用 GovernanceShardingSphereDataSource；或者将 GovernanceShardingSphereDataSource 配置在JPA， MyBatis 等 ORM 框架中配合使用。

```java
@Resource
private DataSource dataSource;
```
