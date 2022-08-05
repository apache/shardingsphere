+++
pre = "<b>3.11. </b>"
title = "DB Compatibility"
weight = 11
chapter = true
+++

## Definition

Thanks to the innovation of information technology, and fast application iteration speed, data traffic is growing exponentially - ultimately continuously changing data storage and computation models. 

Transaction-oriented, Big Data, IoT, and other scenarios are increasingly fragmented, indicating that a single database can no longer be applied to all circumstances. At the same time, things are getting more and more sophisticated, so it becomes the norm to use different databases for similar scenarios. This shows that database fragmentation has already become an irreversible trend.

Database compatibility mainly refers to the compatibility of elements like syntax, and protocols for many fragmented databases. 

## Relevant Concepts

### SQL (Structured Query Language)

SQL is a language used to manage databases and became an American National Standards Institute (ANSI) standard in 1986 and a standard established by International Organization for Standardization (ISO) standard in 1987 [13]. Since then, this standard has undergone a series of updates to include several new features. Despite its presence, most databases have their unique languages besides SQL, which may lead to different dialects depending on the specific database system. 

### Database Protocols

It is possible to deploy data products for client-server communication. Just like SQL, different databases, or even different versions of the same database, support different communication protocols, so accessing many different databases with the same protocol is impossible. 

## Limitations

### SQL Parsing

SQL is the standard language for users to communicate with the database. The SQL parsing engine is responsible for parsing SQL strings into abstract syntax trees for Apache ShardingSphere to understand and implement its incremental functionality. 

MySQL, PostgreSQL, SQLServer, Oracle, openGauss, and SQL92 compliant SQL dialects are currently supported. Due to the complexity of SQL syntax, there are still a few SQL currently not supported.

This section lists those SQL types in detail for users' reference. 

You are welcome to add anything not covered in the list, and we will work hard to support these in future versions as soon as possible. 

Source code: https://github.com/apache/shardingsphere/tree/master/shardingsphere-sql-parser

#### MySQL

SQL not supported are listed below:

|SQL|
|---|
|CLONE LOCAL DATA DIRECTORY = ‘clone_dir’|
|INSTALL COMPONENT ‘file://component1’, ‘file://component2’|
|UNINSTALL COMPONENT ‘file://component1’, ‘file://component2’|
|REPAIR TABLE t_order|
|OPTIMIZE TABLE t_order|
|CHECKSUM TABLE t_order|
|CHECK TABLE t_order|
|SET RESOURCE GROUP group_name|
|DROP RESOURCE GROUP group_name|
|CREATE RESOURCE GROUP group_name TYPE = SYSTEM|
|ALTER RESOURCE GROUP rg1 VCPU = 0-63|

Source Code：https://github.com/apache/shardingsphere/tree/master/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-mysql


#### openGauss

SQL not supported are listed below:

|SQL|
|---|
|CREATE type avg_state AS (total bigint, count bigint);|
|CREATE AGGREGATE my_avg(int4) (stype = avg_state, sfunc = avg_transfn, finalfunc = avg_finalfn)|
|CREATE TABLE agg_data_2k AS SELECT g FROM generate_series(0, 1999) g;|
|CREATE SCHEMA alt_nsp1;|
|ALTER AGGREGATE alt_agg3(int) OWNER TO regress_alter_generic_user2;|
|CREATE CONVERSION alt_conv1 FOR ‘LATIN1’ TO ‘UTF8’ FROM iso8859_1_to_utf8;|
|CREATE FOREIGN DATA WRAPPER alt_fdw1|
|CREATE SERVER alt_fserv1 FOREIGN DATA WRAPPER alt_fdw1|
|CREATE LANGUAGE alt_lang1 HANDLER plpgsql_call_handler|
|CREATE STATISTICS alt_stat1 ON a, b FROM alt_regress_1|
|CREATE TEXT SEARCH DICTIONARY alt_ts_dict1 (template=simple)|
|CREATE RULE def_view_test_ins AS ON INSERT TO def_view_test DO INSTEAD INSERT INTO def_test SELECT new.*|
|ALTER TABLE alterlock SET (toast.autovacuum_enabled = off)|
|CREATE PUBLICATION pub1 FOR TABLE alter1.t1, ALL TABLES IN SCHEMA alter2|

