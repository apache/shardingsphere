+++
pre = "<b>4.5.2. </b>"
title = "使用手册"
weight = 2
+++

## 使用手册

### 环境要求

纯 JAVA 开发，JDK 建议 1.8 以上版本。

支持迁移场景如下：

| 源端                  | 目标端                | 是否支持 |
| --------------------- | -------------------- | ------- |
| MySQL(5.1.15 ~ 5.7.x) | ShardingSphere-Proxy | 是      |
| PostgreSQL(9.4 ~ )    | ShardingSphere-Proxy | 是      |

**注意**：

如果后端连接 MySQL 数据库，请下载 [mysql-connector-java-5.1.47.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar)，并将其放入 `${shardingsphere-scaling}\lib` 目录。

### 权限要求

MySQL 需要开启 `binlog`，`binlog format` 为Row模式，且迁移时所使用用户需要赋予 Replication 相关权限。

```
+-----------------------------------------+---------------------------------------+
| Variable_name                           | Value                                 |
+-----------------------------------------+---------------------------------------+
| log_bin                                 | ON                                    |
| binlog_format                           | ROW                                   |
+-----------------------------------------+---------------------------------------+

+------------------------------------------------------------------------------+
|Grants for ${username}@${host}                                                |
+------------------------------------------------------------------------------+
|GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO ${username}@${host}     |
|.......                                                                       |
+------------------------------------------------------------------------------+
```

