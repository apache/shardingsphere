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
- The sharding table for creating binding table rules needs to use the same resources and the same actual tables
- The sharding table for creating binding table rules needs to use the same sharding algorithm

### Example

#### Create a binding table rule

```sql
-- Before creating a binding table rule, you need to create a sharding table t_order, t_order_item
CREATE SHARDING BINDING TABLE RULES (t_order,t_order_item);
```

### Related links
- [CREATE SHARDING TABLE RULE](/en/reference/distsql/syntax/rdl/rule-definition/create-sharding-table-rule/)

