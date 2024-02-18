+++
title = "SHOW TRANSACTION RULE"
weight = 2
+++

### 描述

`SHOW TRANSACTION RULE` 语法用于查询事务规则配置。
### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowTransactionRule ::=
  'SHOW' 'TRANSACTION' 'RULE'
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 返回值说明

| 列             | 说明      |
|---------------|---------|
| default_type  | 默认事务类型  |
| provider_type | 事务提供者类型 |
| props         | 事务参数    |

### 示例

- 查询事务规则配置

```sql
SHOW TRANSACTION RULE;
```

```sql
mysql> SHOW TRANSACTION RULE;
+--------------+---------------+-------+
| default_type | provider_type | props |
+--------------+---------------+-------+
| LOCAL        |               |       |
+--------------+---------------+-------+
1 row in set (0.05 sec)
```

### 保留字

`SHOW`、`TRANSACTION`、`RULE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)