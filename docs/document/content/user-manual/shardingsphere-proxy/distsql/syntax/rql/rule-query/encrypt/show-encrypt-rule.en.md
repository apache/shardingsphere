+++
title = "SHOW ENCRYPT RULES"
weight = 1
+++

### Description

The `SHOW ENCRYPT RULES` syntax is used to query encryption rules for a specified database.

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

### Note

- When `databaseName` is not specified, then `DATABASE`is currently used as the default name. If `DATABASE` is not used, you will receive a `No database selected` prompt.

### Return value description

| Column                   | Description                               |
|--------------------------|-------------------------------------------|
| table                    | Logical table name                        |
| logic_column             | Logical column name                       |
| cipher_column            | Ciphertext column name                    |
| assisted_query_column    | Assisted query column name                |
| like_query_column        | Like query column name                    |
| encryptor_type           | Encryption algorithm type                 |
| encryptor_props          | Encryption algorithm parameter            |
| assisted_query_type      | Assisted query algorithm type             |
| assisted_query_props     | Assisted query algorithm parameter        |
| like_query_type          | Like query algorithm type                 |
| like_query_props         | Like query algorithm parameter            |




### Example

- Query encrypt rules for specified database.

```sql
SHOW ENCRYPT RULES FROM encrypt_db;
```

```sql
mysql> SHOW ENCRYPT RULES FROM encrypt_db;
+-----------+--------------+---------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+
| table     | logic_column | cipher_column | assisted_query_column | like_query_column | encryptor_type | encryptor_props         | assisted_query_type | assisted_query_props | like_query_type | like_query_props |
+-----------+--------------+---------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+
| t_user    | pwd          | pwd_cipher    |                       |                   | AES            | aes-key-value=123456abc |                     |                      |                 |                  |
| t_encrypt | pwd          | pwd_cipher    |                       |                   | AES            | aes-key-value=123456abc |                     |                      |                 |                  |
+-----------+--------------+---------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+
2 rows in set (0.00 sec)
```

- Query encrypt rules for current database.

```sql
SHOW ENCRYPT RULES;
```

```sql
mysql> SHOW ENCRYPT RULES;
+-----------+--------------+---------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+
| table     | logic_column | cipher_column | assisted_query_column | like_query_column | encryptor_type | encryptor_props         | assisted_query_type | assisted_query_props | like_query_type | like_query_props |
+-----------+--------------+---------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+
| t_user    | pwd          | pwd_cipher    |                       |                   | AES            | aes-key-value=123456abc |                     |                      |                 |                  |
| t_encrypt | pwd          | pwd_cipher    |                       |                   | AES            | aes-key-value=123456abc |                     |                      |                 |                  |
+-----------+--------------+---------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+
2 rows in set (0.00 sec)
```

- Query specified encrypt rule in specified database.

```sql
SHOW ENCRYPT TABLE RULE t_encrypt FROM encrypt_db;
```

```sql
mysql> SHOW ENCRYPT TABLE RULE t_encrypt FROM encrypt_db;
+-----------+--------------+---------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+
| table     | logic_column | cipher_column | assisted_query_column | like_query_column | encryptor_type | encryptor_props         | assisted_query_type | assisted_query_props | like_query_type | like_query_props |
+-----------+--------------+---------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+
| t_encrypt | pwd          | pwd_cipher    |                       |                   | AES            | aes-key-value=123456abc |                     |                      |                 |                  |
+-----------+--------------+---------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+
1 row in set (0.01 sec)
```

- Query specified encrypt rule in current database.

```sql
SHOW ENCRYPT TABLE RULE t_encrypt;
```

```sql
mysql> SHOW ENCRYPT TABLE RULE t_encrypt;
+-----------+--------------+---------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+
| table     | logic_column | cipher_column | assisted_query_column | like_query_column | encryptor_type | encryptor_props         | assisted_query_type | assisted_query_props | like_query_type | like_query_props |
+-----------+--------------+---------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+
| t_encrypt | pwd          | pwd_cipher    |                       |                   | AES            | aes-key-value=123456abc |                     |                      |                 |                  |
+-----------+--------------+---------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+
1 row in set (0.01 sec)
```

### Reserved word

`SHOW`, `ENCRYPT`, `TABLE`, `RULE`, `RULES`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
