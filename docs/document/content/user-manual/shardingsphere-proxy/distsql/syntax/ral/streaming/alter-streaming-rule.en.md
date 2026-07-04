+++
title = "ALTER STREAMING RULE"
weight = 2
+++

### Description

The `ALTER STREAMING RULE` syntax is used to alter the CDC streaming rule.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
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
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- The CDC streaming rule has default values and does not need to be created.

### Example

```sql
ALTER STREAMING RULE (
  READ(WORKER_THREAD=20, BATCH_SIZE=1000, SHARDING_SIZE=10000000, RATE_LIMITER (TYPE(NAME='QPS', PROPERTIES('qps'='500')))),
  WRITE(WORKER_THREAD=20, BATCH_SIZE=1000, RATE_LIMITER (TYPE(NAME='TPS', PROPERTIES('tps'='2000')))),
  STREAM_CHANNEL (TYPE(NAME='MEMORY', PROPERTIES('block-queue-size'='2000')))
);
```

### Reserved word

`ALTER`, `STREAMING`, `RULE`, `READ`, `WRITE`, `WORKER_THREAD`, `BATCH_SIZE`, `SHARDING_SIZE`, `RATE_LIMITER`, `STREAM_CHANNEL`, `TYPE`, `NAME`, `PROPERTIES`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
