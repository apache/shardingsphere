+++
title = "Distributed Transaction"
weight = 3
+++

## Configuration Item Explanation

LOCAL Model
```yaml
rules:
  - !TRANSACTION
    defaultType: LOCAL
```

XA Model
```yaml
rules:
  - !TRANSACTION
    defaultType: XA
    providerType: Narayana
```
