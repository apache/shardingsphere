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
    : characterSetName_? STRING_ collateClause_?
    ;

numberLiterals
   : MINUS_? NUMBER_
   ;

dateTimeLiterals
    : (DATE | TIME | TIMESTAMP) STRING_
    | LBE_ identifier STRING_ RBE_
    ;

hexadecimalLiterals
    : characterSetName_? HEX_DIGIT_ collateClause_?
    ;

bitValueLiterals
    : characterSetName_? BIT_NUM_ collateClause_?
    ;

booleanLiterals
    : TRUE | FALSE
    ;

nullValueLiterals
    : NULL
    ;

characterSetName_
    : IDENTIFIER_
    ;

collationName_
   : IDENTIFIER_
   ;

identifier
    : IDENTIFIER_ | unreservedWord
    ;

unreservedWord
    : ACCOUNT | ACTION | AFTER | ALGORITHM | ALWAYS | ANY | AUTO_INCREMENT 
    | AVG_ROW_LENGTH | BEGIN | BTREE | CHAIN | CHARSET | CHECKSUM | CIPHER 
    | CLIENT | COALESCE | COLUMNS | COLUMN_FORMAT | COMMENT | COMMIT | COMMITTED 
    | COMPACT | COMPRESSED | COMPRESSION | CONNECTION | CONSISTENT | CURRENT | DATA 
    | DATE | DELAY_KEY_WRITE | DISABLE | DISCARD | DISK | DUPLICATE | ENABLE 
    | ENCRYPTION | ENFORCED | END | ENGINE | ESCAPE | EVENT | EXCHANGE 
    | EXECUTE | FILE | FIRST | FIXED | FOLLOWING | GLOBAL | HASH 
    | IMPORT_ | INSERT_METHOD | INVISIBLE | KEY_BLOCK_SIZE | LAST | LESS 
    | LEVEL | MAX_ROWS | MEMORY | MIN_ROWS | MODIFY | NO | NONE | OFFSET 
    | PACK_KEYS | PARSER | PARTIAL | PARTITIONING | PASSWORD | PERSIST | PERSIST_ONLY 
    | PRECEDING | PRIVILEGES | PROCESS | PROXY | QUICK | REBUILD | REDUNDANT 
    | RELOAD | REMOVE | REORGANIZE | REPAIR | REVERSE | ROLLBACK | ROLLUP 
    | ROW_FORMAT | SAVEPOINT | SESSION | SHUTDOWN | SIMPLE | SLAVE | SOUNDS 
    | SQL_BIG_RESULT | SQL_BUFFER_RESULT | SQL_CACHE | SQL_NO_CACHE | START | STATS_AUTO_RECALC | STATS_PERSISTENT 
    | STATS_SAMPLE_PAGES | STORAGE | SUBPARTITION | SUPER | TABLES | TABLESPACE | TEMPORARY 
    | THAN | TIME | TIMESTAMP | TRANSACTION | TRUNCATE | UNBOUNDED | UNKNOWN 
    | UPGRADE | VALIDATION | VALUE | VIEW | VISIBLE | WEIGHT_STRING | WITHOUT 
    | MICROSECOND | SECOND | MINUTE | HOUR | DAY | WEEK | MONTH
    | QUARTER | YEAR | AGAINST | LANGUAGE | MODE | QUERY | EXPANSION
    | BOOLEAN | MAX | MIN | SUM | COUNT | AVG | BIT_AND
    | BIT_OR | BIT_XOR | GROUP_CONCAT | JSON_ARRAYAGG | JSON_OBJECTAGG | STD | STDDEV
    | STDDEV_POP | STDDEV_SAMP | VAR_POP | VAR_SAMP | VARIANCE | EXTENDED | STATUS
    | FIELDS | INDEXES | USER | ROLE | OJ | AUTOCOMMIT | OFF | ROTATE | INSTANCE | MASTER | BINLOG |ERROR
    | SCHEDULE | COMPLETION | DO | DEFINER | START | EVERY | HOST | SOCKET | OWNER | PORT | RETURNS | CONTAINS
    | SECURITY | INVOKER | UNDEFINED | MERGE | TEMPTABLE | CASCADED | LOCAL | SERVER | WRAPPER | OPTIONS | DATAFILE
    | FILE_BLOCK_SIZE | EXTENT_SIZE | INITIAL_SIZE | AUTOEXTEND_SIZE | MAX_SIZE | NODEGROUP
    | WAIT | LOGFILE | UNDOFILE | UNDO_BUFFER_SIZE | REDO_BUFFER_SIZE | DEFINITION | ORGANIZATION
    | DESCRIPTION | REFERENCE | FOLLOWS | PRECEDES | NAME |CLOSE | OPEN | NEXT | HANDLER | PREV
    | IMPORT | CONCURRENT | XML | POSITION | SHARE | DUMPFILE | CLONE | AGGREGATE | INSTALL | UNINSTALL | COMPONENT
    | RESOURCE | FLUSH | RESET | RESTART | HOSTS | RELAY | EXPORT | USER_RESOURCES | SLOW | GENERAL | CACHE
    | SUBJECT | ISSUER | OLD | RANDOM | RETAIN | MAX_USER_CONNECTIONS | MAX_CONNECTIONS_PER_HOUR | MAX_UPDATES_PER_HOUR
    | MAX_QUERIES_PER_HOUR | REUSE | OPTIONAL | HISTORY | NEVER | EXPIRE | TYPE | CONTEXT | CODE | CHANNEL | SOURCE
    | IO_THREAD | SQL_THREAD | SQL_BEFORE_GTIDS | SQL_AFTER_GTIDS | MASTER_LOG_FILE | MASTER_LOG_POS | RELAY_LOG_FILE
    | RELAY_LOG_POS | SQL_AFTER_MTS_GAPS | UNTIL | DEFAULT_AUTH | PLUGIN_DIR | STOP | SIGNED | FAILED_LOGIN_ATTEMPTS
    | PASSWORD_LOCK_TIME | MASTER_COMPRESSION_ALGORITHMS | MASTER_ZSTD_COMPRESSION_LEVEL | MASTER_SSL | MASTER_SSL_CA
    | MASTER_SSL_CAPATH | MASTER_SSL_CERT | MASTER_SSL_CRL | MASTER_SSL_CRLPATH | MASTER_SSL_KEY | MASTER_SSL_CIPHER
    | MASTER_TLS_VERSION | MASTER_TLS_CIPHERSUITES | MASTER_PUBLIC_KEY_PATH | GET_MASTER_PUBLIC_KEY | IGNORE_SERVER_IDS
    | MASTER_HOST | MASTER_USER | MASTER_PASSWORD | MASTER_PORT | PRIVILEGE_CHECKS_USER | REQUIRE_ROW_FORMAT | MASTER_CONNECT_RETRY
    | MASTER_RETRY_COUNT | MASTER_DELAY | MASTER_HEARTBEAT_PERIOD | MASTER_AUTO_POSITION | REPLICATE_DO_DB | REPLICATE_IGNORE_DB
    | REPLICATE_DO_TABLE | REPLICATE_IGNORE_TABLE | REPLICATE_WILD_DO_TABLE | REPLICATE_WILD_IGNORE_TABLE | REPLICATE_REWRITE_DB
    | GROUP_REPLICATION
    ;

