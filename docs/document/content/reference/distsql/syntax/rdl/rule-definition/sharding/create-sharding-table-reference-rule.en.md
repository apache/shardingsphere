+++
title = "CREATE SHARDING TABLE REFERENCE RULE"
weight = 6
+++

## Description

The `CREATE SHARDING TABLE REFERENCE RULE` syntax is used to add reference relationships and create reference table rules for
tables with sharding table rules

### Syntax

```sql
CreateShardingTableReferenceRule ::=
  'CREATE' 'SHARDING' 'TABLE' 'REFERENCE' 'RULE'  referenceRelationshipDefinition  (',' referenceRelationshipDefinition )*

referenceRelationshipDefinition ::=
  '(' tableName (',' tableName)* ')'

tableName ::=
  identifier
```

### Supplement

- Creating reference relationships rules can only use sharding tables;
- A sharding table can only have one reference relationships;
- The sharding table for creating reference relationships needs to use the same storage unit and the same actual tables. For
  example `su_${0..1}.t_order_${0..1}` 与 `su_${0..1}.t_order_item_${0..1}`;
- The sharding table for creating reference relationships needs to use the same sharding algorithm for the sharding
  column. For example `t_order_{order_id % 2}` and `t_order_item_{order_item_id % 2}`;
- Only one reference rule can exist, but can contain multiple reference relationships, so can not
  execute `CREATE SHARDING TABLE REFERENCE RULE` more than one time. When a reference table rule already exists but a
  reference relationship needs to be added, you need to use `ALTER SHARDING TABLE REFERENCE RULE` to modify the reference
  table.

### Example

#### 1.Create a reference table rule

```sql
-- Before creating a reference table rule, you need to create sharding table rules t_order, t_order_item
CREATE SHARDING TABLE REFERENCE RULES (t_order,t_order_item);
```

#### 2.Create multiple reference table rules

```sql
-- Before creating reference table rules, you need to create sharding table rules t_order, t_order_item, t_product, t_product_item
CREATE SHARDING TABLE REFERENCE RULES (t_order,t_order_item),(t_product,t_product_item);
```

### Reserved word

`CREATE`, `SHARDING`, `TABLE`, `REFERENCE`, `RULES`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
- [CREATE SHARDING TABLE RULE](/en/reference/distsql/syntax/rdl/rule-definition/create-sharding-table-rule/)
