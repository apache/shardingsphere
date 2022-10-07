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
    : (PLUS_ | MINUS_)? NUMBER_
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
    : regularIdentifier | delimitedIdentifier
    ;

regularIdentifier
    : IDENTIFIER_ | unreservedWord
    ;

delimitedIdentifier
    : DELIMITED_IDENTIFIER_
    ;

unreservedWord
    : TRUNCATE | FUNCTION | TRIGGER | LIMIT | OFFSET | SAVEPOINT | BOOLEAN
    | ARRAY | LOCALTIME | LOCALTIMESTAMP | QUARTER | WEEK | MICROSECOND | ENABLE
    | DISABLE | BINARY | HIDDEN_ | MOD | PARTITION | TOP | ROW
    | XOR | ALWAYS | ROLE | START | ALGORITHM | AUTO | BLOCKERS
    | CLUSTERED | COLUMNSTORE | CONTENT | CONCAT | DATABASE | DAYS | DENY | DETERMINISTIC
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
    | OUTPUT | INSERTED | DELETED | KB | MB | GB | TB | FILENAME | MAXSIZE | FILEGROWTH | UNLIMITED | MEMORY_OPTIMIZED_DATA | FILEGROUP | NON_TRANSACTED_ACCESS
    | DB_CHAINING | TRUSTWORTHY | GROUP | ROWS | DATE | DATEPART | CAST | DAY
    | FORWARD_ONLY | KEYSET | FAST_FORWARD | READ_ONLY | SCROLL_LOCKS | OPTIMISTIC | TYPE_WARNING | SCHEMABINDING | CALLER
    | OWNER | SNAPSHOT | REPEATABLE | SERIALIZABLE | NATIVE_COMPILATION | VIEW_METADATA | INSTEAD | APPEND | INCREMENT | CACHE | MINVALUE | MAXVALUE | RESTART
    | LOB_COMPACTION | COMPRESS_ALL_ROW_GROUPS | REORGANIZE | RESUME | PAUSE | ABORT
    | ACCELERATED_DATABASE_RECOVERY | PERSISTENT_VERSION_STORE_FILEGROUP | IMMEDIATE | NO_WAIT | TARGET_RECOVERY_TIME | SECONDS | HONOR_BROKER_PRIORITY
    | ERROR_BROKER_CONVERSATIONS | NEW_BROKER | DISABLE_BROKER | ENABLE_BROKER | MEMORY_OPTIMIZED_ELEVATE_TO_SNAPSHOT | READ_COMMITTED_SNAPSHOT | ALLOW_SNAPSHOT_ISOLATION
    | RECURSIVE_TRIGGERS | QUOTED_IDENTIFIER | NUMERIC_ROUNDABORT | CONCAT_NULL_YIELDS_NULL | COMPATIBILITY_LEVEL | ARITHABORT | ANSI_WARNINGS | ANSI_PADDING | ANSI_NULLS
    | ANSI_NULL_DEFAULT | PAGE_VERIFY | CHECKSUM | TORN_PAGE_DETECTION | BULK_LOGGED | RECOVERY | TOTAL_EXECUTION_CPU_TIME_MS | TOTAL_COMPILE_CPU_TIME_MS | STALE_CAPTURE_POLICY_THRESHOLD
    | EXECUTION_COUNT | QUERY_CAPTURE_POLICY | WAIT_STATS_CAPTURE_MODE | MAX_PLANS_PER_QUERY | QUERY_CAPTURE_MODE | SIZE_BASED_CLEANUP_MODE | INTERVAL_LENGTH_MINUTES | MAX_STORAGE_SIZE_MB
    | DATA_FLUSH_INTERVAL_SECONDS | CLEANUP_POLICY | CUSTOM | STALE_QUERY_THRESHOLD_DAYS | OPERATION_MODE | QUERY_STORE | CURSOR_DEFAULT | GLOBAL | CURSOR_CLOSE_ON_COMMIT | HOURS | CHANGE_RETENTION
    | AUTO_CLEANUP | CHANGE_TRACKING | AUTOMATIC_TUNING | FORCE_LAST_GOOD_PLAN | AUTO_UPDATE_STATISTICS_ASYNC | AUTO_UPDATE_STATISTICS | AUTO_SHRINK | AUTO_CREATE_STATISTICS | INCREMENTAL | AUTO_CLOSE
    | DATA_RETENTION | TEMPORAL_HISTORY_RETENTION | EDITION | MIXED_PAGE_ALLOCATION | DISABLED | ALLOWED | HADR | MULTI_USER | RESTRICTED_USER | SINGLE_USER | OFFLINE | EMERGENCY | SUSPEND | DATE_CORRELATION_OPTIMIZATION
    | ELASTIC_POOL | SERVICE_OBJECTIVE | DATABASE_NAME | ALLOW_CONNECTIONS | GEO | NAMED | DATEFIRST | BACKUP_STORAGE_REDUNDANCY | FORCE_FAILOVER_ALLOW_DATA_LOSS | SECONDARY | FAILOVER | DEFAULT_FULLTEXT_LANGUAGE
    | DEFAULT_LANGUAGE | INLINE | NESTED_TRIGGERS | TRANSFORM_NOISE_WORDS | TWO_DIGIT_YEAR_CUTOFF | PERSISTENT_LOG_BUFFER | DIRECTORY_NAME | DATEFORMAT | DELAYED_DURABILITY | TRANSFER | SCHEMA | PASSWORD | AUTHORIZATION
    ;

