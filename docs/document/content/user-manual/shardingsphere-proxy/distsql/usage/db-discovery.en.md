+++
title = "DB Discovery"
weight = 5
+++

## Resource Operation

```sql
ADD RESOURCE ds_0 (
HOST=127.0.0.1,
PORT=3306,
DB=ds_0,
USER=root,
PASSWORD=root
),RESOURCE ds_1 (
HOST=127.0.0.1,
PORT=3306,
DB=ds_1,
USER=root,
PASSWORD=root
),RESOURCE ds_2 (
HOST=127.0.0.1,
PORT=3306,
DB=ds_2,
USER=root,
PASSWORD=root
);
```

## Rule Operation

- Create DB discovery rule

```sql
CREATE DB_DISCOVERY RULE group_0 (
RESOURCES(ds_0,ds_1),
TYPE(NAME=mgr,PROPERTIES(groupName='92504d5b-6dec',keepAliveCron=''))
);
```

- Alter DB discovery rule

```sql
ALTER DB_DISCOVERY RULE group_0 (
RESOURCES(ds_0,ds_1,ds_2),
TYPE(NAME=mgr,PROPERTIES(groupName='92504d5b-6dec' ,keepAliveCron=''))
);
```

- Drop db_discovery rule

```sql
DROP DB_DISCOVERY RULE group_0;
```

- Drop resource

```sql
DROP RESOURCE ds_0,ds_1,ds_2;
```

- Drop distributed database

```sql
DROP DATABASE discovery_db;
```
