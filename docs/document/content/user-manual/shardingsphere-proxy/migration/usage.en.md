+++
title = "Manual"
weight = 2
+++

## MySQL user guide

### Environment

Supported MySQL versions: 5.1.15 to 8.0.x.

### Authority required

1. Enable `binlog` in source

MySQL 5.7 `my.cnf` configuration sample:

```
[mysqld]
server-id=1
log-bin=mysql-bin
binlog-format=row
binlog-row-image=full
max_connections=600
```

Run the following command and check whether `binlog` is enabled.
```
show variables like '%log_bin%';
show variables like '%binlog%';
```

If the following information is displayed, binlog is enabled.
```
+-----------------------------------------+---------------------------------------+
| Variable_name                           | Value                                 |
+-----------------------------------------+---------------------------------------+
| log_bin                                 | ON                                    |
| binlog_format                           | ROW                                   |
| binlog_row_image                        | FULL                                  |
+-----------------------------------------+---------------------------------------+
```

2. Grant Replication-related permissions for source MySQL account.

Run the following command to check whether the user has migration permission.
```
SHOW GRANTS FOR 'migration_user';
```

Result sample: 
```
+------------------------------------------------------------------------------+
|Grants for ${username}@${host}                                                |
+------------------------------------------------------------------------------+
|GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO ${username}@${host}     |
|.......                                                                       |
+------------------------------------------------------------------------------+
```

3. Grant DDL DML permissions for MySQL account

Source MySQL account needs SELECT permission. Example:
```sql
GRANT SELECT ON migration_ds_0.* TO `migration_user`@`%`;
```

Target MySQL account needs part of DDL and all DML permissions. Example:
```sql
GRANT CREATE, DROP, INDEX, SELECT, INSERT, UPDATE, DELETE ON *.* TO `migration_user`@`%`;
```

