+++
date = "2016-02-05T17:03:18+08:00"
title = "基于暗示(Hint)的分片键值注册方法"
weight = 12
+++

# 基于暗示(Hint)的分片键值注册方法

> 提示:阅读本文前请详细预读 [使用指南](../user_guide)

## 背景
对`Shanrding-JDBC`有初步了解的朋友已经发现了：在编写分片算法的时候，传入的分片键值是来自`SQL`语句中`WHERE`条件的。
例如逻辑表`t_order`如果其数据源分片键为`user_id`，
分片算法是奇数值路由到`db1`偶数值路由到`db2`；表分片键为`order_id`，
分片算法是奇数值路由到`t_order_1`偶数值路由到`t_order_2`，如果执行如下sql语句：
```sql
select * from t_order where user_id = 1 and order_id = 2
```
那么在数据源分片算法的`shardingValue`参数将会传入`1`用于分片计算，结果为路由到`db1`;
表分片算法的`shardingValue`参数将会传入`2`用于分片计算，结果为路由到`t_order_2`。最终SQL为：
```sql
select * from db1.t_order_2 where user_id = 1 and order_id = 2
```

__现有一个假设，如果`WHERE`中没有`user_id`和`order_id`的条件，那么是否可以进行分片计算呢？__

答案是肯定的。下面就介绍一下`Sharding-JDBC`对这个问题的解决方法。

## 基于暗示(Hint)的分片键值管理器
要解决上面的问题，我们使用`com.dangdang.ddframe.rdb.sharding.api.HintShardingValueManager`。
该管理器是使用`ThreadLocal`技术管理分片键值的。
使用例子：
```java
String sql = "SELECT * FROM t_order";
        
try (
        Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
    HintShardingValueManager.init();
    HintShardingValueManager.registerShardingValueOfDatabase("t_order", "user_id", 1);
    HintShardingValueManager.registerShardingValueOfTable("t_order", "order_id", 2);
    try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
            ...
        }
    }
} finally {
    HintShardingValueManager.clear();
}
```

### 初始化方法
使用`HintShardingValueManager.init()`表示开始执行操作，此时会初始化`ThreadLocal`中的数据。

注意：__在一个线程中只能初始化一次__。否则会抛出`CAN NOT init repeatedly`的异常。

### 注册分片键值
- 使用`HintShardingValueManager.registerShardingValueOfDatabase`来注册数据源分片键值
- 使用`HintShardingValueManager.registerShardingValueOfTable`来注册表分片键值

每种分片键值注册方法中有两个重载方法，参数较短的方法可以简化相等条件的分片值注入。

### 清除注册的分片键值
由于分片键值保存在`ThreadLocal`中，所以需要在操作结束的使用调用`HintShardingValueManager.clear()`来清除`ThreadLocal`中的内容。
如果你没有调用该方法，那么在该线程被复用时（线程池场景），`HintShardingValueManager.init()`被调用将抛出`CAN NOT init repeatedly`异常。

__建议使用`try-finally`或者`AOP`编程的方式来调用清除方法__





