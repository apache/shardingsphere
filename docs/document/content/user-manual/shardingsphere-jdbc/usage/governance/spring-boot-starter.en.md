+++
title = "Use Spring Boot Starter"
weight = 3
+++

## Import Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-governance-spring-boot-starter</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- import if using ZooKeeper -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-governance-repository-zookeeper-curator</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- import if using Etcd -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-governance-repository-etcd</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

## Configure Rule

```properties
spring.shardingsphere.governance.name=governance-spring-boot-shardingsphere-test
spring.shardingsphere.governance.registry-center.type=Zookeeper
spring.shardingsphere.governance.registry-center.server-lists=localhost:2181
spring.shardingsphere.governance.additional-config-center.type=Zookeeper
spring.shardingsphere.governance.additional-config-center.server-lists=localhost:2182
spring.shardingsphere.governance.overwrite=true
```

## Use GovernanceShardingSphereDataSource in Spring

GovernanceShardingSphereDataSource can be used directly by injection; 
or configure GovernanceShardingSphereDataSource in ORM frameworks such as JPA or MyBatis.

```java
@Resource
private DataSource dataSource;
```
