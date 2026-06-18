+++
title = "SQL 翻译"
weight = 9
+++

## 背景信息

SQL 翻译 YAML 配置方式具有可读性高，使用简单的特点。通过 YAML 文件的方式，用户可以将代码与配置分离，并且根据需要方便地修改配置文件。

## 参数解释

```yaml
sqlTranslator:
  type: # SQL 翻译器类型
  useOriginalSQLWhenTranslatingFailed: # SQL 翻译失败是否使用原始 SQL 继续执行
```

## 操作步骤

1. 配置翻译类型 type
2. 配置 useOriginalSQLWhenTranslatingFailed 参数，是否在 SQL 翻译失败后使用原始 SQL 继续执行

## 配置示例

```yaml
sqlTranslator:
  type: Native
  useOriginalSQLWhenTranslatingFailed: true
```

## 相关参考
- [JAVA API：SQL 翻译](/cn/user-manual/shardingsphere-jdbc/java-api/rules/sql-translator/)
