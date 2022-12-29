+++
title = "Encrypt"
weight = 5
+++

## Syntax

```sql
SHOW ENCRYPT RULES [FROM databaseName]

SHOW ENCRYPT TABLE RULE tableName [FROM databaseName]
```
- Support to query all data encryption rules and specify logical table name query

## Return Value Description

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

## Example

*Show Encrypt Rules*
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

*Show Encrypt Table Rule Table Name*
```sql
mysql> SHOW ENCRYPT TABLE RULE t_encrypt;
+-----------+--------------+---------------+--------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+--------------------------+
| table     | logic_column | cipher_column | plain_column | assisted_query_column | like_query_column | encryptor_type | encryptor_props         | assisted_query_type | assisted_query_props | like_query_type | like_query_props | query_with_cipher_column |
+-----------+--------------+---------------+--------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+--------------------------+
| t_encrypt | pwd          | pwd_cipher    | pwd_plain    |                       |                   | AES            | aes-key-value=123456abc |                     |                      |                 |                  | true                     |
+-----------+--------------+---------------+--------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+--------------------------+
1 row in set (0.01 sec)

mysql> SHOW ENCRYPT TABLE RULE t_encrypt FROM encrypt_db;
+-----------+--------------+---------------+--------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+--------------------------+
| table     | logic_column | cipher_column | plain_column | assisted_query_column | like_query_column | encryptor_type | encryptor_props         | assisted_query_type | assisted_query_props | like_query_type | like_query_props | query_with_cipher_column |
+-----------+--------------+---------------+--------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+--------------------------+
| t_encrypt | pwd          | pwd_cipher    | pwd_plain    |                       |                   | AES            | aes-key-value=123456abc |                     |                      |                 |                  | true                     |
+-----------+--------------+---------------+--------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+--------------------------+
1 row in set (0.01 sec)
```
