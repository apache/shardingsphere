+++
title = "Encryption"
weight = 3
+++

## Root Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration

Attributes:

| *Name*                | *DataType*                                  | *Description*       |
| --------------------- | ------------------------------------------- | ------------------- |
| tables (+)            | Collection\<EncryptTableRuleConfiguration\> | Encrypt table rules |
| encryptStrategies (+) | Collection\<EncryptStrategyConfiguration\>  | Encrypt strategies  |

## Encrypt Strategy Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.EncryptStrategyConfiguration

Attributes:

| *Name*     | *DataType* | *Description*               |
| ---------- | ---------- | --------------------------- |
| name       | String     | Encrypt strategy name       |
| type       | String     | Encrypt strategy type       |
| properties | Properties | Encrypt strategy properties |

Apache ShardingSphere built-in implemented classes of encrypt algorithm are:

### MD5 Encrypt Algorithm

Class name: org.apache.shardingsphere.encrypt.strategy.impl.MD5EncryptAlgorithm

Attributes: None

### AES Encrypt Algorithm

Class name: org.apache.shardingsphere.encrypt.strategy.impl.AESEncryptAlgorithm

Attributes:

| *Name*        | *DataType* | *Description* |
| ------------- | ---------- | ------------- |
| aes.key.value | String     | AES KEY       |

### RC4 Encrypt Algorithm

Class name: org.apache.shardingsphere.encrypt.strategy.impl.RC4EncryptAlgorithm

Attributes:

| *Name*        | *DataType* | *Description* |
| ------------- | ---------- | ------------- |
| rc4.key.value | String     | RC4 KEY       |

## Encrypt Table Rule Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.EncryptTableRuleConfiguration

Attributes:

| *Name*      | *DataType*                                   | *Description*   |
| ----------- | -------------------------------------------- | --------------- |
| name        | String                                       | Table name      |
| columns (+) | Collection\<EncryptColumnRuleConfiguration\> | Encrypt columns |

### Encrypt Column Rule Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.EncryptColumnRuleConfiguration

Attributes:

| *Name*                  | *DataType* | *Description*              |
| ----------------------- | ---------- | -------------------------- |
| name                    | String     | Logic column name          |
| plainColumn (?)         | String     | Plain column name          |
| cipherColumn            | String     | Cipher column name         |
| assistedQueryColumn (?) | String     | Assisted query column name |
| encryptStrategyName     | String     | Encrypt strategy name      |
