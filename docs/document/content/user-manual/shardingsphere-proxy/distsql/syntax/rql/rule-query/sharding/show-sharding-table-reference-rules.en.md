+++
title = "SHOW SHARDING TABLE REFERENCE RULE"
weight = 13
+++

### Description

`SHOW SHARDING TABLE REFERENCE RULE` syntax is used to query specified sharding table reference rule in the specified logical database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowShardingBindingTableRules::=
  'SHOW' 'SHARDING' 'TABLE' 'REFERENCE' ('RULE' ruleName | 'RULES') ('FROM' databaseName)?

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

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, No database selected will be prompted.

### Return value description

| Columns                  | Descriptions                       |
|--------------------------|------------------------------------|
| name                     | Sharding table reference rule name |
| sharding_table_reference | sharding table reference           |

### Example

- Query sharding table reference rules for the specified logical database

```sql
SHOW SHARDING TABLE REFERENCE RULES FROM sharding_db;
```

```sql
mysql> SHOW SHARDING TABLE REFERENCE RULES FROM sharding_db;
+-------+--------------------------+
| name  | sharding_table_reference |
+-------+--------------------------+
| ref_0 | t_a,t_b                  |
| ref_1 | t_c,t_d                  |
+-------+--------------------------+
2 rows in set (0.00 sec)
```

- Query sharding table reference rules for the current logical database

```sql
SHOW SHARDING TABLE REFERENCE RULES;
```

```sql
mysql> SHOW SHARDING TABLE REFERENCE RULES;
+-------+--------------------------+
| name  | sharding_table_reference |
+-------+--------------------------+
| ref_0 | t_a,t_b                  |
| ref_1 | t_c,t_d                  |
+-------+--------------------------+
2 rows in set (0.00 sec)
```

- Query specified sharding table reference rule for the specified logical database

```sql
SHOW SHARDING TABLE REFERENCE RULE ref_0 FROM sharding_db;
```

```sql
mysql> SHOW SHARDING TABLE REFERENCE RULE FROM sharding_db;
+-------+--------------------------+
| name  | sharding_table_reference |
+-------+--------------------------+
| ref_0 | t_a,t_b                  |
+-------+--------------------------+
1 row in set (0.00 sec)
```

- Query specified sharding table reference rule for the current logical database

```sql
SHOW SHARDING TABLE REFERENCE RULE ref_0;
```

```sql
mysql> SHOW SHARDING TABLE REFERENCE RULE ref_0;
+-------+--------------------------+
| name  | sharding_table_reference |
+-------+--------------------------+
| ref_0 | t_a,t_b                  |
+-------+--------------------------+
1 row in set (0.00 sec)
```

### Reserved word

`SHOW`, `SHARDING`, `TABLE`, `REFERENCE`, `RULE`, `RULES`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
