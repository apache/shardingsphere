+++
title = "CREATE MASK RULE"
weight = 1
+++

## 描述

`CREATE MASK RULE` 语法用于创建数据脱敏规则。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
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
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `algorithmType` 指定数据脱敏算法类型，请参考 [数据脱敏算法](/cn/user-manual/common-config/builtin-algorithm/mask/)；
- 重复的 `ruleName` 将无法被创建；
- `ifNotExists` 子句用于避免出现 `Duplicate mask rule` 错误。

### 示例

#### 创建数据脱敏规则

```sql
CREATE MASK RULE t_mask (
COLUMNS(
(NAME=phone_number,TYPE(NAME='MASK_FROM_X_TO_Y', PROPERTIES("from-x"=1, "to-y"=2, "replace-char"="*"))),
(NAME=address,TYPE(NAME='MD5'))
));
```

#### 使用 `ifNotExists` 子句创建数据脱敏规则

```sql
CREATE MASK RULE IF NOT EXISTS t_mask (
COLUMNS(
(NAME=phone_number,TYPE(NAME='MASK_FROM_X_TO_Y', PROPERTIES("from-x"=1, "to-y"=2, "replace-char"="*"))),
(NAME=address,TYPE(NAME='MD5'))
));
```

### 保留字

`CREATE`、`MASK`、`RULE`、`COLUMNS`、`NAME`、`TYPE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [数据脱敏算法](/cn/user-manual/common-config/builtin-algorithm/mask/)