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

The ShardingSphereDataSource created by YamlGovernanceShardingSphereDataSourceFactory implements the standard JDBC DataSource interface.

```java
// Indicate YAML file path
File yamlFile = // ...

DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
```

### Create Governance DataSource

The GovernanceShardingSphereDataSource created by YamlGovernanceShardingSphereDataSourceFactory implements the standard JDBC DataSource interface.


```java
// Indicate YAML file path
File yamlFile = // ...

DataSource dataSource = YamlGovernanceShardingSphereDataSourceFactory.createDataSource(yamlFile);
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

## YAML Configuration Item

### Data Source Configuration

It is divided into single data source configuration and multi data source configuration.
Single data source configuration used for data encryption rules; and multi data source configuration used for fragmentation, primary-replica replication and other rules.
If features such as encryption and sharding are used in combination, a multi data source configuration should be used.

#### Single Data Source Configuration

##### Configuration Example

```yaml
dataSource: !!org.apache.commons.dbcp2.BasicDataSource
  driverClassName: com.mysql.jdbc.Driver
  url: jdbc:mysql://127.0.0.1:3306/ds_name
  username: root
  password: root
```

##### Configuration Item Explanation

```yaml
dataSource: # <!!Data source pool implementation class> `!!` means class instantiation
  driverClassName: # Class name of database driver
  url: # Database URL
  username: # Database username
  password: # Database password
    # ... Other properties for data source pool
```

#### Multi Data Source Configuration

##### Configuration Example

```yaml
dataSources:
  ds_0: !!org.apache.commons.dbcp2.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/ds_0
    username: sa
    password:
  ds_1: !!org.apache.commons.dbcp2.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/ds_1
    username: sa
    password:
```

##### Configuration Item Explanation

```yaml
dataSources: # Data sources configuration, multiple <data-source-name> available
  <data-source-name>: # <!!Data source pool implementation class> `!!` means class instantiation
    driverClassName: # Class name of database driver
    url: # Database URL
    username: # Database username
    password: # Database password
    # ... Other properties for data source pool
```

### Rule Configuration

Begin to configure with the rule alias to configure multiple rules.

#### Configuration Example

```yaml
rules:
-! XXX_RULE_0
  xxx
-! XXX_RULE_1
  xxx
```

#### Configuration Item Explanation

```yaml
rules:
-! XXX_RULE # Rule alias, `-` means can configure multi rules
  # ... Specific rule configurations
```

Please refer to specific rule configuration for more details.

### Properties Configuration

#### Configuration Example

```yaml
props:
  xxx: xxx
```

#### Configuration Item Explanation

```yaml
props:
  xxx: xxx # Properties key and value
```

Please refer to specific rule configuration for more details.

### YAML Syntax Explanation

`!!` means instantiation of that class

`!` means self-defined alias

`-` means one or multiple can be included

`[]` means array, can substitutable with `-` each other
