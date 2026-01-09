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

import Comments, Symbol, Keyword, MySQLKeyword, Literals;

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
    | MID
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
    | ARCHIVE
    | BLACKHOLE
    | CSV
    | FEDERATED
    | INNODB
    | MEMORY
    | MRG_MYISAM
    | MYISAM
    | NDB
    | NDBCLUSTER
    | PERFORMANCE_SCHEMA
    | TOKUDB
    | REDO_LOG
    | LAST_VALUE
    | PRIMARY
    | MAXVALUE
    | BIT_XOR
    | MYSQL_MAIN
    | RANGE
    | UTC_DATE
    | UTC_TIME
    | UTC_TIMESTAMP
    | UTC_TIMESTAMP
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
    : (UNDERSCORE_CHARSET | UL_BINARY )? string_+ | NCHAR_TEXT
    ;

numberLiterals
    : (PLUS_ | MINUS_)? NUMBER_
    ;

temporalLiterals
    : (DATE | TIME | TIMESTAMP) textString
    ;

hexadecimalLiterals
    : UNDERSCORE_CHARSET? UL_BINARY? HEX_DIGIT_ collateClause?
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
    | unreservedWord
    | DOUBLE_QUOTED_TEXT
    | UNDERSCORE_CHARSET
    | BQUOTA_STRING
    ;

