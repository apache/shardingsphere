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

import Symbol, Keyword, MySQLKeyword, Literals, BaseRule;

insert
    : INSERT insertSpecification INTO? tableName partitionNames? (insertValuesClause | setAssignmentsClause | insertSelectClause) onDuplicateKeyClause?
    ;

insertSpecification
    : (LOW_PRIORITY | DELAYED | HIGH_PRIORITY)? IGNORE?
    ;

insertValuesClause
    : (LP_ RP_ | columnNames)? (VALUES | VALUE) (assignmentValues (COMMA_ assignmentValues)* | rowConstructorList) valueReference?
    ;

insertSelectClause
    : valueReference? columnNames? select
    ;

onDuplicateKeyClause
    : ON DUPLICATE KEY UPDATE assignment (COMMA_ assignment)*
    ;

valueReference
    : AS alias derivedColumns?
    ;

derivedColumns
    : LP_ alias (COMMA_ alias)* RP_
    ;

replace
    : REPLACE replaceSpecification? INTO? tableName partitionNames? (replaceValuesClause | setAssignmentsClause | replaceSelectClause)
    ;

replaceSpecification
    : LOW_PRIORITY | DELAYED
    ;

replaceValuesClause
    : columnNames? (VALUES | VALUE) (assignmentValues (COMMA_ assignmentValues)* | rowConstructorList) valueReference?
    ;

replaceSelectClause
    : valueReference? columnNames? select
    ;

update
    : withClause? UPDATE updateSpecification_ tableReferences setAssignmentsClause whereClause? orderByClause? limitClause?
    ;

updateSpecification_
    : LOW_PRIORITY? IGNORE?
    ;

assignment
    : columnName EQ_ assignmentValue
    ;

setAssignmentsClause
    : valueReference? SET assignment (COMMA_ assignment)*
    ;

assignmentValues
    : LP_ assignmentValue (COMMA_ assignmentValue)* RP_
    | LP_ RP_
    ;

assignmentValue
    : expr | DEFAULT | blobValue
    ;

blobValue
    : UL_BINARY STRING_
    ;

delete
    : DELETE deleteSpecification (singleTableClause | multipleTablesClause) whereClause? orderByClause? limitClause?
    ;

deleteSpecification
    : LOW_PRIORITY? QUICK? IGNORE?
    ;

singleTableClause
    : FROM tableName (AS? alias)? partitionNames?
    ;

multipleTablesClause
    : tableAliasRefList FROM tableReferences | FROM tableAliasRefList USING tableReferences
    ;

multipleTableNames
    : tableName DOT_ASTERISK_? (COMMA_ tableName DOT_ASTERISK_?)*
    ;

select
    : queryExpression lockClauseList?
    | queryExpressionParens
    | selectWithInto
    ;

selectWithInto
    : LP_ selectWithInto RP_
    | queryExpression selectIntoExpression lockClauseList?
    | queryExpression lockClauseList selectIntoExpression
    ;

queryExpression
    : withClause? (queryExpressionBody | queryExpressionParens) orderByClause? limitClause?
    ;

queryExpressionBody
    : queryPrimary
    | queryExpressionParens UNION unionOption? (queryPrimary | queryExpressionParens)
    | queryExpressionBody UNION unionOption? (queryPrimary | queryExpressionParens)
    ;

queryExpressionParens
    : LP_ (queryExpressionParens | queryExpression lockClauseList?) RP_
    ;

queryPrimary
    : querySpecification
    | tableValueConstructor
    | explicitTable
    ;

querySpecification
    : SELECT selectSpecification* projections selectIntoExpression? fromClause? whereClause? groupByClause? havingClause? windowClause?
    ;

call
    : CALL identifier (LP_ (expr (COMMA_ expr)*)? RP_)?
    ;

doStatement
    : DO expr (COMMA_ expr)*
    ;

handlerStatement
    : handlerOpenStatement | handlerReadIndexStatement | handlerReadStatement | handlerCloseStatement
    ;

handlerOpenStatement
    : HANDLER tableName OPEN (AS? identifier)?
    ;

handlerReadIndexStatement
    : HANDLER tableName READ indexName ( comparisonOperator LP_ identifier RP_ | (FIRST | NEXT | PREV | LAST) )
    whereClause? limitClause?
    ;

handlerReadStatement
    : HANDLER tableName READ (FIRST | NEXT)
    whereClause? limitClause?
    ;

handlerCloseStatement
    : HANDLER tableName CLOSE
    ;

importStatement
    : IMPORT TABLE FROM STRING_ (COMMA_ STRING_)?
    ;

loadDataStatement
    : LOAD DATA
      (LOW_PRIORITY | CONCURRENT)? LOCAL? 
      INFILE STRING_
      (REPLACE | IGNORE)?
      INTO TABLE tableName partitionNames?
      (CHARACTER SET identifier)?
      ((FIELDS | COLUMNS) selectFieldsInto+ )?
      ( LINES selectLinesInto+ )?
      ( IGNORE numberLiterals (LINES | ROWS) )?
      fieldOrVarSpec?
      (setAssignmentsClause)?
    ;

