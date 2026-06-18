+++
title = "Alibaba Cloud's OpenSergo & ShardingSphere release database governance standard for microservices - combining Database Plus and Database Mesh concepts"
weight = 79
chapter = true 

+++

# Background

Recently, [Alibaba Cloud](https://us.alibabacloud.com/?utm_key=se_1007722888&utm_content=se_1007722888&gclid=Cj0KCQiAyMKbBhD1ARIsANs7rEE71bHtu1aPbMS_E5-awHyWwTtyRn8CfmMU0qD1eH2hKSVEIDxcxaIaAuAVEALw_wcB)'s [OpenSergo](https://opensergo.io/) and [ShardingSphere](https://shardingsphere.apache.org/) jointly released the database governance standard for microservices. By combining the [Database Plus](https://medium.com/faun/whats-the-database-plus-concepand-what-challenges-can-it-solve-715920ba65aa?source=your_stories_page-------------------------------------) and [Database Mesh](https://medium.com/faun/shardingsphere-database-mesh-4ad75bf4bac8?source=your_stories_page-------------------------------------) concepts, the two communities have standardized the existing database governance concepts, patterns, and paths, further completing the database governance ecosystem under the cloud-native environment.

![img](https://shardingsphere.apache.org/blog/img/2022_11_15_Alibaba_Cloud's_OpenSergo_&_ShardingSphere_release_database_governance_standard_for_microservices_—_combining_Database_Plus_and_Database_Mesh_concepts1.png)

***The founders of both communities expressed their opinions concerning the collaboration between the ShardingSphere community and the OpenSergo community:***

**Zhang Liang, the PMC Chair of the Apache ShardingSphere community:**

In the microservices field, the interaction and collaboration between services have been gradually perfected, but there is still no effective standard for services to access the database. Being, ShardingSphere has been continuously following the **"connect, enhance, and pluggable"** design philosophy. "Connect" refers to providing standardized protocols and interfaces, breaking the barriers for development languages to access heterogeneous databases. It's forward-looking for OpenSergo to propose the microservice-oriented governance standard and take the initiative to include database access in the standard. I'm pleased to see ShardingSphere and OpenSergo working together to build the standard for microservices which is a pivotal entrance to access databases.

**Zhao Yihao, the founder of the OpenSergo community:**

In microservice governance, in addition to the governance of the microservices itself, it is also a critical step to ensure business reliability and continuity to deal with microservices' access to databases. As a Top-Level project in the database governance field, ShardingSphere has integrated a wealth of best practices and technical experience, which complements OpenSergo. In this context, we work with the ShardingSphere community to jointly build a database governance standard from the perspective of microservices, so that enterprises can carry out unified and standardized governance and management on different data layer frameworks and traffic.

**Note:** database governance in this article includes all aspects of database governance in microservice systems. All business information and key data need a robust and stable database system as it is the most important state terminal.

# The database is increasingly vital in the microservice system

To meet flexible business requirements, the application architecture can be transformed from monolithic to service-oriented and then to microservice-oriented. In this case, the database used to store the core data becomes the focus of the distributed system.

Enterprises take advantage of microservices to separate services and adopt a distributed architecture to achieve loose coupling among multiple services, flexible adjustment and combination of services, and high availability of systems. In particular, microservices indeed have delivered enormous benefits in the face of rapidly changing businesses.

However, after services are separated, their corresponding underlying databases should also be separated to ensure the independent deployment of each service. In this way, each service can be an independent unit, finally achieving the microservices. In this context, the database is becoming increasingly complicated:

- The transformation from monolithic to microservice-oriented architecture models leads to increasingly complicated services, diversified requirements, larger scale infrastructure, complex call relations between services, and higher requirements on the underlying database performance.
- Different transactions usually involve multiple services - but it is a challenge to ensure data consistency between services.
- It is also challenging to query data across multiple services.

With most backend applications, their system performance improvement is mainly limited to databases. Particularly in a microservice environment, it is a team's top priority to deal with database performance governance. Database governance naturally becomes an indispensable part of microservice governance.

In database governance, we now mainly focus on **read/write splitting, sharding, shadow databases, database discovery, and distributed transactions**. At the same time, how to use databases and the actual database storage nodes rely on **the virtual database and database endpoint**.

In response to the above-mentioned problems, OpenSergo and ShardingSphere have assimilated the latter's database governance experience and released the **database governance standard under microservices**. By doing so, they can standardize the database governance method, lower the entry barriers of this field, and improve the business applicability of microservices.

# ShardingSphere's strategies on database traffic governance

**1. VirtualDatabase**

In database governance, whether it is read/write splitting, sharding, shadow database or encryption, audit, and access control, they all have to act on a specific database. Here such a logical database is called a virtual database, namely VirtualDatabase.

From the application's viewpoint, VirtualDatabase refers to a set of specific database access information, which can achieve governance capability by binding corresponding governance strategies.

**2. DatabaseEndpoint**

In database governance, VirtualDatabase declares the logical database available to applications, but actually, data storage depends on a physical database. Here it is called database access endpoint, namely DatabaseEndpoint.

DatabaseEndpoint is imperceptible to applications. It can only be bound to VirtualDatabase by a specific governance strategy and then be connected and used.

**3. ReadWriteSplitting**

Read/write splitting is a commonly used database extension method. The primary database is used for transactional read/write operations, while the secondary database is mainly used for queries.

**4. Sharding**

Data sharding is an extension strategy based on data attributes. Once data attributes are calculated, requests are sent to a specific data backend. Currently, sharding consists of sharding with shard keys and automatic sharding. Sharding with shard keys needs to specify which table or column is to be sharded and the algorithms used for sharding.

**5. Encryption**

To meet the requirements of audit security and compliance, enterprises need to provide strict security measures for data storage, such as data encryption.

Data encryption parses the SQL entered by users and rewrites SQL according to the encryption rules provided by users.

By doing so, the plaintext data can be encrypted, and plaintext data (optional) and ciphertext data can be both stored in the underlying database.

When the user queries the data, Encryption only takes the ciphertext data from the database, decrypts it, and finally returns the decrypted original data to the user.

**6. Shadow**

Shadow database can receive grayscale traffic or data test requests in a grayscale environment or test environment, and flexibly configure multiple routing methods combined with shadow algorithms.

**7. DatabaseDiscovery**

Database auto-discovery can detect the change of data source status according to the high availability configuration of the database and then make adjustments to traffic strategy accordingly.

For example, if the backend data source is [MySQL](https://www.mysql.com/) MGR, you can set the database discovery type as MYSQL.MGR, specify `group-name` and configure corresponding heartbeat intervals.

**8. DistributedTransaction**

It can declare configurations related to distributed transactions. Users can declare the transaction types without additional configuration.

# Database Governance Example

```sql
# Virtual database configuration
apiVersion: database.opensergo.io/v1alpha1
kind: VirtualDatabase
metadata:
  name: sharding_db
spec:
  services:
  - name: sharding_db
    databaseMySQL:
      db: sharding_db
      host: localhost
      port: 3306
      user: root
      password: root
    sharding: "sharding_db"  # Declare the desired sharding strategy.
---
# The database endpoint configuration of the first data source
apiVersion: database.opensergo.io/v1alpha1
kind: DatabaseEndpoint
metadata:
  name: ds_0
spec:
  database:
    MySQL:                 # Declare the backend data source type and other related information.
      url: jdbc:mysql://192.168.1.110:3306/demo_ds_0?serverTimezone=UTC&useSSL=false
      username: root
      password: root
      connectionTimeout: 30000
      idleTimeoutMilliseconds: 60000
      maxLifetimeMilliseconds: 1800000
      maxPoolSize: 50
      minPoolSize: 1      
---
# The database endpoint configuration of the second data source
apiVersion: database.opensergo.io/v1alpha1
kind: DatabaseEndpoint
metadata:
  name: ds_1
spec:
  database:
    MySQL:                              # Declare the backend data source type and other related information.
      url: jdbc:mysql://192.168.1.110:3306/demo_ds_1?serverTimezone=UTC&useSSL=false
      username: root
      password: root
      connectionTimeout: 30000
      idleTimeoutMilliseconds: 60000
      maxLifetimeMilliseconds: 1800000
      maxPoolSize: 50
      minPoolSize: 1
---
# Sharding configuration
apiVersion: database.opensergo.io/v1alpha1
kind: Sharding
metadata:
  name: sharding_db
spec:
  tables: # map[string]object type
    t_order:
      actualDataNodes: "ds_${0..1}.t_order_${0..1}"
      tableStrategy:
        standard:
          shardingColumn: "order_id"
          shardingAlgorithmName: "t_order_inline"
      keyGenerateStrategy:
        column: "order_id"
        keyGeneratorName: "snowflake"
    t_order_item:
      actualDataNodes: "ds_${0..1}.t_order_item_${0..1}"
      tableStrategy:
        standard:
          shardingColumn: "order_id"
          shardingAlgorithmName: "t_order_item_inline"
      keyGenerateStrategy:
        column: order_item_id
        keyGeneratorName: snowflake
  bindingTables:
  - "t_order,t_order_item"
  defaultDatabaseStrategy:
    standard:
     shardingColumn: "user_id"
     shardingAlgorithmName: "database_inline"
  # defaultTableStrategy: # Null means none 
  shardingAlgorithms: # map[string]object type
    database_inline:
      type: INLINE    
      props: # map[string]string type
        algorithm-expression: "ds_${user_id % 2}"
    t_order_inline:  
      type: INLINE    
      props:
        algorithm-expression: "d_order_${order_id % 2}"      
    t_order_item_inline:
      type: INLINE    
      props:
        algorithm-expression: "d_order_item_${order_id % 2}"
  keyGenerators: # map[string]object type
    snowflake:
      type: SNOWFLAKE
```

# About Apache ShardingSphere

[Apache ShardingSphere](https://shardingsphere.apache.org/) is a distributed database ecosystem that can transform any database into a distributed database and enhance it with sharding, elastic scaling, encryption features & more.

Apache ShardingSphere follows the Database Plus concept, designed to build an ecosystem on top of fragmented heterogeneous databases. It focuses on how to fully use the computing and storage capabilities of databases rather than creating a brand-new database. It attaches greater importance to the collaboration between multiple databases instead of the database itself.

🔗 [**Apache ShardingSphere Useful Links**](https://linktr.ee/ApacheShardingSphere)

# About OpenSergo

[OpenSergo](https://opensergo.io/) is an open and universal service governance specification that is oriented toward distributed service architecture and covers a full-link heterogeneous ecosystem.

It is formed based on the industry's service governance scenarios and practices. The biggest characteristic of OpenSergo is defining service governance rules with a unified set of configuration/DSL/protocol and is oriented towards multi-language heterogeneous architecture, achieving full-link ecosystem coverage.

No matter if the microservice language is Java, Go, Node.js, or some other language, or whether it's a standard microservice or Mesh-based access, developers can use the same set of OpenSergo CRD standard configurations.

This allows developers to implement unified governance and control for each layer, ranging from the gateway to microservices, from database to cache, and from registration and discovery to the configuration of services.

🔗 [**OpenSergo GitHub**](https://github.com/opensergo/opensergo-specification)