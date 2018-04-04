+++
toc = true
title = "YAML configuration"
weight = 3
+++

## YAML configuration

### Import the dependency of maven

```xml
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### Configuration Example

#### Sharding Configuration
```yaml
dataSources:
  db0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: org.h2.Driver
    url: jdbc:h2:mem:db0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
    username: sa
    password: 
    maxActive: 100
  db1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: org.h2.Driver
    url: jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
    username: sa
    password: 
    maxActive: 100

shardingRule:
  tables:
    config:
      actualDataNodes: db${0..1}.t_config
    t_order: 
      actualDataNodes: db${0..1}.t_order_${0..1}
      databaseStrategy: 
        standard:
          shardingColumn: user_id
          preciseAlgorithmClassName: io.shardingjdbc.core.yaml.fixture.SingleAlgorithm
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmInlineExpression: t_order_${order_id % 2}
      keyGeneratorColumnName: order_id
      keyGeneratorClass: io.shardingjdbc.core.yaml.fixture.IncrementKeyGenerator
    t_order_item:
      actualDataNodes: db${0..1}.t_order_item_${0..1}
      databaseStrategy: 
        standard:
          shardingColumn: user_id
          preciseAlgorithmClassName: io.shardingjdbc.core.yaml.fixture.SingleAlgorithm
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmInlineExpression: t_order_item_${order_id % 2}
  # t_order and t_order are all bindingTables of each other because of their same sharding strategies.
  bindingTables:
    - t_order,t_order
  # The default sharding strategy
  defaultDatabaseStrategy:
    none:
  defaultTableStrategy:
    complex:
      shardingColumns: id, order_id
      algorithmClassName: io.shardingjdbc.core.yaml.fixture.MultiAlgorithm
  props:
    sql.show: true
```

#### The config items for Sharding

```yaml
dataSources: # Config for data source
  <data_source_name> # Config for DB connection pool class. One or many configs are ok.
    driverClassName: # Class name for database driver.
    url: # The url for database connection.
    username: # Username used to access DB.
    password: # Password used to access DB.
    ... # Other configs for connection pool.

defaultDataSourceName: # Default datasource. Notice: Tables without sharding rules are accessed by using the default data source.

tables: # The config for sharding, One or many configs for logic_table_name are ok.
    <logic_table_name>: # Table name for LogicTables
        actualDataNodes: # Actual data nodes configured in the format of *datasource_name.table_name*, multiple configs spliced with commas, supporting the inline expression. The default value is composed of configured datasources and logic table. This default config is to generate broadcast table (*The same table existed in every DB for cascade query*) or to split databases without spliting tables.
        databaseStrategy: # Strategy for sharding databases, only one strategy can be chosen from following strategies:
            standard: # Standard sharding strategy for single sharding column.
                shardingColumn: # Sharding Column
                preciseAlgorithmClassName: # The class name for precise-sharding-algorithm used for = and IN. The default constructor or on-parametric constructor is needed.
                rangeAlgorithmClassName: # (Optional) The class name for range-sharding-algorithm used for BETWEEN. The default constructor or on-parametric constructor is needed.
            complex: # Complex sharding strategy for multiple sharding columns.
                shardingColumns : # Sharding Column, multiple sharding columns spliced with commas. 
                algorithmClassName: # The class name for sharding-algorithm. The default constructor or on-parametric constructor is needed.
            inline: inline # Inline sharding strategy.
                shardingColumn : # Sharding Column
                algorithmInlineExpression: #  The inline expression conformed to groovy dynamic syntax for sharding. 
            hint: # Hint sharding strategy
                algorithmClassName: # The class name for sharding-algorithm. The default constructor or on-parametric constructor is needed.
            none: # No sharding
        tableStrategy: # Strategy for sharding tables. The details is same as Strategy for sharding databases.
  bindingTables: # Config for Blinding tables
  - A list of logic_table_name, multiple logic_table_names spliced with commas.
  
defaultDatabaseStrategy: # Default strategy for sharding databases. The details is same as databaseStrategy.
 
defaultTableStrategy: # Default strategy for sharding databases. The details is same as tableStrategy.

props: Property Configuration (Optional)
    sql.show: # To show SQL or not. Default: false
    executor.size: # The number of running thread. Default: The number of CPU cores.
```

#### The construction method for data source of Sharding

```java
    DataSource dataSource = ShardingDataSourceFactory.createDataSource(yamlFile);
```

#### Read-write splitting Configuration
```yaml
dataSources:
  db_master: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: org.h2.Driver
    url: jdbc:h2:mem:db_master;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
    username: sa
    password: 
    maxActive: 100
  db_slave_0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: org.h2.Driver
    url: jdbc:h2:mem:db_slave_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
    username: sa
    password: 
    maxActive: 100
  db_slave_1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: org.h2.Driver
    url: jdbc:h2:mem:db_slave_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
    username: sa
    password: 
    maxActive: 100

masterSlaveRule:
  name: db_ms

  masterDataSourceName: db_master

  slaveDataSourceNames: [db_slave_0, db_slave_1]
```

#### The config items for Read-write splitting

```yaml
dataSource: # Config for data sourc same as previous dataSource.

name: # Data source name for sharding.

masterDataSourceName: Datasource name for Master datasource

slaveDataSourceNames：Datasource name for Slave datasource, multiple datasource put in an Array.
```

#### The construction method for data source of Read-write splitting

```java
    DataSource dataSource = MasterSlaveDataSourceFactory.createDataSource(yamlFile);
```

### More detail on YAML Configuration
!! :implementation class.

[] :multiple items.
