+++
pre = "<b>2.2. </b>"
toc = true
title = "Sharding-Proxy"
weight = 2
+++

## 1. Configure sharding rule

Edit `%SHARDING_PROXY_HOME%\conf\config-xxx.yaml`. More details please reference [Configuration Manual](/en/manual/sharding-proxy/configuration/).

Edit `%SHARDING_PROXY_HOME%\conf\server.yaml`. More details please reference [Configuration Manual](/en/manual/sharding-proxy/configuration/). 

## 2. Start server

* Use default configuration to start

```sh
${sharding-proxy}\bin\start.sh
```

* Set port to start

```sh
${sharding-proxy}\bin\start.sh ${port}
```
