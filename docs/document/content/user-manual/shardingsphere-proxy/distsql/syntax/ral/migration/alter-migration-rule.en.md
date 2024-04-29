+++
title = "ALTER MIGRATION RULE"
weight = 2
+++

### Description

The `ALTER MIGRATION RULE` syntax is used to alter migration rule.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
AlterMigrationRule ::=
  'ALTER' 'MIGRATION' 'RULE' ('(' (readConfiguration ',')?  (writeConfiguration  ',')? (dataChannel)? ')')?

readConfiguration ::=
  'READ' '(' ('WORKER_THREAD' '=' workerThreadPoolSize ',')? ('BATCH_SIZE' '=' batchSize ',')? ('SHARDING_SIZE' '=' shardingSize ',')? (rateLimiter)? ')'

writeConfiguration ::=
  'WRITE' '(' ('WORKER_THREAD' '=' workerThreadPoolSize ',')? ('BATCH_SIZE' '=' batchSize ',')? ('SHARDING_SIZE' '=' shardingSize ',')? (rateLimiter)? ')'

dataChannel ::=
  'STREAM_CHANNEL' '(' 'TYPE' '(' 'NAME' '=' algorithmName ',' propertiesDefinition ')' ')'

workerThreadPoolSize ::=
  int

batchSize ::=
  int

shardingSize ::=
  int

rateLimiter ::=
  'RATE_LIMITER' '(' 'TYPE' '(' 'NAME' '=' algorithmName ',' propertiesDefinition ')' ')'

algorithmName ::=
  string

propertiesDefinition ::=
  'PROPERTIES' '(' key '=' value (',' key '=' value)* ')'

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

### Example

```sql
ALTER MIGRATION RULE (
  READ( WORKER_THREAD=20, BATCH_SIZE=1000, SHARDING_SIZE=10000000, RATE_LIMITER (TYPE(NAME='QPS',PROPERTIES('qps'='500')))), 
  WRITE( WORKER_THREAD=20, BATCH_SIZE=1000, RATE_LIMITER (TYPE(NAME='TPS',PROPERTIES('tps'='2000')))), 
  STREAM_CHANNEL ( TYPE(NAME='MEMORY',PROPERTIES('block-queue-size'='2000')))
  );
```

### Reserved word

`ALTER`, `MIGRATION`, `RULE`, `READ`, `WRITE`, `WORKER_THREAD`, `BATCH_SIZE`, `SHARDING_SIZE`, `STREAM_CHANNEL`, `TYPE`, `NAME`, `PROPERTIES`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
