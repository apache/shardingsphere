+++
title = "数据加密"
weight = 4
+++

## 定义

```sql
SHOW ENCRYPT RULES [FROM schemaName]

SHOW ENCRYPT TABLE RULE tableName [from schemaName]
```
- 支持查询所有的数据加密规则和指定逻辑表名查询

## 说明

| 列             | 说明        |
| -------------- | ---------- |
| table          | 逻辑表名     |
| logicColumn    | 逻辑列名     |
| cipherColumn   | 密文列名     |
| plainColumn    | 明文列名     |
| encryptorType  | 加密算法类型 |
| encryptorProps | 加密算法参数 |

## 示例

*SHOW ENCRYPT RULES*
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

*SHOW ENCRYPT TABLE RULE tableName*
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
