+++
toc = true
title = "编排治理"
weight = 4
+++

使用治理功能需要指定一个注册中心。配置将全部存入注册中心，可以在每次启动时使用本地配置覆盖注册中心配置，也可以只通过注册中心读取配置。

## 不使用Spring

### 引入Maven依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-jdbc-orchestration</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>

<!--若使用zookeeper, 请加入下面Maven坐标-->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-orchestration-reg-zookeeper-curator</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>
```

### 基于Java编码的规则配置

```java
    // 省略配置dataSourceMap以及shardingRuleConfig
    // ...

    // 配置注册中心
    RegistryCenterConfiguration regConfig = new RegistryCenterConfiguration("zookeeper");
    regConfig.setServerLists("localhost:2181");
    regConfig.setNamespace("sharding-sphere-orchestration");

    // 配置治理
    OrchestrationConfiguration orchConfig = new OrchestrationConfiguration("orchestration-sharding-data-source", regConfig, false);

    // 获取数据源对象
    DataSource dataSource = OrchestrationShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, new Properties(), orchConfig);
```

### 基于Yaml的规则配置

或通过Yaml方式配置，与以上配置等价：

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

## 使用Spring

### 引入Maven依赖

```xml
<!-- for spring boot -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-jdbc-orchestration-spring-boot-starter</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>

<!--若使用zookeeper, 请加入下面Maven坐标-->
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

<!--若使用zookeeper, 请加入下面Maven坐标-->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-orchestration-reg-zookeeper-curator</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>
```

### 基于Spring boot的规则配置

```properties
spring.shardingsphere.orchestration.name=orchestration-sharding-data-source
spring.shardingsphere.orchestration.overwrite=false
spring.shardingsphere.orchestration.registry.type=zookeeper
spring.shardingsphere.orchestration.registry.server-lists=localhost:2181
spring.shardingsphere.orchestration.registry.namespace=sharding-jdbc-orchestration
```

### 基于Spring命名空间的规则配置

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

更多的详细配置请参考[配置手册](/cn/manual/sharding-jdbc/configuration/)。
