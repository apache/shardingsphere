grammar SQLServerBase;

import SQLServerKeyword, Keyword, Symbol, BaseRule, DataType;

ID
    : (LBT_? DQ_? [a-zA-Z_$#][a-zA-Z0-9_$#]* DQ_? RBT_? DOT_)* DOT_* (LBT_? DQ_? [a-zA-Z_$#][a-zA-Z0-9_$#]* DQ_? RBT_?) | [a-zA-Z0-9_$]+ DOT_ASTERISK_
    ;

dataType
    : dataTypeName_ (dataTypeLength | LP_ MAX RP_ | LP_ (CONTENT | DOCUMENT)? ignoredIdentifier_ RP_)?
    ;

dataTypeName_
    : ID
    ;

privateExprOfDb
    : windowedFunction | atTimeZoneExpr | castExpr | convertExpr
    ;

atTimeZoneExpr
    : ID (WITH TIME ZONE)? STRING_
    ;

castExpr
    : CAST LP_ expr AS dataType (LP_ NUMBER_ RP_)? RP_
    ;

convertExpr
    : CONVERT (dataType (LP_ NUMBER_ RP_)? COMMA_ expr (COMMA_ NUMBER_)?)
    ;

windowedFunction
    : functionCall overClause
    ;

overClause
    : OVER LP_ partitionByClause? orderByClause? rowRangeClause? RP_ 
    ;

partitionByClause
    : PARTITION BY expr (COMMA_ expr)*
    ;

orderByClause
    : ORDER BY orderByExpr (COMMA_ orderByExpr)*
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
    : UNBOUNDED PRECEDING | NUMBER_ PRECEDING | CURRENT ROW
    ;

windowFrameFollowing
    : UNBOUNDED FOLLOWING | NUMBER_ FOLLOWING | CURRENT ROW
    ;

columnList
    : LP_ columnNameWithSort (COMMA_ columnNameWithSort)* RP_
    ;

columnNameWithSort
    : columnName (ASC | DESC)?
    ;

indexOption
    : FILLFACTOR EQ_ NUMBER_
    | eqOnOffOption
    | (COMPRESSION_DELAY | MAX_DURATION) eqTime
    | MAXDOP EQ_ NUMBER_
    | compressionOption onPartitionClause?
    ;

compressionOption
    : DATA_COMPRESSION EQ_ (NONE | ROW | PAGE | COLUMNSTORE | COLUMNSTORE_ARCHIVE)
    ;

eqTime
    : EQ_ NUMBER_ (MINUTES)?
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
    : partitionExpression (COMMA_ partitionExpression)*
    ;

partitionExpression
    : NUMBER_ | numberRange
    ;

numberRange
    : NUMBER_ TO NUMBER_
    ;

lowPriorityLockWait
    : WAIT_AT_LOW_PRIORITY LP_ MAX_DURATION EQ_ NUMBER_ (MINUTES)? COMMA_ ABORT_AFTER_WAIT EQ_ (NONE | SELF | BLOCKERS) RP_
    ;

onLowPriorLockWait
    : ON (LP_ lowPriorityLockWait RP_)?
    ;
