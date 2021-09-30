+++
title = "RAL"
weight = 1
+++

## 定义

RAL (Resource & Rule Administration Language) 为 Apache ShardingSphere 的管理语言，负责强制路由、事务类型切换、弹性伸缩、分片执行计划查询等增量功能的操作。

## 使用实战

## 强制路由

| 语句                                                | 说明                                                            | 示例                                           |
|:---------------------------------------------------|:----------------------------------------------------------------|:-----------------------------------------------|
|set readwrite_splitting hint source = [auto / write]| 针对当前连接，设置读写分离的路由策略（自动路由或强制到写库）              | set readwrite_splitting hint source = write   |  
|set sharding hint database_value = yy               | 针对当前连接，设置 hint 仅对数据库分片有效，并添加分片值，yy：数据库分片值 | set sharding hint database_value = 100        |  
|add sharding hint database_value xx = yy            | 针对当前连接，为表 xx 添加分片值 yy，xx：逻辑表名称，yy：数据库分片值     | add sharding hint database_value t_order= 100 |  
|add sharding hint table_value xx = yy               | 针对当前连接，为表 xx 添加分片值 yy，xx：逻辑表名称，yy：表分片值        | add sharding hint table_value t_order = 100   |  
|clear hint                                          | 针对当前连接，清除 hint 所有设置                                    | clear hint                                    |  
|clear [sharding hint / readwrite_splitting hint]    | 针对当前连接，清除 sharding 或 readwrite_splitting 的 hint 设置     | clear readwrite_splitting hint                |  
|show [sharding / readwrite_splitting] hint status   | 针对当前连接，查询 sharding 或 readwrite_splitting 的 hint 设置     | show readwrite_splitting hint status          |  

## 弹性伸缩

| 语句                                                | 说明                                                           | 示例                                           |
|:---------------------------------------------------|:--------------------------------------------------------------|:-----------------------------------------------|
|show scaling list                                   | 查询运行列表                                                    | show scaling list                              |  
|show scaling status xx                              | 查询任务状态，xx：任务id                                         | show scaling status 1234                       |  
|start scaling xx                                    | 开始运行任务，xx：任务id                                         | start scaling 1234                             |  
|stop scaling xx                                     | 停止运行任务，xx：任务id                                         | stop scaling 12345                              |  
|drop scaling xx                                     | 移除任务，xx：任务id                                            | drop scaling 1234                              |  
|reset scaling xx                                    | 重置任务进度，xx：任务id                                         | reset scaling 1234                             |  
|check scaling xx                                    | 数据一致性校验，xx：任务id                                        | check scaling 1234                             |  
|show scaling check algorithms                       | 展示可用的一致性校验算法                                          | show scaling check algorithms                  |  
|stop scaling source writing xx                      | 旧的 ShardingSphere 数据源停写，xx：任务id                        | stop scaling source writing 1234               |  
|checkout scaling xx                                 | 切换至新的 ShardingSphere 数据源，xx：任务id                      | checkout scaling 1234                         |  


## 熔断

| 语句                                                               | 说明                                | 示例                                           |
|:------------------------------------------------------------------|:------------------------------------|:----------------------------------------------|
|[enable / disable] readwrite_splitting read xxx [from schema]      | 启用 / 禁用读库                       | enable readwrite_splitting read xxx           |  
|[enable / disable] instance IP=xxx, PORT=xxx                       | 启用 / 禁用proxy实例                  | disable instance IP=127.0.0.1, PORT=3307      |  

## 其他

| 语句                                                | 说明                                                          | 示例                                           |
|:---------------------------------------------------|:--------------------------------------------------------------|:----------------------------------------------|
|set variable transaction_type = xx                  | 修改当前连接的事务类型, 支持LOCAL，XA，BASE                        | set variable transaction_type = XA            |  
|show variable transaction_type                      | 查询当前连接的事务类型                                            | show variable transaction_type                |  
|show variable cached_connections                    | 查询当前连接中缓存的物理数据库连接个数                               | show variable cached_connections              |  
|preview SQL                                         | 预览实际 SQL                                                    | preview select * from t_order                 |  

## 注意事项

ShardingSphere-Proxy 默认不支持 hint，如需支持，请在 `conf/server.yaml` 中，将 `properties` 的属性 `proxy-hint-enabled` 设置为 true。
