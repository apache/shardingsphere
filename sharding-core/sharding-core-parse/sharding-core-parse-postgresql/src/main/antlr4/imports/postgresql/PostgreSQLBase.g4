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

grammar PostgreSQLBase;

import Symbol, Keyword, Literals, BaseRule;

columnDefinition
    : columnName dataType collateClause? columnConstraint*
    ;

collateClause
    : COLLATE collationName
    ;

columnConstraint
    : constraintClause? columnConstraintOption constraintOptionalParam
    ;

constraintClause
    : CONSTRAINT ignoredIdentifier_
    ;

columnConstraintOption
    : NOT? NULL
    | checkOption
    | DEFAULT defaultExpr
    | GENERATED (ALWAYS | BY DEFAULT) AS IDENTITY (LP_ sequenceOptions RP_)?
    | UNIQUE indexParameters
    | primaryKey indexParameters
    | REFERENCES tableName (LP_ columnName RP_)? (MATCH FULL | MATCH PARTIAL | MATCH SIMPLE)?(ON DELETE action)? foreignKeyOnAction*
    ;

checkOption
    : CHECK expr (NO INHERIT)?
    ;

defaultExpr
    : CURRENT_TIMESTAMP | expr
    ;

sequenceOptions
    : sequenceOption+
    ;

sequenceOption
    : START WITH? NUMBER_
    | INCREMENT BY? NUMBER_
    | MAXVALUE NUMBER_
    | NO MAXVALUE
    | MINVALUE NUMBER_
    | NO MINVALUE
    | CYCLE
    | NO CYCLE
    | CACHE NUMBER_
    ;

indexParameters
    : (USING INDEX TABLESPACE ignoredIdentifier_)?
    ;

action
    : NO ACTION | RESTRICT | CASCADE | SET (NULL | DEFAULT)
    ;

foreignKeyOnAction
    : ON (UPDATE foreignKeyOn | DELETE foreignKeyOn)
    ;

foreignKeyOn
    : RESTRICT | CASCADE | SET NULL | NO ACTION | SET DEFAULT
    ;

constraintOptionalParam
    : (NOT? DEFERRABLE)? (INITIALLY (DEFERRED | IMMEDIATE))?
    ;

dataType
    : dataTypeName_ intervalFields? dataTypeLength? (WITHOUT TIME ZONE | WITH TIME ZONE)? (LBT_ RBT_)* | IDENTIFIER_
    ;

dataTypeName_
    : IDENTIFIER_ IDENTIFIER_ | IDENTIFIER_
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

privateExprOfDb
    : aggregateExpression
    | windowFunction
    | arrayConstructorWithCast
    | (TIMESTAMP (WITH TIME ZONE)? STRING_)
    | extractFromFunction
    ;

pgExpr
    : castExpr | collateExpr | expr
    ;

aggregateExpression
    : IDENTIFIER_ (LP_ (ALL | DISTINCT)? exprs orderByClause? RP_) asteriskWithParen (LP_ exprs RP_ WITHIN GROUP LP_ orderByClause RP_) filterClause?
    ;

filterClause
    : FILTER LP_ WHERE booleanPrimary RP_
    ;

asteriskWithParen
    : LP_ ASTERISK_ RP_
    ;

windowFunction
    : IDENTIFIER_ (exprList | asteriskWithParen) filterClause? windowFunctionWithClause
    ;

windowFunctionWithClause
    : OVER (IDENTIFIER_ | LP_ windowDefinition RP_)
    ;

windowDefinition
    : IDENTIFIER_? (PARTITION BY exprs)? (orderByClause (COMMA_ orderByClause)*)? frameClause?
    ;

orderByClause
    : ORDER BY expr (ASC | DESC | USING operator)? (NULLS (FIRST | LAST))?
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
    : EXTRACT LP_ IDENTIFIER_ FROM IDENTIFIER_ RP_
    ;
