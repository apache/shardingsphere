grammar SQLServerTableBase;

import SQLServerKeyword,Keyword,Symbol,SQLServerBase,BaseRule,DataType;

columnDefinition
    : columnName dataType
    columnDefinitionOption*
    (columnConstraint(COMMA columnConstraint)*)?
    columnIndex?
    ;

columnDefinitionOption
    : FILESTREAM  
    | COLLATE collationName
    | SPARSE  
    | MASKED WITH LP_  FUNCTION EQ_ STRING RIGHT_PAREN
    | (CONSTRAINT constraintName)? DEFAULT expr
    | IDENTITY (LP_  NUMBER COMMA NUMBER RIGHT_PAREN )?
    | NOT FOR REPLICATION
    | GENERATED ALWAYS AS ROW (START | END) HIDDEN_?
    | NOT? NULL
    | ROWGUIDCOL 
    | ENCRYPTED WITH 
       LP_  
         COLUMN_ENCRYPTION_KEY EQ_ keyName COMMA  
         ENCRYPTION_TYPE EQ_ ( DETERMINISTIC | RANDOMIZED ) COMMA   
         ALGORITHM EQ_ STRING 
       RIGHT_PAREN 
    | columnConstraint (COMMA columnConstraint)*
    | columnIndex
    ;    
    

columnConstraint  
    : (CONSTRAINT constraintName)?   
    (     primaryKeyConstraint
        | columnForeignKeyConstraint 
        | checkConstraint
    )
    ;

primaryKeyConstraint
    : (primaryKey | UNIQUE) 
    (diskTablePrimaryKeyConstraintOption | memoryTablePrimaryKeyConstraintOption)
    ;

diskTablePrimaryKeyConstraintOption
    : (CLUSTERED | NONCLUSTERED)? 
    primaryKeyWithClause?
    primaryKeyOnClause?
    ;    

columnForeignKeyConstraint
    : (FOREIGN KEY)?  
    REFERENCES tableName LP_  columnName RIGHT_PAREN   
    foreignKeyOnAction*
    ;

foreignKeyOnAction
    : ON DELETE foreignKeyOn
    | ON UPDATE foreignKeyOn
    | NOT FOR REPLICATION
    ;       
    
foreignKeyOn
    : NO ACTION 
    | CASCADE 
    | SET NULL 
    | SET DEFAULT 
    ;
    
memoryTablePrimaryKeyConstraintOption
    : CLUSTERED withBucket?
    ;
    
hashWithBucket
    : HASH columnList withBucket
    ;
    
withBucket
    : WITH LP_ BUCKET_COUNT EQ_ NUMBER RIGHT_PAREN
    ;
    
primaryKeyWithClause
    : WITH 
    ((FILLFACTOR EQ_ NUMBER)    
     | (LP_  indexOption (COMMA indexOption)* RIGHT_PAREN) 
    )
    ;

primaryKeyOnClause
    : onSchemaColumn 
    | onFileGroup
    | onString
    ;
 
 onSchemaColumn
 	: ON schemaName LP_  columnName RIGHT_PAREN
 	;
 	
 onFileGroup
 	: ON fileGroup
 	;

onString
 	: ON STRING
 	; 

checkConstraint:
    CHECK(NOT FOR REPLICATION)? LP_  expr RIGHT_PAREN  
    ;
    
columnIndex
    : INDEX indexName ( CLUSTERED | NONCLUSTERED )?  
    ( WITH LP_  indexOption (COMMA indexOption)*  RIGHT_PAREN )?  
    indexOnClause?   
    ( FILESTREAM_ON ( fileGroup | schemaName | STRING ) )?  
    ;
    
indexOnClause
	: onSchemaColumn 
    | onFileGroup
    | onDefault
    ;
    	
onDefault
 	: ON DEFAULT
 	; 	
 	
tableConstraint 
    : (CONSTRAINT constraintName)?   
    (
          tablePrimaryConstraint
        | tableForeignKeyConstraint   
        | checkConstraint 
    )
    ;
    
tablePrimaryConstraint
    : primaryKeyUnique   
    (diskTablePrimaryConstraintOption | memoryTablePrimaryConstraintOption)
    ;

primaryKeyUnique
    : primaryKey 
    | UNIQUE
    ;
    
diskTablePrimaryConstraintOption    
    : (CLUSTERED | NONCLUSTERED)?   
    columnList
    primaryKeyWithClause?
    primaryKeyOnClause?
    ;

memoryTablePrimaryConstraintOption    
    : NONCLUSTERED
    (columnList 
           | hashWithBucket)
    ;
 
tableForeignKeyConstraint
    : (FOREIGN KEY)? columnList
    REFERENCES tableName columnList  
    foreignKeyOnAction*
    ;
    
computedColumnDefinition  
    : columnName AS expr   
    (PERSISTED( NOT NULL )?)?  
    columnConstraint?   
    ;
    
columnSetDefinition 
    : columnSetName ID COLUMN_SET FOR ALL_SPARSE_COLUMNS  
    ;