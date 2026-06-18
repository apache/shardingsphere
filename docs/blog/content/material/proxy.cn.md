+++
title = "揭秘 Sharding-Proxy——面向DBA的数据库中间层"
weight = 1
chapter = true
+++

### 讲师介绍

**张永伦**：京东金融运维部高级软件工程师

曾在传统行业工作多年，从事基础软件开发工作。后投身互联网，在京东金融开始了爬虫生涯，感叹互联网数据量之大，但心中仍对偏底层的软件感兴趣。今年有幸加入到 Sharding-Sphere，能够做自己感兴趣的事情，希望以后多做些工作，提升自己，回报社区。

大家好，我今天想跟大家分享的是 Sharding-Sphere 的第二个产品 Sharding-Proxy。

在上个月亮相的 Sharding-Sphere 3.0.0.M1 中首次发布了 Sharding-Proxy，希望这次分享能够通过几个优化实践，帮助大家管中窥豹，从几个关键细节想象出 Sharding-Proxy 的全貌。至于更详细的 MySQL 协议、IO 模型、Netty 等议题，以后有机会再和大家专题分享。

### 一、Sharding-Proxy 简介

#### 1. Sharding-Proxy 概览

Sharding-Proxy 定位为透明化的数据库代理端，提供封装了数据库二进制协议的服务端版本，用于完成对异构语言的支持。目前先提供 MySQL 版本，它可以使用任何兼容 MySQL 协议的访问客户端操作数据（如：MySQLCommandClient、MySQLWorkbench 等），对 DBA 更加友好。

- 对应用程序完全透明，可直接当做 MySQL 使用；

- 适用于任何兼容 MySQL 协议的客户端。
    

