+++
title = "编排治理"
weight = 14
+++


### 引入Maven依赖

```xml
<!-- for spring boot -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-orchestration-spring-boot-starter</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!--若使用zookeeper, 请加入下面Maven坐标-->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-orchestration-reg-zookeeper-curator</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

```xml
<!-- for spring namespace -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-orchestration-spring-namespace</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!--若使用zookeeper, 请加入下面Maven坐标-->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-orchestration-reg-zookeeper-curator</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### 基于Spring boot的规则配置

```properties
spring.shardingsphere.orchestration.spring_boot_ds_sharding.orchestration-type=registry_center,config_center,metadata_center
spring.shardingsphere.orchestration.spring_boot_ds_sharding.instance-type=zookeeper
spring.shardingsphere.orchestration.spring_boot_ds_sharding.server-lists=localhost:2181
spring.shardingsphere.orchestration.spring_boot_ds_sharding.namespace=orchestration-spring-boot-shardingsphere-test
spring.shardingsphere.orchestration.spring_boot_ds_sharding.props.overwrite=true
```

### 基于Spring命名空间的规则配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:orchestration="http://shardingsphere.apache.org/schema/shardingsphere/orchestration"
       xmlns="http://www.springframework.org/schema/beans"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/orchestration
                           http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd">
     <import resource="namespace/shardingDataSourceNamespace.xml" />
     <util:properties id="instance-props">
               <prop key="max-retries">3</prop>
               <prop key="operation-timeout-milliseconds">3000</prop>
         </util:properties>
         <orchestration:instance id="regCenter" orchestration-type="registry_center,config_center,metadata_center" instance-type="zookeeper" server-lists="localhost:2181" namespace="orchestration-spring-namespace-demo"
                                    props-ref="instance-props" />
         <orchestration:data-source id="shardingDatabasesTablesDataSource" data-source-ref="realShardingDatabasesTablesDataSource" instance-ref="regCenter" overwrite="true" />
         <orchestration:slave-data-source id="masterSlaveDataSource" data-source-ref="realMasterSlaveDataSource" instance-ref="regCenter" overwrite="true" />
         <orchestration:data-source id="encryptDataSource" data-source-ref="realEncryptDataSource" instance-ref="regCenter" overwrite="true" />
</beans>
```

更多的详细配置请参考[配置手册](/cn/user-manual/shardingsphere-jdbc/configuration/)。
