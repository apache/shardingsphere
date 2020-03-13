# shardingsphere-ui backend API

## 1.User authentication related interfaces

### 1.1 User login

POST /api/login

#### Request

| Parameter | Field type | Essential | Describe      |
| --------- | ---------- | --------- | ------------- |
| username  | String     | Y         | User name     |
| password  | String     | Y         | User password |

#### Example

```
curl -X GET http://localhost:8088/api/login
```

#### Response

| Parameter   | Field type | Describe                  |
| ----------- | ---------- | ------------------------- |
| success     | Boolean    | Is the request successful |
| errorCode   | Integer    | Error code                |
| errorMsg    | String     | Wrong description         |
| accessToken | String     | Access credentials        |
| username    | String     | User name                 |

```
{
    "success": true,
    "errorCode": 0,
    "errorMsg": null,
    "model": {"accessToken":"string","username":"string"}
}
```

**Note: accessToken is obtained. All subsequent requests need to carry this voucher in request headers: `Access-Token: accessToken`**

## 2.Relevant interfaces of Registration Center

### 2.1 Get all registry configurations

GET /api/reg-center

#### Example

```
curl -X GET http://localhost:8088/api/reg-center
```

#### Response

| Parameter          | Field type | Describe                                    |
| ------------------ | ---------- | ------------------------------------------- |
| success            | Boolean    | Is the request successful                   |
| errorCode          | Integer    | Error code                                  |
| errorMsg           | String     | Wrong description                           |
| name               | String     | Name of Registration Center                 |
| registryCenterType | String     | Registry type: "zookeeper" / "etcd"         |
| serverLists        | String     | Service address of Registration Center      |
| namespace          | String     | Namespace of the registry                   |
| orchestrationName  | String     | Data governance instance name               |
| digest             | String     | Permission token to connect to the registry |
| activated          | Boolean    | Is it active                                |

```
{
  "success": true,
  "errorCode": 0,
  "errorMsg": null,
  "model": [
    {
      "name": "string",
      "registryCenterType": "Zookeeper",
      "serverLists": "string",
      "namespace": "string",
      "orchestrationName": "string",
      "digest": "string",
      "activated": true
    },
    {
      "name": "string",
      "registryCenterType": "Etcd",
      "serverLists": "string",
      "namespace": "string",
      "orchestrationName": "string",
      "digest": "string",
      "activated": false
    }
  ]
}
```

### 2.2 New registry configuration

POST /api/reg-center

#### Example

```
curl -X POST http://localhost:8088/api/reg-center
```

#### Request

| Parameter          | Field type | Essential | Describe                                    |
| ------------------ | ---------- | --------- | ------------------------------------------- |
| name               | String     | Y         | Name of Registration Center                 |
| registryCenterType | String     | Y         | Registry type: "zookeeper" / "etcd"         |
| serverLists        | String     | Y         | Service address of Registration Center      |
| namespace          | String     | Y         | Namespace of the registry                   |
| orchestrationName  | String     | Y         | Data governance instance name               |
| digest             | String     | N         | Permission token to connect to the registry |

```
{
  "name": "string",
  "namespace": "string",
  "orchestrationName": "string",
  "registryCenterType": "Zookeeper",
  "serverLists": "string"
}
```

#### Response

| Parameter | Field type | Describe                  |
| --------- | ---------- | ------------------------- |
| success   | Boolean    | Is the request successful |
| errorCode | Integer    | Error code                |
| errorMsg  | String     | Wrong description         |

```
{ "success": true, "errorCode": 0, "errorMsg": null, "model": null }
```

### 2.3 Delete registry configuration

DELETE /api/reg-center

#### Request

| Parameter | Field type | Essential | Describe                    |
| --------- | ---------- | --------- | --------------------------- |
| name      | String     | Y         | Name of Registration Center |

```
{"name":"string"}
```

#### Response

| Parameter | Field type | Describe                  |
| --------- | ---------- | ------------------------- |
| success   | Boolean    | Is the request successful |
| errorCode | Integer    | Error code                |
| errorMsg  | String     | Wrong description         |

```
{"success":true,"errorCode":0,"errorMsg":null,"model":null}
```

### 2.4 Activate And Connect TheRegistry

POST /api/reg-center/connect

#### Request

