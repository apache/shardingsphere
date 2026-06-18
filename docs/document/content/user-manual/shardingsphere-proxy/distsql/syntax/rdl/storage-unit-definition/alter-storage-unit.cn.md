+++
title = "ALTER STORAGE UNIT"
weight = 2
+++

### 描述

`ALTER STORAGE UNIT` 语法用于修改当前所选逻辑库（DATABASE）的存储单元。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
AlterStorageUnit ::=
  'ALTER' 'STORAGE' 'UNIT' storageUnitsDefinition (',' checkPrivileges)?

storageUnitsDefinition ::=
  storageUnitDefinition (',' storageUnitDefinition)*

storageUnitDefinition ::=
  storageUnitName '(' ('HOST' '=' hostName ',' 'PORT' '=' port ',' 'DB' '=' dbName | 'URL' '=' url) ',' 'USER' '=' user (',' 'PASSWORD' '=' password)? (',' propertiesDefinition)?')'

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

### 补充说明

- 修改存储单元前请确认已经在 Proxy 中创建逻辑数据库，并执行 `use` 命令选择一个逻辑数据库；
- `ALTER STORAGE UNIT`不允许改变该存储单元关联的真实数据源（通过 host、port 和 db 判断）；
- `ALTER STORAGE UNIT`会发生连接池的切换，这个操作可能对进行中的业务造成影响，请谨慎使用；
- 请确认修改的存储单元是可以正常连接的， 否则将不能修改成功；
- `PROPERTIES` 为可选参数，用于自定义连接池属性，`key` 必须和连接池参数名一致;
- 可通过 `CHECK_PRIVILEGES` 指定注册时校验存储单元用户的权限，`privilegeType` 支持的类型有 `SELECT`、`XA`、`PIPELINE`、`NONE`，缺省值为 `SELECT`，当类型列表中包含 `NONE` 时，跳过权限校验。

### 示例

- 使用 HOST & PORT 方式修改存储单元

```sql
ALTER STORAGE UNIT ds_0 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db_0",
    USER="root",
    PASSWORD="root"
);
```

- 使用 HOST & PORT 方式修改存储单元并设置连接池属性

```sql
ALTER STORAGE UNIT ds_1 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db_1",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"=10)
);
```

- 使用 URL 模式修改存储单元并设置连接池属性

```sql
ALTER STORAGE UNIT ds_2 (
    URL="jdbc:mysql://127.0.0.1:3306/db_2?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"=10,"idleTimeout"=30000)
);
```

- 修改存储单元时检查 `SELECT`、`XA` 和 `PIPELINE` 权限

```sql
ALTER STORAGE UNIT ds_2 (
    URL="jdbc:mysql://127.0.0.1:3306/db_2?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"=10,"idleTimeout"=30000)
), CHECK_PRIVILEGES=SELECT,XA,PIPELINE;
```

### 保留字

`ALTER`、`STORAGE`、`UNIT`、`HOST`、`PORT`、`DB`、`USER`、`PASSWORD`、`PROPERTIES`、`URL`、`CHECK_PRIVILEGES`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)