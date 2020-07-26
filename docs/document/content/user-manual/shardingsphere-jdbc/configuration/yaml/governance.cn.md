+++
title = "分布式治理"
weight = 6
+++

## 配置项说明

### 治理

```yaml
orchestration:
  demo_yaml_ds_sharding: #治理实例名称
    instanceType: #治理实例类型。如：zookeeper, etcd, apollo, nacos
    serverLists: #治理服务列表。包括 IP 地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181
    namespace: #治理命名空间
    props: #配置本实例需要的其他参数，例如 ZooKeeper 的连接参数等
      overwrite: #本地配置是否覆盖配置中心配置。如果可覆盖，每次启动都以本地配置为准
```

### 集群管理

```yaml
cluster:
  heartbeat:
    sql: #心跳检测 SQL
    threadCount: #心跳检测线程池大小
    interval: #心跳检测间隔时间 (s)
    retryEnable: #是否支持失败重试，可设置 true 或 false
    retryMaximum: #最大重试次数
    retryInterval: #重试间隔时间 (s)
```
