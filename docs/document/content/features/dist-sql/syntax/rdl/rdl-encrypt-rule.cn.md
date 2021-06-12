+++
title = "数据加密"
weight = 4
+++

## 定义

```sql
CREATE ENCRYPT RULE encryptRuleDefinition [, encryptRuleDefinition] ...

ALTER ENCRYPT RULE encryptRuleDefinition [, encryptRuleDefinition] ...

DROP ENCRYPT RULE tableName [, tableName] ...

encryptRuleDefinition:
    tableName(COLUMNS(columnDefinition [, columnDefinition] ...))

columnDefinition:
    (NAME=columnName [, PLAIN=plainColumnName] , CIPHER=cipherColumnName, encryptAlgorithm)

encryptAlgorithm:
    TYPE(NAME=encryptAlgorithmType [, PROPERTIES([algorithmProperties] )] )

algorithmProperties:
    algorithmProperty [, algorithmProperty] ...

algorithmProperty:
    key=value                          
```
- `PLAIN` 指定明文数据列，`CIPHER` 指定密文数据列
- `encryptAlgorithmType` 指定加密算法类型，请参考 [加密算法](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/encrypt/)
- 重复的 `tableName` 将无法被创建

## 示例

```sql
CREATE ENCRYPT RULE t_encrypt (
COLUMNS(
(NAME=user_id,PLAIN=user_plain,CIPHER=user_cipher,TYPE(NAME=AES,PROPERTIES('aes-key-value'=123456abc))),
(NAME=order_id, CIPHER =order_cipher,TYPE(NAME=MD5))
)),
t_encrypt_2 (
COLUMNS(
(NAME=user_id,PLAIN=user_plain,CIPHER=user_cipher,TYPE(NAME=AES,PROPERTIES('aes-key-value'=123456abc))),
(NAME=order_id, CIPHER=order_cipher,TYPE(NAME=MD5))
));

ALTER ENCRYPT RULE t_encrypt (
COLUMNS(
(NAME=user_id,PLAIN=user_plain,CIPHER=user_cipher,TYPE(NAME=AES,PROPERTIES('aes-key-value'=123456abc))),
(NAME=order_id,CIPHER=order_cipher,TYPE(NAME=MD5))
));

DROP ENCRYPT RULE t_encrypt,t_encrypt_2;
```
