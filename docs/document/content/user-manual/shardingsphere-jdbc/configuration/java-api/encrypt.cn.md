+++
title = "数据加密"
weight = 3
+++

## 配置入口

类名称：org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration

可配置属性：

| *名称*                 | *数据类型*                                   | *说明*       |
| --------------------- | ------------------------------------------- | ------------ |
| encryptStrategies (+) | Collection\<EncryptStrategyConfiguration\>  | 加解密策略列表 |
| tables (+)            | Collection\<EncryptTableRuleConfiguration\> | 加密表规则列表 |

## 加解密策略配置

类名称：org.apache.shardingsphere.encrypt.api.config.EncryptStrategyConfiguration

可配置属性：

| *名称*      |*数据类型*   | *说明*           |
| ---------- | ---------- | ---------------- |
| name       | String     | 加解密策略名称     |
| type       | String     | 加解密策略类型     |
| properties | Properties | 加解密策略属性配置 |

Apache ShardingSphere 内置的加解密算法实现类包括：

### MD5 加解密策略

类名称：org.apache.shardingsphere.encrypt.strategy.impl.MD5Encryptor

可配置属性：无

### AES 加解密算法

类名称：org.apache.shardingsphere.encrypt.strategy.impl.AESEncryptor

可配置属性：

| *名称*         | *数据类型* | *说明*        |
| ------------- | --------- | ------------- |
| aes.key.value | String    | AES 使用的 KEY |

### RC4 加解密算法

类名称：org.apache.shardingsphere.encrypt.strategy.impl.RC4Encryptor

可配置属性：

| *名称*         | *数据类型* | *说明*        |
| ------------- | --------- | ------------- |
| rc4.key.value | String    | RC4 使用的 KEY |

## 加密表规则配置

类名称：org.apache.shardingsphere.encrypt.api.config.EncryptTableRuleConfiguration

可配置属性：

| *名称*      | *数据类型*                                    | *说明*     |
| ----------- | -------------------------------------------- | --------- |
| name        | String                                       | 表名称     |
| columns (+) | Collection\<EncryptColumnRuleConfiguration\> | 加密列列表 |

### 加密列规则配置

类名称：org.apache.shardingsphere.encrypt.api.config.EncryptColumnRuleConfiguration

可配置属性：

| *名称*                  | *数据类型* | *说明*        |
| ----------------------- | -------- | ------------- |
| name                    | String   | 逻辑列名称     |
| plainColumn (?)         | String   | 原文列名称     |
| cipherColumn            | String   | 密文列名称     |
| assistedQueryColumn (?) | String   | 查询辅助列名称 |
| encryptorName           | String   | 加密策略名称   |
