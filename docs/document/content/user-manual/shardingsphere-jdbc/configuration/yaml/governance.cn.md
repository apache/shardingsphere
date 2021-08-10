+++
title = "分布式治理"
weight = 5
+++

## 配置项说明

### 治理

```yaml
schemaName: #可选项, 替换原有的logic_db, 可通过该参数与Proxy同时在线
governance:
  registryCenter: # 注册中心
    type: # 治理持久化类型。如：Zookeeper, etcd
    namespace: # 注册中心命名空间
    serverLists: # 治理服务列表。包括 IP 地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181 
  overwrite: # 本地配置是否覆盖配置中心配置。如果可覆盖，每次启动都以本地配置为准
```
