+++
pre = "<b>5.2. </b>"
title = "配置"
weight = 2
chapter = true
+++

## RuleBuilder

### 全限定类名

[`org.apache.shardingsphere.infra.rule.builder.RuleBuilder`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/rule/builder/RuleBuilder.java)

### 定义

用于将用户配置转化为规则对象的接口

### 已知实现

| *配置标识*                              | *详细说明*                       | *全限定类名*                                                                                                                                                                                                                                                                                        |
|-------------------------------------|------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AuthorityRuleConfiguration          | 用于将权限用户配置转化为权限规则对象           | [`org.apache.shardingsphere.authority.rule.builder.AuthorityRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/kernel/authority/core/src/main/java/org/apache/shardingsphere/authority/rule/builder/AuthorityRuleBuilder.java)                                                 |
| SQLParserRuleConfiguration          | 用于将 SQL 解析用户配置转化为 SQL 解析规则对象 | [`org.apache.shardingsphere.parser.rule.builder.SQLParserRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/kernel/sql-parser/core/src/main/java/org/apache/shardingsphere/parser/rule/builder/SQLParserRuleBuilder.java)                                                          |
| TransactionRuleConfiguration        | 用于将事务用户配置转化为事务规则对象           | [`org.apache.shardingsphere.transaction.rule.builder.TransactionRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/core/src/main/java/org/apache/shardingsphere/transaction/rule/builder/TransactionRuleBuilder.java)                                       |
| SingleRuleConfiguration             | 用于将单表用户配置转化为单表规则对象           | [`org.apache.shardingsphere.singletable.rule.builder.SingleRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/kernel/single/core/src/main/java/org/apache/shardingsphere/single/rule/builder/SingleRuleBuilder.java)                                                |
| ShardingRuleConfiguration           | 用于将分片用户配置转化为分片规则对象           | [`org.apache.shardingsphere.sharding.rule.builder.ShardingRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/rule/builder/ShardingRuleBuilder.java)                                                    |
| ReadwriteSplittingRuleConfiguration | 用于将读写分离用户配置转化为读写分离规则对象       | [`org.apache.shardingsphere.readwritesplitting.rule.builder.ReadwriteSplittingRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/features/readwrite-splitting/core/src/main/java/org/apache/shardingsphere/readwritesplitting/rule/builder/ReadwriteSplittingRuleBuilder.java) |
| EncryptRuleConfiguration            | 用于将加密用户配置转化为加密规则对象           | [`org.apache.shardingsphere.encrypt.rule.builder.EncryptRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/features/encrypt/core/src/main/java/org/apache/shardingsphere/encrypt/rule/builder/EncryptRuleBuilder.java)                                                         |
| ShadowRuleConfiguration             | 用于将影子库用户配置转化为影子库规则对象         | [`org.apache.shardingsphere.shadow.rule.builder.ShadowRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/features/shadow/core/src/main/java/org/apache/shardingsphere/shadow/rule/builder/ShadowRuleBuilder.java)                                                              |

## YamlRuleConfigurationSwapper

### 全限定类名

