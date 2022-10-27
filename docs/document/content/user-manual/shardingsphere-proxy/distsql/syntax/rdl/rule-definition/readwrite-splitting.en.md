+++
title = "Readwrite-Splitting"
weight = 3
+++

## Syntax

```sql
CREATE READWRITE_SPLITTING RULE readwriteSplittingRuleDefinition [, readwriteSplittingRuleDefinition] ...

ALTER READWRITE_SPLITTING RULE readwriteSplittingRuleDefinition [, readwriteSplittingRuleDefinition] ...

DROP READWRITE_SPLITTING RULE ruleName [, ruleName] ...

readwriteSplittingRuleDefinition:
    ruleName ([staticReadwriteSplittingRuleDefinition | dynamicReadwriteSplittingRuleDefinition] 
              [, loadBalancerDefinition])

staticReadwriteSplittingRuleDefinition:
    WRITE_STORAGE_UNIT=storageUnitName, READ_STORAGE_UNITS(storageUnitName [, storageUnitName] ... )

dynamicReadwriteSplittingRuleDefinition:
    AUTO_AWARE_RESOURCE=resourceName [, WRITE_DATA_SOURCE_QUERY_ENABLED=writeDataSourceQueryEnabled]

loadBalancerDefinition:
    TYPE(NAME=loadBalancerType [, PROPERTIES([algorithmProperties] )] )

algorithmProperties:
    algorithmProperty [, algorithmProperty] ...

algorithmProperty:
    key=value

writeDataSourceQueryEnabled:
    TRUE | FALSE
```

### Parameters Explained
| name                        | DateType   | Description                                                                                                 |
|:----------------------------|:-----------|:------------------------------------------------------------------------------------------------------------|
| ruleName                    | IDENTIFIER | Rule name                                                                                                   |
| storageUnitName             | IDENTIFIER | Registered data source name                                                                                 |
| autoAwareResourceName       | IDENTIFIER | Database discovery logic data source name                                                                   |
| writeDataSourceQueryEnabled | BOOLEAN    | All read data source are offline, write data source whether the data source is responsible for read traffic |
| loadBalancerType            | STRING     | Load balancing algorithm type                                                                               |

### Notes

- Support the creation of static readwrite-splitting rules and dynamic readwrite-splitting rules
- Dynamic readwrite-splitting rules rely on database discovery rules
- `loadBalancerType` specifies the load balancing algorithm type, please refer to [Load Balance Algorithm](/en/user-manual/common-config/builtin-algorithm/load-balance/)
- Duplicate `ruleName` will not be created

## Example

```sql
// Static
CREATE READWRITE_SPLITTING RULE ms_group_0 (
WRITE_STORAGE_UNIT=write_ds,
READ_STORAGE_UNITS(read_ds_0,read_ds_1),
TYPE(NAME="random")
);

// Dynamic
CREATE READWRITE_SPLITTING RULE ms_group_1 (
AUTO_AWARE_RESOURCE=group_0,
WRITE_DATA_SOURCE_QUERY_ENABLED=false,
TYPE(NAME="random",PROPERTIES(write_ds=2,read_ds_0=2,read_ds_1=2,read_ds_2=1))
);

ALTER READWRITE_SPLITTING RULE ms_group_1 (
WRITE_STORAGE_UNIT=write_ds,
READ_STORAGE_UNITS(read_ds_0,read_ds_1,read_ds_2),
TYPE(NAME="random",PROPERTIES(write_ds=2,read_ds_0=2,read_ds_1=2,read_ds_2=1))
);

DROP READWRITE_SPLITTING RULE ms_group_1;
```
