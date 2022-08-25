+++ 
title = "ShardingSphere-Proxy Front-End Protocol Troubleshooting Guide and Examples"
weight = 66
chapter = true 
+++

[ShardingSphere-Proxy](https://shardingsphere.apache.org/document/current/en/quick-start/shardingsphere-proxy-quick-start/), positioned as a transparent database proxy, is one of [Apache ShardingSphere](https://shardingsphere.apache.org/)’s access points.

ShardingSphere-Proxy implements a database protocol that can be accessed by any client using or compatible with [MySQL](https://www.mysql.com/) / [PostgreSQL](https://www.postgresql.org/) / [openGauss](https://opengauss.org/en/) protocols. The advantage of ShardingSphere-Proxy over [ShardingSphere-JDBC](https://shardingsphere.apache.org/document/current/en/overview/#shardingsphere-jdbc) is the support for heterogeneous languages and the operable entry point to the database cluster for DBAs.

Similar to ShardingSphere’s SQL parsing module, establishing ShardingSphere-Proxy’s support for database protocols is a long-term process that requires developers to continuously improve the database protocol deployment.

This post will introduce you to the tools commonly used in database protocol development by presenting a troubleshooting guide for ShardingSphere-Proxy MySQL protocol issues as a case study.
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/fq6vixjvjskcwoggzq2v.png)
 

## 1. Use Wireshark to analyze network protocols
[Wireshark](https://www.wireshark.org/) is a popular network protocol analysis tool with built-in support for parsing hundreds of protocols (including the MySQL / PostgreSQL protocols relevant to this article) and the ability to read many different types of packet capture formats.

The full features, installation and other details about Wireshark can be found in the official Wireshark documents.

## 1.1 Packet capture using tools like Wireshark or tcpdump
**1.1.1 Wireshark**
Wireshark itself has the ability to capture packets, so if the environment connected to ShardingSphere-Proxy can run Wireshark, you can use it to capture packets directly.

After initiating Wireshark , first select the correct network card.

For example, if you are running ShardingSphere-Proxy locally, the client connects to ShardingSphere-Proxy on port 3307 at 127.0.0.1 and the traffic passes through the Loopback NIC, which is selected as the target of packet capture.
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/r5i0nphl9vk859im6lea.png)
 

Once the NIC is selected, Wireshark starts capturing packets. Since there may be a lot of traffic from other processes on the NIC, it is necessary to filter out the traffic coming from specified port.
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/p11ab3e8qjqdiaa7repp.png)
 

**1.1.2 tcpdump**
In cases where ShardingSphere-Proxy is deployed in an online environment, or when you cannot use Wireshark to capture packets, consider using tcpdump or other tools.

NIC eth0 as target, filter TCP port 3307, write the result to /path/to/dump.cap. Command Example:

`tcpdump -i eth0 -w /path/to/dump.cap tcp port 3307`
To know how to use tcpdump, you can man tcpdump. tcpdump’s packet capture result file can be opened through Wireshark.

**1.1.3 Note**
When a client connects to MySQL, SSL encryption may be automatically enabled, causing the packet capture result to not directly parse the protocol content. You can disable SSL by specifying parameters using the MySQL client command line with the following command:

`mysql --ssl-mode=disable`
Parameters can be added using JDBC with the following parameters:

`jdbc:mysql://127.0.0.1:3306/db?useSSL=false`

## 1.2 Use Wireshark to read packet capture result
Wireshark supports reading multiple packet capture file formats, including tcpdump’s capture format.

By default, Wireshark decodes port 3306 to MySQL protocol and port 5432 to PostgreSQL protocol. For cases where ShardingSphere-Proxy may use a different port, you can configure the protocol for specified port in Decode As…

For example, ShardingSphere-Proxy MySQL uses 3307 port, which can be parsed as SQL protocols according to the following steps:
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/0xusspvc11lrc87earnk.png)
 ![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/k66xw1v28ocq3nh335jy.png)
 


Once Wireshark is able to parse out the MySQL protocol, we can add filters to display only the MySQL protocol data:

`tcp.port == 3307 and mysql`
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/sjdeclrsse0mvzyo03c3.png)
 

After selecting the correct protocol for the specified port, you can see the contents in the Wireshark window.

For example, after the client establishes a TCP connection with the server, the MySQL server initiates a `Greeting` to the client as shown below:
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/h1pck7o9xxnhk0d3zx90.png)
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/3sldzmzjbr81wtio465j.png)
  

