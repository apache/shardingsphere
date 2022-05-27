+++
pre = "<b>4.2. </b>"
title = "数据库网关"
weight = 2
chapter = true
+++

## 背景

随着数据库碎片化趋势的不可逆转，多种类型数据库的共存已渐成常态。使用一种 SQL 方言访问异构数据库的场景在不断增加。

## 挑战

多样化的数据库的存在，使访问数据库的 SQL 方言难于标准化，工程师需要针对不同种类的数据库使用不同的方言，缺乏统一化的查询平台。

将不同类型的数据库方言自动翻译为后端数据库所使用的方言，让工程师可以使用任意一种数据库方言访问所有的后端异构数据库，可以极大的降低开发和维护成本。

## 目标

**SQL 方言的自动翻译，是 Apache ShardingSphere 数据库网关希望达成的主要目标。**

## 现状

Apache ShardingSphere 的 SQL 方言翻译处于**实验阶段**。

目前已支持 MySQL/PostgreSQL 的方言自动翻译，工程师可以使用 MySQL 的方言和协议，访问 PostgreSQL 数据库，反之亦然。

**源码：https://github.com/apache/shardingsphere/tree/master/shardingsphere-kernel/shardingsphere-sql-translator**
