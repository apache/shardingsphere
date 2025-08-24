+++
title = "Optional Plugins"
weight = 6
+++

ShardingSphere only includes the implementation of the core SPI by default, and there is a part of the SPI that contains third-party dependencies in Git Source
Implemented plugins are not included. Retrievable at https://central.sonatype.com/.

SPI and existing implementation classes of SPI corresponding to all plugins can be retrieved at https://shardingsphere.apache.org/document/current/cn/dev-manual/.

All the built-in plugins for ShardingSphere-Proxy are listed below in the form of 'groupId:artifactId'.

- `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-zookeeper`, the ZooKeeper implementation of the persistent definition of cluster mode configuration information
- `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-etcd`, Etcd implementation of persistent definition of cluster mode configuration information
- `org.apache.shardingsphere:shardingsphere-jdbc`, JDBC module
- `org.apache.shardingsphere:shardingsphere-database-protocol-core`, database protocol core
- `org.apache.shardingsphere:shardingsphere-protocol-mysql`, the MySQL implementation of the database protocol
- `org.apache.shardingsphere:shardingsphere-protocol-postgresql`, the PostgreSQL implementation of the database protocol
- `org.apache.shardingsphere:shardingsphere-protocol-opengauss`, the OpenGauss implementation of the database protocol
- `org.apache.shardingsphere:shardingsphere-protocol-firebird`, the Firebird implementation of the database protocol
- `org.apache.shardingsphere:shardingsphere-proxy-frontend-core`, used by ShardingSphere-Proxy to parse and adapt the protocol for accessing the database
- `org.apache.shardingsphere:shardingsphere-proxy-frontend-mysql`, a MySQL implementation for ShardingSphere-Proxy to parse and adapt the protocol for accessing the database
- `org.apache.shardingsphere:shardingsphere-proxy-frontend-postgresql`, a PostgreSQL implementation for ShardingSphere-Proxy to parse and adapt the protocol for accessing the database
- `org.apache.shardingsphere:shardingsphere-proxy-frontend-opengauss`, an openGauss implementation for ShardingSphere-Proxy to parse and adapt the protocol for accessing the database
- `org.apache.shardingsphere:shardingsphere-proxy-frontend-firebird`, an Firebird implementation for ShardingSphere-Proxy to parse and adapt the protocol for accessing the database
- `org.apache.shardingsphere:shardingsphere-proxy-backend-core`, the backend core for ShardingSphere Proxy
- `org.apache.shardingsphere:shardingsphere-standalone-mode-core`, the persistence definition core of single-machine mode configuration information

For the core `org.apache.shardingsphere:shardingsphere-jdbc`,Its built-in plugins reference[ShardingSphere-JDBC Optional Plugins](/en/user-manual/shardingsphere-jdbc/optional-plugins/).

If ShardingSphere Proxy needs to use optional plugins, you need to download the JAR containing its SPI implementation and its dependent JARs from Maven Central.

All optional plugins are listed below in the form of `groupId:artifactId`.

- Standalone mode configuration information persistence definition
  - `org.apache.shardingsphere:shardingsphere-standalone-mode-repository-jdbc`, JDBC based persistence
- XA transaction manager provider definition
  - `org.apache.shardingsphere:shardingsphere-transaction-xa-narayana`, XA distributed transaction manager based on Narayana
- Row Value Expressions definition
  - `org.apache.shardingsphere:shardingsphere-infra-expr-espresso`，Row Value Expressions that uses the Groovy syntax based on GraalVM Truffle's Espresso implementation
- Database type identification
  - `org.apache.shardingsphere:shardingsphere-infra-database-hive`, Adaptation of jdbcURL for `JDBC support` of Hive, and metadata loading implementation 
  - `org.apache.shardingsphere:shardingsphere-infra-database-presto`, Adaptation of jdbcURL for `JDBC support` of Presto, and metadata loading implementation
- SQL parsing
  - `org.apache.shardingsphere:shardingsphere-parser-sql-engine-clickhouse`, ClickHouse dialect implementation of SQL parsing
  - `org.apache.shardingsphere:shardingsphere-parser-sql-engine-hive`， Hive dialect implementation of SQL parsing

In addition to the above optional plugins, ShardingSphere community developers have contributed a number of plugin implementations. These plugins can be found in [ShardingSphere Plugins](https://github.com/apache/shardingsphere-plugin) repository. Plugins in ShardingSphere Plugin repository would remain the same release plan with ShardingSphere, you can build plugin jar by yourself, and install into ShardingSphere.
