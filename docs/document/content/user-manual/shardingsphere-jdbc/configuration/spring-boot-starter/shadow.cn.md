+++
title = "影子库"
weight = 4
+++

## 配置项说明

```properties
spring.shardingsphere.datasource.names= # 省略数据源配置

spring.shardingsphere.rules.shadow.column= # 影子字段名称名称
spring.shardingsphere.rules.shadow.shadow-mappings.<product-data-source-name>= # 影子数据库名称
```