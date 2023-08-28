+++
title = "Optional Plugins"
weight = 6
+++

ShardingSphere only includes the implementation of the core SPI by default, and there is a part of the SPI that contains third-party dependencies in Git Source
Implemented plugins are not included. Retrievable at https://central.sonatype.com/.

SPI and existing implementation classes of SPI corresponding to all plugins can be retrieved at https://shardingsphere.apache.org/document/current/cn/dev-manual/.

All the built-in plugins for ShardingSphere-JDBC are listed below in the form of 'groupId:artifactId'.

- `org.apache.shardingsphere:shardingsphere-authority-core`, the user authority to load the logical core
- `org.apache.shardingsphere:shardingsphere-cluster-mode-core`, the persistent definition core of cluster mode configuration information
- `org.apache.shardingsphere:shardingsphere-db-discovery-core`, high availability core
- `org.apache.shardingsphere:shardingsphere-encrypt-core`, data encryption core
- `org.apache.shardingsphere:shardingsphere-encrypt-sm`, the SM encrypt algorithm.
- `org.apache.shardingsphere:shardingsphere-infra-context`, the kernel operation and metadata refresh mechanism of Context
- `org.apache.shardingsphere:shardingsphere-logging-core`, logging core
- `org.apache.shardingsphere:shardingsphere-mask-core`, data masking core
- `org.apache.shardingsphere:shardingsphere-mysql-dialect-exception`, MySQL implementation of database gateway
- `org.apache.shardingsphere:shardingsphere-parser-core`, SQL parsing core
- `org.apache.shardingsphere:shardingsphere-postgresql-dialect-exception`, PostgreSQL implementation of database
- `org.apache.shardingsphere:shardingsphere-readwrite-splitting-core`, read-write splitting core
- `org.apache.shardingsphere:shardingsphere-shadow-core`, shadow library core
- `org.apache.shardingsphere:shardingsphere-sharding-core`, data sharding core
- `org.apache.shardingsphere:shardingsphere-single-core`, single-table (only the only table that exists in all sharded data sources) core
- `org.apache.shardingsphere:shardingsphere-sql-federation-core`, federation query executor core
- `org.apache.shardingsphere:shardingsphere-sql-parser-mysql`, MySQL dialect implementation of SQL parsing
- `org.apache.shardingsphere:shardingsphere-sql-parser-opengauss`, OpenGauss dialect implementation of SQL parsing
- `org.apache.shardingsphere:shardingsphere-sql-parser-oracle`, Oracle dialect implementation of SQL parsing
- `org.apache.shardingsphere:shardingsphere-sql-parser-postgresql`, PostgreSQL dialect implementation of SQL parsing
- `org.apache.shardingsphere:shardingsphere-sql-parser-sql92`,the SQL 92 dialect implementation of SQL parsing
- `org.apache.shardingsphere:shardingsphere-sql-parser-sqlserver`, SQL Server dialect implementation of SQL parsing
- `org.apache.shardingsphere:shardingsphere-standalone-mode-core`, the persistence definition core of single-machine mode configuration information
- `org.apache.shardingsphere:shardingsphere-standalone-mode-repository-jdbc-h2`, H2 implementation of persistent definition of configuration information in stand-alone mode
- `org.apache.shardingsphere:shardingsphere-traffic-core`, traffic governance core
- `org.apache.shardingsphere:shardingsphere-transaction-core`, XA Distributed Transaction Manager Core

If ShardingSphere-JDBC needs to use optional plugins, you need to download the JAR containing its SPI implementation and its dependent JARs from Maven Central.

All optional plugins are listed below in the form of `groupId:artifactId`.

- Standalone mode configuration information persistence definition
  - `org.apache.shardingsphere:shardingsphere-standalone-mode-repository-jdbc-mysql`, MySQL based persistence
- Cluster mode configuration information persistence definition
  - `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-zookeeper`, Zookeeper based persistence
  - `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-etcd`, Etcd based persistence
  - `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-nacos`, Nacos based persistence
  - `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-consul`, Consul based persistence
- XA transaction manager provider definition
  - `org.apache.shardingsphere:shardingsphere-transaction-xa-narayana`, XA distributed transaction manager based on Narayana
- SQL translator
  - `org.apache.shardingsphere:shardingsphere-sql-translator-jooq-provider`, JooQ SQL translator
