+++ 
title = "DistSQL Applications: Building a Dynamic Distributed Database"
weight = 69
chapter = true 
+++

## Background
Ever since the release of [ShardingSphere 5.0.0](https://shardingsphere.apache.org/document/5.0.0/en/overview/), [DistSQL](https://shardingsphere.apache.org/document/5.1.0/en/concepts/distsql/) has been providing strong dynamic management capabilities to the ShardingSphere ecosystem.

Thanks to DistSQL, users have been empowered to do the following:

- Create logical databases online.
- Dynamically configure rules (i.e. sharding, data encryption, read/write splitting, database discovery, shadow DB, and global rules).
- Adjust storage resources in real-time.
- Switch transaction types instantly.
- Turn SQL logs on and off at any time.
- Preview the SQL routing results.
At the same time, in the context of increasingly diversified scenarios, more and more DistSQL features are being created and a variety of valuable syntaxes have been gaining popularity among users.

## Overview
This post takes data sharding as an example to illustrate DistSQL’s application scenarios and related sharding methods.

A series of DistSQL statements are concatenated through practical cases, to give you a complete set of practical DistSQL sharding management schemes, which create and maintain distributed databases through dynamic management. The following DistSQL will be used in this example:
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/ja3n9bttj1z976itrg6o.png)
 

## Practical Case
**Required scenarios**

- Create two sharding tables: `t_order` and `t_order_item`.
- For both tables, database shards are carried out with the `user_id` field, and table shards with the `order_id` field.
- The number of shards is 2 databases * 3 tables.
As shown in the figure below:
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/m8e9u22k4ep68oabmols.png)
 

**Setting up the environment**

1.Prepare a MySQL database instance for access. Create two new databases: `demo_ds_0` and `demo_ds_1`.

> Here we take MySQL as an example, but you can also use PostgreSQL or openGauss databases.

