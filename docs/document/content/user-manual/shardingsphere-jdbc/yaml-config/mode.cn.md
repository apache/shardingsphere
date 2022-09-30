+++
title = "模式配置"
weight = 1
+++

## 参数解释

```yaml
mode (?): # 不配置则默认单机模式
  type: # 运行模式类型。可选配置：Standalone、Cluster
  repository (?): # 久化仓库配置
```

### 单机模式

```yaml
mode:
  type: Standalone
  repository:
    type: # 持久化仓库类型
    props: # 持久化仓库所需属性
      foo_key: foo_value
      bar_key: bar_value
```

### 集群模式 (推荐)

```yaml
mode:
  type: Cluster
  repository:
    type: # 持久化仓库类型
    props: # 持久化仓库所需属性
      namespace: # 注册中心命名空间
      server-lists: # 注册中心连接地址
      foo_key: foo_value
      bar_key: bar_value
```

## 注意事项

1. 生产环境建议使用集群模式部署。
1. 集群模式部署推荐使用 `ZooKeeper` 注册中心。

## 配置示例

### 单机模式

```yaml
mode:
  type: Standalone
  repository:
    type: JDBC
```

### 集群模式 (推荐)

```yaml
mode:
  type: Cluster
  repository:
    type: ZooKeeper
    props: 
      namespace: governance
      server-lists: localhost:2181
      retryIntervalMilliseconds: 500
      timeToLiveSeconds: 60
```

## 相关参考

- [ZooKeeper 注册中心安装与使用](https://zookeeper.apache.org/doc/r3.7.1/zookeeperStarted.html)
- 持久化仓库类型的详情，请参见[内置持久化仓库类型列表](/cn/user-manual/common-config/builtin-algorithm/metadata-repository/)。
