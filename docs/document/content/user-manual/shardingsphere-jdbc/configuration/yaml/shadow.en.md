+++
title = "Shadow DB"
weight = 4
+++

## Configuration Item Explanation

```yaml
dataSources: # Omit data source configuration

rules:
- !SHADOW
  column: # Shadow column name
  sourceDataSourceNames: # Source Data Source names
     # ...
  shadowDataSourceNames: # Shadow Data Source names
     # ... 

props:
  # ...
```

