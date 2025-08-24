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

parser grammar DMLStatement;

import BaseRule;

options {tokenVocab = ModeLexer;}

insert
    : withClause? INSERT INTO insertTarget insertRest optOnConflict? returningClause?
    ;

insertTarget
    : qualifiedName | qualifiedName AS colId
    ;

insertRest
    : select
    | OVERRIDING overrideKind VALUE select
    | LP_ insertColumnList RP_ select
    | LP_ insertColumnList RP_ OVERRIDING overrideKind VALUE select
    | DEFAULT VALUES
    ;

overrideKind
    : USER | SYSTEM
    ;

insertColumnList
    : insertColumnItem
    | insertColumnList COMMA_ insertColumnItem
    ;

insertColumnItem
    : colId optIndirection
    ;

optOnConflict
    : ON CONFLICT optConfExpr DO UPDATE SET setClauseList whereClause?
    | ON CONFLICT optConfExpr DO NOTHING
    ;

optConfExpr
    : LP_ indexParams RP_ whereClause?
    | ON CONSTRAINT name
    |
    ;

update
    : withClause? UPDATE relationExprOptAlias SET setClauseList fromClause? whereOrCurrentClause? returningClause?
    ;

setClauseList
    : setClause
    | setClauseList COMMA_ setClause
    ;

setClause
    : setTarget EQ_ aExpr
    | LP_ setTargetList RP_ EQ_ aExpr
    ;

setTarget
    : colId
    ;

setTargetList
    : setTarget
    | setTargetList COMMA_ setTarget
    ;

returningClause
    : RETURNING targetList
    ;

delete
    : withClause? DELETE FROM relationExprOptAlias usingClause? whereOrCurrentClause? returningClause?
    ;

relationExprOptAlias
    : relationExpr
    | relationExpr colId
    | relationExpr AS colId
    ;

usingClause
    : USING fromList
    ;

select
    : selectNoParens | selectWithParens
    ;

selectWithParens
    : LP_ selectNoParens RP_ | LP_ selectWithParens RP_
    ;

selectNoParens
    : selectClauseN
    | selectClauseN sortClause
    | selectClauseN sortClause? forLockingClause selectLimit?
    | selectClauseN sortClause? selectLimit forLockingClause?
    | withClause selectClauseN
    | withClause selectClauseN sortClause
    | withClause selectClauseN sortClause? forLockingClause selectLimit?
    | withClause selectClauseN sortClause? selectLimit forLockingClause?
    ;

selectClauseN
    : simpleSelect
    | selectWithParens
    | selectClauseN INTERSECT allOrDistinct? selectClauseN
    | selectClauseN (UNION | EXCEPT) allOrDistinct? selectClauseN
    ;

simpleSelect
    : SELECT ALL? targetList? intoClause? fromClause? whereClause? groupClause? havingClause? windowClause?
    | SELECT distinctClause targetList intoClause? fromClause? whereClause? groupClause? havingClause? windowClause?
    | valuesClause
    | TABLE relationExpr
    ;

withClause
    : WITH cteList
    | WITH RECURSIVE cteList
    ;

intoClause
    : INTO optTempTableName
    ;

optTempTableName
    : TEMPORARY TABLE? qualifiedName
    | TEMP TABLE? qualifiedName
    | LOCAL TEMPORARY TABLE? qualifiedName
    | LOCAL TEMP TABLE? qualifiedName
    | GLOBAL TEMPORARY TABLE? qualifiedName
    | GLOBAL TEMP TABLE? qualifiedName
    | UNLOGGED TABLE? qualifiedName
    | TABLE? qualifiedName
    | qualifiedName
    ;

cteList
    : commonTableExpr (COMMA_ commonTableExpr)*
    ;

commonTableExpr
    :  alias optNameList AS optMaterialized LP_ preparableStmt RP_
    ;

optMaterialized
    : MATERIALIZED | NOT MATERIALIZED |
    ;

optNameList
    :LP_ nameList RP_ |
    ;

preparableStmt
    : select
    | insert
    | update
    | delete
    ;

forLockingClause
    : forLockingItems | FOR READ ONLY
    ;

forLockingItems
    : forLockingItem
    | forLockingItems forLockingItem
    ;

forLockingItem
    : forLockingStrength lockedRelsList? nowaitOrSkip?
    ;

nowaitOrSkip
    : NOWAIT
    | APOSTROPHE_SKIP LOCKED
    ;

forLockingStrength
    : FOR UPDATE
    | FOR NO KEY UPDATE
    | FOR SHARE
    | FOR KEY SHARE
    ;

lockedRelsList
    : OF qualifiedNameList
    ;

qualifiedNameList
    : qualifiedName
    | qualifiedNameList COMMA_ qualifiedName
    ;

selectLimit
    : limitClause offsetClause?
    | offsetClause limitClause?
    ;

valuesClause
    : VALUES LP_ exprList RP_
    | valuesClause COMMA_ LP_ exprList RP_
    ;

limitClause
    : LIMIT selectLimitValue
    | FETCH firstOrNext selectFetchValue? rowOrRows onlyOrWithTies
    ;

offsetClause
    : OFFSET selectOffsetValue rowOrRows?
    ;

selectLimitValue
    : cExpr
    | ALL
    ;

selectOffsetValue
    : cExpr
    ;

selectFetchValue
    : cExpr
    ;

rowOrRows
    : ROW | ROWS
    ;

firstOrNext
    : FIRST | NEXT
    ;

onlyOrWithTies
    : ONLY | WITH TIES
    ;

