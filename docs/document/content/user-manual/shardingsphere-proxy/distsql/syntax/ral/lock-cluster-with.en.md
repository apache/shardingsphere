+++
title = "LOCK CLUSTER WITH"
weight = 16
+++

### Description

The `LOCK CLUSTER WITH` syntax is utilized to apply a lock with a specific algorithm to the `CLUSTER`.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}

```sql
LockClusterWith ::=
  'LOCK' 'CLUSTER' 'WITH' lockStrategy ('TIMEOUT' timeoutMillis)?

timeoutmillis ::=
  long
```

{{% /tab %}}
{{% tab name="Railroad diagram" %}}

<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>{{% /tab %}}{{< /tabs >}}

### Supplement

- When the `CLUSTER` is already locked, it is impossible to re-lock it, otherwise an exception will be thrown.
- Currently, the `lockStrategy` supports two lock strategies, namely the exclusive lock `WRITE` and the read-write lock `READ_WRITE` .
- The `timeoutMillis` is used to indicate the timeout period for attempting to acquire the lock, with the unit being milliseconds. When not specified, the default value is 3,000 milliseconds.

### Example

- Lock the `CLUSTER` with an exclusive lock without setting the timeout.
- 
```sql
LOCK CLUSTER WITH WRITE;
```

- Lock the CLUSTER with a read-write lock and set the timeout to 2000 milliseconds.
- 
```sql
LOCK CLUSTER WITH READ_WRITE TIMEOUT 2000;
```

### Reserved words

`LOCK`,`CLUSTER`,`WITH`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
