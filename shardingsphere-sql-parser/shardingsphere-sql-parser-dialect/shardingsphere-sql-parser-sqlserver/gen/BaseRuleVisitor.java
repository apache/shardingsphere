// Generated from /home/totalo/code/shardingsphere/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-sqlserver/src/main/antlr4/imports/sqlserver/BaseRule.g4 by ANTLR 4.9.1
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link BaseRuleParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface BaseRuleVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#parameterMarker}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterMarker(BaseRuleParser.ParameterMarkerContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#literals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiterals(BaseRuleParser.LiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#stringLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringLiterals(BaseRuleParser.StringLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#numberLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberLiterals(BaseRuleParser.NumberLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#dateTimeLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeLiterals(BaseRuleParser.DateTimeLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#hexadecimalLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHexadecimalLiterals(BaseRuleParser.HexadecimalLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#bitValueLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitValueLiterals(BaseRuleParser.BitValueLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#booleanLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanLiterals(BaseRuleParser.BooleanLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#nullValueLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullValueLiterals(BaseRuleParser.NullValueLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(BaseRuleParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#unreservedWord}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnreservedWord(BaseRuleParser.UnreservedWordContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#schemaName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaName(BaseRuleParser.SchemaNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#tableName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableName(BaseRuleParser.TableNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#columnName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnName(BaseRuleParser.ColumnNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#owner}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOwner(BaseRuleParser.OwnerContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName(BaseRuleParser.NameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#columnNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNames(BaseRuleParser.ColumnNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#columnNamesWithSort}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNamesWithSort(BaseRuleParser.ColumnNamesWithSortContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#tableNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableNames(BaseRuleParser.TableNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#indexName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexName(BaseRuleParser.IndexNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#collationName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollationName(BaseRuleParser.CollationNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlias(BaseRuleParser.AliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#dataTypeLength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataTypeLength(BaseRuleParser.DataTypeLengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#primaryKey}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryKey(BaseRuleParser.PrimaryKeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(BaseRuleParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#logicalOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOperator(BaseRuleParser.LogicalOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#notOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotOperator(BaseRuleParser.NotOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#booleanPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanPrimary(BaseRuleParser.BooleanPrimaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOperator(BaseRuleParser.ComparisonOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate(BaseRuleParser.PredicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#bitExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitExpr(BaseRuleParser.BitExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#simpleExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleExpr(BaseRuleParser.SimpleExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCall(BaseRuleParser.FunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#aggregationFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregationFunction(BaseRuleParser.AggregationFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#aggregationFunctionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregationFunctionName(BaseRuleParser.AggregationFunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#distinct}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDistinct(BaseRuleParser.DistinctContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#specialFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecialFunction(BaseRuleParser.SpecialFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#castFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCastFunction(BaseRuleParser.CastFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#charFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharFunction(BaseRuleParser.CharFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#regularFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRegularFunction(BaseRuleParser.RegularFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#regularFunctionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRegularFunctionName(BaseRuleParser.RegularFunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#caseExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseExpression(BaseRuleParser.CaseExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#caseWhen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseWhen(BaseRuleParser.CaseWhenContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#caseElse}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseElse(BaseRuleParser.CaseElseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#privateExprOfDb}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivateExprOfDb(BaseRuleParser.PrivateExprOfDbContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#subquery}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubquery(BaseRuleParser.SubqueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#orderByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByClause(BaseRuleParser.OrderByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#orderByItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByItem(BaseRuleParser.OrderByItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataType(BaseRuleParser.DataTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#dataTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataTypeName(BaseRuleParser.DataTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#atTimeZoneExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtTimeZoneExpr(BaseRuleParser.AtTimeZoneExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#castExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCastExpr(BaseRuleParser.CastExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#convertExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConvertExpr(BaseRuleParser.ConvertExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#windowedFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowedFunction(BaseRuleParser.WindowedFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#overClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOverClause(BaseRuleParser.OverClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#partitionByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionByClause(BaseRuleParser.PartitionByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#rowRangeClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowRangeClause(BaseRuleParser.RowRangeClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#windowFrameExtent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowFrameExtent(BaseRuleParser.WindowFrameExtentContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#windowFrameBetween}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowFrameBetween(BaseRuleParser.WindowFrameBetweenContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#windowFrameBound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowFrameBound(BaseRuleParser.WindowFrameBoundContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#windowFramePreceding}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowFramePreceding(BaseRuleParser.WindowFramePrecedingContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#windowFrameFollowing}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowFrameFollowing(BaseRuleParser.WindowFrameFollowingContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#columnNameWithSort}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNameWithSort(BaseRuleParser.ColumnNameWithSortContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#indexOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexOption(BaseRuleParser.IndexOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#compressionOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompressionOption(BaseRuleParser.CompressionOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#eqTime}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqTime(BaseRuleParser.EqTimeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#eqOnOffOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqOnOffOption(BaseRuleParser.EqOnOffOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#eqKey}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqKey(BaseRuleParser.EqKeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#eqOnOff}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqOnOff(BaseRuleParser.EqOnOffContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#onPartitionClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOnPartitionClause(BaseRuleParser.OnPartitionClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#partitionExpressions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionExpressions(BaseRuleParser.PartitionExpressionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#partitionExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionExpression(BaseRuleParser.PartitionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#numberRange}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberRange(BaseRuleParser.NumberRangeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#lowPriorityLockWait}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLowPriorityLockWait(BaseRuleParser.LowPriorityLockWaitContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#onLowPriorLockWait}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOnLowPriorLockWait(BaseRuleParser.OnLowPriorLockWaitContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#ignoredIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIgnoredIdentifier(BaseRuleParser.IgnoredIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#ignoredIdentifiers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIgnoredIdentifiers(BaseRuleParser.IgnoredIdentifiersContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#matchNone}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatchNone(BaseRuleParser.MatchNoneContext ctx);
}