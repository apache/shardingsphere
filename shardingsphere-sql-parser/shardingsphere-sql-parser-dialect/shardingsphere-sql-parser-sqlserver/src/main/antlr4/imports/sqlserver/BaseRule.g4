/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

grammar BaseRule;

import Symbol, Keyword, SQLServerKeyword, Literals;

parameterMarker
    : QUESTION_
    ;

literals
    : stringLiterals
    | numberLiterals
    | dateTimeLiterals
    | hexadecimalLiterals
    | bitValueLiterals
    | booleanLiterals
    | nullValueLiterals
    ;

stringLiterals
    : STRING_
    ;

numberLiterals
   : MINUS_? NUMBER_
   ;

dateTimeLiterals
    : (DATE | TIME | TIMESTAMP) STRING_
    | LBE_ identifier STRING_ RBE_
    ;

hexadecimalLiterals
    : HEX_DIGIT_
    ;

bitValueLiterals
    : BIT_NUM_
    ;
    
booleanLiterals
    : TRUE | FALSE
    ;

nullValueLiterals
    : NULL
    ;

identifier
    : IDENTIFIER_ | unreservedWord
    ;

unreservedWord
    : TRUNCATE | FUNCTION | TRIGGER | LIMIT | OFFSET | SAVEPOINT | BOOLEAN
    | ARRAY | LOCALTIME | LOCALTIMESTAMP | QUARTER | WEEK | MICROSECOND | ENABLE
    | DISABLE | BINARY | HIDDEN_ | MOD | PARTITION | TOP | ROW
    | XOR | ALWAYS | ROLE | START | ALGORITHM | AUTO | BLOCKERS
    | CLUSTERED | COLUMNSTORE | CONTENT | DATABASE | DAYS | DENY | DETERMINISTIC
    | DISTRIBUTION | DOCUMENT | DURABILITY | ENCRYPTED | FILESTREAM | FILETABLE | FOLLOWING
    | HASH | HEAP | INBOUND | INFINITE | LOGIN | MASKED | MAXDOP 
    | MINUTES | MONTHS | MOVE | NOCHECK | NONCLUSTERED | OBJECT | OFF
    | ONLINE | OUTBOUND | OVER | PAGE | PARTITIONS | PAUSED | PERIOD
    | PERSISTED | PRECEDING | RANDOMIZED | RANGE | REBUILD | REPLICATE | REPLICATION
    | RESUMABLE | ROWGUIDCOL | SAVE | SELF | SPARSE | SWITCH | TRAN
    | TRANCOUNT | UNBOUNDED | YEARS | WEEKS | ABORT_AFTER_WAIT | ALLOW_PAGE_LOCKS | ALLOW_ROW_LOCKS
    | ALL_SPARSE_COLUMNS | BUCKET_COUNT | COLUMNSTORE_ARCHIVE | COLUMN_ENCRYPTION_KEY | COLUMN_SET | COMPRESSION_DELAY | DATABASE_DEAULT
    | DATA_COMPRESSION | DATA_CONSISTENCY_CHECK | ENCRYPTION_TYPE | SYSTEM_TIME | SYSTEM_VERSIONING | TEXTIMAGE_ON | WAIT_AT_LOW_PRIORITY
    | STATISTICS_INCREMENTAL | STATISTICS_NORECOMPUTE | ROUND_ROBIN | SCHEMA_AND_DATA | SCHEMA_ONLY | SORT_IN_TEMPDB | IGNORE_DUP_KEY
    | IMPLICIT_TRANSACTIONS | MAX_DURATION | MEMORY_OPTIMIZED | MIGRATION_STATE | PAD_INDEX | REMOTE_DATA_ARCHIVE | FILESTREAM_ON
    | FILETABLE_COLLATE_FILENAME | FILETABLE_DIRECTORY | FILETABLE_FULLPATH_UNIQUE_CONSTRAINT_NAME | FILETABLE_PRIMARY_KEY_CONSTRAINT_NAME | FILETABLE_STREAMID_UNIQUE_CONSTRAINT_NAME
    | FILLFACTOR | FILTER_PREDICATE | HISTORY_RETENTION_PERIOD | HISTORY_TABLE | LOCK_ESCALATION | DROP_EXISTING | ROW_NUMBER
    | CONTROL | TAKE | OWNERSHIP | DEFINITION | APPLICATION | ASSEMBLY | SYMMETRIC | ASYMMETRIC
    | SERVER | RECEIVE | CHANGE | TRACE | TRACKING | RESOURCES | SETTINGS
    | STATE | AVAILABILITY | CREDENTIAL | ENDPOINT | EVENT | NOTIFICATION
    | LINKED | AUDIT | DDL | SQL | XML | IMPERSONATE | SECURABLES | AUTHENTICATE
    | EXTERNAL | ACCESS | ADMINISTER | BULK | OPERATIONS | UNSAFE | SHUTDOWN
    | SCOPED | CONFIGURATION |DATASPACE | SERVICE | CERTIFICATE | CONTRACT | ENCRYPTION
    | MASTER | DATA | SOURCE | FILE | FORMAT | LIBRARY | FULLTEXT | MASK | UNMASK
    | MESSAGE | TYPE | REMOTE | BINDING | ROUTE | SECURITY | POLICY | AGGREGATE | QUEUE
    | RULE | SYNONYM | COLLECTION | SCRIPT | KILL | BACKUP | LOG | SHOWPLAN
    | SUBSCRIBE | QUERY | NOTIFICATIONS | CHECKPOINT | SEQUENCE | INSTANCE | DO | DEFINER | LOCAL | CASCADED
    | NEXT | NAME | INTEGER | TYPE | MAX | MIN | SUM | COUNT | AVG | FIRST | DATETIME2
    | OUTPUT | INSERTED | DELETED
    ;

