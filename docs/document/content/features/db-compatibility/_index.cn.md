+++
pre = "<b>3.11. </b>"
title = "数据库兼容"
weight = 11
chapter = true
+++

## 定义

随着通信技术的革新，全新领域的应用层出不穷，推动和颠覆整个人类社会协作模式的革新。 数据存量随着应用的探索不断增加，数据的存储和计算模式无时无刻面临着创新。
面向交易、大数据、关联分析、物联网等场景越来越细分，单一数据库再也无法适用于所有的应用场景。 与此同时，场景内部也愈加细化，相似场景使用不同数据库已成为常态。 由此可见，数据库碎片化的趋势已经不可逆转。
数据库兼容，主要指的是针对众多的碎片化的数据库的语法、协议等等的兼容能力。

## 相关概念

### SQL （Structured Query Language 结构化查询语言）

是用于管理数据库的语言 ，SQL 在 1986 年成为美国国家标准学会（ANSI）的一项标准，在 1987 年成为国际标准化组织（ISO）标准 [13]。此后，这一标准经过了一系列的增订，加入了大量新特性。虽然有这一标准的存在，然而大部分数据库不只会支持 SQL 标准，还会有一些自己独有的语言，这就导致根据具体的数据库系统不同，也可能会支持不同的方言。

### 数据库协议

可以部署数据产品后进行客户端和服务端通信，就像 SQL 一样，不同的数据库，甚至同一个数据库的不同版本也会支持不同的通信协议，因此同一种协议，是无法访问多种不同的数据库的。

## 使用限制

### SQL 解析

SQL 是使用者与数据库交流的标准语言。 SQL 解析引擎负责将 SQL 字符串解析为抽象语法树，供 Apache ShardingSphere 理解并实现其增量功能。
目前支持 MySQL, PostgreSQL, SQLServer, Oracle, openGauss 以及符合 SQL92 规范的 SQL 方言。 由于 SQL 语法的复杂性，目前仍然存在少量不支持的 SQL。
本章节详细罗列出目前不支持的 SQL 种类，供使用者参考。
其中有未涉及到的 SQL 欢迎补充，未支持的 SQL 也尽量会在未来的版本中支持。

源码：https://github.com/apache/shardingsphere/tree/master/shardingsphere-sql-parser

#### MySQL

MySQL 不支持的 SQL 清单如下：

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

源码：https://github.com/apache/shardingsphere/tree/master/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-mysql

#### openGauss

openGauss 不支持的 SQL 清单如下：

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

源码：https://github.com/apache/shardingsphere/tree/master/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-opengauss

#### PostgreSQL

PostgreSQL 不支持的 SQL 清单如下：

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

源码：https://github.com/apache/shardingsphere/tree/master/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-postgresql

### 数据库协议

Apache ShardingSphere 目前实现了 MySQL 和 PostgreSQL 协议。
源码：https://github.com/apache/shardingsphere/tree/master/shardingsphere-db-protocol

### 特性支持

Apache ShardingSphere 为数据库提供了分布式协作的能力，同时将一部分数据库特性抽象到了上层，进行统一管理，以降低用户的使用难度。
因此，对于统一提供的特性，原生的 SQL 将不再下发到数据库，并提示该操作不被支持，用户可使用 ShardingSphere 提供的的方式进行代替。
本章节详细罗列出目前不支持的数据库特性和相关的 SQL 语句，供使用者参考。
其中有未涉及到的 SQL 欢迎补充。

#### MySQL

MySQL 不支持的 SQL 清单如下：

**用户和角色**

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

**授权**

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
