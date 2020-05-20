+++
title = "Orchestration"
weight = 4
+++

Using orchestration requires designating a config center, registry center & metadata center, in which all the configurations are saved. Users can either use local configurations to cover config center configurations or read configurations from config center.

## Not Use Spring

### Introduce Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-orchestration</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
<!--If you want to use zookeeper, please use the artifactId below.-->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-orchestration-reg-zookeeper-curator</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### Rule Configuration Based on Java

```java
    // Configure dataSourceMap and shardingRuleConfig
    // ...

    // Configure config/registry/metadata center
    Properties properties = new Properties();
    properties.setProperty("overwrite", overwrite);
    CenterConfiguration centerConfiguration = new CenterConfiguration("zookeeper", properties);
    centerConfiguration.setServerLists("localhost:2181");
    centerConfiguration.setNamespace("shardingsphere-orchestration");
    centerConfiguration.setOrchestrationType("registry_center,config_center,metadata_center");

    // Configure orchestration
    Map<String, CenterConfiguration> instanceConfigurationMap = new HashMap<String, CenterConfiguration>();
    instanceConfigurationMap.put("orchestration-shardingsphere-data-source", centerConfiguration);

    // Get data source
    OrchestrationShardingDataSourceFactory.createDataSource(
                    createDataSourceMap(), createShardingRuleConfig(), new HashMap<String, Object>(), new Properties(), new OrchestrationConfiguration(instanceConfigurationMap));
```

### Rule Configuration Based on Yaml

Or use Yaml to configure, similar as above configurations:

```yaml
orchestration:
  orchestration_ds:
      orchestrationType: registry_center,config_center,metadata_center
      instanceType: zookeeper
      serverLists: localhost:2181
      namespace: orchestration
      props:
        overwrite: true
```

```java
    DataSource dataSource = YamlOrchestrationShardingDataSourceFactory.createDataSource(yamlFile);
```

## Using Spring

### Introduce Maven Dependency

```xml
<!-- for spring boot -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-orchestration-spring-boot-starter</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!--If you want to use zookeeper, please add the Maven below.-->
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

<!--If you want to use zookeeper, please add the Maven below.-->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-orchestration-reg-zookeeper-curator</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### Rule Configuration Based on Spring Boot

```properties
spring.shardingsphere.orchestration.spring_boot_ds_sharding.orchestration-type=registry_center,config_center,metadata_center
spring.shardingsphere.orchestration.spring_boot_ds_sharding.instance-type=zookeeper
spring.shardingsphere.orchestration.spring_boot_ds_sharding.server-lists=localhost:2181
spring.shardingsphere.orchestration.spring_boot_ds_sharding.namespace=orchestration-spring-boot-shardingsphere-test
spring.shardingsphere.orchestration.spring_boot_ds_sharding.props.overwrite=true
```

### Rule Configuration Based on Spring Name Space

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

For detailed configurations, please refer to [Configuration Manual](http://shardingsphere.apache.org/document/current/cn/manual/shardingsphere-jdbc/configuration/).