unreservedWord
    : MAX | MIN | SUM | COUNT | GROUP_CONCAT | CAST | POSITION | SUBSTRING | SUBSTR | MID | EXTRACT | TRIM | LAST_DAY | TRADITIONAL | TREE | MYSQL_MAIN | MYSQL_ADMIN | INSTANT | INPLACE | COPY | UL_BINARY | AUTOCOMMIT | REDO_LOG | DELIMITER | ARCHIVE | BLACKHOLE | CSV | FEDERATED | INNODB | MEMORY | MRG_MYISAM | MYISAM | NDB | NDBCLUSTER | PERFORMANCE_SCHEMA | TOKUDB
    | ACCESSIBLE | ACCOUNT | ACTION | ACTIVE | ADD | ADMIN | AFTER | AGAINST | AGGREGATE | ALGORITHM | ALL | ALTER | ALWAYS | ANALYZE | AND | ANY | ARRAY | AS | ASC | ASCII | ASENSITIVE | AT | ATTRIBUTE | AUTOEXTEND_SIZE | AUTHENTICATION | AUTO | AUTO_INCREMENT | AVG | ASSIGN_GTIDS_TO_ANONYMOUS_TRANSACTIONS | BIT_XOR | AVG_ROW_LENGTH | BACKUP | BEFORE | BERNOULLI | BEGIN | BETWEEN | BIGINT | BINARY | BINLOG | BIT | BLOB | BLOCK | BOOL | BOOLEAN | BOTH | BTREE | BUCKETS | BULK | BY | BYTE | CACHE | CALL | CASCADE | CASCADED | CASE | CATALOG_NAME | CHAIN | CHANGE | CHANGED | CHANNEL | CHALLENGE_RESPONSE | CHAR | CHAR_VARYING | CHARACTER | CHARACTER_VARYING | CHARSET | CHECK | CHECKSUM | CIPHER | CLASS_ORIGIN | CLIENT | CLONE | CLOSE | COALESCE | CODE | COLLATE | COLLATION | COLUMN | COLUMNS | COLUMN_FORMAT | COLUMN_NAME | COMMENT | COMMIT | COMMITTED | COMPACT | COMPLETION | COMPONENT | COMPRESSED | COMPRESSION | CONCURRENT | CONDITION | CONNECTION | CONSISTENT | CONSTRAINT | CONSTRAINT_CATALOG | CONSTRAINT_NAME | CONSTRAINT_SCHEMA | CONTAINS | CONTEXT | CONTINUE | CONVERT | CPU | CREATE | CROSS | CUBE | CUME_DIST | CURRENT | CURRENT_DATE | CURRENT_TIME | CURRENT_TIMESTAMP | CURRENT_USER | CURSOR | CURSOR_NAME | DATA | DATABASE | DATABASES | DATAFILE | DATE | DATETIME | DAY | DAY_HOUR | DAY_MICROSECOND | DAY_MINUTE | DAY_SECOND | DEALLOCATE | DEC | DECIMAL | DECLARE | DEFAULT | DEFAULT_AUTH | DEFINER | DEFINITION | DELAYED | DELAY_KEY_WRITE | DELETE | DENSE_RANK | DESC | DESCRIBE | DESCRIPTION | DETERMINISTIC | DIAGNOSTICS | DIRECTORY | DISABLE | DISCARD | DISK | DISTINCT | DISTINCTROW | DIV | DO | DOUBLE | DROP | DUAL | DUMPFILE | DUPLICATE | DYNAMIC | EACH | ELSE | ELSEIF | EMPTY | ENABLE | ENCLOSED | ENCRYPTION | END | ENDS | ENFORCED | ENGINE | ENGINES | ENGINE_ATTRIBUTE | ENUM | ERROR | ERRORS | ESCAPE | ESCAPED | EVENT | EVENTS | EVERY | EXCEPT | EXCHANGE | EXCLUDE | EXECUTE | EXISTS | EXIT | EXPANSION | EXPIRE | EXPLAIN | EXPORT | EXTENDED | EXTENT_SIZE | FAILED_LOGIN_ATTEMPTS | FALSE | FAST | FAULTS | FETCH | FIELDS | FILE | FILE_BLOCK_SIZE | FILTER | FINISH | FIRST | FIRST_VALUE | FIXED | FLOAT | FLOAT4 | FLOAT8 | FLUSH | FOLLOWING | FOLLOWS | FOR | FORCE | FOREIGN | FORMAT | FOUND | FROM | FULL | FULLTEXT | FUNCTION | GENERAL | GENERATED | GEOMETRY | GEOMCOLLECTION | GEOMETRYCOLLECTION | GET | GET_FORMAT | GET_MASTER_PUBLIC_KEY | GLOBAL | GRANT | GRANTS | GROUP | GROUPING | GROUPS | GTIDS | GROUP_REPLICATION | GET_SOURCE_PUBLIC_KEY | GTID_ONLY | GENERATE | HANDLER | HASH | HAVING | HELP | HIGH_PRIORITY | HISTOGRAM | HISTORY | HOST | HOSTS | HOUR | HOUR_MICROSECOND | HOUR_MINUTE | HOUR_SECOND | IDENTIFIED | IF | IGNORE | IGNORE_SERVER_IDS | IMPORT | IN | INACTIVE | INDEX | INDEXES | INITIAL | INFILE | INITIAL_SIZE | INNER | INOUT | INSENSITIVE | INSERT | INSERT_METHOD | INSTALL | INSTANCE | INT | INT1 | INT2 | INT3 | INT4 | INT8 | INTEGER | INTERSECT | INTERVAL | INTO | INVISIBLE | INVOKER | IO | IO_AFTER_GTIDS | IO_BEFORE_GTIDS | IO_THREAD | IPC | IS | ISOLATION | ISSUER | ITERATE | JOIN | JSON | JSON_TABLE | JSON_VALUE | KEY | KEYS | KEY_BLOCK_SIZE | KILL | KEYRING | LAG | LANGUAGE | LAST | LAST_VALUE | LATERAL | LEAD | LEADING | LEAVE | LEAVES | LEFT | LESS | LEVEL | LIKE | LIMIT | LINEAR | LINES | LINESTRING | LIST | LOAD | LOCAL | LOCALTIME | LOCALTIMESTAMP | LOCK | LOCKED | LOCKS | LOGFILE | LOGS | LONG | LONGBLOB | LONGTEXT | LONG_CHAR_VARYING | LONG_VARCHAR | LOOP | LOW_PRIORITY | MASTER | MASTER_AUTO_POSITION | MASTER_BIND | MASTER_COMPRESSION_ALGORITHM | MASTER_CONNECT_RETRY | MASTER_DELAY | MASTER_HEARTBEAT_PERIOD | MASTER_HOST | MASTER_LOG_FILE | MASTER_LOG_POS | MASTER_PASSWORD | MASTER_PORT | MASTER_PUBLIC_KEY_PATH | MASTER_RETRY_COUNT | MASTER_SERVER_ID | MASTER_SSL | MASTER_SSL_CA | MASTER_SSL_CAPATH | MASTER_SSL_CERT | MASTER_SSL_CIPHER | MASTER_SSL_CRL | MASTER_SSL_CRLPATH | MASTER_SSL_KEY | MASTER_SSL_VERIFY_SERVER_CERT | MASTER_TLS_CIPHERSUITES | MASTER_TLS_VERSION | MASTER_USER | MASTER_ZSTD_COMPRESSION_LEVEL | MANUAL | MATCH | MAXVALUE | MAX_CONNECTIONS_PER_HOUR | MAX_QUERIES_PER_HOUR | MAX_ROWS | MAX_SIZE | MAX_UPDATES_PER_HOUR | MAX_USER_CONNECTIONS | MEDIUM | MEDIUMBLOB | MEDIUMINT | MEDIUMTEXT | MEMBER | MEMORY | MERGE | MESSAGE_TEXT | MICROSECOND | MIDDLEINT | MIGRATE | MINUTE | MINUTE_MICROSECOND | MINUTE_SECOND | MIN_ROWS | MOD | MODE | MODIFIES | MODIFY | MONTH | MULTILINESTRING | MULTIPOINT | MULTIPOLYGON | MUTEX | MYSQL_ERRNO | NAME | NAMES | NATIONAL | NATIONAL_CHAR | NATIONAL_CHAR_VARYING | NATURAL | NCHAR | NDB | NDBCLUSTER | NESTED | NETWORK_NAMESPACE | NEVER | NEW | NEXT | NO | NODEGROUP | NONE | SHARED | EXCLUSIVE | NOT | NOWAIT | NO_WAIT | NO_WRITE_TO_BINLOG | NTH_VALUE | NTILE | NULL | NULLS | NUMBER | NUMERIC | NVARCHAR | OF | OFF | OFFSET | OJ | OLD | ON | ONE | ONLY | OPEN | OPTIMIZE | OPTIMIZER_COSTS | OPTION | OPTIONAL | OPTIONALLY | OPTIONS | OR | ORDER | ORDINALITY | ORGANIZATION | OTHERS | OUT | OUTER | OUTFILE | OVER | OWNER | PACK_KEYS | PAGE | PARSER | PARTIAL | PARSE_TREE | PARTITION | PARTITIONING | PARTITIONS | PASSWORD | PASSWORD_LOCK_TIME | PATH | PERCENT_RANK | PERSIST | PERSIST_ONLY | PHASE | PLUGIN | PLUGINS | PLUGIN_DIR | POINT | POLYGON | PORT | PRECEDES | PRECEDING | PRECISION | PREPARE | PRESERVE | PREV | PRIMARY | PRIVILEGES | PRIVILEGE_CHECKS_USER | PROCEDURE | PROCESS | PROCESSLIST | PROFILE | PROFILES | PROXY | PURGE | QUALIFY | QUARTER | QUERY | QUICK | RANDOM | RANGE | RANK | READ | READS | READ_ONLY | READ_WRITE | REAL | REBUILD | RECOVER | RECURSIVE | REDO_BUFFER_SIZE | REDUNDANT | REFERENCE | REFERENCES | REGEXP | RELAY | RELAYLOG | RELAY_LOG_FILE | RELAY_LOG_POS | RELAY_THREAD | RELEASE | RELOAD | REMOVE | RENAME | REORGANIZE | REPAIR | REPEAT | REPEATABLE | REPLACE | REPLICA | REPLICAS | REPLICATE_DO_DB | REPLICATE_DO_TABLE | REPLICATE_IGNORE_DB | REPLICATE_IGNORE_TABLE | REPLICATE_REWRITE_DB | REPLICATE_WILD_DO_TABLE | REPLICATE_WILD_IGNORE_TABLE | REPLICATION | REQUIRE | REQUIRE_ROW_FORMAT | REQUIRE_TABLE_PRIMARY_KEY_CHECK | RESET | RESIGNAL | RESOURCE | RESPECT | RESTART | RESTORE | RESTRICT | RESUME | RETAIN | REGISTRATION | RETURN | RETURNED_SQLSTATE | RETURNING | RETURNS | REUSE | REVERSE | REVOKE | RIGHT | RLIKE | ROLE | ROLLBACK | ROLLUP | ROTATE | ROUTINE | ROW | ROWS | ROW_COUNT | ROW_FORMAT | ROW_NUMBER | RTREE | S3 | SAVEPOINT | SCHEDULE | SCHEMA | SCHEMAS | SCHEMA_NAME | SECOND | SECONDARY | SECONDARY_ENGINE | SECONDARY_ENGINE_ATTRIBUTE | SECONDARY_LOAD | SECONDARY_UNLOAD | SECOND_MICROSECOND | SECURITY | SELECT | SENSITIVE | SEPARATOR | SERIAL | SERIALIZABLE | SERVER | SESSION | SET | SHARE | SHOW | SHUTDOWN | SIGNAL | SIGNED | SIGNED_INT | SIGNED_INTEGER | SIMPLE | SKIP_SYMBOL | SLAVE | SLOW | SMALLINT | SNAPSHOT | SOCKET | SOME | SONAME | SOUNDS | SOURCE | SPATIAL | SPECIFIC | SQL | SQLEXCEPTION | SQLSTATE | SQLWARNING | SQL_AFTER_GTIDS | SQL_AFTER_MTS_GAPS | SQL_BEFORE_GTIDS | SQL_BIG_RESULT | SQL_BUFFER_RESULT | SQL_CALC_FOUND_ROWS | SQL_NO_CACHE | SQL_SMALL_RESULT | SQL_THREAD | SQL_TSI_DAY | SQL_TSI_HOUR | SQL_TSI_MINUTE | SQL_TSI_MONTH | SQL_TSI_QUARTER | SQL_TSI_SECOND | SQL_TSI_WEEK | SQL_TSI_YEAR | SRID | SSL | STACKED | START | STARTING | STARTS | STATS_AUTO_RECALC | STATS_PERSISTENT | STATS_SAMPLE_PAGES | STATUS | STOP | STORAGE | STORED | STRAIGHT_JOIN | STREAM | STRING | SUBCLASS_ORIGIN | SUBJECT | SUBPARTITION | SUBPARTITIONS | SUPER | SUSPEND | SWAPS | SWITCHES | SYSTEM | SOURCE_BIND | SOURCE_HOST | SOURCE_USER | SOURCE_PASSWORD | SOURCE_PORT | SOURCE_LOG_FILE | SOURCE_LOG_POS | SOURCE_AUTO_POSITION | SOURCE_HEARTBEAT_PERIOD | SOURCE_CONNECT_RETRY | SOURCE_RETRY_COUNT | SOURCE_CONNECTION_AUTO_FAILOVER | SOURCE_DELAY | SOURCE_COMPRESSION_ALGORITHMS | SOURCE_ZSTD_COMPRESSION_LEVEL | SOURCE_SSL | SOURCE_SSL_CA | SOURCE_SSL_CAPATH | SOURCE_SSL_CERT | SOURCE_SSL_CRL | SOURCE_SSL_CRLPATH | SOURCE_SSL_KEY | SOURCE_SSL_CIPHER | SOURCE_SSL_VERIFY_SERVER_CERT | SOURCE_TLS_VERSION | SOURCE_TLS_CIPHERSUITES | SOURCE_PUBLIC_KEY_PATH | TABLE | TABLES | TABLESPACE | TABLE_CHECKSUM | TABLE_NAME | TEMPORARY | TEMPTABLE | TERMINATED | TEXT | THAN | THEN | THREAD_PRIORITY | TIES | TIME | TIMESTAMP | TIMESTAMP_ADD | TIMESTAMP_DIFF | TINYBLOB | TINYINT | TINYTEXT | TLS | TO | TRAILING | TRANSACTION | TRIGGER | TRIGGERS | TRUE | TRUNCATE | TYPE | TYPES | UNBOUNDED | UNCOMMITTED | UNDEFINED | UNDO | UNDOFILE | UNDO_BUFFER_SIZE | UNICODE | UNINSTALL | UNION | UNIQUE | UNKNOWN | UNLOCK | UNSIGNED | UNSIGNED_INT | UNSIGNED_INTEGER | UNTIL | UPDATE | UPGRADE | USAGE | USE | URL | USER | USER_RESOURCES | USE_FRM | USING | UTC_DATE | UTC_TIME | UTC_TIMESTAMP | VALIDATION | VALUE | VALUES | VARBINARY | VARCHAR | VARCHARACTER | VARIABLES | VARYING | VCPU | VIEW | VIRTUAL | VISIBLE | WAIT | WARNINGS | WEEK | WEIGHT_STRING | WHEN | WHERE | WHILE | WINDOW | WITH | WITHOUT | WORK | WRAPPER | WRITE | X509 | XA | XID | XML | XOR | YEAR | YEAR_MONTH | ZEROFILL | JSON_ARRAY | JSON_ARRAY_APPEND | JSON_ARRAY_INSERT | JSON_CONTAINS | JSON_CONTAINS_PATH | JSON_DEPTH | JSON_EXTRACT | JSON_INSERT | JSON_KEYS | JSON_LENGTH | JSON_MERGE | JSON_MERGE_PATCH | JSON_MERGE_PRESERVE | JSON_OBJECT | JSON_OVERLAPS | JSON_PRETTY | JSON_QUOTE | JSON_REMOVE | JSON_REPLACE | JSON_SCHEMA_VALID | JSON_SCHEMA_VALIDATION_REPORT | JSON_SEARCH | JSON_SET | JSON_STORAGE_FREE | JSON_STORAGE_SIZE | JSON_TYPE | JSON_UNQUOTE | JSON_VALID | ZONE | TIMESTAMPDIFF | AUTHENTICATION_FIDO | FACTOR
    ;

