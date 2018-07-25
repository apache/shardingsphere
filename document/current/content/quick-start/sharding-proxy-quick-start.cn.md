+++
pre = "<b>2.2. </b>"
toc = true
title = "Sharding-Proxy"
weight = 2
+++

## 1. 规则配置

编辑`%SHARDING_PROXY_HOME%\conf\config.yaml`。详情请参见[配置手册](/cn/manual/sharding-proxy/configuration/)。
 
> 如需使用自定义配置文件，可在%SHARDING_PROXY_HOME%\conf\下创建自定义配置文件，并通过下方配置进行启动。

## 2. 启动服务

* 使用默认配置项

```sh
${sharding-proxy}\bin\start.sh ${port}
```

* 配置端口

```sh
${sharding-proxy}\bin\start.sh ${port}
```

* 配置端口和配置文件

```sh
${sharding-proxy}\bin\start.sh ${port} ${file_name}
```