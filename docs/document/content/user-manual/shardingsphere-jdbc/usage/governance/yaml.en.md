+++
title = "Use YAML"
weight = 2
+++

## Import Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-orchestration</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- import if using ZooKeeper -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-orchestration-repository-zookeeper-curator</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- import if using Etcd -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-orchestration-repository-etcd</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

## Configure Rule

Using ZooKeeper as config center and registry center for example.

```yaml
orchestration:
  name: orchestration_ds
  registryCenter:
      type: Zookeeper
      serverLists: localhost:2181
  overwrite: true
```

```java
// Create OrchestrationShardingSphereDataSource
DataSource dataSource = YamlOrchestrationShardingSphereDataSourceFactory.createDataSource(yamlFile);
```

## Use OrchestrationShardingSphereDataSource

The OrchestrationShardingSphereDataSource created by YamlOrchestrationShardingSphereDataSourceFactory implements the standard JDBC DataSource interface.
Developer can choose to use native JDBC or ORM frameworks such as JPA or MyBatis through the DataSource.

Take native JDBC usage as an example:

```java
DataSource dataSource = YamlOrchestrationShardingSphereDataSourceFactory.createDataSource(yamlFile);
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