[`org.apache.shardingsphere.infra.yaml.config.swapper.NewYamlRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/infra/common/src/main/java/org/apache/shardingsphere/infra/yaml/config/swapper/rule/NewYamlRuleConfigurationSwapper.java)

### 定义

用于将 YAML 配置转化为标准用户配置

### 已知实现

| *配置标识*              | *详细说明*                            | *全限定类名*                                                                                                                                                                                                                                                                                                                          |
|---------------------|-----------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AUTHORITY           | 用于将权限规则的 YAML 配置转化为权限规则标准配置       | [`org.apache.shardingsphere.authority.yaml.swapper.NewYamlAuthorityRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/kernel/authority/core/src/main/java/org/apache/shardingsphere/authority/yaml/swapper/NewYamlAuthorityRuleConfigurationSwapper.java)                                                 |
| SQL_PARSER          | 用于将 SQL 解析的 YAML 配置转化为 SQL 解析标准配置 | [`org.apache.shardingsphere.parser.yaml.swapper.NewYamlSQLParserRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/kernel/sql-parser/core/src/main/java/org/apache/shardingsphere/parser/yaml/swapper/NewYamlSQLParserRuleConfigurationSwapper.java)                                                          |
| TRANSACTION         | 用于将事务的 YAML 配置转化为事务标准配置           | [`org.apache.shardingsphere.transaction.yaml.swapper.NewYamlTransactionRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/core/src/main/java/org/apache/shardingsphere/transaction/yaml/swapper/NewYamlTransactionRuleConfigurationSwapper.java)                                       |
| SINGLE              | 用于将单表的 YAML 配置转化为单表标准配置           | [`org.apache.shardingsphere.singletable.yaml.config.swapper.NewYamlSingleRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/kernel/single/core/src/main/java/org/apache/shardingsphere/single/yaml/config/swapper/NewYamlSingleRuleConfigurationSwapper.java)                                  |
| SHARDING            | 用于将分片的 YAML 配置转化为分片标准配置           | [`org.apache.shardingsphere.sharding.yaml.swapper.NewYamlShardingRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/yaml/swapper/NewYamlShardingRuleConfigurationSwapper.java)                                                    |
| READWRITE_SPLITTING | 用于将读写分离的 YAML 配置转化为读写分离标准配置       | [`org.apache.shardingsphere.readwritesplitting.yaml.swapper.NewYamlReadwriteSplittingRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/features/readwrite-splitting/core/src/main/java/org/apache/shardingsphere/readwritesplitting/yaml/swapper/NewYamlReadwriteSplittingRuleConfigurationSwapper.java) |
| ENCRYPT             | 用于将加密的 YAML 配置转化为加密标准配置           | [`org.apache.shardingsphere.encrypt.yaml.swapper.NewYamlEncryptRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/features/encrypt/core/src/main/java/org/apache/shardingsphere/encrypt/yaml/swapper/NewYamlEncryptRuleConfigurationSwapper.java)                                                         |
| SHADOW              | 用于将影子库的 YAML 配置转化为影子库标准配置         | [`org.apache.shardingsphere.shadow.yaml.swapper.NewYamlShadowRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/features/shadow/core/src/main/java/org/apache/shardingsphere/shadow/yaml/swapper/NewYamlShadowRuleConfigurationSwapper.java)                                                              |
| SQL_TRANSLATOR      | 用于将 SQL 转换的 YAML 配置转化为 SQL 转换标准配置 | [`org.apache.shardingsphere.sqltranslator.yaml.swapper.NewYamlSQLTranslatorRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/kernel/sql-translator/core/src/main/java/org/apache/shardingsphere/sqltranslator/yaml/swapper/NewYamlSQLTranslatorRuleConfigurationSwapper.java)                            |

## ShardingSphereYamlConstruct

### 全限定类名

[`org.apache.shardingsphere.infra.yaml.engine.constructor.ShardingSphereYamlConstruct`](https://github.com/apache/shardingsphere/blob/master/infra/util/src/main/java/org/apache/shardingsphere/infra/util/yaml/constructor/ShardingSphereYamlConstruct.java)

### 定义

用于将定制化对象和 YAML 相互转化

### 已知实现

| *配置标识*                                | *详细说明*                | *全限定类名*                                                                                                                                                                                                                                                                                                             |
|---------------------------------------|-----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| YamlNoneShardingStrategyConfiguration | 用于将不分片策略对象和 YAML 相互转化 | [`org.apache.shardingsphere.sharding.yaml.engine.construct.NoneShardingStrategyConfigurationYamlConstruct`](https://github.com/apache/shardingsphere/blob/master/features/sharding/core/src/main/java/org/apache/shardingsphere/sharding/yaml/engine/construct/NoneShardingStrategyConfigurationYamlConstruct.java) |
