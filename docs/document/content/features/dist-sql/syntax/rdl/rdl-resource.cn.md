+++
title = "数据源资源"
weight = 1
+++

## 定义

```sql
ADD RESOURCE dataSource [, dataSource] ...

dataSource:
    dataSourceName(HOST=hostName,PORT=port,DB=dbName,USER=user [, PASSWORD=password])
    
DROP RESOURCE dataSourceName [, dataSourceName] ...    
```

- 添加资源前请确认已经创建分布式数据库，并执行 `use` 命令成功选择一个数据库
- 确认增加的资源是可以正常连接的， 否则将不能添加成功
- 重复的 `dataSourceName` 不允许被添加
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
);

DROP RESOURCE resource_0, resource_1;
```
