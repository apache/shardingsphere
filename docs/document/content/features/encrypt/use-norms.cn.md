+++
pre = "<b>3.6.3. </b>"
title = "使用规范"
weight = 3
+++

## 支持项

* 后端数据库为 MySQL、Oracle、PostgreSQL、SQLServer；
* 用户需要对数据库表中某个或多个列进行加密（数据加密 & 解密）；
* 兼容所有常用SQL。

## 不支持项

* 用户需要自行处理数据库中原始的存量数据、洗数；
* 使用加密功能+分库分表功能，部分特殊SQL不支持，请参考[SQL使用规范]( https://shardingsphere.apache.org/document/current/cn/features/sharding/use-norms/sql/)；
* 加密字段无法支持比较操作，如：大于小于、ORDER BY、BETWEEN、LIKE等；
* 加密字段无法支持计算操作，如：AVG、SUM以及计算表达式。
