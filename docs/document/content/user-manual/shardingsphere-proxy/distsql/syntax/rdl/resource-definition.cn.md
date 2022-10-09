+++
title = "资源定义"
weight = 1
+++

## 语法说明

```sql
ADD RESOURCE resourceDefinition [, resourceDefinition] ...

ALTER RESOURCE resourceDefinition [, resourceDefinition] ...
    
DROP RESOURCE resourceName [, resourceName] ... [ignore single tables]

resourceDefinition:
    simpleSource | urlSource

simpleSource:
    resourceName(HOST=hostname,PORT=port,DB=dbName,USER=user [,PASSWORD=password] [,PROPERTIES(property [,property]) ...])

urlSource:
    resourceName(URL=url,USER=user [,PASSWORD=password] [,PROPERTIES(property [,property]) ...])

property:
    key=value
```

### 参数解释

| 名称          | 数据类型    | 说明       |
|:-------------|:-----------|:-----------|
| resourceName | IDENTIFIER | 资源名称    |
| hostname     | STRING     | 数据源地址  |
| port         | INT        | 数据源端口  |
| dbName       | STRING     | 物理库名称  |
| url          | STRING     | URL 地址   |
| user         | STRING     | 用户名     |
| password     | STRING     | 密码       |

### 注意事项

- 添加资源前请确认已经创建分布式数据库，并执行 `use` 命令成功选择一个数据库；
- 确认将要添加或修改的资源是可以正常连接的， 否则将不能操作成功；
- 不允许重复的 `resourceName`；
- `PROPERTIES` 用于自定义连接池参数，`key` 和 `value` 均为 STRING 类型；
- `ALTER RESOURCE` 修改资源时不允许改变该资源关联的真实数据源；
- `ALTER RESOURCE` 修改资源时会发生连接池的切换，此操作可能对进行中的业务造成影响，请谨慎使用；
- `DROP RESOURCE` 只会删除逻辑资源，不会删除真实的数据源；
- 被规则引用的资源将无法被删除；
- 若资源只被 `single table rule` 引用，且用户确认可以忽略该限制，则可以添加可选参数 `ignore single tables` 进行强制删除。

## 示例

```sql
ADD RESOURCE resource_0 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db0",
    USER="root",
    PASSWORD="root"
),resource_1 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db1",
    USER="root"
),resource_2 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="db2",
    USER="root",
    PROPERTIES("maximumPoolSize"="10")
),resource_3 (
    URL="jdbc:mysql://127.0.0.1:3306/db3?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"="10","idleTimeout"="30000")
);

ALTER RESOURCE resource_0 (
    HOST="127.0.0.1",
    PORT=3309,
    DB="db0",
    USER="root",
    PASSWORD="root"
),resource_1 (
    URL="jdbc:mysql://127.0.0.1:3309/db1?serverTimezone=UTC&useSSL=false",
    USER="root",
    PASSWORD="root",
    PROPERTIES("maximumPoolSize"="10","idleTimeout"="30000")
);

DROP RESOURCE resource_0, resource_1;
DROP RESOURCE resource_2, resource_3 ignore single tables;
```
