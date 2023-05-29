+++
title = "SHOW READWRITE_SPLITTING RULE"
weight = 1
+++

### Description

The `SHOW READWRITE_SPLITTING RULE` syntax is used to query specified readwrite-splitting rules for specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowReadWriteSplittingRule::=
  'SHOW' 'READWRITE_SPLITTING' ('RULE' ruleName | 'RULES') ('FROM' databaseName)?

ruleName ::=
  identifier  

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Return value description

| Column                            | Description                                          |
|-----------------------------------|------------------------------------------------------|
| name                              | Readwrite-splitting rule name                        |
| write_data_source_name            | Write data source name                               |
| read_data_source_names            | Read data source name list                           |
| transactional_read_query_strategy | Routing strategy for read query within a transaction |
| load_balancer_type                | Load balance algorithm type                          |
| load_balancer_props               | Load balance algorithm parameter                     |

### Example

- Query readwrite-splitting rules for specified database.

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

- Query readwrite-splitting rules for current database.

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

- Query specified readwrite-splitting rule for specified database.

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

- Query specified readwrite-splitting rule for current database.

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

### Reserved word

`SHOW`, `READWRITE_SPLITTING`, `RULE`, `RULES`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
