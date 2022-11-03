+++
title = "ALTER TRANSACTION RULE"
weight = 5
+++

### 描述

`ALTER TRANSACTION RULE` 语法用于修改事务规则

### 语法

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

### 补充说明

- `defaultTransactionType` 支持 `LOCAL`、`XA`、`BASE`

- `transactionManager` 支持  `Atomikos`、`Narayana` 和 `Bitronix`

### 示例

- 修改事务规则配置

```sql
ALTER TRANSACTION RULE(
  DEFAULT="XA”,TYPE(NAME="Narayana”, PROPERTIES(“databaseName"="jbossts”,“host"="127.0.0.1”))
);
```

### 保留字

`ALTER`、`TRANSACTION`、`RULE`、`DEFAULT`、`TYPE`、`NAME`、`PROPERTIES`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)