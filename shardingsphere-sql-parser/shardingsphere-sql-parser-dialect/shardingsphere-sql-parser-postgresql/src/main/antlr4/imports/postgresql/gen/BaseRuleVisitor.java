// Generated from /home/totalo/code/shardingsphere/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-postgresql/src/main/antlr4/imports/postgresql/BaseRule.g4 by ANTLR 4.9.1
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
	 * Visit a parse tree produced by {@link BaseRuleParser#reservedKeyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReservedKeyword(BaseRuleParser.ReservedKeywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#numberLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberLiterals(BaseRuleParser.NumberLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#literalsType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteralsType(BaseRuleParser.LiteralsTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(BaseRuleParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#unicodeEscapes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnicodeEscapes(BaseRuleParser.UnicodeEscapesContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#uescape}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUescape(BaseRuleParser.UescapeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#unreservedWord}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnreservedWord(BaseRuleParser.UnreservedWordContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#typeFuncNameKeyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeFuncNameKeyword(BaseRuleParser.TypeFuncNameKeywordContext ctx);
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
	 * Visit a parse tree produced by {@link BaseRuleParser#tableNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableNames(BaseRuleParser.TableNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#columnNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNames(BaseRuleParser.ColumnNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#collationName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollationName(BaseRuleParser.CollationNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#indexName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexName(BaseRuleParser.IndexNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlias(BaseRuleParser.AliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#primaryKey}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryKey(BaseRuleParser.PrimaryKeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#logicalOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOperator(BaseRuleParser.LogicalOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOperator(BaseRuleParser.ComparisonOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#patternMatchingOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPatternMatchingOperator(BaseRuleParser.PatternMatchingOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#cursorName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCursorName(BaseRuleParser.CursorNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#aExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAExpr(BaseRuleParser.AExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#bExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBExpr(BaseRuleParser.BExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#cExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCExpr(BaseRuleParser.CExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#indirection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndirection(BaseRuleParser.IndirectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#optIndirection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptIndirection(BaseRuleParser.OptIndirectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#indirectionEl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndirectionEl(BaseRuleParser.IndirectionElContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#sliceBound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSliceBound(BaseRuleParser.SliceBoundContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#inExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInExpr(BaseRuleParser.InExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#caseExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseExpr(BaseRuleParser.CaseExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#whenClauseList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhenClauseList(BaseRuleParser.WhenClauseListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#whenClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhenClause(BaseRuleParser.WhenClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#caseDefault}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseDefault(BaseRuleParser.CaseDefaultContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#caseArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseArg(BaseRuleParser.CaseArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#columnref}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnref(BaseRuleParser.ColumnrefContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#qualOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualOp(BaseRuleParser.QualOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#subqueryOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubqueryOp(BaseRuleParser.SubqueryOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#allOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAllOp(BaseRuleParser.AllOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#op}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOp(BaseRuleParser.OpContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#mathOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathOperator(BaseRuleParser.MathOperatorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonExtract}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonExtract(BaseRuleParser.JsonExtractContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonExtractText}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonExtractText(BaseRuleParser.JsonExtractTextContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonPathExtract}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonPathExtract(BaseRuleParser.JsonPathExtractContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonPathExtractText}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonPathExtractText(BaseRuleParser.JsonPathExtractTextContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbContainRight}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbContainRight(BaseRuleParser.JsonbContainRightContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbContainLeft}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbContainLeft(BaseRuleParser.JsonbContainLeftContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbContainTopKey}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbContainTopKey(BaseRuleParser.JsonbContainTopKeyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbContainAnyTopKey}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbContainAnyTopKey(BaseRuleParser.JsonbContainAnyTopKeyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbContainAllTopKey}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbContainAllTopKey(BaseRuleParser.JsonbContainAllTopKeyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbConcat}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbConcat(BaseRuleParser.JsonbConcatContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbDelete}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbDelete(BaseRuleParser.JsonbDeleteContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbPathDelete}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbPathDelete(BaseRuleParser.JsonbPathDeleteContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbPathContainAnyValue}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbPathContainAnyValue(BaseRuleParser.JsonbPathContainAnyValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbPathPredicateCheck}
	 * labeled alternative in {@link BaseRuleParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbPathPredicateCheck(BaseRuleParser.JsonbPathPredicateCheckContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#qualAllOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualAllOp(BaseRuleParser.QualAllOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#ascDesc}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAscDesc(BaseRuleParser.AscDescContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#anyOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnyOperator(BaseRuleParser.AnyOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#frameClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrameClause(BaseRuleParser.FrameClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#frameExtent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrameExtent(BaseRuleParser.FrameExtentContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#frameBound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrameBound(BaseRuleParser.FrameBoundContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#windowExclusionClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowExclusionClause(BaseRuleParser.WindowExclusionClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#row}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRow(BaseRuleParser.RowContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#explicitRow}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplicitRow(BaseRuleParser.ExplicitRowContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#implicitRow}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImplicitRow(BaseRuleParser.ImplicitRowContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#subType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubType(BaseRuleParser.SubTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#arrayExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayExpr(BaseRuleParser.ArrayExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#arrayExprList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayExprList(BaseRuleParser.ArrayExprListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#funcArgList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncArgList(BaseRuleParser.FuncArgListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#paramName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParamName(BaseRuleParser.ParamNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#funcArgExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncArgExpr(BaseRuleParser.FuncArgExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#typeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeList(BaseRuleParser.TypeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#funcApplication}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncApplication(BaseRuleParser.FuncApplicationContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#funcName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncName(BaseRuleParser.FuncNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#aexprConst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAexprConst(BaseRuleParser.AexprConstContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#qualifiedName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedName(BaseRuleParser.QualifiedNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#colId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColId(BaseRuleParser.ColIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#typeFunctionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeFunctionName(BaseRuleParser.TypeFunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#functionTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionTable(BaseRuleParser.FunctionTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#xmlTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlTable(BaseRuleParser.XmlTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#xmlTableColumnList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlTableColumnList(BaseRuleParser.XmlTableColumnListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#xmlTableColumnEl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlTableColumnEl(BaseRuleParser.XmlTableColumnElContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#xmlTableColumnOptionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlTableColumnOptionList(BaseRuleParser.XmlTableColumnOptionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#xmlTableColumnOptionEl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlTableColumnOptionEl(BaseRuleParser.XmlTableColumnOptionElContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#xmlNamespaceList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlNamespaceList(BaseRuleParser.XmlNamespaceListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#xmlNamespaceEl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlNamespaceEl(BaseRuleParser.XmlNamespaceElContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#funcExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncExpr(BaseRuleParser.FuncExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#withinGroupClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithinGroupClause(BaseRuleParser.WithinGroupClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#filterClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFilterClause(BaseRuleParser.FilterClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#functionExprWindowless}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionExprWindowless(BaseRuleParser.FunctionExprWindowlessContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#ordinality}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrdinality(BaseRuleParser.OrdinalityContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#functionExprCommonSubexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionExprCommonSubexpr(BaseRuleParser.FunctionExprCommonSubexprContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#typeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeName(BaseRuleParser.TypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#simpleTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleTypeName(BaseRuleParser.SimpleTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#exprList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprList(BaseRuleParser.ExprListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#extractList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtractList(BaseRuleParser.ExtractListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#extractArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtractArg(BaseRuleParser.ExtractArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#genericType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericType(BaseRuleParser.GenericTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#typeModifiers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeModifiers(BaseRuleParser.TypeModifiersContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#numeric}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumeric(BaseRuleParser.NumericContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#constDatetime}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstDatetime(BaseRuleParser.ConstDatetimeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#timezone}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimezone(BaseRuleParser.TimezoneContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#character}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacter(BaseRuleParser.CharacterContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#characterWithLength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterWithLength(BaseRuleParser.CharacterWithLengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#characterWithoutLength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterWithoutLength(BaseRuleParser.CharacterWithoutLengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#characterClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterClause(BaseRuleParser.CharacterClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#optFloat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptFloat(BaseRuleParser.OptFloatContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#attrs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttrs(BaseRuleParser.AttrsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#attrName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttrName(BaseRuleParser.AttrNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#colLable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColLable(BaseRuleParser.ColLableContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#bit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBit(BaseRuleParser.BitContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#bitWithLength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitWithLength(BaseRuleParser.BitWithLengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#bitWithoutLength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitWithoutLength(BaseRuleParser.BitWithoutLengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#constInterval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstInterval(BaseRuleParser.ConstIntervalContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#optInterval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptInterval(BaseRuleParser.OptIntervalContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#optArrayBounds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptArrayBounds(BaseRuleParser.OptArrayBoundsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#intervalSecond}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalSecond(BaseRuleParser.IntervalSecondContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#unicodeNormalForm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnicodeNormalForm(BaseRuleParser.UnicodeNormalFormContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#trimList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrimList(BaseRuleParser.TrimListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#overlayList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOverlayList(BaseRuleParser.OverlayListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#overlayPlacing}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOverlayPlacing(BaseRuleParser.OverlayPlacingContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#substrFrom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubstrFrom(BaseRuleParser.SubstrFromContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#substrFor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubstrFor(BaseRuleParser.SubstrForContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#positionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPositionList(BaseRuleParser.PositionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#substrList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubstrList(BaseRuleParser.SubstrListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#xmlAttributes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlAttributes(BaseRuleParser.XmlAttributesContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#xmlAttributeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlAttributeList(BaseRuleParser.XmlAttributeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#xmlAttributeEl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlAttributeEl(BaseRuleParser.XmlAttributeElContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#xmlExistsArgument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlExistsArgument(BaseRuleParser.XmlExistsArgumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#xmlPassingMech}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlPassingMech(BaseRuleParser.XmlPassingMechContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#documentOrContent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocumentOrContent(BaseRuleParser.DocumentOrContentContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#xmlWhitespaceOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlWhitespaceOption(BaseRuleParser.XmlWhitespaceOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#xmlRootVersion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlRootVersion(BaseRuleParser.XmlRootVersionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#xmlRootStandalone}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlRootStandalone(BaseRuleParser.XmlRootStandaloneContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#rowsFromItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowsFromItem(BaseRuleParser.RowsFromItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#rowsFromList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowsFromList(BaseRuleParser.RowsFromListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#columnDefList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnDefList(BaseRuleParser.ColumnDefListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#tableFuncElementList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableFuncElementList(BaseRuleParser.TableFuncElementListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#tableFuncElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableFuncElement(BaseRuleParser.TableFuncElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#collateClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollateClause(BaseRuleParser.CollateClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#anyName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnyName(BaseRuleParser.AnyNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#aliasClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAliasClause(BaseRuleParser.AliasClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#nameList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNameList(BaseRuleParser.NameListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#funcAliasClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncAliasClause(BaseRuleParser.FuncAliasClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#tablesampleClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablesampleClause(BaseRuleParser.TablesampleClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#repeatableClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRepeatableClause(BaseRuleParser.RepeatableClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#allOrDistinct}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAllOrDistinct(BaseRuleParser.AllOrDistinctContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#sortClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSortClause(BaseRuleParser.SortClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#sortbyList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSortbyList(BaseRuleParser.SortbyListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#sortby}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSortby(BaseRuleParser.SortbyContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#nullsOrder}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullsOrder(BaseRuleParser.NullsOrderContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#distinctClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDistinctClause(BaseRuleParser.DistinctClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#distinct}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDistinct(BaseRuleParser.DistinctContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#overClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOverClause(BaseRuleParser.OverClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#windowSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowSpecification(BaseRuleParser.WindowSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#windowName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowName(BaseRuleParser.WindowNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#partitionClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionClause(BaseRuleParser.PartitionClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#indexParams}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexParams(BaseRuleParser.IndexParamsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#indexElemOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexElemOptions(BaseRuleParser.IndexElemOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#indexElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexElem(BaseRuleParser.IndexElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#collate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollate(BaseRuleParser.CollateContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#optClass}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptClass(BaseRuleParser.OptClassContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#reloptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReloptions(BaseRuleParser.ReloptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#reloptionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReloptionList(BaseRuleParser.ReloptionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#reloptionElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReloptionElem(BaseRuleParser.ReloptionElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#defArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefArg(BaseRuleParser.DefArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#funcType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncType(BaseRuleParser.FuncTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#selectWithParens}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectWithParens(BaseRuleParser.SelectWithParensContext ctx);
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
	 * Visit a parse tree produced by {@link BaseRuleParser#dataTypeLength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataTypeLength(BaseRuleParser.DataTypeLengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#characterSet}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterSet(BaseRuleParser.CharacterSetContext ctx);
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
	 * Visit a parse tree produced by {@link BaseRuleParser#signedIconst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSignedIconst(BaseRuleParser.SignedIconstContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#booleanOrString}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanOrString(BaseRuleParser.BooleanOrStringContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#nonReservedWord}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonReservedWord(BaseRuleParser.NonReservedWordContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#colNameKeyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColNameKeyword(BaseRuleParser.ColNameKeywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#databaseName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDatabaseName(BaseRuleParser.DatabaseNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#roleSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoleSpec(BaseRuleParser.RoleSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#varName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarName(BaseRuleParser.VarNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#varList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarList(BaseRuleParser.VarListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#varValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarValue(BaseRuleParser.VarValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#zoneValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitZoneValue(BaseRuleParser.ZoneValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#numericOnly}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericOnly(BaseRuleParser.NumericOnlyContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#isoLevel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsoLevel(BaseRuleParser.IsoLevelContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#columnDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnDef(BaseRuleParser.ColumnDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#colQualList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColQualList(BaseRuleParser.ColQualListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#colConstraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColConstraint(BaseRuleParser.ColConstraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#constraintAttr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintAttr(BaseRuleParser.ConstraintAttrContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#colConstraintElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColConstraintElem(BaseRuleParser.ColConstraintElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#parenthesizedSeqOptList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenthesizedSeqOptList(BaseRuleParser.ParenthesizedSeqOptListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#seqOptList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSeqOptList(BaseRuleParser.SeqOptListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#seqOptElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSeqOptElem(BaseRuleParser.SeqOptElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#optColumnList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptColumnList(BaseRuleParser.OptColumnListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#columnElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnElem(BaseRuleParser.ColumnElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#columnList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnList(BaseRuleParser.ColumnListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#generatedWhen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGeneratedWhen(BaseRuleParser.GeneratedWhenContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#noInherit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNoInherit(BaseRuleParser.NoInheritContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#consTableSpace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConsTableSpace(BaseRuleParser.ConsTableSpaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefinition(BaseRuleParser.DefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#defList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefList(BaseRuleParser.DefListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#defElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefElem(BaseRuleParser.DefElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#colLabel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColLabel(BaseRuleParser.ColLabelContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#keyActions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyActions(BaseRuleParser.KeyActionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#keyDelete}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyDelete(BaseRuleParser.KeyDeleteContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#keyUpdate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyUpdate(BaseRuleParser.KeyUpdateContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#keyAction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyAction(BaseRuleParser.KeyActionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#keyMatch}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyMatch(BaseRuleParser.KeyMatchContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#createGenericOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateGenericOptions(BaseRuleParser.CreateGenericOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#genericOptionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericOptionList(BaseRuleParser.GenericOptionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#genericOptionElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericOptionElem(BaseRuleParser.GenericOptionElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#genericOptionArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericOptionArg(BaseRuleParser.GenericOptionArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#genericOptionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericOptionName(BaseRuleParser.GenericOptionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#replicaIdentity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReplicaIdentity(BaseRuleParser.ReplicaIdentityContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#operArgtypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperArgtypes(BaseRuleParser.OperArgtypesContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#funcArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncArg(BaseRuleParser.FuncArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#argClass}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgClass(BaseRuleParser.ArgClassContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#funcArgsList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncArgsList(BaseRuleParser.FuncArgsListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#nonReservedWordOrSconst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonReservedWordOrSconst(BaseRuleParser.NonReservedWordOrSconstContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#fileName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFileName(BaseRuleParser.FileNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#roleList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoleList(BaseRuleParser.RoleListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#setResetClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetResetClause(BaseRuleParser.SetResetClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#setRest}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetRest(BaseRuleParser.SetRestContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#transactionModeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransactionModeList(BaseRuleParser.TransactionModeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#transactionModeItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransactionModeItem(BaseRuleParser.TransactionModeItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#setRestMore}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetRestMore(BaseRuleParser.SetRestMoreContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#encoding}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEncoding(BaseRuleParser.EncodingContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#genericSet}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericSet(BaseRuleParser.GenericSetContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#variableResetStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableResetStmt(BaseRuleParser.VariableResetStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#resetRest}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResetRest(BaseRuleParser.ResetRestContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#genericReset}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericReset(BaseRuleParser.GenericResetContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#relationExprList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationExprList(BaseRuleParser.RelationExprListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#relationExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationExpr(BaseRuleParser.RelationExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#commonFuncOptItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommonFuncOptItem(BaseRuleParser.CommonFuncOptItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#functionSetResetClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionSetResetClause(BaseRuleParser.FunctionSetResetClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#rowSecurityCmd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowSecurityCmd(BaseRuleParser.RowSecurityCmdContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#event}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEvent(BaseRuleParser.EventContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#typeNameList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeNameList(BaseRuleParser.TypeNameListContext ctx);
}