Please refer to [MySQL GRANT](https://dev.mysql.com/doc/refman/8.0/en/grant.html)

### Complete procedure example

#### Requirements

1. Prepare the source database, table, and data in MySQL.

```sql
DROP DATABASE IF EXISTS migration_ds_0;
CREATE DATABASE migration_ds_0 DEFAULT CHARSET utf8;

USE migration_ds_0

CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));

INSERT INTO t_order (order_id, user_id, status) VALUES (1,2,'ok'),(2,4,'ok'),(3,6,'ok'),(4,1,'ok'),(5,3,'ok'),(6,5,'ok');
```

2. Prepare the target database in MySQL.

```sql
DROP DATABASE IF EXISTS migration_ds_10;
CREATE DATABASE migration_ds_10 DEFAULT CHARSET utf8;

DROP DATABASE IF EXISTS migration_ds_11;
CREATE DATABASE migration_ds_11 DEFAULT CHARSET utf8;

DROP DATABASE IF EXISTS migration_ds_12;
CREATE DATABASE migration_ds_12 DEFAULT CHARSET utf8;
```

#### Procedure

1. Create a new logical database in proxy and configure storage units and rules.

```sql
CREATE DATABASE sharding_db;

USE sharding_db

REGISTER STORAGE UNIT ds_2 (
    URL="jdbc:mysql://127.0.0.1:3306/migration_ds_10?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
), ds_3 (
    URL="jdbc:mysql://127.0.0.1:3306/migration_ds_11?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
), ds_4 (
    URL="jdbc:mysql://127.0.0.1:3306/migration_ds_12?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
);

CREATE SHARDING TABLE RULE t_order(
STORAGE_UNITS(ds_2,ds_3,ds_4),
SHARDING_COLUMN=order_id,
TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="6")),
KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME="snowflake"))
);
```

If you are migrating to a heterogeneous database, you need to execute the table-creation statements in proxy.

2. Configure the source storage units in proxy.

```sql
REGISTER MIGRATION SOURCE STORAGE UNIT ds_0 (
    URL="jdbc:mysql://127.0.0.1:3306/migration_ds_0?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
);
```

3. Start data migration.

```sql
MIGRATE TABLE ds_0.t_order INTO t_order;
```

Or you can specify a target logical database.

```sql
MIGRATE TABLE ds_0.t_order INTO sharding_db.t_order;
```

4. Check the data migration job list.

```sql
SHOW MIGRATION LIST;
```

Result example:
```
+--------------------------------------------+--------------+----------------+--------+---------------------+-----------+
| id                                         | tables       | job_item_count | active | create_time         | stop_time |
+--------------------------------------------+--------------+----------------+--------+---------------------+-----------+
| j0102p00002333dcb3d9db141cef14bed6fbf1ab54 | ds_0.t_order | 1              | true   | 2023-09-20 14:41:32 | NULL      |
+--------------------------------------------+--------------+----------------+--------+---------------------+-----------+
```

5. View the data migration details.

```sql
SHOW MIGRATION STATUS 'j0102p00002333dcb3d9db141cef14bed6fbf1ab54';
```

Result example:
```
+------+-------------+--------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
| item | data_source | tables       | status                   | active | processed_records_count | inventory_finished_percentage | incremental_idle_seconds | error_message |
+------+-------------+--------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
| 0    | ds_0        | ds_0.t_order | EXECUTE_INCREMENTAL_TASK | true   | 6                       | 100                           |                          |               |
+------+-------------+--------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
```

6. Verify data consistency.

```sql
CHECK MIGRATION 'j0102p00002333dcb3d9db141cef14bed6fbf1ab54' BY TYPE (NAME='DATA_MATCH');
```

Data consistency check algorithm list:

```sql
SHOW MIGRATION CHECK ALGORITHMS;
```

Result example:
```
+-------------+--------------+--------------------------------------------------------------+----------------------------+
| type        | type_aliases | supported_database_types                                     | description                |
+-------------+--------------+--------------------------------------------------------------+----------------------------+
| CRC32_MATCH |              | MySQL,MariaDB,H2                                             | Match CRC32 of records.    |
| DATA_MATCH  |              | SQL92,MySQL,PostgreSQL,openGauss,Oracle,SQLServer,MariaDB,H2 | Match raw data of records. |
+-------------+--------------+--------------------------------------------------------------+----------------------------+
```

If encrypt rule is configured in target proxy, then `DATA_MATCH` could be used.

If you are migrating to a heterogeneous database, then `DATA_MATCH` could be used.

Query data consistency check progress:
```sql
SHOW MIGRATION CHECK STATUS 'j0102p00002333dcb3d9db141cef14bed6fbf1ab54';
```

Result example:
```
+--------------+--------+---------------------+--------+-------------------------------+-----------------------------+--------------------------+-------------------------+-------------------------+------------------+----------------+-----------------+---------------+
| tables       | result | check_failed_tables | active | inventory_finished_percentage | inventory_remaining_seconds | incremental_idle_seconds | check_begin_time        | check_end_time          | duration_seconds | algorithm_type | algorithm_props | error_message |
+--------------+--------+---------------------+--------+-------------------------------+-----------------------------+--------------------------+-------------------------+-------------------------+------------------+----------------+-----------------+---------------+
| ds_0.t_order | true   |                     | false  | 100                           | 0                           |                          | 2023-09-20 14:45:31.992 | 2023-09-20 14:45:33.519 | 1                | DATA_MATCH     |                 |               |
+--------------+--------+---------------------+--------+-------------------------------+-----------------------------+--------------------------+-------------------------+-------------------------+------------------+----------------+-----------------+---------------+
```

7. Commit the job.

```sql
COMMIT MIGRATION 'j0102p00002333dcb3d9db141cef14bed6fbf1ab54';
```

Please refer to [RAL#Migration](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/#migration) for more details.

## PostgreSQL user guide

### Environment

Supported PostgreSQL version: 9.4 or later.

### Authority required

1. Enable [test_decoding](https://www.postgresql.org/docs/9.4/test-decoding.html) in source.

2. Modify WAL configuration in source.

`postgresql.conf` configuration sample:
```
wal_level = logical
max_wal_senders = 10
max_replication_slots = 10
wal_sender_timeout = 0
max_connections = 600
```

Please refer to [Write Ahead Log](https://www.postgresql.org/docs/9.6/runtime-config-wal.html) and [Replication](https://www.postgresql.org/docs/9.6/runtime-config-replication.html ) for details.

3. Grant replication permission for source PostgreSQL account.

`pg_hba.conf` instance configuration:
```
host replication repl_acct 0.0.0.0/0 md5
```

Please refer to [The pg_hba.conf File](https://www.postgresql.org/docs/9.6/auth-pg-hba-conf.html) for details.

4. Grant DDL DML permissions for PostgreSQL account.

If you are using a non-super admin account for migration, you need to GRANT CREATE and CONNECT privileges on the database used for migration.

```sql
GRANT CREATE, CONNECT ON DATABASE migration_ds_0 TO migration_user;
```

The account also needs to have access to the migrated tables and schema. Take the t_order table under test schema as an example. 

```sql
\c migration_ds_0

GRANT USAGE ON SCHEMA test TO GROUP migration_user;
GRANT SELECT ON TABLE test.t_order TO migration_user;
```

PostgreSQL has the concept of OWNER, and if the account is the OWNER of a database, SCHEMA, or table, the relevant steps can be omitted.

Please refer to [PostgreSQL GRANT](https://www.postgresql.org/docs/current/sql-grant.html)

### Complete procedure example

#### Requirements

1. Prepare the source database, table, and data in PostgreSQL.

```sql
DROP DATABASE IF EXISTS migration_ds_0;
CREATE DATABASE migration_ds_0;

\c migration_ds_0

CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));

INSERT INTO t_order (order_id, user_id, status) VALUES (1,2,'ok'),(2,4,'ok'),(3,6,'ok'),(4,1,'ok'),(5,3,'ok'),(6,5,'ok');
```

2. Prepare the target database in PostgreSQL.

```sql
DROP DATABASE IF EXISTS migration_ds_10;
CREATE DATABASE migration_ds_10;

DROP DATABASE IF EXISTS migration_ds_11;
CREATE DATABASE migration_ds_11;

DROP DATABASE IF EXISTS migration_ds_12;
CREATE DATABASE migration_ds_12;
```

#### Procedure

1. Create a new logical database in proxy and configure storage units and rules.

```sql
CREATE DATABASE sharding_db;

\c sharding_db

REGISTER STORAGE UNIT ds_2 (
    URL="jdbc:postgresql://127.0.0.1:5432/migration_ds_10",
    USER="postgres",
    PASSWORD="root",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
), ds_3 (
    URL="jdbc:postgresql://127.0.0.1:5432/migration_ds_11",
    USER="postgres",
    PASSWORD="root",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
), ds_4 (
    URL="jdbc:postgresql://127.0.0.1:5432/migration_ds_12",
    USER="postgres",
    PASSWORD="root",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
);

CREATE SHARDING TABLE RULE t_order(
STORAGE_UNITS(ds_2,ds_3,ds_4),
SHARDING_COLUMN=order_id,
TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="6")),
KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME="snowflake"))
);
```

If you are migrating to a heterogeneous database, you need to execute the table-creation statements in proxy.

2. Configure the source storage units in proxy.

```sql
REGISTER MIGRATION SOURCE STORAGE UNIT ds_0 (
    URL="jdbc:postgresql://127.0.0.1:5432/migration_ds_0",
    USER="postgres",
    PASSWORD="root",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
);
```

3. Enable data migration.

```sql
MIGRATE TABLE ds_0.t_order INTO t_order;
```

Or you can specify a target logical database.

```sql
MIGRATE TABLE ds_0.t_order INTO sharding_db.t_order;
```

Or you can specify a source schema name.

```sql
MIGRATE TABLE ds_0.public.t_order INTO sharding_db.t_order;
```

4. Check the data migration job list.

```sql
SHOW MIGRATION LIST;
```

Result example:
```
+--------------------------------------------+--------------+----------------+--------+---------------------+-----------+
| id                                         | tables       | job_item_count | active | create_time         | stop_time |
+--------------------------------------------+--------------+----------------+--------+---------------------+-----------+
| j0102p00002333dcb3d9db141cef14bed6fbf1ab54 | ds_0.t_order | 1              | true   | 2023-09-20 14:41:32 | NULL      |
+--------------------------------------------+--------------+----------------+--------+---------------------+-----------+
```

5. View the data migration details.

```sql
SHOW MIGRATION STATUS 'j0102p00002333dcb3d9db141cef14bed6fbf1ab54';
```

Result example:
```
+------+-------------+--------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
| item | data_source | tables       | status                   | active | processed_records_count | inventory_finished_percentage | incremental_idle_seconds | error_message |
+------+-------------+--------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
| 0    | ds_0        | ds_0.t_order | EXECUTE_INCREMENTAL_TASK | true   | 6                       | 100                           |                          |               |
+------+-------------+--------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
```

6. Verify data consistency.

```
CHECK MIGRATION 'j0102p00002333dcb3d9db141cef14bed6fbf1ab54';
Query OK, 0 rows affected (0.09 sec)
```

Query data consistency check progress:
```sql
SHOW MIGRATION CHECK STATUS 'j0102p00002333dcb3d9db141cef14bed6fbf1ab54';
```

Result example:
```
+--------------+--------+---------------------+--------+-------------------------------+-----------------------------+--------------------------+-------------------------+-------------------------+------------------+----------------+-----------------+---------------+
| tables       | result | check_failed_tables | active | inventory_finished_percentage | inventory_remaining_seconds | incremental_idle_seconds | check_begin_time        | check_end_time          | duration_seconds | algorithm_type | algorithm_props | error_message |
+--------------+--------+---------------------+--------+-------------------------------+-----------------------------+--------------------------+-------------------------+-------------------------+------------------+----------------+-----------------+---------------+
| ds_0.t_order | true   |                     | false  | 100                           | 0                           |                          | 2023-09-20 14:45:31.992 | 2023-09-20 14:45:33.519 | 1                | DATA_MATCH     |                 |               |
+--------------+--------+---------------------+--------+-------------------------------+-----------------------------+--------------------------+-------------------------+-------------------------+------------------+----------------+-----------------+---------------+
```

7. Commit the job.

```sql
COMMIT MIGRATION 'j0102p00002333dcb3d9db141cef14bed6fbf1ab54';
```

Please refer to [RAL#Migration](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/#migration) for more details.

## openGauss user guide

### Environment

Supported openGauss version: 2.0.1 to 3.0.0.

### Authority required

1. Modify WAL configuration in source.

`postgresql.conf` configuration sample:
```
wal_level = logical
max_wal_senders = 10
max_replication_slots = 10
wal_sender_timeout = 0
max_connections = 600
```

Please refer to [Write Ahead Log](https://docs.opengauss.org/en/docs/2.0.1/docs/Developerguide/settings.html) and [Replication](https://docs.opengauss.org/en/docs/2.0.1/docs/Developerguide/sending-server.html) for details.

2. Grant replication permission for source openGauss account.

`pg_hba.conf` instance configuration:
```
host replication repl_acct 0.0.0.0/0 md5
```

Please refer to [Configuring Client Access Authentication](https://docs.opengauss.org/en/docs/2.0.1/docs/Developerguide/configuring-client-access-authentication.html) and [Example: Logic Replication Code](https://docs.opengauss.org/en/docs/2.0.1/docs/Developerguide/example-logic-replication-code.html) for details.

3. Grant DDL DML permissions for openGauss account.

If you are using a non-super admin account for migration, you need to GRANT CREATE and CONNECT privileges on the database used for migration.

```sql
GRANT CREATE, CONNECT ON DATABASE migration_ds_0 TO migration_user;
```

The account also needs to have access to the migrated tables and schema. Take the t_order table under test schema as an example. 

```sql
\c migration_ds_0

GRANT USAGE ON SCHEMA test TO GROUP migration_user;
GRANT SELECT ON TABLE test.t_order TO migration_user;
```

openGauss has the concept of OWNER, and if the account is the OWNER of a database, SCHEMA, or table, the relevant steps can be omitted.

openGauss does not allow normal accounts to operate in public schema, so if the migrated table is in public schema, you need to authorize additional.

Please refer to [openGauss GRANT](https://docs.opengauss.org/en/docs/2.0.1/docs/Developerguide/grant.html)

```sql
GRANT ALL PRIVILEGES TO migration_user;
```

### Complete procedure example

#### Requirements

1. Prepare the source database, table, and data.

1.1. Isomorphic database.

```sql
DROP DATABASE IF EXISTS migration_ds_0;
CREATE DATABASE migration_ds_0;

\c migration_ds_0

CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));

INSERT INTO t_order (order_id, user_id, status) VALUES (1,2,'ok'),(2,4,'ok'),(3,6,'ok'),(4,1,'ok'),(5,3,'ok'),(6,5,'ok');
```

1.2. Heterogeneous database.

MySQL example:
```sql
DROP DATABASE IF EXISTS migration_ds_0;
CREATE DATABASE migration_ds_0 DEFAULT CHARSET utf8;

USE migration_ds_0

CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));

INSERT INTO t_order (order_id, user_id, status) VALUES (1,2,'ok'),(2,4,'ok'),(3,6,'ok'),(4,1,'ok'),(5,3,'ok'),(6,5,'ok');
```

2. Prepare the target database in openGauss.

```sql
DROP DATABASE IF EXISTS migration_ds_10;
CREATE DATABASE migration_ds_10;

DROP DATABASE IF EXISTS migration_ds_11;
CREATE DATABASE migration_ds_11;

DROP DATABASE IF EXISTS migration_ds_12;
CREATE DATABASE migration_ds_12;
```

#### Procedure

1. Create a new logical database and configure storage units and rules.

1.1. Create logic database.

```sql
CREATE DATABASE sharding_db;

\c sharding_db
```

1.2. Register storage units.

```sql
REGISTER STORAGE UNIT ds_2 (
    URL="jdbc:opengauss://127.0.0.1:5432/migration_ds_10",
    USER="gaussdb",
    PASSWORD="Root@123",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
), ds_3 (
    URL="jdbc:opengauss://127.0.0.1:5432/migration_ds_11",
    USER="gaussdb",
    PASSWORD="Root@123",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
), ds_4 (
    URL="jdbc:opengauss://127.0.0.1:5432/migration_ds_12",
    USER="gaussdb",
    PASSWORD="Root@123",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
);
```

1.3. Create sharding table rule.

```sql
CREATE SHARDING TABLE RULE t_order(
STORAGE_UNITS(ds_2,ds_3,ds_4),
SHARDING_COLUMN=order_id,
TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="6")),
KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME="snowflake"))
);
```

1.4. Create target table.

If you are migrating to a heterogeneous database, you need to execute the table-creation statements in proxy.

MySQL example:
```sql
CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));
```

2. Configure the source storage units in proxy.

2.1. Isomorphic database.

```sql
REGISTER MIGRATION SOURCE STORAGE UNIT ds_0 (
    URL="jdbc:opengauss://127.0.0.1:5432/migration_ds_0",
    USER="gaussdb",
    PASSWORD="Root@123",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
);
```

2.2. Heterogeneous database.

MySQL example:
```sql
REGISTER MIGRATION SOURCE STORAGE UNIT ds_0 (
    URL="jdbc:mysql://127.0.0.1:3306/migration_ds_0?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
);
```

3. Enable data migration.

```sql
MIGRATE TABLE ds_0.t_order INTO t_order;
```

Or you can specify a target logical database.

```sql
MIGRATE TABLE ds_0.t_order INTO sharding_db.t_order;
```

Or you can specify a source schema name.

```sql
MIGRATE TABLE ds_0.public.t_order INTO sharding_db.t_order;
```

4. Check the data migration job list.

```sql
SHOW MIGRATION LIST;
```

Result example:
```
+--------------------------------------------+--------------+----------------+--------+---------------------+-----------+
| id                                         | tables       | job_item_count | active | create_time         | stop_time |
+--------------------------------------------+--------------+----------------+--------+---------------------+-----------+
| j0102p00002333dcb3d9db141cef14bed6fbf1ab54 | ds_0.t_order | 1              | true   | 2023-09-20 14:41:32 | NULL      |
+--------------------------------------------+--------------+----------------+--------+---------------------+-----------+
```

5. View the data migration details.

```sql
SHOW MIGRATION STATUS 'j0102p00002333dcb3d9db141cef14bed6fbf1ab54';
```

Result example:
```
+------+-------------+--------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
| item | data_source | tables       | status                   | active | processed_records_count | inventory_finished_percentage | incremental_idle_seconds | error_message |
+------+-------------+--------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
| 0    | ds_0        | ds_0.t_order | EXECUTE_INCREMENTAL_TASK | true   | 6                       | 100                           |                          |               |
+------+-------------+--------------+--------------------------+--------+-------------------------+-------------------------------+--------------------------+---------------+
```

6. Verify data consistency.

```
CHECK MIGRATION 'j0102p00002333dcb3d9db141cef14bed6fbf1ab54';
Query OK, 0 rows affected (0.09 sec)
```

Query data consistency check progress:
```sql
SHOW MIGRATION CHECK STATUS 'j0102p00002333dcb3d9db141cef14bed6fbf1ab54';
```

Result example:
```
+--------------+--------+---------------------+--------+-------------------------------+-----------------------------+--------------------------+-------------------------+-------------------------+------------------+----------------+-----------------+---------------+
| tables       | result | check_failed_tables | active | inventory_finished_percentage | inventory_remaining_seconds | incremental_idle_seconds | check_begin_time        | check_end_time          | duration_seconds | algorithm_type | algorithm_props | error_message |
+--------------+--------+---------------------+--------+-------------------------------+-----------------------------+--------------------------+-------------------------+-------------------------+------------------+----------------+-----------------+---------------+
| ds_0.t_order | true   |                     | false  | 100                           | 0                           |                          | 2023-09-20 14:45:31.992 | 2023-09-20 14:45:33.519 | 1                | DATA_MATCH     |                 |               |
+--------------+--------+---------------------+--------+-------------------------------+-----------------------------+--------------------------+-------------------------+-------------------------+------------------+----------------+-----------------+---------------+
```

7. Commit the job.

```sql
COMMIT MIGRATION 'j0102p00002333dcb3d9db141cef14bed6fbf1ab54';
```

Please refer to [RAL#Migration](/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/#migration) for more details.
