+++
title = "DROP SHARDING KEY GENERATE STRATEGY"
weight = 9
+++

## 描述

`DROP SHARDING KEY GENERATE STRATEGY` 语法用于删除当前所选逻辑库中指定的分片主键生成策略。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
DropShardingKeyGenerateStrategy ::=
  'DROP' 'SHARDING' 'KEY' 'GENERATE' 'STRATEGY' ifExists? keyGenerateStrategyName (',' keyGenerateStrategyName)*

ifExists ::=
  'IF' 'EXISTS'

keyGenerateStrategyName ::=
  identifier
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- 该语法仅删除分片主键生成策略定义，不会级联删除被引用的分片主键生成器；
- `ifExists` 子句用于避免出现 `Sharding key generate strategy not exists` 错误。

### 示例

- 删除分片主键生成策略

```sql
DROP SHARDING KEY GENERATE STRATEGY order_id_strategy;
```

- 一次删除多个分片主键生成策略

```sql
DROP SHARDING KEY GENERATE STRATEGY order_id_strategy, order_sequence_strategy;
```

- 使用 `ifExists` 子句删除分片主键生成策略

```sql
DROP SHARDING KEY GENERATE STRATEGY IF EXISTS order_id_strategy;
```

### 保留字

`DROP`、`SHARDING`、`KEY`、`GENERATE`、`STRATEGY`、`IF`、`EXISTS`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [SHOW SHARDING KEY GENERATE STRATEGY](/cn/user-manual/shardingsphere-proxy/distsql/syntax/rql/rule-query/sharding/show-sharding-key-generate-strategy/)
