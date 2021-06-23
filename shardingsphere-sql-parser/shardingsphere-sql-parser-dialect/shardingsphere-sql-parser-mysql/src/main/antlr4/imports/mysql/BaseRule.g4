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

import Symbol, Keyword, MySQLKeyword, Literals;

parameterMarker
    : QUESTION_
    ;
    
customKeyword
    : MAX
    | MIN
    | SUM
    | COUNT
    | GROUP_CONCAT
    | CAST
    | POSITION
    | SUBSTRING
    | SUBSTR
    | EXTRACT
    | TRIM
    | LAST_DAY
    | TRADITIONAL
    | TREE
    | MYSQL_ADMIN
    | INSTANT
    | INPLACE
    | COPY
    | UL_BINARY
    | AUTOCOMMIT
    | INNODB
    | REDO_LOG
    ;
    
literals
    : stringLiterals
    | numberLiterals
    | temporalLiterals
    | hexadecimalLiterals
    | bitValueLiterals
    | booleanLiterals
    | nullValueLiterals
    ;
    
string_
    : DOUBLE_QUOTED_TEXT | SINGLE_QUOTED_TEXT
    ;
    
stringLiterals
    : UNDERSCORE_CHARSET? string_ | NCHAR_TEXT
    ;
    
numberLiterals
    : NUMBER_
    ;
    
temporalLiterals
    : (DATE | TIME | TIMESTAMP) SINGLE_QUOTED_TEXT
    ;
    
hexadecimalLiterals
    : UNDERSCORE_CHARSET? HEX_DIGIT_ collateClause?
    ;
    
bitValueLiterals
    : UNDERSCORE_CHARSET? BIT_NUM_ collateClause?
    ;
    
booleanLiterals
    : TRUE | FALSE
    ;
    
nullValueLiterals
    : NULL
    ;
    
collationName
    : textOrIdentifier | BINARY
    ;
    
identifier
    : IDENTIFIER_
    | identifierKeywordsUnambiguous
    | identifierKeywordsAmbiguous1RolesAndLabels
    | identifierKeywordsAmbiguous2Labels
    | identifierKeywordsAmbiguous3Roles
    | identifierKeywordsAmbiguous4SystemVariables
    | customKeyword
    | DOUBLE_QUOTED_TEXT
    ;
    
identifierKeywordsUnambiguous
    : ACTION
    | ACCOUNT
    | ACTIVE
