+++
pre = "<b>6.2. </b>"
title = "Configuration"
weight = 2
chapter = true
+++

## SPI Interface

| *SPI Name*                                      | *Description*                                       |
| ---------------------------------------------- | ----------------------------------------------- |
| RuleBuilder                                    | Used to convert user configurations into rule objects                        |
| YamlRuleConfigurationSwapper                   | Used to convert YAML configuration to standard user configuration                  |
| ShardingSphereYamlConstruct                    | Used to convert custom objects and YAML to and from each other                |

## Sample

### RuleBuilder

| *Implementation Class*                                        | *Description*                        |
|------------------------------------------------|-------------------------------|
| AlgorithmProvidedReadwriteSplittingRuleBuilder | Used to convert algorithm-based read-write splitting user configuration into read-write splitting rule objects   |
| AlgorithmProvidedDatabaseDiscoveryRuleBuilder  | Used to convert algorithm-based database discovery user configuration into database discovery rule objects |
| AlgorithmProvidedShardingRuleBuilder           | Used to convert algorithm-based sharding user configuration into sharding rule objects       |
| AlgorithmProvidedEncryptRuleBuilder            | Used to convert algorithm-based encryption user configuration into encryption rule objects       |
| AlgorithmProvidedShadowRuleBuilder             | Used to convert algorithm-based shadow database user configuration into shadow database rule objects     |
| ReadwriteSplittingRuleBuilder                  | Used to convert read-write splitting user configuration into read-write splitting rule objects        |
| DatabaseDiscoveryRuleBuilder                   | Used to convert database discovery user configuration into database discovery rule objects      |
| SingleTableRuleBuilder                         | Used to convert single-table user configuration into a single-table rule objects            |
| AuthorityRuleBuilder                           | Used to convert authority user configuration into authority rule objects            |
| ShardingRuleBuilder                            | Used to convert sharding user configuration into sharding rule objects            |
| EncryptRuleBuilder                             | Used to convert encrypted user configuration into encryption rule objects            |
| ShadowRuleBuilder                              | Used to convert shadow database user configuration into shadow database rule objects          |
| TransactionRuleBuilder                         | Used to convert transaction user configuration into transaction rule objects            |
| SQLParserRuleBuilder                           | Used to convert SQL parser user configuration into SQL parser rule objects  |

### YamlRuleConfigurationSwapper
| *Implementation Class*                                                      | *Description*                            |
| --------------------------------------------------------------- |-----------------------------------|
| ReadwriteSplittingRuleAlgorithmProviderConfigurationYamlSwapper | Used to convert algorithm-based read-write splitting configuration into read-write splitting standard configuration         |
| DatabaseDiscoveryRuleAlgorithmProviderConfigurationYamlSwapper  | Used to convert algorithm-based database discovery configuration into database discovery standard configuration       |
| ShardingRuleAlgorithmProviderConfigurationYamlSwapper           | Used to convert algorithm-based sharding configuration into sharding standard configuration             |
| EncryptRuleAlgorithmProviderConfigurationYamlSwapper            | Used to convert algorithm-based encryption configuration into encryption standard configuration             |
| ShadowRuleAlgorithmProviderConfigurationYamlSwapper             | Used to convert algorithm-based shadow database configuration into shadow database standard configuration           |
| ReadwriteSplittingRuleConfigurationYamlSwapper                  | Used to convert the YAML configuration of read-write splitting into the standard configuration of read-write splitting       |
| DatabaseDiscoveryRuleConfigurationYamlSwapper                   | Used to convert the YAML configuration of database discovery into the standard configuration of database discovery     |
| AuthorityRuleConfigurationYamlSwapper                           | Used to convert the YAML configuration of authority rules into standard configuration of authority rules       |
| ShardingRuleConfigurationYamlSwapper                            | Used to convert the YAML configuration of the sharding into the standard configuration of the sharding           |
| EncryptRuleConfigurationYamlSwapper                             | Used to convert encrypted YAML configuration into encrypted standard configuration           |
| ShadowRuleConfigurationYamlSwapper                              | Used to convert the YAML configuration of the shadow database into the standard configuration of the shadow database         |
| TransactionRuleConfigurationYamlSwapper                         | Used to convert the YAML configuration of the transaction into the standard configuration of the transaction           |
| SingleTableRuleConfigurationYamlSwapper                         | Used to convert the YAML configuration of the single table into the standard configuration of the single table           |
| SQLParserRuleConfigurationYamlSwapper                           | Used to convert the YAML configuration of the SQL parser into the standard configuration of the SQL parser |
| SQLTranslatorRuleConfigurationYamlSwapper                       | Used to convert the YAML configuration of the SQL transformation to the SQL transformation standard configuration |


### ShardingSphereYamlConstruct

| *Implementation Class*                                     | *Description*                       |
| ---------------------------------------------- | ------------------------------- |
| NoneShardingStrategyConfigurationYamlConstruct | Used to convert non-sharding policy objects and YAML to and from each other |
