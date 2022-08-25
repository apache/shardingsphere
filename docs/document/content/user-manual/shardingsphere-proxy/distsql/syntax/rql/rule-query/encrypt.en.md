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
| ------------------------- | ----------------------------------------  |
| table                     | Logical table name                        |
| logic_column              | Logical column name                       |
| logic_data_type           | Logical column data type                  |
| cipher_column             | Ciphertext column name                    |
| cipher_data_type          | Ciphertext column data type               |
| plain_column              | Plaintext column name                     |
| plain_data_type           | Plaintext column data type                |
| assisted_query_column     | Assisted query column name                |
| assisted_query_data_type  | Assisted query column data type           |
| encryptor_type            | Encryption algorithm type                 |
| encryptor_props           | Encryption algorithm parameter            |
| query_with_cipher_column  | Whether to use encrypted column for query |

## Example

*Show Encrypt Rules*
```sql
mysql> SHOW ENCRYPT RULES FROM encrypt_db;
+-------------+--------------+-----------------+---------------+------------------+--------------+-----------------+-----------------------+--------------------------+----------------+-------------------------+--------------------------+
| table       | logic_column | logic_data_type | cipher_column | cipher_data_type | plain_column | plain_data_type | assisted_query_column | assisted_query_data_type | encryptor_type | encryptor_props         | query_with_cipher_column |
+-------------+--------------+-----------------+---------------+------------------+--------------+-----------------+-----------------------+--------------------------+----------------+-------------------------+--------------------------+
| t_encrypt   | user_id      |                 | user_cipher   |                  | user_plain   |                 |                       |                          | AES            | aes-key-value=123456abc | true                     |
| t_encrypt   | order_id     |                 | order_cipher  |                  |              |                 |                       |                          | MD5            |                         | true                     |
| t_encrypt_2 | user_id      |                 | user_cipher   |                  | user_plain   |                 |                       |                          | AES            | aes-key-value=123456abc | false                    |
| t_encrypt_2 | order_id     |                 | order_cipher  |                  |              |                 |                       |                          | MD5            |                         | false                    |
+-------------+--------------+-----------------+---------------+------------------+--------------+-----------------+-----------------------+--------------------------+----------------+-------------------------+--------------------------+
4 rows in set (0.78 sec)
```

*Show Encrypt Table Rule Table Name*
```sql
mysql> SHOW ENCRYPT TABLE RULE t_encrypt;
+-------------+--------------+-----------------+---------------+------------------+--------------+-----------------+-----------------------+--------------------------+----------------+-------------------------+--------------------------+
| table       | logic_column | logic_data_type | cipher_column | cipher_data_type | plain_column | plain_data_type | assisted_query_column | assisted_query_data_type | encryptor_type | encryptor_props         | query_with_cipher_column |
+-------------+--------------+-----------------+---------------+------------------+--------------+-----------------+-----------------------+--------------------------+----------------+-------------------------+--------------------------+
| t_encrypt   | user_id      |                 | user_cipher   |                  | user_plain   |                 |                       |                          | AES            | aes-key-value=123456abc | true                     |
| t_encrypt   | order_id     |                 | order_cipher  |                  |              |                 |                       |                          | MD5            |                         | true                     |
+-------------+--------------+-----------------+---------------+------------------+--------------+-----------------+-----------------------+--------------------------+----------------+-------------------------+--------------------------+
2 rows in set (0.01 sec)

mysql> SHOW ENCRYPT TABLE RULE t_encrypt FROM encrypt_db;
+-------------+--------------+-----------------+---------------+------------------+--------------+-----------------+-----------------------+--------------------------+----------------+-------------------------+--------------------------+
| table       | logic_column | logic_data_type | cipher_column | cipher_data_type | plain_column | plain_data_type | assisted_query_column | assisted_query_data_type | encryptor_type | encryptor_props         | query_with_cipher_column |
+-------------+--------------+-----------------+---------------+------------------+--------------+-----------------+-----------------------+--------------------------+----------------+-------------------------+--------------------------+
| t_encrypt   | user_id      |                 | user_cipher   |                  | user_plain   |                 |                       |                          | AES            | aes-key-value=123456abc | true                     |
| t_encrypt   | order_id     |                 | order_cipher  |                  |              |                 |                       |                          | MD5            |                         | true                     |
+-------------+--------------+-----------------+---------------+------------------+--------------+-----------------+-----------------------+--------------------------+----------------+-------------------------+--------------------------+
2 rows in set (0.01 sec))
```
