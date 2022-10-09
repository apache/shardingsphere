+++
title = "Optional Plugins"
weight = 5
+++

ShardingSphere only includes the implementation of the core SPI by default, and there is a part of the SPI that contains
third-party dependencies in Git Source
Implemented plugins are not included. Retrievable at https://central.sonatype.dev/.

SPI and existing implementation classes of SPI corresponding to all plugins can be retrieved
at https://shardingsphere.apache.org/document/current/cn/dev-manual/.

All built-in plugins are listed below in the form of `groupId:artifactId`.

- `org.apache.shardingsphere:shardingsphere-db-protocol-core`, database protocol core
- `org.apache.shardingsphere:shardingsphere-mysql-protocol`, the MySQL implementation of the database protocol
- `org.apache.shardingsphere:shardingsphere-postgresql-protocol`, the PostgresSQL implementation of the database protocol
- `org.apache.shardingsphere:shardingsphere-opengauss-protocol`, the OpenGauss implementation of the database protocol
- `org.apache.shardingsphere:shardingsphere-proxy-frontend-core`, used by ShardingSphere-Proxy to parse and adapt the protocol for accessing the database
- `org.apache.shardingsphere:shardingsphere-proxy-frontend-mysql`, a MySQL implementation for ShardingSphere-Proxy to parse and adapt the protocol for accessing the database
- `org.apache.shardingsphere:shardingsphere-proxy-frontend-reactive-mysql`, the `vertx-sql-client` implementation of MySQL for ShardingSphere-Proxy to parse and adapt the protocol for accessing the database
- `org.apache.shardingsphere:shardingsphere-proxy-frontend-postgresql`, a PostgresSQL implementation for ShardingSphere-Proxy to parse and adapt the protocol for accessing the database
- `org.apache.shardingsphere:shardingsphere-proxy-frontend-opengauss`, an openGauss implementation for ShardingSphere-Proxy to parse and adapt the protocol for accessing the database
- `org.apache.shardingsphere:shardingsphere-proxy-backend`, the backend for ShardingSphere Proxy
- `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-zookeeper-curator`, the zookeeper implementation of the persistent definition of cluster mode configuration information
- `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-etcd`, etcd implementation of persistent definition of cluster mode configuration information
- `org.apache.shardingsphere:shardingsphere-jdbc-core`

For the core `org.apache.shardingsphere:shardingsphere-jdbc-core`, the following plugins are built-in.

- `org.apache.shardingsphere:shardingsphere-transaction-core`, XA Distributed Transaction Manager Core
- `org.apache.shardingsphere:shardingsphere-sql-parser-sql92`, the SQL 92 dialect implementation of SQL parsing
- `org.apache.shardingsphere:shardingsphere-sql-parser-mysql`, MySQL dialect implementation of SQL parsing
- `org.apache.shardingsphere:shardingsphere-sql-parser-postgresql`, PostgresSQL dialect implementation of SQL parsing
- `org.apache.shardingsphere:shardingsphere-sql-parser-oracle`, Oracle dialect parsing implementation for SQL parsing
- `org.apache.shardingsphere:shardingsphere-sql-parser-sqlserver`, the SQL Server dialect implementation of SQL parsing
- `org.apache.shardingsphere:shardingsphere-sql-parser-opengauss`, the OpenGauss dialect implementation of SQL parsing
- `org.apache.shardingsphere:shardingsphere-mysql-dialect-exception`, MySQL implementation of database gateway
- `org.apache.shardingsphere:shardingsphere-postgresql-dialect-exception`, PostgresSQL implementation of database
  gateway
- `org.apache.shardingsphere:shardingsphere-authority-core`, the user authority to load the logical core
- `org.apache.shardingsphere:shardingsphere-single-table-core`, single-table (only the only table that exists in all
  sharded data sources) core
- `org.apache.shardingsphere:shardingsphere-traffic-core`, traffic governance core
- `org.apache.shardingsphere:shardingsphere-infra-context`, the kernel operation and metadata refresh mechanism of
  Context
- `org.apache.shardingsphere:shardingsphere-standalone-mode-core`, the persistence definition core of single-machine
  mode configuration information
- `org.apache.shardingsphere:shardingsphere-standalone-mode-repository-jdbc-h2`, H2 implementation of persistent
  definition of configuration information in stand-alone mode
- `org.apache.shardingsphere:shardingsphere-cluster-mode-core`, the persistent definition core of cluster mode
  configuration information
- `org.apache.shardingsphere:shardingsphere-sharding-core`, data sharding core
- `org.apache.shardingsphere:shardingsphere-sharding-cache`, refer
  to https://github.com/apache/shardingsphere/issues/21223
- `org.apache.shardingsphere:shardingsphere-readwrite-splitting-core`, read-write splitting core
- `org.apache.shardingsphere:shardingsphere-db-discovery-core`, high availability core
- `org.apache.shardingsphere:shardingsphere-encrypt-core`, data encryption core
- `org.apache.shardingsphere:shardingsphere-shadow-core`, shadow library core
- `org.apache.shardingsphere:shardingsphere-sql-federation-core`, federation query executor core
- `org.apache.shardingsphere:shardingsphere-sql-federation-executor-advanced`, the `advanced` implementation of
  federated query executor
- `org.apache.shardingsphere:shardingsphere-sql-federation-executor-original`, the `original` implementation of
  federated query executor
- `org.apache.shardingsphere:shardingsphere-parser-core`, SQL parsing core

If ShardingSphere Proxy needs to use optional plugins, you need to download the JAR containing its SPI implementation
and its dependent JARs from Maven Central.

All optional plugins are listed below in the form of `groupId:artifactId`.

- Cluster mode configuration information persistence definition
    - `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-nacos`, Nacos based persistence
    - `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-consul`, Consul based persistence
- XA transaction manager provider definition
    - `org.apache.shardingsphere:shardingsphere-transaction-xa-narayana`, XA distributed transaction manager based on
      Narayana
    - `org.apache.shardingsphere:shardingsphere-transaction-xa-bitronix`, XA distributed transaction manager based on
      Bitronix
- SQL translator
    - `org.apache.shardingsphere:shardingsphere-sql-translator-jooq-provider`, JooQ SQL translator
