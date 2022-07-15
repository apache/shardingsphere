+++
pre = "<b>6.2. </b>"
title = "Configuration"
weight = 2
chapter = true
+++

## RuleBuilder

### Fully-qualified class name

[`org.apache.shardingsphere.infra.rule.builder.RuleBuilder`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/rule/builder/RuleBuilder.java)

### Definition

Used to convert user configurations into rule objects

### Implementation classes

| *Implementation Class*                         | *Description*                                                                                                  | *Fully-qualified class name* |
| ---------------------------------------------- | -------------------------------------------------------------------------------------------------------------- | ---------------------------- |
| AlgorithmProvidedReadwriteSplittingRuleBuilder | Used to convert algorithm-based read-write splitting user configuration into read-write splitting rule objects | TODO |
| AlgorithmProvidedDatabaseDiscoveryRuleBuilder  | Used to convert algorithm-based database discovery user configuration into database discovery rule objects     | TODO |
| AlgorithmProvidedShardingRuleBuilder           | Used to convert algorithm-based sharding user configuration into sharding rule objects                         | TODO |
| AlgorithmProvidedEncryptRuleBuilder            | Used to convert algorithm-based encryption user configuration into encryption rule objects                     | TODO |
| AlgorithmProvidedShadowRuleBuilder             | Used to convert algorithm-based shadow database user configuration into shadow database rule objects           | TODO |
| ReadwriteSplittingRuleBuilder                  | Used to convert read-write splitting user configuration into read-write splitting rule objects                 | TODO |
| DatabaseDiscoveryRuleBuilder                   | Used to convert database discovery user configuration into database discovery rule objects                     | TODO |
| SingleTableRuleBuilder                         | Used to convert single-table user configuration into a single-table rule objects                               | TODO |
| AuthorityRuleBuilder                           | Used to convert authority user configuration into authority rule objects                                       | TODO |
| ShardingRuleBuilder                            | Used to convert sharding user configuration into sharding rule objects                                         | TODO |
| EncryptRuleBuilder                             | Used to convert encrypted user configuration into encryption rule objects                                      | TODO |
| ShadowRuleBuilder                              | Used to convert shadow database user configuration into shadow database rule objects                           | TODO |
| TransactionRuleBuilder                         | Used to convert transaction user configuration into transaction rule objects                                   | TODO |
| SQLParserRuleBuilder                           | Used to convert SQL parser user configuration into SQL parser rule objects                                     | TODO |

## YamlRuleConfigurationSwapper

### Fully-qualified class name

[`org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/yaml/config/swapper/YamlRuleConfigurationSwapper.java)

### Definition

Used to convert YAML configuration to standard user configuration

### Implementation classes

| *Implementation Class*                                          | *Description*                                                                                                          | *Fully-qualified class name* |
| --------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------- | --------------------------- |
| ReadwriteSplittingRuleAlgorithmProviderConfigurationYamlSwapper | Used to convert algorithm-based read-write splitting configuration into read-write splitting standard configuration    | TODO |
| DatabaseDiscoveryRuleAlgorithmProviderConfigurationYamlSwapper  | Used to convert algorithm-based database discovery configuration into database discovery standard configuration        | TODO |
| ShardingRuleAlgorithmProviderConfigurationYamlSwapper           | Used to convert algorithm-based sharding configuration into sharding standard configuration                            | TODO |
| EncryptRuleAlgorithmProviderConfigurationYamlSwapper            | Used to convert algorithm-based encryption configuration into encryption standard configuration                        | TODO |
| ShadowRuleAlgorithmProviderConfigurationYamlSwapper             | Used to convert algorithm-based shadow database configuration into shadow database standard configuration              | TODO |
| ReadwriteSplittingRuleConfigurationYamlSwapper                  | Used to convert the YAML configuration of read-write splitting into the standard configuration of read-write splitting | TODO |
| DatabaseDiscoveryRuleConfigurationYamlSwapper                   | Used to convert the YAML configuration of database discovery into the standard configuration of database discovery     | TODO |
| AuthorityRuleConfigurationYamlSwapper                           | Used to convert the YAML configuration of authority rules into standard configuration of authority rules               | TODO |
| ShardingRuleConfigurationYamlSwapper                            | Used to convert the YAML configuration of the sharding into the standard configuration of the sharding                 | TODO |
| EncryptRuleConfigurationYamlSwapper                             | Used to convert encrypted YAML configuration into encrypted standard configuration                                     | TODO |
| ShadowRuleConfigurationYamlSwapper                              | Used to convert the YAML configuration of the shadow database into the standard configuration of the shadow database   | TODO |
| TransactionRuleConfigurationYamlSwapper                         | Used to convert the YAML configuration of the transaction into the standard configuration of the transaction           | TODO |
| SingleTableRuleConfigurationYamlSwapper                         | Used to convert the YAML configuration of the single table into the standard configuration of the single table         | TODO |
| SQLParserRuleConfigurationYamlSwapper                           | Used to convert the YAML configuration of the SQL parser into the standard configuration of the SQL parser             | TODO |
| SQLTranslatorRuleConfigurationYamlSwapper                       | Used to convert the YAML configuration of the SQL transformation to the SQL transformation standard configuration      | TODO |

## ShardingSphereYamlConstruct

### Fully-qualified class name

[`org.apache.shardingsphere.infra.yaml.engine.constructor.ShardingSphereYamlConstruct`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/yaml/engine/constructor/ShardingSphereYamlConstruct.java)

### Definition

Used to convert custom objects and YAML to and from each other

### Implementation classes

| *Implementation Class*                         | *Description*                                                               | *Fully-qualified class name* |
| ---------------------------------------------- | --------------------------------------------------------------------------- | ---------------------------- |
| NoneShardingStrategyConfigurationYamlConstruct | Used to convert non-sharding policy objects and YAML to and from each other | TODO |
