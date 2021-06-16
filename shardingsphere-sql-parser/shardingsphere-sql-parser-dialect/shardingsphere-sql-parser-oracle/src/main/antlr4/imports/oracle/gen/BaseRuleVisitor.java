// Generated from /home/totalo/code/shardingsphere/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-oracle/src/main/antlr4/imports/oracle/BaseRule.g4 by ANTLR 4.9.1
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
	 * Visit a parse tree produced by {@link BaseRuleParser#viewName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitViewName(BaseRuleParser.ViewNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#columnName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnName(BaseRuleParser.ColumnNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#objectName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectName(BaseRuleParser.ObjectNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#clusterName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClusterName(BaseRuleParser.ClusterNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#indexName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexName(BaseRuleParser.IndexNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#constraintName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintName(BaseRuleParser.ConstraintNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#savepointName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSavepointName(BaseRuleParser.SavepointNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#synonymName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSynonymName(BaseRuleParser.SynonymNameContext ctx);
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
	 * Visit a parse tree produced by {@link BaseRuleParser#tablespaceName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablespaceName(BaseRuleParser.TablespaceNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#tablespaceSetName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablespaceSetName(BaseRuleParser.TablespaceSetNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#serviceName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitServiceName(BaseRuleParser.ServiceNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#ilmPolicyName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIlmPolicyName(BaseRuleParser.IlmPolicyNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#functionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionName(BaseRuleParser.FunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#dbLink}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDbLink(BaseRuleParser.DbLinkContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#parameterValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterValue(BaseRuleParser.ParameterValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#directoryName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirectoryName(BaseRuleParser.DirectoryNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#dispatcherName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDispatcherName(BaseRuleParser.DispatcherNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#clientId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClientId(BaseRuleParser.ClientIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#opaqueFormatSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpaqueFormatSpec(BaseRuleParser.OpaqueFormatSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#accessDriverType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAccessDriverType(BaseRuleParser.AccessDriverTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType(BaseRuleParser.TypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#varrayItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarrayItem(BaseRuleParser.VarrayItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#nestedItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNestedItem(BaseRuleParser.NestedItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#storageTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStorageTable(BaseRuleParser.StorageTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#lobSegname}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLobSegname(BaseRuleParser.LobSegnameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#locationSpecifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLocationSpecifier(BaseRuleParser.LocationSpecifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#xmlSchemaURLName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlSchemaURLName(BaseRuleParser.XmlSchemaURLNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#elementName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElementName(BaseRuleParser.ElementNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#subpartitionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubpartitionName(BaseRuleParser.SubpartitionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#parameterName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterName(BaseRuleParser.ParameterNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#editionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEditionName(BaseRuleParser.EditionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#containerName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContainerName(BaseRuleParser.ContainerNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#partitionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionName(BaseRuleParser.PartitionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#partitionSetName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionSetName(BaseRuleParser.PartitionSetNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#partitionKeyValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionKeyValue(BaseRuleParser.PartitionKeyValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#subpartitionKeyValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubpartitionKeyValue(BaseRuleParser.SubpartitionKeyValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#zonemapName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitZonemapName(BaseRuleParser.ZonemapNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#flashbackArchiveName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFlashbackArchiveName(BaseRuleParser.FlashbackArchiveNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#roleName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoleName(BaseRuleParser.RoleNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#password}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPassword(BaseRuleParser.PasswordContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#logGroupName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogGroupName(BaseRuleParser.LogGroupNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#columnNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNames(BaseRuleParser.ColumnNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#tableNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableNames(BaseRuleParser.TableNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#oracleId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOracleId(BaseRuleParser.OracleIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#collationName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollationName(BaseRuleParser.CollationNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#columnCollationName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnCollationName(BaseRuleParser.ColumnCollationNameContext ctx);
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
	 * Visit a parse tree produced by {@link BaseRuleParser#exprs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprs(BaseRuleParser.ExprsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#exprList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprList(BaseRuleParser.ExprListContext ctx);
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
	 * Visit a parse tree produced by {@link BaseRuleParser#attributeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeName(BaseRuleParser.AttributeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#indexTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexTypeName(BaseRuleParser.IndexTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#simpleExprs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleExprs(BaseRuleParser.SimpleExprsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#lobItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLobItem(BaseRuleParser.LobItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#lobItems}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLobItems(BaseRuleParser.LobItemsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#lobItemList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLobItemList(BaseRuleParser.LobItemListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataType(BaseRuleParser.DataTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#specialDatatype}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecialDatatype(BaseRuleParser.SpecialDatatypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#dataTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataTypeName(BaseRuleParser.DataTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#datetimeTypeSuffix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDatetimeTypeSuffix(BaseRuleParser.DatetimeTypeSuffixContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#treatFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTreatFunction(BaseRuleParser.TreatFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#privateExprOfDb}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivateExprOfDb(BaseRuleParser.PrivateExprOfDbContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#caseExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseExpr(BaseRuleParser.CaseExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#simpleCaseExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleCaseExpr(BaseRuleParser.SimpleCaseExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#searchedCaseExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearchedCaseExpr(BaseRuleParser.SearchedCaseExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#elseClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElseClause(BaseRuleParser.ElseClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#intervalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalExpression(BaseRuleParser.IntervalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#objectAccessExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectAccessExpression(BaseRuleParser.ObjectAccessExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#constructorExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstructorExpr(BaseRuleParser.ConstructorExprContext ctx);
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
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#hashSubpartitionQuantity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHashSubpartitionQuantity(BaseRuleParser.HashSubpartitionQuantityContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#odciParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOdciParameters(BaseRuleParser.OdciParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#databaseName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDatabaseName(BaseRuleParser.DatabaseNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#locationName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLocationName(BaseRuleParser.LocationNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#fileName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFileName(BaseRuleParser.FileNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#asmFileName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsmFileName(BaseRuleParser.AsmFileNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#fileNumber}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFileNumber(BaseRuleParser.FileNumberContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#instanceName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceName(BaseRuleParser.InstanceNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#logminerSessionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogminerSessionName(BaseRuleParser.LogminerSessionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#tablespaceGroupName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablespaceGroupName(BaseRuleParser.TablespaceGroupNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#copyName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopyName(BaseRuleParser.CopyNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#mirrorName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMirrorName(BaseRuleParser.MirrorNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#uriString}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUriString(BaseRuleParser.UriStringContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#qualifiedCredentialName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedCredentialName(BaseRuleParser.QualifiedCredentialNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#pdbName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPdbName(BaseRuleParser.PdbNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#diskgroupName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDiskgroupName(BaseRuleParser.DiskgroupNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#templateName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTemplateName(BaseRuleParser.TemplateNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#aliasName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAliasName(BaseRuleParser.AliasNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#domain}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDomain(BaseRuleParser.DomainContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#dateValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateValue(BaseRuleParser.DateValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#sessionId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSessionId(BaseRuleParser.SessionIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#serialNumber}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSerialNumber(BaseRuleParser.SerialNumberContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#instanceId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstanceId(BaseRuleParser.InstanceIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#sqlId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSqlId(BaseRuleParser.SqlIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#logFileName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogFileName(BaseRuleParser.LogFileNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#logFileGroupsArchivedLocationName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogFileGroupsArchivedLocationName(BaseRuleParser.LogFileGroupsArchivedLocationNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#asmVersion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsmVersion(BaseRuleParser.AsmVersionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#walletPassword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWalletPassword(BaseRuleParser.WalletPasswordContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#hsmAuthString}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHsmAuthString(BaseRuleParser.HsmAuthStringContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#targetDbName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTargetDbName(BaseRuleParser.TargetDbNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#certificateId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCertificateId(BaseRuleParser.CertificateIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#categoryName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCategoryName(BaseRuleParser.CategoryNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#offset}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOffset(BaseRuleParser.OffsetContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#rowcount}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowcount(BaseRuleParser.RowcountContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#percent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPercent(BaseRuleParser.PercentContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#rollbackSegment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRollbackSegment(BaseRuleParser.RollbackSegmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#queryName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryName(BaseRuleParser.QueryNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#cycleValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCycleValue(BaseRuleParser.CycleValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#noCycleValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNoCycleValue(BaseRuleParser.NoCycleValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#orderingColumn}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderingColumn(BaseRuleParser.OrderingColumnContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#subavName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubavName(BaseRuleParser.SubavNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#baseAvName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBaseAvName(BaseRuleParser.BaseAvNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#measName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMeasName(BaseRuleParser.MeasNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#levelRef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLevelRef(BaseRuleParser.LevelRefContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#offsetExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOffsetExpr(BaseRuleParser.OffsetExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#memberKeyExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberKeyExpr(BaseRuleParser.MemberKeyExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#depthExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDepthExpression(BaseRuleParser.DepthExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#unitName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnitName(BaseRuleParser.UnitNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BaseRuleParser#procedureName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcedureName(BaseRuleParser.ProcedureNameContext ctx);
}