+++
pre = "<b>3.6.5. </b>"
toc = true
title = "performance test"
weight = 5
+++

## Target

Performance test is classified as loss test and promotion test according to its verification target. Insert & update & delete which regarded as an association operation and select which focus on sharding optimization are used to evaluate performance based on the four different scenarios (single route, master slave, master slave & encrypt & sharding, full route).
To achieve the result better, these tests are performed based on 200w data with 20 concurrent threads for 30 minutes.

## Test Scenarios

#### Single Route
Four databases that each contains 1024 tables with id used for database sharding and k used for table sharding are designed for this scenario.
Single route select sql statement is chosen here.

#### Master Slave
One master database and one slave database is designed for this scene. 
Single route select sql statement is chosen here.

#### Master Slave & Encrypt & Sharding
Four databases that each contains 1024 tables with id used for database sharding, k used for table sharding, c encrypted with aes and  pad encrypted with md5 are designed for this scene.
Single route select sql statement is chosen here.

#### Full Route
Four databases that each contains one table are designed for this scene, field 'id' is used for database sharding and 'k' is used for table sharding.
Full route select sql statement is chosen here.

## Testing Environment

#### Table Structure of Database
```shell
+-------+------------+------+-----+---------+----------------+
| Field | Type       | Null | Key | Default | Extra          |
+-------+------------+------+-----+---------+----------------+
| id    | bigint(20) | NO   | PRI | NULL    | auto_increment |
| k     | int(11)    | NO   |     | 0       |                |
| c     | char(120)  | NO   |     |         |                |
| pad   | char(60)   | NO   |     |         |                |
+-------+------------+------+-----+---------+----------------+
```
#### Test Scenarios Configuration
The same configurations are used for Sharding-Jdbc and Sharding-Proxy, while Mysql with one database connected is designed for comparision loss/promotion test.
The details for these scenarios are shown as fellows.

