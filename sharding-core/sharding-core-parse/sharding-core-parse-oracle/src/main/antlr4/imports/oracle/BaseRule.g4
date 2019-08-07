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

import Symbol, Keyword, OracleKeyword, Literals;

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
    | LBE_ identifier_ STRING_ RBE_
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

identifier_
    : IDENTIFIER_ | unreservedWord_
    ;

unreservedWord_
    : TRUNCATE | FUNCTION | PROCEDURE | CASE | WHEN | CAST | TRIM | SUBSTRING
    | NATURAL | JOIN | FULL | INNER | OUTER | LEFT | RIGHT
    | CROSS | USING | IF | TRUE | FALSE | LIMIT | OFFSET
    | BEGIN | COMMIT | ROLLBACK | SAVEPOINT | BOOLEAN | DOUBLE | CHARACTER
    | ARRAY | INTERVAL | TIME | TIMESTAMP | LOCALTIME | LOCALTIMESTAMP | YEAR
    | QUARTER | MONTH | WEEK | DAY | HOUR | MINUTE | SECOND
    | MICROSECOND | MAX | MIN | SUM | COUNT | AVG | ENABLE
    | DISABLE | BINARY | ESCAPE | MOD | UNKNOWN | XOR | ALWAYS
    | CASCADE | GENERATED | PRIVILEGES | READ | WRITE | REFERENCES | TRANSACTION
    | ROLE | VISIBLE | INVISIBLE | EXECUTE | USE | DEBUG | UNDER
    | FLASHBACK | ARCHIVE | REFRESH | QUERY | REWRITE | KEEP | SEQUENCE
    | INHERIT | TRANSLATE | SQL | MERGE | AT | BITMAP | CACHE | CHECKPOINT
    | CONNECT | CONSTRAINTS | CYCLE | DBTIMEZONE | ENCRYPT | DECRYPT | DEFERRABLE
    | DEFERRED | EDITION | ELEMENT | END | EXCEPTIONS | FORCE | GLOBAL
    | IDENTITY | INITIALLY | INVALIDATE | JAVA | LEVELS | LOCAL | MAXVALUE
    | MINVALUE | NOMAXVALUE | NOMINVALUE | MINING | MODEL | NATIONAL | NEW
    | NOCACHE | NOCYCLE | NOORDER | NORELY | NOVALIDATE | ONLY | PRESERVE
    | PROFILE | REF | REKEY | RELY | REPLACE | SOURCE | SALT
    | SCOPE | SORT | SUBSTITUTABLE | TABLESPACE | TEMPORARY | TRANSLATION | TREAT
    | NO | TYPE | UNUSED | VALUE | VARYING | VIRTUAL | ZONE
    | ADVISOR | ADMINISTER | TUNING | MANAGE | MANAGEMENT | OBJECT | CLUSTER
    | CONTEXT | EXEMPT | REDACTION | POLICY | DATABASE | SYSTEM | AUDIT
    | LINK | ANALYZE | DICTIONARY | DIMENSION | INDEXTYPE | EXTERNAL | JOB
    | CLASS | PROGRAM | SCHEDULER | LIBRARY | LOGMINING | MATERIALIZED | CUBE
    | MEASURE | FOLDER | BUILD | PROCESS | OPERATOR | OUTLINE | PLUGGABLE
    | CONTAINER | SEGMENT | RESTRICTED | COST | SYNONYM | BACKUP | UNLIMITED
    | BECOME | CHANGE | NOTIFICATION | ACCESS | PRIVILEGE | PURGE | RESUMABLE
    | SYSGUID | SYSBACKUP | SYSDBA | SYSDG | SYSKM | SYSOPER | DBA_RECYCLEBIN |SCHEMA
    | DO | DEFINER | CURRENT_USER | CASCADED | CLOSE | OPEN | NEXT | NAME
    ;

schemaName
    : identifier_
    ;

tableName
    : (owner DOT_)? name
    ;

columnName
    : (owner DOT_)? name
    ;

owner
    : identifier_
    ;

name
    : identifier_
    ;

columnNames
    : LP_? columnName (COMMA_ columnName)* RP_?
    ;

tableNames
    : LP_? tableName (COMMA_ tableName)* RP_?
    ;

indexName
    : identifier_
    ;

oracleId
    : IDENTIFIER_ | (STRING_ DOT_)* STRING_
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

exprs
    : expr (COMMA_ expr)*
    ;

exprList
    : LP_ exprs RP_
    ;

