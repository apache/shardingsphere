+++
title = "CREATE MASK RULE"
weight = 1
+++

## Description

The `CREATE MASK RULE` syntax is used to create a mask rule.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
CreateEncryptRule ::=
  'CREATE' 'MASK' 'RULE' ifNotExists? maskRuleDefinition (',' maskRuleDefinition)*

ifNotExists ::=
  'IF' 'NOT' 'EXISTS'

maskRuleDefinition ::=
  ruleName '(' 'COLUMNS' '(' columnDefinition (',' columnDefinition)* ')' ')'

columnDefinition ::=
  '(' 'NAME' '=' columnName ',' maskAlgorithmDefinition ')'

maskAlgorithmDefinition ::=
  'TYPE' '(' 'NAME' '=' algorithmType (',' propertiesDefinition)? ')'

propertiesDefinition ::=
  'PROPERTIES' '(' key '=' value (',' key '=' value)* ')'

ruleName ::=
  identifier

columnName ::=
  identifier

algorithmType ::=
  literal

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

### Note

- `algorithmType` specifies the data masking algorithm type. For more details, please refer to [Data Masking Algorithm](/en/user-manual/common-config/builtin-algorithm/mask/);
- Duplicate `ruleName` will not be created;
- `ifNotExists` clause is used for avoid `Duplicate mask rule` error.

### Example

#### Create a mask rule

```sql
CREATE MASK RULE t_mask (
COLUMNS(
(NAME=phone_number,TYPE(NAME='MASK_FROM_X_TO_Y', PROPERTIES("from-x"=1, "to-y"=2, "replace-char"="*"))),
(NAME=address,TYPE(NAME='MD5'))
));
```

#### Create mask rule with `ifNotExists` clause

```sql
CREATE MASK RULE IF NOT EXISTS t_mask (
COLUMNS(
(NAME=phone_number,TYPE(NAME='MASK_FROM_X_TO_Y', PROPERTIES("from-x"=1, "to-y"=2, "replace-char"="*"))),
(NAME=address,TYPE(NAME='MD5'))
));
```

### Reserved words

`CREATE`, `MASK`, `RULE`, `COLUMNS`, `NAME`, `TYPE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [Data Masking Algorithm](/en/user-manual/common-config/builtin-algorithm/mask/)
