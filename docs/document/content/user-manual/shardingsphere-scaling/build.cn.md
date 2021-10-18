+++
pre = "<b>5.4.1. </b>"
title = "运行部署"
weight = 1
+++

## 部署启动

1. 执行以下命令，编译生成 ShardingSphere-Scaling 和 ShardingSphere-Proxy 二进制包：

```
git clone --depth 1 https://github.com/apache/shardingsphere.git
cd shardingsphere
mvn clean install -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Drat.skip=true -Djacoco.skip=true -DskipITs -DskipTests -Prelease
```

发布包：
- /shardingsphere-distribution/shardingsphere-scaling-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-scaling-bin.tar.gz
- /shardingsphere-distribution/shardingsphere-proxy-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-proxy-bin.tar.gz

或者通过[下载页面]( https://shardingsphere.apache.org/document/current/cn/downloads/ )获取安装包。

2. 解压缩 scaling 发布包，修改配置文件 `conf/server.yaml`，这里主要修改启动端口，保证不与本机其他端口冲突，同时修改断点续传服务（可选）地址即可：

```yaml
scaling:
  port: 8888
  blockQueueSize: 10000
  workerThread: 30

mode:
  type: Cluster
  repository:
    type: ZooKeeper
    props:
      namespace: governance_ds
      server-lists: localhost:2181
```

3. 启动 ShardingSphere-Scaling：

```
sh bin/server_start.sh
```

4. 查看 scaling 日志 `logs/stdout.log`，确保启动成功。

5. 使用 curl 命令再次确认 scaling 正常运行。

```
curl -X GET http://localhost:8888/scaling/job/list
```

响应应为：

```
{"success":true,"errorCode":0,"errorMsg":null,"model":[]}
```

6. 解压缩 proxy 发布包，修改配置文件 `conf/server.yaml`，这里主要是开启 `scaling` 和 `mode` 配置：
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

7. 启动 ShardingSphere-Proxy：

```
sh bin/start.sh
```

8. 查看 proxy 日志 `logs/stdout.log`，确保启动成功。

## 结束

### ShardingSphere-Scaling

```
 sh bin/server_stop.sh
```

### ShardingSphere-Proxy

```
 sh bin/stop.sh
```

## 应用配置项

### ShardingSphere-Scaling

应用现有配置项如下，相应的配置可在 `conf/server.yaml` 中修改：

| 名称           | 说明                                    | 默认值 |
| -------------- | -------------------------------------- | ------ |
| port           | HTTP服务监听端口                         | 8888   |
| blockQueueSize | 数据传输通道队列大小                      | 10000  |
| workerThread   | 工作线程池大小，允许同时运行的迁移任务线程数 | 30     |
 
