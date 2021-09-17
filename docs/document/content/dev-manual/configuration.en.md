+++
pre = "<b>5.2. </b>"
title = "Configuration"
weight = 2
chapter = true
+++

## RuleBuilder

| *SPI Name*                                        | *Description*                                                                                                       |
| ------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| RuleBuilder                                       | Used to convert user configurations to rule objects                                                                 |

| *Implementation Class*                            | *Description*                                                                                                       |
| ------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| AlgorithmProvidedReadwriteSplittingRuleBuilder    | Used to convert algorithm-based read-write separation user configuration into read-write separation rule objects    |
| AlgorithmProvidedDatabaseDiscoveryRuleBuilder     | Used to convert algorithm-based database discovery user configuration into database discovery rule objects          |
| AlgorithmProvidedShardingRuleBuilder              | Used to convert algorithm-based sharding user configuration into sharding rule objects                              |
| AlgorithmProvidedEncryptRuleBuilder               | Used to convert algorithm-based encryption user configuration into encryption rule objects                          |
| AlgorithmProvidedShadowRuleBuilder                | Used to convert algorithm-based shadow database user configuration into shadow database rule objects                |
| ReadwriteSplittingRuleBuilder                     | Used to convert read-write separation user configuration into read-write separation rule objects                    |
| DatabaseDiscoveryRuleBuilder                      | Used to convert database discovery user configuration into database discovery rule objects                          |
| SingleTableRuleBuilder                            | Used to convert single-table user configuration into a single-table rule objects                                    |
| AuthorityRuleBuilder                              | Used to convert permission user configuration into permission rule objects                                          |
| ShardingRuleBuilder                               | Used to convert sharding user configuration into sharding rule objects                                              |
| EncryptRuleBuilder                                | Used to convert encrypted user configuration into encryption rule objects                                           |
| ShadowRuleBuilder                                 | Used to convert shadow database user configuration into shadow database rule objects                                |
| TransactionRuleBuilder                            | Used to convert transaction user configuration into transaction rule objects                                        |

## YamlRuleConfigurationSwapper

| *SPI Name*                                                        | *Description*                                                                                                            |
| ----------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| YamlRuleConfigurationSwapper                                      | Used to convert YAML configuration to standard user configuration                                                        |

| *Implementation Class*                                            | *Description*                                                                                                            |
| ----------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| ReadwriteSplittingRuleAlgorithmProviderConfigurationYamlSwapper   | Used to convert algorithm-based read-write separation configuration into read-write separation standard configuration    |
| DatabaseDiscoveryRuleAlgorithmProviderConfigurationYamlSwapper    | Used to convert algorithm-based database discovery configuration into database discovery standard configuration          |
| ShardingRuleAlgorithmProviderConfigurationYamlSwapper             | Used to convert algorithm-based sharding configuration into sharding standard configuration                              |
| EncryptRuleAlgorithmProviderConfigurationYamlSwapper              | Used to convert algorithm-based encryption configuration into encryption standard configuration                        　|
| ShadowRuleAlgorithmProviderConfigurationYamlSwapper               | Used to convert algorithm-based shadow database configuration into shadow database standard configuration                |
| ReadwriteSplittingRuleConfigurationYamlSwapper                    | Used to convert the YAML configuration of read-write separation into the standard configuration of read-write separation |
| DatabaseDiscoveryRuleConfigurationYamlSwapper                     | Used to convert the YAML configuration of database discovery into the standard configuration of database discovery       |
| AuthorityRuleConfigurationYamlSwapper                             | Used to convert the YAML configuration of permission rules into standard configuration of permission rules               |
| ShardingRuleConfigurationYamlSwapper                              | Used to convert the YAML configuration of the shard into the standard configuration of the shard                         |
| EncryptRuleConfigurationYamlSwapper                               | Used to convert encrypted YAML configuration into encrypted standard configuration                                       |
| ShadowRuleConfigurationYamlSwapper                                | Used to convert the YAML configuration of the shadow database into the standard configuration of the shadow database     |
| TransactionRuleConfigurationYamlSwapper                           | Used to convert the YAML configuration of the transaction into the standard configuration of the transaction             |

## ShardingSphereYamlConstruct

| *SPI Name*                                     | *Description*                                                |
| ---------------------------------------------- | ------------------------------------------------------------ |
| ShardingSphereYamlConstruct                    | Used to convert customized objects and YAML to each other    |

| *Implementation Class*                         | *Description*                                                |
| ---------------------------------------------- | ------------------------------------------------------------ |
| NoneShardingStrategyConfigurationYamlConstruct | Used to convert non sharding strategy and YAML to each other |
