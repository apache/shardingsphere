+++
title = "ALTER MASK RULE"
weight = 2
+++

## 描述

`ALTER MASK RULE` 语法用于修改数据脱敏规则。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
CreateEncryptRule ::=
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
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `algorithmType` 指定数据脱敏算法类型，请参考 [数据脱敏算法](/cn/user-manual/common-config/builtin-algorithm/mask/)。

### 示例

#### 修改数据脱敏规则

```sql
ALTER MASK RULE t_mask (
COLUMNS(
(NAME=phone_number,TYPE(NAME='MASK_FROM_X_TO_Y', PROPERTIES("from-x"=1, "to-y"=2, "replace-char"="*"))),
(NAME=address,TYPE(NAME='MD5'))
));
```

### 保留字

`ALTER`、`MASK`、`RULE`、`COLUMNS`、`NAME`、`TYPE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [数据脱敏算法](/cn/user-manual/common-config/builtin-algorithm/mask/)