+++
title = "Manual"
weight = 2
+++

## MySQL user guide

### Environment

Supported MySQL versions: 5.1.15 to 5.7.x.

### Authority required

1. Enable `binlog`

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

2. Grant Replication-related permissions for MySQL account.

Run the following command and see whether the user has migration permission.
```
SHOW GRANTS FOR 'user';
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

### Complete procedure example

#### Prerequisite

1. Prepare the source database, table, and data in MySQL.

Sample: 

```sql
DROP DATABASE IF EXISTS migration_ds_0;
CREATE DATABASE migration_ds_0 DEFAULT CHARSET utf8;

USE migration_ds_0

CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id));

INSERT INTO t_order (order_id, user_id, status) VALUES (1,2,'ok'),(2,4,'ok'),(3,6,'ok'),(4,1,'ok'),(5,3,'ok'),(6,5,'ok');
```

2. Prepare the target database in MySQL.

Sample: 

```sql
DROP DATABASE IF EXISTS migration_ds_10;
CREATE DATABASE migration_ds_10 DEFAULT CHARSET utf8;

DROP DATABASE IF EXISTS migration_ds_11;
CREATE DATABASE migration_ds_11 DEFAULT CHARSET utf8;

DROP DATABASE IF EXISTS migration_ds_12;
CREATE DATABASE migration_ds_12 DEFAULT CHARSET utf8;
```

#### Procedure

1. Create a new logical database in proxy and configure resources and rules.

```sql
CREATE DATABASE sharding_db;

USE sharding_db

