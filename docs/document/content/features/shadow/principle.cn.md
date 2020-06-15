+++
pre = "<b>3.7.2. </b>"
title = "实现原理"
weight = 2
+++

### 整体架构

Apache ShardingSphere 通过解析 SQL，根据配置文件中用户设置的影子规则，对传入的 SQL 进行路由并改写，删除影子字段与字段值。用户无需关注具体过程，
使用时仅对 SQL 进行相应改造，添加影子字段与相应的配置即可。

![执行流程](https://shardingsphere.apache.org/document/current/img/shadow/execute.png)

### 影子规则

影子规则包含影子字段及映射关系。

![规则](https://shardingsphere.apache.org/document/current/img/shadow/rule_cn.png)

### 处理过程

以 INSERT 语句为例，在写入数据时，Apache ShardingSphere 会对 SQL 进行解析，再根据配置文件中的规则，构造一条路由链。在当前版本的功能中，
影子功能处于路由链中的最后一个执行单元，即，如果有其他需要路由的规则存在，如分片，Apache ShardingSphere 会首先根据分片规则，路由到某一个数据库，再
执行影子路由，将影子数据路由到与之对应的影子库，生产数据则维持不变。

接着对 SQL 进行改写，由于影子字段为逻辑字段，在数据库中实际不存在，所以在改写过程中会删除这个字段及其对应的参数。

DML 语句的处理过程同理，对于非 DML 语句，如创建数据表等，会在生产数据库与影子数据库分别执行。

![sql过程](https://shardingsphere.apache.org/document/current/img/shadow/sql.png)

