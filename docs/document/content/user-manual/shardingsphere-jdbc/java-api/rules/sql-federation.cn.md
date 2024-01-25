+++
title = "联邦查询"
weight = 13
+++

## 背景信息

该功能为**实验性功能，暂不适合核心系统生产环境使用**。
当关联查询中的多个表分布在不同的数据库实例上时，通过开启联邦查询可以进行跨库关联查询，以及子查询。

## 参数解释

类名称：org.apache.shardingsphere.sqlfederation.api.config.SQLFederationRuleConfiguration

可配置属性：

| *名称*                  | *数据类型*  | *说明*              | *默认值* |
|-----------------------|---------|-------------------|-------|
| sqlFederationEnabled            | boolean | 是否开启联邦查询          | -     |
| allQueryUseSQLFederation | boolean | 是否全部查询 SQL 使用联邦查询 | -     |
| executionPlanCache | org.apache.shardingsphere.sql.parser.api.CacheOption | 执行计划缓存            | -     |

## 本地缓存配置

类名称：org.apache.shardingsphere.sql.parser.api.CacheOption

可配置属性：

| *名称*            | *数据类型* | *说明*       | *默认值*               |
|-----------------|--------|------------|---------------------|
| initialCapacity | int    | 执行计划缓存初始容量 | 执行计划本地缓存初始默认值 2000  |
| maximumSize     | long   | 执行计划缓存最大容量 | 执行计划本地缓存最大默认值 65535 |

## 配置示例

```java
private SQLFederationRuleConfiguration createSQLFederationRuleConfiguration() {
    CacheOption executionPlanCache = new CacheOption(2000, 65535L);
    return new SQLFederationRuleConfiguration(true, false, executionPlanCache);
}
```

## 相关参考

- [YAML 配置：联邦查询](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/sql-federation/)
