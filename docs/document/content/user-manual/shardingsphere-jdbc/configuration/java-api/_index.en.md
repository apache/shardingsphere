+++
title = "Java API"
weight = 1
chapter = true
+++

## Introduction

Java API is the foundation of all configuration methods in ShardingSphere-JDBC, 
and other configurations will eventually be transformed into Java API configuration methods.

The Java API is the most complex and flexible configuration method, which is suitable for the scenarios requiring dynamic configuration through programming.

## Usage

### Create Simple DataSource

The ShardingSphereDataSource created by ShardingSphereDataSourceFactory implements the standard JDBC DataSource interface.

```java
// Build data source map
Map<String, DataSource> dataSourceMap = // ...

// Build rule configurations
Collection<RuleConfiguration> configurations = // ...

// Build properties
Properties props = // ...

DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, configurations, props);
```

### Create Governance DataSource

The GovernanceShardingSphereDataSource created by GovernanceShardingSphereDataSourceFactory implements the standard JDBC DataSource interface.


```java
// Build data source map
Map<String, DataSource> dataSourceMap = // ...

// Build rule configurations
Collection<RuleConfiguration> configurations = // ...

// Build properties
Properties props = // ...

// Build governance configuration
GovernanceConfiguration governanceConfig = // ...

DataSource dataSource = GovernanceShardingSphereDataSourceFactory.createDataSource(dataSourceMap, configurations, props, governanceConfig);
```

### Use DataSource

Developer can choose to use native JDBC or ORM frameworks such as JPA or MyBatis through the DataSource.

Take native JDBC usage as an example:

```java
DataSource dataSource = // Use Apache ShardingSphere factory to create DataSource
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
