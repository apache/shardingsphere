+++ 
title = "Your Guide to DistSQL Cluster Governance Capability——Apache ShardingSphere Feature Update"
weight = 57
chapter = true 
+++

Since Apache ShardingSphere 5.0.0-Beta version included DistSQL, it made the project increasingly loved by developers and Ops teams for its advantages such as dynamic effects, no restart, and elegant syntax close to standard SQL.

With upgrades to 5.0.0 and 5.1.0, the ShardingSphere community has once again added abundant syntax to DistSQL, bringing more practical features.

In this post, the community co-authors will share the latest functions of DistSQL from the perspective of “cluster governance”.

## ShardingSphere Cluster
In a typical cluster composed of ShardingSphere-Proxy, there are multiple `compute nodes` and storage nodes, as shown in the figure below:

To make it easier to understand, in ShardingSphere, we refer to Proxy as a compute node and Proxy-managed distributed database resources (such as `ds_0`, `ds_1`) as `resources` or `storage nodes`.
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/i233gunx5u6tr3xej822.png)
 

Multiple Proxy or compute nodes are connected to the same register center, sharing configuration, and rules, and can sense each other’s online status.

These compute nodes also share the underlying storage nodes, so they can perform read and write operations to the storage nodes at the same time. The user application is connected to any compute node and can perform equivalent operations.

Through this cluster architecture, you can quickly scale Proxy horizontally when compute resources are insufficient, reducing the risk of a single point of failure and improving system availability.

The load balancing mechanism can also be added between application and compute node.

