+++
title = "影子库压测"
weight = 5
+++

## 资源操作

```sql
REGISTER STORAGE UNIT ds_0 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="ds_0",
    USER="root",
    PASSWORD="root"
),ds_1 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="ds_1",
    USER="root",
    PASSWORD="root"
),ds_2 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="ds_2",
    USER="root",
    PASSWORD="root"
);
```

## 规则操作

- 创建影子库压测规则

```sql
CREATE SHADOW RULE group_0(
SOURCE=ds_0,
SHADOW=ds_1,
t_order(TYPE(NAME="SIMPLE_HINT", PROPERTIES("foo"="bar")),TYPE(NAME="REGEX_MATCH", PROPERTIES("operation"="insert","column"="user_id", "regex"='[1]'))), 
t_order_item(TYPE(NAME="SIMPLE_HINT", PROPERTIES("foo"="bar"))));
```

- 修改影子库压测规则

```sql
ALTER SHADOW RULE group_0(
SOURCE=ds_0,
SHADOW=ds_2,
t_order_item(TYPE(NAME="SIMPLE_HINT", PROPERTIES("foo"="bar"))));
```

- 删除影子库压测规则

```sql
DROP SHADOW RULE group_0;
```

- 删除数据源

```sql
UNREGISTER STORAGE UNIT ds_0,ds_1,ds_2;
```

9. 删除分布式数据库

```sql
DROP DATABASE foo_db;
```
