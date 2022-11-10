+++
title = "SHOW MIGRATION RULE"
weight = 2
+++

### Description

The `SHOW MIGRATION RULE` syntax is used to query migration rule.

### Syntax

```sql
ShowMigrationRule ::=
  'SHOW' 'MIGRATION' 'RULE'
```

### Return Value Description

| Colume        | Description                |
|---------------|----------------------------|
|read           |Data reading configuration  |
|write          |Data writting configuration |
|stream_channel |Data channel                |

### Example

- Query migration rule

```sql
SHOW MIGRATION RULE;
```

```sql
mysql> SHOW MIGRATION RULE;
+--------------------------------------------------------------+--------------------------------------+------------------------------------------------------+
| read                                                         | write                                | stream_channel                                       |
+--------------------------------------------------------------+--------------------------------------+------------------------------------------------------+
| {"workerThread":40,"batchSize":1000,"shardingSize":10000000} | {"workerThread":40,"batchSize":1000} | {"type":"MEMORY","props":{"block-queue-size":10000}} |
+--------------------------------------------------------------+--------------------------------------+------------------------------------------------------+
1 row in set (0.01 sec)
```

### Reserved word

`SHOW`, `MIGRATION`, `RULE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
