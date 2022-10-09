+++
title = "DistSQL"
weight = 3
chapter = true
+++

This chapter will introduce the detailed syntax of DistSQL.

## Definition

DistSQL (Distributed SQL) is Apache ShardingSphere's specific SQL, providing additional operation capabilities compared to standard SQL.

Flexible rule configuration and resource management & control capabilities are one of the characteristics of Apache ShardingSphere.

When using 4.x and earlier versions, developers can operate data just like using a database, but they need to configure resources and rules through YAML file (or registry center). However, the YAML file format and the changes brought by using the registry center made it unfriendly to DBAs.

Starting from version 5.x, DistSQL enables users to operate Apache ShardingSphere just like a database, transforming it from a framework and middleware for developers to a database product for DBAs.

## Related Concepts

DistSQL is divided into RDL, RQL, RAL and RUL.

### RDL

Resource & Rule Definition Language, is responsible for the definition of resources and rules.

### RQL

Resource & Rule Query Language, is responsible for the query of resources and rules.

### RAL

Resource & Rule Administration Language, is responsible for hint, circuit breaker, configuration import and export, scaling control and other management functions.

### RUL

Resource & Rule Utility Language, is responsible for SQL parsing, SQL formatting, preview execution plan, etc.

## Impact on the System

### Before

Before having DistSQL, users used SQL to operate data while using YAML configuration files to manage ShardingSphere, as shown below:

![Before](https://shardingsphere.apache.org/document/current/img/distsql/before.png)

At that time, users faced the following problems:
- Different types of clients are required to operate data and manage ShardingSphere configuration.
- Multiple logical databases require multiple YAML files.
- Editing a YAML file requires writing permissions.
- Need to restart ShardingSphere after editing YAML.

### After

With the advent of DistSQL, the operation of ShardingSphere has also changed:

![After](https://shardingsphere.apache.org/document/current/img/distsql/after.png)

Now, the user experience has been greatly improved:
- Uses the same client to operate data and ShardingSphere configuration.
- No need for additional YAML files, and the logical databases are managed through DistSQL.
- Editing permissions for files are no longer required, and configuration is managed through DistSQL.
- Configuration changes take effect in real-time without restarting ShardingSphere.

## Limitations

DistSQL can be used only with ShardingSphere-Proxy, not with ShardingSphere-JDBC for now.

## How it works

Like standard SQL, DistSQL is recognized by the parsing engine of ShardingSphere. It converts the input statement into an abstract syntax tree and then generates the `Statement` corresponding to each grammar, which is processed by the appropriate `Handler`.

![Overview](https://shardingsphere.apache.org/document/current/img/distsql/overview.png)

## Related References

[User Manual: DistSQL](/en/user-manual/shardingsphere-proxy/distsql/)
