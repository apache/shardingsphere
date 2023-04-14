+++
title = "Managing Metadata in Sharded Database Environments with ShardingSphere's Built-in Metadata Function"
weight = 93
chapter = true 

+++

Apache ShardingSphere is a popular open-source data management platform that supports sharding, encryption, read/write splitting, transactions, and high availability. The metadata of ShardingSphere comprises of rules, data sources, and table structures, which are crucial for the platform's proper functioning. ShardingSphere uses the governance center's capabilities, such as ZooKeeper and etcd, for the sharing and modification of cluster configurations to achieve the horizontal expansion of computing nodes.

In this blog post, we will focus on understanding the metadata structure of Apache ShardingSphere. We will explore the three-layer metadata structure of ShardingSphere in ZooKeeper, which includes the metadata information, built-in metadata database, and simulated MySQL database.

![img](https://shardingsphere.apache.org/blog/img/2023_03_23_Managing_Metadata_in_Sharded_Database_Environments_with_ShardingSphere's_Built-in_Metadata_Function1.png)

# Metadata Structure:

To better understand the metadata structure, we can start by examining the cluster mode of ShardingSphere-Proxy. The metadata structure of ShardingSphere in ZooKeeper has a three-layer hierarchy. The first layer is the governance_ds, which contains the metadata information, built-in metadata database, and simulated MySQL database.

```Plaintext
governance_ds
--metadata (metadata information)
----sharding_db (logical database name)
------active_version (currently active version)
------versions
--------0
----------data_sources (underlying database information)
----------rules (rules of logical database, such as sharding, encryption, etc.)
------schemas (table and view information)
--------sharding_db
----------tables
------------t_order
------------t_single
----------views
----shardingsphere (built-in metadata database)
------schemas
--------shardingsphere
----------tables
------------sharding_table_statics (sharding statistics table)
------------cluster_information （version information）
----performance_schema (simulated MySQL database)
------schemas
--------performance_schema
----------tables
------------accounts
----information_schema (simulated MySQL database)
------schemas
--------information_schema
----------tables
------------tables
------------schemata
------------columns
------------engines
------------routines
------------parameters
------------views
----mysql
----sys
--sys_data (specific row information of built-in metadata database)
----shardingsphere
------schemas
--------shardingsphere
----------tables
------------sharding_table_statistics
--------------79ff60bc40ab09395bed54cfecd08f94
--------------e832393209c9a4e7e117664c5ff8fc61
------------cluster_information
--------------d387c4f7de791e34d206f7dd59e24c1c
```

The metadata directory stores the rules and data source information, including the currently active metadata version, which is under the `active_version` node. The versions under the metadata directory store the different versions of the rules and database connection information.

The `schemas` directory stores the table and view information of the logical database. ShardingSphere stores the decorated table structure information after applying the rules. For example, for sharding tables, it retrieves the structure from one of the actual tables, replaces the table name, and does not show the real encrypted column information in the table structure to enable users to operate on the logical database directly.

The built-in metadata database under the metadata directory has a similar structure to the logical database. However, it stores some built-in table structures, such as `sharding_table_statics` and `cluster_information`. These tables will be further discussed in the subsequent content.

The `performance_schema`, `information_schema`, `mysql`,` sys`, and other nodes under the metadata directory simulate the data dictionary of MySQL. They are used to support various client tools to connect to the proxy, and in the future, data collection will be increased to support queries on these data dictionaries.

The three-layer metadata structure of ShardingSphere, consisting of `governance_ds`, `metadata`, and built-in metadata database, is designed to provide compatibility with different database formats. For instance, PostgreSQL has a three-layer structure consisting of instance, database, and schema, whereas MySQL has a two-layer structure of database and table. Therefore, ShardingSphere adds an identical logical schema layer for MySQL to ensure logical uniformity.

Understanding the metadata structure of Apache ShardingSphere is crucial for developers who wish to use the platform effectively. By examining the structure of ShardingSphere metadata, we can get a better understanding of how the platform stores and manages data sources and table structures.

# ShardingSphere Built-in Metadata Database

In the previous section, we talked about the ShardingSphere built-in metadata database and its two tables: `sharding_table_statistics` (sharding information collection table) and `cluster_information` (version information table). We also discussed how the metadata database can be used to store internal collection information and user-set information (not yet implemented).

In this section, we will dive deeper into how the built-in metadata database works, including data collection and query implementation.

## Data Collection

The ShardingSphere built-in metadata database relies on data collection to gather information into memory and synchronize it with the governance center to ensure synchronization between clusters.

To illustrate how data is collected into memory, let's take the `sharding_table_statistics` table as an example. The `ShardingSphereDataCollector` interface defines a method for data collection:

```java
public interface ShardingSphereDataCollector extends TypedSPI {
    Optional<ShardingSphereTableData> collect(String databaseName, ShardingSphereTable table, Map<String, ShardingSphereDatabase> shardingSphereDatabases) throws SQLException;
}
```

This method is called by the `ShardingSphereDataCollectorRunnable` scheduled task. The current implementation starts a scheduled task on the Proxy to perform data collection and uses the built-in metadata table to differentiate data collectors for data collection. In the future, based on feedback from the community, this part may change into an e-job trigger method for collection.

The `ShardingStatisticsTableCollector` class shows the logic of collecting information. It uses the underlying data source and sharding rules to query database information and obtain statistical information.

## Query Implementation

After data collection is completed, the `ShardingSphereDataScheduleCollector` class compares the collected information with the information in memory. If it finds that they are inconsistent, it sends an event to the governance center through `EVENTBUS`. After receiving the event, the governance center updates the information of other nodes and performs memory synchronization.

The code for the listening event class is as follows:

```java
public final class ShardingSphereSchemaDataRegistrySubscriber {
    
    private final ShardingSphereDataPersistService persistService;
    
    private final GlobalLockPersistService lockPersistService;
    
    public ShardingSphereSchemaDataRegistrySubscriber(final ClusterPersistRepository repository, final GlobalLockPersistService globalLockPersistService, final EventBusContext eventBusContext) {
        persistService = new ShardingSphereDataPersistService(repository);
        lockPersistService = globalLockPersistService;
        eventBusContext.register(this);
    }
    
    @Subscribe
    public void update(final ShardingSphereSchemaDataAlteredEvent event) {
        String databaseName = event.getDatabaseName();
        String schemaName = event.getSchemaName();
        GlobalLockDefinition lockDefinition = new GlobalLockDefinition("sys_data_" + event.getDatabaseName() + event.getSchemaName() + event.getTableName());
        if (lockPersistService.tryLock(lockDefinition, 10_000)) {
            try {
                persistService.getTableRowDataPersistService().persist(databaseName, schemaName, event.getTableName(), event...
```

In this section, we've explored how the ShardingSphere built-in metadata database works, including data collection and query implementation. By storing table structures in metadata and table content in `sys_data`, we can directly query the information of the table of built-in metadata database through SQL.

In the next section, we'll discuss the benefits of using the ShardingSphere built-in metadata database and how it can improve the performance and scalability of your system.

## Support for PostgreSQL \d Query

The PostgreSQL \d command is one of the most commonly used commands in the PG client. To implement the query of \d, it is necessary to implement the corresponding SQL statements and to decorate the data in a certain way, such as replacing sharded tables with logical tables.

The actual execution statement of \d is as follows:

```mysql
SELECT n.nspname as "Schema",
  c.relname as "Name",
  CASE c.relkind 
    WHEN 'r' THEN 'table' 
    WHEN 'v' THEN 'view' 
    WHEN 'i' THEN 'index' 
    WHEN 'I' THEN 'global partition index' 
    WHEN 'S' THEN 'sequence' 
    WHEN 'L' THEN 'large sequence' 
    WHEN 'f' THEN 'foreign table' 
    WHEN 'm' THEN 'materialized view'  
    WHEN 'e' THEN 'stream' 
    WHEN 'o' THEN 'contview' 
  END as "Type",
  pg_catalog.pg_get_userbyid(c.relowner) as "Owner",
  c.reloptions as "Storage"
FROM pg_catalog.pg_class c
  LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
WHERE c.relkind IN ('r','v','m','S','L','f','e','o','')
  AND n.nspname <> 'pg_catalog' 
  AND n.nspname <> 'db4ai' 
  AND n.nspname <> 'information_schema'
  AND n.nspname !~ '^pg_toast'
  AND c.relname not like 'matviewmap\_%'
  AND c.relname not like 'mlog\_%'
  AND pg_catalog.pg_table_is_visible(c.oid)
ORDER BY 1,2;
```

To implement the query of this statement, we need to collect information from the two tables `pg_catalog.pg_class` and `pg_catalog.pg_namespace`. In addition, we also need to simulate the return results of the following two functions: `pg_catalog.pg_get_userbyid(c.relowner)` and `pg_catalog.pg_table_is_visible(c.oid)`.

The logic of the collection of tables is similar to the `sharding_table_statistics` table mentioned above, so we will not elaborate on it here. Because there is a lot of content in `pg_class`, we only collect some of the information related to \d. In addition, during the data collection stage, due to the existence of sharding rules, we need to display logical table names, so further decoration of the collected information is required, such as table name replacement.

During the query process, we only need to simulate the return results of functions. Fortunately, Calcite provides the ability to register functions. Of course, it is only a simple mock currently and can be further expanded into real data in the future.

```java
/**
    /**
 * Create catalog reader.
 *
 * @param schemaName schema name
 * @param schema schema
 * @param relDataTypeFactory rel data type factory
 * @param connectionConfig connection config
 * @return calcite catalog reader
 */
public static CalciteCatalogReader createCatalogReader(
  final String schemaName, 
  final Schema schema, 
  final RelDataTypeFactory relDataTypeFactory, 
  final CalciteConnectionConfig connectionConfig
) {
  CalciteSchema rootSchema = CalciteSchema.createRootSchema(true);
  rootSchema.add(schemaName, schema);
  registryUserDefinedFunction(schemaName, rootSchema.plus());
  return new CalciteCatalogReader(
    rootSchema, 
```

In conclusion, ShardingSphere's built-in metadata function provides a powerful tool for managing metadata in a sharded database environment. With this function, users can easily retrieve information on sharded tables and other database objects, and further extend the capabilities of their database management systems. While this feature is still in the experimental stage, it shows great potential for future development and improvement. We encourage users to explore and contribute to the ShardingSphere community, and together, we can continue to advance the capabilities of metadata management in sharded database environments.

# Relevant Links:

🔗 [MySQL Metadata Query Tasks](https://github.com/apache/shardingsphere/issues/24378)

🔗 [ShardingSphere Official Website](https://shardingsphere.apache.org/)

🔗 [ShardingSphere Official Project Repo](https://github.com/apache/shardingsphere)

🔗 [ShardingSphere Twitter](https://twitter.com/ShardingSphere)

🔗 [ShardingSphere Slack](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)