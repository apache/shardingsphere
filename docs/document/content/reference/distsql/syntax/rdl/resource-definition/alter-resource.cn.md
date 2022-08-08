+++
title = "ALTER RESOURCE"
weight = 3
+++

### 描述

`ALTER RESOURCE` 语法用于修改当前所选逻辑库（DATABASE）的资源。

### 语法

```sql
AlterResource ::=
  'ALTER' 'RESOURCE' dataSource (',' dataSource)*

dataSource ::=
  dataSourceName '(' ( 'HOST' '=' hostName ',' 'PORT' '=' port ',' 'DB' '=' dbName  |  'URL' '=' url  ) ',' 'USER' '=' user (',' 'PASSWORD' '=' password )?  (',' 'PROPERTIES'  '(' ( key  '=' value ) ( ',' key  '=' value )* ')'  )?')'

dataSourceName ::=
  identifier

hostname ::=
  identifier | ip

dbName ::=
  identifier

port ::=
  int

password ::=
  identifier | int | string 

user ::=
  identifier

url ::=
  identifier | string
```

### 补充说明

- 修改资源前请确认已经在 Proxy 中创建逻辑数据库，并执行 `use` 命令成功选择一个逻辑数据库；
- `ALTER RESOURCE`不允许改变该资源关联的真实数据源；
- `ALTER RESOURCE`会发生连接池的切换，这个操作可能对进行中的业务造成影响，请谨慎使用；
- 确认添加的资源是可以正常连接的， 否则将不能添加成功；
- `dataSourceName` 区分大小写；
- `dataSourceName` 在当前逻辑库中需要唯一；
- `dataSourceName` 命名只允许使用字母、数字以及 `_` ，且必须以字母开头；
- `poolProperty` 用于自定义连接池参数，`key` 必须和连接池参数名一致，`value` 支持 int 和 String 类型；
- 当 `password` 包含特殊字符时，建议使用 string 形式；例如 `password@123`的 string 形式为 `"password@123"`。

### 示例

- 使用标准模式修改资源

```sql
ALTER RESOURCE ds_0 (
    HOST=127.0.0.1,
    PORT=3306,
    DB=db_0,
    USER=root,
    PASSWORD=root
);
```

- 使用标准模式修改资源并设置连接池参数

```sql
ALTER RESOURCE ds_1 (
    HOST=127.0.0.1,
    PORT=3306,
    DB=db_1,
    USER=root,
    PASSWORD=root,
    PROPERTIES("maximumPoolSize"=10)
);
```

- 使用 URL 模式修改资源并设置连接池参数

```sql
ALTER RESOURCE ds_2 (
    URL="jdbc:mysql://127.0.0.1:3306/db_2?serverTimezone=UTC&useSSL=false",
    USER=root,
    PASSWORD=root,
    PROPERTIES("maximumPoolSize"=10,"idleTimeout"="30000")
);
```

### 保留字

`ALTER`、`RESOURCE`、`HOST`、`PORT`、`DB`、`USER`、`PASSWORD`、`PROPERTIES`、`URL`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)