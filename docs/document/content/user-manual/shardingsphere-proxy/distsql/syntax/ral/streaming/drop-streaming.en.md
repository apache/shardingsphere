+++
title = "DROP STREAMING"
weight = 5
+++

### Description

The `DROP STREAMING` syntax is used to drop a specified CDC streaming job.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
DropStreaming ::=
  'DROP' 'STREAMING' jobId

jobId ::=
  integer | identifier | string
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `jobId` needs to be obtained through [SHOW STREAMING LIST](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/streaming/show-streaming-list/).

### Example

- Drop CDC streaming job

```sql
DROP STREAMING j0302p0000702a83116fcee83f70419ca5e2993791;
```

```sql
sharding_db=> DROP STREAMING j0302p0000702a83116fcee83f70419ca5e2993791;
SUCCESS
```

### Reserved word

`DROP`, `STREAMING`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [SHOW STREAMING LIST](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/streaming/show-streaming-list/)
