+++
title = "影子库"
weight = 4
+++

## 配置项说明

```yaml
dataSources: #省略数据源配置

rules:
- !SHADOW
  column: # 影子字段名
  sourceDataSourceNames: # 影子前数据库名
     # ...
  shadowDataSourceNames: # 对应的影子库名
     # ... 

props:
  # ...
```
