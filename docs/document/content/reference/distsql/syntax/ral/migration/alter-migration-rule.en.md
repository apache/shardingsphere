+++
title = "ALTER MIGRATION RULE"
weight = 3
+++

### Description

The `ALTER MIGRATION RULE` syntax is used to alter migration rule.

### Syntax

```sql
AlterMigrationRule ::=
  'ALTER' 'MIGRATION' 'RULE' ( '(' (readConfiguration ',')?  (writeConfiguration  ',')? (dataChannel)? ')' )?

readConfiguration ::=
  'READ' '(' ('WORKER_THREAD' '=' workerThreadPoolSize ',')? ('BATCH_SIZE' '=' batchSize ',')? ('SHARDING_SIZE' '=' shardingSize ',')? (rateLimiter)? ')'

writeConfiguration ::=
  'WRITE' '(' ('WORKER_THREAD' '=' workerThreadPoolSize ',')? ('BATCH_SIZE' '=' batchSize ',')? ('SHARDING_SIZE' '=' shardingSize ',')? (rateLimiter)? ')'

dataChannel ::=
  'STREAM_CHANNEL' '(' 'TYPE' '(' 'NAME' '=' algorithmName ',' 'PROPERTIES' '(' propertyDefinition ')'

workerThreadPoolSize ::=
  int

batchSize ::=
  int

shardingSize ::=
  int

rateLimiter ::=
  'RATE_LIMITER' '(' 'TYPE' '(' 'NAME' '=' algorithmName ',' 'PROPERTIES' '(' propertyDefinition ')'

algorithmName ::=
  string

propertyDefinition ::=
  ( key  '=' value ) ( ',' key  '=' value )* 

key ::=
  string

value ::=
  string
```

### Supplement

- `ALTER MIGRATION RULE` able to modify only one configuration in the data migration rule without affecting other configurations.

### Example

- Alter migration rule

```sql
ALTER MIGRATION RULE (
  READ( WORKER_THREAD=40, BATCH_SIZE=1000, SHARDING_SIZE=10000000, RATE_LIMITER (TYPE(NAME='QPS',PROPERTIES('qps'='500')))), 
  WRITE( WORKER_THREAD=40, BATCH_SIZE=1000, RATE_LIMITER (TYPE(NAME='TPS',PROPERTIES('tps'='2000')))), 
  STREAM_CHANNEL ( TYPE(NAME='MEMORY',PROPERTIES('block-queue-size'='10000')))
  );
```

- Alter read configuration only in migration rule

```sql
ALTER MIGRATION RULE (
  READ(WORKER_THREAD=40, BATCH_SIZE=1000, SHARDING_SIZE=10000000, RATE_LIMITER (TYPE(NAME='QPS',PROPERTIES('qps'='500'))))
  );
```

- Alter write configuration only in migration rule

```sql
ALTER MIGRATION RULE (
  WRITE(WORKER_THREAD=40, BATCH_SIZE=1000, SHARDING_SIZE=10000000, RATE_LIMITER (TYPE(NAME='QPS',PROPERTIES('qps'='500'))))
  );
```
- Alter stream channel configuration in migration rule

```sql
ALTER MIGRATION RULE (
  STREAM_CHANNEL ( TYPE( NAME='MEMORY', PROPERTIES('block-queue-size'='10000')))
  );
```

### Reserved word

`ALTER`, `MIGRATION`, `RULE`, `READ`, `WRITE`, `WORKER_THREAD`, `BATCH_SIZE`, `SHARDING_SIZE`, `STREAM_CHANNEL`, `TYPE`, `NAME`, `PROPERTIES`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
