+++
title = "CREATE ENCRYPT RULE"
weight = 2
+++

## Description

The `CREATE READWRITE_SPLITTING RULE` syntax is used to create a readwrite splitting rule.

### Syntax

```sql
CreateEncryptRule ::=
  'CREATE' 'ENCRYPT' 'RULE' encryptDefinition ( ',' encryptDefinition )*

encryptDefinition ::=
  ruleName '(' 'COLUMNS' '(' columnDefinition ( ',' columnDefinition )*  ')' ( ',' 'QUERY_WITH_CIPHER_COLUMN' '=' ( 'TRUE' | 'FALSE' ) )? ')'

columnDefinition ::=
  '(' 'NAME' '=' columnName ( ',' 'PLAIN' '=' plainColumnName )? ',' 'CIPHER' '=' cipherColumnName ( ',' 'ASSISTED_QUERY_COLUMN' '=' assistedQueryColumnName )? ( ',' 'LIKE_QUERY_COLUMN' '=' likeQueryColumnName )? ',' encryptAlgorithmDefinition ( ',' assistedQueryAlgorithmDefinition )? ( ',' likeQueryAlgorithmDefinition )? ')' 

encryptAlgorithmDefinition ::=
  'ENCRYPT_ALGORITHM' '(' 'TYPE' '(' 'NAME' '=' encryptAlgorithmType ( ',' propertiesDefinition )? ')'

assistedQueryAlgorithmDefinition ::=
  'ASSISTED_QUERY_ALGORITHM' '(' 'TYPE' '(' 'NAME' '=' encryptAlgorithmType ( ',' propertiesDefinition )? ')'

likeQueryAlgorithmDefinition ::=
  'LIKE_QUERY_ALGORITHM' '(' 'TYPE' '(' 'NAME' '=' encryptAlgorithmType ( ',' propertiesDefinition )? ')'

propertiesDefinition ::=
  'PROPERTIES' '(' property ( ',' property )* ')'

property ::=
  key '=' value

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

### Supplement

- `PLAIN` specifies the plain column, `CIPHER` specifies the cipher column, `ASSISTED_QUERY_COLUMN` specifies the assisted query column，`LIKE_QUERY_COLUMN` specifies the like query column
- `encryptAlgorithmType` specifies the encryption algorithm type, please refer to [Encryption Algorithm](/en/user-manual/common-config/builtin-algorithm/encrypt/)
- Duplicate `ruleName` will not be created

### Example

#### Create a encrypt rule

```sql
CREATE ENCRYPT RULE t_encrypt (
COLUMNS(
(NAME=user_id,PLAIN=user_plain,CIPHER=user_cipher,ENCRYPT_ALGORITHM(TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456abc')))),
(NAME=order_id, CIPHER =order_cipher,ENCRYPT_ALGORITHM(TYPE(NAME='MD5')))
),QUERY_WITH_CIPHER_COLUMN=true),
t_encrypt_2 (
COLUMNS(
(NAME=user_id,PLAIN=user_plain,CIPHER=user_cipher,ENCRYPT_ALGORITHM(TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456abc')))),
(NAME=order_id, CIPHER=order_cipher,ENCRYPT_ALGORITHM(TYPE(NAME='MD5')))
), QUERY_WITH_CIPHER_COLUMN=FALSE);
```

### Reserved word

`CREATE`, `ENCRYPT`, `RULE`, `COLUMNS`, `NAME`, `CIPHER`, `PLAIN`, `QUERY_WITH_CIPHER_COLUMN`, `TYPE`, `TRUE`, `FALSE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
- [Encryption Algorithm](/en/user-manual/common-config/builtin-algorithm/encrypt/)
