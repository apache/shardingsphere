+++
pre = "<b>5.2. </b>"
title = "Configuration"
weight = 2
chapter = true
+++

## RuleBuilder

### Fully-qualified class name

[`org.apache.shardingsphere.infra.rule.builder.RuleBuilder`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/rule/builder/RuleBuilder.java)

### Definition

Used to convert user configurations into rule objects

### Implementation classes

| *Configuration Type*                | *Description*                                                                                  | *Fully-qualified class name*                                                                                                                                                                                                                                                                   |
|-------------------------------------|------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AuthorityRuleConfiguration          | Used to convert authority user configuration into authority rule objects                       | [`org.apache.shardingsphere.authority.rule.builder.AuthorityRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/kernel/authority/core/src/main/java/org/apache/shardingsphere/authority/rule/builder/AuthorityRuleBuilder.java)                                                 |
| SQLParserRuleConfiguration          | Used to convert SQL parser user configuration into SQL parser rule objects                     | [`org.apache.shardingsphere.parser.rule.builder.SQLParserRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/kernel/sql-parser/core/src/main/java/org/apache/shardingsphere/parser/rule/builder/SQLParserRuleBuilder.java)                                                          |
| TransactionRuleConfiguration        | Used to convert transaction user configuration into transaction rule objects                   | [`org.apache.shardingsphere.transaction.rule.builder.TransactionRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/core/src/main/java/org/apache/shardingsphere/transaction/rule/builder/TransactionRuleBuilder.java)                                       |
| SingleRuleConfiguration             | Used to convert single-table user configuration into a single-table rule objects               | [`org.apache.shardingsphere.singletable.rule.builder.SingleRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/kernel/single/core/src/main/java/org/apache/shardingsphere/single/rule/builder/SingleRuleBuilder.java)                                                |
| ShardingRuleConfiguration           | Used to convert sharding user configuration into sharding rule objects                         | [`org.apache.shardingsphere.sharding.rule.builder.ShardingRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/rule/builder/ShardingRuleBuilder.java)                                                    |
| ReadwriteSplittingRuleConfiguration | Used to convert read-write splitting user configuration into read-write splitting rule objects | [`org.apache.shardingsphere.readwritesplitting.rule.builder.ReadwriteSplittingRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/features/readwrite-splitting/core/src/main/java/org/apache/shardingsphere/readwritesplitting/rule/builder/ReadwriteSplittingRuleBuilder.java) |
| EncryptRuleConfiguration            | Used to convert encrypted user configuration into encryption rule objects                      | [`org.apache.shardingsphere.encrypt.rule.builder.EncryptRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/features/encrypt/core/src/main/java/org/apache/shardingsphere/encrypt/rule/builder/EncryptRuleBuilder.java)                                                         |
| ShadowRuleConfiguration             | Used to convert shadow database user configuration into shadow database rule objects           | [`org.apache.shardingsphere.shadow.rule.builder.ShadowRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/features/shadow/core/src/main/java/org/apache/shardingsphere/shadow/rule/builder/ShadowRuleBuilder.java)                                                              |

## YamlRuleConfigurationSwapper

### Fully-qualified class name