| Parameter | Field type | Essential | Describe                    |
| --------- | ---------- | --------- | --------------------------- |
| name      | String     | Y         | Name of Registration Center |

```
{"name":"string"}
```

#### Response

| Parameter | Field type | Describe                  |
| --------- | ---------- | ------------------------- |
| success   | Boolean    | Is the request successful |
| errorCode | Integer    | Error code                |
| errorMsg  | String     | Wrong description         |

```
{"success":true,"errorCode":0,"errorMsg":null,"model":null}
```

**Note: after the registration center is activated successfully, subsequent configuration and governance operations can be performed. Only one active registry is allowed for the service.**

### 2.5 Get activated registry

GET /api/reg-center/activated

#### Request

无

#### Response

| Parameter          | Field type | 描述                                        |
| ------------------ | ---------- | ------------------------------------------- |
| success            | Boolean    | Is the request successful                   |
| errorCode          | Integer    | Error code                                  |
| errorMsg           | String     | Wrong description                           |
| name               | String     | Name of Registration Center                 |
| registryCenterType | String     | Registry type: "zookeeper" / "etcd"         |
| serverLists        | String     | Service address of Registration Center      |
| namespace          | String     | Namespace of the registry                   |
| orchestrationName  | String     | Data governance instance name               |
| digest             | String     | Permission token to connect to the registry |
| activated          | Boolean    | Is it active                                |

```
{
  "success": true,
  "errorCode": 0,
  "errorMsg": null,
  "model": {
    "name": "string",
    "registryCenterType": "Zookeeper",
    "serverLists": "string",
    "namespace": "string",
    "orchestrationName": "string",
    "digest": "string",
    "activated": true
  }
}
```

## 3.Configuration center schema configuration related interfaces

### 3.1 Get all schema names

`GET /api/schema`

#### Request

无

#### Response

| Parameter | Field type | Describe                  |
| --------- | ---------- | ------------------------- |
| success   | Boolean    | Is the request successful |
| errorCode | Integer    | Error code                |
| errorMsg  | String     | Wrong description         |
| model     | Collection | schema name list          |

```
{
  "success": true,
  "errorCode": 0,
  "errorMsg": null,
  "model": ["sharding_order", "sharding_db", "master_slave_db"]
}
```

### 3.2 Get data fragmentation rules of schema

`GET /api/schema/rule/{schemaName}`

#### Request

| Parameter  | Field type | Essential | Describe    |
| ---------- | ---------- | --------- | ----------- |
| schemaName | String     | Y         | schema name |

#### Response

| Parameter | Field type | Describe                  |
| --------- | ---------- | ------------------------- |
| success   | Boolean    | Is the request successful |
| errorCode | Integer    | Error code                |
| errorMsg  | String     | Wrong description         |
| model     | String     | yaml string               |

```
{"success":true,"errorCode":0,"errorMsg":null,"model":"yaml string"}
```

### 3.3 Modify data fragmentation rules of schema

`PUT /api/schema/rule/{schemaName}`

#### Request

| Parameter  | Field type | Essential | Describe    |
| ---------- | ---------- | --------- | ----------- |
| schemaName | String     | Y         | schema name |
| ruleConfig | String     | Y         | yaml string |

```java
{"ruleConfig":"yaml string"}
```

#### Response

| Parameter | Field type | Describe                  |
| --------- | ---------- | ------------------------- |
| success   | Boolean    | Is the request successful |
| errorCode | Integer    | Error code                |
| errorMsg  | String     | Wrong description         |

```
{"success":true,"errorCode":0,"errorMsg":null,"model":null}
```

### 3.4 Get the data source configuration of schema

`GET /api/schema/datasource/{schemaName}`

#### Request

| Parameter  | Field type | Essential | Describe    |
| ---------- | ---------- | --------- | ----------- |
| schemaName | String     | Y         | schema name |

#### Response

| Parameter | Field type | Describe                  |
| --------- | ---------- | ------------------------- |
| success   | Boolean    | Is the request successful |
| errorCode | Integer    | Error code                |
| errorMsg  | String     | Wrong description         |
| model     | String     | yaml string               |

```
{"success":true,"errorCode":0,"errorMsg":null,"model":"yaml string"}
```

### 3.5 Modify the data source configuration of schema

`PUT /api/schema/datasource/{schemaName}`

