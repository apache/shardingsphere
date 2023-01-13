+++
title = "ALTER ENCRYPT RULE"
weight = 3
+++

## 说明

 `ALTER ENCRYPT RULE` 语法用于修改加密规则

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
AlterEncryptRule ::=
  'ALTER' 'ENCRYPT' 'RULE' encryptDefinition (',' encryptDefinition)*

encryptDefinition ::=
  ruleName '(' 'COLUMNS' '(' columnDefinition (',' columnDefinition)*  ')' (',' 'QUERY_WITH_CIPHER_COLUMN' '=' ('TRUE' | 'FALSE'))? ')'

columnDefinition ::=
  '(' 'NAME' '=' columnName (',' 'PLAIN' '=' plainColumnName)? ',' 'CIPHER' '=' cipherColumnName (',' 'ASSISTED_QUERY_COLUMN' '=' assistedQueryColumnName)? (',' 'LIKE_QUERY_COLUMN' '=' likeQueryColumnName)? ',' encryptAlgorithmDefinition (',' assistedQueryAlgorithmDefinition)? (',' likeQueryAlgorithmDefinition)? ')' 

encryptAlgorithmDefinition ::=
  'ENCRYPT_ALGORITHM' '(' 'TYPE' '(' 'NAME' '=' encryptAlgorithmType (',' propertiesDefinition)? ')'

assistedQueryAlgorithmDefinition ::=
  'ASSISTED_QUERY_ALGORITHM' '(' 'TYPE' '(' 'NAME' '=' encryptAlgorithmType (',' propertiesDefinition)? ')'

likeQueryAlgorithmDefinition ::=
  'LIKE_QUERY_ALGORITHM' '(' 'TYPE' '(' 'NAME' '=' encryptAlgorithmType (',' propertiesDefinition)? ')'

propertiesDefinition ::=
  'PROPERTIES' '(' key '=' value (',' key '=' value)* ')'

tableName ::=
  identifier

columnName ::=
  identifier

plainColumnName ::=
  identifier

cipherColumnName ::=
  identifier

assistedQueryColumnName ::=
  identifier

likeQueryColumnName ::=
  identifier

encryptAlgorithmType ::=
  string

key ::=
  string

value ::=
  literal
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `PLAIN` 指定明文数据列，`CIPHER` 指定密文数据列，`ASSISTED_QUERY_COLUMN` 指定辅助查询列，`LIKE_QUERY_COLUMN` 指定模糊查询列；
- `encryptAlgorithmType` 指定加密算法类型，请参考 [加密算法](/cn/user-manual/common-config/builtin-algorithm/encrypt/)；
- 重复的 `ruleName` 将无法被创建。

### 示例

- 修改加密规则

```sql
ALTER ENCRYPT RULE t_encrypt (
COLUMNS(
(NAME=user_id,PLAIN=user_plain,CIPHER=user_cipher,ENCRYPT_ALGORITHM(TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456abc')))),
(NAME=order_id,CIPHER=order_cipher,ENCRYPT_ALGORITHM(TYPE(NAME='MD5')))
), QUERY_WITH_CIPHER_COLUMN=TRUE);
```

### 保留字

`ALTER`、`ENCRYPT`、`RULE`、`COLUMNS`、`NAME`、`CIPHER`、`PLAIN`、`QUERY_WITH_CIPHER_COLUMN`、`TYPE`、`TRUE`、`FALSE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [加密算法](/cn/user-manual/common-config/builtin-algorithm/encrypt/)