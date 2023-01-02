+++
title = "REFRESH TABLE METADATA FROM STORAGE UNIT"
weight = 7
+++

### 描述

`REFRESH TABLE METADATA FROM STORAGE UNIT` 语法用于刷新指定存储单元中指定 `SCHEMA` 中表的元数据

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
RefreshTableMetadataFromStorageUnit ::=
  'REFRESH' 'TABLE' 'METADATA' 'FROM' 'STORAGE' 'UNIT' storageUnitName 'SCHEMA' schemaName

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

- 如果 `SCHEMA` 中不存在表，则会删除该 `SCHEMA`

### 示例

- 刷新指定存储单元中指定 `SCHEMA` 中表的元数据

```sql
REFRESH TABLE METADATA FROM STORAGE UNIT ds_1 SCHEMA db_schema;
```

### 保留字

`REFRESH`、`TABLE`、`METADATA`、`FROM`、`STORAGE`、`UNIT`、`SCHEMA`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)