textOrIdentifier
    : identifier | string_ | ipAddress
    ;

ipAddress
    : IP_ADDRESS
    ;

variable
    : userVariable | systemVariable
    ;

userVariable
    : AT_ textOrIdentifier
    | textOrIdentifier
    ;

systemVariable
    : AT_ AT_ (systemVariableScope=(GLOBAL | SESSION | LOCAL) DOT_)? rvalueSystemVariable
    ;

rvalueSystemVariable
    : textOrIdentifier
    | textOrIdentifier DOT_ identifier
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

databaseName
    : identifier
    ;

databaseNames
    : databaseName (COMMA_ databaseName)*
    ;

charsetName
    : textOrIdentifier | BINARY | DEFAULT
    ;

databasePairs
    : databasePair (COMMA_ databasePair)*
    ;

databasePair
    : LP_ databaseName COMMA_ databaseName RP_
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

oldColumn
    : columnName
    ;

newColumn
    : columnName
    ;

delimiterName
    : textOrIdentifier | ('\\'. | ~('\'' | '"' | '`' | '\\'))+
    ;

userIdentifierOrText
    : textOrIdentifier (AT_ textOrIdentifier)?
    ;

username
    : userIdentifierOrText | CURRENT_USER (LP_ RP_)?
    ;

eventName
    : (owner DOT_)? identifier
    ;

