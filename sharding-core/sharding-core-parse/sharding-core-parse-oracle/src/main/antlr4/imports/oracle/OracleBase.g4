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

grammar OracleBase;

import Symbol, OracleKeyword, Keyword, DataType, BaseRule;

ID
    : (BQ_?[a-zA-Z_$][a-zA-Z0-9_$#]* BQ_? DOT_)? (BQ_?[a-zA-Z_$][a-zA-Z0-9_$#]* BQ_?) | [a-zA-Z_$#0-9]+ DOT_ASTERISK_
    ;

oracleId
    : ID | (STRING_ DOT_)* STRING_
    ;

tableName
    : oracleId
    ;

columnName
    : oracleId
    ;

indexName
    : oracleId
    ;

attributeName
    : oracleId
    ;

indexTypeName
    : ID
    ;

simpleExprsWithParen
    : LP_ simpleExprs RP_ 
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
    : dataTypeName_ (LP_ NUMBER_ ID RP_) | NATIONAL dataTypeName_ VARYING? LP_ NUMBER_ RP_ | dataTypeName_ LP_? columnName RP_?
    ;

dataTypeName_
    : ID ID | ID
    ;

datetimeTypeSuffix
    : (WITH LOCAL? TIME ZONE)? | TO MONTH | TO SECOND (LP_ NUMBER_ RP_)?
    ;

privateExprOfDb
    : treatFunction | caseExpr | intervalExpression | objectAccessExpression | constructorExpr
    ;

treatFunction
    : TREAT LP_ expr AS REF? dataTypeName_ RP_
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

dateTimeExpr
    : expr AT (LOCAL | TIME ZONE (STRING_ | DBTIMEZONE | expr))
    ;

exprRecursive
    : PRIOR expr
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
