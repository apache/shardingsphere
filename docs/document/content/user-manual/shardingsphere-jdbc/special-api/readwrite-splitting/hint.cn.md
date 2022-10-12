+++
title = "强制路由"
weight = 1
+++

## 背景信息

Apache ShardingSphere 使用 ThreadLocal 管理主库路由标记进行强制路由。 可以通过编程的方式向 HintManager 中添加主库路由标记，该值仅在当前线程内生效。
Apache ShardingSphere 还可以通过 SQL 中增加注释的方式进行主库路由。

Hint 在读写分离场景下，主要用于强制在主库进行某些数据操作。

## 操作步骤

1. 调用 `HintManager.getInstance()` 获取 HintManager 实例；
2. 调用 `HintManager.setWriteRouteOnly()` 方法设置主库路由标记；
3. 执行 SQL 语句完成路由和执行；
4. 调用 `HintManager.close()` 清理 ThreadLocal 中的内容。

## 配置示例

### 使用 Hint 强制主库路由

#### 使用手动编程的方式

##### 获取 HintManager

与基于 Hint 的数据分片相同。

##### 设置主库路由

使用 hintManager.setWriteRouteOnly 设置主库路由。

##### 清除分片键值

与基于 Hint 的数据分片相同。

##### 完整代码示例

```java
String sql = "SELECT * FROM t_order";
try (HintManager hintManager = HintManager.getInstance();
     Connection conn = dataSource.getConnection();
     PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
    hintManager.setWriteRouteOnly();
    try (ResultSet rs = preparedStatement.executeQuery()) {
        while (rs.next()) {
            // ...        }
    }
}
```

#### 使用 SQL 注释的方式

##### 使用规范

SQL Hint 功能需要用户提前开启解析注释的配置，设置 `sqlCommentParseEnabled` 为 `true`。 注释格式暂时只支持 `/* */`，内容需要以 `SHARDINGSPHERE_HINT:` 开始，属性名为 `writeRouteOnly`。

##### 完整示例

```java
/* SHARDINGSPHERE_HINT: WRITE_ROUTE_ONLY=true */
SELECT * FROM t_order;
```

- [核心特性：读写分离](/cn/features/readwrite-splitting/)
- [开发者指南：读写分离](/cn/dev-manual/readwrite-splitting/)
