+++
title = "A Distributed Database Load Balancing Architecture Based on ShardingSphere: Demo and User Case"
weight = 87
chapter = true
+++

This post introduces how to build a distributed database load-balancing architecture based on ShardingSphere, and the impact of introducing load balancing with a user case.

Finally, a one-stop solution for Apache ShardingSphere distributed databases on the cloud will be presented and demonstrated at the end.

# ShardingSphere-based distributed database load balancing architecture

## ShardingSphere Load Balancing Architecture Essentials

As many of our readers may already know, Apache ShardingSphere is a distributed database ecosystem that transforms any database into a distributed database and enhances it with data sharding, elastic scaling, encryption, and other capabilities.

It consists of two products, ShardingSphere-JDBC and ShardingSphere-Proxy, which can be deployed independently and support hybrid deployments for use with each other. The following figure illustrates the hybrid deployment architecture:

![img](https://shardingsphere.apache.org/blog/img/2023_02_15_A_Distributed_Database_Load_Balancing_Architecture_Based_on_ShardingSphere_Demo_&_User_Case1.png)

**ShardingSphere-JDBC Load Balancing Solution**

ShardingSphere-JDBC is a lightweight Java framework with additional services provided in the JDBC layer. ShardingSphere-JDBC simply adds computational operations before the application performs database operations, and the application process still connects directly to the database via the database driver.

As a result, users don't have to worry about load balancing with ShardingSphere-JDBC, and can focus on how their application is load balanced.

**SharidngSphere-Proxy Load Balancing Solution**

***Deployment Structure***

ShardingSphere-Proxy is a transparent database proxy that provides services to database clients via the database protocol. The following figure shows the for ShardingSphere-Proxy as a standalone deployed process with load balancing on top of it:

![img](https://shardingsphere.apache.org/blog/img/2023_02_15_A_Distributed_Database_Load_Balancing_Architecture_Based_on_ShardingSphere_Demo_&_User_Case2.png)

***Load Balancing Solution Essentials***

Some of our community contributors have discussed in detail how to build a ShardingSphere-Proxy cluster, and some have asked about the inconsistent behavior of ShardingSphere-Proxy after load balancing:

- How to build a ShardingSphere-Proxy cluster? https://github.com/apache/shardingsphere/discussions/12593
- Requests sent by clients through HA-Proxy do not poll multiple instances of ShardingSphere-Proxy: https://github.com/apache/shardingsphere/issues/20016

The key point of ShardingSphere-Proxy cluster load balancing is that the database protocol itself is designed to be stateful. For example, connection authentication status, transaction status, Prepared Statement, etc.

If the load balancing on top of the ShardingSphere-Proxy is unable to understand the database protocol, the only option is to select a four-tier load balancing proxy ShardingSphere-Proxy cluster. In this case, the state of the database connection between the client and ShardingSphere-Proxy is maintained by a specific Proxy instance.

Because the state of the connection itself is maintained in a specific Proxy instance, four-tier load balancing can only achieve connection-level load balancing. Multiple requests for the same database connection cannot be polled to multiple Proxy instances, i.e. request-level load balancing is not possible.

**Note:** This article does not cover the details of four-tier load balancing and seven-tier load balancing.

***Recommendations for the application layer***

Theoretically, there is no functional difference between a client connecting directly to a single ShardingSphere-Proxy or to a ShardingSphere-Proxy cluster via a load-balancing portal. However, there are some differences in the technical implementation and configuration of the different load balancers.

For example, in the case of a direct connection to ShardingSphere-Proxy with no limit on the maximum time a database connection session can be held, some ELB products have a maximum session hold time of 60 minutes at Layer 4. If an idle database connection is closed by a load balancing timeout, but the client is not aware of the passive TCP connection closure, this may cause the application to report an error.

Therefore, in addition to considerations at the load balancing level, there are also measures that the client itself can consider to avoid the impact of introducing load balancing.

***Consider on-demand connection creation for scenarios with long execution intervals***

For example, if a connection's single instance is created and used continuously, the database connection will be idle most of the time when executing a timed job with a 1-hour interval and a short execution time.

If the client itself is not aware of changes in the connection state, the long idle time increases the uncertainty of the connection state.

For scenarios with long execution intervals, consider creating connections on demand and releasing them after use.

***Consider managing database connections through connection pooling***

General database connection pools have the ability to maintain valid connections, reject failed connections, etc.

Managing database connections through connection pools can reduce the cost of maintaining connections yourself.

***The client considers enabling TCP KeepAlive***

TCP `KeepAlive` configuration is generally supported by clients, e.g:

- MySQL Connector/J supports the configuration of `autoReconnect` or `tcpKeepAlive`, which are not enabled by default.
- The PostgreSQL JDBC Driver supports the configuration of `tcpKeepAlive`, which is not enabled by default.

Nevertheless, there are some limitations to the way TCP `KeepAlive` can be enabled:

- The client does not necessarily support the configuration of TCP `KeepAlive` or automatic reconnection.
- The client does not intend to make any code or configuration adjustments.
- TCP `KeepAlive` is dependent on the operating system implementation and configuration.

# User Case: connection outage due to improper load balancing configuration

A while back, a community user provided feedback that the ShardingSphere-Proxy cluster they deployed was providing services to the public via upper-layer load balancing, and in the process, they found problems with the stability of the connection between their application and ShardingSphere-Proxy.

## Problem Description

For the sake of our case, let's consider that a user's production environment uses a 3-node ShardingSphere-Proxy cluster, which serves applications through a cloud vendor's ELB.

![img](https://shardingsphere.apache.org/blog/img/2023_02_15_A_Distributed_Database_Load_Balancing_Architecture_Based_on_ShardingSphere_Demo_&_User_Case3.png)

One of the applications is a resident process that executes timed jobs, which are executed hourly and have database operations in the job logic. User feedback is that each time a timed job is triggered, an error is reported in the application log:

```markdown
send of 115 bytes failed with errno=104 Connection reset by peer
```

Checking the ShardingSphere-Proxy logs, there are no abnormal messages.

The issue only occurs with timed jobs that execute on an hourly basis, and all other applications access ShardingSphere-Proxy normally.

As the job logic has a retry mechanism, the job executes successfully after each retry, with no impact on the original business.

## Problem Analysis

The reason why the application shows an error is clear: the client is sending data to a closed TCP connection.

Therefore, the troubleshooting goal is to identify the exact reason why the TCP connection was closed.

If you encounter any of the three reasons listed below, we recommend that you perform a network packet capture on both the application and the ShardingSphere-Proxy side within a few minutes before and after the point at which the problem occurs:

- The problem will recur on an hourly basis.
- The issue is network related.
- The issue does not affect the user's real-time operations.

**Packet Capture Phenomenon I**

ShardingSphere-Proxy receives a TCP connection establishment request from the client every 15 seconds, but the client sends an RST to the Proxy immediately after the connection is established with three handshakes.

The client sends an RST to the Proxy without any response after receiving the Server Greeting, or even before the Proxy has sent the Server Greeting.

![img](https://shardingsphere.apache.org/blog/img/2023_02_15_A_Distributed_Database_Load_Balancing_Architecture_Based_on_ShardingSphere_Demo_&_User_Case4.png)

However, no traffic matching the above behavior was found in the application-side packet capture results.

By consulting the documentation of the ELB used by the user, we found that the above network interaction is how the four-layer health check mechanism of that ELB is implemented. Therefore, this phenomenon is not relevant to the problem in this case.

![img](https://shardingsphere.apache.org/blog/img/2023_02_15_A_Distributed_Database_Load_Balancing_Architecture_Based_on_ShardingSphere_Demo_&_User_Case5.png)

**Packet Capture Phenomenon II**

The MySQL connection is established between the client and the ShardingSphere-Proxy and the client sends an RST to the Proxy during the TCP connection disconnection phase.

![img](https://shardingsphere.apache.org/blog/img/2023_02_15_A_Distributed_Database_Load_Balancing_Architecture_Based_on_ShardingSphere_Demo_&_User_Case6.png)

The above packet capture results show that the client first initiated the `COM_QUIT` command to ShardingSphere-Proxy, i.e. the MySQL connection was disconnected by the client, including but not limited to the following possible scenarios:

- The application has finished using the MySQL connection and closed the database connection normally.
- The application's database connection to ShardingSphere-Proxy is managed by a connection pool, which performs a release operation for idle connections that have timed out or have exceeded their maximum lifetime.

As the connection is actively closed on the application side, it does not theoretically affect other business operations, unless there is a problem with the application's logic.

After several rounds of packet analysis, no RSTs were found to have been sent to the client by the ShardingSphere-Proxy in the minutes before and after the problem resurfaced.

Based on the available information, it is possible that the connection between the client and ShardingSphere-Proxy was disconnected earlier, but the packet capture time was limited and did not capture the moment of disconnection.

Since the ShardingSphere-Proxy itself does not have the logic to actively disconnect the client, the problem is being investigated at both the client and ELB levels.

**Client application and ELB configuration check**

Based on user feedback:

- The application's timed jobs are executed hourly, the application does not use a database connection pool, and a database connection is manually maintained and provided for ongoing use by the timed jobs.
- The ELB is configured with four levels of session hold and a session idle timeout of 40 minutes.

Considering the frequency of execution of timed jobs, we recommend that users modify the ELB session idle timeout to be greater than the execution interval of timed jobs. After the user modifies the ELB timeout to 66 minutes, the Connection reset problem no longer occurs.

If packet capturing is continued during troubleshooting, it is highly likely that the ELB will capture traffic that disconnects the TCP connection at the 40th minute of each hour.

## **Problem Conclusion**

The client reported an error Connection reset by peer Root cause:

The ELB idle timeout was less than the timed task execution interval and the client was idle for longer than the ELB session hold timeout, resulting in the connection between the client and ShardingSphere-Proxy being disconnected by the ELB timeout.

The client sends data to a TCP connection that has been closed by the ELB, resulting in the error Connection reset by peer.

***Timeout simulation experiment***

In this paper, we conduct a simple experiment to verify the performance of the client after a load balancing session timeout, and perform packet capture during the experiment to analyse network traffic to observe the behaviour of load balancing.

***Build a load-balanced ShardingSphere-Proxy clustered environment***

Theoretically, any four-tier load balancing implementation can be the subject of this article, so this article uses `nginx` as a four-tier load balancing technology implementation.

***Configure nginx stream***

The idle timeout is set to 1 minute, i.e. the TCP session is held for a maximum of 1 minute.

```sql
user  nginx;
worker_processes  auto;

error_log  /var/log/nginx/error.log notice;
pid        /var/run/nginx.pid;

events {
    worker_connections  1024;
}

stream {
    upstream shardingsphere {
        hash $remote_addr consistent;

        server proxy0:3307;
        server proxy1:3307;
    }

    server {
        listen 3306;
        proxy_timeout 1m;
        proxy_pass shardingsphere;
    }
}
```

***Construct Docker compose***

```yaml
version: "3.9"
services:

  nginx:
    image: nginx:1.22.0
    ports:
      - 3306:3306
    volumes:
      - /path/to/nginx.conf:/etc/nginx/nginx.conf

  proxy0:
    image: apache/shardingsphere-proxy:5.3.0
    hostname: proxy0
    ports:
      - 3307

  proxy1:
    image: apache/shardingsphere-proxy:5.3.0
    hostname: proxy1
    ports:
      - 3307
```

***Startup environment***

```bash
 $ docker compose up -d 
[+] Running 4/4
 ⠿ Network lb_default     Created                                                                                                      0.0s
 ⠿ Container lb-proxy1-1  Started                                                                                                      0.5s
 ⠿ Container lb-proxy0-1  Started                                                                                                      0.6s
 ⠿ Container lb-nginx-1   Started            
```

***Simulation of client-side same-connection based timed tasks***

*Construct client-side deferred SQL execution*

Here the ShardingSphere-Proxy is accessed via Java and MySQL Connector/J.

The logic is roughly as follows:

1. Establish a connection to the ShardingSphere-Proxy and execute a query to the Proxy.
2. Wait 55 seconds and then execute another query to the Proxy.
3. Wait 65 seconds and then execute another query to the Proxy.

```java
public static void main(String[] args) {
    try (Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306?useSSL=false", "root", "root"); Statement statement = connection.createStatement()) {
        log.info(getProxyVersion(statement));
        TimeUnit.SECONDS.sleep(55);
        log.info(getProxyVersion(statement));
        TimeUnit.SECONDS.sleep(65);
        log.info(getProxyVersion(statement));
    } catch (Exception e) {
        log.error(e.getMessage(), e);
    }
}

private static String getProxyVersion(Statement statement) throws SQLException {
    try (ResultSet resultSet = statement.executeQuery("select version()")) {
        if (resultSet.next()) {
            return resultSet.getString(1);
        }
    }
    throw new UnsupportedOperationException();
}
```

***Expected and client-side run results***

Expected results:

1. A client connection to the ShardingSphere-Proxy is established and the first query is successful.
2. The client's second query is successful.
3. The client's third query results in an error due to a broken TCP connection, as the nginx idle timeout is set to 1 minute.

The execution results are as expected. Due to differences between the programming language and the database driver, the error messages behave differently, but the underlying cause is the same: both are TCP connections have been disconnected.

The logs are shown below:

```sql
15:29:12.734 [main] INFO icu.wwj.hello.jdbc.ConnectToLBProxy - 5.7.22-ShardingSphere-Proxy 5.1.1
15:30:07.745 [main] INFO icu.wwj.hello.jdbc.ConnectToLBProxy - 5.7.22-ShardingSphere-Proxy 5.1.1
15:31:12.764 [main] ERROR icu.wwj.hello.jdbc.ConnectToLBProxy - Communications link failure
The last packet successfully received from the server was 65,016 milliseconds ago. The last packet sent successfully to the server was 65,024 milliseconds ago.
        at com.mysql.cj.jdbc.exceptions.SQLError.createCommunicationsException(SQLError.java:174)
        at com.mysql.cj.jdbc.exceptions.SQLExceptionsMapping.translateException(SQLExceptionsMapping.java:64)
        at com.mysql.cj.jdbc.StatementImpl.executeQuery(StatementImpl.java:1201)
        at icu.wwj.hello.jdbc.ConnectToLBProxy.getProxyVersion(ConnectToLBProxy.java:28)
        at icu.wwj.hello.jdbc.ConnectToLBProxy.main(ConnectToLBProxy.java:21)
Caused by: com.mysql.cj.exceptions.CJCommunicationsException: Communications link failure

The last packet successfully received from the server was 65,016 milliseconds ago. The last packet sent successfully to the server was 65,024 milliseconds ago.
        at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
        at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:77)
        at java.base/jdk.internal.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
        at java.base/java.lang.reflect.Constructor.newInstanceWithCaller(Constructor.java:499)
        at java.base/java.lang.reflect.Constructor.newInstance(Constructor.java:480)
        at com.mysql.cj.exceptions.ExceptionFactory.createException(ExceptionFactory.java:61)
        at com.mysql.cj.exceptions.ExceptionFactory.createException(ExceptionFactory.java:105)
        at com.mysql.cj.exceptions.ExceptionFactory.createException(ExceptionFactory.java:151)
        at com.mysql.cj.exceptions.ExceptionFactory.createCommunicationsException(ExceptionFactory.java:167)
        at com.mysql.cj.protocol.a.NativeProtocol.readMessage(NativeProtocol.java:581)
        at com.mysql.cj.protocol.a.NativeProtocol.checkErrorMessage(NativeProtocol.java:761)
        at com.mysql.cj.protocol.a.NativeProtocol.sendCommand(NativeProtocol.java:700)
        at com.mysql.cj.protocol.a.NativeProtocol.sendQueryPacket(NativeProtocol.java:1051)
        at com.mysql.cj.protocol.a.NativeProtocol.sendQueryString(NativeProtocol.java:997)
        at com.mysql.cj.NativeSession.execSQL(NativeSession.java:663)
        at com.mysql.cj.jdbc.StatementImpl.executeQuery(StatementImpl.java:1169)
        ... 2 common frames omitted
Caused by: java.io.EOFException: Can not read response from server. Expected to read 4 bytes, read 0 bytes before connection was unexpectedly lost.
        at com.mysql.cj.protocol.FullReadInputStream.readFully(FullReadInputStream.java:67)
        at com.mysql.cj.protocol.a.SimplePacketReader.readHeaderLocal(SimplePacketReader.java:81)
        at com.mysql.cj.protocol.a.SimplePacketReader.readHeader(SimplePacketReader.java:63)
        at com.mysql.cj.protocol.a.SimplePacketReader.readHeader(SimplePacketReader.java:45)
        at com.mysql.cj.protocol.a.TimeTrackingPacketReader.readHeader(TimeTrackingPacketReader.java:52)
        at com.mysql.cj.protocol.a.TimeTrackingPacketReader.readHeader(TimeTrackingPacketReader.java:41)
        at com.mysql.cj.protocol.a.MultiPacketReader.readHeader(MultiPacketReader.java:54)
        at com.mysql.cj.protocol.a.MultiPacketReader.readHeader(MultiPacketReader.java:44)
        at com.mysql.cj.protocol.a.NativeProtocol.readMessage(NativeProtocol.java:575)
        ... 8 common frames omitted
```

***Analysis of packet capture results***

The packet capture results show that after the connection idle timeout, nginx simultaneously disconnects from the client and the Proxy over TCP. However, as the client is not aware of this, nginx returns an RST after sending the command.

After the `nginx` connection idle timeout, the TCP disconnection process with the Proxy is completed normally, and the Proxy is completely unaware when the client sends subsequent requests using the disconnected connection.

Analyze the following packet capture results:

- Numbers 1–44 are the interaction between the client and the ShardingSphere-Proxy to establish a MySQL connection.
- Numbers 45–50 are the first queries performed by the client.
- Numbers 55–60 are the second query executed 55 seconds after the first query is executed by the client.
- Numbers 73–77 are the TCP connection disconnection processes initiated by nginx to both the client and ShardingSphere-Proxy after the session times out.
- Numbers 78–79 are the third query executed 65 seconds after the client executes the second query, and Connection Reset occurs.

![img](https://shardingsphere.apache.org/blog/img/2023_02_15_A_Distributed_Database_Load_Balancing_Architecture_Based_on_ShardingSphere_Demo_&_User_Case7.png)

# ShardingSphere on Cloud One-Stop Solution

Deploying and maintaining ShardingSphere-Proxy clusters and load balancing manually can be labor intensive and time consuming. To address this issue, Apache ShardingSphere has launched ShardingSphere on Cloud, a collection of cloud-based solutions.

ShardingSphere-on-Cloud includes automated deployment scripts to virtual machines in cloud environments such as AWS, GCP and Alibaba Cloud, such as CloudFormation Stack templates, Terraform one-click deployment scripts, Helm Charts in Kubernetes cloud-native environments, Operator.

ShardingSphere-on-Cloud includes the following tools: Helm Charts, Operator, automatic horizontal scaling, and other tools in a Kubernetes cloud-native environment, as well as a variety of hands-on content on high availability, observability, security compliance, and more.

The new cloud project provides the following capabilities:

- Helm Charts-based ShardingSphere-Proxy for one-click deployment in Kubernetes environments.
- Operator-based ShardingSphere-Proxy for one-click deployment and automated maintenance in Kubernetes environments.
- AWS CloudFormation-based ShardingSphere-Proxy for rapid deployment.
- Terraform-based rapid deployment of ShardingSphere-Proxy in AWS environments.

This post briefly demonstrates one of the fundamental capabilities of ShardingSphere on Cloud: one-click deployment of ShardingSphere-Proxy clusters in Kubernetes using Helm Charts.

1. Use the following 3 line command to create a 3-node ShardingSphere-Proxy cluster within a Kubernetes cluster with the default configuration and serve it through the Service.

```bash
helm repo add shardingsphere https://apache.github.io/shardingsphere-on-cloud
helm repo update
helm install shardingsphere-proxy shardingsphere/apache-shardingsphere-proxy-charts -n shardingsphere
```

![img](https://shardingsphere.apache.org/blog/img/2023_02_15_A_Distributed_Database_Load_Balancing_Architecture_Based_on_ShardingSphere_Demo_&_User_Case8.png)

2. The application can access the ShardingSphere-Proxy cluster via the svc domain.

```bash
kubectl run mysql-client --image=mysql:5.7.36 --image-pull-policy=IfNotPresent -- sleep 300
kubectl exec -i -t mysql-client -- mysql -h shardingsphere-proxy-apache-shardingsphere-proxy.shardingsphere.svc.cluster.local -P3307 -uroot -proot
```

![img](https://shardingsphere.apache.org/blog/img/2023_02_15_A_Distributed_Database_Load_Balancing_Architecture_Based_on_ShardingSphere_Demo_&_User_Case9.png)

This is just a demonstration of one of the basic capabilities of ShardingSphere on Cloud.

For more advanced features that are available in production environments, please refer to the official ShardingSphere-on-Cloud [documentation](https://shardingsphere.apache.org/oncloud/current/cn/overview/).

# Relevant Links

**🔗** [**ShardingSphere Official Website**](https://shardingsphere.apache.org/)

**🔗** [**ShardingSphere Official Project Repo**](https://github.com/apache/shardingsphere)

**🔗** [**ShardingSphere-on-Cloud Official Website**](https://shardingsphere.apache.org/oncloud/)

**🔗**[ **ShardingSphere-on-Cloud Project Repo**](https://github.com/apache/shardingsphere-on-cloud)

**🔗** [**ShardingSphere Twitter**](https://twitter.com/ShardingSphere)

**🔗** [**ShardingSphere Slack**](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)
