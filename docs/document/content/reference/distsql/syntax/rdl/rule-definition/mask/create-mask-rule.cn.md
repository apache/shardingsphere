+++
title = "CREATE MASK RULE"
weight = 2
+++

## 描述

The `CREATE MASK RULE` 语法用于创建数据脱敏规则.

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
CreateEncryptRule ::=
  'CREATE' 'MASK' 'RULE' maskRuleDefinition (',' maskRuleDefinition)*

maskRuleDefinition ::=
  ruleName '(' 'COLUMNS' '(' columnDefinition (',' columnDefinition)* ')' ')'

columnDefinition ::=
  '(' 'NAME' '=' columnName ',' maskAlgorithmDefinition ')'

maskAlgorithmDefinition ::=
  'TYPE' '(' 'NAME' '=' maskAlgorithmType (',' propertiesDefinition)? ')'

propertiesDefinition ::=
  'PROPERTIES' '(' key '=' value (',' key '=' value)* ')'

ruleName ::=
  identifier

columnName ::=
  identifier

maskAlgorithmType ::=
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

- `maskAlgorithmType` 指定数据脱敏算法类型，请参考 [数据脱敏算法](/cn/user-manual/common-config/builtin-algorithm/mask/)；
- 重复的 `ruleName` 将无法被创建。

### 示例

#### 创建数据脱敏规则

```sql
CREATE MASK RULE t_mask (
COLUMNS(
(NAME=phone_number,TYPE(NAME='MASK_FROM_X_TO_Y', PROPERTIES("from-x"=1, "to-y"=2, "replace-char"="*"))),
(NAME=address,TYPE(NAME='MD5'))
));
```

### 保留字

`CREATE`、`MASK`、`RULE`、`COLUMNS`、`NAME`、`TYPE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)
- [数据脱敏算法](/cn/user-manual/common-config/builtin-algorithm/mask/)