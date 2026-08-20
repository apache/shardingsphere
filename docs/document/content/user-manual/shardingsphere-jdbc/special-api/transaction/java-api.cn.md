+++
title = "使用 Java API"
weight = 1
+++

## 背景信息

使用 ShardingSphere-JDBC 时，可以通过 API 的方式使用 XA 和 BASE 模式的事务。

## 前提条件

引入 ShardingSphere-JDBC Maven 依赖：

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

使用 XA 事务时，`shardingsphere-jdbc` 已传递依赖 XA 核心模块；还需要根据 `providerType` 选择一个 XA provider。

使用 Narayana：

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-transaction-xa-narayana</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

使用 Atomikos：

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-transaction-xa-atomikos</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

使用 BASE 事务时，需要同时引入 ShardingSphere Seata AT 集成模块和 Seata Client：

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-transaction-base-seata-at</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
<dependency>
    <groupId>org.apache.seata</groupId>
    <artifactId>seata-all</artifactId>
    <version>2.6.0</version>
    <exclusions>
        <exclusion>
            <groupId>org.antlr</groupId>
            <artifactId>antlr4-runtime</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

## 操作步骤

使用事务执行业务逻辑

## 配置示例

```java
// 使用 ShardingSphereDataSource 获取连接，执行事务操作
try (Connection connection = dataSource.getConnection()) {
    connection.setAutoCommit(false);
    PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO t_order (user_id, status) VALUES (?, ?)");
    preparedStatement.setObject(1, 1000);
    preparedStatement.setObject(2, "init");
    preparedStatement.executeUpdate();
    connection.commit();
}
```
