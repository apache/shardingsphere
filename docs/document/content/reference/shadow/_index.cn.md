+++
pre = "<b>7.8. </b>"
title = "影子库"
weight = 8
+++

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
