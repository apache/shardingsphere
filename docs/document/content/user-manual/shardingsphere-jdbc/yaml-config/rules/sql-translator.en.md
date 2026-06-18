+++
title = "SQL Translator"
weight = 9
+++

## Background

The SQL translator YAML configuration is readable and easy to use. The YAML files allow you to separate the code from the configuration, and easily modify the configuration file as needed.

## Parameters

```yaml
sqlTranslator:
  type: # SQL translator type
  useOriginalSQLWhenTranslatingFailed: # Whether use original SQL when translating failed
```

## Procedure
1. Set SQL translator type.
2. Set useOriginalSQLWhenTranslatingFailed to decide whether use original SQL when translating failed.

## Sample

```yaml
sqlTranslator:
  type: Native
  useOriginalSQLWhenTranslatingFailed: true
```

## Related References
- [JAVA API: SQL Translator](/en/user-manual/shardingsphere-jdbc/java-api/rules/sql-translator/)
