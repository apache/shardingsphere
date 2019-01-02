grammar SQLServerCreateTable;

import SQLServerKeyword, DataType, Keyword, SQLServerTableBase, SQLServerBase, BaseRule, Symbol;

createTable
    : createTableHeader createTableBody
    ;
    
createTableHeader
    : CREATE TABLE tableName
    ;
    
createTableBody
    : (AS FILETABLE)?
    LP_ createTableDefinition (COMMA createTableDefinition)* (COMMA periodClause)? RP_
    (ON (schemaName LP_ columnName RP_ | fileGroup | STRING))?
    (TEXTIMAGE_ON (fileGroup | STRING))?
    ((FILESTREAM_ON (schemaName) | fileGroup STRING))?
    (WITH LP_ tableOption (COMMA tableOption)* RP_)?
    ;
    
createTableDefinition
    : columnDefinition | computedColumnDefinition | columnSetDefinition | tableConstraint | tableIndex
    ;
    
periodClause
    : PERIOD FOR SYSTEM_TIME LP_ columnName COMMA columnName RP_
    ;
    
tableIndex
    : INDEX indexName
    (
        (CLUSTERED | NONCLUSTERED )? columnList
        | CLUSTERED COLUMNSTORE
        | NONCLUSTERED? (COLUMNSTORE columnList | hashWithBucket) 
        | CLUSTERED COLUMNSTORE (WITH LP_ COMPRESSION_DELAY EQ_ (NUMBER MINUTES?) RP_)?
    )
    (WHERE expr)?
    (WITH LP_ indexOption ( COMMA indexOption)* RP_)? indexOnClause?
    (FILESTREAM_ON (groupName | schemaName | STRING))?
    ;
    
tableOption
    : DATA_COMPRESSION EQ_ (NONE | ROW | PAGE) (ON PARTITIONS LP_ partitionExpressions RP_)?
    | FILETABLE_DIRECTORY EQ_ directoryName 
    | FILETABLE_COLLATE_FILENAME EQ_ (collationName | DATABASE_DEAULT)
    | FILETABLE_PRIMARY_KEY_CONSTRAINT_NAME EQ_ constraintName
    | FILETABLE_STREAMID_UNIQUE_CONSTRAINT_NAME EQ_ constraintName
    | FILETABLE_FULLPATH_UNIQUE_CONSTRAINT_NAME EQ_ constraintName
    | SYSTEM_VERSIONING EQ_ ON (LP_ HISTORY_TABLE EQ_ tableName (COMMA DATA_CONSISTENCY_CHECK EQ_ (ON | OFF))? RP_)?
    | REMOTE_DATA_ARCHIVE EQ_ (ON (LP_ tableStretchOptions (COMMA tableStretchOptions)* RP_)? | OFF LP_ MIGRATION_STATE EQ_ PAUSED RP_)
    | tableOptOption
    | distributionOption
    | dataWareHouseTableOption
    ;
    
tableOptOption
    : (MEMORY_OPTIMIZED EQ_ ON)
    | (DURABILITY EQ_ (SCHEMA_ONLY | SCHEMA_AND_DATA)) 
    | (SYSTEM_VERSIONING EQ_ ON ( LP_ HISTORY_TABLE EQ_ tableName (COMMA DATA_CONSISTENCY_CHECK EQ_ ( ON | OFF ) )? RP_ )?)
    ;
    
distributionOption
    : DISTRIBUTION EQ_ (HASH LP_ columnName RP_ | ROUND_ROBIN | REPLICATE) 
    ;
    
dataWareHouseTableOption
    : CLUSTERED COLUMNSTORE INDEX | HEAP | dataWareHousePartitionOption
    ;
    
dataWareHousePartitionOption
    : (PARTITION LP_ columnName RANGE (LEFT | RIGHT)? FOR VALUES LP_ simpleExpr (COMMA simpleExpr)* RP_ RP_)
    ;
    
tableStretchOptions 
    : (FILTER_PREDICATE EQ_ (NULL | functionCall) COMMA)? MIGRATION_STATE EQ_ (OUTBOUND | INBOUND | PAUSED)
    ;
