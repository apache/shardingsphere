+++
title = "存储单元定义"
weight = 1
+++

## 语法说明

```sql
REGISTER STORAGE UNIT storageUnitDefinition [, storageUnitDefinition] ...

ALTER STORAGE UNIT storageUnitDefinition [, storageUnitDefinition] ...
    
UNREGISTER STORAGE UNIT storageUnitName [, storageUnitName] ... [ignore single tables]

storageUnitDefinition:
    simpleSource | urlSource

simpleSource:
    storageUnitName(HOST=hostname,PORT=port,DB=dbName,USER=user [,PASSWORD=password] [,PROPERTIES(property [,property]) ...])

urlSource:
    storageUnitName(URL=url,USER=user [,PASSWORD=password] [,PROPERTIES(property [,property]) ...])

property:
    key=value
```

### 参数解释

| 名称                 | 数据类型    | 说明     |
|:-------------------|:-----------|:-------|
| storageUnitName    | IDENTIFIER | 存储单元名称 |
| hostname           | STRING     | 数据源地址  |
| port               | INT        | 数据源端口  |
| dbName             | STRING     | 物理库名称  |
| url                | STRING     | URL 地址 |
| user               | STRING     | 用户名    |
| password           | STRING     | 密码     |

### 注意事项

- 添加存储单元前请确认已经创建分布式数据库，并执行 `use` 命令成功选择一个数据库；
- 确认将要添加或修改的存储单元是可以正常连接的， 否则将不能操作成功；
- 不允许重复的 `storageUnitName`；
- `PROPERTIES` 用于自定义连接池参数，`key` 和 `value` 均为 STRING 类型；
- `ALTER STORAGE UNIT` 修改存储单元时不允许改变该存储单元关联的真实数据源；
- `ALTER STORAGE UNIT` 修改存储单元时会发生连接池的切换，此操作可能对进行中的业务造成影响，请谨慎使用；
- `UNREGISTER STORAGE UNIT` 只会删除逻辑存储单元，不会删除真实的数据源；
- 被规则引用的存储单元将无法被删除；
- 若存储单元只被 `single table rule` 引用，且用户确认可以忽略该限制，则可以添加可选参数 `ignore single tables` 进行强制删除。

## 示例

```sql
REGISTER STORAGE UNIT su_0 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db0",
    USER="root",
    PASSWORD="root"
),su_1 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db1",
    USER="root"
),su_2 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db2",
    USER="root",
    PROPERTIES("maximumPoolSize"="10")
),su_3 (
    URL="jdbc:mysql://127.0.0.1:3306/db3?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"="10","idleTimeout"="30000")
);

ALTER STORAGE UNIT su_0 (
    HOST="127.0.0.1",
    PORT=3309,
    DB="db0",
    USER="root",
    PASSWORD="root"
),su_1 (
    URL="jdbc:mysql://127.0.0.1:3309/db1?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"="10","idleTimeout"="30000")
);

UNREGISTER STORAGE UNIT su_0, su_1;
UNREGISTER STORAGE UNIT su_2, su_3 ignore single tables;
```
