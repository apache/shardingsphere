+++
pre = "<b>4.5.1. </b>"
title = "运行部署"
weight = 1
+++

## 部署启动

1. 执行以下命令，编译生成 ShardingSphere-Scaling 二进制包：

```

git clone https://github.com/apache/shardingsphere.git；
cd shardingsphere;
mvn clean install -Prelease;
```

发布包所在目录为：`/shardingsphere-distribution/shardingsphere-scaling-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-scaling-bin.tar.gz`。

2. 解压缩发布包，修改配置文件 `conf/server.yaml`，这里主要修改启动端口，保证不与本机其他端口冲突，其他值保持默认即可：

```
port: 8888
blockQueueSize: 10000
pushTimeout: 1000
workerThread: 30
```

3. 启动 ShardingSphere-Scaling：

```
sh bin/start.sh
```

4. 查看日志 `logs/stdout.log`，确保启动成功。

5. 使用 curl 命令再次确认正常运行。

```
curl -X GET http://localhost:8888/scaling/job/list
```

响应应为：

```
{"success":true,"errorCode":0,"errorMsg":null,"model":[]}
```

## 结束 ShardingSphere-Scaling
   
 ```
 sh bin/stop.sh
 ```
 
## 应用配置项
 
应用现有配置项如下，相应的配置可在 `conf/server.yaml` 中修改：

| 名称           | 说明                                    | 默认值 |
| -------------- | -------------------------------------- | ------ |
| port           | HTTP服务监听端口                         | 8888   |
| blockQueueSize | 数据传输通道队列大小                      | 10000  |
| pushTimeout    | 数据推送超时时间，单位：毫秒               | 1000   |
| workerThread   | 工作线程池大小，允许同时运行的迁移任务线程数 | 30     |
 
