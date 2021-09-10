+++
title = "mode"
weight = 5
+++

## 配置项说明

### Memory mode
```yaml
schema:
  name: # 该参数为可选项，不配置则默认采用logic_db作为schemaName。通过该参数与治理模块可实现JDBC与PROXY同时在线
mode:
  type: # Memory 模式
```

### Standalone mode
```yaml
schema:
  name: # 该参数为可选项，不配置则默认采用logic_db作为schemaName。通过该参数与治理模块可实现JDBC与PROXY同时在线
mode:
  type: # Standalone 模式
  repository:
    type: # File 类型
    props:
      path: # 配置信息存储路径
  overwrite: true # 本地配置是否覆盖文件配置。如果可覆盖，每次启动都以本地配置为准。
```

### Cluster mode

```yaml
schema:
  name: # 该参数为可选项，不配置则默认采用logic_db作为schemaName。通过该参数与治理模块可实现JDBC与PROXY同时在线
mode:
  type: # Cluster 模式
  repository:
    type: # ZooKeeper 或 Etcd
    props:
      namespace: # Cluster 实例名称
      server-lists: # Zookeeper 或 Etcd 服务列表。包括 IP 地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181
  overwrite: true # 本地配置是否覆盖配置中心配置。如果可覆盖，每次启动都以本地配置为准。
```
