+++
title = "数据库发现"
weight = 5
+++

## 资源操作

```sql
ADD RESOURCE ds_0 (
HOST=127.0.0.1,
PORT=3306,
DB=ds_0,
USER=root,
PASSWORD=root
),ds_1 (
HOST=127.0.0.1,
PORT=3306,
DB=ds_1,
USER=root,
PASSWORD=root
),ds_2 (
HOST=127.0.0.1,
PORT=3306,
DB=ds_2,
USER=root,
PASSWORD=root
);
```

## 规则操作

- 创建数据库发现规则

```sql
CREATE DB_DISCOVERY RULE group_0 (
RESOURCES(ds_0,ds_1),
TYPE(NAME=mgr,PROPERTIES(groupName='92504d5b-6dec',keepAliveCron=''))
);
```

- 修改数据库发现规则

```sql
ALTER DB_DISCOVERY RULE group_0 (
RESOURCES(ds_0,ds_1,ds_2),
TYPE(NAME=mgr,PROPERTIES(groupName='92504d5b-6dec' ,keepAliveCron=''))
);
```

- 删除数据库发现规则

```sql
DROP DB_DISCOVERY RULE group_0;
```

- 删除数据源

```sql
DROP RESOURCE ds_0,ds_1,ds_2;
```

- 删除分布式数据库

```sql
DROP DATABASE discovery_db;
```
