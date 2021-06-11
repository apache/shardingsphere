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

import Symbol, Keyword, OracleKeyword, Literals, BaseRule, Comments, DDLStatement;

insert
    : INSERT (insertSingleTable | insertMultiTable)
    ;

insertSingleTable
    : insertIntoClause (insertValuesClause | select)
    ;

insertMultiTable
    : (ALL multiTableElement+ | conditionalInsertClause) select
    ;

multiTableElement
    : insertIntoClause insertValuesClause
    ;

conditionalInsertClause
    : (ALL | FIRST)? conditionalInsertWhenPart+ conditionalInsertElsePart?
    ;

conditionalInsertWhenPart
    : WHEN expr THEN multiTableElement+
    ;

conditionalInsertElsePart
    : ELSE multiTableElement+
    ;

insertIntoClause
    : INTO tableName (AS? alias)?
    ;

insertValuesClause
    : columnNames? VALUES assignmentValues (COMMA_ assignmentValues)*
    ;

update
    : UPDATE updateSpecification? tableReferences setAssignmentsClause whereClause?
    ;

updateSpecification
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
    : DELETE deleteSpecification? (singleTableClause | multipleTablesClause) whereClause?
    ;

deleteSpecification
    : ONLY
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
    : selectSubquery forUpdateClause?
    ;

selectSubquery
    : (queryBlock | selectUnionClause | parenthesisSelectSubquery) orderByClause? rowLimitingClause
    ;

selectUnionClause
    : ((queryBlock | parenthesisSelectSubquery) orderByClause? rowLimitingClause) ((UNION ALL? | INTERSECT | MINUS) selectSubquery)+
    ;

parenthesisSelectSubquery
    : LP_ selectSubquery RP_
    ;

unionClause
    : queryBlock (UNION (ALL | DISTINCT)? queryBlock)*
    ;

queryBlock
    : withClause? SELECT hint? duplicateSpecification? projections fromClause? whereClause? groupByClause? havingClause?
    ;

withClause
    : WITH plsqlDeclarations? ((subqueryFactoringClause | subavFactoringClause) (COMMA_ (subqueryFactoringClause | subavFactoringClause))*)?
    ;

plsqlDeclarations
    : (functionDeclaration | procedureDeclaration)+
    ;

functionDeclaration
    : functionHeading ((DETERMINISTIC | PIPELINED | PARALLEL_ENABLE | RESULT_CACHE)+)?
    ;

functionHeading
    : FUNCTION functionName (LP_ parameterDeclaration (SQ_ parameterDeclaration)* RP_)? RETURN dataType
    ;

parameterDeclaration
    : parameterName ((IN? dataType ((COLON_ EQ_ | DEFAULT) expr)?) | (IN? OUT NOCOPY? dataType))?
    ;

procedureDeclaration
    : procedureHeading procedureProperties
    ;

procedureHeading
    : PROCEDURE procedureName (LP_ parameterDeclaration (SQ_ parameterDeclaration)* RP_)?
    ;

procedureProperties
    : (accessibleByClause | defaultCollationClause | invokerRightsClause)*
    ;

accessibleByClause
    : ACCESSIBLE BY LP_ accessor (COMMA_ accessor)* RP_
    ;

accessor
    : unitKind? unitName
    ;

unitKind
    : FUNCTION | PROCEDURE | PACKAGE | TRIGGER | TYPE
    ;

defaultCollationClause
    : DEFAULT COLLATION collationOption
    ;

collationOption
    : USING_NLS_COMP
    ;

invokerRightsClause
    : AUTHID (CURRENT_USER | DEFINER)
    ;

subqueryFactoringClause
    : queryName (LP_ alias (COMMA_ alias)* RP_)? AS LP_ selectSubquery RP_ searchClause? cycleClause? 
    ;

searchClause
    : SEARCH (DEPTH | BREADTH) FIRST BY (alias (ASC | DESC)? (NULLS FIRST | NULLS LAST)?) (COMMA_ (alias (ASC | DESC)? (NULLS FIRST | NULLS LAST)?))* SET orderingColumn
    ;

cycleClause
    : CYCLE alias (COMMA_ alias)* SET alias TO cycleValue DEFAULT noCycleValue
    ;

subavFactoringClause
    : subavName ANALYTIC VIEW AS LP_ subavClause RP_
    ;

subavClause
    : USING baseAvName hierarchiesClause? filterClauses? addCalcsClause?
    ;

hierarchiesClause
    : HIERARCHIES LP_ ((alias DOT_)? alias (COMMA_ (alias DOT_)? alias)*)? RP_
    ;

