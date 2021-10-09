+++
pre = "<b>3.7.3. </b>"
title = "使用规范"
weight = 3
+++

## 支持项

* 后端数据库为 MySQL、Oracle、PostgreSQL、SQLServer；
* 支持MDL，DDL语句；
* 兼容所有常用SQL；

## 不支持项

* 影子字段无法支持范围值匹配操作，如：BETWEEN、HAVING、subQuery等；
* 使用影子库功能+分库分表功能，部分特殊SQL不支持，请参考[SQL使用规范]( https://shardingsphere.apache.org/document/current/cn/features/sharding/use-norms/sql/)；
* DDL语句不支持列影子算法；
