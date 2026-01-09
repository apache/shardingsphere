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
    : INSERT insertSpecification INTO? tableName partitionNames? (insertValuesClause | setAssignmentsClause | insertSelectClause) onDuplicateKeyClause?
    ;

insertSpecification
    : (LOW_PRIORITY | DELAYED | HIGH_PRIORITY)? IGNORE?
    ;

insertValuesClause
    : (LP_ fields? RP_ )? (VALUES | VALUE) (assignmentValues (COMMA_ assignmentValues)* | rowConstructorList) valueReference?
    ;

fields
    : insertIdentifier (COMMA_ insertIdentifier)*
    ;

insertIdentifier
    : columnRef | tableWild
    ;

tableWild
    : identifier DOT_ (identifier DOT_)? ASTERISK_
    ;

insertSelectClause
    : valueReference? (LP_ fields? RP_)? select
    ;

onDuplicateKeyClause
    : (AS identifier derivedColumns?)?  ON DUPLICATE KEY UPDATE assignment (COMMA_ assignment)*
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
    : (LP_ fields? RP_)? (VALUES | VALUE) (assignmentValues (COMMA_ assignmentValues)* | rowConstructorList) valueReference?
    ;

replaceSelectClause
    : valueReference? (LP_ fields? RP_)? select
    ;

update
    : withClause? UPDATE updateSpecification_ tableReferences setAssignmentsClause whereClause? orderByClause? limitClause?
    ;

updateSpecification_
    : LOW_PRIORITY? IGNORE?
    ;

assignment
    : columnRef EQ_ assignmentValue
    ;

setAssignmentsClause
    : valueReference? SET assignment (COMMA_ assignment)*
    ;

assignmentValues
    : LP_ assignmentValue (COMMA_ assignmentValue)* RP_
    | LP_ RP_
    ;

assignmentValue
    : blobValue | expr | DEFAULT
    ;

blobValue
    : UL_BINARY string_
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
    | queryExpressionParens
    | queryExpressionBody INTERSECT combineOption? queryExpressionBody
    | queryExpressionBody (UNION | EXCEPT) combineOption? queryExpressionBody
    ;

queryExpressionParens
    : LP_ (queryExpressionParens | queryExpression lockClauseList?) RP_
    ;

queryPrimary
    : querySpecification
    | tableValueConstructor
    | tableStatement
    ;

querySpecification
    : SELECT selectSpecification* projections selectIntoExpression? fromClause? whereClause? groupByClause? havingClause? windowClause?
    ;

call
    : CALL (owner DOT_)? identifier (LP_ (expr (COMMA_ expr)*)? RP_)?
    ;

doStatement
    : DO expr (AS? alias)? (COMMA_ expr (AS? alias)?)*
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
    : IMPORT TABLE FROM textString (COMMA_ textString)?
    ;

loadStatement
    : loadDataStatement | loadXmlStatement
    ;

loadDataStatement
    : LOAD DATA
      (LOW_PRIORITY | CONCURRENT)? LOCAL? 
      INFILE string_
      (REPLACE | IGNORE)?
      INTO TABLE tableName partitionNames?
      (CHARACTER SET identifier)?
      (COLUMNS selectFieldsInto+ )?
      ( LINES selectLinesInto+ )?
      ( IGNORE numberLiterals (LINES | ROWS) )?
      fieldOrVarSpec?
      (setAssignmentsClause)?
    ;

loadXmlStatement
    : LOAD XML
      (LOW_PRIORITY | CONCURRENT)? LOCAL? 
      INFILE string_
      (REPLACE | IGNORE)?
      INTO TABLE tableName
      (CHARACTER SET identifier)?
      (ROWS IDENTIFIED BY LT_ string_ GT_)?
      ( IGNORE numberLiterals (LINES | ROWS) )?
      fieldOrVarSpec?
      (setAssignmentsClause)?
    ;

tableStatement
    : TABLE tableName
    ;

tableValueConstructor
    : VALUES rowConstructorList
    ;

rowConstructorList
    : ROW assignmentValues (COMMA_ ROW assignmentValues)*
    ;

withClause
    : WITH RECURSIVE? cteClause (COMMA_ cteClause)*
    ;

cteClause
    : alias (LP_ columnNames RP_)? AS subquery
    ;

selectSpecification
    : duplicateSpecification | HIGH_PRIORITY | STRAIGHT_JOIN | SQL_SMALL_RESULT | SQL_BIG_RESULT | SQL_BUFFER_RESULT | SQL_NO_CACHE | SQL_CALC_FOUND_ROWS
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
    : (identifier DOT_)? identifier DOT_ASTERISK_
    ;

fromClause
    : FROM (DUAL | tableReferences)
    ;

tableReferences
    // DORIS CHANGED BEGIN
    : tableReference (COMMA_ tableReference)* (COMMA_ regularFunction)?
    // DORIS CHANGED END
    ;

escapedTableReference
    : tableFactor joinedTable*
    ;

tableReference
    : (tableFactor | LBE_ OJ escapedTableReference RBE_) joinedTable*
    ;

tableFactor
    : tableName partitionNames? (AS? alias)? indexHintList?
    | subquery AS? alias (LP_ columnNames RP_)?
    | expr (AS? alias)?
    | LATERAL subquery AS? alias (LP_ columnNames RP_)?
    | LP_ tableReferences RP_
    ;

partitionNames
    : PARTITION LP_ identifier (COMMA_ identifier)* RP_
    ;

indexHintList
    : indexHint (indexHint)*
    ;

indexHint
    : USE (INDEX | KEY) indexHintClause LP_ (indexNameList)? RP_
    | (IGNORE | FORCE) (INDEX | KEY) indexHintClause LP_ indexNameList RP_
    ;

indexHintClause
    : (FOR (JOIN | ORDER BY | GROUP BY))?
    ;

indexNameList
    : indexName (COMMA_ indexName)*
    ;

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
    : ON expr | USING LP_ columnNames RP_
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
    : identifier AS windowSpecification
    ;

subquery
    : queryExpressionParens
    ;

selectLinesInto
    : STARTING BY string_ | TERMINATED BY string_
    ;

selectFieldsInto
    : TERMINATED BY string_ | OPTIONALLY? ENCLOSED BY string_ | ESCAPED BY string_
    ;

selectIntoExpression
    : INTO variable (COMMA_ variable )* | INTO DUMPFILE string_
    | INTO OUTFILE string_ (CHARACTER SET charsetName)?(COLUMNS selectFieldsInto+)? (LINES selectLinesInto+)? (FORMAT AS formatName)? outfileProperties?
    ;

formatName
    : identifier
    ;

outfileProperties
    : PROPERTIES LP_ outfileProperty (COMMA_ outfileProperty)* RP_
    ;

outfileProperty
    : string_ EQ_ string_
    ;

lockClause
    : FOR lockStrength tableLockingList? lockedRowAction?
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
    : tableIdentOptWild (COMMA_ tableIdentOptWild)*
    ;
