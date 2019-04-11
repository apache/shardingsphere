+++
toc = true
title = "路由"
weight = 2
+++
##路由<br>
路由是指将原SQL转换成在真实数据库真实表中可执行SQL的过程。<br>
例如：<br>
`SELECT * FROM t_order WHERE order_id IN (1, 2);`<br>
路由后成为<br>
`SELECT * FROM t_order_0 WHERE order_id IN (1, 2);`<br>
`SELECT * FROM t_order_1 WHERE order_id IN (1, 2);`<br>
路由按照是否使用分片键，可分为分片路由和广播路由。<br>
##分片路由<br>
分片路由细分为标准路由、笛卡尔积路由和直接路由这3种类型。<br>
 - 标准路由<br>
 下面主要介绍四种类型的标准路由。<br>
   - 单片路由：原SQL分片键的操作符是等号，映射后只有一条可执行SQL。<br>
   例如：<br>
`SELECT * FROM t_order WHERE order_id=2;`<br>
路由后成为<br>
`SELECT * FROM t_order_0 WHERE order_id=2;`<br>
   - 多片路由：原SQL分片键的操作符是IN，映射后有多条可执行SQL。<br>
   例如：<br>
`SELECT * FROM t_order WHERE order_id IN (1, 2);`<br>
路由后成为<br>
`SELECT * FROM t_order_0 WHERE order_id IN (1, 2);`<br>
`SELECT * FROM t_order_1 WHERE order_id IN (1, 2);`<br>
   - 范围路由：原SQL分片键的操作符是BETWEEN，映射后有多条可执行SQL。<br>
   例如：<br>
`SELECT * FROM t_order WHERE order_id BETWEEN (1, 12);`<br>
路由后成为<br>
`SELECT * FROM t_order_0 WHERE order_id BETWEEN (1, 12);`<br>
`SELECT * FROM t_order_1 WHERE order_id BETWEEN (1, 12);`<br>
   - 带有绑定表的多片路由：绑定表指分片规则一致的主表和子表。<br>
   例如：t_order表和t_order_item表，均按照order_id分片，则此两张表互为绑定表关系。绑定表之间的多表关联查询不会出现笛卡尔积关联，关联查询效率将大大提升。<br>
`SELECT * FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id  WHERE order_id IN (1, 2);`<br>
路由后成为<br>
`SELECT * FROM t_order_0 o JOIN t_order_item_0 i ON o.order_id=i.order_id  WHERE order_id IN (1, 2);`<br>
`SELECT * FROM t_order_1 o JOIN t_order_item_1 i ON o.order_id=i.order_id  WHERE order_id IN (1, 2);`<br>
与之相对应的例子就是下面要讲的笛卡尔积路由。<br>
  - 笛卡尔路由<br>
    是指在关联查询中，各个表的分片键不同，没有绑定表。导致转换后的可执行SQL时，真实表之间需要进行笛卡尔积组合生成大量SQL。<br>
    例如：<br>
 假设分片键order_id将数值10路由至第0片，将数值11路由至第1片，那么路由后的SQL应该为4条，它们呈现为笛卡尔积：<br>
 `SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);`<br>
 路由后成为<br>
 `SELECT i.* FROM t_order_0 o JOIN t_order_item_0 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);`<br>
 `SELECT i.* FROM t_order_0 o JOIN t_order_item_1 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);`<br>
 `SELECT i.* FROM t_order_1 o JOIN t_order_item_0 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);`<br>
 `SELECT i.* FROM t_order_1 o JOIN t_order_item_1 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);`<br>
 - 直接路由<br>
 满足直接路由的条件比较苛刻，一般通过hint方式实现。<br>
   - Hint路由<br>
它需要通过Hint（使用HintAPI直接指定路由至库表）方式分片，并且是只分库不分表的前提下，则可以避免SQL解析和之后的结果归并。直接路由还可以用于分片键不在SQL中的场景。<br>
例如，设置用于数据库分片的键为3，`hintManager.setDatabaseShardingValue(3)`。<br>
下方是使用API的代码样例：
```
String sql = "SELECT * FROM t_order";
try (HintManager hintManager = HintManager.getInstance();
Connection conn = dataSource.getConnection();
PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
	hintManager.setDatabaseShardingValue(3);
    try (ResultSet rs = preparedStatement.executeQuery()) {
        while (rs.next()) {
            //...
        }
    }
}
```
假如路由算法为’value%2‘。当一个逻辑库order_database对应2个真实库order_database_0,order_databse_1时，路由后sql将在order_databse_1上执行。<br>
##广播路由<br>
广播路由主要分为全库路由、全库表路由、全实例路由、单播路由和阻断路由。<br>
 - 全库路由<br>
用于处理对数据库的操作，包括用于库设置的SET类型的数据库管理命令，以及TCL这样的事务控制语句。在这种情况下，会根据逻辑库的名字遍历所有符合名字匹配的真实库，并在真实库中执行该命令。<br>
例如：执行`set autocommit=0;`。<br>
在order_database中执行,order_database有两个真实库。则实际会在order_database_0,order_database_1上都执行这个命令。<br>
 - 全库表路由<br>
用于处理对数据库中与其逻辑表相关的所有真实表的操作，主要包括不带分片键的DQL和DML，以及DDL等。<br>
例如：执行`SELECT * FROM t_order WHERE good_prority IN (1, 10);`。<br>
则会遍历所有数据库中的所有表，逐一匹配逻辑表和真实表名，能够匹配得上则执行。<br>
路由后成为<br>
`SELECT * FROM t_order_0 WHERE good_prority IN (1, 10);`<br>
`SELECT * FROM t_order_1 WHERE good_prority IN (1, 10);`<br>
`SELECT * FROM t_order_2 WHERE good_prority IN (1, 10);`<br>
`SELECT * FROM t_order_3 WHERE good_prority IN (1, 10);`<br>
 - 全实例路由<br>
用于DCL操作，授权语句针对的是数据库的实例。无论一个实例中包含多少个Schema，每个数据库的实例执行一次。<br>
例如：执行`create user customer@10.98.17.138 identified by '123';`。<br>
这个命令将在所有的真实数据库实例中执行，以确保customer用户可以访问每一个实例。<br>
 - 单播路由<br>
用于获取某一真实表信息的场景，它仅需要从任意库中的任意真实表中获取数据即可。<br>
例如：执行`DESCRIBE t_order;`。<br>
t_order的两个真实表t_order_0,t_order_1的描述结构相同，所以这个命令在任意真实表上选择执行一次。<br>
 - 阻断路由<br>
用于屏蔽SQL对数据库的操作。<br>
例如：执行`USE order_db;`。<br>
这个命令不会在真实数据库中执行，因为ShardingSphere采用的是逻辑Schema的方式，无需将切换数据库Schema的命令发送至数据库中。<br>