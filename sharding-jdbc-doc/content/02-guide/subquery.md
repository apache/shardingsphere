+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "分页及子查询"
weight = 9
prev = "/02-guide/transaction/"
next = "/03-design"

+++

Sharding-JDBC完全支持MySQL、PostgreSQL和Oracle的分页查询，SQLServer由于分页查询较为复杂，仅部分支持。

# 分页性能

## 性能瓶颈

查询偏移量过大的分页会导致数据库获取数据性能低下，以MySQL为例：

```sql
SELECT * FROM t_order ORDER BY id LIMIT 1000000, 10
```

这句SQL会使得MySQL在无法利用索引的情况下跳过1000000条记录后，再获取10条记录，其性能可想而知。而在分库分表的情况下（假设分为2个库），为了保证数据的正确性，SQL会改写为：

```sql
SELECT * FROM t_order ORDER BY id LIMIT 0, 1000010
```

即将偏移量前的记录全部取出，并仅获取排序后的最后10条记录。这会在数据库本身就执行很慢的情况下，进一步加剧性能瓶颈。因为原SQL仅需要传输10条记录至客户端，而改写之后的SQL则会传输1000010*2的记录至客户端。

## Sharding-JDBC的优化

Sharding-JDBC进行了2个方面的优化。

首先，Sharding-JDBC采用流式处理 + 归并排序的方式来避免内存的过量占用。Sharding-JDBC的SQL改写，不可避免的占用了额外的带宽，但并不会导致内存暴涨。
与直觉不同，大多数人认为Sharding-JDBC会将1000010*2记录全部加载至内存，进而占用大量内存而导致内存溢出。
但由于每个结果集的记录是有序的，因此Sharding-JDBC每次比较仅获取各个分片的当前结果集记录，驻留在内存中的记录仅为当前路由到的分片的结果集的当前游标指向而已。
对于本身即有序的待排序对象，归并排序的时间复杂度仅为O(n)，性能损耗很小。

其次，Sharding-JDBC对仅落至单分片的查询进行进一步优化。落至单分片查询的请求并不需要改写SQL也可以保证记录的正确性，因此在此种情况下，Sharding-JDBC并未进行SQL改写，从而达到节省带宽的目的。

## 更好的分页解决方案

由于LIMIT并不能通过索引查询数据，因此如果可以保证ID的连续性，通过ID进行分页是比较好的解决方案：

```sql
SELECT * FROM t_order WHERE id > 100000 AND id <= 100010 ORDER BY id
```

或通过记录上次查询结果的最后一条记录的ID进行下一页的查询：

```sql
SELECT * FROM t_order WHERE id > 100000 LIMIT 10
```

# 分页子查询

Oracle和SQLServer的分页都需要通过子查询来处理，Sharding-JDBC支持分页相关的子查询。

## Oracle

Sharding-JDBC支持使用rownum进行分页：

```sql
SELECT * FROM (SELECT row_.*, rownum rownum_ FROM (SELECT o.order_id as order_id FROM t_order o JOIN t_order_item i ON o.order_id = i.order_id) row_ WHERE rownum <= ?) WHERE rownum > ?
```

目前不支持rownum + BETWEEN的分页方式。

## SQLServer

Sharding-JDBC支持使用TOP + ROW_NUMBER() OVER配合进行分页：

```sql
SELECT * FROM (SELECT TOP (?) ROW_NUMBER() OVER (ORDER BY o.order_id DESC) AS rownum, * FROM t_order o) AS temp WHERE temp.rownum > ? ORDER BY temp.order_id
```

Sharding-JDBC也支持SQLServer 2012之后的OFFSET FETCH的分页方式：

```sql
SELECT * FROM t_order o ORDER BY id OFFSET ? ROW FETCH NEXT ? ROWS ONLY
```

目前不支持使用WITH xxx AS (SELECT ...)的方式进行分页。由于Hibernate自动生成的SQLServer分页语句使用了WITH语句，因此目前并不支持基于Hibernate的SQLServer分页。
目前也不支持使用两个TOP + 子查询的方式实现分页。

## MySQL, PostgreSQL

MySQL和PostgreSQL都支持LIMIT分页，无需子查询：

```sql
SELECT * FROM t_order o ORDER BY id LIMIT ? OFFSET ?
```

# 其他子查询

Sharding-JDBC除了分页子查询的支持之外，也支持同等模式的子查询。无论嵌套多少层，Sharding-JDBC都可以解析至第一个包含数据表的子查询，一旦在下层嵌套中再次找到包含数据表的子查询将直接抛出解析异常。

例如，以下子查询可以支持：

```sql
SELECT COUNT(*) FROM (SELECT * FROM t_order o)
```

以下子查询不支持：

```sql
SELECT COUNT(*) FROM (SELECT * FROM t_order o WHERE o.id IN (SELECT id FROM t_order WHERE status = ?))
```

简单来说，通过子查询进行非功能需求，在大部分情况下是可以支持的。比如分页、统计总数等；而通过子查询实现业务查询当前并不能支持。
