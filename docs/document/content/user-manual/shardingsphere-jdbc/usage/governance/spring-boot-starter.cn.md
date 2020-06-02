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
    <artifactId>shardingsphere-orchestration-center-zookeeper-curator</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- 使用 Etcd 时，需要引入此模块 -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-orchestration-center-etcd</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

## 规则配置

```properties
spring.shardingsphere.orchestration.spring_boot_ds.orchestration-type=registry_center,config_center,metadata_center
spring.shardingsphere.orchestration.spring_boot_ds.instance-type=zookeeper
spring.shardingsphere.orchestration.spring_boot_ds.server-lists=localhost:2181
spring.shardingsphere.orchestration.spring_boot_ds.namespace=orchestration-spring-boot-shardingsphere-test
spring.shardingsphere.orchestration.spring_boot_ds.props.overwrite=true
```

## 在 Spring 中使用 OrchestrationShardingSphereDataSource

直接通过注入的方式即可使用 OrchestrationShardingSphereDataSource；或者将 OrchestrationShardingSphereDataSource 配置在JPA， MyBatis 等 ORM 框架中配合使用。

```java
@Resource
private DataSource dataSource;
```
