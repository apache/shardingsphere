+++
title = "CREATE SHARDING KEY GENERATOR"
weight = 6
+++

## 描述

`CREATE SHARDING KEY GENERATOR` 语法用于为当前所选的逻辑库添加分布式主键生成器

### 语法定义

```SQL
CreateShardingAlgorithm ::=
  'CREATE' 'SHARDING' 'KEY' 'GENERATOR' keyGeneratorName '(' algorithmDefinition ')'

algorithmDefinition ::=
  'TYPE' '(' 'NAME' '=' algorithmType ( ',' 'PROPERTIES'  '(' propertyDefinition  ')' )?')'  

propertyDefinition ::=
  ( key  '=' value ) ( ',' key  '=' value )*

keyGeneratorName ::=
  identifier
  
algorithmType ::=
  identifier
```

### 补充说明

- `algorithmType` 为分布式主键生成算法类型，详细的分布式主键生成算法类型信息请参考[分布式序列算法类型](cn/user-manual/shardingsphere-jdbc/builtin-algorithm/keygen/)

### 示例

#### 创建分布式主键生成器

```SQL
CREATE SHARDING KEY GENERATOR snowflake_key_generator (
    TYPE(NAME=SNOWFLAKE, PROPERTIES("max-vibration-offset"=3))
);
```

### 保留字

    CREATE、SHARDING、KEY、GENERATOR、TYPE、NAME、PROPERTIES

### 相关链接
- [保留字](/cn/reference/distsql/syntax/reserved-word/)

