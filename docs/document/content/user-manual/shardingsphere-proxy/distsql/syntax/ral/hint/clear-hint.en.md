+++
title = "CLEAR HINT"
weight = 6
+++

### Description

The `CLEAR HINT` syntax is used to clear hint settings for current connection.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ClearHint ::=
  'CLEAR' ('SHARDING' | 'READWRITE_SPLITTING')? 'HINT' 
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- When `SHARDING`/`READWRITE_SPLITTING` is not specified, the default is clear all hint settings.

### Example

- Clear hint settings of sharding

```sql
CLEAR SHARDING HINT;
```

- Clear hint settings of readwrite splitting

```sql
CLEAR READWRITE_SPLITTING HINT;
```

- Clear all hint settings

```sql
CLEAR HINT;
```

### Reserved word

`CLEAR`, `SHARDING`, `READWRITE_SPLITTING`, `HINT`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
