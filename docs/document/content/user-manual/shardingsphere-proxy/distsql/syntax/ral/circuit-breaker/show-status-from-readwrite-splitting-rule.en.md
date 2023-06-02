+++
title = "SHOW STATUS FROM READWRITE_SPLITTING RULE"
weight = 2
+++

### Description

The `SHOW STATUS FROM READWRITE_SPLITTING RULE` syntax is used to query readwrite-splitting storage unit status for specified readwrite-splitting rule in specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowStatusFromReadwriteSplittingRule ::=
  'SHOW' 'STATUS' 'FROM' 'READWRITE_SPLITTING' ('RULES' | 'RULE' groupName) ('FROM' databaseName)?

groupName ::=
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

### Return Value Description

| Columns        | Description         |
|----------------|---------------------|
| resource       | storage unit name   |
| status         | storage unit status |

### Example

- Query readwrite-splitting storage unit status for specified readwrite-splitting rule in specified database.

```sql
SHOW STATUS FROM READWRITE_SPLITTING RULE ms_group_0 FROM sharding_db;
```

```sql
mysql> SHOW STATUS FROM READWRITE_SPLITTING RULE ms_group_0 FROM sharding_db;
+----------+---------+
| resource | status  |
+----------+---------+
| ds_0     | enabled |
| ds_1     | enabled |
| ds_2     | enabled |
| ds_3     | enabled |
+----------+---------+
4 rows in set (0.01 sec)
```

- Query all readwrite-splitting storage unit from specified database

```sql
SHOW STATUS FROM READWRITE_SPLITTING RULES FROM sharding_db;
```

```sql
mysql> SHOW STATUS FROM READWRITE_SPLITTING RULES FROM sharding_db;
+----------+---------+
| resource | status  |
+----------+---------+
| ds_0     | enabled |
| ds_1     | enabled |
| ds_2     | enabled |
| ds_3     | enabled |
+----------+---------+
4 rows in set (0.00 sec)
```

- Query readwrite-splitting storage unit status for specified readwrite-splitting rule in current database

```sql
SHOW STATUS FROM READWRITE_SPLITTING RULE ms_group_0;
```

```sql
mysql> SHOW STATUS FROM READWRITE_SPLITTING RULE ms_group_0;
+----------+---------+
| resource | status  |
+----------+---------+
| ds_0     | enabled |
| ds_1     | enabled |
| ds_2     | enabled |
| ds_3     | enabled |
+----------+---------+
4 rows in set (0.01 sec)
```

- Query all readwrite-splitting storage unit from current database

```sql
mysql> SHOW STATUS FROM READWRITE_SPLITTING RULES;
```

```sql
mysql> SHOW STATUS FROM READWRITE_SPLITTING RULES;
+----------+---------+
| resource | status  |
+----------+---------+
| ds_0     | enabled |
| ds_1     | enabled |
| ds_2     | enabled |
| ds_3     | enabled |
+----------+---------+
4 rows in set (0.01 sec)
```

### Reserved word

`SHOW`, `STATUS`, `FROM`, `READWRITE_SPLITTING`, `RULE`, `RULES`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
