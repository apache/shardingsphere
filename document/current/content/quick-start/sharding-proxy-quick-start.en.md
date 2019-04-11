+++
pre = "<b>2.2. </b>"
toc = true
title = "Sharding-Proxy"
weight = 2
+++

## 1. Sharding Rule Configuration

Edit `%SHARDING_PROXY_HOME%\conf\config-xxx.yaml`. Please refer to [Configuration Manual](/en/manual/sharding-proxy/configuration/) for more details.

Edit `%SHARDING_PROXY_HOME%\conf\server.yaml`. Please refer to [Configuration Manual](/en/manual/sharding-proxy/configuration/) for more details.

## 2. Start Server

* Use default configuration to start

```sh
${sharding-proxy}\bin\start.sh
```

* Configure the port

```sh
${sharding-proxy}\bin\start.sh ${port}
```
