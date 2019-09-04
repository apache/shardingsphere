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
    : INSERT insertSpecification_ INTO? tableName partitionNames_? (insertValuesClause | setAssignmentsClause | insertSelectClause) onDuplicateKeyClause?
    ;

insertSpecification_
    : (LOW_PRIORITY | DELAYED | HIGH_PRIORITY)? IGNORE?
    ;

insertValuesClause
    : columnNames? (VALUES | VALUE) assignmentValues (COMMA_ assignmentValues)*
    ;

insertSelectClause
    : columnNames? select
    ;

onDuplicateKeyClause
    : ON DUPLICATE KEY UPDATE assignment (COMMA_ assignment)*
    ;

replace
    : REPLACE replaceSpecification_? INTO? tableName partitionNames_? (insertValuesClause | setAssignmentsClause | insertSelectClause)
    ;

replaceSpecification_
    : LOW_PRIORITY | DELAYED
    ;

update
    : UPDATE updateSpecification_ tableReferences setAssignmentsClause whereClause? orderByClause? limitClause?
    ;

updateSpecification_
    : LOW_PRIORITY? IGNORE?
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
    : expr | DEFAULT | blobValue
    ;

blobValue
    : UL_BINARY STRING_
    ;

delete
    : DELETE deleteSpecification_ (singleTableClause_ | multipleTablesClause_) whereClause?
    ;

deleteSpecification_
    : LOW_PRIORITY? QUICK? IGNORE?
    ;

singleTableClause_
    : FROM tableName (AS? alias)? partitionNames_?
    ;

multipleTablesClause_
    : multipleTableNames_ FROM tableReferences | FROM multipleTableNames_ USING tableReferences
    ;

multipleTableNames_
    : tableName DOT_ASTERISK_? (COMMA_ tableName DOT_ASTERISK_?)*
    ;

select 
    : withClause_? unionClause_
    ;

callStatement
    : CALL identifier_ (LP_ (identifier_ | expr)? RP_)?
    ;

doStatement
    : DO expr (COMMA_ expr)?
    ;

handlerStatement
    : handlerOpenStatement | handlerReadIndexStatement | handlerReadStatement | handlerCloseStatement
    ;

handlerOpenStatement
    : HANDLER tableName OPEN (AS? identifier_)?
    ;

handlerReadIndexStatement
    : HANDLER tableName READ identifier_ ( comparisonOperator LP_ identifier_ RP_ | (FIRST | NEXT | PREV | LAST) ) 
    (WHERE expr)? (LIMIT numberLiterals)?
    ;

handlerReadStatement
    : HANDLER tableName READ (FIRST | NEXT)
    (WHERE expr)? (LIMIT numberLiterals)?
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
      INTO TABLE tableName
      (PARTITION LP_ identifier_ (COMMA_ identifier_)* RP_ )?
      (CHARACTER SET identifier_)?
      ( (FIELDS | COLUMNS) selectFieldsInto_+ )?
      ( LINES selectLinesInto_+ )?
      ( IGNORE numberLiterals (LINES | ROWS) )?
      ( LP_ identifier_ (COMMA_ identifier_)* RP_ )?
      (setAssignmentsClause)?
    ;

loadXmlStatement
    : LOAD XML
      (LOW_PRIORITY | CONCURRENT)? LOCAL? 
      INFILE STRING_
      (REPLACE | IGNORE)?
      INTO TABLE tableName
      (CHARACTER SET identifier_)?
      (ROWS IDENTIFIED BY LT_ STRING_ GT_)?
      ( IGNORE numberLiterals (LINES | ROWS) )?
      ( LP_ identifier_ (COMMA_ identifier_)* RP_ )?
      (setAssignmentsClause)?
    ;

withClause_
    : WITH RECURSIVE? cteClause_ (COMMA_ cteClause_)*
    ;

cteClause_
    : ignoredIdentifier_ columnNames? AS subquery
    ;

unionClause_
    : selectClause (UNION (ALL | DISTINCT)? selectClause)*
    ;

selectClause
    : SELECT selectSpecification_* selectItems fromClause? whereClause? groupByClause? havingClause? windowClause_? orderByClause? limitClause? selectIntoExpression_? lockClause?
    ;

selectSpecification_
    : duplicateSpecification | HIGH_PRIORITY | STRAIGHT_JOIN | SQL_SMALL_RESULT | SQL_BIG_RESULT | SQL_BUFFER_RESULT | (SQL_CACHE | SQL_NO_CACHE) | SQL_CALC_FOUND_ROWS
    ;

duplicateSpecification
    : ALL | DISTINCT | DISTINCTROW
    ;

selectItems
    : (unqualifiedShorthand | selectItem) (COMMA_ selectItem)*
    ;

selectItem
    : (columnName | expr) (AS? alias)? | qualifiedShorthand
    ;

alias
    : identifier_ | STRING_
    ;

unqualifiedShorthand
    : ASTERISK_
    ;

qualifiedShorthand
    : identifier_ DOT_ASTERISK_
    ;

fromClause
    : FROM tableReferences
    ;

tableReferences
    : escapedTableReference_ (COMMA_ escapedTableReference_)*
    ;

escapedTableReference_
    : tableReference  | LBE_ OJ tableReference RBE_
    ;

tableReference
    : (tableFactor joinedTable)+ | tableFactor joinedTable*
    ;

tableFactor
    : tableName partitionNames_? (AS? alias)? indexHintList_? | subquery columnNames? | LP_ tableReferences RP_
    ;

partitionNames_ 
    : PARTITION LP_ identifier_ (COMMA_ identifier_)* RP_
    ;

indexHintList_
    : indexHint_ (COMMA_ indexHint_)*
    ;

indexHint_
    : (USE | IGNORE | FORCE) (INDEX | KEY) (FOR (JOIN | ORDER BY | GROUP BY))? LP_ indexName (COMMA_ indexName)* RP_
    ;

joinedTable
    : ((INNER | CROSS)? JOIN | STRAIGHT_JOIN) tableFactor joinSpecification?
    | (LEFT | RIGHT) OUTER? JOIN tableFactor joinSpecification
    | NATURAL (INNER | (LEFT | RIGHT) (OUTER))? JOIN tableFactor
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

windowClause_
    : WINDOW windowItem_ (COMMA_ windowItem_)*
    ;

windowItem_
    : ignoredIdentifier_ AS LP_ windowSpecification_ RP_
    ;

subquery
    : LP_ unionClause_ RP_ AS? alias?
    ;

selectLinesInto_
    : STARTING BY STRING_ | TERMINATED BY STRING_
    ;

selectFieldsInto_
    : TERMINATED BY STRING_ | OPTIONALLY? ENCLOSED BY STRING_ | ESCAPED BY STRING_
    ;

selectIntoExpression_
    : INTO identifier_ (COMMA_ identifier_ )* | INTO DUMPFILE STRING_
    | (INTO OUTFILE STRING_ (CHARACTER SET IDENTIFIER_)?((FIELDS | COLUMNS) selectFieldsInto_+)? (LINES selectLinesInto_+)?)
    ;

lockClause
    : FOR UPDATE | LOCK IN SHARE MODE
    ;