[`org.apache.shardingsphere.infra.yaml.config.swapper.NewYamlRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/yaml/config/swapper/rule/NewYamlRuleConfigurationSwapper.java)

### Definition

Used to convert YAML configuration to standard user configuration

### Implementation classes

| *Configuration Type* | *Description*                                                                                                          | *Fully-qualified class name*                                                                                                                                                                                                                                                                                                     |
|----------------------|------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AUTHORITY            | Used to convert the YAML configuration of authority rules into standard configuration of authority rules               | [`org.apache.shardingsphere.authority.yaml.swapper.NewYamlAuthorityRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/kernel/authority/core/src/main/java/org/apache/shardingsphere/authority/yaml/swapper/NewYamlAuthorityRuleConfigurationSwapper.java)                                                 |
| SQL_PARSER           | Used to convert the YAML configuration of the SQL parser into the standard configuration of the SQL parser             | [`org.apache.shardingsphere.parser.yaml.swapper.NewYamlSQLParserRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/kernel/sql-parser/core/src/main/java/org/apache/shardingsphere/parser/yaml/swapper/NewYamlSQLParserRuleConfigurationSwapper.java)                                                          |
| TRANSACTION          | Used to convert the YAML configuration of the transaction into the standard configuration of the transaction           | [`org.apache.shardingsphere.transaction.yaml.swapper.NewYamlTransactionRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/core/src/main/java/org/apache/shardingsphere/transaction/yaml/swapper/NewYamlTransactionRuleConfigurationSwapper.java)                                       |
| SINGLE               | Used to convert the YAML configuration of the single table into the standard configuration of the single table         | [`org.apache.shardingsphere.singletable.yaml.config.swapper.NewYamlSingleRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/kernel/single/core/src/main/java/org/apache/shardingsphere/single/yaml/config/swapper/NewYamlSingleRuleConfigurationSwapper.java)                                  |
| SHARDING             | Used to convert the YAML configuration of the sharding into the standard configuration of the sharding                 | [`org.apache.shardingsphere.sharding.yaml.swapper.NewYamlShardingRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/yaml/swapper/NewYamlShardingRuleConfigurationSwapper.java)                                                    |
| READWRITE_SPLITTING  | Used to convert the YAML configuration of read-write splitting into the standard configuration of read-write splitting | [`org.apache.shardingsphere.readwritesplitting.yaml.swapper.NewYamlReadwriteSplittingRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/features/readwrite-splitting/core/src/main/java/org/apache/shardingsphere/readwritesplitting/yaml/swapper/NewYamlReadwriteSplittingRuleConfigurationSwapper.java) |
| ENCRYPT              | Used to convert encrypted YAML configuration into encrypted standard configuration                                     | [`org.apache.shardingsphere.encrypt.yaml.swapper.NewYamlEncryptRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/features/encrypt/core/src/main/java/org/apache/shardingsphere/encrypt/yaml/swapper/NewYamlEncryptRuleConfigurationSwapper.java)                                                         |
| SHADOW               | Used to convert the YAML configuration of the shadow database into the standard configuration of the shadow database   | [`org.apache.shardingsphere.shadow.yaml.swapper.NewYamlShadowRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/features/shadow/core/src/main/java/org/apache/shardingsphere/shadow/yaml/swapper/NewYamlShadowRuleConfigurationSwapper.java)                                                              |
| SQL_TRANSLATOR       | Used to convert the YAML configuration of the SQL transformation to the SQL transformation standard configuration      | [`org.apache.shardingsphere.sqltranslator.yaml.swapper.NewYamlSQLTranslatorRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/kernel/sql-translator/core/src/main/java/org/apache/shardingsphere/sqltranslator/yaml/swapper/NewYamlSQLTranslatorRuleConfigurationSwapper.java)                            |

## ShardingSphereYamlConstruct

### Fully-qualified class name

[`org.apache.shardingsphere.infra.yaml.engine.constructor.ShardingSphereYamlConstruct`](https://github.com/apache/shardingsphere/blob/master/infra/util/src/main/java/org/apache/shardingsphere/infra/util/yaml/constructor/ShardingSphereYamlConstruct.java)

### Definition

Used to convert custom objects and YAML to and from each other

### Implementation classes

| *Configuration Type*                  | *Description*                                                               | *Fully-qualified class name*                                                                                                                                                                                                                                                                                        |
|---------------------------------------|-----------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| YamlNoneShardingStrategyConfiguration | Used to convert non-sharding policy objects and YAML to and from each other | [`org.apache.shardingsphere.sharding.yaml.engine.construct.NoneShardingStrategyConfigurationYamlConstruct`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/yaml/engine/construct/NoneShardingStrategyConfigurationYamlConstruct.java) |