Example: the client executes SQL select version() with the protocol shown below:
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/80t0hbqgl3xfa47nto7e.png)
 

## 2. Protocol Troubeshooting Case Study: ShardingSphere-Proxy MySQL support oversized data packages

## 2.1 Problem Description
Using MySQL Connector/J 8.0.28 as a client to connect to ShardingSphere-Proxy 5.1.1, bulk insertion error prompted while executing.

Problem solved after replacing driver MySQL Connector/J 5.1.38.

```
[INFO ] 2022-05-21 17:32:22.375 [main] o.a.s.p.i.BootstrapInitializer - Database name is `MySQL`, version is `8.0.28`
[INFO ] 2022-05-21 17:32:22.670 [main] o.a.s.p.frontend.ShardingSphereProxy - ShardingSphere-Proxy start success
[ERROR] 2022-05-21 17:37:57.925 [Connection-143-ThreadExecutor] o.a.s.p.f.c.CommandExecutorTask - Exception occur: 
java.lang.IllegalArgumentException: Sequence ID of MySQL command packet must be `0`.
 at com.google.common.base.Preconditions.checkArgument(Preconditions.java:142)
 at org.apache.shardingsphere.db.protocol.mysql.packet.command.MySQLCommandPacketTypeLoader.getCommandPacketType(MySQLCommandPacketTypeLoader.java:38)
 at org.apache.shardingsphere.proxy.frontend.mysql.command.MySQLCommandExecuteEngine.getCommandPacketType(MySQLCommandExecuteEngine.java:50)
 at org.apache.shardingsphere.proxy.frontend.mysql.command.MySQLCommandExecuteEngine.getCommandPacketType(MySQLCommandExecuteEngine.java:46)
 at org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask.executeCommand(CommandExecutorTask.java:95)
 at org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask.run(CommandExecutorTask.java:72)
 at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)
 at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)
 at java.base/java.lang.Thread.run(Thread.java:834)
[ERROR] 2022-05-21 17:44:24.926 [Connection-317-ThreadExecutor] o.a.s.p.f.c.CommandExecutorTask - Exception occur: 
java.lang.IllegalArgumentException: Sequence ID of MySQL command packet must be `0`.
 at com.google.common.base.Preconditions.checkArgument(Preconditions.java:142)
 at org.apache.shardingsphere.db.protocol.mysql.packet.command.MySQLCommandPacketTypeLoader.getCommandPacketType(MySQLCommandPacketTypeLoader.java:38)
 at org.apache.shardingsphere.proxy.frontend.mysql.command.MySQLCommandExecuteEngine.getCommandPacketType(MySQLCommandExecuteEngine.java:50)
 at org.apache.shardingsphere.proxy.frontend.mysql.command.MySQLCommandExecuteEngine.getCommandPacketType(MySQLCommandExecuteEngine.java:46)
 at org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask.executeCommand(CommandExecutorTask.java:95)
 at org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask.run(CommandExecutorTask.java:72)
 at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)
 at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)
 at java.base/java.lang.Thread.run(Thread.java:834)
```

## 2.2 Troubleshooting
The error occurred at the front end of the Proxy, which excludes the backend JDBC Driver and is related to the protocol implementation.

**2.2.1 Analysis**
Directly determine in the source code that if the sequence ID is not equal to 0, an error is reported.

