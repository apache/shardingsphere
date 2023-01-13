+++
title = "CLEAR HINT"
weight = 6
+++

### 描述

`CLEAR HINT` 语法用于针对当前连接，清除 hint 设置

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ClearHint ::=
  'CLEAR' ('SHARDING' | 'READWRITE_SPLITTING')? 'HINT' 
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- 未指定 `SHARDING` / `READWRITE_SPLITTING` 时，默认清除所有 hint 设置

### 示例

- 清除 `SHARDING` 的 hint 设置

```sql
CLEAR SHARDING HINT;
```

- 清除 `READWRITE_SPLITTING` 的 hint 设置

```sql
CLEAR READWRITE_SPLITTING HINT;
```

- 清除所有 hint 设置

```sql
CLEAR HINT;
```

### 保留字

`CLEAR`、`SHARDING`、`READWRITE_SPLITTING`、`HINT`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)