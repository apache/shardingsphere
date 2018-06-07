+++
toc = true
title = "Orchestration"
weight = 4
+++

Use orchestration feature need indicate a registry center. Configuration will save into registry center. Configuration can overwrite by local when every application startup, or load from registry center only.  

## Without spring

### Add maven dependency

```xml
<dependency>
    <groupId>shardingsphere</groupId>
    <artifactId>sharding-jdbc-orchestration</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>
```

### Configure orchestration with java

```java
    // Configure dataSourceMap and shardingRuleConfig
    // ...
    
    // Configure registry center of Zookeeper
    ZookeeperConfiguration zkConfig = new ZookeeperConfiguration();
    zkConfig.setServerLists("localhost:2181");
    zkConfig.setNamespace("sharding-jdbc-orchestration");
    
    // Configure orchestration configuration
    OrchestrationConfiguration orchConfig = new OrchestrationConfiguration("orchestration-sharding-data-source", zkConfig, false, OrchestrationConfiguration.SHARDING);
    
    // Get data source
    DataSource dataSource = OrchestrationShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, new ConcurrentHashMap(), new Properties(), orchConfig);
```

### Configure orchestration with yaml

To configure by yaml, similar with the configuration method of java codes:

```yaml
orchestration:
  name: orchestration-sharding-data-source
  type: SHARDING
  overwrite: false
  zookeeper:
    serverLists: localhost:2181
    namespace: sharding-jdbc-orchestration
```

```java
    DataSource dataSource = YamlOrchestrationShardingDataSourceFactory.createDataSource(yamlFile);
```

## Using spring

### Add maven dependency

```xml
<!-- for spring boot -->
<dependency>
    <groupId>shardingsphere</groupId>
    <artifactId>sharding-jdbc-orchestration-spring-boot-starter</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>

<!-- for spring namespace -->
<dependency>
    <groupId>shardingsphere</groupId>
    <artifactId>sharding-jdbc-orchestration-spring-namespace</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>
```

### Configure orchestration with spring boot

```properties
sharding.jdbc.config.orchestration.name=orchestration-sharding-data-source
sharding.jdbc.config.orchestration.type=sharding
sharding.jdbc.config.orchestration.overwrite=false
sharding.jdbc.config.orchestration.zookeeper.server-lists=localhost:2181
sharding.jdbc.config.orchestration.zookeeper.namespace=sharding-jdbc-orchestration
```

### Configure orchestration with spring namespace

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:sharding="http://shardingsphere.io/schema/shardingsphere/orchestration/sharding" 
    xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://shardingsphere.io/schema/shardingsphere/orchestration/sharding 
                        http://shardingsphere.io/schema/shardingsphere/orchestration/sharding/sharding.xsd
                        ">
    <sharding:data-source id="shardingDatabaseTableDataSource" registry-center-ref="regCenter" />
</beans>
```

More details please reference [configuration manual](/en/manual/sharding-jdbc/configuration/).
