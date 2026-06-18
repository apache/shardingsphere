+++
title = "ShardingSphere-Proxy Database Protocol Interaction Explained"
weight = 89
chapter = true 

+++

Database protocols are considered relatively unknown knowledge for most developers. Generally, users and developers use databases through off-the-shelf database clients and drivers, without manipulating database protocols directly.

However, having some basic understanding of the features and processes of database protocols can help developers provide some ideas to identify troubleshooting problems when it comes to database functionality and performance issues.

In this post, we will introduce the features of commonly used open source database protocols such as MySQL and PostgreSQL, and explain how ShardingSphere-Proxy interacts with clients at the database protocol level.

# Introduction to the ShardingSphere Access Side

The Apache ShardingSphere ecosystem includes ShardingSphere-JDBC and ShardingSphere-Proxy, deployable independently or in a hybrid deployment.

They both provide standardised incremental functionality based on databases as storage nodes for a variety of application scenarios such as Java homogeneous, heterogeneous languages, cloud native and more.

![img](https://shardingsphere.apache.org/blog/img/2023_02_23_ShardingSphere-Proxy_Database_Protocol_Interaction_Explained1.png)

ShardingSphere-JDBC is a Java-based SDK that implements the standard JDBC and is lightweight and high performance, but also has obvious limitations. Nevertheless, the limitations of ShardingSphere-JDBC in terms of access are addressed by ShardingSphere-Proxy on the access side.

- ShardingSphere-Proxy supports connections via any database client, database driver, and is not limited to JVM-based languages such as Java.
- Simplifies data management. In particular, in scenarios where data sharding or encryption is used, ShardingSphere-Proxy can be used as a unified portal to manipulate data without having to consider the actual node where the data is stored or to manually decrypt it, etc.
- Provides unified operation and maintenance control capabilities. In cluster mode, ShardingSphere-Proxy can be used to unify the management of ShardingSphere rules and configurations.
- Heavyweight operations can be performed. ShardingSphere-JDBC is in the same process as the application, where heavyweight computation and I/O operations may impact application performance, while ShardingSphere-Proxy starts as a separate process and supports horizontal scaling, and performing heavyweight operations without impacting application performance.

# Database Protocol Features

There are already a number of specific interpretations of MySQL or PostgreSQL protocols on the Internet, so we won't go into detail in this article.

This section focuses on the features of each database protocol, such as its support for Pipelining and how bulk operations are represented in the protocol.

## MySQL Protocol

The MySQL protocol is typically a “one question, one answer" protocol, e.g. to execute SQL using Prepared Statement. At the protocol level, you need to execute `COM_STMT_PREPARE` and `COM_STMT_EXECUTE` respectively.

![img](https://shardingsphere.apache.org/blog/img/2023_02_23_ShardingSphere-Proxy_Database_Protocol_Interaction_Explained2.png)

**Image source:** [MySQL documentation](https://dev.mysql.com/doc/dev/mysql-server/latest/mysqlx_protocol_use_cases.html).

MySQL has added an X Plugin since 5.7.12, which allows MySQL to add support for document type stores while maintaining the original relational store.

The X Plugin uses a new communication protocol, X Protocol, which uses port 33060 by default and supports pipelining, i.e. the client can send a batch of commands to the client at a time, reducing the RTT (Round-trip time) associated with the “one question, one answer" model (Round-trip time).

For example, if a SQL is executed using `Prepared Statement`, at the protocol level there are `Prepare` and `Execute` steps, but at the network transport level, these two steps can be combined and sent. This can theoretically reduce the RTT by one compared to the original protocol.

![img](https://shardingsphere.apache.org/blog/img/2023_02_23_ShardingSphere-Proxy_Database_Protocol_Interaction_Explained3.png)

**Image source:** [MySQL documentation](https://dev.mysql.com/doc/dev/mysql-server/latest/mysqlx_protocol_use_cases.html).

However, the X Plugin for MySQL does not seem to be catching on at the moment, and most scenarios are still based on the original MySQL protocol for client and server communication.

In the case of batch operations, the MySQL protocol command `COM_STMT_EXECUTE`, which executes the `Prepared Statement `statement, can only send one set of parameters at a time, making the "one question, one answer" approach somewhat inefficient:

![img](https://shardingsphere.apache.org/blog/img/2023_02_23_ShardingSphere-Proxy_Database_Protocol_Interaction_Explained4.png)

**Image source:** [MySQL documentation](https://dev.mysql.com/doc/dev/mysql-server/latest/mysqlx_protocol_use_cases.html).

The protocol itself is not designed to support batch operations, so they can only be optimised at the client level.

In the case of MySQL Connector/J, for example, when the parameter `rewriteBatchedStatements` is enabled, MySQL Connector/J will internally take the multiple sets of parameters set by the `addBatch` method, combine insert values or compose update/delete into multiple statements, and send them at the protocol level sent in one go.

This is a small increase in CPU overhead in exchange for a reduction in RTT. For example, for a Prepared Statement insert statement, add multiple parameters to execute:

```Mysql
INSERT INTO tbl VALUES (?, ?, ?);
addBatch [1, "foo", "bar"]
addBatch [2, "baz", "fuz"]
```

MySQL Connector/J Actual Execution Statement:

```Mysql
INSERT INTO tbl VALUES (1, "foo", "bar"),(2, "baz", "fuz");
```

For bulk update / delete, MySQL Connector/J will execute multiple statements via `COM_QUERY`, e.g:

```Mysql
UPDATE tbl SET name = ? WHERE id = ?;
addBatch ["foo", 1]
addBatch ["bar", 2]
```

MySQL Connector/J Actual Execution Statement:

```Mysql
UPDATE tbl SET name = "foo" WHERE id = 1;UPDATE tbl SET name = "bar" WHERE id = 2;
```

## PostgreSQL Protocol

Compared to the MySQL protocol, the PostgreSQL protocol definition looks simpler and the PostgreSQL protocol supports Pipelining:

PostgreSQL's Extended Query breaks up SQL execution into multiple steps, with the following common operations.

- **Parse:** parses the SQL into a `Prepared Statement`.
- **Describe:** Get the metadata of a `Prepared Statement` or `Portal`.
- **Bind:** Bind the actual parameters of the `Prepared Statement` to produce an executable `Portal`; `Bind: Bind `the actual parameters of the `Prepared Statement` to produce an executable `Portal`.
- **Execute:** executing the `Portal`.
- **Close:** closes the `Prepared Statement` or `Portal`.

An example of the PostgreSQL JDBC protocol interaction with the database is as follows:

![img](https://shardingsphere.apache.org/blog/img/2023_02_23_ShardingSphere-Proxy_Database_Protocol_Interaction_Explained5.png)

In a bulk operation scenario, the client can send multiple sets of parameters in one continuous `Bind`, `Execute`, which is multiple packets at the protocol level, but at the TCP transport level, one batch of packets can be sent out at a time.

![img](https://shardingsphere.apache.org/blog/img/2023_02_23_ShardingSphere-Proxy_Database_Protocol_Interaction_Explained6.png)

Pipelining-enabled protocols, combined with I/O multiplexing, offer certain advantages in terms of throughput. For example, Vert.x PostgreSQL, a database driver based on multiplexed I/O, scored first place in the TechEmpower Benchmark Round 15 test in the Single Query scenario (database spotting).

![img](https://shardingsphere.apache.org/blog/img/2023_02_23_ShardingSphere-Proxy_Database_Protocol_Interaction_Explained7.png)

Image source: [Tech Empower](https://www.techempower.com/benchmarks/#section=data-r15&test=db).

## openGauss Protocol

openGauss adds a Batch Bind message to the PostgreSQL Protocol 3.0, which only sends one set of parameters at a time.

openGauss adds a Batch Bind message to support sending multiple sets of parameters at the same time.

![img](https://shardingsphere.apache.org/blog/img/2023_02_23_ShardingSphere-Proxy_Database_Protocol_Interaction_Explained8.png)

Additionally, openGauss has been enhanced in terms of authentication security. The general flow of the protocol is identical to that of PostgreSQL.

# ShardingSphere-Proxy Front-End Interaction Flow Explained

## Relationship between ShardingSphere-Proxy and Database Protocol

Database protocols, like HTTP and other protocols, are the standard for communication between the client and the server. Each database defines its own protocol.

For example, the MySQL database defines its own set of protocols, as well as the Protobuf-based X Protocol; PostgreSQL also defines its own set of protocols and etc.

The average user or developer uses an off-the-shelf client or corresponding driver and the protocol is relatively transparent to them. Therefore, ShardingSphere-Proxy implements the database protocol and serves it to the public, so that users can use ShardingSphere-Proxy as if it were a database.

The specific database protocol versions currently supported by ShardingSphere-Proxy are:

- MySQL Protocol 4.1 (since MySQL 4.1)
- PostgreSQL Protocol 3.0 (since PostgreSQL 7.4)
- openGauss Protocol 3.00 / 3.50 / 3.51

## Overall Process for the Access Side

ShardingSphere-Proxy and ShardingSphere-JDBC share the ShardingSphere kernel module and provide different access to users.

ShardingSphere-Proxy exists as a standalone process and provides services to the outside world as a database protocol. ShardingSphere-JDBC is a set of SDKs that can be called directly by users through code.

![img](https://shardingsphere.apache.org/blog/img/2023_02_23_ShardingSphere-Proxy_Database_Protocol_Interaction_Explained9.png)

## ShardingSphere-Proxy Front-end Process

The ShardingSphere-Proxy front-end uses Netty to implement the database protocol. The Netty event-driven approach to front-end connections allows the ShardingSphere-Proxy front-end to maintain a large number of client connections.

The protocol unwrapping and encoding logic is mainly performed in the Netty `EventLoop` thread. As the ShardingSphere-Proxy backend still uses JDBC to interact with the database, a dedicated thread pool is used to execute the ShardingSphere kernel logic and database interaction after the protocol data has been unpacked to avoid blocking in the Netty `EventLoop` thread.

![img](https://shardingsphere.apache.org/blog/img/2023_02_23_ShardingSphere-Proxy_Database_Protocol_Interaction_Explained10.png)

`PacketCodecs` are mainly used for unpacking and encoding data. As mentioned earlier in the introduction to database protocols, the PostgreSQL protocol supports pipelining and can send a batch of packets at a time.

## Example:

The following diagram shows the request protocol for a PostgreSQL client to execute the `select current_schema()` statement using `Prepared Statement`, where the SQL parsing and execution steps of `Prepared Statement` are sent by the client to the server for execution in one go.

![img](https://shardingsphere.apache.org/blog/img/2023_02_23_ShardingSphere-Proxy_Database_Protocol_Interaction_Explained11.png)

What the server receives is a stream of bytes. How can this stream be split into multiple protocol packets?

Taking the PostgreSQL protocol format as an example, except for the Startup Message, the format of each protocol packet is 1 byte of message type + 4 bytes of data length (including the length itself) + data, with the following structure:

![img](https://shardingsphere.apache.org/blog/img/2023_02_23_ShardingSphere-Proxy_Database_Protocol_Interaction_Explained12.png)

The MySQL protocol packet format is similar.

The `PacketCodec` simply follows the protocol format definition and splits the incoming byte stream correctly.

After the byte stream has been split, the remaining steps are to parse the data according to the database protocol and get the SQL and parameters to be executed.

Once the SQL and parameters are available, the rest of the execution process is essentially the same as executing SQL via ShardingSphere-JDBC.

After the ShardingSphere-Proxy backend executes SQL via JDBC, the result set is a Java object and the `PacketCodec` calls the specific encoding logic to convert the Java object into a byte stream according to the database protocol, assembles it into a packet and responds to the client.

![img](https://shardingsphere.apache.org/blog/img/2023_02_23_ShardingSphere-Proxy_Database_Protocol_Interaction_Explained13.png)

This is the general flow of the ShardingSphere-Proxy front-end database protocol interaction.

## How to Feedback Suspected Proxy Protocol Issues to the ShardingSphere Community?

Due to the differences in computing power between ShardingSphere-Proxy and databases, the Proxy does not yet have 100% support for database protocols, and there are inevitably some unsupported cases in the process.

This article gives some suggestions for feedback when users encounter problems with Proxy that are suspected to be caused by imperfect implementation of the Proxy protocol.

## Demo Available for Simple Problem Replication

If you have a problem that can be reproduced by constructing simple code (e.g. just using Python and installing a few simple dependencies), you can provide the code and steps to reproduce the problem directly in the Github issue.

**Case:** a community member has submitted a problem with aDjango.db [transaction to ShardingSphere-Proxy MySQL](https://github.com/apache/shardingsphere/issues/18461).

The author provided a reproduction in the issue to help the ShardingSphere team fix the problem.

## Directly Submit a Problem Fix PR

For relatively simple issues, the ShardingSphere team can provide ideas for fixing them, and community members who are in a position to do so can consider submitting a PR to fix them directly.

**Case:** a community member gave feedback on an issue with ShardingSphere-Proxy that was reporting an error[ when connecting to it via Python asyncpg](https://github.com/apache/shardingsphere/issues/23885).

The problem is that the Python asyncpg database driver adds quotes to the encoding name when sending `client_encoding` to ShardingSphere-Proxy. The ShardingSphere-Proxy PostgreSQL does not take into account that the encoding name contains quotes (which is supported by the PostgreSQL database), resulting in an encoding recognition error.

The issue's author was already in a position to reproduce the problem, and submitted a PR to fix the problem directly under the guidance of the ShardingSphere team.

## Capture the Traffic Between the Client and the Proxy Using a Packet Capture Tool

Some heterogeneous languages users may encounter issues with ShardingSphere-Proxy that are not related to a specific functionality and are suspected to be at the protocol level.

Due to differences in the technology stack between the user and the ShardingSphere team, the ShardingSphere team may not be able to quickly reproduce the issue locally. In this case, consider feeding back to the ShardingSphere community by capturing network traffic between the client and the ShardingSphere-Proxy.

There is a lot of information available on the Internet on how to use this tool, so we won't mention them here.

**Case:** A .NET MySqlConnector using ShardingSphere-Proxy issue was submitted by a community member some time ago [with the following problem](https://github.com/apache/shardingsphere/issues/23857).

There is an error about the .NET connection to ShardingSphere-Proxy reported in the Issue. According to the stack, the error is caused during `TryResetConnectionAsync` and the last exception is thrown under the Protocol-related code, so this could be a ShardingSphere-Proxy protocol implementation does not behave the same way as MySQL.

```plaintext
An error occurred using the connection to database .....

 MySqlConnector.MySqlProtocolException: Packet received out-of-order. Expected 1; got 2.
         at MySqlConnector.Protocol.Serialization.ProtocolUtility.<DoReadPayloadAsync>g__AddContinuation|5_0(ValueTask`1 readPacketTask, BufferedByteReader bufferedByteReader, IByteHandler byteHandler, F
unc`1 getNextSequenceNumber, ArraySegmentHolder`1 previousPayloads, ProtocolErrorBehavior protocolErrorBehavior, IOBehavior ioBehavior) in /_/src/MySqlConnector/Protocol/Serialization/ProtocolUtility.cs:
line 476
         at MySqlConnector.Core.ServerSession.ReceiveReplyAsyncAwaited(ValueTask`1 task) in /_/src/MySqlConnector/Core/ServerSession.cs:line 943
         at MySqlConnector.Core.ServerSession.TryResetConnectionAsync(ConnectionSettings cs, MySqlConnection connection, IOBehavior ioBehavior, CancellationToken cancellationToken) in /_/src/MySqlConnect
or/Core/ServerSession.cs:line 616
```

As this issue is costly to reproduce, it's not easy for the ShardingSphere team to reproduce the issue locally, and the community has provided protocol traffic between the client and the Proxy.

![img](https://shardingsphere.apache.org/blog/img/2023_02_23_ShardingSphere-Proxy_Database_Protocol_Interaction_Explained14.png)

Based on the protocol packet capture results, the ShardingSphere team immediately identified the issue as a problem with the implementation of the ShardingSphere-Proxy MySQL packet encoding logic.

## Relevant Links:

**🔗** [**ShardingSphere Official Website**](https://shardingsphere.apache.org/)

**🔗** [**ShardingSphere Official Project Repo**](https://github.com/apache/shardingsphere)

**🔗** [**ShardingSphere Twitter**](https://twitter.com/ShardingSphere)

**🔗** [**ShardingSphere Slack**](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)