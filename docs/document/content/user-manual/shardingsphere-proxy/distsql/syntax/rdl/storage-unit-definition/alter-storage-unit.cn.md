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
  'ALTER' 'STORAGE' 'UNIT' storageUnitDefinition (',' storageUnitDefinition)*

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
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- 修改存储单元前请确认已经在 Proxy 中创建逻辑数据库，并执行 `use` 命令成功选择一个逻辑数据库；
- `ALTER STORAGE UNIT`不允许改变该存储单元关联的真实数据源；
- `ALTER STORAGE UNIT`会发生连接池的切换，这个操作可能对进行中的业务造成影响，请谨慎使用；
- 确认添加的存储单元是可以正常连接的， 否则将不能添加成功；
- `storageUnitName` 区分大小写；
- `storageUnitName` 在当前逻辑库中需要唯一；
- `storageUnitName` 命名只允许使用字母、数字以及 `_` ，且必须以字母开头；
- `poolProperty` 用于自定义连接池参数，`key` 必须和连接池参数名一致。

### 示例

- 使用标准模式修改存储单元

```sql
ALTER STORAGE UNIT ds_0 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db_0",
    USER="root",
    PASSWORD="root"
);
```

- 使用标准模式修改存储单元并设置连接池参数

```sql
ALTER STORAGE UNIT ds_0 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db_1",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"=10)
);
```

- 使用 URL 模式修改存储单元并设置连接池参数

```sql
ALTER STORAGE UNIT ds_0 (
    URL="jdbc:mysql://127.0.0.1:3306/db_2?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"=10,"idleTimeout"="30000")
);
```

### 保留字

`ALTER`、`STORAGE`、`UNIT`、`HOST`、`PORT`、`DB`、`USER`、`PASSWORD`、`PROPERTIES`、`URL`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)