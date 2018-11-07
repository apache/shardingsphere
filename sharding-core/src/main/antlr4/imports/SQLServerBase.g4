grammar SQLServerBase;

import SQLServerKeyword,Keyword,Symbol,BaseRule,DataType;

ID
    : (LEFT_BRACKET? DOUBLE_QUOTA? [a-zA-Z_$#][a-zA-Z0-9_$#]* DOUBLE_QUOTA? RIGHT_BRACKET? DOT)* DOT*
    (LEFT_BRACKET? DOUBLE_QUOTA? [a-zA-Z_$#][a-zA-Z0-9_$#]* DOUBLE_QUOTA? RIGHT_BRACKET?)
    |[a-zA-Z0-9_$]+ DOT ASTERISK
    ;
    
dataType
    : typeName   
    (
          dataTypeLength
        | LP_ MAX RIGHT_PAREN
        | LP_ (CONTENT | DOCUMENT)? xmlSchemaCollection RIGHT_PAREN
    )?   
    ;
    
privateExprOfDb
    : windowedFunction
    | atTimeZoneExpr
    | castExpr
    | convertExpr
    ;

atTimeZoneExpr
    : ID (WITH TIME ZONE)? STRING
    ;
    
castExpr
    : CAST LP_ expr AS dataType (LP_  NUMBER RIGHT_PAREN )? RIGHT_PAREN  
    ;
    
convertExpr
    : CONVERT ( dataType (LP_  NUMBER RIGHT_PAREN )? COMMA expr (COMMA NUMBER)?)
    ;
    
windowedFunction
     : functionCall overClause
     ;
     
 overClause
    : OVER 
    LP_     
          partitionByClause?
          orderByClause?  
          rowRangeClause? 
    RIGHT_PAREN 
    ;
    
partitionByClause
    : PARTITION BY expr (COMMA expr)*  
    ;
    
orderByClause   
   : ORDER BY orderByExpr (COMMA orderByExpr)*
   ;
  
orderByExpr
    : expr (COLLATE collationName)? (ASC | DESC)? 
    ;
    
rowRangeClause 
    : (ROWS | RANGE) windowFrameExtent
    ;
 
 windowFrameExtent
    : windowFramePreceding
    | windowFrameBetween 
    ; 
    
windowFrameBetween
    : BETWEEN windowFrameBound AND windowFrameBound  
    ;
    
windowFrameBound  
    : windowFramePreceding 
    | windowFrameFollowing 
    ;
    
windowFramePreceding  
    : (UNBOUNDED PRECEDING)  
    | NUMBER PRECEDING  
    | CURRENT ROW  
    ; 

windowFrameFollowing
    : UNBOUNDED FOLLOWING  
    | NUMBER FOLLOWING  
    | CURRENT ROW  
    ;

columnList
    : LP_ columnNameWithSort (COMMA columnNameWithSort)* RIGHT_PAREN 
    ;
    
columnNameWithSort
    : columnName ( ASC | DESC )?
    ;

indexOption
    : FILLFACTOR EQ_ NUMBER
    | eqOnOffOption
    | ((COMPRESSION_DELAY | MAX_DURATION) eqTime)
    | MAXDOP EQ_ NUMBER
    | compressionOption onPartitionClause?
    ;
    
compressionOption
    : DATA_COMPRESSION EQ_ ( NONE | ROW | PAGE | COLUMNSTORE | COLUMNSTORE_ARCHIVE)
    ;
    
eqTime
    : EQ_ NUMBER (MINUTES)?
    ;
    
eqOnOffOption
    : (
       PAD_INDEX
      | SORT_IN_TEMPDB
      | IGNORE_DUP_KEY
      | STATISTICS_NORECOMPUTE
      | STATISTICS_INCREMENTAL
      | DROP_EXISTING
      | ONLINE
      | RESUMABLE
      | ALLOW_ROW_LOCKS
      | ALLOW_PAGE_LOCKS
      | COMPRESSION_DELAY
      | SORT_IN_TEMPDB
    )
    eqOnOff 
    ;
 
eqOnOff
    : EQ_ ( ON | OFF )
    ;

onPartitionClause
    : ON PARTITIONS LP_ partitionExpressions RIGHT_PAREN
    ;

partitionExpressions
    : partitionExpression (COMMA partitionExpression)*
    ;

partitionExpression
    : NUMBER 
    | numberRange
    ;
    
numberRange   
    : NUMBER TO NUMBER  
    ;

lowPriorityLockWait
    : WAIT_AT_LOW_PRIORITY LP_ MAX_DURATION EQ_ NUMBER ( MINUTES )? COMMA
    ABORT_AFTER_WAIT EQ_ ( NONE | SELF | BLOCKERS ) RIGHT_PAREN
    ;

onLowPriorLockWait
    : ON (LP_ lowPriorityLockWait RIGHT_PAREN)?
    ;