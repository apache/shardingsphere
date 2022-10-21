+++
title = "读写分离"
weight = 2
+++

## 背景信息
读写分离 Spring 命名空间的配置方式，适用于传统的 Spring 项目，通过命名空间 xml 配置文件的方式配置分片规则和属性，由 Spring 完成 ShardingSphereDataSource 对象的创建和管理，避免额外的编码工作。

## 参数解释

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/readwrite-splitting/readwrite-splitting-5.2.1.xsd](http://shardingsphere.apache.org/schema/shardingsphere/readwrite-splitting/readwrite-splitting-5.2.1.xsd)

\<readwrite-splitting:rule />

| *名称*                | *类型* | *说明*           |
| -------------------- | ------ | --------------- |
| id                   | 属性   | Spring Bean Id   |
| data-source-rule (+) | 标签   | 读写分离数据源规则配置 |

\<readwrite-splitting:data-source-rule />

| *名称*                     | *类型* | *说明*                 |
| -------------------------- | ----- | --------------------- |
| id                         | 属性  | 读写分离数据源规则名称    |
| static-strategy            | 标签  | 静态读写分离类型         |
| dynamic-strategy           | 标签  | 动态读写分离类型         |
| load-balance-algorithm-ref | 属性  | 负载均衡算法名称         |

\<readwrite-splitting:static-strategy />

| *名称*                     | *类型* | *说明*                             |
| -------------------------- | ----- | --------------------------------- |
| id                         | 属性  | 静态读写分离名称                     |
| write-data-source-name     | 属性  | 写库数据源名称                       |
| read-data-source-names     | 属性  | 读库数据源列表，多个从数据源用逗号分隔  |
| load-balance-algorithm-ref | 属性  | 负载均衡算法名称                     |

\<readwrite-splitting:dynamic-strategy />

| *名称*                            | *类型* | *说明*                            |
| -------------------------------- | ----- | --------------------------------- |
| id                               | 属性  | 动态读写分离名称                     |
| auto-aware-data-source-name      | 属性  | 数据库发现逻辑数据源名称              |
| write-data-source-query-enabled  | 属性  | 读库全部下线，主库是否承担读流量       |
| load-balance-algorithm-ref       | 属性  | 负载均衡算法名称                     |


\<readwrite-splitting:load-balance-algorithm />

| *名称*     | *类型* | *说明*           |
| --------- | ----- | ---------------- |
| id        | 属性  | 负载均衡算法名称    |
| type      | 属性  | 负载均衡算法类型    |
| props (?) | 标签  | 负载均衡算法属性配置 |

算法类型的详情，请参见[内置负载均衡算法列表](/cn/user-manual/common-config/builtin-algorithm/load-balance)。
查询一致性路由的详情，请参见[核心特性：读写分离](/cn/features/readwrite-splitting/)。

## 操作步骤
1. 添加读写分离数据源
2. 设置负载均衡算法
3. 使用读写分离数据源

## 配置示例
```xml
<readwrite-splitting:load-balance-algorithm id="randomStrategy" type="RANDOM" />
    
<readwrite-splitting:rule id="readWriteSplittingRule">
    <readwrite-splitting:data-source-rule id="demo_ds" load-balance-algorithm-ref="randomStrategy">
        <readwrite-splitting:static-strategy id="staticStrategy" write-data-source-name="demo_write_ds" read-data-source-names="demo_read_ds_0, demo_read_ds_1"/>
    </readwrite-splitting:data-source-rule>
</readwrite-splitting:rule>

<shardingsphere:data-source id="readWriteSplittingDataSource" data-source-names="demo_write_ds, demo_read_ds_0, demo_read_ds_1" rule-refs="readWriteSplittingRule" />
```

## 相关参考
- [核心特性：读写分离](/cn/features/readwrite-splitting/)
- [Java API：读写分离](/cn/user-manual/shardingsphere-jdbc/java-api/rules/readwrite-splitting/)
- [YAML 配置：读写分离](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/readwrite-splitting/)
- [Spring Boot Starter：读写分离](/cn/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/readwrite-splitting/)
