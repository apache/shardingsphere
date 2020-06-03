+++
title = "Encryption"
weight = 3
+++

## Root Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration

Attributes:

| *Name*         | *DataType*                                   | *Description*  |
| -------------- | -------------------------------------------- | -------------- |
| encryptors (+) | Map\<String, EncryptorConfiguration\>        | Encryptors     |
| tables (+)     | Collection\<EncryptTableRuleConfiguration\>  | Encrypt tables |

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

## Encrypt Table Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.EncryptTableRuleConfiguration

Attributes:

| *Name*      | *DataType*                               | *Description*   |
| ----------- | ---------------------------------------- | --------------- |
| name        | String                                   | Table name      |
| columns (+) | Collection\<EncryptColumnConfiguration\> | Encrypt columns |

### Encrypt Column Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.EncryptColumnConfiguration

Attributes:

| *Name*                  | *DataType* | *Description*              |
| ----------------------- | ---------- | -------------------------- |
| name                    | String     | Logic column name          |
| plainColumn (?)         | String     | Plain column name          |
| cipherColumn            | String     | Cipher column name         |
| assistedQueryColumn (?) | String     | Assisted query column name |
| encryptor               | String     | Encryptor type             |
