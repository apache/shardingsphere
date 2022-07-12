+++
title = "元数据持久化仓库"
weight = 1
+++

## H2 数据库持久化

类型：H2

适用模式：Standalone

可配置属性：

| *名称*                        | *数据类型* | *说明*            | *默认值*         |
| ---------------------------- | --------- | ---------------- | --------------- |
| jdbcUrl                      | String    | 连接数据库的 URL   | jdbc:h2:mem:config;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL |
| user                         | String    | 访问数据库的用户名  | sa                                                                      |
| password                     | String    | 访问数据库的密码    |                                                                         |

## ZooKeeper 持久化

类型：ZooKeeper

适用模式：Cluster

可配置属性：

| *名称*                        | *数据类型* | *说明*              | *默认值*       |
| ---------------------------- | --------- | ------------------ | ------------- |
| retryIntervalMilliseconds    | int       | 重试间隔毫秒数        | 500           |
| maxRetries                   | int       | 客户端连接最大重试次数  | 3             |
| timeToLiveSeconds            | int       | 临时数据失效的秒数     | 60            |
| operationTimeoutMilliseconds | int       | 客户端操作超时的毫秒数  | 500           |
| digest                       | String    | 登录认证密码          |               |

## Etcd 持久化

类型：Etcd

适用模式：Cluster

可配置属性：

| *名称*                        | *数据类型* | *说明*               | *默认值*         |
| ---------------------------- | --------- | ------------------- | --------------- |
| timeToLiveSeconds            | long      | 临时数据失效的秒数     | 30              |
| connectionTimeout            | long      | 连接超时秒数          | 30              |
