grammar SQLServerAlterTable;

import SQLServerKeyword, DataType, Keyword, SQLServerTableBase, SQLServerBase, BaseRule, Symbol;

alterTable
    : alterTableOp
    (
        alterColumn
        | addColumn
        | alterDrop
        | alterCheckConstraint
        | alterTrigger
        | alterSwitch
        | alterSet
        | tableOption
        | REBUILD
    )
    ;
    
alterTableOp
    : ALTER TABLE tableName
    ;
    
alterColumn
    : modifyColumn
    ;
    
modifyColumn
    : alterColumnOp dataType (COLLATE collationName)? (NULL | NOT NULL)? SPARSE?
    ;
    
alterColumnOp
    : ALTER COLUMN columnName
    ;
    
addColumn
    : (WITH (CHECK | NOCHECK))?
    ADD
    (
        alterColumnAddOption (COMMA alterColumnAddOption)*
        | (columnNameGeneratedClause COMMA periodClause| periodClause COMMA columnNameGeneratedClause)
    )
    ;
    
periodClause
    : PERIOD FOR SYSTEM_TIME LP_ columnName COMMA columnName RP_
    ;
    
alterColumnAddOption
    : columnDefinition
    | computedColumnDefinition
    | columnSetDefinition
    | tableConstraint
    | tableIndex
    | constraintForColumn
    ;
    
constraintForColumn
    : (CONSTRAINT constraintName)? DEFAULT simpleExpr FOR columnName
    ;
    
columnNameWithSortsWithParen
    : LP_ columnNameWithSort (COMMA columnNameWithSort)* RP_ 
    ;
    
columnNameWithSort
    : columnName (ASC | DESC)?
    ;
    
columnIndex 
    : indexWithName (CLUSTERED | NONCLUSTERED)? HASH withBucket
    ;
    
columnNameGeneratedClause:
    columnNameGenerated DEFAULT simpleExpr (WITH VALUES)? COMMA columnNameGenerated
    ;
    
columnNameGenerated
    : columnName typeName GENERATED ALWAYS AS ROW (START | END)?
    HIDDEN_? (NOT NULL)? (CONSTRAINT constraintName)?
    ;
    
alterDrop
    : DROP 
    (
        alterTableDropConstraint
        | dropColumn
        | dropIndexDef
        | PERIOD FOR SYSTEM_TIME
    )
    ;
    
alterTableDropConstraint
    : CONSTRAINT? (IF EXISTS)? dropConstraintName (COMMA dropConstraintName)*
    ;
    
dropConstraintName
    : constraintName dropConstraintWithClause?
    ;
    
dropConstraintWithClause
    : WITH LP_ dropConstraintOption (COMMA dropConstraintOption)* RP_
    ;
    
dropConstraintOption
    : (
          MAXDOP EQ_ NUMBER
          | ONLINE EQ_ ( ON | OFF )
          | MOVE TO (schemaName LP_ columnName RP_ | fileGroup | STRING)
    )
    ;
    
dropColumn
    : COLUMN (IF EXISTS)? columnNames
    ;
    
dropIndexDef
    : INDEX (IF EXISTS)? indexName (COMMA indexName)*
    ;
    
alterCheckConstraint 
    : WITH? (CHECK | NOCHECK) CONSTRAINT (ALL | (constraintName (COMMA constraintName)*))
    ;
    
alterTrigger 
    : (ENABLE| DISABLE) TRIGGER (ALL | (triggerName ( COMMA triggerName)*))
    ;
    
alterSwitch
    : SWITCH ( PARTITION expr )? TO tableName (PARTITION expr)? (WITH LP_ lowPriorityLockWait RP_ )?
    ;
    
alterSet
    : SET LP_ (setFileStreamClause | setSystemVersionClause) RP_ 
    ;
    
setFileStreamClause
    : FILESTREAM_ON EQ_ ( schemaName | fileGroup | STRING )
    ;
    
setSystemVersionClause
    : SYSTEM_VERSIONING EQ_ (OFF | alterSetOnClause)
    ;
    
alterSetOnClause
    : ON
    (
        LP_ (HISTORY_TABLE EQ_ tableName)?
        (COMMA? DATA_CONSISTENCY_CHECK EQ_ ( ON | OFF ))? 
        (COMMA? HISTORY_RETENTION_PERIOD EQ_ (INFINITE | (NUMBER (DAY | DAYS | WEEK | WEEKS | MONTH | MONTHS | YEAR | YEARS ))))?
        RP_
    )?
    ;
    
tableIndex
    : indexWithName (indexNonClusterClause | indexClusterClause)
    ;
    
indexWithName
    : INDEX indexName
    ;
    
indexNonClusterClause
    : NONCLUSTERED
    (hashWithBucket | (columnNameWithSortsWithParen indexOnClause?)) 
    ;
    
indexOnClause
    : ON groupName | DEFAULT
    ;
    
indexClusterClause
    : CLUSTERED COLUMNSTORE (WITH COMPRESSION_DELAY EQ_ NUMBER MINUTES?)? indexOnClause?
    ;
    
tableOption
    : SET LP_ LOCK_ESCALATION EQ_ (AUTO | TABLE | DISABLE) RP_
    | MEMORY_OPTIMIZED EQ_ ON
    | DURABILITY EQ_ (SCHEMA_ONLY | SCHEMA_AND_DATA) 
    | SYSTEM_VERSIONING EQ_ ON (LP_ HISTORY_TABLE EQ_ tableName
        (COMMA DATA_CONSISTENCY_CHECK EQ_ (ON | OFF))? RP_ )?
    ;
