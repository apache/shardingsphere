+++
title = "LOCK CLUSTER WITH"
weight = 16
+++

### 描述

`LOCK CLUSTER WITH` 语法用于向 `CLUSTER` 施加特定算法的锁。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}

```sql
LockClusterWith ::=
  'LOCK' 'CLUSTER' 'WITH' lockStrategy ('TIMEOUT' timeoutMillis)?

timeoutmillis ::=
  long
```

{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- 当 `CLUSTER` 已经处于被锁状态时，无法重复加锁，否则会抛出异常。
- `lockStrategy` 当前支持两种锁策略，分别是排他锁 `WRITE` 与读写锁 `READ_WRITE`。
- `timeoutMillis` 用于表明尝试加锁的超时时间，其单位为毫秒，未指定时，默认为 3000 毫秒。

### 示例

- 采用排他锁锁定 `CLUSTER` ，不设置超时时间

```sql
LOCK CLUSTER WITH WRITE;
```

- 采用读写锁锁定 `CLUSTER` ，并设置超时时间为 2000 毫秒

```sql
LOCK CLUSTER WITH READ_WRITE TIMEOUT 2000;
```

### 保留字

`LOCK`、`CLUSTER`、`WITH`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
