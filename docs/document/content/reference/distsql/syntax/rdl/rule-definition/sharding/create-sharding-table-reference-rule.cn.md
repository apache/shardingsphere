+++
title = "CREATE SHARDING TABLE REFERENCE RULE"
weight = 13
+++

## 描述

`CREATE SHARDING TABLE REFERENCE RULE` 语法用于为分片表创建关联规则

### 语法定义

```sql
CreateShardingTableReferenceRule ::=
  'CREATE' 'SHARDING' 'TABLE' 'REFERENCE' 'RULE'  referenceRelationshipDefinition  (',' referenceRelationshipDefinition)*

referenceRelationshipDefinition ::=
   ruleName '(' tableName (',' tableName)* ')'

tableName ::=
  identifier
```

### 补充说明

- 只能为分片表创建关联关系；
- 一张分片表只能具有一个关联关系；
- 关联的分片表应分布在相同的存储单元，并且分片个数相同。例如 `ds_${0..1}.t_order_${0..1}` 与 `ds_${0..1}.t_order_item_${0..1}`；
- 关联的分片表应使用一致的分片算法。例如 `t_order_${order_id % 2}` 与 `t_order_item_${order_item_id % 2}`；

### 示例

#### 1.创建关联关系

```sql
-- 创建关联关系之前需要先创建分片规则 t_order,t_order_item
CREATE SHARDING TABLE REFERENCE RULE ref_0 (t_order,t_order_item);
```

#### 2.创建多个关联关系

```sql
-- 创建关联关系之前需要先创建分片规则 t_order,t_order_item,t_product,t_product_item
CREATE SHARDING TABLE REFERENCE RULE ref_0 (t_order,t_order_item), ref_1 (t_product,t_product_item);
```

### 保留字

`CREATE`、`SHARDING`、`TABLE`、`REFERENCE`、`RULE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)
- [CREATE SHARDING TABLE RULE](/cn/reference/distsql/syntax/rdl/rule-definition/create-sharding-table-rule/)
