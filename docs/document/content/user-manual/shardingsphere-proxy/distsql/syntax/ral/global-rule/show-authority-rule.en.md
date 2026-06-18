+++
title = "SHOW AUTHORITY RULE"
weight = 1
+++

### Description

The `SHOW AUTHORITY RULE` syntax is used to query authority rule configuration.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowAuthorityRule ::=
  'SHOW' 'AUTHORITY' 'RULE'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return Value Description

| Column      | Description             |
|-------------|-------------------------|
| users       | users                   |
| provider    | privilege provider type |
| props       | privilege properties    |

### Example

- Query authority rule configuration

```sql
SHOW AUTHORITY RULE;
```

```sql
mysql> SHOW AUTHORITY RULE;
+--------------------+---------------+-------+
| users              | provider      | props |
+--------------------+---------------+-------+
| root@%; sharding@% | ALL_PERMITTED |       |
+--------------------+---------------+-------+
1 row in set (0.07 sec)
```

### Reserved word

`SHOW`, `AUTHORITY`, `RULE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
