+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "FAQ"
weight = 2
prev = "/01-start/quick-start/"
next = "/01-start/features/"

+++

### 1. 阅读源码时为什么会出现编译错误?

回答：

Sharding-JDBC使用lombok实现极简代码。关于更多使用和安装细节，请参考[lombok官网](https://projectlombok.org/download.html)。

### 2. 1.5.0版本之前出现java.lang.NoSuchMethodError:com.alibaba.druid.sql.ast.expr.SQLAggregateExpr.getOption().....异常的解决方法？

回答：

Sharding-JDBC在1.5.0版本之前使用Druid作为SQL解析的基础库，需确保业务代码中使用的Druid与Sharding-JDBC使用的版本一致，之前使用的Druid是`1.0.12`版本。

### 3. 使用Spring命名空间时在网上相应地址找不到xsd?

回答：

Spring命名空间使用规范并未强制要求将xsd文件部署至公网地址，因此我们并未将`http://www.dangdang.com/schema/ddframe/rdb/rdb.xsd`部署至公网，但不影响正常使用。

sharding-jdbc-config-spring的jar包中`META-INF\spring.schemas`配置了xsd文件的位置：`META-INF\namespace\rdb.xsd`，需确保jar包中该文件存在。

### 4. Cloud not resolve placeholder ... in string value ...异常的解决方法?

回答：

在Spring的配置文件中，由于inline表达式使用了Groovy语法，Groovy语法的变量符与Spring默认占位符同为`${}`，因此需要在配置文件中增加：

```xml
<context:property-placeholder location="classpath:conf/rdb/conf.properties" ignore-unresolvable="true"/>
```

### 5. inline表达式返回结果为何出现浮点数？

回答：

Java的整数相除结果是整数，但是对于inline表达式中的Groovy语法则不同，整数相除结果是浮点数。
想获得除法整数结果需要将A/B改为A.intdiv(B)。


### 6. 使用Proxool时分库结果不正确？

回答：

使用Proxool配置多个数据源时，应该为每个数据源设置alias，因为Proxool在获取连接时会判断连接池中是否包含已存在的alias，不配置alias会造成每次都只从一个数据源中获取连接。

以下是Proxool源码中ProxoolDataSource类getConnection方法的关键代码：

```java
    if(!ConnectionPoolManager.getInstance().isPoolExists(this.alias)) {
        this.registerPool();
    }
```

更多关于alias使用方法请参考[Proxool官网](http://proxool.sourceforge.net/configure.html)。

PS:sourceforge网站需要翻墙访问。

### 7. 使用SQLSever和PostgreSQL时，聚合列不加别名会抛异常？

回答：

SQLServer和PostgreSQL获取不加别名的聚合列会改名。例如，如下SQL：

```sql
SELECT SUM(num), SUM(num2) FROM table_xxx;
```

SQLServer获取到的列为空字符串和(2)，PostgreSQL获取到的列为空sum和sum(2)。这将导致Sharding-JDBC在结果归并时无法找到相应的列而出错。

正确的SQL写法应为：

```sql
SELECT SUM(num) AS sum_num, SUM(num2) AS sum_num2 FROM table_xxx;
```

### 8. 1.5.x之前支持OR，1.5.x之后不再支持，是什么原因？

回答：

1.5.x之前对OR支持并不完善，在复杂场景会有问题。OR的复杂度不仅在于解析，更在于路由。而且非常不适合在分布式数据库中使用，会极大的影响性能。OR是需要将OR和AND的组合拆解成全AND，才可以真正的执行SQL。
举例说明：

```sql
WHERE (a=? OR b=?) AND c=?
```

必须拆解为

```sql
WHERE a=? AND c=?
WHERE b=? AND c=?
```

两条语句才能执行。

再举一个具体的例子：

```sql
WHERE id=1 OR status=‘OK’
```

这样的SQL，如果id是分片键，应该如何处理呢？
首先，需要路由到id=1的库或表，单表获取即可。
其次，因为有OR，需要在所有的数据库和表中全路由，取出所有的status=‘OK’的数据。
最后，将两种数据归并。
因此，SQL必须拆为两条，一条为WHERE id=1，另一条为status=‘OK’，而且他们的分片路由方式截然不同。

如果考虑到很多OR和AND的组合就更加复杂，必须组成一个多维递归的树结构。这种性能对于分布式数据库无法接受，也不可控，因此Sharding-JDBC选择不对OR进行支持。

### 9. 如果SQL在Sharding-JDBC中执行不正确，该如何调试？

回答：

Sharding-JDBC 1.5.0版本之后提供了sql.show的配置，可以将Sharding-JDBC从解析上下文，到改写后的SQL以及最终路由至的数据源的细节信息全部打印至info日志。
sql.show配置默认关闭，如果需要请通过配置开启。详情请参见[配置手册](/02-guide/configuration/)。

### 10. 如果只有部分数据库分库分表，是否需要将不分库分表的表也配置在分片规则中？

回答：

是的。因为Sharding-JDBC是将多个数据源合并为一个统一的逻辑数据源。因此即使不分库分表的部分，不配置分片规则Sharding-JDBC即无法精确的断定应该路由至哪个数据源。
但是Sharding-JDBC提供了两种变通的方式，有助于简化配置。

方法1：配置default-data-source，凡是在默认数据源中的表可以无需配置在分片规则中，Sharding-JDBC将在找不到分片数据源的情况下将表路由至默认数据源。

方法2：将不参与分库分表的数据源独立于Sharding-JDBC之外，在应用中使用多个数据源分别处理分片和不分片的情况。

### 11. Sharding-JDBC提供的默认分布式自增主键策略为什么是不连续的，且尾数大多为偶数？

回答：

Sharding-JDBC采用snowflake算法作为默认的分布式分布式自增主键策略，用于保证分布式的情况下可以无中心化的生成不重复的自增序列。因此自增主键可以保证递增，但无法保证连续。

而snowflake算法的最后4位是在同一毫秒内的访问递增值。因此，如果毫秒内并发度不高，最后4位为零的几率则很大。因此并发度不高的应用生成偶数主键的几率会更高。


### 12. 指定了泛型为Long的SingleKeyTableShardingAlgorithm，遇到ClassCastException: Integer can not cast to Long ？

回答：

必须确保数据库表中该字段和分片算法该字段类型一致，如：数据库中该字段类型为int(11)，泛型所对应的分片类型应为Integer，如果需要配置为Long类型，请确保数据库中该字段类型为bigint。