//    | ADDDATE
    | ADMIN
    | AFTER
    | AGAINST
    | AGGREGATE
    | ALGORITHM
    | ALWAYS
    | ANY
    | ARRAY
    | AT
    | ATTRIBUTE
    | AUTOEXTEND_SIZE
    | AUTO_INCREMENT
    | AVG_ROW_LENGTH
    | AVG
    | BACKUP
    | BINLOG
    | BIT
    | BLOCK
    | BOOLEAN
    | BOOL
    | BTREE
    | BUCKETS
    | CASCADED
    | CATALOG_NAME
    | CHAIN
    | CHANGED
    | CHANNEL
    | CIPHER
    | CLASS_ORIGIN
    | CLIENT
    | CLOSE
    | COALESCE
    | CODE
    | COLLATION
    | COLUMNS
    | COLUMN_FORMAT
    | COLUMN_NAME
    | COMMITTED
    | COMPACT
    | COMPLETION
    | COMPONENT
    | COMPRESSED
    | COMPRESSION
    | CONCURRENT
    | CONNECTION
    | CONSISTENT
    | CONSTRAINT_CATALOG
    | CONSTRAINT_NAME
    | CONSTRAINT_SCHEMA
    | CONTEXT
    | CPU
    | CURRENT
    | CURSOR_NAME
    | DATAFILE
    | DATA
    | DATETIME
    | DATE
    | DAY
    | DEFAULT_AUTH
    | DEFINER
    | DEFINITION
    | DELAY_KEY_WRITE
    | DESCRIPTION
    | DIAGNOSTICS
    | DIRECTORY
    | DISABLE
    | DISCARD
    | DISK
    | DUMPFILE
    | DUPLICATE
    | DYNAMIC
    | ENABLE
    | ENCRYPTION
    | ENDS
    | ENFORCED
    | ENGINES
    | ENGINE
    | ENGINE_ATTRIBUTE
    | ENUM
    | ERRORS
    | ERROR
    | ESCAPE
    | EVENTS
    | EVERY
    | EXCHANGE
    | EXCLUDE
    | EXPANSION
    | EXPIRE
    | EXPORT
    | EXTENDED
    | EXTENT_SIZE
    | FAILED_LOGIN_ATTEMPTS
    | FAST
    | FAULTS
    | FILE_BLOCK_SIZE
    | FILTER
    | FIRST
    | FIXED
    | FOLLOWING
    | FORMAT
    | FOUND
    | FULL
    | GENERAL
    | GEOMETRYCOLLECTION
    | GEOMETRY
    | GET_FORMAT
    | GET_MASTER_PUBLIC_KEY
    | GRANTS
    | GROUP_REPLICATION
    | HASH
    | HISTOGRAM
    | HISTORY
    | HOSTS
    | HOST
    | HOUR
    | IDENTIFIED
    | IGNORE_SERVER_IDS
    | INACTIVE
    | INDEXES
    | INITIAL_SIZE
    | INSERT_METHOD
    | INSTANCE
    | INVISIBLE
    | INVOKER
    | IO
    | IPC
    | ISOLATION
    | ISSUER
    | JSON
    | JSON_VALUE
    | KEY_BLOCK_SIZE
    | LAST
    | LEAVES
    | LESS
    | LEVEL
    | LINESTRING
    | LIST
    | LOCKED
    | LOCKS
    | LOGFILE
    | LOGS
    | MASTER_AUTO_POSITION
    | MASTER_COMPRESSION_ALGORITHM
    | MASTER_CONNECT_RETRY
    | MASTER_DELAY
    | MASTER_HEARTBEAT_PERIOD
    | MASTER_HOST
    | NETWORK_NAMESPACE
    | MASTER_LOG_FILE
    | MASTER_LOG_POS
    | MASTER_PASSWORD
    | MASTER_PORT
    | MASTER_PUBLIC_KEY_PATH
    | MASTER_RETRY_COUNT
    | MASTER_SERVER_ID
    | MASTER_SSL_CAPATH
    | MASTER_SSL_CA
    | MASTER_SSL_CERT
    | MASTER_SSL_CIPHER
    | MASTER_SSL_CRLPATH
    | MASTER_SSL_CRL
    | MASTER_SSL_KEY
    | MASTER_SSL
    | MASTER
    | MASTER_TLS_CIPHERSUITES
    | MASTER_TLS_VERSION
    | MASTER_USER
    | MASTER_ZSTD_COMPRESSION_LEVEL
    | MAX_CONNECTIONS_PER_HOUR
    | MAX_QUERIES_PER_HOUR
    | MAX_ROWS
    | MAX_SIZE
    | MAX_UPDATES_PER_HOUR
    | MAX_USER_CONNECTIONS
    | MEDIUM
    | MEMBER
    | MEMORY
    | MERGE
    | MESSAGE_TEXT
    | MICROSECOND
    | MIGRATE
    | MINUTE
    | MIN_ROWS
    | MODE
    | MODIFY
    | MONTH
    | MULTILINESTRING
    | MULTIPOINT
    | MULTIPOLYGON
    | MUTEX
    | MYSQL_ERRNO
    | NAMES
    | NAME
    | NATIONAL
    | NCHAR
    | NDBCLUSTER
    | NESTED
    | NEVER
    | NEW
    | NEXT
    | NODEGROUP
    | NOWAIT
    | NO_WAIT
    | NULLS
    | NUMBER
    | NVARCHAR
    | OFF
    | OFFSET
    | OJ
    | OLD
    | ONE
    | ONLY
    | OPEN
    | OPTIONAL
    | OPTIONS
    | ORDINALITY
    | ORGANIZATION
    | OTHERS
    | OWNER
    | PACK_KEYS
    | PAGE
    | PARSER
    | PARTIAL
    | PARTITIONING
    | PARTITIONS
    | PASSWORD
    | PASSWORD_LOCK_TIME
    | PATH
    | PHASE
    | PLUGINS
    | PLUGIN_DIR
    | PLUGIN
    | POINT
    | POLYGON
    | PORT
    | PRECEDING
    | PRESERVE
    | PREV
    | PRIVILEGES
    | PRIVILEGE_CHECKS_USER
    | PROCESSLIST
    | PROFILES
    | PROFILE
    | QUARTER
    | QUERY
    | QUICK
    | RANDOM
    | READ_ONLY
    | REBUILD
    | RECOVER
    | REDO_BUFFER_SIZE
    | REDUNDANT
    | REFERENCE
    | RELAY
    | RELAYLOG
    | RELAY_LOG_FILE
    | RELAY_LOG_POS
    | RELAY_THREAD
    | REMOVE
    | REORGANIZE
    | REPEATABLE
    | REPLICATE_DO_DB
    | REPLICATE_DO_TABLE
    | REPLICATE_IGNORE_DB
    | REPLICATE_IGNORE_TABLE
    | REPLICATE_REWRITE_DB
    | REPLICATE_WILD_DO_TABLE
    | REPLICATE_WILD_IGNORE_TABLE
    | REQUIRE_ROW_FORMAT
