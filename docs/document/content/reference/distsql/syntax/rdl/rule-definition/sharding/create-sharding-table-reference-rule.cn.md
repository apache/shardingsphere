+++
title = "CREATE SHARDING TABLE REFERENCE RULE"
weight = 13
+++

## 描述

`CREATE SHARDING TABLE REFERENCE RULE` 语法用于为具有分片规则的表（分片表）添加关联关系并创建关联规则

### 语法定义

```sql
CreateShardingTableReferenceRule ::=
  'CREATE' 'SHARDING' 'TABLE' 'REFERENCE' 'RULE'  referenceRelationshipDefinition  (',' referenceRelationshipDefinition )*

referenceRelationshipDefinition ::=
  '(' tableName (',' tableName)* ')'

tableName ::=
  identifier
```

### 补充说明

- 只有分片表才能创建关联关系；
- 一个分片表只能具有一个关联关系；
- 添加关联关系的分片表需要使用相同的存储单元，并且分片节点个数相同。例如 `su_${0..1}.t_order_${0..1}` 与 `su_${0..1}.t_order_item_${0..1}`；
- 添加关联关系的分片表需要对分片键使用相同的分片算法。例如 `t_order_${order_id % 2}` 与 `t_order_item_${order_item_id % 2}`；
- 只能存在一个关联规则，但可包含多个关联关系，因此无法重复执行 `CREATE SHARDING TABLE REFERENCE RULE`。
  当关联规则已经存在但还需要添加关联关系时，需要使用 `ALTER SHARDING TABLE REFERENCE RULE` 来修改关联规则。

### 示例

#### 1.创建关联关系

```sql
-- 创建关联关系之前需要先创建分片表 t_order,t_order_item
CREATE SHARDING TABLE REFERENCE RULES (t_order,t_order_item);
```

#### 2.创建多个关联关系

```sql
-- 创建关联关系之前需要先创建分片表 t_order,t_order_item,t_product,t_product_item
CREATE SHARDING TABLE REFERENCE RULES (t_order,t_order_item),(t_product,t_product_item);
```

### 保留字

`CREATE`、`SHARDING`、`TABLE`、`REFERENCE`、`RULES`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)
- [CREATE SHARDING TABLE RULE](/cn/reference/distsql/syntax/rdl/rule-definition/create-sharding-table-rule/)
