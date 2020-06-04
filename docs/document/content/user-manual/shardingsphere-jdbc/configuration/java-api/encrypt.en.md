+++
title = "Encryption"
weight = 3
+++

## Root Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration

Attributes:

| *Name*                | *DataType*                                  | *Description*       |
| --------------------- | ------------------------------------------- | ------------------- |
| encryptStrategies (+) | Collection\<EncryptStrategyConfiguration\>  | Encrypt strategies  |
| tables (+)            | Collection\<EncryptTableRuleConfiguration\> | Encrypt table rules |

## Encrypt Table Rule Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration

Attributes:

| *Name*      | *DataType*                                   | *Description*        |
| ----------- | -------------------------------------------- | -------------------- |
| name        | String                                       | Table name           |
| columns (+) | Collection\<EncryptColumnRuleConfiguration\> | Encrypt column rules |

### Encrypt Column Rule Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration

Attributes:

| *Name*                  | *DataType* | *Description*              |
| ----------------------- | ---------- | -------------------------- |
| logicColumn             | String     | Logic column name          |
| cipherColumn            | String     | Cipher column name         |
| assistedQueryColumn (?) | String     | Assisted query column name |
| plainColumn (?)         | String     | Plain column name          |
| encryptStrategyName     | String     | Encrypt strategy name      |

## Encrypt Strategy Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.strategy.EncryptStrategyConfiguration

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