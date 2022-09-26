+++
title = "CREATE SHARDING AUDITOR"
weight = 8
+++

## 描述

`CREATE SHARDING AUDITOR` 语法用于为当前所选的逻辑库添加分片审计生成器

### 语法定义

```sql
CreateShardingAlgorithm ::=
  'CREATE' 'SHARDING' 'AUDITOR' auditorName '(' algorithmDefinition ')'

algorithmDefinition ::=
  'TYPE' '(' 'NAME' '=' algorithmType ( ',' 'PROPERTIES'  '(' propertyDefinition  ')' )?')'  

propertyDefinition ::=
  ( key  '=' value ) ( ',' key  '=' value )*

auditorName ::=
  identifier
  
algorithmType ::=
  string
```

### 补充说明

- `algorithmType`
  为分片审计算法类型，详细的分片审计生成算法类型信息请参考[分片审计算法类型](/cn/user-manual/common-config/builtin-algorithm/audit/)。

### 示例

#### 创建分片审计器

```sql
CREATE SHARDING AUDITOR sharding_key_required_auditor (
    TYPE(NAME="DML_SHARDING_CONDITIONS", PROPERTIES("a"="b"))
);
```

### 保留字

`CREATE`、`SHARDING`、`AUDITOR`、`TYPE`、`NAME`、`PROPERTIES`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)
