grammar SQLServerAlterTable;
import SQLServerKeyword, DataType, Keyword, SQLServerBase,BaseRule,Symbol;

alterTable:
    alterTableOp
    (alterColumn
    |addColumn
    |alterDrop
    |alterCheckConstraint
    |alterTrigger
    |alterTracking
    |alterSwitch
    |alterSet
    |alterRebuild
    |mergeOrSplit
    |tableOption
    |fileTableOption
    |stretchConfiguration)
    ;
    
alterTableOp:
    ALTER TABLE tableName
    ;
    
alterColumn:
     ALTER COLUMN columnName   
    (   
       dataType   
        (
            (COLLATE collationName)   
           ( NULL | NOT NULL ) 
            |( SPARSE )
        )*
      | ((ADD | DROP)  ( ROWGUIDCOL | PERSISTED | NOT FOR REPLICATION | SPARSE | HIDDEN_)) 
      | ((ADD | DROP) MASKED (WITH LEFT_PAREN FUNCTION EQ_OR_ASSIGN STRING RIGHT_PAREN )?) 
    )   
    (WITH LEFT_PAREN ONLINE EQ_OR_ASSIGN ON | OFF RIGHT_PAREN)?  
    ;

addColumn:
    (WITH (CHECK | NOCHECK))?
    ADD   
    (
       (alterColumnAddOption (COMMA  alterColumnAddOption)*) 
      |(
          (columnNameGeneratedClause COMMA periodClause)
        |(periodClause COMMA columnNameGeneratedClause)
        )
    )  
    ;

periodClause:
    PERIOD FOR SYSTEM_TIME LEFT_PAREN  columnName   
    COMMA columnName RIGHT_PAREN
    ;
    
alterColumnAddOption:
    columnDefinition
    |constraintForColumn
    | computedColumnDefinition  
    | tableConstraint   
    | columnSetDefinition
    | tableIndex
    | columnIndex
    ;
      
constraintForColumn:
    columnConstraint FOR columnName
    ;

columnDefinition:
    columnName dataType  
    columnConstraint?
    collateClause?
    (NOT? NULL)?
    ;

columnConstraint :   
    (CONSTRAINT constraintName )?   
    DEFAULT simpleExpr
    ;

computedColumnDefinition :  
    columnName AS expr   
    (PERSISTED( NOT NULL )?)?  
    columnConstraint?   
    ;

tableConstraint:  
    (CONSTRAINT constraintName )?   
    (
        tablePrimaryConstraint
        | tableForeignKeyConstraint   
        | (CHECK expr)
    )
    ;

tablePrimaryConstraint: 
    primaryKeyUnique  (CLUSTERED | NONCLUSTERED)? 
    (  
        columnNameWithSortsWithParen
        |hashWithBucket
    )
    ;

hashWithBucket:
    HASH columnList withBucket
    ;
    
withBucket:
    WITH LEFT_PAREN BUCKET_COUNT EQ_OR_ASSIGN NUMBER RIGHT_PAREN
    ;

primaryKeyUnique:
    (PRIMARY KEY) 
    | UNIQUE
    ;

columnNameWithSortsWithParen:
    LEFT_PAREN columnNameWithSort (COMMA columnNameWithSort)* RIGHT_PAREN 
    ;
    
columnNameWithSort:
    columnName ( ASC | DESC )?
    ;

tableForeignKeyConstraint:
    (FOREIGN KEY)? columnList
    REFERENCES tableName columnList  
    ;

columnSetDefinition :  
    columnSetName ID COLUMN_SET FOR ALL_SPARSE_COLUMNS  
    ;

columnIndex:   
    INDEX indexName 
    ( CLUSTERED | NONCLUSTERED )?  
    HASH withBucket  
    ;
        
columnNameGeneratedClause:
    columnNameGenerated
    DEFAULT simpleExpr (WITH VALUES)? COMMA  
    columnNameGenerated
    ;
    
columnNameGenerated:
    columnName typeName GENERATED ALWAYS AS ROW (START | END)?   
    HIDDEN_? (NOT NULL)? (CONSTRAINT constraintName)?
    ;
 
alterDrop:
    DROP 
    (
        |alterTableDropConstraint
        |alterTableDropColumn
        |alterTableDropIndex
        |(PERIOD FOR SYSTEM_TIME)
    )
    ;
    
alterTableDropConstraint:
    CONSTRAINT? (IF EXISTS)?  
    dropConstraintName (COMMA dropConstraintName)*
    ;
    
dropConstraintName:
    constraintName dropConstraintWithClause?
    ;

dropConstraintWithClause:
    WITH  LEFT_PAREN dropConstraintOption ( COMMA dropConstraintOption)* RIGHT_PAREN   
    ;

dropConstraintOption:    
    (   
        (MAXDOP EQ_OR_ASSIGN NUMBER ) 
      | (ONLINE EQ_OR_ASSIGN ( ON | OFF ))  
      | (MOVE TO   
         ( schemaName LEFT_PAREN columnName RIGHT_PAREN | fileGroup | STRING ) ) 
    )
    ;  
 
 alterTableDropColumn:
    COLUMN (IF EXISTS)? columnNames
    ; 

alterTableDropIndex:
    INDEX (IF EXISTS)?  
    indexName (COMMA indexName)*
    ;
    
alterCheckConstraint:  
    WITH? (CHECK | NOCHECK) CONSTRAINT   
    (ALL | (constraintName (COMMA constraintName)*))  
    ;
    
alterTrigger: 
    (ENABLE| DISABLE) TRIGGER   
    (ALL | (triggerName ( COMMA triggerName)*))  
    ;
    
