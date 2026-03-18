+++
title = "SHOW STATUS FROM READWRITE_SPLITTING RULE"
weight = 2
+++

### 描述

`SHOW STATUS FROM READWRITE_SPLITTING RULE` 语法用于查询指定逻辑库中指定读写分离规则中读写分离存储单元状态。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowStatusFromReadwriteSplittingRule ::=
  'SHOW' 'STATUS' 'FROM' 'READWRITE_SPLITTING' ('RULES' | 'RULE' groupName) ('FROM' databaseName)?

groupName ::=
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

| 列            | 说明       |
|--------------|----------|
| name         | 读写分离规则名称 |
| storage_unit | 存储单元名称   |
| status       | 存储单元状态   |

### 示例

- 查询指定逻辑库中指定读写分离规则中读写分离存储单元状态

```sql
SHOW STATUS FROM READWRITE_SPLITTING RULE ms_group_0 FROM sharding_db;
```

```sql
mysql> SHOW STATUS FROM READWRITE_SPLITTING RULE ms_group_0 FROM sharding_db;
+-------------+--------------+----------+
| name        | storage_unit | status   |
+-------------+--------------+----------+
| ms_group_0  | ds_0         | disabled |
+-------------+--------------+----------+
1 rows in set (0.01 sec)
```

- 查询指定逻辑库中所有读写分离存储单元状态

```sql
SHOW STATUS FROM READWRITE_SPLITTING RULES FROM sharding_db;
```

```sql
mysql> SHOW STATUS FROM READWRITE_SPLITTING RULES FROM sharding_db;
+-------------+--------------+----------+
| name        | storage_unit | status   |
+-------------+--------------+----------+
| ms_group_0  | ds_0         | disabled |
+-------------+--------------+----------+
1 rows in set (0.00 sec)
```

- 查询当前逻辑库中指定读写分离规则中读写分离存储单元状态

```sql
SHOW STATUS FROM READWRITE_SPLITTING RULE ms_group_0;
```

```sql
mysql> SHOW STATUS FROM READWRITE_SPLITTING RULE ms_group_0;
+-------------+--------------+----------+
| name        | storage_unit | status   |
+-------------+--------------+----------+
| ms_group_0  | ds_0         | disabled |
+-------------+--------------+----------+
1 rows in set (0.01 sec)
```

- 查询当前逻辑库中所有读写分离存储单元状态

```sql
mysql> SHOW STATUS FROM READWRITE_SPLITTING RULES;
```

```sql
mysql> SHOW STATUS FROM READWRITE_SPLITTING RULES;
+-------------+--------------+----------+
| name        | storage_unit | status   |
+-------------+--------------+----------+
| ms_group_0  | ds_0         | disabled |
+-------------+--------------+----------+
1 rows in set (0.01 sec)
```

### 保留字

`SHOW`、`STATUS`、`FROM`、`READWRITE_SPLITTING`、`RULE`、`RULES`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
