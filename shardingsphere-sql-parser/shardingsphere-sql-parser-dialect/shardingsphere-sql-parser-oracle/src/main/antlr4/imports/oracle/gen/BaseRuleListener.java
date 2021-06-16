// Generated from /home/totalo/code/shardingsphere/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-oracle/src/main/antlr4/imports/oracle/BaseRule.g4 by ANTLR 4.9.1
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
	 * Enter a parse tree produced by {@link BaseRuleParser#viewName}.
	 * @param ctx the parse tree
	 */
	void enterViewName(BaseRuleParser.ViewNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#viewName}.
	 * @param ctx the parse tree
	 */
	void exitViewName(BaseRuleParser.ViewNameContext ctx);
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
	 * Enter a parse tree produced by {@link BaseRuleParser#objectName}.
	 * @param ctx the parse tree
	 */
	void enterObjectName(BaseRuleParser.ObjectNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#objectName}.
	 * @param ctx the parse tree
	 */
	void exitObjectName(BaseRuleParser.ObjectNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#clusterName}.
	 * @param ctx the parse tree
	 */
	void enterClusterName(BaseRuleParser.ClusterNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#clusterName}.
	 * @param ctx the parse tree
	 */
	void exitClusterName(BaseRuleParser.ClusterNameContext ctx);
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
	 * Enter a parse tree produced by {@link BaseRuleParser#constraintName}.
	 * @param ctx the parse tree
	 */
	void enterConstraintName(BaseRuleParser.ConstraintNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#constraintName}.
	 * @param ctx the parse tree
	 */
	void exitConstraintName(BaseRuleParser.ConstraintNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#savepointName}.
	 * @param ctx the parse tree
	 */
	void enterSavepointName(BaseRuleParser.SavepointNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#savepointName}.
	 * @param ctx the parse tree
	 */
	void exitSavepointName(BaseRuleParser.SavepointNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#synonymName}.
	 * @param ctx the parse tree
	 */
	void enterSynonymName(BaseRuleParser.SynonymNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#synonymName}.
	 * @param ctx the parse tree
	 */
	void exitSynonymName(BaseRuleParser.SynonymNameContext ctx);
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
	 * Enter a parse tree produced by {@link BaseRuleParser#tablespaceName}.
	 * @param ctx the parse tree
	 */
	void enterTablespaceName(BaseRuleParser.TablespaceNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#tablespaceName}.
	 * @param ctx the parse tree
	 */
	void exitTablespaceName(BaseRuleParser.TablespaceNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#tablespaceSetName}.
	 * @param ctx the parse tree
	 */
	void enterTablespaceSetName(BaseRuleParser.TablespaceSetNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#tablespaceSetName}.
	 * @param ctx the parse tree
	 */
	void exitTablespaceSetName(BaseRuleParser.TablespaceSetNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#serviceName}.
	 * @param ctx the parse tree
	 */
	void enterServiceName(BaseRuleParser.ServiceNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#serviceName}.
	 * @param ctx the parse tree
	 */
	void exitServiceName(BaseRuleParser.ServiceNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#ilmPolicyName}.
	 * @param ctx the parse tree
	 */
	void enterIlmPolicyName(BaseRuleParser.IlmPolicyNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#ilmPolicyName}.
	 * @param ctx the parse tree
	 */
	void exitIlmPolicyName(BaseRuleParser.IlmPolicyNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#functionName}.
	 * @param ctx the parse tree
	 */
	void enterFunctionName(BaseRuleParser.FunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#functionName}.
	 * @param ctx the parse tree
	 */
	void exitFunctionName(BaseRuleParser.FunctionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#dbLink}.
	 * @param ctx the parse tree
	 */
	void enterDbLink(BaseRuleParser.DbLinkContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#dbLink}.
	 * @param ctx the parse tree
	 */
	void exitDbLink(BaseRuleParser.DbLinkContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#parameterValue}.
	 * @param ctx the parse tree
	 */
	void enterParameterValue(BaseRuleParser.ParameterValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#parameterValue}.
	 * @param ctx the parse tree
	 */
	void exitParameterValue(BaseRuleParser.ParameterValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#directoryName}.
	 * @param ctx the parse tree
	 */
	void enterDirectoryName(BaseRuleParser.DirectoryNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#directoryName}.
	 * @param ctx the parse tree
	 */
	void exitDirectoryName(BaseRuleParser.DirectoryNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#dispatcherName}.
	 * @param ctx the parse tree
	 */
	void enterDispatcherName(BaseRuleParser.DispatcherNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#dispatcherName}.
	 * @param ctx the parse tree
	 */
	void exitDispatcherName(BaseRuleParser.DispatcherNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#clientId}.
	 * @param ctx the parse tree
	 */
	void enterClientId(BaseRuleParser.ClientIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#clientId}.
	 * @param ctx the parse tree
	 */
	void exitClientId(BaseRuleParser.ClientIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#opaqueFormatSpec}.
	 * @param ctx the parse tree
	 */
	void enterOpaqueFormatSpec(BaseRuleParser.OpaqueFormatSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#opaqueFormatSpec}.
	 * @param ctx the parse tree
	 */
	void exitOpaqueFormatSpec(BaseRuleParser.OpaqueFormatSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#accessDriverType}.
	 * @param ctx the parse tree
	 */
	void enterAccessDriverType(BaseRuleParser.AccessDriverTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#accessDriverType}.
	 * @param ctx the parse tree
	 */
	void exitAccessDriverType(BaseRuleParser.AccessDriverTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(BaseRuleParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(BaseRuleParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#varrayItem}.
	 * @param ctx the parse tree
	 */
	void enterVarrayItem(BaseRuleParser.VarrayItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#varrayItem}.
	 * @param ctx the parse tree
	 */
	void exitVarrayItem(BaseRuleParser.VarrayItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#nestedItem}.
	 * @param ctx the parse tree
	 */
	void enterNestedItem(BaseRuleParser.NestedItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#nestedItem}.
	 * @param ctx the parse tree
	 */
	void exitNestedItem(BaseRuleParser.NestedItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#storageTable}.
	 * @param ctx the parse tree
	 */
	void enterStorageTable(BaseRuleParser.StorageTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#storageTable}.
	 * @param ctx the parse tree
	 */
	void exitStorageTable(BaseRuleParser.StorageTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#lobSegname}.
	 * @param ctx the parse tree
	 */
	void enterLobSegname(BaseRuleParser.LobSegnameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#lobSegname}.
	 * @param ctx the parse tree
	 */
	void exitLobSegname(BaseRuleParser.LobSegnameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#locationSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterLocationSpecifier(BaseRuleParser.LocationSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#locationSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitLocationSpecifier(BaseRuleParser.LocationSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#xmlSchemaURLName}.
	 * @param ctx the parse tree
	 */
	void enterXmlSchemaURLName(BaseRuleParser.XmlSchemaURLNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#xmlSchemaURLName}.
	 * @param ctx the parse tree
	 */
	void exitXmlSchemaURLName(BaseRuleParser.XmlSchemaURLNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#elementName}.
	 * @param ctx the parse tree
	 */
	void enterElementName(BaseRuleParser.ElementNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#elementName}.
	 * @param ctx the parse tree
	 */
	void exitElementName(BaseRuleParser.ElementNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#subpartitionName}.
	 * @param ctx the parse tree
	 */
	void enterSubpartitionName(BaseRuleParser.SubpartitionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#subpartitionName}.
	 * @param ctx the parse tree
	 */
	void exitSubpartitionName(BaseRuleParser.SubpartitionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#parameterName}.
	 * @param ctx the parse tree
	 */
	void enterParameterName(BaseRuleParser.ParameterNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#parameterName}.
	 * @param ctx the parse tree
	 */
	void exitParameterName(BaseRuleParser.ParameterNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#editionName}.
	 * @param ctx the parse tree
	 */
	void enterEditionName(BaseRuleParser.EditionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#editionName}.
	 * @param ctx the parse tree
	 */
	void exitEditionName(BaseRuleParser.EditionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#containerName}.
	 * @param ctx the parse tree
	 */
	void enterContainerName(BaseRuleParser.ContainerNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#containerName}.
	 * @param ctx the parse tree
	 */
	void exitContainerName(BaseRuleParser.ContainerNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#partitionName}.
	 * @param ctx the parse tree
	 */
	void enterPartitionName(BaseRuleParser.PartitionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#partitionName}.
	 * @param ctx the parse tree
	 */
	void exitPartitionName(BaseRuleParser.PartitionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#partitionSetName}.
	 * @param ctx the parse tree
	 */
	void enterPartitionSetName(BaseRuleParser.PartitionSetNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#partitionSetName}.
	 * @param ctx the parse tree
	 */
	void exitPartitionSetName(BaseRuleParser.PartitionSetNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#partitionKeyValue}.
	 * @param ctx the parse tree
	 */
	void enterPartitionKeyValue(BaseRuleParser.PartitionKeyValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#partitionKeyValue}.
	 * @param ctx the parse tree
	 */
	void exitPartitionKeyValue(BaseRuleParser.PartitionKeyValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#subpartitionKeyValue}.
	 * @param ctx the parse tree
	 */
	void enterSubpartitionKeyValue(BaseRuleParser.SubpartitionKeyValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#subpartitionKeyValue}.
	 * @param ctx the parse tree
	 */
	void exitSubpartitionKeyValue(BaseRuleParser.SubpartitionKeyValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#zonemapName}.
	 * @param ctx the parse tree
	 */
	void enterZonemapName(BaseRuleParser.ZonemapNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#zonemapName}.
	 * @param ctx the parse tree
	 */
	void exitZonemapName(BaseRuleParser.ZonemapNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#flashbackArchiveName}.
	 * @param ctx the parse tree
	 */
	void enterFlashbackArchiveName(BaseRuleParser.FlashbackArchiveNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#flashbackArchiveName}.
	 * @param ctx the parse tree
	 */
	void exitFlashbackArchiveName(BaseRuleParser.FlashbackArchiveNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#roleName}.
	 * @param ctx the parse tree
	 */
	void enterRoleName(BaseRuleParser.RoleNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#roleName}.
	 * @param ctx the parse tree
	 */
	void exitRoleName(BaseRuleParser.RoleNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#password}.
	 * @param ctx the parse tree
	 */
	void enterPassword(BaseRuleParser.PasswordContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#password}.
	 * @param ctx the parse tree
	 */
	void exitPassword(BaseRuleParser.PasswordContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#logGroupName}.
	 * @param ctx the parse tree
	 */
	void enterLogGroupName(BaseRuleParser.LogGroupNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#logGroupName}.
	 * @param ctx the parse tree
	 */
	void exitLogGroupName(BaseRuleParser.LogGroupNameContext ctx);
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
	 * Enter a parse tree produced by {@link BaseRuleParser#oracleId}.
	 * @param ctx the parse tree
	 */
	void enterOracleId(BaseRuleParser.OracleIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#oracleId}.
	 * @param ctx the parse tree
	 */
	void exitOracleId(BaseRuleParser.OracleIdContext ctx);
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
	 * Enter a parse tree produced by {@link BaseRuleParser#columnCollationName}.
	 * @param ctx the parse tree
	 */
	void enterColumnCollationName(BaseRuleParser.ColumnCollationNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#columnCollationName}.
	 * @param ctx the parse tree
	 */
	void exitColumnCollationName(BaseRuleParser.ColumnCollationNameContext ctx);
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
	 * Enter a parse tree produced by {@link BaseRuleParser#exprs}.
	 * @param ctx the parse tree
	 */
	void enterExprs(BaseRuleParser.ExprsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#exprs}.
	 * @param ctx the parse tree
	 */
	void exitExprs(BaseRuleParser.ExprsContext ctx);
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
	 * Enter a parse tree produced by {@link BaseRuleParser#attributeName}.
	 * @param ctx the parse tree
	 */
	void enterAttributeName(BaseRuleParser.AttributeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#attributeName}.
	 * @param ctx the parse tree
	 */
	void exitAttributeName(BaseRuleParser.AttributeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#indexTypeName}.
	 * @param ctx the parse tree
	 */
	void enterIndexTypeName(BaseRuleParser.IndexTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#indexTypeName}.
	 * @param ctx the parse tree
	 */
	void exitIndexTypeName(BaseRuleParser.IndexTypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#simpleExprs}.
	 * @param ctx the parse tree
	 */
	void enterSimpleExprs(BaseRuleParser.SimpleExprsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#simpleExprs}.
	 * @param ctx the parse tree
	 */
	void exitSimpleExprs(BaseRuleParser.SimpleExprsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#lobItem}.
	 * @param ctx the parse tree
	 */
	void enterLobItem(BaseRuleParser.LobItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#lobItem}.
	 * @param ctx the parse tree
	 */
	void exitLobItem(BaseRuleParser.LobItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#lobItems}.
	 * @param ctx the parse tree
	 */
	void enterLobItems(BaseRuleParser.LobItemsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#lobItems}.
	 * @param ctx the parse tree
	 */
	void exitLobItems(BaseRuleParser.LobItemsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#lobItemList}.
	 * @param ctx the parse tree
	 */
	void enterLobItemList(BaseRuleParser.LobItemListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#lobItemList}.
	 * @param ctx the parse tree
	 */
	void exitLobItemList(BaseRuleParser.LobItemListContext ctx);
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
	 * Enter a parse tree produced by {@link BaseRuleParser#specialDatatype}.
	 * @param ctx the parse tree
	 */
	void enterSpecialDatatype(BaseRuleParser.SpecialDatatypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#specialDatatype}.
	 * @param ctx the parse tree
	 */
	void exitSpecialDatatype(BaseRuleParser.SpecialDatatypeContext ctx);
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
	 * Enter a parse tree produced by {@link BaseRuleParser#datetimeTypeSuffix}.
	 * @param ctx the parse tree
	 */
	void enterDatetimeTypeSuffix(BaseRuleParser.DatetimeTypeSuffixContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#datetimeTypeSuffix}.
	 * @param ctx the parse tree
	 */
	void exitDatetimeTypeSuffix(BaseRuleParser.DatetimeTypeSuffixContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#treatFunction}.
	 * @param ctx the parse tree
	 */
	void enterTreatFunction(BaseRuleParser.TreatFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#treatFunction}.
	 * @param ctx the parse tree
	 */
	void exitTreatFunction(BaseRuleParser.TreatFunctionContext ctx);
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
	 * Enter a parse tree produced by {@link BaseRuleParser#simpleCaseExpr}.
	 * @param ctx the parse tree
	 */
	void enterSimpleCaseExpr(BaseRuleParser.SimpleCaseExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#simpleCaseExpr}.
	 * @param ctx the parse tree
	 */
	void exitSimpleCaseExpr(BaseRuleParser.SimpleCaseExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#searchedCaseExpr}.
	 * @param ctx the parse tree
	 */
	void enterSearchedCaseExpr(BaseRuleParser.SearchedCaseExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#searchedCaseExpr}.
	 * @param ctx the parse tree
	 */
	void exitSearchedCaseExpr(BaseRuleParser.SearchedCaseExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#elseClause}.
	 * @param ctx the parse tree
	 */
	void enterElseClause(BaseRuleParser.ElseClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#elseClause}.
	 * @param ctx the parse tree
	 */
	void exitElseClause(BaseRuleParser.ElseClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#intervalExpression}.
	 * @param ctx the parse tree
	 */
	void enterIntervalExpression(BaseRuleParser.IntervalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#intervalExpression}.
	 * @param ctx the parse tree
	 */
	void exitIntervalExpression(BaseRuleParser.IntervalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#objectAccessExpression}.
	 * @param ctx the parse tree
	 */
	void enterObjectAccessExpression(BaseRuleParser.ObjectAccessExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#objectAccessExpression}.
	 * @param ctx the parse tree
	 */
	void exitObjectAccessExpression(BaseRuleParser.ObjectAccessExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#constructorExpr}.
	 * @param ctx the parse tree
	 */
	void enterConstructorExpr(BaseRuleParser.ConstructorExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#constructorExpr}.
	 * @param ctx the parse tree
	 */
	void exitConstructorExpr(BaseRuleParser.ConstructorExprContext ctx);
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
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#hashSubpartitionQuantity}.
	 * @param ctx the parse tree
	 */
	void enterHashSubpartitionQuantity(BaseRuleParser.HashSubpartitionQuantityContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#hashSubpartitionQuantity}.
	 * @param ctx the parse tree
	 */
	void exitHashSubpartitionQuantity(BaseRuleParser.HashSubpartitionQuantityContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#odciParameters}.
	 * @param ctx the parse tree
	 */
	void enterOdciParameters(BaseRuleParser.OdciParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#odciParameters}.
	 * @param ctx the parse tree
	 */
	void exitOdciParameters(BaseRuleParser.OdciParametersContext ctx);
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
	 * Enter a parse tree produced by {@link BaseRuleParser#locationName}.
	 * @param ctx the parse tree
	 */
	void enterLocationName(BaseRuleParser.LocationNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#locationName}.
	 * @param ctx the parse tree
	 */
	void exitLocationName(BaseRuleParser.LocationNameContext ctx);
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
	 * Enter a parse tree produced by {@link BaseRuleParser#asmFileName}.
	 * @param ctx the parse tree
	 */
	void enterAsmFileName(BaseRuleParser.AsmFileNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#asmFileName}.
	 * @param ctx the parse tree
	 */
	void exitAsmFileName(BaseRuleParser.AsmFileNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#fileNumber}.
	 * @param ctx the parse tree
	 */
	void enterFileNumber(BaseRuleParser.FileNumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#fileNumber}.
	 * @param ctx the parse tree
	 */
	void exitFileNumber(BaseRuleParser.FileNumberContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#instanceName}.
	 * @param ctx the parse tree
	 */
	void enterInstanceName(BaseRuleParser.InstanceNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#instanceName}.
	 * @param ctx the parse tree
	 */
	void exitInstanceName(BaseRuleParser.InstanceNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#logminerSessionName}.
	 * @param ctx the parse tree
	 */
	void enterLogminerSessionName(BaseRuleParser.LogminerSessionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#logminerSessionName}.
	 * @param ctx the parse tree
	 */
	void exitLogminerSessionName(BaseRuleParser.LogminerSessionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#tablespaceGroupName}.
	 * @param ctx the parse tree
	 */
	void enterTablespaceGroupName(BaseRuleParser.TablespaceGroupNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#tablespaceGroupName}.
	 * @param ctx the parse tree
	 */
	void exitTablespaceGroupName(BaseRuleParser.TablespaceGroupNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#copyName}.
	 * @param ctx the parse tree
	 */
	void enterCopyName(BaseRuleParser.CopyNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#copyName}.
	 * @param ctx the parse tree
	 */
	void exitCopyName(BaseRuleParser.CopyNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#mirrorName}.
	 * @param ctx the parse tree
	 */
	void enterMirrorName(BaseRuleParser.MirrorNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#mirrorName}.
	 * @param ctx the parse tree
	 */
	void exitMirrorName(BaseRuleParser.MirrorNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#uriString}.
	 * @param ctx the parse tree
	 */
	void enterUriString(BaseRuleParser.UriStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#uriString}.
	 * @param ctx the parse tree
	 */
	void exitUriString(BaseRuleParser.UriStringContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#qualifiedCredentialName}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedCredentialName(BaseRuleParser.QualifiedCredentialNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#qualifiedCredentialName}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedCredentialName(BaseRuleParser.QualifiedCredentialNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#pdbName}.
	 * @param ctx the parse tree
	 */
	void enterPdbName(BaseRuleParser.PdbNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#pdbName}.
	 * @param ctx the parse tree
	 */
	void exitPdbName(BaseRuleParser.PdbNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#diskgroupName}.
	 * @param ctx the parse tree
	 */
	void enterDiskgroupName(BaseRuleParser.DiskgroupNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#diskgroupName}.
	 * @param ctx the parse tree
	 */
	void exitDiskgroupName(BaseRuleParser.DiskgroupNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#templateName}.
	 * @param ctx the parse tree
	 */
	void enterTemplateName(BaseRuleParser.TemplateNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#templateName}.
	 * @param ctx the parse tree
	 */
	void exitTemplateName(BaseRuleParser.TemplateNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#aliasName}.
	 * @param ctx the parse tree
	 */
	void enterAliasName(BaseRuleParser.AliasNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#aliasName}.
	 * @param ctx the parse tree
	 */
	void exitAliasName(BaseRuleParser.AliasNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#domain}.
	 * @param ctx the parse tree
	 */
	void enterDomain(BaseRuleParser.DomainContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#domain}.
	 * @param ctx the parse tree
	 */
	void exitDomain(BaseRuleParser.DomainContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#dateValue}.
	 * @param ctx the parse tree
	 */
	void enterDateValue(BaseRuleParser.DateValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#dateValue}.
	 * @param ctx the parse tree
	 */
	void exitDateValue(BaseRuleParser.DateValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#sessionId}.
	 * @param ctx the parse tree
	 */
	void enterSessionId(BaseRuleParser.SessionIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#sessionId}.
	 * @param ctx the parse tree
	 */
	void exitSessionId(BaseRuleParser.SessionIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#serialNumber}.
	 * @param ctx the parse tree
	 */
	void enterSerialNumber(BaseRuleParser.SerialNumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#serialNumber}.
	 * @param ctx the parse tree
	 */
	void exitSerialNumber(BaseRuleParser.SerialNumberContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#instanceId}.
	 * @param ctx the parse tree
	 */
	void enterInstanceId(BaseRuleParser.InstanceIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#instanceId}.
	 * @param ctx the parse tree
	 */
	void exitInstanceId(BaseRuleParser.InstanceIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#sqlId}.
	 * @param ctx the parse tree
	 */
	void enterSqlId(BaseRuleParser.SqlIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#sqlId}.
	 * @param ctx the parse tree
	 */
	void exitSqlId(BaseRuleParser.SqlIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#logFileName}.
	 * @param ctx the parse tree
	 */
	void enterLogFileName(BaseRuleParser.LogFileNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#logFileName}.
	 * @param ctx the parse tree
	 */
	void exitLogFileName(BaseRuleParser.LogFileNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#logFileGroupsArchivedLocationName}.
	 * @param ctx the parse tree
	 */
	void enterLogFileGroupsArchivedLocationName(BaseRuleParser.LogFileGroupsArchivedLocationNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#logFileGroupsArchivedLocationName}.
	 * @param ctx the parse tree
	 */
	void exitLogFileGroupsArchivedLocationName(BaseRuleParser.LogFileGroupsArchivedLocationNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#asmVersion}.
	 * @param ctx the parse tree
	 */
	void enterAsmVersion(BaseRuleParser.AsmVersionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#asmVersion}.
	 * @param ctx the parse tree
	 */
	void exitAsmVersion(BaseRuleParser.AsmVersionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#walletPassword}.
	 * @param ctx the parse tree
	 */
	void enterWalletPassword(BaseRuleParser.WalletPasswordContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#walletPassword}.
	 * @param ctx the parse tree
	 */
	void exitWalletPassword(BaseRuleParser.WalletPasswordContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#hsmAuthString}.
	 * @param ctx the parse tree
	 */
	void enterHsmAuthString(BaseRuleParser.HsmAuthStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#hsmAuthString}.
	 * @param ctx the parse tree
	 */
	void exitHsmAuthString(BaseRuleParser.HsmAuthStringContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#targetDbName}.
	 * @param ctx the parse tree
	 */
	void enterTargetDbName(BaseRuleParser.TargetDbNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#targetDbName}.
	 * @param ctx the parse tree
	 */
	void exitTargetDbName(BaseRuleParser.TargetDbNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#certificateId}.
	 * @param ctx the parse tree
	 */
	void enterCertificateId(BaseRuleParser.CertificateIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#certificateId}.
	 * @param ctx the parse tree
	 */
	void exitCertificateId(BaseRuleParser.CertificateIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#categoryName}.
	 * @param ctx the parse tree
	 */
	void enterCategoryName(BaseRuleParser.CategoryNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#categoryName}.
	 * @param ctx the parse tree
	 */
	void exitCategoryName(BaseRuleParser.CategoryNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#offset}.
	 * @param ctx the parse tree
	 */
	void enterOffset(BaseRuleParser.OffsetContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#offset}.
	 * @param ctx the parse tree
	 */
	void exitOffset(BaseRuleParser.OffsetContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#rowcount}.
	 * @param ctx the parse tree
	 */
	void enterRowcount(BaseRuleParser.RowcountContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#rowcount}.
	 * @param ctx the parse tree
	 */
	void exitRowcount(BaseRuleParser.RowcountContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#percent}.
	 * @param ctx the parse tree
	 */
	void enterPercent(BaseRuleParser.PercentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#percent}.
	 * @param ctx the parse tree
	 */
	void exitPercent(BaseRuleParser.PercentContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#rollbackSegment}.
	 * @param ctx the parse tree
	 */
	void enterRollbackSegment(BaseRuleParser.RollbackSegmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#rollbackSegment}.
	 * @param ctx the parse tree
	 */
	void exitRollbackSegment(BaseRuleParser.RollbackSegmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#queryName}.
	 * @param ctx the parse tree
	 */
	void enterQueryName(BaseRuleParser.QueryNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#queryName}.
	 * @param ctx the parse tree
	 */
	void exitQueryName(BaseRuleParser.QueryNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#cycleValue}.
	 * @param ctx the parse tree
	 */
	void enterCycleValue(BaseRuleParser.CycleValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#cycleValue}.
	 * @param ctx the parse tree
	 */
	void exitCycleValue(BaseRuleParser.CycleValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#noCycleValue}.
	 * @param ctx the parse tree
	 */
	void enterNoCycleValue(BaseRuleParser.NoCycleValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#noCycleValue}.
	 * @param ctx the parse tree
	 */
	void exitNoCycleValue(BaseRuleParser.NoCycleValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#orderingColumn}.
	 * @param ctx the parse tree
	 */
	void enterOrderingColumn(BaseRuleParser.OrderingColumnContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#orderingColumn}.
	 * @param ctx the parse tree
	 */
	void exitOrderingColumn(BaseRuleParser.OrderingColumnContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#subavName}.
	 * @param ctx the parse tree
	 */
	void enterSubavName(BaseRuleParser.SubavNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#subavName}.
	 * @param ctx the parse tree
	 */
	void exitSubavName(BaseRuleParser.SubavNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#baseAvName}.
	 * @param ctx the parse tree
	 */
	void enterBaseAvName(BaseRuleParser.BaseAvNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#baseAvName}.
	 * @param ctx the parse tree
	 */
	void exitBaseAvName(BaseRuleParser.BaseAvNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#measName}.
	 * @param ctx the parse tree
	 */
	void enterMeasName(BaseRuleParser.MeasNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#measName}.
	 * @param ctx the parse tree
	 */
	void exitMeasName(BaseRuleParser.MeasNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#levelRef}.
	 * @param ctx the parse tree
	 */
	void enterLevelRef(BaseRuleParser.LevelRefContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#levelRef}.
	 * @param ctx the parse tree
	 */
	void exitLevelRef(BaseRuleParser.LevelRefContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#offsetExpr}.
	 * @param ctx the parse tree
	 */
	void enterOffsetExpr(BaseRuleParser.OffsetExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#offsetExpr}.
	 * @param ctx the parse tree
	 */
	void exitOffsetExpr(BaseRuleParser.OffsetExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#memberKeyExpr}.
	 * @param ctx the parse tree
	 */
	void enterMemberKeyExpr(BaseRuleParser.MemberKeyExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#memberKeyExpr}.
	 * @param ctx the parse tree
	 */
	void exitMemberKeyExpr(BaseRuleParser.MemberKeyExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#depthExpression}.
	 * @param ctx the parse tree
	 */
	void enterDepthExpression(BaseRuleParser.DepthExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#depthExpression}.
	 * @param ctx the parse tree
	 */
	void exitDepthExpression(BaseRuleParser.DepthExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#unitName}.
	 * @param ctx the parse tree
	 */
	void enterUnitName(BaseRuleParser.UnitNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#unitName}.
	 * @param ctx the parse tree
	 */
	void exitUnitName(BaseRuleParser.UnitNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BaseRuleParser#procedureName}.
	 * @param ctx the parse tree
	 */
	void enterProcedureName(BaseRuleParser.ProcedureNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BaseRuleParser#procedureName}.
	 * @param ctx the parse tree
	 */
	void exitProcedureName(BaseRuleParser.ProcedureNameContext ctx);
}