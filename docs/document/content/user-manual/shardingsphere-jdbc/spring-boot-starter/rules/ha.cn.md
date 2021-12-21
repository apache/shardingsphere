+++
title = "高可用"
weight = 3
+++

## 配置项说明

```properties
spring.shardingsphere.datasource.names= # 省略数据源配置，请参考使用手册

spring.shardingsphere.rules.database-discovery.data-sources.<database-discovery-data-source-name>.data-source-names= # 数据源名称，多个数据源用逗号分隔 如：ds_0, ds_1
spring.shardingsphere.rules.database-discovery.data-sources.<database-discovery-data-source-name>.discovery-heartbeat-name= # 检测心跳名称
spring.shardingsphere.rules.database-discovery.data-sources.<database-discovery-data-source-name>.discovery-type-name= # 高可用类型名称

spring.shardingsphere.rules.database-discovery.discovery-heartbeats.<discovery-heartbeat-name>.props.keep-alive-cron= # cron 表达式，如：'0/5 * * * * ?'

spring.shardingsphere.rules.database-discovery.discovery-types.<discovery-type-name>.type= # 高可用类型，如： MGR、openGauss
spring.shardingsphere.rules.database-discovery.discovery-types.<discovery-type-name>.props.group-name= # 高可用类型必要参数，如 MGR 的 group-name
```