// TODO comb expr
expr
    : expr logicalOperator expr
    | notOperator_ expr
    | LP_ expr RP_
    | booleanPrimary_
    ;

logicalOperator
    : OR | OR_ | AND | AND_
    ;

notOperator_
    : NOT | NOT_
    ;

booleanPrimary_
    : booleanPrimary_ IS NOT? (TRUE | FALSE | UNKNOWN | NULL)
    | booleanPrimary_ SAFE_EQ_ predicate
    | booleanPrimary_ comparisonOperator predicate
    | booleanPrimary_ comparisonOperator (ALL | ANY) subquery
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
    | LBE_ identifier_ expr RBE_
    | caseExpression_
    | privateExprOfDb
    ;

functionCall
    : aggregationFunction | specialFunction_ | regularFunction_ 
    ;

aggregationFunction
    : aggregationFunctionName_ LP_ distinct? (expr (COMMA_ expr)* | ASTERISK_)? RP_
    ;

aggregationFunctionName_
    : MAX | MIN | SUM | COUNT | AVG
    ;

distinct
    : DISTINCT
    ;

specialFunction_
    : castFunction_  | charFunction_
    ;

castFunction_
    : CAST LP_ expr AS dataType RP_
    ;

charFunction_
    : CHAR LP_ expr (COMMA_ expr)* (USING ignoredIdentifier_)? RP_
    ;

regularFunction_
    : regularFunctionName_ LP_ (expr (COMMA_ expr)* | ASTERISK_)? RP_
    ;

regularFunctionName_
    : identifier_ | IF | LOCALTIME | LOCALTIMESTAMP | INTERVAL
    ;

caseExpression_
    : CASE simpleExpr? caseWhen_+ caseElse_?
    ;

caseWhen_
    : WHEN expr THEN expr
    ;

caseElse_
    : ELSE expr
    ;

subquery
    : matchNone
    ;

orderByClause
    : ORDER BY orderByItem (COMMA_ orderByItem)*
    ;

orderByItem
    : (columnName | numberLiterals | expr) (ASC | DESC)?
    ;

attributeName
    : oracleId
    ;

indexTypeName
    : IDENTIFIER_
    ;

simpleExprs
    : simpleExpr (COMMA_ simpleExpr)*
    ;

lobItem
    : attributeName | columnName
    ;

lobItems
    : lobItem (COMMA_ lobItem)*
    ;

lobItemList
    : LP_ lobItems RP_
    ;

dataType
    : dataTypeName_ dataTypeLength? | specialDatatype | dataTypeName_ dataTypeLength? datetimeTypeSuffix
    ;

specialDatatype
    : dataTypeName_ (LP_ NUMBER_ IDENTIFIER_ RP_) | NATIONAL dataTypeName_ VARYING? LP_ NUMBER_ RP_ | dataTypeName_ LP_? columnName RP_?
    ;

dataTypeName_
    : IDENTIFIER_ IDENTIFIER_ | IDENTIFIER_
    ;

datetimeTypeSuffix
    : (WITH LOCAL? TIME ZONE)? | TO MONTH | TO SECOND (LP_ NUMBER_ RP_)?
    ;

treatFunction
    : TREAT LP_ expr AS REF? dataTypeName_ RP_
    ;

privateExprOfDb
    : treatFunction | caseExpr | intervalExpression | objectAccessExpression | constructorExpr
    ;

caseExpr
    : CASE (simpleCaseExpr | searchedCaseExpr) elseClause? END
    ;

simpleCaseExpr
    : expr searchedCaseExpr+
    ;

searchedCaseExpr
    : WHEN expr THEN simpleExpr
    ;

elseClause
    : ELSE expr
    ;

intervalExpression
    : LP_ expr MINUS_ expr RP_ (DAY (LP_ NUMBER_ RP_)? TO SECOND (LP_ NUMBER_ RP_)? | YEAR (LP_ NUMBER_ RP_)? TO MONTH)
    ;

objectAccessExpression
    : (LP_ simpleExpr RP_ | treatFunction) DOT_ (attributeName (DOT_ attributeName)* (DOT_ functionCall)? | functionCall)
    ;

constructorExpr
    : NEW dataTypeName_ exprList
    ;

ignoredIdentifier_
    : IDENTIFIER_
    ;

ignoredIdentifiers_
    : ignoredIdentifier_ (COMMA_ ignoredIdentifier_)*
    ;

matchNone
    : 'Default does not match anything'
    ;