#### Request

| Parameter  | Field type | Essential | Describe    |
| ---------- | ---------- | --------- | ----------- |
| schemaName | String     | Y         | schema name |

| Parameter        | Field type | Essential | Describe    |
| ---------------- | ---------- | --------- | ----------- |
| dataSourceConfig | String     | Y         | yaml string |

```
{"dataSourceConfig":"yaml string"}
```

#### Response

| Parameter | Field type | Describe                  |
| --------- | ---------- | ------------------------- |
| success   | Boolean    | Is the request successful |
| errorCode | Integer    | Error code                |
| errorMsg  | String     | Wrong description         |

```
{"success":true,"errorCode":0,"errorMsg":null,"model":null}
```

## 4.Sharding-Proxy Authentication

### 4.1 Get the authentication configuration of sharding proxy

`GET /api/authentication`

#### Request

无

#### Response

| Parameter | Field type | Describe                  |
| --------- | ---------- | ------------------------- |
| success   | Boolean    | Is the request successful |
| errorCode | Integer    | Error code                |
| errorMsg  | String     | Wrong description         |
| username  | String     | Login user name           |
| password  | String     | Login password            |

```
{
  "success": true,
  "errorCode": 0,
  "errorMsg": null,
  "model": { "username": "string", "password": "string" }
}
```

### 4.2 Modify the authentication configuration of sharding proxy

`PUT /api/authentication`

#### Request

| Parameter | Field type | Essential | Describe        |
| --------- | ---------- | --------- | --------------- |
| username  | String     | Y         | Login user name |
| password  | String     | Y         | Login password  |

```
{"username":"string","password":"string"}
```

#### Response

| Parameter | Field type | Describe                  |
| --------- | ---------- | ------------------------- |
| success   | Boolean    | Is the request successful |
| errorCode | Integer    | Error code                |
| errorMsg  | String     | Wrong description         |

```
{"success":true,"errorCode":0,"errorMsg":null,"model":null}
```

## 5.Configuration center configmap configuration related interfaces

### 5.1 Get configmap configuration

`GET /api/config-map`

#### Request

无

#### Response

| Parameter | Field type | Describe                  |
| --------- | ---------- | ------------------------- |
| success   | Boolean    | Is the request successful |
| errorCode | Integer    | Error code                |
| errorMsg  | String     | Wrong description         |
| model     | Map        | config map                |

```
{
  "success": true,
  "errorCode": 0,
  "errorMsg": null,
  "model": {
    "sharding-key1": "sharding-value1",
    "sharding-key2": "sharding-value2",
    "master-slave-key0": "master-slave-value0",
    "master-slave-key1": "master-slave-value1"
  }
}
```

### 5.2 Modify configmap configuration

`PUT /api/config-map`

#### Request

**ConfigMap**

```
{
  "sharding-key1": "sharding-value1",
  "sharding-key2": "sharding-value2",
  "master-slave-key0": "master-slave-value0",
  "master-slave-key1": "master-slave-value1"
}
```

#### Response

| Parameter | Field type | Describe                  |
| --------- | ---------- | ------------------------- |
| success   | Boolean    | Is the request successful |
| errorCode | Integer    | Error code                |
| errorMsg  | String     | Wrong description         |

```
{"success":true,"errorCode":0,"errorMsg":null,"model":null}
```

## 6.Configuration center props configuration related interfaces

### 6.1 Get property configuration

`GET /api/props`

#### Request

无

#### Response

| Parameter | Field type | Describe                  |
| --------- | ---------- | ------------------------- |
| success   | Boolean    | Is the request successful |
| errorCode | Integer    | Error code                |
| errorMsg  | String     | Wrong description         |
| model     | String     | yaml string               |

```
{"success":true,"errorCode":0,"errorMsg":null,"model":"yaml string"}
```

### 6.2 Modify property configuration

`PUT /api/props`

#### Request

| Parameter | Field type | Essential | Describe    |
| --------- | ---------- | --------- | ----------- |
| props     | String     | Y         | yaml string |

```
{"props":"yaml string"}
```

#### Response

| Parameter | Field type | Describe                  |
| --------- | ---------- | ------------------------- |
| success   | Boolean    | Is the request successful |
| errorCode | Integer    | Error code                |
| errorMsg  | String     | Wrong description         |

