+++
title = "Shadow"
weight = 5
+++

## Resource Operation

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

## Rule Operation

- Create shadow rule

```sql
CREATE SHADOW RULE group_0(
SOURCE=ds_0,
SHADOW=ds_1,
t_order(TYPE(NAME="SIMPLE_HINT", PROPERTIES("foo"="bar")),TYPE(NAME="REGEX_MATCH", PROPERTIES("operation"="insert","column"="user_id", "regex"='[1]'))), 
t_order_item(TYPE(NAME="SIMPLE_HINT", PROPERTIES("foo"="bar"))));
```

- Alter shadow rule

```sql
ALTER SHADOW RULE group_0(
SOURCE=ds_0,
SHADOW=ds_2,
t_order_item(TYPE(NAME="SIMPLE_HINT", PROPERTIES("foo"="bar"))));
```

- Drop shadow rule

```sql
DROP SHADOW RULE group_0;
```

- Drop storage unit

```sql
UNREGISTER STORAGE UNIT ds_0,ds_1,ds_2;
```

- Drop distributed database

```sql
DROP DATABASE foo_db;
```
