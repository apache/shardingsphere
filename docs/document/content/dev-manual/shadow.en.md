+++
pre = "<b>5.8. </b>"
title = "Shadow DB"
weight = 8
chapter = true
+++

## ShadowAlgorithm

### Fully-qualified class name

[`org.apache.shardingsphere.shadow.spi.ShadowAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/shadow/api/src/main/java/org/apache/shardingsphere/shadow/spi/ShadowAlgorithm.java)

### Definition

Shadow algorithm's definition

### Implementation classes

| *Configuration Type* | *Description*                                          | *Fully-qualified class name*                                                                                                                                                                                                                                                            |
|----------------------|--------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| VALUE_MATCH          | Match shadow algorithms based on field values          | [`org.apache.shardingsphere.shadow.algorithm.shadow.column.ColumnValueMatchedShadowAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/shadow/core/src/main/java/org/apache/shardingsphere/shadow/algorithm/shadow/column/ColumnValueMatchedShadowAlgorithm.java) |
| REGEX_MATCH          | Regular matching shadow algorithm based on field value | [`org.apache.shardingsphere.shadow.algorithm.shadow.column.ColumnRegexMatchedShadowAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/shadow/core/src/main/java/org/apache/shardingsphere/shadow/algorithm/shadow/column/ColumnRegexMatchedShadowAlgorithm.java) |
| SQL_HINT             | Shadow algorithm on sql hint                           | [`org.apache.shardingsphere.shadow.algorithm.shadow.hint.SQLHintShadowAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/shadow/core/src/main/java/org/apache/shardingsphere/shadow/algorithm/shadow/hint/SQLHintShadowAlgorithm.java)                           |
