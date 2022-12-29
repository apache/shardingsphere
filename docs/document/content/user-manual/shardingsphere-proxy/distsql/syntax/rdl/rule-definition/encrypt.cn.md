+++
title = "数据加密"
weight = 5
+++

## 语法说明

```sql
CREATE ENCRYPT RULE ifNotExistsClause? encryptRuleDefinition [, encryptRuleDefinition] ...

ALTER ENCRYPT RULE encryptRuleDefinition [, encryptRuleDefinition] ...

DROP ENCRYPT RULE tableName [, tableName] ...

ifNotExistsClause:
    IF NOT EXISTS

encryptRuleDefinition:
    tableName(COLUMNS(columnDefinition [, columnDefinition] ...), QUERY_WITH_CIPHER_COLUMN=queryWithCipherColumn)

columnDefinition:
    (NAME=columnName [, PLAIN=plainColumnName] , CIPHER=cipherColumnName [, ASSISTED_QUERY_COLUMN=assistedQueryColumnName] [, LIKE_QUERY_COLUMN=likeQueryColumnName], encryptAlgorithm [, assistedQueryAlgorithm] [, likeQueryAlgorithm] [, QUERY_WITH_CIPHER_COLUMN=queryWithCipherColumn])

encryptAlgorithm:
    ENCRYPT_ALGORITHM(TYPE(NAME=encryptAlgorithmType [, PROPERTIES([algorithmProperties] )] ))

assistedQueryAlgorithm
    ASSISTED_QUERY_ALGORITHM(TYPE(NAME=encryptAlgorithmType [, PROPERTIES([algorithmProperties] )] ))

likeQueryAlgorithm
    LIKE_QUERY_ALGORITHM(TYPE(NAME=encryptAlgorithmType [, PROPERTIES([algorithmProperties] )] ))

algorithmProperties:
    algorithmProperty [, algorithmProperty] ...

algorithmProperty:
    key=value                          
```

### 参数解释
| 名称                   | 数据类型       | 说明          |
|:------------------------|:-----------|:--------------|
| tableName               | IDENTIFIER | 表名称         |
| columnName              | IDENTIFIER | 逻辑数据列名称  |
| plainColumnName         | IDENTIFIER | 明文数据列名称  |
| cipherColumnName        | IDENTIFIER | 加密数据列名称  |
| assistedQueryColumnName | IDENTIFIER | 辅助查询列名称  |
| likeQueryColumnName     | IDENTIFIER | 模糊查询列名称  |
| encryptAlgorithmType    | STRING     | 加密算法类型名称 |

### 注意事项

- `PLAIN` 指定明文数据列，`CIPHER` 指定密文数据列；
- `encryptAlgorithmType` 指定加密算法类型，请参考 [加密算法](/cn/user-manual/common-config/builtin-algorithm/encrypt/)；
- 重复的 `tableName` 将无法被创建；
- `queryWithCipherColumn` 支持大写或小写的 true 或 false。

## 示例

```sql
CREATE ENCRYPT RULE IF NOT EXISTS t_encrypt (
COLUMNS(
(NAME=user_id,PLAIN=user_plain,CIPHER=user_cipher,ASSISTED_QUERY_COLUMN=user_assisted,LIKE_QUERY_COLUMN=user_like,ENCRYPT_ALGORITHM(TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456abc'))),ASSISTED_QUERY_ALGORITHM(TYPE(NAME='MD5')), LIKE_QUERY_ALGORITHM(TYPE(NAME='CHAR_DIGEST_LIKE'))),
(NAME=order_id, CIPHER =order_cipher, ENCRYPT_ALGORITHM(TYPE(NAME='MD5')), QUERY_WITH_CIPHER_COLUMN=FALSE)
), QUERY_WITH_CIPHER_COLUMN=true),
t_encrypt_2 (
COLUMNS(
(NAME=user_id,PLAIN=user_plain,CIPHER=user_cipher, ENCRYPT_ALGORITHM(TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456abc')))),
(NAME=order_id, CIPHER=order_cipher,ENCRYPT_ALGORITHM(TYPE(NAME='MD5')))
), QUERY_WITH_CIPHER_COLUMN=FALSE);

ALTER ENCRYPT RULE t_encrypt (
COLUMNS(
(NAME=user_id,PLAIN=user_plain,CIPHER=user_cipher,ASSISTED_QUERY_COLUMN=user_assisted,LIKE_QUERY_COLUMN=user_like,ENCRYPT_ALGORITHM(TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456'))),ASSISTED_QUERY_ALGORITHM(TYPE(NAME='MD5')), LIKE_QUERY_ALGORITHM(TYPE(NAME='CHAR_DIGEST_LIKE'))),
(NAME=order_id,CIPHER=order_cipher,ENCRYPT_ALGORITHM(TYPE(NAME='MD5')), QUERY_WITH_CIPHER_COLUMN=true)
), QUERY_WITH_CIPHER_COLUMN=FALSE);

DROP ENCRYPT RULE t_encrypt,t_encrypt_2;
```
