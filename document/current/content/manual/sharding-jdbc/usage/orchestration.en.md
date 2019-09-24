+++
toc = true
title = "Orchestration"
weight = 4
+++

Using orchestration requires designating a registry center, in which all the configurations are saved. Users can either use local configurations to cover registry center configurations or read configurations from registry center.

## Not Use Spring

### Introduce Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-jdbc-orchestration</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>
<!--If you want to use zookeeper, please use the artifactId below.-->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-orchestration-reg-zookeeper-curator</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>
```

### Rule Configuration Based on Java

```java
    // Configure dataSourceMap and shardingRuleConfig
    // ...

    // Configure registry center
    RegistryCenterConfiguration regConfig = new RegistryCenterConfiguration("zookeeper");
    regConfig.setServerLists("localhost:2181");
    regConfig.setNamespace("sharding-sphere-orchestration");

    // Configure orchestration
    OrchestrationConfiguration orchConfig = new OrchestrationConfiguration("orchestration-sharding-data-source", regConfig, false);

    // Get data source
    DataSource dataSource = OrchestrationShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, new Properties(), orchConfig);
```

### Rule Configuration Based on Yaml

Or use Yaml to configure, similar as above configurations:

```yaml
orchestration:
  name: orchestration-sharding-data-source
  overwrite: false
  registry:
    type: zookeeper
    serverLists: localhost:2181
    namespace: sharding-sphere-orchestration
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
    <artifactId>sharding-jdbc-orchestration-spring-boot-starter</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>

<!--If you want to use zookeeper, please add the Maven below.-->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-orchestration-reg-zookeeper-curator</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>
```

```xml
<!-- for spring namespace -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-jdbc-orchestration-spring-namespace</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>

<!--If you want to use zookeeper, please add the Maven below.-->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-orchestration-reg-zookeeper-curator</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>
```

### Rule Configuration Based on Spring Boot

```properties
spring.shardingsphere.orchestration.name=orchestration-sharding-data-source
spring.shardingsphere.orchestration.overwrite=false
spring.shardingsphere.orchestration.registry.type=zookeeper
spring.shardingsphere.orchestration.registry.server-lists=localhost:2181
spring.shardingsphere.orchestration.registry.namespace=sharding-jdbc-orchestration
```

### Rule Configuration Based on Spring Name Space

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:orchestraion="http://shardingsphere.apache.org/schema/shardingsphere/orchestration"
       xmlns="http://www.springframework.org/schema/beans"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/orchestration
                           http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd">
     <import resource="namespace/shardingDataSourceNamespace.xml" />
     <orchestraion:registry-center id="regCenter" type="zookeeper" server-lists="localhost:3181" namespace="orchestration-spring-namespace-test" operation-timeout-milliseconds="1000" max-retries="3" />
     <orchestraion:sharding-data-source id="simpleShardingOrchestration" data-source-ref="simpleShardingDataSource" registry-center-ref="regCenter" />
</beans>
```

For detailed configurations, please refer to [Configuration Manual](http://shardingsphere.apache.org/document/current/cn/manual/sharding-jdbc/configuration/).
