+++
title = "Featured update! DISTSQL cluster governance capacity detailed explanation"
weight = 37
chapter = true
+++

# Featured update! DISTSQL cluster governance capacity detailed explanation

## Background

Since the release of Apache ShardingSphere 5.0.0-Beta, DistSQL has quickly come to the forefront of users' minds and has become increasingly popular among developers and operators for its unique "dynamic, no restart" advantage and beautiful syntax that is close to standard SQL. 
With the 5.0.0 and 5.1.0 iterations, the ShardingSphere community has once again added a large amount of syntax to DistSQL, bringing many useful features.

**In this article, we will explain the latest features of DistSQL from a "cluster governance" perspective.**

## ShardingSphere Cluster

In a typical cluster consisting of ShardingSphere-Proxy, it typically contains multiple Proxies, multiple storage nodes, as shown in the figure 1.

> For communication purposes, in ShardingSphere, we refer to the Proxy as Compute Node and the distributed database resources managed by the Proxy (e.g., ds_0, ds_1), as resource or storage nodes.

![1](https://shardingsphere.apache.org/blog/img/DistSQL_Cluster_Governance_Capabilities_img_1.png)

In this case, multiple Proxies are connected to the same registry, share configuration and rules, and are aware of each other's online status. At the same time, these Proxies also share the underlying storage node, and they can simultaneously read and write to the storage node. 
At this point, a user's application connected to any of the Proxies can perform equivalent operations.

With this cluster architecture, users can quickly scale the Proxy horizontally when computing resources are insufficient, and can also reduce the risk of single point of failure and improve system availability to a certain extent.

> Mechanisms for load balancing between applications and compute nodes can also be added, and are not extended here.

## Proxy Governance

Compute node governance for Cluster mode, more information about the mode can be found in the 
"[ShardingSphere Mode Operational Mode Explained](https://mp.weixin.qq.com/s?__biz=MzUzNjgwODk2Mw==&mid=2247486083&idx=1&sn=29d58f9a41194f30c6c1c4ea272692f5&chksm=faf1d1efcd8658f9a0c669cc82d794204a61103502c702f19f5b029ba47f578be88a7d0c82b1&scene=21&cur_album_id=2127063591433371652#wechat_redirect)".

### Cluster Preparation

Here is an example of a single machine simulating three Proxies, using the following schema configuration.

```
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

Execute the following start-up commands, respectively.

```
sh %SHARDINGSPHERE_PROXY_HOME%/bin/start.sh 3307
sh %SHARDINGSPHERE_PROXY_HOME%/bin/start.sh 3308
sh %SHARDINGSPHERE_PROXY_HOME%/bin/start.sh 3309
```

After the three Proxy instances are successfully started, the cluster of compute nodes is ready.

### SHOW INSTANCE LIST

Use a client to connect to any compute node, e.g. 3307.

```shell
mysql -h 127.0.0.1 -P 3307 -u root -p
```

View a list of instances.

```shell
mysql> SHOW INSTANCE LIST;
+----------------+-----------+------+---------+
| instance_id    | host      | port | status  |
+----------------+-----------+------+---------+
| 10.7.5.35@3309 | 10.7.5.35 | 3309 | enabled |
| 10.7.5.35@3308 | 10.7.5.35 | 3308 | enabled |
| 10.7.5.35@3307 | 10.7.5.35 | 3307 | enabled |
+----------------+-----------+------+---------+
```

The meanings of the fields are as follows.

- instance_id: the id of the instance, currently consisting of host and port.
- host: the host address.
- port: the port number.
- status: the status of the instance, enabled and disabled means enabled and disabled respectively.

### DISABLE INSTANCE

The DISABLE INSTANCE statement is used to set the specified compute node to a disabled state. Note that this directive does not terminate the process of the target instance, but only logically disables it. DISABLE INSTANCE supports the following syntactic forms.

```
DISABLE INSTANCE 10.7.5.35@3308;
# or
DISABLE INSTANCE IP=10.7.5.35, PORT=3308;
```

For example.

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

After executing the DISABLE INSTANCE statement, you can see by querying again that the status of the instance on port 3308 has been updated to disabled, indicating that Proxy has been disabled.

In this case, if there is a client connected to 10.7.5.35@3308, any SQL execution will prompt an exception.

```
1000 - Circuit break mode is ON.
```

Tip: Disabling the Proxy that is currently accepting commands is not allowed at this time. If you send DISABLE INSTANCE 10.7.5.35@3309; to 10.7.5.35@3309, you will receive an exception.

### ENABLE INSTANCE

The ENABLE INSTANCE statement is used to set the specified compute node to the enabled state. ENABLE INSTANCE also supports the following syntactic forms.

```
ENABLE INSTANCE 10.7.5.35@3308;
# or
ENABLE INSTANCE IP=10.7.5.35, PORT=3308;
```

For example.

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

After executing the ENABLE INSTANCE statement, you can see by querying again that the status of the instance on port 3308 has been restored to the enabled state.

## Proxy Parameter Management

In our previous article "[SCTL Reborn: Into the Arms of RAL](https://mp.weixin.qq.com/s?__biz=MzUzNjgwODk2Mw==&mid=2247485738&idx=1&sn=843fe15a4c51c823fb32f6a4be06973d&chksm=faf1d246cd865b5084252c2184a9309487e8c751b8654e6d5654d25238e36f08e7e717c49eec&scene=21&cur_album_id=2127063591433371652#wechat_redirect)", 
we explained the evolution of SCTL (ShardingSphere Control Language) to RAL (Resource & Rule Administration Language), and brought the new SHOW VARIABLE and SET VARIABLE syntax. However, in version 5.0.0-Beta, the DistSQL RAL for the VARIABLE category contains only the following three statements.

```
SET VARIABLE TRANSACTION_TYPE = xx; （LOCAL, XA, BASE）
SHOW VARIABLE TRANSACTION_TYPE;
SHOW VARIABLE CACHED_CONNECTIONS;
```

After a lot of customer feedback, we found that querying and modifying the props configuration of the Proxy (located in server.yaml) is also a high frequency operation. So, starting from 5.0.0 GA version, DistSQL RAL adds support for props configuration.

### SHOW VARIABLE

First let's review the contents of the props configuration.

```
props:
  max-connections-size-per-query: 1
  kernel-executor-size: 16  # Infinite by default.
  proxy-frontend-flush-threshold: 128  # The default value is 128.
  proxy-opentracing-enabled: false
  proxy-hint-enabled: false
  sql-show: false
  check-table-metadata-enabled: false
    # Proxy backend query fetch size. A larger value may increase the memory usage of ShardingSphere Proxy.
    # The default value is -1, which means set the minimum value for different JDBC drivers.
  proxy-backend-query-fetch-size: -1
  proxy-frontend-executor-size: 0 # Proxy frontend executor size. The default value is 0, which means let Netty decide.
    # Available options of proxy backend executor suitable: OLAP(default), OLTP. The OLTP option may reduce time cost of writing packets to client, but it may increase the latency of SQL execution
    # and block other clients if client connections are more than `proxy-frontend-executor-size`, especially executing slow SQL.
  proxy-backend-executor-suitable: OLAP
  proxy-frontend-max-connections: 0 # Less than or equal to 0 means no limitation.
  sql-federation-enabled: false
    # Available proxy backend driver type: JDBC (default), ExperimentalVertx
  proxy-backend-driver-type: JDBC
```

Users can now perform interactive queries with the following syntax.

```
SHOW VARIABLE PROXY_PROPERTY_NAME;
```

For example.

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
......
```

Tip: In DistSQL syntax, the parameter names are separated by underscores, unlike in the file.

### SHOW ALL VARIABLES

Due to the large number of parameters in the Proxy, the user can also query the values of all the parameters by means of SHOW ALL VARIABLES.

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

### SET VARIABLE

Managing resources and rules dynamically is a unique advantage of DistSQL. It is now possible to dynamically update props parameters using the SET VARIABLE statement as well, e.g.

```
# Turn on SQL log output
SET VARIABLE SQL_SHOW = true;
# Turn on the hint function
SET VARIABLE PROXY_HINT_ENABLED = true;
# Turn on Federal Inquiry
SET VARIABLE SQL_FEDERATION_ENABLED = true;
......
```

Tip.

- The following parameters can be modified with the SET VARIABLE statement, but the new values will only take effect after the Proxy restarts.
  - kernel_executor_size
  - proxy_frontend_executor_size
  - proxy_backend_driver_type
- The following parameters are read-only and cannot be modified.
  - cached_connections
- The other unspecified parameters take effect immediately after modification.

## Storage Node Management

In ShardingSphere, storage nodes are not directly bound to compute nodes. Since the same storage node may play different roles in different logical libraries (schema) for different business logic at the same time, the storage node is always associated with a logical library.

In DistSQL, storage node management is performed by RESOURCE-related statements, including

- ADD RESOURCE
- ALTER RESOURCE
- DROP RESOURCE
- SHOW SCHEMA RESOURCES

### Logical Library Preparation

The RESOURCE statement only works on logical libraries, so before you can do this, you need to create and successfully select a logical library using the USE command.

```
DROP DATABASE IF EXISTS sharding_db;

CREATE DATABASE sharding_db;

USE sharding_db;
```

### ADD RESOURCE

The ADD RESOURCE syntax supports the following forms.

- Designation HOST, PORT, DB
  ```
  ADD RESOURCE resource_0 (
    HOST=127.0.0.1,
    PORT=3306,
    DB=db0,
    USER=root,
    PASSWORD=root
  );
  ```
- Specified URL
  ```
  ADD RESOURCE resource_1 (
    URL="jdbc:mysql://127.0.0.1:3306/db1?serverTimezone=UTC&useSSL=false",
    USER=root,
    PASSWORD=root
  );
  ```

Both of these syntax forms support the extended parameter PROPERTIES, which is used to specify the configuration of the connection pool between the Proxy and the storage node, e.g.

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

Tip: Only the URL form supports specifying JDBC connection parameters, such as useSSL.

### ALTER RESOURCE

ALTER RESOURCE is used to modify the connection information of existing storage nodes, such as changing the connection pool size, modifying JDBC connection parameters, etc.

In syntactic form, ALTER RESOURCE is identical to ADD RESOURCE, e.g.

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

Tip: Because modifying storage nodes may result in metadata changes or application data exceptions, ALTER RESOURCE cannot modify the connected target DB; only the following can be modified.

- User Name
- User Password 
- PROPERTIES Connection Pool Parameters 
- JDBC Parameters

### DROP RESOURCE

DROP RESOURCE is used to remove the storage node from the logical library and does not remove any data from the storage node. The syntax example is as follows.

```
DROP RESOURCE resource_0, resource_1;
```

Hint: To protect the correct data, the storage nodes referenced by the rule cannot be deleted.

For example, if t_order is a sliced table and its actual tables are distributed in resource_0 and resource_1, then resource_0 and resource_1 are referenced by t_order's slicing rule and cannot be deleted.

### SHOW SCHEMA RESOURCES

SHOW SCHEMA RESOURCES is used to query the storage nodes in the logical library and supports the following syntax forms.

```
# Query the storage node in the logical library of the current use
SHOW SCHEMA RESOURCES;
# Query the storage nodes in the specified logical library
SHOW SCHEMA RESOURCES FROM sharding_db;
```

Example: After adding 4 storage nodes with the ADD RESOURCE command as described above, perform the following query operation.

![2](https://shardingsphere.apache.org/blog/img/DistSQL_Cluster_Governance_Capabilities_img_2.png)

> Due to the large number of columns of query results, only some of them are intercepted.

This is how you can dynamically manage storage nodes via DistSQL. Compared to modifying YAML files, DistSQL executes in real time without restarting the Proxy compute nodes, making online operations more secure.

At the same time, changes performed via DistSQL are synchronized in real time to other compute nodes within the cluster via the registry, and clients connected to either compute node can instantly query the storage node for changes.

This, then, is the beauty of cluster governance.

## Apache ShardingSphere Open Source Project Links:

[ShardingSphere Github](https://github.com/apache/shardingsphere)

[ShardingSphere Twitter](https://twitter.com/ShardingSphere)

[ShardingSphere Slack Channel](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9%7EI4rYcR18bq0SYTg)

[Contributor Guide](https://shardingsphere.apache.org/community/cn/involved/)

## Author

**Longtao Jiang**

SphereEx Middleware R&D Engineer

Apache ShardingSphere Committer

Mainly responsible for innovation and development of DistSQL and security related features.

![3](https://shardingsphere.apache.org/blog/img/Blog_20_img_2_Jiang_Longtao_Photo.png)

**Chengxiang Lan**

SphereEx Middleware R&D Engineer

Apache ShardingSphere Committer

Currently focused on the design and development of DistSQL.

![4](https://shardingsphere.apache.org/blog/img/Lan_Chengxiang_Photo.png)