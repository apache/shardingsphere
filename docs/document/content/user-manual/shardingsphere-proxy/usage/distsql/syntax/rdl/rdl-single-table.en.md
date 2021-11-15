+++
title = "Single Table"
weight = 7
+++

## Definition

```sql
CREATE DEFAULT SINGLE TABLE RULE singleTableRuleDefinition

ALTER DEFAULT SINGLE TABLE RULE singleTableRuleDefinition

DROP DEFAULT SINGLE TABLE RULE

singleTableRuleDefinition:
    RESOURCE = resourceName
```
- `RESOURCES` needs to use data source resources managed by RDL


## Example

### Single Table Rule

```sql
CREATE SINGLE TABLE RULE RESOURCE = ds_0

ALTER SINGLE TABLE RULE RESOURCE = ds_1

DROP SINGLE TABLE RULE RESOURCE
```
