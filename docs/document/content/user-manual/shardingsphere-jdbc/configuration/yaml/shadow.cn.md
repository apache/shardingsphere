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
  shadowMappings: # 生产库与影子库的映射关系，key 为生产库名，value 为逻辑库名
    # ...

props:
  # ...
```
