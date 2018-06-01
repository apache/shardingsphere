+++
toc = true
title = "强制路由"
weight = 3
+++

## 基于暗示(Hint)的数据分片

### 实例化

```java
HintManager hintManager = HintManager.getInstance();
```

### 添加分片键值

- 使用hintManager.addDatabaseShardingValue来添加数据源分片键值。
- 使用hintManager.addTableShardingValue来添加表分片键值。

每种分片键值注册方法中有两个重载方法，参数较短的方法可以简化相等条件的分片值注入。

### 清除分片键值

分片键值保存在ThreadLocal中，所以需要在操作结束时调用hintManager.close()来清除ThreadLocal中的内容。

__hintManager实现了AutoCloseable接口，可推荐使用try with resource自动关闭。__

### 完整代码示例

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
            // ...
        }
    }
}
```

## 基于暗示(Hint)的强制主库路由

### 实例化

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
