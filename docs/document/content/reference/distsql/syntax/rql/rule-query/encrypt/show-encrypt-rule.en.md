+++
title = "SHOW ENCRYPT RULES"
weight = 2
+++

### Description

The `SHOW ENCRYPT RULES` syntax is used to query encrypt rules for specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowEncryptRule::=
  'SHOW' 'ENCRYPT' ('RULES' | 'TABLE' 'RULE' ruleName) ('FROM' databaseName)?

ruleName ::=
  identifier

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Return value description

| Column                    | Description                               |
| ------------------------- |-------------------------------------------|
| table                     | Logical table name                        |
| logic_column              | Logical column name                       |
| cipher_column             | Ciphertext column name                    |
| plain_column              | Plaintext column name                     |
| assisted_query_column     | Assisted query column name                |
| like_query_column         | Like query column name                    |
| encryptor_type            | Encryption algorithm type                 |
| encryptor_props           | Encryption algorithm parameter            |
| assisted_query_type       | Assisted query algorithm type             |
| assisted_query_props      | Assisted query algorithm parameter        |
| like_query_type           | Like query algorithm type                 |
| like_query_props          | Like query algorithm parameter            |
| query_with_cipher_column  | Whether to use encrypted column for query |




### Example

- Query encrypt rules for specified database.

```sql
SHOW ENCRYPT RULES FROM encrypt_db;
```

```sql
mysql> SHOW ENCRYPT RULES FROM encrypt_db;
+-----------+--------------+---------------+--------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+--------------------------+
| table     | logic_column | cipher_column | plain_column | assisted_query_column | like_query_column | encryptor_type | encryptor_props         | assisted_query_type | assisted_query_props | like_query_type | like_query_props | query_with_cipher_column |
+-----------+--------------+---------------+--------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+--------------------------+
| t_user    | pwd          | pwd_cipher    | pwd_plain    |                       |                   | AES            | aes-key-value=123456abc |                     |                      |                 |                  | true                     |
| t_encrypt | pwd          | pwd_cipher    | pwd_plain    |                       |                   | AES            | aes-key-value=123456abc |                     |                      |                 |                  | true                     |
+-----------+--------------+---------------+--------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+--------------------------+
2 rows in set (0.00 sec)
```

- Query encrypt rules for current database.

```sql
SHOW ENCRYPT RULES;
```

```sql
mysql> SHOW ENCRYPT RULES;
+-----------+--------------+---------------+--------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+--------------------------+
| table     | logic_column | cipher_column | plain_column | assisted_query_column | like_query_column | encryptor_type | encryptor_props         | assisted_query_type | assisted_query_props | like_query_type | like_query_props | query_with_cipher_column |
+-----------+--------------+---------------+--------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+--------------------------+
| t_user    | pwd          | pwd_cipher    | pwd_plain    |                       |                   | AES            | aes-key-value=123456abc |                     |                      |                 |                  | true                     |
| t_encrypt | pwd          | pwd_cipher    | pwd_plain    |                       |                   | AES            | aes-key-value=123456abc |                     |                      |                 |                  | true                     |
+-----------+--------------+---------------+--------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+--------------------------+
2 rows in set (0.00 sec)
```

- Query specified encrypt rule in specified database.

```sql
SHOW ENCRYPT TABLE RULE t_encrypt FROM encrypt_db;
```

```sql
mysql> SHOW ENCRYPT TABLE RULE t_encrypt FROM encrypt_db;
+-----------+--------------+---------------+--------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+--------------------------+
| table     | logic_column | cipher_column | plain_column | assisted_query_column | like_query_column | encryptor_type | encryptor_props         | assisted_query_type | assisted_query_props | like_query_type | like_query_props | query_with_cipher_column |
+-----------+--------------+---------------+--------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+--------------------------+
| t_encrypt | pwd          | pwd_cipher    | pwd_plain    |                       |                   | AES            | aes-key-value=123456abc |                     |                      |                 |                  | true                     |
+-----------+--------------+---------------+--------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+--------------------------+
1 row in set (0.01 sec)
```

- Query specified encrypt rule in current database.

```sql
SHOW ENCRYPT TABLE RULE t_encrypt;
```

```sql
mysql> SHOW ENCRYPT TABLE RULE t_encrypt;
+-----------+--------------+---------------+--------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+--------------------------+
| table     | logic_column | cipher_column | plain_column | assisted_query_column | like_query_column | encryptor_type | encryptor_props         | assisted_query_type | assisted_query_props | like_query_type | like_query_props | query_with_cipher_column |
+-----------+--------------+---------------+--------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+--------------------------+
| t_encrypt | pwd          | pwd_cipher    | pwd_plain    |                       |                   | AES            | aes-key-value=123456abc |                     |                      |                 |                  | true                     |
+-----------+--------------+---------------+--------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+--------------------------+
1 row in set (0.01 sec)
```

### Reserved word

`SHOW`, `ENCRYPT`, `TABLE`, `RULE`, `RULES`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