serverName
    : textOrIdentifier
    ;

wrapperName
    : textOrIdentifier
    ;

functionName
    : (owner DOT_)? identifier
    ;

procedureName
    : (owner DOT_)? identifier
    ;

viewName
    : (owner DOT_)? identifier
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
    : textOrIdentifier
    ;

hostname
    : string_
    ;

port
    : NUMBER_
    ;

cloneInstance
    : username AT_ hostname COLON_ port
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
    : username | roleName
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
    | expr andOperator expr
    | expr orOperator expr
    | expr XOR expr
    | notOperator expr
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
    | booleanPrimary MEMBER OF LP_ (expr) RP_
    | booleanPrimary comparisonOperator predicate
    | booleanPrimary comparisonOperator (ALL | ANY) subquery
    | booleanPrimary assignmentOperator predicate
    | predicate
    ;

assignmentOperator
    : EQ_ | ASSIGNMENT_
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
    | bitExpr NOT? (REGEXP | RLIKE) bitExpr
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
    : parameterMarker
    | literals
    | simpleExpr collateClause
    | simpleExpr VERTICAL_BAR_ VERTICAL_BAR_ simpleExpr
    | (PLUS_ | MINUS_ | TILDE_ | notOperator | BINARY) simpleExpr
    | ROW? LP_ expr (COMMA_ expr)* RP_
    | EXISTS? subquery
    | LBE_ identifier expr RBE_
    | path (RETURNING dataType)? onEmpty? onError?
    | matchExpression
    | caseExpression
    | intervalExpression
    | implicitConcat
    | functionCall
    | columnRef
    | variable
    ;

