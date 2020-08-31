+++
title = "分布式治理"
weight = 6
+++

## 配置项说明

### 治理

```yaml
governance:  
  name: # 治理名称
  registryCenter: # 配置中心
    type: # 治理持久化类型。如：Zookeeper, etcd
    serverLists: # 治理服务列表。包括 IP 地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181 
  additionalConfigCenter:
    type: # 治理持久化类型。如：Zookeeper, etcd, Nacos, Apollo
    serverLists: # 治理服务列表。包括 IP 地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181 
  overwrite: # 本地配置是否覆盖配置中心配置。如果可覆盖，每次启动都以本地配置为准
```
