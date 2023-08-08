+++
title = "Optional Plugins"
weight = 6
+++

ShardingSphere only includes the implementation of the core SPI by default, and there is a part of the SPI that contains third-party dependencies in Git Source
Implemented plugins are not included. Retrievable at https://central.sonatype.com/.

SPI and existing implementation classes of SPI corresponding to all plugins can be retrieved at https://shardingsphere.apache.org/document/current/cn/dev-manual/.

All the built-in plugins for ShardingSphere-Proxy are listed below in the form of 'groupId:artifactId'.

- `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-etcd`, etcd implementation of persistent definition of cluster mode configuration information
- `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-zookeeper`, the zookeeper implementation of the persistent definition of cluster mode configuration information
- `org.apache.shardingsphere:shardingsphere-jdbc-core`, JDBC core
- `org.apache.shardingsphere:shardingsphere-db-protocol-core`, database protocol core
- `org.apache.shardingsphere:shardingsphere-mysql-protocol`, the MySQL implementation of the database protocol
- `org.apache.shardingsphere:shardingsphere-postgresql-protocol`, the PostgreSQL implementation of the database protocol
- `org.apache.shardingsphere:shardingsphere-opengauss-protocol`, the OpenGauss implementation of the database protocol
- `org.apache.shardingsphere:shardingsphere-proxy-frontend-core`, used by ShardingSphere-Proxy to parse and adapt the protocol for accessing the database
- `org.apache.shardingsphere:shardingsphere-proxy-frontend-mysql`, a MySQL implementation for ShardingSphere-Proxy to parse and adapt the protocol for accessing the database
- `org.apache.shardingsphere:shardingsphere-proxy-frontend-postgresql`, a PostgreSQL implementation for ShardingSphere-Proxy to parse and adapt the protocol for accessing the database
- `org.apache.shardingsphere:shardingsphere-proxy-frontend-opengauss`, an openGauss implementation for ShardingSphere-Proxy to parse and adapt the protocol for accessing the database
- `org.apache.shardingsphere:shardingsphere-proxy-backend-core`, the backend core for ShardingSphere Proxy
- `org.apache.shardingsphere:shardingsphere-standalone-mode-core`, the persistence definition core of single-machine mode configuration information

For the core `org.apache.shardingsphere:shardingsphere-jdbc-core`,Its built-in plugins reference[ShardingSphere-JDBC Optional Plugins](/en/user-manual/shardingsphere-jdbc/optional-plugins/).

If ShardingSphere Proxy needs to use optional plugins, you need to download the JAR containing its SPI implementation and its dependent JARs from Maven Central.

All optional plugins are listed below in the form of `groupId:artifactId`.

- Standalone mode configuration information persistence definition
  - `org.apache.shardingsphere:shardingsphere-standalone-mode-repository-jdbc`, JDBC based persistence
- Cluster mode configuration information persistence definition
  - `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-nacos`, Nacos based persistence
  - `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-consul`, Consul based persistence
- XA transaction manager provider definition
  - `org.apache.shardingsphere:shardingsphere-transaction-xa-narayana`, XA distributed transaction manager based on Narayana
- SQL translator
  - `org.apache.shardingsphere:shardingsphere-sql-translator-jooq-provider`, JooQ SQL translator
