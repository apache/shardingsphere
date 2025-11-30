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
    : withClause? INSERT INTO insertTarget insertRest optOnDuplicateKey? returningClause?
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

optOnDuplicateKey
    : ON DUPLICATE KEY UPDATE assignment (COMMA_ assignment)*
    | ON DUPLICATE KEY UPDATE NOTHING
    ;

assignment
    : setTarget EQ_ aExpr
    | setTarget EQ_ VALUES LP_ name RP_
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
    : colId (DOT_ colId)?
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
    : selectClauseN sortClause? (forLockingClause selectLimit? | selectLimit forLockingClause?)?
    | withClause selectClauseN
    | withClause selectClauseN sortClause
    | withClause selectClauseN sortClause? forLockingClause selectLimit?
    | withClause selectClauseN sortClause? selectLimit forLockingClause?
    ;

selectClauseN
    : simpleSelect
    | selectWithParens
    | selectClauseN INTERSECT allOrDistinct? selectClauseN
    | selectClauseN (UNION | EXCEPT | MINUS) allOrDistinct? selectClauseN
    ;

simpleSelect
    : SELECT distinctClause targetList intoClause? fromClause? whereClause? groupClause? havingClause? windowClause?
    | valuesClause
    | TABLE relationExpr
    | SELECT ALL? targetList? intoClause? fromClause? whereClause? groupClause? havingClause? windowClause?
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
    | LIMIT selectOffsetValue COMMA_ selectLimitValue
    | FETCH firstOrNext selectFetchValue? rowOrRows onlyOrWithTies
    ;

offsetClause
    : OFFSET selectOffsetValue rowOrRows?
    ;

selectLimitValue
    : ALL
    | cExpr
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
    | aExpr AS? identifier?
    | ASTERISK_
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
    : tableReference joinedTable
    | relationExpr
    | relationExpr aliasClause?
    | relationExpr tablesampleClause
    | relationExpr aliasClause? tablesampleClause
    | functionTable
    | functionTable funcAliasClause?
    | LATERAL functionTable
    | LATERAL functionTable funcAliasClause?
    | xmlTable
    | xmlTable aliasClause?
    | LATERAL xmlTable
    | LATERAL xmlTable aliasClause?
    | selectWithParens
    | selectWithParens aliasClause?
    | LATERAL selectWithParens
    | LATERAL selectWithParens aliasClause?
    | LP_ tableReference joinedTable RP_
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
    : COPY (BINARY)? qualifiedName (LP_ columnList RP_)? (FROM | TO) PROGRAM?
      (STRING_ | STDIN | STDOUT) copyDelimiter? (WITH)? copyOptions whereClause?
    | COPY LP_ preparableStmt RP_ TO PROGRAM? (STRING_ | STDIN | STDOUT) WITH? copyOptions
    ;

copyOptions
    : copyOptList | LP_ copyGenericOptList RP_
    ;

copyGenericOptList
    : copyGenericOptElem (COMMA_ copyGenericOptElem)*
    ;

copyGenericOptElem
    : colLabel copyGenericOptArg
    ;

copyGenericOptArg
    : booleanOrString
    | numericOnly
    | ASTERISK_
    | LP_ copyGenericOptArgList RP_
    ;

copyGenericOptArgList
    : copyGenericOptArgListItem (COMMA_ copyGenericOptArgListItem)*
    ;

copyGenericOptArgListItem
    : booleanOrString
    ;

copyOptList
    : copyOptItem*
    ;

copyOptItem
    : BINARY
    | FREEZE
    | DELIMITER (AS)? STRING_
    | NULL (AS)? STRING_
    | CSV
    | HEADER
    | QUOTE (AS)? STRING_
    | ESCAPE (AS)? STRING_
    | FORCE QUOTE columnList
    | FORCE QUOTE ASTERISK_
    | FORCE NOT NULL columnList
    | FORCE NULL columnList
    | ENCODING STRING_
    ;

copyDelimiter
    : (USING)? DELIMITERS STRING_
    ;
