+++
toc = true
title = "数据治理"
weight = 4
+++

使用数据治理功能需要指定一个注册中心。配置将全部存入注册中心，可以在每次启动时使用本地配置覆盖注册中心配置，也可以只通过注册中心读取配置。

## 不使用Spring

### 引入Maven依赖

```xml
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-orchestration</artifactId>
    <version>${sharding-jdbc.version}</version>
</dependency>
```

### 基于Java编码的规则配置

```java
    // 省略配置dataSourceMap以及shardingRuleConfig
    // ...
    
    // 配置基于Zookeeper的注册中心
    ZookeeperConfiguration zkConfig = new ZookeeperConfiguration();
    zkConfig.setServerLists("localhost:2181");
    zkConfig.setNamespace("sharding-jdbc-orchestration");
    
    // 配置数据治理
    OrchestrationConfiguration orchConfig = new OrchestrationConfiguration("orchestration-sharding-data-source", zkConfig, false, OrchestrationConfiguration.SHARDING);
    
    // 获取数据源对象
    DataSource dataSource = OrchestrationShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, new ConcurrentHashMap(), new Properties(), orchConfig);
```

### 基于Yaml的规则配置

或通过Yaml方式配置，与以上配置等价：

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

## 使用Spring

### 引入Maven依赖

```xml
<!-- for spring boot -->
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-orchestration-spring-boot-starter</artifactId>
    <version>${sharding-jdbc.version}</version>
</dependency>

<!-- for spring namespace -->
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-orchestration-spring-namespace</artifactId>
    <version>${sharding-jdbc.version}</version>
</dependency>
```

### 基于Spring boot的规则配置

```properties
sharding.jdbc.config.orchestration.name=orchestration-sharding-data-source
sharding.jdbc.config.orchestration.type=sharding
sharding.jdbc.config.orchestration.overwrite=false
sharding.jdbc.config.orchestration.zookeeper.server-lists=localhost:2181
sharding.jdbc.config.orchestration.zookeeper.namespace=sharding-jdbc-orchestration
```

### 基于Spring命名空间的规则配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:sharding="http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding" 
    xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding/sharding.xsd
                        ">
    <sharding:data-source id="shardingDatabaseTableDataSource" registry-center-ref="regCenter" />
</beans>
```

更多的详细配置请参考[配置手册](/manual/sharding-jdbc/configuration/)。
