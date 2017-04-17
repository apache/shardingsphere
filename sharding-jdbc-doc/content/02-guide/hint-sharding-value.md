+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "强制路由"
weight = 6
prev = "/02-guide/configuration"
next = "/02-guide/id-generator"

+++

> 提示:阅读本文前请详细预读 [分库分表](/02-guide/sharding)

## 背景
对Sharding-JDBC有初步了解的朋友已经发现了：在编写分片算法的时候，传入的分片键值是来自SQL语句中WHERE条件的。
例如逻辑表t_order如果其数据源分片键为user_id，
分片算法是奇数值路由到db1偶数值路由到db2；表分片键为order_id，
分片算法是奇数值路由到t_order_1偶数值路由到t_order_2，如果执行如下sql语句：

```sql
select * from t_order where user_id = 1 and order_id = 2
```

那么在数据源分片算法的shardingValue参数将会传入1用于分片计算，结果为路由到db1;
表分片算法的shardingValue参数将会传入2用于分片计算，结果为路由到t_order_2。最终SQL为：

```sql
select * from db1.t_order_2 where user_id = 1 and order_id = 2
```

__现有一个假设，如果WHERE中没有user_id和order_id的条件，那么是否可以进行分片计算呢？__

答案是肯定的。下面就介绍一下Sharding-JDBC对这个问题的解决方法。

## 基于暗示(Hint)的分片键值管理器
要解决上面的问题，我们使用com.dangdang.ddframe.rdb.sharding.api.HintManager。
该管理器是使用ThreadLocal技术管理分片键值的。
使用例子：

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

### 实例化

```java
// 初始化ThreadLocal中的数据
HintManager hintManager = HintManager.getInstance()
```



### 添加分片键值
- 使用hintManager.addDatabaseShardingValue来添加数据源分片键值
- 使用hintManager.addTableShardingValue来添加表分片键值

每种分片键值注册方法中有两个重载方法，参数较短的方法可以简化相等条件的分片值注入。

### 清除添加的分片键值
分片键值保存在ThreadLocal中，所以需要在操作结束时调用hintManager.close()来清除ThreadLocal中的内容。

__hintManager实现了AutoCloseable接口，可推荐使用try with resource自动关闭。__