filterClauses
    : FILTER FACT LP_ filterClause (COMMA_ filterClause)* RP_
    ;

filterClause
    : (MEASURES | (alias DOT_)? alias) TO predicate
    ;

addCalcsClause
    : ADD MEASURES LP_ calcMeasClause (COMMA_ calcMeasClause)* RP_
    ;

calcMeasClause
    : measName AS LP_ calcMeasExpression RP_
    ;

calcMeasExpression
    : avExpression | expr
    ;

avExpression
    : avMeasExpression | avHierExpression
    ;

avMeasExpression
    : leadLagExpression | windowExpression | rankExpression | shareOfExpression | qdrExpression
    ;

leadLagExpression
    : leadLagFunctionName LP_ calcMeasExpression RP_ OVER LP_ leadLagClause RP_
    ;

leadLagFunctionName
    : LAG | LAG_DIFF | LAG_DIF_PERCENT | LEAD | LEAD_DIFF | LEAD_DIFF_PERCENT
    ;

leadLagClause
    : HIERARCHY hierarchyRef OFFSET offsetExpr ((WITHIN (LEVEL | PARENT)) | (ACROSS ANCESTOR AT LEVEL levelRef (POSITION FROM (BEGINNING | END))?))?
    ;

hierarchyRef
    : (alias DOT_)? alias
    ;

windowExpression
    : aggregationFunction OVER LP_ windowClause RP_
    ;

windowClause
    : HIERARCHY hierarchyRef BETWEEN (precedingBoundary | followingBoundary) (WITHIN (LEVEL | PARENT | ANCESTOR AT LEVEL levelRef))?
    ;

precedingBoundary
    : (UNBOUNDED PRECEDING | offsetExpr PRECEDING) AND (CURRENT MEMBER | offsetExpr (PRECEDING | FOLLOWING) | UNBOUNDED FOLLOWING)
    ;

followingBoundary
    : (CURRENT MEMBER | offsetExpr FOLLOWING) AND (offsetExpr FOLLOWING | UNBOUNDED FOLLOWING)
    ;

rankExpression
    : rankFunctionName LP_ RP_ OVER LP_ rankClause RP_
    ;

rankFunctionName
    : RANK | DENSE_RANK | AVERAGE_RANK | ROW_NUMBER
    ;

rankClause
    : HIERARCHY hierarchyRef ORDER BY calcMeasOrderByClause (COMMA_ calcMeasOrderByClause)* (WITHIN (LEVEL | PARENT | ANCESTOR AT LEVEL levelRef))?
    ;

calcMeasOrderByClause
    : calcMeasExpression (ASC | DESC)? (NULLS (FIRST | LAST))?
    ;

shareOfExpression
    : SHARE_OF LP_ calcMeasExpression shareClause RP_
    ;

shareClause
    : HIERARCHY hierarchyRef (PARENT | LEVEL levelRef | MEMBER memberExpression)
    ;

memberExpression
    : levelMemberLiteral | hierNavigationExpression | CURRENT MEMBER | NULL | ALL
    ;

levelMemberLiteral
    : levelRef (posMemberKeys | namedMemberKeys)
    ;

posMemberKeys
    : SQ_ LBT_ SQ_ memberKeyExpr (COMMA_ memberKeyExpr)* SQ_ RBT_ SQ_
    ;

namedMemberKeys
    : SQ_ LBT_ SQ_ (attributeName EQ_ memberKeyExpr) (COMMA_ (attributeName EQ_ memberKeyExpr))* SQ_ RBT_ SQ_
    ;

hierNavigationExpression
    : hierAncestorExpression | hierParentExpression | hierLeadLagExpression
    ;

hierAncestorExpression
    : HIER_ANCESTOR LP_ memberExpression AT (LEVEL levelRef | DEPTH depthExpression) RP_ 
    ;

hierParentExpression
    : HIER_PARENT LP_ memberExpression RP_
    ;

hierLeadLagExpression
    : (HIER_LEAD | HIER_LAG) LP_ hierLeadLagClause RP_
    ;

hierLeadLagClause
    : memberExpression OFFSET offsetExpr (WITHIN ((LEVEL | PARENT) | (ACROSS ANCESTOR AT LEVEL levelRef (POSITION FROM (BEGINNING | END))?)))?
    ;

qdrExpression
    : QUALIFY LP_ calcMeasExpression COMMA_ qualifier RP_
    ;

qualifier
    : hierarchyRef EQ_ memberExpression
    ;

