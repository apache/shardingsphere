+++
title = "CREATE READWRITE_SPLITTING RULE"
weight = 2
+++

## 描述

`CREATE READWRITE_SPLITTING RULE` 语法用于创建读写分离规则

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
CreateReadwriteSplittingRule ::=
  'CREATE' 'READWRITE_SPLITTING' 'RULE' ifNotExists? readwriteSplittingDefinition (',' readwriteSplittingDefinition)*

ifNotExists ::=
  'IF' 'NOT' 'EXISTS'

readwriteSplittingDefinition ::=
  ruleName '(' (staticReadwriteSplittingDefinition | dynamicReadwriteSplittingDefinition) (',' loadBalancerDefinition)? ')'

staticReadwriteSplittingDefinition ::=
    'WRITE_STORAGE_UNIT' '=' writeStorageUnitName ',' 'READ_STORAGE_UNITS' '(' storageUnitName (',' storageUnitName)* ')'

dynamicReadwriteSplittingDefinition ::=
    'AUTO_AWARE_RESOURCE' '=' resourceName (',' 'WRITE_DATA_SOURCE_QUERY_ENABLED' '=' ('TRUE' | 'FALSE'))?

loadBalancerDefinition ::=
    'TYPE' '(' 'NAME' '=' loadBalancerType (',' propertiesDefinition)? ')'

ruleName ::=
  identifier

writeStorageUnitName ::=
  identifier

storageUnitName ::=
  identifier

resourceName ::=
  identifier
    
loadBalancerType ::=
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

- 支持创建静态读写分离规则和动态读写分离规则；
- 动态读写分离规则依赖于数据库发现规则；
- `loadBalancerType` 指定负载均衡算法类型，请参考负载均衡算法；
- 重复的 `ruleName` 将无法被创建；
- `ifNotExists` 子句用于避免出现 `Duplicate readwrite_splitting rule` 错误。

### 示例

#### 创建静态读写分离规则

```sql
CREATE READWRITE_SPLITTING RULE ms_group_0 (
    WRITE_STORAGE_UNIT=write_ds,
    READ_STORAGE_UNITS(read_ds_0,read_ds_1),
    TYPE(NAME="random")
);
```

#### 创建动态读写分离规则

```sql
CREATE READWRITE_SPLITTING RULE ms_group_1 (
    AUTO_AWARE_RESOURCE=group_0,
    WRITE_DATA_SOURCE_QUERY_ENABLED=false,
    TYPE(NAME="random")
);
```

#### 使用 `ifNotExists` 子句创建读写分离规则

- 静态读写分离规则

```sql
CREATE READWRITE_SPLITTING RULE IF NOT EXISTS ms_group_0 (
    WRITE_STORAGE_UNIT=write_ds,
    READ_STORAGE_UNITS(read_ds_0,read_ds_1),
    TYPE(NAME="random")
);
```

- 动态读写分离规则

```sql
CREATE READWRITE_SPLITTING RULE IF NOT EXISTS ms_group_1 (
    AUTO_AWARE_RESOURCE=group_0,
    WRITE_DATA_SOURCE_QUERY_ENABLED=false,
    TYPE(NAME="random")
);
```

### 保留字

`CREATE`、`READWRITE_SPLITTING`、`RULE`、`WRITE_RESOURCE`、`READ_RESOURCES`、`AUTO_AWARE_RESOURCE`、`WRITE_DATA_SOURCE_QUERY_ENABLED`
、`TYPE`、`NAME`、`PROPERTIES`、`TRUE`、`FALSE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
