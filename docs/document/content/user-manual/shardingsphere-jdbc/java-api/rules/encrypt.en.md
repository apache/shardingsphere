+++
title = "Encryption"
weight = 4
+++

## Root Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration

Attributes:

| *Name*                    | *DataType*                                          | *Description*                                                                                  | *Default Value* |
| ------------------------- | --------------------------------------------------- | ---------------------------------------------------------------------------------------------- | --------------- |
| tables (+)                | Collection\<EncryptTableRuleConfiguration\>         | Encrypt table rule configurations                                                              |                 |
| encryptors (+)            | Map\<String, ShardingSphereAlgorithmConfiguration\> | Encrypt algorithm name and configurations                                                      |                 |
| queryWithCipherColumn (?) | boolean                                             | Whether query with cipher column for data encrypt. User you can use plaintext to query if have | true            |

## Encrypt Table Rule Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration

Attributes:

| *Name*      | *DataType*                                   | *Description*                      |
| ----------- | -------------------------------------------- | ---------------------------------- |
| name        | String                                       | Table name                         |
| columns (+) | Collection\<EncryptColumnRuleConfiguration\> | Encrypt column rule configurations |
| queryWithCipherColumn (?) | boolean                                             | The current table whether query with cipher column for data encrypt.  | true            |

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

Please refer to [Built-in Encrypt Algorithm List](/en/user-manual/shardingsphere-jdbc/builtin-algorithm/encrypt) for more details about type of algorithm.
