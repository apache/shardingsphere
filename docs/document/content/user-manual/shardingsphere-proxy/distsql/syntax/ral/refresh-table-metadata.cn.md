+++
title = "REFRESH TABLE METADATA"
weight = 9
+++

### 描述

`REFRESH TABLE METADATA` 语法用于刷新表元数据。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
RefreshTableMetadata ::=
  'REFRESH' 'TABLE' 'METADATA' (tableName | tableName 'FROM' 'STORAGE' 'UNIT' storageUnitName ('SCHEMA' schemaName)?)?

tableName ::=
  identifier

storageUnitName ::=
  identifier

schemaName ::=
  identifier
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- 未指定 `tableName` 和 `storageUnitName` 时，默认刷新所有表的元数据；

- 刷新元数据需要使用 `DATABASE` 如果未使用 `DATABASE` 则会提示 `No database selected`；

- 如果 `SCHEMA` 中不存在表，则会删除该 `SCHEMA`。

### 示例

- 刷新指定存储单元中指定 `SCHEMA` 中指定表的元数据

```sql
REFRESH TABLE METADATA t_order FROM STORAGE UNIT ds_1 SCHEMA db_schema;
```

- 刷新指定存储单元中指定 `SCHEMA` 中所有表的元数据

```sql
REFRESH TABLE METADATA FROM STORAGE UNIT ds_1 SCHEMA db_schema;
```

- 刷新指定存储单元中指定表的元数据

```sql
REFRESH TABLE METADATA t_order FROM STORAGE UNIT ds_1;
```

- 刷新指定表的元数据

```sql
REFRESH TABLE METADATA t_order;
```

- 刷新所有表的元数据

```sql
REFRESH TABLE METADATA;
```

### 保留字

`REFRESH`、`TABLE`、`METADATA`、`FROM`、`STORAGE`、`UNIT`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)