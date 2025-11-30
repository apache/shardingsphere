+++
title = "Updates and FAQ — Your 1 Minute Quick Start Guide to ShardingSphere"
weight = 20
chapter = true
+++

## Background

Apache ShardingSphere is an Apache Top-Level project and is one of the most popular open-source big data projects. It was started about 5 years ago, and now ShardingSphere has over 14K+ stars and 270+ contributors in its community.

The successful project has already launched and updated many versions. Apache ShardingSphere now supports many powerful features and keeps optimizing its configuration rules. We want to help users understand all features and configuration rules, to help them quickly test and run components, and ultimately help them achieve best performance, so we decide to start the shardingsphere-example project.

shardingsphere-example is an independent Maven project. It’s preserved in the “examples” file of Apache ShardingSphere. Link:

[https://github.com/apache/shardingsphere/tree/master/examples]()

## Modules & Explanation

The shardingsphere-example project contains many modules. It provides users with guides and configuration examples of features like horizontal scaling, read and write separation, distributed governance, distributed transaction, data encryption, hint manager, shadow database, etc.

It also includes common tools such as Java API, YAML, Spring Boot, Spring Namespace. In addition to ShardingSphere-JDBC, now we add use examples of ShardingSphere-Proxy and ShardingSphere-Parser in shardingsphere-example. You can easily find all features of Apache ShardingSphere as well as their scenarios, and their flexible configurations in our official repo. The table below shows how the modules are distributed in shardingsphere-example.

~~~
shardingsphere-example
├── example-core
│ ├── config-utility
│ ├── example-api
│ ├── example-raw-jdbc
│ ├── example-spring-jpa
│ └── example-spring-mybatis
├── shardingsphere-jdbc-example
│ ├── sharding-example
│ │ ├── sharding-raw-jdbc-example
│ │ ├── sharding-spring-boot-jpa-example
│ │ ├── sharding-spring-boot-mybatis-example
│ │ ├── sharding-spring-namespace-jpa-example
│ │ └── sharding-spring-namespace-mybatis-example
│ ├── governance-example
│ │ ├── governance-raw-jdbc-example
│ │ ├── governance-spring-boot-mybatis-example
│ │ └── governance-spring-namespace-mybatis-example
│ ├── transaction-example
│ │ ├── transaction-2pc-xa-atomikos-raw-jdbc-example
│ │ ├── transaction-2pc-xa-bitronix-raw-jdbc-example
│ │ ├── transaction-2pc-xa-narayana-raw-jdbc-example
│ │ ├── transaction-2pc-xa-spring-boot-example
│ │ ├── transaction-2pc-xa-spring-namespace-example
│ │ ├── transaction-base-seata-raw-jdbc-example
│ │ └── transaction-base-seata-spring-boot-example
│ ├── other-feature-example
│ │ ├── encrypt-example
│ │ │ ├── encrypt-raw-jdbc-example
│ │ │ ├── encrypt-spring-boot-mybatis-example
│ │ │ └── encrypt-spring-namespace-mybatis-example
│ │ ├── hint-example
│ │ │ └── hint-raw-jdbc-example
│ │ └── shadow-example
│ │ │ ├── shadow-raw-jdbc-example
│ │ │ ├── shadow-spring-boot-mybatis-example
│ │ │ └── shadow-spring-namespace-mybatis-example
│ ├── extension-example
│ │ └── custom-sharding-algortihm-example
├── shardingsphere-parser-example
├── shardingsphere-proxy-example
│ ├── shardingsphere-proxy-boot-mybatis-example
│ └── shardingsphere-proxy-hint-example
└── src/resources
└── manual_schema.sql
~~~

**example-core**

The module example-core contains entity, interface definition and other public codes

**shardingsphere-jdbc-example**

The example module ShardingSphere-JDBC displays ShardingSphere-JDBC features and how to use them.

**sharding-example**

The module displays how to use ShardingSphere-JDBC to scale out in scenarios like sharding, horizontal scaling, veritical scaling, read and write seperation, as well as read and write seperation plus sharding.

In terms of integration with ORM, this module also provides users with examples of MyBatis and JPA integrations.

**governance-example**

This module is about the distributed governance of ShardingSphere-JDBC, and includes related scenerios combined with features like sharding, read and write seperation, data encryption, shadow database.

>Note: The example of distributed governance depends on Apache Zookeeper. Please adopt self-deloyment.

**transaction-example**

This module displays the multiple ways of distributed transaction management ShardingSphere-JDBC supports. A user can base on his application and choose an appropriate distributed transaction coordinator. Given the complexity of distributed transactions, all examples in this module are based on vertical scaling, horizontal scaling and sharding.

>Note: When you use Seata, please adopt self-deployment.

**other-feature-example**

This module gives examples of some ShardingSphere-JDBC features, i.e., encrypt (data encryption), hint (hint manager), shadow (shadow database).

**encrypt-example**

This module displays examples of data encryption. It also tells users how to use and access Java API, YAML, Spring Boot, Spring Namespace.

**hint-example**

This shows examples of hint manager. However, at present, there is only YAML configuration example. We welcome more scenarios.

**shadow-example**

This gives examples of shadow database, including its application combined with data encryption, sharding, and read/write separation.

**extension-example**

The module tells users how to use custom extension of ShardingSphere-JDBC. Users can leverage SPI or other ways provided by ShardingSphere to extend features.

**custom-sharding-algortihm algorithm-example**

The module shows how a user can use ‘CLASS_BASED’ and customize his sharding algorithm.

**shardingsphere-parser-example**

SQLParserEngine is the SQL parse engine of Apache ShardingSphere. It is also the base of ShardingSphere-JDBC and ShardingSphere-Proxy. When a user inputs a SQL text, SQLParserEngine parses it and makes it recognizable expressions. Then it’s fine to have enhancement such as routing or rewriting.

Following the release of its 5.0.0-alpha version, Apache ShardingSphere’s core feature SQL Parser is totally open to its users. They can use API and call SQLParserEngin. This way they can meet more of their own business demands by having such an effective SQL parsing in their systems.

In the module, users can learn how to use SQLParserEngine API. It provides different syntactical rules of languages, such as MySQL, PostgreSQL, Oracle, SQL Server and SQL 92.

**shardingsphere-proxy-example**

The example module of ShardingSphere-Proxy includes configuration examples of common scenarios like sharding, read and write separation and hint manager. Since features of ShardingSphere-Proxy are almost the same as that of ShardingSphere-JDBC, users can refer to shardingsphere-jdbc-example when they fail to find the example they want in shardingsphere-proxy-example.

**shardingsphere-proxy-boot-mybatis-example**

In the module users can learn how they can use Proxy to configure sharding, and how they use SpringBoot + MyBatis to access data.

**shardingsphere-proxy-hint-example**

In this module, a user can know how to use Proxy to configure hint manager and how to use Java cliend-end to access data.

## New Optimization

Apache ShardingSphere 5.0.0-beta version is coming soon, so the community contributors also have updated shardingsphere-example. They optimized the following:

* JDK version

* Component version

* ClassName

* Configuration profiles

* SQL script

Related details are as follows:

**JDK version upgrade**

According to JetBrains’s “A Picture of Java in 2020”, Java8 LTS is the most popular version among Java developers.

![](https://shardingsphere.apache.org/blog/img/Blog_20_img_1_Popularity_of_Java_versions_in_2020_en.png)

Following this update, shardingsphere-example uses Java 8 and newer versions. If you use Java 7 or earlier versions, please update your JDK version first.

**String dependency upgrade**

In shardingsphere-example, we update string dependency components.

* spring-boot version from1.5.17 to 2.0.9.RELEASE

* springframework version from 4.3.20.RELEASE to 5.0.13.RELEASE

* mybatis-spring-boot-start version from 1.3.0 to 2.0.1

*  mybatis-spring version from 1.3.0 to 2.0.1

**Persistence framework upgrade**

In sharding-sphere-example, we update the persistence frameworks MyBatis and Hibernate.

* mybatis version from 3.4.2 to 3.5.1

* hibernate version from 4.3.11.Final to 5.2.18.Final

**Connection pooling upgrade**

In sharding-sphere-example, we update the database connection pool HikariCP.

* HikariCP artifactId from HikariCP-java7 to HikariCP

* HikariCP version from 2.4.11 to 3.4.2

**Database driver upgrade**

In sharding-sphere-example, we update the database connection drivers of MySQL and PostgreSQL

* mysql-connector-java version from 5.1.42 to 5.1.47

* postgresql version from 42.2.5.jre7 to 42.2.5

## Example

In this section, we give several typical examples and show you how to configure and run shardingsphere-example.

There are many modules in the project shardingsphere-example. But for now, we only choose several popular application scenarios of ShardingSphere-JDBC.

**Preparation**

1. Maven is the project’s build tool of shardingsphere-example. Please prepare for it first;

2. Prepare Apache ShardingSphere. If you have not downloaded Apache ShardingSphere, please download and compile it first. You can use the reference below:

> git clone https://github.com/apache/shardingsphere.git
cd shardingsphere
mvn clean install -Prelease,default-dep

3. Import the shardingsphere-example project to your IDE;

4. Prepare a manageable database environment, such as local MySQL examples;

5. If you need to test read and write separation, please make sure that your your master-slave synchronization is OK;

6. Execute the DB init script:：examples/src/resources/manual_schema.sql

## Scenarios & Examples

**sharding-spring-boot-mybatis-example: Sharding**

**1. Path**

examples/shardingsphere-jdbc-example/sharding-example/sharding-spring-boot-mybatis-example

**2. Goal**

This example shows the application of using ShardingSphere-JDBC in combination with SpringBoot and MyBatis to realize sharding. The sharding goal is to shard one table into four evenly preserved in two different databases.

**3. Preparation**

* Configure application.properties

* set spring.profiles.active as sharding-databases-tables

* Configure application-sharding-databases-tables.

* Change jdbc-url with your database location and setup your user ID, password, etc.

* Set the attribute of spring.shardingsphere.props.sql-show as true

See more details in _Configuration Manual_

**4. Run**

Run at startup:ShardingSpringBootMybatisExample.java

Now, you can observe routing of all SQL expressions in Logic SQL and Actual SQL of logs and understand how sharding works.

**Sharding-raw-jdbc-example: Read and write splitting**

**1. Path**

examples/shardingsphere-jdbc-example/sharding-example/sharding-raw-jdbc-example

**2. Goal**

The example shows how users can use YAML and configure the feature read and write splitting of ShardingSphere-JDBC . The goal is to separate one writing database and two reading databases.

**3. Preparation**

* Configure META-INF/readwrite-splitting.yaml

* Change jdbc-url with your database location and setup your user ID, password, etc.

* Set props.sql-show as true

See more details in _Configuration Manual_.

**4. Run**

Open the startup: ShardingRawYamlConfigurationExample.javaand set *shardingType* as *ShardingType.READWRITE_SPLITTING*. Run the startup.

Now, you can observe routing of all SQL expressions in Logic SQL and Actual SQL of logs and understand how read and write splitting works.

*>Note: when master-slave database synchronization fails, there will be query errors.*

**Custom-sharding-algortihm-example: Custom algorithm**

**1. Path**

examples/shardingsphere-jdbc-example/extension-example/custom-sharding-algortihm-example/class-based-sharding-algorithm-example

**2. Goal**

The example shows how users can use CLASS_BASED and extend their own custom algorithm . By doing so, ShardingSphere-JDBC can base on a custom algorithm and calculate sharding results during sharding routing. The scenario is how to use custom sharding algorithm to scale out.

**3. Preparation**

Prepare your sharding algorithm that shall base on business needs and use any interfaces of `StandardShardingAlgorithm`, `ComplexKeysShardingAlgorithm` or `HintShardingAlgorithm`. In the example, we show how to use `ClassBasedStandardShardingAlgorithmFixture`

* Configure META-INF/sharding-databases.yaml

* Change jdbc-url with your database location and setup your user ID, password, etc.

* Set props.sql-show as true.

*> Note: For shardingAlgorithms, when a type is CLASS_BASED, you can use props and assign class and absolute path of a custom algorithm. Thus, the configuration is done.*

See more details in _Configuration Manual_.

**4. Run**

Run the startup: YamlClassBasedShardingAlgorithmExample.java

Now you can use logs and observe your database. You can also use methods like DEBUG and check input and output of your custom algorithm.

## Summary

Our brief ends here. In the future, we will share with you more examples of ShardingSphere-JDBC, ShardingSphere-Proxy and ShardingSphere-Parser.

If you have any questions or have found any issues, we are looking forward to your comments on our _GitHub issue_, or you can submit your pull request and join us, or join our _Slack community_. We welcome anyone who would like to be part of this Top-Level project and make a contribution. For more information please visit our _Contributor Guide_.

**Authors**

![](https://shardingsphere.apache.org/blog/img/Blog_20_img_2_Jiang_Longtao_Photo.png)

I’m Jiang Longtao, SphereEx middleware engineer & Apache ShardingSphere contributor. At present, I focus on ShardingSphere database middleware and its open source community.

![](https://shardingsphere.apache.org/blog/img/Blog_20_img_3_Hou_Yang_Photo.png)

I’m Hou Yang, and I am a middleware enigneer at SphereEx. I love open source and I want to contribute to building a better community with everyone.

**ShardingSphere Community:**

ShardingSphere Github: [https://github.com/apache/shardingsphere]()

ShardingSphere Twitter: [https://twitter.com/ShardingSphere]()

ShardingSphere Slack Channel: [apacheshardingsphere.slack.com]()





