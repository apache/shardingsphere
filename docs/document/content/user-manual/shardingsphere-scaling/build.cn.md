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

> Scaling还是实验性质的功能，建议使用master分支最新版本，点击此处[下载每日构建版本]( https://github.com/apache/shardingsphere#nightly-builds )

2. 解压缩 proxy 发布包，修改配置文件`conf/config-sharding.yaml`。详情请参见[proxy启动手册](/cn/user-manual/shardingsphere-proxy/startup/bin/)。

3. 修改配置文件 `conf/server.yaml`，详情请参见[模式配置](/cn/user-manual/shardingsphere-jdbc/yaml-config/mode/)。

目前 `mode` 必须是 `Cluster`，需要提前启动对应的注册中心。

配置示例：
```yaml
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

4. 开启 scaling

方法1：修改配置文件 `conf/config-sharding.yaml` 的 `scalingName` 和 `scaling` 部分。

配置项说明：
```yaml
rules:
- !SHARDING
  # 忽略的配置
  
  scalingName: # 启用的弹性伸缩配置名称
  scaling:
    <scaling-action-config-name> (+):
      input: # 数据读取配置。如果不配置则部分参数默认生效。
        workerThread: # 从源端摄取全量数据的线程池大小。如果不配置则使用默认值。
        batchSize: # 一次查询操作返回的最大记录数。如果不配置则使用默认值。
        rateLimiter: # 限流算法。如果不配置则不限流。
          type: # 算法类型。可选项：QPS
          props: # 算法属性
            qps: # qps属性。适用算法类型：QPS
      output: # 数据写入配置。如果不配置则部分参数默认生效。
        workerThread: # 数据写入到目标端的线程池大小。如果不配置则使用默认值。
        batchSize: # 一次批量写入操作的最大记录数。如果不配置则使用默认值。
        rateLimiter: # 限流算法。如果不配置则不限流。
          type: # 算法类型。可选项：TPS
          props: # 算法属性
            tps: # tps属性。适用算法类型：TPS
      streamChannel: # 数据通道，连接生产者和消费者，用于 input 和 output 环节。如果不配置则默认使用 MEMORY 类型
        type: # 算法类型。可选项：MEMORY
        props: # 算法属性
          block-queue-size: # 属性：阻塞队列大小
      completionDetector: # 作业是否接近完成检测算法。如果不配置则无法自动进行后续步骤，可以通过 DistSQL 手动操作。
        type: # 算法类型。可选项：IDLE
        props: # 算法属性
          incremental-task-idle-minute-threshold: # 如果增量同步任务不再活动超过一定时间，那么可以认为增量同步任务接近完成。适用算法类型：IDLE
      dataConsistencyChecker: # 数据一致性校验算法。如果不配置则跳过这个步骤。
        type: # 算法类型。可选项：DATA_MATCH, CRC32_MATCH
        props: # 算法属性
          chunk-size: # 一次查询操作返回的最大记录数
```

配置示例：
```yaml
rules:
- !SHARDING
  # 忽略的配置
  
  scalingName: default_scaling
  scaling:
    default_scaling:
      input:
        workerThread: 40
        batchSize: 1000
        rateLimiter:
          type: QPS
          props:
            qps: 50
      output:
        workerThread: 40
        batchSize: 1000
        rateLimiter:
          type: TPS
          props:
            tps: 2000
      streamChannel:
        type: MEMORY
        props:
          block-queue-size: 10000
      completionDetector:
        type: IDLE
        props:
          incremental-task-idle-minute-threshold: 30
      dataConsistencyChecker:
        type: DATA_MATCH
        props:
          chunk-size: 1000
```

以上的 `completionDetector`，`dataConsistencyChecker` 都可以通过实现 SPI 自定义。可以参考现有实现，详情请参见[开发者手册#弹性伸缩](/cn/dev-manual/scaling/)。

方法2：通过 DistSQL 配置 scaling

创建 scaling 配置示例：
```sql
CREATE SHARDING SCALING RULE default_scaling (
INPUT(
  WORKER_THREAD=40,
  BATCH_SIZE=1000,
  RATE_LIMITER(TYPE(NAME=QPS, PROPERTIES("qps"=50)))
),
OUTPUT(
  WORKER_THREAD=40,
  BATCH_SIZE=1000,
  RATE_LIMITER(TYPE(NAME=TPS, PROPERTIES("tps"=2000)))
),
STREAM_CHANNEL(TYPE(NAME=MEMORY, PROPERTIES("block-queue-size"=10000))),
COMPLETION_DETECTOR(TYPE(NAME=IDLE, PROPERTIES("incremental-task-idle-minute-threshold"=3))),
DATA_CONSISTENCY_CHECKER(TYPE(NAME=DATA_MATCH, PROPERTIES("chunk-size"=1000)))
);
```

详情请参见[RDL#数据分片](/cn/user-manual/shardingsphere-proxy/distsql/syntax/rdl/rule-definition/sharding/)。

5. 启动 ShardingSphere-Proxy：

```
sh bin/start.sh
```

6. 查看 proxy 日志 `logs/stdout.log`，看到日志中出现：

```
[INFO ] [main] o.a.s.p.frontend.ShardingSphereProxy - ShardingSphere-Proxy start success
```

确认启动成功。

## 结束

```
 sh bin/stop.sh
```
