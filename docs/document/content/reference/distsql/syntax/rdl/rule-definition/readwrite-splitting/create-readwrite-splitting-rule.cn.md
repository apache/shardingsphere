+++
title = "CREATE READWRITE_SPLITTING RULE"
weight = 2
+++

## 描述

`CREATE DEFAULT SINGLE TABLE RULE` 语法用于创建读写分离规则

### 语法定义

```sql
CreateReadwriteSplittingRule ::=
  'CREATE' 'READWRITE_SPLITTING' 'RULE' readwriteSplittingDefinition ( ',' readwriteSplittingDefinition )*

readwriteSplittingDefinition ::=
  ruleName '(' ( staticReadwriteSplittingDefinition | dynamicReadwriteSplittingDefinition ) ( ',' loadBalancerDefinition )? ')'

staticReadwriteSplittingDefinition ::=
    'WRITE_RESOURCE' '=' writeResourceName ',' 'READ_RESOURCES' '(' ruleName (',' ruleName)* ')'

dynamicReadwriteSplittingDefinition ::=
    'AUTO_AWARE_RESOURCE' '=' resourceName ( ',' 'WRITE_DATA_SOURCE_QUERY_ENABLED' '=' ('TRUE' | 'FALSE') )?

loadBalancerDefinition ::=
    'TYPE' '(' 'NAME' '=' loadBalancerType ( ',' 'PROPERTIES' '(' 'key' '=' 'value' ( ',' 'key' '=' 'value' )* ')' )? ')'

ruleName ::=
  identifier

writeResourceName ::=
  identifier

resourceName ::=
  identifier
    
loadBalancerType ::=
  string
```

### 补充说明

- 支持创建静态读写分离规则和动态读写分离规则；
- 动态读写分离规则依赖于数据库发现规则；
- `loadBalancerType` 指定负载均衡算法类型，请参考负载均衡算法；
- 重复的 `ruleName` 将无法被创建。

### 示例

#### 创建静态读写分离规则

```sql
CREATE READWRITE_SPLITTING RULE ms_group_0 (
    WRITE_RESOURCE=write_ds,
    READ_RESOURCES(read_ds_0,read_ds_1),
    TYPE(NAME="random")
);
```

#### 创建动态读写分离规则

```sql
CREATE READWRITE_SPLITTING RULE ms_group_1 (
    AUTO_AWARE_RESOURCE=group_0,
    WRITE_DATA_SOURCE_QUERY_ENABLED=false,
    TYPE(NAME="random",PROPERTIES("read_weight"="2:1"))
);
```

### 保留字

`CREATE`、`READWRITE_SPLITTING`、`RULE`、`WRITE_RESOURCE`、`READ_RESOURCES`、`AUTO_AWARE_RESOURCE`、`WRITE_DATA_SOURCE_QUERY_ENABLED`
、`TYPE`、`NAME`、`PROPERTIES`、`TRUE`、`FALSE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)
