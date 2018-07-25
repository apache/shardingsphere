+++
pre = "<b>2.2. </b>"
toc = true
title = "Sharding-Proxy"
weight = 2
+++

## 1. Configure sharding rule

Edit `%SHARDING_PROXY_HOME%\conf\config.yaml`. More details please reference [Configuration Manual](/en/manual/sharding-proxy/configuration/). 

> If you want use self-defined configuration file，please create your configuration file in %SHARDING_PROXY_HOME%\conf\ and then start server as follows.

## 2. Start server

* Use default configuration to start

```sh
${sharding-proxy}\bin\start.sh ${port}
```

* Set port to start

```sh
${sharding-proxy}\bin\start.sh ${port}
```

* Set port and configuration file name to start

```sh
${sharding-proxy}\bin\start.sh ${port} ${file_name}
```