+++
title = "ALTER TRANSACTION RULE"
weight = 5
+++

### Description

The `ALTER TRANSACTION RULE` syntax is used to alter transaction rule configuration.

### Syntax

```sql
AlterTransactionRule ::=
  'ALTER' 'TRANSACTION' 'RULE' '(' 'DEFAULT' '=' defaultTransactionType ',' 'TYPE' '(' 'NAME' '=' transactionManager ',' 'PROPERTIES' '(' key '=' value (',' key '=' value)* ')' ')' ')'

defaultTransactionType ::=
  string

transactionManager ::=
  string

key ::=
  string

value ::=
  string
```

### Supplement

- `defaultTransactionType` support `LOCAL`, `XA`, `BASE`

- `transactionManager` support  `Atomikos`, `Narayana` and `Bitronix`

### Example

- Alter transaction rule

```sql
ALTER TRANSACTION RULE(
  DEFAULT="XA”,TYPE(NAME="Narayana”, PROPERTIES(“databaseName"="jbossts”,“host"="127.0.0.1”))
);
```

### Reserved word

`ALTER`, `TRANSACTION`, `RULE`, `DEFAULT`, `TYPE`, `NAME`, `PROPERTIES`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
