+++
title = "保留字"
weight = 3
+++

## 保留字

---

### 数据源名保留字

```sql
SYS, MYSQL, INFORMATION_SCHEMA, PERFORMANCE_SCHEMA
```

### 标准保留字

```sql
ADD, RESOURCE, HOST, PORT, DB, USER, PASSWORD, PROPERTIES, URL
```

### 补充说明

- 数据源名保留字影响资源命名以及读写分离规则、高可用规则、影子库规则的规则命名
- 标准保留字影响资源和所有规则的变量命名
- 上述保留字大小写不敏感