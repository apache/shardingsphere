+++
title = "UNLOCK CLUSTER"
weight = 17
+++

### Description

The `UNLOCK CLUSTER` syntax is used to release the lock applied to the `CLUSTER` by the `LOCK CLUSTER WITH` statement.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}

```sql
UnlockCluster ::=
  'UNLOCK' 'CLUSTER' ('TIMEOUT' timeoutMillis)?

timeoutmillis ::=
  long
```

{{% /tab %}}
{{% tab name="Railroad diagram" %}}

<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>{{% /tab %}}{{< /tabs >}}

### Supplement

- When the `CLUSTER` is not in a locked state, it is impossible to release the lock; otherwise, an exception will be thrown.
- `timeoutMillis` is used to indicate the timeout duration for attempting to unlock, with the unit being milliseconds. When not specified, the default value is 3,000 milliseconds.

### Example

- Unlock the `CLUSTER` without setting a timeout.
- 
```sql
UNLOCK CLUSTER;
```

- Unlock the `CLUSTER` and set the timeout to 2000 milliseconds.

```sql
UNLOCK CLUSTER TIMEOUT 2000;
```

### Reserved words

`UNLOCK`,`CLUSTER`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
