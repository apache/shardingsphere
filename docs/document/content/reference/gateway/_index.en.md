+++
pre = "<b>7.2. </b>"
title = "Database Gateway"
weight = 2
+++

Apache ShardingSphere provides the ability for SQL dialect translation to achieve automatic conversion between database dialects. For example, users can use MySQL client to connect ShardingSphere and send SQL based on MySQL dialect. ShardingSphere can automatically identify user protocol and storage node type, automatically complete SQL dialect conversion, and access heterogeneous storage nodes such as PostgreSQL.

![Gateway](https://shardingsphere.apache.org/document/current/img/gateway/gateway_en.png)