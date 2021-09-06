+++
title = "分布式治理"
weight = 5
+++

## 配置项说明

### 治理

```properties
spring.shardingsphere.schema.name= # JDBC数据源别名

spring.shardingsphere.mode.type=Cluster  # 配置存储模式：Cluster、Standalone、Memory(默认)
spring.shardingsphere.mode.repository.type=ZooKeeper  # 治理持久化类型。如：Cluster(Zookeeper, etcd)，Standalone(Local)
spring.shardingsphere.mode.repository.props.namespace=demo_spring_boot_ds_sharding # 实例命名空间
spring.shardingsphere.mode.repository.props.server-lists=localhost:2181 # 治理服务列表。包括 IP 地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181
spring.shardingsphere.mode.overwrite=true # 本地配置是否覆盖配置中心配置。如果可覆盖，每次启动都以本地配置为准.
```
