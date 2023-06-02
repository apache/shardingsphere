+++
title = "SHOW MIGRATION CHECK ALGORITHM"
weight = 9
+++

### 描述

`SHOW MIGRATION CHECK ALGORITHM` 语法用于查询数据迁移一致性校验算法。
### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowMigrationCheckAlgorithm ::=
  'SHOW' 'MIGRATION' 'CHECK' 'ALGORITHMS'
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}


### 返回值说明

| 列                        | 说明        |
|--------------------------|-----------|
| type                     | 一致性校验算法类型 |
| supported_database_types | 支持数据库类型   |
| description              | 说明        |
### 示例

- 查询数据迁移一致性校验算法

```sql
SHOW MIGRATION CHECK ALGORITHMS;
```

```sql
mysql> SHOW MIGRATION CHECK ALGORITHMS;
+-------------+--------------------------------------------------------------+----------------------------+
| type        | supported_database_types                                     | description                |
+-------------+--------------------------------------------------------------+----------------------------+
| CRC32_MATCH | MySQL                                                        | Match CRC32 of records.    |
| DATA_MATCH  | SQL92,MySQL,MariaDB,PostgreSQL,openGauss,Oracle,SQLServer,H2 | Match raw data of records. |
+-------------+--------------------------------------------------------------+----------------------------+
2 rows in set (0.03 sec)
```

### 保留字

`SHOW`、`MIGRATION`、`CHECK`、`ALGORITHMS`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)