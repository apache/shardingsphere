+++
pre = "<b>2.3. </b>"
title = "ShardingSphere-Scaling(Alpha)"
weight = 3
+++

## 1. Rule Configuration

Edit `%SHARDINGSPHERE_SCALING_HOME%/conf/server.yaml`. Please refer to [Configuration Manual](/en/user-manual/shardingsphere-scaling/usage/) for more details.

## 2. Import Dependencies

If the backend database is PostgreSQL, there's no need for additional dependencies.

If the backend database is MySQL, please download [mysql-connector-java-5.1.47.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar) and put it into `%SHARDINGSPHERE_SCALING_HOME%/lib` directory.

## 3. Start Server

```bash
sh %SHARDINGSPHERE_SCALING_HOME%/bin/start.sh
```

## 4. Create Migration Job

Use HTTP interface to manage the migration jobs.

Create migration job:

```bash
curl -X POST \
  http://localhost:8888/scaling/job/start \
  -H 'content-type: application/json' \
  -d '{
   "ruleConfiguration": {
      "sourceDatasource":"dataSources:\n ds_0:\n  dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n  props:\n    jdbcUrl: jdbc:mysql://127.0.0.1:3306/test?serverTimezone=UTC&useSSL=false\n    username: root\n    password: '123456'\n    connectionTimeout: 30000\n    idleTimeout: 60000\n    maxLifetime: 1800000\n    maxPoolSize: 50\n    minPoolSize: 1\n    maintenanceIntervalMilliseconds: 30000\n    readOnly: false\n",
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

Please refer to [Configuration Manual](/en/user-manual/shardingsphere-scaling/usage/) for more details. 
