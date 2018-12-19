+++
toc = true
title = "分布式事务"
weight = 5
+++

## 引入Maven依赖

```xml
<dependency>
    <groupId>io.shardingsphere</groupId>
    <artifactId>sharding-transaction-2pc-xa</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

XA事务管理器将以SPI的方式被Sharding-JDBC所加载。

## 连接池配置

ShardingSphere支持将普通的数据库连接池，转换为支持XA事务的连接池，对HikariCP, Druid和DBCP2连接池内置支持，无需额外配置。
其它连接池需要用户实现`DataSourceMapConverter`的SPI接口进行扩展，可以参考`io.shardingsphere.transaction.xa.convert.swap.HikariParameterSwapper`的实现。
若ShardingSphere无法找到合适的实现，则会按默认的配置创建XA事务连接池。默认属性如下：

| *属性名称*                         | *默认值*   |
| -----------------------------------| ----------|
| connectionTimeoutMilliseconds      | 30 * 1000 |
| idleTimeoutMilliseconds            | 60 * 1000 |
| maintenanceIntervalMilliseconds    | 30 * 1000 |
| maxLifetimeMilliseconds            | 0 (无限制)|
| maxPoolSize                        | 50        |
| minPoolSize                        | 1         |

## 事务类型切换

ShardingSphere的事务类型存放在`TransactionTypeHolder`的本地线程变量中，因此在数据库连接创建前修改此值，可以达到自由切换事务类型的效果。

注意：数据库连接创建之后，事务类型将无法更改。

### API方式

```java
TransactionTypeHolder.set(TransactionType.LOCAL);
```

或

```java
TransactionTypeHolder.set(TransactionType.XA);
```

### Spring注解方式

引入Maven依赖：

```xml
<dependency>
    <groupId>io.shardingsphere</groupId>
    <artifactId>sharding-transaction-spring</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

然后在需要事务的方法或类中添加相关注解即可，例如：

```java
@ShardingTransactionType(TransactionType.LOCAL)
@Transactional
```

或

```java
@ShardingTransactionType(TransactionType.XA)
@Transactionnal
```

注意：`@ShardingTransactionType`需要同Spring的`@Transactional`配套使用，事务才会生效。

## Atomikos参数配置

ShardingSphere默认的XA事务管理器为Atomikos。
可以通过在项目的classpath中添加`jta.properties`来定制化Atomikos配置项。
具体的配置规则请参考Atomikos的[官方文档](https://www.atomikos.com/Documentation/JtaProperties)。