avHierExpression
    : hierFunctionName LP_ memberExpression WITHIN HIERARCHY hierarchyRef RP_
    ;

hierFunctionName
    : HIER_CAPTION | HIER_DEPTH | HIER_DESCRIPTION | HIER_LEVEL | HIER_MEMBER_NAME | HIER_MEMBER_UNIQUE_NAME
    ;

duplicateSpecification
    : (DISTINCT | UNIQUE) | ALL
    ;

projections
    : (unqualifiedShorthand | projection) (COMMA_ projection)*
    ;

projection
    : (columnName | expr) (AS? alias)? | qualifiedShorthand
    ;

unqualifiedShorthand
    : ASTERISK_
    ;

qualifiedShorthand
    : identifier DOT_ASTERISK_
    ;

selectList
    : unqualifiedShorthand | selectProjection (COMMA_ selectProjection)*
    ;

selectProjection
    : ((queryName | (tableName | viewName | materializedViewName) | alias) DOT_ASTERISK_) | selectProjectionExprClause
    ;

selectProjectionExprClause
    : expr (AS? alias)?
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
    : tableName (AS? alias)? | subquery AS? alias? columnNames? | LP_ tableReferences RP_
    ;

joinedTable
    : NATURAL? ((INNER | CROSS)? JOIN) tableFactor joinSpecification?
    | NATURAL? (LEFT | RIGHT | FULL) OUTER? JOIN tableFactor joinSpecification?
    ;

joinSpecification
    : ON expr | USING columnNames
    ;

selectFromClause
    : FROM fromClauseList
    ;

fromClauseList
    : fromClauseOption (COMMA_ fromClauseOption)*
    ;

fromClauseOption
    : joinClause | parenthesisJoinClause | selectTableReference | inlineAnalyticView
    ;

selectTableReference
    : (queryTableExpressionClause | containersClause | shardsClause) alias?
    ;

queryTableExpressionClause
    : (ONLY LP_ queryTableExpression RP_ | queryTableExpression) flashbackQueryClause? (pivotClause | unpivotClause | rowPatternClause)?
    ;

flashbackQueryClause
    : (VERSIONS (BETWEEN (SCN | TIMESTAMP) | PERIOD FOR validTimeColumn BETWEEN) (expr | MINVALUE) AND (expr | MAXVALUE)) 
    | AS OF ((SCN | TIMESTAMP) expr | PERIOD FOR validTimeColumn expr)
    ;

queryTableExpression
    : queryTableExpressionSampleClause | lateralClause | tableCollectionExpression | queryName
    ;

queryTableExpressionSampleClause
    : (queryTableExpressionTableClause | queryTableExpressionViewClause | hierarchyName | queryTableExpressionAnalyticClause | (owner DOT_)? inlineExternalTable) sampleClause?
    ;

queryTableExpressionTableClause
    : tableName (mofifiedExternalTable | partitionExtensionClause | AT_ dbLink)?
    ;

queryTableExpressionViewClause
    : (viewName | materializedViewName) (AT_ dbLink)?
    ;

queryTableExpressionAnalyticClause
    : analyticViewName (HIERARCHIES LP_ (((attrDim DOT_)? hierarchyName) (COMMA_ ((attrDim DOT_)? hierarchyName))*)? RP_)?
    ;

lateralClause
    : LATERAL? LP_ selectSubquery subqueryRestrictionClause? RP_
    ;

inlineExternalTable
    : EXTERNAL LP_ LP_ columnDefinition (COMMA_ columnDefinition)* RP_ inlineExternalTableProperties RP_
    ;

inlineExternalTableProperties
    : (TYPE accessDriverType)? externalTableDataProps (REJECT LIMIT (INTEGER_ | UNLIMITED))?
    ;

mofifiedExternalTable
    : EXTERNAL MODIFY modifyExternalTableProperties
    ;

modifyExternalTableProperties
    : (DEFAULT DIRECTORY directoryName)? (LOCATION LP_ ((directoryName COLON_)? SQ_ locationSpecifier SQ_) (COMMA_ ((directoryName COLON_)? SQ_ locationSpecifier SQ_))* RP_)? 
    (ACCESS PARAMETERS (BADFILE fileName | LOGFILE fileName | DISCARDFILE fileName))? (REJECT LIMIT (INTEGER_ | UNLIMITED))?
    ;

pivotClause
    : PIVOT XML? LP_ (aggregationFunction LP_ expr RP_ (AS? alias)?) (COMMA_ (aggregationFunction LP_ expr RP_ (AS? alias)?))* pivotForClause pivotInClause RP_
    ;

