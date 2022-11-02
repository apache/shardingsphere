+++
title = "模式配置"
weight = 1
+++

## 参数解释

```properties
spring.shardingsphere.mode.type= # 运行模式类型。可选配置：Standalone、Cluster
spring.shardingsphere.mode.repository= # 持久化仓库配置。
```

### 单机模式

```properties
spring.shardingsphere.mode.type=Standalone
spring.shardingsphere.mode.repository.type= # 持久化仓库类型
spring.shardingsphere.mode.repository.props.<key>= # 持久化仓库所需属性
```

### 集群模式 (推荐)

```properties
spring.shardingsphere.mode.type=Cluster
spring.shardingsphere.mode.repository.type= # 持久化仓库类型
spring.shardingsphere.mode.repository.props.namespace= # 注册中心命名空间
spring.shardingsphere.mode.repository.props.server-lists= # 注册中心连接地址
spring.shardingsphere.mode.repository.props.<key>= # 持久化仓库所需属性
```

## 注意事项

1. 生产环境建议使用集群模式部署。
1. 集群模式部署推荐使用 `ZooKeeper` 注册中心。
1. `ZooKeeper` 存在配置信息时，则以 `ZooKeeper` 中的配置为准。

## 操作步骤

1. 引入 MAVEN 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-boot-starter</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

注意：请将 `${latest.release.version}` 更改为实际的版本号。

## 配置示例

### 单机模式

```properties
spring.shardingsphere.mode.type=Standalone
spring.shardingsphere.mode.repository.type=JDBC
```

### 集群模式 (推荐)

```properties
spring.shardingsphere.mode.type=Cluster
spring.shardingsphere.mode.repository.type=ZooKeeper
spring.shardingsphere.mode.repository.props.namespace=governance
spring.shardingsphere.mode.repository.props.server-lists=localhost:2181
spring.shardingsphere.mode.repository.props.retryIntervalMilliseconds=500
spring.shardingsphere.mode.repository.props.timeToLiveSeconds=60
```

## 相关参考

- [ZooKeeper 注册中心安装与使用](https://zookeeper.apache.org/doc/r3.7.1/zookeeperStarted.html)
- - 持久化仓库类型的详情，请参见[内置持久化仓库类型列表](/cn/user-manual/common-config/builtin-algorithm/metadata-repository/)。
