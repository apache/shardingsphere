+++
title = "ALTER READWRITE_SPLITTING RULE"
weight = 3
+++

## 描述

`ALTER READWRITE_SPLITTING RULE` 语法用于修改读写分离规则

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
AlterReadwriteSplittingRule ::=
  'ALTER' 'READWRITE_SPLITTING' 'RULE' readwriteSplittingDefinition (',' readwriteSplittingDefinition)*

readwriteSplittingDefinition ::=
  ruleName '(' (staticReadwriteSplittingDefinition | dynamicReadwriteSplittingDefinition) (',' loadBalancerDefinition)? ')'

staticReadwriteSplittingDefinition ::=
    'WRITE_STORAGE_UNIT' '=' writeStorageUnitName ',' 'READ_STORAGE_UNITS' '(' storageUnitName (',' storageUnitName)* ')'

dynamicReadwriteSplittingDefinition ::=
    'AUTO_AWARE_RESOURCE' '=' resourceName

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

- 动态读写分离规则依赖于数据库发现规则；
- `loadBalancerType` 指定负载均衡算法类型，请参考[负载均衡算法]((/cn/user-manual/common-config/builtin-algorithm/load-balance/))；

### 示例

#### 修改静态读写分离规则

```sql
ALTER READWRITE_SPLITTING RULE ms_group_0 (
    WRITE_STORAGE_UNIT=write_ds,
    READ_STORAGE_UNITS(read_ds_0,read_ds_1),
    TYPE(NAME="random")
);
```

#### 修改动态读写分离规则

```sql
ALTER READWRITE_SPLITTING RULE ms_group_1 (
    AUTO_AWARE_RESOURCE=group_0
    TYPE(NAME="random")
);
```

### 保留字

`ALTER`、`READWRITE_SPLITTING`、`RULE`、`WRITE_STORAGE_UNIT`、`READ_STORAGE_UNITS`、`AUTO_AWARE_RESOURCE`
、`TYPE`、`NAME`、`PROPERTIES`、`TRUE`、`FALSE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [负载均衡算法](/cn/user-manual/common-config/builtin-algorithm/load-balance/)
