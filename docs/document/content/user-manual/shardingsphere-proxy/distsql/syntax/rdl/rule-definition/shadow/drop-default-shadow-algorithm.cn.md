+++
title = "DROP DEFAULT SHADOW ALGORITHM"
weight = 6
+++

## 描述

`DROP DEFAULT SHADOW ALGORITHM` 语法用于从当前逻辑库中删除默认影子库压测算法。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
DropDefaultShadowAlgorithm ::=
  'DROP' 'DEFAULT' 'SHADOW' 'ALGORITHM' ifExists?

ifExists ::=
  'IF' 'EXISTS'

```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `ifExists` 子句用于避免 `Default shadow algorithm not exists` 错误。

### 示例

- 删除默认影子库压测算法

```sql
DROP DEFAULT SHADOW ALGORITHM;
```

- 使用 `ifExists` 子句删除默认影子库压测算法

```sql
DROP DEFAULT SHADOW ALGORITHM IF EXISTS;
```

### 保留字

`DROP`、`DEFAULT`、`SHADOW`、`ALGORITHM`、`IF`、`EXISTS`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
