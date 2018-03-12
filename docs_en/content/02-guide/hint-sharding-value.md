+++
toc = true
title = "Mandatory Routing"
weight = 7
prev = "/02-guide/orchestration"
next = "/02-guide/key-generator"

+++

> Notice: Please read [Sharding](/02-guide/sharding) carefully before reading this chapter. 

## Background

After reading the previous introduction, you can notice that the sharding columns in the sharding algorithm always come from the WHERE condition in the SQLs.
For example: for the logical table t_order, if the sharding column of the data source is user_id, the record with the odd number of user_id will be routed to db1, and the one with the even value will be routed to db2. If the sharding column of its table is order_id, the record with the odd number of order_id is routed to t_order_1, and the one with the even value is routed to t_order_2.
So if you execute the following SQL statement:

```sql
select * from t_order where user_id = 1 and order_id = 2
```

Then, 1 will be the shardingValue of sharding algorithm of data source, similarly 2 will be the shardingValue parameter of the table sharding algorithm to figure out that the result record is routed to db1.t_order_2.
The final SQL is:

```sql
select * from db1.t_order_2 where user_id = 1 and order_id = 2
```

__Suppose there are no sharding columns like user_id, order_id in WHERE, can the Sharding be proceeded normally?__

The answer is No problem. How? Please continue to read.

## The manager of sharding columns based on Hint
To solve the above problem, we need to use com.dangdang.ddframe.rdb.sharding.api.HintManager which uses ThreadLocal to manage sharding columns.

For example:

```java
String sql = "SELECT * FROM t_order";
        
try (
        HintManager hintManager = HintManager.getInstance();
        Connection conn = dataSource.getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
    hintManager.addDatabaseShardingValue("t_order", "user_id", 1);
    hintManager.addTableShardingValue("t_order", "order_id", 2);
    try (ResultSet rs = preparedStatement.executeQuery()) {
        while (rs.next()) {
            ...
        }
    }
}
```

### Instantiation

```java
// to  initialize the data in ThreadLocal.
HintManager hintManager = HintManager.getInstance()
```

### Add Sharding columns and corresponding data
- To add Sharding columns and corresponding data of data source by hintManager.addDatabaseShardingValue.
- To add Sharding columns and corresponding data of table by hintManager.addTableShardingValue.

There are two overload methods of the registration for each kind of sharding, and the shorter method can simplify the sharding injection of the = condition.

### Clean added Sharding columns and corresponding data
The added Sharding columns and corresponding data are saved in ThreadLocal, so you need to clean the content of the ThreadLocal by calling hintManager.close() after the end of the operations.

__hintManager implement AutoCloseable interface, and it is recommended to use the *Try with resource* to clean automatically.__
