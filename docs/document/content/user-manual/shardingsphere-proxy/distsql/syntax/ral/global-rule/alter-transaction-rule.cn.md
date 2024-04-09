+++
title = "ALTER TRANSACTION RULE"
weight = 3
+++

### 描述

`ALTER TRANSACTION RULE` 语法用于修改事务规则。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
AlterTransactionRule ::=
  'ALTER' 'TRANSACTION' 'RULE' '(' 'DEFAULT' '=' defaultTransactionType ',' 'TYPE' '(' 'NAME' '=' transactionManager ',' propertiesDefinition ')' ')'

propertiesDefinition ::=
  'PROPERTIES' '(' key '=' value (',' key '=' value)* ')'

defaultTransactionType ::=
  string

transactionManager ::=
  string

key ::=
  string

value ::=
  literal
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `defaultTransactionType` 支持 `LOCAL`、`XA`、`BASE`

- `transactionManager` 支持  `Atomikos` 和 `Narayana`

### 示例

- 修改事务规则配置

```sql
ALTER TRANSACTION RULE(
  DEFAULT="XA", TYPE(NAME="Narayana", PROPERTIES("databaseName"="jbossts", "host"="127.0.0.1"))
);
```

### 保留字

`ALTER`、`TRANSACTION`、`RULE`、`DEFAULT`、`TYPE`、`NAME`、`PROPERTIES`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)