+++
title = "ALTER TRANSACTION RULE"
weight = 3
+++

### Description

The `ALTER TRANSACTION RULE` syntax is used to alter transaction rule configuration.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
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
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `defaultTransactionType` support `LOCAL`, `XA`, `BASE`

- `transactionManager` support  `Atomikos` and `Narayana`

### Example

- Alter transaction rule

```sql
ALTER TRANSACTION RULE(
  DEFAULT="XA", TYPE(NAME="Narayana", PROPERTIES("databaseName"="jbossts", "host"="127.0.0.1"))
);
```

### Reserved word

`ALTER`, `TRANSACTION`, `RULE`, `DEFAULT`, `TYPE`, `NAME`, `PROPERTIES`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
