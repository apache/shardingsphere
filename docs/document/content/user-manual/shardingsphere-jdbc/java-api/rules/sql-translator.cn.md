+++
title = "SQL 翻译"
weight = 8
+++

## 配置入口

类名称：org.apache.shardingsphere.sqltranslator.api.config.SQLTranslatorRuleConfiguration

可配置属性：

| *名称*                                   | *数据类型* | *说明*                           |
| --------------------------------------- | --------- | ------------------------------- |
| type                                    | String    | SQL 翻译器类型                    |
| useOriginalSQLWhenTranslatingFailed (?) | boolean   | SQL 翻译失败是否使用原始 SQL 继续执行 |
