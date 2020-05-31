+++
title = "强制路由"
weight = 3
+++

## 简介

ShardingSphere使用ThreadLocal管理分片键值进行Hint强制路由。可以通过编程的方式向HintManager中添加分片值，该分片值仅在当前线程内生效。
Hint方式主要使用场景：

1.分片字段不存在SQL中、数据库表结构中，而存在于外部业务逻辑。

2.强制在主库进行某些数据操作。

## 基于暗示(Hint)的数据分片

### 配置Hint分片算法

Hint分片算法需要用户实现`org.apache.shardingsphere.api.sharding.hint.HintShardingAlgorithm`接口。ShardingSphere在进行Routing时，如果发现LogicTable的`TableRule`采用了
Hint的分片算法，将会从`HintManager`中获取分片值进行路由操作。

参考配置如下：

```yaml
shardingRule:
  tables:
   t_order:
        actualDataNodes: demo_ds_${0..1}.t_order_${0..1}
        databaseStrategy:
          hint:
            algorithmClassName: org.apache.shardingsphere.userAlgo.HintAlgorithm
        tableStrategy:
          hint:
            algorithmClassName: org.apache.shardingsphere.userAlgo.HintAlgorithm
  defaultTableStrategy:
    none:
  defaultKeyGenerator:
    type: SNOWFLAKE
    column: order_id
props:
    sql.show: true
```

### 获取HintManager

```java
HintManager hintManager = HintManager.getInstance();
```

### 添加分片键值

- 使用hintManager.addDatabaseShardingValue来添加数据源分片键值。
- 使用hintManager.addTableShardingValue来添加表分片键值。

> 分库不分表情况下，强制路由至某一个分库时，可使用`hintManager.setDatabaseShardingValue`方式添加分片。通过此方式添加分片键值后，将跳过SQL解析和改写阶段，从而提高整体执行效率。

### 清除分片键值

分片键值保存在ThreadLocal中，所以需要在操作结束时调用hintManager.close()来清除ThreadLocal中的内容。

__hintManager实现了AutoCloseable接口，可推荐使用try with resource自动关闭。__

### 完整代码示例

```java
// Sharding database and table with using hintManager.
        String sql = "SELECT * FROM t_order";
        try (HintManager hintManager = HintManager.getInstance();
             Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            hintManager.addDatabaseShardingValue("t_order", 1);
            hintManager.addTableShardingValue("t_order", 2);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    // ...
                }
            }
        }

// Sharding database without sharding table and routing to only one database with using hintManger.
        String sql = "SELECT * FROM t_order";
        try (HintManager hintManager = HintManager.getInstance();
             Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            hintManager.setDatabaseShardingValue(3);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    // ...
                }
            }
        }
```

## 基于暗示(Hint)的强制主库路由

### 获取HintManager

与基于暗示(Hint)的数据分片相同。

### 设置主库路由

- 使用hintManager.setMasterRouteOnly设置主库路由。

### 清除分片键值

与基于暗示(Hint)的数据分片相同。

### 完整代码示例

```java
String sql = "SELECT * FROM t_order";
try (
        HintManager hintManager = HintManager.getInstance();
        Connection conn = dataSource.getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
    hintManager.setMasterRouteOnly();
    try (ResultSet rs = preparedStatement.executeQuery()) {
        while (rs.next()) {
            // ...
        }
    }
}
```

### example

[hint-example](https://github.com/apache/shardingsphere/tree/master/examples/shardingsphere-jdbc-example/other-feature-example/hint-example)
