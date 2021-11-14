+++
title = "模式配置"
weight = 1
+++

缺省配置为使用内存模式。

## 配置项说明

```properties
spring.shardingsphere.mode.type= # 运行模式类型。可选配置：Memory、Standalone、Cluster
spring.shardingsphere.mode.repository= # 持久化仓库配置。Memory 类型无需持久化
spring.shardingsphere.mode.overwrite= # 是否使用本地配置覆盖持久化配置
```

### 内存模式

```properties
spring.shardingsphere.mode.type=Memory
```

### 单机模式

```properties
spring.shardingsphere.mode.type=Standalone
spring.shardingsphere.mode.repository.type= # 持久化仓库类型
spring.shardingsphere.mode.repository.props.<key>= # 持久化仓库所需属性
spring.shardingsphere.mode.overwrite= # 是否使用本地配置覆盖持久化配置
```

### 集群模式

```properties
spring.shardingsphere.mode.type=Cluster
spring.shardingsphere.mode.repository.type= # 持久化仓库类型
spring.shardingsphere.mode.repository.namespace= # 注册中心命名空间
spring.shardingsphere.mode.repository.serverLists= # 注册中心连接地址
spring.shardingsphere.mode.repository.props.<key>= # 持久化仓库所需属性
spring.shardingsphere.mode.overwrite= # 是否使用本地配置覆盖持久化配置
```

持久化仓库类型的详情，请参见[内置持久化仓库类型列表](/cn/user-manual/shardingsphere-jdbc/builtin-algorithm/metadata-repository/)。
