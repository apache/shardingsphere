+++
title = "数据加密"
weight = 3
+++

## 配置入口

类名称：org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration

可配置属性：

| *名称*          | *数据类型*                                   | *说明*      |
| -------------- | ------------------------------------------- | ----------- |
| encryptors (+) | Collection\<EncryptorConfiguration\>        | 加解密器列表 |
| tables (+)     | Collection\<EncryptTableRuleConfiguration\> | 加密表列表   |

## 加解密器配置

类名称：org.apache.shardingsphere.encrypt.api.config.EncryptorConfiguration

可配置属性：

| *名称*      |*数据类型*   | *说明*         |
| ---------- | ---------- | -------------- |
| name       | String     | 加解密器名称     |
| type       | String     | 加解密器类型     |
| properties | Properties | 加解密器属性配置 |

Apache ShardingSphere 内置的加解密器算法实现类包括：

### MD5 加解密器

类名称：org.apache.shardingsphere.encrypt.strategy.impl.MD5Encryptor

可配置属性：无

### AES 加解密器

类名称：org.apache.shardingsphere.encrypt.strategy.impl.AESEncryptor

可配置属性：

| *名称*         | *数据类型* | *说明*        |
| ------------- | --------- | ------------- |
| aes.key.value | String    | AES 使用的 KEY |

### RC4 加解密器

类名称：org.apache.shardingsphere.encrypt.strategy.impl.RC4Encryptor

可配置属性：

| *名称*         | *数据类型* | *说明*        |
| ------------- | --------- | ------------- |
| rc4.key.value | String    | RC4 使用的 KEY |

## 加密表配置

类名称：org.apache.shardingsphere.encrypt.api.config.EncryptTableRuleConfiguration

可配置属性：

| *名称*      | *数据类型*                                | *说明* |
| ----------- | ---------------------------------------- | --------- |
| name        | String                                   | 表名称     |
| columns (+) | Collection\<EncryptColumnConfiguration\> | 加密列列表 |

### 加密列配置

类名称：org.apache.shardingsphere.encrypt.api.config.EncryptColumnConfiguration

可配置属性：

| *名称*                  | *数据类型* | *说明*        |
| ----------------------- | -------- | ------------- |
| name                    | String   | 逻辑列名称     |
| plainColumn (?)         | String   | 原文列名称     |
| cipherColumn            | String   | 密文列名称     |
| assistedQueryColumn (?) | String   | 查询辅助列名称 |
| encryptorName           | String   | 加密器名称     |
