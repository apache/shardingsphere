+++
title = "Use Spring Namespace"
weight = 4
+++

## Import Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-governance-spring-namespace</artifactId>
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
     <governance:data-source id="encryptDataSource" data-source-names="demo_ds" reg-center-ref="regCenter" config-center-ref="configCenter" rule-refs="encryptRule" overwrite="true" >
        <props>
            <prop key="query-with-cipher-column">true</prop>
        </props>
     </governance:data-source>
</beans>
```

## Use GovernanceShardingSphereDataSource in Spring

GovernanceShardingSphereDataSource can be used directly by injection; 
or configure GovernanceShardingSphereDataSource in ORM frameworks such as JPA or MyBatis.

```java
@Resource
private DataSource dataSource;
```
