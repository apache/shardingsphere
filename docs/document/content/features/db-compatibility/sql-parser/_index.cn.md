+++
title = "SQL 解析"
weight = 1
chapter = true
+++

SQL 是使用者与数据库交流的标准语言。
SQL 解析引擎负责将 SQL 字符串解析为抽象语法树，供 Apache ShardingSphere 理解并实现其增量功能。

目前支持 MySQL, PostgreSQL, SQLServer, Oracle, openGauss 以及符合 SQL92 规范的 SQL 方言。
由于 SQL 语法的复杂性，目前仍然存在少量不支持的 SQL。

本章节详细罗列出目前不支持的 SQL 种类，供使用者参考。

其中有未涉及到的 SQL 欢迎补充，未支持的 SQL 也尽量会在未来的版本中支持。
