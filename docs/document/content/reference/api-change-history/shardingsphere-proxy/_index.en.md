+++
pre = "<b>8.8.2. </b>"
title = "ShardingSphere-Proxy"
weight = 8
chapter = true
+++

## 5.0.0-beta

### Configuration Item Explanation

```yaml

rules:
  - !AUTHORITY
    users:
      - root@%:root
      - sharding@:sharding
    provider:
      type: ALL_PRIVILEGES_PERMITTED

props:
  max-connections-size-per-query: 1
  kernel-executor-size: 16  # Infinite by default.
  proxy-frontend-flush-threshold: 128  # The default value is 128.
  proxy-opentracing-enabled: false
  proxy-hint-enabled: false
  query-with-cipher-column: true
  sql-show: false
  check-table-metadata-enabled: false
  lock-wait-timeout-milliseconds: 50000 # The maximum time to wait for a lock
  ```

## 4.1.1

### Configuration Item Explanation

```yaml

orchestration:
  name: orchestration_ds
  overwrite: true
  registry:
    serverLists: localhost:2181
    namespace: orchestration

authentication:
  username: root
  password: root

props:
  max.connections.size.per.query: 1
  acceptor.size: 16  # The default value is available processors count * 2.
  executor.size: 16  # Infinite by default.
  proxy.frontend.flush.threshold: 128  # The default value is 128.
    # LOCAL: Proxy will run with LOCAL transaction.
```
