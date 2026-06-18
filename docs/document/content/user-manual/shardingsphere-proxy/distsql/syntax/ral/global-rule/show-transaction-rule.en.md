+++
title = "SHOW TRANSACTION RULE"
weight = 2
+++

### Description

The `SHOW TRANSACTION RULE` syntax is used to query transaction rule configuration.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowTransactionRule ::=
  'SHOW' 'TRANSACTION' 'RULE'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return Value Description

| Column   | Description             |
|----------|-------------------------|
| users    | users                   |
| provider | privilege provider type |
| props    | privilege properties    |

### Example

- Query transaction rule configuration

```sql
SHOW TRANSACTION RULE;
```

```sql
mysql> SHOW TRANSACTION RULE;
+--------------+---------------+-------+
| default_type | provider_type | props |
+--------------+---------------+-------+
| LOCAL        |               |       |
+--------------+---------------+-------+
1 row in set (0.05 sec)
```

### Reserved word

`SHOW`, `TRANSACTION`, `RULE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
