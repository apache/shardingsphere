+++
title = "强制路由"
weight = 1
+++

## 简介

Apache ShardingSphere 使用 ThreadLocal 管理分片键值进行强制路由。
可以通过编程的方式向 HintManager 中添加分片值，该分片值仅在当前线程内生效。  
Apache ShardingSphere 还可以通过 SQL 中增加注释的方式进行强制路由。

Hint 的主要使用场景：

* 分片字段不存在 SQL 和数据库表结构中，而存在于外部业务逻辑。
* 强制在主库进行某些数据操作。
* 强制在指定数据库进行某些数据操作。

## 使用方法

### 使用 Hint 分片

#### 规则配置

Hint 分片算法需要用户实现 `org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingAlgorithm` 接口。
Apache ShardingSphere 在进行路由时，将会从 HintManager 中获取分片值进行路由操作。

参考配置如下：

```yaml
rules:
- !SHARDING
  tables:
    t_order:
      actualDataNodes: demo_ds_${0..1}.t_order_${0..1}
      databaseStrategy:
        hint:
          algorithmClassName: xxx.xxx.xxx.HintXXXAlgorithm
      tableStrategy:
        hint:
          algorithmClassName: xxx.xxx.xxx.HintXXXAlgorithm
  defaultTableStrategy:
    none:
  defaultKeyGenerateStrategy:
    type: SNOWFLAKE
    column: order_id

props:
    sql-show: true
```

#### 获取 HintManager

```java
HintManager hintManager = HintManager.getInstance();
```

#### 添加分片键值

- 使用 `hintManager.addDatabaseShardingValue` 来添加数据源分片键值。
- 使用 `hintManager.addTableShardingValue` 来添加表分片键值。

> 分库不分表情况下，强制路由至某一个分库时，可使用 `hintManager.setDatabaseShardingValue` 方式添加分片。

#### 清除分片键值

分片键值保存在 ThreadLocal 中，所以需要在操作结束时调用 `hintManager.close()` 来清除 ThreadLocal 中的内容。

__hintManager 实现了 AutoCloseable 接口，可推荐使用 try with resource 自动关闭。__

#### 完整代码示例

```java
// Sharding database and table with using HintManager
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

// Sharding database without sharding table and routing to only one database with using HintManager
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

### 使用 Hint 强制主库路由

#### 使用手动编程的方式

##### 获取 HintManager

与基于 Hint 的数据分片相同。

##### 设置主库路由

- 使用 `hintManager.setWriteRouteOnly` 设置主库路由。

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
            // ...
        }
    }
}
```

#### 使用 SQL 注释的方式

##### 使用规范

SQL Hint 功能需要用户提前开启解析注释的配置，设置 `sqlCommentParseEnabled` 为 `true`。
注释格式暂时只支持 `/* */`，内容需要以 `ShardingSphere hint:` 开始，属性名为 `writeRouteOnly`。

##### 完整示例
```sql
/* ShardingSphere hint: writeRouteOnly=true */
SELECT * FROM t_order;
```

### 使用 Hint 路由至指定数据库

#### 使用手动编程的方式

##### 获取 HintManager

与基于 Hint 的数据分片相同。

##### 设置路由至指定数据库

- 使用 `hintManager.setDataSourceName` 设置数据库名称。

##### 完整代码示例

```java
String sql = "SELECT * FROM t_order";
try (HintManager hintManager = HintManager.getInstance();
     Connection conn = dataSource.getConnection();
     PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
    hintManager.setDataSourceName("ds_0");
    try (ResultSet rs = preparedStatement.executeQuery()) {
        while (rs.next()) {
            // ...
        }
    }
}
```

#### 使用 SQL 注释的方式

##### 使用规范

SQL Hint 功能需要用户提前开启解析注释的配置，设置 `sqlCommentParseEnabled` 为 `true`，目前只支持路由至一个数据源。
注释格式暂时只支持 `/* */`，内容需要以 `ShardingSphere hint:` 开始，属性名为 `dataSourceName`。

##### 完整示例
```sql
/* ShardingSphere hint: dataSourceName=ds_0 */
SELECT * FROM t_order;
```