```java
public final class MySQLCommandPacketTypeLoader {
    
    /**
     * Get command packet type.
     *
     * @param payload packet payload for MySQL
     * @return command packet type for MySQL
     */
    public static MySQLCommandPacketType getCommandPacketType(final MySQLPacketPayload payload) {
        Preconditions.checkArgument(0 == payload.readInt1(), "Sequence ID of MySQL command packet must be `0`.");
        return MySQLCommandPacketType.valueOf(payload.readInt1());
    }
}
```
**Code link:**
[https://github.com/apache/shardingsphere/blob/d928165ea4f6ecf2983b2a3a8670ff66ffe63647/shardingsphere-db-protocol/shardingsphere-db-protocol-mysql/src/main/java/org/apache/shardingsphere/db/protocol/mysql/packet/command/MySQLCommandPacketTypeLoader.java#L38](https://github.com/apache/shardingsphere/blob/d928165ea4f6ecf2983b2a3a8670ff66ffe63647/shardingsphere-db-protocol/shardingsphere-db-protocol-mysql/src/main/java/org/apache/shardingsphere/db/protocol/mysql/packet/command/MySQLCommandPacketTypeLoader.java#L38)

In accordance with MySQL protocol documentation, consider when the sequence ID will not equal 0 [2].

- The server responds multiple messages to the client.
- The client sends multiple consecutive messages.
- ……
In this case, the message header of MySQL Packet consists of 3 bytes length + 1 byte Sequence ID [3], so the maximum length of Payload part is 16 MB — 1.

Considering that the error is generated during bulk insertion, the problem might be that the data sent by the client exceeds the length limit of a single MySQL Packet and was split into multiple consecutive MySQL Packets, which the Proxy could not handle.

**2.2.2 Trying to recreate the problem**
Using a `longtext` type field. The original idea was to construct a SQL with a length of more than 16 MB, but inadvertently we found that the error was also reported when the SQL length was more than 8 MB. The code was reproduced as follows:

```java
try (Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:13306/bmsql", "root", "root")) {
    try (Statement statement = connection.createStatement()) {
        statement.execute("drop table if exists foo");
        statement.execute("create table foo (id bigint primary key, str0 longtext)");
        long id = ThreadLocalRandom.current().nextLong();
        String str0 = RandomStringUtils.randomAlphanumeric(1 << 23);
        String sql = "insert into foo (id, str0) values (" + id + ", '" + str0 + "')";
        System.out.println(sql.length());
        statement.execute(sql);
    }
}
```
Error reported as follows:
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/ykgz23olfk6pw4smytq1.png)
 

Wireshark packet capture results show that packet length 0x80003C == 8388668 with only one MySQL Packet, and the sequence ID is only 0:
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/svt0hq9nbm53arq4ve0j.png)
 

Debugging the code reveals that the readMediumLE() method used by the Proxy was a signed number and the Packet length was read as a negative number.
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/ldp56zvqvd0a3g3wez75.png)
 

The problem is relatively easy to fix, just replace the corresponding `readUnsignedMediumLE()` method.

Although the error message is consistent with the problem description, it does not yet completely solve the underlying issue.

After the length exceeding issue is fixed, continue troubleshooting the problem. Send approximately 64 MB of data to ShardingSphere-Proxy using the following code:

```java
try (Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:13306/bmsql", "root", "root")) {
    try (Statement statement = connection.createStatement()) {
        statement.execute("drop table if exists foo");
        statement.execute("create table foo (id bigint primary key, str0 longtext)");
        long id = ThreadLocalRandom.current().nextLong();
        String str0 = RandomStringUtils.randomAlphanumeric(1 << 26);
        String sql = "insert into foo (id, str0) values (" + id + ", '" + str0 + "')";
        statement.execute(sql);
    }
}
```
Error Occurred:
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/xl44b5uwm3oavlz9ufkb.png)
 

Analyze packet capture results:
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/n8k75fbhvm9l009ormot.png)
 
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/7oj0fsvqf7w1tzr3c3oe.png)
 

The results indicate that the client sent multiple 16MB data packets, and Wireshark was unable to parse the MySQL long packets properly, but we can locate the MySQL Packet Header by using the search function.

Correlating with ShardingSphere-Proxy MySQL decoding logic:

```java
int payloadLength = in.markReaderIndex().readUnsignedMediumLE();
int remainPayloadLength = SEQUENCE_LENGTH + payloadLength;
if (in.readableBytes() < remainPayloadLength) {
    in.resetReaderIndex();
    return;
}
out.add(in.readRetainedSlice(SEQUENCE_LENGTH + payloadLength));
```
The problem is clear: because ShardingSphere-Proxy didn’t aggregate packets, multiple packets are parsed separately by Proxy as multiple commands, and because the `Sequence ID` of subsequent packets is greater than 0, the Proxy’s internal assertion logic for the Sequence ID reported an error.

## 2.3 Troubleshooting and Repair
After troubleshooting, the error was reported as:

- (Direct cause) The ShardingSphere-Proxy MySQL protocol unpacket logic does not handle the length sign correctly [4].
- (Root cause) ShardingSphere-Proxy MySQL does not aggregate packets larger than 16 MB [5].

It is first important to understand how MySQL protocol handles very long packets [6].

- When the total data length exceeds 16 MB — 1, the protocol splits the data into multiple Packets of length 16 MB — 1 until the final data length is less than 16 MB — 1, as shown in the following figure:
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/dwundre9oa9slkzyx6vu.png)
 

