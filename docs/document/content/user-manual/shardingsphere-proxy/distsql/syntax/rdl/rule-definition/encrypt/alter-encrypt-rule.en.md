+++
title = "ALTER ENCRYPT RULE"
weight = 2
+++

## Description

The `ALTER ENCRYPT RULE` syntax is used to alter encryption rules.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
AlterEncryptRule ::=
  'ALTER' 'ENCRYPT' 'RULE' encryptDefinition (',' encryptDefinition)*

encryptDefinition ::=
  ruleName '(' 'COLUMNS' '(' columnDefinition (',' columnDefinition)*  ')' ')'

columnDefinition ::=
  '(' 'NAME' '=' columnName ',' 'CIPHER' '=' cipherColumnName (',' 'ASSISTED_QUERY' '=' assistedQueryColumnName)? (',' 'LIKE_QUERY' '=' likeQueryColumnName)? ',' encryptAlgorithmDefinition (',' assistedQueryAlgorithmDefinition)? (',' likeQueryAlgorithmDefinition)? ')' 

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
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `CIPHER` specifies the cipher column, `ASSISTED_QUERY` specifies the assisted query column，`LIKE_QUERY` specifies the like query column
- `encryptAlgorithmType` specifies the encryption algorithm type, please refer to [Encryption Algorithm](/en/user-manual/common-config/builtin-algorithm/encrypt/)

### Example

- Alter an encrypt rule

```sql
ALTER ENCRYPT RULE t_encrypt (
COLUMNS(
(NAME=user_id,CIPHER=user_cipher,ENCRYPT_ALGORITHM(TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456abc')))),
(NAME=order_id,CIPHER=order_cipher,ENCRYPT_ALGORITHM(TYPE(NAME='MD5')))
));
```

### Reserved words

`ALTER`, `ENCRYPT`, `RULE`, `COLUMNS`, `NAME`, `CIPHER`, `ASSISTED_QUERY`, `LIKE_QUERY`, `ENCRYPT_ALGORITHM`, `ASSISTED_QUERY_ALGORITHM`, `LIKE_QUERY_ALGORITHM`, `TYPE`, `TRUE`, `FALSE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [Encryption Algorithm](/en/user-manual/common-config/builtin-algorithm/encrypt/)