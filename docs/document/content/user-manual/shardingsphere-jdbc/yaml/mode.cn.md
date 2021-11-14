+++
title = "模式配置"
weight = 1
+++

## 配置项说明

```yaml
mode (?): # 不配置则默认内存模式
  type: # 运行模式类型。可选配置：Memory、Standalone、Cluster
  repository (?): # 久化仓库配置。Memory 类型无需持久化
  overwrite: # 是否使用本地配置覆盖持久化配置
```

### 内存模式

```yaml
mode:
  type: Memory
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
  overwrite: # 是否使用本地配置覆盖持久化配置
```

### 集群模式

```yaml
mode:
  type: Cluster
  repository:
    type: # 持久化仓库类型
    namespace: # 注册中心命名空间
    serverLists: # 注册中心连接地址
    props: # 持久化仓库所需属性
      foo_key: foo_value
      bar_key: bar_value
  overwrite: # 是否使用本地配置覆盖持久化配置
```

持久化仓库类型的详情，请参见[内置持久化仓库类型列表](/cn/user-manual/shardingsphere-jdbc/builtin-algorithm/metadata-repository/)。