- When the data length is exactly equal to 16 MB — 1 or a multiple thereof, one or more packets of length 16 MB — 1 are sent followed by a packet of length 0, as shown in the following figure:
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/2ag3mbjanbchj012eu5c.png)
 
**Solution:** in order for the protocol implementation of ShardingSphere-Proxy MySQL to not care about how very long packets are handled, it is better to aggregate the packets in the data decoding logic.

In the ShardingSphere-Proxy front-end [Netty](https://netty.io/) decoding logic, when a data Packet of length `0xFFFFFF` is encountered, the Payload part of multiple MySQL Packets is aggregated via CompositeByteBuf.

See the Pull Request in the reference documentation for specific codes.

The following issues are currently fixed:

- Correctly handle packet length numeric notation [7].
- MySQL protocol decoding logic supports more than 16 MB packets [8].
Potential issues to be addressed later, including but not limited to:

- The MySQL protocol encoding logic does not support responding to packets larger than 16 MB.

## 3. Summarizing troubleshooting methods for Shardingsphere front-end protocols
For protocol-based problem troubleshooting, you first need to be familiar with the corresponding protocols and the database protocols including but not limited to:

Observing the protocol of the client’s direct connection to the database via packet capture tools.

According to database protocol documents, read the protocol coding logic source code of the official client database (e.g. JDBC Driver).

Once you have a basic grasp of packet capture tools and protocols, you can start troubleshooting ShardingSphere-Proxy front-end protocol issues.

The code entry, where ShardingSphere-Proxy establishes the connection with the client, is in org.apache.shardingsphere.proxy.frontend.netty.ServerHandlerInitializer[9], which can be used as a starting point to identify the problem.

The solutions presented in this article have been released with Apache ShardingSphere 5.1.2 [10].

## Relevant Links:
[1] [https://www.wireshark.org/](https://www.wireshark.org/)

[2] [https://dev.mysql.com/doc/internals/en/sequence-id.html](https://dev.mysql.com/doc/internals/en/sequence-id.html
)
[3] [https://dev.mysql.com/doc/internals/en/mysql-packet.html](https://dev.mysql.com/doc/internals/en/mysql-packet.html)

[4] [https://github.com/apache/shardingsphere/issues/17891](https://github.com/apache/shardingsphere/issues/17891)

[5] [https://github.com/apache/shardingsphere/issues/17907](https://github.com/apache/shardingsphere/issues/17907)

[6] [https://dev.mysql.com/doc/internals/en/sending-more-than-16mbyte.html](https://dev.mysql.com/doc/internals/en/sending-more-than-16mbyte.html)

[7] [https://github.com/apache/shardingsphere/pull/17898](https://github.com/apache/shardingsphere/pull/17898)

[8] [https://github.com/apache/shardingsphere/pull/17914](https://github.com/apache/shardingsphere/pull/17914
)
[9] [https://github.com/apache/shardingsphere/blob/2c9936497214b8a654cb56d43583f62cd7a6b76b/shardingsphere-proxy/shardingsphere-proxy-frontend/shardingsphere-proxy-frontend-core/src/main/java/org/apache/shardingsphere/proxy/frontend/netty/ServerHandlerInitializer.java](https://github.com/apache/shardingsphere/blob/2c9936497214b8a654cb56d43583f62cd7a6b76b/shardingsphere-proxy/shardingsphere-proxy-frontend/shardingsphere-proxy-frontend-core/src/main/java/org/apache/shardingsphere/proxy/frontend/netty/ServerHandlerInitializer.java
)
[10] [https://shardingsphere.apache.org/document/current/cn/downloads/](https://shardingsphere.apache.org/document/current/cn/downloads/)

[GitHub issue](https://github.com/apache/shardingsphere/issues)

[Contributor Guide](https://shardingsphere.apache.org/community/en/involved/)

[ShardingSphere Twitter](https://twitter.com/ShardingSphere)

[ShardingSphere Slack
](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)
[Chinese Community](https://community.sphere-ex.com/)

## Author

Weijie Wu, Apache ShardingSphere PMC, R&D Engineer of [SphereEx](https://www.sphere-ex.com/en/) Infrastructure. Weijie focuses on the Apache ShardingSphere access side and the ShardingSphere subproject [ElasticJob](https://shardingsphere.apache.org/elasticjob/).
