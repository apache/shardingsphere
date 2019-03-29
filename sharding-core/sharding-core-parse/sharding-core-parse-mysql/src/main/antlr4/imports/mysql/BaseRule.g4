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

import Keyword, Symbol, Literals;

identifier_
    : IDENTIFIER_ | reservedWord_
    ;

reservedWord_
    : ACCOUNT | ACTION | AFTER | ALGORITHM | ALWAYS | ANY | AUTO_INCREMENT 
    | AVG_ROW_LENGTH | BEGIN | BTREE | CHAIN | CHARSET | CHECKSUM | CIPHER 
    | CLIENT | COALESCE | COLUMNS | COLUMN_FORMAT| COMMENT | COMMIT | COMMITTED 
    | COMPACT | COMPRESSED | COMPRESSION | CONNECTION | CONSISTENT | CURRENT| DATA 
    | DATE | DAY | DELAY_KEY_WRITE | DISABLE | DISCARD | DISK | DUPLICATE 
    | ENABLE | ENCRYPTION | ENFORCED | END | ENGINE | ESCAPE | EVENT 
    | EXCHANGE| EXECUTE | FILE | FIRST | FIXED | FOLLOWING | GLOBAL 
    | HASH| IMPORT_ | INSERT_METHOD | INVISIBLE | KEY_BLOCK_SIZE | LAST | LESS 
    | LEVEL| MAX_ROWS | MEMORY | MIN_ROWS | MODIFY | NO | NONE 
    | OFFSET| PACK_KEYS | PARSER | PARTIAL | PARTITIONING | PASSWORD | PERSIST 
    | PERSIST_ONLY | PRECEDING| PRIVILEGES | PROCESS | PROXY | QUICK | REBUILD 
    | REDUNDANT | RELOAD| REMOVE | REORGANIZE | REPAIR | REVERSE | ROLLBACK 
    | ROLLUP | ROW_FORMAT| SAVEPOINT | SESSION | SHUTDOWN | SIMPLE | SLAVE 
    | SOUNDS | SQL_BIG_RESULT| SQL_BUFFER_RESULT | SQL_CACHE | SQL_NO_CACHE | START | STATS_AUTO_RECALC 
    | STATS_PERSISTENT | STATS_SAMPLE_PAGES| STORAGE | SUBPARTITION | SUPER | TABLES | TABLESPACE 
    | TEMPORARY | THAN| TIME | TIMESTAMP | TRANSACTION | TRUNCATE | UNBOUNDED 
    | UNKNOWN | UPGRADE| VALIDATION | VALUE | VIEW | VISIBLE | WEIGHT_STRING 
    | WITHOUT
    ;

tableName
    : (identifier_ DOT_)? identifier_ | identifier_ DOT_ASTERISK_ | ASTERISK_
    ;

ownerName
    : identifier_
    ;

columnName
    : (ownerName DOT_)? identifier_
    ;

indexName
    : identifier_
    ;

alias
    : identifier_ | STRING_
    ;

dataTypeLength
    : LP_ NUMBER_ (COMMA_ NUMBER_)? RP_
    ;

primaryKey
    : PRIMARY? KEY
    ;

columnNames
    : LP_ columnName (COMMA_ columnName)* RP_
    ;

exprs
    : expr (COMMA_ expr)*
    ;

exprList
    : LP_ exprs RP_
    ;

expr
    : expr AND expr
    | expr AND_ expr
    | expr XOR expr
    | LP_ expr RP_
    | NOT expr
    | NOT_ expr
    | expr OR expr
    | expr OR_ expr
    | booleanPrimary
    | exprRecursive
    ;

exprRecursive
    : matchNone
    ;

booleanPrimary
    : booleanPrimary IS NOT? (TRUE | FALSE | UNKNOWN |NULL)
    | booleanPrimary SAFE_EQ_ predicate
    | booleanPrimary comparisonOperator predicate
    | booleanPrimary comparisonOperator (ALL | ANY) subquery
    | predicate
    ;

comparisonOperator
    : EQ_
    | GTE_
    | GT_
    | LTE_
    | LT_
    | NEQ_
    ;

predicate
    : bitExpr NOT? IN subquery
    | bitExpr NOT? IN LP_ simpleExpr (COMMA_ simpleExpr)* RP_
    | bitExpr NOT? BETWEEN simpleExpr AND predicate
    | bitExpr SOUNDS LIKE simpleExpr
    | bitExpr NOT? LIKE simpleExpr (ESCAPE simpleExpr)*
    | bitExpr NOT? (REGEXP | RLIKE) simpleExpr
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
    | bitExpr MOD bitExpr
    | bitExpr MOD_ bitExpr
    | bitExpr CARET_ bitExpr
    | bitExpr PLUS_ intervalExpr
    | bitExpr MINUS_ intervalExpr
    | simpleExpr
    ;

simpleExpr
    : functionCall
    | literal
    | columnName
    | simpleExpr collateClause
    //| param_marker
    | variable
    | simpleExpr AND_ simpleExpr
    | PLUS_ simpleExpr
    | MINUS_ simpleExpr
    | TILDE_ simpleExpr
    | NOT_ simpleExpr
    | BINARY simpleExpr
    | exprList
    | ROW exprList
    | subquery
    | EXISTS subquery
    // | (identifier_ expr)
    //| match_expr
    | caseExpress
    | intervalExpr
    | privateExprOfDb
    ;

functionCall
    : matchNone
    ;

distinct
    : DISTINCT
    ;

intervalExpr
    : matchNone
    ;

caseExpress
    : matchNone
    ;

privateExprOfDb
    : matchNone
    ;

variable
    : matchNone
    ;

literal
    : question
    | number
    | TRUE
    | FALSE
    | NULL
    | LBE_ identifier_ STRING_ RBE_
    | HEX_DIGIT_
    | string
    | identifier_ STRING_ collateClause?
    | (DATE | TIME | TIMESTAMP) STRING_
    | identifier_? BIT_NUM_ collateClause?
    ;

question
    : QUESTION_
    ;

number
   : NUMBER_
   ;

string
    : STRING_
    ;

subquery
    : matchNone
    ;

collateClause
    : matchNone
    ;

orderByClause
    : ORDER BY orderByItem (COMMA_ orderByItem)*
    ;

orderByItem
    : (columnName | number | expr) (ASC | DESC)?
    ;

unqualifiedShorthand
    : ASTERISK_
    ;

qualifiedShorthand
    : identifier_ DOT_ASTERISK_
    ;

ignoredIdentifier_
    : identifier_ (DOT_ identifier_)?
    ;

ignoredIdentifiers_
    : ignoredIdentifier_ (COMMA_ ignoredIdentifier_)*
    ;

matchNone
    : 'Default does not match anything'
    ;
