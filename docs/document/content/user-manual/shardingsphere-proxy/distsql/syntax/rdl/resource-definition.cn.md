+++
title = "资源定义"
weight = 1
+++

## 语法说明

```sql
ADD RESOURCE dataSource [, dataSource] ...

ALTER RESOURCE dataSource [, dataSource] ...
    
DROP RESOURCE dataSourceName [, dataSourceName] ... [ignore single tables]

dataSource:
    simpleSource | urlSource

simpleSource:
    dataSourceName(HOST=hostName,PORT=port,DB=dbName,USER=user [,PASSWORD=password] [,PROPERTIES(poolProperty [,poolProperty]) ...])

urlSource:
    dataSourceName(URL=url,USER=user [,PASSWORD=password] [,PROPERTIES(poolProperty [,poolProperty]) ...])

poolProperty:
    "key"= ("value" | value)
```

- 添加资源前请确认已经创建分布式数据库，并执行 `use` 命令成功选择一个数据库
- 确认增加的资源是可以正常连接的， 否则将不能添加成功
- 重复的 `dataSourceName` 不允许被添加
- 在同一 `dataSource` 的定义中，`simpleSource` 和 `urlSource` 语法不可混用
- `poolProperty` 用于自定义连接池参数，`key` 必须和连接池参数名一致，`value` 支持 int 和 String 类型
- `ALTER RESOURCE` 修改资源时会发生连接池的切换，这个操作可能对进行中的业务造成影响，请谨慎使用
- `DROP RESOURCE` 只会删除逻辑资源，不会删除真实的数据源
- 被规则引用的资源将无法被删除
- 若资源只被 `single table rule` 引用，且用户确认可以忽略该限制，则可以添加可选参数 `ignore single tables` 进行强制删除

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

ALTER RESOURCE resource_0 (
    HOST=127.0.0.1,
    PORT=3309,
    DB=db0,
    USER=root,
    PASSWORD=root
),resource_1 (
    URL="jdbc:mysql://127.0.0.1:3309/db1?serverTimezone=UTC&useSSL=false",
    USER=root,
    PASSWORD=root,
    PROPERTIES("maximumPoolSize"=10,"idleTimeout"="30000")
);

DROP RESOURCE resource_0, resource_1;
DROP RESOURCE resource_2, resource_3 ignore single tables;
```
