+++
title = "Usage"
weight = 2
chapter = true
+++

This chapter will introduce how to use DistSQL to manage resources and rules in a distributed database.

## Pre-work

Use MySQL as example, can replace to other databases.

1. Start the MySQL service;
1. Create to be registered MySQL databases;
1. Create role and user in MySQL with creation permission for ShardingSphere-Proxy;
1. Start Zookeeper service;
1. Add `mode` and `authentication` configurations to `server.yaml`;
1. Start ShardingSphere-Proxy;
1. Use SDK or terminal connect to ShardingSphere-Proxy.

## Create Logic Database

1. Create logic database

```sql
CREATE DATABASE foo_db;
```

2. Use newly created logic database

```sql
USE foo_db;
```

## Resource Operation

More details please see concentrate rule examples.

## Rule Operation

More details please see concentrate rule examples.

### Notice

1. Currently, `DROP DATABASE` will only remove the `logical distributed database`, not the user's actual database;
1. `DROP TABLE` will delete all logical fragmented tables and actual tables in the database;
1. `CREATE DATABASE` will only create a `logical distributed database`, so users need to create actual databases in advance.
