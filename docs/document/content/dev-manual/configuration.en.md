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

| *Configuration Type*                                 | *Description*                                                                                                  | *Fully-qualified class name*                                                                                                                                                                                                                                                                                                                                                                      |
|------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AuthorityRuleConfiguration                           | Used to convert authority user configuration into authority rule objects                                       | [`org.apache.shardingsphere.authority.rule.builder.AuthorityRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/kernel/authority/core/src/main/java/org/apache/shardingsphere/authority/rule/builder/AuthorityRuleBuilder.java)                                                                                             |
| SQLParserRuleConfiguration                           | Used to convert SQL parser user configuration into SQL parser rule objects                                     | [`org.apache.shardingsphere.parser.rule.builder.SQLParserRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/kernel/parser/core/src/main/java/org/apache/shardingsphere/parser/rule/builder/SQLParserRuleBuilder.java)                                                                                                         |
| TransactionRuleConfiguration                         | Used to convert transaction user configuration into transaction rule objects                                   | [`org.apache.shardingsphere.transaction.rule.builder.TransactionRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/core/src/main/java/org/apache/shardingsphere/transaction/rule/builder/TransactionRuleBuilder.java)                                                                                 |
| SingleTableRuleConfiguration                         | Used to convert single-table user configuration into a single-table rule objects                               | [`org.apache.shardingsphere.singletable.rule.builder.SingleTableRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/kernel/single-table/core/src/main/java/org/apache/shardingsphere/singletable/rule/builder/SingleTableRuleBuilder.java)                                                                               |
| ShardingRuleConfiguration                            | Used to convert sharding user configuration into sharding rule objects                                         | [`org.apache.shardingsphere.sharding.rule.builder.ShardingRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/rule/builder/ShardingRuleBuilder.java)                                                                                                 |
| ReadwriteSplittingRuleConfiguration                  | Used to convert read-write splitting user configuration into read-write splitting rule objects                 | [`org.apache.shardingsphere.readwritesplitting.rule.builder.ReadwriteSplittingRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/features/readwrite-splitting/core/src/main/java/org/apache/shardingsphere/readwritesplitting/rule/builder/ReadwriteSplittingRuleBuilder.java)                                   |
| DatabaseDiscoveryRuleConfiguration                   | Used to convert database discovery user configuration into database discovery rule objects                     | [`org.apache.shardingsphere.dbdiscovery.rule.builder.DatabaseDiscoveryRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/features/db-discovery/core/src/main/java/org/apache/shardingsphere/dbdiscovery/rule/builder/DatabaseDiscoveryRuleBuilder.java)                                                                 |
| EncryptRuleConfiguration                             | Used to convert encrypted user configuration into encryption rule objects                                      | [`org.apache.shardingsphere.encrypt.rule.builder.EncryptRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/features/encrypt/core/src/main/java/org/apache/shardingsphere/encrypt/rule/builder/EncryptRuleBuilder.java)                                                                                                       |
| ShadowRuleConfiguration                              | Used to convert shadow database user configuration into shadow database rule objects                           | [`org.apache.shardingsphere.shadow.rule.builder.ShadowRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/features/shadow/core/src/main/java/org/apache/shardingsphere/shadow/rule/builder/ShadowRuleBuilder.java)                                                                                                             |

## YamlRuleConfigurationSwapper

### Fully-qualified class name

[`org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/yaml/config/swapper/rule/YamlRuleConfigurationSwapper.java)

### Definition

Used to convert YAML configuration to standard user configuration

### Implementation classes