path
    : string_
    ;

onEmpty
    : (NULL | ERROR | DEFAULT literals) ON EMPTY
    ;

onError
    : (NULL | ERROR | DEFAULT literals) ON ERROR
    ;

columnRef
    : identifier (DOT_ identifier)? (DOT_ identifier)?
    ;

columnRefList
    : columnRef (COMMA_ columnRef)*
    ;

functionCall
    : aggregationFunction | specialFunction | jsonFunction | regularFunction | udfFunction | specialAnalysisFunction
    ;

udfFunction
    : functionName LP_ (expr? | expr (COMMA_ expr)*) RP_
    ;

separatorName
    : SEPARATOR string_
    ;

aggregationExpression
    : expr (COMMA_ expr)* | ASTERISK_
    ;

aggregationFunction
    : aggregationFunctionName LP_ (distinct | all)? aggregationExpression? collateClause? separatorName? RP_ overClause?
    ;

jsonFunction
    : jsonTableFunction
    | jsonFunctionName LP_ (expr? | expr (COMMA_ expr)*) RP_
    | columnRef (JSON_SEPARATOR | JSON_UNQUOTED_SEPARATOR) path
    ;

jsonTableFunction
    : JSON_TABLE LP_ expr COMMA_ path jsonTableColumns RP_
    ;

