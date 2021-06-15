+++
title = "读写分离"
weight = 3
+++

## 定义

```sql
SHOW READWRITE_SPLITTING RULES [FROM schemaName]               
```

## 说明

| 列                      | 说明                                  |
| ----------------------- | ------------------------------------ |
| name                    | 规则名称                               |
| autoAwareDataSourceName | 自动发现数据源名称（配置动态读写分离规则显示）|
| writeDataSourceName     | 写数据源名称                            |
| readDataSourceNames     | 读数据源名称列表                         |
| loadBalancerType        | 负载均衡算法类型                         |
| loadBalancerProps       | 负载均衡算法参数                         |