ADD RESOURCE ds_2 (
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
RESOURCES(ds_2,ds_3,ds_4),
SHARDING_COLUMN=order_id,
TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="6")),
KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME="snowflake"))
);
```

If you are migrating to a heterogeneous database, you need to execute the table-creation statements in proxy.

2. Configure the source resources in proxy.

```sql
ADD MIGRATION SOURCE RESOURCE ds_0 (
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

```sql
+-------------------------------------+---------+----------------------+--------+---------------------+-----------+
| id                                  | tables  | sharding_total_count | active | create_time         | stop_time |
+-------------------------------------+---------+----------------------+--------+---------------------+-----------+
| j015d4ee1b8a5e7f95df19babb2794395e8 | t_order | 1                    | true   | 2022-08-22 16:37:01 | NULL      |
+-------------------------------------+---------+----------------------+--------+---------------------+-----------+
```

5. View the data migration details.

```sql
SHOW MIGRATION STATUS 'j015d4ee1b8a5e7f95df19babb2794395e8';
+------+-------------+--------------------------+--------+-------------------------------+--------------------------+
| item | data_source | status                   | active | inventory_finished_percentage | incremental_idle_seconds |
+------+-------------+--------------------------+--------+-------------------------------+--------------------------+
| 0    | ds_0        | EXECUTE_INCREMENTAL_TASK | true   | 100                           | 141                      |
+------+-------------+--------------------------+--------+-------------------------------+--------------------------+
```

6. Verify data consistency.

```sql
CHECK MIGRATION 'j015d4ee1b8a5e7f95df19babb2794395e8' BY TYPE (NAME='CRC32_MATCH');
+------------+----------------------+----------------------+-----------------------+-------------------------+
| table_name | source_records_count | target_records_count | records_count_matched | records_content_matched |
+------------+----------------------+----------------------+-----------------------+-------------------------+
| t_order    | 6                    | 6                    | true                  | true                    |
+------------+----------------------+----------------------+-----------------------+-------------------------+
```

Data consistency check algorithm list:

```sql
SHOW MIGRATION CHECK ALGORITHMS;
+-------------+--------------------------------------------------------------+----------------------------+
| type        | supported_database_types                                     | description                |
+-------------+--------------------------------------------------------------+----------------------------+
| CRC32_MATCH | MySQL                                                        | Match CRC32 of records.    |
| DATA_MATCH  | SQL92,MySQL,MariaDB,PostgreSQL,openGauss,Oracle,SQLServer,H2 | Match raw data of records. |
+-------------+--------------------------------------------------------------+----------------------------+
```

If encrypt rule is configured in target proxy, then `CRC32_MATCH` could be not used.

7. Stop the job.

```sql
STOP MIGRATION 'j015d4ee1b8a5e7f95df19babb2794395e8';
```

8. Clear the job.

```sql
CLEAN MIGRATION 'j015d4ee1b8a5e7f95df19babb2794395e8';
```

## PostgreSQL user guide

### Environment

Supported PostgreSQL version: 9.4 or later.

### Authority required

1. Enable [test_decoding](https://www.postgresql.org/docs/9.4/test-decoding.html).

2. Modify WAL Configuration.

`postgresql.conf` configuration sample: 
```
wal_level = logical
max_wal_senders = 10
max_replication_slots = 10
max_connections = 600
```

Please refer to [Write Ahead Log](https://www.postgresql.org/docs/9.6/runtime-config-wal.html) and [Replication](https://www.postgresql.org/docs/9.6/runtime-config-replication.html ) for details.

3. Configure PostgreSQL and grant Proxy the replication permission.

`pg_hba.conf` instance configuration: 

```sql
host replication repl_acct 0.0.0.0/0 md5
```

Please refer to [The pg_hba.conf File](https://www.postgresql.org/docs/9.6/auth-pg-hba-conf.html) for details.

### Complete procedure example

#### Prerequisite

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

1. Create a new logical database in proxy and configure resources and rules.

```sql
CREATE DATABASE sharding_db;

\c sharding_db

ADD RESOURCE ds_2 (
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
RESOURCES(ds_2,ds_3,ds_4),
SHARDING_COLUMN=order_id,
TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="6")),
KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME="snowflake"))
);
```

If you are migrating to a heterogeneous database, you need to execute the table-creation statements in proxy.

2. Configure the source resources in proxy.

```sql
ADD MIGRATION SOURCE RESOURCE ds_0 (
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

Result sample: 

```sql
+-------------------------------------+---------+----------------------+--------+---------------------+-----------+
| id                                  | tables  | sharding_total_count | active | create_time         | stop_time |
+-------------------------------------+---------+----------------------+--------+---------------------+-----------+
| j015d4ee1b8a5e7f95df19babb2794395e8 | t_order | 1                    | true   | 2022-08-22 16:37:01 | NULL      |
+-------------------------------------+---------+----------------------+--------+---------------------+-----------+
```

5. View the data migration details.

```sql
SHOW MIGRATION STATUS 'j015d4ee1b8a5e7f95df19babb2794395e8';
+------+-------------+--------------------------+--------+-------------------------------+--------------------------+
| item | data_source | status                   | active | inventory_finished_percentage | incremental_idle_seconds |
+------+-------------+--------------------------+--------+-------------------------------+--------------------------+
| 0    | ds_0        | EXECUTE_INCREMENTAL_TASK | true   | 100                           | 141                      |
+------+-------------+--------------------------+--------+-------------------------------+--------------------------+
```

6. Verify data consistency.

```sql
CHECK MIGRATION 'j015d4ee1b8a5e7f95df19babb2794395e8' BY TYPE (NAME='DATA_MATCH');
+------------+----------------------+----------------------+-----------------------+-------------------------+
| table_name | source_records_count | target_records_count | records_count_matched | records_content_matched |
+------------+----------------------+----------------------+-----------------------+-------------------------+
| t_order    | 6                    | 6                    | true                  | true                    |
+------------+----------------------+----------------------+-----------------------+-------------------------+
```

7. Stop the job.

```sql
STOP MIGRATION 'j015d4ee1b8a5e7f95df19babb2794395e8';
```

8. Clear the job.

```sql
CLEAN MIGRATION 'j015d4ee1b8a5e7f95df19babb2794395e8';
```

## openGauss user guide

### Environment

Supported openGauss version: 2.0.1 to 3.0.0.

### Authority required

1. Modify WAL configuration.

`postgresql.conf` configuration sample:
```
wal_level = logical
max_wal_senders = 10
max_replication_slots = 10
wal_sender_timeout = 0
max_connections = 600
```

Please refer to [Write Ahead Log](https://opengauss.org/en/docs/2.0.1/docs/Developerguide/settings.html) and [Replication](https://opengauss.org/en/docs/2.0.1/docs/Developerguide/sending-server.html) for details.

2. Configure PostgreSQL and grant Proxy the replication permission.

`pg_hba.conf` instance configuration: 

```sql
host replication repl_acct 0.0.0.0/0 md5
```

Please refer to [Configuring Client Access Authentication](https://opengauss.org/en/docs/2.0.1/docs/Developerguide/configuring-client-access-authentication.html) and [Example: Logic Replication Code](https://opengauss.org/en/docs/2.0.1/docs/Developerguide/example-logic-replication-code.html) for details.

### Complete procedure example

#### Prerequisite

1. Prepare the source database, table, and data in openGauss.

```sql
DROP DATABASE IF EXISTS migration_ds_0;
CREATE DATABASE migration_ds_0;

\c migration_ds_0

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

1. Create a new logical database and configure resources and rules.

```sql
CREATE DATABASE sharding_db;

\c sharding_db

ADD RESOURCE ds_2 (
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

CREATE SHARDING TABLE RULE t_order(
RESOURCES(ds_2,ds_3,ds_4),
SHARDING_COLUMN=order_id,
TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="6")),
KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME="snowflake"))
);
```

If you are migrating to a heterogeneous database, you need to execute the table-creation statements in proxy.

2. Configure the source resources in proxy.

```sql
ADD MIGRATION SOURCE RESOURCE ds_2 (
    URL="jdbc:opengauss://127.0.0.1:5432/migration_ds_0",
    USER="gaussdb",
    PASSWORD="Root@123",
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

```sql
+-------------------------------------+---------+----------------------+--------+---------------------+-----------+
| id                                  | tables  | sharding_total_count | active | create_time         | stop_time |
+-------------------------------------+---------+----------------------+--------+---------------------+-----------+
| j015d4ee1b8a5e7f95df19babb2794395e8 | t_order | 1                    | true   | 2022-08-22 16:37:01 | NULL      |
+-------------------------------------+---------+----------------------+--------+---------------------+-----------+
```

5. View the data migration details.

```sql
SHOW MIGRATION STATUS 'j015d4ee1b8a5e7f95df19babb2794395e8';
+------+-------------+--------------------------+--------+-------------------------------+--------------------------+
| item | data_source | status                   | active | inventory_finished_percentage | incremental_idle_seconds |
+------+-------------+--------------------------+--------+-------------------------------+--------------------------+
| 0    | ds_0        | EXECUTE_INCREMENTAL_TASK | true   | 100                           | 141                      |
+------+-------------+--------------------------+--------+-------------------------------+--------------------------+
```

6. Verify data consistency.

```sql
CHECK MIGRATION 'j015d4ee1b8a5e7f95df19babb2794395e8' BY TYPE (NAME='DATA_MATCH');
+------------+----------------------+----------------------+-----------------------+-------------------------+
| table_name | source_records_count | target_records_count | records_count_matched | records_content_matched |
+------------+----------------------+----------------------+-----------------------+-------------------------+
| t_order    | 6                    | 6                    | true                  | true                    |
+------------+----------------------+----------------------+-----------------------+-------------------------+
```

7. Stop the job.

```sql
STOP MIGRATION 'j015d4ee1b8a5e7f95df19babb2794395e8';
```

8. Clear the job.

```
CLEAN MIGRATION 'j015d4ee1b8a5e7f95df19babb2794395e8';
```
