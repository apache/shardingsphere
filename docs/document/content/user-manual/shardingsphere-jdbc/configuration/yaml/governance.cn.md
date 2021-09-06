+++
title = "分布式治理"
weight = 5
+++

## 配置项说明

### 治理

```yaml
schemaName: #该参数为可选项，不配置则默认采用logic_db作为schemaName。通过该参数与治理模块可实现JDBC与PROXY同时在线
mode:
  type: Cluster #配置存储类型。如：Cluster、Standalone、Memory(默认)
  repository:
    type: ZooKeeper # 治理持久化类型。如：Cluster(Zookeeper, etcd)，Standalone(Local)
    props:
      namespace: demo_yaml_ds_sharding # 注册中心命名空间
      server-lists: localhost:2181 # 治理服务列表。包括 IP 地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181 
  overwrite: true # 本地配置是否覆盖配置中心配置。如果可覆盖，每次启动都以本地配置为准
```