jsonTableColumns
    : COLUMNS LP_ jsonTableColumn (COMMA_ jsonTableColumn)* RP_
    ;

jsonTableColumn
    : name FOR ORDINALITY
    | name dataType PATH path jsonTableColumnOnEmpty? jsonTableColumnOnError?
    | name dataType EXISTS PATH string_ path
    | NESTED PATH? path COLUMNS
    ;

jsonTableColumnOnEmpty
    : (NULL | DEFAULT string_ | ERROR) ON EMPTY
    ;

jsonTableColumnOnError
    : (NULL | DEFAULT string_ | ERROR) ON ERROR
    ;

jsonFunctionName
    : JSON_ARRAY | JSON_ARRAY_APPEND |  JSON_ARRAY_INSERT |  JSON_CONTAINS
    | JSON_CONTAINS_PATH | JSON_DEPTH | JSON_EXTRACT | JSON_INSERT | JSON_KEYS | JSON_LENGTH | JSON_MERGE | JSON_MERGE_PATCH
    | JSON_MERGE_PRESERVE | JSON_OBJECT | JSON_OVERLAPS | JSON_PRETTY | JSON_QUOTE | JSON_REMOVE | JSON_REPLACE
    | JSON_SCHEMA_VALID | JSON_SCHEMA_VALIDATION_REPORT | JSON_SEARCH | JSON_SET | JSON_STORAGE_FREE | JSON_STORAGE_SIZE
    | JSON_TYPE | JSON_UNQUOTE | JSON_VALID | JSON_VALUE | MEMBER OF
    ;

