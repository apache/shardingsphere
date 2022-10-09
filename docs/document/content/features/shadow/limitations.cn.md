+++
title = "使用限制"
weight = 2
+++

## 基于 Hint 的影子算法

* 无。

## 基于列的影子算法

* 不支持 DDL；
* 不支持范围、分组和子查询，如：BETWEEN、GROUP BY ... HAVING 等。
  SQL 支持列表：
  - INSERT
  
  |  *SQL*                                                                           |  *是否支持*  |
  | -------------------------------------------------------------------------------- | ----------- |
  | INSERT INTO table (column,...) VALUES (value,...)                                |  支持       |
  | INSERT INTO table (column,...) VALUES (value,...),(value,...),...                |  支持       |
  | INSERT INTO table (column,...) SELECT column1 from table1 where column1 = value1 |  不支持     |
  
  - SELECT/UPDATE/DELETE
  
  |  *条件类型*             |  *SQL*                                                                                   |  *是否支持*  |
  | ---------------------- | ---------------------------------------------------------------------------------------- | ----------- |
  | =                      | SELECT/UPDATE/DELETE ... WHERE column = value                                            | 支持         |
  | LIKE/NOT LIKE          | SELECT/UPDATE/DELETE ... WHERE column LIKE/NOT LIKE value                                | 支持         |                        
  | IN/NOT IN              | SELECT/UPDATE/DELETE ... WHERE column IN/NOT IN (value1,value2,...)                      | 支持         |
  | BETWEEN                | SELECT/UPDATE/DELETE ... WHERE column BETWEEN value1 AND value2                          | 不支持       |
  | GROUP BY ... HAVING... | SELECT/UPDATE/DELETE ... WHERE ... GROUP BY column HAVING column > value                 | 不支持       |
  | 子查询                  | SELECT/UPDATE/DELETE ... WHERE column = (SELECT column FROM table WHERE column = value)  | 不支持       |