**Compute Node Governance**
Compute node governance is suitable for Cluster mode. For more information about the ShardingSphere modes, please see [Your Detailed Guide to Apache ShardingSphere’s Operating Modes](https://medium.com/codex/your-detailed-guide-to-apache-shardingspheres-operating-modes-e50df1ee56e4).

**Cluster Preparation**
Take a standalone simulation of three Proxy compute nodes as an example. To use the mode, follow the configuration below:

```yaml
mode:
  type: Cluster
  repository:
    type: ZooKeeper
    props:
      namespace: governance_ds
      server-lists: localhost:2181
      retryIntervalMilliseconds: 500
      timeToLiveSeconds: 60
      maxRetries: 3
      operationTimeoutMilliseconds: 500
  overwrite: false
```
Execute the bootup command separately:

```bash
sh %SHARDINGSPHERE_PROXY_HOME%/bin/start.sh 3307
sh %SHARDINGSPHERE_PROXY_HOME%/bin/start.sh 3308
sh %SHARDINGSPHERE_PROXY_HOME%/bin/start.sh 3309
```
After the three Proxy instances are successfully started, the compute node cluster is ready.

`SHOW INSTANCE LIST`

Use the client to connect to any compute node, such as 3307:

```bash
mysql -h 127.0.0.1 -P 3307 -u root -p
```

View the list of instances:

```
mysql> SHOW INSTANCE LIST;
+----------------+-----------+------+---------+
| instance_id    | host      | port | status  |
+----------------+-----------+------+---------+
| 10.7.5.35@3309 | 10.7.5.35 | 3309 | enabled |
| 10.7.5.35@3308 | 10.7.5.35 | 3308 | enabled |
| 10.7.5.35@3307 | 10.7.5.35 | 3307 | enabled |
+----------------+-----------+------+---------+
```
The above fields mean:

- `instance_id `: The id of the instance, which is currently composed of host and port.
- `Host` : host address.
- `Port` : port number.
- `Status` : the status of the instance enabled or disabled

`DISABLE INSTANCE`

`DISABLE INSTANCE` statement is used to set the specified compute node to a disabled state.

💡Note: 
the statement does not terminate the process of the target instance, but only virtually deactivates it.

`DISABLE INSTANCE` supports the following syntax forms:

```
DISABLE INSTANCE 10.7.5.35@3308;
#or
DISABLE INSTANCE IP=10.7.5.35, PORT=3308;
```
Example:

```
mysql> DISABLE INSTANCE 10.7.5.35@3308;
Query OK, 0 rows affected (0.02 sec)

mysql> SHOW INSTANCE LIST;
+----------------+-----------+------+----------+
| instance_id    | host      | port | status   | 
+----------------+-----------+------+----------+
| 10.7.5.35@3309 | 10.7.5.35 | 3309 | enabled  |
| 10.7.5.35@3308 | 10.7.5.35 | 3308 | disabled |
| 10.7.5.35@3307 | 10.7.5.35 | 3307 | enabled  |
+----------------+-----------+------+----------+
```
After executing the `DISABLE INSTANCE `statement, by querying again, you can see that the instance status of Port 3308 has been updated to `disabled` , indicating that the compute node has been disabled.

If there is a client connected to the `10.7.5.35@3308` , executing any SQL statement will prompt an exception:

`1000 - Circuit break mode is ON.`
💡Note: 
It is not allowed to disable the current compute node. If you send `10.7.5.35@3309` to `DISABLE INSTANCE 10.7.5.35@3309` , you will receive an exception prompt.

`ENABLE INSTANCE`

`ENABLE INSTANCE` statement is used to set the specified compute node to an enabled state. `ENABLE INSTANCE` supports the following syntax forms:

```
ENABLE INSTANCE 10.7.5.35@3308;
#or
ENABLE INSTANCE IP=10.7.5.35, PORT=3308;
```
Example:

```
mysql> SHOW INSTANCE LIST;
+----------------+-----------+------+----------+
| instance_id    | host      | port | status   | 
+----------------+-----------+------+----------+
| 10.7.5.35@3309 | 10.7.5.35 | 3309 | enabled  |
| 10.7.5.35@3308 | 10.7.5.35 | 3308 | disabled |
| 10.7.5.35@3307 | 10.7.5.35 | 3307 | enabled  |
+----------------+-----------+------+----------+

mysql> ENABLE INSTANCE 10.7.5.35@3308;
Query OK, 0 rows affected (0.01 sec)

mysql> SHOW INSTANCE LIST;
+----------------+-----------+------+----------+
| instance_id    | host      | port | status   | 
+----------------+-----------+------+----------+
| 10.7.5.35@3309 | 10.7.5.35 | 3309 | enabled  |
| 10.7.5.35@3308 | 10.7.5.35 | 3308 | enabled  |
| 10.7.5.35@3307 | 10.7.5.35 | 3307 | enabled  |
+----------------+-----------+------+----------+
```

After executing the `ENABLE INSTANCE` statement, you can query again and view that the instance state of Port 3308 has been restored to `enabled`.

## How to Manage Compute Node Parameters
In the previous article [Integrating SCTL into RAL — Making Apache ShardingSphere Perfect for Database Management](https://dzone.com/articles/integrating-sctl-into-distsqls-ral-making-apache-s), we explained the evolution of SCTL (ShardingSphere Control Language) to RAL (Resource & Rule Administration Language) and the new `SHOW VARIABLE` and `SET VARIABLE` syntax.

However, in 5.0.0-Beta, the `VARIABLE` category of DistSQL RAL only contains only the following three statements:

```
SET VARIABLE TRANSACTION_TYPE = xx; （LOCAL, XA, BASE）
SHOW VARIABLE TRANSACTION_TYPE;
SHOW VARIABLE CACHED_CONNECTIONS;
```
By listening to the community’s feedback, we noticed that querying and modifying the props configuration of Proxy (located in `server.yaml`) is also a frequent operation. Therefore, we have added support for props configuration in DistSQL RAL since the 5.0.0 GA version.

`SHOW VARIABLE`

First, let’s review how to configure props:

```yaml
props:
  max-connections-size-per-query: 1
  kernel-executor-size: 16  # Infinite by default.
  proxy-frontend-flush-threshold: 128  # The default value is 128.
  proxy-opentracing-enabled: false
  proxy-hint-enabled: false
  sql-show: false
  check-table-metadata-enabled: false
  show-process-list-enabled: false
    # Proxy backend query fetch size. A larger value may increase the memory usage of ShardingSphere Proxy.
    # The default value is -1, which means set the minimum value for different JDBC drivers.
  proxy-backend-query-fetch-size: -1
  check-duplicate-table-enabled: false
  proxy-frontend-executor-size: 0 # Proxy frontend executor size. The default value is 0, which means let Netty decide.
    # Available options of proxy backend executor suitable: OLAP(default), OLTP. The OLTP option may reduce time cost of writing packets to client, but it may increase the latency of SQL execution
    # and block other clients if client connections are more than `proxy-frontend-executor-size`, especially executing slow SQL.
  proxy-backend-executor-suitable: OLAP
  proxy-frontend-max-connections: 0 # Less than or equal to 0 means no limitation.
  sql-federation-enabled: false
    # Available proxy backend driver type: JDBC (default), ExperimentalVertx
  proxy-backend-driver-type: JDBC
```
Now, you can perform interactive queries by using the following syntax:

`SHOW VARIABLE PROXY_PROPERTY_NAME;`

Example:

```
mysql> SHOW VARIABLE MAX_CONNECTIONS_SIZE_PER_QUERY;
+--------------------------------+
| max_connections_size_per_query |
+--------------------------------+
| 1                              |
+--------------------------------+
1 row in set (0.00 sec)

mysql> SHOW VARIABLE SQL_SHOW;
+----------+
| sql_show |
+----------+
| false    |
+----------+
1 row in set (0.00 sec)
……
```
……
💡Note:
For DistSQL syntax, parameter keys are separated by underscores.

`SHOW ALL VARIABLES`

Since there are plenty of parameters in Proxy, you can also query all parameter values through `SHOW ALL VARIABLES` :

```
mysql> SHOW ALL VARIABLES;
+---------------------------------------+----------------+
| variable_name                         | variable_value |
+---------------------------------------+----------------+
| sql_show                              | false          |
| sql_simple                            | false          |
| kernel_executor_size                  | 0              |
| max_connections_size_per_query        | 1              |
| check_table_metadata_enabled          | false          |
| proxy_frontend_database_protocol_type |                |
| proxy_frontend_flush_threshold        | 128            |
| proxy_opentracing_enabled             | false          |
| proxy_hint_enabled                    | false          |
| show_process_list_enabled             | false          |
| lock_wait_timeout_milliseconds        | 50000          |
| proxy_backend_query_fetch_size        | -1             |
| check_duplicate_table_enabled         | false          |
| proxy_frontend_executor_size          | 0              |
| proxy_backend_executor_suitable       | OLAP           |
| proxy_frontend_max_connections        | 0              |
| sql_federation_enabled                | false          |
| proxy_backend_driver_type             | JDBC           |
| agent_plugins_enabled                 | false          |
| cached_connections                    | 0              |
| transaction_type                      | LOCAL          |
+---------------------------------------+----------------+
21 rows in set (0.01 sec)
```

`SET VARIABLE`

Dynamic management of resources and rules is a special advantage of DistSQL. Now you can also dynamically update props parameters by using the `SET VARIABLE` statement. For example:

```
#Enable SQL log output
SET VARIABLE SQL_SHOW = true;
#Turn on hint function
SET VARIABLE PROXY_HINT_ENABLED = true;
#Open federal query
SET VARIABLE SQL_FEDERATION_ENABLED = true;
……
```
💡Note:

The following parameters can be modified by the SET VARIABLE statement, but the new value takes effect only after the Proxy restart:

- `kernel_executor_size`
- `proxy_frontend_executor_size`
- `proxy_backend_driver_type`

The following parameters are read-only and cannot be modified:

- `cached_connections`

Other parameters will take effect immediately after modification.

## How to Manage Storage nodes
In ShardingSphere, storage nodes are not directly bound to compute nodes. Because one storage node may play different roles in different schemas at the same time, in order to implement different business logic. Storage nodes are always associated with a schema.

For DistSQL, storage nodes are managed through `RESOURCE` related statements, including:

- `ADD RESOURCE`
- `ALTER RESOURCE`
- `DROP RESOURCE`
- `SHOW SCHEMA RESOURCES`

**Schema Preparation**
`RESOURCE` related statements only work on schemas, so before operating, you need to create and use `USE` command to successfully select a schema:

```
DROP DATABASE IF EXISTS sharding_db;
CREATE DATABASE sharding_db;
USE sharding_db;
```

`ADD RESOURCE`

`ADD RESOURCE` supports the following syntax forms:

Specify `HOST`, `PORT`, `DB`

```
ADD RESOURCE resource_0 (
    HOST=127.0.0.1,
    PORT=3306,
    DB=db0,
    USER=root,
    PASSWORD=root
);
```
Specify `URL`

```
ADD RESOURCE resource_1 (
    URL="jdbc:mysql://127.0.0.1:3306/db1?serverTimezone=UTC&useSSL=false",
    USER=root,
    PASSWORD=root
);
```
The above two syntax forms support the extension parameter PROPERTIES that is used to specify the attribute configuration of the connection pool between the Proxy and the storage node, such as:

```
ADD RESOURCE resource_2 (
    HOST=127.0.0.1,
    PORT=3306,
    DB=db2,
    USER=root,
    PASSWORD=root,
    PROPERTIES("maximumPoolSize"=10)
),resource_3 (
    URL="jdbc:mysql://127.0.0.1:3306/db3?serverTimezone=UTC&useSSL=false",
    USER=root,
    PASSWORD=root,
    PROPERTIES("maximumPoolSize"=10,"idleTimeout"="30000")
);
```
💡 Note: Specifying JDBC connection parameters, such as `useSSL`, is supported only with URL form.

`ALTER RESOURCE`

`ALTER RESOURCE` is used to modify the connection information of storage nodes, such as changing the size of a connection pool, modifying JDBC connection parameters, etc.

Syntactically, `ALTER RESOURCE` is identical to `ADD RESOURCE`.

```
ALTER RESOURCE resource_2 (
    HOST=127.0.0.1,
    PORT=3306,
    DB=db2,
    USER=root,
    PROPERTIES("maximumPoolSize"=50)
),resource_3 (
    URL="jdbc:mysql://127.0.0.1:3306/db3?serverTimezone=GMT&useSSL=false",
    USER=root,
    PASSWORD=root,
    PROPERTIES("maximumPoolSize"=50,"idleTimeout"="30000")
);
```
💡 Note: Since modifying the storage node may cause metadata changes or application data exceptions, `ALTER RESOURCE` cannot used to modify the target database of the connection. Only the following values can be modified:

- User name
- User password
- `PROPERTIES` connection pool parameters
- JDBC parameters

`DROP RESOURCE`

`DROP RESOURCE` is used to delete storage nodes from a schema without deleting any data in the storage node. The statement example is as follows:

`DROP RESOURCE resource_0, resource_1;`

💡 Note: In order to ensure data correctness, the storage node referenced by the rule cannot be deleted.

`t_order` is a sharding table, and its actual tables are distributed in `resource_0` and `resource_1`. When `resource_0` and `resource_1` are referenced by `t_order` sharding rules, they cannot be deleted.

`SHOW SCHEMA RESOURCES`

`SHOW SCHEMA RESOURCES` is used to query storage nodes in schemas and supports the following syntax forms:

```
#Query the storage node in the current schema
SHOW SCHEMA RESOURCES;
#Query the storage node in the specified schema
SHOW SCHEMA RESOURCES FROM sharding_db;
```

Example:

Add 4 storage nodes through the above-mentioned `ADD RESOURCE` command, and then execute a query:



> There are actually a large number of columns in the query result, but here we only show part of it.

Above we have introduced you to the ways to dynamically manage storage nodes through DistSQL.

Compared with modifying YAML files, exectuting DistSQL statements is real-time, and there is no need to restart the Proxy or compute node, making online operations safer.

Changes executed through DistSQL can be synchronized to other compute nodes in the cluster in real time through the register center, while the client connected to any compute node can also query changes of storage nodes in real time.

Apache ShardingSphere’s cluster governance is very powerful.

## Conclusion
If you have any questions or suggestions about Apache ShardingSphere, please open an issue on the GitHub issue list. If you are interested in contributing to the project, you’re very welcome to join the Apache ShardingSphere community.

GitHub issue：https://github.com/apache/shardingsphere/issues

Issues · apache/shardingsphere
New issue Have a question about this project? Sign up for a free GitHub account to open an issue and contact its…
github.com

## Reference
**1. ShardingSphere-Proxy Quickstart: **[https://shardingsphere.apache.org/document/5.1.0/cn/quick-start/shardingsphere-proxy-quick-start/](https://shardingsphere.apache.org/document/5.1.0/en/quick-start/shardingsphere-proxy-quick-start/)

**2.DistSQL RDL：**https://shardingsphere.apache.org/document/current/en/user-manual/shardingsphere-proxy/distsql/syntax/rdl/resource-definition/

**3.DistSQL RQL：**https://shardingsphere.apache.org/document/current/en/user-manual/shardingsphere-proxy/distsql/syntax/rql/resource-query/

**4.DistSQL RAL：**https://shardingsphere.apache.org/document/current/en/user-manual/shardingsphere-proxy/distsql/syntax/ral/

**Apache ShardingSphere Project Links:**
[ShardingSphere Github](https://github.com/apache/shardingsphere/issues?page=1&q=is%3Aopen+is%3Aissue+label%3A%22project%3A+OpenForce+2022%22)

[ShardingSphere Twitter](https://twitter.com/ShardingSphere)

[ShardingSphere Slack](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)

[Contributor Guide](https://shardingsphere.apache.org/community/cn/involved/)

**Authors**
**Longtao JIANG**

SphereEx Middleware Development Engineer & Apache ShardingSphere Committer

Jiang works on DistSQL and security features R&D.

**Chengxiang Lan**

SphereEx Middleware Development Engineer & Apache ShardingSphere Committer

Lan contributes to DistSQL’s R&D.
