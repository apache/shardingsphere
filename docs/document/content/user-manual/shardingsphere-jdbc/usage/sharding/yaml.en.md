+++
title = "Use YAML"
weight = 2
+++

## Import Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

## Configure Rule

ShardingSphere-JDBC YAML file consists of data sources, rules and properties configuration.
The following example is the configuration of 2 databases and 2 tables, 
whose databases take module and split according to `order_id`, tables take module and split according to `order_id`.

```yaml
# Configure actual data sources
dataSources:
  # Configure the first data source
  ds0: !!org.apache.commons.dbcp2.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds0
    username: root
    password:
  # Configure the second data source
  ds1: !!org.apache.commons.dbcp2.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds1
    username: root
    password: 

rules:
# Configure sharding rule
- !SHARDING
  tables:
    # Configure t_order table rule
    t_order: 
      actualDataNodes: ds${0..1}.t_order${0..1}
      # Configure database sharding strategy
      databaseStrategy:
        standard:
          shardingColumn: user_id
          shardingAlgorithmName: database_inline
      # Configure table sharding strategy
      tableStrategy:
        standard:
          shardingColumn: order_id
          shardingAlgorithmName: table_inline
    t_order_item: 
    # Omit t_order_item table rule configuration ...
    # ...
  
  # Configure sharding algorithms
  shardingAlgorithms:
    database_inline:
      type: INLINE
      props:
        algorithm-expression: ds${user_id % 2}
    table_inline:
      type: INLINE
      props:
        algorithm-expression: t_order_${order_id % 2}
```

```java
// Create ShardingSphereDataSource
DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
```

## Use ShardingSphereDataSource

The ShardingSphereDataSource created by YamlShardingSphereDataSourceFactory implements the standard JDBC DataSource interface.
Developer can choose to use native JDBC or ORM frameworks such as JPA or MyBatis through the DataSource.

Take native JDBC usage as an example:

```java
DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
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
