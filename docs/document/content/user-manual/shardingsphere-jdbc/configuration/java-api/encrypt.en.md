+++
title = "Encryption"
weight = 3
+++

## Root Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration

Attributes:

| *Name*         | *DataType*                                   | *Description*                          |
| -------------- | -------------------------------------------- | -------------------------------------- |
| tables (+)     | Map\<String, EncryptTableRuleConfiguration\> | Encrypt table names and encrypt tables |
| encryptors (+) | Map\<String, EncryptorConfiguration\>        | Encryptor names and encryptors         |

## Encrypt Table Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.EncryptTableRuleConfiguration

Attributes:

| *Name*         | *DataType*                                    | *Description*                    |
| -------------- | --------------------------------------------- | -------------------------------- |
| columns (+)    | Map\<String, EncryptColumnRuleConfiguration\> | Encrypt column names and columns |

### Encrypt Column Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.EncryptColumnRuleConfiguration

Attributes:

| *Name*                  | *DataType* | *Description*              |
| ----------------------- | ---------- | -------------------------- |
| plainColumn (?)         | String     | Plain column name          |
| cipherColumn            | String     | Cipher column name         |
| assistedQueryColumn (?) | String     | Assisted query column name |
| encryptor               | String     | Encryptor type             |

## Encryptor Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.EncryptorConfiguration

Attributes:

| *Name*     | *DataType* | *Description*         |
| ---------- | ---------- | --------------------- |
| type       | String     | Encryptor type        |
| properties | Properties | Encryptor properties  |

Apache ShardingSphere built-in implemented classes of Encryptor are:

### MD5 Encryptor

Class name: org.apache.shardingsphere.encrypt.strategy.impl.MD5Encryptor

Attributes: None

### AES Encryptor

Class name: org.apache.shardingsphere.encrypt.strategy.impl.AESEncryptor

Attributes:

| *Name*        | *DataType* | *Description* |
| ------------- | ---------- | ------------- |
| aes.key.value | String     | AES KEY       |

### RC4 Encryptor

Class name: org.apache.shardingsphere.encrypt.strategy.impl.RC4Encryptor

Attributes:

| *Name*        | *DataType* | *Description* |
| ------------- | ---------- | ------------- |
| rc4.key.value | String     | RC4 KEY       |
