+++
title = "Reserved word"
weight = 3
+++

## Reserved Word

---

### Data source name reserved words

```sql
SYS, MYSQL, INFORMATION_SCHEMA, PERFORMANCE_SCHEMA
```

### Standard reserved words

```sql
ADD, RESOURCE, HOST, PORT, DB, USER, PASSWORD, PROPERTIES, URL
```

### Supplement

- Data source name reserved words affect resource naming and rule naming of readwrite splitting rules, database discovery rule, and shadow rule
- Standard reserved words affect resource and all rule variable naming
- The above reserved words are not case-sensitive