+++
title = "Readwrite_splitting"
weight = 2
+++

## Resource Operation

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

## Rule Operation

- Create readwrite_splitting rule

```sql
CREATE READWRITE_SPLITTING RULE group_0 (
WRITE_RESOURCE=write_ds,
READ_RESOURCES(read_ds),
TYPE(NAME="random")
);
```

- Alter readwrite_splitting rule

```sql
ALTER READWRITE_SPLITTING RULE group_0 (
WRITE_RESOURCE=write_ds,
READ_RESOURCES(read_ds),
TYPE(NAME="random",PROPERTIES("read_weight"="2:0"))
);
```

- Drop readwrite_splitting rule

```sql
DROP READWRITE_SPLITTING RULE group_0;
```

- Drop resource

```sql
UNREGISTER STORAGE UNIT write_ds,read_ds;
```

- Drop distributed database

```sql
DROP DATABASE readwrite_splitting_db;
```
