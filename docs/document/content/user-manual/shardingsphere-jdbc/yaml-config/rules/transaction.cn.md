+++
title = "分布式事务"
weight = 3
+++

## 配置项说明

# 使用 LOCAL 事务
```yaml
rules:
  - !TRANSACTION
    defaultType: LOCAL
```

# 使用 XA 事务
```yaml
rules:
  - !TRANSACTION
    defaultType: XA
    providerType: Narayana
```
