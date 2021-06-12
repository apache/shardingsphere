+++
title = "数据加密"
weight = 4
+++

## 定义

```sql
SHOW ENCRYPT RULES [FROM schemaName]

SHOW ENCRYPT TABLE RULE tableName [from schemaName]
```
- 支持查询所有的数据加密规则和指定逻辑表名查询

## 说明

| 列             | 说明        |
| -------------- | ---------- |
| table          | 逻辑表名     |
| logicColumn    | 逻辑列名     |
| cipherColumn   | 密文列名     |
| plainColumn    | 明文列名     |
| encryptorType  | 加密算法类型 |
| encryptorProps | 加密算法参数 |
