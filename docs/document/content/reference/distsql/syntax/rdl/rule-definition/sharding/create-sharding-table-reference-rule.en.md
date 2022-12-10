+++
title = "CREATE SHARDING TABLE REFERENCE RULE"
weight = 13
+++

## Description

The `CREATE SHARDING TABLE REFERENCE RULE` syntax is used to create reference rule for sharding tables

### Syntax

```sql
CreateShardingTableReferenceRule ::=
  'CREATE' 'SHARDING' 'TABLE' 'REFERENCE' 'RULE'  referenceRelationshipDefinition  (',' referenceRelationshipDefinition )*

referenceRelationshipDefinition ::=
   ruleName '(' tableName (',' tableName)* ')'

tableName ::=
  identifier
```

### Supplement

- Sharding table reference rule can only be created for sharding tables;
- A sharding table can only be associated with one sharding table reference rule;
- The referenced sharding tables should be sharded in the same storage units and have the same number of sharding nodes. For
  example `ds_${0..1}.t_order_${0..1}` and `ds_${0..1}.t_order_item_${0..1}`;
- The referenced sharding tables should use consistent sharding algorithms. For example `t_order_{order_id % 2}` and `t_order_item_{order_item_id % 2}`;

### Example

#### 1.Create a sharding table reference rule

```sql
-- Before creating a sharding table reference rule, you need to create sharding table rules t_order, t_order_item
CREATE SHARDING TABLE REFERENCE RULE ref_0 (t_order,t_order_item);
```

#### 2.Create multiple sharding table reference rules

```sql
-- Before creating sharding table reference rules, you need to create sharding table rules t_order, t_order_item, t_product, t_product_item
CREATE SHARDING TABLE REFERENCE RULE ref_0 (t_order,t_order_item), ref_1 (t_product,t_product_item);
```

### Reserved word

`CREATE`, `SHARDING`, `TABLE`, `REFERENCE`, `RULE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
- [CREATE SHARDING TABLE RULE](/en/reference/distsql/syntax/rdl/rule-definition/create-sharding-table-rule/)
