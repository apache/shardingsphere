+++
title = "Create a Distributed Database Solution Based on PostgreSQL/openGauss"
weight = 28
chapter = true
+++

# Create a Distributed Database Solution Based on PostgreSQL/openGauss

As the MySQL ShardingSphere-Proxy is becoming mature and widely accepted, ShardingSphere is also focusing on the PostgreSQL ShardingSphere-Proxy.

Compared with the alpha and beta, lots of improvements such as PostgreSQL agreement realization, SQL support and control, have been made in ShardingSphere-Proxy 5.0.0. This lays the foundation for full docking with the PostgreSQL ecosystem in the future. The ecosystem integration of ShardingSphere-Proxy and PostgreSQL provides users, on the basis of PostgreSQL database, with transparent and enhanced capabilities, such as: data sharding, read/write splitting, shadow database, data masking and data desensitization as well as distributed governance.

Besides PostgreSQL, the open source database openGauss, which is developed by Huawei, is also gaining increasing popularity. With the capability and ecosystem of Shardingsphere openGauss, which enjoys outstanding standalone performance, it’ll be possible to create distributed database solutions that meet the needs of increasingly diversified scenarios.

Currently, ShardingSphere PostgreSQL and openGauss Proxy supports most capabilities of the Apache ShardingSphere ecosystem, including data sharding, read/write splitting, shadow database, data masking/desensitization and distributed governance. Therefore, it’s almost as mature as the ShardingSphere MySQL Proxy.

This article will introduce improvements to the ShardingSphere-Proxy 5.0.0 built on PostgreSQL and its ecosystem integration with openGauss.

## Introduction to ShardingSphere-Proxy

ShardingSphere-Proxy is an adapter in the ShardingSphere ecosystem and is positioned as a transparent database proxy to users. ShardingSphere Proxy is not limited to Java. Instead, it realizes MySQL and PostgreSQL database protocols, and users can use various clients compatible with MySQL / PostgreSQL protocols to access and manipulate data.

