+++
title = "SHOW SHARDING TABLE NODES"
weight = 10

+++

### Description

`SHOW SHARDING TABLE RULES USED ALGORITHM` syntax is used to query sharding rules used specified sharding algorithm in specified logical database

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowShardingTableRulesUsedAlgorithm::=
  'SHOW' 'SHARDING' 'TABLE' 'RULES' 'USED' 'ALGORITHM' algorithmName ('FROM' databaseName)?

algorithmName ::=
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

- When databaseName is not specified, the default is the currently used DATABASE. If DATABASE is not used, No database selected will be prompted.

### Return value description

| Columns | Descriptions       |
|---------|--------------------|
| type    | Sharding rule type |
| name    | Sharding rule name |

### Example

- Query sharding table rules for the specified sharding algorithm in spicified logical database

```sql
SHOW SHARDING TABLE RULES USED ALGORITHM table_inline FROM sharding_db;
```

```sql
mysql> SHOW SHARDING TABLE RULES USED ALGORITHM table_inline FROM sharding_db;
+-------+--------------+
| type  | name         |
+-------+--------------+
| table | t_order_item |
+-------+--------------+
1 row in set (0.00 sec)
```

- Query sharding table rules for specified sharding algorithm in the current logical database

```sql
SHOW SHARDING TABLE RULES USED ALGORITHM table_inline;
```

```sql
mysql> SHOW SHARDING TABLE RULES USED ALGORITHM table_inline;
+-------+--------------+
| type  | name         |
+-------+--------------+
| table | t_order_item |
+-------+--------------+
1 row in set (0.01 sec)
```

### Reserved word

`SHOW`, `SHARDING`, `TABLE`, `RULES`, `USED`, `ALGORITHM`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
