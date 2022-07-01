+++
title = "分布式事务"
weight = 3
+++

## 配置入口

org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration

可配置属性：

| *名称*            | *数据类型*  | *说明*        |
| ---------------- | ---------- | ------------ |
| defaultType      | String     | 默认事务类型   |
| providerType (?) | String     | 事务提供者类型 |
| props (?)        | Properties | 事务属性配置   |