aggregationFunctionName
    : MAX | MIN | SUM | COUNT | AVG | BIT_XOR | GROUP_CONCAT
    ;

distinct
    : DISTINCT
    ;

all
    : ALL
    ;

overClause
    : OVER (windowSpecification | identifier)
    ;

windowSpecification
    : LP_ identifier? (PARTITION BY expr (COMMA_ expr)*)? orderByClause? frameClause? RP_
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
    : castFunction
    | convertFunction
    | currentUserFunction
    | charFunction
    | extractFunction
    | groupConcatFunction
    | positionFunction
    | substringFunction
    | trimFunction
    | valuesFunction
    | weightStringFunction
    | windowFunction
    | groupingFunction
    | timeStampAddFunction
    | timeStampDiffFunction
    ;

currentUserFunction
    : CURRENT_USER (LP_ RP_)?
    ;

groupingFunction
    : GROUPING LP_ expr (COMMA_ expr)* RP_
    ;

timeStampAddFunction
    : TIMESTAMPADD LP_ intervalUnit COMMA_ expr COMMA_ expr RP_
    ;

timeStampDiffFunction
    : TIMESTAMPDIFF LP_ intervalUnit COMMA_ expr COMMA_ expr RP_
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
    : CAST LP_ expr AS castType ARRAY? RP_
    | CAST LP_ expr AT TIME ZONE expr AS DATETIME typeDatetimePrecision? RP_
    ;

convertFunction
    : CONVERT LP_ expr COMMA_ castType RP_
    | CONVERT LP_ expr USING charsetName RP_
    ;

castType
    : castTypeName = BINARY fieldLength?
    | castTypeName = CHAR fieldLength? charsetWithOptBinary?
    | (castTypeName = NCHAR | castTypeName = NATIONAL_CHAR) fieldLength?
    | castTypeName = (SIGNED | SIGNED_INT | SIGNED_INTEGER)
    | castTypeName = (UNSIGNED | UNSIGNED_INT | UNSIGNED_INTEGER)
    | castTypeName = DATE
    | castTypeName = TIME typeDatetimePrecision?
    | castTypeName = DATETIME typeDatetimePrecision?
    | castTypeName = DECIMAL (fieldLength | precision)?
    | castTypeName = JSON
    | castTypeName = REAL
    | castTypeName = DOUBLE PRECISION?
    | castTypeName = FLOAT precision?
    | castTypeName = YEAR
    ;

positionFunction
    : POSITION LP_ expr IN expr RP_
    ;

substringFunction
    : (SUBSTRING | SUBSTR | MID) LP_ expr FROM substringParam (FOR substringParam)? RP_
    | (SUBSTRING | SUBSTR | MID) LP_ expr COMMA_ substringParam (COMMA_ substringParam)? RP_
    ;

substringParam
    : numberLiterals | expr
    ;

extractFunction
    : EXTRACT LP_ intervalUnit FROM expr RP_
    ;

charFunction
    : CHAR LP_ expr (COMMA_ expr)* (USING charsetName)? RP_
    ;

trimFunction
    : TRIM LP_ ((LEADING | BOTH | TRAILING) expr? FROM)? expr RP_
    | TRIM LP_ (expr FROM)? expr RP_
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
    : IF | LOCALTIME | LOCALTIMESTAMP | REPLACE | INSERT | INTERVAL | MOD
    | DATABASE | SCHEMA | LEFT | RIGHT | DATE | DAY | GEOMETRYCOLLECTION | REPEAT
    | LINESTRING | MULTILINESTRING | MULTIPOINT | MULTIPOLYGON | POINT | POLYGON
    | TIME | TIMESTAMP | TIMESTAMP_ADD | TIMESTAMP_DIFF | DATE | CURRENT_TIMESTAMP
    | CURRENT_DATE | CURRENT_TIME | UTC_TIMESTAMP | identifier
    ;