//    | REQUIRE_TABLE_PRIMARY_KEY_CHECK
    | USER_RESOURCES
    | RESPECT
    | RESTORE
    | RESUME
    | RETAIN
    | RETURNED_SQLSTATE
    | RETURNING
    | RETURNS
    | REUSE
    | REVERSE
    | ROLE
    | ROLLUP
    | ROTATE
    | ROUTINE
    | ROW_COUNT
    | ROW_FORMAT
    | RTREE
    | SCHEDULE
    | SCHEMA_NAME
    | SECONDARY_ENGINE
    | SECONDARY_ENGINE_ATTRIBUTE
    | SECONDARY_LOAD
    | SECONDARY
    | SECONDARY_UNLOAD
    | SECOND
    | SECURITY
    | SERIALIZABLE
    | SERIAL
    | SERVER
    | SHARE
    | SIMPLE
//    | SKIP
    | SLOW
    | SNAPSHOT
    | SOCKET
    | SONAME
    | SOUNDS
    | SOURCE
    | SQL_AFTER_GTIDS
    | SQL_AFTER_MTS_GAPS
    | SQL_BEFORE_GTIDS
    | SQL_BUFFER_RESULT
    | SQL_NO_CACHE
    | SQL_THREAD
    | SRID
    | STACKED
    | STARTS
    | STATS_AUTO_RECALC
    | STATS_PERSISTENT
    | STATS_SAMPLE_PAGES
    | STATUS
    | STORAGE
    | STREAM
    | STRING
    | SUBCLASS_ORIGIN
//    | SUBDATE
    | SUBJECT
    | SUBPARTITIONS
    | SUBPARTITION
    | SUSPEND
    | SWAPS
    | SWITCHES
    | TABLES
    | TABLESPACE
    | TABLE_CHECKSUM
    | TABLE_NAME
    | TEMPORARY
    | TEMPTABLE
    | TEXT
    | THAN
    | THREAD_PRIORITY
    | TIES
    | TIMESTAMP_ADD
    | TIMESTAMP_DIFF
    | TIMESTAMP
    | TIME
    | TLS
    | TRANSACTION
    | TRIGGERS
    | TYPES
    | TYPE
    | UNBOUNDED
    | UNCOMMITTED
    | UNDEFINED
    | UNDOFILE
    | UNDO_BUFFER_SIZE
    | UNKNOWN
    | UNTIL
    | UPGRADE
    | USER
    | USE_FRM
    | VALIDATION
    | VALUE
    | VARIABLES
    | VCPU
    | VIEW
    | VISIBLE
    | WAIT
    | WARNINGS
    | WEEK
    | WEIGHT_STRING
    | WITHOUT
    | WORK
    | WRAPPER
    | X509
    | XID
    | XML
    | YEAR
    ;
    
