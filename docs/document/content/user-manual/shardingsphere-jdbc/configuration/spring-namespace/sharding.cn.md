+++
title = "数据分片"
weight = 1
+++

## 配置项说明

TODO

算法类型的详情，请参见[内置分片算法列表](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/sharding)和[内置分布式序列算法列表](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/keygen)。

## 注意事项

行表达式标识符可以使用 `${...}` 或 `$->{...}`，但前者与 Spring 本身的属性文件占位符冲突，因此在 Spring 环境中使用行表达式标识符建议使用 `$->{...}`。
