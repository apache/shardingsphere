+++
title = "SQL Translator"
weight = 9
+++

## Background

By using SQL translator in the form of Java API, you can easily integrate into various systems and flexibly customize user requirements.

## Parameters

Class: org.apache.shardingsphere.sqltranslator.api.config.SQLTranslatorRuleConfiguration

Attributes:

| *name*                                  | *DataType* | *Description*                                    |
|-----------------------------------------|------------|--------------------------------------------------|
| type                                    | String     | SQL translator type                              |
| useOriginalSQLWhenTranslatingFailed (?) | boolean    | Whether use original SQL when translating failed |

## Procedure

1. Set SQL translator type.
2. Set useOriginalSQLWhenTranslatingFailed to decide whether use original SQL when translating failed.

## Sample

```java
SQLTranslatorRuleConfiguration ruleConfig = new SQLTranslatorRuleConfiguration("Native", new Properties(), false);
String translatedSQL = new SQLTranslatorRule(ruleConfig).translate();
```

## Related References
- [YAML Configuration: SQL Translator](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/sql-translator/)
