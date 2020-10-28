+++
title = "读写分离"
weight = 2
+++

## 配置项说明

```properties
spring.shardingsphere.datasource.names= # 省略数据源配置

spring.shardingsphere.rules.replica-query.data-sources.<replica-query-data-source-name>.primary-data-source-name= # 主数据源名称
spring.shardingsphere.rules.replica-query.data-sources.<replica-query-data-source-name>.replica-data-source-names= # 从数据源名称，多个从数据源用逗号分隔
spring.shardingsphere.rules.replica-query.data-sources.<replica-query-data-source-name>.load-balancer-name= # 负载均衡算法名称

# 负载均衡算法配置
spring.shardingsphere.rules.replica-query.load-balancers.<load-balance-algorithm-name>.type= # 负载均衡算法类型
spring.shardingsphere.rules.replica-query.load-balancers.<load-balance-algorithm-name>.props.xxx= # 负载均衡算法属性配置
```

算法类型的详情，请参见[内置负载均衡算法列表](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/load-balance)。
