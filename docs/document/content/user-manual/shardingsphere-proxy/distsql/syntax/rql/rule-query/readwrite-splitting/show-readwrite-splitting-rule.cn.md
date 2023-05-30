+++
title = "SHOW READWRITE_SPLITTING RULE"
weight = 1
+++

### 描述

`SHOW READWRITE_SPLITTING RULE` 语法用于查询指定逻辑库中的指定读写分离规则。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowReadWriteSplittingRule::=
  'SHOW' 'READWRITE_SPLITTING' ('RULE' ruleName | 'RULES') ('FROM' databaseName)?

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

| 列                                | 说明                |
|---------------------------------- |------------------- |
| name                              | 读写分离规则名称     |
| write_data_source_name            | 写数据源名称         |
| read_data_source_names            | 读数据源名称列表     |
| transactional_read_query_strategy | 事务内读请求路由策略  |
| load_balancer_type                | 负载均衡算法类型      |
| load_balancer_props               | 负载均衡算法参数      |

### 示例

- 查询指定逻辑库中的读写分离规则

```sql
SHOW READWRITE_SPLITTING RULES FROM readwrite_splitting_db;
```

```sql
mysql> SHOW READWRITE_SPLITTING RULES FROM readwrite_splitting_db;
+------------+-------------------------+-------------------------+-----------------------------------+--------------------+---------------------+
| name       | write_storage_unit_name | read_storage_unit_names | transactional_read_query_strategy | load_balancer_type | load_balancer_props |
+------------+-------------------------+-------------------------+-----------------------------------+--------------------+---------------------+
| ms_group_0 | write_ds                | read_ds_0,read_ds_1     | DYNAMIC                           | random             |                     |
+------------+-------------------------+-------------------------+-----------------------------------+--------------------+---------------------+
1 row in set (0.01 sec)
```

- 查询当前逻辑库中的读写分离规则

```sql
SHOW READWRITE_SPLITTING RULES;
```

```sql
mysql> SHOW READWRITE_SPLITTING RULES;
+------------+-------------------------+-------------------------+-----------------------------------+--------------------+---------------------+
| name       | write_storage_unit_name | read_storage_unit_names | transactional_read_query_strategy | load_balancer_type | load_balancer_props |
+------------+-------------------------+-------------------------+-----------------------------------+--------------------+---------------------+
| ms_group_0 | write_ds                | read_ds_0,read_ds_1     | DYNAMIC                           | random             |                     |
+------------+-------------------------+-------------------------+-----------------------------------+--------------------+---------------------+
1 row in set (0.01 sec)
```

- 查询指定逻辑库中的指定读写分离规则

```sql
SHOW READWRITE_SPLITTING RULE ms_group_0 FROM readwrite_splitting_db;
```

```sql
mysql> SHOW READWRITE_SPLITTING RULE ms_group_0 FROM readwrite_splitting_db;
+------------+-------------------------+-------------------------+-----------------------------------+--------------------+---------------------+
| name       | write_storage_unit_name | read_storage_unit_names | transactional_read_query_strategy | load_balancer_type | load_balancer_props |
+------------+-------------------------+-------------------------+-----------------------------------+--------------------+---------------------+
| ms_group_0 | write_ds                | read_ds_0,read_ds_1     | DYNAMIC                           | random             |                     |
+------------+-------------------------+-------------------------+-----------------------------------+--------------------+---------------------+
1 row in set (0.01 sec)
```

- 查询当前逻辑库中的指定读写分离规则

```sql
SHOW READWRITE_SPLITTING RULE ms_group_0;
```

```sql
mysql> SHOW READWRITE_SPLITTING RULE ms_group_0;
+------------+-------------------------+-------------------------+-----------------------------------+--------------------+---------------------+
| name       | write_storage_unit_name | read_storage_unit_names | transactional_read_query_strategy | load_balancer_type | load_balancer_props |
+------------+-------------------------+-------------------------+-----------------------------------+--------------------+---------------------+
| ms_group_0 | write_ds                | read_ds_0,read_ds_1     | DYNAMIC                           | random             |                     |
+------------+-------------------------+-------------------------+-----------------------------------+--------------------+---------------------+
1 row in set (0.01 sec)
```

### 保留字

`SHOW`、`READWRITE_SPLITTING`、`RULE`、`RULES`、`FROM`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
