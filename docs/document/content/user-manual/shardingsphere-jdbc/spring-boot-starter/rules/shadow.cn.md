+++
title = "影子库"
weight = 5
+++

## 配置项说明

```properties
spring.shardingsphere.datasource.names= # 省略数据源配置，请参考使用手册

spring.shardingsphere.rules.shadow.enable= # 影子库开关。 可选值：true/false，默认为false

spring.shardingsphere.rules.shadow.data-sources.shadow-data-source.source-data-source-name= # 生产数据源名称
spring.shardingsphere.rules.shadow.data-sources.shadow-data-source.shadow-data-source-name= # 影子数据源名称

spring.shardingsphere.rules.shadow.tables.<table-name>.data-source-names= # 影子表关联影子数据源名称列表（多个值用","隔开）
spring.shardingsphere.rules.shadow.tables.<table-name>.shadow-algorithm-names= # 影子表关联影子算法名称列表（多个值用","隔开）

spring.shardingsphere.rules.shadow.defaultShadowAlgorithmName= # 默认影子算法名称，选配项。

spring.shardingsphere.rules.shadow.shadow-algorithms.<shadow-algorithm-name>.type= # 影子算法类型
spring.shardingsphere.rules.shadow.shadow-algorithms.<shadow-algorithm-name>.props.xxx= # 影子算法属性配置
```
