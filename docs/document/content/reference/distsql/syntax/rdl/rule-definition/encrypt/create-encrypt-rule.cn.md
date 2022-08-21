+++
title = "CREATE ENCRYPT RULE"
weight = 2
+++

## 描述

`CREATE ENCRYPT RULE` 语法用于创建数据加密规则

### 语法定义

```sql
CreateEncryptRule ::=
  'CREATE' 'ENCRYPT' 'RULE' encryptDefinition ( ',' encryptDefinition )*

encryptDefinition ::=
  tableName '(' 'COLUMNS' '(' columnDefinition ( ',' columnDefinition )*  ')' ',' 'QUERY_WITH_CIPHER_COLUMN' '=' queryWithCipherColumn ')'

columnDefinition ::=
    'NAME' '=' columnName ',' ( 'PLAIN' '=' plainColumnName )? 'CIPHER' '=' cipherColumnName ','  'TYPE' '(' 'NAME' '=' encryptAlgorithmType ( ',' 'PROPERTIES' '(' 'key' '=' 'value' ( ',' 'key' '=' 'value' )* ')' )? ')'

tableName ::=
  identifier

queryWithCipherColumn ::=
  identifier

columnName ::=
  identifier

plainColumnName ::=
  identifier

cipherColumnName ::=
  identifier

encryptAlgorithmType ::=
  identifier
```

### 补充说明

- `PLAIN` 指定明文数据列，`CIPHER` 指定密文数据列；
- `encryptAlgorithmType` 指定加密算法类型，请参考 加密算法；
- 重复的 `tableName` 将无法被创建；
- queryWithCipherColumn 支持大写或小写的 `true` 或 `false`。

### 示例

#### 创建数据加密规则

```sql
CREATE ENCRYPT RULE t_encrypt (
COLUMNS(
(NAME=user_id,PLAIN=user_plain,CIPHER=user_cipher,TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456abc'))),
(NAME=order_id, CIPHER =order_cipher,TYPE(NAME='MD5'))
),QUERY_WITH_CIPHER_COLUMN=true),
t_encrypt_2 (
COLUMNS(
(NAME=user_id,PLAIN=user_plain,CIPHER=user_cipher,TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456abc'))),
(NAME=order_id, CIPHER=order_cipher,TYPE(NAME='MD5'))
), QUERY_WITH_CIPHER_COLUMN=FALSE);
```

### 保留字

`CREATE`、`ENCRYPT`、`RULE`、`COLUMNS`、`NAME`、`CIPHER`、`PLAIN`、`QUERY_WITH_CIPHER_COLUMN`、`TYPE`、`TRUE`、`FALSE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)