+++
title = "DROP STREAMING"
weight = 5

+++

### 描述

`DROP STREAMING` 语法用于删除指定 CDC Streaming 作业。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
DropStreaming ::=
  'DROP' 'STREAMING' jobId

jobId ::=
  integer | identifier | string
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `jobId` 需要通过 [SHOW STREAMING LIST](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/streaming/show-streaming-list/) 语法查询获得。

### 示例

- 删除 CDC Streaming 作业

```sql
DROP STREAMING j0302p0000702a83116fcee83f70419ca5e2993791;
```

```sql
sharding_db=> DROP STREAMING j0302p0000702a83116fcee83f70419ca5e2993791;
SUCCESS
```

### 保留字

`DROP`、`STREAMING`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [SHOW STREAMING LIST](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/streaming/show-streaming-list/)
