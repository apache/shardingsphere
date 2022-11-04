+++
title = "ALTER SHARDING TABLE REFERENCE RULE"
weight = 14
+++

## Description

The `ALTER SHARDING TABLE REFERENCE RULE` syntax is used to alter reference table rules. 

### Syntax

```sql
AlterShardingTableReferenceRule ::=
  'ALTER' 'SHARDING' 'TABLE' 'REFERENCE' 'RULE'  referenceRelationshipDefinition  (',' referenceRelationshipDefinition )*

referenceRelationshipDefinition ::=
  '(' tableName (',' tableName)* ')'

tableName ::=
  identifier
```

### Supplement

- A sharding table can only have one reference relationships;
- The sharding table for creating reference relationships needs to use the same storage unit and the same actual tables. For
  example `su_${0..1}.t_order_${0..1}` 与 `su_${0..1}.t_order_item_${0..1}`;
- The sharding table for creating reference relationships needs to use the same sharding algorithm for the sharding
  column. For example `t_order_{order_id % 2}` and `t_order_item_{order_item_id % 2}`;

### Example

#### 1. Alter a reference table rule

```sql
ALTER SHARDING TABLE REFERENCE RULE (t_order,t_order_item);
```

#### 2. Alter multiple reference table rules

```sql
ALTER SHARDING TABLE REFERENCE RULE (t_order,t_order_item),(t_product,t_product_item);
```

### Reserved word

`ALTER`, `SHARDING`, `TABLE`, `REFERENCE`, `RULE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
- [CREATE SHARDING TABLE RULE](/en/reference/distsql/syntax/rdl/rule-definition/create-sharding-table-rule/)
