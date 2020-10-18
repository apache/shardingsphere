// Generated from /Users/zhangliang/personal/shardingsphere/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-mysql/src/main/antlr4/imports/mysql/DALStatement.g4 by ANTLR 4.8
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link DALStatementParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface DALStatementVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#use}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUse(DALStatementParser.UseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#help}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHelp(DALStatementParser.HelpContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#explain}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplain(DALStatementParser.ExplainContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showDatabases}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowDatabases(DALStatementParser.ShowDatabasesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showTables}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowTables(DALStatementParser.ShowTablesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showTableStatus}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowTableStatus(DALStatementParser.ShowTableStatusContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showColumns}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowColumns(DALStatementParser.ShowColumnsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showIndex}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowIndex(DALStatementParser.ShowIndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showCreateTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowCreateTable(DALStatementParser.ShowCreateTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showOther}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowOther(DALStatementParser.ShowOtherContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#fromSchema}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromSchema(DALStatementParser.FromSchemaContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#fromTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromTable(DALStatementParser.FromTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showLike}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowLike(DALStatementParser.ShowLikeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showColumnLike_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowColumnLike_(DALStatementParser.ShowColumnLike_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showWhereClause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowWhereClause_(DALStatementParser.ShowWhereClause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showFilter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowFilter(DALStatementParser.ShowFilterContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showProfileType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowProfileType(DALStatementParser.ShowProfileTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#setVariable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetVariable(DALStatementParser.SetVariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#variableAssign}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableAssign(DALStatementParser.VariableAssignContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showBinaryLogs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowBinaryLogs(DALStatementParser.ShowBinaryLogsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showBinlogEvents}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowBinlogEvents(DALStatementParser.ShowBinlogEventsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showCharacterSet}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowCharacterSet(DALStatementParser.ShowCharacterSetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showCollation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowCollation(DALStatementParser.ShowCollationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showCreateDatabase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowCreateDatabase(DALStatementParser.ShowCreateDatabaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showCreateEvent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowCreateEvent(DALStatementParser.ShowCreateEventContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showCreateFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowCreateFunction(DALStatementParser.ShowCreateFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showCreateProcedure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowCreateProcedure(DALStatementParser.ShowCreateProcedureContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showCreateTrigger}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowCreateTrigger(DALStatementParser.ShowCreateTriggerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showCreateUser}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowCreateUser(DALStatementParser.ShowCreateUserContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showCreateView}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowCreateView(DALStatementParser.ShowCreateViewContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showEngine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowEngine(DALStatementParser.ShowEngineContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showEngines}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowEngines(DALStatementParser.ShowEnginesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showErrors}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowErrors(DALStatementParser.ShowErrorsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showEvents}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowEvents(DALStatementParser.ShowEventsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showFunctionCode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowFunctionCode(DALStatementParser.ShowFunctionCodeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showFunctionStatus}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowFunctionStatus(DALStatementParser.ShowFunctionStatusContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showGrant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowGrant(DALStatementParser.ShowGrantContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showMasterStatus}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowMasterStatus(DALStatementParser.ShowMasterStatusContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showOpenTables}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowOpenTables(DALStatementParser.ShowOpenTablesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showPlugins}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowPlugins(DALStatementParser.ShowPluginsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showPrivileges}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowPrivileges(DALStatementParser.ShowPrivilegesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showProcedureCode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowProcedureCode(DALStatementParser.ShowProcedureCodeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showProcedureStatus}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowProcedureStatus(DALStatementParser.ShowProcedureStatusContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showProcesslist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowProcesslist(DALStatementParser.ShowProcesslistContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showProfile}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowProfile(DALStatementParser.ShowProfileContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showProfiles}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowProfiles(DALStatementParser.ShowProfilesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showRelaylogEvent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowRelaylogEvent(DALStatementParser.ShowRelaylogEventContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showSlavehost}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowSlavehost(DALStatementParser.ShowSlavehostContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showSlaveStatus}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowSlaveStatus(DALStatementParser.ShowSlaveStatusContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showStatus}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowStatus(DALStatementParser.ShowStatusContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showTrriggers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowTrriggers(DALStatementParser.ShowTrriggersContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showVariables}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowVariables(DALStatementParser.ShowVariablesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#showWarnings}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowWarnings(DALStatementParser.ShowWarningsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#setCharacter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetCharacter(DALStatementParser.SetCharacterContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#setName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetName(DALStatementParser.SetNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#clone}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClone(DALStatementParser.CloneContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#cloneAction_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCloneAction_(DALStatementParser.CloneAction_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#createUdf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateUdf(DALStatementParser.CreateUdfContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#installComponent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstallComponent(DALStatementParser.InstallComponentContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#installPlugin}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstallPlugin(DALStatementParser.InstallPluginContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#uninstallComponent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUninstallComponent(DALStatementParser.UninstallComponentContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#uninstallPlugin}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUninstallPlugin(DALStatementParser.UninstallPluginContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#analyzeTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnalyzeTable(DALStatementParser.AnalyzeTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#checkTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCheckTable(DALStatementParser.CheckTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#checkTableOption_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCheckTableOption_(DALStatementParser.CheckTableOption_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#checksumTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChecksumTable(DALStatementParser.ChecksumTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#optimizeTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptimizeTable(DALStatementParser.OptimizeTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#repairTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRepairTable(DALStatementParser.RepairTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#alterResourceGroup}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterResourceGroup(DALStatementParser.AlterResourceGroupContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#vcpuSpec_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVcpuSpec_(DALStatementParser.VcpuSpec_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#createResourceGroup}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateResourceGroup(DALStatementParser.CreateResourceGroupContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#dropResourceGroup}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropResourceGroup(DALStatementParser.DropResourceGroupContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#setResourceGroup}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetResourceGroup(DALStatementParser.SetResourceGroupContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#binlog}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinlog(DALStatementParser.BinlogContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#cacheIndex}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCacheIndex(DALStatementParser.CacheIndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#tableIndexList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableIndexList(DALStatementParser.TableIndexListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#partitionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionList(DALStatementParser.PartitionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#flush}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFlush(DALStatementParser.FlushContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#flushOption_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFlushOption_(DALStatementParser.FlushOption_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#tablesOption_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablesOption_(DALStatementParser.TablesOption_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#kill}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKill(DALStatementParser.KillContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#loadIndexInfo}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoadIndexInfo(DALStatementParser.LoadIndexInfoContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#resetStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResetStatement(DALStatementParser.ResetStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#resetOption_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResetOption_(DALStatementParser.ResetOption_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#resetPersist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResetPersist(DALStatementParser.ResetPersistContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#restart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRestart(DALStatementParser.RestartContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#shutdown}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShutdown(DALStatementParser.ShutdownContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#explainType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplainType(DALStatementParser.ExplainTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#explainableStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplainableStatement(DALStatementParser.ExplainableStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#formatName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormatName(DALStatementParser.FormatNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#parameterMarker}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterMarker(DALStatementParser.ParameterMarkerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#customKeyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCustomKeyword(DALStatementParser.CustomKeywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#literals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiterals(DALStatementParser.LiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#stringLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringLiterals(DALStatementParser.StringLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#numberLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberLiterals(DALStatementParser.NumberLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#dateTimeLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeLiterals(DALStatementParser.DateTimeLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#hexadecimalLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHexadecimalLiterals(DALStatementParser.HexadecimalLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#bitValueLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitValueLiterals(DALStatementParser.BitValueLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#booleanLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanLiterals(DALStatementParser.BooleanLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#nullValueLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullValueLiterals(DALStatementParser.NullValueLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#characterSetName_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterSetName_(DALStatementParser.CharacterSetName_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#collationName_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollationName_(DALStatementParser.CollationName_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(DALStatementParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#unreservedWord}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnreservedWord(DALStatementParser.UnreservedWordContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable(DALStatementParser.VariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#scope}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScope(DALStatementParser.ScopeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#schemaName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaName(DALStatementParser.SchemaNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#schemaNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaNames(DALStatementParser.SchemaNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#schemaPairs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaPairs(DALStatementParser.SchemaPairsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#schemaPair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaPair(DALStatementParser.SchemaPairContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#tableName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableName(DALStatementParser.TableNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#columnName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnName(DALStatementParser.ColumnNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#indexName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexName(DALStatementParser.IndexNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#userName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUserName(DALStatementParser.UserNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#eventName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEventName(DALStatementParser.EventNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#serverName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitServerName(DALStatementParser.ServerNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#wrapperName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWrapperName(DALStatementParser.WrapperNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#functionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionName(DALStatementParser.FunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#viewName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitViewName(DALStatementParser.ViewNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#owner}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOwner(DALStatementParser.OwnerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlias(DALStatementParser.AliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName(DALStatementParser.NameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#tableNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableNames(DALStatementParser.TableNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#columnNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNames(DALStatementParser.ColumnNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#groupName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupName(DALStatementParser.GroupNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#routineName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoutineName(DALStatementParser.RoutineNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#shardLibraryName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShardLibraryName(DALStatementParser.ShardLibraryNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#componentName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComponentName(DALStatementParser.ComponentNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#pluginName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPluginName(DALStatementParser.PluginNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#hostName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHostName(DALStatementParser.HostNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#port}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPort(DALStatementParser.PortContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#cloneInstance}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCloneInstance(DALStatementParser.CloneInstanceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#cloneDir}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCloneDir(DALStatementParser.CloneDirContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#channelName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChannelName(DALStatementParser.ChannelNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#logName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogName(DALStatementParser.LogNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#roleName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoleName(DALStatementParser.RoleNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#engineName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEngineName(DALStatementParser.EngineNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#triggerName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerName(DALStatementParser.TriggerNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#triggerTime}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerTime(DALStatementParser.TriggerTimeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#userOrRole}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUserOrRole(DALStatementParser.UserOrRoleContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#partitionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionName(DALStatementParser.PartitionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#triggerEvent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerEvent(DALStatementParser.TriggerEventContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#triggerOrder}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerOrder(DALStatementParser.TriggerOrderContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(DALStatementParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#logicalOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOperator(DALStatementParser.LogicalOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#notOperator_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotOperator_(DALStatementParser.NotOperator_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#booleanPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanPrimary(DALStatementParser.BooleanPrimaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOperator(DALStatementParser.ComparisonOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate(DALStatementParser.PredicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#bitExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitExpr(DALStatementParser.BitExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#simpleExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleExpr(DALStatementParser.SimpleExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCall(DALStatementParser.FunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#aggregationFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregationFunction(DALStatementParser.AggregationFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#aggregationFunctionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregationFunctionName(DALStatementParser.AggregationFunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#distinct}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDistinct(DALStatementParser.DistinctContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#overClause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOverClause_(DALStatementParser.OverClause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#windowSpecification_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowSpecification_(DALStatementParser.WindowSpecification_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#partitionClause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionClause_(DALStatementParser.PartitionClause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#frameClause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrameClause_(DALStatementParser.FrameClause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#frameStart_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrameStart_(DALStatementParser.FrameStart_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#frameEnd_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrameEnd_(DALStatementParser.FrameEnd_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#frameBetween_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrameBetween_(DALStatementParser.FrameBetween_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#specialFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecialFunction(DALStatementParser.SpecialFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#currentUserFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCurrentUserFunction(DALStatementParser.CurrentUserFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#groupConcatFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupConcatFunction(DALStatementParser.GroupConcatFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#windowFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowFunction(DALStatementParser.WindowFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#castFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCastFunction(DALStatementParser.CastFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#convertFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConvertFunction(DALStatementParser.ConvertFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#positionFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPositionFunction(DALStatementParser.PositionFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#substringFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubstringFunction(DALStatementParser.SubstringFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#extractFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtractFunction(DALStatementParser.ExtractFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#charFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharFunction(DALStatementParser.CharFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#trimFunction_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrimFunction_(DALStatementParser.TrimFunction_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#valuesFunction_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValuesFunction_(DALStatementParser.ValuesFunction_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#weightStringFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWeightStringFunction(DALStatementParser.WeightStringFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#levelClause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLevelClause_(DALStatementParser.LevelClause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#levelInWeightListElement_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLevelInWeightListElement_(DALStatementParser.LevelInWeightListElement_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#regularFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRegularFunction(DALStatementParser.RegularFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#shorthandRegularFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShorthandRegularFunction(DALStatementParser.ShorthandRegularFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#completeRegularFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompleteRegularFunction(DALStatementParser.CompleteRegularFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#regularFunctionName_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRegularFunctionName_(DALStatementParser.RegularFunctionName_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#matchExpression_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatchExpression_(DALStatementParser.MatchExpression_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#matchSearchModifier_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatchSearchModifier_(DALStatementParser.MatchSearchModifier_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#caseExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseExpression(DALStatementParser.CaseExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#datetimeExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDatetimeExpr(DALStatementParser.DatetimeExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#binaryLogFileIndexNumber}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryLogFileIndexNumber(DALStatementParser.BinaryLogFileIndexNumberContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#caseWhen_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseWhen_(DALStatementParser.CaseWhen_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#caseElse_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseElse_(DALStatementParser.CaseElse_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#intervalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalExpression(DALStatementParser.IntervalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#intervalValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalValue(DALStatementParser.IntervalValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#intervalUnit_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalUnit_(DALStatementParser.IntervalUnit_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#subquery}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubquery(DALStatementParser.SubqueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#orderByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByClause(DALStatementParser.OrderByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#orderByItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByItem(DALStatementParser.OrderByItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataType(DALStatementParser.DataTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#dataTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataTypeName(DALStatementParser.DataTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#dataTypeLength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataTypeLength(DALStatementParser.DataTypeLengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#collectionOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollectionOptions(DALStatementParser.CollectionOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#characterSet_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterSet_(DALStatementParser.CharacterSet_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#collateClause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollateClause_(DALStatementParser.CollateClause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#ignoredIdentifier_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIgnoredIdentifier_(DALStatementParser.IgnoredIdentifier_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#ignoredIdentifiers_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIgnoredIdentifiers_(DALStatementParser.IgnoredIdentifiers_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#fieldOrVarSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldOrVarSpec(DALStatementParser.FieldOrVarSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#notExistClause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotExistClause_(DALStatementParser.NotExistClause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#existClause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExistClause_(DALStatementParser.ExistClause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#pattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPattern(DALStatementParser.PatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#connectionId_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConnectionId_(DALStatementParser.ConnectionId_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#labelName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLabelName(DALStatementParser.LabelNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#cursorName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCursorName(DALStatementParser.CursorNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#conditionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditionName(DALStatementParser.ConditionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#insert}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert(DALStatementParser.InsertContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#insertSpecification_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertSpecification_(DALStatementParser.InsertSpecification_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#insertValuesClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertValuesClause(DALStatementParser.InsertValuesClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#insertSelectClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertSelectClause(DALStatementParser.InsertSelectClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#onDuplicateKeyClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOnDuplicateKeyClause(DALStatementParser.OnDuplicateKeyClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#valueReference_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValueReference_(DALStatementParser.ValueReference_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#derivedColumns_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDerivedColumns_(DALStatementParser.DerivedColumns_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#replace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReplace(DALStatementParser.ReplaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#replaceSpecification_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReplaceSpecification_(DALStatementParser.ReplaceSpecification_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#replaceValuesClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReplaceValuesClause(DALStatementParser.ReplaceValuesClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#replaceSelectClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReplaceSelectClause(DALStatementParser.ReplaceSelectClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#update}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdate(DALStatementParser.UpdateContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#updateSpecification_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdateSpecification_(DALStatementParser.UpdateSpecification_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(DALStatementParser.AssignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#setAssignmentsClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetAssignmentsClause(DALStatementParser.SetAssignmentsClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#assignmentValues}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentValues(DALStatementParser.AssignmentValuesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#assignmentValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentValue(DALStatementParser.AssignmentValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#blobValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlobValue(DALStatementParser.BlobValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#delete}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDelete(DALStatementParser.DeleteContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#deleteSpecification_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeleteSpecification_(DALStatementParser.DeleteSpecification_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#singleTableClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleTableClause(DALStatementParser.SingleTableClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#multipleTablesClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultipleTablesClause(DALStatementParser.MultipleTablesClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#multipleTableNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultipleTableNames(DALStatementParser.MultipleTableNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#select}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect(DALStatementParser.SelectContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#call}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCall(DALStatementParser.CallContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#doStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDoStatement(DALStatementParser.DoStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#handlerStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHandlerStatement(DALStatementParser.HandlerStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#handlerOpenStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHandlerOpenStatement(DALStatementParser.HandlerOpenStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#handlerReadIndexStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHandlerReadIndexStatement(DALStatementParser.HandlerReadIndexStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#handlerReadStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHandlerReadStatement(DALStatementParser.HandlerReadStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#handlerCloseStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHandlerCloseStatement(DALStatementParser.HandlerCloseStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#importStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportStatement(DALStatementParser.ImportStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#loadDataStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoadDataStatement(DALStatementParser.LoadDataStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#loadXmlStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoadXmlStatement(DALStatementParser.LoadXmlStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#tableStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableStatement(DALStatementParser.TableStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#valuesStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValuesStatement(DALStatementParser.ValuesStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#columnDesignator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnDesignator(DALStatementParser.ColumnDesignatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#rowConstructorList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowConstructorList(DALStatementParser.RowConstructorListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#withClause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithClause_(DALStatementParser.WithClause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#cteClause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCteClause_(DALStatementParser.CteClause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#unionClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnionClause(DALStatementParser.UnionClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#selectClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectClause(DALStatementParser.SelectClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#selectSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectSpecification(DALStatementParser.SelectSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#duplicateSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDuplicateSpecification(DALStatementParser.DuplicateSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#projections}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProjections(DALStatementParser.ProjectionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#projection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProjection(DALStatementParser.ProjectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#unqualifiedShorthand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnqualifiedShorthand(DALStatementParser.UnqualifiedShorthandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#qualifiedShorthand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedShorthand(DALStatementParser.QualifiedShorthandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#fromClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromClause(DALStatementParser.FromClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#tableReferences}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableReferences(DALStatementParser.TableReferencesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#escapedTableReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEscapedTableReference(DALStatementParser.EscapedTableReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#tableReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableReference(DALStatementParser.TableReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#tableFactor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableFactor(DALStatementParser.TableFactorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#partitionNames_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionNames_(DALStatementParser.PartitionNames_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#indexHintList_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexHintList_(DALStatementParser.IndexHintList_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#indexHint_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexHint_(DALStatementParser.IndexHint_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#joinedTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinedTable(DALStatementParser.JoinedTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#joinSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinSpecification(DALStatementParser.JoinSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#whereClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhereClause(DALStatementParser.WhereClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#groupByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupByClause(DALStatementParser.GroupByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#havingClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingClause(DALStatementParser.HavingClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#limitClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimitClause(DALStatementParser.LimitClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#limitRowCount}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimitRowCount(DALStatementParser.LimitRowCountContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#limitOffset}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimitOffset(DALStatementParser.LimitOffsetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#windowClause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowClause_(DALStatementParser.WindowClause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#windowItem_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowItem_(DALStatementParser.WindowItem_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#selectLinesInto_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectLinesInto_(DALStatementParser.SelectLinesInto_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#selectFieldsInto_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectFieldsInto_(DALStatementParser.SelectFieldsInto_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#selectIntoExpression_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectIntoExpression_(DALStatementParser.SelectIntoExpression_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DALStatementParser#lockClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLockClause(DALStatementParser.LockClauseContext ctx);
}