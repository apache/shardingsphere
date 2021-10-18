+++
pre = "<b>3.7.2. </b>"
title = "实现原理"
weight = 2
+++

## 整体架构

Apache ShardingSphere 通过解析 SQL，对传入的 SQL 进行影子判定，根据配置文件中用户设置的影子规则，路由到生产库或者影子库。

![执行流程](https://shardingsphere.apache.org/document/current/img/shadow/execute.png)

## 影子规则

影子规则包含影子数据源映射关系，影子表以及影子算法。

![规则](https://shardingsphere.apache.org/document/current/img/shadow/rule_cn.png)

**影子库开关**：影子库功能开关，默认值 `false`。可选值 `true`/`false`

**影子库映射**：生产数据源名称和影子数据源名称映射关系。

**影子表**：压测相关的表，影子库中必须包含影子表。影子表需要指定对应的影子库映射和影子算法。

**影子算法**：SQL 路由影子算法。

## 路由过程

以 INSERT 语句为例，在写入数据时，Apache ShardingSphere 会对 SQL 进行解析，再根据配置文件中的规则，构造一条路由链。在当前版本的功能中，
影子功能处于路由链中的最后一个执行单元，即，如果有其他需要路由的规则存在，如分片，Apache ShardingSphere 会首先根据分片规则，路由到某一个数据库，再
执行影子路由判定流程，判定执行SQL满足影子规则的配置，数据路由到与之对应的影子库，生产数据则维持不变。

## 影子判定流程
影子库开关开启时，会对执行的 SQL 语句进行影子判定。影子判定目前支持两种类型算法，用户可根据实际业务需求选择一种或者组合使用。

### DML 语句

支持两种算法。影子判定会首先判断执行 SQL 关联的表是否和影子表有交集。如果有交集，对交集部分影子表关联的影子算法依次判定。如果影子表关联影子算法有任何一个判定成功。SQL 语句路由到影子库。
没有交集或者影子算法判定不成功，SQL 语句路由到生产库。

### DDL 语句

仅支持注解影子算法。一般不会对 DDL 语句的压力测试。主要做为影子库环境的初始化或者影子表调整时执行。

影子判定会首先判断执行 SQL 是否包含注解，如果包含注解对影子规则中的注解影子算法依次判定。如果注解影子算法有任何一个判定成功。SQL 语句路由到影子库。
没有 SQL 不包含注解或者注解影子算法判定不成功，路由到生产库。

## 影子算法

影子算法详情，请参见[内置影子算法列表](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/shadow)

## 使用案例

### 场景需求

假设一个电商网站要对下单业务进行压测，对订单表 `t_order` 进行压测。生产数据执行到生产库，即：ds。测试数据执行到影子库，即：ds-shadow。

### 影子库配置

建议配置如下（YAML 格式展示）：

```yaml
enable: true
  data-sources:
    shadow-data-source:
      source-data-source-name: ds
      shadow-data-source-name: ds-shadow
tables:
  t_order:
    data-source-names: shadow-data-source
    shadow-algorithm-names:
      - simple-note-algorithm
      - user-id-match-algorithm
shadow-algorithms:
  simple-note-algorithm:
    type: SIMPLE_NOTE
    props:
      shadow: true
      foo: bar
  user-id-match-algorithm:
    type: COLUMN_REGEX_MATCH
    props:
      operation: insert
      column: user_id
      regex: "[0]"
      
props:
  sql-comment-parse-enabled: true
```

**注意**：
- 如果使用注解影子算法，需要打开解析 SQL 注释配置项 `sql-comment-parse-enabled: true`。默认为关闭状态。
  请参考[属性配置]( https://shardingsphere.apache.org/document/current/cn/user-manual/shardingsphere-jdbc/configuration/props/)


### 影子库环境准备

* 创建影子库 `ds-shadow`。

* 创建压测相关影子表，影子表结构与生产环境对应表结构必须一致。假设需要在影子库创建 `t_order` 表。创建表语句需要添加 SQL 注释 `/*shadow:true,foo:bar,...*/`。即：
```sql
CREATE TABLE t_order (order_id INT(11) primary key, user_id int(11) not null, ...) /*shadow:true,foo:bar,...*/
``` 
执行到影子库。

### 影子算法使用
   
1. 列影子算法使用

假设 `t_order` 表中包含字段下单用户ID的 `user_id`。 如果实现的效果，当用户ID为 `0` 的用户创建订单产生的数据。 即：
```sql
INSERT INTO t_order (order_id, user_id, ...) VALUES (xxx..., 0, ...)
```
会执行到影子库，其他数据执行到生产库。

无需修改任何 SQL 或者代码，只需要对压力测试的数据进行控制就可以实现在线的压力测试。

算法配置如下（YAML 格式展示）：

```yaml
shadow-algorithms:
  user-id-match-algorithm:
    type: COLUMN_REGEX_MATCH
    props:
      operation: insert
      column: user_id
      regex: "[0]"
```

**注意**：影子表使用列影子算法时，相同类型操作（INSERT, UPDATE, DELETE, SELECT）目前仅支持单个字段。

2. 使用注解影子算法

假设 `t_order` 表中没有存储可以对值进行控制的列。或者控制的值不包含在执行 SQL 的中。可以添加一条注解到执行的 SQL 中，即：
```sql
SELECT * FROM t_order WHERE order_id = xxx /*shadow:true,foo:bar,...*/ 
```
会执行到影子库。

算法配置如下（YAML 格式展示）：

```yaml
shadow-algorithms:
  simple-note-algorithm:
    type: SIMPLE_NOTE
    props:
      shadow: true
      foo: bar
```

3. 混合使用影子模式

假设对 `t_order` 表压测以上两种场景都需要覆盖。 即，

```sql
INSERT INTO t_order (order_id, user_id, ...) VALUES (xxx..., 0, ...);

SELECT * FROM t_order WHERE order_id = xxx /*shadow:true,foo:bar,...*/;
```
满足对复杂场景压力测试支持。

算法配置如下（YAML 格式展示）：

```yaml
shadow-algorithms:
  user-id-match-algorithm:
    type: COLUMN_REGEX_MATCH
    props:
      operation: insert
      column: user_id
      regex: "[0]"
  simple-note-algorithm:
    type: SIMPLE_NOTE
    props:
      shadow: true
      foo: bar
```
