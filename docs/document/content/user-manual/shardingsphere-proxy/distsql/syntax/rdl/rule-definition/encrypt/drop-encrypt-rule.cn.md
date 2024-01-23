+++
title = "DROP ENCRYPT RULE"
weight = 3
+++

## 说明

`DROP ENCRYPT RULE` 语法用于删除加密规则。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
DropEncryptRule ::=
  'DROP' 'ENCRYPT' 'RULE' ifExists? ruleName (',' ruleName)*

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

- `ifExists` 子句用于避免 `Encrypt rule not exists` 错误。

### 示例

- 删除加密规则

```sql
DROP ENCRYPT RULE t_encrypt, t_encrypt_2;
```

- 使用 `ifExists` 删除加密规则

```sql
DROP ENCRYPT RULE IF EXISTS t_encrypt, t_encrypt_2;
```

### 保留字

`DROP`, `ENCRYPT`, `RULE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