```
{"success":true,"errorCode":0,"errorMsg":null,"model":null}
```

## 7.Arrange interfaces related to governance

### 7.1 Get running instance information

`GET /api/orchestration/instance`

#### Request

无

#### Response

_Response Body_: (`io.shardingsphere.shardingui.web.response.ResponseResult<java.util.Collection<io.shardingsphere.shardingui.common.dto.InstanceDTO>>`)

| Parameter  | Field type | Describe                  |
| ---------- | ---------- | ------------------------- |
| success    | Boolean    | Is the request successful |
| errorCode  | Integer    | Error code                |
| errorMsg   | String     | Wrong description         |
| serverIp   | String     | IP                        |
| instanceId | String     | Instance ID               |
| enabled    | Boolean    | Instance ID enable status |

```
{
  "success": true,
  "errorCode": 0,
  "errorMsg": null,
  "model": [{ "serverIp": "string", "instanceId": "string", "enabled": true }]
}
```

### 7.2 Modify running instance status

`PUT /api/orchestration/instance`

#### Request

| Parameter  | Field type | Essential | Describe                                                    |
| ---------- | ---------- | --------- | ----------------------------------------------------------- |
| instanceId | String     | Y         | Running instance ID                                         |
| enabled    | Boolean    | Y         | Instance ID enabling status, true: enabled, false: Disabled |

```
{"instanceId":"string","enabled":false}
```

#### Response

| Parameter | Field type | Describe                  |
| --------- | ---------- | ------------------------- |
| success   | Boolean    | Is the request successful |
| errorCode | Integer    | Error code                |
| errorMsg  | String     | Wrong description         |

```java
{"success":true,"errorCode":0,"errorMsg":null,"model":null}
```

### 7.3 Get from library information

`GET /api/orchestration/datasource`

#### Request

无

#### Response

_Response Body_: (`io.shardingsphere.shardingui.web.response.ResponseResult<java.util.Collection<io.shardingsphere.shardingui.common.dto.SlaveDataSourceDTO>>`)

| Parameter            | Field type | Describe                                                          |
| -------------------- | ---------- | ----------------------------------------------------------------- |
| success              | Boolean    | Is the request successful                                         |
| errorCode            | Integer    | Error code                                                        |
| errorMsg             | String     | Wrong description                                                 |
| schema               | String     | Schema name of the slave Library                                  |
| masterDataSourceName | String     | The name of the master library corresponding to the slave Library |
| slaveDataSourceName  | String     | Name from library                                                 |
| enabled              | Boolean    | Enable status from library                                        |

```
{
  "success": true,
  "errorCode": 0,
  "errorMsg": null,
  "model": [
    {
      "schema": "master_slave_db",
      "masterDataSourceName": "master_ds",
      "slaveDataSourceName": "slave_ds_0",
      "enabled": true
    },
    {
      "schema": "master_slave_db",
      "masterDataSourceName": "master_ds",
      "slaveDataSourceName": "slave_ds_1",
      "enabled": true
    }
  ]
}
```

### 7.4 Modify slave status

`PUT /api/orchestration/datasource`

#### Request

| Parameter            | Field type | Essential                                                         | Describe |
| -------------------- | ---------- | ----------------------------------------------------------------- | -------- |
| schema               | String     | Schema name of the slave Library                                  |
| masterDataSourceName | String     | The name of the master library corresponding to the slave Library |
| slaveDataSourceName  | String     | Name from library                                                 |
| enabled              | Boolean    | Enable status from library                                        |

```
{
  "schema": "master_slave_db",
  "masterDataSourceName": "master_ds",
  "slaveDataSourceName": "slave_ds_0",
  "enabled": true
}
```

#### Response

| Parameter | Field type | Describe                  |
| --------- | ---------- | ------------------------- |
| success   | Boolean    | Is the request successful |
| errorCode | Integer    | Error code                |
| errorMsg  | String     | Wrong description         |

```
{"success":true,"errorCode":0,"errorMsg":null,"model":null}
```

## shardingscaling

### Get sharding scaling service

GET /api/shardingscaling

#### Example

```

curl -X GET http://localhost:8088/api/shardingscaling

```

#### Response