![](https://shardingsphere.apache.org/blog/img/proxy1.jpg)

与其他两个产品（Sharding-JDBC、Sharding-Sidecar）的对比：

![](https://shardingsphere.apache.org/blog/img/proxy2.jpg)

它们既可以独立使用，也可以相互配合，以不同的架构模型、不同的切入点，实现相同的功能目标。而其核心功能，如数据分片、读写分离、柔性事务等，都是同一套实现代码。

举个例子，对于仅使用 Java 为开发技术栈的场景，Sharding-JDBC 对各种 Java 的 ORM 框架支持度非常高，开发人员可以非常便利地将数据分片能力引入到现有的系统中，并将其部署至线上环境运行，而 DBA 就可以通过部署一个 Sharding-Proxy 实例，对数据进行查询和管理。

#### 2. Sharding-Proxy 架构

![](https://shardingsphere.apache.org/blog/img/proxy3.jpg)

整个架构可以分为前端、后端和核心组件三部分来看：

- 前端（Frontend）负责与客户端进行网络通信，采用的是基于NIO的客户端/服务器框架，在 Windows 和 Mac 操作系统下采用 NIO 模型，Linux 系统自动适配为 Epoll 模型，在通信的过程中完成对 MySQL 协议的编解码；
    
- 核心组件（Core-module）得到解码的 MySQL 命令后，开始调用 Sharding-Core 对 SQL 进行解析、改写、路由、归并等核心功能；
    
- 后端（Backend）与真实数据库的交互暂时借助基于 BIO 的 Hikari 连接池。BIO 的方式在数据库集群规模很大，或者一主多从的情况下，性能会有所下降。所以未来我们还会提供 NIO 的方式连接真实数据库。
    

![](https://shardingsphere.apache.org/blog/img/proxy4.jpg)

这种方式下 Proxy 的吞吐量将得到极大提高，能够有效应对大规模数据库集群。

### 二、Sharding-Proxy 功能细节

#### 1. PreparedStatement 功能实现

我在 Sharding-Sphere 的第一个任务就是实现 Proxy 的 PreparedStatement 功能。据说这是一个高大上的功能，能够预编译 SQL 提高查询速度和防止 SQL 注入攻击等。一次服务端预编译，多次查询，降低 SQL 编译开销，提升了效率，听起来没毛病。然而在做完之后却发现被坑了，SQL 执行效率不但没有提高，甚至用肉眼都能看出来比原始的 Statement 还要慢。

先抛开 Proxy 不说，我们通过 wireshark 抓包看看运行 PreparedStatement 时 MySQL 协议是如何交互的。

示例代码如下：

![](https://shardingsphere.apache.org/blog/img/proxy5.jpg)

代码很容易理解，使用 PreparedStatement 执行两次查询操作，每次都把参数 user_id 设置为 10。分析抓到的包，JDBC 和 MySQL 之间的协议消息交互如下：

![](https://shardingsphere.apache.org/blog/img/proxy6.jpg)

JDBC 向 MySQL 进行了两次查询（Query），MySQL 返回给 JDBC 两次结果（Response），第一条消息不是我们期望的 PreparedStatement，SELECT 里面也没有问号，这就说明 prepare 没有生效，至少对 MySQL 服务来说没有生效。

对于这个问题，我想大家心里都有数，是因为 JDBC 的 url 没有设置参数 useServerPrepStmts=true，这个参数的作用是让 MySQL 服务进行 prepare。没有这个参数就是让 JDBC 进行 prepare，MySQL 完全感知不到，是没有什么意义的。接下来我们在url中加上这个参数：

```
jdbc:mysql://127.0.0.1:3306/demo_ds?useServerPrepStmts=true
```

交互过程变成了这样：

![](https://shardingsphere.apache.org/blog/img/proxy7.jpg)

初看这是一个正确的流程，第 1 条消息是 PreparedStatement，SELECT 里也带问号了，通知 MySQL 对 SQL 进行预编译；第2条消息 MySQL 告诉 JDBC 准备成功；第 3 条消息 JDBC 把参数设置为 10；第 4 条消息 MySQL 返回查询结果。然而到了第5条，JDBC 怎么又发了一遍 PreparedStatement？

预期应该是以后的每条查询都只是通过 ExecuteStatement 传参数的值，这样才能达到一次预编译多次运行的效果。

如果每次都“预编译”，那就相当于没有预编译，而且相对于普通查询，还多了两次消息传递的开销：Response（prepareok）和ExecuteStatement（parameter=10）。看来性能的问题就是出在这里了。

像这样使用 PreparedStatement 还不如不用，一定是哪里搞错了，于是我开始阅读 JDBC 源代码，终于发现了另一个需要设置的参数——cachePrepStmts。我们加上这个参数看看会不会发生奇迹：

```
jdbc:mysql://127.0.0.1:3306/demo_ds?
useServerPrepStmts=true&cachePrepStmts=true
```

果然得到了我们预期的消息流程，而且经过测试，速度也比普通查询快了：

![](https://shardingsphere.apache.org/blog/img/proxy8.jpg)

从第 5 条消息开始，每次查询只传参数值就可以了，终于达到了一次编译多次运行的效果，MySQL 的效率得到了提高。而且由于 ExecuteStatement 只传了参数的值，消息长度上比完整的 SQL 短了很多，网络 IO 的效率也得到了提升。

原来 cachePrepStmts=true 这个参数的意思是告诉 JDBC 缓存需要 prepare 的 SQL，比如 “SELECT * FROM t_order WHERE user_id=?”，运行过一次后，下次再运行就跳过 PreparedStatement，直接用 ExecuteStatement 设置参数值。

明白原理后，就知道该怎么优化 Proxy 了。Proxy 采用的是 Hikari 数据库连接池，在初始化的时候为其设置上面的两个参数：

```
config.addDataSourceProperty("useServerPrepStmts","true");
config.addDataSourceProperty("cachePrepStmts","true");
```

这样就保证了 Proxy 和 MySQL 服务之间的性能。那么 Proxy 和 Client 之间的性能如何保证呢？

Proxy 在收到 Client 的 PreparedStatement 的时候，并不会把这条消息转发给 MySQL，因为 SQL 里的分片键是问号，Proxy 不知道该路由到哪个真实数据库。Proxy 收到这条消息后只是缓存了SQL，存储在一个 StatementId 到 SQL 的 Map 里面，等收到 ExecuteStatement 的时候才真正请求数据库。

这个逻辑在优化前是没问题的，因为每一次查询都是一个新的 PreparedStatement 流程，ExecuteStatement 会把参数类型和参数值告诉客户端。

加上两个参数后，消息内容发生了变化，ExecuteStatement 在发送第二次的时候，消息体里只有参数值而没有参数类型，Proxy 不知道类型就不能正确的取出值。所以 Proxy 需要做的优化就是在 PreparedStatement 开始的时候缓存参数类型。

![](https://shardingsphere.apache.org/blog/img/proxy9.jpg)

完成以上优化后，Client-Proxy 和 Proxy-MySQL 两侧的消息交互都变成了最后这张图的流程，从第 9 步开始高效查询。

#### 2. Hikari 连接池配置优化

Proxy 在初始化的时候，会为每一个真实数据库配置一个 Hikari 连接池。根据分片规则，SQL 被路由到某些真实库，通过 Hikari 连接得到执行结果，最后 Proxy 对结果进行归并返回给客户端。那么，数据库连接池到底该设置多大？对于这个众说纷纭的话题，今天该有一个定论了。

你会惊喜的发现，这个问题不是设置“多大”，反而是应该设置“多小”！如果我说执行一个任务，串行比并行更快，是不是有点反直觉？

即使是单核 CPU 的计算机也能“同时”支持数百个线程。但我们都应该知道这只不过是操作系统用“时间片”玩的一个小花招。事实上，一个 CPU 核心同一时刻只能执行一个线程，然后操作系统切换上下文，CPU 执行另一个线程，如此往复。

一个 CPU 进行计算的基本规律是，顺序执行任务 A 和任务 B 永远比通过时间片“同时”执行 A 和 B 要快。一旦线程的数量超过了 CPU 核心的数量，再增加线程数就只会更慢，而不是更快。一个对 Oracle 的测试验证了这个观点。

参考链接：

http://www.dailymotion.com/video/x2s8uec

测试者把连接池的大小从 2048 逐渐降低到 96，TPS 从 16163 上升到 20702，平响从 110ms 下降到 3ms。

当然，也不是那么简单地让连接数等于 CPU 数就行了，还要考虑网络 IO 和磁盘 IO 的影响。当发生 IO 时，线程被阻塞，此时操作系统可以将那个空闲的 CPU 核心用于服务其他线程。所以，由于线程总是在 I/O 上阻塞，我们可以让线程（连接）数比 CPU 核心多一些，这样能够在同样的时间内完成更多的工作。到底应该多多少呢？PostgreSQL 进行了一个 benchmark 测试：

![](https://shardingsphere.apache.org/blog/img/proxy10.jpg)

TPS 的增长速度从 50 个连接的时候开始变慢。根据这个结果，PostgreSQL 给出了如下公式：

```
connections=((core_count*2)+effective_spindle_count)
``` 

连接数=((核心数\* 2 )+磁盘数)。即使是 32 核的机器，60 多个连接也就够用了。所以，小伙伴们在配置 Proxy 数据源的时候，不要动不动就写上几百个连接，不仅浪费资源，还会拖慢速度。

#### 3. 结果归并优化

目前 Proxy 访问真实数据库使用的是 JDBC，很快 Netty+MySQLProtocol 异步访问方式也会上线，两者会并存，由用户选择用哪种方法访问。

在 Proxy 中使用 JDBC 的 ResultSet 会对内存造成非常大的压力。Proxy 前端对应 m 个 client，后端又对应n个真实数据库，后端把数据传递给前端 client 的过程中，数据都需要经过 Proxy 的内存。如果数据在 Proxy 内存中呆的时间长了，那么内存就可能被打满，造成服务不可用的后果。所以，ResultSet 内存效率可以从两个方向优化，一个是减少数据在 Proxy 中的停留时间，另一个是限流。

我们先看看优化前 Proxy 的内存表现。使用 5 个客户端连接 Proxy，每个客户端查询出 15 万条数据。结果如下图：

![](https://shardingsphere.apache.org/blog/img/proxy11.jpg)


可以看到，Proxy 的内存在一直增长，即时 GC 也回收不掉的。这是因为 ResultSet 会阻塞住 next()，直到查询回来的所有数据都保存到内存中。这是 ResultSet 默认提取数据的方式，大量占用内存。

那么，有没有一种方式，让 ResultSet 收到一条数据就可以立即消费呢？在 Connector/J 文档中有这样一句话：

```
If you are working with ResultSets that have a large number of rows or large values and cannot allocate heap space in your JVM for the memory required, you can tell the driver to stream the results back one row at a time.

如果你使用 ResultSet 遇到查询结果太多，以致堆内存都装不下的情况，你可以指示驱动使用流式结果集，一次返回一条数据。
```

文档参考链接：

https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-reference-implementation-notes.html

激活这个功能只需在创建 Statement 实例的时候设置一个参数：

```
stmt.setFetchSize(Integer.MIN_VALUE);
```

这样就完成了。这样 Proxy 就可以在查询指令后立即通过 next() 消费数据了，数据也可以在下次 GC 的时候被清理掉。当然，Proxy 在对结果做归并的时候，也需要优化成即时归并，而不再是把所有数据都取出来再进行归并，Sharding-Core 提供即时归并的接口，这里就不详细介绍了。下面看看优化后的效果，如下图：

![](https://shardingsphere.apache.org/blog/img/proxy12.jpg)

数据在内存中停留时间缩短，每次 GC 都回收掉了数据，内存效率大幅提升。看到这里，好像已经大功告成了，然而水还很深，请大家穿上潜水服继续跟我探索。图 2 是在最理想的情况产生的，即 Client 从 Proxy 消费数据的速度，大于等于 Proxy 从 MySQL 消费数据的速度。

![](https://shardingsphere.apache.org/blog/img/proxy13.jpg)

如果 Client 由于某种原因消费变慢了，或者干脆不消费了，会发生什么呢？通过测试发现，内存使用量直线拉升，比图 1 更强劲，最后将内存耗尽，Proxy 被 KO。

下面我们就先搞清楚为什么会发生这种现象，然后介绍对 ResultSet 的第 2 个优化：限流。

下图加上了几个主要的缓存：

- SO_RCVBUF/SO_SNDBUF 是 TCP 缓存；

- ChannelOutboundBuffer 是 Netty 写缓存。

![](https://shardingsphere.apache.org/blog/img/proxy14.jpg)

当 Client 阻塞的时候，它的 SO_RCVBUF 会被瞬间打满，然后通过滑动窗口机制通知 Proxy 不要再发送数据了，同时 Proxy 的 SO_SNDBUF 也会瞬间被 Netty 打满。

Proxy 的 SO_SNDBUF 满了之后，Netty 的 ChannelOutboundBuffer 就会像一个无底洞一样，吞掉所有 MySQL 发来的数据，因为在默认情况下 ChannelOutboundBuffer 是无界的。

由于有用户（Netty）在消费，所以 Proxy 的 SO_RCVBUF 一直有空间，导致 MySQL 会一直发送数据，而 Netty 则不停的把数据存到 ChannelOutboundBuffer，直到内存耗尽。

搞清原理之后就知道，我们的目标就是当 Client 阻塞的时候，Proxy 不再接收 MySQL 的数据。

Netty 通过水位参数 WRITE_BUFFER_WATER_MARK 来控制写缓冲区：

- 当 Buffer 大小超过高水位线，我们就控制 Netty 不让再往里面写，当 Buffer 大小低于低水位线的时候，才允许写入；

- 当 ChannelOutboundBuffer 满时，Proxy 的 SO_RCVBUF 被打满，通知 MySQL 停止发送数据。

所以，在这种情况下，Proxy 所消耗的内存只是 ChannelOutboundBuffer 高水位线的大小。

#### 4. Proxy 的两种模式

在即将发布的 Sharding-Sphere 3.0.0.M2 版本中，Proxy 会加入两种代理模式的配置：

- MEMORY_STRICTLY：Proxy 会保持一个数据库中所有被路由到的表的连接，这种方式的好处是利用流式 ResultSet 来节省内存。
    
- CONNECTION_STRICTLY：代理在取出 ResultSet 中的所有数据后会释放连接，同时，内存的消耗将会增加。

简单可以理解为，如果你想消耗更小的内存，就用 MEMORY_STRICTLY 模式；如果你想消耗更少的连接，就用 CONNECTION_STRICTLY 模式。

MEMORY_STRICTLY 的原理其实就是我们前面介绍的内容，优点已经说过了。它带来的一个副作用是，流式 ResultSet 需要保持对数据库的连接，必须与所有路由到的真实表成功建立连接后，才能够进行即时归并，进而返回结果给客户端。

假设数据库设置 max_user_connections=80，而该库被路由到的表是 100 个，那么无论如何也不可能同时建立 100 个连接，也就无法归并返回结果。

CONNECTION_STRICTLY 就是为了解决以上问题而存在的。不使用流式 ResultSet，内存消耗增加。但该模式不需要保持与数据库的连接，每次取出 ResultSet 内的全量数据后即可释放连接。

还是刚才的例子 max_user_connections=80，而该库被路由到的表是 100 个。Proxy 会先建立 80 个连接查询数据，另外 20 个连接请求被缓存在连接池队列中，随着前面查询的完成，这 20 个请求会陆续成功连接数据库。

如果你对这个配置还感到迷惑，那么记住一句话，只有当 max_user_connections 小于该库可能被路由到的最大表数量时，才使用 CONNECTION_STRICTLY。

### 三、小结

Sharding-Sphere 自 2016 年开源以来不断精进和发展，被越来越多企业和个人所使用，同时也为我们提供了重要的成功案例。未来，我们将不断优化当前的特性，陆续推出大家关注的柔性事务、数据治理等更多新特性。如果大家有什么想法、意见和建议，也欢迎留言与我们交流，更欢迎加入到 Sharding-Sphere 的开源项目中：

- https://github.com/sharding-sphere/sharding-sphere/
    
- https://gitee.com/sharding-sphere/sharding-sphere/

### Q&A

Q1：Sidecar 是干什么的？

A1：Sharding-Sidecar 是 Sharding-Sphere 的第三个产品，目前仍在规划中。定位为 Kubernetes 或 Mesos 的云原生数据库代理，以 DaemonSet 的形式代理所有对数据库的访问。

Q2：“问如果你对这个配置还感到迷惑，那么记住一句话，只有当 max_user_connections 小于该库可能被路由到的最大表数量时，才使用 CONNECTION_STRICTLY。”这句话是不是说反了？

A2：CONNECTION_STRICTLY 就是为了省连接的。max_user_connections 小，所以用 CONNECTION_STRICTLY 模式。

Q3：stmt.setFetchSize（custom_size）；设置的场景使用类似 Mybatis 之类的框架查询大量数据到内存，一般就是放到一个 list 中，然后处理。内存还是会占用。

A3：这种情况占的是客户端的内存，不会影响 Proxy。

Q4：问出现查询大数据量到内存的场景，是不是只能使用原生 JDBC，查一批处理一批，不放内存？

A4：Mybatis 是在客户端控制的，不影响 Proxy。

### 直播回放链接

https://m.qlchat.com/topic/details?topicId=2000001395952730&minimal=1

想了解关于 Sharding-Sphere 的更多细节？

**不妨来“2018 DAMS 中国数据资产管理峰会”**

**听听京东金融数据研发负责人张亮老师的解析**

![](https://shardingsphere.apache.org/blog/img/proxy15.jpg)

