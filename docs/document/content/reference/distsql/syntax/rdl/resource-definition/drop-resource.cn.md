+++
title = "DROP RESOURCE"
weight = 4
+++

### 描述

`DROP RESOURCE` 语法用于从当前逻辑库中移除资源。

### 语法

```sql
DropResource ::=
  'DROP' 'RESOURCE' ( 'IF' 'EXISTS' )? resourceName  ( ',' resourceName )* ( 'IGNORE' 'SINGLE' 'TABLES' )?

resourceName ::=
  identifier
```

### 补充说明

- `DROP RESOURCE` 只会移除 Proxy 中的资源，不会删除与资源对应的真实数据源；
- 无法移除已经被规则使用的资源。移除被规则使用的资源时会提示 `Resources are still in used`；
- 将要移除的资源中仅包含 `SINGLE TABLE RULE`，且用户确认可以忽略该限制时，可添加 `IGNORE SINGLE TABLES` 关键字移除资源。

### 示例

- 移除资源

```sql
DROP RESOURCE ds_0;
```

- 移除多个资源

```sql
DROP RESOURCE ds_1, ds_2;
```

- 忽略单表移除资源

```sql
DROP RESOURCE ds_3 IGNORE SINGLE TABLES;
```

- 如果资源存在则移除

```sql
DROP RESOURCE IF EXISTS ds_4;
```

### 保留字

`DROP`、`RESOURCE`、`IF`、`EXISTS`、`IGNORE`、`SINGLE`、`TABLES`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)