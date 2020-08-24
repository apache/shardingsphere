+++
title = "读写分离"
weight = 2
+++

## 配置项说明

```yaml
dataSources: # 省略数据源配置

rules:
- !MASTER_SLAVE
  dataSources:
    <data-source-name> (+): # 读写分离逻辑数据源名称
      masterDataSourceName: # 主库数据源名称
      slaveDataSourceNames: 
        - <slave-data_source-name> (+) # 从库数据源名称
      loadBalancerName: # 负载均衡算法名称
  
  # 负载均衡算法配置
  loadBalancers:
    <load-balancer-name> (+): # 负载均衡算法名称
      type: # 负载均衡算法类型
      props: # 负载均衡算法属性配置
        # ...

props:
  # ...
```

算法类型的详情，请参见[内置负载均衡算法列表](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/load-balance)。
