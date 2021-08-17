+++
title = "RAL"
weight = 1
+++

## 定义

RAL (Resource & Rule Administration Language) 为 Apache ShardingSphere 的管理语言，负责Hint、事务类型切换、分片执行计划查询等增量功能的操作。

## 使用实战

| 语句                                                | 说明                                                           | 示例                                           |
|:---------------------------------------------------|:--------------------------------------------------------------|:-----------------------------------------------|
|set variable transaction_type = xx                  | 修改当前连接的事务类型, 支持LOCAL，XA，BASE                         | set variable transaction_type = XA            |  
|show variable transaction_type                      | 查询当前连接的事务类型                                             | show variable transaction_type                |  
|show variable cached_connections                    | 查询当前连接中缓存的物理数据库连接个数                                | show variable cached_connections              |  
|preview SQL                                         | 预览实际 SQL                                                    | preview select * from t_order                 |  
|set readwrite_splitting hint source = [auto / write]| 针对当前连接，设置读写分离的路由策略（自动路由或强制到写库）              | set readwrite_splitting hint source = write   |  
|set sharding hint database_value = yy               | 针对当前连接，设置 hint 仅对数据库分片有效，并添加分片值，yy：数据库分片值 | set sharding hint database_value = 100        |  
|add sharding hint database_value xx= yy             | 针对当前连接，为表 xx 添加分片值 yy，xx：逻辑表名称，yy：数据库分片值     | add sharding hint database_value t_order= 100 |  
|add sharding hint table_value xx = yy               | 针对当前连接，为表 xx 添加分片值 yy，xx：逻辑表名称，yy：表分片值        | add sharding hint table_value t_order = 100   |  
|clear hint                                          | 针对当前连接，清除 hint 所有设置                                    | clear hint                                    |  
|clear [sharding hint / readwrite_splitting hint]    | 针对当前连接，清除 sharding 或 readwrite_splitting 的 hint 设置     | clear readwrite_splitting hint                |  
|show [sharding / readwrite_splitting] hint status   | 针对当前连接，查询 sharding 或 readwrite_splitting 的 hint 设置     | show readwrite_splitting hint status          |  

## 注意事项

ShardingSphere-Proxy 默认不支持 hint，如需支持，请在 `conf/server.yaml` 中，将 `properties` 的属性 `proxy-hint-enabled` 设置为 true。
