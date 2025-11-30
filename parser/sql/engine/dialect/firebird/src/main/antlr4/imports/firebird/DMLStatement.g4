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

grammar DMLStatement;

import BaseRule;

insert
    : INSERT INTO? tableName (insertValuesClause | insertSelectClause) returningClause?
    ;

insertValuesClause
    : columnNames? (VALUES | VALUE) assignmentValues (COMMA_ assignmentValues)*
    ;

insertSelectClause
    : columnNames? select
    ;

returningClause
    : RETURNING projections
    ;

update
    : UPDATE tableReferences setAssignmentsClause whereClause? returningClause?
    ;

assignment
    : columnName EQ_ VALUES? LP_? assignmentValue RP_?
    ;

setAssignmentsClause
    : SET assignment (COMMA_ assignment)*
    ;

assignmentValues
    : LP_ assignmentValue (COMMA_ assignmentValue)* RP_
    | LP_ RP_
    ;

assignmentValue
    : expr | DEFAULT | blobValue | bindLiterals
    ;

blobValue
    : STRING_
    ;

delete
    : DELETE singleTableClause whereClause? returningClause?
    ;

singleTableClause
    : FROM tableName (AS? alias)?
    ;

select
    : withClause? combineClause
    ;

combineClause
    : selectClause (UNION (DISTINCT | ALL)? selectClause)*
    ;

selectClause
    : SELECT firstSkipClause? selectSpecification* projections fromClause? whereClause? groupByClause? havingClause? orderByClause? limitClause? optimizeClause?
    ;

selectSpecification
    : duplicateSpecification
    ;

firstSkipClause
    : FIRST firstValue (SKIP_ skipValue)?
    | SKIP_ skipValue
    ;

firstValue
    : LP_? (numberLiterals | parameterMarker) RP_?
    ;

skipValue
    : LP_? (numberLiterals | parameterMarker) RP_?
    ;

duplicateSpecification
    : ALL | DISTINCT
    ;

projections
    : (unqualifiedShorthand | projection) (COMMA_ projection)*
    ;

projection
    : (columnName | expr) (AS? alias)? | qualifiedShorthand
    ;

alias
    : identifier | STRING_
    ;

unqualifiedShorthand
    : ASTERISK_
    ;

qualifiedShorthand
    : identifier DOT_ASTERISK_
    ;

fromClause
    : FROM tableReferences joinedTable?
    ;

tableReferences
    : escapedTableReference (COMMA_ escapedTableReference)*
    ;

escapedTableReference
    : tableReference | LBE_ tableReference RBE_
    ;

tableReference
    : tableFactor joinedTable*
    ;

tableFactor
    : tableName (AS? alias)?
    | subquery (AS? alias)? columnNames?
    | expr (AS? alias)?
    | LP_ tableReferences RP_
    ;

joinedTable
    : ((INNER | CROSS)? JOIN) tableFactor joinSpecification?
    | (LEFT | RIGHT | FULL) OUTER? JOIN tableFactor joinSpecification
    | NATURAL (INNER | (LEFT | RIGHT | FULL) (OUTER?))? JOIN tableFactor
    ;

joinSpecification
    : ON expr | USING columnNames
    ;

whereClause
    : WHERE (expr | CURRENT OF cursorName)
    ;

groupByClause
    : GROUP BY orderByItem (COMMA_ orderByItem)*
    ;

havingClause
    : HAVING expr
    ;

subquery
    : LP_ (withClause? combineClause) RP_
    ;

withClause
    : WITH RECURSIVE? cteClause (COMMA_ cteClause)*
    ;

cteClause
    : alias (LP_ columnNames RP_)? AS subquery
    ;

merge
    : MERGE intoClause usingClause
    mergeWhen (mergeWhen)*
    (RETURNING returnExprListClause (INTO variableListClause)?)?
    ;

intoClause
    : INTO (tableName | viewName | subquery) (AS? alias)?
    ;

usingClause
    : USING ((tableName | viewName) | subquery) (AS? alias)? ON expr
    ;

mergeWhen
    : mergeWhenMatched | mergeWhenNotMatched
    ;

mergeWhenMatched
    : WHEN MATCHED (AND expr)? THEN (UPDATE SET columnName EQ_ expr (COMMA_ (columnName EQ_ expr))* | DELETE )
    ;

mergeWhenNotMatched
    : WHEN NOT MATCHED (AND expr)? THEN INSERT columnNames? VALUES LP_ expr RP_
    ;

returnExpr
    : expr (AS? alias)
    ;

returnExprListClause
    : returnExpr (COMMA_ returnExpr)*
    ;

variableList
    : LBT_ COLON_ RBT_ variableName
    ;

variableListClause
    : variableList (COMMA_ variableList)*
    ;
