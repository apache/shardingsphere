+++
pre = "<b>5.2. </b>"
title = "配置"
weight = 2
chapter = true
+++

## RuleBuilder

### 全限定类名

[`org.apache.shardingsphere.infra.rule.builder.RuleBuilder`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/rule/builder/RuleBuilder.java)

### 定义

用于将用户配置转化为规则对象的接口

### 已知实现

| *配置标识*                                     | *详细说明*                                      | *全限定类名* |
| -------------------------------------------- | ---------------------------------------------- | ---------- |
| AuthorityRuleConfiguration                   | 用于将权限用户配置转化为权限规则对象                  | [`org.apache.shardingsphere.authority.rule.builder.AuthorityRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-kernel/shardingsphere-authority/shardingsphere-authority-core/src/main/java/org/apache/shardingsphere/authority/rule/builder/AuthorityRuleBuilder.java) |
| SQLParserRuleConfiguration                   | 用于将 SQL 解析用户配置转化为 SQL 解析规则对象        | [`org.apache.shardingsphere.parser.rule.builder.SQLParserRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-kernel/shardingsphere-parser/shardingsphere-parser-core/src/main/java/org/apache/shardingsphere/parser/rule/builder/SQLParserRuleBuilder.java) |
| TransactionRuleConfiguration                 | 用于将事务用户配置转化为事务规则对象                  | [`org.apache.shardingsphere.transaction.rule.builder.TransactionRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-kernel/shardingsphere-transaction/shardingsphere-transaction-core/src/main/java/org/apache/shardingsphere/transaction/rule/builder/TransactionRuleBuilder.java) |
| SingleTableRuleConfiguration                 | 用于将单表用户配置转化为单表规则对象                  | [`org.apache.shardingsphere.singletable.rule.builder.SingleTableRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-kernel/shardingsphere-single-table/shardingsphere-single-table-core/src/main/java/org/apache/shardingsphere/singletable/rule/builder/SingleTableRuleBuilder.java) |
| ShardingRuleConfiguration                    | 用于将分片用户配置转化为分片规则对象                  | [`org.apache.shardingsphere.sharding.rule.builder.ShardingRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-core/src/main/java/org/apache/shardingsphere/sharding/rule/builder/ShardingRuleBuilder.java) |
| AlgorithmProvidedShardingRuleConfiguration   | 用于将基于算法的分片用户配置转化为分片规则对象          | [`org.apache.shardingsphere.sharding.rule.builder.AlgorithmProvidedShardingRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-core/src/main/java/org/apache/shardingsphere/sharding/rule/builder/AlgorithmProvidedShardingRuleBuilder.java) |
| ReadwriteSplittingRuleConfiguration          | 用于将读写分离用户配置转化为读写分离规则对象            | [`org.apache.shardingsphere.readwritesplitting.rule.builder.ReadwriteSplittingRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-readwrite-splitting/shardingsphere-readwrite-splitting-core/src/main/java/org/apache/shardingsphere/readwritesplitting/rule/builder/ReadwriteSplittingRuleBuilder.java) |
| AlgorithmReadwriteSplittingRuleConfiguration | 用于将基于算法的读写分离用户配置转化为读写分离规则对象    | [`org.apache.shardingsphere.readwritesplitting.rule.builder.AlgorithmProvidedReadwriteSplittingRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-readwrite-splitting/shardingsphere-readwrite-splitting-core/src/main/java/org/apache/shardingsphere/readwritesplitting/rule/builder/AlgorithmProvidedReadwriteSplittingRuleBuilder.java) |
| DatabaseDiscoveryRuleConfiguration           | 用于将数据库发现用户配置转化为数据库发现规则对象         | [`org.apache.shardingsphere.dbdiscovery.rule.builder.DatabaseDiscoveryRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-db-discovery/shardingsphere-db-discovery-core/src/main/java/org/apache/shardingsphere/dbdiscovery/rule/builder/DatabaseDiscoveryRuleBuilder.java) |
| AlgorithmDatabaseDiscoveryRuleConfiguration  | 用于将基于算法的数据库发现用户配置转化为数据库发现规则对象 | [`org.apache.shardingsphere.dbdiscovery.rule.builder.AlgorithmProvidedDatabaseDiscoveryRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-db-discovery/shardingsphere-db-discovery-core/src/main/java/org/apache/shardingsphere/dbdiscovery/rule/builder/AlgorithmProvidedDatabaseDiscoveryRuleBuilder.java) |
| EncryptRuleConfiguration                     | 用于将加密用户配置转化为加密规则对象                   | [`org.apache.shardingsphere.encrypt.rule.builder.EncryptRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-encrypt/shardingsphere-encrypt-core/src/main/java/org/apache/shardingsphere/encrypt/rule/builder/EncryptRuleBuilder.java) |
| AlgorithmEncryptRuleConfiguration            | 用于将基于算法的加密用户配置转化为加密规则对象           | [`org.apache.shardingsphere.encrypt.rule.builder.AlgorithmProvidedEncryptRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-encrypt/shardingsphere-encrypt-core/src/main/java/org/apache/shardingsphere/encrypt/rule/builder/AlgorithmProvidedEncryptRuleBuilder.java) |
| ShadowRuleConfiguration                      | 用于将影子库用户配置转化为影子库规则对象                | [`org.apache.shardingsphere.shadow.rule.builder.ShadowRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-shadow/shardingsphere-shadow-core/src/main/java/org/apache/shardingsphere/shadow/rule/builder/ShadowRuleBuilder.java) |
| AlgorithmShadowRuleConfiguration             | 用于将基于算法的影子库用户配置转化为影子库规则对象        | [`org.apache.shardingsphere.shadow.rule.builder.AlgorithmProvidedShadowRuleBuilder`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-shadow/shardingsphere-shadow-core/src/main/java/org/apache/shardingsphere/shadow/rule/builder/AlgorithmProvidedShadowRuleBuilder.java) |

## YamlRuleConfigurationSwapper

### 全限定类名

[`org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/yaml/config/swapper/YamlRuleConfigurationSwapper.java)

