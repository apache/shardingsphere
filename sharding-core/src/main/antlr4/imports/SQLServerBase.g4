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
        | LEFT_PAREN MAX RIGHT_PAREN
        | LEFT_PAREN (CONTENT | DOCUMENT)? xmlSchemaCollection RIGHT_PAREN
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
    : CAST LEFT_PAREN expr AS dataType (LEFT_PAREN  NUMBER RIGHT_PAREN )? RIGHT_PAREN  
    ;
    
convertExpr
    : CONVERT ( dataType (LEFT_PAREN  NUMBER RIGHT_PAREN )? COMMA expr (COMMA NUMBER)?)
    ;
    
windowedFunction
     : functionCall overClause
     ;
     
 overClause
    : OVER 
    LEFT_PAREN     
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
    : LEFT_PAREN columnNameWithSort (COMMA columnNameWithSort)* RIGHT_PAREN 
    ;
    
columnNameWithSort
    : columnName ( ASC | DESC )?
    ;

indexOption
    : FILLFACTOR EQ_OR_ASSIGN NUMBER
    | eqOnOffOption
    | ((COMPRESSION_DELAY | MAX_DURATION) eqTime)
    | MAXDOP EQ_OR_ASSIGN NUMBER
    | compressionOption onPartitionClause?
    ;
    
compressionOption
    : DATA_COMPRESSION EQ_OR_ASSIGN ( NONE | ROW | PAGE | COLUMNSTORE | COLUMNSTORE_ARCHIVE)
    ;
    
eqTime
    : EQ_OR_ASSIGN NUMBER (MINUTES)?
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
    : EQ_OR_ASSIGN ( ON | OFF )
    ;

onPartitionClause
    : ON PARTITIONS LEFT_PAREN partitionExpressions RIGHT_PAREN
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
    : WAIT_AT_LOW_PRIORITY LEFT_PAREN MAX_DURATION EQ_OR_ASSIGN NUMBER ( MINUTES )? COMMA
    ABORT_AFTER_WAIT EQ_OR_ASSIGN ( NONE | SELF | BLOCKERS ) RIGHT_PAREN
    ;

onLowPriorLockWait
    : ON (LEFT_PAREN lowPriorityLockWait RIGHT_PAREN)?
    ;
    