databaseName
    : identifier
    ;

schemaName
    : identifier
    ;

functionName
    : (owner DOT_)? name
    ;

procedureName
    : (owner DOT_)? name (SEMI_ numberLiterals)?
    ;

viewName
    : (owner DOT_)? name
    ;

triggerName
    : (schemaName DOT_)? name
    ;

sequenceName
    : (schemaName DOT_)? name
    ;

tableName
    : (owner DOT_)? name
    ;

queueName
    : (schemaName DOT_)? name
    ;

contractName
    : name
    ;

serviceName
    : name
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

constraintName
    : identifier
    ;

collationName
    : STRING_ | IDENTIFIER_
    ;

alias
    : identifier | STRING_
    ;

dataTypeLength
    : LP_ (NUMBER_ (COMMA_ NUMBER_)?)? RP_
    ;

primaryKey
    : PRIMARY? KEY
    ;

// TODO comb expr
expr
    : expr andOperator expr
    | expr orOperator expr
    | notOperator expr
    | LP_ expr RP_
    | booleanPrimary
    ;

andOperator
    : AND | AND_
    ;

orOperator
    : OR | OR_
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
    | variableName
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
    : (owner DOT_)? identifier | IF | LOCALTIME | LOCALTIMESTAMP | INTERVAL
    ;

caseExpression
    : CASE simpleExpr? caseWhen+ caseElse? END
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

orderByClause
    : ORDER BY orderByItem (COMMA_ orderByItem)*
    (OFFSET expr (ROW | ROWS) (FETCH (FIRST | NEXT) expr (ROW | ROWS) ONLY)?)?
    ;

orderByItem
    : (columnName | numberLiterals | expr) (COLLATE identifier)? (ASC | DESC)?
    ;

dataType
    : (ignoredIdentifier DOT_)? dataTypeName (dataTypeLength | LP_ MAX RP_ | LP_ (CONTENT | DOCUMENT)? ignoredIdentifier RP_)?
    ;

dataTypeName
    : BIGINT | NUMERIC | BIT | SMALLINT | DECIMAL | SMALLMONEY | INT | TINYINT | MONEY | FLOAT | REAL
    | DATE | DATETIMEOFFSET | SMALLDATETIME | DATETIME | DATETIME2 | TIME | CHAR | VARCHAR | TEXT | NCHAR | NVARCHAR
    | NTEXT | BINARY | VARBINARY | IMAGE | SQL_VARIANT | XML | UNIQUEIDENTIFIER | HIERARCHYID | GEOMETRY
    | GEOGRAPHY | IDENTIFIER_ | INTEGER
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
    | OPTIMIZE_FOR_SEQUENTIAL_KEY
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

variableName
    : identifier
    ;

executeAsClause
    : (EXEC | EXECUTE) AS (CALLER | SELF | OWNER | stringLiterals)
    ;

transactionName
    : identifier
    ;

transactionVariableName
    : variableName
    ;

savepointName
    : identifier
    ;

savepointVariableName
    : variableName
    ;

entityType
    : OBJECT | TYPE
    ;

ifExists
    : IF EXISTS
    ;
