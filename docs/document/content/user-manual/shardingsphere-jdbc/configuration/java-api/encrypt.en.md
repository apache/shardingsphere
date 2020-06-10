+++
title = "Encryption"
weight = 3
+++

## Root Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration

Attributes:

| *Name*         | *DataType*                                   | *Description*                              |
| -------------- | -------------------------------------------- | ------------------------------------------ |
| tables (+)     | Collection\<EncryptTableRuleConfiguration\>  | Encrypt table rule configurations          |
| encryptors (+) | Map\<String, EncryptAlgorithmConfiguration\> | Encrypt algorithm name and configurations  |

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

Class name: org.apache.shardingsphere.encrypt.api.config.strategy.EncryptAlgorithmConfiguration

Attributes:

| *Name*     | *DataType* | *Description*                |
| ---------- | ---------- | ---------------------------- |
| name       | String     | Encrypt algorithm name       |
| type       | String     | Encrypt algorithm type       |
| properties | Properties | Encrypt algorithm properties |

Apache ShardingSphere built-in implemented classes of encrypt algorithm are:

### MD5 Encrypt Algorithm

Class name: org.apache.shardingsphere.encrypt.algorithm.MD5EncryptAlgorithm

Attributes: None

### AES Encrypt Algorithm

Class name: org.apache.shardingsphere.encrypt.algorithm.AESEncryptAlgorithm

Attributes:

| *Name*        | *DataType* | *Description* |
| ------------- | ---------- | ------------- |
| aes.key.value | String     | AES KEY       |

### RC4 Encrypt Algorithm

Class name: org.apache.shardingsphere.encrypt.algorithm.RC4EncryptAlgorithm

Attributes:

| *Name*        | *DataType* | *Description* |
| ------------- | ---------- | ------------- |
| rc4.key.value | String     | RC4 KEY       |