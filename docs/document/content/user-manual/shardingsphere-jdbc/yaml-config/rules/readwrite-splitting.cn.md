+++
title = "读写分离"
weight = 2
+++

## 配置项说明

```yaml
rules:
- !READWRITE_SPLITTING
  dataSources:
    <data-source-name> (+): # 读写分离逻辑数据源名称
        type: # 读写分离类型，比如：Static，Dynamic
        props:
          auto-aware-data-source-name: # 自动发现数据源名称（与数据库发现配合使用）
          write-data-source-query-enabled: # 从库全部宕机、主库是否承担读流量（与数据库发现配合使用）
          write-data-source-name: # 写库数据源名称
          read-data-source-names: # 读库数据源名称，多个从数据源用逗号分隔
      loadBalancerName: # 负载均衡算法名称
  
  # 负载均衡算法配置
  loadBalancers:
    <load-balancer-name> (+): # 负载均衡算法名称
      type: # 负载均衡算法类型
      props: # 负载均衡算法属性配置
        # ...
```

算法类型的详情，请参见[内置负载均衡算法列表](/cn/user-manual/shardingsphere-jdbc/builtin-algorithm/load-balance)。
查询一致性路由的详情，请参见[使用规范](/cn/features/readwrite-splitting/use-norms)。
