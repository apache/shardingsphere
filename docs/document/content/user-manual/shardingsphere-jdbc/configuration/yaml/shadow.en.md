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
  shadowMappings: # Mapping relationship between production database and shadow database, key is the name of the production database, and value is the name of the shadow database
    # ...

props:
  # ...
```

