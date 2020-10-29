+++
title = "分布式治理"
weight = 6
+++

## 配置项说明

### 治理

```properties
spring.shardingsphere.governance.name= # 治理名称
spring.shardingsphere.governance.registry-center.type= # 治理持久化类型。如：Zookeeper, etcd, Apollo, Nacos
spring.shardingsphere.governance.registry-center.server-lists= # 治理服务列表。包括 IP 地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181
spring.shardingsphere.governance.registry-center.props= # 其它配置
spring.shardingsphere.governance.additional-config-center.type= # 可选的配置中心类型。如：Zookeeper, etcd, Apollo, Nacos
spring.shardingsphere.governance.additional-config-center.server-lists= # 可选的配置中心服务列表。包括 IP 地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181
spring.shardingsphere.governance.additional-config-center.props= # 可选的配置中心其它配置
spring.shardingsphere.governance.overwrite= # 本地配置是否覆盖配置中心配置。如果可覆盖，每次启动都以本地配置为准.
```
