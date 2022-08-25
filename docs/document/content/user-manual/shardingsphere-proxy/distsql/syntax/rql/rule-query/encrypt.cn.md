+++
title = "数据加密"
weight = 5
+++

## 语法说明

```sql
SHOW ENCRYPT RULES [FROM databaseName]

SHOW ENCRYPT TABLE RULE tableName [FROM databaseName]
```
- 支持查询所有的数据加密规则和指定逻辑表名查询。

## 返回值说明

| 列                        | 说明                |
| ------------------------- | ------------------ |
| table                     | 逻辑表名             |
| logic_column              | 逻辑列名             |
| logic_data_type           | 逻辑列数据类型        |
| cipher_column             | 密文列名             |
| cipher_data_type          | 密文列数据类型        |
| plain_column              | 明文列名             |
| plain_data_type           | 明文列数据类型        |
| assisted_query_column     | 辅助查询列名          |
| assisted_query_data_type  | 辅助查询列数据类型     |
| encryptor_type            | 加密算法类型          |
| encryptor_props           | 加密算法参数          |
| query_with_cipher_column  | 是否使用加密列进行查询  |

## 示例

*显示加密规则*
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

*显示加密表规则表名*
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
