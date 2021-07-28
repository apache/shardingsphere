+++
title = "数据源资源"
weight = 1
+++

## 定义

```sql
ADD RESOURCE dataSource [, dataSource] ...

dataSource:
    simpleSource | urlSource

simpleSource:
    dataSourceName(HOST=hostName,PORT=port,DB=dbName,USER=user [,PASSWORD=password] [,PROPERTIES(poolProperty [,poolProperty]) ...])

urlSource:
    dataSourceName(URL=url,USER=user [,PASSWORD=password] [,PROPERTIES(poolProperty [,poolProperty]) ...])

poolProperty:
    "key"= ("value" | value)
    
DROP RESOURCE dataSourceName [, dataSourceName] ...    
```

- 添加资源前请确认已经创建分布式数据库，并执行 `use` 命令成功选择一个数据库
- 确认增加的资源是可以正常连接的， 否则将不能添加成功
- 重复的 `dataSourceName` 不允许被添加
- 在同一 `dataSource` 的定义中，`simpleSource` 和 `urlSource` 语法不可混用
- `poolProperty` 用于自定义连接池参数，`key` 必须和连接池参数名一致，`value` 支持 int 和 String 类型
- `DROP RESOURCE` 只会删除逻辑资源，不会删除真实的数据源
- 被规则引用的资源将无法被删除

## 示例

```sql
ADD RESOURCE resource_0 (
    HOST=127.0.0.1,
    PORT=3306,
    DB=db0,
    USER=root,
    PASSWORD=root
),resource_1 (
    HOST=127.0.0.1,
    PORT=3306,
    DB=db1,
    USER=root
),resource_2 (
    HOST=127.0.0.1,
    PORT=3306,
    DB=db2,
    USER=root,
    PROPERTIES("maximumPoolSize"=10)
),resource_3 (
    URL="jdbc:mysql://127.0.0.1:3306/db3?serverTimezone=UTC&useSSL=false",
    USER=root,
    PASSWORD=root,
    PROPERTIES("maximumPoolSize"=10,"idleTimeout"="30000")
);

DROP RESOURCE resource_0, resource_1, resource_2, resource_3;
```
