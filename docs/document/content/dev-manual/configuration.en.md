+++
pre = "<b>5.2. </b>"
title = "Configuration"
weight = 2
chapter = true
+++

## ShardingSphereRuleBuilder

| *SPI Name*                | *Description*                                                                       |
| ------------------------- | ----------------------------------------------------------------------------------- |
| ShardingSphereRuleBuilder | Used to convert user configurations to rule objects                                 |

| *Implementation Class*          | *Description*                                                                                   |
| ------------------------------- | ----------------------------------------------------------------------------------------------- |
| ShardingRuleBuilder             | Used to convert user sharding configurations to sharding rule objects                           |
| MasterSlaveRuleBuilder          | Used to convert user master-slave configurations to master-slave rule objects                   |
| ConsensusReplicationRuleBuilder | Used to convert user consensus replication configurations to consensus replication rule objects |
| EncryptRuleBuilder              | Used to convert user encryption configurations to encryption rule objects                       |
| ShadowRuleBuilder               | Used to convert user shadow database configurations to shadow database rule objects             |

## YamlRuleConfigurationSwapper

| *SPI Name*                              | *Description*                                                                                |
| --------------------------------------- | -------------------------------------------------------------------------------------------- |
| YamlRuleConfigurationSwapper            | Used to convert YAML configuration to standard user configuration                            |

| *Implementation Class*                           | *Description*                                                                                            |
| ------------------------------------------------ | -------------------------------------------------------------------------------------------------------- |
| ShardingRuleConfigurationYamlSwapper             | Used to convert YAML sharding configuration to standard sharding configuration                           |
| MasterSlaveRuleConfigurationYamlSwapper          | Used to convert YAML master-slave configuration to standard master-slave configuration                   |
| ConsensusReplicationRuleConfigurationYamlSwapper | Used to convert YAML consensus replication configuration to standard consensus replication configuration |
| EncryptRuleConfigurationYamlSwapper              | Used to convert YAML encryption configuration to standard encryption configuration                       |
| ShadowRuleConfigurationYamlSwapper               | Used to convert YAML shadow database configuration to standard shadow database configuration             |

## ShardingSphereYamlConstruct

| *SPI Name*                                     | *Description*                                                |
| ---------------------------------------------- | ------------------------------------------------------------ |
| ShardingSphereYamlConstruct                    | Used to convert customized objects and YAML to each other    |

| *Implementation Class*                         | *Description*                                                |
| ---------------------------------------------- | ------------------------------------------------------------ |
| NoneShardingStrategyConfigurationYamlConstruct | Used to convert non sharding strategy and YAML to each other |
