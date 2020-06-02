+++
title = "数据加密"
weight = 3
+++

## 配置入口

类名称：org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration

可配置属性：

| *名称*          | *数据类型*                                   | *说明*            |
| -------------- | -------------------------------------------- | ---------------- |
| tables (+)     | Map\<String, EncryptTableRuleConfiguration\> | 加密表名称和列表   |
| encryptors (+) | Map\<String, EncryptorConfiguration\>        | 加解密器名称和列表 |

## 加密表配置

类名称：org.apache.shardingsphere.encrypt.api.config.EncryptTableRuleConfiguration

可配置属性：

| *名称*          | *数据类型*                                    | *说明*          |
| -------------- | --------------------------------------------- | -------------- |
| columns (+)    | Map\<String, EncryptColumnConfiguration\>     | 加密列名称和列表 |

### 加密列配置

类名称：org.apache.shardingsphere.encrypt.api.config.EncryptColumnConfiguration

可配置属性：

| *名称*                  | *数据类型* | *说明*       |
| ----------------------- | -------- | ------------ |
| plainColumn (?)         | String   | 原文列名称    |
| cipherColumn            | String   | 密文列名称    |
| assistedQueryColumn (?) | String   | 查询辅助列名称 |
| encryptor               | String   | 加密器类型    |

## 加解密器配置

类名称：org.apache.shardingsphere.encrypt.api.config.EncryptorConfiguration

可配置属性：

| *名称*      |*数据类型*   | *说明*         |
| ---------- | ---------- | -------------- |
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