![](https://shardingsphere.apache.org/blog/img/PostgreSQL_openGauss_img_1.png)


|       | ShardingSphere-JDBC | ShardingSphere-Proxy     |
| :---        |    :----:   |          ---: |
| Database      | Any       | Databases based on MySQL / PostgreSQL protocol   |
| Connected Consumption   | High        | Low      |
| Heterogeneous Language   | Support Java among other JVM-based languages        | Any      |
| Performance   | Low loss        | Relatively high loss      |
| Decentralization   | Yes        | No      |
| Static Entry   | No        | Yes      |

> ShardingSphere-Proxy is capable of addressing the following problems: with sub-database, sub-table or other rules in place, the data will be distributed to multiple database instances, which will inevitably cause some inconvenience to management; or when non-Java developers need the capabilities provided by ShardingSphere…this is exactly what ShardingSphere-Proxy was built for.

ShardingSphere-Proxy hides the back-end database. For the client, it’s just like using a database. Users don’t need to worry about how ShardingSphere coordinates the databases behind it. Therefore, it is more friendly to non-Java developers or DBAs.

Protocol-wise, ShardingSphere PostgreSQL Proxy realizes most Extended Query protocols and supports heterogeneous languages to drive and connect Proxy through PostgreSQL and openGauss. Based on reusing the PostgreSQL protocol, ShardingSphere openGauss Proxy also supports openGauss’s unique function of batch insertion protocol.

However, since ShardingSphere-Proxy has an extra layer of network interaction compared to ShardingSphere-JDBC, the delay of SQL execution increases, and the loss is slightly higher than that of ShardingSphere-JDBC.

## ShardingSphere-Proxy and PostgreSQL Ecosystem Integration


![Be compatible with PostgreSQL Simple Query and Extended Query](https://shardingsphere.apache.org/blog/img/PostgreSQL_openGauss_img_2.png)

Simple Query and Extended Query are the most common protocols for most users using PostgreSQL. For instance, when using the following command line tool `psql` to connect PostgreSQL for CRUD operation, the Simple Query is often used to interact with the database.

`$ psql -h 127.0.0.1 -U postgres
psql (14.0 (Debian 14.0-1.pgdg110+1))
Type "help" for help.
postgres=# select id, name from person where age < 35;
 id | name 
----+------
  1 | Foo
(1 row)`

The protocol interaction diagram of Simple Query is as follows:

![3](https://shardingsphere.apache.org/blog/img/PostgreSQL_openGauss_img_3.png)

When using PostgreSQL JDBC Driver and other drivers, code is as follows PreparedStatement, which corresponds to the Extended Query protocol in default.

`String sql = "select id, name from person where age > ?";
PreparedStatement ps = connection.prepareStatement(sql);
ps.setInt(1, 35);
ResultSet resultSet = ps.executeQuery();`

The protocol interaction diagram of Extended Query is as follows:

![4](https://shardingsphere.apache.org/blog/img/PostgreSQL_openGauss_img_4.png)

Currently, ShardingSphere PostgreSQL Proxy realizes protocols of Simple Query with the most Extended Query. However, since database client end and driver have encapsulated API for users to use, users do not need to worry about database protocols.

ShardingSphere-Proxy is compatible with Simple Query and Extended Query of PostgreSQL, meaning that users could use commonly used PostgreSQL client ends or drivers to connect with ShardingSphere-Proxy for CRUD operation to make use of the incremental capability provided by ShardingSphere in the databases’ upper layer.

## ShardingSphere-Proxy and openGauss Ecosystem Integration

### Support openGauss JDBC Driver

openGauss database has a corresponding JDBC Driver. The prefix of JDBC URL is `jdbc:opengauss`. Although JDBC drivers using PostgreSQL can also connect with openGauss database, batch insertion and other unique features of openGauss would not be able to function fully. ShardingSphere integrates openGauss database which could recognize openGauss JDBC Driver, **allowing developers to use the openGauss JDBC Driver directly with ShardingSphere.**

### Support openGauss Batch Insertion Protocol

For example, when we prepare an insert sentence such as the following:

`insert into person (id, name, age) values (?, ?, ?)`

Taking JDBC for example, we could use the following method to carry out batch insertion:

`String sql = "insert into person (id, name, age) values (?, ?, ?)";
PreparedStatement ps = connection.prepareStatement(sql);
ps.setLong(1, 1);
ps.setString(2, "Foo");
ps.setInt(3, 18);
ps.addBatch();
ps.setLong(1, 2);
ps.setString(2, "Bar");
ps.setInt(3, 36);
ps.addBatch();
ps.setLong(1, 3);
ps.setString(2, "Tom");
ps.setInt(3, 54);
ps.addBatch();
ps.executeBatch();`

At the PostgreSQL protocol layer, `Bind` can transfer one set of parameters to form Portal, and `Execute` can conduct one Portal each time.

Batch insertion could be realized through the repetition of `Bind` and `Execute`. The protocol interaction diagram is as follows:

![PostgreSQL Batch Insertion](https://shardingsphere.apache.org/blog/img/PostgreSQL_openGauss_img_5.png)

`Batch Bind` is a message exclusive to openGauss. Compared with `Bind`, `Batch Bind` can transfer multiple sets of parameters at a time. The protocol interaction diagram using `Batch Bind` to perform batch insertion is as follows:

![openGauss Batch Insertion](https://shardingsphere.apache.org/blog/img/PostgreSQL_openGauss_img_6.png)

ShardingSphere-Proxy openGauss supports Batch Bind protocol, meaning that **users could use the openGauss client end or driver to perform batch insertion of the ShardingSphere Proxy.**

##Future ShardingSphere-Proxy Developments

###Support ShardingSphere PostgreSQL Proxy Logic Query in MetaData

ShardingSphere-Proxy is a transparent database proxy, which means that users do not need to think about how Proxy coordinates the databases.

In the picture below, when configuring logic database `sharding_db` and logic table `person` ShardingSphere-Proxy, there are four tables in two databases behind Proxy.

![](https://shardingsphere.apache.org/blog/img/PostgreSQL_openGauss_img_7.png)

Currently, when executing `show schemas` and `show tables` languages in ShardingSphere MySQL Proxy, the query results are listed as logic database `sharding_db` and logic table `person`.

When using `psql` to connect `PostgreSQL`, users could query databases and tables through `\l`, `\d` and other requests. 

However, different from MySQL, `show tables` is a language supported by MySQL, while `\d` used in `psql` actually corresponds to a more complicated SQL. Currently, when using ShardingSphere PostgreSQL Proxy, logic databases or logic tables cannot be queried.

###Describe Prepared Statement Supporting Extended Query

There are two types of Describe message in PostgreSQL protocol, which are Describe Portal and Describe Prepared Statement. Currently, the ShardingSphere Proxy only supports Describe Portal.

An example of the practical application of Describe Prepared Statement:

Get the MetaData of the result set before executing PreparedStatement.

`PreparedStatement preparedStatement = connection.prepareStatement("select * from t_order limit ?");
ResultSetMetaData metaData = preparedStatement.getMetaData();`

The ecosystem integration of ShardingSphere and PostgreSQL and openGauss is still underway, with some steps left before full completion. If you are interested in what we are doing, you’re welcome to join the ShardingSphere community on GitHub, Twitter, Slack or through the official mailing list. 


**Reference**

* https://www.postgresql.org/docs/current/protocol.html

* https://gitee.com/opengauss/openGauss-connector-jdbc/blob/master/pgjdbc/src/main/java/org/postgresql/core/v3/QueryExecutorImpl.java#L1722

* https://gitee.com/opengauss/openGauss-connector-jdbc/blob/master/pgjdbc/src/main/java/org/postgresql/core/v3/QueryExecutorImpl.java#L1722

**Open Source Project Links:**

ShardingSphere Github: https://github.com/apache/shardingsphere

ShardingSphere Twitter: https://twitter.com/ShardingSphere

ShardingSphere Slack Channel:https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg

GitHub Issues: https://github.com/apache/shardingsphere/issues

Contributor Guide:https://shardingsphere.apache.org/community/cn/involved/

GitHub: https://github.com/apache/shardingsphere

### Author

>Apache ShardingSphere Committer & Middleware Engineer at SphereEx. Contributed to the development of Apache ShardingSphere and Apache ShardingSphere ElasticJob.

![](https://shardingsphere.apache.org/blog/img/Wu_Weijie_Photo.png)
