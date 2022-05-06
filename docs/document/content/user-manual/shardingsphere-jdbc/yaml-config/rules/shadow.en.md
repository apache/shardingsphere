+++
title = "Shadow DB"
weight = 5
+++

## Configuration Item Explanation

```yaml
rules:
  - !SHADOW
    dataSources:
      shadowDataSource:
        sourceDataSourceName: # Production data source name
        shadowDataSourceName: # Shadow data source name
    tables:
      <table-name>:
        dataSourceNames: # Shadow table location shadow data source names
          - <shadow-data-source> 
        shadowAlgorithmNames: # Shadow table location shadow algorithm names
          - <shadow-algorithm-name>
    defaultShadowAlgorithmName: # Default shadow algorithm name
    shadowAlgorithms:
      <shadow-algorithm-name> (+): # Shadow algorithm name
        type: # Shadow algorithm type
        props: # Shadow algorithm property configuration
        # ...
```
