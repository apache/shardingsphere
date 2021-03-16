// Generated from /home/guimy/github/shardingsphere/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-sql92/src/main/antlr4/imports/sql92/DMLStatement.g4 by ANTLR 4.9.1
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link DMLStatementParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface DMLStatementVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#insert}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert(DMLStatementParser.InsertContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#insertValuesClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertValuesClause(DMLStatementParser.InsertValuesClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#insertSelectClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertSelectClause(DMLStatementParser.InsertSelectClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#update}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdate(DMLStatementParser.UpdateContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(DMLStatementParser.AssignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#setAssignmentsClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetAssignmentsClause(DMLStatementParser.SetAssignmentsClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#assignmentValues}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentValues(DMLStatementParser.AssignmentValuesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#assignmentValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentValue(DMLStatementParser.AssignmentValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#blobValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlobValue(DMLStatementParser.BlobValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#delete}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDelete(DMLStatementParser.DeleteContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#singleTableClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleTableClause(DMLStatementParser.SingleTableClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#select}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect(DMLStatementParser.SelectContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#unionClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnionClause(DMLStatementParser.UnionClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#selectClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectClause(DMLStatementParser.SelectClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#selectSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectSpecification(DMLStatementParser.SelectSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#duplicateSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDuplicateSpecification(DMLStatementParser.DuplicateSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#projections}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProjections(DMLStatementParser.ProjectionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#projection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProjection(DMLStatementParser.ProjectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlias(DMLStatementParser.AliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#unqualifiedShorthand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnqualifiedShorthand(DMLStatementParser.UnqualifiedShorthandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#qualifiedShorthand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedShorthand(DMLStatementParser.QualifiedShorthandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#fromClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromClause(DMLStatementParser.FromClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#tableReferences}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableReferences(DMLStatementParser.TableReferencesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#escapedTableReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEscapedTableReference(DMLStatementParser.EscapedTableReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#tableReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableReference(DMLStatementParser.TableReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#tableFactor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableFactor(DMLStatementParser.TableFactorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#joinedTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinedTable(DMLStatementParser.JoinedTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#joinSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinSpecification(DMLStatementParser.JoinSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#whereClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhereClause(DMLStatementParser.WhereClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#groupByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupByClause(DMLStatementParser.GroupByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#havingClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingClause(DMLStatementParser.HavingClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#limitClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimitClause(DMLStatementParser.LimitClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#limitRowCount}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimitRowCount(DMLStatementParser.LimitRowCountContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#limitOffset}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimitOffset(DMLStatementParser.LimitOffsetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#subquery}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubquery(DMLStatementParser.SubqueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#parameterMarker}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterMarker(DMLStatementParser.ParameterMarkerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#literals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiterals(DMLStatementParser.LiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#stringLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringLiterals(DMLStatementParser.StringLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#numberLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberLiterals(DMLStatementParser.NumberLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#dateTimeLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeLiterals(DMLStatementParser.DateTimeLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#hexadecimalLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHexadecimalLiterals(DMLStatementParser.HexadecimalLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#bitValueLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitValueLiterals(DMLStatementParser.BitValueLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#booleanLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanLiterals(DMLStatementParser.BooleanLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#nullValueLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullValueLiterals(DMLStatementParser.NullValueLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(DMLStatementParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#unreservedWord}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnreservedWord(DMLStatementParser.UnreservedWordContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable(DMLStatementParser.VariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#schemaName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaName(DMLStatementParser.SchemaNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#tableName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableName(DMLStatementParser.TableNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#columnName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnName(DMLStatementParser.ColumnNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#viewName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitViewName(DMLStatementParser.ViewNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#owner}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOwner(DMLStatementParser.OwnerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName(DMLStatementParser.NameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#columnNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNames(DMLStatementParser.ColumnNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#tableNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableNames(DMLStatementParser.TableNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#characterSetName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterSetName(DMLStatementParser.CharacterSetNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(DMLStatementParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#logicalOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOperator(DMLStatementParser.LogicalOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#notOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotOperator(DMLStatementParser.NotOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#booleanPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanPrimary(DMLStatementParser.BooleanPrimaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOperator(DMLStatementParser.ComparisonOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate(DMLStatementParser.PredicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#bitExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitExpr(DMLStatementParser.BitExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#simpleExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleExpr(DMLStatementParser.SimpleExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCall(DMLStatementParser.FunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#aggregationFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregationFunction(DMLStatementParser.AggregationFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#aggregationFunctionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregationFunctionName(DMLStatementParser.AggregationFunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#distinct}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDistinct(DMLStatementParser.DistinctContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#specialFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecialFunction(DMLStatementParser.SpecialFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#castFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCastFunction(DMLStatementParser.CastFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#convertFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConvertFunction(DMLStatementParser.ConvertFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#positionFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPositionFunction(DMLStatementParser.PositionFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#substringFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubstringFunction(DMLStatementParser.SubstringFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#extractFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtractFunction(DMLStatementParser.ExtractFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#trimFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrimFunction(DMLStatementParser.TrimFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#regularFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRegularFunction(DMLStatementParser.RegularFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#regularFunctionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRegularFunctionName(DMLStatementParser.RegularFunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#matchExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatchExpression(DMLStatementParser.MatchExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#caseExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseExpression(DMLStatementParser.CaseExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#caseWhen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseWhen(DMLStatementParser.CaseWhenContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#caseElse}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseElse(DMLStatementParser.CaseElseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#intervalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalExpression(DMLStatementParser.IntervalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#intervalUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalUnit(DMLStatementParser.IntervalUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#orderByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByClause(DMLStatementParser.OrderByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#orderByItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByItem(DMLStatementParser.OrderByItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataType(DMLStatementParser.DataTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#dataTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataTypeName(DMLStatementParser.DataTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#dataTypeLength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataTypeLength(DMLStatementParser.DataTypeLengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#characterSet}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterSet(DMLStatementParser.CharacterSetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#collateClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollateClause(DMLStatementParser.CollateClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#ignoredIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIgnoredIdentifier(DMLStatementParser.IgnoredIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#dropBehaviour}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropBehaviour(DMLStatementParser.DropBehaviourContext ctx);
}