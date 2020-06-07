+++
title = "读写分离"
weight = 2
+++

## 配置入口

类名称：org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration

可配置属性：

| *名称*           | *数据类型*                                            | *说明*       |
| --------------- | ---------------------------------------------------- | ------------ |
| dataSources (+) | Collection\<MasterSlaveDataSourceRuleConfiguration\> | 主从数据源列表 |

## 主从数据源配置

类名称：org.apache.shardingsphere.masterslave.api.config.rule.MasterSlaveDataSourceRuleConfiguration

可配置属性：

| *名称*                  | *数据类型*                        | *说明*            | *默认值*        |
| ----------------------- | -------------------------------- | ---------------- | -------------- |
| name                    | String                           | 读写分离数据源名称 | -              |
| masterDataSourceName    | String                           | 主库数据源名称     | -              |
| slaveDataSourceNames    | Collection\<String\>             | 从库数据源名称列表 | -              |
| loadBalanceStrategy (?) | LoadBalanceStrategyConfiguration | 从库负载均衡算法   | 轮询负载均衡算法 |

## 从库负载均衡策略配置

| *名称*          | *数据类型*  | *说明*               | *默认值*        |
| -------------- | ---------- | -------------------- | -------------- |
| type           | String     | 从库负载均衡算法类型    | -              |
| properties (?) | Properties | 从库负载均衡算法属性配置 | 空             |

Apache ShardingSphere 内置的从库负载均衡算法实现类包括：

### 轮询算法

类名称：org.apache.shardingsphere.masterslave.strategy.RoundRobinMasterSlaveLoadBalanceAlgorithm

可配置属性：无

### 随机访问算法

类名称：org.apache.shardingsphere.masterslave.strategy.RandomMasterSlaveLoadBalanceAlgorithm

可配置属性：无
