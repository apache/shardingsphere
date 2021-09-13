+++
title = "Mode"
weight = 5
+++

## 配置项说明

### Memory mode
```yaml
schema:
  name: # JDBC数据源别名，该参数可实现JDBC与PROXY共享配置
mode:
  type: # Memory
```

### Standalone mode
```yaml
schema:
  name: # JDBC数据源别名，该参数可实现JDBC与PROXY共享配置
mode:
  type: # Standalone
  repository:
    type: # Standalone 配置持久化类型。如：File
    props:
      path: # 配置信息存储路径
  overwrite: true # 本地配置是否覆盖文件配置。如果可覆盖，每次启动都以本地配置为准。
```

### Cluster mode

```yaml
schema:
  name: # JDBC数据源别名，该参数可实现JDBC与PROXY共享配置
mode:
  type: # Cluster
  repository:
    type: # Cluster 持久化类型。如：Zookeeper，Etcd
    props:
      namespace: # Cluster 实例名称
      server-lists: # Zookeeper 或 Etcd 服务列表。包括 IP 地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181
  overwrite: true # 本地配置是否覆盖配置中心配置。如果可覆盖，每次启动都以本地配置为准。
```
