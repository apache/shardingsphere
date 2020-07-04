+++
title = "影子库"
weight = 4
+++

## 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/shadow/shadow-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/shadow/shadow-5.0.0.xsd)

\<shadow:rule />

| *名称*      | *类型* | *说明*                          |
| ----------- | ----- | ------------------------------- |
| id          | 属性  | Spring Bean Id                  |
| column      | 属性  | 影子字段名称                      |
| mappings(?) | 标签  | 生产数据库与影子数据库的映射关系配置 |

\<shadow:mapping />

| *名称*                   | *类型* | *说明*                          |
| ------------------------ | ----- | ------------------------------- |
| product-data-source-name | 属性  | 生产数据库名称                    |
| shadow-data-source-name  | 属性  | 影子数据库名称                    |

