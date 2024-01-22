+++
title = "DROP SHARDING TABLE REFERENCE RULE"
weight = 14
+++

## 描述

`DROP SHARDING TABLE REFERENCE RULE` 语法用删除指定的关联规则。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
DropShardingTableReferenceRule ::=
  'DROP' 'SHARDING' 'TABLE' 'REFERENCE' 'RULE' ifExists? ruleName (',' ruleName)*

ifExists ::=
  'IF' 'EXISTS'

ruleName ::=
  identifier
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `ifExists` 子句用于避免 `Sharding reference rule not exists` 错误。

### 示例

- 删除单个关联规则
 
```sql
DROP SHARDING TABLE REFERENCE RULE ref_0;
```

- 删除多个关联规则

```sql
DROP SHARDING TABLE REFERENCE RULE ref_0, ref_1;
```

- 使用 `ifExists` 子句删除关联规则

```sql
DROP SHARDING TABLE REFERENCE RULE IF EXISTS ref_0;
```

### 保留字

`DROP`、`SHARDING`、`TABLE`、`REFERENCE`、`RULE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)