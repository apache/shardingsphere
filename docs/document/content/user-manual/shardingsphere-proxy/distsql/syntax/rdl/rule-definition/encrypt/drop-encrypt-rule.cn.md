+++
title = "DROP ENCRYPT RULE"
weight = 4
+++

## 说明

`DROP ENCRYPT RULE` 语法用于删除加密规则

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
DropEncryptRule ::=
  'DROP' 'ENCRYPT' 'RULE' tableName (',' tableName)*
    
tableName ::=
  identifier
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 示例

- 删除加密规则

```sql
DROP ENCRYPT RULE t_encrypt, t_encrypt_2;
```

### 保留字

`DROP`, `ENCRYPT`, `RULE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