##### Single Route Configuration
```yaml
schemaName: sharding_db

dataSources:
  press_test_0:
    url: jdbc:mysql://***.***.***.***:****/press_test?serverTimezone=UTC&useSSL=false
    username: test
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  press_test_1:
    url: jdbc:mysql://***.***.***.***:****/press_test?serverTimezone=UTC&useSSL=false
    username: test
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  press_test_2:
    url: jdbc:mysql://***.***.***.***:****/press_test?serverTimezone=UTC&useSSL=false
    username: test 
    password: 
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  press_test_3:
    url: jdbc:mysql://***.***.***.***:****/press_test?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
shardingRule:
    tables:
      test:
        actualDataNodes: press_test_${0..3}.test${0..1023}
        tableStrategy:
          inline:
            shardingColumn: k
            algorithmExpression: test${k % 1024}
        keyGenerator:
            type: SNOWFLAKE
            column: id
    defaultDatabaseStrategy:
      inline:
        shardingColumn: id
        algorithmExpression: press_test_${id % 4}
    defaultTableStrategy:
      none:
```
##### Master Slave Configuration
```yaml
schemaName: sharding_db

dataSources:
  master_ds:
    url: jdbc:mysql://***.***.***.***:****/press_test?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  slave_ds_0:
    url: jdbc:mysql://***.***.***.***:****/press_test?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
masterSlaveRule:
  name: ms_ds
  masterDataSourceName: master_ds
  slaveDataSourceNames:
    - slave_ds_0
```
##### Master Slave & Encrypt & Sharding configuration
```yaml
schemaName: sharding_db

dataSources:
  master_ds_0:
    url: jdbc:mysql://***.***.***.***:****/press_test?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  slave_ds_0:
    url: jdbc:mysql://***.***.***.***:****/press_test?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  master_ds_1:
    url: jdbc:mysql://***.***.***.***:****/press_test?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  slave_ds_1:
    url: jdbc:mysql://***.***.***.***:****/press_test?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  master_ds_2:
    url: jdbc:mysql://***.***.***.***:****/press_test?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  slave_ds_2:
    url: jdbc:mysql://***.***.***.***:****/press_test?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  master_ds_3:
    url: jdbc:mysql://***.***.***.***:****/press_test?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  slave_ds_3:
    url: jdbc:mysql://***.***.***.***:****/press_test?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
shardingRule:
  tables:
    test:
      actualDataNodes: ms_ds_${0..3}.test${0..1023}
      databaseStrategy:
        inline:
          shardingColumn: id
          algorithmExpression: ms_ds_${id % 4}
      tableStrategy:
        inline:
          shardingColumn: k
          algorithmExpression: test${k % 1024}
      keyGenerator:
        type: SNOWFLAKE
        column: id
  bindingTables:
    - test
  defaultDataSourceName: master_ds_1
  defaultTableStrategy:
    none:
  masterSlaveRules:
    ms_ds_0:
      masterDataSourceName: master_ds_0
      slaveDataSourceNames:
        - slave_ds_0
      loadBalanceAlgorithmType: ROUND_ROBIN
    ms_ds_1:
      masterDataSourceName: master_ds_1
      slaveDataSourceNames:
        - slave_ds_1
      loadBalanceAlgorithmType: ROUND_ROBIN
    ms_ds_2:
      masterDataSourceName: master_ds_2
      slaveDataSourceNames:
        - slave_ds_2
      loadBalanceAlgorithmType: ROUND_ROBIN
    ms_ds_3:
      masterDataSourceName: master_ds_3
      slaveDataSourceNames:
        - slave_ds_3
      loadBalanceAlgorithmType: ROUND_ROBIN
encryptRule:
  encryptors:
    encryptor_aes:
      type: aes
      props:
        aes.key.value: 123456abc
    encryptor_md5:
      type: md5
  tables:
    sbtest:
      columns:
        c:
          plainColumn: c_plain
          cipherColumn: c_cipher
          encryptor: encryptor_aes
        pad:
          cipherColumn: pad_cipher
          encryptor: encryptor_md5    
```
##### Full Route Configuration
```yaml
schemaName: sharding_db

dataSources:
  press_test_0:
    url: jdbc:mysql://***.***.***.***:****/press_test?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  press_test_1:
    url: jdbc:mysql://***.***.***.***:****/press_test?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  press_test_2:
    url: jdbc:mysql://***.***.***.***:****/press_test?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
  press_test_3:
    url: jdbc:mysql://***.***.***.***:****/press_test?serverTimezone=UTC&useSSL=false
    username: test
    password:
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 200
shardingRule:
  tables:
    test:
      actualDataNodes: press_test_${0..3}.test1
      tableStrategy:
        inline:
          shardingColumn: k
          algorithmExpression: test1
      keyGenerator:
          type: SNOWFLAKE
          column: id
  defaultDatabaseStrategy:
    inline:
      shardingColumn: id
      algorithmExpression: press_test_${id % 4}
  defaultTableStrategy:
    none:  
```
## Test Result Verification
#### SQL Statement 
```shell
Insert+Update+Delete sql statements:
Insert into press_test(k,c,pad) values (1,"###","###")
Update press_test set c="###-#",pad="###-#" where id=**
Delete from press_test where id=**
select sql statement for full route:
select max(id) from test where id%4=1
select sql statement for single route:
select id,k from test where id=1 and k=1
```
#### Jmeter Class
```shell
See: https://github.com/apache/incubator-shardingsphere-benchmark/tree/master/shardingsphere-benchmark
```
#### Compile & Build
```shell
git clone https://github.com/apache/incubator-shardingsphere-benchmark.git
cd incubator-shardingsphere-benchmark/shardingsphere-benchmark
maven clean install
```
#### perform Test
```shell
cp target/shardingsphere-benchmark-1.0-SNAPSHOT-jar-with-dependencies.jar apache-jmeter-4.0/lib/ext
jmeter –n –t test_plan/test.jmx
test.jmx example:https://github.com/apache/incubator-shardingsphere-benchmark/tree/master/report/script/test_plan/test.jmx
```
#### Process Result Data
Make sure the location of result.jtl file is correct.
```shell
sh shardingsphere-benchmark/report/script/gen_report.sh
```