2.Deploy [Apache ShardingSphere-Proxy 5.1.2](https://shardingsphere.apache.org/document/5.1.2/en/overview/) and [Apache ZooKeeper](https://zookeeper.apache.org/). ZooKeeper acts as a governance center and stores ShardingSphere metadata information.

3.Configure `server.yaml` in the `Proxy conf` directory as follows:

```yaml
mode:
  type: Cluster
  repository:
    type: ZooKeeper
    props:
      namespace: governance_ds
      server-lists: localhost:2181  # ZooKeeper address
      retryIntervalMilliseconds: 500
      timeToLiveSeconds: 60
      maxRetries: 3
      operationTimeoutMilliseconds: 500
  overwrite: false
rules:
  - !AUTHORITY
    users:
      - root@%:root
```
4.Start ShardingSphere-Proxy and connect it to Proxy using a client, for example:

```bash
mysql -h 127.0.0.1 -P 3307 -u root -p
```
**Creating a distributed database**

```sql
CREATE DATABASE sharding_db;
USE sharding_db;
```
**Adding storage resources**
1.Add storage resources corresponding to the prepared [MySQL](https://www.mysql.com/) database.

```
ADD RESOURCE ds_0 (
    HOST=127.0.0.1,
    PORT=3306,
    DB=demo_ds_0,
    USER=root,
    PASSWORD=123456
), ds_1(
    HOST=127.0.0.1,
    PORT=3306,
    DB=demo_ds_1,
    USER=root,
    PASSWORD=123456
);
```
2.View storage resources

```
mysql> SHOW DATABASE RESOURCES\G;
*************************** 1. row ***************************
                           name: ds_1
                           type: MySQL
                           host: 127.0.0.1
                           port: 3306
                             db: demo_ds_1
                            -- Omit partial attributes
*************************** 2. row ***************************
                           name: ds_0
                           type: MySQL
                           host: 127.0.0.1
                           port: 3306
                             db: demo_ds_0
                            -- Omit partial attributes
```

> Adding \G to the query statement aims to make the output format more readable, and it is not a must.

**Creating sharding rules**
ShardingSphere’s sharding rules support regular sharding and automatic sharding. Both sharding methods have the same effect. The difference is that the configuration of automatic sharding is more concise, while regular sharding is more flexible and independent.

> Please refer to the following links for more details on automatic sharding:

[Intro to DistSQL-An Open Source and More Powerful SQL](https://medium.com/nerd-for-tech/intro-to-distsql-an-open-source-more-powerful-sql-bada4099211?source=your_stories_page-------------------------------------)

[AutoTable: Your Butler-Like Sharding Configuration Tool](https://medium.com/geekculture/autotable-your-butler-like-sharding-configuration-tool-9a45dbb7e285)

Next, we’ll adopt regular sharding and use the `INLINE` expression algorithm to implement the sharding scenarios described in the requirements.

**Primary key generator**

The primary key generator can generate a secure and unique primary key for a data table in a distributed scenario. For details, refer to the document [Distributed Primary Key](https://shardingsphere.apache.org/document/current/en/features/sharding/concept/key-generator/).

1.Create the primary key generator.

```
CREATE SHARDING KEY GENERATOR snowflake_key_generator (
TYPE(NAME=SNOWFLAKE)
);
```
2.Query primary key generator

```
mysql> SHOW SHARDING KEY GENERATORS;
+-------------------------+-----------+-------+
| name                    | type      | props |
+-------------------------+-----------+-------+
| snowflake_key_generator | snowflake | {}    |
+-------------------------+-----------+-------+
1 row in set (0.01 sec)
```
**Sharding algorithm**

1.Create a database sharding algorithm, used by `t_order` and `t_order_item` in common.

```
-- Modulo 2 based on user_id in database sharding
CREATE SHARDING ALGORITHM database_inline (
TYPE(NAME=INLINE,PROPERTIES("algorithm-expression"="ds_${user_id % 2}"))
);
```
2.Create different table shards algorithms for `t_order` and `t_order_item`.

```
-- Modulo 3 based on order_id in table sharding
CREATE SHARDING ALGORITHM t_order_inline (
TYPE(NAME=INLINE,PROPERTIES("algorithm-expression"="t_order_${order_id % 3}"))
);
CREATE SHARDING ALGORITHM t_order_item_inline (
TYPE(NAME=INLINE,PROPERTIES("algorithm-expression"="t_order_item_${order_id % 3}"))
);
```
3.Query sharding algorithm

```
mysql> SHOW SHARDING ALGORITHMS;
+---------------------+--------+---------------------------------------------------+
| name                | type   | props                                             |
+---------------------+--------+---------------------------------------------------+
| database_inline     | inline | algorithm-expression=ds_${user_id % 2}            |
| t_order_inline      | inline | algorithm-expression=t_order_${order_id % 3}      |
| t_order_item_inline | inline | algorithm-expression=t_order_item_${order_id % 3} |
+---------------------+--------+---------------------------------------------------+
3 rows in set (0.00 sec)
```
**Default sharding strategy**

Sharding strategy consists of sharding key and sharding algorithm. Please refer to [Sharding Strategy](https://shardingsphere.apache.org/document/current/en/features/sharding/concept/sharding/) for its concept.

Sharding strategy consists of `databaseStrategy` and `tableStrategy`.

Since `t_order` and `t_order_item` have the same database sharding field and sharding algorithm, we create a default strategy that will be used by all shard tables with no sharding strategy configured:

1.Create a default database sharding strategy

```
CREATE DEFAULT SHARDING DATABASE STRATEGY (
TYPE=STANDARD,SHARDING_COLUMN=user_id,SHARDING_ALGORITHM=database_inline
);
```
2.Query default strategy

```
mysql> SHOW DEFAULT SHARDING STRATEGY\G;
*************************** 1. row ***************************
                    name: TABLE
                    type: NONE
         sharding_column:
 sharding_algorithm_name:
 sharding_algorithm_type:
sharding_algorithm_props:
*************************** 2. row ***************************
                    name: DATABASE
                    type: STANDARD
         sharding_column: user_id
 sharding_algorithm_name: database_inline
 sharding_algorithm_type: inline
sharding_algorithm_props: {algorithm-expression=ds_${user_id % 2}}
2 rows in set (0.00 sec)
```

> The default table sharding strategy is not configured, so the default strategy of `TABLE` is `NONE`.

**Sharding rules**

The primary key generator and sharding algorithm are both ready. Now create sharding rules.

1.`t_order`

```
CREATE SHARDING TABLE RULE t_order (
DATANODES("ds_${0..1}.t_order_${0..2}"),
TABLE_STRATEGY(TYPE=STANDARD,SHARDING_COLUMN=order_id,SHARDING_ALGORITHM=t_order_inline),
KEY_GENERATE_STRATEGY(COLUMN=order_id,KEY_GENERATOR=snowflake_key_generator)
);
```

> `DATANODES` specifies the data nodes of shard tables.
> `TABLE_STRATEGY` specifies the table strategy, among which `SHARDING_ALGORITHM` uses created sharding algorithm `t_order_inline`;
> `KEY_GENERATE_STRATEGY` specifies the primary key generation strategy of the table. Skip this configuration if primary key generation is not required.

2.`t_order_item`

```
CREATE SHARDING TABLE RULE t_order_item (
DATANODES("ds_${0..1}.t_order_item_${0..2}"),
TABLE_STRATEGY(TYPE=STANDARD,SHARDING_COLUMN=order_id,SHARDING_ALGORITHM=t_order_item_inline),
KEY_GENERATE_STRATEGY(COLUMN=order_item_id,KEY_GENERATOR=snowflake_key_generator)
);
```
3.Query sharding rules

```
mysql> SHOW SHARDING TABLE RULES\G;
*************************** 1. row ***************************
                            table: t_order
                actual_data_nodes: ds_${0..1}.t_order_${0..2}
              actual_data_sources:
           database_strategy_type: STANDARD
         database_sharding_column: user_id
 database_sharding_algorithm_type: inline
database_sharding_algorithm_props: algorithm-expression=ds_${user_id % 2}
              table_strategy_type: STANDARD
            table_sharding_column: order_id
    table_sharding_algorithm_type: inline
   table_sharding_algorithm_props: algorithm-expression=t_order_${order_id % 3}
              key_generate_column: order_id
               key_generator_type: snowflake
              key_generator_props:
*************************** 2. row ***************************
                            table: t_order_item
                actual_data_nodes: ds_${0..1}.t_order_item_${0..2}
              actual_data_sources:
           database_strategy_type: STANDARD
         database_sharding_column: user_id
 database_sharding_algorithm_type: inline
database_sharding_algorithm_props: algorithm-expression=ds_${user_id % 2}
              table_strategy_type: STANDARD
            table_sharding_column: order_id
    table_sharding_algorithm_type: inline
   table_sharding_algorithm_props: algorithm-expression=t_order_item_${order_id % 3}
              key_generate_column: order_item_id
               key_generator_type: snowflake
              key_generator_props:
2 rows in set (0.00 sec)
```
💡So far, the sharding rules for `t_order` and `t_order_item` have been configured.

A bit complicated? Well, you can also skip the steps of creating the primary key generator, sharding algorithm, and default strategy, and complete the sharding rules in one step. Let's see how to make it easier.

**Syntax**
Now, if we have to add a shard table `t_order_detail`, we can create sharding rules as follows:

```
CREATE SHARDING TABLE RULE t_order_detail (
DATANODES("ds_${0..1}.t_order_detail_${0..1}"),
DATABASE_STRATEGY(TYPE=STANDARD,SHARDING_COLUMN=user_id,SHARDING_ALGORITHM(TYPE(NAME=INLINE,PROPERTIES("algorithm-expression"="ds_${user_id % 2}")))),
TABLE_STRATEGY(TYPE=STANDARD,SHARDING_COLUMN=order_id,SHARDING_ALGORITHM(TYPE(NAME=INLINE,PROPERTIES("algorithm-expression"="t_order_detail_${order_id % 3}")))),
KEY_GENERATE_STRATEGY(COLUMN=detail_id,TYPE(NAME=snowflake))
);
```
**Note: **The above statement specified database sharding strategy, table strategy, and primary key generation strategy, but it didn’t use existing algorithms.

Therefore, the DistSQL engine automatically uses the input expression to create an algorithm for the sharding rules of `t_order_detail`. Now the primary key generator, sharding algorithm, and sharding rules are as follows:

1.Primary key generator

```
mysql> SHOW SHARDING KEY GENERATORS;
+--------------------------+-----------+-------+
| name                     | type      | props |
+--------------------------+-----------+-------+
| snowflake_key_generator  | snowflake | {}    |
| t_order_detail_snowflake | snowflake | {}    |
+--------------------------+-----------+-------+
2 rows in set (0.00 sec)
```
2.Sharding algorithm

```
mysql> SHOW SHARDING ALGORITHMS;
+--------------------------------+--------+-----------------------------------------------------+
| name                           | type   | props                                               |
+--------------------------------+--------+-----------------------------------------------------+
| database_inline                | inline | algorithm-expression=ds_${user_id % 2}              |
| t_order_inline                 | inline | algorithm-expression=t_order_${order_id % 3}        |
| t_order_item_inline            | inline | algorithm-expression=t_order_item_${order_id % 3}   |
| t_order_detail_database_inline | inline | algorithm-expression=ds_${user_id % 2}              |
| t_order_detail_table_inline    | inline | algorithm-expression=t_order_detail_${order_id % 3} |
+--------------------------------+--------+-----------------------------------------------------+
5 rows in set (0.00 sec)
```
3.Sharding rules

```
mysql> SHOW SHARDING TABLE RULES\G;
*************************** 1. row ***************************
                            table: t_order
                actual_data_nodes: ds_${0..1}.t_order_${0..2}
              actual_data_sources:
           database_strategy_type: STANDARD
         database_sharding_column: user_id
 database_sharding_algorithm_type: inline
database_sharding_algorithm_props: algorithm-expression=ds_${user_id % 2}
              table_strategy_type: STANDARD
            table_sharding_column: order_id
    table_sharding_algorithm_type: inline
   table_sharding_algorithm_props: algorithm-expression=t_order_${order_id % 3}
              key_generate_column: order_id
               key_generator_type: snowflake
              key_generator_props:
*************************** 2. row ***************************
                            table: t_order_item
                actual_data_nodes: ds_${0..1}.t_order_item_${0..2}
              actual_data_sources:
           database_strategy_type: STANDARD
         database_sharding_column: user_id
 database_sharding_algorithm_type: inline
database_sharding_algorithm_props: algorithm-expression=ds_${user_id % 2}
              table_strategy_type: STANDARD
            table_sharding_column: order_id
    table_sharding_algorithm_type: inline
   table_sharding_algorithm_props: algorithm-expression=t_order_item_${order_id % 3}
              key_generate_column: order_item_id
               key_generator_type: snowflake
              key_generator_props:
*************************** 3. row ***************************
                            table: t_order_detail
                actual_data_nodes: ds_${0..1}.t_order_detail_${0..1}
              actual_data_sources:
           database_strategy_type: STANDARD
         database_sharding_column: user_id
 database_sharding_algorithm_type: inline
database_sharding_algorithm_props: algorithm-expression=ds_${user_id % 2}
              table_strategy_type: STANDARD
            table_sharding_column: order_id
    table_sharding_algorithm_type: inline
   table_sharding_algorithm_props: algorithm-expression=t_order_detail_${order_id % 3}
              key_generate_column: detail_id
               key_generator_type: snowflake
              key_generator_props:
3 rows in set (0.01 sec)
```
**Note:** In the `CREATE SHARDING TABLE RULE` statement, `DATABASE_STRATEGY`, `TABLE_STRATEGY`, and `KEY_GENERATE_STRATEGY` can all reuse existing algorithms.

Alternatively, they can be defined quickly through syntax. The difference is that additional algorithm objects are created. Users can use it flexibly based on scenarios.

After the configuration verification rules are created, you can verify them in the following ways:

**Checking node distribution**

DistSQL provides `SHOW SHARDING TABLE NODES` for checking node distribution and users can quickly learn the distribution of shard tables.
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/psvtq8k8jo4m0jnadhzw.png)
 

We can see the node distribution of the shard table is consistent with what is described in the requirement.

**SQL Preview**

Previewing SQL is also an easy way to verify configurations. Its syntax is `PREVIEW SQL`:

1.Query with no shard key with all routes
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/n4m1glocjtigxdcx5yg3.png)
 

2.Specify `user_id` to query with a single database route
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/adgjmx6ffhq6beag8nnw.png)
 

3.Specify `user_id` and `order_id` with a single table route
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/vryt0m6wfdp8rn4vkihp.png)
 

> Single-table routes scan the least shard tables and offer the highest efficiency.

### DistSQL auxiliary query
During the system maintenance, algorithms or storage resources that are no longer in use may need to be released, or resources that need to be released may have been referenced and cannot be deleted. The following DistSQL can solve these problems.

**Query unused resources**

1.Syntax: `SHOW UNUSED RESOURCES`

2.Sample:
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/jsise5qea0j690twk7e9.png)
 

**Query unused primary key generator**

1.Syntax: `SHOW UNUSED SHARDING KEY GENERATORS`

2.Sample:
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/ufoqmha63wnaadedj9vv.png)
 

