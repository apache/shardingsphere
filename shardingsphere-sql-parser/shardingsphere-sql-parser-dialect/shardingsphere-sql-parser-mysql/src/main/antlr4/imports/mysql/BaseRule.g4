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
    : characterSetName? STRING_ collateClause?
    ;

numberLiterals
   : MINUS_? NUMBER_
   ;

dateTimeLiterals
    : (DATE | TIME | TIMESTAMP) STRING_
    | LBE_ identifier STRING_ RBE_
    ;

hexadecimalLiterals
    : characterSetName? HEX_DIGIT_ collateClause?
    ;

bitValueLiterals
    : characterSetName? BIT_NUM_ collateClause?
    ;

booleanLiterals
    : TRUE | FALSE
    ;

nullValueLiterals
    : NULL
    ;

characterSetName
    : IDENTIFIER_
    ;

collationName_
   : IDENTIFIER_
   ;

identifier
    : IDENTIFIER_ | unreservedWord | customKeyword
    ;

unreservedWord
    : ACCOUNT | ACTION | ACTIVE | ADMIN | AFTER | AGAINST | AGGREGATE | ALGORITHM | ALWAYS |  ANY
    | ASCII | AT | ATTRIBUTE | AUTOEXTEND_SIZE | AUTO_INCREMENT | AVG
    | AVG_ROW_LENGTH | BACKUP | BEGIN | BINLOG | BIT | BLOCK | BOOL | BOOLEAN | BTREE | BUCKETS | BYTE
    | CACHE | CASCADED | CATALOG_NAME | CHAIN | CHANGED | CHANNEL | CHARSET | CHECKSUM | CIPHER | CLASS_ORIGIN
    | CLIENT | CLONE | CLOSE | COALESCE | CODE | COLLATION | COLUMNS | COLUMN_FORMAT | COMMENT | COMMIT | COMMITTED
    | COMPACT | COMPLETION | COMPONENT | COMPRESSED | COMPRESSION | CONCURRENT | CONNECTION | CONSISTENT
    | CONSTRAINT_CATALOG | CONSTRAINT_NAME | CONSTRAINT_SCHEMA | CONTAINS | CONTEXT | CPU | CURRENT | CURSOR_NAME
    | DATA | DATAFILE | DATE | DATETIME | DAY | DEFAULT_AUTH | DEFINER | DEFINITION | DEALLOCATE | DELAY_KEY_WRITE
    | DESCRIPTION | DIAGNOSTICS | DIRECTORY | DISABLE | DISCARD | DISK | DO | DUMPFILE | DUPLICATE | DYNAMIC | ENABLE
    | ENCRYPTION | ENFORCED | END | ENDS | ENGINE | ENGINES | ENGINE_ATTRIBUTE | ENUM | ERROR | ERRORS
    | ESCAPE | EVENT | EVENTS | EVERY | EXCHANGE | EXCLUDE | EXECUTE | EXPANSION | EXPIRE | EXPORT | EXTENDED
    | EXTENT_SIZE | FAILED_LOGIN_ATTEMPTS | FAST | FAULTS | FIELDS | FILE | FILE_BLOCK_SIZE | FILTER | FIRST
    | FIXED | FLUSH | FOLLOWING | FOLLOWS | FORMAT | FULL | GENERAL | GEOMCOLLECTION | GEOMETRY | GEOMETRYCOLLECTION
    | GET_FORMAT | GET_MASTER_PUBLIC_KEY | GLOBAL | GRANTS | GROUP_REPLICATION | HANDLER | HASH | HELP | HISTOGRAM | HISTORY | HOST
    | HOSTS | HOUR | IDENTIFIED | IGNORE_SERVER_IDS | IMPORT | INACTIVE | INDEXES | INITIAL_SIZE
    | INSERT_METHOD | INSTALL | INSTANCE | INVISIBLE | INVOKER | IO | IO_THREAD | IPC | ISOLATION | ISSUER
    | JSON | JSON_VALUE | KEY_BLOCK_SIZE | LANGUAGE | LAST | LEAVES | LESS
    | LEVEL | LINESTRING | LIST | LOCAL | LOCKED | LOCKS | LOGFILE | LOGS | MANAGED | MASTER | MASTER_AUTO_POSITION
    | MASTER_COMPRESSION_ALGORITHMS | MASTER_CONNECT_RETRY | MASTER_DELAY | MASTER_HEARTBEAT_PERIOD | MASTER_HOST
    | MASTER_LOG_FILE | MASTER_LOG_POS | MASTER_PASSWORD | MASTER_PUBLIC_KEY_PATH | MASTER_PORT | MASTER_RETRY_COUNT
    | MASTER_SERVER_ID | MASTER_SSL | MASTER_SSL_CA | MASTER_SSL_CAPATH | MASTER_SSL_CERT | MASTER_SSL_CIPHER
    | MASTER_SSL_CRL | MASTER_SSL_CRLPATH | MASTER_SSL_KEY | MASTER_TLS_CIPHERSUITES | MASTER_TLS_VERSION | MASTER_USER
    | MASTER_ZSTD_COMPRESSION_LEVEL | MAX_CONNECTIONS_PER_HOUR | MAX_QUERIES_PER_HOUR | MAX_ROWS | MAX_SIZE | MAX_UPDATES_PER_HOUR
    | MAX_USER_CONNECTIONS | MEDIUM | MEMORY | MERGE | MESSAGE_TEXT | MICROSECOND | MIGRATE | MINUTE | MIN_ROWS | MODE
    | MODIFY | MONTH | MULTILINESTRING | MULTIPOINT | MULTIPOLYGON | MUTEX | MYSQL_ERRNO | NAME | NAMES
    | NATIONAL | NCHAR | NDB | NDBCLUSTER | NESTED | NETWORK_NAMESPACE | NEVER | NEW | NEXT | NO | NODEGROUP
    | NONE | NOWAIT | NO_WAIT | NULLS | NUMBER | NVARCHAR | OFFSET | OFF | OJ | OLD | ONE | ONLY | OPEN | OPTIONAL | OPTIONS
    | ORDINALITY | OTHERS | OWNER | PACK_KEYS | PAGE | PARSER | PARTIAL | PARTITIONING | PASSWORD | PASSWORD_LOCK_TIME
    | PATH | PERSIST | PERSIST_ONLY | PHASE | PLUGIN | PLUGINS | PLUGIN_DIR | POINT | POLYGON | PORT | PRECEDES
    | PRECEDING | PREPARE | PRESERVE | PREV | PRIVILEGES | PRIVILEGE_CHECKS_USER | PROCESS | PROCESSLIST | PROFILE
    | PROFILES | PROXY | QUARTER | QUERY| QUICK | RANDOM | READ_ONLY | REBUILD | RECOVER | REDO_BUFFER_SIZE
    | REDUNDANT | REFERENCE | RELAY | RELAYLOG | RELAY_LOG_FILE | RELAY_LOG_POS | RELAY_THREAD
    | RELOAD | REMOVE | REORGANIZE | REPAIR | REPEATABLE | REPLICATE_DO_DB | REPLICATE_DO_TABLE | REPLICATE_IGNORE_DB
    | REPLICATE_IGNORE_TABLE | REPLICATE_REWRITE_DB | REPLICATE_WILD_DO_TABLE | REPLICATE_WILD_IGNORE_TABLE | REPLICATION
    | REQUIRE_ROW_FORMAT | RESET | RESOURCE | RESPECT | RESTART | RESTORE | RESUME | RETAIN | RETURNED_SQLSTATE
    | RETURNING | RETURNS | REUSE | REVERSE | ROLE | ROLLBACK | ROLLUP | ROTATE | ROUTINE | ROW_COUNT
    | ROW_FORMAT | RTREE | SAVEPOINT | SCHEDULE | SCHEMA_NAME | SECOND | SECONDARY | SECONDARY_ENGINE
    | SECONDARY_ENGINE_ATTRIBUTE | SECONDARY_LOAD | SECONDARY_UNLOAD | SECURITY | SERIAL | SERIALIZABLE | SERVER
    | SESSION | SHARE | SHUTDOWN | SIGNED | SIMPLE | SLAVE | SLOW | SNAPSHOT | SOCKET | SOME | SONAME
    | SOUNDS | SOURCE | SQL_AFTER_GTIDS | SQL_AFTER_MTS_GAPS | SQL_BEFORE_GTIDS | SQL_BUFFER_RESULT | SQL_NO_CACHE
    | SQL_THREAD | SQL_TSI_DAY | SQL_TSI_HOUR | SQL_TSI_MINUTE | SQL_TSI_MONTH | SQL_TSI_QUARTER | SQL_TSI_SECOND
    | SQL_TSI_WEEK | SQL_TSI_YEAR | SRID | STACKED | START | STARTS | STATS_AUTO_RECALC | STATS_PERSISTENT
    | STATS_SAMPLE_PAGES | STATUS | STOP | STORAGE | STREAM | STRING | SUBCLASS_ORIGIN | SUBJECT | SUBPARTITION
    | SUBPARTITIONS | SUPER | SUSPEND | SWAPS | SWITCHES | SQL_CACHE | TABLES | TABLESPACE | TABLE_CHECKSUM
    | TABLE_NAME | TEMPORARY | TEMPTABLE | TEXT | THAN | THREAD_PRIORITY | TIES | TIME | TIMESTAMP | TIMESTAMPADD
    | TIMESTAMPDIFF | TLS | TRANSACTION | TRIGGERS | TRUNCATE | TYPE | TYPES | UNBOUNDED | UNCOMMITTED | UNDEFINED
    | UNDOFILE | UNDO_BUFFER_SIZE | UNICODE | UNINSTALL | UNKNOWN | UNTIL
    | UPGRADE | USER | USER_RESOURCES | USE_FRM | VALIDATION | VALUE | VARIABLES | VCPU | VIEW | VISIBLE
    | WAIT | WARNINGS | WEEK | WEIGHT_STRING | WITHOUT | WORK | WRAPPER | X509 | XA | XID | XML | YEAR
    ;

