+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "分布式主键"
weight = 7
prev = "/02-guide/hint-sharding-value"
next = "/02-guide/transaction"

+++

## 实现动机

传统数据库软件开发中，主键自动生成技术是基本需求。而各大数据库对于该需求也提供了相应的支持，比如MySQL的自增键。
对于MySQL而言，分库分表之后，不同表生成全局唯一的Id是非常棘手的问题。因为同一个逻辑表内的不同实际表之间的自增键是无法互相感知的，
这样会造成重复Id的生成。我们当然可以通过约束表生成键的规则来达到数据的不重复，但是这需要引入额外的运维力量来解决重复性问题，并使框架缺乏扩展性。

目前有许多第三方解决方案可以完美解决这个问题，比如UUID等依靠特定算法自生成不重复键，或者通过引入Id生成服务等。
但也正因为这种多样性导致了Sharding-JDBC如果强依赖于任何一种方案就会限制其自身的发展。

基于以上的原因，最终采用了以JDBC接口来实现对于生成Id的访问，而将底层具体的Id生成实现分离出来。

## 使用方法

使用方法分为设置自动生成键和获取生成键两部分

### 设置自动生成键

配置自增列：

```java
TableRule.builder("t_order").autoIncrementColumns("order_id");
```

设置Id生成器的实现类，该类必须实现com.dangdang.ddframe.rdb.sharding.id.generator.IdGenerator接口。

配置全局生成器(com.x.x.AIdGenerator):

```java
ShardingRule.builder().idGenerator(com.x.x.AIdGenerator.class);

```

有时候我们希望部分表的Id生成器与全局Id生成器不同，比如t_order_item表希望使用com.x.x.BIdGenerator来生成Id:

```java
TableRule.builder("t_order_item").autoIncrementColumns("order_item_id", com.x.x.BIdGenerator.class);
```

这样t_order就使用com.x.x.AIdGenerator生成Id，而t_order_item使用com.x.x.BIdGenerator生成Id。


### 获取自动生成键

通过JDBC提供的API来获取。对于Statement来说调用```statement.execute("INSERT ...", Statement.RETURN_GENERATED_KEYS)```
来通知需要返回的生成的键值。对于PreparedStatement则是```connection.prepareStatement("INSERT ...", Statement.RETURN_GENERATED_KEYS)```

调用```statement.getGeneratedKeys()```来获取键值的ResultSet。

### 其他框架配置

关于Spring，YAML，MyBatis和JPA（Hibernate）的配置请参考
[示例工程](https://github.com/dangdangdotcom/sharding-jdbc/tree/master/sharding-jdbc-example)。

# 通用的分布式主键生成器

需要引入以下依赖

```xml
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>sharding-jdbc-self-id-generator</artifactId>
    <version>${sharding-jdbc.version}</version>
</dependency>
```
类名称：com.dangdang.ddframe.rdb.sharding.id.generator.self.CommonSelfIdGenerator

该生成器作为默认的生成器实现提供，生成的数据为64bit的long型数据。
在数据库中应该用大于等于64bit的数字类型的字段来保存该值，比如在MySQL中应该使用BIGINT。

其二进制表示形式包含四部分，从高位到低位分表为：1bit符号位(为0)，41bit时间位，10bit工作进程位，12bit序列位。

### 时间位(41bit)

从2016年11月1日零点到现在的毫秒数，时间可以使用到2156年，满足大部分系统的要求。

### 工作进程位(10bit)

该标志在Java进程内是唯一的，如果是分布式应用部署应保证每个进程的工作进程Id是不同的。该值默认为0，目前可以通过三种方式设置。

 1. 调用静态方法CommonSelfIdGenerator.setWorkerId("xxxx")设置。
 1. 设置Java的系统变量，也就是再启动命令行中设置-Dsjdbc.self.id.generator.worker.id=xxx设置。
 1. 设置系统环境变量，通过SJDBC_SELF_ID_GENERATOR_WORKER_ID=xxx设置。

### 序列位(12bit)

该序列是用来在同一个毫秒内生成不同的Id。如果在这个毫秒内生成的数量超过4096(2的12次方)，那么生成器会等待到下个毫秒继续生成。

### 总结

从Id的组成部分看，不同进程的Id肯定是不同的，同一个进程首先是通过时间位保证不重复，如果时间相同则是通过序列位保证。
同时由于时间位是单调递增的，且各个服务器如果大体做了时间同步，那么生成的Id在分布式环境可以认为是总体有序的。
这就保证了对索引字段的插入的高效性。例如MySQL的Innodb存储引擎的主键。