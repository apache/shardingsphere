+++
title = "SET READWRITE_SPLITTING HINT SOURCE"
weight = 2
+++

### Description

The `SET READWRITE_SPLITTING HINT SOURCE` syntax is used to set readwrite splitting routing strategy for current connection.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
SetReadwriteSplittingHintSource ::=
  'SET' 'READWRITE_SPLITTING' 'HINT' 'SOURCE' '='('auto' | 'write')
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Example

- Set the read-write splitting routing strategy to auto

```sql
SET READWRITE_SPLITTING HINT SOURCE = auto;
```

- Set the read-write splitting routing strategy to write

```sql
SET READWRITE_SPLITTING HINT SOURCE = write;
```

### Reserved word

`SET`, `READWRITE_SPLITTING`, `HINT`, `SOURCE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
