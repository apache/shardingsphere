+++
title = "单表"
weight = 2
+++

## 定义

```sql
SET DEFAULT SINGLE TABLE STORAGE UNIT = (storageUnitName | RANDOM)
```
- `storageUnitName` 需使用 RDL 管理的存储单元。`RANDOM` 代表随机存储。


## 示例
```sql
SET DEFAULT SINGLE TABLE STORAGE UNIT = ds_0

SET DEFAULT SINGLE TABLE STORAGE UNIT = RANDOM
```
