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

### 2. java.lang.NoSuchMethodError:com.alibaba.druid.sql.ast.expr.SQLAggregateExpr.getOption().....异常的解决方法？

回答：

目前Sharding-JDBC使用Druid作为SQL解析的基础库，请确保业务代码中使用的Druid与Sharding-JDBC使用的版本一致，目前Sharding-JDBC使用的是`1.0.12`版本。

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
