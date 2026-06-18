+++
title = "DROP BROADCAST TABLE RULE"
weight = 2
+++

## 描述

`DROP BROADCAST TABLE RULE` 语法用于为指定的广播表删除广播规则。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
DropBroadcastTableRule ::=
  'DROP' 'BROADCAST' 'TABLE' 'RULE' ifExists? tableName (',' tableName)* 

ifExists ::=
  'IF' 'EXISTS'

tableName ::=
  identifier
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `tableName` 可使用已经存在的广播规则的表；
- `ifExists` 子句用于避免 `Broadcast rule not exists` 错误。

### 示例

- 为指定广播表删除广播规则
 
```sql
DROP BROADCAST TABLE RULE t_province, t_city;
```

- 使用 `ifExists` 子句为广播表删除广播规则

```sql
DROP BROADCAST TABLE RULE IF EXISTS t_province, t_city;
```

### 保留字

`DROP`、`BROADCAST`、`TABLE`、`RULE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)