identifierKeywordsAmbiguous1RolesAndLabels
    : EXECUTE
    | RESTART
    | SHUTDOWN
    ;
    
identifierKeywordsAmbiguous2Labels
    : ASCII
    | BEGIN
    | BYTE
    | CACHE
    | CHARSET
    | CHECKSUM
    | CLONE
    | COMMENT
    | COMMIT
    | CONTAINS
    | DEALLOCATE
    | DO
    | END
    | FLUSH
    | FOLLOWS
    | HANDLER
    | HELP
    | IMPORT
    | INSTALL
    | LANGUAGE
    | NO
    | PRECEDES
    | PREPARE
    | REPAIR
    | RESET
    | ROLLBACK
    | SAVEPOINT
    | SIGNED
    | SLAVE
    | START
    | STOP
    | TRUNCATE
    | UNICODE
    | UNINSTALL
    | XA
    ;
    
identifierKeywordsAmbiguous3Roles
    : EVENT
    | FILE
    | NONE
    | PROCESS
    | PROXY
    | RELOAD
    | REPLICATION
    | RESOURCE
    | SUPER
    ;
    
identifierKeywordsAmbiguous4SystemVariables
    : GLOBAL
    | LOCAL
    | PERSIST
    | PERSIST_ONLY
    | SESSION
    ;
    
textOrIdentifier
    : identifier | string_
    ;
    
variable
    : userVariable | systemVariable
    ;
    
userVariable
    : AT_ textOrIdentifier
    ;
    
systemVariable
    : AT_ AT_ systemVariableScope=(GLOBAL | SESSION | LOCAL)? textOrIdentifier (DOT_ identifier)?
    ;
    
setSystemVariable
    : AT_ AT_ (optionType DOT_)? internalVariableName
    ;
    
optionType
    : GLOBAL | PERSIST | PERSIST_ONLY | SESSION | LOCAL
    ;
    
internalVariableName
    : identifier
    | DEFAULT DOT_ identifier
    | identifier DOT_ identifier
    ;
    
setExprOrDefault
    : expr | DEFAULT | ALL | ON | BINARY | ROW | SYSTEM
    ;
    
transactionCharacteristics
    : transactionAccessMode (COMMA_ isolationLevel)?
    | isolationLevel (COMMA_ transactionAccessMode)?
    ;
    
isolationLevel
    : ISOLATION LEVEL isolationTypes
    ;
    
isolationTypes
    : REPEATABLE READ | READ COMMITTED | READ UNCOMMITTED | SERIALIZABLE
    ;
    
transactionAccessMode
    : READ (WRITE | ONLY)
    ;
    
schemaName
    : identifier
    ;
    
schemaNames
    : schemaName (COMMA_ schemaName)*
    ;
    
charsetName
    : textOrIdentifier | BINARY
    ;
    
schemaPairs
    : schemaPair (COMMA_ schemaPair)*
    ;
    
schemaPair
    : LP_ schemaName COMMA_ schemaName RP_
    ;
    
tableName
    : (owner DOT_)? name
    ;
    
columnName
    : identifier
    ;
    
indexName
    : identifier
    ;

constraintName
    : identifier
    ;

userIdentifierOrText
    : textOrIdentifier (AT_ textOrIdentifier)?
    ;
    
userName
    : userIdentifierOrText | CURRENT_USER (LP_ RP_)?
    ;
    
eventName
    : identifier (DOT_ identifier)?
    ;
    
serverName
    : textOrIdentifier
    ; 
    
wrapperName
    : textOrIdentifier
    ;
    
functionName
    : identifier
    | (owner DOT_)? identifier
    ;
    
viewName
    : identifier
    | (owner DOT_)? identifier
    ;
    
