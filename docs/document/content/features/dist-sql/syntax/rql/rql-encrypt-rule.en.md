+++
title = "Encrypt"
weight = 4
+++

## Definition

```sql
SHOW ENCRYPT RULES [FROM schemaName]

SHOW ENCRYPT TABLE RULE tableName [from schemaName]
```
- Support to query all data encryption rules and specify logical table name query

## Description

| Column         | Description                     |
| -------------- | ------------------------------- |
| table          | Logical table name              |
| logicColumn    | Logical column name             |
| cipherColumn   | Ciphertext column name          |
| plainColumn    | Plaintext column name           |
| encryptorType  | Encryption algorithm type       |
| encryptorProps | Encryption algorithm parameter  |

## Example

*Show Encrypt Rules*
```sql
mysql> show encrypt rules from encrypt_db;
+-----------+-------------+--------------+-------------+---------------+-------------------------+
| table     | logicColumn | cipherColumn | plainColumn | encryptorType | encryptorProps          |
+-----------+-------------+--------------+-------------+---------------+-------------------------+
| t_encrypt | order_id    | order_cipher | NULL        | MD5           |                         |
| t_encrypt | user_id     | user_cipher  | user_plain  | AES           | aes-key-value=123456abc |
| t_order   | item_id     | order_cipher | NULL        | MD5           |                         |
| t_order   | order_id    | user_cipher  | user_plain  | AES           | aes-key-value=123456abc |
+-----------+-------------+--------------+-------------+---------------+-------------------------+
4 rows in set (0.01 sec)
```

*Show Encrypt Table Rule Table Name*
```sql
mysql> show encrypt table rule t_encrypt;
+-----------+-------------+--------------+-------------+---------------+-------------------------+
| table     | logicColumn | cipherColumn | plainColumn | encryptorType | encryptorProps          |
+-----------+-------------+--------------+-------------+---------------+-------------------------+
| t_encrypt | order_id    | order_cipher | NULL        | MD5           |                         |
| t_encrypt | user_id     | user_cipher  | user_plain  | AES           | aes-key-value=123456abc |
+-----------+-------------+--------------+-------------+---------------+-------------------------+
2 rows in set (0.00 sec)

mysql> show encrypt table rule t_encrypt from encrypt_db;
+-----------+-------------+--------------+-------------+---------------+-------------------------+
| table     | logicColumn | cipherColumn | plainColumn | encryptorType | encryptorProps          |
+-----------+-------------+--------------+-------------+---------------+-------------------------+
| t_encrypt | order_id    | order_cipher | NULL        | MD5           |                         |
| t_encrypt | user_id     | user_cipher  | user_plain  | AES           | aes-key-value=123456abc |
+-----------+-------------+--------------+-------------+---------------+-------------------------+
2 rows in set (0.00 sec)
```
