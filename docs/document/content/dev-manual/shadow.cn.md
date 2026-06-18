+++
pre = "<b>5.8. </b>"
title = "影子库"
weight = 8
chapter = true
+++

## ShadowAlgorithm

### 全限定类名

[`org.apache.shardingsphere.shadow.spi.ShadowAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/shadow/api/src/main/java/org/apache/shardingsphere/shadow/spi/ShadowAlgorithm.java)

### 定义

影子库提供的影子算法

### 已知实现

| *配置表示*      | *详细说明*            | *完全限定类名*                                                                                                                                                                                                                                                                                |
|-------------|-------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| VALUE_MATCH | 基于字段值匹配影子算法       | [`org.apache.shardingsphere.shadow.algorithm.shadow.column.ColumnValueMatchedShadowAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/shadow/core/src/main/java/org/apache/shardingsphere/shadow/algorithm/shadow/column/ColumnValueMatchedShadowAlgorithm.java) |
| REGEX_MATCH | 基于字段值正则匹配影子算法     | [`org.apache.shardingsphere.shadow.algorithm.shadow.column.ColumnRegexMatchedShadowAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/shadow/core/src/main/java/org/apache/shardingsphere/shadow/algorithm/shadow/column/ColumnRegexMatchedShadowAlgorithm.java) |
| SQL_HINT    | 基于 SQL Hint 的影子算法 | [`org.apache.shardingsphere.shadow.algorithm.shadow.hint.SQLHintShadowAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/shadow/core/src/main/java/org/apache/shardingsphere/shadow/algorithm/shadow/hint/SQLHintShadowAlgorithm.java)                           |
