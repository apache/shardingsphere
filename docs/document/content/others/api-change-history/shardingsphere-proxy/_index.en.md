+++
pre = "<b>7.2.2. </b>"
title = "ShardingSphere-Proxy"
weight = 2
chapter = true
+++

## 5.0.0-beta

#### Configuration Item Explanation

```yaml

rules:
  - !AUTHORITY
    users:
      - root@%:root
      - sharding@:sharding
    provider:
      type: NATIVE

props:
  max-connections-size-per-query: 1
  executor-size: 16  # Infinite by default.
  proxy-frontend-flush-threshold: 128  # The default value is 128.
    # LOCAL: Proxy will run with LOCAL transaction.
    # XA: Proxy will run with XA transaction.
    # BASE: Proxy will run with B.A.S.E transaction.
  proxy-transaction-type: LOCAL
  xa-transaction-manager-type: Atomikos
  proxy-opentracing-enabled: false
  proxy-hint-enabled: false
  query-with-cipher-column: true
  sql-show: false
  check-table-metadata-enabled: false
  lock-wait-timeout-milliseconds: 50000 # The maximum time to wait for a lock
  ```

## 4.1.1

#### Configuration Item Explanation

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