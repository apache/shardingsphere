+++
title = "SQL Hint"
weight = 3
chapter = true
+++

## Background

At present, most relational databases basically provide SQL Hint as a supplement to SQL syntax. SQL Hint allows users to intervene in the execution process of SQL through the built-in Hint syntax of the database, to complete some special functions or realize optimization of SQL execution.
ShardingSphere also provides SQL Hint syntax, allowing users to perform force route for sharding and read-write splitting, and data source pass through.

## Use specification

The SQL Hint syntax of ShardingSphere needs to be written in SQL in the form of comments. The SQL Hint syntax format only supports `/* */` temporarily, and the Hint content needs to start with `SHARDINGSPHERE_HINT:`, and then define the attribute key/value pairs corresponding to different features, separated by commas when there are multiple attributes.
The SQL Hint syntax format of ShardingSphere is as follows:

```sql
/* SHARDINGSPHERE_HINT: {key} = {value}, {key} = {value} */ SELECT * FROM t_order;
```

If you use the MySQL client to connect, you need to add the `-c` option to retain comments, and the client defaults to `--skip-comments` to filter comments.

## Parameters

The following attributes can be defined in ShardingSphere SQL Hint. In order to be compatible with the lower version SQL Hint syntax, the attributes defined in the alias can also be used:

| *Name*                      | *Alias*               | *Data Type* | *Description*                                                             | *Default Value* |
|-----------------------------|-----------------------|-------------|---------------------------------------------------------------------------|-----------------|
| SHARDING_DATABASE_VALUE (?) | shardingDatabaseValue | Comparable  | Database sharding value, used when config Hint sharding strategy          | -               |
| SHARDING_TABLE_VALUE (?)    | shardingTableValue    | Comparable  | Table sharding value, used when config Hint sharding strategy             | -               |
| WRITE_ROUTE_ONLY (?)        | writeRouteOnly        | boolean     | Route to the write datasource when use readwrite-splitting                | false           |
| DATA_SOURCE_NAME (?)        | dataSourceName        | String      | Data source pass through, route SQL directly to the specified data source | -               |
| SKIP_SQL_REWRITE (?)        | skipSQLRewrite        | boolean     | Skip the SQL rewrite phase                                                | false           |
| DISABLE_AUDIT_NAMES (?)     | disableAuditNames     | String      | Disable the specified SQL audit algorithm                                 | -               |
| SHADOW (?)                  | shadow                | boolean     | Route to the shadow datasource when use shadow                            | false           |


## SQL Hint

### Sharding

The optional attributes of sharding SQL Hint include:

- `{table}.SHARDING_DATABASE_VALUE`: used to add data source sharding value corresponding to the `{table}` table, multiple attributes are separated by commas;
- `{table}.SHARDING_TABLE_VALUE`: used to add table sharding value corresponding to the `{table}` table, multiple attributes are separated by commas.

> In the case of only database sharding, when forcing routing to a certain datasource, you can use the `SHARDING_DATABASE_VALUE` method to set the sharding value without specifying `{table}`.

An example of using the SQL Hint of sharding:

```sql
/* SHARDINGSPHERE_HINT: t_order.SHARDING_DATABASE_VALUE=1, t_order.SHARDING_TABLE_VALUE=1 */ SELECT * FROM t_order;
```

### ReadwriteSplitting

The optional attribute of read-write splitting SQL Hint is `WRITE_ROUTE_ONLY`, and `true` means that the current SQL is forced to be routed to write datasource for execution.

An example of using the SQL Hint for read-write splitting:

```sql
/* SHARDINGSPHERE_HINT: WRITE_ROUTE_ONLY=true */ SELECT * FROM t_order;
```

### DataSource Pass Through

The optional attribute of datasource pass through SQL Hint is `DATA_SOURCE_NAME`, which needs to specify the name of the data source registered in the ShardingSphere logic database.

An example of using the SQL Hint of data source pass through:

```sql
/* SHARDINGSPHERE_HINT: DATA_SOURCE_NAME=ds_0 */ SELECT * FROM t_order;
```

### SKIP SQL REWRITE

The optional attribute of skip SQL rewriting SQL Hint is `SKIP_SQL_REWRITE`, and `true` means skipping the current SQL rewriting stage.

An example of skipping SQL rewrite SQL Hint:

```sql
/* SHARDINGSPHERE_HINT: SKIP_SQL_REWRITE=true */ SELECT * FROM t_order;
```

### DISABLE SQL AUDIT

The optional attribute of disable SQL audit is `DISABLE_AUDIT_NAMES`, you need to specify names of SQL audit algorithm that needs to be disabled, and multiple SQL audit algorithms need to be separated by commas.

An example of disable sql audit SQL Hint:

```sql
/* SHARDINGSPHERE_HINT: DISABLE_AUDIT_NAMES=sharding_key_required_auditor */ SELECT * FROM t_order;
```

### SHADOW

The optional attribute of the shadow database pressure test SQL Hint is `SHADOW`, and `true` means that the current SQL will be routed to the shadow database data source for execution.

An example of using shadow SQL Hint:

```sql
/* SHARDINGSPHERE_HINT: SHADOW=true */ SELECT * FROM t_order;
```
