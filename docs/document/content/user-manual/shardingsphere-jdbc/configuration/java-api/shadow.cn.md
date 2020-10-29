+++
title = "影子库"
weight = 4
+++

## 配置入口

类名称：org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration

可配置属性：

| *名称*         | *数据类型*              | *说明*                                                            |
| -------------- | --------------------- | ----------------------------------------------------------------- |
| column         | String                | SQL 中的影子字段名，该值为 true 的 SQL 会路由到影子库执行               |
| sourceDataSourceNames | List\<String\> | 生产数据库名称 |
| shadowDataSourceNames | List\<String\> | 影子数据库名称，与上面一一对应 |