**Query unused sharding algorithm**

1.Syntax: `SHOW UNUSED SHARDING ALGORITHMS`

2.Sample:
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/17qyt8evxl7l3pqd9hc8.png)
 

**Query rules that use the target storage resources**

1.Syntax: `SHOW RULES USED RESOURCE`

2.Sample:
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/d4pzeukg6umnbk52uw9r.png)
 

> All rules that use the resource can be queried, not limited to the `Sharding Rule`.

**Query sharding rules that use the target primary key generator**

1.Syntax: `SHOW SHARDING TABLE RULES USED KEY GENERATOR`

2.Sample:
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/ugwcfdmf3sdcb4xzpivh.png)
 

**Query sharding rules that use the target algorithm**

1.Syntax: `SHOW SHARDING TABLE RULES USED ALGORITHM`

2.Sample:
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/xnq0u0491e7vwavkithv.png)
 

## Conclusion
This post takes the data sharding scenario as an example to introduce [DistSQL](https://shardingsphere.apache.org/document/5.1.0/en/concepts/distsql/)’s applications and methods.

DistSQL provides flexible syntax to help simplify operations. In addition to the `INLINE` algorithm, DistSQL supports standard sharding, compound sharding, Hint sharding, and custom sharding algorithms. More examples will be covered in the coming future.

If you have any questions or suggestions about [Apache ShardingSphere](https://shardingsphere.apache.org/), please feel free to post them on the GitHub Issue list.

## Project Links:

[ShardingSphere Github](https://github.com/apache/shardingsphere/issues?page=1&q=is%3Aopen+is%3Aissue+label%3A%22project%3A+OpenForce+2022%22)

[ShardingSphere Twitter](https://twitter.com/ShardingSphere)

[ShardingSphere Slack
](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)
[Contributor Guide](https://shardingsphere.apache.org/community/cn/involved/)

[GitHub Issues](https://github.com/apache/shardingsphere/issues)

[Contributor Guide](https://shardingsphere.apache.org/community/en/involved/)

## References

1. [Concept-DistSQL](https://shardingsphere.apache.org/document/current/en/concepts/distsql/)

2. [Concept-Distributed Primary Key](https://shardingsphere.apache.org/document/current/en/features/sharding/concept/key-generator/)

3. [Concept-Sharding Strategy](https://shardingsphere.apache.org/document/current/en/features/sharding/concept/sharding/)

4. [Concept INLINE Expression
](https://shardingsphere.apache.org/document/current/en/features/sharding/concept/inline-expression/)
5. [Built-in Sharding Algorithm
](https://shardingsphere.apache.org/document/current/en/user-manual/common-config/builtin-algorithm/sharding/)
6. [User Manual: DistSQL
](https://shardingsphere.apache.org/document/current/en/user-manual/shardingsphere-proxy/distsql/syntax/)

## Author
**Jiang Longtao**
[SphereEx](https://www.sphere-ex.com/en/) Middleware R&D Engineer & Apache ShardingSphere Committer.

Longtao focuses on the R&D of DistSQL and related features.