PostgreSQL 需要开启 [test_decoding](https://www.postgresql.org/docs/9.4/test-decoding.html)

### API接口

弹性迁移组件提供了简单的 HTTP API 接口

#### 创建迁移任务

接口描述：POST /scaling/job/start

请求体：

| 参数                                               | 描述                                                         |
| ------------------------------------------------- | ------------------------------------------------------------ |
| ruleConfiguration.source                          | 源端数据源相关配置                                             |
| ruleConfiguration.target                          | 目标端数据源相关配置                                           |
| jobConfiguration.concurrency                      | 迁移并发度，举例：如果设置为3，则待迁移的表将会有三个线程同时对该表进行迁移，前提是该表有整数型主键 |

数据源配置：

| 参数                                               | 描述                                                         |
| ------------------------------------------------- | ------------------------------------------------------------ |
| type                                              | 数据源类型（可选参数：shardingSphereJdbc、jdbc）                |
| parameter                                         | 数据源参数                                                    |

Parameter配置：

type = shardingSphereJdbc 

| 参数                                               | 描述                                                         |
| ------------------------------------------------- | ------------------------------------------------------------ |
| dataSource                                        | 源端sharding sphere数据源相关配置                              |
| rule                                              | 源端sharding sphere表规则相关配置                              |

type = jdbc 

| 参数                                               | 描述                                                         |
| ------------------------------------------------- | ------------------------------------------------------------ |
| name                                              | jdbc 名称                                                    |
| ruleConfiguration.targetDataSources.url           | jdbc 连接                                                    |
| ruleConfiguration.targetDataSources.username      | jdbc 用户                                                    |
| ruleConfiguration.targetDataSources.password      | jdbc 密码                                                    |

*** 注意 ***

当前 source type 必须是 shardingSphereJdbc

示例：

```
curl -X POST \
  http://localhost:8888/scaling/job/start \
  -H 'content-type: application/json' \
  -d '{
        "ruleConfiguration": {
          "source": {
            "type": "shardingSphereJdbc",
            "parameter": {
              "dataSource":"
                dataSources:
                  ds_0:
                    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
                    props:
                      driverClassName: com.mysql.jdbc.Driver
                      jdbcUrl: jdbc:mysql://127.0.0.1:3306/scaling_0?useSSL=false
                      username: scaling
                      password: scaling
                  ds_1:
                    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
                    props:
                      driverClassName: com.mysql.jdbc.Driver
                      jdbcUrl: jdbc:mysql://127.0.0.1:3306/scaling_1?useSSL=false
                      username: scaling
                      password: scaling
                ",
              "rule":"
                rules:
                - !SHARDING
                  tables:
                    t_order:
                      actualDataNodes: ds_$->{0..1}.t_order_$->{0..1}
                      databaseStrategy:
                        standard:
                          shardingColumn: order_id
                          shardingAlgorithmName: t_order_db_algorith
                      logicTable: t_order
                      tableStrategy:
                        standard:
                          shardingColumn: user_id
                          shardingAlgorithmName: t_order_tbl_algorith
                  shardingAlgorithms:
                    t_order_db_algorith:
                      type: INLINE
                      props:
                        algorithm-expression: ds_$->{order_id % 2}
                    t_order_tbl_algorith:
                      type: INLINE
                      props:
                        algorithm-expression: t_order_$->{user_id % 2}
                "
            }
          },
          "target": {
              "type": "jdbc",
              "parameter": {
                "username": "root",
                "password": "root",
                "url": "jdbc:mysql://127.0.0.1:3307/sharding_db?serverTimezone=UTC&useSSL=false"
              }
          }
        },
        "jobConfiguration":{
          "concurrency":"3"
        }
      }'
```

返回信息：

```
{
   "success": true,
   "errorCode": 0,
   "errorMsg": null,
   "model": null
}
```

#### 查询迁移任务进度

接口描述：GET /scaling/job/progress/{jobId}

示例：
```
curl -X GET \
  http://localhost:8888/scaling/job/progress/1
```

返回信息：
```
{
   "success": true,
   "errorCode": 0,
   "errorMsg": null,
   "model": {
        "id": 1,
        "jobName": "Local Sharding Scaling Job",
        "status": "RUNNING/STOPPED"
        "syncTaskProgress": [{
            "id": "127.0.0.1-3306-test",
            "status": "PREPARING/MIGRATE_HISTORY_DATA/SYNCHRONIZE_REALTIME_DATA/STOPPING/STOPPED",
            "historySyncTaskProgress": [{
                "id": "history-test-t1#0",
                "estimatedRows": 41147,
                "syncedRows": 41147
            }, {
                "id": "history-test-t1#1",
                "estimatedRows": 42917,
                "syncedRows": 42917
            }, {
                "id": "history-test-t1#2",
                "estimatedRows": 43543,
                "syncedRows": 43543
            }, {
                "id": "history-test-t2#0",
                "estimatedRows": 39679,
                "syncedRows": 39679
            }, {
                "id": "history-test-t2#1",
                "estimatedRows": 41483,
                "syncedRows": 41483
            }, {
                "id": "history-test-t2#2",
                "estimatedRows": 42107,
                "syncedRows": 42107
            }],
            "realTimeSyncTaskProgress": {
                "id": "realtime-test",
                "delayMillisecond": 1576563771372,
                "position": {
                    "filename": "ON.000007",
                    "position": 177532875,
                    "serverId": 0
                }
            }
        }]
   }
}
```

#### 查询所有迁移任务

接口描述：GET /scaling/job/list

示例：

```
curl -X GET \
  http://localhost:8888/scaling/job/list
```

返回信息：

```
{
  "success": true,
  "errorCode": 0,
  "model": [
    {
      "jobId": 1,
      "jobName": "Local Sharding Scaling Job",
      "status": "RUNNING"
    }
  ]
}
```

#### 停止迁移任务

接口描述：POST /scaling/job/stop

请求体：

| 参数      | 描述      |
| --------- | -------- |
| jobId     | job id   |

示例：
```
curl -X POST \
  http://localhost:8888/scaling/job/stop \
  -H 'content-type: application/json' \
  -d '{
   "jobId":1
}'
```
返回信息：
```
{
   "success": true,
   "errorCode": 0,
   "errorMsg": null,
   "model": null
}
```

## 通过UI界面来操作

ShardingSphere-Scaling 与 ShardingSphere-UI 集成了用户界面，所以上述所有任务相关的操作都可以通过 UI 界面点点鼠标来实现，当然本质上还是调用了上述基本接口。

更多信息请参考 ShardingSphere-UI 项目。