loadXmlStatement
    : LOAD XML
      (LOW_PRIORITY | CONCURRENT)? LOCAL? 
      INFILE STRING_
      (REPLACE | IGNORE)?
      INTO TABLE tableName
      (CHARACTER SET identifier)?
      (ROWS IDENTIFIED BY LT_ STRING_ GT_)?
      ( IGNORE numberLiterals (LINES | ROWS) )?
      fieldOrVarSpec?
      (setAssignmentsClause)?
    ;

explicitTable
    : TABLE tableName
    ;

tableValueConstructor
    : VALUES rowConstructorList
    ;

columnDesignator
    : STRING_
    ;

rowConstructorList
    : ROW assignmentValues (COMMA_ ROW assignmentValues)*
    ;

withClause
    : WITH RECURSIVE? cteClause (COMMA_ cteClause)*
    ;

cteClause
    : ignoredIdentifier columnNames? AS subquery
    ;

selectSpecification
    : duplicateSpecification | HIGH_PRIORITY | STRAIGHT_JOIN | SQL_SMALL_RESULT | SQL_BIG_RESULT | SQL_BUFFER_RESULT | (SQL_CACHE | SQL_NO_CACHE) | SQL_CALC_FOUND_ROWS
    ;

duplicateSpecification
    : ALL | DISTINCT | DISTINCTROW
    ;

projections
    : (unqualifiedShorthand | projection) (COMMA_ projection)*
    ;

projection
    : expr (AS? alias)? | qualifiedShorthand
    ;

unqualifiedShorthand
    : ASTERISK_
    ;

qualifiedShorthand
    : identifier DOT_ASTERISK_
    ;

fromClause
    : FROM (DUAL | tableReferences)
    ;

tableReferences
    : tableReference (COMMA_ tableReference)*
    ;

escapedTableReference
    : tableFactor joinedTable*
    ;

tableReference
    : (tableFactor | LBE_ OJ escapedTableReference RBE_) joinedTable*
    ;

tableFactor
    : tableName partitionNames? (AS? alias)? indexHintList? | subquery AS? alias columnNames? | LP_ tableReferences RP_
    ;

partitionNames
    : PARTITION LP_ identifier (COMMA_ identifier)* RP_
    ;

indexHintList
    : indexHint (COMMA_ indexHint)*
    ;

indexHint
    : (USE | IGNORE | FORCE) (INDEX | KEY) (FOR (JOIN | ORDER BY | GROUP BY))? LP_ indexName (COMMA_ indexName)* RP_
    ;

//joinedTable
//    : ((INNER | CROSS)? JOIN | STRAIGHT_JOIN) tableFactor joinSpecification?
//    | (LEFT | RIGHT) OUTER? JOIN tableFactor joinSpecification
//    | NATURAL (INNER | (LEFT | RIGHT) (OUTER))? JOIN tableFactor
//    ;

joinedTable
    : innerJoinType tableReference joinSpecification?
    | outerJoinType tableReference joinSpecification
    | naturalJoinType tableFactor
    ;

innerJoinType
    : (INNER | CROSS)? JOIN
    | STRAIGHT_JOIN
    ;

outerJoinType
    : (LEFT | RIGHT) OUTER? JOIN
    ;

naturalJoinType
    : NATURAL INNER? JOIN
    | NATURAL (LEFT | RIGHT) OUTER? JOIN
    ;

joinSpecification
    : ON expr | USING columnNames
    ;

whereClause
    : WHERE expr
    ;

groupByClause
    : GROUP BY orderByItem (COMMA_ orderByItem)* (WITH ROLLUP)?
    ;

havingClause
    : HAVING expr
    ;

limitClause
    : LIMIT ((limitOffset COMMA_)? limitRowCount | limitRowCount OFFSET limitOffset)
    ;

limitRowCount
    : numberLiterals | parameterMarker
    ;
    
limitOffset
    : numberLiterals | parameterMarker
    ;

windowClause
    : WINDOW windowItem (COMMA_ windowItem)*
    ;

windowItem
    : ignoredIdentifier AS LP_ windowSpecification RP_
    ;

subquery
    : queryExpressionParens
    ;

selectLinesInto
    : STARTING BY STRING_ | TERMINATED BY STRING_
    ;

selectFieldsInto
    : TERMINATED BY STRING_ | OPTIONALLY? ENCLOSED BY STRING_ | ESCAPED BY STRING_
    ;

selectIntoExpression
    : INTO variable (COMMA_ variable )* | INTO DUMPFILE STRING_
    | (INTO OUTFILE STRING_ (CHARACTER SET IDENTIFIER_)?((FIELDS | COLUMNS) selectFieldsInto+)? (LINES selectLinesInto+)?)
    ;

lockClause
    : FOR lockStrength lockedRowAction?
    | FOR lockStrength
    | LOCK IN SHARE MODE
    ;

lockClauseList
    : lockClause+
    ;

lockStrength
    : UPDATE | SHARE
    ;

lockedRowAction
    : SKIP_SYMBOL LOCKED | NOWAIT
    ;

tableLockingList
    : OF tableAliasRefList
    ;

tableIdentOptWild
    : tableName DOT_ASTERISK_?
    ;

tableAliasRefList
    : tableIdentOptWild+
    ;
