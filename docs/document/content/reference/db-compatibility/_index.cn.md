+++
pre = "<b>7.1. </b>"
title = "数据兼容性"
weight = 1
+++

![SQL 解析的核心 抽象语法树](https://shardingsphere.apache.org/document/current/img/db-compatibility/principle1.png)

- SQL 兼容

SQL 是使用者与数据库交流的标准语言。 SQL 解析引擎负责将 SQL 字符串解析为抽象语法树，供 Apache ShardingSphere 理解并实现其增量功能。

ShardingSphere 目前支持 MySQL, PostgreSQL, SQLServer, Oracle, openGauss 以及符合 SQL92 规范的 SQL 方言。 由于 SQL 语法的复杂性，目前仍然存在少量不支持的 SQL。

- 数据库协议兼容

Apache ShardingSphere 目前根据不同的数据协议，实现了 MySQL 和 PostgreSQL 协议。

- 特性支持

Apache ShardingSphere 为数据库提供了分布式协作的能力，同时将一部分数据库特性抽象到了上层，进行统一管理，以降低用户的使用难度。

因此，对于统一提供的特性，原生的 SQL 将不再下发到数据库，并提示该操作不被支持，用户可使用 ShardingSphere 提供的的方式进行代替。