+++
pre = "<b>5.13. </b>"
title = "影子库"
weight = 13
chapter = true
+++

## SPI 接口

### 完全限定的类名

[`org.apache.shardingsphere.shadow.spi.ShadowAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-shadow/shardingsphere-shadow-api/src/main/java/org/apache/shardingsphere/shadow/spi/ShadowAlgorithm.java)

| *SPI 名称*       | *详细说明*   |
|---------------- |------------ |
| ShadowAlgorithm | 影子库路由算法 |

## 示例

### ShadowAlgorithm

| *已知实现类*                      | *详细说明*              | *完全限定的类名* |
|-------------------------------- |----------------------- | ------------- |
| ColumnValueMatchShadowAlgorithm | 基于字段值匹配影子算法     | [`org.apache.shardingsphere.shadow.algorithm.shadow.column.ColumnValueMatchShadowAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-shadow/shardingsphere-shadow-core/src/main/java/org/apache/shardingsphere/shadow/algorithm/shadow/column/ColumnValueMatchShadowAlgorithm.java) |
| ColumnRegexMatchShadowAlgorithm | 基于字段值正则匹配影子算法  | [`org.apache.shardingsphere.shadow.algorithm.shadow.column.ColumnRegexMatchShadowAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-shadow/shardingsphere-shadow-core/src/main/java/org/apache/shardingsphere/shadow/algorithm/shadow/column/ColumnRegexMatchShadowAlgorithm.java) |
| SimpleHintShadowAlgorithm    | 基于 Hint 简单匹配影子算法 | [`org.apache.shardingsphere.shadow.algorithm.shadow.hint.SimpleHintShadowAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-shadow/shardingsphere-shadow-core/src/main/java/org/apache/shardingsphere/shadow/algorithm/shadow/hint/SimpleHintShadowAlgorithm.java) |