owner
    : identifier
    ;
    
alias
    : textOrIdentifier
    ;
    
name
    : identifier
    ;
    
tableList
    : tableName (COMMA_ tableName)*
    ;
    
viewNames
    : viewName (COMMA_ viewName)*
    ;
    
columnNames
    : columnName (COMMA_ columnName)*
    ;
    
groupName
    : identifier
    ;
    
routineName
    : identifier
    ;
    
shardLibraryName
    : stringLiterals
    ;
    
componentName
    : string_
    ;
    
pluginName
    : identifier
    ;
    
hostName
    : string_
    ;
    
port
    : NUMBER_
    ;
    
cloneInstance
    : userName AT_ hostName COLON_ port
    ;
    
cloneDir
    : string_
    ;
    
channelName
    : identifier (DOT_ identifier)?
    ;
    
logName
    : stringLiterals
    ;
    
roleName
    : roleIdentifierOrText (AT_ textOrIdentifier)?
    ;
    
roleIdentifierOrText
    : identifier | string_
    ;
    
engineRef
    : textOrIdentifier
    ;
    
triggerName
    : identifier (DOT_ identifier)?
    ;
    
triggerTime
    : BEFORE | AFTER
    ;
    
tableOrTables
    : TABLE | TABLES
    ;
    
userOrRole
    : userName | roleName
    ;
    
partitionName
    : identifier
    ;
    
identifierList
    : identifier (COMMA_ identifier)*
    ;
    
allOrPartitionNameList
    : ALL | identifierList
    ;
    
triggerEvent
    : INSERT | UPDATE | DELETE
    ;
    
triggerOrder
    : (FOLLOWS | PRECEDES) triggerName
    ;
    
expr
    : booleanPrimary
    | expr logicalOperator expr
    | expr XOR expr
    | notOperator expr
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
    | bitExpr SOUNDS LIKE bitExpr
    | bitExpr NOT? LIKE simpleExpr (ESCAPE simpleExpr)?
    | bitExpr NOT? REGEXP bitExpr
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
    | bitExpr DIV bitExpr
    | bitExpr MOD bitExpr
    | bitExpr MOD_ bitExpr
    | bitExpr CARET_ bitExpr
    | bitExpr PLUS_ intervalExpression
    | bitExpr MINUS_ intervalExpression
    | simpleExpr
    ;
    
simpleExpr
    : functionCall
    | parameterMarker
    | literals
    | columnRef
    | simpleExpr COLLATE textOrIdentifier
    | variable
    | simpleExpr OR_ simpleExpr
    | (PLUS_ | MINUS_ | TILDE_ | notOperator | BINARY) simpleExpr
    | ROW? LP_ expr (COMMA_ expr)* RP_
    | EXISTS? subquery
    | LBE_ identifier expr RBE_
    | identifier (JSON_SEPARATOR | JSON_UNQUOTED_SEPARATOR) string_
    | matchExpression
    | caseExpression
    | intervalExpression
    ;
    
columnRef
    : identifier (DOT_ identifier)? (DOT_ identifier)?
    ;
    
columnRefList
    : columnRef (COMMA_ columnRef)*
    ;
    
functionCall
    : aggregationFunction | specialFunction | regularFunction 
    ;
    
aggregationFunction
    : aggregationFunctionName LP_ distinct? (expr (COMMA_ expr)* | ASTERISK_)? RP_ overClause?
    ;
    
aggregationFunctionName
    : MAX | MIN | SUM | COUNT | AVG
    ;
    
distinct
    : DISTINCT
    ;
    
overClause
    : OVER (LP_ windowSpecification RP_ | identifier)
    ;
    
windowSpecification
    : identifier? (PARTITION BY expr (COMMA_ expr)*)? orderByClause? frameClause?
    ;
    
frameClause
    : (ROWS | RANGE) (frameStart | frameBetween)
    ;
    
frameStart
    : CURRENT ROW | UNBOUNDED PRECEDING | UNBOUNDED FOLLOWING | expr PRECEDING | expr FOLLOWING
    ;
    
