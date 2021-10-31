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

2. 解压缩 proxy 发布包，修改配置文件 `conf/server.yaml`，这里主要是开启 `scaling` 和 `mode` 配置：
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

3. 启动 ShardingSphere-Proxy：

```
sh bin/start.sh
```

4. 查看 proxy 日志 `logs/stdout.log`，确保启动成功。

## 结束

```
 sh bin/stop.sh
```

## 应用配置项

应用现有配置项如下，相应的配置可在 `conf/server.yaml` 中修改：

| 名称           | 说明                                    | 默认值 |
| -------------- | -------------------------------------- | ------ |
| blockQueueSize | 数据传输通道队列大小                      | 10000  |
| workerThread   | 工作线程池大小，允许同时运行的迁移任务线程数 | 40     |
