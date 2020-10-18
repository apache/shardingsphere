// Generated from /Users/zhangliang/personal/shardingsphere/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-mysql/src/main/antlr4/imports/mysql/DALStatement.g4 by ANTLR 4.8
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DALStatementParser}.
 */
public interface DALStatementListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#use}.
	 * @param ctx the parse tree
	 */
	void enterUse(DALStatementParser.UseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#use}.
	 * @param ctx the parse tree
	 */
	void exitUse(DALStatementParser.UseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#help}.
	 * @param ctx the parse tree
	 */
	void enterHelp(DALStatementParser.HelpContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#help}.
	 * @param ctx the parse tree
	 */
	void exitHelp(DALStatementParser.HelpContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#explain}.
	 * @param ctx the parse tree
	 */
	void enterExplain(DALStatementParser.ExplainContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#explain}.
	 * @param ctx the parse tree
	 */
	void exitExplain(DALStatementParser.ExplainContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showDatabases}.
	 * @param ctx the parse tree
	 */
	void enterShowDatabases(DALStatementParser.ShowDatabasesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showDatabases}.
	 * @param ctx the parse tree
	 */
	void exitShowDatabases(DALStatementParser.ShowDatabasesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showTables}.
	 * @param ctx the parse tree
	 */
	void enterShowTables(DALStatementParser.ShowTablesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showTables}.
	 * @param ctx the parse tree
	 */
	void exitShowTables(DALStatementParser.ShowTablesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showTableStatus}.
	 * @param ctx the parse tree
	 */
	void enterShowTableStatus(DALStatementParser.ShowTableStatusContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showTableStatus}.
	 * @param ctx the parse tree
	 */
	void exitShowTableStatus(DALStatementParser.ShowTableStatusContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showColumns}.
	 * @param ctx the parse tree
	 */
	void enterShowColumns(DALStatementParser.ShowColumnsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showColumns}.
	 * @param ctx the parse tree
	 */
	void exitShowColumns(DALStatementParser.ShowColumnsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showIndex}.
	 * @param ctx the parse tree
	 */
	void enterShowIndex(DALStatementParser.ShowIndexContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showIndex}.
	 * @param ctx the parse tree
	 */
	void exitShowIndex(DALStatementParser.ShowIndexContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showCreateTable}.
	 * @param ctx the parse tree
	 */
	void enterShowCreateTable(DALStatementParser.ShowCreateTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showCreateTable}.
	 * @param ctx the parse tree
	 */
	void exitShowCreateTable(DALStatementParser.ShowCreateTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showOther}.
	 * @param ctx the parse tree
	 */
	void enterShowOther(DALStatementParser.ShowOtherContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showOther}.
	 * @param ctx the parse tree
	 */
	void exitShowOther(DALStatementParser.ShowOtherContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#fromSchema}.
	 * @param ctx the parse tree
	 */
	void enterFromSchema(DALStatementParser.FromSchemaContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#fromSchema}.
	 * @param ctx the parse tree
	 */
	void exitFromSchema(DALStatementParser.FromSchemaContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#fromTable}.
	 * @param ctx the parse tree
	 */
	void enterFromTable(DALStatementParser.FromTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#fromTable}.
	 * @param ctx the parse tree
	 */
	void exitFromTable(DALStatementParser.FromTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showLike}.
	 * @param ctx the parse tree
	 */
	void enterShowLike(DALStatementParser.ShowLikeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showLike}.
	 * @param ctx the parse tree
	 */
	void exitShowLike(DALStatementParser.ShowLikeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showColumnLike_}.
	 * @param ctx the parse tree
	 */
	void enterShowColumnLike_(DALStatementParser.ShowColumnLike_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showColumnLike_}.
	 * @param ctx the parse tree
	 */
	void exitShowColumnLike_(DALStatementParser.ShowColumnLike_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showWhereClause_}.
	 * @param ctx the parse tree
	 */
	void enterShowWhereClause_(DALStatementParser.ShowWhereClause_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showWhereClause_}.
	 * @param ctx the parse tree
	 */
	void exitShowWhereClause_(DALStatementParser.ShowWhereClause_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showFilter}.
	 * @param ctx the parse tree
	 */
	void enterShowFilter(DALStatementParser.ShowFilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showFilter}.
	 * @param ctx the parse tree
	 */
	void exitShowFilter(DALStatementParser.ShowFilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showProfileType}.
	 * @param ctx the parse tree
	 */
	void enterShowProfileType(DALStatementParser.ShowProfileTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showProfileType}.
	 * @param ctx the parse tree
	 */
	void exitShowProfileType(DALStatementParser.ShowProfileTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#setVariable}.
	 * @param ctx the parse tree
	 */
	void enterSetVariable(DALStatementParser.SetVariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#setVariable}.
	 * @param ctx the parse tree
	 */
	void exitSetVariable(DALStatementParser.SetVariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#variableAssign}.
	 * @param ctx the parse tree
	 */
	void enterVariableAssign(DALStatementParser.VariableAssignContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#variableAssign}.
	 * @param ctx the parse tree
	 */
	void exitVariableAssign(DALStatementParser.VariableAssignContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showBinaryLogs}.
	 * @param ctx the parse tree
	 */
	void enterShowBinaryLogs(DALStatementParser.ShowBinaryLogsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showBinaryLogs}.
	 * @param ctx the parse tree
	 */
	void exitShowBinaryLogs(DALStatementParser.ShowBinaryLogsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showBinlogEvents}.
	 * @param ctx the parse tree
	 */
	void enterShowBinlogEvents(DALStatementParser.ShowBinlogEventsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showBinlogEvents}.
	 * @param ctx the parse tree
	 */
	void exitShowBinlogEvents(DALStatementParser.ShowBinlogEventsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showCharacterSet}.
	 * @param ctx the parse tree
	 */
	void enterShowCharacterSet(DALStatementParser.ShowCharacterSetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showCharacterSet}.
	 * @param ctx the parse tree
	 */
	void exitShowCharacterSet(DALStatementParser.ShowCharacterSetContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showCollation}.
	 * @param ctx the parse tree
	 */
	void enterShowCollation(DALStatementParser.ShowCollationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showCollation}.
	 * @param ctx the parse tree
	 */
	void exitShowCollation(DALStatementParser.ShowCollationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showCreateDatabase}.
	 * @param ctx the parse tree
	 */
	void enterShowCreateDatabase(DALStatementParser.ShowCreateDatabaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showCreateDatabase}.
	 * @param ctx the parse tree
	 */
	void exitShowCreateDatabase(DALStatementParser.ShowCreateDatabaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showCreateEvent}.
	 * @param ctx the parse tree
	 */
	void enterShowCreateEvent(DALStatementParser.ShowCreateEventContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showCreateEvent}.
	 * @param ctx the parse tree
	 */
	void exitShowCreateEvent(DALStatementParser.ShowCreateEventContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showCreateFunction}.
	 * @param ctx the parse tree
	 */
	void enterShowCreateFunction(DALStatementParser.ShowCreateFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showCreateFunction}.
	 * @param ctx the parse tree
	 */
	void exitShowCreateFunction(DALStatementParser.ShowCreateFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showCreateProcedure}.
	 * @param ctx the parse tree
	 */
	void enterShowCreateProcedure(DALStatementParser.ShowCreateProcedureContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showCreateProcedure}.
	 * @param ctx the parse tree
	 */
	void exitShowCreateProcedure(DALStatementParser.ShowCreateProcedureContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showCreateTrigger}.
	 * @param ctx the parse tree
	 */
	void enterShowCreateTrigger(DALStatementParser.ShowCreateTriggerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showCreateTrigger}.
	 * @param ctx the parse tree
	 */
	void exitShowCreateTrigger(DALStatementParser.ShowCreateTriggerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showCreateUser}.
	 * @param ctx the parse tree
	 */
	void enterShowCreateUser(DALStatementParser.ShowCreateUserContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showCreateUser}.
	 * @param ctx the parse tree
	 */
	void exitShowCreateUser(DALStatementParser.ShowCreateUserContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showCreateView}.
	 * @param ctx the parse tree
	 */
	void enterShowCreateView(DALStatementParser.ShowCreateViewContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showCreateView}.
	 * @param ctx the parse tree
	 */
	void exitShowCreateView(DALStatementParser.ShowCreateViewContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showEngine}.
	 * @param ctx the parse tree
	 */
	void enterShowEngine(DALStatementParser.ShowEngineContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showEngine}.
	 * @param ctx the parse tree
	 */
	void exitShowEngine(DALStatementParser.ShowEngineContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showEngines}.
	 * @param ctx the parse tree
	 */
	void enterShowEngines(DALStatementParser.ShowEnginesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showEngines}.
	 * @param ctx the parse tree
	 */
	void exitShowEngines(DALStatementParser.ShowEnginesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showErrors}.
	 * @param ctx the parse tree
	 */
	void enterShowErrors(DALStatementParser.ShowErrorsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showErrors}.
	 * @param ctx the parse tree
	 */
	void exitShowErrors(DALStatementParser.ShowErrorsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showEvents}.
	 * @param ctx the parse tree
	 */
	void enterShowEvents(DALStatementParser.ShowEventsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showEvents}.
	 * @param ctx the parse tree
	 */
	void exitShowEvents(DALStatementParser.ShowEventsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showFunctionCode}.
	 * @param ctx the parse tree
	 */
	void enterShowFunctionCode(DALStatementParser.ShowFunctionCodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showFunctionCode}.
	 * @param ctx the parse tree
	 */
	void exitShowFunctionCode(DALStatementParser.ShowFunctionCodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showFunctionStatus}.
	 * @param ctx the parse tree
	 */
	void enterShowFunctionStatus(DALStatementParser.ShowFunctionStatusContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showFunctionStatus}.
	 * @param ctx the parse tree
	 */
	void exitShowFunctionStatus(DALStatementParser.ShowFunctionStatusContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showGrant}.
	 * @param ctx the parse tree
	 */
	void enterShowGrant(DALStatementParser.ShowGrantContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showGrant}.
	 * @param ctx the parse tree
	 */
	void exitShowGrant(DALStatementParser.ShowGrantContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showMasterStatus}.
	 * @param ctx the parse tree
	 */
	void enterShowMasterStatus(DALStatementParser.ShowMasterStatusContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showMasterStatus}.
	 * @param ctx the parse tree
	 */
	void exitShowMasterStatus(DALStatementParser.ShowMasterStatusContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showOpenTables}.
	 * @param ctx the parse tree
	 */
	void enterShowOpenTables(DALStatementParser.ShowOpenTablesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showOpenTables}.
	 * @param ctx the parse tree
	 */
	void exitShowOpenTables(DALStatementParser.ShowOpenTablesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showPlugins}.
	 * @param ctx the parse tree
	 */
	void enterShowPlugins(DALStatementParser.ShowPluginsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showPlugins}.
	 * @param ctx the parse tree
	 */
	void exitShowPlugins(DALStatementParser.ShowPluginsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showPrivileges}.
	 * @param ctx the parse tree
	 */
	void enterShowPrivileges(DALStatementParser.ShowPrivilegesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showPrivileges}.
	 * @param ctx the parse tree
	 */
	void exitShowPrivileges(DALStatementParser.ShowPrivilegesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showProcedureCode}.
	 * @param ctx the parse tree
	 */
	void enterShowProcedureCode(DALStatementParser.ShowProcedureCodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showProcedureCode}.
	 * @param ctx the parse tree
	 */
	void exitShowProcedureCode(DALStatementParser.ShowProcedureCodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showProcedureStatus}.
	 * @param ctx the parse tree
	 */
	void enterShowProcedureStatus(DALStatementParser.ShowProcedureStatusContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showProcedureStatus}.
	 * @param ctx the parse tree
	 */
	void exitShowProcedureStatus(DALStatementParser.ShowProcedureStatusContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showProcesslist}.
	 * @param ctx the parse tree
	 */
	void enterShowProcesslist(DALStatementParser.ShowProcesslistContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showProcesslist}.
	 * @param ctx the parse tree
	 */
	void exitShowProcesslist(DALStatementParser.ShowProcesslistContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showProfile}.
	 * @param ctx the parse tree
	 */
	void enterShowProfile(DALStatementParser.ShowProfileContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showProfile}.
	 * @param ctx the parse tree
	 */
	void exitShowProfile(DALStatementParser.ShowProfileContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showProfiles}.
	 * @param ctx the parse tree
	 */
	void enterShowProfiles(DALStatementParser.ShowProfilesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showProfiles}.
	 * @param ctx the parse tree
	 */
	void exitShowProfiles(DALStatementParser.ShowProfilesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showRelaylogEvent}.
	 * @param ctx the parse tree
	 */
	void enterShowRelaylogEvent(DALStatementParser.ShowRelaylogEventContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showRelaylogEvent}.
	 * @param ctx the parse tree
	 */
	void exitShowRelaylogEvent(DALStatementParser.ShowRelaylogEventContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showSlavehost}.
	 * @param ctx the parse tree
	 */
	void enterShowSlavehost(DALStatementParser.ShowSlavehostContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showSlavehost}.
	 * @param ctx the parse tree
	 */
	void exitShowSlavehost(DALStatementParser.ShowSlavehostContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showSlaveStatus}.
	 * @param ctx the parse tree
	 */
	void enterShowSlaveStatus(DALStatementParser.ShowSlaveStatusContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showSlaveStatus}.
	 * @param ctx the parse tree
	 */
	void exitShowSlaveStatus(DALStatementParser.ShowSlaveStatusContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showStatus}.
	 * @param ctx the parse tree
	 */
	void enterShowStatus(DALStatementParser.ShowStatusContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showStatus}.
	 * @param ctx the parse tree
	 */
	void exitShowStatus(DALStatementParser.ShowStatusContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showTrriggers}.
	 * @param ctx the parse tree
	 */
	void enterShowTrriggers(DALStatementParser.ShowTrriggersContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showTrriggers}.
	 * @param ctx the parse tree
	 */
	void exitShowTrriggers(DALStatementParser.ShowTrriggersContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showVariables}.
	 * @param ctx the parse tree
	 */
	void enterShowVariables(DALStatementParser.ShowVariablesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showVariables}.
	 * @param ctx the parse tree
	 */
	void exitShowVariables(DALStatementParser.ShowVariablesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#showWarnings}.
	 * @param ctx the parse tree
	 */
	void enterShowWarnings(DALStatementParser.ShowWarningsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#showWarnings}.
	 * @param ctx the parse tree
	 */
	void exitShowWarnings(DALStatementParser.ShowWarningsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#setCharacter}.
	 * @param ctx the parse tree
	 */
	void enterSetCharacter(DALStatementParser.SetCharacterContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#setCharacter}.
	 * @param ctx the parse tree
	 */
	void exitSetCharacter(DALStatementParser.SetCharacterContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#setName}.
	 * @param ctx the parse tree
	 */
	void enterSetName(DALStatementParser.SetNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#setName}.
	 * @param ctx the parse tree
	 */
	void exitSetName(DALStatementParser.SetNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#clone}.
	 * @param ctx the parse tree
	 */
	void enterClone(DALStatementParser.CloneContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#clone}.
	 * @param ctx the parse tree
	 */
	void exitClone(DALStatementParser.CloneContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#cloneAction_}.
	 * @param ctx the parse tree
	 */
	void enterCloneAction_(DALStatementParser.CloneAction_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#cloneAction_}.
	 * @param ctx the parse tree
	 */
	void exitCloneAction_(DALStatementParser.CloneAction_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#createUdf}.
	 * @param ctx the parse tree
	 */
	void enterCreateUdf(DALStatementParser.CreateUdfContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#createUdf}.
	 * @param ctx the parse tree
	 */
	void exitCreateUdf(DALStatementParser.CreateUdfContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#installComponent}.
	 * @param ctx the parse tree
	 */
	void enterInstallComponent(DALStatementParser.InstallComponentContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#installComponent}.
	 * @param ctx the parse tree
	 */
	void exitInstallComponent(DALStatementParser.InstallComponentContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#installPlugin}.
	 * @param ctx the parse tree
	 */
	void enterInstallPlugin(DALStatementParser.InstallPluginContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#installPlugin}.
	 * @param ctx the parse tree
	 */
	void exitInstallPlugin(DALStatementParser.InstallPluginContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#uninstallComponent}.
	 * @param ctx the parse tree
	 */
	void enterUninstallComponent(DALStatementParser.UninstallComponentContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#uninstallComponent}.
	 * @param ctx the parse tree
	 */
	void exitUninstallComponent(DALStatementParser.UninstallComponentContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#uninstallPlugin}.
	 * @param ctx the parse tree
	 */
	void enterUninstallPlugin(DALStatementParser.UninstallPluginContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#uninstallPlugin}.
	 * @param ctx the parse tree
	 */
	void exitUninstallPlugin(DALStatementParser.UninstallPluginContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#analyzeTable}.
	 * @param ctx the parse tree
	 */
	void enterAnalyzeTable(DALStatementParser.AnalyzeTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#analyzeTable}.
	 * @param ctx the parse tree
	 */
	void exitAnalyzeTable(DALStatementParser.AnalyzeTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#checkTable}.
	 * @param ctx the parse tree
	 */
	void enterCheckTable(DALStatementParser.CheckTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#checkTable}.
	 * @param ctx the parse tree
	 */
	void exitCheckTable(DALStatementParser.CheckTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#checkTableOption_}.
	 * @param ctx the parse tree
	 */
	void enterCheckTableOption_(DALStatementParser.CheckTableOption_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#checkTableOption_}.
	 * @param ctx the parse tree
	 */
	void exitCheckTableOption_(DALStatementParser.CheckTableOption_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#checksumTable}.
	 * @param ctx the parse tree
	 */
	void enterChecksumTable(DALStatementParser.ChecksumTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#checksumTable}.
	 * @param ctx the parse tree
	 */
	void exitChecksumTable(DALStatementParser.ChecksumTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#optimizeTable}.
	 * @param ctx the parse tree
	 */
	void enterOptimizeTable(DALStatementParser.OptimizeTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#optimizeTable}.
	 * @param ctx the parse tree
	 */
	void exitOptimizeTable(DALStatementParser.OptimizeTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#repairTable}.
	 * @param ctx the parse tree
	 */
	void enterRepairTable(DALStatementParser.RepairTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#repairTable}.
	 * @param ctx the parse tree
	 */
	void exitRepairTable(DALStatementParser.RepairTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#alterResourceGroup}.
	 * @param ctx the parse tree
	 */
	void enterAlterResourceGroup(DALStatementParser.AlterResourceGroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#alterResourceGroup}.
	 * @param ctx the parse tree
	 */
	void exitAlterResourceGroup(DALStatementParser.AlterResourceGroupContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#vcpuSpec_}.
	 * @param ctx the parse tree
	 */
	void enterVcpuSpec_(DALStatementParser.VcpuSpec_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#vcpuSpec_}.
	 * @param ctx the parse tree
	 */
	void exitVcpuSpec_(DALStatementParser.VcpuSpec_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#createResourceGroup}.
	 * @param ctx the parse tree
	 */
	void enterCreateResourceGroup(DALStatementParser.CreateResourceGroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#createResourceGroup}.
	 * @param ctx the parse tree
	 */
	void exitCreateResourceGroup(DALStatementParser.CreateResourceGroupContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#dropResourceGroup}.
	 * @param ctx the parse tree
	 */
	void enterDropResourceGroup(DALStatementParser.DropResourceGroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#dropResourceGroup}.
	 * @param ctx the parse tree
	 */
	void exitDropResourceGroup(DALStatementParser.DropResourceGroupContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#setResourceGroup}.
	 * @param ctx the parse tree
	 */
	void enterSetResourceGroup(DALStatementParser.SetResourceGroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#setResourceGroup}.
	 * @param ctx the parse tree
	 */
	void exitSetResourceGroup(DALStatementParser.SetResourceGroupContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#binlog}.
	 * @param ctx the parse tree
	 */
	void enterBinlog(DALStatementParser.BinlogContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#binlog}.
	 * @param ctx the parse tree
	 */
	void exitBinlog(DALStatementParser.BinlogContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#cacheIndex}.
	 * @param ctx the parse tree
	 */
	void enterCacheIndex(DALStatementParser.CacheIndexContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#cacheIndex}.
	 * @param ctx the parse tree
	 */
	void exitCacheIndex(DALStatementParser.CacheIndexContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#tableIndexList}.
	 * @param ctx the parse tree
	 */
	void enterTableIndexList(DALStatementParser.TableIndexListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#tableIndexList}.
	 * @param ctx the parse tree
	 */
	void exitTableIndexList(DALStatementParser.TableIndexListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#partitionList}.
	 * @param ctx the parse tree
	 */
	void enterPartitionList(DALStatementParser.PartitionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#partitionList}.
	 * @param ctx the parse tree
	 */
	void exitPartitionList(DALStatementParser.PartitionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#flush}.
	 * @param ctx the parse tree
	 */
	void enterFlush(DALStatementParser.FlushContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#flush}.
	 * @param ctx the parse tree
	 */
	void exitFlush(DALStatementParser.FlushContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#flushOption_}.
	 * @param ctx the parse tree
	 */
	void enterFlushOption_(DALStatementParser.FlushOption_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#flushOption_}.
	 * @param ctx the parse tree
	 */
	void exitFlushOption_(DALStatementParser.FlushOption_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#tablesOption_}.
	 * @param ctx the parse tree
	 */
	void enterTablesOption_(DALStatementParser.TablesOption_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#tablesOption_}.
	 * @param ctx the parse tree
	 */
	void exitTablesOption_(DALStatementParser.TablesOption_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#kill}.
	 * @param ctx the parse tree
	 */
	void enterKill(DALStatementParser.KillContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#kill}.
	 * @param ctx the parse tree
	 */
	void exitKill(DALStatementParser.KillContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#loadIndexInfo}.
	 * @param ctx the parse tree
	 */
	void enterLoadIndexInfo(DALStatementParser.LoadIndexInfoContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#loadIndexInfo}.
	 * @param ctx the parse tree
	 */
	void exitLoadIndexInfo(DALStatementParser.LoadIndexInfoContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#resetStatement}.
	 * @param ctx the parse tree
	 */
	void enterResetStatement(DALStatementParser.ResetStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#resetStatement}.
	 * @param ctx the parse tree
	 */
	void exitResetStatement(DALStatementParser.ResetStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#resetOption_}.
	 * @param ctx the parse tree
	 */
	void enterResetOption_(DALStatementParser.ResetOption_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#resetOption_}.
	 * @param ctx the parse tree
	 */
	void exitResetOption_(DALStatementParser.ResetOption_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#resetPersist}.
	 * @param ctx the parse tree
	 */
	void enterResetPersist(DALStatementParser.ResetPersistContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#resetPersist}.
	 * @param ctx the parse tree
	 */
	void exitResetPersist(DALStatementParser.ResetPersistContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#restart}.
	 * @param ctx the parse tree
	 */
	void enterRestart(DALStatementParser.RestartContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#restart}.
	 * @param ctx the parse tree
	 */
	void exitRestart(DALStatementParser.RestartContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#shutdown}.
	 * @param ctx the parse tree
	 */
	void enterShutdown(DALStatementParser.ShutdownContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#shutdown}.
	 * @param ctx the parse tree
	 */
	void exitShutdown(DALStatementParser.ShutdownContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#explainType}.
	 * @param ctx the parse tree
	 */
	void enterExplainType(DALStatementParser.ExplainTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#explainType}.
	 * @param ctx the parse tree
	 */
	void exitExplainType(DALStatementParser.ExplainTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#explainableStatement}.
	 * @param ctx the parse tree
	 */
	void enterExplainableStatement(DALStatementParser.ExplainableStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#explainableStatement}.
	 * @param ctx the parse tree
	 */
	void exitExplainableStatement(DALStatementParser.ExplainableStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#formatName}.
	 * @param ctx the parse tree
	 */
	void enterFormatName(DALStatementParser.FormatNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#formatName}.
	 * @param ctx the parse tree
	 */
	void exitFormatName(DALStatementParser.FormatNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#parameterMarker}.
	 * @param ctx the parse tree
	 */
	void enterParameterMarker(DALStatementParser.ParameterMarkerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#parameterMarker}.
	 * @param ctx the parse tree
	 */
	void exitParameterMarker(DALStatementParser.ParameterMarkerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#customKeyword}.
	 * @param ctx the parse tree
	 */
	void enterCustomKeyword(DALStatementParser.CustomKeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#customKeyword}.
	 * @param ctx the parse tree
	 */
	void exitCustomKeyword(DALStatementParser.CustomKeywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#literals}.
	 * @param ctx the parse tree
	 */
	void enterLiterals(DALStatementParser.LiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#literals}.
	 * @param ctx the parse tree
	 */
	void exitLiterals(DALStatementParser.LiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#stringLiterals}.
	 * @param ctx the parse tree
	 */
	void enterStringLiterals(DALStatementParser.StringLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#stringLiterals}.
	 * @param ctx the parse tree
	 */
	void exitStringLiterals(DALStatementParser.StringLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#numberLiterals}.
	 * @param ctx the parse tree
	 */
	void enterNumberLiterals(DALStatementParser.NumberLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#numberLiterals}.
	 * @param ctx the parse tree
	 */
	void exitNumberLiterals(DALStatementParser.NumberLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#dateTimeLiterals}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeLiterals(DALStatementParser.DateTimeLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#dateTimeLiterals}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeLiterals(DALStatementParser.DateTimeLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#hexadecimalLiterals}.
	 * @param ctx the parse tree
	 */
	void enterHexadecimalLiterals(DALStatementParser.HexadecimalLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#hexadecimalLiterals}.
	 * @param ctx the parse tree
	 */
	void exitHexadecimalLiterals(DALStatementParser.HexadecimalLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#bitValueLiterals}.
	 * @param ctx the parse tree
	 */
	void enterBitValueLiterals(DALStatementParser.BitValueLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#bitValueLiterals}.
	 * @param ctx the parse tree
	 */
	void exitBitValueLiterals(DALStatementParser.BitValueLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#booleanLiterals}.
	 * @param ctx the parse tree
	 */
	void enterBooleanLiterals(DALStatementParser.BooleanLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#booleanLiterals}.
	 * @param ctx the parse tree
	 */
	void exitBooleanLiterals(DALStatementParser.BooleanLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#nullValueLiterals}.
	 * @param ctx the parse tree
	 */
	void enterNullValueLiterals(DALStatementParser.NullValueLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#nullValueLiterals}.
	 * @param ctx the parse tree
	 */
	void exitNullValueLiterals(DALStatementParser.NullValueLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#characterSetName_}.
	 * @param ctx the parse tree
	 */
	void enterCharacterSetName_(DALStatementParser.CharacterSetName_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#characterSetName_}.
	 * @param ctx the parse tree
	 */
	void exitCharacterSetName_(DALStatementParser.CharacterSetName_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#collationName_}.
	 * @param ctx the parse tree
	 */
	void enterCollationName_(DALStatementParser.CollationName_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#collationName_}.
	 * @param ctx the parse tree
	 */
	void exitCollationName_(DALStatementParser.CollationName_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(DALStatementParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(DALStatementParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#unreservedWord}.
	 * @param ctx the parse tree
	 */
	void enterUnreservedWord(DALStatementParser.UnreservedWordContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#unreservedWord}.
	 * @param ctx the parse tree
	 */
	void exitUnreservedWord(DALStatementParser.UnreservedWordContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#variable}.
	 * @param ctx the parse tree
	 */
	void enterVariable(DALStatementParser.VariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#variable}.
	 * @param ctx the parse tree
	 */
	void exitVariable(DALStatementParser.VariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#scope}.
	 * @param ctx the parse tree
	 */
	void enterScope(DALStatementParser.ScopeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#scope}.
	 * @param ctx the parse tree
	 */
	void exitScope(DALStatementParser.ScopeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#schemaName}.
	 * @param ctx the parse tree
	 */
	void enterSchemaName(DALStatementParser.SchemaNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#schemaName}.
	 * @param ctx the parse tree
	 */
	void exitSchemaName(DALStatementParser.SchemaNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#schemaNames}.
	 * @param ctx the parse tree
	 */
	void enterSchemaNames(DALStatementParser.SchemaNamesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#schemaNames}.
	 * @param ctx the parse tree
	 */
	void exitSchemaNames(DALStatementParser.SchemaNamesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#schemaPairs}.
	 * @param ctx the parse tree
	 */
	void enterSchemaPairs(DALStatementParser.SchemaPairsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#schemaPairs}.
	 * @param ctx the parse tree
	 */
	void exitSchemaPairs(DALStatementParser.SchemaPairsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#schemaPair}.
	 * @param ctx the parse tree
	 */
	void enterSchemaPair(DALStatementParser.SchemaPairContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#schemaPair}.
	 * @param ctx the parse tree
	 */
	void exitSchemaPair(DALStatementParser.SchemaPairContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#tableName}.
	 * @param ctx the parse tree
	 */
	void enterTableName(DALStatementParser.TableNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#tableName}.
	 * @param ctx the parse tree
	 */
	void exitTableName(DALStatementParser.TableNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#columnName}.
	 * @param ctx the parse tree
	 */
	void enterColumnName(DALStatementParser.ColumnNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#columnName}.
	 * @param ctx the parse tree
	 */
	void exitColumnName(DALStatementParser.ColumnNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#indexName}.
	 * @param ctx the parse tree
	 */
	void enterIndexName(DALStatementParser.IndexNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#indexName}.
	 * @param ctx the parse tree
	 */
	void exitIndexName(DALStatementParser.IndexNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#userName}.
	 * @param ctx the parse tree
	 */
	void enterUserName(DALStatementParser.UserNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#userName}.
	 * @param ctx the parse tree
	 */
	void exitUserName(DALStatementParser.UserNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#eventName}.
	 * @param ctx the parse tree
	 */
	void enterEventName(DALStatementParser.EventNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#eventName}.
	 * @param ctx the parse tree
	 */
	void exitEventName(DALStatementParser.EventNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#serverName}.
	 * @param ctx the parse tree
	 */
	void enterServerName(DALStatementParser.ServerNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#serverName}.
	 * @param ctx the parse tree
	 */
	void exitServerName(DALStatementParser.ServerNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#wrapperName}.
	 * @param ctx the parse tree
	 */
	void enterWrapperName(DALStatementParser.WrapperNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#wrapperName}.
	 * @param ctx the parse tree
	 */
	void exitWrapperName(DALStatementParser.WrapperNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#functionName}.
	 * @param ctx the parse tree
	 */
	void enterFunctionName(DALStatementParser.FunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#functionName}.
	 * @param ctx the parse tree
	 */
	void exitFunctionName(DALStatementParser.FunctionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#viewName}.
	 * @param ctx the parse tree
	 */
	void enterViewName(DALStatementParser.ViewNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#viewName}.
	 * @param ctx the parse tree
	 */
	void exitViewName(DALStatementParser.ViewNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#owner}.
	 * @param ctx the parse tree
	 */
	void enterOwner(DALStatementParser.OwnerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#owner}.
	 * @param ctx the parse tree
	 */
	void exitOwner(DALStatementParser.OwnerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#alias}.
	 * @param ctx the parse tree
	 */
	void enterAlias(DALStatementParser.AliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#alias}.
	 * @param ctx the parse tree
	 */
	void exitAlias(DALStatementParser.AliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#name}.
	 * @param ctx the parse tree
	 */
	void enterName(DALStatementParser.NameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#name}.
	 * @param ctx the parse tree
	 */
	void exitName(DALStatementParser.NameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#tableNames}.
	 * @param ctx the parse tree
	 */
	void enterTableNames(DALStatementParser.TableNamesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#tableNames}.
	 * @param ctx the parse tree
	 */
	void exitTableNames(DALStatementParser.TableNamesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#columnNames}.
	 * @param ctx the parse tree
	 */
	void enterColumnNames(DALStatementParser.ColumnNamesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#columnNames}.
	 * @param ctx the parse tree
	 */
	void exitColumnNames(DALStatementParser.ColumnNamesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#groupName}.
	 * @param ctx the parse tree
	 */
	void enterGroupName(DALStatementParser.GroupNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#groupName}.
	 * @param ctx the parse tree
	 */
	void exitGroupName(DALStatementParser.GroupNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#routineName}.
	 * @param ctx the parse tree
	 */
	void enterRoutineName(DALStatementParser.RoutineNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#routineName}.
	 * @param ctx the parse tree
	 */
	void exitRoutineName(DALStatementParser.RoutineNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#shardLibraryName}.
	 * @param ctx the parse tree
	 */
	void enterShardLibraryName(DALStatementParser.ShardLibraryNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#shardLibraryName}.
	 * @param ctx the parse tree
	 */
	void exitShardLibraryName(DALStatementParser.ShardLibraryNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#componentName}.
	 * @param ctx the parse tree
	 */
	void enterComponentName(DALStatementParser.ComponentNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#componentName}.
	 * @param ctx the parse tree
	 */
	void exitComponentName(DALStatementParser.ComponentNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#pluginName}.
	 * @param ctx the parse tree
	 */
	void enterPluginName(DALStatementParser.PluginNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#pluginName}.
	 * @param ctx the parse tree
	 */
	void exitPluginName(DALStatementParser.PluginNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#hostName}.
	 * @param ctx the parse tree
	 */
	void enterHostName(DALStatementParser.HostNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#hostName}.
	 * @param ctx the parse tree
	 */
	void exitHostName(DALStatementParser.HostNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#port}.
	 * @param ctx the parse tree
	 */
	void enterPort(DALStatementParser.PortContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#port}.
	 * @param ctx the parse tree
	 */
	void exitPort(DALStatementParser.PortContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#cloneInstance}.
	 * @param ctx the parse tree
	 */
	void enterCloneInstance(DALStatementParser.CloneInstanceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#cloneInstance}.
	 * @param ctx the parse tree
	 */
	void exitCloneInstance(DALStatementParser.CloneInstanceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#cloneDir}.
	 * @param ctx the parse tree
	 */
	void enterCloneDir(DALStatementParser.CloneDirContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#cloneDir}.
	 * @param ctx the parse tree
	 */
	void exitCloneDir(DALStatementParser.CloneDirContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#channelName}.
	 * @param ctx the parse tree
	 */
	void enterChannelName(DALStatementParser.ChannelNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#channelName}.
	 * @param ctx the parse tree
	 */
	void exitChannelName(DALStatementParser.ChannelNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#logName}.
	 * @param ctx the parse tree
	 */
	void enterLogName(DALStatementParser.LogNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#logName}.
	 * @param ctx the parse tree
	 */
	void exitLogName(DALStatementParser.LogNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#roleName}.
	 * @param ctx the parse tree
	 */
	void enterRoleName(DALStatementParser.RoleNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#roleName}.
	 * @param ctx the parse tree
	 */
	void exitRoleName(DALStatementParser.RoleNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#engineName}.
	 * @param ctx the parse tree
	 */
	void enterEngineName(DALStatementParser.EngineNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#engineName}.
	 * @param ctx the parse tree
	 */
	void exitEngineName(DALStatementParser.EngineNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#triggerName}.
	 * @param ctx the parse tree
	 */
	void enterTriggerName(DALStatementParser.TriggerNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#triggerName}.
	 * @param ctx the parse tree
	 */
	void exitTriggerName(DALStatementParser.TriggerNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#triggerTime}.
	 * @param ctx the parse tree
	 */
	void enterTriggerTime(DALStatementParser.TriggerTimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#triggerTime}.
	 * @param ctx the parse tree
	 */
	void exitTriggerTime(DALStatementParser.TriggerTimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#userOrRole}.
	 * @param ctx the parse tree
	 */
	void enterUserOrRole(DALStatementParser.UserOrRoleContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#userOrRole}.
	 * @param ctx the parse tree
	 */
	void exitUserOrRole(DALStatementParser.UserOrRoleContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#partitionName}.
	 * @param ctx the parse tree
	 */
	void enterPartitionName(DALStatementParser.PartitionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#partitionName}.
	 * @param ctx the parse tree
	 */
	void exitPartitionName(DALStatementParser.PartitionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#triggerEvent}.
	 * @param ctx the parse tree
	 */
	void enterTriggerEvent(DALStatementParser.TriggerEventContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#triggerEvent}.
	 * @param ctx the parse tree
	 */
	void exitTriggerEvent(DALStatementParser.TriggerEventContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#triggerOrder}.
	 * @param ctx the parse tree
	 */
	void enterTriggerOrder(DALStatementParser.TriggerOrderContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#triggerOrder}.
	 * @param ctx the parse tree
	 */
	void exitTriggerOrder(DALStatementParser.TriggerOrderContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(DALStatementParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(DALStatementParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#logicalOperator}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOperator(DALStatementParser.LogicalOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#logicalOperator}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOperator(DALStatementParser.LogicalOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#notOperator_}.
	 * @param ctx the parse tree
	 */
	void enterNotOperator_(DALStatementParser.NotOperator_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#notOperator_}.
	 * @param ctx the parse tree
	 */
	void exitNotOperator_(DALStatementParser.NotOperator_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#booleanPrimary}.
	 * @param ctx the parse tree
	 */
	void enterBooleanPrimary(DALStatementParser.BooleanPrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#booleanPrimary}.
	 * @param ctx the parse tree
	 */
	void exitBooleanPrimary(DALStatementParser.BooleanPrimaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOperator(DALStatementParser.ComparisonOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOperator(DALStatementParser.ComparisonOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterPredicate(DALStatementParser.PredicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitPredicate(DALStatementParser.PredicateContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#bitExpr}.
	 * @param ctx the parse tree
	 */
	void enterBitExpr(DALStatementParser.BitExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#bitExpr}.
	 * @param ctx the parse tree
	 */
	void exitBitExpr(DALStatementParser.BitExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#simpleExpr}.
	 * @param ctx the parse tree
	 */
	void enterSimpleExpr(DALStatementParser.SimpleExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#simpleExpr}.
	 * @param ctx the parse tree
	 */
	void exitSimpleExpr(DALStatementParser.SimpleExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCall(DALStatementParser.FunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCall(DALStatementParser.FunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#aggregationFunction}.
	 * @param ctx the parse tree
	 */
	void enterAggregationFunction(DALStatementParser.AggregationFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#aggregationFunction}.
	 * @param ctx the parse tree
	 */
	void exitAggregationFunction(DALStatementParser.AggregationFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#aggregationFunctionName}.
	 * @param ctx the parse tree
	 */
	void enterAggregationFunctionName(DALStatementParser.AggregationFunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#aggregationFunctionName}.
	 * @param ctx the parse tree
	 */
	void exitAggregationFunctionName(DALStatementParser.AggregationFunctionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#distinct}.
	 * @param ctx the parse tree
	 */
	void enterDistinct(DALStatementParser.DistinctContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#distinct}.
	 * @param ctx the parse tree
	 */
	void exitDistinct(DALStatementParser.DistinctContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#overClause_}.
	 * @param ctx the parse tree
	 */
	void enterOverClause_(DALStatementParser.OverClause_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#overClause_}.
	 * @param ctx the parse tree
	 */
	void exitOverClause_(DALStatementParser.OverClause_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#windowSpecification_}.
	 * @param ctx the parse tree
	 */
	void enterWindowSpecification_(DALStatementParser.WindowSpecification_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#windowSpecification_}.
	 * @param ctx the parse tree
	 */
	void exitWindowSpecification_(DALStatementParser.WindowSpecification_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#partitionClause_}.
	 * @param ctx the parse tree
	 */
	void enterPartitionClause_(DALStatementParser.PartitionClause_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#partitionClause_}.
	 * @param ctx the parse tree
	 */
	void exitPartitionClause_(DALStatementParser.PartitionClause_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#frameClause_}.
	 * @param ctx the parse tree
	 */
	void enterFrameClause_(DALStatementParser.FrameClause_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#frameClause_}.
	 * @param ctx the parse tree
	 */
	void exitFrameClause_(DALStatementParser.FrameClause_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#frameStart_}.
	 * @param ctx the parse tree
	 */
	void enterFrameStart_(DALStatementParser.FrameStart_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#frameStart_}.
	 * @param ctx the parse tree
	 */
	void exitFrameStart_(DALStatementParser.FrameStart_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#frameEnd_}.
	 * @param ctx the parse tree
	 */
	void enterFrameEnd_(DALStatementParser.FrameEnd_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#frameEnd_}.
	 * @param ctx the parse tree
	 */
	void exitFrameEnd_(DALStatementParser.FrameEnd_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#frameBetween_}.
	 * @param ctx the parse tree
	 */
	void enterFrameBetween_(DALStatementParser.FrameBetween_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#frameBetween_}.
	 * @param ctx the parse tree
	 */
	void exitFrameBetween_(DALStatementParser.FrameBetween_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#specialFunction}.
	 * @param ctx the parse tree
	 */
	void enterSpecialFunction(DALStatementParser.SpecialFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#specialFunction}.
	 * @param ctx the parse tree
	 */
	void exitSpecialFunction(DALStatementParser.SpecialFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#currentUserFunction}.
	 * @param ctx the parse tree
	 */
	void enterCurrentUserFunction(DALStatementParser.CurrentUserFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#currentUserFunction}.
	 * @param ctx the parse tree
	 */
	void exitCurrentUserFunction(DALStatementParser.CurrentUserFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#groupConcatFunction}.
	 * @param ctx the parse tree
	 */
	void enterGroupConcatFunction(DALStatementParser.GroupConcatFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#groupConcatFunction}.
	 * @param ctx the parse tree
	 */
	void exitGroupConcatFunction(DALStatementParser.GroupConcatFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#windowFunction}.
	 * @param ctx the parse tree
	 */
	void enterWindowFunction(DALStatementParser.WindowFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#windowFunction}.
	 * @param ctx the parse tree
	 */
	void exitWindowFunction(DALStatementParser.WindowFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#castFunction}.
	 * @param ctx the parse tree
	 */
	void enterCastFunction(DALStatementParser.CastFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#castFunction}.
	 * @param ctx the parse tree
	 */
	void exitCastFunction(DALStatementParser.CastFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#convertFunction}.
	 * @param ctx the parse tree
	 */
	void enterConvertFunction(DALStatementParser.ConvertFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#convertFunction}.
	 * @param ctx the parse tree
	 */
	void exitConvertFunction(DALStatementParser.ConvertFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#positionFunction}.
	 * @param ctx the parse tree
	 */
	void enterPositionFunction(DALStatementParser.PositionFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#positionFunction}.
	 * @param ctx the parse tree
	 */
	void exitPositionFunction(DALStatementParser.PositionFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#substringFunction}.
	 * @param ctx the parse tree
	 */
	void enterSubstringFunction(DALStatementParser.SubstringFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#substringFunction}.
	 * @param ctx the parse tree
	 */
	void exitSubstringFunction(DALStatementParser.SubstringFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#extractFunction}.
	 * @param ctx the parse tree
	 */
	void enterExtractFunction(DALStatementParser.ExtractFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#extractFunction}.
	 * @param ctx the parse tree
	 */
	void exitExtractFunction(DALStatementParser.ExtractFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#charFunction}.
	 * @param ctx the parse tree
	 */
	void enterCharFunction(DALStatementParser.CharFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#charFunction}.
	 * @param ctx the parse tree
	 */
	void exitCharFunction(DALStatementParser.CharFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#trimFunction_}.
	 * @param ctx the parse tree
	 */
	void enterTrimFunction_(DALStatementParser.TrimFunction_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#trimFunction_}.
	 * @param ctx the parse tree
	 */
	void exitTrimFunction_(DALStatementParser.TrimFunction_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#valuesFunction_}.
	 * @param ctx the parse tree
	 */
	void enterValuesFunction_(DALStatementParser.ValuesFunction_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#valuesFunction_}.
	 * @param ctx the parse tree
	 */
	void exitValuesFunction_(DALStatementParser.ValuesFunction_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#weightStringFunction}.
	 * @param ctx the parse tree
	 */
	void enterWeightStringFunction(DALStatementParser.WeightStringFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#weightStringFunction}.
	 * @param ctx the parse tree
	 */
	void exitWeightStringFunction(DALStatementParser.WeightStringFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#levelClause_}.
	 * @param ctx the parse tree
	 */
	void enterLevelClause_(DALStatementParser.LevelClause_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#levelClause_}.
	 * @param ctx the parse tree
	 */
	void exitLevelClause_(DALStatementParser.LevelClause_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#levelInWeightListElement_}.
	 * @param ctx the parse tree
	 */
	void enterLevelInWeightListElement_(DALStatementParser.LevelInWeightListElement_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#levelInWeightListElement_}.
	 * @param ctx the parse tree
	 */
	void exitLevelInWeightListElement_(DALStatementParser.LevelInWeightListElement_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#regularFunction}.
	 * @param ctx the parse tree
	 */
	void enterRegularFunction(DALStatementParser.RegularFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#regularFunction}.
	 * @param ctx the parse tree
	 */
	void exitRegularFunction(DALStatementParser.RegularFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#shorthandRegularFunction}.
	 * @param ctx the parse tree
	 */
	void enterShorthandRegularFunction(DALStatementParser.ShorthandRegularFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#shorthandRegularFunction}.
	 * @param ctx the parse tree
	 */
	void exitShorthandRegularFunction(DALStatementParser.ShorthandRegularFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#completeRegularFunction}.
	 * @param ctx the parse tree
	 */
	void enterCompleteRegularFunction(DALStatementParser.CompleteRegularFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#completeRegularFunction}.
	 * @param ctx the parse tree
	 */
	void exitCompleteRegularFunction(DALStatementParser.CompleteRegularFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#regularFunctionName_}.
	 * @param ctx the parse tree
	 */
	void enterRegularFunctionName_(DALStatementParser.RegularFunctionName_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#regularFunctionName_}.
	 * @param ctx the parse tree
	 */
	void exitRegularFunctionName_(DALStatementParser.RegularFunctionName_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#matchExpression_}.
	 * @param ctx the parse tree
	 */
	void enterMatchExpression_(DALStatementParser.MatchExpression_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#matchExpression_}.
	 * @param ctx the parse tree
	 */
	void exitMatchExpression_(DALStatementParser.MatchExpression_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#matchSearchModifier_}.
	 * @param ctx the parse tree
	 */
	void enterMatchSearchModifier_(DALStatementParser.MatchSearchModifier_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#matchSearchModifier_}.
	 * @param ctx the parse tree
	 */
	void exitMatchSearchModifier_(DALStatementParser.MatchSearchModifier_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#caseExpression}.
	 * @param ctx the parse tree
	 */
	void enterCaseExpression(DALStatementParser.CaseExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#caseExpression}.
	 * @param ctx the parse tree
	 */
	void exitCaseExpression(DALStatementParser.CaseExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#datetimeExpr}.
	 * @param ctx the parse tree
	 */
	void enterDatetimeExpr(DALStatementParser.DatetimeExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#datetimeExpr}.
	 * @param ctx the parse tree
	 */
	void exitDatetimeExpr(DALStatementParser.DatetimeExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#binaryLogFileIndexNumber}.
	 * @param ctx the parse tree
	 */
	void enterBinaryLogFileIndexNumber(DALStatementParser.BinaryLogFileIndexNumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#binaryLogFileIndexNumber}.
	 * @param ctx the parse tree
	 */
	void exitBinaryLogFileIndexNumber(DALStatementParser.BinaryLogFileIndexNumberContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#caseWhen_}.
	 * @param ctx the parse tree
	 */
	void enterCaseWhen_(DALStatementParser.CaseWhen_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#caseWhen_}.
	 * @param ctx the parse tree
	 */
	void exitCaseWhen_(DALStatementParser.CaseWhen_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#caseElse_}.
	 * @param ctx the parse tree
	 */
	void enterCaseElse_(DALStatementParser.CaseElse_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#caseElse_}.
	 * @param ctx the parse tree
	 */
	void exitCaseElse_(DALStatementParser.CaseElse_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#intervalExpression}.
	 * @param ctx the parse tree
	 */
	void enterIntervalExpression(DALStatementParser.IntervalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#intervalExpression}.
	 * @param ctx the parse tree
	 */
	void exitIntervalExpression(DALStatementParser.IntervalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#intervalValue}.
	 * @param ctx the parse tree
	 */
	void enterIntervalValue(DALStatementParser.IntervalValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#intervalValue}.
	 * @param ctx the parse tree
	 */
	void exitIntervalValue(DALStatementParser.IntervalValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#intervalUnit_}.
	 * @param ctx the parse tree
	 */
	void enterIntervalUnit_(DALStatementParser.IntervalUnit_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#intervalUnit_}.
	 * @param ctx the parse tree
	 */
	void exitIntervalUnit_(DALStatementParser.IntervalUnit_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#subquery}.
	 * @param ctx the parse tree
	 */
	void enterSubquery(DALStatementParser.SubqueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#subquery}.
	 * @param ctx the parse tree
	 */
	void exitSubquery(DALStatementParser.SubqueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#orderByClause}.
	 * @param ctx the parse tree
	 */
	void enterOrderByClause(DALStatementParser.OrderByClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#orderByClause}.
	 * @param ctx the parse tree
	 */
	void exitOrderByClause(DALStatementParser.OrderByClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#orderByItem}.
	 * @param ctx the parse tree
	 */
	void enterOrderByItem(DALStatementParser.OrderByItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#orderByItem}.
	 * @param ctx the parse tree
	 */
	void exitOrderByItem(DALStatementParser.OrderByItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterDataType(DALStatementParser.DataTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitDataType(DALStatementParser.DataTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#dataTypeName}.
	 * @param ctx the parse tree
	 */
	void enterDataTypeName(DALStatementParser.DataTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#dataTypeName}.
	 * @param ctx the parse tree
	 */
	void exitDataTypeName(DALStatementParser.DataTypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#dataTypeLength}.
	 * @param ctx the parse tree
	 */
	void enterDataTypeLength(DALStatementParser.DataTypeLengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#dataTypeLength}.
	 * @param ctx the parse tree
	 */
	void exitDataTypeLength(DALStatementParser.DataTypeLengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#collectionOptions}.
	 * @param ctx the parse tree
	 */
	void enterCollectionOptions(DALStatementParser.CollectionOptionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#collectionOptions}.
	 * @param ctx the parse tree
	 */
	void exitCollectionOptions(DALStatementParser.CollectionOptionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#characterSet_}.
	 * @param ctx the parse tree
	 */
	void enterCharacterSet_(DALStatementParser.CharacterSet_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#characterSet_}.
	 * @param ctx the parse tree
	 */
	void exitCharacterSet_(DALStatementParser.CharacterSet_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#collateClause_}.
	 * @param ctx the parse tree
	 */
	void enterCollateClause_(DALStatementParser.CollateClause_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#collateClause_}.
	 * @param ctx the parse tree
	 */
	void exitCollateClause_(DALStatementParser.CollateClause_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#ignoredIdentifier_}.
	 * @param ctx the parse tree
	 */
	void enterIgnoredIdentifier_(DALStatementParser.IgnoredIdentifier_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#ignoredIdentifier_}.
	 * @param ctx the parse tree
	 */
	void exitIgnoredIdentifier_(DALStatementParser.IgnoredIdentifier_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#ignoredIdentifiers_}.
	 * @param ctx the parse tree
	 */
	void enterIgnoredIdentifiers_(DALStatementParser.IgnoredIdentifiers_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#ignoredIdentifiers_}.
	 * @param ctx the parse tree
	 */
	void exitIgnoredIdentifiers_(DALStatementParser.IgnoredIdentifiers_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#fieldOrVarSpec}.
	 * @param ctx the parse tree
	 */
	void enterFieldOrVarSpec(DALStatementParser.FieldOrVarSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#fieldOrVarSpec}.
	 * @param ctx the parse tree
	 */
	void exitFieldOrVarSpec(DALStatementParser.FieldOrVarSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#notExistClause_}.
	 * @param ctx the parse tree
	 */
	void enterNotExistClause_(DALStatementParser.NotExistClause_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#notExistClause_}.
	 * @param ctx the parse tree
	 */
	void exitNotExistClause_(DALStatementParser.NotExistClause_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#existClause_}.
	 * @param ctx the parse tree
	 */
	void enterExistClause_(DALStatementParser.ExistClause_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#existClause_}.
	 * @param ctx the parse tree
	 */
	void exitExistClause_(DALStatementParser.ExistClause_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#pattern}.
	 * @param ctx the parse tree
	 */
	void enterPattern(DALStatementParser.PatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#pattern}.
	 * @param ctx the parse tree
	 */
	void exitPattern(DALStatementParser.PatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#connectionId_}.
	 * @param ctx the parse tree
	 */
	void enterConnectionId_(DALStatementParser.ConnectionId_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#connectionId_}.
	 * @param ctx the parse tree
	 */
	void exitConnectionId_(DALStatementParser.ConnectionId_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#labelName}.
	 * @param ctx the parse tree
	 */
	void enterLabelName(DALStatementParser.LabelNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#labelName}.
	 * @param ctx the parse tree
	 */
	void exitLabelName(DALStatementParser.LabelNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#cursorName}.
	 * @param ctx the parse tree
	 */
	void enterCursorName(DALStatementParser.CursorNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#cursorName}.
	 * @param ctx the parse tree
	 */
	void exitCursorName(DALStatementParser.CursorNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#conditionName}.
	 * @param ctx the parse tree
	 */
	void enterConditionName(DALStatementParser.ConditionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#conditionName}.
	 * @param ctx the parse tree
	 */
	void exitConditionName(DALStatementParser.ConditionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#insert}.
	 * @param ctx the parse tree
	 */
	void enterInsert(DALStatementParser.InsertContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#insert}.
	 * @param ctx the parse tree
	 */
	void exitInsert(DALStatementParser.InsertContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#insertSpecification_}.
	 * @param ctx the parse tree
	 */
	void enterInsertSpecification_(DALStatementParser.InsertSpecification_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#insertSpecification_}.
	 * @param ctx the parse tree
	 */
	void exitInsertSpecification_(DALStatementParser.InsertSpecification_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#insertValuesClause}.
	 * @param ctx the parse tree
	 */
	void enterInsertValuesClause(DALStatementParser.InsertValuesClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#insertValuesClause}.
	 * @param ctx the parse tree
	 */
	void exitInsertValuesClause(DALStatementParser.InsertValuesClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#insertSelectClause}.
	 * @param ctx the parse tree
	 */
	void enterInsertSelectClause(DALStatementParser.InsertSelectClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#insertSelectClause}.
	 * @param ctx the parse tree
	 */
	void exitInsertSelectClause(DALStatementParser.InsertSelectClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#onDuplicateKeyClause}.
	 * @param ctx the parse tree
	 */
	void enterOnDuplicateKeyClause(DALStatementParser.OnDuplicateKeyClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#onDuplicateKeyClause}.
	 * @param ctx the parse tree
	 */
	void exitOnDuplicateKeyClause(DALStatementParser.OnDuplicateKeyClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#valueReference_}.
	 * @param ctx the parse tree
	 */
	void enterValueReference_(DALStatementParser.ValueReference_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#valueReference_}.
	 * @param ctx the parse tree
	 */
	void exitValueReference_(DALStatementParser.ValueReference_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#derivedColumns_}.
	 * @param ctx the parse tree
	 */
	void enterDerivedColumns_(DALStatementParser.DerivedColumns_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#derivedColumns_}.
	 * @param ctx the parse tree
	 */
	void exitDerivedColumns_(DALStatementParser.DerivedColumns_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#replace}.
	 * @param ctx the parse tree
	 */
	void enterReplace(DALStatementParser.ReplaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#replace}.
	 * @param ctx the parse tree
	 */
	void exitReplace(DALStatementParser.ReplaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#replaceSpecification_}.
	 * @param ctx the parse tree
	 */
	void enterReplaceSpecification_(DALStatementParser.ReplaceSpecification_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#replaceSpecification_}.
	 * @param ctx the parse tree
	 */
	void exitReplaceSpecification_(DALStatementParser.ReplaceSpecification_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#replaceValuesClause}.
	 * @param ctx the parse tree
	 */
	void enterReplaceValuesClause(DALStatementParser.ReplaceValuesClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#replaceValuesClause}.
	 * @param ctx the parse tree
	 */
	void exitReplaceValuesClause(DALStatementParser.ReplaceValuesClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#replaceSelectClause}.
	 * @param ctx the parse tree
	 */
	void enterReplaceSelectClause(DALStatementParser.ReplaceSelectClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#replaceSelectClause}.
	 * @param ctx the parse tree
	 */
	void exitReplaceSelectClause(DALStatementParser.ReplaceSelectClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#update}.
	 * @param ctx the parse tree
	 */
	void enterUpdate(DALStatementParser.UpdateContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#update}.
	 * @param ctx the parse tree
	 */
	void exitUpdate(DALStatementParser.UpdateContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#updateSpecification_}.
	 * @param ctx the parse tree
	 */
	void enterUpdateSpecification_(DALStatementParser.UpdateSpecification_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#updateSpecification_}.
	 * @param ctx the parse tree
	 */
	void exitUpdateSpecification_(DALStatementParser.UpdateSpecification_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(DALStatementParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(DALStatementParser.AssignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#setAssignmentsClause}.
	 * @param ctx the parse tree
	 */
	void enterSetAssignmentsClause(DALStatementParser.SetAssignmentsClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#setAssignmentsClause}.
	 * @param ctx the parse tree
	 */
	void exitSetAssignmentsClause(DALStatementParser.SetAssignmentsClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#assignmentValues}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentValues(DALStatementParser.AssignmentValuesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#assignmentValues}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentValues(DALStatementParser.AssignmentValuesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentValue(DALStatementParser.AssignmentValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentValue(DALStatementParser.AssignmentValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#blobValue}.
	 * @param ctx the parse tree
	 */
	void enterBlobValue(DALStatementParser.BlobValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#blobValue}.
	 * @param ctx the parse tree
	 */
	void exitBlobValue(DALStatementParser.BlobValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#delete}.
	 * @param ctx the parse tree
	 */
	void enterDelete(DALStatementParser.DeleteContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#delete}.
	 * @param ctx the parse tree
	 */
	void exitDelete(DALStatementParser.DeleteContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#deleteSpecification_}.
	 * @param ctx the parse tree
	 */
	void enterDeleteSpecification_(DALStatementParser.DeleteSpecification_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#deleteSpecification_}.
	 * @param ctx the parse tree
	 */
	void exitDeleteSpecification_(DALStatementParser.DeleteSpecification_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#singleTableClause}.
	 * @param ctx the parse tree
	 */
	void enterSingleTableClause(DALStatementParser.SingleTableClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#singleTableClause}.
	 * @param ctx the parse tree
	 */
	void exitSingleTableClause(DALStatementParser.SingleTableClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#multipleTablesClause}.
	 * @param ctx the parse tree
	 */
	void enterMultipleTablesClause(DALStatementParser.MultipleTablesClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#multipleTablesClause}.
	 * @param ctx the parse tree
	 */
	void exitMultipleTablesClause(DALStatementParser.MultipleTablesClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#multipleTableNames}.
	 * @param ctx the parse tree
	 */
	void enterMultipleTableNames(DALStatementParser.MultipleTableNamesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#multipleTableNames}.
	 * @param ctx the parse tree
	 */
	void exitMultipleTableNames(DALStatementParser.MultipleTableNamesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#select}.
	 * @param ctx the parse tree
	 */
	void enterSelect(DALStatementParser.SelectContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#select}.
	 * @param ctx the parse tree
	 */
	void exitSelect(DALStatementParser.SelectContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#call}.
	 * @param ctx the parse tree
	 */
	void enterCall(DALStatementParser.CallContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#call}.
	 * @param ctx the parse tree
	 */
	void exitCall(DALStatementParser.CallContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#doStatement}.
	 * @param ctx the parse tree
	 */
	void enterDoStatement(DALStatementParser.DoStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#doStatement}.
	 * @param ctx the parse tree
	 */
	void exitDoStatement(DALStatementParser.DoStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#handlerStatement}.
	 * @param ctx the parse tree
	 */
	void enterHandlerStatement(DALStatementParser.HandlerStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#handlerStatement}.
	 * @param ctx the parse tree
	 */
	void exitHandlerStatement(DALStatementParser.HandlerStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#handlerOpenStatement}.
	 * @param ctx the parse tree
	 */
	void enterHandlerOpenStatement(DALStatementParser.HandlerOpenStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#handlerOpenStatement}.
	 * @param ctx the parse tree
	 */
	void exitHandlerOpenStatement(DALStatementParser.HandlerOpenStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#handlerReadIndexStatement}.
	 * @param ctx the parse tree
	 */
	void enterHandlerReadIndexStatement(DALStatementParser.HandlerReadIndexStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#handlerReadIndexStatement}.
	 * @param ctx the parse tree
	 */
	void exitHandlerReadIndexStatement(DALStatementParser.HandlerReadIndexStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#handlerReadStatement}.
	 * @param ctx the parse tree
	 */
	void enterHandlerReadStatement(DALStatementParser.HandlerReadStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#handlerReadStatement}.
	 * @param ctx the parse tree
	 */
	void exitHandlerReadStatement(DALStatementParser.HandlerReadStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#handlerCloseStatement}.
	 * @param ctx the parse tree
	 */
	void enterHandlerCloseStatement(DALStatementParser.HandlerCloseStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#handlerCloseStatement}.
	 * @param ctx the parse tree
	 */
	void exitHandlerCloseStatement(DALStatementParser.HandlerCloseStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#importStatement}.
	 * @param ctx the parse tree
	 */
	void enterImportStatement(DALStatementParser.ImportStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#importStatement}.
	 * @param ctx the parse tree
	 */
	void exitImportStatement(DALStatementParser.ImportStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#loadDataStatement}.
	 * @param ctx the parse tree
	 */
	void enterLoadDataStatement(DALStatementParser.LoadDataStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#loadDataStatement}.
	 * @param ctx the parse tree
	 */
	void exitLoadDataStatement(DALStatementParser.LoadDataStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#loadXmlStatement}.
	 * @param ctx the parse tree
	 */
	void enterLoadXmlStatement(DALStatementParser.LoadXmlStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#loadXmlStatement}.
	 * @param ctx the parse tree
	 */
	void exitLoadXmlStatement(DALStatementParser.LoadXmlStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#tableStatement}.
	 * @param ctx the parse tree
	 */
	void enterTableStatement(DALStatementParser.TableStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#tableStatement}.
	 * @param ctx the parse tree
	 */
	void exitTableStatement(DALStatementParser.TableStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#valuesStatement}.
	 * @param ctx the parse tree
	 */
	void enterValuesStatement(DALStatementParser.ValuesStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#valuesStatement}.
	 * @param ctx the parse tree
	 */
	void exitValuesStatement(DALStatementParser.ValuesStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#columnDesignator}.
	 * @param ctx the parse tree
	 */
	void enterColumnDesignator(DALStatementParser.ColumnDesignatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#columnDesignator}.
	 * @param ctx the parse tree
	 */
	void exitColumnDesignator(DALStatementParser.ColumnDesignatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#rowConstructorList}.
	 * @param ctx the parse tree
	 */
	void enterRowConstructorList(DALStatementParser.RowConstructorListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#rowConstructorList}.
	 * @param ctx the parse tree
	 */
	void exitRowConstructorList(DALStatementParser.RowConstructorListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#withClause_}.
	 * @param ctx the parse tree
	 */
	void enterWithClause_(DALStatementParser.WithClause_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#withClause_}.
	 * @param ctx the parse tree
	 */
	void exitWithClause_(DALStatementParser.WithClause_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#cteClause_}.
	 * @param ctx the parse tree
	 */
	void enterCteClause_(DALStatementParser.CteClause_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#cteClause_}.
	 * @param ctx the parse tree
	 */
	void exitCteClause_(DALStatementParser.CteClause_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#unionClause}.
	 * @param ctx the parse tree
	 */
	void enterUnionClause(DALStatementParser.UnionClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#unionClause}.
	 * @param ctx the parse tree
	 */
	void exitUnionClause(DALStatementParser.UnionClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#selectClause}.
	 * @param ctx the parse tree
	 */
	void enterSelectClause(DALStatementParser.SelectClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#selectClause}.
	 * @param ctx the parse tree
	 */
	void exitSelectClause(DALStatementParser.SelectClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#selectSpecification}.
	 * @param ctx the parse tree
	 */
	void enterSelectSpecification(DALStatementParser.SelectSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#selectSpecification}.
	 * @param ctx the parse tree
	 */
	void exitSelectSpecification(DALStatementParser.SelectSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#duplicateSpecification}.
	 * @param ctx the parse tree
	 */
	void enterDuplicateSpecification(DALStatementParser.DuplicateSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#duplicateSpecification}.
	 * @param ctx the parse tree
	 */
	void exitDuplicateSpecification(DALStatementParser.DuplicateSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#projections}.
	 * @param ctx the parse tree
	 */
	void enterProjections(DALStatementParser.ProjectionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#projections}.
	 * @param ctx the parse tree
	 */
	void exitProjections(DALStatementParser.ProjectionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#projection}.
	 * @param ctx the parse tree
	 */
	void enterProjection(DALStatementParser.ProjectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#projection}.
	 * @param ctx the parse tree
	 */
	void exitProjection(DALStatementParser.ProjectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#unqualifiedShorthand}.
	 * @param ctx the parse tree
	 */
	void enterUnqualifiedShorthand(DALStatementParser.UnqualifiedShorthandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#unqualifiedShorthand}.
	 * @param ctx the parse tree
	 */
	void exitUnqualifiedShorthand(DALStatementParser.UnqualifiedShorthandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#qualifiedShorthand}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedShorthand(DALStatementParser.QualifiedShorthandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#qualifiedShorthand}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedShorthand(DALStatementParser.QualifiedShorthandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void enterFromClause(DALStatementParser.FromClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void exitFromClause(DALStatementParser.FromClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#tableReferences}.
	 * @param ctx the parse tree
	 */
	void enterTableReferences(DALStatementParser.TableReferencesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#tableReferences}.
	 * @param ctx the parse tree
	 */
	void exitTableReferences(DALStatementParser.TableReferencesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#escapedTableReference}.
	 * @param ctx the parse tree
	 */
	void enterEscapedTableReference(DALStatementParser.EscapedTableReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#escapedTableReference}.
	 * @param ctx the parse tree
	 */
	void exitEscapedTableReference(DALStatementParser.EscapedTableReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#tableReference}.
	 * @param ctx the parse tree
	 */
	void enterTableReference(DALStatementParser.TableReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#tableReference}.
	 * @param ctx the parse tree
	 */
	void exitTableReference(DALStatementParser.TableReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#tableFactor}.
	 * @param ctx the parse tree
	 */
	void enterTableFactor(DALStatementParser.TableFactorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#tableFactor}.
	 * @param ctx the parse tree
	 */
	void exitTableFactor(DALStatementParser.TableFactorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#partitionNames_}.
	 * @param ctx the parse tree
	 */
	void enterPartitionNames_(DALStatementParser.PartitionNames_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#partitionNames_}.
	 * @param ctx the parse tree
	 */
	void exitPartitionNames_(DALStatementParser.PartitionNames_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#indexHintList_}.
	 * @param ctx the parse tree
	 */
	void enterIndexHintList_(DALStatementParser.IndexHintList_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#indexHintList_}.
	 * @param ctx the parse tree
	 */
	void exitIndexHintList_(DALStatementParser.IndexHintList_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#indexHint_}.
	 * @param ctx the parse tree
	 */
	void enterIndexHint_(DALStatementParser.IndexHint_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#indexHint_}.
	 * @param ctx the parse tree
	 */
	void exitIndexHint_(DALStatementParser.IndexHint_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#joinedTable}.
	 * @param ctx the parse tree
	 */
	void enterJoinedTable(DALStatementParser.JoinedTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#joinedTable}.
	 * @param ctx the parse tree
	 */
	void exitJoinedTable(DALStatementParser.JoinedTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#joinSpecification}.
	 * @param ctx the parse tree
	 */
	void enterJoinSpecification(DALStatementParser.JoinSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#joinSpecification}.
	 * @param ctx the parse tree
	 */
	void exitJoinSpecification(DALStatementParser.JoinSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void enterWhereClause(DALStatementParser.WhereClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void exitWhereClause(DALStatementParser.WhereClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#groupByClause}.
	 * @param ctx the parse tree
	 */
	void enterGroupByClause(DALStatementParser.GroupByClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#groupByClause}.
	 * @param ctx the parse tree
	 */
	void exitGroupByClause(DALStatementParser.GroupByClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#havingClause}.
	 * @param ctx the parse tree
	 */
	void enterHavingClause(DALStatementParser.HavingClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#havingClause}.
	 * @param ctx the parse tree
	 */
	void exitHavingClause(DALStatementParser.HavingClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#limitClause}.
	 * @param ctx the parse tree
	 */
	void enterLimitClause(DALStatementParser.LimitClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#limitClause}.
	 * @param ctx the parse tree
	 */
	void exitLimitClause(DALStatementParser.LimitClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#limitRowCount}.
	 * @param ctx the parse tree
	 */
	void enterLimitRowCount(DALStatementParser.LimitRowCountContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#limitRowCount}.
	 * @param ctx the parse tree
	 */
	void exitLimitRowCount(DALStatementParser.LimitRowCountContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#limitOffset}.
	 * @param ctx the parse tree
	 */
	void enterLimitOffset(DALStatementParser.LimitOffsetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#limitOffset}.
	 * @param ctx the parse tree
	 */
	void exitLimitOffset(DALStatementParser.LimitOffsetContext ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#windowClause_}.
	 * @param ctx the parse tree
	 */
	void enterWindowClause_(DALStatementParser.WindowClause_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#windowClause_}.
	 * @param ctx the parse tree
	 */
	void exitWindowClause_(DALStatementParser.WindowClause_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#windowItem_}.
	 * @param ctx the parse tree
	 */
	void enterWindowItem_(DALStatementParser.WindowItem_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#windowItem_}.
	 * @param ctx the parse tree
	 */
	void exitWindowItem_(DALStatementParser.WindowItem_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#selectLinesInto_}.
	 * @param ctx the parse tree
	 */
	void enterSelectLinesInto_(DALStatementParser.SelectLinesInto_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#selectLinesInto_}.
	 * @param ctx the parse tree
	 */
	void exitSelectLinesInto_(DALStatementParser.SelectLinesInto_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#selectFieldsInto_}.
	 * @param ctx the parse tree
	 */
	void enterSelectFieldsInto_(DALStatementParser.SelectFieldsInto_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#selectFieldsInto_}.
	 * @param ctx the parse tree
	 */
	void exitSelectFieldsInto_(DALStatementParser.SelectFieldsInto_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#selectIntoExpression_}.
	 * @param ctx the parse tree
	 */
	void enterSelectIntoExpression_(DALStatementParser.SelectIntoExpression_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#selectIntoExpression_}.
	 * @param ctx the parse tree
	 */
	void exitSelectIntoExpression_(DALStatementParser.SelectIntoExpression_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DALStatementParser#lockClause}.
	 * @param ctx the parse tree
	 */
	void enterLockClause(DALStatementParser.LockClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DALStatementParser#lockClause}.
	 * @param ctx the parse tree
	 */
	void exitLockClause(DALStatementParser.LockClauseContext ctx);
}