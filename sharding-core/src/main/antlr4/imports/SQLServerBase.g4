grammar SQLServerBase;

import SQLServerKeyword, Keyword, Symbol, BaseRule, DataType;

ID
    : (LBT_? DQ_? [a-zA-Z_$#][a-zA-Z0-9_$#]* DQ_? RBT_? DOT)* DOT* (LBT_? DQ_? [a-zA-Z_$#][a-zA-Z0-9_$#]* DQ_? RBT_?)
    | [a-zA-Z0-9_$]+ DOT_ASTERISK
    ;
    
dataType
    : typeName (dataTypeLength | LP_ MAX RP_ | LP_ (CONTENT | DOCUMENT)? xmlSchemaCollection RP_)?
    ;
    
privateExprOfDb
    : windowedFunction | atTimeZoneExpr | castExpr | convertExpr
    ;
    
atTimeZoneExpr
    : ID (WITH TIME ZONE)? STRING
    ;
    
castExpr
    : CAST LP_ expr AS dataType (LP_ NUMBER RP_)? RP_
    ;
    
convertExpr
    : CONVERT ( dataType (LP_ NUMBER RP_)? COMMA expr (COMMA NUMBER)?)
    ;
    
windowedFunction
    : functionCall overClause
    ;
    
overClause
    : OVER LP_ partitionByClause? orderByClause? rowRangeClause? RP_ 
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
    : windowFramePreceding | windowFrameBetween 
    ;
    
windowFrameBetween
    : BETWEEN windowFrameBound AND windowFrameBound
    ;
    
windowFrameBound
    : windowFramePreceding | windowFrameFollowing 
    ;
    
windowFramePreceding
    : UNBOUNDED PRECEDING | NUMBER PRECEDING | CURRENT ROW
    ;
    
windowFrameFollowing
    : UNBOUNDED FOLLOWING | NUMBER FOLLOWING | CURRENT ROW
    ;
    
columnList
    : LP_ columnNameWithSort (COMMA columnNameWithSort)* RP_ 
    ;
    
columnNameWithSort
    : columnName (ASC | DESC)?
    ;
    
indexOption
    : FILLFACTOR EQ_ NUMBER
    | eqOnOffOption
    | (COMPRESSION_DELAY | MAX_DURATION) eqTime
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
    : eqKey eqOnOff 
    ;
    
eqKey
    : PAD_INDEX
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
    ;
    
eqOnOff
    : EQ_ (ON | OFF)
    ;
    
onPartitionClause
    : ON PARTITIONS LP_ partitionExpressions RP_
    ;
    
partitionExpressions
    : partitionExpression (COMMA partitionExpression)*
    ;
    
partitionExpression
    : NUMBER | numberRange
    ;
    
numberRange
    : NUMBER TO NUMBER
    ;
    
lowPriorityLockWait
    : WAIT_AT_LOW_PRIORITY LP_ MAX_DURATION EQ_ NUMBER ( MINUTES )? COMMA
    ABORT_AFTER_WAIT EQ_ ( NONE | SELF | BLOCKERS ) RP_
    ;
    
onLowPriorLockWait
    : ON (LP_ lowPriorityLockWait RP_)?
    ;
