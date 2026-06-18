+++
title = "ALTER SHARDING KEY GENERATOR"
weight = 9
+++

## 描述

`ALTER SHARDING KEY GENERATOR` 语法用于修改当前所选逻辑库中的独立分布式主键生成器。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
AlterShardingKeyGenerator ::=
  'ALTER' 'SHARDING' 'KEY' 'GENERATOR' keyGeneratorName '(' algorithmDefinition ')'

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

- `keyGeneratorName` 为需要修改的分布式主键生成器名称；
- `algorithmDefinition` 用于定义新的主键生成器算法及其参数；
- 若指定的分布式主键生成器不存在，将提示缺失对应规则；
- `algorithmType` 为主键生成算法类型，详细信息请参考[分布式主键](/cn/user-manual/common-config/builtin-algorithm/keygen/)。

### 示例

- 修改分布式主键生成器

```sql
ALTER SHARDING KEY GENERATOR snowflake_generator (
TYPE(NAME="UUID")
);
```

- 修改分布式主键生成器并指定属性

```sql
ALTER SHARDING KEY GENERATOR snowflake_generator (
TYPE(NAME="SNOWFLAKE",PROPERTIES("worker-id"=2))
);
```

### 保留字

`ALTER`、`SHARDING`、`KEY`、`GENERATOR`、`TYPE`、`NAME`、`PROPERTIES`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [CREATE SHARDING KEY GENERATOR](/cn/user-manual/shardingsphere-proxy/distsql/syntax/rdl/rule-definition/sharding/create-sharding-key-generator/)
- [SHOW SHARDING KEY GENERATOR](/cn/user-manual/shardingsphere-proxy/distsql/syntax/rql/rule-query/sharding/show-sharding-key-generator/)