specialAnalysisFunction
    : geomCollectionFunction
    ;

geomCollectionFunction
    : GEOMCOLLECTION LP_ expr (COMMA_ expr)* RP_
    ;

matchExpression
    : MATCH (columnRefList | LP_ columnRefList RP_ ) AGAINST LP_ expr matchSearchModifier? RP_
    ;

matchSearchModifier
    : IN NATURAL LANGUAGE MODE | IN NATURAL LANGUAGE MODE WITH QUERY EXPANSION | IN BOOLEAN MODE | WITH QUERY EXPANSION
    ;

caseExpression
    : CASE expr? caseWhen+ caseElse? END
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

implicitConcat
    : string_ (string_)*
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
    | dataTypeName = (FLOAT | DECIMAL | DEC | NUMERIC | FIXED) (fieldLength | precision)? fieldOptions?
    | dataTypeName = BIT fieldLength?
    | dataTypeName = (BOOL | BOOLEAN)
    | dataTypeName = CHAR fieldLength? charsetWithOptBinary?
    | (dataTypeName = NCHAR | dataTypeName = NATIONAL_CHAR) fieldLength? BINARY?
    | dataTypeName = (SIGNED | SIGNED_INT | SIGNED_INTEGER)
    | dataTypeName = BINARY fieldLength?
    | (dataTypeName = CHAR_VARYING | dataTypeName = CHARACTER_VARYING | dataTypeName = VARCHAR) fieldLength charsetWithOptBinary?
    | (dataTypeName = NATIONAL VARCHAR | dataTypeName = NVARCHAR | dataTypeName = NCHAR VARCHAR | dataTypeName = NATIONAL_CHAR_VARYING | dataTypeName = NCHAR VARYING) fieldLength BINARY?
    | dataTypeName = VARBINARY fieldLength?
    | dataTypeName = YEAR fieldLength? fieldOptions?
    | dataTypeName = DATE
    | dataTypeName = TIME typeDatetimePrecision?
    | dataTypeName = (UNSIGNED | UNSIGNED_INT | UNSIGNED_INTEGER)
    | dataTypeName = TIMESTAMP typeDatetimePrecision?
    | dataTypeName = DATETIME typeDatetimePrecision?
    | dataTypeName = TINYBLOB
    | dataTypeName = BLOB fieldLength?
    | dataTypeName = (MEDIUMBLOB | LONGBLOB)
    | dataTypeName = LONG VARBINARY
    | dataTypeName = (LONG_CHAR_VARYING | LONG_VARCHAR)? charsetWithOptBinary?
    | dataTypeName = TINYTEXT charsetWithOptBinary?
    | dataTypeName = TEXT fieldLength? charsetWithOptBinary?
    | dataTypeName = MEDIUMTEXT charsetWithOptBinary?
    | dataTypeName = LONGTEXT charsetWithOptBinary?
    | dataTypeName = ENUM stringList charsetWithOptBinary?
    | dataTypeName = SET stringList charsetWithOptBinary?
    | dataTypeName = (SERIAL | JSON | GEOMETRY | GEOMCOLLECTION | GEOMETRYCOLLECTION | POINT | MULTIPOINT | LINESTRING | MULTILINESTRING | POLYGON | MULTIPOLYGON)
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
    | charset charsetName collateClause? BINARY?
    | BINARY (charset charsetName collateClause?)?
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
    : COLLATE (collationName | parameterMarker)
    ;

fieldOrVarSpec
    : LP_ (userVariable (COMMA_ userVariable)*)? RP_
    ;

ifNotExists
    : IF NOT EXISTS
    ;

ifExists
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

combineOption
    : ALL | DISTINCT
    ;

noWriteToBinLog
    : LOCAL
    | NO_WRITE_TO_BINLOG
    ;

channelOption
    : FOR CHANNEL string_
    ;