frameEnd
    : frameStart
    ;
    
frameBetween
    : BETWEEN frameStart AND frameEnd
    ;
    
specialFunction
    : groupConcatFunction | windowFunction | castFunction | convertFunction | positionFunction | substringFunction | extractFunction 
    | charFunction | trimFunction | weightStringFunction | valuesFunction | currentUserFunction
    ;
    
currentUserFunction
    : CURRENT_USER (LP_ RP_)?
    ;
    
groupConcatFunction
    : GROUP_CONCAT LP_ distinct? (expr (COMMA_ expr)* | ASTERISK_)? (orderByClause)? (SEPARATOR expr)? RP_
    ;
    
windowFunction
    : funcName = (ROW_NUMBER | RANK | DENSE_RANK | CUME_DIST | PERCENT_RANK) LP_ RP_ windowingClause
    | funcName = NTILE (simpleExpr) windowingClause
    | funcName = (LEAD | LAG) LP_ expr leadLagInfo? RP_ nullTreatment? windowingClause
    | funcName = (FIRST_VALUE | LAST_VALUE) LP_ expr RP_ nullTreatment? windowingClause
    | funcName = NTH_VALUE LP_ expr COMMA_ simpleExpr RP_ (FROM (FIRST | LAST))? nullTreatment? windowingClause
    ;
    
windowingClause
    : OVER (windowName=identifier | windowSpecification)
    ;
    
leadLagInfo
    : COMMA_ (NUMBER_ | QUESTION_) (COMMA_ expr)?
    ;
    
nullTreatment
    : (RESPECT | IGNORE) NULLS
    ;
    
checkType
    : FOR UPGRADE | QUICK | FAST | MEDIUM | EXTENDED | CHANGED
    ;
    
repairType
    : QUICK | EXTENDED | USE_FRM
    ;
    
castFunction
    : CAST LP_ expr AS dataType RP_
    ;
    
convertFunction
    : CONVERT LP_ expr COMMA_ castType RP_
    | CONVERT LP_ expr USING charsetName RP_
    ;
    
castType
    : BINARY fieldLength?
    | CHAR fieldLength? charsetWithOptBinary?
    | nchar fieldLength?
    | SIGNED INT?
    | UNSIGNED INT?
    | DATE
    | TIME typeDatetimePrecision?
    | DATETIME typeDatetimePrecision?
    | DECIMAL (fieldLength | precision)?
    | JSON
    | REAL
    | DOUBLE PRECISION
    | FLOAT precision?
    ;
    
nchar
    : NCHAR | NATIONAL CHAR
    ;
    
positionFunction
    : POSITION LP_ expr IN expr RP_
    ;
    
substringFunction
    : (SUBSTRING | SUBSTR) LP_ expr FROM NUMBER_ (FOR NUMBER_)? RP_
    | (SUBSTRING | SUBSTR) LP_ expr COMMA_ NUMBER_ (COMMA_ NUMBER_)? RP_
    ;
    
extractFunction
    : EXTRACT LP_ identifier FROM expr RP_
    ;
    
charFunction
    : CHAR LP_ expr (COMMA_ expr)* (USING charsetName)? RP_
    ;
    
trimFunction
    : TRIM LP_ (LEADING | BOTH | TRAILING) string_ FROM string_ RP_
    ;
    
valuesFunction
    : VALUES LP_ columnRefList RP_
    ;
    
weightStringFunction
    : WEIGHT_STRING LP_ expr (AS dataType)? levelClause? RP_
    ;
    
levelClause
    : LEVEL (levelInWeightListElement (COMMA_ levelInWeightListElement)* | NUMBER_ MINUS_ NUMBER_)
    ;
    
levelInWeightListElement
    : NUMBER_ direction? REVERSE?
    ;
    
regularFunction
    : completeRegularFunction
    | shorthandRegularFunction
    ;
    
