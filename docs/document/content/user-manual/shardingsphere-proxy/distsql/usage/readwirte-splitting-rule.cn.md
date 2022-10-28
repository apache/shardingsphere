+++
title = "读写分离"
weight = 2
+++

## 资源操作

```sql
REGISTER STORAGE UNIT write_ds (
    HOST="127.0.0.1",
    PORT=3306,
    DB="ds_0",
    USER="root",
    PASSWORD="root"
),read_ds (
    HOST="127.0.0.1",
    PORT=3307,
    DB="ds_0",
    USER="root",
    PASSWORD="root"
);
```

## 规则操作

- 创建读写分离规则

```sql
CREATE READWRITE_SPLITTING RULE group_0 (
WRITE_STORAGE_UNIT=write_ds,
READ_STORAGE_UNITS(read_ds),
TYPE(NAME="random")
);
```

- 修改读写分离规则

```sql
ALTER READWRITE_SPLITTING RULE group_0 (
WRITE_STORAGE_UNIT=write_ds,
READ_STORAGE_UNITS(read_ds),
TYPE(NAME="random",PROPERTIES("read_weight"="2:0"))
);
```

- 删除读写分离规则

```sql
DROP READWRITE_SPLITTING RULE group_0;
```

- 移除数据源

```sql
UNREGISTER STORAGE UNIT write_ds,read_ds;
```

- 删除分布式数据库

```sql
DROP DATABASE readwrite_splitting_db;
```
