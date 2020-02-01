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
    : INSERT INTO tableName (AS? alias)? (insertValuesClause | insertSelectClause)
    ;

insertValuesClause
    : columnNames? VALUES assignmentValues (COMMA_ assignmentValues)*
    ;

insertSelectClause
    : columnNames? select
    ;

update
    : UPDATE updateSpecification_? tableReferences setAssignmentsClause whereClause?
    ;

updateSpecification_
    : ONLY
    ;

assignment
    : columnName EQ_ assignmentValue
    ;

setAssignmentsClause
    : SET assignment (COMMA_ assignment)*
    ;

assignmentValues
    : LP_ assignmentValue (COMMA_ assignmentValue)* RP_
    | LP_ RP_
    ;

assignmentValue
    : expr | DEFAULT
    ;

delete
    : DELETE deleteSpecification_? (singleTableClause | multipleTablesClause) whereClause?
    ;

deleteSpecification_
    : ONLY
    ;

singleTableClause
    : FROM tableName (AS? alias)?
    ;

multipleTablesClause
    : multipleTableNames FROM tableReferences | FROM multipleTableNames USING tableReferences
    ;

multipleTableNames
    : tableName DOT_ASTERISK_? (COMMA_ tableName DOT_ASTERISK_?)*
    ;

select 
    : unionClause
    ;

unionClause
    : selectClause (UNION (ALL | DISTINCT)? selectClause)*
    ;

selectClause
    : SELECT duplicateSpecification? projections fromClause? whereClause? groupByClause? havingClause? orderByClause? limitClause?
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
    : FROM tableReferences
    ;

tableReferences
    : tableReference (COMMA_ tableReference)*
    ;

tableReference
    : tableFactor joinedTable*
    ;

tableFactor
    : tableName (AS? alias)? | subquery columnNames? | LP_ tableReferences RP_
    ;

joinedTable
    : NATURAL? ((INNER | CROSS)? JOIN) tableFactor joinSpecification?
    | NATURAL? (LEFT | RIGHT | FULL) OUTER? JOIN tableFactor joinSpecification?
    ;

joinSpecification
    : ON expr | USING columnNames
    ;

whereClause
    : WHERE expr
    ;

groupByClause
    : GROUP BY orderByItem (COMMA_ orderByItem)*
    ;

havingClause
    : HAVING expr
    ;

limitClause
    : limitRowCountSyntax_ limitOffsetSyntax_?
    | limitOffsetSyntax_ limitRowCountSyntax_?
    ;

limitRowCountSyntax_
    : LIMIT (ALL | limitRowCount)
    ;

limitRowCount
    : numberLiterals | parameterMarker
    ;

limitOffsetSyntax_
    : OFFSET limitOffset (ROW | ROWS)?
    ;

limitOffset
    : numberLiterals | parameterMarker
    ;

subquery
    : LP_ unionClause RP_ AS? alias?
    ;