| *Configuration Type* | *Description*                                                                                                          | *Fully-qualified class name* |
| -------------------- | ---------------------------------------------------------------------------------------------------------------------- | ---------------------------- |
| AUTHORITY            | Used to convert the YAML configuration of authority rules into standard configuration of authority rules               | [`org.apache.shardingsphere.authority.yaml.swapper.YamlAuthorityRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/kernel/authority/core/src/main/java/org/apache/shardingsphere/authority/yaml/swapper/YamlAuthorityRuleConfigurationSwapper.java) |
| SQL_PARSER           | Used to convert the YAML configuration of the SQL parser into the standard configuration of the SQL parser             | [`org.apache.shardingsphere.parser.yaml.swapper.YamlSQLParserRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/kernel/parser/core/src/main/java/org/apache/shardingsphere/parser/yaml/swapper/YamlSQLParserRuleConfigurationSwapper.java) |
| TRANSACTION          | Used to convert the YAML configuration of the transaction into the standard configuration of the transaction           | [`org.apache.shardingsphere.transaction.yaml.swapper.YamlTransactionRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/core/src/main/java/org/apache/shardingsphere/transaction/yaml/swapper/YamlTransactionRuleConfigurationSwapper.java) |
| SINGLE               | Used to convert the YAML configuration of the single table into the standard configuration of the single table         | [`org.apache.shardingsphere.singletable.yaml.config.swapper.YamlSingleTableRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/kernel/single-table/core/src/main/java/org/apache/shardingsphere/singletable/yaml/config/swapper/YamlSingleTableRuleConfigurationSwapper.java) |
| SHARDING             | Used to convert the YAML configuration of the sharding into the standard configuration of the sharding                 | [`org.apache.shardingsphere.sharding.yaml.swapper.YamlShardingRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/yaml/swapper/YamlShardingRuleConfigurationSwapper.java) |
| READWRITE_SPLITTING  | Used to convert the YAML configuration of read-write splitting into the standard configuration of read-write splitting | [`org.apache.shardingsphere.readwritesplitting.yaml.swapper.YamlReadwriteSplittingRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/features/readwrite-splitting/core/src/main/java/org/apache/shardingsphere/readwritesplitting/yaml/swapper/YamlReadwriteSplittingRuleConfigurationSwapper.java) |
| DB_DISCOVERY         | Used to convert the YAML configuration of database discovery into the standard configuration of database discovery     | [`org.apache.shardingsphere.dbdiscovery.yaml.swapper.YamlDatabaseDiscoveryRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/features/db-discovery/core/src/main/java/org/apache/shardingsphere/dbdiscovery/yaml/swapper/YamlDatabaseDiscoveryRuleConfigurationSwapper.java) |
| ENCRYPT              | Used to convert encrypted YAML configuration into encrypted standard configuration                                     | [`org.apache.shardingsphere.encrypt.yaml.swapper.YamlEncryptRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/features/encrypt/core/src/main/java/org/apache/shardingsphere/encrypt/yaml/swapper/YamlEncryptRuleConfigurationSwapper.java) |
| SHADOW               | Used to convert the YAML configuration of the shadow database into the standard configuration of the shadow database   | [`org.apache.shardingsphere.shadow.yaml.swapper.YamlShadowRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/features/shadow/core/src/main/java/org/apache/shardingsphere/shadow/yaml/swapper/YamlShadowRuleConfigurationSwapper.java) |
| SQL_TRANSLATOR       | Used to convert the YAML configuration of the SQL transformation to the SQL transformation standard configuration      | [`org.apache.shardingsphere.sqltranslator.yaml.swapper.YamlSQLTranslatorRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/kernel/sql-translator/core/src/main/java/org/apache/shardingsphere/sqltranslator/yaml/swapper/YamlSQLTranslatorRuleConfigurationSwapper.java) |

## ShardingSphereYamlConstruct

### Fully-qualified class name

[`org.apache.shardingsphere.infra.yaml.engine.constructor.ShardingSphereYamlConstruct`](https://github.com/apache/shardingsphere/blob/master/infra/util/src/main/java/org/apache/shardingsphere/infra/util/yaml/constructor/ShardingSphereYamlConstruct.java)

### Definition

Used to convert custom objects and YAML to and from each other

### Implementation classes

| *Configuration Type*                  | *Description*                                                               | *Fully-qualified class name* |
| ------------------------------------- | --------------------------------------------------------------------------- | ---------------------------- |
| YamlNoneShardingStrategyConfiguration | Used to convert non-sharding policy objects and YAML to and from each other | [`org.apache.shardingsphere.sharding.yaml.engine.construct.NoneShardingStrategyConfigurationYamlConstruct`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/yaml/engine/construct/NoneShardingStrategyConfigurationYamlConstruct.java) |
