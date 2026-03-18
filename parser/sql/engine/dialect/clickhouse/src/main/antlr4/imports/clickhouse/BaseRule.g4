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

import Symbol, Keyword, ClickHouseKeyword, Literals;

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
    : characterSetName? STRING_ collateClause?
    ;

numberLiterals
    : (PLUS_ | MINUS_)? NUMBER_
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

identifier
    : IDENTIFIER_
    | unreservedWord
    | interval
    ;

unreservedWord
    : AFTER | ALIAS | ALL | ALTER | AND | ANTI | ANY | ARRAY | AS | ASCENDING | ASOF | AST | ASYNC | ATTACH | BETWEEN | BOTH | BY | CASE
    | CAST | CHECK | CLEAR | CLUSTER | CODEC | COLLATE | COLUMN | COMMENT | CONSTRAINT | CREATE | CROSS | CUBE | CURRENT | DATABASE
    | DATABASES | DATE | DEDUPLICATE | DEFAULT | DELAY | DELETE | DESCRIBE | DESC | DESCENDING | DETACH | DICTIONARIES | DICTIONARY | DISK
    | DISTINCT | DISTRIBUTED | DROP | ELSE | END | ENGINE | EVENTS | EXISTS | EXPLAIN | EXPRESSION | EXTRACT | FETCHES | FINAL | FIRST
    | FLUSH | FOR | FOLLOWING | FOR | FORMAT | FREEZE | FROM | FULL | FUNCTION | GLOBAL | GRANULARITY | GROUP | HAVING | HIERARCHICAL | ID
    | IF | ILIKE | IN | INDEX | INJECTIVE | INNER | INSERT | INTERVAL | INTO | IS | IS_OBJECT_ID | JOIN | JSON_FALSE | JSON_TRUE | KEY
    | KILL | LAST | LAYOUT | LEADING | LEFT | LIFETIME | LIKE | LIMIT | LIVE | LOCAL | LOGS | MATERIALIZE | MATERIALIZED | MAX | MERGES
    | MIN | MODIFY | MOVE | MUTATION | NO | NOT | NULLS | OFFSET | ON | OPTIMIZE | OR | ORDER | OUTER | OUTFILE | OVER | PARTITION
    | POPULATE | PRECEDING | PREWHERE | PRIMARY | RANGE | RELOAD | REMOVE | RENAME | REPLACE | REPLICA | REPLICATED | RIGHT | ROLLUP | ROW
    | ROWS | SAMPLE | SELECT | SEMI | SENDS | SET | SETTINGS | SHOW | SOURCE | START | STOP | SUBSTRING | SYNC | SYNTAX | SYSTEM | TABLE
    | TABLES | TEMPORARY | TEST | THEN | TIES | TIMEOUT | TIMESTAMP | TOTALS | TRAILING | TRIM | TRUNCATE | TO | TOP | TTL | TYPE
    | UNBOUNDED | UNION | UPDATE | USE | USING | UUID | VALUES | VIEW | VOLUME | WATCH | WHEN | WHERE | WINDOW | WITH
    ;

interval
    : SECOND 
    | MINUTE 
    | HOUR 
    | DAY 
    | WEEK 
    | MONTH 
    | QUARTER 
    | YEAR;

variable
    : (AT_? AT_)? (GLOBAL | LOCAL)? DOT_? identifier
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

viewName
    : identifier
    | (owner DOT_)? identifier
    ;

owner
    : identifier
    ;

name
    : identifier
    ;

constraintName
    : identifier
    ;

columnNames
    : LP_? columnName (COMMA_ columnName)* RP_?
    ;

tableNames
    : LP_? tableName (COMMA_ tableName)* RP_?
    ;

characterSetName
    : IDENTIFIER_
    ;

cluster
    : ON CLUSTER identifier
    ;

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
    : OR
    ;

notOperator
    : NOT | NOT_
    ;

booleanPrimary
    : booleanPrimary IS NOT? (TRUE | FALSE  | NULL)
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
    | (PLUS_ | MINUS_ | TILDE_ | NOT_) simpleExpr
    | LP_ expr (COMMA_ expr)* RP_
    | EXISTS? subquery
    | LBE_ identifier expr RBE_
    | caseExpression
    | intervalExpression
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
    : castFunction | positionFunction | substringFunction | extractFunction | trimFunction
    ;

castFunction
    : CAST LP_ (expr | NULL) AS dataType RP_
    ;

positionFunction
    : POSITION LP_ expr IN expr RP_
    ;

substringFunction
    : SUBSTRING LP_ expr FROM NUMBER_ (FOR NUMBER_)? RP_
    ;

extractFunction
    : EXTRACT LP_ identifier FROM expr RP_
    ;

trimFunction
    : TRIM LP_ (LEADING | BOTH | TRAILING) STRING_ FROM STRING_ RP_
    ;

regularFunction
    : regularFunctionName LP_ (expr (COMMA_ expr)* | ASTERISK_)? RP_
    ;

regularFunctionName
    : identifier | IF  | LOCALTIME | LOCALTIMESTAMP | INTERVAL
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

intervalExpression
    : INTERVAL expr intervalUnit
    ;

intervalUnit
    : MICROSECOND 
    | SECOND 
    | MINUTE 
    | HOUR 
    | DAY 
    | WEEK 
    | MONTH 
    | QUARTER 
    | YEAR
    ;

subquery
    : 'Default does not match anything'
    ;

orderByClause
    : ORDER BY orderByItem (COMMA_ orderByItem)*
    ;

orderByItem
    : (columnName | numberLiterals) (ASC | DESC)?
    ;

dataType
    : dataTypeName dataTypeLength? characterSet? collateClause?
    | dataTypeName LP_ STRING_ (COMMA_ STRING_)* RP_ characterSet? collateClause?
    ;

dataTypeName
    : UINT8 
    | UINT16 
    | UINT32 
    | UINT64 
    | INT8 
    | INT16 
    | INT32 
    | INT64 
    | FLOAT32 
    | FLOAT64
    | STRING 
    | FIXED_STRING
    | DATE 
    | DATETIME 
    | DATETIME64
    | ENUM8 
    | ENUM16
    | UUID
    | ARRAY 
    | TUPLE
    | IPV4 
    | IPV6 
    | NESTED
    | NULLABLE
    | identifier
    ;

dataTypeLength
    : LP_ NUMBER_ (COMMA_ NUMBER_)? RP_
    ;

characterSet
    : (CHARACTER | CHAR) SET EQ_? ignoredIdentifier
    ;

collateClause
    : COLLATE EQ_? (STRING_ | ignoredIdentifier)
    ;

ignoredIdentifier
    : identifier (DOT_ identifier)?
    ;
