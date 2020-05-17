+++
pre = "<b>2.3. </b>"
title = "ShardingSphere-Scaling(Alpha)"
weight = 3
+++

## 快速开始

### 部署启动

#### 1. 执行以下命令，编译生成ShardingSphere-Scaling二进制包：

```

git clone https://github.com/apache/shardingsphere.git；
cd shardingsphere;
mvn clean install -Prelease;
```

发布包所在目录为：`/shardingsphere-distribution/shardingsphere-scaling-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-scaling-bin.tar.gz`。

#### 2. 解压缩发布包，修改配置文件`conf/server.yaml`，这里主要修改启动端口，保证不与本机其他端口冲突，其他值保持默认即可：

```
port: 8888
blockQueueSize: 10000
pushTimeout: 1000
workerThread: 30
```

#### 3. 启动ShardingSphere-Scaling：

```
sh bin/start.sh
```

**注意**：

如果后端连接MySQL数据库，需要下载[MySQL Connector/J](https://cdn.mysql.com//Downloads/Connector-J/mysql-connector-java-5.1.47.tar.gz)，
解压缩后，将mysql-connector-java-5.1.47.jar拷贝到${shardingsphere-scaling}\lib目录。

#### 4. 查看日志`logs/stdout.log`，确保启动成功。

### 创建迁移任务

ShardingSphere-Scaling提供相应的HTTP接口来管理迁移任务，部署启动成功后，我们可以调用相应的接口来启动迁移任务。

创建迁移任务：

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

注意：上述需要修改`ruleConfiguration.sourceDatasource`和`ruleConfiguration.sourceRule`，分别为源端ShardingSphere数据源和数据表规则相关配置；

以及`ruleConfiguration.destinationDataSources`中目标端ShardingSphere-Proxy的相关信息。

返回如下信息，表示任务创建成功：

```
{
   "success": true,
   "errorCode": 0,
   "errorMsg": null,
   "model": null
}
```

需要注意的是，目前ShardingSphere-Scaling任务创建成功后，便会自动运行，进行数据的迁移。

### 查询任务进度

执行如下命令获取当前所有迁移任务：

```
curl -X GET \
  http://localhost:8888/shardingscaling/job/list
```

返回示例如下：

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

进一步查询任务具体迁移状态：

```
curl -X GET \
  http://localhost:8888/shardingscaling/job/progress/1
```

返回任务详细信息如下：

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

### 结束任务

数据迁移完成后，我们可以调用接口结束任务：

```
curl -X POST \
  http://localhost:8888/shardingscaling/job/stop \
  -H 'content-type: application/json' \
  -d '{
   "jobId":1
}'
```

返回如下信息表示任务成功结束：

```
{
   "success": true,
   "errorCode": 0,
   "errorMsg": null,
   "model": null
}
```

### 结束ShardingSphere-Scaling

```
sh bin/stop.sh
```
