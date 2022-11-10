+++
title = "ALTER ENCRYPT RULE"
weight = 3
+++

## 说明

 `ALTER ENCRYPT RULE` 语法用于修改加密规则

### 语法

```sql
AlterEncryptRule ::=
  'ALTER' 'ENCRYPT' 'RULE' encryptDefinition ( ',' encryptDefinition )*

encryptDefinition ::=
  tableName '(' 'COLUMNS' '(' columnDefinition ( ',' columnDefinition )*  ')' ',' 'QUERY_WITH_CIPHER_COLUMN' '=' ( 'TRUE' | 'FALSE' ) ')'

columnDefinition ::=
    'NAME' '=' columnName ',' ( 'PLAIN' '=' plainColumnName )? 'CIPHER' '=' cipherColumnName ','  'TYPE' '(' 'NAME' '=' encryptAlgorithmType ( ',' 'PROPERTIES' '(' 'key' '=' 'value' ( ',' 'key' '=' 'value' )* ')' )? ')'
    
tableName ::=
  identifier

columnName ::=
  identifier

plainColumnName ::=
  identifier

cipherColumnName ::=
  identifier

encryptAlgorithmType ::=
  string
```

### Supplement

- `PLAIN` 指定明文数据列，`CIPHER` 指定密文数据列；
- `encryptAlgorithmType` 指定加密算法类型，请参考 [加密算法](/cn/user-manual/common-config/builtin-algorithm/encrypt/)；
- 重复的 `tableName` 将无法被创建；
- `queryWithCipherColumn` 支持大写或小写的 true 或 false。

### 示例

- 修改加密规则

```sql
ALTER ENCRYPT RULE t_encrypt (
COLUMNS(
(NAME=user_id,PLAIN=user_plain,CIPHER=user_cipher,TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456abc'))),
(NAME=order_id,CIPHER=order_cipher,TYPE(NAME='MD5'))
), QUERY_WITH_CIPHER_COLUMN=TRUE);
```

### 保留字

`ALTER`、`ENCRYPT`、`RULE`、`COLUMNS`、`NAME`、`CIPHER`、`PLAIN`、`QUERY_WITH_CIPHER_COLUMN`、`TYPE`、`TRUE`、`FALSE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)
- [加密算法](/cn/user-manual/common-config/builtin-algorithm/encrypt/)