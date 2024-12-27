+++
title = "UNLOCK CLUSTER"
weight = 17
+++

### 描述

`UNLOCK CLUSTER` 语法用于解除通过 `LOCK CLUSTER WITH` 语句施加在 `CLUSTER` 上的锁。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}

```sql
UnlockCluster ::=
  'UNLOCK' 'CLUSTER' ('TIMEOUT' timeoutMillis)?

timeoutmillis ::=
  long
```

{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- 当 `CLUSTER` 不处于被锁状态时，无法解除锁，否则会抛出异常。
- `timeoutMillis` 表明尝试解锁的超时时间，其单位为毫秒，未指定时，默认为 3000 毫秒。

### 示例

- 解锁 `CLUSTER` ，不设置超时时间。

```sql
UNLOCK CLUSTER;
```

- 解锁 `CLUSTER` ，并设置超时时间为 2000 毫秒。

```sql
UNLOCK CLUSTER TIMEOUT 2000;
```

### 保留字

`UNLOCK`、`CLUSTER`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
