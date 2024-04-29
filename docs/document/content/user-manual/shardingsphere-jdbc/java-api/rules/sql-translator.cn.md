+++
title = "SQL 翻译"
weight = 9
+++

## 背景信息

通过 Java API 形式使用 SQL 翻译，可以方便得集成进入各种系统，灵活定制用户需求。

## 参数解释

类名称：org.apache.shardingsphere.sqltranslator.api.config.SQLTranslatorRuleConfiguration

可配置属性：

| *名称*                                    | *数据类型*  | *说明*                    |
|-----------------------------------------|---------|-------------------------|
| type                                    | String  | SQL 翻译器类型               |
| useOriginalSQLWhenTranslatingFailed (?) | boolean | SQL 翻译失败是否使用原始 SQL 继续执行 |

## 操作步骤

1. 配置翻译类型 type
2. 配置 useOriginalSQLWhenTranslatingFailed 参数，是否在 SQL 翻译失败后使用原始 SQL 继续执行

## 配置示例

```java
SQLTranslatorRuleConfiguration ruleConfig = new SQLTranslatorRuleConfiguration("Native", new Properties(), false);
String translatedSQL = new SQLTranslatorRule(ruleConfig).translate();
```

## 相关参考
- [YAML 配置：SQL 翻译](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/sql-translator/)
