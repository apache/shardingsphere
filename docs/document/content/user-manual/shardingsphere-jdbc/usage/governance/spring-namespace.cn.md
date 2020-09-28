+++
title = "使用 Spring 命名空间"
weight = 4
+++

## 引入Maven依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-governance-spring-namespace</artifactId>
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

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:governance="http://shardingsphere.apache.org/schema/shardingsphere/governance"
       xmlns="http://www.springframework.org/schema/beans"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/governance
                           http://shardingsphere.apache.org/schema/shardingsphere/governance/governance.xsd">
     <util:properties id="instance-properties">
         <prop key="max-retries">3</prop>
         <prop key="operation-timeout-milliseconds">3000</prop>
     </util:properties>
     <governance:reg-center id="regCenter" type="Zookeeper" server-lists="localhost:2181" />
     <governance:config-center id="configCenter" type="ZooKeeper" server-lists="localhost:2182" />
     <governance:data-source id="shardingDatabasesTablesDataSource" data-source-names="demo_ds_0, demo_ds_1" reg-center-ref="regCenter" config-center-ref="configCenter" rule-refs="shardingRule" overwrite="true" />
     <governance:data-source id="primaryReplicaReplicationDataSource" data-source-names="demo_primary_ds, demo_replica_ds_0, demo_replica_ds_1" reg-center-ref="regCenter" config-center-ref="configCenter" rule-refs="primaryReplicaRule" overwrite="true" />
     <governance:data-source id="encryptDataSource" data-source-names="demo_ds" reg-center-ref="regCenter" config-center-ref="configCenter" rule-refs="encryptRule" overwrite="true" >
        <props>
            <prop key="query-with-cipher-column">true</prop>
        </props>
     </governance:data-source>
</beans>
```

## 在 Spring 中使用 GovernanceShardingSphereDataSource

直接通过注入的方式即可使用 GovernanceShardingSphereDataSource；或者将 GovernanceShardingSphereDataSource 配置在JPA， MyBatis 等 ORM 框架中配合使用。

```java
@Resource
private DataSource dataSource;
```
