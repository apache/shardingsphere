+++
title = "REGISTER STORAGE UNIT"
weight = 1
+++

### 描述

`REGISTER STORAGE UNIT` 语法用于为当前所选逻辑库（DATABASE）注册存储单元。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
RegisterStorageUnit ::=
  'REGISTER' 'STORAGE' 'UNIT' ifNotExists? storageUnitDefinition (',' storageUnitDefinition)*

storageUnitDefinition ::=
  storageUnitName '(' ('HOST' '=' hostName ',' 'PORT' '=' port ',' 'DB' '=' dbName | 'URL' '=' url) ',' 'USER' '=' user (',' 'PASSWORD' '=' password)? (',' propertiesDefinition)?')'

ifNotExists ::=
  'IF' 'NOT' 'EXISTS'

storageUnitName ::=
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

propertiesDefinition ::=
  'PROPERTIES' '(' key '=' value (',' key '=' value)* ')'

key ::=
  string

value ::=
  literal
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 特别说明

- 注册存储单元前请确认已经在 Proxy 中创建逻辑数据库，并执行 `use` 命令成功选择一个逻辑数据库；
- 确认注册的存储单元是可以正常连接的， 否则将不能注册成功；
- `storageUnitName` 区分大小写；
- `storageUnitName` 在当前逻辑库中需要唯一；
- `storageUnitName` 命名只允许使用字母、数字以及 `_` ，且必须以字母开头；
- `poolProperty` 用于自定义连接池参数，`key` 必须和连接池参数名一致；
- `ifNotExists` 子句用于避免出现 `Duplicate storage unit` 的错误。

### 示例

- 使用标准模式注册存储单元

```sql
REGISTER STORAGE UNIT ds_0 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db_0",
    USER="root",
    PASSWORD="root"
);
```

- 使用标准模式注册存储单元并设置连接池参数

```sql
REGISTER STORAGE UNIT ds_0 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db_1",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"=10)
);
```

- 使用 URL 模式注册存储单元并设置连接池参数

```sql
REGISTER STORAGE UNIT ds_0 (
    URL="jdbc:mysql://127.0.0.1:3306/db_2?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"=10,"idleTimeout"="30000")
);
```

- 使用 `ifNotExists` 子句注册存储单元

```sql
REGISTER STORAGE UNIT IF NOT EXISTS ds_0 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db_0",
    USER="root",
    PASSWORD="root"
);
```

### 保留字

`REGISTER`、`STORAGE`、`UNIT`、`HOST`、`PORT`、`DB`、`USER`、`PASSWORD`、`PROPERTIES`、`URL`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)