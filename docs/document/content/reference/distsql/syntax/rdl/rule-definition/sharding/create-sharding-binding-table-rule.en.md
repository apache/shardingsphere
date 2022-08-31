+++
title = "CREATE SHARDING BINDING TABLE RULE"
weight = 6
+++

## Description

The `CREATE SHARDING BINDING TABLE RULE` syntax is used to add binding relationships and create binding table rules for
tables with sharding table rules

### Syntax

```sql
CreateBindingTableRule ::=
  'CREATE' 'SHARDING' 'BINDING' 'TABLE' 'RULES'  bindingTableDefinition  (',' bindingTableDefinition )*

bindingTableDefinition ::=
  '(' tableName (',' tableName)* ')'

tableName ::=
  string
```

### Supplement

- Creating binding relationships rules can only use sharding tables;
- A sharding table can only have one binding relationships;
- The sharding table for creating binding relationships needs to use the same resources and the same actual tables. For
  example `ds_${0..1}.t_order_${0..1}` 与 `ds_${0..1}.t_order_item_${0..1}`;
- The sharding table for creating binding relationships needs to use the same sharding algorithm for the sharding
  column. For example `t_order_{order_id % 2}` and `t_order_item_{order_item_id % 2}`;
- Only one binding rule can exist, but can contain multiple binding relationships, so can not
  execute `CREATE SHARDING BINDING TABLE RULE` more than one time. When a binding table rule already exists but a
  binding relationship needs to be added, you need to use `ALTER SHARDING BINDING TABLE RULE` to modify the binding
  table.

### Example

#### 1.Create a binding table rule

```sql
-- Before creating a binding table rule, you need to create sharding table rules t_order, t_order_item
CREATE SHARDING BINDING TABLE RULES (t_order,t_order_item);
```

#### 2.Create multiple binding table rules

```sql
-- Before creating binding table rules, you need to create sharding table rules t_order, t_order_item, t_product, t_product_item
CREATE SHARDING BINDING TABLE RULES (t_order,t_order_item),(t_product,t_product_item);
```

### Reserved word

`CREATE`, `SHARDING`, `BINDING`, `TABLE`, `RULES`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
- [CREATE SHARDING TABLE RULE](/en/reference/distsql/syntax/rdl/rule-definition/create-sharding-table-rule/)
