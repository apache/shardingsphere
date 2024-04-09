+++
title = "ALTER MASK RULE"
weight = 2
+++

## Description

The `ALTER MASK RULE` syntax is used to create a mask rule.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
AlterEncryptRule ::=
  'ALTER' 'MASK' 'RULE' maskRuleDefinition (',' maskRuleDefinition)*

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

### Supplement

- `algorithmType` specifies the data masking algorithm type, please refer to [Data Masking Algorithm](/en/user-manual/common-config/builtin-algorithm/mask/).

### Example

#### Alter a mask rule

```sql
ALTER MASK RULE t_mask (
COLUMNS(
(NAME=phone_number,TYPE(NAME='MASK_FROM_X_TO_Y', PROPERTIES("from-x"=1, "to-y"=2, "replace-char"="*"))),
(NAME=address,TYPE(NAME='MD5'))
));
```

### Reserved words

`ALTER`, `MASK`, `RULE`, `COLUMNS`, `NAME`, `TYPE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [Data Masking Algorithm](/en/user-manual/common-config/builtin-algorithm/mask/)
