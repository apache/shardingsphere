+++
title = "Single Table"
weight = 2
+++

## Definition

```sql
SET DEFAULT SINGLE TABLE STORAGE UNIT = (storageUnitName | RANDOM)
```
- `storageUnitName` needs to use storage unit managed by RDL. The `RANDOM` stands for random storage.

## Example
```sql
SET DEFAULT SINGLE TABLE STORAGE UNIT = ds_0

SET DEFAULT SINGLE TABLE STORAGE UNIT = RANDOM
```
