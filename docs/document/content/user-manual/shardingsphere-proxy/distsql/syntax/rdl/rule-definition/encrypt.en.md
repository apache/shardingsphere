+++
title = "Encrypt"
weight = 5
+++

## Syntax

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

### Parameters Explained
| name                    | DateType   | Description                    |
|:------------------------|:-----------|:-------------------------------|
| tableName               | IDENTIFIER | Table name                     |
| columnName              | IDENTIFIER | Logic column name              |
| plainColumnName         | IDENTIFIER | Plain column name              |
| cipherColumnName        | IDENTIFIER | Cipher column name             |
| assistedQueryColumnName | IDENTIFIER | Assisted query column name     |
| likeQueryColumnName     | IDENTIFIER | Like query column name         |
| encryptAlgorithmType    | STRING     | Encryption algorithm type name |

### Notes 

- `PLAIN` specifies the plain column, `CIPHER` specifies the cipher column
- `encryptAlgorithmType` specifies the encryption algorithm type, please refer to [Encryption Algorithm](/en/user-manual/common-config/builtin-algorithm/encrypt/)
- Duplicate `tableName` will not be created
- `queryWithCipherColumn` support uppercase or lowercase true or false

## Example

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
