+++
title = "Single Table"
weight = 2
+++

## Definition

```sql
SET DEFAULT SINGLE TABLE storageUnitDefinition

storageUnitDefinition:
    STORAGE UNIT = storageUnitName | RANDOM
```
- `STORAGE UNIT` needs to use storage unit managed by RDL. The RANDOM keyword stands for random storage.

## Example
```sql
SET DEFAULT SINGLE TABLE STORAGE UNIT = ds_0

SET DEFAULT SINGLE TABLE STORAGE UNIT = RANDOM
```
