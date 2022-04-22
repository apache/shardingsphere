+++
title = "CREATE SHARDING BROADCAST TABLE RULE"
weight = 5
+++

## 描述

`CREATE SHARDING BROADCAST TABLE RULE` 语法用于为需要广播的表（广播表）创建广播规则

### 语法定义

```SQL
CreateDefaultShardingStrategy ::=
  'CREATE' 'DEFAULT' 'SHARDING' ('DATABASE' | 'TABLE') 'STRATEGY' '(' shardingStrategy ')'

shardingStrategy ::=
  'TYPE' '=' strategyType ',' ( 'SHARDING_COLUMN' '=' columnName  | 'SHARDING_COLUMNS' '=' columnNames ) ',' ( 'SHARDING_ALGORITHM' '=' algorithmName | algorithmDefinition )

algorithmDefinition ::=
  'TYPE' '(' 'NAME' '=' algorithmType ( ',' 'PROPERTIES'  '(' propertyDefinition  ')' )?')'  

columnNames ::=
  columnName (',' columnName)+

columnName ::=
  identifier

algorithmName ::=
  identifier
  
algorithmType ::=
  identifier
```

### 补充说明

- `tableName` 可使用已经存在的表或者将要创建的表
- 广播规则无法重复创建，但可包含多个广播表

### 示例

#### 创建广播规则

```SQL
-- 将 t_province， t_city 添加到广播规则中 
CREATE SHARDING BROADCAST TABLE RULES (t_province, t_city);
```