variable
    : (AT_? AT_)? scope? DOT_? identifier
    ;

scope
    : GLOBAL | PERSIST | PERSIST_ONLY | SESSION
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
    : expr logicalOperator expr
    | expr XOR expr
    | notOperator_ expr
    | LP_ expr RP_
    | booleanPrimary
    ;

logicalOperator
    : OR | OR_ | AND | AND_
    ;

notOperator_
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
    | (PLUS_ | MINUS_ | TILDE_ | NOT_ | BINARY) simpleExpr
    | ROW? LP_ expr (COMMA_ expr)* RP_
    | EXISTS? subquery
    | LBE_ identifier expr RBE_
    | matchExpression_
    | caseExpression
    | intervalExpression
    ;

functionCall
    : aggregationFunction | specialFunction | regularFunction 
    ;

aggregationFunction
    : aggregationFunctionName LP_ distinct? (expr (COMMA_ expr)* | ASTERISK_)? RP_ overClause_?
    ;

aggregationFunctionName
    : MAX | MIN | SUM | COUNT | AVG | BIT_AND | BIT_OR
    | BIT_XOR | JSON_ARRAYAGG | JSON_OBJECTAGG | STD | STDDEV | STDDEV_POP | STDDEV_SAMP
    | VAR_POP | VAR_SAMP | VARIANCE
    ;

distinct
    : DISTINCT
    ;

overClause_
    : OVER (LP_ windowSpecification_ RP_ | identifier)
    ;

windowSpecification_
    : identifier? partitionClause_? orderByClause? frameClause_?
    ;

partitionClause_
    : PARTITION BY expr (COMMA_ expr)*
    ;

frameClause_
    : (ROWS | RANGE) (frameStart_ | frameBetween_)
    ;