targetList
    : targetEl
    | targetList COMMA_ targetEl
    ;

targetEl
    : colId DOT_ASTERISK_
    | aExpr AS identifier
    | aExpr identifier
    | aExpr
    | ASTERISK_
    | colId DOT_ASTERISK_ AS identifier
    ;

groupClause
    : GROUP BY groupByList
    ;

groupByList
    : groupByItem (COMMA_ groupByItem)*
    ;

groupByItem
    : aExpr
    | emptyGroupingSet
    | cubeClause
    | rollupClause
    | groupingSetsClause
    ;

emptyGroupingSet
    : LP_ RP_
    ;

rollupClause
    : ROLLUP LP_ exprList RP_
    ;

cubeClause
    : CUBE LP_ exprList RP_
    ;

groupingSetsClause
    : GROUPING SETS LP_ groupByList RP_
    ;

windowClause
    : WINDOW windowDefinitionList
    ;

windowDefinitionList
    : windowDefinition
    | windowDefinitionList COMMA_ windowDefinition
    ;

windowDefinition
    : colId AS windowSpecification
    ;

windowSpecification
    : LP_ existingWindowName? partitionClause? sortClause? frameClause? RP_
    ;

existingWindowName
    : colId
    ;

partitionClause
    : PARTITION BY exprList
    ;

frameClause
    : RANGE frameExtent optWindowExclusionClause
    | ROWS frameExtent optWindowExclusionClause
    | GROUPS frameExtent optWindowExclusionClause
    ;

frameExtent
    : frameBound
    | BETWEEN frameBound AND frameBound
    ;

frameBound
    : UNBOUNDED PRECEDING
    | UNBOUNDED FOLLOWING
    | CURRENT ROW
    | aExpr PRECEDING
    | aExpr FOLLOWING
    ;

optWindowExclusionClause
    : EXCLUDE CURRENT ROW
    | EXCLUDE GROUP
    | EXCLUDE TIES
    | EXCLUDE NO OTHERS
    |
    ;

alias
    : identifier | STRING_
    ;

fromClause
    : FROM fromList
    ;

fromList
    : tableReference | fromList COMMA_ tableReference
    ;

tableReference
    : relationExpr aliasClause?
    | relationExpr aliasClause? tablesampleClause
    | functionTable funcAliasClause?
    | LATERAL functionTable funcAliasClause?
    | xmlTable aliasClause?
    | LATERAL xmlTable aliasClause?
    | selectWithParens aliasClause?
    | LATERAL selectWithParens aliasClause?
    | tableReference joinedTable
    | LP_ tableReference joinedTable RP_ aliasClause?
    ;

joinedTable
    : crossJoinType tableReference
    | innerJoinType tableReference joinQual
    | outerJoinType tableReference joinQual
    | naturalJoinType tableReference
    ;

crossJoinType
    : CROSS JOIN
    ;

innerJoinType
    : INNER? JOIN
    ;

outerJoinType
    : (FULL | LEFT | RIGHT) OUTER? JOIN
    ;

naturalJoinType
    : NATURAL INNER? JOIN
    | NATURAL (FULL | LEFT | RIGHT) OUTER? JOIN
    ;

joinOuter
    : OUTER
    ;

joinQual
    : USING LP_ nameList RP_
    | ON aExpr
    ;


relationExpr
    : qualifiedName
    | qualifiedName ASTERISK_
    | ONLY qualifiedName
    | ONLY LP_ qualifiedName RP_
    ;

whereClause
    : WHERE aExpr
    ;

whereOrCurrentClause
    : whereClause
    | WHERE CURRENT OF cursorName
    ;

havingClause
    : HAVING aExpr
    ;

doStatement
    : DO dostmtOptList
    ;

dostmtOptList
    : dostmtOptItem+
    ;

dostmtOptItem
    : STRING_ | LANGUAGE nonReservedWordOrSconst
    ;

copy
    : copyWithTableOrQuery | copyWithTableOrQueryBinaryCsv | copyWithTableBinary
    ;

copyWithTableOrQuery
    : COPY (qualifiedName columnNames? | LP_ preparableStmt RP_) (FROM | TO) (fileName | PROGRAM STRING_ | STDIN | STDOUT) (WITH? LP_ copyOptionList RP_)? whereClause?
    ;

copyOptionList
    : copyOption (COMMA_ copyOption)*
    ;

copyOption
    : FORMAT identifier
    | FREEZE booleanValue?
    | DELIMITER STRING_
    | NULL STRING_
    | HEADER booleanValue?
    | QUOTE STRING_
    | ESCAPE STRING_
    | FORCE_QUOTE (columnNames | ASTERISK_)
    | FORCE_NOT_NULL columnNames
    | FORCE_NULL columnNames
    | ENCODING STRING_
    ;


copyWithTableOrQueryBinaryCsv
    : COPY (qualifiedName columnNames? | LP_ preparableStmt RP_) (FROM | TO) (fileName | STDIN | STDOUT) (WITH? BINARY? (DELIMITER AS? STRING_)? (NULL AS? STRING_)? (CSV HEADER? (QUOTE AS? STRING_)? (ESCAPE AS? STRING_)? (FORCE NOT NULL columnName (COMMA_ columnName)*)? (FORCE QUOTE (columnName (COMMA_ columnName)* | ASTERISK_))?)?)?
    ;

copyWithTableBinary
    : COPY BINARY? qualifiedName (FROM | TO) (fileName | STDIN | STDOUT) (USING? DELIMITERS STRING_)? (WITH NULL AS STRING_)?
    ;


