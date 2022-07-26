+++
title = "SQL 翻译"
weight = 8
+++

## 配置项说明

```yaml
rules:
- !SQL_TRANSLATOR
  type: # SQL 翻译器类型
  useOriginalSQLWhenTranslatingFailed: # SQL 翻译失败是否使用原始 SQL 继续执行
```
