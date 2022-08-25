+++
title = "ADD RESOURCE"
weight = 2
+++

### 描述

`ADD RESOURCE` 语法用于为当前所选逻辑库（DATABASE）添加资源。

### 语法

```sql
AddResource ::=
  'ADD' 'RESOURCE' resourceDefinition (',' resourceDefinition)*

resourceDefinition ::=
  resourceName '(' ( 'HOST' '=' hostName ',' 'PORT' '=' port ',' 'DB' '=' dbName  |  'URL' '=' url  ) ',' 'USER' '=' user (',' 'PASSWORD' '=' password )?  (',' proerties)?')'

resourceName ::=
  identifier

hostname ::=
  string
    
port ::=
  int

dbName ::=
  string

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

- 添加资源前请确认已经在 Proxy 中创建逻辑数据库，并执行 `use` 命令成功选择一个逻辑数据库；
- 确认添加的资源是可以正常连接的， 否则将不能添加成功；
- `resourceName` 区分大小写；
- `resourceName` 在当前逻辑库中需要唯一；
- `resourceName` 命名只允许使用字母、数字以及 `_` ，且必须以字母开头；
- `poolProperty` 用于自定义连接池参数，`key` 必须和连接池参数名一致，`value` 支持 int 和 String 类型；
- 当 `password` 包含特殊字符时，建议使用 string 形式；例如 `password@123`的 string 形式为 `"password@123"`。

### 示例

- 使用标准模式添加资源

```sql
ADD RESOURCE ds_0 (
    HOST=127.0.0.1,
    PORT=3306,
    DB=db_0,
    USER=root,
    PASSWORD=root
);
```

- 使用标准模式添加资源并设置连接池参数

```sql
ADD RESOURCE ds_1 (
    HOST=127.0.0.1,
    PORT=3306,
    DB=db_1,
    USER=root,
    PASSWORD=root,
    PROPERTIES("maximumPoolSize"=10)
);
```

- 使用 URL 模式添加资源并设置连接池参数

```sql
ADD RESOURCE ds_2 (
    URL="jdbc:mysql://127.0.0.1:3306/db_2?serverTimezone=UTC&useSSL=false",
    USER=root,
    PASSWORD=root,
    PROPERTIES("maximumPoolSize"=10,"idleTimeout"="30000")
);
```

### 保留字

`ADD`、`RESOURCE`、`HOST`、`PORT`、`DB`、`USER`、`PASSWORD`、`PROPERTIES`、`URL`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)