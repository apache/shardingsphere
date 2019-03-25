+++
pre = "<b>2.2. </b>"
toc = true
title = "Sharding-Proxy"
weight = 2
+++

## 1. 规则配置

编辑`%SHARDING_PROXY_HOME%\conf\config-xxx.yaml`。详情请参见[配置手册](/cn/manual/sharding-proxy/configuration/)。

编辑`%SHARDING_PROXY_HOME%\conf\server.yaml`。详情请参见[配置手册](/cn/manual/sharding-proxy/configuration/)。
 
## 2. 启动服务

* 使用默认配置项

```sh
${sharding-proxy}\bin\start.sh
```

* 配置端口

```sh
${sharding-proxy}\bin\start.sh ${port}
```
