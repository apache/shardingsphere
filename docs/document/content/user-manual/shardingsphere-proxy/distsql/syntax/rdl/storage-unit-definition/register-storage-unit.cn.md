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
  'REGISTER' 'STORAGE' 'UNIT' ifNotExists? storageUnitsDefinition (',' checkPrivileges)?

storageUnitsDefinition ::=
  storageUnitDefinition (',' storageUnitDefinition)*

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

checkPrivileges ::=
  'CHECK_PRIVILEGES' '=' privilegeType (',' privilegeType)*

privilegeType ::=
  identifier
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
- `PROPERTIES` 为可选参数，用于自定义连接池属性，`key` 必须和连接池参数名一致；
- 可通过 `CHECK_PRIVILEGES` 指定注册时校验存储单元用户的权限，`privilegeType` 支持的类型有 `SELECT`、`XA`、`PIPELINE`、`NONE`，缺省值为 `SELECT`，当类型列表中包含 `NONE` 时，跳过权限校验。

### 示例

- 使用 HOST & PORT 方式注册存储单元

```sql
REGISTER STORAGE UNIT ds_0 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db_0",
    USER="root",
    PASSWORD="root"
);
```

- 使用 HOST & PORT 方式注册存储单元并设置连接池属性

```sql
REGISTER STORAGE UNIT ds_1 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db_1",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"=10)
);
```

- 使用 URL 方式注册存储单元并设置连接池属性

```sql
REGISTER STORAGE UNIT ds_2 (
    URL="jdbc:mysql://127.0.0.1:3306/db_2?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"=10,"idleTimeout"=30000)
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

- 注册时校验 `SELECT`、`XA` 和 `PIPELINE` 权限

```sql
REGISTER STORAGE UNIT ds_3 (
    URL="jdbc:mysql://127.0.0.1:3306/db_3?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"=10,"idleTimeout"=30000)
), CHECK_PRIVILEGES=SELECT,XA,PIPELINE;
```

### 保留字

`REGISTER`、`STORAGE`、`UNIT`、`HOST`、`PORT`、`DB`、`USER`、`PASSWORD`、`PROPERTIES`、`URL`、`CHECK_PRIVILEGES`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)