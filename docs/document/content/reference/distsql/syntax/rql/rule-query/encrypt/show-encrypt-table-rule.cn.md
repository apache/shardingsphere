+++
title = "SHOW ENCRYPT TABLE RULE"
weight = 3
+++

### 描述

`SHOW ENCRYPT TABLE RULE` 语法用于查询指定逻辑库中指定表的数据加密规则。

### 语法

```sql
ShowEncryptTableRule::=
  'SHOW' 'ENCRYPT' 'TABLE' 'RULE' tabeName ('FROM' databaseName)?

tableName ::=
  identifier

databaseName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列                        | 说明                |
|--------------------------| ------------------ |
| table                    | 逻辑表名             |
| logic_column             | 逻辑列名             |
| cipher_column            | 密文列名             |
| plain_column             | 明文列名             |
| assisted_query_column    | 辅助查询列名          |
| like_query_column        | 模糊查询列名          |
| encryptor_type           | 加密算法类型          |
| encryptor_props          | 加密算法参数          |
| assisted_query_type      | 辅助查询算法类型       |
| assisted_query_props     | 辅助查询算法参数       |
| like_query_type          | 模糊查询算法类型       |
| like_query_props         | 模糊查询算法参数       |
| query_with_cipher_column | 是否使用加密列进行查询  |

### 示例

- 查询指定逻辑库中指定表的数据加密规则

```sql
SHOW ENCRYPT TABLE RULE t_encrypt FROM test1;
```

```sql
mysql> SHOW ENCRYPT TABLE RULE t_encrypt FROM test1;
+-----------+--------------+---------------+--------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+--------------------------+
| table     | logic_column | cipher_column | plain_column | assisted_query_column | like_query_column | encryptor_type | encryptor_props         | assisted_query_type | assisted_query_props | like_query_type | like_query_props | query_with_cipher_column |
+-----------+--------------+---------------+--------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+--------------------------+
| t_encrypt | pwd          | pwd_cipher    | pwd_plain    |                       |                   | AES            | aes-key-value=123456abc |                     |                      |                 |                  | true                     |
+-----------+--------------+---------------+--------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+--------------------------+
1 row in set (0.01 sec)
```

- 查询当前逻辑库中指定表的数据加密规则

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

### 保留字

`SHOW`、`ENCRYPT`、`TABLE`、`RULE`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)