frameStart_
    : CURRENT ROW | UNBOUNDED PRECEDING | UNBOUNDED FOLLOWING | expr PRECEDING | expr FOLLOWING
    ;

frameEnd_
    : frameStart_
    ;

frameBetween_
    : BETWEEN frameStart_ AND frameEnd_
    ;

specialFunction
    : groupConcatFunction | windowFunction | castFunction | convertFunction | positionFunction | substringFunction | extractFunction 
    | charFunction | trimFunction_ | weightStringFunction | valuesFunction_
    ;

groupConcatFunction
    : GROUP_CONCAT LP_ distinct? (expr (COMMA_ expr)* | ASTERISK_)? (orderByClause)? (SEPARATOR expr)? RP_
    ;

windowFunction
    : identifier LP_ expr (COMMA_ expr)* RP_ overClause_
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
    :  (SUBSTRING | SUBSTR) LP_ expr FROM NUMBER_ (FOR NUMBER_)? RP_
    ;

extractFunction
    : EXTRACT LP_ identifier FROM expr RP_
    ;

charFunction
    : CHAR LP_ expr (COMMA_ expr)* (USING ignoredIdentifier_)? RP_
    ;

trimFunction_
    : TRIM LP_ (LEADING | BOTH | TRAILING) STRING_ FROM STRING_ RP_
    ;

valuesFunction_
    : VALUES LP_ columnName RP_
    ;

weightStringFunction
    : WEIGHT_STRING LP_ expr (AS dataType)? levelClause_? RP_
    ;

levelClause_
    : LEVEL (levelInWeightListElement_ (COMMA_ levelInWeightListElement_)* | NUMBER_ MINUS_ NUMBER_)
    ;

levelInWeightListElement_
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
    : regularFunctionName_ (LP_ (expr (COMMA_ expr)* | ASTERISK_)? RP_)
    ;
    
regularFunctionName_
    : IF | UNIX_TIMESTAMP | LOCALTIME | LOCALTIMESTAMP | NOW | REPLACE | INTERVAL | SUBSTRING | MOD
    | DATABASE | LEFT | RIGHT | LOWER | UPPER | DATE | DATEDIFF | DATE_FORMAT | DAY | DAYNAME | DAYOFMONTH | DAYOFWEEK | DAYOFYEAR
    | GEOMCOLLECTION | GEOMETRYCOLLECTION | LINESTRING | MULTILINESTRING | MULTIPOINT | MULTIPOLYGON | POINT | POLYGON | STR_TO_DATE
    | ST_AREA | ST_ASBINARY | ST_ASGEOJSON | ST_ASTEXT | ST_ASWKB | ST_ASWKT | ST_BUFFER | ST_BUFFER_STRATEGY | ST_CENTROID | ST_CONTAINS
    | ST_CONVEXHULL | ST_CROSSES | ST_DIFFERENCE | ST_DIMENSION | ST_DISJOINT | ST_DISTANCE | ST_DISTANCE_SPHERE | ST_ENDPOINT | ST_ENVELOPE
    | ST_EQUALS | ST_EXTERIORRING | ST_GEOHASH | ST_GEOMCOLLFROMTEXT | ST_GEOMCOLLFROMTXT | ST_GEOMCOLLFROMWKB | ST_GEOMETRYCOLLECTIONFROMTEXT
    | ST_GEOMETRYCOLLECTIONFROMWKB | ST_GEOMETRYFROMTEXT | ST_GEOMETRYFROMWKB | ST_GEOMETRYN | ST_GEOMETRYTYPE | ST_GEOMFROMGEOJSON
    | ST_GEOMFROMTEXT | ST_GEOMFROMWKB | ST_INTERIORRINGN | ST_INTERSECTION | ST_INTERSECTS | ST_ISCLOSED | ST_ISEMPTY | ST_ISSIMPLE
    | ST_ISVALID | ST_LATFROMGEOHASH | ST_LATITUDE | ST_LENGTH | ST_LINEFROMTEXT | ST_LINEFROMWKB | ST_LINESTRINGFROMTEXT | ST_LINESTRINGFROMWKB
    | ST_LONGFROMGEOHASH | ST_LONGITUDE | ST_MAKEENVELOPE | ST_MLINEFROMTEXT | ST_MLINEFROMWKB | ST_MULTILINESTRINGFROMTEXT | ST_MULTILINESTRINGFROMWKB
    | ST_MPOINTFROMTEXT | ST_MPOINTFROMWKB | ST_MULTIPOINTFROMTEXT | ST_MULTIPOINTFROMWKB | ST_MPOLYFROMTEXT | ST_MPOLYFROMWKB | ST_MULTIPOLYGONFROMTEXT
    | ST_MULTIPOLYGONFROMWKB | ST_NUMGEOMETRIES | ST_NUMINTERIORRING | ST_NUMINTERIORRINGS | ST_NUMPOINTS | ST_OVERLAPS | ST_POINTFROMGEOHASH
    | ST_POINTFROMTEXT | ST_POINTFROMWKB | ST_POINTN | ST_POLYFROMTEXT | ST_POLYFROMWKB | ST_POLYGONFROMTEXT | ST_POLYGONFROMWKB | ST_SIMPLIFY
    | ST_SRID | ST_STARTPOINT | ST_SWAPXY | ST_SYMDIFFERENCE | ST_TOUCHES | ST_TRANSFORM | ST_UNION | ST_VALIDATE | ST_WITHIN | ST_X | ST_Y
    | TIME | TIMEDIFF | TIMESTAMP | TIMESTAMPADD | TIMESTAMPDIFF | TIME_FORMAT | TIME_TO_SEC | AES_DECRYPT | AES_ENCRYPT | FROM_BASE64 | TO_BASE64
    | ADDDATE | ADDTIME | DATE | DATE_ADD | DATE_SUB | identifier
    ;