```

{
"success": true,
"errorCode": 0,
"errorMsg": null,
"model": {
"serviceName": "scaling",
"serviceType": "ShardingScaling",
"serviceUrl": "localhost:8084"
}
}

OR

{
"success": false,
"errorCode": 0,
"errorMsg": "No configured sharding scaling services",
"model": null
}

```

### Add sharding scaling service

POST /api/shardingscaling

#### Body

| Parameter   | Describe                      |
| ----------- | ----------------------------- |
| serviceName | user defined name of service  |
| serviceType | Fixed value `ShardingScaling` |
| serviceUrl   | user defined url of service   |

#### Example

```

curl -X POST \
 http://localhost:8088/api/shardingscaling \
 -H 'content-type: application/json' \
 -d '{
"serviceName": "scaling",
"serviceType": "ShardingScaling",
"serviceUrl": "localhost:8084"
}'

```

#### Response

```

{
"success": true,
"errorCode": 0,
"errorMsg": null,
"model": null
}

```

### Delete sharding scaling service

DELETE /api/shardingscaling

#### Example

```

curl -X DELETE http://localhost:8088/api/shardingscaling

```

#### Response

```

{
"success": true,
"errorCode": 0,
"errorMsg": null,
"model": null
}

```

### Start scaling job

POST /api/shardingscaling/job/start

#### Body

| Parameter                                         | Describe                                        |
| ------------------------------------------------- | ----------------------------------------------- |
| ruleConfiguration.sourceDatasource                | source sharding proxy data source configuration |
| ruleConfiguration.sourceRule                      | source sharding proxy table rule configuration  |
| ruleConfiguration.destinationDataSources.name     | destination sharding proxy name                 |
| ruleConfiguration.destinationDataSources.url      | destination sharding proxy jdbc url             |
| ruleConfiguration.destinationDataSources.username | destination sharding proxy username             |
| ruleConfiguration.destinationDataSources.password | destination sharding proxy password             |
| jobConfiguration.concurrency                      | sync task proposed concurrency                  |

#### Example

```

curl -X POST \
 http://localhost:8088/api/shardingscaling/job/start \
 -H 'content-type: application/json' \
 -d '{
"ruleConfiguration": {
"sourceDatasource": "ds*0: !!YamlDataSourceConfiguration\n dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n properties:\n jdbcUrl: jdbc:mysql://127.0.0.1:3306/test?serverTimezone=UTC&useSSL=false\n username: root\n password: '\''123456'\''\n connectionTimeout: 30000\n idleTimeout: 60000\n maxLifetime: 1800000\n maxPoolSize: 50\n minPoolSize: 1\n maintenanceIntervalMilliseconds: 30000\n readOnly: false\n",
"sourceRule": "defaultDatabaseStrategy:\n inline:\n algorithmExpression: ds*\${user_id % 2}\n shardingColumn: user_id\ntables:\n t1:\n actualDataNodes: ds_0.t1\n keyGenerator:\n column: order_id\n type: SNOWFLAKE\n logicTable: t1\n tableStrategy:\n inline:\n algorithmExpression: t1\n shardingColumn: order_id\n t2:\n actualDataNodes: ds_0.t2\n keyGenerator:\n column: order_item_id\n type: SNOWFLAKE\n logicTable: t2\n tableStrategy:\n inline:\n algorithmExpression: t2\n shardingColumn: order_id\n",
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

#### Response

```

{
"success": true,
"errorCode": 0,
"errorMsg": null,
"model": null
}

```

### Get scaling progress

GET /api/shardingscaling/job/progress/{jobId}

#### Example

```

curl -X GET \
 http://localhost:8088/api/shardingscaling/job/progress/1

```

#### Response

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

### List scaling jobs

GET /api/shardingscaling/job/list

#### Example

```

curl -X GET \
 http://localhost:8088/api/shardingscaling/job/list

```

#### Response

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

### Stop scaling job

POST /api/shardingscaling/job/stop

#### Body

| Parameter | Describe |
| --------- | -------- |
| jobId     | job id   |

#### Example

```

curl -X POST \
 http://localhost:8088/api/shardingscaling/job/stop \
 -H 'content-type: application/json' \
 -d '{
"jobId":1
}'

```

#### Response

```

{
"success": true,
"errorCode": 0,
"errorMsg": null,
"model": null
}

```

```

```
