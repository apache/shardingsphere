+++
title = "数据加密"
weight = 5
+++

## 定义

```sql
SHOW ENCRYPT RULES [FROM schemaName]

SHOW ENCRYPT TABLE RULE tableName [from schemaName]
```
- 支持查询所有的数据加密规则和指定逻辑表名查询

## 说明

| 列              | 说明        |
| --------------- | ---------- |
| table           | 逻辑表名     |
| logic_column    | 逻辑列名     |
| cipher_column   | 密文列名     |
| plain_column    | 明文列名     |
| encryptor_type  | 加密算法类型  |
| encryptor_props | 加密算法参数  |

## 示例

*显示加密规则*
```sql
mysql> show encrypt rules from encrypt_db;
+-----------+--------------+---------------+--------------+----------------+-------------------------+
| table     | logic_column | cipher_column | plain_column | encryptor_type | encryptor_props         |
+-----------+--------------+---------------+--------------+----------------+-------------------------+
| t_encrypt | order_id     | order_cipher  | NULL         | MD5            |                         |
| t_encrypt | user_id      | user_cipher   | user_plain   | AES            | aes-key-value=123456abc |
| t_order   | item_id      | order_cipher  | NULL         | MD5            |                         |
| t_order   | order_id     | user_cipher   | user_plain   | AES            | aes-key-value=123456abc |
+-----------+--------------+---------------+--------------+----------------+-------------------------+
4 rows in set (0.01 sec)
```

*显示加密表规则表名*
```sql
mysql> show encrypt table rule t_encrypt;
+-----------+--------------+---------------+--------------+----------------+-------------------------+
| table     | logic_column | cipher_column | plain_column | encryptor_type | encryptor_props         |
+-----------+--------------+---------------+--------------+----------------+-------------------------+
| t_encrypt | order_id     | order_cipher  | NULL         | MD5            |                         |
| t_encrypt | user_id      | user_cipher   | user_plain   | AES            | aes-key-value=123456abc |
+-----------+--------------+---------------+--------------+----------------+-------------------------+
2 rows in set (0.00 sec)

mysql> show encrypt table rule t_encrypt from encrypt_db;
+-----------+--------------+---------------+--------------+----------------+-------------------------+
| table     | logic_column | cipher_column | plain_column | encryptor_type | encryptor_props         |
+-----------+--------------+---------------+--------------+----------------+-------------------------+
| t_encrypt | order_id     | order_cipher  | NULL         | MD5            |                         |
| t_encrypt | user_id      | user_cipher   | user_plain   | AES            | aes-key-value=123456abc |
+-----------+--------------+---------------+--------------+----------------+-------------------------+
2 rows in set (0.00 sec)
```
