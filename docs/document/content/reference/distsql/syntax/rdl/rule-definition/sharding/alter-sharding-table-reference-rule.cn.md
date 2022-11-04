+++
title = "ALTER SHARDING TABLE REFERENCE RULE"
weight = 14
+++

## 描述

`ALTER SHARDING TABLE REFERENCE RULE` 语法用于修改关联规则

### 语法定义

```sql
AlterShardingTableReferenceRule ::=
  'ALTER' 'SHARDING' 'TABLE' 'REFERENCE' 'RULE'  referenceRelationshipDefinition  (',' referenceRelationshipDefinition )*

referenceRelationshipDefinition ::=
  '(' tableName (',' tableName)* ')'

tableName ::=
  identifier
```

### 补充说明

- 一个分片表只能具有一个关联关系；
- 添加关联关系的分片表需要使用相同的存储单元，并且分片节点个数相同。例如 `su_${0..1}.t_order_${0..1}` 与 `su_${0..1}.t_order_item_${0..1}`；
- 添加关联关系的分片表需要对分片键使用相同的分片算法。例如 `t_order_${order_id % 2}` 与 `t_order_item_${order_item_id % 2}`；

### 示例

#### 1.创建关联关系

```sql
ALTER SHARDING TABLE REFERENCE RULE (t_order,t_order_item);
```

#### 2.创建多个关联关系

```sql
ALTER SHARDING TABLE REFERENCE RULE (t_order,t_order_item),(t_product,t_product_item);
```

### 保留字

`ALTER`、`SHARDING`、`TABLE`、`REFERENCE`、`RULE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)
- [CREATE SHARDING TABLE RULE](/cn/reference/distsql/syntax/rdl/rule-definition/create-sharding-table-rule/)
