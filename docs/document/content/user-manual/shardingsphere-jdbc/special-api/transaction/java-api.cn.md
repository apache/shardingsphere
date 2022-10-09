+++
title = "使用 Java API"
weight = 1
+++

## 背景信息

使用 ShardingSphere-JDBC 时，可以通过 API 的方式使用 XA 和 BASE 模式的事务。

## 前提条件

引入 Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- 使用 XA 事务时，需要引入此模块 -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-transaction-xa-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- 使用 XA 的 Narayana模式时，需要引入此模块 -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-transaction-xa-narayana</artifactId>
    <version>${project.version}</version>
</dependency>

<!-- 使用 BASE 事务时，需要引入此模块 -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-transaction-base-seata-at</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```


## 操作步骤

1. 设置事务类型
2. 执行业务逻辑

## 配置示例

```java
TransactionTypeHolder.set(TransactionType.XA); // 支持 TransactionType.LOCAL, TransactionType.XA, TransactionType.BASE
        try (Connection conn = dataSource.getConnection()) { // 使用 ShardingSphereDataSource
        conn.setAutoCommit(false);
        PreparedStatement ps = conn.prepareStatement("INSERT INTO t_order (user_id, status) VALUES (?, ?)");
        ps.setObject(1, 1000);
        ps.setObject(2, "init");
        ps.executeUpdate();
        conn.commit();
        }
```