schemaName
    : identifier
    ;

tableName
    : (owner DOT_)? name
    ;

columnName
    : (owner DOT_)? name
    ;

owner
    : identifier
    ;

name
    : identifier
    ;

columnNames
    : LP_ columnName (COMMA_ columnName)* RP_
    ;

columnNamesWithSort
    : LP_ columnNameWithSort (COMMA_ columnNameWithSort)* RP_
    ;

tableNames
    : LP_? tableName (COMMA_ tableName)* RP_?
    ;

indexName
    : identifier
    ;

collationName
    : STRING_ | IDENTIFIER_
    ;

alias
    : IDENTIFIER_
    ;

dataTypeLength
    : LP_ (NUMBER_ (COMMA_ NUMBER_)?)? RP_
    ;

primaryKey
    : PRIMARY? KEY
    ;

// TODO comb expr
expr
    : expr logicalOperator expr
    | notOperator expr
    | LP_ expr RP_
    | booleanPrimary
    ;

logicalOperator
    : OR | OR_ | AND | AND_
    ;

notOperator
    : NOT | NOT_
    ;

booleanPrimary
    : booleanPrimary IS NOT? (TRUE | FALSE | UNKNOWN | NULL)
    | booleanPrimary SAFE_EQ_ predicate
    | booleanPrimary comparisonOperator predicate
    | booleanPrimary comparisonOperator (ALL | ANY) subquery
    | predicate
    ;

comparisonOperator
    : EQ_ | GTE_ | GT_ | LTE_ | LT_ | NEQ_
    ;

predicate
    : bitExpr NOT? IN subquery
    | bitExpr NOT? IN LP_ expr (COMMA_ expr)* RP_
    | bitExpr NOT? BETWEEN bitExpr AND predicate
    | bitExpr NOT? LIKE simpleExpr (ESCAPE simpleExpr)?
    | bitExpr
    ;

bitExpr
    : bitExpr VERTICAL_BAR_ bitExpr
    | bitExpr AMPERSAND_ bitExpr
    | bitExpr SIGNED_LEFT_SHIFT_ bitExpr
    | bitExpr SIGNED_RIGHT_SHIFT_ bitExpr
    | bitExpr PLUS_ bitExpr
    | bitExpr MINUS_ bitExpr
    | bitExpr ASTERISK_ bitExpr
    | bitExpr SLASH_ bitExpr
    | bitExpr MOD_ bitExpr
    | bitExpr CARET_ bitExpr
    | simpleExpr
    ;

simpleExpr
    : functionCall
    | parameterMarker
    | literals
    | columnName
    | simpleExpr OR_ simpleExpr
    | (PLUS_ | MINUS_ | TILDE_ | NOT_ | BINARY) simpleExpr
    | ROW? LP_ expr (COMMA_ expr)* RP_
    | EXISTS? subquery
    | LBE_ identifier expr RBE_
    | caseExpression
    | privateExprOfDb
    ;

functionCall
    : aggregationFunction | specialFunction | regularFunction 
    ;

aggregationFunction
    : aggregationFunctionName LP_ distinct? (expr (COMMA_ expr)* | ASTERISK_)? RP_
    ;

aggregationFunctionName
    : MAX | MIN | SUM | COUNT | AVG
    ;

distinct
    : DISTINCT
    ;

specialFunction
    : castFunction  | charFunction
    ;

castFunction
    : CAST LP_ expr AS dataType RP_
    ;

charFunction
    : CHAR LP_ expr (COMMA_ expr)* (USING ignoredIdentifier)? RP_
    ;

regularFunction
    : regularFunctionName LP_ (expr (COMMA_ expr)* | ASTERISK_)? RP_
    ;

regularFunctionName
    : identifier | IF | LOCALTIME | LOCALTIMESTAMP | INTERVAL
    ;

caseExpression
    : CASE simpleExpr? caseWhen+ caseElse?
    ;

caseWhen
    : WHEN expr THEN expr
    ;

caseElse
    : ELSE expr
    ;

privateExprOfDb
    : windowedFunction | atTimeZoneExpr | castExpr | convertExpr
    ;

subquery
    : matchNone
    ;

orderByClause
    : ORDER BY orderByItem (COMMA_ orderByItem)*
    (OFFSET expr (ROW | ROWS) (FETCH (FIRST | NEXT) expr (ROW | ROWS) ONLY)?)?
    ;

orderByItem
    : (columnName | numberLiterals | expr) (ASC | DESC)?
    ;

dataType
    : dataTypeName (dataTypeLength | LP_ MAX RP_ | LP_ (CONTENT | DOCUMENT)? ignoredIdentifier RP_)?
    ;

dataTypeName
    : BIGINT | NUMERIC | BIT | SMALLINT | DECIMAL | SMALLMONEY | INT | TINYINT | MONEY | FLOAT | REAL
    | DATE | DATETIMEOFFSET | SMALLDATETIME | DATETIME | DATETIME2 | TIME | CHAR | VARCHAR | TEXT | NCHAR | NVARCHAR
    | NTEXT | BINARY | VARBINARY | IMAGE | SQL_VARIANT | XML | UNIQUEIDENTIFIER | HIERARCHYID | GEOMETRY
    | GEOGRAPHY | IDENTIFIER_
    ;

atTimeZoneExpr
    : IDENTIFIER_ (WITH TIME ZONE)? STRING_
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

ignoredIdentifier
    : IDENTIFIER_
    ;

ignoredIdentifiers
    : ignoredIdentifier (COMMA_ ignoredIdentifier)*
    ;

matchNone
    : 'Default does not match anything'
    ;
