+++
pre = "<b>2.3. </b>"
title = "ShardingSphere-Scaling(Alpha)"
weight = 3
+++

## Quick Start

### Deployment

#### 1. Execute the following command to compile and generate the shardingsphere-scaling binary package:

```
git clone https://github.com/apache/shardingsphere.git；
cd shardingsphere;
mvn clean install -Prelease;
```

The binary package's directory is:`/shardingsphere-distribution/shardingsphere-scaling-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-scaling-bin.tar.gz`。

#### 2. Unzip the distribution package, modify the configuration file `conf/server.yaml`, we should ensure the port does not conflict with others, and other values can be left as default:

```
port: 8888
blockQueueSize: 10000
pushTimeout: 1000
workerThread: 30
```

#### 3. start up ShardingSphere-Scaling:

```
sh bin/start.sh
```

**Attention**: 

If the backend database is MySQL, download [MySQL Connector/J](https://cdn.mysql.com//Downloads/Connector-J/mysql-connector-java-5.1.47.tar.gz) 
and decompress, then copy mysql-connector-java-5.1.47.jar to ${shardingsphere-scaling}\lib directory.

#### 4. See the log file `logs/stdout.log`，ensure successful startup.

### Start scaling job

ShardingSphere-Scaling provides a corresponding HTTP interface to manage the migration jobs. We can invoke the appropriate interface to start the migration job.

Start scaling job:

```
curl -X POST \
  http://localhost:8888/shardingscaling/job/start \
  -H 'content-type: application/json' \
  -d '{
   "ruleConfiguration": {
      "sourceDatasource": "ds_0: !!org.apache.shardingsphere.orchestration.core.configuration.YamlDataSourceConfiguration\n  dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n  properties:\n    jdbcUrl: jdbc:mysql://127.0.0.1:3306/test?serverTimezone=UTC&useSSL=false\n    username: root\n    password: '\''123456'\''\n    connectionTimeout: 30000\n    idleTimeout: 60000\n    maxLifetime: 1800000\n    maxPoolSize: 50\n    minPoolSize: 1\n    maintenanceIntervalMilliseconds: 30000\n    readOnly: false\n",
      "sourceRule": "defaultDatabaseStrategy:\n  inline:\n    algorithmExpression: ds_${user_id % 2}\n    shardingColumn: user_id\ntables:\n  t1:\n    actualDataNodes: ds_0.t1\n    keyGenerator:\n      column: order_id\n      type: SNOWFLAKE\n    logicTable: t1\n    tableStrategy:\n      inline:\n        algorithmExpression: t1\n        shardingColumn: order_id\n  t2:\n    actualDataNodes: ds_0.t2\n    keyGenerator:\n      column: order_item_id\n      type: SNOWFLAKE\n    logicTable: t2\n    tableStrategy:\n      inline:\n        algorithmExpression: t2\n        shardingColumn: order_id\n",
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

Note: The `ruleConfiguration.sourceDatasource` and `ruleConfiguration.sourceRule` should be changed to source's ShardingSphere's datasource and table rule.

What's more, the `ruleConfiguration.destinationDataSources` should be changed to destination's ShardingSphere-Proxy.

The following information is returned, indicating that the job was successfully created:

```
{
   "success": true,
   "errorCode": 0,
   "errorMsg": null,
   "model": null
}
```

It should be noted that, after the ShardingSphere-Scaling's job is successfully created, it will automatically run.

### Get scaling progress

Run the following command to get all current migration jobs:

```
curl -X GET \
  http://localhost:8888/shardingscaling/job/list
```

Response:

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

Further query job's specific migration status：

```
curl -X GET \
  http://localhost:8888/shardingscaling/job/progress/1
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
        "status": "RUNNING"
        "syncTaskProgress": [{
            "id": "127.0.0.1-3306-test",
            "status": "SYNCHRONIZE_REALTIME_DATA",
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
                "logPosition": {
                    "filename": "ON.000007",
                    "position": 177532875,
                    "serverId": 0
                }
            }
        }]
   }
}
```

### Stop scaling job

After the data migration is over, we can call the interface to end the job:

```
curl -X POST \
  http://localhost:8888/shardingscaling/job/stop \
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

### Shutdown ShardingSphere-Scaling

```
sh bin/stop.sh
```
