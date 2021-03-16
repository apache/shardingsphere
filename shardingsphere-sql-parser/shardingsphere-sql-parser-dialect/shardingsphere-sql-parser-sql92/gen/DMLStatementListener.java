// Generated from /home/guimy/github/shardingsphere/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-sql92/src/main/antlr4/imports/sql92/DMLStatement.g4 by ANTLR 4.9.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DMLStatementParser}.
 */
public interface DMLStatementListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#insert}.
	 * @param ctx the parse tree
	 */
	void enterInsert(DMLStatementParser.InsertContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#insert}.
	 * @param ctx the parse tree
	 */
	void exitInsert(DMLStatementParser.InsertContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#insertValuesClause}.
	 * @param ctx the parse tree
	 */
	void enterInsertValuesClause(DMLStatementParser.InsertValuesClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#insertValuesClause}.
	 * @param ctx the parse tree
	 */
	void exitInsertValuesClause(DMLStatementParser.InsertValuesClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#insertSelectClause}.
	 * @param ctx the parse tree
	 */
	void enterInsertSelectClause(DMLStatementParser.InsertSelectClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#insertSelectClause}.
	 * @param ctx the parse tree
	 */
	void exitInsertSelectClause(DMLStatementParser.InsertSelectClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#update}.
	 * @param ctx the parse tree
	 */
	void enterUpdate(DMLStatementParser.UpdateContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#update}.
	 * @param ctx the parse tree
	 */
	void exitUpdate(DMLStatementParser.UpdateContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(DMLStatementParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(DMLStatementParser.AssignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#setAssignmentsClause}.
	 * @param ctx the parse tree
	 */
	void enterSetAssignmentsClause(DMLStatementParser.SetAssignmentsClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#setAssignmentsClause}.
	 * @param ctx the parse tree
	 */
	void exitSetAssignmentsClause(DMLStatementParser.SetAssignmentsClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#assignmentValues}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentValues(DMLStatementParser.AssignmentValuesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#assignmentValues}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentValues(DMLStatementParser.AssignmentValuesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentValue(DMLStatementParser.AssignmentValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentValue(DMLStatementParser.AssignmentValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#blobValue}.
	 * @param ctx the parse tree
	 */
	void enterBlobValue(DMLStatementParser.BlobValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#blobValue}.
	 * @param ctx the parse tree
	 */
	void exitBlobValue(DMLStatementParser.BlobValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#delete}.
	 * @param ctx the parse tree
	 */
	void enterDelete(DMLStatementParser.DeleteContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#delete}.
	 * @param ctx the parse tree
	 */
	void exitDelete(DMLStatementParser.DeleteContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#singleTableClause}.
	 * @param ctx the parse tree
	 */
	void enterSingleTableClause(DMLStatementParser.SingleTableClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#singleTableClause}.
	 * @param ctx the parse tree
	 */
	void exitSingleTableClause(DMLStatementParser.SingleTableClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#select}.
	 * @param ctx the parse tree
	 */
	void enterSelect(DMLStatementParser.SelectContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#select}.
	 * @param ctx the parse tree
	 */
	void exitSelect(DMLStatementParser.SelectContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#unionClause}.
	 * @param ctx the parse tree
	 */
	void enterUnionClause(DMLStatementParser.UnionClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#unionClause}.
	 * @param ctx the parse tree
	 */
	void exitUnionClause(DMLStatementParser.UnionClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#selectClause}.
	 * @param ctx the parse tree
	 */
	void enterSelectClause(DMLStatementParser.SelectClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#selectClause}.
	 * @param ctx the parse tree
	 */
	void exitSelectClause(DMLStatementParser.SelectClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#selectSpecification}.
	 * @param ctx the parse tree
	 */
	void enterSelectSpecification(DMLStatementParser.SelectSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#selectSpecification}.
	 * @param ctx the parse tree
	 */
	void exitSelectSpecification(DMLStatementParser.SelectSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#duplicateSpecification}.
	 * @param ctx the parse tree
	 */
	void enterDuplicateSpecification(DMLStatementParser.DuplicateSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#duplicateSpecification}.
	 * @param ctx the parse tree
	 */
	void exitDuplicateSpecification(DMLStatementParser.DuplicateSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#projections}.
	 * @param ctx the parse tree
	 */
	void enterProjections(DMLStatementParser.ProjectionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#projections}.
	 * @param ctx the parse tree
	 */
	void exitProjections(DMLStatementParser.ProjectionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#projection}.
	 * @param ctx the parse tree
	 */
	void enterProjection(DMLStatementParser.ProjectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#projection}.
	 * @param ctx the parse tree
	 */
	void exitProjection(DMLStatementParser.ProjectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#alias}.
	 * @param ctx the parse tree
	 */
	void enterAlias(DMLStatementParser.AliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#alias}.
	 * @param ctx the parse tree
	 */
	void exitAlias(DMLStatementParser.AliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#unqualifiedShorthand}.
	 * @param ctx the parse tree
	 */
	void enterUnqualifiedShorthand(DMLStatementParser.UnqualifiedShorthandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#unqualifiedShorthand}.
	 * @param ctx the parse tree
	 */
	void exitUnqualifiedShorthand(DMLStatementParser.UnqualifiedShorthandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#qualifiedShorthand}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedShorthand(DMLStatementParser.QualifiedShorthandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#qualifiedShorthand}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedShorthand(DMLStatementParser.QualifiedShorthandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void enterFromClause(DMLStatementParser.FromClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void exitFromClause(DMLStatementParser.FromClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#tableReferences}.
	 * @param ctx the parse tree
	 */
	void enterTableReferences(DMLStatementParser.TableReferencesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#tableReferences}.
	 * @param ctx the parse tree
	 */
	void exitTableReferences(DMLStatementParser.TableReferencesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#escapedTableReference}.
	 * @param ctx the parse tree
	 */
	void enterEscapedTableReference(DMLStatementParser.EscapedTableReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#escapedTableReference}.
	 * @param ctx the parse tree
	 */
	void exitEscapedTableReference(DMLStatementParser.EscapedTableReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#tableReference}.
	 * @param ctx the parse tree
	 */
	void enterTableReference(DMLStatementParser.TableReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#tableReference}.
	 * @param ctx the parse tree
	 */
	void exitTableReference(DMLStatementParser.TableReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#tableFactor}.
	 * @param ctx the parse tree
	 */
	void enterTableFactor(DMLStatementParser.TableFactorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#tableFactor}.
	 * @param ctx the parse tree
	 */
	void exitTableFactor(DMLStatementParser.TableFactorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#joinedTable}.
	 * @param ctx the parse tree
	 */
	void enterJoinedTable(DMLStatementParser.JoinedTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#joinedTable}.
	 * @param ctx the parse tree
	 */
	void exitJoinedTable(DMLStatementParser.JoinedTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#joinSpecification}.
	 * @param ctx the parse tree
	 */
	void enterJoinSpecification(DMLStatementParser.JoinSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#joinSpecification}.
	 * @param ctx the parse tree
	 */
	void exitJoinSpecification(DMLStatementParser.JoinSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void enterWhereClause(DMLStatementParser.WhereClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void exitWhereClause(DMLStatementParser.WhereClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#groupByClause}.
	 * @param ctx the parse tree
	 */
	void enterGroupByClause(DMLStatementParser.GroupByClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#groupByClause}.
	 * @param ctx the parse tree
	 */
	void exitGroupByClause(DMLStatementParser.GroupByClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#havingClause}.
	 * @param ctx the parse tree
	 */
	void enterHavingClause(DMLStatementParser.HavingClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#havingClause}.
	 * @param ctx the parse tree
	 */
	void exitHavingClause(DMLStatementParser.HavingClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#limitClause}.
	 * @param ctx the parse tree
	 */
	void enterLimitClause(DMLStatementParser.LimitClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#limitClause}.
	 * @param ctx the parse tree
	 */
	void exitLimitClause(DMLStatementParser.LimitClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#limitRowCount}.
	 * @param ctx the parse tree
	 */
	void enterLimitRowCount(DMLStatementParser.LimitRowCountContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#limitRowCount}.
	 * @param ctx the parse tree
	 */
	void exitLimitRowCount(DMLStatementParser.LimitRowCountContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#limitOffset}.
	 * @param ctx the parse tree
	 */
	void enterLimitOffset(DMLStatementParser.LimitOffsetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#limitOffset}.
	 * @param ctx the parse tree
	 */
	void exitLimitOffset(DMLStatementParser.LimitOffsetContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#subquery}.
	 * @param ctx the parse tree
	 */
	void enterSubquery(DMLStatementParser.SubqueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#subquery}.
	 * @param ctx the parse tree
	 */
	void exitSubquery(DMLStatementParser.SubqueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#parameterMarker}.
	 * @param ctx the parse tree
	 */
	void enterParameterMarker(DMLStatementParser.ParameterMarkerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#parameterMarker}.
	 * @param ctx the parse tree
	 */
	void exitParameterMarker(DMLStatementParser.ParameterMarkerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#literals}.
	 * @param ctx the parse tree
	 */
	void enterLiterals(DMLStatementParser.LiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#literals}.
	 * @param ctx the parse tree
	 */
	void exitLiterals(DMLStatementParser.LiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#stringLiterals}.
	 * @param ctx the parse tree
	 */
	void enterStringLiterals(DMLStatementParser.StringLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#stringLiterals}.
	 * @param ctx the parse tree
	 */
	void exitStringLiterals(DMLStatementParser.StringLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#numberLiterals}.
	 * @param ctx the parse tree
	 */
	void enterNumberLiterals(DMLStatementParser.NumberLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#numberLiterals}.
	 * @param ctx the parse tree
	 */
	void exitNumberLiterals(DMLStatementParser.NumberLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#dateTimeLiterals}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeLiterals(DMLStatementParser.DateTimeLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#dateTimeLiterals}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeLiterals(DMLStatementParser.DateTimeLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#hexadecimalLiterals}.
	 * @param ctx the parse tree
	 */
	void enterHexadecimalLiterals(DMLStatementParser.HexadecimalLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#hexadecimalLiterals}.
	 * @param ctx the parse tree
	 */
	void exitHexadecimalLiterals(DMLStatementParser.HexadecimalLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#bitValueLiterals}.
	 * @param ctx the parse tree
	 */
	void enterBitValueLiterals(DMLStatementParser.BitValueLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#bitValueLiterals}.
	 * @param ctx the parse tree
	 */
	void exitBitValueLiterals(DMLStatementParser.BitValueLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#booleanLiterals}.
	 * @param ctx the parse tree
	 */
	void enterBooleanLiterals(DMLStatementParser.BooleanLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#booleanLiterals}.
	 * @param ctx the parse tree
	 */
	void exitBooleanLiterals(DMLStatementParser.BooleanLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#nullValueLiterals}.
	 * @param ctx the parse tree
	 */
	void enterNullValueLiterals(DMLStatementParser.NullValueLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#nullValueLiterals}.
	 * @param ctx the parse tree
	 */
	void exitNullValueLiterals(DMLStatementParser.NullValueLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(DMLStatementParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(DMLStatementParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#unreservedWord}.
	 * @param ctx the parse tree
	 */
	void enterUnreservedWord(DMLStatementParser.UnreservedWordContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#unreservedWord}.
	 * @param ctx the parse tree
	 */
	void exitUnreservedWord(DMLStatementParser.UnreservedWordContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#variable}.
	 * @param ctx the parse tree
	 */
	void enterVariable(DMLStatementParser.VariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#variable}.
	 * @param ctx the parse tree
	 */
	void exitVariable(DMLStatementParser.VariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#schemaName}.
	 * @param ctx the parse tree
	 */
	void enterSchemaName(DMLStatementParser.SchemaNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#schemaName}.
	 * @param ctx the parse tree
	 */
	void exitSchemaName(DMLStatementParser.SchemaNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#tableName}.
	 * @param ctx the parse tree
	 */
	void enterTableName(DMLStatementParser.TableNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#tableName}.
	 * @param ctx the parse tree
	 */
	void exitTableName(DMLStatementParser.TableNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#columnName}.
	 * @param ctx the parse tree
	 */
	void enterColumnName(DMLStatementParser.ColumnNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#columnName}.
	 * @param ctx the parse tree
	 */
	void exitColumnName(DMLStatementParser.ColumnNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#viewName}.
	 * @param ctx the parse tree
	 */
	void enterViewName(DMLStatementParser.ViewNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#viewName}.
	 * @param ctx the parse tree
	 */
	void exitViewName(DMLStatementParser.ViewNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#owner}.
	 * @param ctx the parse tree
	 */
	void enterOwner(DMLStatementParser.OwnerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#owner}.
	 * @param ctx the parse tree
	 */
	void exitOwner(DMLStatementParser.OwnerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#name}.
	 * @param ctx the parse tree
	 */
	void enterName(DMLStatementParser.NameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#name}.
	 * @param ctx the parse tree
	 */
	void exitName(DMLStatementParser.NameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#columnNames}.
	 * @param ctx the parse tree
	 */
	void enterColumnNames(DMLStatementParser.ColumnNamesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#columnNames}.
	 * @param ctx the parse tree
	 */
	void exitColumnNames(DMLStatementParser.ColumnNamesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#tableNames}.
	 * @param ctx the parse tree
	 */
	void enterTableNames(DMLStatementParser.TableNamesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#tableNames}.
	 * @param ctx the parse tree
	 */
	void exitTableNames(DMLStatementParser.TableNamesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#characterSetName}.
	 * @param ctx the parse tree
	 */
	void enterCharacterSetName(DMLStatementParser.CharacterSetNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#characterSetName}.
	 * @param ctx the parse tree
	 */
	void exitCharacterSetName(DMLStatementParser.CharacterSetNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(DMLStatementParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(DMLStatementParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#logicalOperator}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOperator(DMLStatementParser.LogicalOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#logicalOperator}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOperator(DMLStatementParser.LogicalOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#notOperator}.
	 * @param ctx the parse tree
	 */
	void enterNotOperator(DMLStatementParser.NotOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#notOperator}.
	 * @param ctx the parse tree
	 */
	void exitNotOperator(DMLStatementParser.NotOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#booleanPrimary}.
	 * @param ctx the parse tree
	 */
	void enterBooleanPrimary(DMLStatementParser.BooleanPrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#booleanPrimary}.
	 * @param ctx the parse tree
	 */
	void exitBooleanPrimary(DMLStatementParser.BooleanPrimaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOperator(DMLStatementParser.ComparisonOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOperator(DMLStatementParser.ComparisonOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterPredicate(DMLStatementParser.PredicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitPredicate(DMLStatementParser.PredicateContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#bitExpr}.
	 * @param ctx the parse tree
	 */
	void enterBitExpr(DMLStatementParser.BitExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#bitExpr}.
	 * @param ctx the parse tree
	 */
	void exitBitExpr(DMLStatementParser.BitExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#simpleExpr}.
	 * @param ctx the parse tree
	 */
	void enterSimpleExpr(DMLStatementParser.SimpleExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#simpleExpr}.
	 * @param ctx the parse tree
	 */
	void exitSimpleExpr(DMLStatementParser.SimpleExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCall(DMLStatementParser.FunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCall(DMLStatementParser.FunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#aggregationFunction}.
	 * @param ctx the parse tree
	 */
	void enterAggregationFunction(DMLStatementParser.AggregationFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#aggregationFunction}.
	 * @param ctx the parse tree
	 */
	void exitAggregationFunction(DMLStatementParser.AggregationFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#aggregationFunctionName}.
	 * @param ctx the parse tree
	 */
	void enterAggregationFunctionName(DMLStatementParser.AggregationFunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#aggregationFunctionName}.
	 * @param ctx the parse tree
	 */
	void exitAggregationFunctionName(DMLStatementParser.AggregationFunctionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#distinct}.
	 * @param ctx the parse tree
	 */
	void enterDistinct(DMLStatementParser.DistinctContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#distinct}.
	 * @param ctx the parse tree
	 */
	void exitDistinct(DMLStatementParser.DistinctContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#specialFunction}.
	 * @param ctx the parse tree
	 */
	void enterSpecialFunction(DMLStatementParser.SpecialFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#specialFunction}.
	 * @param ctx the parse tree
	 */
	void exitSpecialFunction(DMLStatementParser.SpecialFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#castFunction}.
	 * @param ctx the parse tree
	 */
	void enterCastFunction(DMLStatementParser.CastFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#castFunction}.
	 * @param ctx the parse tree
	 */
	void exitCastFunction(DMLStatementParser.CastFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#convertFunction}.
	 * @param ctx the parse tree
	 */
	void enterConvertFunction(DMLStatementParser.ConvertFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#convertFunction}.
	 * @param ctx the parse tree
	 */
	void exitConvertFunction(DMLStatementParser.ConvertFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#positionFunction}.
	 * @param ctx the parse tree
	 */
	void enterPositionFunction(DMLStatementParser.PositionFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#positionFunction}.
	 * @param ctx the parse tree
	 */
	void exitPositionFunction(DMLStatementParser.PositionFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#substringFunction}.
	 * @param ctx the parse tree
	 */
	void enterSubstringFunction(DMLStatementParser.SubstringFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#substringFunction}.
	 * @param ctx the parse tree
	 */
	void exitSubstringFunction(DMLStatementParser.SubstringFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#extractFunction}.
	 * @param ctx the parse tree
	 */
	void enterExtractFunction(DMLStatementParser.ExtractFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#extractFunction}.
	 * @param ctx the parse tree
	 */
	void exitExtractFunction(DMLStatementParser.ExtractFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#trimFunction}.
	 * @param ctx the parse tree
	 */
	void enterTrimFunction(DMLStatementParser.TrimFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#trimFunction}.
	 * @param ctx the parse tree
	 */
	void exitTrimFunction(DMLStatementParser.TrimFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#regularFunction}.
	 * @param ctx the parse tree
	 */
	void enterRegularFunction(DMLStatementParser.RegularFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#regularFunction}.
	 * @param ctx the parse tree
	 */
	void exitRegularFunction(DMLStatementParser.RegularFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#regularFunctionName}.
	 * @param ctx the parse tree
	 */
	void enterRegularFunctionName(DMLStatementParser.RegularFunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#regularFunctionName}.
	 * @param ctx the parse tree
	 */
	void exitRegularFunctionName(DMLStatementParser.RegularFunctionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#matchExpression}.
	 * @param ctx the parse tree
	 */
	void enterMatchExpression(DMLStatementParser.MatchExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#matchExpression}.
	 * @param ctx the parse tree
	 */
	void exitMatchExpression(DMLStatementParser.MatchExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#caseExpression}.
	 * @param ctx the parse tree
	 */
	void enterCaseExpression(DMLStatementParser.CaseExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#caseExpression}.
	 * @param ctx the parse tree
	 */
	void exitCaseExpression(DMLStatementParser.CaseExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#caseWhen}.
	 * @param ctx the parse tree
	 */
	void enterCaseWhen(DMLStatementParser.CaseWhenContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#caseWhen}.
	 * @param ctx the parse tree
	 */
	void exitCaseWhen(DMLStatementParser.CaseWhenContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#caseElse}.
	 * @param ctx the parse tree
	 */
	void enterCaseElse(DMLStatementParser.CaseElseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#caseElse}.
	 * @param ctx the parse tree
	 */
	void exitCaseElse(DMLStatementParser.CaseElseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#intervalExpression}.
	 * @param ctx the parse tree
	 */
	void enterIntervalExpression(DMLStatementParser.IntervalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#intervalExpression}.
	 * @param ctx the parse tree
	 */
	void exitIntervalExpression(DMLStatementParser.IntervalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#intervalUnit}.
	 * @param ctx the parse tree
	 */
	void enterIntervalUnit(DMLStatementParser.IntervalUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#intervalUnit}.
	 * @param ctx the parse tree
	 */
	void exitIntervalUnit(DMLStatementParser.IntervalUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#orderByClause}.
	 * @param ctx the parse tree
	 */
	void enterOrderByClause(DMLStatementParser.OrderByClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#orderByClause}.
	 * @param ctx the parse tree
	 */
	void exitOrderByClause(DMLStatementParser.OrderByClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#orderByItem}.
	 * @param ctx the parse tree
	 */
	void enterOrderByItem(DMLStatementParser.OrderByItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#orderByItem}.
	 * @param ctx the parse tree
	 */
	void exitOrderByItem(DMLStatementParser.OrderByItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterDataType(DMLStatementParser.DataTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitDataType(DMLStatementParser.DataTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#dataTypeName}.
	 * @param ctx the parse tree
	 */
	void enterDataTypeName(DMLStatementParser.DataTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#dataTypeName}.
	 * @param ctx the parse tree
	 */
	void exitDataTypeName(DMLStatementParser.DataTypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#dataTypeLength}.
	 * @param ctx the parse tree
	 */
	void enterDataTypeLength(DMLStatementParser.DataTypeLengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#dataTypeLength}.
	 * @param ctx the parse tree
	 */
	void exitDataTypeLength(DMLStatementParser.DataTypeLengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#characterSet}.
	 * @param ctx the parse tree
	 */
	void enterCharacterSet(DMLStatementParser.CharacterSetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#characterSet}.
	 * @param ctx the parse tree
	 */
	void exitCharacterSet(DMLStatementParser.CharacterSetContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#collateClause}.
	 * @param ctx the parse tree
	 */
	void enterCollateClause(DMLStatementParser.CollateClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#collateClause}.
	 * @param ctx the parse tree
	 */
	void exitCollateClause(DMLStatementParser.CollateClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#ignoredIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterIgnoredIdentifier(DMLStatementParser.IgnoredIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#ignoredIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitIgnoredIdentifier(DMLStatementParser.IgnoredIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#dropBehaviour}.
	 * @param ctx the parse tree
	 */
	void enterDropBehaviour(DMLStatementParser.DropBehaviourContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#dropBehaviour}.
	 * @param ctx the parse tree
	 */
	void exitDropBehaviour(DMLStatementParser.DropBehaviourContext ctx);
}