pivotForClause
    : FOR (columnName | LP_ columnName (COMMA_ columnName)* RP_)
    ;

pivotInClause
    : IN LP_ ((((expr | LP_ expr (COMMA_ expr)* RP_) (AS? alias)?) (COMMA_ ((expr | LP_ expr (COMMA_ expr)* RP_) (AS? alias)?))*) | 
    selectSubquery | ANY (COMMA_ ANY)*) RP_
    ;

unpivotClause
    : UNPIVOT ((INCLUDE | EXCLUDE) NULLS)? LP_ (columnName | LP_ columnName (COMMA_ columnName)* RP_) pivotForClause unpivotInClause RP_
    ;

unpivotInClause
    : IN LP_ ((columnName | LP_ columnName (COMMA_ columnName)* RP_) (AS (literals | LP_ literals (COMMA_ literals)* RP_))?) 
    (COMMA_ ((columnName | LP_ columnName (COMMA_ columnName)* RP_) (AS (literals | LP_ literals (COMMA_ literals)* RP_))?))* RP_
    ;

sampleClause
    : SAMPLE BLOCK? LP_ samplePercent RP_ (SEED LP_ seedValue RP_)?
    ;

partitionExtensionClause
    : (PARTITION (LP_ partitionName RP_ | FOR LP_ partitionKeyValue (COMMA_ partitionKeyValue)* RP_)) 
    | (SUBPARTITION (LP_ subpartitionName RP_ | FOR LP_ subpartitionKeyValue (COMMA_ subpartitionKeyValue)* RP_))
    ;

subqueryRestrictionClause
    : WITH (READ ONLY | CHECK OPTION) (CONSTRAINT constraintName)?
    ;

tableCollectionExpression
    : TABLE LP_ collectionExpression RP_ (LP_ PLUS_ RP_)?
    ;

containersClause
    : CONTAINERS LP_ (tableName | viewName) RP_
    ;

shardsClause
    : SHARDS LP_ (tableName | viewName) RP_
    ;

joinClause
    : selectTableReference selectJoin+
    ;

selectJoin
    : selectJoinOption
    ;

selectJoinOption
    : innerCrossJoinClause | outerJoinClause | crossOuterApplyClause
    ;

innerCrossJoinClause
    : innerJoinClause | crossJoinClause
    ;

innerJoinClause
    : INNER? JOIN selectTableReference selectJoinSpecification
    ;

crossJoinClause
    : (CROSS | NATURAL INNER?) JOIN selectTableReference
    ;

outerJoinClause
    : queryPartitionClause? NATURAL? outerJoinType JOIN selectTableReference queryPartitionClause? selectJoinSpecification?
    ;

selectJoinSpecification
    : ON condition | USING LP_ columnName (COMMA_ columnName)* RP_
    ;

queryPartitionClause
    : PARTITION BY ((expr (COMMA_ expr)*) | LP_ expr (COMMA_ expr)* RP_)
    ;

outerJoinType
    : (FULL | LEFT | RIGHT) OUTER?
    ;

crossOuterApplyClause
    : (CROSS | OUTER) APPLY (selectTableReference | collectionExpression)
    ;

parenthesisJoinClause
    : LP_ joinClause RP_
    ;

inlineAnalyticView
    : ANALYTIC VIEW LP_ subavClause RP_ (AS? alias)?
    ;

collectionExpression
    : selectSubquery | columnName | functionCall | expr
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
    : LP_ selectSubquery RP_
    ;

forUpdateClause
    : FOR UPDATE (OF forUpdateClauseList)? ((NOWAIT | WAIT INTEGER_) | SKIP_SYMBOL LOCKED)?
    ;

forUpdateClauseList
    : forUpdateClauseOption (COMMA_ forUpdateClauseOption)*
    ;

forUpdateClauseOption
    : ((tableName | viewName) DOT_)? columnName
    ;

rowLimitingClause
    : (OFFSET offset (ROW | ROWS))? (FETCH (FIRST | NEXT) (rowcount | percent PERCENT)? (ROW | ROWS) (ONLY | WITH TIES))?
    ;

rowPatternClause
    : MATCH_RECOGNIZE LP_ rowPatternPartitionBy? rowPatternOrderBy? rowPatternMeasures? rowPatternRowsPerMatch? rowPatternSkipTo? 
    PATTERN LP_ rowPattern RP_ rowPatternSubsetClause? DEFINE rowPatternDefinitionList RP_
    ;

rowPatternPartitionBy
    : PARTITION BY columnName (COMMA_ columnName)*
    ;

