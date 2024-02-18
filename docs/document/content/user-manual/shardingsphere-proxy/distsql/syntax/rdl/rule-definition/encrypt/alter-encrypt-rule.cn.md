+++
title = "ALTER ENCRYPT RULE"
weight = 2
+++

## 说明

 `ALTER ENCRYPT RULE` 语法用于修改加密规则。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
AlterEncryptRule ::=
  'ALTER' 'ENCRYPT' 'RULE' encryptDefinition (',' encryptDefinition)*

encryptDefinition ::=
  ruleName '(' 'COLUMNS' '(' columnDefinition (',' columnDefinition)*  ')' ')'

columnDefinition ::=
  '(' 'NAME' '=' columnName ',' 'CIPHER' '=' cipherColumnName (',' 'ASSISTED_QUERY' '=' assistedQueryColumnName)? (',' 'LIKE_QUERY' '=' likeQueryColumnName)? ',' encryptAlgorithmDefinition (',' assistedQueryAlgorithmDefinition)? (',' likeQueryAlgorithmDefinition)? ')' 

encryptAlgorithmDefinition ::=
  'ENCRYPT_ALGORITHM' '(' 'TYPE' '(' 'NAME' '=' algorithmType (',' propertiesDefinition)? ')'

assistedQueryAlgorithmDefinition ::=
  'ASSISTED_QUERY_ALGORITHM' '(' 'TYPE' '(' 'NAME' '=' algorithmType (',' propertiesDefinition)? ')'

likeQueryAlgorithmDefinition ::=
  'LIKE_QUERY_ALGORITHM' '(' 'TYPE' '(' 'NAME' '=' algorithmType (',' propertiesDefinition)? ')'

propertiesDefinition ::=
  'PROPERTIES' '(' key '=' value (',' key '=' value)* ')'

ruleName ::=
  identifier

columnName ::=
  identifier

cipherColumnName ::=
  identifier

assistedQueryColumnName ::=
  identifier

likeQueryColumnName ::=
  identifier

algorithmType ::=
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

### 补充说明

- `CIPHER` 指定密文数据列，`ASSISTED_QUERY` 指定辅助查询列，`LIKE_QUERY` 指定模糊查询列；
- `algorithmType` 指定加密算法类型，请参考 [加密算法](/cn/user-manual/common-config/builtin-algorithm/encrypt/)；
- 重复的 `ruleName` 将无法被创建。

### 示例

- 修改加密规则

```sql
ALTER ENCRYPT RULE t_encrypt (
COLUMNS(
(NAME=user_id,CIPHER=user_cipher,ASSISTED_QUERY=assisted_query_user,LIKE_QUERY=like_query_user,ENCRYPT_ALGORITHM(TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456abc'))),ASSISTED_QUERY_ALGORITHM(TYPE(NAME='MD5')),LIKE_QUERY_ALGORITHM(TYPE(NAME='CHAR_DIGEST_LIKE'))),
(NAME=order_id,CIPHER=order_cipher,ASSISTED_QUERY=assisted_query_order,LIKE_QUERY=like_query_order,ENCRYPT_ALGORITHM(TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456abc'))),ASSISTED_QUERY_ALGORITHM(TYPE(NAME='MD5')),LIKE_QUERY_ALGORITHM(TYPE(NAME='CHAR_DIGEST_LIKE')))
));
```

### 保留字

`ALTER`、`ENCRYPT`、`RULE`、`COLUMNS`、`NAME`、`CIPHER`、`ASSISTED_QUERY`、`LIKE_QUERY`、`ENCRYPT_ALGORITHM`、`ASSISTED_QUERY_ALGORITHM`、`LIKE_QUERY_ALGORITHM`、`TYPE`、`TRUE`、`FALSE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [加密算法](/cn/user-manual/common-config/builtin-algorithm/encrypt/)
