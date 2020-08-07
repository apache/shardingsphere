+++
title = "Use Spring Boot Starter"
weight = 3
+++

## Import Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-orchestration-spring-boot-starter</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- import if using ZooKeeper -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-orchestration-repository-zookeeper-curator</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- import if using Etcd -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-orchestration-repository-etcd</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

## Configure Rule

```properties
spring.shardingsphere.orchestration.name=orchestration-spring-boot-shardingsphere-test
spring.shardingsphere.orchestration.registryCenter.type=Zookeeper
spring.shardingsphere.orchestration.registryCenter.server-lists=localhost:2181
spring.shardingsphere.orchestration.additionalConfigCenter.type=Zookeeper
spring.shardingsphere.orchestration.additionalConfigCenter.server-lists=localhost:2182
spring.shardingsphere.orchestration.overwrite=true
```

## Use OrchestrationShardingSphereDataSource in Spring

OrchestrationShardingSphereDataSource can be used directly by injection; 
or configure OrchestrationShardingSphereDataSource in ORM frameworks such as JPA or MyBatis.

```java
@Resource
private DataSource dataSource;
```
