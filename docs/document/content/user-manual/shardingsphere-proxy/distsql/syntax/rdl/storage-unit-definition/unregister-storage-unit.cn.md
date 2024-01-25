+++
title = "UNREGISTER STORAGE UNIT"
weight = 3
+++

### 描述

`UNREGISTER STORAGE UNIT` 语法用于从当前逻辑库中移除存储单元。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
UnregisterStorageUnit ::=
  'UNREGISTER' 'STORAGE' 'UNIT' ifExists? storageUnitName (',' storageUnitName)* (ignoreSingleTables | ignoreBroadcastTables | ignoreSingleAndBroadcastTables)?

ignoreSingleTables ::=
    'IGNORE' 'SINGLE' 'TABLES'

ignoreBroadcastTables ::=
    'IGNORE' 'BROADCAST' 'TABLES'

ignoreSingleAndBroadcastTables ::=
    'IGNORE' ('SINGLE' ',' 'BROADCAST' | 'BROADCAST' ',' 'SINGLE') 'TABLES'

ifExists ::=
  'IF' 'EXISTS'

storageUnitName ::=
  identifier
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `UNREGISTER STORAGE UNIT` 只会移除 Proxy 中的存储单元，不会删除与存储单元对应的真实数据源；
- 无法移除已经被规则使用的存储单元。移除被规则使用的存储单元时会提示 `Storage unit are still in used`；
- 将要移除的存储单元中仅包含 `SINGLE RULE`、`BROADCAST RULE`，且用户确认可以忽略该限制时，可添加 `IGNORE SINGLE TABLES` 、 `IGNORE BROADCAST TABLES` 、 `IGNORE SINGLE, BROADCAST TABLES` 关键字移除存储单元；
- `ifExists` 子句用于避免 `Storage unit not exists` 错误。

### 示例

- 移除存储单元

```sql
UNREGISTER STORAGE UNIT ds_0;
```

- 移除多个存储单元

```sql
UNREGISTER STORAGE UNIT ds_0, ds_1;
```

- 忽略单表移除存储单元

```sql
UNREGISTER STORAGE UNIT ds_0 IGNORE SINGLE TABLES;
```

- 忽略广播表移除存储单元

```sql
UNREGISTER STORAGE UNIT ds_0 IGNORE BROADCAST TABLES;
```

- 忽略单表和广播表移除存储单元

```sql
UNREGISTER STORAGE UNIT ds_0 IGNORE SINGLE, BROADCAST TABLES;
```

- 使用 `ifExists` 子句移除存储单元

```sql
UNREGISTER STORAGE UNIT IF EXISTS ds_0;
```

### 保留字

`DROP`、`STORAGE`、`UNIT`、`IF`、`EXISTS`、`IGNORE`、`SINGLE`、`BROADCAST`、`TABLES`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)