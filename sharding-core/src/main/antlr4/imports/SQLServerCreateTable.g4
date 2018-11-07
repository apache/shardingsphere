grammar SQLServerCreateTable;

import SQLServerKeyword, DataType, Keyword, SQLServerTableBase,SQLServerBase,BaseRule,Symbol;

createTable
    : createTableHeader createTableBody
    ;

createTableHeader
    : CREATE TABLE tableName
    ;
    
createTableBody
    : (AS FILETABLE)?  
    LP_ 
        createTableDefinition (COMMA createTableDefinition)*
        (COMMA periodClause)?  
    RIGHT_PAREN  
    (ON 
    	(schemaName LP_  columnName RIGHT_PAREN   
           | fileGroup   
           | STRING 
        ) 
    )?   
    (TEXTIMAGE_ON (fileGroup | STRING) )?   
     ((FILESTREAM_ON (schemaName) 
       | fileGroup   
         STRING) 
     )?  
    (WITH LP_  tableOption (COMMA tableOption)*  RIGHT_PAREN)?  
    ;
    
createTableDefinition
    : columnDefinition  
    | computedColumnDefinition    
    | columnSetDefinition   
    | tableConstraint   
    | tableIndex
    ;
 
periodClause
    : PERIOD FOR SYSTEM_TIME LP_  columnName   
    COMMA columnName RIGHT_PAREN
    ;

tableIndex
    : INDEX indexName 
    (
          (CLUSTERED | NONCLUSTERED )? columnList 
         | CLUSTERED COLUMNSTORE 
         | NONCLUSTERED? (COLUMNSTORE columnList | hashWithBucket) 
         |CLUSTERED COLUMNSTORE (WITH LP_  COMPRESSION_DELAY EQ_ (NUMBER MINUTES?) RIGHT_PAREN)?
    ) 
    (WHERE expr)?
    (WITH LP_ indexOption ( COMMA indexOption)* RIGHT_PAREN)?   
    indexOnClause?   
    (FILESTREAM_ON ( groupName | schemaName | STRING ))?  
    ;
    
tableOption  
    : DATA_COMPRESSION EQ_ ( NONE | ROW | PAGE ) (ON PARTITIONS LP_  partitionExpressions RIGHT_PAREN )?
    | FILETABLE_DIRECTORY EQ_ directoryName 
    | FILETABLE_COLLATE_FILENAME EQ_ ( collationName | DATABASE_DEAULT )
    | FILETABLE_PRIMARY_KEY_CONSTRAINT_NAME EQ_ constraintName
    | FILETABLE_STREAMID_UNIQUE_CONSTRAINT_NAME  EQ_ constraintName  
    | FILETABLE_FULLPATH_UNIQUE_CONSTRAINT_NAME  EQ_ constraintName 
    |SYSTEM_VERSIONING EQ_ ON (LP_  HISTORY_TABLE EQ_ tableName   
         (COMMA DATA_CONSISTENCY_CHECK EQ_ ( ON | OFF ) )? RIGHT_PAREN)?
    | REMOTE_DATA_ARCHIVE EQ_   
        (
           ON (LP_  tableStretchOptions (COMMA tableStretchOptions)* RIGHT_PAREN )?
         | OFF LP_  MIGRATION_STATE EQ_ PAUSED RIGHT_PAREN 
        )
    |tableOptOption
    |distributionOption
    |dataWareHouseTableOption     
    ;

tableOptOption 
    : (MEMORY_OPTIMIZED EQ_ ON)   
    | (DURABILITY EQ_ (SCHEMA_ONLY | SCHEMA_AND_DATA)) 
    | (SYSTEM_VERSIONING EQ_ ON ( LP_  HISTORY_TABLE EQ_ tableName  
        (COMMA DATA_CONSISTENCY_CHECK EQ_ ( ON | OFF ) )? RIGHT_PAREN )?)   
    ;

distributionOption     
    : DISTRIBUTION EQ_ 
    (
          HASH LP_ columnName RIGHT_PAREN
        | ROUND_ROBIN 
        | REPLICATE
     ) 
    ; 
    
dataWareHouseTableOption
    : (CLUSTERED COLUMNSTORE INDEX)
    | HEAP   
    | dataWareHousePartitionOption
    ;
 
 dataWareHousePartitionOption
     : (PARTITION LP_ columnName  RANGE (LEFT | RIGHT)?  
        FOR VALUES LP_  simpleExpr (COMMA simpleExpr)* RIGHT_PAREN  RIGHT_PAREN)
     ; 
     
tableStretchOptions 
     : (FILTER_PREDICATE EQ_ ( NULL | functionCall ) COMMA )?  
     MIGRATION_STATE EQ_ ( OUTBOUND | INBOUND | PAUSED )  
     ;