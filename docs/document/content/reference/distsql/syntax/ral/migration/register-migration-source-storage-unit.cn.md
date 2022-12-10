+++
title = "REGISTER MIGRATION SOURCE STORAGE UNIT"
weight = 4
+++

### 描述

`REGISTER MIGRATION SOURCE STORAGE UNIT` 语法用于为当前连接注册数据迁移源存储单元。

### 语法

```sql
RegisterStorageUnit ::=
  'REGISTER' 'MIGRATION' 'SOURCE' 'STORAGE' 'UNIT' storageUnitDefinition (',' storageUnitDefinition)*

storageUnitDefinition ::=
  StorageUnitName '('  'URL' '=' url ',' 'USER' '=' user (',' 'PASSWORD' '=' password )?  (',' proerties)?')'

storageUnitName ::=
  identifier

url ::=
  string

user ::=
  string

password ::=
  string

proerties ::=
  PROPERTIES '(' property ( ',' property )* ')'

property ::=
  key '=' value

key ::=
  string

value ::=
  string
```

### 特别说明

- 确认注册的数据迁移源存储单元是可以正常连接的， 否则将不能注册成功；
- `storageUnitName` 区分大小写；
- `storageUnitName` 在当前连接中需要唯一；
- `storageUnitName` 命名只允许使用字母、数字以及 `_` ，且必须以字母开头；
- `poolProperty` 用于自定义连接池参数，`key` 必须和连接池参数名一致，`value` 支持 int 和 String 类型；
- 当 `password` 包含特殊字符时，建议使用 string 形式；例如 `password@123`的 string 形式为 `"password@123"`；
- 数据迁移源存储单元暂时仅支持使用 `URL` 注册，暂时不支持使用 `HOST` 和 `PORT`。

### 示例

- 注册数据迁移源存储单元

```sql
REGISTER MIGRATION SOURCE STORAGE UNIT su_0 (
    URL="jdbc:mysql://127.0.0.1:3306/migration_su_0?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="123456"
);
```

- 注册数据迁移源存储单元并设置连接池参数

```sql
REGISTER MIGRATION SOURCE STORAGE UNIT su_0 (
    URL="jdbc:mysql://127.0.0.1:3306/migration_su_0?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="123456",
    PROPERTIES("minPoolSize"="1","maxPoolSize"="20","idleTimeout"="60000")
);
```

### 保留字

`REGISTER`、`MIGRATION`、`SOURCE`、`STORAGE`、`UNIT`、`USER`、`PASSWORD`、`PROPERTIES`、`URL`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)