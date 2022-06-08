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
    : withClause? INSERT top? INTO? tableName (AS? alias)? (insertDefaultValue | insertValuesClause | insertSelectClause)
    ;

insertDefaultValue
    : columnNames? outputClause? DEFAULT VALUES
    ;

insertValuesClause
    : columnNames? outputClause? VALUES assignmentValues (COMMA_ assignmentValues)*
    ;

insertSelectClause
    : columnNames? outputClause? select
    ;

update
    : withClause? UPDATE top? tableReferences setAssignmentsClause whereClause? (OPTION queryHint)?
    ;

assignment
    : columnName EQ_ assignmentValue
    ;

setAssignmentsClause
    : SET assignment (COMMA_ assignment)* fromClause?
    ;

assignmentValues
    : LP_ assignmentValue (COMMA_ assignmentValue)* RP_
    | LP_ RP_
    ;

assignmentValue
    : expr | DEFAULT
    ;

delete
    : withClause? DELETE top? (singleTableClause | multipleTablesClause) outputClause? whereClause? (OPTION queryHint)?
    ;

singleTableClause
    : FROM? LP_? tableName RP_? (AS? alias)?
    ;

multipleTablesClause
    : multipleTableNames FROM tableReferences | FROM multipleTableNames USING tableReferences
    ;

multipleTableNames
    : tableName DOT_ASTERISK_? (COMMA_ tableName DOT_ASTERISK_?)*
    ;

select
    : aggregationClause
    ;

aggregationClause
    : selectClause ((UNION (ALL)? | EXCEPT | INTERSECT) selectClause)*
    ;

selectClause
    : selectWithClause? SELECT duplicateSpecification? projections fromClause? whereClause? groupByClause? havingClause? orderByClause? forClause?
    ;

duplicateSpecification
    : ALL | DISTINCT
    ;

projections
    : (unqualifiedShorthand | projection) (COMMA_ projection)*
    ;

projection
    : (top | columnName | expr) (AS? alias)? | qualifiedShorthand
    ;

top
    : TOP LP_? topNum RP_? PERCENT? (WITH TIES)? (ROW_NUMBER LP_ RP_ OVER LP_ orderByClause RP_)?
    ;

topNum
    : numberLiterals | parameterMarker
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
    : tableName (AS? alias)? | subquery AS? alias columnNames? | LP_ tableReferences RP_
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

subquery
    : LP_ aggregationClause RP_
    ;

withClause
    : WITH cteClauseSet
    ;

cteClauseSet
    : cteClause (COMMA_ cteClause)*
    ;

cteClause
    : identifier columnNames? AS subquery
    ;

outputClause
    : OUTPUT (outputWithColumns | outputWithAaterisk) (INTO outputTableName columnNames?)?
    ;

outputWithColumns
    : outputWithColumn (COMMA_ outputWithColumn)*
    ;

outputWithColumn
    : (INSERTED | DELETED) DOT_ name (AS? alias)?
    ;

outputWithAaterisk
    : (INSERTED | DELETED) DOT_ASTERISK_
    ;

outputTableName
    : (AT_ name) | tableName
    ;

queryHint
    : (HASH | ORDER) GROUP
    | (CONCAT | HASH | MERGE) UNION
    | (LOOP | MERGE | HASH) JOIN
    | EXPAND VIEWS
    | FAST INT_NUM_
    | FORCE ORDER
    | (FORCE | DISABLE) EXTERNALPUSHDOWN
    | (FORCE | DISABLE) SCALEOUTEXECUTION
    | IGNORE_NONCLUSTERED_COLUMNSTORE_INDEX
    | KEEP PLAN
    | KEEPFIXED PLAN
    | MAX_GRANT_PERCENT EQ_ DECIMAL_NUM_
    | MIN_GRANT_PERCENT EQ_ DECIMAL_NUM_
    | MAXDOP INT_NUM_
    | MAXRECURSION INT_NUM_
    | NO_PERFORMANCE_SPOOL
    | OPTIMIZE FOR LP_ AT_ name (UNKNOWN | EQ_ identifier)* RP_
    | OPTIMIZE FOR UNKNOWN
    | PARAMETERIZATION (SIMPLE | FORCED)
    | QUERYTRACEON INT_NUM_
    | RECOMPILE
    | ROBUST PLAN
    | USE HINT LP_ useHitName* RP_
    | USE PLAN NCHAR_TEXT
    ;

