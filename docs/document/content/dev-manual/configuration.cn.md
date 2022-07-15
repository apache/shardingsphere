+++
pre = "<b>6.2. </b>"
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

| *已知实现类*                                     | *详细说明*                                      | *全限定类名*                  |
| ---------------------------------------------- | ---------------------------------------------- | --------------------------- |
| AuthorityRuleBuilder                           | 用于将权限用户配置转化为权限规则对象                  | TODO |
| SQLParserRuleBuilder                           | 用于将 SQL 解析用户配置转化为 SQL 解析规则对象        | TODO |
| TransactionRuleBuilder                         | 用于将事务用户配置转化为事务规则对象                  | TODO |
| SingleTableRuleBuilder                         | 用于将单表用户配置转化为单表规则对象                  | TODO |
| ShardingRuleBuilder                            | 用于将分片用户配置转化为分片规则对象                  | TODO |
| AlgorithmProvidedShardingRuleBuilder           | 用于将基于算法的分片用户配置转化为分片规则对象          | TODO |
| ReadwriteSplittingRuleBuilder                  | 用于将读写分离用户配置转化为读写分离规则对象            | TODO |
| AlgorithmProvidedReadwriteSplittingRuleBuilder | 用于将基于算法的读写分离用户配置转化为读写分离规则对象    | TODO |
| DatabaseDiscoveryRuleBuilder                   | 用于将数据库发现用户配置转化为数据库发现规则对象         | TODO |
| AlgorithmProvidedDatabaseDiscoveryRuleBuilder  | 用于将基于算法的数据库发现用户配置转化为数据库发现规则对象 | TODO |
| EncryptRuleBuilder                             | 用于将加密用户配置转化为加密规则对象                   | TODO |
| AlgorithmProvidedEncryptRuleBuilder            | 用于将基于算法的加密用户配置转化为加密规则对象           | TODO |
| AlgorithmProvidedShadowRuleBuilder             | 用于将基于算法的影子库用户配置转化为影子库规则对象        | TODO |
| ShadowRuleBuilder                              | 用于将影子库用户配置转化为影子库规则对象                | TODO |

## YamlRuleConfigurationSwapper

### 全限定类名

[`org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapper`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/yaml/config/swapper/YamlRuleConfigurationSwapper.java)

### 定义

用于将 YAML 配置转化为标准用户配置

### 已知实现

| *已知实现类*                                                      | *详细说明*                                    | *全限定类名*                  |
| --------------------------------------------------------------- | ------------------------------------------- | --------------------------- |
| ReadwriteSplittingRuleAlgorithmProviderConfigurationYamlSwapper | 用于将基于算法的读写分离配置转化为读写分离标准配置    | TODO |
| DatabaseDiscoveryRuleAlgorithmProviderConfigurationYamlSwapper  | 用于将基于算法的数据库发现配置转化为数据库发现标准配置 | TODO |
| ShardingRuleAlgorithmProviderConfigurationYamlSwapper           | 用于将基于算法的分片配置转化为分片标准配置           | TODO |
| EncryptRuleAlgorithmProviderConfigurationYamlSwapper            | 用于将基于算法的加密配置转化为加密标准配置           | TODO |
| ShadowRuleAlgorithmProviderConfigurationYamlSwapper             | 用于将基于算法的影子库配置转化为影子库标准配置        | TODO |
| ReadwriteSplittingRuleConfigurationYamlSwapper                  | 用于将读写分离的 YAML 配置转化为读写分离标准配置     | TODO |
| DatabaseDiscoveryRuleConfigurationYamlSwapper                   | 用于将数据库发现的 YAML 配置转化为数据库发现标准配置  | TODO |
| AuthorityRuleConfigurationYamlSwapper                           | 用于将权限规则的 YAML 配置转化为权限规则标准配置     | TODO |
| ShardingRuleConfigurationYamlSwapper                            | 用于将分片的 YAML 配置转化为分片标准配置           | TODO |
| EncryptRuleConfigurationYamlSwapper                             | 用于将加密的 YAML 配置转化为加密标准配置           | TODO |
| ShadowRuleConfigurationYamlSwapper                              | 用于将影子库的 YAML 配置转化为影子库标准配置        | TODO |
| TransactionRuleConfigurationYamlSwapper                         | 用于将事务的 YAML 配置转化为事务标准配置           | TODO |
| SingleTableRuleConfigurationYamlSwapper                         | 用于将单表的 YAML 配置转化为单表标准配置           | TODO |
| SQLParserRuleConfigurationYamlSwapper                           | 用于将 SQL 解析的 YAML 配置转化为 SQL 解析标准配置 | TODO |
| SQLTranslatorRuleConfigurationYamlSwapper                       | 用于将 SQL 转换的 YAML 配置转化为 SQL 转换标准配置 | TODO |
| SQLTranslatorRuleConfigurationYamlSwapper                       | 用于将 SQL 转换的 YAML 配置转化为 SQL 转换标准配置 | TODO |

## ShardingSphereYamlConstruct

### 全限定类名

[`org.apache.shardingsphere.infra.yaml.engine.constructor.ShardingSphereYamlConstruct`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/yaml/engine/constructor/ShardingSphereYamlConstruct.java)

### 定义

用于将定制化对象和 YAML 相互转化

### 已知实现

| *已知实现类*                                     | *详细说明*                       | *全限定类名*                  |
| ---------------------------------------------- | ------------------------------- | --------------------------- |
| NoneShardingStrategyConfigurationYamlConstruct | 用于将不分片策略对象和 YAML 相互转化 | TODO |
