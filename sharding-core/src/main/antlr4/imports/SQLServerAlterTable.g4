grammar SQLServerAlterTable;

import SQLServerKeyword, DataType, Keyword, SQLServerTableBase,SQLServerBase,BaseRule,Symbol;

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
       (alterColumnAddOption (COMMA alterColumnAddOption)*) 
      |(
          (columnNameGeneratedClause COMMA periodClause)
        |(periodClause COMMA columnNameGeneratedClause)
        )
    )  
    ;

periodClause
    : PERIOD FOR SYSTEM_TIME LEFT_PAREN columnName   
    COMMA columnName RIGHT_PAREN
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
    : LEFT_PAREN columnNameWithSort (COMMA columnNameWithSort)* RIGHT_PAREN 
    ;
    
columnNameWithSort
    : columnName (ASC | DESC)?
    ;

columnIndex 
    : indexWithName
    (CLUSTERED | NONCLUSTERED)?
    HASH withBucket  
    ;
        
columnNameGeneratedClause:
    columnNameGenerated
    DEFAULT simpleExpr (WITH VALUES)? COMMA  
    columnNameGenerated
    ;
    
columnNameGenerated
    : columnName typeName GENERATED ALWAYS AS ROW (START | END)?   
    HIDDEN_? (NOT NULL)? (CONSTRAINT constraintName)?
    ;
 
alterDrop
    : DROP 
    (
        | alterTableDropConstraint
        | dropColumn
        | alterDropIndex
        | PERIOD FOR SYSTEM_TIME
    )
    ;
    
alterTableDropConstraint
    : CONSTRAINT? (IF EXISTS)?  
    dropConstraintName (COMMA dropConstraintName)*
    ;
    
dropConstraintName
    : constraintName dropConstraintWithClause?
    ;

dropConstraintWithClause
    : WITH  LEFT_PAREN dropConstraintOption (COMMA dropConstraintOption)* RIGHT_PAREN   
    ;

dropConstraintOption   
    : (   
          MAXDOP EQ_OR_ASSIGN NUMBER
          | ONLINE EQ_OR_ASSIGN ( ON | OFF )
          | MOVE TO (schemaName LEFT_PAREN columnName RIGHT_PAREN | fileGroup | STRING)
    )
    ;  
 
dropColumn
    : COLUMN (IF EXISTS)? columnNames
    ; 

alterDropIndex:
    INDEX (IF EXISTS)?  
    indexName (COMMA indexName)*
    ;
    
alterCheckConstraint 
    : WITH? (CHECK | NOCHECK) CONSTRAINT   
    (ALL | (constraintName (COMMA constraintName)*))  
    ;
    
alterTrigger: 
    (ENABLE| DISABLE) TRIGGER   
    (ALL | (triggerName ( COMMA triggerName)*))  
    ;
    
alterSwitch
    : SWITCH ( PARTITION expr )? TO tableName   
    (PARTITION expr)?  
    (WITH LEFT_PAREN lowPriorityLockWait RIGHT_PAREN )?  
    ;
    
alterSet    
    : SET   
    LEFT_PAREN  
    (
        setFileStreamClause
        | setSystemVersionClause
    ) 
    RIGHT_PAREN 
    ; 

setFileStreamClause
    : FILESTREAM_ON EQ_OR_ASSIGN ( schemaName | fileGroup | STRING )
    ;
    
setSystemVersionClause
    : SYSTEM_VERSIONING EQ_OR_ASSIGN   
    (   
        OFF   
       | alterSetOnClause
    ) 
    ;
    
alterSetOnClause
    : ON   
    (
      LEFT_PAREN 
        (HISTORY_TABLE EQ_OR_ASSIGN tableName)?  
        (COMMA? DATA_CONSISTENCY_CHECK EQ_OR_ASSIGN ( ON | OFF ))? 
        (COMMA? HISTORY_RETENTION_PERIOD EQ_OR_ASSIGN (INFINITE | (NUMBER (DAY | DAYS | WEEK | WEEKS | MONTH | MONTHS | YEAR | YEARS ))))?  
      RIGHT_PAREN  
    )?
    ;
      
    

tableIndex
    : indexWithName 
    (
        indexNonClusterClause
        | indexClusterClause
    )
    ;

indexWithName
	: INDEX indexName
	;

indexNonClusterClause
    : NONCLUSTERED
    (hashWithBucket | (columnNameWithSortsWithParen indexOnClause?)) 
    ;
    
indexOnClause
    : ON groupName
    | DEFAULT
    ;

indexClusterClause
    : CLUSTERED COLUMNSTORE 
    (WITH COMPRESSION_DELAY EQ_OR_ASSIGN NUMBER MINUTES?)?
    indexOnClause?
    ;

tableOption
    : SET LEFT_PAREN LOCK_ESCALATION EQ_OR_ASSIGN (AUTO | TABLE | DISABLE) RIGHT_PAREN   
    | MEMORY_OPTIMIZED EQ_OR_ASSIGN ON  
    | DURABILITY EQ_OR_ASSIGN (SCHEMA_ONLY | SCHEMA_AND_DATA) 
    | SYSTEM_VERSIONING EQ_OR_ASSIGN ON ( LEFT_PAREN  HISTORY_TABLE EQ_OR_ASSIGN tableName  
        (COMMA DATA_CONSISTENCY_CHECK EQ_OR_ASSIGN ( ON | OFF ) )? RIGHT_PAREN )?  
    ;
  
