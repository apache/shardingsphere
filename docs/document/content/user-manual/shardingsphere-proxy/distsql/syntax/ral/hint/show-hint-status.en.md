+++
title = "SHOW HINT STATUS"
weight = 7
+++

### Description

The `SHOW HINT STATUS` syntax is used to query hint settings for current connection.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowHintStatus ::=
  'SHOW' ('SHARDING' | 'READWRITE_SPLITTING') 'HINT' 'STATUS'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- When `SHARDING`/`READWRITE_SPLITTING` is not specified, the default is clear all hint settings.

### Example

- Query hint settings of sharding

```sql
SHOW SHARDING HINT STATUS;
```

- Query hint settings of readwrite splitting

```sql
SHOW READWRITE_SPLITTING HINT STATUS;
```

### Reserved word

`SHOW`, `SHARDING`, `READWRITE_SPLITTING`, `HINT`, `STATUS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
