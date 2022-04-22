+++
title = "CREATE SHARDING BINDING TABLE RULE"
weight = 3
+++

## Description

The `CREATE SHARDING BINDING TABLE RULE` syntax is used to add a binding rule to sharding tables.

### Syntax

```SQL
CreateBindingTableRule ::=
  'CREATE' 'SHARDING' 'BINDING' 'TABLE' 'RULES' '(' tableName (',' tableName)* ')'

tableName ::=
  identifier
```

### Supplement

- Creating binding table rules can only use sharding tables
- A sharding table can only have one binding table rule
- The sharding table for creating binding table rules needs to use the same resources and the same actual tables. For example `ds_${0..1}.t_order_${0..1}` 与 `ds_${0..1}.t_order_item_${0..1}`
- The sharding table for creating binding table rules needs to use the same sharding algorithm for the sharding column.  For example `t_order_{order_id % 2}` and `t_order_item_{order_item_id % 2}`

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

### Related links
- [CREATE SHARDING TABLE RULE](/en/reference/distsql/syntax/rdl/rule-definition/create-sharding-table-rule/)

