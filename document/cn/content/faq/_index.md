+++
pre = "<b>5. </b>"
title = "FAQ"
weight = 5
chapter = true
+++

#### 1. 阅读源码时为什么会出现编译错误?

回答：

Sharding-Sphere使用lombok实现极简代码。关于更多使用和安装细节，请参考[lombok官网](https://projectlombok.org/download.html)。

sharding-jdbc-orchestration模块需要先执行`mvn install`命令，根据protobuf文件生成gRPC相关的java文件。

#### 2. Cloud not resolve placeholder ... in string value ...异常的解决方法?

回答：

行表达式标识符可以使用`${...}`或`$->{...}`，但前者与Spring本身的属性文件占位符冲突，因此在Spring环境中使用行表达式标识符建议使用`$->{...}`。

#### 3. inline表达式返回结果为何出现浮点数？

回答：

Java的整数相除结果是整数，但是对于inline表达式中的Groovy语法则不同，整数相除结果是浮点数。
想获得除法整数结果需要将A/B改为A.intdiv(B)。

#### 4. 使用Proxool时分库结果不正确？

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

#### 5. 使用SQLSever和PostgreSQL时，聚合列不加别名会抛异常？

回答：

SQLServer和PostgreSQL获取不加别名的聚合列会改名。例如，如下SQL：

```sql
SELECT SUM(num), SUM(num2) FROM table_xxx;
```

SQLServer获取到的列为空字符串和(2)，PostgreSQL获取到的列为空sum和sum(2)。这将导致Sharding-Sphere在结果归并时无法找到相应的列而出错。

正确的SQL写法应为：

```sql
SELECT SUM(num) AS sum_num, SUM(num2) AS sum_num2 FROM table_xxx;
```

#### 6. 如果SQL在Sharding-Sphere中执行不正确，该如何调试？

回答：

在Sharding-Proxy以及Sharding-JDBC 1.5.0版本之后提供了sql.show的配置，可以将解析上下文和改写后的SQL以及最终路由至的数据源的细节信息全部打印至info日志。
sql.show配置默认关闭，如果需要请通过配置开启。

#### 7. 如果只有部分数据库分库分表，是否需要将不分库分表的表也配置在分片规则中？

回答：

是的。因为Sharding-Sphere是将多个数据源合并为一个统一的逻辑数据源。因此即使不分库分表的部分，不配置分片规则Sharding-Sphere即无法精确的断定应该路由至哪个数据源。
但是Sharding-Sphere提供了两种变通的方式，有助于简化配置。

方法1：配置default-data-source，凡是在默认数据源中的表可以无需配置在分片规则中，Sharding-Sphere将在找不到分片数据源的情况下将表路由至默认数据源。

方法2：将不参与分库分表的数据源独立于Sharding-Sphere之外，在应用中使用多个数据源分别处理分片和不分片的情况。

#### 8. Sharding-Sphere提供的默认分布式自增主键策略为什么是不连续的，且尾数大多为偶数？

回答：

Sharding-Sphere采用snowflake算法作为默认的分布式分布式自增主键策略，用于保证分布式的情况下可以无中心化的生成不重复的自增序列。因此自增主键可以保证递增，但无法保证连续。

而snowflake算法的最后4位是在同一毫秒内的访问递增值。因此，如果毫秒内并发度不高，最后4位为零的几率则很大。因此并发度不高的应用生成偶数主键的几率会更高。


#### 9. 指定了泛型为Long的SingleKeyTableShardingAlgorithm，遇到ClassCastException: Integer can not cast to Long?

回答：

必须确保数据库表中该字段和分片算法该字段类型一致，如：数据库中该字段类型为int(11)，泛型所对应的分片类型应为Integer，如果需要配置为Long类型，请确保数据库中该字段类型为bigint。

#### 10. Sharding-JDBC除了支持自带的分布式自增主键之外，还能否支持原生的自增主键？

回答：是的，可以支持。但原生自增主键有使用限制，即不能将原生自增主键同时作为分片键使用。

由于Sharding-JDBC并不知晓数据库的表结构，而原生自增主键是不包含在原始SQL中内的，因此Sharding-JDBC无法将该字段解析为分片字段。如自增主键非分片键，则无需关注，可正常返回；若自增主键同时作为分片键使用，Sharding-JDBC无法解析其分片值，导致SQL路由至多张表，从而影响应用的正确性。

而原生自增主键返回的前提条件是INSERT SQL必须最终路由至一张表，因此，面对返回多表的INSERT SQL，自增主键则会返回零。

#### 11. Oracle数据库使用Timestamp类型的Order By语句抛出异常提示“Order by value must implements Comparable”?

回答：

针对上面问题解决方式有两种：
1.配置启动JVM参数“-oracle.jdbc.J2EE13Compliant=true”
2.通过代码在项目初始化时设置System.getProperties().setProperty("oracle.jdbc.J2EE13Compliant", "true");

原因如下:

com.dangdang.ddframe.rdb.sharding.merger.orderby.OrderByValue#getOrderValues()方法如下:

```java
    private List<Comparable<?>> getOrderValues() throws SQLException {
        List<Comparable<?>> result = new ArrayList<>(orderByItems.size());
        for (OrderItem each : orderByItems) {
            Object value = resultSet.getObject(each.getIndex());
            Preconditions.checkState(null == value || value instanceof Comparable, "Order by value must implements Comparable");
            result.add((Comparable<?>) value);
        }
        return result;
    }
```

使用了resultSet.getObject(int index)方法，针对TimeStamp oracle会根据oracle.jdbc.J2EE13Compliant属性判断返回java.sql.TimeStamp还是自定义oralce.sql.TIMESTAMP
详见ojdbc源码oracle.jdbc.driver.TimestampAccessor#getObject(int var1)方法：

```java
    Object getObject(int var1) throws SQLException {
        Object var2 = null;
        if(this.rowSpaceIndicator == null) {
            DatabaseError.throwSqlException(21);
        }

        if(this.rowSpaceIndicator[this.indicatorIndex + var1] != -1) {
            if(this.externalType != 0) {
                switch(this.externalType) {
                case 93:
                    return this.getTimestamp(var1);
                default:
                    DatabaseError.throwSqlException(4);
                    return null;
                }
            }

            if(this.statement.connection.j2ee13Compliant) {
                var2 = this.getTimestamp(var1);
            } else {
                var2 = this.getTIMESTAMP(var1);
            }
        }

        return var2;
    }
```

#### 12. 使用Spring命名空间时找不到xsd?

回答：

Spring命名空间使用规范并未强制要求将xsd文件部署至公网地址，但考虑到部分用户的需求，我们也将相关xsd文件部署至Sharding-Sphere官网。

实际上sharding-jdbc-core-config-spring的jar包中META-INF\spring.schemas配置了xsd文件的位置：META-INF\namespace\sharding.xsd和META-INF\namespace\master-slave.xsd，只需确保jar包中该文件存在即可。
