+++
toc = true
title = "Merger Engine"
weight = 5
+++

Result merger refers to merging multi-data result set acquired from all the data nodes as one result set and returning it to the request end rightly.

In function, the result merger supported by ShardingSphere can be divided into iteration, order-by, group-by, pagination and aggregation these five kinds, which are in composition relation rather than clash relation. 
In structure, it can be divided into stream merger, memory merger and decorator merger, in which stream merger and the memory merger clash with each other, and the decorator merger can be further processed based on stream merger and memory merger.

Able to reduce the memory consumption to a large extend, the most prior choice of merger method is to follow the database returned result set, since the result set is returned from database line by line, instead of loading all the data to the memory at one time.

Stream merger means, each time, the data acquired from the result set is able to return the single piece of right data line by line. 
It is the most suitable one for the original result set return method of the database. 
Iteration, order-by, and stream group-by all belong to the range of stream merger.

Memory merger needs to iterate all the data in the result set and store it in the memory first; after unified grouping, ordering, aggregation and other computations, then pack it into data result set which is visited line by line and return that.

Decorator merger merges and reinforces all the result sets function uniformly. Currently, decorator merger has pagination merger and aggregation merger these two kinds.

## Iteration Merger

As the simplest merger method, iteration merger requires only the combination of multiple data result sets into a sing direction chain table. 
After iterating current data result sets in the chain table, it only needs to move the element of chain table to the next position and iterate the next data result set.

## Order-by Merger

For the existence of `ORDER BY` sentence in SQL, each data result has its own order, so ordering the data value currently pointed by cursors in the result set is enough. 
It is equal to sequencing multiple ordered arrays, and therefore, order-by merger is the most suitable ordering algorithm in this situation.

When merging order inquiries, ShardingSphere will compare current data values of each result set, which is realized by Comparable interface of Java, and put them into the priority queue. 
Each time when acquiring the next piece of data, it only needs to move down the result set in the top end of the line, renter the priority order according to the new cursor and relocate its own position.

Here is an instance to explain the order-by merger of ShardingSphere. 
The following is an example diagram of ordering by the score. 
Data result sets returned by 3 diagrams are shown in the example, and each one of them has already been ordered according to the score, but there is no order between 3 data result sets. 
When ordering the data value currently pointed by cursors in these 3 result sets and putting them into the priority queue, the data value of t_score_0 is the biggest, followed by that of t_score_2 and t_score_1 in sequence. 
Thus, the priority queue is ordered by the sequence of t_score_0, t_score_2 and t_score_1.

