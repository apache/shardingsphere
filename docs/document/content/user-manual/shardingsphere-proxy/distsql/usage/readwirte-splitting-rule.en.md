+++
title = "readwrite_splitting"
weight = 2
+++

## Usage

### Pre-work

1. Start the MySQL service
2. Create MySQL database (refer to ShardingProxy data source configuration rules)
3. Create a role or user with creation permission for ShardingProxy
4. Start Zookeeper service (for persistent configuration)

### Start ShardingProxy

1. Add `mode` and `authentication` configurations to `server.yaml` (please refer to the example of ShardingProxy)
2. Start ShardingProxy ([Related introduction](/en/quick-start/shardingsphere-proxy-quick-start/))

### Create a distributed database and sharding tables

1. Connect to ShardingProxy
2. Create a distributed database

```sql
CREATE DATABASE readwrite_splitting_db;
```

3. Use newly created database

```sql
USE readwrite_splitting_db;
```

4. Configure data source information

```sql
ADD RESOURCE write_ds (
HOST=127.0.0.1,
PORT=3306,
DB=ds_0,
USER=root,
PASSWORD=root
),read_ds (
HOST=127.0.0.1,
PORT=3307,
DB=ds_0,
USER=root,
PASSWORD=root
);
```

5. Create readwrite_splitting rule

```sql
CREATE READWRITE_SPLITTING RULE group_0 (
WRITE_RESOURCE=write_ds,
READ_RESOURCES(read_ds),
TYPE(NAME=random)
);
```

6. Alter readwrite_splitting rule

```sql
ALTER READWRITE_SPLITTING RULE group_0 (
WRITE_RESOURCE=write_ds,
READ_RESOURCES(read_ds),
TYPE(NAME=random,PROPERTIES(read_weight='2:0'))
)
```

7. Drop readwrite_splitting rule

```sql
DROP READWRITE_SPLITTING RULE group_0;
```

8. Drop resource

```sql
DROP RESOURCE write_ds,read_ds;
```

9. Drop distributed database

```sql
DROP DATABASE readwrite_splitting_db;
```

### Notice

1. Currently, `DROP DATABASE` will only remove the `logical distributed database`, not the user's actual database. 
2. `DROP TABLE` will delete all logical fragmented tables and actual tables in the database.
3. `CREATE DATABASE` will only create a `logical distributed database`, so users need to create actual databases in advance .

