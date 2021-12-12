+++
title = "运行部署"
weight = 1
+++

## 部署启动

1. 执行以下命令，编译生成 ShardingSphere-Proxy 二进制包：

```
git clone --depth 1 https://github.com/apache/shardingsphere.git
cd shardingsphere
mvn clean install -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Drat.skip=true -Djacoco.skip=true -DskipITs -DskipTests -Prelease
```

发布包：
- /shardingsphere-distribution/shardingsphere-proxy-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-proxy-bin.tar.gz

或者通过[下载页面]( https://shardingsphere.apache.org/document/current/cn/downloads/ )获取安装包。

> Scaling还是实验性质的功能，在持续进化中，建议使用master分支最新版本，点击此处[下载最新版本]( http://117.48.121.24:8080/Proxy/ )

2. 解压缩 proxy发布包，修改配置文件`conf/sharding.yaml`，配置`schemaName`，`dataSources`，`rules`里的`shardingAlgorithms`和`keyGenerators`，以下示例可供参考：
```yaml
schemaName: sharding_db

dataSources:
 ds_0:
   url: jdbc:mysql://127.0.0.1:3308/account?serverTimezone=UTC&useSSL=false
   username: root
   password: root
   connectionTimeoutMilliseconds: 30000
   idleTimeoutMilliseconds: 60000
   maxLifetimeMilliseconds: 1800000
   maxPoolSize: 50
   minPoolSize: 1

rules:
- !SHARDING
 tables:
   user:
     actualDataNodes: ds_0.user
     databaseStrategy:
       standard:
         shardingColumn: id
         shardingAlgorithmName: database_inline
     tableStrategy:
       standard:
         shardingColumn: id
         shardingAlgorithmName: user_inline
     keyGenerateStrategy:
       column: id
       keyGeneratorName: snowflake
 
 shardingAlgorithms:
   database_inline:
     type: INLINE
     props:
       algorithm-expression: ds_0
   user_inline:
     type: INLINE
     props:
       algorithm-expression: user_${id % 16}
 
 keyGenerators:
   snowflake:
     type: SNOWFLAKE
     props:
       worker-id: 123
```

3. 解压缩 proxy 发布包，修改配置文件 `conf/server.yaml`，这里主要是开启 `scaling` 和 `mode` 配置：
如果配置的Mode是Cluster，需要提前启动对应的配置中心。


```yaml
scaling:
  blockQueueSize: 10000
  workerThread: 40
  clusterAutoSwitchAlgorithm:
    type: IDLE
    props:
      incremental-task-idle-minute-threshold: 30
  dataConsistencyCheckAlgorithm:
    type: DEFAULT

mode:
  type: Cluster
  repository:
    type: ZooKeeper
    props:
      namespace: governance_ds
      server-lists: localhost:2181
      retryIntervalMilliseconds: 500
      timeToLiveSeconds: 60
      maxRetries: 3
      operationTimeoutMilliseconds: 500
  overwrite: false
```

打开`clusterAutoSwitchAlgorithm`配置代表开启自动检测任务是否完成及切换配置，目前系统提供了`IDLE`类型实现。

打开`dataConsistencyCheckAlgorithm`配置设置数据校验算法，关闭该配置系统将不进行数据校验。目前系统提供了`DEFAULT`类型实现，`DEFAULT`算法目前支持的数据库：`MySQL`。其他数据库还不能打开这个配置项，相关支持还在开发中。

可以通过`ScalingClusterAutoSwitchAlgorithm`接口自定义一个SPI实现，通过`ScalingDataConsistencyCheckAlgorithm`接口自定义一个SPI实现。详情请参见[开发者手册#弹性伸缩](/cn/dev-manual/scaling/)。

overwrite字段含义为：控制配置文件是否覆盖 zk 元数据，一般可在测试时使用。

3. 启动 ShardingSphere-Proxy：

```
sh bin/start.sh
```

4. 查看 proxy 日志 `logs/stdout.log`，看到日志中出现：
```
[INFO ] 2021-12-08 15:28:48.336 [main] o.a.s.p.frontend.ShardingSphereProxy - ShardingSphere-Proxy start success
```

确认启动成功



## 结束

```
 sh bin/stop.sh
```

## 应用配置项

应用现有配置项如下，相应的配置可在 `conf/server.yaml` 中修改：
| 一级Key | 二级Key                       | 三级Key                                                      | 说明                                                         | 默认值    |
| ------- | ----------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | --------- |
| Scaling |                               | blockQueueSize                                               | 数据传输通道队列大小                                         | 10000     |
|         |                               | workerThread                                                 | 工作线程池大小，允许同时运行的迁移任务线程数                 | 40        |
|         | clusterAutoSwitchAlgorithm    | type                                                         | 开启自动检测任务是否完成及切换配置，目前系统提供了IDLE类型实现 | -         |
|         |                               | props:incremental-task-idle-minute-threshold                 | 增量同步空闲时间最大值，超过该值，进入下一状态               | 30 (分钟) |
|         | dataConsistencyCheckAlgorithm | type                                                         | 配置设置数据校验算法，关闭该配置系统将不进行数据校验。目前系统提供了DEFAULT类型实现，DEFAULT算法目前支持的数据库：MySQL。其他数据库还不能打开这个配置项，相关支持还在开发中可以通过ScalingClusterAutoSwitchAlgorithm接口自定义一个SPI实现，通过ScalingDataConsistencyCheckAlgorithm接口自定义一个SPI实现。详情请参见[开发者手册#弹性伸缩](https://shardingsphere.apache.org/document/current/cn/dev-manual/scaling/)。 | -         |
| mode    | type                          | Cluster                                                      |                                                              | -         |
|         | repository                    | type、props | 注册中心，当前支持Zookeeper，Etcd                            | -         |
|         | overwrite                     | -                                                            | 控制配置文件是否覆盖 zk 元数据，一般可在测试时使用。         | false     |
