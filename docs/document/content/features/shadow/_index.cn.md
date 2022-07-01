+++
pre = "<b>7.6. </b>"
title = "影子库"
weight = 6
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
  | LIKE/NOT LIKE | SELECT/UPDATE/DELETE ... WHERE column LIKE/NOT LIKE value  | 支持  |                        | IN/NOT IN | SELECT/UPDATE/DELETE ... WHERE column IN/NOT IN (value1,value2,...)  | 支持 |
  | BETWEEN | SELECT/UPDATE/DELETE ... WHERE column BETWEEN value1 AND value2  | 不支持  |
  | GROUP BY ... HAVING... | SELECT/UPDATE/DELETE ... WHERE ... GROUP BY column HAVING column > value  | 不支持     |
  | 子查询  | SELECT/UPDATE/DELETE ... WHERE column = (SELECT column FROM table WHERE column = value) | 不支持  |

## 原理介绍
Apache ShardingSphere 通过解析 SQL，对传入的 SQL 进行影子判定，根据配置文件中用户设置的影子规则，路由到生产库或者影子库。
![执行流程](https://shardingsphere.apache.org/document/current/img/shadow/execute.png)

以 INSERT 语句为例，在写入数据时，Apache ShardingSphere 会对 SQL 进行解析，再根据配置文件中的规则，构造一条路由链。 在当前版本的功能中，影子功能处于路由链中的最后一个执行单元，即，如果有其他需要路由的规则存在，如分片，Apache ShardingSphere 会首先根据分片规则，路由到某一个数据库，再执行影子路由判定流程，判定执行SQL满足影子规则的配置，数据路由到与之对应的影子库，生产数据则维持不变。

### DML 语句
支持两种算法。影子判定会首先判断执行 SQL 相关表与配置的影子表是否有交集。如果有交集，依次判定交集部分影子表关联的影子算法，有任何一个判定成功。SQL 语句路由到影子库。
影子表没有交集或者影子算法判定不成功，SQL 语句路由到生产库。

### DDL 语句
仅支持注解影子算法。在压测场景下，DDL 语句一般不需要测试。主要在初始化或者修改影子库中影子表时使用。
影子判定会首先判断执行 SQL 是否包含注解。如果包含注解，影子规则中配置的 HINT 影子算法依次判定。有任何一个判定成功。SQL 语句路由到影子库。
执行 SQL 不包含注解或者 HINT 影子算法判定不成功，SQL 语句路由到生产库。

## 相关参考
[JAVA API：影子库配置](/cn/user-manual/shardingsphere-jdbc/java-api/rules/shadow/)

[YAML 配置：影子库配置](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/shadow/)

[ Spring Boot Starter：影子库配置 ](/cn/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/shadow/)

[Spring 命名空间：影子库配置](/cn/user-manual/shardingsphere-jdbc/spring-namespace/rules/shadow/)
