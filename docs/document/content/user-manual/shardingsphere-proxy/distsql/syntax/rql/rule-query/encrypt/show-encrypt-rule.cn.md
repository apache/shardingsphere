+++
title = "SHOW ENCRYPT RULES"
weight = 1
+++

### 描述

`SHOW ENCRYPT RULES` 语法用于查询指定逻辑库中的数据加密规则。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowEncryptRule::=
  'SHOW' 'ENCRYPT' ('RULES' | 'TABLE' 'RULE' ruleName) ('FROM' databaseName)?

ruleName ::=
  identifier

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列                        | 说明          |
|--------------------------|-------------|
| table                    | 逻辑表名        |
| logic_column             | 逻辑列名        |
| cipher_column            | 密文列名        |
| assisted_query_column    | 辅助查询列名      |
| like_query_column        | 模糊查询列名      |
| encryptor_type           | 加密算法类型      |
| encryptor_props          | 加密算法参数      |
| assisted_query_type      | 辅助查询算法类型    |
| assisted_query_props     | 辅助查询算法参数    |
| like_query_type          | 模糊查询算法类型    |
| like_query_props         | 模糊查询算法参数    |

### 示例

- 查询指定逻辑库中的数据加密规则

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
+-----------+--------------+---------------+--------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+
2 rows in set (0.00 sec)
```

- 查询当前逻辑库中的数据加密规则

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
+-----------+--------------+---------------+--------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+
2 rows in set (0.00 sec)
```

- 查询指定逻辑库中指定的数据加密规则

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

- 查询当前逻辑库中指定的数据加密规则

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

### 保留字

`SHOW`、`ENCRYPT`、`TABLE`、`RULE`、`RULES`、`FROM`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)

