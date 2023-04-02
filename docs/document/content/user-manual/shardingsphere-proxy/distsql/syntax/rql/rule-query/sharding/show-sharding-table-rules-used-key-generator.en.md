+++
title = "SHOW SHARDING TABLE RULES USED KEY GENERATOR"
weight = 11

+++

### Description

`SHOW SHARDING TABLE RULES USED ALGORITHM` syntax is used to query sharding rules used specified sharding key generator in specified logical database

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowShardingTableRulesUsedKeyGenerator::=
  'SHOW' 'SHARDING' 'TABLE' 'RULES' 'USED' 'KEY' 'GENERATOR' keyGeneratorName ('FROM' databaseName)?

keyGeneratorName ::=
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

- Query sharding table rules for the specified sharding key generator in spicified logical database

```sql
SHOW SHARDING TABLE RULES USED KEY GENERATOR snowflake_key_generator FROM sharding_db;
```

```sql
mysql> SHOW SHARDING TABLE RULES USED KEY GENERATOR snowflake_key_generator FROM sharding_db;
+-------+--------------+
| type  | name         |
+-------+--------------+
| table | t_order_item |
+-------+--------------+
1 row in set (0.00 sec)
```

- Query sharding table rules for specified sharding key generator in the current logical database

```sql
SHOW SHARDING TABLE RULES USED KEY GENERATOR snowflake_key_generator;
```

```sql
mysql> SHOW SHARDING TABLE RULES USED KEY GENERATOR snowflake_key_generator;
+-------+--------------+
| type  | name         |
+-------+--------------+
| table | t_order_item |
+-------+--------------+
1 row in set (0.01 sec)
```

### Reserved word

`SHOW`, `SHARDING`, `TABLE`, `USED`, `KEY`, `GENERATOR`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
