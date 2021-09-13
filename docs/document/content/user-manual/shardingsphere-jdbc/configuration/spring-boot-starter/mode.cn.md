+++
title = "Mode"
weight = 5
+++

## 配置项说明

### Memory mode
```properties
spring.shardingsphere.mode.type= # Memory
```

### Standalone mode
```properties
spring.shardingsphere.mode.type= # Standalone
spring.shardingsphere.mode.repository.type= # Standalone 配置持久化类型。如：File
spring.shardingsphere.mode.repository.props.path= # 配置信息存储路径
spring.shardingsphere.mode.overwrite= # 本地配置是否覆盖文件配置。如果可覆盖，每次启动都以本地配置为准。
```

### Cluster mode
```properties
spring.shardingsphere.mode.type= # Cluster
spring.shardingsphere.mode.repository.type= # Cluster 持久化类型。如：Zookeeper，Etcd
spring.shardingsphere.mode.repository.props.namespace= # Cluster 实例名称
spring.shardingsphere.mode.repository.props.server-lists= # Zookeeper 或 Etcd 服务列表。包括 IP 地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181
spring.shardingsphere.mode.overwrite= # 本地配置是否覆盖配置中心配置。如果可覆盖，每次启动都以本地配置为准。
```
