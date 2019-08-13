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
    : QUESTION_ literalsType_?
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
    : unicodeEscapes_? STRING_ literalsType_? uescape_?
    ;

numberLiterals
   : MINUS_? NUMBER_ literalsType_?
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

literalsType_
    : COLON_ COLON_ IDENTIFIER_
    ;

identifier_
    : unicodeEscapes_? IDENTIFIER_ uescape_? |  unreservedWord_ 
    ;

unicodeEscapes_
    : ('U' | 'u') AMPERSAND_
    ;

uescape_
    : UESCAPE STRING_
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
    | USAGE | VALID | VALIDATE | WITHIN | WITHOUT | ZONE | GROUPS
    | RECURSIVE | INSTANCE | DEFINER | PRESERVE | SQL | LOCAL | CASCADED
    | CLOSE | OPEN | NEXT | NAME
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

tableNames
    : LP_? tableName (COMMA_ tableName)* RP_?
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

primaryKey
    : PRIMARY? KEY
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
    | simpleExpr COLLATE (STRING_ | identifier_)
    | simpleExpr OR_ simpleExpr
    | (PLUS_ | MINUS_ | TILDE_ | NOT_ | BINARY) simpleExpr
    | ROW? LP_ expr (COMMA_ expr)* RP_
    | EXISTS? subquery
    | LBE_ identifier_ expr RBE_
    | caseExpression_
    ;

functionCall
    : aggregationFunction | specialFunction_ | regularFunction_ 
    ;

aggregationFunction
    : aggregationFunctionName_ LP_ distinct? (expr (COMMA_ expr)* | ASTERISK_)? RP_ filterClause_?
    ;

aggregationFunctionName_
    : MAX | MIN | SUM | COUNT | AVG
    ;

distinct
    : DISTINCT
    ;

filterClause_
    : FILTER LP_ WHERE expr RP_
    ;

windowFunction_
    : identifier_ LP_ expr (COMMA_ expr)* RP_ filterClause_ OVER identifier_? windowDefinition_
    ;

windowDefinition_
    : pratitionClause_? orderByClause? frameClause_?
    ;

pratitionClause_
    : PARTITION BY expr (COMMA_ expr)*
    ;

frameClause_
    : (RANGE | ROWS | GROUPS) (frameStart_ | frameBetween_)
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

specialFunction_
    : windowFunction_ | castFunction_  | charFunction_
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
    : identifier_ | IF | CURRENT_TIMESTAMP | LOCALTIME | LOCALTIMESTAMP | INTERVAL
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
    : 'Default does not match anything'
    ;

orderByClause
    : ORDER BY orderByItem (COMMA_ orderByItem)*
    ;

orderByItem
    : (columnName | numberLiterals | expr) (ASC | DESC)?
    ;

dataType
    : dataTypeName_ dataTypeLength? characterSet_? collateClause_? | dataTypeName_ LP_ STRING_ (COMMA_ STRING_)* RP_ characterSet_? collateClause_?
    ;

dataTypeName_
    : identifier_ identifier_?
    ;

dataTypeLength
    : LP_ NUMBER_ (COMMA_ NUMBER_)? RP_
    ;

characterSet_
    : (CHARACTER | CHAR) SET EQ_? ignoredIdentifier_
    ;

collateClause_
    : COLLATE EQ_? (STRING_ | ignoredIdentifier_)
    ;

ignoredIdentifier_
    : identifier_ (DOT_ identifier_)?
    ;

ignoredIdentifiers_
    : ignoredIdentifier_ (COMMA_ ignoredIdentifier_)*
    ;
