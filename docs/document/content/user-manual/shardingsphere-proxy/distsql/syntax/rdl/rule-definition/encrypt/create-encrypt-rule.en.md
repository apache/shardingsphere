+++
title = "CREATE ENCRYPT RULE"
weight = 1
+++

## Description

The `CREATE ENCRYPT RULE` syntax is used to create encrypt rules.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
CreateEncryptRule ::=
  'CREATE' 'ENCRYPT' 'RULE' ifNotExists? encryptDefinition (',' encryptDefinition)*

ifNotExists ::=
  'IF' 'NOT' 'EXISTS'

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

- `CIPHER` specifies the cipher column, `ASSISTED_QUERY` specifies the assisted query column，`LIKE_QUERY` specifies the like query column;
- `encryptAlgorithmType` specifies the encryption algorithm type, please refer to [Encryption Algorithm](/en/user-manual/common-config/builtin-algorithm/encrypt/);
- Duplicate `ruleName` will not be created;
- `ifNotExists` clause used for avoid `Duplicate encrypt rule` error.

### Example

#### Create an encrypt rule

```sql
CREATE ENCRYPT RULE t_encrypt (
COLUMNS(
(NAME=user_id,CIPHER=user_cipher,ENCRYPT_ALGORITHM(TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456abc')))),
(NAME=order_id, CIPHER =order_cipher,ENCRYPT_ALGORITHM(TYPE(NAME='MD5')))
)),
t_encrypt_2 (
COLUMNS(
(NAME=user_id,CIPHER=user_cipher,ENCRYPT_ALGORITHM(TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456abc')))),
(NAME=order_id, CIPHER=order_cipher,ENCRYPT_ALGORITHM(TYPE(NAME='MD5')))
));
```

#### Create an encrypt rule with `ifNotExists` clause

```sql
CREATE ENCRYPT RULE IF NOT EXISTS t_encrypt (
COLUMNS(
(NAME=user_id,CIPHER=user_cipher,ENCRYPT_ALGORITHM(TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456abc')))),
(NAME=order_id, CIPHER =order_cipher,ENCRYPT_ALGORITHM(TYPE(NAME='MD5')))
)),
t_encrypt_2 (
COLUMNS(
(NAME=user_id,CIPHER=user_cipher,ENCRYPT_ALGORITHM(TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456abc')))),
(NAME=order_id, CIPHER=order_cipher,ENCRYPT_ALGORITHM(TYPE(NAME='MD5')))
));
```

### Reserved words

`CREATE`, `ENCRYPT`, `RULE`, `COLUMNS`, `NAME`, `CIPHER`, `ASSISTED_QUERY`, `LIKE_QUERY`, `ENCRYPT_ALGORITHM`, `ASSISTED_QUERY_ALGORITHM`, `LIKE_QUERY_ALGORITHM`, `TYPE`, `TRUE`, `FALSE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [Encryption Algorithm](/en/user-manual/common-config/builtin-algorithm/encrypt/)
