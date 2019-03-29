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

import Symbol, Keyword, Literals;

schemaName
    : IDENTIFIER_
    ;

tableName
    : IDENTIFIER_
    ;

columnName
    : IDENTIFIER_
    ;

collationName
    : STRING_ | IDENTIFIER_
    ;

indexName
    : IDENTIFIER_
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
    | bitExpr NOT? REGEXP simpleExpr
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
    : IDENTIFIER_ LP_ distinct? (exprs | ASTERISK_)? RP_
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
    | LBE_ IDENTIFIER_ STRING_ RBE_
    | HEX_DIGIT_
    | string
    | IDENTIFIER_ STRING_ collateClause?
    | (DATE | TIME | TIMESTAMP) STRING_
    | IDENTIFIER_? BIT_NUM_ collateClause?
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

asterisk
    : ASTERISK_
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
