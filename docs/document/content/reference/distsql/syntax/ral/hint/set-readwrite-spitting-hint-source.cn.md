+++
title = "SET READWRITE_SPLITTING HINT SOURCE"
weight = 2
+++

### 描述

`SET READWRITE_SPLITTING HINT SOURCE` 语法用于针对当前连接，设置读写分离的路由策略（自动路由或强制到写库）
### 语法

```sql
SetReadwriteSplittingHintSource ::=
  'SET' 'READWRITE_SPLITTING' 'HINT' 'SOURCE' '='('auto'|'write')
```

### 示例

- 设置读写分离的路由策略为 auto

```sql
SET READWRITE_SPLITTING HINT SOURCE = auto;
```

- 设置读写分离的路由策略为 write

```sql
SET READWRITE_SPLITTING HINT SOURCE = write;
```

### 保留字

`SET`、`READWRITE_SPLITTING`、`HINT`、`SOURCE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)