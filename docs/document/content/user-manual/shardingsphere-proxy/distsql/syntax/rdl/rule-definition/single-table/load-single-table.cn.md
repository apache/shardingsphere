+++
title = "LOAD SINGLE TABLE"
weight = 1
+++

## 描述

`LOAD SINGLE TABLE` 用于加载单表。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
loadSingleTable ::=
  'LOAD' 'SINGLE' 'TABLE' tableDefinition

tableDefinition ::=
  tableIdentifier (',' tableIdentifier)*

tableIdentifier ::=
  '*.*' | '*.*.*' | storageUnitName '.*' | storageUnitName '.*.*' | storageUnitName '.' schemaName '.*' | storageUnitName '.' tableName | storageUnitName '.' schemaName '.' tableName

storageUnitName ::=
  identifier

schemaName ::=
  identifier

tableName ::=
  identifier
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- PostgreSQL 和 OpenGauss 协议下支持指定 schemaName

### 示例

- 加载指定单表

```sql
LOAD SINGLE TABLE ds_0.t_single;
```

- 加载指定存储节点中的全部单表

```sql
LOAD SINGLE TABLE ds_0.*;
```

- 加载全部单表

```sql
LOAD SINGLE TABLE *.*;
```

### 保留字

`LOAD`、`SINGLE`、`TABLE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)