![Order by merger examole 1](http://shardingsphere.apache.org/document/current/img/sharding/order_by_merge_1.png)

This diagram illustrates how the order-by merger works when using next invocation. 
We can see from the diagram that when using next invocation, t_score_0 at the first of the queue will be popped out. After returning the data value currently pointed by the cursor, i.e., 100, to the client end, the cursor will be moved down and t_score_0 will be put back to the queue. 
While the priority queue will also be ordered according to the t_score_0 data value (90 here) pointed by the cursor of current data result set. 
According to the current value, t_score_0 is at the last of the queue, and in the second place of the queue formerly, the data result set of t_score_2, automatically moves to the first place of the queue.

In the second use of next, t_score_2 in the first position is popped out of the queue. 
Its value pointed by the cursor of the data result set is returned to the client end, with its cursor moved down to rejoin the queue, and the following will be in the same way. 
If there is no data in the result set, it will not rejoin the queue.

![Order by merger examole 2](http://shardingsphere.apache.org/document/current/img/sharding/order_by_merge_2.png)

It can be seen that, under the circumstance that single data result set is ordered while multiple data result set is disordered, ShardingSphere does not need to upload all the data to the memory to order. 
By the order-by merger method, each next only acquires one right piece of data each time, so the memory consumption can be saved to a large extent.

On the other hand, the order-by merger of ShardingSphere has its order on horizontal axis and vertical axis that maintains the data result set. 
Naturally ordered, vertical axis refers to each data result set itself, which is acquired by SQL that contains `ORDER BY`. 
Horizontal axis refers to the current value pointed by each data result set, and its order needs to be maintained by the priority queue. 
Each moving down of the current cursor of data result set requires to put it in the priority order again, which means the operation of cursor moving down can only happen when the data result set is in the first place of the queue.

## Group-by Merger

With the most complicated situation, group-by merger can be divided into stream group-by merger and memory group-by merger. 
Stream group-by merger requires the field and order type (ASC or DESC) of SQL order item and group-by item to be consistent. 
Otherwise, its data accuracy can only be maintained by memory merger.

For instance, suppose the sheet structure is divided according to the subject, and each sheet contains each examinee’s name (to simplify, the same name is not taken into consideration) and score. 
The SQL used to acquire each examinee’s total score is as follow:

```sql
SELECT name, SUM(score) FROM t_score GROUP BY name ORDER BY name;
```

When order-by item and group-by item are totally consistent, the data obtained are continuous. 
For the data needed to group are all stored in the data value currently pointed by cursors of each data result set, stream group-by merger can be used, as illustrated by the diagram:

![Group by merger examole 1](http://shardingsphere.apache.org/document/current/img/sharding/group_by_merge_1_v3.png)

The merging logic will be similar as that of order-by merger. The following picture shows how stream group-by merger works when using next invocation.

![Group by merger examole 2](http://shardingsphere.apache.org/document/current/img/sharding/group_by_merge_2_v2.png)

We can see from the picture, in the first next invocation, t_score_java in the first position, along with the data having the grouping value of “Jetty” in other result sets, will be popped out of the queue. 
After acquiring all the students’ scores with the name of “Jetty”, the accumulation operation will be proceeded. 
Hence, after the first next invocation is finished, the result set acquired is the sum of Jetty’s scores. 
In the same time, all the cursors in data result sets will be moved down to the next different data value of “Jetty”, and rearranged according to the value currently pointed by cursors of the result set. 
Thus, the data that contains the second-place name “John” will be put at the beginning of the queue.

Stream group-by merger is different from order-by merger only in two points:

1. It will take out all the data with the same grouping item from multiple data result sets once.
2. It does the aggregation calculation according to the type of aggregation calculation function.

For the dis-conformation between the grouping item and the order item, it requires to upload all the result set data to the memory to group and aggregate, since the relevant data value needed to acquire grouping information is not continuous, and stream merger is not able to use. 
For example, if acquiring each examinee’s total score through the following SQL and ordering them from the highest to the lowest:

```sql
SELECT name, SUM(score) FROM t_score GROUP BY name ORDER BY score DESC;
```

Then, stream merger is not able to use, for the data taken out from each data result set is the same as the original data of the diagram ordered by score in the upper half part structure.

When there is only SQL contained in the group-by sentence, according to the realization of different databases, its order may not be the same as the grouping order. 
The lack of ordering sentence indicates that the order is not emphasized in this SQL. 
Therefore, through the adaption of SQL optimization, ShardingSphere can automatically add the ordering item consistent with the grouping item, converting it from the memory group-by merger method that consumes the memory to the stream group-by merger scheme.

## Aggregation Merger

No matter stream group-by merger or memory group-by merger, they process the aggregation function in a consistent way. 
Therefore, aggregation merger is an additional merging ability based on what have been introduced above, i.e., comparison, sum and average these three kinds.

Comparison aggregation function refers to `MAX` and `MIN`. They need to compare all the result set data and return its maximum or minimum value directly.

Sum aggregation function refers to `SUM` and `COUNT`. They need to sum up all the result set data.

Average aggregation function refers only to `AVG`. 
It must be calculated through `SUM` and `COUNT` of SQL adaption, which has been covered above in the content of SQL adaption, so we will state no more here.

## Pagination Merger

All the merger types stated above can be paginated. 
Pagination is the decorator added on other kinds of mergers, through the decorator model of which, ShardingSphere augments its ability to paginate the data result set. 
Pagination merger is responsible for filtering the data unnecessary to acquire.

The pagination function of ShardingSphere can be misleading to users in that they may think it will take a large amount of memory. 
In distributed situations, it can only guarantee the data accuracy by readapting` LIMIT 10000000, 10` to `LIMIT 0, 10000010`. 
Users can easily bear the misconception that ShardingSphere uploads a large amount of meaningless data to the memory and causes the risk of memory overflow. 
Actually, it can be known from the principle of stream merger, only the case of memory group-by merger will upload all the data to the memory. 
Generally speaking, however, SQL used for OLAP grouping, is applied more frequently to massive calculation or small result generation situation, rather than generates vast result data. 
Except for the case of memory group-by merger, stream merger is the method to acquire data result set in other cases: 
ShardingSphere would skip the data unnecessary to take by the method of next in result set, rather than storing them in the memory.

What’s to be noticed, pagination with `LIMIT` is not the best practice actually, because a large amount of data still needs to be transmitted to the memory space of ShardingSphere for the sake of ordering. 
`LIMIT` cannot search for data by index, so paginating with ID is a better solution on the premise that the ID continuity can be guaranteed. 
For example:

```sql
SELECT * FROM t_order WHERE id > 100000 AND id <= 100010 ORDER BY id;
```

Or search the next page through the ID of the last query result record, for example:

```sql
SELECT * FROM t_order WHERE id > 10000000 LIMIT 10;
```

The overall structure division of merger engine is shown in the following diagram:

![Merge Architecture](http://shardingsphere.apache.org/document/current/img/sharding/merge_architecture_en.png)
