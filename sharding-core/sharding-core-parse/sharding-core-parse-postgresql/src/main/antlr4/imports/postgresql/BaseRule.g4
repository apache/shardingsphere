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

import Keyword, PostgreSQLKeyword, Symbol, Literals;

parameterMarker
    : QUESTION_
    ;

literals
    : stringLiterals
    | numberLiterals
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
    : ADMIN | ESCAPE | EXCLUDE | KEY | PARTITION | SET | UNKNOWN 
    | ADD | ALTER | ALWAYS | CASCADE | COMMIT | CURRENT | DAY 
    | DELETE | DISABLE | DROP | ENABLE | FUNCTION | GENERATED | INDEX 
    | ISOLATION | LEVEL | OPTION | PRIVILEGES | READ | REVOKE | ROLE 
    | ROLLBACK | ROWS | START | TRANSACTION | TRUNCATE | YEAR | ACTION 
    | BEGIN | CACHE | CHARACTERISTICS | CLUSTER | COMMENTS | CONSTRAINTS | CYCLE 
    | DATA | DATABASE | DEFAULTS | DEFERRED | DEPENDS | DOMAIN | EXCLUDING 
    | EXECUTE | EXTENDED | EXTENSION | EXTERNAL | EXTRACT | FILTER 
    | FIRST | FOLLOWING | FORCE | GLOBAL | HOUR | IDENTITY | IF | IMMEDIATE 
    | INCLUDING | INCREMENT | INDEXES | INHERIT | INHERITS | INCLUDE | INSERT 
    | LANGUAGE | LARGE | LAST | LOCAL | LOGGED | MAIN | MATCH 
    | MAXVALUE | MINVALUE | MINUTE | MONTH | NOTHING | NULLS | OBJECT
    | OF | OIDS | OVER | OWNED | OWNER | PARTIAL | PLAIN
    | PRECEDING | PROCEDURE | RANGE | RENAME | REPLICA | RESET | RESTART
    | RESTRICT | ROUTINE | RULE | SAVEPOINT | SCHEMA | SECOND | SECURITY
    | SEQUENCE | SESSION | SHOW | SIMPLE | STATISTICS | STORAGE | TABLESPACE
    | TEMP | TEMPORARY | TRIGGER | TYPE | UNBOUNDED | UNLOGGED | UPDATE
    | USAGE | VALID | VALIDATE | WITHIN | WITHOUT | ZONE
    ;

schemaName
    : identifier_
    ;

tableName
    : identifier_
    ;

tableNames
    : LP_? tableName (COMMA_ tableName)* RP_?
    ;

columnName
    : identifier_
    ;

columnNames
    : LP_ columnName (COMMA_ columnName)* RP_
    ;

collationName
    : STRING_ | identifier_
    ;

indexName
    : identifier_
    ;

alias
    : identifier_
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

expr
    : expr AND expr
    | expr AND_ expr
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
    | bitExpr NOT? IN LP_ simpleExpr (COMMA_ simpleExpr)* RP_
    | bitExpr NOT? BETWEEN simpleExpr AND predicate
    | bitExpr NOT? LIKE simpleExpr (ESCAPE simpleExpr)*
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
    | parameterMarker
    | literals
    | columnName
    | simpleExpr collateClause
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
    | caseExpress
    | intervalExpr
    | privateExprOfDb
    ;

functionCall
    : identifier_ LP_ distinct? (exprs | ASTERISK_)? RP_
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
    : aggregateExpression
    | windowFunction
    | arrayConstructorWithCast
    | (TIMESTAMP (WITH TIME ZONE)? STRING_)
    | extractFromFunction
    ;

variable
    : matchNone
    ;

string
    : STRING_
    ;

subquery
    : matchNone
    ;

collateClause
    : COLLATE collationName
    ;

orderByClause
    : ORDER BY expr (ASC | DESC | USING operator)? (NULLS (FIRST | LAST))?
    ;

orderByItem
    : (columnName | numberLiterals | expr) (ASC | DESC)?
    ;

asterisk
    : ASTERISK_
    ;

dataType
    : dataTypeName_ intervalFields? dataTypeLength? (WITHOUT TIME ZONE | WITH TIME ZONE)? (LBT_ RBT_)* | identifier_
    ;

dataTypeName_
    : identifier_ identifier_ | identifier_
    ;

intervalFields
    : intervalField (TO intervalField)?
    ;

intervalField
    : YEAR
    | MONTH
    | DAY
    | HOUR
    | MINUTE
    | SECOND
    ;

pgExpr
    : castExpr | collateExpr | expr
    ;

aggregateExpression
    : identifier_ (LP_ (ALL | DISTINCT)? exprs orderByClause? RP_) asteriskWithParen (LP_ exprs RP_ WITHIN GROUP LP_ orderByClause RP_) filterClause?
    ;

filterClause
    : FILTER LP_ WHERE booleanPrimary RP_
    ;

asteriskWithParen
    : LP_ ASTERISK_ RP_
    ;

windowFunction
    : identifier_ (exprList | asteriskWithParen) filterClause? windowFunctionWithClause
    ;

windowFunctionWithClause
    : OVER (identifier_ | LP_ windowDefinition RP_)
    ;

windowDefinition
    : identifier_? (PARTITION BY exprs)? (orderByClause (COMMA_ orderByClause)*)? frameClause?
    ;

operator
    : SAFE_EQ_
    | EQ_
    | NEQ_
    | GT_
    | GTE_
    | LT_
    | LTE_
    | AND_
    | OR_
    | NOT_
    ;

frameClause
    : (RANGE | ROWS) frameStart | (RANGE | ROWS) BETWEEN frameStart AND frameEnd
    ;

frameStart
    : UNBOUNDED PRECEDING
    | NUMBER_ PRECEDING
    | CURRENT ROW
    | NUMBER_ FOLLOWING
    | UNBOUNDED FOLLOWING
    ;

frameEnd
    : frameStart
    ;

castExpr
    : CAST LP_ expr AS dataType RP_ | expr COLON_ COLON_ dataType
    ;

castExprWithCOLON_
    : COLON_ COLON_ dataType(LBT_ RBT_)*
    ;

collateExpr
    : expr COLLATE expr
    ;

arrayConstructorWithCast
    : arrayConstructor castExprWithCOLON_? | ARRAY LBT_ RBT_ castExprWithCOLON_
    ;

arrayConstructor
    : ARRAY LBT_ exprs RBT_ | ARRAY LBT_ arrayConstructor (COMMA_ arrayConstructor)* RBT_
    ;

extractFromFunction
    : EXTRACT LP_ identifier_ FROM identifier_ RP_
    ;

ignoredIdentifier_
    : identifier_
    ;

ignoredIdentifiers_
    : ignoredIdentifier_ (COMMA_ ignoredIdentifier_)*
    ;

matchNone
    : 'Default does not match anything'
    ;
