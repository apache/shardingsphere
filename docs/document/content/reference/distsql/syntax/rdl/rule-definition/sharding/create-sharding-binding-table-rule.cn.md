+++
title = "CREATE SHARDING BINDING TABLE RULE"
weight = 3
+++

## 描述

`CREATE SHARDING BINDING TABLE RULE` 语法用于为具有分片规则的表(分片表)添加绑定关系

### 语法定义

```SQL
CreateBindingTableRule ::=
  'CREATE' 'SHARDING' 'BINDING' 'TABLE' 'RULES' '(' tableName (',' tableName)* ')'

tableName ::=
  identifier
```

### 补充说明

- 只有分片表才能创建绑定关系
- 一个分片表只能具有一个绑定关系
- 创建绑定关系的分片表需要使用相同的资源，并且分片节点个数相同，否则绑定关系无法生效
- 创建绑定关系的分片表需要使用相同的分片算法，否则绑定关系无法生效

### 示例

#### 创建绑定关系

```sql
-- 创建绑定关系之前需要先创建分片表 t_order,t_order_item
CREATE SHARDING BINDING TABLE RULES (t_order,t_order_item);
```

### 相关链接
- [保留字](/cn/reference/distsql/syntax/reserved-word/)
- [CREATE SHARDING TABLE RULE](/cn/reference/distsql/syntax/rdl/rule-definition/create-sharding-table-rule/)

