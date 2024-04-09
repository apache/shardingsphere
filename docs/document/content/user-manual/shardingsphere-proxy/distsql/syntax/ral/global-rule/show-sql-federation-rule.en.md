+++
title = "SHOW SQL_FEDERATION RULE"
weight = 8
+++

### Description

The `SHOW SQL_FEDERATION RULE` syntax is used to query the federated query configuration.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowSQLFederationRule ::=
  'SHOW' 'SQL_FEDERATION' 'RULE'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return Value Description

| Column                       | Description                                |
|------------------------------|--------------------------------------------|
| sql_federation_enabled       | SQL federation enabled configuration       |
| all_query_use_sql_federation | all query use SQL federation configuration |
| execution_plan_cache         | execution plan cache configuration         |

### Example

- Query sql federation rule configuration

```sql
SHOW SQL_FEDERATION RULE;
```

```sql
mysql> show sql_federation rule;
+------------------------+------------------------------+-------------------------------------------+
| sql_federation_enabled | all_query_use_sql_federation | execution_plan_cache                      |
+------------------------+------------------------------+-------------------------------------------+
| true                   | false                        | initialCapacity: 2000, maximumSize: 65535 |
+------------------------+------------------------------+-------------------------------------------+
1 row in set (0.31 sec)
```

### Reserved word

`SHOW`、`SQL_FEDERATION`、`RULE`

### Related links

- [Related links](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
