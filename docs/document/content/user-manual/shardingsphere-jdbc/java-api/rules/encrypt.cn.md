+++
title = "数据加密"
weight = 4
+++

## 配置入口

类名称：org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration

可配置属性：

| *名称*                     | *数据类型*                                           | *说明*                                                 | *默认值* |
| ------------------------- | --------------------------------------------------- | ----------------------------------------------------- | ------- |
| tables (+)                | Collection\<EncryptTableRuleConfiguration\>         | 加密表规则配置                                           |        |
| encryptors (+)            | Map\<String, ShardingSphereAlgorithmConfiguration\> | 加解密算法名称和配置                                      |        |
| queryWithCipherColumn (?) | boolean                                             | 是否使用加密列进行查询。在有原文列的情况下，可以使用原文列进行查询 | true   |

## 加密表规则配置

类名称：org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration

可配置属性：

| *名称*       | *数据类型*                                    | *说明*           |
| ----------- | -------------------------------------------- | --------------- |
| name        | String                                       | 表名称           |
| columns (+) | Collection\<EncryptColumnRuleConfiguration\> | 加密列规则配置列表 |
| queryWithCipherColumn (?) | boolean                                             | 该表是否使用加密列进行查询 | true   |

## 加密列规则配置

类名称：org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration

可配置属性：

| *名称*                  | *数据类型* | *说明*        |
| ----------------------- | -------- | ------------- |
| logicColumn             | String   | 逻辑列名称     |
| cipherColumn            | String   | 密文列名称     |
| assistedQueryColumn (?) | String   | 查询辅助列名称 |
| plainColumn (?)         | String   | 原文列名称     |
| encryptorName           | String   | 加密算法名称   |

## 加解密算法配置

类名称：org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration

可配置属性：

| *名称*      |*数据类型*   | *说明*           |
| ---------- | ---------- | ---------------- |
| name       | String     | 加解密算法名称     |
| type       | String     | 加解密算法类型     |
| properties | Properties | 加解密算法属性配置 |

算法类型的详情，请参见[内置加密算法列表](/cn/user-manual/shardingsphere-jdbc/builtin-algorithm/encrypt)。
