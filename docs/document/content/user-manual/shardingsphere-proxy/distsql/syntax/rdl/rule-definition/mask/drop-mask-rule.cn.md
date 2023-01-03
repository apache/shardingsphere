+++
title = "DROP MASK RULE"
weight = 4
+++

## 说明

`DROP MASK RULE` 语法用于删除数据脱敏规则

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
DropEncryptRule ::=
  'DROP' 'MASK' 'RULE' ruleName (',' ruleName)*
    
ruleName ::=
  identifier
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 示例

- 删除数据脱敏规则

```sql
DROP MASK RULE t_mask, t_mask_1;
```

### 保留字

`DROP`, `MASK`, `RULE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)
