+++
title = "DROP ENCRYPT RULE"
weight = 4
+++

## 说明

`DROP ENCRYPT RULE` 语法用于删除加密规则

### 语法

```sql
DropEncryptRule ::=
  'DROP' 'ENCRYPT' 'RULE' tableName ( ',' tableName )*
    
tableName ::=
  identifier
```

### 示例

- 删除加密规则

```sql
DROP ENCRYPT RULE t_encrypt, t_encrypt_2;
```

### 保留字

`DROP`, `ENCRYPT`, `RULE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)
