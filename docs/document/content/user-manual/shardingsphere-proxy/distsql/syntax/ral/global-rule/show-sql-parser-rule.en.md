+++
title = "SHOW SQL_PARSER RULE"
weight = 4
+++

### Description

The `SHOW SQL_PARSER RULE` syntax is used to query sql parser rule configuration.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowSqlParserRule ::=
  'SHOW' 'SQL_PARSER' 'RULE'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return Value Description

| Column                    | Description                       |
|---------------------------|-----------------------------------|
| parse_tree_cache          | parse tree cache configuration    |
| sql_statement_cache       | SQL statement cache configuration |

### Example

- Query sql parser rule configuration

```sql
SHOW SQL_PARSER RULE;
```

```sql
mysql> SHOW SQL_PARSER RULE;
+-----------------------------------------+-------------------------------------------+
| parse_tree_cache                        | sql_statement_cache                       |
+-----------------------------------------+-------------------------------------------+
| initialCapacity: 128, maximumSize: 1024 | initialCapacity: 2000, maximumSize: 65535 |
+-----------------------------------------+-------------------------------------------+
1 row in set (0.05 sec)
```

### Reserved word

`SHOW`, `SQL_PARSER`, `RULE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