shorthandRegularFunction
    : CURRENT_DATE | CURRENT_TIME (LP_ NUMBER_? RP_)? | CURRENT_TIMESTAMP | LAST_DAY | LOCALTIME | LOCALTIMESTAMP
    ;
    
completeRegularFunction
    : regularFunctionName (LP_ (expr (COMMA_ expr)* | ASTERISK_)? RP_)
    ;
    
regularFunctionName
    : IF | LOCALTIME | LOCALTIMESTAMP | REPLACE | INTERVAL | MOD
    | DATABASE | SCHEMA | LEFT | RIGHT | DATE | DAY | GEOMETRYCOLLECTION
    | LINESTRING | MULTILINESTRING | MULTIPOINT | MULTIPOLYGON | POINT | POLYGON
    | TIME | TIMESTAMP | TIMESTAMP_ADD | TIMESTAMP_DIFF | DATE | CURRENT_TIMESTAMP | identifier
    ;
    
matchExpression
    : MATCH (columnRefList | LP_ columnRefList RP_ ) AGAINST LP_ expr matchSearchModifier? RP_
    ;
    
matchSearchModifier
    : IN NATURAL LANGUAGE MODE | IN NATURAL LANGUAGE MODE WITH QUERY EXPANSION | IN BOOLEAN MODE | WITH QUERY EXPANSION
    ;
    
caseExpression
    : CASE simpleExpr? caseWhen+ caseElse? END
    ;
    
datetimeExpr
    : expr
    ;
    
binaryLogFileIndexNumber
    : NUMBER_
    ;
    
caseWhen
    : WHEN expr THEN expr
    ;
    
caseElse
    : ELSE expr
    ;
    
intervalExpression
    : INTERVAL intervalValue
    ;
    
intervalValue
    : expr intervalUnit
    ;
    
intervalUnit
    : MICROSECOND | SECOND | MINUTE | HOUR | DAY | WEEK | MONTH
    | QUARTER | YEAR | SECOND_MICROSECOND | MINUTE_MICROSECOND | MINUTE_SECOND | HOUR_MICROSECOND | HOUR_SECOND
    | HOUR_MINUTE | DAY_MICROSECOND | DAY_SECOND | DAY_MINUTE | DAY_HOUR | YEAR_MONTH
    ;
    
subquery
    : 'refer subquery in DMStement.g4'
    ;
    
orderByClause
    : ORDER BY orderByItem (COMMA_ orderByItem)*
    ;
    
orderByItem
    : (numberLiterals | expr) direction?
    ;
    
dataType
    : dataTypeName = (INTEGER | INT | TINYINT | SMALLINT | MIDDLEINT | MEDIUMINT | BIGINT) fieldLength? fieldOptions?
    | (dataTypeName = REAL | dataTypeName = DOUBLE PRECISION?) precision? fieldOptions?
    | dataTypeName = (FLOAT | DECIMAL | NUMERIC | FIXED) (fieldLength | precision)? fieldOptions?
    | dataTypeName = BIT fieldLength?
    | dataTypeName = (BOOL | BOOLEAN)
    | dataTypeName = CHAR fieldLength? charsetWithOptBinary?
    | (dataTypeName = NCHAR | dataTypeName = NATIONAL CHAR) fieldLength? BINARY?
    | dataTypeName = BINARY fieldLength?
    | (dataTypeName = CHAR VARYING | dataTypeName = VARCHAR) fieldLength charsetWithOptBinary?
    | (dataTypeName = NATIONAL VARCHAR | dataTypeName = NVARCHAR | dataTypeName = NCHAR VARCHAR | dataTypeName = NATIONAL CHAR VARYING | dataTypeName = NCHAR VARYING) fieldLength BINARY?
    | dataTypeName = VARBINARY fieldLength?
    | dataTypeName = YEAR fieldLength? fieldOptions?
    | dataTypeName = DATE
    | dataTypeName = TIME typeDatetimePrecision?
    | dataTypeName = TIMESTAMP typeDatetimePrecision?
    | dataTypeName = DATETIME typeDatetimePrecision?
    | dataTypeName = TINYBLOB
    | dataTypeName = BLOB fieldLength?
    | dataTypeName = (MEDIUMBLOB | LONGBLOB)
    | dataTypeName = LONG VARBINARY
    | dataTypeName = LONG (CHAR VARYING | VARCHAR)? charsetWithOptBinary?
    | dataTypeName = TINYTEXT charsetWithOptBinary?
    | dataTypeName = TEXT fieldLength? charsetWithOptBinary?
    | dataTypeName = MEDIUMTEXT charsetWithOptBinary?
    | dataTypeName = LONGTEXT charsetWithOptBinary?
    | dataTypeName = ENUM stringList charsetWithOptBinary?
    | dataTypeName = SET stringList charsetWithOptBinary?
    | dataTypeName = (SERIAL | JSON | GEOMETRY | GEOMETRYCOLLECTION | POINT | MULTIPOINT | LINESTRING | MULTILINESTRING | POLYGON | MULTIPOLYGON)
    ;
    
