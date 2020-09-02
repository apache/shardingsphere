+++
pre = "<b>4.5.2. </b>"
title = "Manual"
weight = 2
+++

## Manual

### Environment

JAVA，JDK 1.8+.

The migration scene we support：

| Source                     | Destination          | Support |
| -------------------------- | -------------------- | ------- |
| MySQL(5.1.15 ~ 5.7.x)      | ShardingSphere-Proxy | Yes     |
| PostgreSQL(9.4 ~ )         | ShardingSphere-Proxy | Yes     |

**Attention**: 

If the backend database is MySQL, download [MySQL Connector/J](https://cdn.mysql.com//Downloads/Connector-J/mysql-connector-java-5.1.47.tar.gz) 
and decompress, then copy mysql-connector-java-5.1.47.jar to `${shardingsphere-scaling}\lib directory`.

### Privileges

MySQL need to open `binlog`, and `binlog format` should be Row model. Privileges of users scaling used should include Replication privileges.

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

PostgreSQL need to support and open [test_decoding](https://www.postgresql.org/docs/9.4/test-decoding.html) feature.

### API

ShardingSphere-Scaling provides a simple HTTP API

#### Start scaling job

Interface description：POST /scaling/job/start

Body：

| Parameter                                         | Describe                                        |
|---------------------------------------------------|-------------------------------------------------|
| ruleConfiguration.sourceDatasource                | source sharding sphere data source configuration |
| ruleConfiguration.sourceRule                      | source sharding sphere table rule configuration  |
| ruleConfiguration.destinationDataSources.name     | destination sharding proxy name                 |
| ruleConfiguration.destinationDataSources.url      | destination sharding proxy jdbc url             |
| ruleConfiguration.destinationDataSources.username | destination sharding proxy username             |
| ruleConfiguration.destinationDataSources.password | destination sharding proxy password             |
| jobConfiguration.concurrency                      | sync task proposed concurrency                  |

Example：

```
curl -X POST \
  http://localhost:8888/scaling/job/start \
  -H 'content-type: application/json' \
  -d '{
   "ruleConfiguration": {
      "sourceDatasource": "ds_0: !!org.apache.shardingsphere.governance.core.common.yaml.config.YamlDataSourceConfiguration\n  dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n  props:\n    jdbcUrl: jdbc:mysql://127.0.0.1:3306/test?serverTimezone=UTC&useSSL=false\n    username: root\n    password: '\''123456'\''\n    connectionTimeout: 30000\n    idleTimeout: 60000\n    maxLifetime: 1800000\n    maxPoolSize: 50\n    minPoolSize: 1\n    maintenanceIntervalMilliseconds: 30000\n    readOnly: false\n",
      "sourceRule": "defaultDatabaseStrategy:\n  inline:\n    algorithmExpression: ds_${user_id % 2}\n    shardingColumn: user_id\ntables:\n  t1:\n    actualDataNodes: ds_0.t1\n    keyGenerateStrategy:\n      column: order_id\n      type: SNOWFLAKE\n    logicTable: t1\n    tableStrategy:\n      inline:\n        algorithmExpression: t1\n        shardingColumn: order_id\n  t2:\n    actualDataNodes: ds_0.t2\n    keyGenerateStrategy:\n      column: order_item_id\n      type: SNOWFLAKE\n    logicTable: t2\n    tableStrategy:\n      inline:\n        algorithmExpression: t2\n        shardingColumn: order_id\n",
      "destinationDataSources": {
         "name": "dt_0",
         "password": "123456",
         "url": "jdbc:mysql://127.0.0.1:3306/test2?serverTimezone=UTC&useSSL=false",
         "username": "root"
      }
   },
   "jobConfiguration": {
      "concurrency": 3
   }
}'
```

Response：

```
{
   "success": true,
   "errorCode": 0,
   "errorMsg": null,
   "model": null
}
```

#### Get scaling progress

Interface description：GET /scaling/job/progress/{jobId}

Example：
```
curl -X GET \
  http://localhost:8888/scaling/job/progress/1
```

Response：
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

#### List scaling jobs

Interface description：GET /scaling/job/list

Example：
```
curl -X GET \
  http://localhost:8888/scaling/job/list
```

Response：

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

#### Stop scaling job

Interface description：POST /scaling/job/stop

Body：

| Parameter | Describe |
| --------- | -------- |
| jobId     | job id   |

Example：
```
curl -X POST \
  http://localhost:8888/scaling/job/stop \
  -H 'content-type: application/json' \
  -d '{
   "jobId":1
}'
```
Response：
```
{
   "success": true,
   "errorCode": 0,
   "errorMsg": null,
   "model": null
}
```

### Operate through the UI interface

We provide user interface in ShardingSphere-UI, so all the operations related can be implemented with a click of the UI interface.
For more information, please refer to the ShardingSphere-UI module.