alterTracking:
    (ENABLE | DISABLE) CHANGE_TRACKING   
    (WITH LEFT_PAREN TRACK_COLUMNS_UPDATED EQ_OR_ASSIGN (ON | OFF) RIGHT_PAREN )?
    ;
    
alterSwitch:
    SWITCH ( PARTITION expr )?  
    TO tableName   
    ( PARTITION expr)?  
    ( WITH LEFT_PAREN lowPriorityLockWait RIGHT_PAREN )?  
    ;
    
alterSet:    
    SET   
    LEFT_PAREN  
    (
        setFileStreamClause
        |setSystemVersionClause
    ) 
    RIGHT_PAREN 
    ; 

setFileStreamClause:
    FILESTREAM_ON EQ_OR_ASSIGN ( schemaName | fileGroup | STRING )
    ;
    
setSystemVersionClause:
    SYSTEM_VERSIONING EQ_OR_ASSIGN   
    (   
        OFF   
       |alterSetOnClause 
    ) 
    ;
    
alterSetOnClause:
    ON   
    (
      LEFT_PAREN 
        (HISTORY_TABLE EQ_OR_ASSIGN tableName)?  
        (COMMA? DATA_CONSISTENCY_CHECK EQ_OR_ASSIGN ( ON | OFF ))? 
        (COMMA? HISTORY_RETENTION_PERIOD EQ_OR_ASSIGN (INFINITE | (NUMBER (DAY | DAYS | WEEK | WEEKS | MONTH | MONTHS | YEAR | YEARS ))))?  
      RIGHT_PAREN  
    )?
    ;
      
alterRebuild:
    REBUILD   
    (
         (PARTITION EQ_OR_ASSIGN ALL)? WITH LEFT_PAREN rebuildOption ( COMMA rebuildOption)* RIGHT_PAREN
         |(PARTITION EQ_OR_ASSIGN numberRange)? (WITH LEFT_PAREN singlePartitionRebuildOption (COMMA singlePartitionRebuildOption)* RIGHT_PAREN )?  
     )
    ;    
    
fileTableOption :
    (ENABLE | DISABLE) FILETABLE_NAMESPACE
    |SET LEFT_PAREN FILETABLE_DIRECTORY EQ_OR_ASSIGN directoryName RIGHT_PAREN 
    ;  

stretchConfiguration :  
    SET 
    LEFT_PAREN  
        REMOTE_DATA_ARCHIVE   
        (  
            (EQ_OR_ASSIGN ON LEFT_PAREN  tableStretchOptions  RIGHT_PAREN)  
            | (EQ_OR_ASSIGN OFF_WITHOUT_DATA_RECOVERY LEFT_PAREN MIGRATION_STATE EQ_OR_ASSIGN PAUSED RIGHT_PAREN)  
            | (LEFT_PAREN tableStretchOptions (COMMA)? RIGHT_PAREN)  
        )  
    RIGHT_PAREN  
    ;

tableStretchOptions:  
     ( FILTER_PREDICATE EQ_OR_ASSIGN ( NULL | functionCall ) COMMA )?  
       MIGRATION_STATE EQ_OR_ASSIGN ( OUTBOUND | INBOUND | PAUSED )  
    ;

singlePartitionRebuildOption :  
      (SORT_IN_TEMPDB EQ_OR_ASSIGN ( ON | OFF ))  
    | (MAXDOP EQ_OR_ASSIGN NUMBER)  
    | compressionOption 
    | (ONLINE EQ_OR_ASSIGN (OFF |onLowPriorLockWait))
    ;

rebuildOption:   
    | indexOption
    | (ONLINE EQ_OR_ASSIGN (OFF | onLowPriorLockWait))   
    ;
  
alterIndex:
    ALTER INDEX indexName   
    typeName  REBUILD 
    (NONCLUSTERED? WITH LEFT_PAREN BUCKET_COUNT EQ_OR_ASSIGN NUMBER RIGHT_PAREN)?
    ;

tableIndex: 
    INDEX indexName 
    (
      indexNonClusterClause 
      |indexClusterClause
    )
    ;

indexNonClusterClause:
    NONCLUSTERED
    (hashWithBucket | (columnNameWithSortsWithParen indexOnClause?)) 
    ;
    
indexOnClause:
    (ON groupName) 
    | DEFAULT
    ;

indexClusterClause:
    CLUSTERED COLUMNSTORE 
    (WITH COMPRESSION_DELAY EQ_OR_ASSIGN NUMBER MINUTES?)?
    indexOnClause?
    ;

tableOption:  
    (SET LEFT_PAREN LOCK_ESCALATION EQ_OR_ASSIGN (AUTO | TABLE | DISABLE) RIGHT_PAREN)   
    |(MEMORY_OPTIMIZED EQ_OR_ASSIGN ON)   
    | (DURABILITY EQ_OR_ASSIGN (SCHEMA_ONLY | SCHEMA_AND_DATA)) 
    | (SYSTEM_VERSIONING EQ_OR_ASSIGN ON ( LEFT_PAREN  HISTORY_TABLE EQ_OR_ASSIGN tableName  
        (COMMA DATA_CONSISTENCY_CHECK EQ_OR_ASSIGN ( ON | OFF ) )? RIGHT_PAREN )?)   
    ;
  
mergeOrSplit:
    (SPLIT | MERGE) RANGE LEFT_PAREN simpleExpr RIGHT_PAREN   
    ;
 
 numberRange:
    NUMBER (TO NUMBER)?
    ;