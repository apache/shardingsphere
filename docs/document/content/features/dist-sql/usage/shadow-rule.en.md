+++
title = "Shadow"
weight = 5
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

```SQL
CREATE DATABASE shadow_db;
```

3. Use newly created database

```SQL
USE shadow_db;
```

4. Configure data source information

```SQL
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

5. Create shadow rule

```SQL
CREATE SHADOW RULE group_0(
SOURCE=ds_0,
SHADOW=ds_1,
t_order((simple_note_algorithm, TYPE(NAME=SIMPLE_NOTE, PROPERTIES("shadow"="true", foo="bar"))),(TYPE(NAME=COLUMN_REGEX_MATCH, PROPERTIES("operation"="insert","column"="user_id", "regex"='[1]')))), 
t_order_item((TYPE(NAME=SIMPLE_NOTE, PROPERTIES("shadow"="true", "foo"="bar")))));
```

6. Alter shadow rule

```SQL
ALTER SHADOW RULE group_0(
SOURCE=ds_0,
SHADOW=ds_2,
t_order_item((TYPE(NAME=SIMPLE_NOTE, PROPERTIES("shadow"="true", "foo"="bar")))));
```

7. Drop shadow rule

```SQL
DROP SHADOW RULE group_0;
```

8. Drop resource

```SQL
DROP RESOURCE ds_0,ds_1,ds_2;
```

9. Drop distributed database

```SQL
DROP DATABASE shadow_db;
```

### Notice

1. Currently, `DROP DATABASE` will only remove the `logical distributed database`, not the user's actual database. 
2. `DROP TABLE` will delete all logical fragmented tables and actual tables in the database.
3. `CREATE DATABASE` will only create a `logical distributed database`, so users need to create actual databases in advance .
