+++
pre = "<b>5.2. </b>"
title = "配置"
weight = 2
chapter = true
+++

## RuleBuilder

| *SPI 名称*                                         | *详细说明*                                           |
| ------------------------------------------------- | --------------------------------------------------- |
| RuleBuilder                                       | 用于将用户配置转化为规则对象                             |

| *已知实现类*                                        | *详细说明*                                           |
| ------------------------------------------------- | --------------------------------------------------- |
| AlgorithmProvidedReadwriteSplittingRuleBuilder    | 用于将基于算法的读写分离用户配置转化为读写分离规则对象        |
| AlgorithmProvidedDatabaseDiscoveryRuleBuilder     | 用于将基于算法的数据库发现用户配置转化为数据库发现规则对象     |
| AlgorithmProvidedShardingRuleBuilder              | 用于将基于算法的分片用户配置转化为分片规则对象               |
| AlgorithmProvidedEncryptRuleBuilder               | 用于将基于算法的加密用户配置转化为加密规则对象               |
| AlgorithmProvidedShadowRuleBuilder                | 用于将基于算法的影子库用户配置转化为影子库规则对象             |
| ReadwriteSplittingRuleBuilder                     | 用于将读写分离用户配置转化为读写分离规则对象                |
| DatabaseDiscoveryRuleBuilder                      | 用于将数据库发现用户配置转化为数据库发现规则对象              |
| SingleTableRuleBuilder                            | 用于将单表用户配置转化为单表规则对象                        |
| AuthorityRuleBuilder                              | 用于将权限用户配置转化为权限规则对象                        |
| ShardingRuleBuilder                               | 用于将分片用户配置转化为分片规则对象                        |
| EncryptRuleBuilder                                | 用于将加密用户配置转化为加密规则对象                        |
| ShadowRuleBuilder                                 | 用于将影子库用户配置转化为影子库规则对象                     |
| TransactionRuleBuilder                            | 用于将事务用户配置转化为事务规则对象                  　　   |

## YamlRuleConfigurationSwapper

| *SPI 名称*                                                         | *详细说明*                                         |
| ----------------------------------------------------------------- | ------------------------------------------------- |
| YamlRuleConfigurationSwapper                                      | 用于将 YAML 配置转化为标准用户配置                     |

| *已知实现类*                                                        | *详细说明*                                         |
| ----------------------------------------------------------------- | ------------------------------------------------- |
| ReadwriteSplittingRuleAlgorithmProviderConfigurationYamlSwapper   | 用于将基于算法的读写分离配置转化为读写分离标准配置         |
| DatabaseDiscoveryRuleAlgorithmProviderConfigurationYamlSwapper    | 用于将基于算法的数据库发现配置转化为数据库发现标准配置      |
| ShardingRuleAlgorithmProviderConfigurationYamlSwapper             | 用于将基于算法的分片配置转化为分片标准配置                |
| EncryptRuleAlgorithmProviderConfigurationYamlSwapper              | 用于将基于算法的加密配置转化为加密标准配置                |
| ShadowRuleAlgorithmProviderConfigurationYamlSwapper               | 用于将基于算法的影子库配置转化为影子库标准配置      　　   |
| ReadwriteSplittingRuleConfigurationYamlSwapper                    | 用于将读写分离的 YAML 配置转化为读写分离标准配置          |
| DatabaseDiscoveryRuleConfigurationYamlSwapper                     | 用于将数据库发现的 YAML 配置转化为数据库发现标准配置       |
| AuthorityRuleConfigurationYamlSwapper                             | 用于将权限规则的 YAML 配置转化为权限规则标准配置          |
| ShardingRuleConfigurationYamlSwapper                              | 用于将分片的 YAML 配置转化为分片标准配置                |
| EncryptRuleConfigurationYamlSwapper                               | 用于将加密的 YAML 配置转化为加密标准配置                |
| ShadowRuleConfigurationYamlSwapper                                | 用于将影子库的 YAML 配置转化为影子库标准配置             |
| TransactionRuleConfigurationYamlSwapper                           | 用于将事务的 YAML 配置转化为事务标准配置       　　      |

## ShardingSphereYamlConstruct

| *SPI 名称*                                     | *详细说明*                        |
| ---------------------------------------------- | ------------------------------- |
| ShardingSphereYamlConstruct                    | 用于将定制化对象和 YAML 相互转化    |

| *已知实现类*                                    | *详细说明*                        |
| ---------------------------------------------- | -------------------------------- |
| NoneShardingStrategyConfigurationYamlConstruct | 用于将不分片策略对象和 YAML 相互转化 |
