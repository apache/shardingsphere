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

import Symbol, Keyword, PostgreSQLKeyword, Literals, BaseRule;

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
    : colId optIndirection
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
    | selectClauseN UNION allOrDistinct selectClauseN
    | selectClauseN INTERSECT allOrDistinct selectClauseN
    | selectClauseN EXCEPT allOrDistinct selectClauseN
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
    : commonTableExpr
	| cteList COMMA_ commonTableExpr
	;

commonTableExpr
    :  name optNameList AS optMaterialized LP_ preparableStmt RP_
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
	| 'skip' LOCKED
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

qualifiedName
    : colId | colId indirection
	;

selectLimit
    : limitClause offsetClause
	| offsetClause limitClause
	| limitClause
	| offsetClause
	;

valuesClause
    : VALUES LP_ exprList RP_
	| valuesClause COMMA_ LP_ exprList RP_
	;

limitClause
    : LIMIT selectLimitValue
	| LIMIT selectLimitValue COMMA_ selectOffsetValue
	| FETCH firstOrNext selectFetchFirstValue rowOrRows ONLY
	| FETCH firstOrNext selectFetchFirstValue rowOrRows WITH TIES
	| FETCH firstOrNext rowOrRows ONLY
	| FETCH firstOrNext rowOrRows WITH TIES
	;

offsetClause
    : OFFSET selectOffsetValue
	| OFFSET selectFetchFirstValue rowOrRows
	;

selectLimitValue
    : aExpr
	| ALL
	;

selectOffsetValue
    : aExpr
	;

selectFetchFirstValue
    : cExpr
	| PLUS_ NUMBER_
	| MINUS_ NUMBER_
	;

rowOrRows
    : ROW | ROWS
	;

firstOrNext
    : FIRST | NEXT
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
	| tableReference LP_ joinedTable RP_ aliasClause
	;

joinedTable
    : CROSS JOIN tableReference
	| joinType JOIN tableReference joinQual
	| JOIN tableReference joinQual
	| NATURAL joinType JOIN tableReference
	| NATURAL JOIN tableReference
	;

joinType
    : FULL joinOuter?
	| LEFT joinOuter?
	| RIGHT joinOuter?
	| INNER
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

call
    : CALL funcName LP_ callClauses? RP_
    ;

callClauses
    : (ALL | DISTINCT)? funcArgList sortClause?
    | VARIADIC funcArgExpr sortClause
    | funcArgList COMMA_ VARIADIC funcArgExpr sortClause
    | ASTERISK_
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

lock
    : LOCK (TABLE)? relationExprList (IN lockType MODE)? (NOWAIT)?
    ;

lockType
    : ACCESS SHARE
    | ROW SHARE
    | ROW EXCLUSIVE
    | SHARE UPDATE EXCLUSIVE
    | SHARE
    | SHARE ROW EXCLUSIVE
    | EXCLUSIVE
    | ACCESS EXCLUSIVE
    ;

checkpoint
    : CHECKPOINT
    ;

copy
    : COPY (BINARY)? qualifiedName optColumnList (FROM | TO) (PROGRAM)?
      (STRING_ | STDIN | STDOUT) copyDelimiter (WITH)? copyOptions whereClause?
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
