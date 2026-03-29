+++
title = "CREATE SHARDING KEY GENERATOR"
weight = 8
+++

## 描述

`CREATE SHARDING KEY GENERATOR` 语法用于为当前所选逻辑库创建独立的分布式主键生成器。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
CreateShardingKeyGenerator ::=
  'CREATE' 'SHARDING' 'KEY' 'GENERATOR' ifNotExists? keyGeneratorName '(' algorithmDefinition ')'

ifNotExists ::=
  'IF' 'NOT' 'EXISTS'

algorithmDefinition ::=
  'TYPE' '(' 'NAME' '=' algorithmType (',' propertiesDefinition)? ')'

propertiesDefinition ::=
  'PROPERTIES' '(' key '=' value (',' key '=' value)* ')'

key ::=
  string

value ::=
  literal

keyGeneratorName ::=
  identifier

algorithmType ::=
  string
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `keyGeneratorName` 为独立分布式主键生成器名称；
- `algorithmDefinition` 用于定义主键生成器算法及其参数；
- `algorithmType` 为主键生成算法类型，详细信息请参考[分布式主键](/cn/user-manual/common-config/builtin-algorithm/keygen/)；
- `ifNotExists` 子句用于避免出现 `Duplicate sharding key generator` 错误。

### 示例

- 创建分布式主键生成器

```sql
CREATE SHARDING KEY GENERATOR snowflake_generator (
TYPE(NAME="SNOWFLAKE",PROPERTIES("worker-id"=1))
);
```

- 使用 `ifNotExists` 子句创建分布式主键生成器

```sql
CREATE SHARDING KEY GENERATOR IF NOT EXISTS snowflake_generator (
TYPE(NAME="SNOWFLAKE")
);
```

### 保留字

`CREATE`、`SHARDING`、`KEY`、`GENERATOR`、`IF`、`NOT`、`EXISTS`、`TYPE`、`NAME`、`PROPERTIES`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [ALTER SHARDING KEY GENERATOR](/cn/user-manual/shardingsphere-proxy/distsql/syntax/rdl/rule-definition/sharding/alter-sharding-key-generator/)
- [SHOW SHARDING KEY GENERATOR](/cn/user-manual/shardingsphere-proxy/distsql/syntax/rql/rule-query/sharding/show-sharding-key-generator/)
