+++
pre = "<b>4.10. </b>"
title = "影子库"
weight = 10
+++

## 定义
Apache ShardingSphere 全链路在线压测场景下，在数据库层面对于压测数据治理的解决方案。

## 相关概念

### 生产库
生产环境使用的数据库。

### 影子库
压测数据隔离的影子数据库，与生产数据库应当使用相同的配置。

### 影子算法
影子算法和业务实现紧密相关，目前提供 2 种类型影子算法。

- 基于列的影子算法
  通过识别 SQL 中的数据，匹配路由至影子库的场景。
  适用于由压测数据名单驱动的压测场景。
  
- 基于 Hint 的影子算法
  通过识别 SQL 中的注释，匹配路由至影子库的场景。
  适用于由上游系统透传标识驱动的压测场景。
  
## 使用限制

### 基于 Hint 的影子算法
* 无。

### 基于列的影子算法
* 不支持 DDL；
* 不支持范围、分组和子查询，如：BETWEEN、GROUP BY ... HAVING 等。
  SQL 支持列表：
  - INSERT
  
  |  *SQL*  |  *是否支持*  |
  | ------- | ------------ |
  | INSERT INTO table (column,...) VALUES (value,...)   |  支持  |
  | INSERT INTO table (column,...) VALUES (value,...),(value,...),...   |  支持   |
  | INSERT INTO table (column,...) SELECT column1 from table1 where column1 = value1 |  不支持  |
  
  - SELECT/UPDATE/DELETE
  
  |  *条件类型*  |  *SQL*   |  *是否支持*  |
  | ------------ | -------- | ----------- |
  | =  | SELECT/UPDATE/DELETE ... WHERE column = value   | 支持 |
  | LIKE/NOT LIKE | SELECT/UPDATE/DELETE ... WHERE column LIKE/NOT LIKE value  | 支持  |                        
  | IN/NOT IN | SELECT/UPDATE/DELETE ... WHERE column IN/NOT IN (value1,value2,...)  | 支持 |
  | BETWEEN | SELECT/UPDATE/DELETE ... WHERE column BETWEEN value1 AND value2  | 不支持  |
  | GROUP BY ... HAVING... | SELECT/UPDATE/DELETE ... WHERE ... GROUP BY column HAVING column > value  | 不支持     |
  | 子查询  | SELECT/UPDATE/DELETE ... WHERE column = (SELECT column FROM table WHERE column = value) | 不支持  |
