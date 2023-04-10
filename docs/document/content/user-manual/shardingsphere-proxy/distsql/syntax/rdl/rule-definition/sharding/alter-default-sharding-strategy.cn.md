+++
title = "ALTER DEFAULT SHARDING STRATEGY"
weight = 5
+++

## 描述

`ALTER DEFAULT SHARDING STRATEGY` 语法用于修改默认的分片策略。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
AlterDefaultShardingStrategy ::=
  'ALTER' 'DEFAULT' 'SHARDING' ('DATABASE' | 'TABLE') 'STRATEGY' '(' shardingStrategy ')'

shardingStrategy ::=
  'TYPE' '=' strategyType ',' ('SHARDING_COLUMN' '=' columnName | 'SHARDING_COLUMNS' '=' columnNames) ',' 'SHARDING_ALGORITHM' '=' algorithmDefinition

strategyType ::=
  string

algorithmDefinition ::=
  'TYPE' '(' 'NAME' '=' algorithmType ',' propertiesDefinition ')'  

columnNames ::=
  columnName (',' columnName)+

columnName ::=
  identifier

algorithmType ::=
  string

propertiesDefinition ::=
  'PROPERTIES' '(' key '=' value (',' key '=' value)* ')'

key ::=
  string

value ::=
  literal
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- 当使用复合分片算法时，需要通过 `SHARDING_COLUMNS` 指定多个分片键；
- `algorithmType` 为分片算法类型，详细的分片算法类型信息请参考[分片算法](/cn/user-manual/common-config/builtin-algorithm/sharding/)。

### 示例

- 修改默认分表策略

```sql
ALTER DEFAULT SHARDING TABLE STRATEGY (
    TYPE="standard", SHARDING_COLUMN=user_id, SHARDING_ALGORITHM(TYPE(NAME=inline, PROPERTIES("algorithm-expression"="t_order_${user_id % 2}")))
);
```

### 保留字

`ALTER`、`DEFAULT`、`SHARDING`、`DATABASE`、`TABLE`、`STRATEGY`、`TYPE`、`SHARDING_COLUMN`、`SHARDING_COLUMNS`、`SHARDING_ALGORITHM`、`NAME`、`PROPERTIES`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
