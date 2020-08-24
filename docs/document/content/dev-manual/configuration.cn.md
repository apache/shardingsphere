+++
pre = "<b>5.2. </b>"
title = "配置"
weight = 2
chapter = true
+++

## ShardingSphereRuleBuilder

| *SPI 名称*                | *详细说明*                               |
| ------------------------- | -------------------------------------- |
| ShardingSphereRuleBuilder | 用于将用户配置转化为规则对象               |

| *已知实现类*               | *详细说明*                               |
| ------------------------- | --------------------------------------- |
| ShardingRuleBuilder       | 用于将分片用户配置转化为分片规则对象        |
| MasterSlaveRuleBuilder    | 用于将读写分离用户配置转化为读写分离规则对象 |
| ReplicaRuleBuilder        | 用于将多副本用户配置转化为多副本规则对象     |
| EncryptRuleBuilder        | 用于将加密用户配置转化为加密规则对象        |
| ShadowRuleBuilder         | 用于将影子库用户配置转化为影子库规则对象     |

## YamlRuleConfigurationSwapper

| *SPI 名称*                              | *详细说明*                                   |
| --------------------------------------- | ------------------------------------------ |
| YamlRuleConfigurationSwapper            | 用于将 YAML 配置转化为标准用户配置             |

| *已知实现类*                             | *详细说明*                                   |
| --------------------------------------- | ------------------------------------------- |
| ShardingRuleConfigurationYamlSwapper    | 用于将分片的 YAML 配置转化为分片标准配置        |
| MasterSlaveRuleConfigurationYamlSwapper | 用于将读写分离的 YAML 配置转化为读写分离标准配置 |
| ReplicaRuleConfigurationYamlSwapper     | 用于将多副本的 YAML 分片配置转化为多副本标准配置 |
| EncryptRuleConfigurationYamlSwapper     | 用于将加密的 YAML 分片配置转化为加密标准配置     |
| ShadowRuleConfigurationYamlSwapper      | 用于将影子库的 YAML 分片配置转化为影子库标准配置 |

## ShardingSphereYamlConstruct

| *SPI 名称*                                     | *详细说明*                        |
| ---------------------------------------------- | ------------------------------- |
| ShardingSphereYamlConstruct                    | 用于将定制化对象和 YAML 相互转化    |

| *已知实现类*                                    | *详细说明*                        |
| ---------------------------------------------- | -------------------------------- |
| NoneShardingStrategyConfigurationYamlConstruct | 用于将不分片策略对象和 YAML 相互转化 |
