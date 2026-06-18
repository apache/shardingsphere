+++
title = "UNLOAD SINGLE TABLE"
weight = 2
+++

## 描述

`UNLOAD SINGLE TABLE` 用于卸载单表。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
unloadSingleTable ::=
  'UNLOAD' 'SINGLE' 'TABLE' tableNames

tableNames ::=
  tableName (',' tableName)*

tableName ::=
  identifier
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- 与加载不同，卸载单表时仅需指定表名

### 示例

- 卸载指定单表

```sql
UNLOAD SINGLE TABLE t_single;
```

- 卸载全部单表

```sql
UNLOAD SINGLE TABLE *;
-- 或
UNLOAD ALL SINGLE TABLES;
```

### 保留字

`UNLOAD`、`SINGLE`、`TABLE` 、`ALL` 、`TABLES`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)