### 定义

用于将 YAML 配置转化为标准用户配置

### 已知实现

| *配置标识*            | *详细说明*                                    | *全限定类名* |
| ------------------- | -------------------------------------------- | ----------- |
| AUTHORITY           | 用于将权限规则的 YAML 配置转化为权限规则标准配置     | [`org.apache.shardingsphere.authority.yaml.swapper.YamlAuthorityRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-kernel/shardingsphere-authority/shardingsphere-authority-core/src/main/java/org/apache/shardingsphere/authority/yaml/swapper/YamlAuthorityRuleConfigurationSwapper.java) |
| SQL_PARSER          | 用于将 SQL 解析的 YAML 配置转化为 SQL 解析标准配置 | [`org.apache.shardingsphere.parser.yaml.swapper.YamlSQLParserRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-kernel/shardingsphere-parser/shardingsphere-parser-core/src/main/java/org/apache/shardingsphere/parser/yaml/swapper/YamlSQLParserRuleConfigurationSwapper.java) |
| TRANSACTION         | 用于将事务的 YAML 配置转化为事务标准配置           | [`org.apache.shardingsphere.transaction.yaml.swapper.YamlTransactionRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-kernel/shardingsphere-transaction/shardingsphere-transaction-core/src/main/java/org/apache/shardingsphere/transaction/yaml/swapper/YamlTransactionRuleConfigurationSwapper.java) |
| SINGLE              | 用于将单表的 YAML 配置转化为单表标准配置           | [`org.apache.shardingsphere.singletable.yaml.config.swapper.YamlSingleTableRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-kernel/shardingsphere-single-table/shardingsphere-single-table-core/src/main/java/org/apache/shardingsphere/singletable/yaml/config/swapper/YamlSingleTableRuleConfigurationSwapper.java) |
| SHARDING            | 用于将分片的 YAML 配置转化为分片标准配置           | [`org.apache.shardingsphere.sharding.yaml.swapper.YamlShardingRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-core/src/main/java/org/apache/shardingsphere/sharding/yaml/swapper/YamlShardingRuleConfigurationSwapper.java) |
| SHARDING            | 用于将基于算法的分片配置转化为分片标准配置           | [`org.apache.shardingsphere.sharding.yaml.swapper.YamlShardingRuleAlgorithmProviderConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-core/src/main/java/org/apache/shardingsphere/sharding/yaml/swapper/YamlShardingRuleAlgorithmProviderConfigurationSwapper.java) |
| READWRITE_SPLITTING | 用于将读写分离的 YAML 配置转化为读写分离标准配置     | [`org.apache.shardingsphere.readwritesplitting.yaml.swapper.YamlReadwriteSplittingRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-readwrite-splitting/shardingsphere-readwrite-splitting-core/src/main/java/org/apache/shardingsphere/readwritesplitting/yaml/swapper/YamlReadwriteSplittingRuleConfigurationSwapper.java) |
| READWRITE_SPLITTING | 用于将基于算法的读写分离配置转化为读写分离标准配置    | [`org.apache.shardingsphere.readwritesplitting.yaml.swapper.YamlReadwriteSplittingRuleAlgorithmProviderConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-readwrite-splitting/shardingsphere-readwrite-splitting-core/src/main/java/org/apache/shardingsphere/readwritesplitting/yaml/swapper/YamlReadwriteSplittingRuleAlgorithmProviderConfigurationSwapper.java) |
| DB_DISCOVERY        | 用于将数据库发现的 YAML 配置转化为数据库发现标准配置  | [`org.apache.shardingsphere.dbdiscovery.yaml.swapper.YamlDatabaseDiscoveryRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-db-discovery/shardingsphere-db-discovery-core/src/main/java/org/apache/shardingsphere/dbdiscovery/yaml/swapper/YamlDatabaseDiscoveryRuleConfigurationSwapper.java) |
| DB_DISCOVERY        | 用于将基于算法的数据库发现配置转化为数据库发现标准配置 | [`org.apache.shardingsphere.dbdiscovery.yaml.swapper.YamlDatabaseDiscoveryRuleAlgorithmProviderConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-db-discovery/shardingsphere-db-discovery-core/src/main/java/org/apache/shardingsphere/dbdiscovery/yaml/swapper/YamlDatabaseDiscoveryRuleAlgorithmProviderConfigurationSwapper.java) |
| ENCRYPT             | 用于将加密的 YAML 配置转化为加密标准配置           | [`org.apache.shardingsphere.encrypt.yaml.swapper.YamlEncryptRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-encrypt/shardingsphere-encrypt-core/src/main/java/org/apache/shardingsphere/encrypt/yaml/swapper/YamlEncryptRuleConfigurationSwapper.java) |
| ENCRYPT             | 用于将基于算法的加密配置转化为加密标准配置           | [`org.apache.shardingsphere.encrypt.yaml.swapper.YamlEncryptRuleAlgorithmProviderConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-encrypt/shardingsphere-encrypt-core/src/main/java/org/apache/shardingsphere/encrypt/yaml/swapper/YamlEncryptRuleAlgorithmProviderConfigurationSwapper.java) |
| SHADOW              | 用于将影子库的 YAML 配置转化为影子库标准配置        | [`org.apache.shardingsphere.shadow.yaml.swapper.YamlShadowRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-shadow/shardingsphere-shadow-core/src/main/java/org/apache/shardingsphere/shadow/yaml/swapper/YamlShadowRuleConfigurationSwapper.java) |
| SHADOW              | 用于将基于算法的影子库配置转化为影子库标准配置        | [`org.apache.shardingsphere.shadow.yaml.swapper.YamlShadowRuleAlgorithmProviderConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-shadow/shardingsphere-shadow-core/src/main/java/org/apache/shardingsphere/shadow/yaml/swapper/YamlShadowRuleAlgorithmProviderConfigurationSwapper.java) |
| SQL_TRANSLATOR      | 用于将 SQL 转换的 YAML 配置转化为 SQL 转换标准配置 | [`org.apache.shardingsphere.sqltranslator.yaml.swapper.YamlSQLTranslatorRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-kernel/shardingsphere-sql-translator/shardingsphere-sql-translator-core/src/main/java/org/apache/shardingsphere/sqltranslator/yaml/swapper/YamlSQLTranslatorRuleConfigurationSwapper.java) |

## ShardingSphereYamlConstruct

### 全限定类名

[`org.apache.shardingsphere.infra.yaml.engine.constructor.ShardingSphereYamlConstruct`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/yaml/engine/constructor/ShardingSphereYamlConstruct.java)

### 定义

用于将定制化对象和 YAML 相互转化

### 已知实现

| *配置标识*                              | *详细说明*                       | *全限定类名* |
| ------------------------------------- | ------------------------------- | ---------- |
| YamlNoneShardingStrategyConfiguration | 用于将不分片策略对象和 YAML 相互转化 | [`org.apache.shardingsphere.sharding.yaml.engine.construct.NoneShardingStrategyConfigurationYamlConstruct`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-sharding/shardingsphere-sharding-core/src/main/java/org/apache/shardingsphere/sharding/yaml/engine/construct/NoneShardingStrategyConfigurationYamlConstruct.java) |