variable
    : (AT_? AT_)? scope? DOT_? internalVariableName
    ;

scope
    : GLOBAL | PERSIST | PERSIST_ONLY | SESSION | LOCAL
    ;

internalVariableName
    : identifier
    | DEFAULT DOT_ identifier
    | identifier DOT_ identifier
    ;

setExprOrDefault
    : expr | DEFAULT | ALL | BINARY | ROW | SYSTEM
    ;

schemaName
    : identifier
    ;

schemaNames
    : schemaName (COMMA_ schemaName)*
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
    : (owner DOT_)? name
    ;

indexName
    : identifier
    ;

userName
    : STRING_  AT_ STRING_
    | identifier
    | STRING_
    ;

eventName
    : (STRING_ | IDENTIFIER_) AT_ (STRING_ IDENTIFIER_)
    | identifier
    | STRING_ 
    ;

serverName
    : identifier
    | STRING_
    ; 

wrapperName
    : identifier
    | STRING_
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
    : identifier | STRING_
    ;

name
    : identifier
    ;

tableNames
    : LP_? tableName (COMMA_ tableName)* RP_?
    ;
    
viewNames
    : viewName (COMMA_ viewName)*
    ;

columnNames
    : LP_? columnName (COMMA_ columnName)* RP_?
    ;

