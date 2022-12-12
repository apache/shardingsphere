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

- `PLAIN` specifies the plain column, `CIPHER` specifies the cipher column 
- `encryptAlgorithmType` specifies the encryption algorithm type, please refer to [Encryption Algorithm](/en/user-manual/common-config/builtin-algorithm/encrypt/) 
- Duplicate `tableName` will not be created 
- `queryWithCipherColumn` support uppercase or lowercase true or false

### Example

#### Create a encrypt rule

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

### Reserved word

`CREATE`, `ENCRYPT`, `RULE`, `COLUMNS`, `NAME`, `CIPHER`, `PLAIN`, `QUERY_WITH_CIPHER_COLUMN`, `TYPE`, `TRUE`, `FALSE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
- [Encryption Algorithm](/en/user-manual/common-config/builtin-algorithm/encrypt/)
