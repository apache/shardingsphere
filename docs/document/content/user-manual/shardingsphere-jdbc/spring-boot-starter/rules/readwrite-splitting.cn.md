+++
title = "读写分离"
weight = 2
+++

## 配置项说明

### 静态读写分离

```properties
spring.shardingsphere.datasource.names= # 省略数据源配置，请参考使用手册

spring.shardingsphere.rules.readwrite-splitting.data-sources.<readwrite-splitting-data-source-name>.static-strategy.write-data-source-name= # 写库数据源名称
spring.shardingsphere.rules.readwrite-splitting.data-sources.<readwrite-splitting-data-source-name>.static-strategy.read-data-source-names= # 读库数据源列表，多个从数据源用逗号分隔
spring.shardingsphere.rules.readwrite-splitting.data-sources.<readwrite-splitting-data-source-name>.load-balancer-name= # 负载均衡算法名称

# 负载均衡算法配置
spring.shardingsphere.rules.readwrite-splitting.load-balancers.<load-balance-algorithm-name>.type= # 负载均衡算法类型
spring.shardingsphere.rules.readwrite-splitting.load-balancers.<load-balance-algorithm-name>.props.xxx= # 负载均衡算法属性配置
```

### 动态读写分离

```properties
spring.shardingsphere.datasource.names= # 省略数据源配置，请参考使用手册

spring.shardingsphere.rules.readwrite-splitting.data-sources.<readwrite-splitting-data-source-name>.dynamic-strategy.auto-aware-data-source-name= # 数据库发现逻辑数据源名称
spring.shardingsphere.rules.readwrite-splitting.data-sources.<readwrite-splitting-data-source-name>.dynamic-strategy.write-data-source-query-enabled= # 读库全部下线，主库是否承担读流量
spring.shardingsphere.rules.readwrite-splitting.data-sources.<readwrite-splitting-data-source-name>.load-balancer-name= # 负载均衡算法名称

# 负载均衡算法配置
spring.shardingsphere.rules.readwrite-splitting.load-balancers.<load-balance-algorithm-name>.type= # 负载均衡算法类型
spring.shardingsphere.rules.readwrite-splitting.load-balancers.<load-balance-algorithm-name>.props.xxx= # 负载均衡算法属性配置
```


算法类型的详情，请参见[内置负载均衡算法列表](/cn/user-manual/shardingsphere-jdbc/builtin-algorithm/load-balance)。
查询一致性路由的详情，请参见[使用规范](/cn/features/readwrite-splitting/use-norms)。
