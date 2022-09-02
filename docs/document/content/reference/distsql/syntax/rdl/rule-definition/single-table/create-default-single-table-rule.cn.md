+++
title = "CREATE DEFAULT SINGLE TABLE RULE"
weight = 2
+++

## 描述

`CREATE DEFAULT SINGLE TABLE RULE` 语法用于创建默认的单表规则

### 语法定义

```sql
CreateDefaultSingleTableRule ::=
  'CREATE' 'DEFAULT' 'SINGLE' 'TABLE' 'RULE' singleTableDefinition

singleTableDefinition ::=
  'RESOURCE' '=' resourceName

resourceName ::=
  identifier
```

### 补充说明

- `RESOURCE` 需使用 RDL 管理的数据源资源。

### 示例

#### 创建默认单表规则

```sql
CREATE DEFAULT SINGLE TABLE RULE RESOURCE = ds_0;
```

### 保留字

`CREATE`、`SHARDING`、`SINGLE`、`TABLE`、`RULE`、`RESOURCE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)