+++
title = "ALTER STREAMING RULE"
weight = 2

+++

### 描述

`ALTER STREAMING RULE` 语法用于修改 CDC Streaming 规则。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
AlterStreamingRule ::=
  'ALTER' 'STREAMING' 'RULE' '(' readConfiguration? (','? writeConfiguration)? (','? streamChannel)? ')'

readConfiguration ::=
  'READ' '(' workerThread? (','? batchSize)? (','? shardingSize)? (','? rateLimiter)? ')'

writeConfiguration ::=
  'WRITE' '(' workerThread? (','? batchSize)? (','? rateLimiter)? ')'

streamChannel ::=
  'STREAM_CHANNEL' '(' algorithmDefinition ')'

workerThread ::=
  'WORKER_THREAD' '=' int

batchSize ::=
  'BATCH_SIZE' '=' int

shardingSize ::=
  'SHARDING_SIZE' '=' int

rateLimiter ::=
  'RATE_LIMITER' '(' algorithmDefinition ')'

algorithmName ::=
  string

algorithmDefinition ::=
  'TYPE' '(' 'NAME' '=' algorithmName (',' propertiesDefinition)? ')'

propertiesDefinition ::=
  'PROPERTIES' '(' (key '=' value (',' key '=' value)*)? ')'

key ::=
  string

value ::=
  literal
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- CDC Streaming 规则具有默认值，无需创建。

### 示例

```sql
ALTER STREAMING RULE (
  READ(WORKER_THREAD=20, BATCH_SIZE=1000, SHARDING_SIZE=10000000, RATE_LIMITER (TYPE(NAME='QPS', PROPERTIES('qps'='500')))),
  WRITE(WORKER_THREAD=20, BATCH_SIZE=1000, RATE_LIMITER (TYPE(NAME='TPS', PROPERTIES('tps'='2000')))),
  STREAM_CHANNEL (TYPE(NAME='MEMORY', PROPERTIES('block-queue-size'='2000')))
);
```

### 保留字

`ALTER`、`STREAMING`、`RULE`、`READ`、`WRITE`、`WORKER_THREAD`、`BATCH_SIZE`、`SHARDING_SIZE`、`RATE_LIMITER`、`STREAM_CHANNEL`、`TYPE`、`NAME`、`PROPERTIES`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
