// Generated from /home/totalo/code/shardingsphere/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-postgresql/src/main/antlr4/imports/postgresql/BaseRule.g4 by ANTLR 4.9.1
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
	 * Enter a parse tree produced by {@link BaseRuleParser#reservedKeyword}.
	 * @param ctx the parse tree
	 */
	void enterReservedKeyword(BaseRuleParser.ReservedKeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#reservedKeyword}.
	 * @param ctx the parse tree
	 */
	void exitReservedKeyword(BaseRuleParser.ReservedKeywordContext ctx);
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
	 * Enter a parse tree produced by {@link BaseRuleParser#literalsType}.
	 * @param ctx the parse tree
	 */
	void enterLiteralsType(BaseRuleParser.LiteralsTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#literalsType}.
	 * @param ctx the parse tree
	 */
	void exitLiteralsType(BaseRuleParser.LiteralsTypeContext ctx);
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
	 * Enter a parse tree produced by {@link BaseRuleParser#unicodeEscapes}.
	 * @param ctx the parse tree
	 */
	void enterUnicodeEscapes(BaseRuleParser.UnicodeEscapesContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#unicodeEscapes}.
	 * @param ctx the parse tree
	 */
	void exitUnicodeEscapes(BaseRuleParser.UnicodeEscapesContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#uescape}.
	 * @param ctx the parse tree
	 */
	void enterUescape(BaseRuleParser.UescapeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#uescape}.
	 * @param ctx the parse tree
	 */
	void exitUescape(BaseRuleParser.UescapeContext ctx);
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
	 * Enter a parse tree produced by {@link BaseRuleParser#typeFuncNameKeyword}.
	 * @param ctx the parse tree
	 */
	void enterTypeFuncNameKeyword(BaseRuleParser.TypeFuncNameKeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#typeFuncNameKeyword}.
	 * @param ctx the parse tree
	 */
	void exitTypeFuncNameKeyword(BaseRuleParser.TypeFuncNameKeywordContext ctx);
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
	 * Enter a parse tree produced by {@link BaseRuleParser#patternMatchingOperator}.
	 * @param ctx the parse tree
	 */
	void enterPatternMatchingOperator(BaseRuleParser.PatternMatchingOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#patternMatchingOperator}.
	 * @param ctx the parse tree
	 */
	void exitPatternMatchingOperator(BaseRuleParser.PatternMatchingOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#cursorName}.
	 * @param ctx the parse tree
	 */
	void enterCursorName(BaseRuleParser.CursorNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#cursorName}.
	 * @param ctx the parse tree
	 */
	void exitCursorName(BaseRuleParser.CursorNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#aExpr}.
	 * @param ctx the parse tree
	 */
	void enterAExpr(BaseRuleParser.AExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#aExpr}.
	 * @param ctx the parse tree
	 */
	void exitAExpr(BaseRuleParser.AExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#bExpr}.
	 * @param ctx the parse tree
	 */
	void enterBExpr(BaseRuleParser.BExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#bExpr}.
	 * @param ctx the parse tree
	 */
	void exitBExpr(BaseRuleParser.BExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#cExpr}.
	 * @param ctx the parse tree
	 */
	void enterCExpr(BaseRuleParser.CExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#cExpr}.
	 * @param ctx the parse tree
	 */
	void exitCExpr(BaseRuleParser.CExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#indirection}.
	 * @param ctx the parse tree
	 */
	void enterIndirection(BaseRuleParser.IndirectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#indirection}.
	 * @param ctx the parse tree
	 */
	void exitIndirection(BaseRuleParser.IndirectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#optIndirection}.
	 * @param ctx the parse tree
	 */
	void enterOptIndirection(BaseRuleParser.OptIndirectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#optIndirection}.
	 * @param ctx the parse tree
	 */
	void exitOptIndirection(BaseRuleParser.OptIndirectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#indirectionEl}.
	 * @param ctx the parse tree
	 */
	void enterIndirectionEl(BaseRuleParser.IndirectionElContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#indirectionEl}.
	 * @param ctx the parse tree
	 */
	void exitIndirectionEl(BaseRuleParser.IndirectionElContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#sliceBound}.
	 * @param ctx the parse tree
	 */
	void enterSliceBound(BaseRuleParser.SliceBoundContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#sliceBound}.
	 * @param ctx the parse tree
	 */
	void exitSliceBound(BaseRuleParser.SliceBoundContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#inExpr}.
	 * @param ctx the parse tree
	 */
	void enterInExpr(BaseRuleParser.InExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#inExpr}.
	 * @param ctx the parse tree
	 */
	void exitInExpr(BaseRuleParser.InExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#caseExpr}.
	 * @param ctx the parse tree
	 */
	void enterCaseExpr(BaseRuleParser.CaseExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#caseExpr}.
	 * @param ctx the parse tree
	 */
	void exitCaseExpr(BaseRuleParser.CaseExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#whenClauseList}.
	 * @param ctx the parse tree
	 */
	void enterWhenClauseList(BaseRuleParser.WhenClauseListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#whenClauseList}.
	 * @param ctx the parse tree
	 */
	void exitWhenClauseList(BaseRuleParser.WhenClauseListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#whenClause}.
	 * @param ctx the parse tree
	 */
	void enterWhenClause(BaseRuleParser.WhenClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#whenClause}.
	 * @param ctx the parse tree
	 */
	void exitWhenClause(BaseRuleParser.WhenClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#caseDefault}.
	 * @param ctx the parse tree
	 */
	void enterCaseDefault(BaseRuleParser.CaseDefaultContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#caseDefault}.
	 * @param ctx the parse tree
	 */
	void exitCaseDefault(BaseRuleParser.CaseDefaultContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#caseArg}.
	 * @param ctx the parse tree
	 */
	void enterCaseArg(BaseRuleParser.CaseArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#caseArg}.
	 * @param ctx the parse tree
	 */
	void exitCaseArg(BaseRuleParser.CaseArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#columnref}.
	 * @param ctx the parse tree
	 */
	void enterColumnref(BaseRuleParser.ColumnrefContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#columnref}.
	 * @param ctx the parse tree
	 */
	void exitColumnref(BaseRuleParser.ColumnrefContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#qualOp}.
	 * @param ctx the parse tree
	 */
	void enterQualOp(BaseRuleParser.QualOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#qualOp}.
	 * @param ctx the parse tree
	 */
	void exitQualOp(BaseRuleParser.QualOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#subqueryOp}.
	 * @param ctx the parse tree
	 */
	void enterSubqueryOp(BaseRuleParser.SubqueryOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#subqueryOp}.
	 * @param ctx the parse tree
	 */
	void exitSubqueryOp(BaseRuleParser.SubqueryOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#allOp}.
	 * @param ctx the parse tree
	 */
	void enterAllOp(BaseRuleParser.AllOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#allOp}.
	 * @param ctx the parse tree
	 */
	void exitAllOp(BaseRuleParser.AllOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#op}.
	 * @param ctx the parse tree
	 */
	void enterOp(BaseRuleParser.OpContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#op}.
	 * @param ctx the parse tree
	 */
	void exitOp(BaseRuleParser.OpContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#mathOperator}.
	 * @param ctx the parse tree
	 */
	void enterMathOperator(BaseRuleParser.MathOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#mathOperator}.
	 * @param ctx the parse tree
	 */
	void exitMathOperator(BaseRuleParser.MathOperatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonExtract}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonExtract(BaseRuleParser.JsonExtractContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonExtract}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonExtract(BaseRuleParser.JsonExtractContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonExtractText}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonExtractText(BaseRuleParser.JsonExtractTextContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonExtractText}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonExtractText(BaseRuleParser.JsonExtractTextContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonPathExtract}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonPathExtract(BaseRuleParser.JsonPathExtractContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonPathExtract}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonPathExtract(BaseRuleParser.JsonPathExtractContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonPathExtractText}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonPathExtractText(BaseRuleParser.JsonPathExtractTextContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonPathExtractText}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonPathExtractText(BaseRuleParser.JsonPathExtractTextContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbContainRight}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbContainRight(BaseRuleParser.JsonbContainRightContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbContainRight}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbContainRight(BaseRuleParser.JsonbContainRightContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbContainLeft}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbContainLeft(BaseRuleParser.JsonbContainLeftContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbContainLeft}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbContainLeft(BaseRuleParser.JsonbContainLeftContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbContainTopKey}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbContainTopKey(BaseRuleParser.JsonbContainTopKeyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbContainTopKey}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbContainTopKey(BaseRuleParser.JsonbContainTopKeyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbContainAnyTopKey}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbContainAnyTopKey(BaseRuleParser.JsonbContainAnyTopKeyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbContainAnyTopKey}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbContainAnyTopKey(BaseRuleParser.JsonbContainAnyTopKeyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbContainAllTopKey}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbContainAllTopKey(BaseRuleParser.JsonbContainAllTopKeyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbContainAllTopKey}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbContainAllTopKey(BaseRuleParser.JsonbContainAllTopKeyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbConcat}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbConcat(BaseRuleParser.JsonbConcatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbConcat}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbConcat(BaseRuleParser.JsonbConcatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbDelete}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbDelete(BaseRuleParser.JsonbDeleteContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbDelete}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbDelete(BaseRuleParser.JsonbDeleteContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbPathDelete}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbPathDelete(BaseRuleParser.JsonbPathDeleteContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbPathDelete}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbPathDelete(BaseRuleParser.JsonbPathDeleteContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbPathContainAnyValue}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbPathContainAnyValue(BaseRuleParser.JsonbPathContainAnyValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbPathContainAnyValue}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbPathContainAnyValue(BaseRuleParser.JsonbPathContainAnyValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbPathPredicateCheck}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbPathPredicateCheck(BaseRuleParser.JsonbPathPredicateCheckContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbPathPredicateCheck}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbPathPredicateCheck(BaseRuleParser.JsonbPathPredicateCheckContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#qualAllOp}.
	 * @param ctx the parse tree
	 */
	void enterQualAllOp(BaseRuleParser.QualAllOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#qualAllOp}.
	 * @param ctx the parse tree
	 */
	void exitQualAllOp(BaseRuleParser.QualAllOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#ascDesc}.
	 * @param ctx the parse tree
	 */
	void enterAscDesc(BaseRuleParser.AscDescContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#ascDesc}.
	 * @param ctx the parse tree
	 */
	void exitAscDesc(BaseRuleParser.AscDescContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#anyOperator}.
	 * @param ctx the parse tree
	 */
	void enterAnyOperator(BaseRuleParser.AnyOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#anyOperator}.
	 * @param ctx the parse tree
	 */
	void exitAnyOperator(BaseRuleParser.AnyOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#frameClause}.
	 * @param ctx the parse tree
	 */
	void enterFrameClause(BaseRuleParser.FrameClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#frameClause}.
	 * @param ctx the parse tree
	 */
	void exitFrameClause(BaseRuleParser.FrameClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#frameExtent}.
	 * @param ctx the parse tree
	 */
	void enterFrameExtent(BaseRuleParser.FrameExtentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#frameExtent}.
	 * @param ctx the parse tree
	 */
	void exitFrameExtent(BaseRuleParser.FrameExtentContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#frameBound}.
	 * @param ctx the parse tree
	 */
	void enterFrameBound(BaseRuleParser.FrameBoundContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#frameBound}.
	 * @param ctx the parse tree
	 */
	void exitFrameBound(BaseRuleParser.FrameBoundContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#windowExclusionClause}.
	 * @param ctx the parse tree
	 */
	void enterWindowExclusionClause(BaseRuleParser.WindowExclusionClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#windowExclusionClause}.
	 * @param ctx the parse tree
	 */
	void exitWindowExclusionClause(BaseRuleParser.WindowExclusionClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#row}.
	 * @param ctx the parse tree
	 */
	void enterRow(BaseRuleParser.RowContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#row}.
	 * @param ctx the parse tree
	 */
	void exitRow(BaseRuleParser.RowContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#explicitRow}.
	 * @param ctx the parse tree
	 */
	void enterExplicitRow(BaseRuleParser.ExplicitRowContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#explicitRow}.
	 * @param ctx the parse tree
	 */
	void exitExplicitRow(BaseRuleParser.ExplicitRowContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#implicitRow}.
	 * @param ctx the parse tree
	 */
	void enterImplicitRow(BaseRuleParser.ImplicitRowContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#implicitRow}.
	 * @param ctx the parse tree
	 */
	void exitImplicitRow(BaseRuleParser.ImplicitRowContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#subType}.
	 * @param ctx the parse tree
	 */
	void enterSubType(BaseRuleParser.SubTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#subType}.
	 * @param ctx the parse tree
	 */
	void exitSubType(BaseRuleParser.SubTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#arrayExpr}.
	 * @param ctx the parse tree
	 */
	void enterArrayExpr(BaseRuleParser.ArrayExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#arrayExpr}.
	 * @param ctx the parse tree
	 */
	void exitArrayExpr(BaseRuleParser.ArrayExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#arrayExprList}.
	 * @param ctx the parse tree
	 */
	void enterArrayExprList(BaseRuleParser.ArrayExprListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#arrayExprList}.
	 * @param ctx the parse tree
	 */
	void exitArrayExprList(BaseRuleParser.ArrayExprListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#funcArgList}.
	 * @param ctx the parse tree
	 */
	void enterFuncArgList(BaseRuleParser.FuncArgListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#funcArgList}.
	 * @param ctx the parse tree
	 */
	void exitFuncArgList(BaseRuleParser.FuncArgListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#paramName}.
	 * @param ctx the parse tree
	 */
	void enterParamName(BaseRuleParser.ParamNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#paramName}.
	 * @param ctx the parse tree
	 */
	void exitParamName(BaseRuleParser.ParamNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#funcArgExpr}.
	 * @param ctx the parse tree
	 */
	void enterFuncArgExpr(BaseRuleParser.FuncArgExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#funcArgExpr}.
	 * @param ctx the parse tree
	 */
	void exitFuncArgExpr(BaseRuleParser.FuncArgExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#typeList}.
	 * @param ctx the parse tree
	 */
	void enterTypeList(BaseRuleParser.TypeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#typeList}.
	 * @param ctx the parse tree
	 */
	void exitTypeList(BaseRuleParser.TypeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#funcApplication}.
	 * @param ctx the parse tree
	 */
	void enterFuncApplication(BaseRuleParser.FuncApplicationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#funcApplication}.
	 * @param ctx the parse tree
	 */
	void exitFuncApplication(BaseRuleParser.FuncApplicationContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#funcName}.
	 * @param ctx the parse tree
	 */
	void enterFuncName(BaseRuleParser.FuncNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#funcName}.
	 * @param ctx the parse tree
	 */
	void exitFuncName(BaseRuleParser.FuncNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#aexprConst}.
	 * @param ctx the parse tree
	 */
	void enterAexprConst(BaseRuleParser.AexprConstContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#aexprConst}.
	 * @param ctx the parse tree
	 */
	void exitAexprConst(BaseRuleParser.AexprConstContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedName(BaseRuleParser.QualifiedNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedName(BaseRuleParser.QualifiedNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#colId}.
	 * @param ctx the parse tree
	 */
	void enterColId(BaseRuleParser.ColIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#colId}.
	 * @param ctx the parse tree
	 */
	void exitColId(BaseRuleParser.ColIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#typeFunctionName}.
	 * @param ctx the parse tree
	 */
	void enterTypeFunctionName(BaseRuleParser.TypeFunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#typeFunctionName}.
	 * @param ctx the parse tree
	 */
	void exitTypeFunctionName(BaseRuleParser.TypeFunctionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#functionTable}.
	 * @param ctx the parse tree
	 */
	void enterFunctionTable(BaseRuleParser.FunctionTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#functionTable}.
	 * @param ctx the parse tree
	 */
	void exitFunctionTable(BaseRuleParser.FunctionTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#xmlTable}.
	 * @param ctx the parse tree
	 */
	void enterXmlTable(BaseRuleParser.XmlTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#xmlTable}.
	 * @param ctx the parse tree
	 */
	void exitXmlTable(BaseRuleParser.XmlTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#xmlTableColumnList}.
	 * @param ctx the parse tree
	 */
	void enterXmlTableColumnList(BaseRuleParser.XmlTableColumnListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#xmlTableColumnList}.
	 * @param ctx the parse tree
	 */
	void exitXmlTableColumnList(BaseRuleParser.XmlTableColumnListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#xmlTableColumnEl}.
	 * @param ctx the parse tree
	 */
	void enterXmlTableColumnEl(BaseRuleParser.XmlTableColumnElContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#xmlTableColumnEl}.
	 * @param ctx the parse tree
	 */
	void exitXmlTableColumnEl(BaseRuleParser.XmlTableColumnElContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#xmlTableColumnOptionList}.
	 * @param ctx the parse tree
	 */
	void enterXmlTableColumnOptionList(BaseRuleParser.XmlTableColumnOptionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#xmlTableColumnOptionList}.
	 * @param ctx the parse tree
	 */
	void exitXmlTableColumnOptionList(BaseRuleParser.XmlTableColumnOptionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#xmlTableColumnOptionEl}.
	 * @param ctx the parse tree
	 */
	void enterXmlTableColumnOptionEl(BaseRuleParser.XmlTableColumnOptionElContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#xmlTableColumnOptionEl}.
	 * @param ctx the parse tree
	 */
	void exitXmlTableColumnOptionEl(BaseRuleParser.XmlTableColumnOptionElContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#xmlNamespaceList}.
	 * @param ctx the parse tree
	 */
	void enterXmlNamespaceList(BaseRuleParser.XmlNamespaceListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#xmlNamespaceList}.
	 * @param ctx the parse tree
	 */
	void exitXmlNamespaceList(BaseRuleParser.XmlNamespaceListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#xmlNamespaceEl}.
	 * @param ctx the parse tree
	 */
	void enterXmlNamespaceEl(BaseRuleParser.XmlNamespaceElContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#xmlNamespaceEl}.
	 * @param ctx the parse tree
	 */
	void exitXmlNamespaceEl(BaseRuleParser.XmlNamespaceElContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#funcExpr}.
	 * @param ctx the parse tree
	 */
	void enterFuncExpr(BaseRuleParser.FuncExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#funcExpr}.
	 * @param ctx the parse tree
	 */
	void exitFuncExpr(BaseRuleParser.FuncExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#withinGroupClause}.
	 * @param ctx the parse tree
	 */
	void enterWithinGroupClause(BaseRuleParser.WithinGroupClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#withinGroupClause}.
	 * @param ctx the parse tree
	 */
	void exitWithinGroupClause(BaseRuleParser.WithinGroupClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#filterClause}.
	 * @param ctx the parse tree
	 */
	void enterFilterClause(BaseRuleParser.FilterClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#filterClause}.
	 * @param ctx the parse tree
	 */
	void exitFilterClause(BaseRuleParser.FilterClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#functionExprWindowless}.
	 * @param ctx the parse tree
	 */
	void enterFunctionExprWindowless(BaseRuleParser.FunctionExprWindowlessContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#functionExprWindowless}.
	 * @param ctx the parse tree
	 */
	void exitFunctionExprWindowless(BaseRuleParser.FunctionExprWindowlessContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#ordinality}.
	 * @param ctx the parse tree
	 */
	void enterOrdinality(BaseRuleParser.OrdinalityContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#ordinality}.
	 * @param ctx the parse tree
	 */
	void exitOrdinality(BaseRuleParser.OrdinalityContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#functionExprCommonSubexpr}.
	 * @param ctx the parse tree
	 */
	void enterFunctionExprCommonSubexpr(BaseRuleParser.FunctionExprCommonSubexprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#functionExprCommonSubexpr}.
	 * @param ctx the parse tree
	 */
	void exitFunctionExprCommonSubexpr(BaseRuleParser.FunctionExprCommonSubexprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#typeName}.
	 * @param ctx the parse tree
	 */
	void enterTypeName(BaseRuleParser.TypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#typeName}.
	 * @param ctx the parse tree
	 */
	void exitTypeName(BaseRuleParser.TypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#simpleTypeName}.
	 * @param ctx the parse tree
	 */
	void enterSimpleTypeName(BaseRuleParser.SimpleTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#simpleTypeName}.
	 * @param ctx the parse tree
	 */
	void exitSimpleTypeName(BaseRuleParser.SimpleTypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#exprList}.
	 * @param ctx the parse tree
	 */
	void enterExprList(BaseRuleParser.ExprListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#exprList}.
	 * @param ctx the parse tree
	 */
	void exitExprList(BaseRuleParser.ExprListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#extractList}.
	 * @param ctx the parse tree
	 */
	void enterExtractList(BaseRuleParser.ExtractListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#extractList}.
	 * @param ctx the parse tree
	 */
	void exitExtractList(BaseRuleParser.ExtractListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#extractArg}.
	 * @param ctx the parse tree
	 */
	void enterExtractArg(BaseRuleParser.ExtractArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#extractArg}.
	 * @param ctx the parse tree
	 */
	void exitExtractArg(BaseRuleParser.ExtractArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#genericType}.
	 * @param ctx the parse tree
	 */
	void enterGenericType(BaseRuleParser.GenericTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#genericType}.
	 * @param ctx the parse tree
	 */
	void exitGenericType(BaseRuleParser.GenericTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#typeModifiers}.
	 * @param ctx the parse tree
	 */
	void enterTypeModifiers(BaseRuleParser.TypeModifiersContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#typeModifiers}.
	 * @param ctx the parse tree
	 */
	void exitTypeModifiers(BaseRuleParser.TypeModifiersContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#numeric}.
	 * @param ctx the parse tree
	 */
	void enterNumeric(BaseRuleParser.NumericContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#numeric}.
	 * @param ctx the parse tree
	 */
	void exitNumeric(BaseRuleParser.NumericContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#constDatetime}.
	 * @param ctx the parse tree
	 */
	void enterConstDatetime(BaseRuleParser.ConstDatetimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#constDatetime}.
	 * @param ctx the parse tree
	 */
	void exitConstDatetime(BaseRuleParser.ConstDatetimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#timezone}.
	 * @param ctx the parse tree
	 */
	void enterTimezone(BaseRuleParser.TimezoneContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#timezone}.
	 * @param ctx the parse tree
	 */
	void exitTimezone(BaseRuleParser.TimezoneContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#character}.
	 * @param ctx the parse tree
	 */
	void enterCharacter(BaseRuleParser.CharacterContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#character}.
	 * @param ctx the parse tree
	 */
	void exitCharacter(BaseRuleParser.CharacterContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#characterWithLength}.
	 * @param ctx the parse tree
	 */
	void enterCharacterWithLength(BaseRuleParser.CharacterWithLengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#characterWithLength}.
	 * @param ctx the parse tree
	 */
	void exitCharacterWithLength(BaseRuleParser.CharacterWithLengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#characterWithoutLength}.
	 * @param ctx the parse tree
	 */
	void enterCharacterWithoutLength(BaseRuleParser.CharacterWithoutLengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#characterWithoutLength}.
	 * @param ctx the parse tree
	 */
	void exitCharacterWithoutLength(BaseRuleParser.CharacterWithoutLengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#characterClause}.
	 * @param ctx the parse tree
	 */
	void enterCharacterClause(BaseRuleParser.CharacterClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#characterClause}.
	 * @param ctx the parse tree
	 */
	void exitCharacterClause(BaseRuleParser.CharacterClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#optFloat}.
	 * @param ctx the parse tree
	 */
	void enterOptFloat(BaseRuleParser.OptFloatContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#optFloat}.
	 * @param ctx the parse tree
	 */
	void exitOptFloat(BaseRuleParser.OptFloatContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#attrs}.
	 * @param ctx the parse tree
	 */
	void enterAttrs(BaseRuleParser.AttrsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#attrs}.
	 * @param ctx the parse tree
	 */
	void exitAttrs(BaseRuleParser.AttrsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#attrName}.
	 * @param ctx the parse tree
	 */
	void enterAttrName(BaseRuleParser.AttrNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#attrName}.
	 * @param ctx the parse tree
	 */
	void exitAttrName(BaseRuleParser.AttrNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#colLable}.
	 * @param ctx the parse tree
	 */
	void enterColLable(BaseRuleParser.ColLableContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#colLable}.
	 * @param ctx the parse tree
	 */
	void exitColLable(BaseRuleParser.ColLableContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#bit}.
	 * @param ctx the parse tree
	 */
	void enterBit(BaseRuleParser.BitContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#bit}.
	 * @param ctx the parse tree
	 */
	void exitBit(BaseRuleParser.BitContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#bitWithLength}.
	 * @param ctx the parse tree
	 */
	void enterBitWithLength(BaseRuleParser.BitWithLengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#bitWithLength}.
	 * @param ctx the parse tree
	 */
	void exitBitWithLength(BaseRuleParser.BitWithLengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#bitWithoutLength}.
	 * @param ctx the parse tree
	 */
	void enterBitWithoutLength(BaseRuleParser.BitWithoutLengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#bitWithoutLength}.
	 * @param ctx the parse tree
	 */
	void exitBitWithoutLength(BaseRuleParser.BitWithoutLengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#constInterval}.
	 * @param ctx the parse tree
	 */
	void enterConstInterval(BaseRuleParser.ConstIntervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#constInterval}.
	 * @param ctx the parse tree
	 */
	void exitConstInterval(BaseRuleParser.ConstIntervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#optInterval}.
	 * @param ctx the parse tree
	 */
	void enterOptInterval(BaseRuleParser.OptIntervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#optInterval}.
	 * @param ctx the parse tree
	 */
	void exitOptInterval(BaseRuleParser.OptIntervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#optArrayBounds}.
	 * @param ctx the parse tree
	 */
	void enterOptArrayBounds(BaseRuleParser.OptArrayBoundsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#optArrayBounds}.
	 * @param ctx the parse tree
	 */
	void exitOptArrayBounds(BaseRuleParser.OptArrayBoundsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#intervalSecond}.
	 * @param ctx the parse tree
	 */
	void enterIntervalSecond(BaseRuleParser.IntervalSecondContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#intervalSecond}.
	 * @param ctx the parse tree
	 */
	void exitIntervalSecond(BaseRuleParser.IntervalSecondContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#unicodeNormalForm}.
	 * @param ctx the parse tree
	 */
	void enterUnicodeNormalForm(BaseRuleParser.UnicodeNormalFormContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#unicodeNormalForm}.
	 * @param ctx the parse tree
	 */
	void exitUnicodeNormalForm(BaseRuleParser.UnicodeNormalFormContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#trimList}.
	 * @param ctx the parse tree
	 */
	void enterTrimList(BaseRuleParser.TrimListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#trimList}.
	 * @param ctx the parse tree
	 */
	void exitTrimList(BaseRuleParser.TrimListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#overlayList}.
	 * @param ctx the parse tree
	 */
	void enterOverlayList(BaseRuleParser.OverlayListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#overlayList}.
	 * @param ctx the parse tree
	 */
	void exitOverlayList(BaseRuleParser.OverlayListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#overlayPlacing}.
	 * @param ctx the parse tree
	 */
	void enterOverlayPlacing(BaseRuleParser.OverlayPlacingContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#overlayPlacing}.
	 * @param ctx the parse tree
	 */
	void exitOverlayPlacing(BaseRuleParser.OverlayPlacingContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#substrFrom}.
	 * @param ctx the parse tree
	 */
	void enterSubstrFrom(BaseRuleParser.SubstrFromContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#substrFrom}.
	 * @param ctx the parse tree
	 */
	void exitSubstrFrom(BaseRuleParser.SubstrFromContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#substrFor}.
	 * @param ctx the parse tree
	 */
	void enterSubstrFor(BaseRuleParser.SubstrForContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#substrFor}.
	 * @param ctx the parse tree
	 */
	void exitSubstrFor(BaseRuleParser.SubstrForContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#positionList}.
	 * @param ctx the parse tree
	 */
	void enterPositionList(BaseRuleParser.PositionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#positionList}.
	 * @param ctx the parse tree
	 */
	void exitPositionList(BaseRuleParser.PositionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#substrList}.
	 * @param ctx the parse tree
	 */
	void enterSubstrList(BaseRuleParser.SubstrListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#substrList}.
	 * @param ctx the parse tree
	 */
	void exitSubstrList(BaseRuleParser.SubstrListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#xmlAttributes}.
	 * @param ctx the parse tree
	 */
	void enterXmlAttributes(BaseRuleParser.XmlAttributesContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#xmlAttributes}.
	 * @param ctx the parse tree
	 */
	void exitXmlAttributes(BaseRuleParser.XmlAttributesContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#xmlAttributeList}.
	 * @param ctx the parse tree
	 */
	void enterXmlAttributeList(BaseRuleParser.XmlAttributeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#xmlAttributeList}.
	 * @param ctx the parse tree
	 */
	void exitXmlAttributeList(BaseRuleParser.XmlAttributeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#xmlAttributeEl}.
	 * @param ctx the parse tree
	 */
	void enterXmlAttributeEl(BaseRuleParser.XmlAttributeElContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#xmlAttributeEl}.
	 * @param ctx the parse tree
	 */
	void exitXmlAttributeEl(BaseRuleParser.XmlAttributeElContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#xmlExistsArgument}.
	 * @param ctx the parse tree
	 */
	void enterXmlExistsArgument(BaseRuleParser.XmlExistsArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#xmlExistsArgument}.
	 * @param ctx the parse tree
	 */
	void exitXmlExistsArgument(BaseRuleParser.XmlExistsArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#xmlPassingMech}.
	 * @param ctx the parse tree
	 */
	void enterXmlPassingMech(BaseRuleParser.XmlPassingMechContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#xmlPassingMech}.
	 * @param ctx the parse tree
	 */
	void exitXmlPassingMech(BaseRuleParser.XmlPassingMechContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#documentOrContent}.
	 * @param ctx the parse tree
	 */
	void enterDocumentOrContent(BaseRuleParser.DocumentOrContentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#documentOrContent}.
	 * @param ctx the parse tree
	 */
	void exitDocumentOrContent(BaseRuleParser.DocumentOrContentContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#xmlWhitespaceOption}.
	 * @param ctx the parse tree
	 */
	void enterXmlWhitespaceOption(BaseRuleParser.XmlWhitespaceOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#xmlWhitespaceOption}.
	 * @param ctx the parse tree
	 */
	void exitXmlWhitespaceOption(BaseRuleParser.XmlWhitespaceOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#xmlRootVersion}.
	 * @param ctx the parse tree
	 */
	void enterXmlRootVersion(BaseRuleParser.XmlRootVersionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#xmlRootVersion}.
	 * @param ctx the parse tree
	 */
	void exitXmlRootVersion(BaseRuleParser.XmlRootVersionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#xmlRootStandalone}.
	 * @param ctx the parse tree
	 */
	void enterXmlRootStandalone(BaseRuleParser.XmlRootStandaloneContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#xmlRootStandalone}.
	 * @param ctx the parse tree
	 */
	void exitXmlRootStandalone(BaseRuleParser.XmlRootStandaloneContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#rowsFromItem}.
	 * @param ctx the parse tree
	 */
	void enterRowsFromItem(BaseRuleParser.RowsFromItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#rowsFromItem}.
	 * @param ctx the parse tree
	 */
	void exitRowsFromItem(BaseRuleParser.RowsFromItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#rowsFromList}.
	 * @param ctx the parse tree
	 */
	void enterRowsFromList(BaseRuleParser.RowsFromListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#rowsFromList}.
	 * @param ctx the parse tree
	 */
	void exitRowsFromList(BaseRuleParser.RowsFromListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#columnDefList}.
	 * @param ctx the parse tree
	 */
	void enterColumnDefList(BaseRuleParser.ColumnDefListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#columnDefList}.
	 * @param ctx the parse tree
	 */
	void exitColumnDefList(BaseRuleParser.ColumnDefListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#tableFuncElementList}.
	 * @param ctx the parse tree
	 */
	void enterTableFuncElementList(BaseRuleParser.TableFuncElementListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#tableFuncElementList}.
	 * @param ctx the parse tree
	 */
	void exitTableFuncElementList(BaseRuleParser.TableFuncElementListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#tableFuncElement}.
	 * @param ctx the parse tree
	 */
	void enterTableFuncElement(BaseRuleParser.TableFuncElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#tableFuncElement}.
	 * @param ctx the parse tree
	 */
	void exitTableFuncElement(BaseRuleParser.TableFuncElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#collateClause}.
	 * @param ctx the parse tree
	 */
	void enterCollateClause(BaseRuleParser.CollateClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#collateClause}.
	 * @param ctx the parse tree
	 */
	void exitCollateClause(BaseRuleParser.CollateClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#anyName}.
	 * @param ctx the parse tree
	 */
	void enterAnyName(BaseRuleParser.AnyNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#anyName}.
	 * @param ctx the parse tree
	 */
	void exitAnyName(BaseRuleParser.AnyNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#aliasClause}.
	 * @param ctx the parse tree
	 */
	void enterAliasClause(BaseRuleParser.AliasClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#aliasClause}.
	 * @param ctx the parse tree
	 */
	void exitAliasClause(BaseRuleParser.AliasClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#nameList}.
	 * @param ctx the parse tree
	 */
	void enterNameList(BaseRuleParser.NameListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#nameList}.
	 * @param ctx the parse tree
	 */
	void exitNameList(BaseRuleParser.NameListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#funcAliasClause}.
	 * @param ctx the parse tree
	 */
	void enterFuncAliasClause(BaseRuleParser.FuncAliasClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#funcAliasClause}.
	 * @param ctx the parse tree
	 */
	void exitFuncAliasClause(BaseRuleParser.FuncAliasClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#tablesampleClause}.
	 * @param ctx the parse tree
	 */
	void enterTablesampleClause(BaseRuleParser.TablesampleClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#tablesampleClause}.
	 * @param ctx the parse tree
	 */
	void exitTablesampleClause(BaseRuleParser.TablesampleClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#repeatableClause}.
	 * @param ctx the parse tree
	 */
	void enterRepeatableClause(BaseRuleParser.RepeatableClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#repeatableClause}.
	 * @param ctx the parse tree
	 */
	void exitRepeatableClause(BaseRuleParser.RepeatableClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#allOrDistinct}.
	 * @param ctx the parse tree
	 */
	void enterAllOrDistinct(BaseRuleParser.AllOrDistinctContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#allOrDistinct}.
	 * @param ctx the parse tree
	 */
	void exitAllOrDistinct(BaseRuleParser.AllOrDistinctContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#sortClause}.
	 * @param ctx the parse tree
	 */
	void enterSortClause(BaseRuleParser.SortClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#sortClause}.
	 * @param ctx the parse tree
	 */
	void exitSortClause(BaseRuleParser.SortClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#sortbyList}.
	 * @param ctx the parse tree
	 */
	void enterSortbyList(BaseRuleParser.SortbyListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#sortbyList}.
	 * @param ctx the parse tree
	 */
	void exitSortbyList(BaseRuleParser.SortbyListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#sortby}.
	 * @param ctx the parse tree
	 */
	void enterSortby(BaseRuleParser.SortbyContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#sortby}.
	 * @param ctx the parse tree
	 */
	void exitSortby(BaseRuleParser.SortbyContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#nullsOrder}.
	 * @param ctx the parse tree
	 */
	void enterNullsOrder(BaseRuleParser.NullsOrderContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#nullsOrder}.
	 * @param ctx the parse tree
	 */
	void exitNullsOrder(BaseRuleParser.NullsOrderContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#distinctClause}.
	 * @param ctx the parse tree
	 */
	void enterDistinctClause(BaseRuleParser.DistinctClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#distinctClause}.
	 * @param ctx the parse tree
	 */
	void exitDistinctClause(BaseRuleParser.DistinctClauseContext ctx);
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
	 * Enter a parse tree produced by {@link BaseRuleParser#windowSpecification}.
	 * @param ctx the parse tree
	 */
	void enterWindowSpecification(BaseRuleParser.WindowSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#windowSpecification}.
	 * @param ctx the parse tree
	 */
	void exitWindowSpecification(BaseRuleParser.WindowSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#windowName}.
	 * @param ctx the parse tree
	 */
	void enterWindowName(BaseRuleParser.WindowNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#windowName}.
	 * @param ctx the parse tree
	 */
	void exitWindowName(BaseRuleParser.WindowNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#partitionClause}.
	 * @param ctx the parse tree
	 */
	void enterPartitionClause(BaseRuleParser.PartitionClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#partitionClause}.
	 * @param ctx the parse tree
	 */
	void exitPartitionClause(BaseRuleParser.PartitionClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#indexParams}.
	 * @param ctx the parse tree
	 */
	void enterIndexParams(BaseRuleParser.IndexParamsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#indexParams}.
	 * @param ctx the parse tree
	 */
	void exitIndexParams(BaseRuleParser.IndexParamsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#indexElemOptions}.
	 * @param ctx the parse tree
	 */
	void enterIndexElemOptions(BaseRuleParser.IndexElemOptionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#indexElemOptions}.
	 * @param ctx the parse tree
	 */
	void exitIndexElemOptions(BaseRuleParser.IndexElemOptionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#indexElem}.
	 * @param ctx the parse tree
	 */
	void enterIndexElem(BaseRuleParser.IndexElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#indexElem}.
	 * @param ctx the parse tree
	 */
	void exitIndexElem(BaseRuleParser.IndexElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#collate}.
	 * @param ctx the parse tree
	 */
	void enterCollate(BaseRuleParser.CollateContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#collate}.
	 * @param ctx the parse tree
	 */
	void exitCollate(BaseRuleParser.CollateContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#optClass}.
	 * @param ctx the parse tree
	 */
	void enterOptClass(BaseRuleParser.OptClassContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#optClass}.
	 * @param ctx the parse tree
	 */
	void exitOptClass(BaseRuleParser.OptClassContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#reloptions}.
	 * @param ctx the parse tree
	 */
	void enterReloptions(BaseRuleParser.ReloptionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#reloptions}.
	 * @param ctx the parse tree
	 */
	void exitReloptions(BaseRuleParser.ReloptionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#reloptionList}.
	 * @param ctx the parse tree
	 */
	void enterReloptionList(BaseRuleParser.ReloptionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#reloptionList}.
	 * @param ctx the parse tree
	 */
	void exitReloptionList(BaseRuleParser.ReloptionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#reloptionElem}.
	 * @param ctx the parse tree
	 */
	void enterReloptionElem(BaseRuleParser.ReloptionElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#reloptionElem}.
	 * @param ctx the parse tree
	 */
	void exitReloptionElem(BaseRuleParser.ReloptionElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#defArg}.
	 * @param ctx the parse tree
	 */
	void enterDefArg(BaseRuleParser.DefArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#defArg}.
	 * @param ctx the parse tree
	 */
	void exitDefArg(BaseRuleParser.DefArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#funcType}.
	 * @param ctx the parse tree
	 */
	void enterFuncType(BaseRuleParser.FuncTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#funcType}.
	 * @param ctx the parse tree
	 */
	void exitFuncType(BaseRuleParser.FuncTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#selectWithParens}.
	 * @param ctx the parse tree
	 */
	void enterSelectWithParens(BaseRuleParser.SelectWithParensContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#selectWithParens}.
	 * @param ctx the parse tree
	 */
	void exitSelectWithParens(BaseRuleParser.SelectWithParensContext ctx);
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
	 * Enter a parse tree produced by {@link BaseRuleParser#characterSet}.
	 * @param ctx the parse tree
	 */
	void enterCharacterSet(BaseRuleParser.CharacterSetContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#characterSet}.
	 * @param ctx the parse tree
	 */
	void exitCharacterSet(BaseRuleParser.CharacterSetContext ctx);
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
	 * Enter a parse tree produced by {@link BaseRuleParser#signedIconst}.
	 * @param ctx the parse tree
	 */
	void enterSignedIconst(BaseRuleParser.SignedIconstContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#signedIconst}.
	 * @param ctx the parse tree
	 */
	void exitSignedIconst(BaseRuleParser.SignedIconstContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#booleanOrString}.
	 * @param ctx the parse tree
	 */
	void enterBooleanOrString(BaseRuleParser.BooleanOrStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#booleanOrString}.
	 * @param ctx the parse tree
	 */
	void exitBooleanOrString(BaseRuleParser.BooleanOrStringContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#nonReservedWord}.
	 * @param ctx the parse tree
	 */
	void enterNonReservedWord(BaseRuleParser.NonReservedWordContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#nonReservedWord}.
	 * @param ctx the parse tree
	 */
	void exitNonReservedWord(BaseRuleParser.NonReservedWordContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#colNameKeyword}.
	 * @param ctx the parse tree
	 */
	void enterColNameKeyword(BaseRuleParser.ColNameKeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#colNameKeyword}.
	 * @param ctx the parse tree
	 */
	void exitColNameKeyword(BaseRuleParser.ColNameKeywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#databaseName}.
	 * @param ctx the parse tree
	 */
	void enterDatabaseName(BaseRuleParser.DatabaseNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#databaseName}.
	 * @param ctx the parse tree
	 */
	void exitDatabaseName(BaseRuleParser.DatabaseNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#roleSpec}.
	 * @param ctx the parse tree
	 */
	void enterRoleSpec(BaseRuleParser.RoleSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#roleSpec}.
	 * @param ctx the parse tree
	 */
	void exitRoleSpec(BaseRuleParser.RoleSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#varName}.
	 * @param ctx the parse tree
	 */
	void enterVarName(BaseRuleParser.VarNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#varName}.
	 * @param ctx the parse tree
	 */
	void exitVarName(BaseRuleParser.VarNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#varList}.
	 * @param ctx the parse tree
	 */
	void enterVarList(BaseRuleParser.VarListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#varList}.
	 * @param ctx the parse tree
	 */
	void exitVarList(BaseRuleParser.VarListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#varValue}.
	 * @param ctx the parse tree
	 */
	void enterVarValue(BaseRuleParser.VarValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#varValue}.
	 * @param ctx the parse tree
	 */
	void exitVarValue(BaseRuleParser.VarValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#zoneValue}.
	 * @param ctx the parse tree
	 */
	void enterZoneValue(BaseRuleParser.ZoneValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#zoneValue}.
	 * @param ctx the parse tree
	 */
	void exitZoneValue(BaseRuleParser.ZoneValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#numericOnly}.
	 * @param ctx the parse tree
	 */
	void enterNumericOnly(BaseRuleParser.NumericOnlyContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#numericOnly}.
	 * @param ctx the parse tree
	 */
	void exitNumericOnly(BaseRuleParser.NumericOnlyContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#isoLevel}.
	 * @param ctx the parse tree
	 */
	void enterIsoLevel(BaseRuleParser.IsoLevelContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#isoLevel}.
	 * @param ctx the parse tree
	 */
	void exitIsoLevel(BaseRuleParser.IsoLevelContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#columnDef}.
	 * @param ctx the parse tree
	 */
	void enterColumnDef(BaseRuleParser.ColumnDefContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#columnDef}.
	 * @param ctx the parse tree
	 */
	void exitColumnDef(BaseRuleParser.ColumnDefContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#colQualList}.
	 * @param ctx the parse tree
	 */
	void enterColQualList(BaseRuleParser.ColQualListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#colQualList}.
	 * @param ctx the parse tree
	 */
	void exitColQualList(BaseRuleParser.ColQualListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#colConstraint}.
	 * @param ctx the parse tree
	 */
	void enterColConstraint(BaseRuleParser.ColConstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#colConstraint}.
	 * @param ctx the parse tree
	 */
	void exitColConstraint(BaseRuleParser.ColConstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#constraintAttr}.
	 * @param ctx the parse tree
	 */
	void enterConstraintAttr(BaseRuleParser.ConstraintAttrContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#constraintAttr}.
	 * @param ctx the parse tree
	 */
	void exitConstraintAttr(BaseRuleParser.ConstraintAttrContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#colConstraintElem}.
	 * @param ctx the parse tree
	 */
	void enterColConstraintElem(BaseRuleParser.ColConstraintElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#colConstraintElem}.
	 * @param ctx the parse tree
	 */
	void exitColConstraintElem(BaseRuleParser.ColConstraintElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#parenthesizedSeqOptList}.
	 * @param ctx the parse tree
	 */
	void enterParenthesizedSeqOptList(BaseRuleParser.ParenthesizedSeqOptListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#parenthesizedSeqOptList}.
	 * @param ctx the parse tree
	 */
	void exitParenthesizedSeqOptList(BaseRuleParser.ParenthesizedSeqOptListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#seqOptList}.
	 * @param ctx the parse tree
	 */
	void enterSeqOptList(BaseRuleParser.SeqOptListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#seqOptList}.
	 * @param ctx the parse tree
	 */
	void exitSeqOptList(BaseRuleParser.SeqOptListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#seqOptElem}.
	 * @param ctx the parse tree
	 */
	void enterSeqOptElem(BaseRuleParser.SeqOptElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#seqOptElem}.
	 * @param ctx the parse tree
	 */
	void exitSeqOptElem(BaseRuleParser.SeqOptElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#optColumnList}.
	 * @param ctx the parse tree
	 */
	void enterOptColumnList(BaseRuleParser.OptColumnListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#optColumnList}.
	 * @param ctx the parse tree
	 */
	void exitOptColumnList(BaseRuleParser.OptColumnListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#columnElem}.
	 * @param ctx the parse tree
	 */
	void enterColumnElem(BaseRuleParser.ColumnElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#columnElem}.
	 * @param ctx the parse tree
	 */
	void exitColumnElem(BaseRuleParser.ColumnElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#columnList}.
	 * @param ctx the parse tree
	 */
	void enterColumnList(BaseRuleParser.ColumnListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#columnList}.
	 * @param ctx the parse tree
	 */
	void exitColumnList(BaseRuleParser.ColumnListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#generatedWhen}.
	 * @param ctx the parse tree
	 */
	void enterGeneratedWhen(BaseRuleParser.GeneratedWhenContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#generatedWhen}.
	 * @param ctx the parse tree
	 */
	void exitGeneratedWhen(BaseRuleParser.GeneratedWhenContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#noInherit}.
	 * @param ctx the parse tree
	 */
	void enterNoInherit(BaseRuleParser.NoInheritContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#noInherit}.
	 * @param ctx the parse tree
	 */
	void exitNoInherit(BaseRuleParser.NoInheritContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#consTableSpace}.
	 * @param ctx the parse tree
	 */
	void enterConsTableSpace(BaseRuleParser.ConsTableSpaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#consTableSpace}.
	 * @param ctx the parse tree
	 */
	void exitConsTableSpace(BaseRuleParser.ConsTableSpaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#definition}.
	 * @param ctx the parse tree
	 */
	void enterDefinition(BaseRuleParser.DefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#definition}.
	 * @param ctx the parse tree
	 */
	void exitDefinition(BaseRuleParser.DefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#defList}.
	 * @param ctx the parse tree
	 */
	void enterDefList(BaseRuleParser.DefListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#defList}.
	 * @param ctx the parse tree
	 */
	void exitDefList(BaseRuleParser.DefListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#defElem}.
	 * @param ctx the parse tree
	 */
	void enterDefElem(BaseRuleParser.DefElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#defElem}.
	 * @param ctx the parse tree
	 */
	void exitDefElem(BaseRuleParser.DefElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#colLabel}.
	 * @param ctx the parse tree
	 */
	void enterColLabel(BaseRuleParser.ColLabelContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#colLabel}.
	 * @param ctx the parse tree
	 */
	void exitColLabel(BaseRuleParser.ColLabelContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#keyActions}.
	 * @param ctx the parse tree
	 */
	void enterKeyActions(BaseRuleParser.KeyActionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#keyActions}.
	 * @param ctx the parse tree
	 */
	void exitKeyActions(BaseRuleParser.KeyActionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#keyDelete}.
	 * @param ctx the parse tree
	 */
	void enterKeyDelete(BaseRuleParser.KeyDeleteContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#keyDelete}.
	 * @param ctx the parse tree
	 */
	void exitKeyDelete(BaseRuleParser.KeyDeleteContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#keyUpdate}.
	 * @param ctx the parse tree
	 */
	void enterKeyUpdate(BaseRuleParser.KeyUpdateContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#keyUpdate}.
	 * @param ctx the parse tree
	 */
	void exitKeyUpdate(BaseRuleParser.KeyUpdateContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#keyAction}.
	 * @param ctx the parse tree
	 */
	void enterKeyAction(BaseRuleParser.KeyActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#keyAction}.
	 * @param ctx the parse tree
	 */
	void exitKeyAction(BaseRuleParser.KeyActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#keyMatch}.
	 * @param ctx the parse tree
	 */
	void enterKeyMatch(BaseRuleParser.KeyMatchContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#keyMatch}.
	 * @param ctx the parse tree
	 */
	void exitKeyMatch(BaseRuleParser.KeyMatchContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#createGenericOptions}.
	 * @param ctx the parse tree
	 */
	void enterCreateGenericOptions(BaseRuleParser.CreateGenericOptionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#createGenericOptions}.
	 * @param ctx the parse tree
	 */
	void exitCreateGenericOptions(BaseRuleParser.CreateGenericOptionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#genericOptionList}.
	 * @param ctx the parse tree
	 */
	void enterGenericOptionList(BaseRuleParser.GenericOptionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#genericOptionList}.
	 * @param ctx the parse tree
	 */
	void exitGenericOptionList(BaseRuleParser.GenericOptionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#genericOptionElem}.
	 * @param ctx the parse tree
	 */
	void enterGenericOptionElem(BaseRuleParser.GenericOptionElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#genericOptionElem}.
	 * @param ctx the parse tree
	 */
	void exitGenericOptionElem(BaseRuleParser.GenericOptionElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#genericOptionArg}.
	 * @param ctx the parse tree
	 */
	void enterGenericOptionArg(BaseRuleParser.GenericOptionArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#genericOptionArg}.
	 * @param ctx the parse tree
	 */
	void exitGenericOptionArg(BaseRuleParser.GenericOptionArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#genericOptionName}.
	 * @param ctx the parse tree
	 */
	void enterGenericOptionName(BaseRuleParser.GenericOptionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#genericOptionName}.
	 * @param ctx the parse tree
	 */
	void exitGenericOptionName(BaseRuleParser.GenericOptionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#replicaIdentity}.
	 * @param ctx the parse tree
	 */
	void enterReplicaIdentity(BaseRuleParser.ReplicaIdentityContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#replicaIdentity}.
	 * @param ctx the parse tree
	 */
	void exitReplicaIdentity(BaseRuleParser.ReplicaIdentityContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#operArgtypes}.
	 * @param ctx the parse tree
	 */
	void enterOperArgtypes(BaseRuleParser.OperArgtypesContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#operArgtypes}.
	 * @param ctx the parse tree
	 */
	void exitOperArgtypes(BaseRuleParser.OperArgtypesContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#funcArg}.
	 * @param ctx the parse tree
	 */
	void enterFuncArg(BaseRuleParser.FuncArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#funcArg}.
	 * @param ctx the parse tree
	 */
	void exitFuncArg(BaseRuleParser.FuncArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#argClass}.
	 * @param ctx the parse tree
	 */
	void enterArgClass(BaseRuleParser.ArgClassContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#argClass}.
	 * @param ctx the parse tree
	 */
	void exitArgClass(BaseRuleParser.ArgClassContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#funcArgsList}.
	 * @param ctx the parse tree
	 */
	void enterFuncArgsList(BaseRuleParser.FuncArgsListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#funcArgsList}.
	 * @param ctx the parse tree
	 */
	void exitFuncArgsList(BaseRuleParser.FuncArgsListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#nonReservedWordOrSconst}.
	 * @param ctx the parse tree
	 */
	void enterNonReservedWordOrSconst(BaseRuleParser.NonReservedWordOrSconstContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#nonReservedWordOrSconst}.
	 * @param ctx the parse tree
	 */
	void exitNonReservedWordOrSconst(BaseRuleParser.NonReservedWordOrSconstContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#fileName}.
	 * @param ctx the parse tree
	 */
	void enterFileName(BaseRuleParser.FileNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#fileName}.
	 * @param ctx the parse tree
	 */
	void exitFileName(BaseRuleParser.FileNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#roleList}.
	 * @param ctx the parse tree
	 */
	void enterRoleList(BaseRuleParser.RoleListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#roleList}.
	 * @param ctx the parse tree
	 */
	void exitRoleList(BaseRuleParser.RoleListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#setResetClause}.
	 * @param ctx the parse tree
	 */
	void enterSetResetClause(BaseRuleParser.SetResetClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#setResetClause}.
	 * @param ctx the parse tree
	 */
	void exitSetResetClause(BaseRuleParser.SetResetClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#setRest}.
	 * @param ctx the parse tree
	 */
	void enterSetRest(BaseRuleParser.SetRestContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#setRest}.
	 * @param ctx the parse tree
	 */
	void exitSetRest(BaseRuleParser.SetRestContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#transactionModeList}.
	 * @param ctx the parse tree
	 */
	void enterTransactionModeList(BaseRuleParser.TransactionModeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#transactionModeList}.
	 * @param ctx the parse tree
	 */
	void exitTransactionModeList(BaseRuleParser.TransactionModeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#transactionModeItem}.
	 * @param ctx the parse tree
	 */
	void enterTransactionModeItem(BaseRuleParser.TransactionModeItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#transactionModeItem}.
	 * @param ctx the parse tree
	 */
	void exitTransactionModeItem(BaseRuleParser.TransactionModeItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#setRestMore}.
	 * @param ctx the parse tree
	 */
	void enterSetRestMore(BaseRuleParser.SetRestMoreContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#setRestMore}.
	 * @param ctx the parse tree
	 */
	void exitSetRestMore(BaseRuleParser.SetRestMoreContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#encoding}.
	 * @param ctx the parse tree
	 */
	void enterEncoding(BaseRuleParser.EncodingContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#encoding}.
	 * @param ctx the parse tree
	 */
	void exitEncoding(BaseRuleParser.EncodingContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#genericSet}.
	 * @param ctx the parse tree
	 */
	void enterGenericSet(BaseRuleParser.GenericSetContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#genericSet}.
	 * @param ctx the parse tree
	 */
	void exitGenericSet(BaseRuleParser.GenericSetContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#variableResetStmt}.
	 * @param ctx the parse tree
	 */
	void enterVariableResetStmt(BaseRuleParser.VariableResetStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#variableResetStmt}.
	 * @param ctx the parse tree
	 */
	void exitVariableResetStmt(BaseRuleParser.VariableResetStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#resetRest}.
	 * @param ctx the parse tree
	 */
	void enterResetRest(BaseRuleParser.ResetRestContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#resetRest}.
	 * @param ctx the parse tree
	 */
	void exitResetRest(BaseRuleParser.ResetRestContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#genericReset}.
	 * @param ctx the parse tree
	 */
	void enterGenericReset(BaseRuleParser.GenericResetContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#genericReset}.
	 * @param ctx the parse tree
	 */
	void exitGenericReset(BaseRuleParser.GenericResetContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#relationExprList}.
	 * @param ctx the parse tree
	 */
	void enterRelationExprList(BaseRuleParser.RelationExprListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#relationExprList}.
	 * @param ctx the parse tree
	 */
	void exitRelationExprList(BaseRuleParser.RelationExprListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#relationExpr}.
	 * @param ctx the parse tree
	 */
	void enterRelationExpr(BaseRuleParser.RelationExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#relationExpr}.
	 * @param ctx the parse tree
	 */
	void exitRelationExpr(BaseRuleParser.RelationExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#commonFuncOptItem}.
	 * @param ctx the parse tree
	 */
	void enterCommonFuncOptItem(BaseRuleParser.CommonFuncOptItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#commonFuncOptItem}.
	 * @param ctx the parse tree
	 */
	void exitCommonFuncOptItem(BaseRuleParser.CommonFuncOptItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#functionSetResetClause}.
	 * @param ctx the parse tree
	 */
	void enterFunctionSetResetClause(BaseRuleParser.FunctionSetResetClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#functionSetResetClause}.
	 * @param ctx the parse tree
	 */
	void exitFunctionSetResetClause(BaseRuleParser.FunctionSetResetClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#rowSecurityCmd}.
	 * @param ctx the parse tree
	 */
	void enterRowSecurityCmd(BaseRuleParser.RowSecurityCmdContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#rowSecurityCmd}.
	 * @param ctx the parse tree
	 */
	void exitRowSecurityCmd(BaseRuleParser.RowSecurityCmdContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#event}.
	 * @param ctx the parse tree
	 */
	void enterEvent(BaseRuleParser.EventContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#event}.
	 * @param ctx the parse tree
	 */
	void exitEvent(BaseRuleParser.EventContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#typeNameList}.
	 * @param ctx the parse tree
	 */
	void enterTypeNameList(BaseRuleParser.TypeNameListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#typeNameList}.
	 * @param ctx the parse tree
	 */
	void exitTypeNameList(BaseRuleParser.TypeNameListContext ctx);
}