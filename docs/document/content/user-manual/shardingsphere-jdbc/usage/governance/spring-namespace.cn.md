+++
title = "使用 Spring 命名空间"
weight = 4
+++

## 引入Maven依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-orchestration-spring-namespace</artifactId>
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

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:orchestration="http://shardingsphere.apache.org/schema/shardingsphere/orchestration"
       xmlns="http://www.springframework.org/schema/beans"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/orchestration
                           http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd">
     <util:properties id="instance-props">
         <prop key="max-retries">3</prop>
         <prop key="operation-timeout-milliseconds">3000</prop>
     </util:properties>
     <orchestration:instance id="regCenter" orchestration-type="registry_center,config_center,metadata_center" instance-type="zookeeper" server-lists="localhost:2181" namespace="orchestration-spring-namespace-demo" props-ref="instance-props" />
     <orchestration:data-source id="shardingDatabasesTablesDataSource" data-source-ref="realShardingDatabasesTablesDataSource" instance-ref="regCenter" overwrite="true" />
     <orchestration:slave-data-source id="masterSlaveDataSource" data-source-ref="realMasterSlaveDataSource" instance-ref="regCenter" overwrite="true" />
     <orchestration:data-source id="encryptDataSource" data-source-ref="realEncryptDataSource" instance-ref="regCenter" overwrite="true" />
</beans>
```

## 在 Spring 中使用 OrchestrationShardingSphereDataSource

直接通过注入的方式即可使用 OrchestrationShardingSphereDataSource；或者将 OrchestrationShardingSphereDataSource 配置在JPA， MyBatis 等 ORM 框架中配合使用。

```java
@Resource
private DataSource dataSource;
```
