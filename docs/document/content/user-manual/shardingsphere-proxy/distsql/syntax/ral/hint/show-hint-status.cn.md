+++
title = "SHOW HINT STATUS"
weight = 7

+++

### 描述

`SHOW HINT STATUS` 语法用于针对当前连接，查询 hint 设置

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowHintStatus ::=
  'SHOW' ('SHARDING' | 'READWRITE_SPLITTING') 'HINT' 'STATUS'
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 示例

- 查询 `SHARDING` 的 hint 设置

```sql
SHOW SHARDING HINT STATUS;
```

- 查询 `READWRITE_SPLITTING` 的 hint 设置

```sql
SHOW READWRITE_SPLITTING HINT STATUS;
```

### 保留字

`SHOW`、`SHARDING`、`READWRITE_SPLITTING`、`HINT`、`STATUS`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)