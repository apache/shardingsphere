// Generated from /home/totalo/code/shardingsphere/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-sqlserver/src/main/antlr4/imports/sqlserver/BaseRule.g4 by ANTLR 4.9.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link BaseRuleParser}.
 */
public interface BaseRuleListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#parameterMarker}.
	 * @param ctx the parse tree
	 */
	void enterParameterMarker(BaseRuleParser.ParameterMarkerContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#parameterMarker}.
	 * @param ctx the parse tree
	 */
	void exitParameterMarker(BaseRuleParser.ParameterMarkerContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#literals}.
	 * @param ctx the parse tree
	 */
	void enterLiterals(BaseRuleParser.LiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#literals}.
	 * @param ctx the parse tree
	 */
	void exitLiterals(BaseRuleParser.LiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#stringLiterals}.
	 * @param ctx the parse tree
	 */
	void enterStringLiterals(BaseRuleParser.StringLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#stringLiterals}.
	 * @param ctx the parse tree
	 */
	void exitStringLiterals(BaseRuleParser.StringLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#numberLiterals}.
	 * @param ctx the parse tree
	 */
	void enterNumberLiterals(BaseRuleParser.NumberLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#numberLiterals}.
	 * @param ctx the parse tree
	 */
	void exitNumberLiterals(BaseRuleParser.NumberLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#dateTimeLiterals}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeLiterals(BaseRuleParser.DateTimeLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#dateTimeLiterals}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeLiterals(BaseRuleParser.DateTimeLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#hexadecimalLiterals}.
	 * @param ctx the parse tree
	 */
	void enterHexadecimalLiterals(BaseRuleParser.HexadecimalLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#hexadecimalLiterals}.
	 * @param ctx the parse tree
	 */
	void exitHexadecimalLiterals(BaseRuleParser.HexadecimalLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#bitValueLiterals}.
	 * @param ctx the parse tree
	 */
	void enterBitValueLiterals(BaseRuleParser.BitValueLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#bitValueLiterals}.
	 * @param ctx the parse tree
	 */
	void exitBitValueLiterals(BaseRuleParser.BitValueLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#booleanLiterals}.
	 * @param ctx the parse tree
	 */
	void enterBooleanLiterals(BaseRuleParser.BooleanLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#booleanLiterals}.
	 * @param ctx the parse tree
	 */
	void exitBooleanLiterals(BaseRuleParser.BooleanLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#nullValueLiterals}.
	 * @param ctx the parse tree
	 */
	void enterNullValueLiterals(BaseRuleParser.NullValueLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#nullValueLiterals}.
	 * @param ctx the parse tree
	 */
	void exitNullValueLiterals(BaseRuleParser.NullValueLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(BaseRuleParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(BaseRuleParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#unreservedWord}.
	 * @param ctx the parse tree
	 */
	void enterUnreservedWord(BaseRuleParser.UnreservedWordContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#unreservedWord}.
	 * @param ctx the parse tree
	 */
	void exitUnreservedWord(BaseRuleParser.UnreservedWordContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#schemaName}.
	 * @param ctx the parse tree
	 */
	void enterSchemaName(BaseRuleParser.SchemaNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#schemaName}.
	 * @param ctx the parse tree
	 */
	void exitSchemaName(BaseRuleParser.SchemaNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#tableName}.
	 * @param ctx the parse tree
	 */
	void enterTableName(BaseRuleParser.TableNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#tableName}.
	 * @param ctx the parse tree
	 */
	void exitTableName(BaseRuleParser.TableNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#columnName}.
	 * @param ctx the parse tree
	 */
	void enterColumnName(BaseRuleParser.ColumnNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#columnName}.
	 * @param ctx the parse tree
	 */
	void exitColumnName(BaseRuleParser.ColumnNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#owner}.
	 * @param ctx the parse tree
	 */
	void enterOwner(BaseRuleParser.OwnerContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#owner}.
	 * @param ctx the parse tree
	 */
	void exitOwner(BaseRuleParser.OwnerContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#name}.
	 * @param ctx the parse tree
	 */
	void enterName(BaseRuleParser.NameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#name}.
	 * @param ctx the parse tree
	 */
	void exitName(BaseRuleParser.NameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#columnNames}.
	 * @param ctx the parse tree
	 */
	void enterColumnNames(BaseRuleParser.ColumnNamesContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#columnNames}.
	 * @param ctx the parse tree
	 */
	void exitColumnNames(BaseRuleParser.ColumnNamesContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#columnNamesWithSort}.
	 * @param ctx the parse tree
	 */
	void enterColumnNamesWithSort(BaseRuleParser.ColumnNamesWithSortContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#columnNamesWithSort}.
	 * @param ctx the parse tree
	 */
	void exitColumnNamesWithSort(BaseRuleParser.ColumnNamesWithSortContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#tableNames}.
	 * @param ctx the parse tree
	 */
	void enterTableNames(BaseRuleParser.TableNamesContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#tableNames}.
	 * @param ctx the parse tree
	 */
	void exitTableNames(BaseRuleParser.TableNamesContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#indexName}.
	 * @param ctx the parse tree
	 */
	void enterIndexName(BaseRuleParser.IndexNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#indexName}.
	 * @param ctx the parse tree
	 */
	void exitIndexName(BaseRuleParser.IndexNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#collationName}.
	 * @param ctx the parse tree
	 */
	void enterCollationName(BaseRuleParser.CollationNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#collationName}.
	 * @param ctx the parse tree
	 */
	void exitCollationName(BaseRuleParser.CollationNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#alias}.
	 * @param ctx the parse tree
	 */
	void enterAlias(BaseRuleParser.AliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#alias}.
	 * @param ctx the parse tree
	 */
	void exitAlias(BaseRuleParser.AliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#dataTypeLength}.
	 * @param ctx the parse tree
	 */
	void enterDataTypeLength(BaseRuleParser.DataTypeLengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#dataTypeLength}.
	 * @param ctx the parse tree
	 */
	void exitDataTypeLength(BaseRuleParser.DataTypeLengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#primaryKey}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryKey(BaseRuleParser.PrimaryKeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#primaryKey}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryKey(BaseRuleParser.PrimaryKeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(BaseRuleParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(BaseRuleParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#logicalOperator}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOperator(BaseRuleParser.LogicalOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#logicalOperator}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOperator(BaseRuleParser.LogicalOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#notOperator}.
	 * @param ctx the parse tree
	 */
	void enterNotOperator(BaseRuleParser.NotOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#notOperator}.
	 * @param ctx the parse tree
	 */
	void exitNotOperator(BaseRuleParser.NotOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#booleanPrimary}.
	 * @param ctx the parse tree
	 */
	void enterBooleanPrimary(BaseRuleParser.BooleanPrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#booleanPrimary}.
	 * @param ctx the parse tree
	 */
	void exitBooleanPrimary(BaseRuleParser.BooleanPrimaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOperator(BaseRuleParser.ComparisonOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOperator(BaseRuleParser.ComparisonOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterPredicate(BaseRuleParser.PredicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitPredicate(BaseRuleParser.PredicateContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#bitExpr}.
	 * @param ctx the parse tree
	 */
	void enterBitExpr(BaseRuleParser.BitExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#bitExpr}.
	 * @param ctx the parse tree
	 */
	void exitBitExpr(BaseRuleParser.BitExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#simpleExpr}.
	 * @param ctx the parse tree
	 */
	void enterSimpleExpr(BaseRuleParser.SimpleExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#simpleExpr}.
	 * @param ctx the parse tree
	 */
	void exitSimpleExpr(BaseRuleParser.SimpleExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCall(BaseRuleParser.FunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCall(BaseRuleParser.FunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#aggregationFunction}.
	 * @param ctx the parse tree
	 */
	void enterAggregationFunction(BaseRuleParser.AggregationFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#aggregationFunction}.
	 * @param ctx the parse tree
	 */
	void exitAggregationFunction(BaseRuleParser.AggregationFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#aggregationFunctionName}.
	 * @param ctx the parse tree
	 */
	void enterAggregationFunctionName(BaseRuleParser.AggregationFunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#aggregationFunctionName}.
	 * @param ctx the parse tree
	 */
	void exitAggregationFunctionName(BaseRuleParser.AggregationFunctionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#distinct}.
	 * @param ctx the parse tree
	 */
	void enterDistinct(BaseRuleParser.DistinctContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#distinct}.
	 * @param ctx the parse tree
	 */
	void exitDistinct(BaseRuleParser.DistinctContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#specialFunction}.
	 * @param ctx the parse tree
	 */
	void enterSpecialFunction(BaseRuleParser.SpecialFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#specialFunction}.
	 * @param ctx the parse tree
	 */
	void exitSpecialFunction(BaseRuleParser.SpecialFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#castFunction}.
	 * @param ctx the parse tree
	 */
	void enterCastFunction(BaseRuleParser.CastFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#castFunction}.
	 * @param ctx the parse tree
	 */
	void exitCastFunction(BaseRuleParser.CastFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#charFunction}.
	 * @param ctx the parse tree
	 */
	void enterCharFunction(BaseRuleParser.CharFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#charFunction}.
	 * @param ctx the parse tree
	 */
	void exitCharFunction(BaseRuleParser.CharFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#regularFunction}.
	 * @param ctx the parse tree
	 */
	void enterRegularFunction(BaseRuleParser.RegularFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#regularFunction}.
	 * @param ctx the parse tree
	 */
	void exitRegularFunction(BaseRuleParser.RegularFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#regularFunctionName}.
	 * @param ctx the parse tree
	 */
	void enterRegularFunctionName(BaseRuleParser.RegularFunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#regularFunctionName}.
	 * @param ctx the parse tree
	 */
	void exitRegularFunctionName(BaseRuleParser.RegularFunctionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#caseExpression}.
	 * @param ctx the parse tree
	 */
	void enterCaseExpression(BaseRuleParser.CaseExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#caseExpression}.
	 * @param ctx the parse tree
	 */
	void exitCaseExpression(BaseRuleParser.CaseExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#caseWhen}.
	 * @param ctx the parse tree
	 */
	void enterCaseWhen(BaseRuleParser.CaseWhenContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#caseWhen}.
	 * @param ctx the parse tree
	 */
	void exitCaseWhen(BaseRuleParser.CaseWhenContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#caseElse}.
	 * @param ctx the parse tree
	 */
	void enterCaseElse(BaseRuleParser.CaseElseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#caseElse}.
	 * @param ctx the parse tree
	 */
	void exitCaseElse(BaseRuleParser.CaseElseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#privateExprOfDb}.
	 * @param ctx the parse tree
	 */
	void enterPrivateExprOfDb(BaseRuleParser.PrivateExprOfDbContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#privateExprOfDb}.
	 * @param ctx the parse tree
	 */
	void exitPrivateExprOfDb(BaseRuleParser.PrivateExprOfDbContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#subquery}.
	 * @param ctx the parse tree
	 */
	void enterSubquery(BaseRuleParser.SubqueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#subquery}.
	 * @param ctx the parse tree
	 */
	void exitSubquery(BaseRuleParser.SubqueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#orderByClause}.
	 * @param ctx the parse tree
	 */
	void enterOrderByClause(BaseRuleParser.OrderByClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#orderByClause}.
	 * @param ctx the parse tree
	 */
	void exitOrderByClause(BaseRuleParser.OrderByClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#orderByItem}.
	 * @param ctx the parse tree
	 */
	void enterOrderByItem(BaseRuleParser.OrderByItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#orderByItem}.
	 * @param ctx the parse tree
	 */
	void exitOrderByItem(BaseRuleParser.OrderByItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterDataType(BaseRuleParser.DataTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitDataType(BaseRuleParser.DataTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#dataTypeName}.
	 * @param ctx the parse tree
	 */
	void enterDataTypeName(BaseRuleParser.DataTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#dataTypeName}.
	 * @param ctx the parse tree
	 */
	void exitDataTypeName(BaseRuleParser.DataTypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#atTimeZoneExpr}.
	 * @param ctx the parse tree
	 */
	void enterAtTimeZoneExpr(BaseRuleParser.AtTimeZoneExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#atTimeZoneExpr}.
	 * @param ctx the parse tree
	 */
	void exitAtTimeZoneExpr(BaseRuleParser.AtTimeZoneExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#castExpr}.
	 * @param ctx the parse tree
	 */
	void enterCastExpr(BaseRuleParser.CastExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#castExpr}.
	 * @param ctx the parse tree
	 */
	void exitCastExpr(BaseRuleParser.CastExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#convertExpr}.
	 * @param ctx the parse tree
	 */
	void enterConvertExpr(BaseRuleParser.ConvertExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#convertExpr}.
	 * @param ctx the parse tree
	 */
	void exitConvertExpr(BaseRuleParser.ConvertExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#windowedFunction}.
	 * @param ctx the parse tree
	 */
	void enterWindowedFunction(BaseRuleParser.WindowedFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#windowedFunction}.
	 * @param ctx the parse tree
	 */
	void exitWindowedFunction(BaseRuleParser.WindowedFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#overClause}.
	 * @param ctx the parse tree
	 */
	void enterOverClause(BaseRuleParser.OverClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#overClause}.
	 * @param ctx the parse tree
	 */
	void exitOverClause(BaseRuleParser.OverClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#partitionByClause}.
	 * @param ctx the parse tree
	 */
	void enterPartitionByClause(BaseRuleParser.PartitionByClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#partitionByClause}.
	 * @param ctx the parse tree
	 */
	void exitPartitionByClause(BaseRuleParser.PartitionByClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#rowRangeClause}.
	 * @param ctx the parse tree
	 */
	void enterRowRangeClause(BaseRuleParser.RowRangeClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#rowRangeClause}.
	 * @param ctx the parse tree
	 */
	void exitRowRangeClause(BaseRuleParser.RowRangeClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#windowFrameExtent}.
	 * @param ctx the parse tree
	 */
	void enterWindowFrameExtent(BaseRuleParser.WindowFrameExtentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#windowFrameExtent}.
	 * @param ctx the parse tree
	 */
	void exitWindowFrameExtent(BaseRuleParser.WindowFrameExtentContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#windowFrameBetween}.
	 * @param ctx the parse tree
	 */
	void enterWindowFrameBetween(BaseRuleParser.WindowFrameBetweenContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#windowFrameBetween}.
	 * @param ctx the parse tree
	 */
	void exitWindowFrameBetween(BaseRuleParser.WindowFrameBetweenContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#windowFrameBound}.
	 * @param ctx the parse tree
	 */
	void enterWindowFrameBound(BaseRuleParser.WindowFrameBoundContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#windowFrameBound}.
	 * @param ctx the parse tree
	 */
	void exitWindowFrameBound(BaseRuleParser.WindowFrameBoundContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#windowFramePreceding}.
	 * @param ctx the parse tree
	 */
	void enterWindowFramePreceding(BaseRuleParser.WindowFramePrecedingContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#windowFramePreceding}.
	 * @param ctx the parse tree
	 */
	void exitWindowFramePreceding(BaseRuleParser.WindowFramePrecedingContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#windowFrameFollowing}.
	 * @param ctx the parse tree
	 */
	void enterWindowFrameFollowing(BaseRuleParser.WindowFrameFollowingContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#windowFrameFollowing}.
	 * @param ctx the parse tree
	 */
	void exitWindowFrameFollowing(BaseRuleParser.WindowFrameFollowingContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#columnNameWithSort}.
	 * @param ctx the parse tree
	 */
	void enterColumnNameWithSort(BaseRuleParser.ColumnNameWithSortContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#columnNameWithSort}.
	 * @param ctx the parse tree
	 */
	void exitColumnNameWithSort(BaseRuleParser.ColumnNameWithSortContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#indexOption}.
	 * @param ctx the parse tree
	 */
	void enterIndexOption(BaseRuleParser.IndexOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#indexOption}.
	 * @param ctx the parse tree
	 */
	void exitIndexOption(BaseRuleParser.IndexOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#compressionOption}.
	 * @param ctx the parse tree
	 */
	void enterCompressionOption(BaseRuleParser.CompressionOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#compressionOption}.
	 * @param ctx the parse tree
	 */
	void exitCompressionOption(BaseRuleParser.CompressionOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#eqTime}.
	 * @param ctx the parse tree
	 */
	void enterEqTime(BaseRuleParser.EqTimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#eqTime}.
	 * @param ctx the parse tree
	 */
	void exitEqTime(BaseRuleParser.EqTimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#eqOnOffOption}.
	 * @param ctx the parse tree
	 */
	void enterEqOnOffOption(BaseRuleParser.EqOnOffOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#eqOnOffOption}.
	 * @param ctx the parse tree
	 */
	void exitEqOnOffOption(BaseRuleParser.EqOnOffOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#eqKey}.
	 * @param ctx the parse tree
	 */
	void enterEqKey(BaseRuleParser.EqKeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#eqKey}.
	 * @param ctx the parse tree
	 */
	void exitEqKey(BaseRuleParser.EqKeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#eqOnOff}.
	 * @param ctx the parse tree
	 */
	void enterEqOnOff(BaseRuleParser.EqOnOffContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#eqOnOff}.
	 * @param ctx the parse tree
	 */
	void exitEqOnOff(BaseRuleParser.EqOnOffContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#onPartitionClause}.
	 * @param ctx the parse tree
	 */
	void enterOnPartitionClause(BaseRuleParser.OnPartitionClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#onPartitionClause}.
	 * @param ctx the parse tree
	 */
	void exitOnPartitionClause(BaseRuleParser.OnPartitionClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#partitionExpressions}.
	 * @param ctx the parse tree
	 */
	void enterPartitionExpressions(BaseRuleParser.PartitionExpressionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#partitionExpressions}.
	 * @param ctx the parse tree
	 */
	void exitPartitionExpressions(BaseRuleParser.PartitionExpressionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#partitionExpression}.
	 * @param ctx the parse tree
	 */
	void enterPartitionExpression(BaseRuleParser.PartitionExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#partitionExpression}.
	 * @param ctx the parse tree
	 */
	void exitPartitionExpression(BaseRuleParser.PartitionExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#numberRange}.
	 * @param ctx the parse tree
	 */
	void enterNumberRange(BaseRuleParser.NumberRangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#numberRange}.
	 * @param ctx the parse tree
	 */
	void exitNumberRange(BaseRuleParser.NumberRangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#lowPriorityLockWait}.
	 * @param ctx the parse tree
	 */
	void enterLowPriorityLockWait(BaseRuleParser.LowPriorityLockWaitContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#lowPriorityLockWait}.
	 * @param ctx the parse tree
	 */
	void exitLowPriorityLockWait(BaseRuleParser.LowPriorityLockWaitContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#onLowPriorLockWait}.
	 * @param ctx the parse tree
	 */
	void enterOnLowPriorLockWait(BaseRuleParser.OnLowPriorLockWaitContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#onLowPriorLockWait}.
	 * @param ctx the parse tree
	 */
	void exitOnLowPriorLockWait(BaseRuleParser.OnLowPriorLockWaitContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#ignoredIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterIgnoredIdentifier(BaseRuleParser.IgnoredIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#ignoredIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitIgnoredIdentifier(BaseRuleParser.IgnoredIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#ignoredIdentifiers}.
	 * @param ctx the parse tree
	 */
	void enterIgnoredIdentifiers(BaseRuleParser.IgnoredIdentifiersContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#ignoredIdentifiers}.
	 * @param ctx the parse tree
	 */
	void exitIgnoredIdentifiers(BaseRuleParser.IgnoredIdentifiersContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#matchNone}.
	 * @param ctx the parse tree
	 */
	void enterMatchNone(BaseRuleParser.MatchNoneContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#matchNone}.
	 * @param ctx the parse tree
	 */
	void exitMatchNone(BaseRuleParser.MatchNoneContext ctx);
}