matchExpression_
    : MATCH columnNames AGAINST (expr matchSearchModifier_?)
    ;

matchSearchModifier_
    : IN NATURAL LANGUAGE MODE | IN NATURAL LANGUAGE MODE WITH QUERY EXPANSION | IN BOOLEAN MODE | WITH QUERY EXPANSION
    ;

caseExpression
    : CASE simpleExpr? caseWhen_+ caseElse_? END
    ;

datetimeExpr
    : expr
    ;

binaryLogFileIndexNumber
    : NUMBER_
    ;

caseWhen_
    : WHEN expr THEN expr
    ;

caseElse_
    : ELSE expr
    ;

intervalExpression
    : INTERVAL expr intervalUnit_
    ;

intervalUnit_
    : MICROSECOND | SECOND | MINUTE | HOUR | DAY | WEEK | MONTH
    | QUARTER | YEAR | SECOND_MICROSECOND | MINUTE_MICROSECOND | MINUTE_SECOND | HOUR_MICROSECOND | HOUR_SECOND
    | HOUR_MINUTE | DAY_MICROSECOND | DAY_SECOND | DAY_MINUTE | DAY_HOUR | YEAR_MONTH
    ;

subquery
    : 'Default does not match anything'
    ;

orderByClause
    : ORDER BY orderByItem (COMMA_ orderByItem)*
    ;

orderByItem
    : (columnName | numberLiterals | expr) (ASC | DESC)?
    ;

dataType
    : dataTypeName dataTypeLength? characterSet_? collateClause_? (UNSIGNED | SIGNED)? ZEROFILL? | dataTypeName collectionOptions characterSet_? collateClause_?
    ;

dataTypeName
    : INTEGER | INT | SMALLINT | TINYINT | MEDIUMINT | BIGINT | DECIMAL| NUMERIC | FLOAT | DOUBLE | BIT | BOOL | BOOLEAN
    | DEC | DATE | DATETIME | TIMESTAMP | TIME | YEAR | CHAR | VARCHAR | BINARY | VARBINARY | TINYBLOB | TINYTEXT | BLOB
    | TEXT | MEDIUMBLOB | MEDIUMTEXT | LONGBLOB | LONGTEXT | ENUM | SET | GEOMETRY | POINT | LINESTRING | POLYGON
    | MULTIPOINT | MULTILINESTRING | MULTIPOLYGON | GEOMETRYCOLLECTION | JSON
    ;

dataTypeLength
    : LP_ NUMBER_ (COMMA_ NUMBER_)? RP_
    ;

collectionOptions
    : LP_ STRING_ (COMMA_ STRING_)* RP_
    ;

characterSet_
    : (CHARACTER | CHAR) SET EQ_? ignoredIdentifier_
    ;

collateClause_
    : COLLATE EQ_? (STRING_ | ignoredIdentifier_)
    ;

ignoredIdentifier_
    : identifier (DOT_ identifier)?
    ;

ignoredIdentifiers_
    : ignoredIdentifier_ (COMMA_ ignoredIdentifier_)*
    ;

fieldOrVarSpec
    : LP_ (identifier (COMMA_ identifier)*)? RP_
    ;

notExistClause_
    : (IF NOT EXISTS)?
    ;

existClause_
    : (IF EXISTS)?
    ;

pattern
    : STRING_
    ;

connectionId_
    : NUMBER_
    ;
