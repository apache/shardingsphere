+++
title = "Encryption"
weight = 3
+++

## Root Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration

Attributes:

| *Name*         | *DataType*                                          | *Description*                             |
| -------------- | --------------------------------------------------- | ----------------------------------------- |
| tables (+)     | Collection\<EncryptTableRuleConfiguration\>         | Encrypt table rule configurations         |
| encryptors (+) | Map\<String, ShardingSphereAlgorithmConfiguration\> | Encrypt algorithm name and configurations |

## Encrypt Table Rule Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration

Attributes:

| *Name*      | *DataType*                                   | *Description*                      |
| ----------- | -------------------------------------------- | ---------------------------------- |
| name        | String                                       | Table name                         |
| columns (+) | Collection\<EncryptColumnRuleConfiguration\> | Encrypt column rule configurations |

### Encrypt Column Rule Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration

Attributes:

| *Name*                  | *DataType* | *Description*              |
| ----------------------- | ---------- | -------------------------- |
| logicColumn             | String     | Logic column name          |
| cipherColumn            | String     | Cipher column name         |
| assistedQueryColumn (?) | String     | Assisted query column name |
| plainColumn (?)         | String     | Plain column name          |
| encryptorName           | String     | Encrypt algorithm name     |

## Encrypt Algorithm Configuration

Class name: org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration

Attributes:

| *Name*     | *DataType* | *Description*                |
| ---------- | ---------- | ---------------------------- |
| name       | String     | Encrypt algorithm name       |
| type       | String     | Encrypt algorithm type       |
| properties | Properties | Encrypt algorithm properties |

Please refer to [Built-in Encrypt Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/encrypt) for more details about type of algorithm.