rowPatternOrderBy
    : ORDER BY columnName (COMMA_ columnName)*
    ;

rowPatternMeasures
    : MEASURES rowPatternMeasureColumn (COMMA_ rowPatternMeasureColumn)*
    ;

rowPatternMeasureColumn
    : patternMeasExpression AS alias
    ;

rowPatternRowsPerMatch
    : (ONE ROW | ALL ROWS) PER MATCH
    ;

rowPatternSkipTo
    : AFTER MATCH SKIP_SYMBOL (((TO NEXT | PAST LAST) ROW) | (TO (FIRST | LAST)? variableName))
    ;

rowPattern
    : rowPatternTerm
    ;

rowPatternTerm
    : rowPatternFactor
    ;

rowPatternFactor
    : rowPatternPrimary rowPatternQuantifier?
    ;

rowPatternPrimary
    : variableName | DOLLAR_ | CARET_ | (LP_ rowPattern? RP_) | (LBE_ MINUS_ rowPattern MINUS_ RBE_) | rowPatternPermute
    ;

rowPatternPermute
    : PERMUTE LP_ rowPattern (COMMA_ rowPattern)* RP_
    ;

rowPatternQuantifier
    : (ASTERISK_ QUESTION_?) | (PLUS_ QUESTION_?) | (QUESTION_ QUESTION_?) | ((LBE_ INTEGER_? COMMA_ INTEGER_? RBE_ QUESTION_?) | (LBE_ INTEGER_ RBE_))
    ;

rowPatternSubsetClause
    : SUBSET rowPatternSubsetItem (COMMA_ rowPatternSubsetItem)*
    ;

rowPatternSubsetItem
    : variableName EQ_ LP_ variableName (COMMA_ variableName)* RP_
    ;

rowPatternDefinitionList
    : rowPatternDefinition (COMMA_ rowPatternDefinition)*
    ;

rowPatternDefinition
    : variableName AS condition
    ;

patternMeasExpression
    : stringLiterals | numberLiterals | columnName | rowPatternClassifierFunc | rowPatternMatchNumFunc | rowPatternNavigationFunc | rowPatternAggregateFunc
    ;

rowPatternClassifierFunc
    : CLASSIFIER LP_ RP_
    ;

rowPatternMatchNumFunc
    : MATCH_NUMBER LP_ RP_
    ;

rowPatternNavigationFunc
    : rowPatternNavLogical | rowPatternNavPhysical | rowPatternNavCompound
    ;

rowPatternNavLogical
    : (RUNNING | FINAL)? (FIRST | LAST) LP_ expr (COMMA_ offset)? RP_
    ;

rowPatternNavPhysical
    : (PREV | NEXT) LP_ expr (COMMA_ offset)? RP_
    ;

rowPatternNavCompound
    : (PREV | NEXT) LP_ (RUNNING | FINAL)? (FIRST | LAST) LP_ expr (COMMA_ offset)? RP_ (COMMA_ offset)? RP_
    ;

rowPatternAggregateFunc
    : (RUNNING | FINAL)? aggregationFunction
    ;

merge
    : MERGE hint? intoClause usingClause mergeUpdateClause? mergeInsertClause? errorLoggingClause?
    ;

hint
    : BLOCK_COMMENT | INLINE_COMMENT
    ;

intoClause
    : INTO (tableName | viewName) alias?
    ;

usingClause
    : USING ((tableName | viewName) | subquery) alias? ON LP_ expr RP_
    ;

mergeUpdateClause
    : WHEN MATCHED THEN UPDATE SET mergeSetAssignmentsClause whereClause? deleteWhereClause?
    ;

mergeSetAssignmentsClause
    : mergeAssignment (COMMA_ mergeAssignment)*
    ;

mergeAssignment
    : columnName EQ_ mergeAssignmentValue
    ;

mergeAssignmentValue
    : expr | DEFAULT
    ;

deleteWhereClause
    : DELETE whereClause
    ;

mergeInsertClause
    : WHEN NOT MATCHED THEN INSERT mergeInsertColumn? mergeColumnValue whereClause?
    ;

mergeInsertColumn
    : LP_ columnName (COMMA_ columnName)* RP_
    ;

mergeColumnValue
    : VALUES LP_ (expr | DEFAULT) (COMMA_ (expr | DEFAULT))* RP_
    ;

errorLoggingClause
    : LOG ERRORS (INTO tableName)? (LP_ simpleExpr RP_)? (REJECT LIMIT (numberLiterals | UNLIMITED))?
    ;
