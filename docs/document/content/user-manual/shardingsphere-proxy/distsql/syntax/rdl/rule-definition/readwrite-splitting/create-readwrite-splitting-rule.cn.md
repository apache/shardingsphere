+++
title = "CREATE READWRITE_SPLITTING RULE"
weight = 1
+++

## 描述
。
`CREATE READWRITE_SPLITTING RULE` 语法用于创建读写分离规则。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
CreateReadwriteSplittingRule ::=
  'CREATE' 'READWRITE_SPLITTING' 'RULE' ifNotExists? readwriteSplittingDefinition (',' readwriteSplittingDefinition)*

ifNotExists ::=
  'IF' 'NOT' 'EXISTS'

readwriteSplittingDefinition ::=
  ruleName '(' dataSourceDefinition (',' transactionalReadQueryStrategyDefinition)? (',' loadBalancerDefinition)? ')'

dataSourceDefinition ::=
    'WRITE_STORAGE_UNIT' '=' writeStorageUnitName ',' 'READ_STORAGE_UNITS' '(' storageUnitName (',' storageUnitName)* ')' 

transactionalReadQueryStrategyDefinition ::=
    'TRANSACTIONAL_READ_QUERY_STRATEGY' '=' transactionalReadQueryStrategyType

loadBalancerDefinition ::=
    'TYPE' '(' 'NAME' '=' loadBalancerType (',' propertiesDefinition)? ')'

ruleName ::=
  identifier

writeStorageUnitName ::=
  identifier

storageUnitName ::=
  identifier

transactionalReadQueryStrategyType ::=
  string

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

- `transactionalReadQueryStrategyType` 指定事务内读请求路由策略，请参考[YAML 配置](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/readwrite-splitting/)；
- `loadBalancerType` 指定负载均衡算法类型，请参考[负载均衡算法](/cn/user-manual/common-config/builtin-algorithm/load-balance/)；
- 重复的 `ruleName` 将无法被创建；
- `ifNotExists` 子句用于避免出现 `Duplicate readwrite_splitting rule` 错误。

### 示例

#### 创建读写分离规则

```sql
CREATE READWRITE_SPLITTING RULE ms_group_0 (
    WRITE_STORAGE_UNIT=write_ds,
    READ_STORAGE_UNITS(read_ds_0,read_ds_1),
    TYPE(NAME="random")
);
```

#### 使用 `ifNotExists` 子句创建读写分离规则

- 读写分离规则

```sql
CREATE READWRITE_SPLITTING RULE IF NOT EXISTS ms_group_0 (
    WRITE_STORAGE_UNIT=write_ds,
    READ_STORAGE_UNITS(read_ds_0,read_ds_1),
    TYPE(NAME="random")
);
```

### 保留字

`CREATE`、`READWRITE_SPLITTING`、`RULE`、`WRITE_STORAGE_UNIT`、`READ_STORAGE_UNITS`
、`TYPE`、`NAME`、`PROPERTIES`、`TRUE`、`FALSE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [负载均衡算法](/cn/user-manual/common-config/builtin-algorithm/load-balance/)