Source code：https://github.com/apache/shardingsphere/tree/master/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-opengauss

#### PostgreSQL

SQL not supported are listed below:

|SQL|
|---|
|CREATE type avg_state AS (total bigint, count bigint);|
|CREATE AGGREGATE my_avg(int4) (stype = avg_state, sfunc = avg_transfn, finalfunc = avg_finalfn)|
|CREATE TABLE agg_data_2k AS SELECT g FROM generate_series(0, 1999) g;|
|CREATE SCHEMA alt_nsp1;|
|ALTER AGGREGATE alt_agg3(int) OWNER TO regress_alter_generic_user2;|
|CREATE CONVERSION alt_conv1 FOR ‘LATIN1’ TO ‘UTF8’ FROM iso8859_1_to_utf8;|
|CREATE FOREIGN DATA WRAPPER alt_fdw1|
|CREATE SERVER alt_fserv1 FOREIGN DATA WRAPPER alt_fdw1|
|CREATE LANGUAGE alt_lang1 HANDLER plpgsql_call_handler|
|CREATE STATISTICS alt_stat1 ON a, b FROM alt_regress_1|
|CREATE TEXT SEARCH DICTIONARY alt_ts_dict1 (template=simple)|
|CREATE RULE def_view_test_ins AS ON INSERT TO def_view_test DO INSTEAD INSERT INTO def_test SELECT new.*|
|ALTER TABLE alterlock SET (toast.autovacuum_enabled = off)|
|CREATE PUBLICATION pub1 FOR TABLE alter1.t1, ALL TABLES IN SCHEMA alter2|

Source Code: https://github.com/apache/shardingsphere/tree/master/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-postgresql


### Database Protocols

Apache ShardingSphere currently implements MySQL and PostgreSQL protocols
Source Code：https://github.com/apache/shardingsphere/tree/master/shardingsphere-db-protocol

### Features Supported

Apache ShardingSphere provides distributed collaboration capabilities for databases while abstracting some of the features of the database to the upper layer for integrated management to make it easier for users. 

Therefore, for features provided universally, native SQL will no longer be sent down to the database and will send out a notice saying that the operation is not supported. Users can just rely on the methods provided by ShardingSphere instead. 

This section provides a detailed list of currently unsupported database features and related SQL statements for further reference. 

Feel free to add any SQL that is not covered in this section. 

#### MySQL

MySQL does not support this SQL listed below:

**User&Role**

|SQL|
|---|
|CREATE USER ‘finley’@‘localhost’ IDENTIFIED BY ‘password’|
|ALTER USER ‘finley’@‘localhost’ IDENTIFIED BY ‘new_password’|
|DROP USER ‘finley’@‘localhost’;|
|CREATE ROLE ‘app_read’|
|DROP ROLE ‘app_read’|
|SHOW CREATE USER finley|
|SET PASSWORD = ‘auth_string’|
|SET ROLE DEFAULT;|

**Permission**

|SQL|
|---|
|GRANT ALL ON db1.* TO ‘jeffrey’@‘localhost’|
|GRANT SELECT ON world.* TO ‘role3’;|
|GRANT ‘role1’, ‘role2’ TO ‘user1’@‘localhost’|
|REVOKE INSERT ON . FROM ‘jeffrey’@‘localhost’|
|REVOKE ‘role1’, ‘role2’ FROM ‘user1’@‘localhost’|
|REVOKE ALL PRIVILEGES, GRANT OPTION FROM user_or_role|
|SHOW GRANTS FOR ‘jeffrey’@‘localhost’|
|SHOW GRANTS FOR CURRENT_USER|
|FLUSH PRIVILEGES|