groupName
    : IDENTIFIER_
    ;

routineName
    : identifier
    ;

shardLibraryName
    : STRING_
    ;

componentName
    : STRING_
    ;

pluginName
    : IDENTIFIER_
    ;

hostName
    : STRING_
    ;

port
    : NUMBER_
    ;

cloneInstance
    : userName AT_ hostName COLON_ port
    ;

cloneDir
    : IDENTIFIER_
    ;

channelName
    : IDENTIFIER_
    ;

logName
    : identifier
    ;

roleName
    : (STRING_ | IDENTIFIER_) AT_ (STRING_ IDENTIFIER_) | IDENTIFIER_
    ;

engineName
    : IDENTIFIER_
    ;

triggerName
    : IDENTIFIER_
    ;

triggerTime
    : BEFORE | AFTER
    ;

userOrRole
    : userName | roleName
    ;

partitionName
    : IDENTIFIER_
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
    | columnName
    | simpleExpr COLLATE (STRING_ | identifier)
    | variable
    | simpleExpr OR_ simpleExpr
    | (PLUS_ | MINUS_ | TILDE_ | notOperator | BINARY) simpleExpr
    | ROW? LP_ expr (COMMA_ expr)* RP_
    | EXISTS? subquery
    | LBE_ identifier expr RBE_
    | identifier (JSON_SEPARATOR | JSON_UNQUOTED_SEPARATOR) STRING_
    | matchExpression
    | caseExpression
    | intervalExpression
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
    : identifier? partitionClause? orderByClause? frameClause?
    ;

