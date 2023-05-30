+++
title = "ALTER READWRITE_SPLITTING RULE ENABLE/DISABLE"
weight = 1
+++

## 描述

`ALTER READWRITE_SPLITTING RULE ENABLE/DISABLE` 语法用于启用/禁用指定逻辑库中指定读写分离规则中的指定读数据存储单元。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
AlterReadwriteSplittingRule ::=
  'ALTER' 'READWRITE_SPLITTING' 'RULE' groupName ('ENABLE' | 'DISABLE') storageUnitName 'FROM' databaseName

groupName ::=
  identifier

storageUnitName ::=
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

### 示例

- 禁用指定逻辑库中指定读写分离规则中的指定读数据存储单元

```sql
ALTER READWRITE_SPLITTING RULE ms_group_0 DISABLE read_ds_0 FROM sharding_db;
```

- 启用指定逻辑库中指定读写分离规则中的指定读数据存储单元

```sql
ALTER READWRITE_SPLITTING RULE ms_group_0 ENABLE read_ds_0 FROM sharding_db;
```

- 禁用当前逻辑库中指定读写分离规则中的指定读数据存储单元

```sql
ALTER READWRITE_SPLITTING RULE ms_group_0 DISABLE read_ds_0;
```

- 启用当前逻辑库中指定读写分离规则中的指定读数据存储单元

```sql
ALTER READWRITE_SPLITTING RULE ms_group_1 ENABLE read_ds_0;
```

### 保留字

`ALTER`、`READWRITE_SPLITTING`、`RULE`、`ENABLE`、`DISABLE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
