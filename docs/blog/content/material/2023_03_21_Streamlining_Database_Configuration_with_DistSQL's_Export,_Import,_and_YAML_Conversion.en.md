+++
title = "Streamlining Database Configuration with DistSQL's Export, Import, and YAML Conversion"
weight = 92
chapter = true 
+++

DistSQL, which stands for Distributed SQL, is a specialized operating language exclusive to Apache ShardingSphere. This language provides users with a simplified and powerful dynamic management system that allows them to operate ShardingSphere like a traditional database.

One of the key benefits of using DistSQL is the ability to define resources and rules online without the need to modify YAML files and restart the system. This streamlines the management process and reduces the potential for errors.

![img](https://shardingsphere.apache.org/blog/img/2023_03_21_Streamlining_Database_Configuration_with_DistSQL's_Export,_Import,_and_YAML_Conversion1.png)

As the capabilities of DistSQL have grown and users have become more familiar with it, a number of questions have been raised about its use. In this post, we'll explore some of these questions and discuss why they're relevant to today's topic.

Some of the most common questions about DistSQL include:

- Where are the rules defined by DistSQL stored?
- How to transfer a DistSQL defined configuration to a new environment when there are multiple test environments?
- How to write DistSQL syntax?

## Where are the rules defined by DistSQL stored?

When using DistSQL, a common question that arises is where the rules defined by the language are stored. The answer is that these rules are stored in the Governance Centre, which acts as a centralized repository for configuration data.

In cluster mode, users can choose to use either ZooKeeper or etcd as the Governance Centre, while standalone mode allows users to specify their own preferred persistence method. By default, DistSQL uses H2 non-persistent mode. However, it's recommended that users utilize cluster mode to take full advantage of the capabilities of DistSQL.

By storing rules in a centralized location, DistSQL makes it easier to manage and modify configurations. This enhances the overall user experience and streamlines the management process, allowing users to focus on more important tasks.

## How to transfer a DistSQL defined configuration to a new environment when there are multiple test environments?

When using DistSQL, one potential challenge that users may face is transferring data backups from the Governance Centre to a new environment. This process can be complex and time-consuming, especially if the data is stored across multiple nodes.

To address this issue, DistSQL provides an `EXPORT` syntax that allows users to export logical database configurations to YAML format. This makes it easier to migrate data to a new environment and streamlines the backup process.

Whether you're using standalone or clustered mode, the `EXPORT` syntax provides a powerful tool for managing and migrating data. By simplifying the backup process, DistSQL enhances the user experience and improves overall efficiency.

## We have a requirement to back up data on a regular basis. How to back up the configuration of the logical database? Using DistSQL definitions.

As we discussed in the previous question, using the `EXPORT` syntax in DistSQL allows users to export logical database configurations to YAML format. This functionality serves as a useful backup tool, as it enables users to easily store and migrate data to a new environment.

By exporting data as a YAML file, users can ensure that their data is safely backed up and readily available in case of any unexpected issues or system failures. This provides peace of mind and helps to ensure that important data is never lost.

Overall, the `EXPORT` syntax in DistSQL is a valuable tool that enhances the management capabilities of the language. Whether you're using it to migrate data to a new environment or simply to backup your data, the `EXPORT` syntax streamlines the process and makes it easier to manage your data with confidence.

## Does restarting the YAML format takes effect when putting it under a new ShardingSphere instance?

After exporting a YAML file using the `EXPORT` syntax in DistSQL, users have several options for loading the data back into the system. One common approach is to place the YAML file directly in the conf directory of the Proxy. However, if the Proxy is already running, a restart may be required for the changes to take effect.

A more convenient alternative is to use the `IMPORT` statement to dynamically load the data into the system. This approach allows the Proxy to write the data to the Governance Center without requiring a restart, which can save time and reduce disruptions to the system.

By leveraging the `IMPORT` statement, users can take full advantage of the dynamic management capabilities of DistSQL. This enables them to make changes to the system on-the-fly, without the need for manual intervention or system restarts. Overall, the ability to load data dynamically enhances the user experience and makes it easier to manage and maintain a stable and reliable system.

## I have configured sharding and read/write separation rules. Can I only show [2][3] separately when I query? Is there a way to show them together?

In addition to serving as a backup tool, the `EXPORT` statement in DistSQL can also be used to view logical database configurations quickly. By default, when the TO FILE parameter is not specified, the results of `EXPORT` are output in the query result set.

This provides a convenient way for users to quickly access and view their database configurations without needing to navigate through various files or directories. By leveraging the `EXPORT` statement, users can gain a better understanding of the overall state of their system and make more informed decisions about how to manage and maintain it.

## DistSQL looks good, but I used to configure it in YAML and don't know how to write DistSQL syntax. Can anyone teach me?

In addition to the `EXPORT` and `IMPORT` syntaxes we discussed earlier, the community has also developed a `CONVERT` syntax that can be used to convert YAML to DistSQL statements.

The `CONVERT` syntax provides a convenient way for users to transform their YAML configurations into DistSQL statements that can be learned against or copied directly. This makes it easier for users to manage and maintain their database configurations and ensures that they can quickly and easily make changes to their system as needed.

Overall, the combination of `EXPORT`, `IMPORT`, and `CONVERT` syntaxes in DistSQL provides users with a robust set of tools for managing and maintaining logical database configurations. Whether you're backing up your data, loading it dynamically, or converting it to DistSQL statements, these syntaxes can help you streamline your workflow and make it easier to manage your system with confidence.

# Grammatical Exclusion

## EXPORT DATABASE CONFIGURATION

One of the key syntaxes in DistSQL is `EXPORT DATABASE CONFIGURATION`, which is used to export the configuration of a logical database, including data sources and rules, into YAML format. By leveraging this syntax, users can quickly and easily create backups of their system configurations and ensure that they have a reliable and up-to-date record of their system settings.

When executing `EXPORT DATABASE CONFIGURATION`, users can specify the location and format of the exported file using the `TO FILE` parameter. If this parameter is not specified, the results will be output in the query result set.

```mysql
EXPORT DATABASE CONFIGURATION (FROM databaseName)? (TO FILE filePath)?
```

Descriptions are as follows:

```plain text
FROM databaseName is used to specify the logical database to be exported.
When FROM databaseName is not specified, the logical database currently in use is exported.
When the TO FILE filePath parameter is specified, the export information will be exported to the target file.
If the file does not exist, it will be created automatically; if it already exists, it will be overwritten.
filePath is of type STRING.
```

## IMPORT DATABASE CONFIGURATION

Another important syntax in DistSQL is `IMPORT DATABASE CONFIGURATION`, which is used to import configurations, including data sources and rules, from a YAML file. This syntax provides users with a convenient and efficient way to load their configurations into DistSQL without needing to manually define each component.

When executing `IMPORT DATABASE CONFIGURATION`, users can specify the location of the YAML file using the `FROM FILE` parameter. By leveraging this syntax, users can quickly and easily load their configurations into DistSQL and ensure that their system is up-to-date and accurately reflects their settings.

One of the key advantages of the `IMPORT DATABASE CONFIGURATION` syntax is that it can be executed dynamically, without needing to restart the system. This makes it easier to manage and maintain a system while it is running, without needing to interrupt or disrupt normal operations.

Import into the current logic database:

```mysql
IMPORT DATABASE CONFIGURATION FROM FILE filePath
```

Descriptions are as follows:

```plain text
The file to be imported must conform to the Proxy's YAML configuration format. 
The import target must be an empty logical database, i.e. the logical database currently in use has no storage nodes and no rule configuration. 
The file must contain databaseName and be consistent with the name of the logical repository being operated on.
The file must contain the dataSources storage resource configuration.
filePath is of type STRING.
```

## CONVERT YAML CONFIGURATION

Used to convert YAML configurations to their corresponding DistSQL statements.

```mysql
CONVERT YAML CONFIGURATION FROM FILE filePath
```

Descriptions are as follows:

- The file to be converted must conform to Proxy's YAML configuration format.
- The **filePath** is type STRING.

# Practical Demonstration

To demonstrate the usage of the `IMPORT` and `EXPORT DATABASE CONFIGURATION `syntaxes in DistSQL, we'll use a MySQL scenario with the ShardingSphere-Proxy deployed in cluster mode. Before we get started, there are a few key steps we need to take to prepare our environment:

1. Create two databases, demo_ds_0 and demo_ds_1, in MySQL. These databases will be used to store our data and test our configurations.
2. Start the ZooKeeper service. This will provide us with the necessary infrastructure to manage our system configurations.
3. Configure the mode information in the server.yaml file of the ShardingSphere-Proxy and start the proxy service. This will enable us to connect to our MySQL databases and manage our configurations using DistSQL [4].

With these steps completed, we're now ready to start working with the `IMPORT` and `EXPORT DATABASE CONFIGURATION` syntaxes.

## Configuration Logic Database

1. Create logic database

```mysql
CREATE DATABASE sharding_db;
USE sharding_db;
```

2. Register storage nodes

```mysql
REGISTER STORAGE UNIT ds_0 (
    URL="jdbc:mysql://127.0.0.1:3306/demo_ds_0?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="123456",
    PROPERTIES("maximumPoolSize"=10)
),ds_1 (
    URL="jdbc:mysql://127.0.0.1:3306/demo_ds_1?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="123456",
    PROPERTIES("maximumPoolSize"=10)
);
```

3. Creating sharding rules

```mysql
CREATE SHARDING TABLE RULE t_order (
STORAGE_UNITS(ds_0,ds_1),
SHARDING_COLUMN=order_id,TYPE(NAME=MOD,PROPERTIES("sharding-count"=4)),
KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME="snowflake"))
);
```

The operation process is as follows:

![img](https://shardingsphere.apache.org/blog/img/2023_03_21_Streamlining_Database_Configuration_with_DistSQL's_Export,_Import,_and_YAML_Conversion2.png)

By doing this, we dynamically create a logical database `sharding_db` that stores node information and sharding rules in the Governance Center ZooKeeper.

## EXPORT DATABASE CONFIGURATION

View configurations only

```mysql
EXPORT DATABASE CONFIGURATION;
# or
EXPORT DATABASE CONFIGURATION FROM sharding_db;
```

Execution example

```yaml
mysql> EXPORT DATABASE CONFIGURATION;
+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| result                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| databaseName: sharding_db
dataSources:
  ds_1:
    password: 123456
    url: jdbc:mysql://127.0.0.1:3306/demo_ds_1?serverTimezone=UTC&useSSL=false
    username: root
    minPoolSize: 1
    connectionTimeoutMilliseconds: 30000
    maxLifetimeMilliseconds: 2100000
    readOnly: false
    idleTimeoutMilliseconds: 60000
    maxPoolSize: 10
  ds_0:
    password: 123456
    url: jdbc:mysql://127.0.0.1:3306/demo_ds_0?serverTimezone=UTC&useSSL=false
    username: root
    minPoolSize: 1
    connectionTimeoutMilliseconds: 30000
    maxLifetimeMilliseconds: 2100000
    readOnly: false
    idleTimeoutMilliseconds: 60000
    maxPoolSize: 10
rules:
- !SHARDING
  autoTables:
    t_order:
      actualDataSources: ds_0,ds_1
      keyGenerateStrategy:
        column: order_id
        keyGeneratorName: t_order_snowflake
      logicTable: t_order
      shardingStrategy:
        standard:
          shardingAlgorithmName: t_order_mod
          shardingColumn: order_id
  keyGenerators:
    t_order_snowflake:
      type: snowflake
  shardingAlgorithms:
    t_order_mod:
      props:
        sharding-count: '4'
      type: mod
 |
+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
1 row in set (0.03 sec)
```

After executing `EXPORT DATABASE CONFIGURATION`, the logical database configuration is presented in the classic YAML format.

## Export to YAML File

```mysql
EXPORT DATABASE CONFIGURATION TO FILE '/Users/xx/sharding_db.yaml';
# or
EXPORT DATABASE CONFIGURATION FROM sharding_db TO FILE '/Users/xx/sharding_db.yaml';
```

Execution example

```mysql
mysql> EXPORT DATABASE CONFIGURATION TO FILE '/Users/xxx/sharding_db.yaml';
+------------------------------------------------------------------+
| result                                                           |
+------------------------------------------------------------------+
| Successfully exported to：'/Users/xxx/sharding_db.yaml'  |
+------------------------------------------------------------------+
1 row in set (0.01 sec)
```

After adding the `TO FILE` parameter, the results of `EXPORT DATABASE CONFIGURATION` are output to the specified file. The following diagram shows the contents of `sharding_db.yaml`.

![img](https://shardingsphere.apache.org/blog/img/2023_03_21_Streamlining_Database_Configuration_with_DistSQL's_Export,_Import,_and_YAML_Conversion3.png)

## IMPORT DATABASE CONFIGURATION

Once we've successfully exported our database configurations using the `EXPORT DATABASE CONFIGURATION` syntax, we can use the resulting YAML file (in this case, `sharding_db.yaml`) to perform the import operation in any ShardingSphere-Proxy.

To demonstrate how this works, let's first remove the sharding_db metadata from our current configuration. We can then use the `IMPORT` statement to import the configuration from our YAML file and update our metadata with the new information.

By using the `IMPORT` statement in this way, we can easily transfer configurations between different instances of ShardingSphere-Proxy, making it simple to maintain consistency across our system and ensuring that our configurations are always up-to-date and accurate.

**Prepare the new database**

```mysql
USE shardingsphere;
DROP DATABASE sharding_db;
CREATE DATABASE sharding_db;
EXPORT DATABASE CONFIGURATION FROM sharding_db;
```

Execution example

```mysql
mysql> USE shardingsphere;
Database changed
mysql> DROP DATABASE sharding_db;
Query OK, 0 rows affected (0.02 sec)mysql> CREATE DATABASE sharding_db;
Query OK, 0 rows affected (0.02 sec)mysql> EXPORT DATABASE CONFIGURATION FROM sharding_db;
+----------------------------+
| result                     |
+----------------------------+
| databaseName: sharding_db
 |
+----------------------------+
1 row in set (0.01 sec)
```

By doing this, we have created a new logical database, `sharding_db`, which does not contain any configuration information.

## Import Logic Database

```mysql
USE sharding_db;
IMPORT DATABASE CONFIGURATION FROM FILE '/Users/xx/sharding_db.yaml';
```

**Execution example**

```mysql
mysql> USE sharding_db;
Database changed
mysql> IMPORT DATABASE CONFIGURATION FROM FILE '/Users/xx/sharding_db.yaml';
Query OK, 0 rows affected (1.06 sec)
```

Using the `IMPORT DATABASE CONFIGURATION` syntax, we can easily restore an exported logical database configuration to an online instance of ShardingSphere-Proxy. This process is quick and straightforward, allowing us to keep our configurations up-to-date and ensuring consistency across our system.

To confirm that the import has been successful, we can use the `SHOW DATABASES` and `EXPORT DATABASE CONFIGURATION` statements. These statements allow us to check the status of our configurations and ensure that everything has been imported correctly.

Overall, the combination of `EXPORT`, `IMPORT,` and `SHOW` statements provides a powerful set of tools for managing and maintaining database configurations with ShardingSphere-Proxy. By using these tools effectively, we can ensure that our configurations are always accurate and up-to-date, enabling us to build more robust and reliable systems.

## CONVERT YAML CONFIGURATION

The `CONVERT YAML CONFIGURATION` syntax is a powerful tool that enables us to convert exported YAML configurations to DistSQL, making it easier for users to manage and maintain their databases. This feature is particularly useful when we need to migrate configurations between different environments.

To demonstrate this functionality, let's convert the `sharding_db.yaml` file that we exported earlier using the `EXPORT` statement. By converting the YAML to DistSQL, we can migrate the configuration using a SQL client, which is more convenient than copying files for import.

The `CONVERT YAML CONFIGURATION` syntax allows us to easily convert the YAML file to DistSQL. We can then use the resulting statements to migrate the configuration to a new environment. This process is quick and straightforward, allowing us to manage our configurations more efficiently.

Overall, the `CONVERT YAML CONFIGURATION` syntax is a valuable tool for managing and maintaining database configurations with ShardingSphere-Proxy. By using this syntax effectively, we can simplify the process of migrating configurations and ensure that our databases are always up-to-date and accurate.

```mysql
CONVERT YAML CONFIGURATION FROM FILE '/Users/xx/sharding_db.yaml';
```

**Execution example**

```mysql
mysql> CONVERT YAML CONFIGURATION FROM FILE '/Users/xx/sharding_db.yaml';
+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| dist_sql                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| CREATE DATABASE sharding_db;
USE sharding_db;
REGISTER STORAGE UNIT ds_1 (
URL='jdbc:mysql://127.0.0.1:3306/demo_ds_1?serverTimezone=UTC&useSSL=false',
USER='root',
PASSWORD='123456',
PROPERTIES('minPoolSize'='1', 'connectionTimeoutMilliseconds'='30000', 'maxLifetimeMilliseconds'='2100000', 'readOnly'='false', 'idleTimeoutMilliseconds'='60000', 'maxPoolSize'='10')
), ds_0 (
URL='jdbc:mysql://127.0.0.1:3306/demo_ds_0?serverTimezone=UTC&useSSL=false',
USER='root',
PASSWORD='123456',
PROPERTIES('minPoolSize'='1', 'connectionTimeoutMilliseconds'='30000', 'maxLifetimeMilliseconds'='2100000', 'readOnly'='false', 'idleTimeoutMilliseconds'='60000', 'maxPoolSize'='10')
);CREATE SHARDING TABLE RULE t_order (
STORAGE_UNITS(ds_0,ds_1),
SHARDING_COLUMN=order_id,
TYPE(NAME='mod', PROPERTIES('sharding-count'='4')),
KEY_GENERATE_STRATEGY(COLUMN=order_id, TYPE(NAME='snowflake'))
); |
+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
1 row in set (0.01 sec)
```

# Conclusion

In summary, we have explored the features and benefits of DistSQL in Apache ShardingSphere, including the ability to dynamically manage resources and rules through `Export`, `Import`, and `Convert` YAML Configuration syntax.

By using these functions, users can easily back up, migrate, and manage configurations in their distributed SQL environments.

For more information on DistSQL, please refer to the official documentation [1] and our GitHub repo [5].

If you have any questions or feedback regarding Apache ShardingSphere, please don't hesitate to ask on the GitHub issue list, or join our [Slack channel](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-1qeqeecua-Tz7Um62TRPmxNY80Dq38kQ) to discuss. We hope this article has been helpful and informative to you.

# References

🔗 [DistSQL Documentation](https://shardingsphere.apache.org/document/5.3.1/cn/user-manual/shardingsphere-proxy/distsql/)

🔗 [SHOW SHARDING TABLE RULE: documentation](https://shardingsphere.apache.org/document/5.3.1/cn/user-manual/shardingsphere-proxy/distsql/syntax/rql/rule-query/sharding/show-sharding-table-rule/)

🔗 [SHOW READWRITE_SPLITTING RULE](https://shardingsphere.apache.org/document/5.3.1/cn/user-manual/shardingsphere-proxy/distsql/syntax/rql/rule-query/readwrite-splitting/show-readwrite-splitting-rule/)

🔗 [Using the ShardingSphere-Proxy binary release package](https://shardingsphere.apache.org/document/5.3.1/cn/user-manual/shardingsphere-proxy/startup/bin/)

🔗 [GitHub issue list](https://github.com/apache/shardingsphere/issues)