+++
title = "YAML Configuration"
weight = 2
+++

## Introduction

YAML configuration provides interaction with ShardingSphere JDBC through configuration files. 
When used with the governance module together, the configuration of persistence in the configuration center is YAML format.

YAML configuration is the most common configuration mode, which can omit the complexity of programming and simplify user configuration.

## Usage

### Create Simple DataSource

The ShardingSphereDataSource created by YamlOrchestrationShardingSphereDataSourceFactory implements the standard JDBC DataSource interface.

```java
// Indicate YAML file path
File yamlFile = // ...

DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
```

### Create Orchestration DataSource

The OrchestrationShardingSphereDataSource created by YamlOrchestrationShardingSphereDataSourceFactory implements the standard JDBC DataSource interface.


```java
// Indicate YAML file path
File yamlFile = // ...

DataSource dataSource = YamlOrchestrationShardingSphereDataSourceFactory.createDataSource(yamlFile);
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

## YAML Syntax Explanation

`!!` means instantiation of that class

`!` means self-defined alias

`-` means one or multiple can be included

`[]` means array, can substitutable with `-` each other
