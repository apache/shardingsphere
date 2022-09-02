+++
title = "CREATE SHARDING BINDING TABLE RULE"
weight = 6
+++

## 描述

`CREATE SHARDING BINDING TABLE RULE` 语法用于为具有分片规则的表（分片表）添加绑定关系并创建绑定规则

### 语法定义

```sql
CreateBindingTableRule ::=
  'CREATE' 'SHARDING' 'BINDING' 'TABLE' 'RULES'  bindingRelationshipDefinition  (',' bindingRelationshipDefinition )*

bindingRelationshipDefinition ::=
  '(' tableName (',' tableName)* ')'

tableName ::=
  identifier
```

### 补充说明

- 只有分片表才能创建绑定关系；
- 一个分片表只能具有一个绑定关系；
- 添加绑定关系的分片表需要使用相同的资源，并且分片节点个数相同。例如 `ds_${0..1}.t_order_${0..1}` 与 `ds_${0..1}.t_order_item_${0..1}`；
- 添加绑定关系的分片表需要对分片键使用相同的分片算法。例如 `t_order_${order_id % 2}` 与 `t_order_item_${order_item_id % 2}`；
- 只能存在一个绑定规则，但可包含多个绑定关系，因此无法重复执行 `CREATE SHARDING BINDING TABLE RULE`。
  当绑定规则已经存在但还需要添加绑定关系时，需要使用 `ALTER SHARDING BINDING TABLE RULE` 来修改绑定规则。

### 示例

#### 1.创建绑定关系

```sql
-- 创建绑定关系之前需要先创建分片表 t_order,t_order_item
CREATE SHARDING BINDING TABLE RULES (t_order,t_order_item);
```

#### 2.创建多个绑定关系

```sql
-- 创建绑定关系之前需要先创建分片表 t_order,t_order_item,t_product,t_product_item
CREATE SHARDING BINDING TABLE RULES (t_order,t_order_item),(t_product,t_product_item);
```

### 保留字

`CREATE`、`SHARDING`、`BINDING`、`TABLE`、`RULES`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)
- [CREATE SHARDING TABLE RULE](/cn/reference/distsql/syntax/rdl/rule-definition/create-sharding-table-rule/)
