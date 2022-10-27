+++
title = "数据库发现"
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

- 创建数据库发现规则

```sql
CREATE DB_DISCOVERY RULE db_discovery_group_0 (
STORAGE_UNITS(ds_0, ds_1),
TYPE(NAME='MySQL.MGR',PROPERTIES('group-name'='92504d5b-6dec')),
HEARTBEAT(PROPERTIES('keep-alive-cron'='0/5 * * * * ?'))
);
```

- 修改数据库发现规则

```sql
ALTER DB_DISCOVERY RULE db_discovery_group_0 (
STORAGE_UNITS(ds_0, ds_1, ds_2),
TYPE(NAME='MySQL.MGR',PROPERTIES('group-name'='92504d5b-6dec')),
HEARTBEAT(PROPERTIES('keep-alive-cron'='0/5 * * * * ?'))
);
```

- 删除数据库发现规则

```sql
DROP DB_DISCOVERY RULE db_discovery_group_0;
```

- 删除数据库发现类型

```sql
DROP DB_DISCOVERY TYPE db_discovery_group_0_mgr;
```

- 删除数据库发现心跳

```sql
DROP DB_DISCOVERY HEARTBEAT db_discovery_group_0_heartbeat;
```

- 删除数据源

```sql
UNREGISTER STORAGE UNIT ds_0,ds_1,ds_2;
```

- 删除分布式数据库

```sql
DROP DATABASE discovery_db;
```
