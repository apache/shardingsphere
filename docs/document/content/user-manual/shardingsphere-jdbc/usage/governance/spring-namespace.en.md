+++
title = "Use Spring Namespace"
weight = 4
+++

## Import Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-namespace</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- import if using ZooKeeper -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-cluster-mode-repository-zookeeper-curator</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- import if using Etcd -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-cluster-mode-repository-etcd</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

## Configure Rule

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:cluster="http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster"
       xmlns:shardingsphere="http://shardingsphere.apache.org/schema/shardingsphere/datasource"
       xmlns="http://www.springframework.org/schema/beans"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster
                           http://shardingsphere.apache.org/schema/shardingsphere/mode-repository/cluster/repository.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource 
                           http://shardingsphere.apache.org/schema/shardingsphere/datasource/datasource.xsd">
     <cluster:repository id="clusterRepository" type="ZooKeeper" namespace="regCenter" server-lists="localhost:2181">
         <props>
             <prop key="max-retries">3</prop>
             <prop key="operation-timeout-milliseconds">3000</prop>
         </props>
     </cluster:repository>
     <shardingsphere:data-source id="shardingDatabasesTablesDataSource" data-source-names="demo_ds_0, demo_ds_1" rule-refs="shardingRule">
        <shardingsphere:mode type="Cluster" repository-ref="clusterRepository" overwrite="true"/>
     </shardingsphere:data-source>
    <shardingsphere:data-source id="replicaQueryDataSource" data-source-names="demo_primary_ds, demo_replica_ds_0, demo_replica_ds_1" rule-refs="replicaQueryRule">
        <shardingsphere:mode type="Cluster" repository-ref="clusterRepository" overwrite="true"/>
    </shardingsphere:data-source>
    <shardingsphere:data-source id="encryptDataSource" data-source-names="demo_ds" rule-refs="encryptRule">
        <shardingsphere:mode type="Cluster" repository-ref="clusterRepository" overwrite="true"/>
    </shardingsphere:data-source>
</beans>
```

## Use ShardingSphereDataSource in Spring

ShardingSphereDataSource can be used directly by injection; 
or configure ShardingSphereDataSource in ORM frameworks such as JPA or MyBatis.

```java
@Resource
private DataSource dataSource;
```
