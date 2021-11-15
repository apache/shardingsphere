+++
title = "单表"
weight = 7
+++

## 定义

```sql
CREATE DEFAULT SINGLE TABLE RULE singleTableRuleDefinition

ALTER DEFAULT SINGLE TABLE RULE singleTableRuleDefinition

DROP DEFAULT SINGLE TABLE RULE

singleTableRuleDefinition:
    RESOURCE = resourceName
```
- `RESOURCES` 需使用 RDL 管理的数据源资源


## 示例

### Single Table Rule

```sql
CREATE SINGLE TABLE RULE RESOURCE = ds_0

ALTER SINGLE TABLE RULE RESOURCE = ds_1

DROP SINGLE TABLE RULE RESOURCE
```
