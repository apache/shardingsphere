+++
title = "ALTER MIGRATION RULE"
weight = 2

+++

### 描述

`ALTER MIGRATION RULE` 语法用于修改数据迁移规则。
### 语法

{{< tabs >}}
{{% tab name="语法" %}}
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
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `ALTER MIGRATION RULE` 可以只修改数据迁移规则中一项配置并不影响其他配置

### 示例

- 修改数据迁移规则

```sql
ALTER MIGRATION RULE (
  READ( WORKER_THREAD=20, BATCH_SIZE=1000, SHARDING_SIZE=10000000, RATE_LIMITER (TYPE(NAME='QPS',PROPERTIES('qps'='500')))), 
  WRITE( WORKER_THREAD=20, BATCH_SIZE=1000, RATE_LIMITER (TYPE(NAME='TPS',PROPERTIES('tps'='2000')))), 
  STREAM_CHANNEL ( TYPE(NAME='MEMORY',PROPERTIES('block-queue-size'='2000')))
  );
```

- 仅修改数据迁移规则中的数据读取配置

```sql
ALTER MIGRATION RULE (
  READ(WORKER_THREAD=20, BATCH_SIZE=1000, SHARDING_SIZE=10000000, RATE_LIMITER (TYPE(NAME='QPS',PROPERTIES('qps'='500'))))
  );
```

- 仅修改数据迁移规则中的数据写入配置

```sql
ALTER MIGRATION RULE (
  WRITE(WORKER_THREAD=20, BATCH_SIZE=1000, SHARDING_SIZE=10000000, RATE_LIMITER (TYPE(NAME='QPS',PROPERTIES('qps'='500'))))
  );
```

- 仅修改数据迁移规则中的数据通道配置

```sql
ALTER MIGRATION RULE (
  STREAM_CHANNEL ( TYPE( NAME='MEMORY', PROPERTIES('block-queue-size'='2000')))
  );
```

### 保留字

`ALTER`、`MIGRATION`、`RULE`、`READ`、`WRITE`、`WORKER_THREAD`、`BATCH_SIZE`、`SHARDING_SIZE`、`STREAM_CHANNEL`、`TYPE`、`NAME`、`PROPERTIES`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)