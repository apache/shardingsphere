+++
title = "单表"
weight = 12
+++

## 背景信息

单表规则用于指定哪些单表需要被 ShardingSphere 管理，也可设置默认的单表数据源。

## 参数解释

类名称：org.apache.shardingsphere.single.api.config.SingleRuleConfiguration

可配置属性：

| *名称*                  | *数据类型*               | *说明*    | *默认值* |
|-----------------------|----------------------|---------|-------|
| tables (+)            | Collection\<String\> | 单表规则列表  | -     |
| defaultDataSource (?) | String | 单表默认数据源 | -     |

## 操作步骤

1. 初始化 SingleRuleConfiguration；
2. 添加需要加载的单表，配置默认数据源。

## 配置示例

```java
SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration();
ShardingSphereDataSourceFactory.createDataSource(createDataSourceMap(), Arrays.asList(ruleConfig), new Properties());
```

## 相关参考

- [单表](/cn/features/sharding/concept/#单表)
