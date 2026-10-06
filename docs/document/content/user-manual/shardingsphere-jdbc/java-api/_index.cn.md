+++
title = "Java API"
weight = 2
chapter = true
+++

## 简介

Java API 是 ShardingSphere-JDBC 中所有配置方式的基础，其他配置最终都将转化成为 Java API 的配置方式。

Java API 是最繁琐也是最灵活的配置方式，适合需要通过编程进行动态配置的场景下使用。

## 使用步骤

### 引入 Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### 构建数据源

ShardingSphere-JDBC 的 Java API 由 Database 名称、运行模式、数据源集合、规则集合以及属性配置组成。

通过 ShardingSphereDataSourceFactory 工厂创建的 ShardingSphereDataSource 实现自 JDBC 的标准接口 DataSource。

```java
String databaseName = "foo_schema"; // 指定逻辑 Database 名称
ModeConfiguration modeConfig = ... // 构建运行模式
Map<String, DataSource> dataSourceMap = ... // 构建真实数据源
Collection<RuleConfiguration> ruleConfigs = ... // 构建具体规则
Properties props = ... // 构建属性配置
DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(databaseName, modeConfig, dataSourceMap, ruleConfigs, props);
```

模式详情请参见[模式配置](/cn/user-manual/shardingsphere-jdbc/java-api/mode)。

数据源详情请参见[数据源配置](/cn/user-manual/shardingsphere-jdbc/java-api/data-source)。

规则详情请参见[规则配置](/cn/user-manual/shardingsphere-jdbc/java-api/rules)。

### 使用数据源

可通过 DataSource 选择使用原生 JDBC，或 JPA、Hibernate、MyBatis 等 ORM 框架。

以原生 JDBC 使用方式为例：

```java
// 创建 ShardingSphereDataSource
DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(databaseName, modeConfig, dataSourceMap, ruleConfigs, props);

String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=? AND o.order_id=?";
try (
        Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setInt(1, 10);
    ps.setInt(2, 1000);
    try (ResultSet rs = preparedStatement.executeQuery()) {
        while(rs.next()) {
            // ...
        }
    }
}
```

### 执行 DistSQL

ShardingSphere-JDBC 支持通过 JDBC `Statement` 执行 DistSQL，也支持不含参数占位符的 `PreparedStatement`。
以下示例适用于 `5.5.4-SNAPSHOT` 开发版本，使用 `Statement` 注册存储单元、创建分片规则并查询规则。
运行示例前，请确保类路径中包含 ShardingSphere-JDBC 及 H2 JDBC 驱动。

`ShardingSphereDataSourceFactory.createDataSource(databaseName, null)` 创建不含存储单元和业务规则的逻辑库。
运行模式为 `null` 时，默认使用 Standalone 模式和 Memory 元数据仓库，配置不会在应用重启后保留。

```java
DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource("jdbc_distsql_demo", null);
try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
    statement.executeUpdate("REGISTER STORAGE UNIT ds_0 (URL='jdbc:h2:mem:jdbc_distsql_demo;MODE=MySQL',USER='sa',PASSWORD='',"
            + "PROPERTIES('driverClassName'='org.h2.Driver'))");
    statement.executeUpdate("CREATE SHARDING TABLE RULE t_order (STORAGE_UNITS(ds_0),"
            + "SHARDING_COLUMN=user_id,TYPE(NAME='MOD',PROPERTIES('sharding-count'='1')),"
            + "KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME='SNOWFLAKE')))");
    try (ResultSet resultSet = statement.executeQuery("SHOW SHARDING TABLE RULES")) {
        while (resultSet.next()) {
            System.out.println(resultSet.getString("table"));
        }
    }
} finally {
    ((AutoCloseable) dataSource).close();
}
```

使用 `executeUpdate` 执行非查询 DistSQL，使用 `executeQuery` 执行返回结果集的 DistSQL，也可以使用 `execute` 判断是否返回结果集。
非查询 DistSQL 不能在事务中执行，应使用自动提交的连接执行此类语句。
具体语句的可用性取决于类路径中的 DistSQL 执行器和运行模式。
ShardingSphere-JDBC 的默认依赖不包含数据迁移和 CDC 所需的数据管道执行器。
详细语法请参见 [DistSQL 参考](/cn/user-manual/shardingsphere-proxy/distsql/)。
