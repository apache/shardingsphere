+++
title = "Java API"
weight = 1
+++

## Import Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

## Build Data Source

ShardingSphere-JDBC Java API consists of schema name, mode configuration, data source map, rule configurations and properties.

### Build Mode Configuration

Mode configuration include 3 parameters, which are mode type, repository configuration and whether overwrite of local configuration to persist repository.

Mode include memory, standalone and cluster.

There are different repository configuration for each mode:

- Memory mode do not need repository configuration;
- Standalone and cluster mode need to customized repository configuration.

Example for each mode are:

- Memory Mode

```java
ModeConfiguration modeConfig = new ModeConfiguration("Memory", null, true);
```

- Standalone Mode

```java
StandalonePersistRepositoryConfiguration repositoryConfig = new StandalonePersistRepositoryConfiguration("Local", new Properties());
ModeConfiguration modeConfig = new ModeConfiguration("Standalone", repositoryConfig, false);
```

- Cluster Mode

```java
ClusterPersistRepositoryConfiguration repositoryConfig = new ClusterPersistRepositoryConfiguration("ZooKeeper", "my_schema", "localhost:2181", new Properties());
ModeConfiguration modeConfig = new ModeConfiguration("Cluster", repositoryConfig, false);
```

### Build Physical Data Sources

The data sour
The example connection pool is HikariCP, which can be replaced with other connection pools according to business scenarios.

```java
Map<String, DataSource> dataSourceMap = new HashMap<>();

// Configure the 1st data source
HikariDataSource dataSource1 = new HikariDataSource();
dataSource1.setDriverClassName("com.mysql.jdbc.Driver");
dataSource1.setJdbcUrl("jdbc:mysql://localhost:3306/ds_1");
dataSource1.setUsername("root");
dataSource1.setPassword("");
dataSourceMap.put("ds_1", dataSource1);

// Configure the n data source
HikariDataSource dataSourceN = new HikariDataSource();
dataSourceN.setDriverClassName("com.mysql.jdbc.Driver");
dataSourceN.setJdbcUrl("jdbc:mysql://localhost:3306/ds_n");
dataSourceN.setUsername("root");
dataSourceN.setPassword("");
dataSourceMap.put("ds_n", dataSourceN);
```

### Build Rules

Rules belong to pluggable features, please reference rule configuration manual. 

### Build ShardingSphere Data Source

```java
ModeConfiguration modeConfig = ... // Build mode
Map<String, DataSource> dataSourceMap = ... // Build physical data sources
Collection<RuleConfiguration> ruleConfigs = ... // Build rules
Properties props = ... // Build properties
DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource("my_schema", modeConfig, dataSourceMap, ruleConfigs, props);
```

## Use ShardingSphere Data Source

The ShardingSphereDataSource created by ShardingSphereDataSourceFactory implements the standard JDBC DataSource interface.
Developer can choose to use native JDBC or ORM frameworks such as JPA, Hibernate or MyBatis through the DataSource.

Take native JDBC usage as an example:

```java
DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource("my_schema", modeConfig, dataSourceMap, ruleConfigs, props);
String sql = "SELECT xxx FROM xxx WHERE xxx=? AND xxx=?";
try (
        Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setInt(1, 10);
    ps.setInt(2, 1000);
    try (ResultSet rs = ps.executeQuery()) {
        while(rs.next()) {
            // ...
        }
    }
}
```