stringList
    : LP_ textString (COMMA_ textString)* RP_
    ;
    
textString
    : string_
    | HEX_DIGIT_
    | BIT_NUM_
    ;
    
textStringHash
    : string_ | HEX_DIGIT_
    ;
    
fieldOptions
    : (UNSIGNED | SIGNED | ZEROFILL)+
    ;
    
precision
    : LP_ NUMBER_ COMMA_ NUMBER_ RP_
    ;
    
typeDatetimePrecision
    : LP_ NUMBER_ RP_
    ;
    
charsetWithOptBinary
    : ascii
    | unicode
    | BYTE
    | charset charsetName BINARY?
    | BINARY (charset charsetName)?
    ;
    
ascii
    : ASCII BINARY?
    | BINARY ASCII
    ;
    
unicode
    : UNICODE BINARY?
    | BINARY UNICODE
    ;
    
charset
    : (CHAR | CHARACTER) SET
    | CHARSET
    ;
    
defaultCollation
    : DEFAULT? COLLATE EQ_? collationName
    ;
    
defaultEncryption
    : DEFAULT? ENCRYPTION EQ_? string_
    ;
    
defaultCharset
    : DEFAULT? charset EQ_? charsetName
    ;
    
signedLiteral
    : literals
    | (PLUS_ | MINUS_) numberLiterals
    ;
    
now
    : (CURRENT_TIMESTAMP | LOCALTIME | LOCALTIMESTAMP) (LP_ NUMBER_? RP_)?
    ;
    
columnFormat
    : FIXED
    | DYNAMIC
    | DEFAULT
    ;
    
storageMedia
    : DISK
    | MEMORY
    | DEFAULT
    ;
    
direction
    : ASC | DESC
    ;
    
keyOrIndex
    : KEY | INDEX
    ;
    
fieldLength
    : LP_ length=NUMBER_ RP_
    ;
    
characterSet
    : charset charsetName
    ;
    
collateClause
    : COLLATE collationName
    ;
    
fieldOrVarSpec
    : LP_ (identifier (COMMA_ identifier)*)? RP_
    ;
    
notExistClause
    : IF NOT EXISTS
    ;
    
existClause
    : IF EXISTS
    ;
    
connectionId
    : NUMBER_
    ;
    
labelName
    : identifier
    ;
    
cursorName
    : identifier
    ;
    
conditionName
    : identifier
    ;
    
unionOption
    : ALL | DISTINCT
    ;
    
noWriteToBinLog
    : LOCAL
    | NO_WRITE_TO_BINLOG
    ;
    
channelOption
    : FOR CHANNEL string_
    ;
    
preparedStatement
    : PREPARE identifier FROM (stringLiterals | userVariable)
    | executeStatement
    | (DEALLOCATE | DROP) PREPARE identifier
    ;
    
executeStatement
    : EXECUTE identifier (USING executeVarList)?
    ;
    
executeVarList
    : userVariable (COMMA_ userVariable)*
    ;