useHitName
    : SQ_ ASSUME_JOIN_PREDICATE_DEPENDS_ON_FILTERS SQ_
    | SQ_ ASSUME_MIN_SELECTIVITY_FOR_FILTER_ESTIMATES SQ_
    | SQ_ DISABLE_BATCH_MODE_ADAPTIVE_JOINS SQ_
    | SQ_ DISABLE_BATCH_MODE_MEMORY_GRANT_FEEDBACK SQ_
    | SQ_ DISABLE_DEFERRED_COMPILATION_TV SQ_
    | SQ_ DISABLE_INTERLEAVED_EXECUTION_TVF SQ_
    | SQ_ DISABLE_OPTIMIZED_NESTED_LOOP SQ_
    | SQ_ DISABLE_OPTIMIZER_ROWGOAL SQ_
    | SQ_ DISABLE_PARAMETER_SNIFFING SQ_
    | SQ_ DISABLE_ROW_MODE_MEMORY_GRANT_FEEDBACK SQ_
    | SQ_ DISABLE_TSQL_SCALAR_UDF_INLINING SQ_
    | SQ_ DISALLOW_BATCH_MODE SQ_
    | SQ_ ENABLE_HIST_AMENDMENT_FOR_ASC_KEYS SQ_
    | SQ_ ENABLE_QUERY_OPTIMIZER_HOTFIXES SQ_
    | SQ_ FORCE_DEFAULT_CARDINALITY_ESTIMATION SQ_
    | SQ_ FORCE_LEGACY_CARDINALITY_ESTIMATION SQ_
    | SQ_ QUERY_OPTIMIZER_COMPATIBILITY_LEVEL_n SQ_
    | SQ_ QUERY_PLAN_PROFILE SQ_
    ;

forClause
    : FOR (BROWSE | forXmlClause | forJsonClause)
    ;

forXmlClause
    : XML ((RAW (LP_ stringLiterals RP_)? | AUTO) (commonDirectivesForXml (COMMA_ (XMLDATA | XMLSCHEMA (LP_ stringLiterals RP_)?))? (COMMA_ ELEMENTS (XSINIL | ABSENT)?)?)?
    | EXPLICIT (commonDirectivesForXml (COMMA_ XMLDATA)?)?
    | PATH (LP_ stringLiterals RP_)? (commonDirectivesForXml (COMMA_ ELEMENTS (XSINIL | ABSENT)?)?)?)
    ;

commonDirectivesForXml
    : (COMMA_ BINARY BASE64)? (COMMA_ TYPE)? (COMMA_ ROOT (LP_ stringLiterals RP_)?)?
    ;

forJsonClause
    : JSON ((AUTO | PATH) ((COMMA_ ROOT (LP_ stringLiterals RP_)?)? (COMMA_ INCLUDE_NULL_VALUES)? (COMMA_ WITHOUT_ARRAY_WRAPPER)?)?)
    ;

selectWithClause
    : WITH (xmlNamespacesClause COMMA_?)? cteClauseSet?
    ;

xmlNamespacesClause
    : XMLNAMESPACES LP_ xmlNamespaceDeclarationItem (COMMA_ xmlNamespaceDeclarationItem)* RP_
    ;

xmlNamespaceDeclarationItem
    : xmlNamespaceUri AS xmlNamespacePrefix | xmlDefaultNamespaceDeclarationItem
    ;

xmlNamespaceUri
    : stringLiterals
    ;

xmlNamespacePrefix
    : identifier
    ;

xmlDefaultNamespaceDeclarationItem
    : DEFAULT xmlNamespaceUri
    ;