partitionClause
    : PARTITION BY expr (COMMA_ expr)*
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
    : identifier LP_ expr (COMMA_ expr)* RP_ overClause
    ;

castFunction
    : CAST LP_ expr AS dataType RP_
    ;

convertFunction
    : CONVERT LP_ expr COMMA_ dataType RP_
    | CONVERT LP_ expr USING identifier RP_ 
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
    : CHAR LP_ expr (COMMA_ expr)* (USING ignoredIdentifier)? RP_
    ;

trimFunction
    : TRIM LP_ (LEADING | BOTH | TRAILING) STRING_ FROM STRING_ RP_
    ;

valuesFunction
    : VALUES LP_ columnName RP_
    ;

weightStringFunction
    : WEIGHT_STRING LP_ expr (AS dataType)? levelClause? RP_
    ;

levelClause
    : LEVEL (levelInWeightListElement (COMMA_ levelInWeightListElement)* | NUMBER_ MINUS_ NUMBER_)
    ;

levelInWeightListElement
    : NUMBER_ (ASC | DESC)? REVERSE?
    ;

regularFunction
    : completeRegularFunction
    | shorthandRegularFunction
    ;
    
shorthandRegularFunction
    : CURRENT_DATE | CURRENT_TIME | CURRENT_TIMESTAMP | LAST_DAY | LOCALTIME | LOCALTIMESTAMP
    ;
  
completeRegularFunction
    : regularFunctionName (LP_ (expr (COMMA_ expr)* | ASTERISK_)? RP_)
    ;
    
regularFunctionName
    : IF | LOCALTIME | LOCALTIMESTAMP | REPLACE | INTERVAL | MOD
    | DATABASE | LEFT | RIGHT | DATE | DAY | GEOMCOLLECTION | GEOMETRYCOLLECTION
    | LINESTRING | MULTILINESTRING | MULTIPOINT | MULTIPOLYGON | POINT | POLYGON
    | TIME | TIMESTAMP | TIMESTAMPADD | TIMESTAMPDIFF | DATE | CURRENT_TIMESTAMP | identifier
    ;

matchExpression
    : MATCH columnNames AGAINST LP_ expr matchSearchModifier? RP_
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
    : (columnName | numberLiterals | expr) (ASC | DESC)?
    ;

dataType
    : dataTypeName dataTypeLength? characterSet? collateClause? (UNSIGNED | SIGNED)? ZEROFILL? | dataTypeName collectionOptions characterSet? collateClause?
    ;

dataTypeName
    : INTEGER | INT | SMALLINT | TINYINT | MEDIUMINT | BIGINT | DECIMAL| NUMERIC | FLOAT | DOUBLE | BIT | BOOL | BOOLEAN
    | DEC | DATE | DATETIME | TIMESTAMP | TIME | YEAR | CHAR | VARCHAR | BINARY | VARBINARY | TINYBLOB | TINYTEXT | BLOB
    | TEXT | MEDIUMBLOB | MEDIUMTEXT | LONGBLOB | LONGTEXT | ENUM | SET | GEOMETRY | POINT | LINESTRING | POLYGON
    | MULTIPOINT | MULTILINESTRING | MULTIPOLYGON | GEOMETRYCOLLECTION | JSON | UNSIGNED | SIGNED | CHARACTER VARYING
    | FIXED | FLOAT4 | FLOAT8 | INT1 | INT2 | INT3 | INT4 | INT8 | LONG VARBINARY | LONG VARCHAR | LONG | MIDDLEINT
    ;

dataTypeLength
    : LP_ NUMBER_ (COMMA_ NUMBER_)? RP_
    ;

collectionOptions
    : LP_ STRING_ (COMMA_ STRING_)* RP_
    ;

characterSet
    : (CHARSET | CHAR SET | CHARACTER SET) EQ_? ignoredIdentifier
    ;

collateClause
    : COLLATE EQ_? (STRING_ | ignoredIdentifier)
    ;

ignoredIdentifier
    : identifier (DOT_ identifier)?
    ;

ignoredIdentifiers_
    : ignoredIdentifier (COMMA_ ignoredIdentifier)*
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

pattern
    : STRING_
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
