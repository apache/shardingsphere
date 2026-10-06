+++
title = "Java API"
weight = 2
chapter = true
+++

## Overview

Java API is the basic configuration methods in ShardingSphere-JDBC,
and other configurations will eventually be transformed into Java API configuration methods.

The Java API is the most complex and flexible configuration method, which is suitable for the scenarios requiring dynamic configuration through programming.

## Usage

### Import Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

### Create Data Source

ShardingSphere-JDBC Java API consists of database name, mode configuration, data source map, rule configurations and properties.

The ShardingSphereDataSource created by ShardingSphereDataSourceFactory implements the standard JDBC DataSource interface.

```java
String databaseName = "foo_schema"; // Indicate logic database name
ModeConfiguration modeConfig = ... // Build mode configuration
Map<String, DataSource> dataSourceMap = ... // Build actual data sources
Collection<RuleConfiguration> ruleConfigs = ... // Build concentrate rule configurations
Properties props = ... // Build properties
DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(databaseName, modeConfig, dataSourceMap, ruleConfigs, props);
```

Please refer to [Mode Configuration](/en/user-manual/shardingsphere-jdbc/java-api/mode) for more mode details.

Please refer to [Data Source Configuration](/en/user-manual/shardingsphere-jdbc/java-api/data-source) for more data source details.

Please refer to [Rules Configuration](/en/user-manual/shardingsphere-jdbc/java-api/rules) for more rule details.

### Use Data Source

Developer can choose to use native JDBC or ORM frameworks such as JPA, Hibernate or MyBatis through the DataSource.

Take native JDBC usage as an example:

```java
// Create ShardingSphereDataSource
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

### Execute DistSQL

ShardingSphere-JDBC supports executing DistSQL through JDBC `Statement` and through `PreparedStatement` using complete SQL strings without parameter placeholders.
The following example applies to the `5.5.4-SNAPSHOT` development version and uses a `Statement` to register a storage unit, create a sharding rule, and query the rule.
Ensure that ShardingSphere-JDBC and the H2 JDBC driver are on the classpath before running the example.

`ShardingSphereDataSourceFactory.createDataSource(databaseName, null)` creates a logical database without storage units or business rules.
When the mode configuration is `null`, Standalone mode with a Memory metadata repository is used by default, and configuration is not retained after the application restarts.

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

Use `executeUpdate` for non-query DistSQL and `executeQuery` for DistSQL that returns a result set, or use `execute` to determine whether a result set is returned.
Non-query DistSQL cannot execute within a transaction; use a connection with auto-commit enabled for these statements.
The availability of each statement depends on the DistSQL executors on the classpath and the running mode.
The default ShardingSphere-JDBC dependencies do not include the data pipeline executors required for migration and CDC.
For detailed syntax, see the [DistSQL Reference](/en/user-manual/shardingsphere-proxy/distsql/).
