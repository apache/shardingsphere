// Generated from /home/totalo/code/shardingsphere/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-postgresql/src/main/antlr4/imports/postgresql/DDLStatement.g4 by ANTLR 4.9.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DDLStatementParser}.
 */
public interface DDLStatementListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createTable}.
	 * @param ctx the parse tree
	 */
	void enterCreateTable(DDLStatementParser.CreateTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createTable}.
	 * @param ctx the parse tree
	 */
	void exitCreateTable(DDLStatementParser.CreateTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#executeParamClause}.
	 * @param ctx the parse tree
	 */
	void enterExecuteParamClause(DDLStatementParser.ExecuteParamClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#executeParamClause}.
	 * @param ctx the parse tree
	 */
	void exitExecuteParamClause(DDLStatementParser.ExecuteParamClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#partitionBoundSpec}.
	 * @param ctx the parse tree
	 */
	void enterPartitionBoundSpec(DDLStatementParser.PartitionBoundSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#partitionBoundSpec}.
	 * @param ctx the parse tree
	 */
	void exitPartitionBoundSpec(DDLStatementParser.PartitionBoundSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#hashPartbound}.
	 * @param ctx the parse tree
	 */
	void enterHashPartbound(DDLStatementParser.HashPartboundContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#hashPartbound}.
	 * @param ctx the parse tree
	 */
	void exitHashPartbound(DDLStatementParser.HashPartboundContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#hashPartboundElem}.
	 * @param ctx the parse tree
	 */
	void enterHashPartboundElem(DDLStatementParser.HashPartboundElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#hashPartboundElem}.
	 * @param ctx the parse tree
	 */
	void exitHashPartboundElem(DDLStatementParser.HashPartboundElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#typedTableElementList}.
	 * @param ctx the parse tree
	 */
	void enterTypedTableElementList(DDLStatementParser.TypedTableElementListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#typedTableElementList}.
	 * @param ctx the parse tree
	 */
	void exitTypedTableElementList(DDLStatementParser.TypedTableElementListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#typedTableElement}.
	 * @param ctx the parse tree
	 */
	void enterTypedTableElement(DDLStatementParser.TypedTableElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#typedTableElement}.
	 * @param ctx the parse tree
	 */
	void exitTypedTableElement(DDLStatementParser.TypedTableElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#columnOptions}.
	 * @param ctx the parse tree
	 */
	void enterColumnOptions(DDLStatementParser.ColumnOptionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#columnOptions}.
	 * @param ctx the parse tree
	 */
	void exitColumnOptions(DDLStatementParser.ColumnOptionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#colQualList}.
	 * @param ctx the parse tree
	 */
	void enterColQualList(DDLStatementParser.ColQualListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#colQualList}.
	 * @param ctx the parse tree
	 */
	void exitColQualList(DDLStatementParser.ColQualListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#withData}.
	 * @param ctx the parse tree
	 */
	void enterWithData(DDLStatementParser.WithDataContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#withData}.
	 * @param ctx the parse tree
	 */
	void exitWithData(DDLStatementParser.WithDataContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#tableSpace}.
	 * @param ctx the parse tree
	 */
	void enterTableSpace(DDLStatementParser.TableSpaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#tableSpace}.
	 * @param ctx the parse tree
	 */
	void exitTableSpace(DDLStatementParser.TableSpaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#onCommitOption}.
	 * @param ctx the parse tree
	 */
	void enterOnCommitOption(DDLStatementParser.OnCommitOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#onCommitOption}.
	 * @param ctx the parse tree
	 */
	void exitOnCommitOption(DDLStatementParser.OnCommitOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#withOption}.
	 * @param ctx the parse tree
	 */
	void enterWithOption(DDLStatementParser.WithOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#withOption}.
	 * @param ctx the parse tree
	 */
	void exitWithOption(DDLStatementParser.WithOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#tableAccessMethodClause}.
	 * @param ctx the parse tree
	 */
	void enterTableAccessMethodClause(DDLStatementParser.TableAccessMethodClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#tableAccessMethodClause}.
	 * @param ctx the parse tree
	 */
	void exitTableAccessMethodClause(DDLStatementParser.TableAccessMethodClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#accessMethod}.
	 * @param ctx the parse tree
	 */
	void enterAccessMethod(DDLStatementParser.AccessMethodContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#accessMethod}.
	 * @param ctx the parse tree
	 */
	void exitAccessMethod(DDLStatementParser.AccessMethodContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createIndex}.
	 * @param ctx the parse tree
	 */
	void enterCreateIndex(DDLStatementParser.CreateIndexContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createIndex}.
	 * @param ctx the parse tree
	 */
	void exitCreateIndex(DDLStatementParser.CreateIndexContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#include}.
	 * @param ctx the parse tree
	 */
	void enterInclude(DDLStatementParser.IncludeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#include}.
	 * @param ctx the parse tree
	 */
	void exitInclude(DDLStatementParser.IncludeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#indexIncludingParams}.
	 * @param ctx the parse tree
	 */
	void enterIndexIncludingParams(DDLStatementParser.IndexIncludingParamsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#indexIncludingParams}.
	 * @param ctx the parse tree
	 */
	void exitIndexIncludingParams(DDLStatementParser.IndexIncludingParamsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#accessMethodClause}.
	 * @param ctx the parse tree
	 */
	void enterAccessMethodClause(DDLStatementParser.AccessMethodClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#accessMethodClause}.
	 * @param ctx the parse tree
	 */
	void exitAccessMethodClause(DDLStatementParser.AccessMethodClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createDatabase}.
	 * @param ctx the parse tree
	 */
	void enterCreateDatabase(DDLStatementParser.CreateDatabaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createDatabase}.
	 * @param ctx the parse tree
	 */
	void exitCreateDatabase(DDLStatementParser.CreateDatabaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createView}.
	 * @param ctx the parse tree
	 */
	void enterCreateView(DDLStatementParser.CreateViewContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createView}.
	 * @param ctx the parse tree
	 */
	void exitCreateView(DDLStatementParser.CreateViewContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#columnList}.
	 * @param ctx the parse tree
	 */
	void enterColumnList(DDLStatementParser.ColumnListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#columnList}.
	 * @param ctx the parse tree
	 */
	void exitColumnList(DDLStatementParser.ColumnListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#columnElem}.
	 * @param ctx the parse tree
	 */
	void enterColumnElem(DDLStatementParser.ColumnElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#columnElem}.
	 * @param ctx the parse tree
	 */
	void exitColumnElem(DDLStatementParser.ColumnElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropDatabase}.
	 * @param ctx the parse tree
	 */
	void enterDropDatabase(DDLStatementParser.DropDatabaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropDatabase}.
	 * @param ctx the parse tree
	 */
	void exitDropDatabase(DDLStatementParser.DropDatabaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createDatabaseSpecification}.
	 * @param ctx the parse tree
	 */
	void enterCreateDatabaseSpecification(DDLStatementParser.CreateDatabaseSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createDatabaseSpecification}.
	 * @param ctx the parse tree
	 */
	void exitCreateDatabaseSpecification(DDLStatementParser.CreateDatabaseSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createdbOptName}.
	 * @param ctx the parse tree
	 */
	void enterCreatedbOptName(DDLStatementParser.CreatedbOptNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createdbOptName}.
	 * @param ctx the parse tree
	 */
	void exitCreatedbOptName(DDLStatementParser.CreatedbOptNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterTable}.
	 * @param ctx the parse tree
	 */
	void enterAlterTable(DDLStatementParser.AlterTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterTable}.
	 * @param ctx the parse tree
	 */
	void exitAlterTable(DDLStatementParser.AlterTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterIndex}.
	 * @param ctx the parse tree
	 */
	void enterAlterIndex(DDLStatementParser.AlterIndexContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterIndex}.
	 * @param ctx the parse tree
	 */
	void exitAlterIndex(DDLStatementParser.AlterIndexContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropTable}.
	 * @param ctx the parse tree
	 */
	void enterDropTable(DDLStatementParser.DropTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropTable}.
	 * @param ctx the parse tree
	 */
	void exitDropTable(DDLStatementParser.DropTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropTableOpt}.
	 * @param ctx the parse tree
	 */
	void enterDropTableOpt(DDLStatementParser.DropTableOptContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropTableOpt}.
	 * @param ctx the parse tree
	 */
	void exitDropTableOpt(DDLStatementParser.DropTableOptContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropIndex}.
	 * @param ctx the parse tree
	 */
	void enterDropIndex(DDLStatementParser.DropIndexContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropIndex}.
	 * @param ctx the parse tree
	 */
	void exitDropIndex(DDLStatementParser.DropIndexContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropIndexOpt}.
	 * @param ctx the parse tree
	 */
	void enterDropIndexOpt(DDLStatementParser.DropIndexOptContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropIndexOpt}.
	 * @param ctx the parse tree
	 */
	void exitDropIndexOpt(DDLStatementParser.DropIndexOptContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#truncateTable}.
	 * @param ctx the parse tree
	 */
	void enterTruncateTable(DDLStatementParser.TruncateTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#truncateTable}.
	 * @param ctx the parse tree
	 */
	void exitTruncateTable(DDLStatementParser.TruncateTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#restartSeqs}.
	 * @param ctx the parse tree
	 */
	void enterRestartSeqs(DDLStatementParser.RestartSeqsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#restartSeqs}.
	 * @param ctx the parse tree
	 */
	void exitRestartSeqs(DDLStatementParser.RestartSeqsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createTableSpecification}.
	 * @param ctx the parse tree
	 */
	void enterCreateTableSpecification(DDLStatementParser.CreateTableSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createTableSpecification}.
	 * @param ctx the parse tree
	 */
	void exitCreateTableSpecification(DDLStatementParser.CreateTableSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createDefinitionClause}.
	 * @param ctx the parse tree
	 */
	void enterCreateDefinitionClause(DDLStatementParser.CreateDefinitionClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createDefinitionClause}.
	 * @param ctx the parse tree
	 */
	void exitCreateDefinitionClause(DDLStatementParser.CreateDefinitionClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createDefinition}.
	 * @param ctx the parse tree
	 */
	void enterCreateDefinition(DDLStatementParser.CreateDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createDefinition}.
	 * @param ctx the parse tree
	 */
	void exitCreateDefinition(DDLStatementParser.CreateDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#columnDefinition}.
	 * @param ctx the parse tree
	 */
	void enterColumnDefinition(DDLStatementParser.ColumnDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#columnDefinition}.
	 * @param ctx the parse tree
	 */
	void exitColumnDefinition(DDLStatementParser.ColumnDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void enterColumnConstraint(DDLStatementParser.ColumnConstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void exitColumnConstraint(DDLStatementParser.ColumnConstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#constraintClause}.
	 * @param ctx the parse tree
	 */
	void enterConstraintClause(DDLStatementParser.ConstraintClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#constraintClause}.
	 * @param ctx the parse tree
	 */
	void exitConstraintClause(DDLStatementParser.ConstraintClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#columnConstraintOption}.
	 * @param ctx the parse tree
	 */
	void enterColumnConstraintOption(DDLStatementParser.ColumnConstraintOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#columnConstraintOption}.
	 * @param ctx the parse tree
	 */
	void exitColumnConstraintOption(DDLStatementParser.ColumnConstraintOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#checkOption}.
	 * @param ctx the parse tree
	 */
	void enterCheckOption(DDLStatementParser.CheckOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#checkOption}.
	 * @param ctx the parse tree
	 */
	void exitCheckOption(DDLStatementParser.CheckOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#defaultExpr}.
	 * @param ctx the parse tree
	 */
	void enterDefaultExpr(DDLStatementParser.DefaultExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#defaultExpr}.
	 * @param ctx the parse tree
	 */
	void exitDefaultExpr(DDLStatementParser.DefaultExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#sequenceOptions}.
	 * @param ctx the parse tree
	 */
	void enterSequenceOptions(DDLStatementParser.SequenceOptionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#sequenceOptions}.
	 * @param ctx the parse tree
	 */
	void exitSequenceOptions(DDLStatementParser.SequenceOptionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#sequenceOption}.
	 * @param ctx the parse tree
	 */
	void enterSequenceOption(DDLStatementParser.SequenceOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#sequenceOption}.
	 * @param ctx the parse tree
	 */
	void exitSequenceOption(DDLStatementParser.SequenceOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#indexParameters}.
	 * @param ctx the parse tree
	 */
	void enterIndexParameters(DDLStatementParser.IndexParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#indexParameters}.
	 * @param ctx the parse tree
	 */
	void exitIndexParameters(DDLStatementParser.IndexParametersContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#action}.
	 * @param ctx the parse tree
	 */
	void enterAction(DDLStatementParser.ActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#action}.
	 * @param ctx the parse tree
	 */
	void exitAction(DDLStatementParser.ActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#constraintOptionalParam}.
	 * @param ctx the parse tree
	 */
	void enterConstraintOptionalParam(DDLStatementParser.ConstraintOptionalParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#constraintOptionalParam}.
	 * @param ctx the parse tree
	 */
	void exitConstraintOptionalParam(DDLStatementParser.ConstraintOptionalParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#likeOption}.
	 * @param ctx the parse tree
	 */
	void enterLikeOption(DDLStatementParser.LikeOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#likeOption}.
	 * @param ctx the parse tree
	 */
	void exitLikeOption(DDLStatementParser.LikeOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#tableConstraint}.
	 * @param ctx the parse tree
	 */
	void enterTableConstraint(DDLStatementParser.TableConstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#tableConstraint}.
	 * @param ctx the parse tree
	 */
	void exitTableConstraint(DDLStatementParser.TableConstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#tableConstraintOption}.
	 * @param ctx the parse tree
	 */
	void enterTableConstraintOption(DDLStatementParser.TableConstraintOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#tableConstraintOption}.
	 * @param ctx the parse tree
	 */
	void exitTableConstraintOption(DDLStatementParser.TableConstraintOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#exclusionWhereClause}.
	 * @param ctx the parse tree
	 */
	void enterExclusionWhereClause(DDLStatementParser.ExclusionWhereClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#exclusionWhereClause}.
	 * @param ctx the parse tree
	 */
	void exitExclusionWhereClause(DDLStatementParser.ExclusionWhereClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#exclusionConstraintList}.
	 * @param ctx the parse tree
	 */
	void enterExclusionConstraintList(DDLStatementParser.ExclusionConstraintListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#exclusionConstraintList}.
	 * @param ctx the parse tree
	 */
	void exitExclusionConstraintList(DDLStatementParser.ExclusionConstraintListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#exclusionConstraintElem}.
	 * @param ctx the parse tree
	 */
	void enterExclusionConstraintElem(DDLStatementParser.ExclusionConstraintElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#exclusionConstraintElem}.
	 * @param ctx the parse tree
	 */
	void exitExclusionConstraintElem(DDLStatementParser.ExclusionConstraintElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#inheritClause}.
	 * @param ctx the parse tree
	 */
	void enterInheritClause(DDLStatementParser.InheritClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#inheritClause}.
	 * @param ctx the parse tree
	 */
	void exitInheritClause(DDLStatementParser.InheritClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#partitionSpec}.
	 * @param ctx the parse tree
	 */
	void enterPartitionSpec(DDLStatementParser.PartitionSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#partitionSpec}.
	 * @param ctx the parse tree
	 */
	void exitPartitionSpec(DDLStatementParser.PartitionSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#partParams}.
	 * @param ctx the parse tree
	 */
	void enterPartParams(DDLStatementParser.PartParamsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#partParams}.
	 * @param ctx the parse tree
	 */
	void exitPartParams(DDLStatementParser.PartParamsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#partElem}.
	 * @param ctx the parse tree
	 */
	void enterPartElem(DDLStatementParser.PartElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#partElem}.
	 * @param ctx the parse tree
	 */
	void exitPartElem(DDLStatementParser.PartElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#funcExprWindowless}.
	 * @param ctx the parse tree
	 */
	void enterFuncExprWindowless(DDLStatementParser.FuncExprWindowlessContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#funcExprWindowless}.
	 * @param ctx the parse tree
	 */
	void exitFuncExprWindowless(DDLStatementParser.FuncExprWindowlessContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#partStrategy}.
	 * @param ctx the parse tree
	 */
	void enterPartStrategy(DDLStatementParser.PartStrategyContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#partStrategy}.
	 * @param ctx the parse tree
	 */
	void exitPartStrategy(DDLStatementParser.PartStrategyContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createIndexSpecification}.
	 * @param ctx the parse tree
	 */
	void enterCreateIndexSpecification(DDLStatementParser.CreateIndexSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createIndexSpecification}.
	 * @param ctx the parse tree
	 */
	void exitCreateIndexSpecification(DDLStatementParser.CreateIndexSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#concurrentlyClause}.
	 * @param ctx the parse tree
	 */
	void enterConcurrentlyClause(DDLStatementParser.ConcurrentlyClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#concurrentlyClause}.
	 * @param ctx the parse tree
	 */
	void exitConcurrentlyClause(DDLStatementParser.ConcurrentlyClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#onlyClause}.
	 * @param ctx the parse tree
	 */
	void enterOnlyClause(DDLStatementParser.OnlyClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#onlyClause}.
	 * @param ctx the parse tree
	 */
	void exitOnlyClause(DDLStatementParser.OnlyClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#asteriskClause}.
	 * @param ctx the parse tree
	 */
	void enterAsteriskClause(DDLStatementParser.AsteriskClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#asteriskClause}.
	 * @param ctx the parse tree
	 */
	void exitAsteriskClause(DDLStatementParser.AsteriskClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterDefinitionClause}.
	 * @param ctx the parse tree
	 */
	void enterAlterDefinitionClause(DDLStatementParser.AlterDefinitionClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterDefinitionClause}.
	 * @param ctx the parse tree
	 */
	void exitAlterDefinitionClause(DDLStatementParser.AlterDefinitionClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#partitionCmd}.
	 * @param ctx the parse tree
	 */
	void enterPartitionCmd(DDLStatementParser.PartitionCmdContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#partitionCmd}.
	 * @param ctx the parse tree
	 */
	void exitPartitionCmd(DDLStatementParser.PartitionCmdContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterIndexDefinitionClause}.
	 * @param ctx the parse tree
	 */
	void enterAlterIndexDefinitionClause(DDLStatementParser.AlterIndexDefinitionClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterIndexDefinitionClause}.
	 * @param ctx the parse tree
	 */
	void exitAlterIndexDefinitionClause(DDLStatementParser.AlterIndexDefinitionClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#indexPartitionCmd}.
	 * @param ctx the parse tree
	 */
	void enterIndexPartitionCmd(DDLStatementParser.IndexPartitionCmdContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#indexPartitionCmd}.
	 * @param ctx the parse tree
	 */
	void exitIndexPartitionCmd(DDLStatementParser.IndexPartitionCmdContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#renameIndexSpecification}.
	 * @param ctx the parse tree
	 */
	void enterRenameIndexSpecification(DDLStatementParser.RenameIndexSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#renameIndexSpecification}.
	 * @param ctx the parse tree
	 */
	void exitRenameIndexSpecification(DDLStatementParser.RenameIndexSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterIndexDependsOnExtension}.
	 * @param ctx the parse tree
	 */
	void enterAlterIndexDependsOnExtension(DDLStatementParser.AlterIndexDependsOnExtensionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterIndexDependsOnExtension}.
	 * @param ctx the parse tree
	 */
	void exitAlterIndexDependsOnExtension(DDLStatementParser.AlterIndexDependsOnExtensionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterIndexSetTableSpace}.
	 * @param ctx the parse tree
	 */
	void enterAlterIndexSetTableSpace(DDLStatementParser.AlterIndexSetTableSpaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterIndexSetTableSpace}.
	 * @param ctx the parse tree
	 */
	void exitAlterIndexSetTableSpace(DDLStatementParser.AlterIndexSetTableSpaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#tableNamesClause}.
	 * @param ctx the parse tree
	 */
	void enterTableNamesClause(DDLStatementParser.TableNamesClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#tableNamesClause}.
	 * @param ctx the parse tree
	 */
	void exitTableNamesClause(DDLStatementParser.TableNamesClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#tableNameClause}.
	 * @param ctx the parse tree
	 */
	void enterTableNameClause(DDLStatementParser.TableNameClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#tableNameClause}.
	 * @param ctx the parse tree
	 */
	void exitTableNameClause(DDLStatementParser.TableNameClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterTableActions}.
	 * @param ctx the parse tree
	 */
	void enterAlterTableActions(DDLStatementParser.AlterTableActionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterTableActions}.
	 * @param ctx the parse tree
	 */
	void exitAlterTableActions(DDLStatementParser.AlterTableActionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterTableAction}.
	 * @param ctx the parse tree
	 */
	void enterAlterTableAction(DDLStatementParser.AlterTableActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterTableAction}.
	 * @param ctx the parse tree
	 */
	void exitAlterTableAction(DDLStatementParser.AlterTableActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#addColumnSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAddColumnSpecification(DDLStatementParser.AddColumnSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#addColumnSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAddColumnSpecification(DDLStatementParser.AddColumnSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropColumnSpecification}.
	 * @param ctx the parse tree
	 */
	void enterDropColumnSpecification(DDLStatementParser.DropColumnSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropColumnSpecification}.
	 * @param ctx the parse tree
	 */
	void exitDropColumnSpecification(DDLStatementParser.DropColumnSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#modifyColumnSpecification}.
	 * @param ctx the parse tree
	 */
	void enterModifyColumnSpecification(DDLStatementParser.ModifyColumnSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#modifyColumnSpecification}.
	 * @param ctx the parse tree
	 */
	void exitModifyColumnSpecification(DDLStatementParser.ModifyColumnSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#modifyColumn}.
	 * @param ctx the parse tree
	 */
	void enterModifyColumn(DDLStatementParser.ModifyColumnContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#modifyColumn}.
	 * @param ctx the parse tree
	 */
	void exitModifyColumn(DDLStatementParser.ModifyColumnContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterColumnSetOption}.
	 * @param ctx the parse tree
	 */
	void enterAlterColumnSetOption(DDLStatementParser.AlterColumnSetOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterColumnSetOption}.
	 * @param ctx the parse tree
	 */
	void exitAlterColumnSetOption(DDLStatementParser.AlterColumnSetOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#attributeOptions}.
	 * @param ctx the parse tree
	 */
	void enterAttributeOptions(DDLStatementParser.AttributeOptionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#attributeOptions}.
	 * @param ctx the parse tree
	 */
	void exitAttributeOptions(DDLStatementParser.AttributeOptionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#attributeOption}.
	 * @param ctx the parse tree
	 */
	void enterAttributeOption(DDLStatementParser.AttributeOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#attributeOption}.
	 * @param ctx the parse tree
	 */
	void exitAttributeOption(DDLStatementParser.AttributeOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#addConstraintSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAddConstraintSpecification(DDLStatementParser.AddConstraintSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#addConstraintSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAddConstraintSpecification(DDLStatementParser.AddConstraintSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#tableConstraintUsingIndex}.
	 * @param ctx the parse tree
	 */
	void enterTableConstraintUsingIndex(DDLStatementParser.TableConstraintUsingIndexContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#tableConstraintUsingIndex}.
	 * @param ctx the parse tree
	 */
	void exitTableConstraintUsingIndex(DDLStatementParser.TableConstraintUsingIndexContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#storageParameterWithValue}.
	 * @param ctx the parse tree
	 */
	void enterStorageParameterWithValue(DDLStatementParser.StorageParameterWithValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#storageParameterWithValue}.
	 * @param ctx the parse tree
	 */
	void exitStorageParameterWithValue(DDLStatementParser.StorageParameterWithValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#storageParameter}.
	 * @param ctx the parse tree
	 */
	void enterStorageParameter(DDLStatementParser.StorageParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#storageParameter}.
	 * @param ctx the parse tree
	 */
	void exitStorageParameter(DDLStatementParser.StorageParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#renameColumnSpecification}.
	 * @param ctx the parse tree
	 */
	void enterRenameColumnSpecification(DDLStatementParser.RenameColumnSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#renameColumnSpecification}.
	 * @param ctx the parse tree
	 */
	void exitRenameColumnSpecification(DDLStatementParser.RenameColumnSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#renameConstraint}.
	 * @param ctx the parse tree
	 */
	void enterRenameConstraint(DDLStatementParser.RenameConstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#renameConstraint}.
	 * @param ctx the parse tree
	 */
	void exitRenameConstraint(DDLStatementParser.RenameConstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#renameTableSpecification}.
	 * @param ctx the parse tree
	 */
	void enterRenameTableSpecification(DDLStatementParser.RenameTableSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#renameTableSpecification}.
	 * @param ctx the parse tree
	 */
	void exitRenameTableSpecification(DDLStatementParser.RenameTableSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#indexNames}.
	 * @param ctx the parse tree
	 */
	void enterIndexNames(DDLStatementParser.IndexNamesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#indexNames}.
	 * @param ctx the parse tree
	 */
	void exitIndexNames(DDLStatementParser.IndexNamesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterDatabase}.
	 * @param ctx the parse tree
	 */
	void enterAlterDatabase(DDLStatementParser.AlterDatabaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterDatabase}.
	 * @param ctx the parse tree
	 */
	void exitAlterDatabase(DDLStatementParser.AlterDatabaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterDatabaseClause}.
	 * @param ctx the parse tree
	 */
	void enterAlterDatabaseClause(DDLStatementParser.AlterDatabaseClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterDatabaseClause}.
	 * @param ctx the parse tree
	 */
	void exitAlterDatabaseClause(DDLStatementParser.AlterDatabaseClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createdbOptItems}.
	 * @param ctx the parse tree
	 */
	void enterCreatedbOptItems(DDLStatementParser.CreatedbOptItemsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createdbOptItems}.
	 * @param ctx the parse tree
	 */
	void exitCreatedbOptItems(DDLStatementParser.CreatedbOptItemsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createdbOptItem}.
	 * @param ctx the parse tree
	 */
	void enterCreatedbOptItem(DDLStatementParser.CreatedbOptItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createdbOptItem}.
	 * @param ctx the parse tree
	 */
	void exitCreatedbOptItem(DDLStatementParser.CreatedbOptItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterTableCmds}.
	 * @param ctx the parse tree
	 */
	void enterAlterTableCmds(DDLStatementParser.AlterTableCmdsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterTableCmds}.
	 * @param ctx the parse tree
	 */
	void exitAlterTableCmds(DDLStatementParser.AlterTableCmdsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterTableCmd}.
	 * @param ctx the parse tree
	 */
	void enterAlterTableCmd(DDLStatementParser.AlterTableCmdContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterTableCmd}.
	 * @param ctx the parse tree
	 */
	void exitAlterTableCmd(DDLStatementParser.AlterTableCmdContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#constraintAttributeSpec}.
	 * @param ctx the parse tree
	 */
	void enterConstraintAttributeSpec(DDLStatementParser.ConstraintAttributeSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#constraintAttributeSpec}.
	 * @param ctx the parse tree
	 */
	void exitConstraintAttributeSpec(DDLStatementParser.ConstraintAttributeSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#constraintAttributeElem}.
	 * @param ctx the parse tree
	 */
	void enterConstraintAttributeElem(DDLStatementParser.ConstraintAttributeElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#constraintAttributeElem}.
	 * @param ctx the parse tree
	 */
	void exitConstraintAttributeElem(DDLStatementParser.ConstraintAttributeElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterGenericOptions}.
	 * @param ctx the parse tree
	 */
	void enterAlterGenericOptions(DDLStatementParser.AlterGenericOptionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterGenericOptions}.
	 * @param ctx the parse tree
	 */
	void exitAlterGenericOptions(DDLStatementParser.AlterGenericOptionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterGenericOptionList}.
	 * @param ctx the parse tree
	 */
	void enterAlterGenericOptionList(DDLStatementParser.AlterGenericOptionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterGenericOptionList}.
	 * @param ctx the parse tree
	 */
	void exitAlterGenericOptionList(DDLStatementParser.AlterGenericOptionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterGenericOptionElem}.
	 * @param ctx the parse tree
	 */
	void enterAlterGenericOptionElem(DDLStatementParser.AlterGenericOptionElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterGenericOptionElem}.
	 * @param ctx the parse tree
	 */
	void exitAlterGenericOptionElem(DDLStatementParser.AlterGenericOptionElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#genericOptionName}.
	 * @param ctx the parse tree
	 */
	void enterGenericOptionName(DDLStatementParser.GenericOptionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#genericOptionName}.
	 * @param ctx the parse tree
	 */
	void exitGenericOptionName(DDLStatementParser.GenericOptionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropBehavior}.
	 * @param ctx the parse tree
	 */
	void enterDropBehavior(DDLStatementParser.DropBehaviorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropBehavior}.
	 * @param ctx the parse tree
	 */
	void exitDropBehavior(DDLStatementParser.DropBehaviorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterUsing}.
	 * @param ctx the parse tree
	 */
	void enterAlterUsing(DDLStatementParser.AlterUsingContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterUsing}.
	 * @param ctx the parse tree
	 */
	void exitAlterUsing(DDLStatementParser.AlterUsingContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#setData}.
	 * @param ctx the parse tree
	 */
	void enterSetData(DDLStatementParser.SetDataContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#setData}.
	 * @param ctx the parse tree
	 */
	void exitSetData(DDLStatementParser.SetDataContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterIdentityColumnOptionList}.
	 * @param ctx the parse tree
	 */
	void enterAlterIdentityColumnOptionList(DDLStatementParser.AlterIdentityColumnOptionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterIdentityColumnOptionList}.
	 * @param ctx the parse tree
	 */
	void exitAlterIdentityColumnOptionList(DDLStatementParser.AlterIdentityColumnOptionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterIdentityColumnOption}.
	 * @param ctx the parse tree
	 */
	void enterAlterIdentityColumnOption(DDLStatementParser.AlterIdentityColumnOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterIdentityColumnOption}.
	 * @param ctx the parse tree
	 */
	void exitAlterIdentityColumnOption(DDLStatementParser.AlterIdentityColumnOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterColumnDefault}.
	 * @param ctx the parse tree
	 */
	void enterAlterColumnDefault(DDLStatementParser.AlterColumnDefaultContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterColumnDefault}.
	 * @param ctx the parse tree
	 */
	void exitAlterColumnDefault(DDLStatementParser.AlterColumnDefaultContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterOperator}.
	 * @param ctx the parse tree
	 */
	void enterAlterOperator(DDLStatementParser.AlterOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterOperator}.
	 * @param ctx the parse tree
	 */
	void exitAlterOperator(DDLStatementParser.AlterOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterOperatorClass}.
	 * @param ctx the parse tree
	 */
	void enterAlterOperatorClass(DDLStatementParser.AlterOperatorClassContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterOperatorClass}.
	 * @param ctx the parse tree
	 */
	void exitAlterOperatorClass(DDLStatementParser.AlterOperatorClassContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterOperatorClassClauses}.
	 * @param ctx the parse tree
	 */
	void enterAlterOperatorClassClauses(DDLStatementParser.AlterOperatorClassClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterOperatorClassClauses}.
	 * @param ctx the parse tree
	 */
	void exitAlterOperatorClassClauses(DDLStatementParser.AlterOperatorClassClausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterOperatorFamily}.
	 * @param ctx the parse tree
	 */
	void enterAlterOperatorFamily(DDLStatementParser.AlterOperatorFamilyContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterOperatorFamily}.
	 * @param ctx the parse tree
	 */
	void exitAlterOperatorFamily(DDLStatementParser.AlterOperatorFamilyContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterOperatorFamilyClauses}.
	 * @param ctx the parse tree
	 */
	void enterAlterOperatorFamilyClauses(DDLStatementParser.AlterOperatorFamilyClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterOperatorFamilyClauses}.
	 * @param ctx the parse tree
	 */
	void exitAlterOperatorFamilyClauses(DDLStatementParser.AlterOperatorFamilyClausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#opclassItemList}.
	 * @param ctx the parse tree
	 */
	void enterOpclassItemList(DDLStatementParser.OpclassItemListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#opclassItemList}.
	 * @param ctx the parse tree
	 */
	void exitOpclassItemList(DDLStatementParser.OpclassItemListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#opclassItem}.
	 * @param ctx the parse tree
	 */
	void enterOpclassItem(DDLStatementParser.OpclassItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#opclassItem}.
	 * @param ctx the parse tree
	 */
	void exitOpclassItem(DDLStatementParser.OpclassItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#opclassPurpose}.
	 * @param ctx the parse tree
	 */
	void enterOpclassPurpose(DDLStatementParser.OpclassPurposeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#opclassPurpose}.
	 * @param ctx the parse tree
	 */
	void exitOpclassPurpose(DDLStatementParser.OpclassPurposeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterOperatorClauses}.
	 * @param ctx the parse tree
	 */
	void enterAlterOperatorClauses(DDLStatementParser.AlterOperatorClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterOperatorClauses}.
	 * @param ctx the parse tree
	 */
	void exitAlterOperatorClauses(DDLStatementParser.AlterOperatorClausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#operatorDefList}.
	 * @param ctx the parse tree
	 */
	void enterOperatorDefList(DDLStatementParser.OperatorDefListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#operatorDefList}.
	 * @param ctx the parse tree
	 */
	void exitOperatorDefList(DDLStatementParser.OperatorDefListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#operatorDefElem}.
	 * @param ctx the parse tree
	 */
	void enterOperatorDefElem(DDLStatementParser.OperatorDefElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#operatorDefElem}.
	 * @param ctx the parse tree
	 */
	void exitOperatorDefElem(DDLStatementParser.OperatorDefElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#operatorDefArg}.
	 * @param ctx the parse tree
	 */
	void enterOperatorDefArg(DDLStatementParser.OperatorDefArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#operatorDefArg}.
	 * @param ctx the parse tree
	 */
	void exitOperatorDefArg(DDLStatementParser.OperatorDefArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#operatorWithArgtypes}.
	 * @param ctx the parse tree
	 */
	void enterOperatorWithArgtypes(DDLStatementParser.OperatorWithArgtypesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#operatorWithArgtypes}.
	 * @param ctx the parse tree
	 */
	void exitOperatorWithArgtypes(DDLStatementParser.OperatorWithArgtypesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterAggregate}.
	 * @param ctx the parse tree
	 */
	void enterAlterAggregate(DDLStatementParser.AlterAggregateContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterAggregate}.
	 * @param ctx the parse tree
	 */
	void exitAlterAggregate(DDLStatementParser.AlterAggregateContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#aggregateSignature}.
	 * @param ctx the parse tree
	 */
	void enterAggregateSignature(DDLStatementParser.AggregateSignatureContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#aggregateSignature}.
	 * @param ctx the parse tree
	 */
	void exitAggregateSignature(DDLStatementParser.AggregateSignatureContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#aggrArgs}.
	 * @param ctx the parse tree
	 */
	void enterAggrArgs(DDLStatementParser.AggrArgsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#aggrArgs}.
	 * @param ctx the parse tree
	 */
	void exitAggrArgs(DDLStatementParser.AggrArgsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#aggrArgsList}.
	 * @param ctx the parse tree
	 */
	void enterAggrArgsList(DDLStatementParser.AggrArgsListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#aggrArgsList}.
	 * @param ctx the parse tree
	 */
	void exitAggrArgsList(DDLStatementParser.AggrArgsListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#aggrArg}.
	 * @param ctx the parse tree
	 */
	void enterAggrArg(DDLStatementParser.AggrArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#aggrArg}.
	 * @param ctx the parse tree
	 */
	void exitAggrArg(DDLStatementParser.AggrArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterAggregateDefinitionClause}.
	 * @param ctx the parse tree
	 */
	void enterAlterAggregateDefinitionClause(DDLStatementParser.AlterAggregateDefinitionClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterAggregateDefinitionClause}.
	 * @param ctx the parse tree
	 */
	void exitAlterAggregateDefinitionClause(DDLStatementParser.AlterAggregateDefinitionClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterCollation}.
	 * @param ctx the parse tree
	 */
	void enterAlterCollation(DDLStatementParser.AlterCollationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterCollation}.
	 * @param ctx the parse tree
	 */
	void exitAlterCollation(DDLStatementParser.AlterCollationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterCollationClause}.
	 * @param ctx the parse tree
	 */
	void enterAlterCollationClause(DDLStatementParser.AlterCollationClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterCollationClause}.
	 * @param ctx the parse tree
	 */
	void exitAlterCollationClause(DDLStatementParser.AlterCollationClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterConversion}.
	 * @param ctx the parse tree
	 */
	void enterAlterConversion(DDLStatementParser.AlterConversionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterConversion}.
	 * @param ctx the parse tree
	 */
	void exitAlterConversion(DDLStatementParser.AlterConversionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterConversionClause}.
	 * @param ctx the parse tree
	 */
	void enterAlterConversionClause(DDLStatementParser.AlterConversionClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterConversionClause}.
	 * @param ctx the parse tree
	 */
	void exitAlterConversionClause(DDLStatementParser.AlterConversionClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterDefaultPrivileges}.
	 * @param ctx the parse tree
	 */
	void enterAlterDefaultPrivileges(DDLStatementParser.AlterDefaultPrivilegesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterDefaultPrivileges}.
	 * @param ctx the parse tree
	 */
	void exitAlterDefaultPrivileges(DDLStatementParser.AlterDefaultPrivilegesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#defACLAction}.
	 * @param ctx the parse tree
	 */
	void enterDefACLAction(DDLStatementParser.DefACLActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#defACLAction}.
	 * @param ctx the parse tree
	 */
	void exitDefACLAction(DDLStatementParser.DefACLActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#grantGrantOption}.
	 * @param ctx the parse tree
	 */
	void enterGrantGrantOption(DDLStatementParser.GrantGrantOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#grantGrantOption}.
	 * @param ctx the parse tree
	 */
	void exitGrantGrantOption(DDLStatementParser.GrantGrantOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#granteeList}.
	 * @param ctx the parse tree
	 */
	void enterGranteeList(DDLStatementParser.GranteeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#granteeList}.
	 * @param ctx the parse tree
	 */
	void exitGranteeList(DDLStatementParser.GranteeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#grantee}.
	 * @param ctx the parse tree
	 */
	void enterGrantee(DDLStatementParser.GranteeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#grantee}.
	 * @param ctx the parse tree
	 */
	void exitGrantee(DDLStatementParser.GranteeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#defaclPrivilegeTarget}.
	 * @param ctx the parse tree
	 */
	void enterDefaclPrivilegeTarget(DDLStatementParser.DefaclPrivilegeTargetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#defaclPrivilegeTarget}.
	 * @param ctx the parse tree
	 */
	void exitDefaclPrivilegeTarget(DDLStatementParser.DefaclPrivilegeTargetContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#privileges}.
	 * @param ctx the parse tree
	 */
	void enterPrivileges(DDLStatementParser.PrivilegesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#privileges}.
	 * @param ctx the parse tree
	 */
	void exitPrivileges(DDLStatementParser.PrivilegesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#privilegeList}.
	 * @param ctx the parse tree
	 */
	void enterPrivilegeList(DDLStatementParser.PrivilegeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#privilegeList}.
	 * @param ctx the parse tree
	 */
	void exitPrivilegeList(DDLStatementParser.PrivilegeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#privilege}.
	 * @param ctx the parse tree
	 */
	void enterPrivilege(DDLStatementParser.PrivilegeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#privilege}.
	 * @param ctx the parse tree
	 */
	void exitPrivilege(DDLStatementParser.PrivilegeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#defACLOptionList}.
	 * @param ctx the parse tree
	 */
	void enterDefACLOptionList(DDLStatementParser.DefACLOptionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#defACLOptionList}.
	 * @param ctx the parse tree
	 */
	void exitDefACLOptionList(DDLStatementParser.DefACLOptionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#defACLOption}.
	 * @param ctx the parse tree
	 */
	void enterDefACLOption(DDLStatementParser.DefACLOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#defACLOption}.
	 * @param ctx the parse tree
	 */
	void exitDefACLOption(DDLStatementParser.DefACLOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#schemaNameList}.
	 * @param ctx the parse tree
	 */
	void enterSchemaNameList(DDLStatementParser.SchemaNameListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#schemaNameList}.
	 * @param ctx the parse tree
	 */
	void exitSchemaNameList(DDLStatementParser.SchemaNameListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterDomain}.
	 * @param ctx the parse tree
	 */
	void enterAlterDomain(DDLStatementParser.AlterDomainContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterDomain}.
	 * @param ctx the parse tree
	 */
	void exitAlterDomain(DDLStatementParser.AlterDomainContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterDomainClause}.
	 * @param ctx the parse tree
	 */
	void enterAlterDomainClause(DDLStatementParser.AlterDomainClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterDomainClause}.
	 * @param ctx the parse tree
	 */
	void exitAlterDomainClause(DDLStatementParser.AlterDomainClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#constraintName}.
	 * @param ctx the parse tree
	 */
	void enterConstraintName(DDLStatementParser.ConstraintNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#constraintName}.
	 * @param ctx the parse tree
	 */
	void exitConstraintName(DDLStatementParser.ConstraintNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterEventTrigger}.
	 * @param ctx the parse tree
	 */
	void enterAlterEventTrigger(DDLStatementParser.AlterEventTriggerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterEventTrigger}.
	 * @param ctx the parse tree
	 */
	void exitAlterEventTrigger(DDLStatementParser.AlterEventTriggerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterEventTriggerClause}.
	 * @param ctx the parse tree
	 */
	void enterAlterEventTriggerClause(DDLStatementParser.AlterEventTriggerClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterEventTriggerClause}.
	 * @param ctx the parse tree
	 */
	void exitAlterEventTriggerClause(DDLStatementParser.AlterEventTriggerClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#tiggerName}.
	 * @param ctx the parse tree
	 */
	void enterTiggerName(DDLStatementParser.TiggerNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#tiggerName}.
	 * @param ctx the parse tree
	 */
	void exitTiggerName(DDLStatementParser.TiggerNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterExtension}.
	 * @param ctx the parse tree
	 */
	void enterAlterExtension(DDLStatementParser.AlterExtensionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterExtension}.
	 * @param ctx the parse tree
	 */
	void exitAlterExtension(DDLStatementParser.AlterExtensionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterExtensionClauses}.
	 * @param ctx the parse tree
	 */
	void enterAlterExtensionClauses(DDLStatementParser.AlterExtensionClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterExtensionClauses}.
	 * @param ctx the parse tree
	 */
	void exitAlterExtensionClauses(DDLStatementParser.AlterExtensionClausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#functionWithArgtypes}.
	 * @param ctx the parse tree
	 */
	void enterFunctionWithArgtypes(DDLStatementParser.FunctionWithArgtypesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#functionWithArgtypes}.
	 * @param ctx the parse tree
	 */
	void exitFunctionWithArgtypes(DDLStatementParser.FunctionWithArgtypesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#funcArgs}.
	 * @param ctx the parse tree
	 */
	void enterFuncArgs(DDLStatementParser.FuncArgsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#funcArgs}.
	 * @param ctx the parse tree
	 */
	void exitFuncArgs(DDLStatementParser.FuncArgsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#aggregateWithArgtypes}.
	 * @param ctx the parse tree
	 */
	void enterAggregateWithArgtypes(DDLStatementParser.AggregateWithArgtypesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#aggregateWithArgtypes}.
	 * @param ctx the parse tree
	 */
	void exitAggregateWithArgtypes(DDLStatementParser.AggregateWithArgtypesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterExtensionOptList}.
	 * @param ctx the parse tree
	 */
	void enterAlterExtensionOptList(DDLStatementParser.AlterExtensionOptListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterExtensionOptList}.
	 * @param ctx the parse tree
	 */
	void exitAlterExtensionOptList(DDLStatementParser.AlterExtensionOptListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterExtensionOptItem}.
	 * @param ctx the parse tree
	 */
	void enterAlterExtensionOptItem(DDLStatementParser.AlterExtensionOptItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterExtensionOptItem}.
	 * @param ctx the parse tree
	 */
	void exitAlterExtensionOptItem(DDLStatementParser.AlterExtensionOptItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterForeignDataWrapper}.
	 * @param ctx the parse tree
	 */
	void enterAlterForeignDataWrapper(DDLStatementParser.AlterForeignDataWrapperContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterForeignDataWrapper}.
	 * @param ctx the parse tree
	 */
	void exitAlterForeignDataWrapper(DDLStatementParser.AlterForeignDataWrapperContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterForeignDataWrapperClauses}.
	 * @param ctx the parse tree
	 */
	void enterAlterForeignDataWrapperClauses(DDLStatementParser.AlterForeignDataWrapperClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterForeignDataWrapperClauses}.
	 * @param ctx the parse tree
	 */
	void exitAlterForeignDataWrapperClauses(DDLStatementParser.AlterForeignDataWrapperClausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#genericOptionElem}.
	 * @param ctx the parse tree
	 */
	void enterGenericOptionElem(DDLStatementParser.GenericOptionElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#genericOptionElem}.
	 * @param ctx the parse tree
	 */
	void exitGenericOptionElem(DDLStatementParser.GenericOptionElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#genericOptionArg}.
	 * @param ctx the parse tree
	 */
	void enterGenericOptionArg(DDLStatementParser.GenericOptionArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#genericOptionArg}.
	 * @param ctx the parse tree
	 */
	void exitGenericOptionArg(DDLStatementParser.GenericOptionArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#fdwOptions}.
	 * @param ctx the parse tree
	 */
	void enterFdwOptions(DDLStatementParser.FdwOptionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#fdwOptions}.
	 * @param ctx the parse tree
	 */
	void exitFdwOptions(DDLStatementParser.FdwOptionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#fdwOption}.
	 * @param ctx the parse tree
	 */
	void enterFdwOption(DDLStatementParser.FdwOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#fdwOption}.
	 * @param ctx the parse tree
	 */
	void exitFdwOption(DDLStatementParser.FdwOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#handlerName}.
	 * @param ctx the parse tree
	 */
	void enterHandlerName(DDLStatementParser.HandlerNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#handlerName}.
	 * @param ctx the parse tree
	 */
	void exitHandlerName(DDLStatementParser.HandlerNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterGroup}.
	 * @param ctx the parse tree
	 */
	void enterAlterGroup(DDLStatementParser.AlterGroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterGroup}.
	 * @param ctx the parse tree
	 */
	void exitAlterGroup(DDLStatementParser.AlterGroupContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterGroupClauses}.
	 * @param ctx the parse tree
	 */
	void enterAlterGroupClauses(DDLStatementParser.AlterGroupClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterGroupClauses}.
	 * @param ctx the parse tree
	 */
	void exitAlterGroupClauses(DDLStatementParser.AlterGroupClausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterLanguage}.
	 * @param ctx the parse tree
	 */
	void enterAlterLanguage(DDLStatementParser.AlterLanguageContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterLanguage}.
	 * @param ctx the parse tree
	 */
	void exitAlterLanguage(DDLStatementParser.AlterLanguageContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterLargeObject}.
	 * @param ctx the parse tree
	 */
	void enterAlterLargeObject(DDLStatementParser.AlterLargeObjectContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterLargeObject}.
	 * @param ctx the parse tree
	 */
	void exitAlterLargeObject(DDLStatementParser.AlterLargeObjectContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterMaterializedView}.
	 * @param ctx the parse tree
	 */
	void enterAlterMaterializedView(DDLStatementParser.AlterMaterializedViewContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterMaterializedView}.
	 * @param ctx the parse tree
	 */
	void exitAlterMaterializedView(DDLStatementParser.AlterMaterializedViewContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterMaterializedViewClauses}.
	 * @param ctx the parse tree
	 */
	void enterAlterMaterializedViewClauses(DDLStatementParser.AlterMaterializedViewClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterMaterializedViewClauses}.
	 * @param ctx the parse tree
	 */
	void exitAlterMaterializedViewClauses(DDLStatementParser.AlterMaterializedViewClausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#declare}.
	 * @param ctx the parse tree
	 */
	void enterDeclare(DDLStatementParser.DeclareContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#declare}.
	 * @param ctx the parse tree
	 */
	void exitDeclare(DDLStatementParser.DeclareContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#cursorOptions}.
	 * @param ctx the parse tree
	 */
	void enterCursorOptions(DDLStatementParser.CursorOptionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#cursorOptions}.
	 * @param ctx the parse tree
	 */
	void exitCursorOptions(DDLStatementParser.CursorOptionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#cursorOption}.
	 * @param ctx the parse tree
	 */
	void enterCursorOption(DDLStatementParser.CursorOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#cursorOption}.
	 * @param ctx the parse tree
	 */
	void exitCursorOption(DDLStatementParser.CursorOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#executeStmt}.
	 * @param ctx the parse tree
	 */
	void enterExecuteStmt(DDLStatementParser.ExecuteStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#executeStmt}.
	 * @param ctx the parse tree
	 */
	void exitExecuteStmt(DDLStatementParser.ExecuteStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createMaterializedView}.
	 * @param ctx the parse tree
	 */
	void enterCreateMaterializedView(DDLStatementParser.CreateMaterializedViewContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createMaterializedView}.
	 * @param ctx the parse tree
	 */
	void exitCreateMaterializedView(DDLStatementParser.CreateMaterializedViewContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createMvTarget}.
	 * @param ctx the parse tree
	 */
	void enterCreateMvTarget(DDLStatementParser.CreateMvTargetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createMvTarget}.
	 * @param ctx the parse tree
	 */
	void exitCreateMvTarget(DDLStatementParser.CreateMvTargetContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#refreshMatViewStmt}.
	 * @param ctx the parse tree
	 */
	void enterRefreshMatViewStmt(DDLStatementParser.RefreshMatViewStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#refreshMatViewStmt}.
	 * @param ctx the parse tree
	 */
	void exitRefreshMatViewStmt(DDLStatementParser.RefreshMatViewStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterPolicy}.
	 * @param ctx the parse tree
	 */
	void enterAlterPolicy(DDLStatementParser.AlterPolicyContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterPolicy}.
	 * @param ctx the parse tree
	 */
	void exitAlterPolicy(DDLStatementParser.AlterPolicyContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterPolicyClauses}.
	 * @param ctx the parse tree
	 */
	void enterAlterPolicyClauses(DDLStatementParser.AlterPolicyClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterPolicyClauses}.
	 * @param ctx the parse tree
	 */
	void exitAlterPolicyClauses(DDLStatementParser.AlterPolicyClausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterProcedure}.
	 * @param ctx the parse tree
	 */
	void enterAlterProcedure(DDLStatementParser.AlterProcedureContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterProcedure}.
	 * @param ctx the parse tree
	 */
	void exitAlterProcedure(DDLStatementParser.AlterProcedureContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterProcedureClauses}.
	 * @param ctx the parse tree
	 */
	void enterAlterProcedureClauses(DDLStatementParser.AlterProcedureClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterProcedureClauses}.
	 * @param ctx the parse tree
	 */
	void exitAlterProcedureClauses(DDLStatementParser.AlterProcedureClausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterfuncOptList}.
	 * @param ctx the parse tree
	 */
	void enterAlterfuncOptList(DDLStatementParser.AlterfuncOptListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterfuncOptList}.
	 * @param ctx the parse tree
	 */
	void exitAlterfuncOptList(DDLStatementParser.AlterfuncOptListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterFunction}.
	 * @param ctx the parse tree
	 */
	void enterAlterFunction(DDLStatementParser.AlterFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterFunction}.
	 * @param ctx the parse tree
	 */
	void exitAlterFunction(DDLStatementParser.AlterFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterFunctionClauses}.
	 * @param ctx the parse tree
	 */
	void enterAlterFunctionClauses(DDLStatementParser.AlterFunctionClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterFunctionClauses}.
	 * @param ctx the parse tree
	 */
	void exitAlterFunctionClauses(DDLStatementParser.AlterFunctionClausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterPublication}.
	 * @param ctx the parse tree
	 */
	void enterAlterPublication(DDLStatementParser.AlterPublicationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterPublication}.
	 * @param ctx the parse tree
	 */
	void exitAlterPublication(DDLStatementParser.AlterPublicationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterRoutine}.
	 * @param ctx the parse tree
	 */
	void enterAlterRoutine(DDLStatementParser.AlterRoutineContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterRoutine}.
	 * @param ctx the parse tree
	 */
	void exitAlterRoutine(DDLStatementParser.AlterRoutineContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterRule}.
	 * @param ctx the parse tree
	 */
	void enterAlterRule(DDLStatementParser.AlterRuleContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterRule}.
	 * @param ctx the parse tree
	 */
	void exitAlterRule(DDLStatementParser.AlterRuleContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterSequence}.
	 * @param ctx the parse tree
	 */
	void enterAlterSequence(DDLStatementParser.AlterSequenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterSequence}.
	 * @param ctx the parse tree
	 */
	void exitAlterSequence(DDLStatementParser.AlterSequenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterSequenceClauses}.
	 * @param ctx the parse tree
	 */
	void enterAlterSequenceClauses(DDLStatementParser.AlterSequenceClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterSequenceClauses}.
	 * @param ctx the parse tree
	 */
	void exitAlterSequenceClauses(DDLStatementParser.AlterSequenceClausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterServer}.
	 * @param ctx the parse tree
	 */
	void enterAlterServer(DDLStatementParser.AlterServerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterServer}.
	 * @param ctx the parse tree
	 */
	void exitAlterServer(DDLStatementParser.AlterServerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#foreignServerVersion}.
	 * @param ctx the parse tree
	 */
	void enterForeignServerVersion(DDLStatementParser.ForeignServerVersionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#foreignServerVersion}.
	 * @param ctx the parse tree
	 */
	void exitForeignServerVersion(DDLStatementParser.ForeignServerVersionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterStatistics}.
	 * @param ctx the parse tree
	 */
	void enterAlterStatistics(DDLStatementParser.AlterStatisticsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterStatistics}.
	 * @param ctx the parse tree
	 */
	void exitAlterStatistics(DDLStatementParser.AlterStatisticsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterSubscription}.
	 * @param ctx the parse tree
	 */
	void enterAlterSubscription(DDLStatementParser.AlterSubscriptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterSubscription}.
	 * @param ctx the parse tree
	 */
	void exitAlterSubscription(DDLStatementParser.AlterSubscriptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#publicationNameList}.
	 * @param ctx the parse tree
	 */
	void enterPublicationNameList(DDLStatementParser.PublicationNameListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#publicationNameList}.
	 * @param ctx the parse tree
	 */
	void exitPublicationNameList(DDLStatementParser.PublicationNameListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#publicationNameItem}.
	 * @param ctx the parse tree
	 */
	void enterPublicationNameItem(DDLStatementParser.PublicationNameItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#publicationNameItem}.
	 * @param ctx the parse tree
	 */
	void exitPublicationNameItem(DDLStatementParser.PublicationNameItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterSystem}.
	 * @param ctx the parse tree
	 */
	void enterAlterSystem(DDLStatementParser.AlterSystemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterSystem}.
	 * @param ctx the parse tree
	 */
	void exitAlterSystem(DDLStatementParser.AlterSystemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterTablespace}.
	 * @param ctx the parse tree
	 */
	void enterAlterTablespace(DDLStatementParser.AlterTablespaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterTablespace}.
	 * @param ctx the parse tree
	 */
	void exitAlterTablespace(DDLStatementParser.AlterTablespaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterTextSearchConfiguration}.
	 * @param ctx the parse tree
	 */
	void enterAlterTextSearchConfiguration(DDLStatementParser.AlterTextSearchConfigurationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterTextSearchConfiguration}.
	 * @param ctx the parse tree
	 */
	void exitAlterTextSearchConfiguration(DDLStatementParser.AlterTextSearchConfigurationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterTextSearchConfigurationClauses}.
	 * @param ctx the parse tree
	 */
	void enterAlterTextSearchConfigurationClauses(DDLStatementParser.AlterTextSearchConfigurationClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterTextSearchConfigurationClauses}.
	 * @param ctx the parse tree
	 */
	void exitAlterTextSearchConfigurationClauses(DDLStatementParser.AlterTextSearchConfigurationClausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#anyNameList}.
	 * @param ctx the parse tree
	 */
	void enterAnyNameList(DDLStatementParser.AnyNameListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#anyNameList}.
	 * @param ctx the parse tree
	 */
	void exitAnyNameList(DDLStatementParser.AnyNameListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterTextSearchDictionary}.
	 * @param ctx the parse tree
	 */
	void enterAlterTextSearchDictionary(DDLStatementParser.AlterTextSearchDictionaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterTextSearchDictionary}.
	 * @param ctx the parse tree
	 */
	void exitAlterTextSearchDictionary(DDLStatementParser.AlterTextSearchDictionaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterTextSearchParser}.
	 * @param ctx the parse tree
	 */
	void enterAlterTextSearchParser(DDLStatementParser.AlterTextSearchParserContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterTextSearchParser}.
	 * @param ctx the parse tree
	 */
	void exitAlterTextSearchParser(DDLStatementParser.AlterTextSearchParserContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterTextSearchTemplate}.
	 * @param ctx the parse tree
	 */
	void enterAlterTextSearchTemplate(DDLStatementParser.AlterTextSearchTemplateContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterTextSearchTemplate}.
	 * @param ctx the parse tree
	 */
	void exitAlterTextSearchTemplate(DDLStatementParser.AlterTextSearchTemplateContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterTrigger}.
	 * @param ctx the parse tree
	 */
	void enterAlterTrigger(DDLStatementParser.AlterTriggerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterTrigger}.
	 * @param ctx the parse tree
	 */
	void exitAlterTrigger(DDLStatementParser.AlterTriggerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterType}.
	 * @param ctx the parse tree
	 */
	void enterAlterType(DDLStatementParser.AlterTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterType}.
	 * @param ctx the parse tree
	 */
	void exitAlterType(DDLStatementParser.AlterTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterTypeClauses}.
	 * @param ctx the parse tree
	 */
	void enterAlterTypeClauses(DDLStatementParser.AlterTypeClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterTypeClauses}.
	 * @param ctx the parse tree
	 */
	void exitAlterTypeClauses(DDLStatementParser.AlterTypeClausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterTypeCmds}.
	 * @param ctx the parse tree
	 */
	void enterAlterTypeCmds(DDLStatementParser.AlterTypeCmdsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterTypeCmds}.
	 * @param ctx the parse tree
	 */
	void exitAlterTypeCmds(DDLStatementParser.AlterTypeCmdsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterTypeCmd}.
	 * @param ctx the parse tree
	 */
	void enterAlterTypeCmd(DDLStatementParser.AlterTypeCmdContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterTypeCmd}.
	 * @param ctx the parse tree
	 */
	void exitAlterTypeCmd(DDLStatementParser.AlterTypeCmdContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterUserMapping}.
	 * @param ctx the parse tree
	 */
	void enterAlterUserMapping(DDLStatementParser.AlterUserMappingContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterUserMapping}.
	 * @param ctx the parse tree
	 */
	void exitAlterUserMapping(DDLStatementParser.AlterUserMappingContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#authIdent}.
	 * @param ctx the parse tree
	 */
	void enterAuthIdent(DDLStatementParser.AuthIdentContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#authIdent}.
	 * @param ctx the parse tree
	 */
	void exitAuthIdent(DDLStatementParser.AuthIdentContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterView}.
	 * @param ctx the parse tree
	 */
	void enterAlterView(DDLStatementParser.AlterViewContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterView}.
	 * @param ctx the parse tree
	 */
	void exitAlterView(DDLStatementParser.AlterViewContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterViewClauses}.
	 * @param ctx the parse tree
	 */
	void enterAlterViewClauses(DDLStatementParser.AlterViewClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterViewClauses}.
	 * @param ctx the parse tree
	 */
	void exitAlterViewClauses(DDLStatementParser.AlterViewClausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#close}.
	 * @param ctx the parse tree
	 */
	void enterClose(DDLStatementParser.CloseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#close}.
	 * @param ctx the parse tree
	 */
	void exitClose(DDLStatementParser.CloseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#cluster}.
	 * @param ctx the parse tree
	 */
	void enterCluster(DDLStatementParser.ClusterContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#cluster}.
	 * @param ctx the parse tree
	 */
	void exitCluster(DDLStatementParser.ClusterContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#clusterIndexSpecification}.
	 * @param ctx the parse tree
	 */
	void enterClusterIndexSpecification(DDLStatementParser.ClusterIndexSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#clusterIndexSpecification}.
	 * @param ctx the parse tree
	 */
	void exitClusterIndexSpecification(DDLStatementParser.ClusterIndexSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#comment}.
	 * @param ctx the parse tree
	 */
	void enterComment(DDLStatementParser.CommentContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#comment}.
	 * @param ctx the parse tree
	 */
	void exitComment(DDLStatementParser.CommentContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#commentClauses}.
	 * @param ctx the parse tree
	 */
	void enterCommentClauses(DDLStatementParser.CommentClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#commentClauses}.
	 * @param ctx the parse tree
	 */
	void exitCommentClauses(DDLStatementParser.CommentClausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#objectTypeNameOnAnyName}.
	 * @param ctx the parse tree
	 */
	void enterObjectTypeNameOnAnyName(DDLStatementParser.ObjectTypeNameOnAnyNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#objectTypeNameOnAnyName}.
	 * @param ctx the parse tree
	 */
	void exitObjectTypeNameOnAnyName(DDLStatementParser.ObjectTypeNameOnAnyNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#objectTypeName}.
	 * @param ctx the parse tree
	 */
	void enterObjectTypeName(DDLStatementParser.ObjectTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#objectTypeName}.
	 * @param ctx the parse tree
	 */
	void exitObjectTypeName(DDLStatementParser.ObjectTypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropTypeName}.
	 * @param ctx the parse tree
	 */
	void enterDropTypeName(DDLStatementParser.DropTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropTypeName}.
	 * @param ctx the parse tree
	 */
	void exitDropTypeName(DDLStatementParser.DropTypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#objectTypeAnyName}.
	 * @param ctx the parse tree
	 */
	void enterObjectTypeAnyName(DDLStatementParser.ObjectTypeAnyNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#objectTypeAnyName}.
	 * @param ctx the parse tree
	 */
	void exitObjectTypeAnyName(DDLStatementParser.ObjectTypeAnyNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#commentText}.
	 * @param ctx the parse tree
	 */
	void enterCommentText(DDLStatementParser.CommentTextContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#commentText}.
	 * @param ctx the parse tree
	 */
	void exitCommentText(DDLStatementParser.CommentTextContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createAccessMethod}.
	 * @param ctx the parse tree
	 */
	void enterCreateAccessMethod(DDLStatementParser.CreateAccessMethodContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createAccessMethod}.
	 * @param ctx the parse tree
	 */
	void exitCreateAccessMethod(DDLStatementParser.CreateAccessMethodContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createAggregate}.
	 * @param ctx the parse tree
	 */
	void enterCreateAggregate(DDLStatementParser.CreateAggregateContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createAggregate}.
	 * @param ctx the parse tree
	 */
	void exitCreateAggregate(DDLStatementParser.CreateAggregateContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#oldAggrDefinition}.
	 * @param ctx the parse tree
	 */
	void enterOldAggrDefinition(DDLStatementParser.OldAggrDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#oldAggrDefinition}.
	 * @param ctx the parse tree
	 */
	void exitOldAggrDefinition(DDLStatementParser.OldAggrDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#oldAggrList}.
	 * @param ctx the parse tree
	 */
	void enterOldAggrList(DDLStatementParser.OldAggrListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#oldAggrList}.
	 * @param ctx the parse tree
	 */
	void exitOldAggrList(DDLStatementParser.OldAggrListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#oldAggrElem}.
	 * @param ctx the parse tree
	 */
	void enterOldAggrElem(DDLStatementParser.OldAggrElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#oldAggrElem}.
	 * @param ctx the parse tree
	 */
	void exitOldAggrElem(DDLStatementParser.OldAggrElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createCast}.
	 * @param ctx the parse tree
	 */
	void enterCreateCast(DDLStatementParser.CreateCastContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createCast}.
	 * @param ctx the parse tree
	 */
	void exitCreateCast(DDLStatementParser.CreateCastContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#castContext}.
	 * @param ctx the parse tree
	 */
	void enterCastContext(DDLStatementParser.CastContextContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#castContext}.
	 * @param ctx the parse tree
	 */
	void exitCastContext(DDLStatementParser.CastContextContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createCollation}.
	 * @param ctx the parse tree
	 */
	void enterCreateCollation(DDLStatementParser.CreateCollationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createCollation}.
	 * @param ctx the parse tree
	 */
	void exitCreateCollation(DDLStatementParser.CreateCollationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createConversion}.
	 * @param ctx the parse tree
	 */
	void enterCreateConversion(DDLStatementParser.CreateConversionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createConversion}.
	 * @param ctx the parse tree
	 */
	void exitCreateConversion(DDLStatementParser.CreateConversionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createDomain}.
	 * @param ctx the parse tree
	 */
	void enterCreateDomain(DDLStatementParser.CreateDomainContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createDomain}.
	 * @param ctx the parse tree
	 */
	void exitCreateDomain(DDLStatementParser.CreateDomainContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createEventTrigger}.
	 * @param ctx the parse tree
	 */
	void enterCreateEventTrigger(DDLStatementParser.CreateEventTriggerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createEventTrigger}.
	 * @param ctx the parse tree
	 */
	void exitCreateEventTrigger(DDLStatementParser.CreateEventTriggerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#eventTriggerWhenList}.
	 * @param ctx the parse tree
	 */
	void enterEventTriggerWhenList(DDLStatementParser.EventTriggerWhenListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#eventTriggerWhenList}.
	 * @param ctx the parse tree
	 */
	void exitEventTriggerWhenList(DDLStatementParser.EventTriggerWhenListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#eventTriggerWhenItem}.
	 * @param ctx the parse tree
	 */
	void enterEventTriggerWhenItem(DDLStatementParser.EventTriggerWhenItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#eventTriggerWhenItem}.
	 * @param ctx the parse tree
	 */
	void exitEventTriggerWhenItem(DDLStatementParser.EventTriggerWhenItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#eventTriggerValueList}.
	 * @param ctx the parse tree
	 */
	void enterEventTriggerValueList(DDLStatementParser.EventTriggerValueListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#eventTriggerValueList}.
	 * @param ctx the parse tree
	 */
	void exitEventTriggerValueList(DDLStatementParser.EventTriggerValueListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createExtension}.
	 * @param ctx the parse tree
	 */
	void enterCreateExtension(DDLStatementParser.CreateExtensionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createExtension}.
	 * @param ctx the parse tree
	 */
	void exitCreateExtension(DDLStatementParser.CreateExtensionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createExtensionOptList}.
	 * @param ctx the parse tree
	 */
	void enterCreateExtensionOptList(DDLStatementParser.CreateExtensionOptListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createExtensionOptList}.
	 * @param ctx the parse tree
	 */
	void exitCreateExtensionOptList(DDLStatementParser.CreateExtensionOptListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createExtensionOptItem}.
	 * @param ctx the parse tree
	 */
	void enterCreateExtensionOptItem(DDLStatementParser.CreateExtensionOptItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createExtensionOptItem}.
	 * @param ctx the parse tree
	 */
	void exitCreateExtensionOptItem(DDLStatementParser.CreateExtensionOptItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createForeignDataWrapper}.
	 * @param ctx the parse tree
	 */
	void enterCreateForeignDataWrapper(DDLStatementParser.CreateForeignDataWrapperContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createForeignDataWrapper}.
	 * @param ctx the parse tree
	 */
	void exitCreateForeignDataWrapper(DDLStatementParser.CreateForeignDataWrapperContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createForeignTable}.
	 * @param ctx the parse tree
	 */
	void enterCreateForeignTable(DDLStatementParser.CreateForeignTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createForeignTable}.
	 * @param ctx the parse tree
	 */
	void exitCreateForeignTable(DDLStatementParser.CreateForeignTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createForeignTableClauses}.
	 * @param ctx the parse tree
	 */
	void enterCreateForeignTableClauses(DDLStatementParser.CreateForeignTableClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createForeignTableClauses}.
	 * @param ctx the parse tree
	 */
	void exitCreateForeignTableClauses(DDLStatementParser.CreateForeignTableClausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#tableElementList}.
	 * @param ctx the parse tree
	 */
	void enterTableElementList(DDLStatementParser.TableElementListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#tableElementList}.
	 * @param ctx the parse tree
	 */
	void exitTableElementList(DDLStatementParser.TableElementListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#tableElement}.
	 * @param ctx the parse tree
	 */
	void enterTableElement(DDLStatementParser.TableElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#tableElement}.
	 * @param ctx the parse tree
	 */
	void exitTableElement(DDLStatementParser.TableElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#tableLikeClause}.
	 * @param ctx the parse tree
	 */
	void enterTableLikeClause(DDLStatementParser.TableLikeClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#tableLikeClause}.
	 * @param ctx the parse tree
	 */
	void exitTableLikeClause(DDLStatementParser.TableLikeClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#tableLikeOptionList}.
	 * @param ctx the parse tree
	 */
	void enterTableLikeOptionList(DDLStatementParser.TableLikeOptionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#tableLikeOptionList}.
	 * @param ctx the parse tree
	 */
	void exitTableLikeOptionList(DDLStatementParser.TableLikeOptionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#tableLikeOption}.
	 * @param ctx the parse tree
	 */
	void enterTableLikeOption(DDLStatementParser.TableLikeOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#tableLikeOption}.
	 * @param ctx the parse tree
	 */
	void exitTableLikeOption(DDLStatementParser.TableLikeOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createFunction}.
	 * @param ctx the parse tree
	 */
	void enterCreateFunction(DDLStatementParser.CreateFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createFunction}.
	 * @param ctx the parse tree
	 */
	void exitCreateFunction(DDLStatementParser.CreateFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#tableFuncColumnList}.
	 * @param ctx the parse tree
	 */
	void enterTableFuncColumnList(DDLStatementParser.TableFuncColumnListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#tableFuncColumnList}.
	 * @param ctx the parse tree
	 */
	void exitTableFuncColumnList(DDLStatementParser.TableFuncColumnListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#tableFuncColumn}.
	 * @param ctx the parse tree
	 */
	void enterTableFuncColumn(DDLStatementParser.TableFuncColumnContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#tableFuncColumn}.
	 * @param ctx the parse tree
	 */
	void exitTableFuncColumn(DDLStatementParser.TableFuncColumnContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createfuncOptList}.
	 * @param ctx the parse tree
	 */
	void enterCreatefuncOptList(DDLStatementParser.CreatefuncOptListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createfuncOptList}.
	 * @param ctx the parse tree
	 */
	void exitCreatefuncOptList(DDLStatementParser.CreatefuncOptListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createfuncOptItem}.
	 * @param ctx the parse tree
	 */
	void enterCreatefuncOptItem(DDLStatementParser.CreatefuncOptItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createfuncOptItem}.
	 * @param ctx the parse tree
	 */
	void exitCreatefuncOptItem(DDLStatementParser.CreatefuncOptItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#transformTypeList}.
	 * @param ctx the parse tree
	 */
	void enterTransformTypeList(DDLStatementParser.TransformTypeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#transformTypeList}.
	 * @param ctx the parse tree
	 */
	void exitTransformTypeList(DDLStatementParser.TransformTypeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#funcAs}.
	 * @param ctx the parse tree
	 */
	void enterFuncAs(DDLStatementParser.FuncAsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#funcAs}.
	 * @param ctx the parse tree
	 */
	void exitFuncAs(DDLStatementParser.FuncAsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#funcReturn}.
	 * @param ctx the parse tree
	 */
	void enterFuncReturn(DDLStatementParser.FuncReturnContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#funcReturn}.
	 * @param ctx the parse tree
	 */
	void exitFuncReturn(DDLStatementParser.FuncReturnContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#funcArgsWithDefaults}.
	 * @param ctx the parse tree
	 */
	void enterFuncArgsWithDefaults(DDLStatementParser.FuncArgsWithDefaultsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#funcArgsWithDefaults}.
	 * @param ctx the parse tree
	 */
	void exitFuncArgsWithDefaults(DDLStatementParser.FuncArgsWithDefaultsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#funcArgsWithDefaultsList}.
	 * @param ctx the parse tree
	 */
	void enterFuncArgsWithDefaultsList(DDLStatementParser.FuncArgsWithDefaultsListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#funcArgsWithDefaultsList}.
	 * @param ctx the parse tree
	 */
	void exitFuncArgsWithDefaultsList(DDLStatementParser.FuncArgsWithDefaultsListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#funcArgWithDefault}.
	 * @param ctx the parse tree
	 */
	void enterFuncArgWithDefault(DDLStatementParser.FuncArgWithDefaultContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#funcArgWithDefault}.
	 * @param ctx the parse tree
	 */
	void exitFuncArgWithDefault(DDLStatementParser.FuncArgWithDefaultContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createLanguage}.
	 * @param ctx the parse tree
	 */
	void enterCreateLanguage(DDLStatementParser.CreateLanguageContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createLanguage}.
	 * @param ctx the parse tree
	 */
	void exitCreateLanguage(DDLStatementParser.CreateLanguageContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#transformElementList}.
	 * @param ctx the parse tree
	 */
	void enterTransformElementList(DDLStatementParser.TransformElementListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#transformElementList}.
	 * @param ctx the parse tree
	 */
	void exitTransformElementList(DDLStatementParser.TransformElementListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#validatorClause}.
	 * @param ctx the parse tree
	 */
	void enterValidatorClause(DDLStatementParser.ValidatorClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#validatorClause}.
	 * @param ctx the parse tree
	 */
	void exitValidatorClause(DDLStatementParser.ValidatorClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createPolicy}.
	 * @param ctx the parse tree
	 */
	void enterCreatePolicy(DDLStatementParser.CreatePolicyContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createPolicy}.
	 * @param ctx the parse tree
	 */
	void exitCreatePolicy(DDLStatementParser.CreatePolicyContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createProcedure}.
	 * @param ctx the parse tree
	 */
	void enterCreateProcedure(DDLStatementParser.CreateProcedureContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createProcedure}.
	 * @param ctx the parse tree
	 */
	void exitCreateProcedure(DDLStatementParser.CreateProcedureContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createPublication}.
	 * @param ctx the parse tree
	 */
	void enterCreatePublication(DDLStatementParser.CreatePublicationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createPublication}.
	 * @param ctx the parse tree
	 */
	void exitCreatePublication(DDLStatementParser.CreatePublicationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#publicationForTables}.
	 * @param ctx the parse tree
	 */
	void enterPublicationForTables(DDLStatementParser.PublicationForTablesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#publicationForTables}.
	 * @param ctx the parse tree
	 */
	void exitPublicationForTables(DDLStatementParser.PublicationForTablesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createRule}.
	 * @param ctx the parse tree
	 */
	void enterCreateRule(DDLStatementParser.CreateRuleContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createRule}.
	 * @param ctx the parse tree
	 */
	void exitCreateRule(DDLStatementParser.CreateRuleContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#ruleActionList}.
	 * @param ctx the parse tree
	 */
	void enterRuleActionList(DDLStatementParser.RuleActionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#ruleActionList}.
	 * @param ctx the parse tree
	 */
	void exitRuleActionList(DDLStatementParser.RuleActionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#ruleActionStmt}.
	 * @param ctx the parse tree
	 */
	void enterRuleActionStmt(DDLStatementParser.RuleActionStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#ruleActionStmt}.
	 * @param ctx the parse tree
	 */
	void exitRuleActionStmt(DDLStatementParser.RuleActionStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#ruleActionMulti}.
	 * @param ctx the parse tree
	 */
	void enterRuleActionMulti(DDLStatementParser.RuleActionMultiContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#ruleActionMulti}.
	 * @param ctx the parse tree
	 */
	void exitRuleActionMulti(DDLStatementParser.RuleActionMultiContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#notifyStmt}.
	 * @param ctx the parse tree
	 */
	void enterNotifyStmt(DDLStatementParser.NotifyStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#notifyStmt}.
	 * @param ctx the parse tree
	 */
	void exitNotifyStmt(DDLStatementParser.NotifyStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createTrigger}.
	 * @param ctx the parse tree
	 */
	void enterCreateTrigger(DDLStatementParser.CreateTriggerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createTrigger}.
	 * @param ctx the parse tree
	 */
	void exitCreateTrigger(DDLStatementParser.CreateTriggerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#triggerEvents}.
	 * @param ctx the parse tree
	 */
	void enterTriggerEvents(DDLStatementParser.TriggerEventsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#triggerEvents}.
	 * @param ctx the parse tree
	 */
	void exitTriggerEvents(DDLStatementParser.TriggerEventsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#triggerOneEvent}.
	 * @param ctx the parse tree
	 */
	void enterTriggerOneEvent(DDLStatementParser.TriggerOneEventContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#triggerOneEvent}.
	 * @param ctx the parse tree
	 */
	void exitTriggerOneEvent(DDLStatementParser.TriggerOneEventContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#triggerActionTime}.
	 * @param ctx the parse tree
	 */
	void enterTriggerActionTime(DDLStatementParser.TriggerActionTimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#triggerActionTime}.
	 * @param ctx the parse tree
	 */
	void exitTriggerActionTime(DDLStatementParser.TriggerActionTimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#triggerFuncArgs}.
	 * @param ctx the parse tree
	 */
	void enterTriggerFuncArgs(DDLStatementParser.TriggerFuncArgsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#triggerFuncArgs}.
	 * @param ctx the parse tree
	 */
	void exitTriggerFuncArgs(DDLStatementParser.TriggerFuncArgsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#triggerFuncArg}.
	 * @param ctx the parse tree
	 */
	void enterTriggerFuncArg(DDLStatementParser.TriggerFuncArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#triggerFuncArg}.
	 * @param ctx the parse tree
	 */
	void exitTriggerFuncArg(DDLStatementParser.TriggerFuncArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#triggerWhen}.
	 * @param ctx the parse tree
	 */
	void enterTriggerWhen(DDLStatementParser.TriggerWhenContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#triggerWhen}.
	 * @param ctx the parse tree
	 */
	void exitTriggerWhen(DDLStatementParser.TriggerWhenContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#triggerForSpec}.
	 * @param ctx the parse tree
	 */
	void enterTriggerForSpec(DDLStatementParser.TriggerForSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#triggerForSpec}.
	 * @param ctx the parse tree
	 */
	void exitTriggerForSpec(DDLStatementParser.TriggerForSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#triggerReferencing}.
	 * @param ctx the parse tree
	 */
	void enterTriggerReferencing(DDLStatementParser.TriggerReferencingContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#triggerReferencing}.
	 * @param ctx the parse tree
	 */
	void exitTriggerReferencing(DDLStatementParser.TriggerReferencingContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#triggerTransitions}.
	 * @param ctx the parse tree
	 */
	void enterTriggerTransitions(DDLStatementParser.TriggerTransitionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#triggerTransitions}.
	 * @param ctx the parse tree
	 */
	void exitTriggerTransitions(DDLStatementParser.TriggerTransitionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#triggerTransition}.
	 * @param ctx the parse tree
	 */
	void enterTriggerTransition(DDLStatementParser.TriggerTransitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#triggerTransition}.
	 * @param ctx the parse tree
	 */
	void exitTriggerTransition(DDLStatementParser.TriggerTransitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#transitionRelName}.
	 * @param ctx the parse tree
	 */
	void enterTransitionRelName(DDLStatementParser.TransitionRelNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#transitionRelName}.
	 * @param ctx the parse tree
	 */
	void exitTransitionRelName(DDLStatementParser.TransitionRelNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#transitionRowOrTable}.
	 * @param ctx the parse tree
	 */
	void enterTransitionRowOrTable(DDLStatementParser.TransitionRowOrTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#transitionRowOrTable}.
	 * @param ctx the parse tree
	 */
	void exitTransitionRowOrTable(DDLStatementParser.TransitionRowOrTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#transitionOldOrNew}.
	 * @param ctx the parse tree
	 */
	void enterTransitionOldOrNew(DDLStatementParser.TransitionOldOrNewContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#transitionOldOrNew}.
	 * @param ctx the parse tree
	 */
	void exitTransitionOldOrNew(DDLStatementParser.TransitionOldOrNewContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createSequence}.
	 * @param ctx the parse tree
	 */
	void enterCreateSequence(DDLStatementParser.CreateSequenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createSequence}.
	 * @param ctx the parse tree
	 */
	void exitCreateSequence(DDLStatementParser.CreateSequenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#tempOption}.
	 * @param ctx the parse tree
	 */
	void enterTempOption(DDLStatementParser.TempOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#tempOption}.
	 * @param ctx the parse tree
	 */
	void exitTempOption(DDLStatementParser.TempOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createServer}.
	 * @param ctx the parse tree
	 */
	void enterCreateServer(DDLStatementParser.CreateServerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createServer}.
	 * @param ctx the parse tree
	 */
	void exitCreateServer(DDLStatementParser.CreateServerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createStatistics}.
	 * @param ctx the parse tree
	 */
	void enterCreateStatistics(DDLStatementParser.CreateStatisticsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createStatistics}.
	 * @param ctx the parse tree
	 */
	void exitCreateStatistics(DDLStatementParser.CreateStatisticsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createSubscription}.
	 * @param ctx the parse tree
	 */
	void enterCreateSubscription(DDLStatementParser.CreateSubscriptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createSubscription}.
	 * @param ctx the parse tree
	 */
	void exitCreateSubscription(DDLStatementParser.CreateSubscriptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createTablespace}.
	 * @param ctx the parse tree
	 */
	void enterCreateTablespace(DDLStatementParser.CreateTablespaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createTablespace}.
	 * @param ctx the parse tree
	 */
	void exitCreateTablespace(DDLStatementParser.CreateTablespaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createTextSearch}.
	 * @param ctx the parse tree
	 */
	void enterCreateTextSearch(DDLStatementParser.CreateTextSearchContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createTextSearch}.
	 * @param ctx the parse tree
	 */
	void exitCreateTextSearch(DDLStatementParser.CreateTextSearchContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createTransform}.
	 * @param ctx the parse tree
	 */
	void enterCreateTransform(DDLStatementParser.CreateTransformContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createTransform}.
	 * @param ctx the parse tree
	 */
	void exitCreateTransform(DDLStatementParser.CreateTransformContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createType}.
	 * @param ctx the parse tree
	 */
	void enterCreateType(DDLStatementParser.CreateTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createType}.
	 * @param ctx the parse tree
	 */
	void exitCreateType(DDLStatementParser.CreateTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createTypeClauses}.
	 * @param ctx the parse tree
	 */
	void enterCreateTypeClauses(DDLStatementParser.CreateTypeClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createTypeClauses}.
	 * @param ctx the parse tree
	 */
	void exitCreateTypeClauses(DDLStatementParser.CreateTypeClausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#enumValList}.
	 * @param ctx the parse tree
	 */
	void enterEnumValList(DDLStatementParser.EnumValListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#enumValList}.
	 * @param ctx the parse tree
	 */
	void exitEnumValList(DDLStatementParser.EnumValListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createUserMapping}.
	 * @param ctx the parse tree
	 */
	void enterCreateUserMapping(DDLStatementParser.CreateUserMappingContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createUserMapping}.
	 * @param ctx the parse tree
	 */
	void exitCreateUserMapping(DDLStatementParser.CreateUserMappingContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#discard}.
	 * @param ctx the parse tree
	 */
	void enterDiscard(DDLStatementParser.DiscardContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#discard}.
	 * @param ctx the parse tree
	 */
	void exitDiscard(DDLStatementParser.DiscardContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropAccessMethod}.
	 * @param ctx the parse tree
	 */
	void enterDropAccessMethod(DDLStatementParser.DropAccessMethodContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropAccessMethod}.
	 * @param ctx the parse tree
	 */
	void exitDropAccessMethod(DDLStatementParser.DropAccessMethodContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropAggregate}.
	 * @param ctx the parse tree
	 */
	void enterDropAggregate(DDLStatementParser.DropAggregateContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropAggregate}.
	 * @param ctx the parse tree
	 */
	void exitDropAggregate(DDLStatementParser.DropAggregateContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#aggregateWithArgtypesList}.
	 * @param ctx the parse tree
	 */
	void enterAggregateWithArgtypesList(DDLStatementParser.AggregateWithArgtypesListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#aggregateWithArgtypesList}.
	 * @param ctx the parse tree
	 */
	void exitAggregateWithArgtypesList(DDLStatementParser.AggregateWithArgtypesListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropCast}.
	 * @param ctx the parse tree
	 */
	void enterDropCast(DDLStatementParser.DropCastContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropCast}.
	 * @param ctx the parse tree
	 */
	void exitDropCast(DDLStatementParser.DropCastContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropCollation}.
	 * @param ctx the parse tree
	 */
	void enterDropCollation(DDLStatementParser.DropCollationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropCollation}.
	 * @param ctx the parse tree
	 */
	void exitDropCollation(DDLStatementParser.DropCollationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropConversion}.
	 * @param ctx the parse tree
	 */
	void enterDropConversion(DDLStatementParser.DropConversionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropConversion}.
	 * @param ctx the parse tree
	 */
	void exitDropConversion(DDLStatementParser.DropConversionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropDomain}.
	 * @param ctx the parse tree
	 */
	void enterDropDomain(DDLStatementParser.DropDomainContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropDomain}.
	 * @param ctx the parse tree
	 */
	void exitDropDomain(DDLStatementParser.DropDomainContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropEventTrigger}.
	 * @param ctx the parse tree
	 */
	void enterDropEventTrigger(DDLStatementParser.DropEventTriggerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropEventTrigger}.
	 * @param ctx the parse tree
	 */
	void exitDropEventTrigger(DDLStatementParser.DropEventTriggerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropExtension}.
	 * @param ctx the parse tree
	 */
	void enterDropExtension(DDLStatementParser.DropExtensionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropExtension}.
	 * @param ctx the parse tree
	 */
	void exitDropExtension(DDLStatementParser.DropExtensionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropForeignDataWrapper}.
	 * @param ctx the parse tree
	 */
	void enterDropForeignDataWrapper(DDLStatementParser.DropForeignDataWrapperContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropForeignDataWrapper}.
	 * @param ctx the parse tree
	 */
	void exitDropForeignDataWrapper(DDLStatementParser.DropForeignDataWrapperContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropForeignTable}.
	 * @param ctx the parse tree
	 */
	void enterDropForeignTable(DDLStatementParser.DropForeignTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropForeignTable}.
	 * @param ctx the parse tree
	 */
	void exitDropForeignTable(DDLStatementParser.DropForeignTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropFunction}.
	 * @param ctx the parse tree
	 */
	void enterDropFunction(DDLStatementParser.DropFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropFunction}.
	 * @param ctx the parse tree
	 */
	void exitDropFunction(DDLStatementParser.DropFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#functionWithArgtypesList}.
	 * @param ctx the parse tree
	 */
	void enterFunctionWithArgtypesList(DDLStatementParser.FunctionWithArgtypesListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#functionWithArgtypesList}.
	 * @param ctx the parse tree
	 */
	void exitFunctionWithArgtypesList(DDLStatementParser.FunctionWithArgtypesListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropLanguage}.
	 * @param ctx the parse tree
	 */
	void enterDropLanguage(DDLStatementParser.DropLanguageContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropLanguage}.
	 * @param ctx the parse tree
	 */
	void exitDropLanguage(DDLStatementParser.DropLanguageContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropMaterializedView}.
	 * @param ctx the parse tree
	 */
	void enterDropMaterializedView(DDLStatementParser.DropMaterializedViewContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropMaterializedView}.
	 * @param ctx the parse tree
	 */
	void exitDropMaterializedView(DDLStatementParser.DropMaterializedViewContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropOperator}.
	 * @param ctx the parse tree
	 */
	void enterDropOperator(DDLStatementParser.DropOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropOperator}.
	 * @param ctx the parse tree
	 */
	void exitDropOperator(DDLStatementParser.DropOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#operatorWithArgtypesList}.
	 * @param ctx the parse tree
	 */
	void enterOperatorWithArgtypesList(DDLStatementParser.OperatorWithArgtypesListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#operatorWithArgtypesList}.
	 * @param ctx the parse tree
	 */
	void exitOperatorWithArgtypesList(DDLStatementParser.OperatorWithArgtypesListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropOperatorClass}.
	 * @param ctx the parse tree
	 */
	void enterDropOperatorClass(DDLStatementParser.DropOperatorClassContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropOperatorClass}.
	 * @param ctx the parse tree
	 */
	void exitDropOperatorClass(DDLStatementParser.DropOperatorClassContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropOperatorFamily}.
	 * @param ctx the parse tree
	 */
	void enterDropOperatorFamily(DDLStatementParser.DropOperatorFamilyContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropOperatorFamily}.
	 * @param ctx the parse tree
	 */
	void exitDropOperatorFamily(DDLStatementParser.DropOperatorFamilyContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropOwned}.
	 * @param ctx the parse tree
	 */
	void enterDropOwned(DDLStatementParser.DropOwnedContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropOwned}.
	 * @param ctx the parse tree
	 */
	void exitDropOwned(DDLStatementParser.DropOwnedContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropPolicy}.
	 * @param ctx the parse tree
	 */
	void enterDropPolicy(DDLStatementParser.DropPolicyContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropPolicy}.
	 * @param ctx the parse tree
	 */
	void exitDropPolicy(DDLStatementParser.DropPolicyContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropProcedure}.
	 * @param ctx the parse tree
	 */
	void enterDropProcedure(DDLStatementParser.DropProcedureContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropProcedure}.
	 * @param ctx the parse tree
	 */
	void exitDropProcedure(DDLStatementParser.DropProcedureContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropPublication}.
	 * @param ctx the parse tree
	 */
	void enterDropPublication(DDLStatementParser.DropPublicationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropPublication}.
	 * @param ctx the parse tree
	 */
	void exitDropPublication(DDLStatementParser.DropPublicationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropRoutine}.
	 * @param ctx the parse tree
	 */
	void enterDropRoutine(DDLStatementParser.DropRoutineContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropRoutine}.
	 * @param ctx the parse tree
	 */
	void exitDropRoutine(DDLStatementParser.DropRoutineContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropRule}.
	 * @param ctx the parse tree
	 */
	void enterDropRule(DDLStatementParser.DropRuleContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropRule}.
	 * @param ctx the parse tree
	 */
	void exitDropRule(DDLStatementParser.DropRuleContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropSequence}.
	 * @param ctx the parse tree
	 */
	void enterDropSequence(DDLStatementParser.DropSequenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropSequence}.
	 * @param ctx the parse tree
	 */
	void exitDropSequence(DDLStatementParser.DropSequenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropServer}.
	 * @param ctx the parse tree
	 */
	void enterDropServer(DDLStatementParser.DropServerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropServer}.
	 * @param ctx the parse tree
	 */
	void exitDropServer(DDLStatementParser.DropServerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropStatistics}.
	 * @param ctx the parse tree
	 */
	void enterDropStatistics(DDLStatementParser.DropStatisticsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropStatistics}.
	 * @param ctx the parse tree
	 */
	void exitDropStatistics(DDLStatementParser.DropStatisticsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropSubscription}.
	 * @param ctx the parse tree
	 */
	void enterDropSubscription(DDLStatementParser.DropSubscriptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropSubscription}.
	 * @param ctx the parse tree
	 */
	void exitDropSubscription(DDLStatementParser.DropSubscriptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropTablespace}.
	 * @param ctx the parse tree
	 */
	void enterDropTablespace(DDLStatementParser.DropTablespaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropTablespace}.
	 * @param ctx the parse tree
	 */
	void exitDropTablespace(DDLStatementParser.DropTablespaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropTextSearch}.
	 * @param ctx the parse tree
	 */
	void enterDropTextSearch(DDLStatementParser.DropTextSearchContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropTextSearch}.
	 * @param ctx the parse tree
	 */
	void exitDropTextSearch(DDLStatementParser.DropTextSearchContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropTransform}.
	 * @param ctx the parse tree
	 */
	void enterDropTransform(DDLStatementParser.DropTransformContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropTransform}.
	 * @param ctx the parse tree
	 */
	void exitDropTransform(DDLStatementParser.DropTransformContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropTrigger}.
	 * @param ctx the parse tree
	 */
	void enterDropTrigger(DDLStatementParser.DropTriggerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropTrigger}.
	 * @param ctx the parse tree
	 */
	void exitDropTrigger(DDLStatementParser.DropTriggerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropType}.
	 * @param ctx the parse tree
	 */
	void enterDropType(DDLStatementParser.DropTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropType}.
	 * @param ctx the parse tree
	 */
	void exitDropType(DDLStatementParser.DropTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropUserMapping}.
	 * @param ctx the parse tree
	 */
	void enterDropUserMapping(DDLStatementParser.DropUserMappingContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropUserMapping}.
	 * @param ctx the parse tree
	 */
	void exitDropUserMapping(DDLStatementParser.DropUserMappingContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dropView}.
	 * @param ctx the parse tree
	 */
	void enterDropView(DDLStatementParser.DropViewContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dropView}.
	 * @param ctx the parse tree
	 */
	void exitDropView(DDLStatementParser.DropViewContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#importForeignSchema}.
	 * @param ctx the parse tree
	 */
	void enterImportForeignSchema(DDLStatementParser.ImportForeignSchemaContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#importForeignSchema}.
	 * @param ctx the parse tree
	 */
	void exitImportForeignSchema(DDLStatementParser.ImportForeignSchemaContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#importQualification}.
	 * @param ctx the parse tree
	 */
	void enterImportQualification(DDLStatementParser.ImportQualificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#importQualification}.
	 * @param ctx the parse tree
	 */
	void exitImportQualification(DDLStatementParser.ImportQualificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#importQualificationType}.
	 * @param ctx the parse tree
	 */
	void enterImportQualificationType(DDLStatementParser.ImportQualificationTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#importQualificationType}.
	 * @param ctx the parse tree
	 */
	void exitImportQualificationType(DDLStatementParser.ImportQualificationTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#listen}.
	 * @param ctx the parse tree
	 */
	void enterListen(DDLStatementParser.ListenContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#listen}.
	 * @param ctx the parse tree
	 */
	void exitListen(DDLStatementParser.ListenContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#move}.
	 * @param ctx the parse tree
	 */
	void enterMove(DDLStatementParser.MoveContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#move}.
	 * @param ctx the parse tree
	 */
	void exitMove(DDLStatementParser.MoveContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#prepare}.
	 * @param ctx the parse tree
	 */
	void enterPrepare(DDLStatementParser.PrepareContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#prepare}.
	 * @param ctx the parse tree
	 */
	void exitPrepare(DDLStatementParser.PrepareContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#deallocate}.
	 * @param ctx the parse tree
	 */
	void enterDeallocate(DDLStatementParser.DeallocateContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#deallocate}.
	 * @param ctx the parse tree
	 */
	void exitDeallocate(DDLStatementParser.DeallocateContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#prepTypeClause}.
	 * @param ctx the parse tree
	 */
	void enterPrepTypeClause(DDLStatementParser.PrepTypeClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#prepTypeClause}.
	 * @param ctx the parse tree
	 */
	void exitPrepTypeClause(DDLStatementParser.PrepTypeClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#refreshMaterializedView}.
	 * @param ctx the parse tree
	 */
	void enterRefreshMaterializedView(DDLStatementParser.RefreshMaterializedViewContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#refreshMaterializedView}.
	 * @param ctx the parse tree
	 */
	void exitRefreshMaterializedView(DDLStatementParser.RefreshMaterializedViewContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#reIndex}.
	 * @param ctx the parse tree
	 */
	void enterReIndex(DDLStatementParser.ReIndexContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#reIndex}.
	 * @param ctx the parse tree
	 */
	void exitReIndex(DDLStatementParser.ReIndexContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#reIndexClauses}.
	 * @param ctx the parse tree
	 */
	void enterReIndexClauses(DDLStatementParser.ReIndexClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#reIndexClauses}.
	 * @param ctx the parse tree
	 */
	void exitReIndexClauses(DDLStatementParser.ReIndexClausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#reindexOptionList}.
	 * @param ctx the parse tree
	 */
	void enterReindexOptionList(DDLStatementParser.ReindexOptionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#reindexOptionList}.
	 * @param ctx the parse tree
	 */
	void exitReindexOptionList(DDLStatementParser.ReindexOptionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#reindexOptionElem}.
	 * @param ctx the parse tree
	 */
	void enterReindexOptionElem(DDLStatementParser.ReindexOptionElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#reindexOptionElem}.
	 * @param ctx the parse tree
	 */
	void exitReindexOptionElem(DDLStatementParser.ReindexOptionElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#reindexTargetMultitable}.
	 * @param ctx the parse tree
	 */
	void enterReindexTargetMultitable(DDLStatementParser.ReindexTargetMultitableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#reindexTargetMultitable}.
	 * @param ctx the parse tree
	 */
	void exitReindexTargetMultitable(DDLStatementParser.ReindexTargetMultitableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#reindexTargetType}.
	 * @param ctx the parse tree
	 */
	void enterReindexTargetType(DDLStatementParser.ReindexTargetTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#reindexTargetType}.
	 * @param ctx the parse tree
	 */
	void exitReindexTargetType(DDLStatementParser.ReindexTargetTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterForeignTable}.
	 * @param ctx the parse tree
	 */
	void enterAlterForeignTable(DDLStatementParser.AlterForeignTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterForeignTable}.
	 * @param ctx the parse tree
	 */
	void exitAlterForeignTable(DDLStatementParser.AlterForeignTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alterForeignTableClauses}.
	 * @param ctx the parse tree
	 */
	void enterAlterForeignTableClauses(DDLStatementParser.AlterForeignTableClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alterForeignTableClauses}.
	 * @param ctx the parse tree
	 */
	void exitAlterForeignTableClauses(DDLStatementParser.AlterForeignTableClausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createOperator}.
	 * @param ctx the parse tree
	 */
	void enterCreateOperator(DDLStatementParser.CreateOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createOperator}.
	 * @param ctx the parse tree
	 */
	void exitCreateOperator(DDLStatementParser.CreateOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createOperatorClass}.
	 * @param ctx the parse tree
	 */
	void enterCreateOperatorClass(DDLStatementParser.CreateOperatorClassContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createOperatorClass}.
	 * @param ctx the parse tree
	 */
	void exitCreateOperatorClass(DDLStatementParser.CreateOperatorClassContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createOperatorFamily}.
	 * @param ctx the parse tree
	 */
	void enterCreateOperatorFamily(DDLStatementParser.CreateOperatorFamilyContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createOperatorFamily}.
	 * @param ctx the parse tree
	 */
	void exitCreateOperatorFamily(DDLStatementParser.CreateOperatorFamilyContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#securityLabelStmt}.
	 * @param ctx the parse tree
	 */
	void enterSecurityLabelStmt(DDLStatementParser.SecurityLabelStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#securityLabelStmt}.
	 * @param ctx the parse tree
	 */
	void exitSecurityLabelStmt(DDLStatementParser.SecurityLabelStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#securityLabel}.
	 * @param ctx the parse tree
	 */
	void enterSecurityLabel(DDLStatementParser.SecurityLabelContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#securityLabel}.
	 * @param ctx the parse tree
	 */
	void exitSecurityLabel(DDLStatementParser.SecurityLabelContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#securityLabelClausces}.
	 * @param ctx the parse tree
	 */
	void enterSecurityLabelClausces(DDLStatementParser.SecurityLabelClauscesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#securityLabelClausces}.
	 * @param ctx the parse tree
	 */
	void exitSecurityLabelClausces(DDLStatementParser.SecurityLabelClauscesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#unlisten}.
	 * @param ctx the parse tree
	 */
	void enterUnlisten(DDLStatementParser.UnlistenContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#unlisten}.
	 * @param ctx the parse tree
	 */
	void exitUnlisten(DDLStatementParser.UnlistenContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#parameterMarker}.
	 * @param ctx the parse tree
	 */
	void enterParameterMarker(DDLStatementParser.ParameterMarkerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#parameterMarker}.
	 * @param ctx the parse tree
	 */
	void exitParameterMarker(DDLStatementParser.ParameterMarkerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#reservedKeyword}.
	 * @param ctx the parse tree
	 */
	void enterReservedKeyword(DDLStatementParser.ReservedKeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#reservedKeyword}.
	 * @param ctx the parse tree
	 */
	void exitReservedKeyword(DDLStatementParser.ReservedKeywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#numberLiterals}.
	 * @param ctx the parse tree
	 */
	void enterNumberLiterals(DDLStatementParser.NumberLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#numberLiterals}.
	 * @param ctx the parse tree
	 */
	void exitNumberLiterals(DDLStatementParser.NumberLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#literalsType}.
	 * @param ctx the parse tree
	 */
	void enterLiteralsType(DDLStatementParser.LiteralsTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#literalsType}.
	 * @param ctx the parse tree
	 */
	void exitLiteralsType(DDLStatementParser.LiteralsTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(DDLStatementParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(DDLStatementParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#unicodeEscapes}.
	 * @param ctx the parse tree
	 */
	void enterUnicodeEscapes(DDLStatementParser.UnicodeEscapesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#unicodeEscapes}.
	 * @param ctx the parse tree
	 */
	void exitUnicodeEscapes(DDLStatementParser.UnicodeEscapesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#uescape}.
	 * @param ctx the parse tree
	 */
	void enterUescape(DDLStatementParser.UescapeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#uescape}.
	 * @param ctx the parse tree
	 */
	void exitUescape(DDLStatementParser.UescapeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#unreservedWord}.
	 * @param ctx the parse tree
	 */
	void enterUnreservedWord(DDLStatementParser.UnreservedWordContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#unreservedWord}.
	 * @param ctx the parse tree
	 */
	void exitUnreservedWord(DDLStatementParser.UnreservedWordContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#typeFuncNameKeyword}.
	 * @param ctx the parse tree
	 */
	void enterTypeFuncNameKeyword(DDLStatementParser.TypeFuncNameKeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#typeFuncNameKeyword}.
	 * @param ctx the parse tree
	 */
	void exitTypeFuncNameKeyword(DDLStatementParser.TypeFuncNameKeywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#schemaName}.
	 * @param ctx the parse tree
	 */
	void enterSchemaName(DDLStatementParser.SchemaNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#schemaName}.
	 * @param ctx the parse tree
	 */
	void exitSchemaName(DDLStatementParser.SchemaNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#tableName}.
	 * @param ctx the parse tree
	 */
	void enterTableName(DDLStatementParser.TableNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#tableName}.
	 * @param ctx the parse tree
	 */
	void exitTableName(DDLStatementParser.TableNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#columnName}.
	 * @param ctx the parse tree
	 */
	void enterColumnName(DDLStatementParser.ColumnNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#columnName}.
	 * @param ctx the parse tree
	 */
	void exitColumnName(DDLStatementParser.ColumnNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#owner}.
	 * @param ctx the parse tree
	 */
	void enterOwner(DDLStatementParser.OwnerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#owner}.
	 * @param ctx the parse tree
	 */
	void exitOwner(DDLStatementParser.OwnerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#name}.
	 * @param ctx the parse tree
	 */
	void enterName(DDLStatementParser.NameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#name}.
	 * @param ctx the parse tree
	 */
	void exitName(DDLStatementParser.NameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#tableNames}.
	 * @param ctx the parse tree
	 */
	void enterTableNames(DDLStatementParser.TableNamesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#tableNames}.
	 * @param ctx the parse tree
	 */
	void exitTableNames(DDLStatementParser.TableNamesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#columnNames}.
	 * @param ctx the parse tree
	 */
	void enterColumnNames(DDLStatementParser.ColumnNamesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#columnNames}.
	 * @param ctx the parse tree
	 */
	void exitColumnNames(DDLStatementParser.ColumnNamesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#collationName}.
	 * @param ctx the parse tree
	 */
	void enterCollationName(DDLStatementParser.CollationNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#collationName}.
	 * @param ctx the parse tree
	 */
	void exitCollationName(DDLStatementParser.CollationNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#indexName}.
	 * @param ctx the parse tree
	 */
	void enterIndexName(DDLStatementParser.IndexNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#indexName}.
	 * @param ctx the parse tree
	 */
	void exitIndexName(DDLStatementParser.IndexNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#alias}.
	 * @param ctx the parse tree
	 */
	void enterAlias(DDLStatementParser.AliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#alias}.
	 * @param ctx the parse tree
	 */
	void exitAlias(DDLStatementParser.AliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#primaryKey}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryKey(DDLStatementParser.PrimaryKeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#primaryKey}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryKey(DDLStatementParser.PrimaryKeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#logicalOperator}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOperator(DDLStatementParser.LogicalOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#logicalOperator}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOperator(DDLStatementParser.LogicalOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOperator(DDLStatementParser.ComparisonOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOperator(DDLStatementParser.ComparisonOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#patternMatchingOperator}.
	 * @param ctx the parse tree
	 */
	void enterPatternMatchingOperator(DDLStatementParser.PatternMatchingOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#patternMatchingOperator}.
	 * @param ctx the parse tree
	 */
	void exitPatternMatchingOperator(DDLStatementParser.PatternMatchingOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#cursorName}.
	 * @param ctx the parse tree
	 */
	void enterCursorName(DDLStatementParser.CursorNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#cursorName}.
	 * @param ctx the parse tree
	 */
	void exitCursorName(DDLStatementParser.CursorNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#aExpr}.
	 * @param ctx the parse tree
	 */
	void enterAExpr(DDLStatementParser.AExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#aExpr}.
	 * @param ctx the parse tree
	 */
	void exitAExpr(DDLStatementParser.AExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#bExpr}.
	 * @param ctx the parse tree
	 */
	void enterBExpr(DDLStatementParser.BExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#bExpr}.
	 * @param ctx the parse tree
	 */
	void exitBExpr(DDLStatementParser.BExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#cExpr}.
	 * @param ctx the parse tree
	 */
	void enterCExpr(DDLStatementParser.CExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#cExpr}.
	 * @param ctx the parse tree
	 */
	void exitCExpr(DDLStatementParser.CExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#indirection}.
	 * @param ctx the parse tree
	 */
	void enterIndirection(DDLStatementParser.IndirectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#indirection}.
	 * @param ctx the parse tree
	 */
	void exitIndirection(DDLStatementParser.IndirectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#optIndirection}.
	 * @param ctx the parse tree
	 */
	void enterOptIndirection(DDLStatementParser.OptIndirectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#optIndirection}.
	 * @param ctx the parse tree
	 */
	void exitOptIndirection(DDLStatementParser.OptIndirectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#indirectionEl}.
	 * @param ctx the parse tree
	 */
	void enterIndirectionEl(DDLStatementParser.IndirectionElContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#indirectionEl}.
	 * @param ctx the parse tree
	 */
	void exitIndirectionEl(DDLStatementParser.IndirectionElContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#sliceBound}.
	 * @param ctx the parse tree
	 */
	void enterSliceBound(DDLStatementParser.SliceBoundContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#sliceBound}.
	 * @param ctx the parse tree
	 */
	void exitSliceBound(DDLStatementParser.SliceBoundContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#inExpr}.
	 * @param ctx the parse tree
	 */
	void enterInExpr(DDLStatementParser.InExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#inExpr}.
	 * @param ctx the parse tree
	 */
	void exitInExpr(DDLStatementParser.InExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#caseExpr}.
	 * @param ctx the parse tree
	 */
	void enterCaseExpr(DDLStatementParser.CaseExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#caseExpr}.
	 * @param ctx the parse tree
	 */
	void exitCaseExpr(DDLStatementParser.CaseExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#whenClauseList}.
	 * @param ctx the parse tree
	 */
	void enterWhenClauseList(DDLStatementParser.WhenClauseListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#whenClauseList}.
	 * @param ctx the parse tree
	 */
	void exitWhenClauseList(DDLStatementParser.WhenClauseListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#whenClause}.
	 * @param ctx the parse tree
	 */
	void enterWhenClause(DDLStatementParser.WhenClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#whenClause}.
	 * @param ctx the parse tree
	 */
	void exitWhenClause(DDLStatementParser.WhenClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#caseDefault}.
	 * @param ctx the parse tree
	 */
	void enterCaseDefault(DDLStatementParser.CaseDefaultContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#caseDefault}.
	 * @param ctx the parse tree
	 */
	void exitCaseDefault(DDLStatementParser.CaseDefaultContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#caseArg}.
	 * @param ctx the parse tree
	 */
	void enterCaseArg(DDLStatementParser.CaseArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#caseArg}.
	 * @param ctx the parse tree
	 */
	void exitCaseArg(DDLStatementParser.CaseArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#columnref}.
	 * @param ctx the parse tree
	 */
	void enterColumnref(DDLStatementParser.ColumnrefContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#columnref}.
	 * @param ctx the parse tree
	 */
	void exitColumnref(DDLStatementParser.ColumnrefContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#qualOp}.
	 * @param ctx the parse tree
	 */
	void enterQualOp(DDLStatementParser.QualOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#qualOp}.
	 * @param ctx the parse tree
	 */
	void exitQualOp(DDLStatementParser.QualOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#subqueryOp}.
	 * @param ctx the parse tree
	 */
	void enterSubqueryOp(DDLStatementParser.SubqueryOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#subqueryOp}.
	 * @param ctx the parse tree
	 */
	void exitSubqueryOp(DDLStatementParser.SubqueryOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#allOp}.
	 * @param ctx the parse tree
	 */
	void enterAllOp(DDLStatementParser.AllOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#allOp}.
	 * @param ctx the parse tree
	 */
	void exitAllOp(DDLStatementParser.AllOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#op}.
	 * @param ctx the parse tree
	 */
	void enterOp(DDLStatementParser.OpContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#op}.
	 * @param ctx the parse tree
	 */
	void exitOp(DDLStatementParser.OpContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#mathOperator}.
	 * @param ctx the parse tree
	 */
	void enterMathOperator(DDLStatementParser.MathOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#mathOperator}.
	 * @param ctx the parse tree
	 */
	void exitMathOperator(DDLStatementParser.MathOperatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonExtract}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonExtract(DDLStatementParser.JsonExtractContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonExtract}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonExtract(DDLStatementParser.JsonExtractContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonExtractText}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonExtractText(DDLStatementParser.JsonExtractTextContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonExtractText}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonExtractText(DDLStatementParser.JsonExtractTextContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonPathExtract}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonPathExtract(DDLStatementParser.JsonPathExtractContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonPathExtract}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonPathExtract(DDLStatementParser.JsonPathExtractContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonPathExtractText}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonPathExtractText(DDLStatementParser.JsonPathExtractTextContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonPathExtractText}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonPathExtractText(DDLStatementParser.JsonPathExtractTextContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbContainRight}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbContainRight(DDLStatementParser.JsonbContainRightContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbContainRight}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbContainRight(DDLStatementParser.JsonbContainRightContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbContainLeft}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbContainLeft(DDLStatementParser.JsonbContainLeftContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbContainLeft}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbContainLeft(DDLStatementParser.JsonbContainLeftContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbContainTopKey}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbContainTopKey(DDLStatementParser.JsonbContainTopKeyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbContainTopKey}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbContainTopKey(DDLStatementParser.JsonbContainTopKeyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbContainAnyTopKey}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbContainAnyTopKey(DDLStatementParser.JsonbContainAnyTopKeyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbContainAnyTopKey}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbContainAnyTopKey(DDLStatementParser.JsonbContainAnyTopKeyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbContainAllTopKey}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbContainAllTopKey(DDLStatementParser.JsonbContainAllTopKeyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbContainAllTopKey}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbContainAllTopKey(DDLStatementParser.JsonbContainAllTopKeyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbConcat}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbConcat(DDLStatementParser.JsonbConcatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbConcat}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbConcat(DDLStatementParser.JsonbConcatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbDelete}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbDelete(DDLStatementParser.JsonbDeleteContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbDelete}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbDelete(DDLStatementParser.JsonbDeleteContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbPathDelete}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbPathDelete(DDLStatementParser.JsonbPathDeleteContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbPathDelete}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbPathDelete(DDLStatementParser.JsonbPathDeleteContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbPathContainAnyValue}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbPathContainAnyValue(DDLStatementParser.JsonbPathContainAnyValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbPathContainAnyValue}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbPathContainAnyValue(DDLStatementParser.JsonbPathContainAnyValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbPathPredicateCheck}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbPathPredicateCheck(DDLStatementParser.JsonbPathPredicateCheckContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbPathPredicateCheck}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbPathPredicateCheck(DDLStatementParser.JsonbPathPredicateCheckContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#qualAllOp}.
	 * @param ctx the parse tree
	 */
	void enterQualAllOp(DDLStatementParser.QualAllOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#qualAllOp}.
	 * @param ctx the parse tree
	 */
	void exitQualAllOp(DDLStatementParser.QualAllOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#ascDesc}.
	 * @param ctx the parse tree
	 */
	void enterAscDesc(DDLStatementParser.AscDescContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#ascDesc}.
	 * @param ctx the parse tree
	 */
	void exitAscDesc(DDLStatementParser.AscDescContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#anyOperator}.
	 * @param ctx the parse tree
	 */
	void enterAnyOperator(DDLStatementParser.AnyOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#anyOperator}.
	 * @param ctx the parse tree
	 */
	void exitAnyOperator(DDLStatementParser.AnyOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#frameClause}.
	 * @param ctx the parse tree
	 */
	void enterFrameClause(DDLStatementParser.FrameClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#frameClause}.
	 * @param ctx the parse tree
	 */
	void exitFrameClause(DDLStatementParser.FrameClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#frameExtent}.
	 * @param ctx the parse tree
	 */
	void enterFrameExtent(DDLStatementParser.FrameExtentContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#frameExtent}.
	 * @param ctx the parse tree
	 */
	void exitFrameExtent(DDLStatementParser.FrameExtentContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#frameBound}.
	 * @param ctx the parse tree
	 */
	void enterFrameBound(DDLStatementParser.FrameBoundContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#frameBound}.
	 * @param ctx the parse tree
	 */
	void exitFrameBound(DDLStatementParser.FrameBoundContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#windowExclusionClause}.
	 * @param ctx the parse tree
	 */
	void enterWindowExclusionClause(DDLStatementParser.WindowExclusionClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#windowExclusionClause}.
	 * @param ctx the parse tree
	 */
	void exitWindowExclusionClause(DDLStatementParser.WindowExclusionClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#row}.
	 * @param ctx the parse tree
	 */
	void enterRow(DDLStatementParser.RowContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#row}.
	 * @param ctx the parse tree
	 */
	void exitRow(DDLStatementParser.RowContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#explicitRow}.
	 * @param ctx the parse tree
	 */
	void enterExplicitRow(DDLStatementParser.ExplicitRowContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#explicitRow}.
	 * @param ctx the parse tree
	 */
	void exitExplicitRow(DDLStatementParser.ExplicitRowContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#implicitRow}.
	 * @param ctx the parse tree
	 */
	void enterImplicitRow(DDLStatementParser.ImplicitRowContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#implicitRow}.
	 * @param ctx the parse tree
	 */
	void exitImplicitRow(DDLStatementParser.ImplicitRowContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#subType}.
	 * @param ctx the parse tree
	 */
	void enterSubType(DDLStatementParser.SubTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#subType}.
	 * @param ctx the parse tree
	 */
	void exitSubType(DDLStatementParser.SubTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#arrayExpr}.
	 * @param ctx the parse tree
	 */
	void enterArrayExpr(DDLStatementParser.ArrayExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#arrayExpr}.
	 * @param ctx the parse tree
	 */
	void exitArrayExpr(DDLStatementParser.ArrayExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#arrayExprList}.
	 * @param ctx the parse tree
	 */
	void enterArrayExprList(DDLStatementParser.ArrayExprListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#arrayExprList}.
	 * @param ctx the parse tree
	 */
	void exitArrayExprList(DDLStatementParser.ArrayExprListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#funcArgList}.
	 * @param ctx the parse tree
	 */
	void enterFuncArgList(DDLStatementParser.FuncArgListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#funcArgList}.
	 * @param ctx the parse tree
	 */
	void exitFuncArgList(DDLStatementParser.FuncArgListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#paramName}.
	 * @param ctx the parse tree
	 */
	void enterParamName(DDLStatementParser.ParamNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#paramName}.
	 * @param ctx the parse tree
	 */
	void exitParamName(DDLStatementParser.ParamNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#funcArgExpr}.
	 * @param ctx the parse tree
	 */
	void enterFuncArgExpr(DDLStatementParser.FuncArgExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#funcArgExpr}.
	 * @param ctx the parse tree
	 */
	void exitFuncArgExpr(DDLStatementParser.FuncArgExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#typeList}.
	 * @param ctx the parse tree
	 */
	void enterTypeList(DDLStatementParser.TypeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#typeList}.
	 * @param ctx the parse tree
	 */
	void exitTypeList(DDLStatementParser.TypeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#funcApplication}.
	 * @param ctx the parse tree
	 */
	void enterFuncApplication(DDLStatementParser.FuncApplicationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#funcApplication}.
	 * @param ctx the parse tree
	 */
	void exitFuncApplication(DDLStatementParser.FuncApplicationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#funcName}.
	 * @param ctx the parse tree
	 */
	void enterFuncName(DDLStatementParser.FuncNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#funcName}.
	 * @param ctx the parse tree
	 */
	void exitFuncName(DDLStatementParser.FuncNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#aexprConst}.
	 * @param ctx the parse tree
	 */
	void enterAexprConst(DDLStatementParser.AexprConstContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#aexprConst}.
	 * @param ctx the parse tree
	 */
	void exitAexprConst(DDLStatementParser.AexprConstContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedName(DDLStatementParser.QualifiedNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedName(DDLStatementParser.QualifiedNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#colId}.
	 * @param ctx the parse tree
	 */
	void enterColId(DDLStatementParser.ColIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#colId}.
	 * @param ctx the parse tree
	 */
	void exitColId(DDLStatementParser.ColIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#typeFunctionName}.
	 * @param ctx the parse tree
	 */
	void enterTypeFunctionName(DDLStatementParser.TypeFunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#typeFunctionName}.
	 * @param ctx the parse tree
	 */
	void exitTypeFunctionName(DDLStatementParser.TypeFunctionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#functionTable}.
	 * @param ctx the parse tree
	 */
	void enterFunctionTable(DDLStatementParser.FunctionTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#functionTable}.
	 * @param ctx the parse tree
	 */
	void exitFunctionTable(DDLStatementParser.FunctionTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#xmlTable}.
	 * @param ctx the parse tree
	 */
	void enterXmlTable(DDLStatementParser.XmlTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#xmlTable}.
	 * @param ctx the parse tree
	 */
	void exitXmlTable(DDLStatementParser.XmlTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#xmlTableColumnList}.
	 * @param ctx the parse tree
	 */
	void enterXmlTableColumnList(DDLStatementParser.XmlTableColumnListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#xmlTableColumnList}.
	 * @param ctx the parse tree
	 */
	void exitXmlTableColumnList(DDLStatementParser.XmlTableColumnListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#xmlTableColumnEl}.
	 * @param ctx the parse tree
	 */
	void enterXmlTableColumnEl(DDLStatementParser.XmlTableColumnElContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#xmlTableColumnEl}.
	 * @param ctx the parse tree
	 */
	void exitXmlTableColumnEl(DDLStatementParser.XmlTableColumnElContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#xmlTableColumnOptionList}.
	 * @param ctx the parse tree
	 */
	void enterXmlTableColumnOptionList(DDLStatementParser.XmlTableColumnOptionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#xmlTableColumnOptionList}.
	 * @param ctx the parse tree
	 */
	void exitXmlTableColumnOptionList(DDLStatementParser.XmlTableColumnOptionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#xmlTableColumnOptionEl}.
	 * @param ctx the parse tree
	 */
	void enterXmlTableColumnOptionEl(DDLStatementParser.XmlTableColumnOptionElContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#xmlTableColumnOptionEl}.
	 * @param ctx the parse tree
	 */
	void exitXmlTableColumnOptionEl(DDLStatementParser.XmlTableColumnOptionElContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#xmlNamespaceList}.
	 * @param ctx the parse tree
	 */
	void enterXmlNamespaceList(DDLStatementParser.XmlNamespaceListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#xmlNamespaceList}.
	 * @param ctx the parse tree
	 */
	void exitXmlNamespaceList(DDLStatementParser.XmlNamespaceListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#xmlNamespaceEl}.
	 * @param ctx the parse tree
	 */
	void enterXmlNamespaceEl(DDLStatementParser.XmlNamespaceElContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#xmlNamespaceEl}.
	 * @param ctx the parse tree
	 */
	void exitXmlNamespaceEl(DDLStatementParser.XmlNamespaceElContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#funcExpr}.
	 * @param ctx the parse tree
	 */
	void enterFuncExpr(DDLStatementParser.FuncExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#funcExpr}.
	 * @param ctx the parse tree
	 */
	void exitFuncExpr(DDLStatementParser.FuncExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#withinGroupClause}.
	 * @param ctx the parse tree
	 */
	void enterWithinGroupClause(DDLStatementParser.WithinGroupClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#withinGroupClause}.
	 * @param ctx the parse tree
	 */
	void exitWithinGroupClause(DDLStatementParser.WithinGroupClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#filterClause}.
	 * @param ctx the parse tree
	 */
	void enterFilterClause(DDLStatementParser.FilterClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#filterClause}.
	 * @param ctx the parse tree
	 */
	void exitFilterClause(DDLStatementParser.FilterClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#functionExprWindowless}.
	 * @param ctx the parse tree
	 */
	void enterFunctionExprWindowless(DDLStatementParser.FunctionExprWindowlessContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#functionExprWindowless}.
	 * @param ctx the parse tree
	 */
	void exitFunctionExprWindowless(DDLStatementParser.FunctionExprWindowlessContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#ordinality}.
	 * @param ctx the parse tree
	 */
	void enterOrdinality(DDLStatementParser.OrdinalityContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#ordinality}.
	 * @param ctx the parse tree
	 */
	void exitOrdinality(DDLStatementParser.OrdinalityContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#functionExprCommonSubexpr}.
	 * @param ctx the parse tree
	 */
	void enterFunctionExprCommonSubexpr(DDLStatementParser.FunctionExprCommonSubexprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#functionExprCommonSubexpr}.
	 * @param ctx the parse tree
	 */
	void exitFunctionExprCommonSubexpr(DDLStatementParser.FunctionExprCommonSubexprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#typeName}.
	 * @param ctx the parse tree
	 */
	void enterTypeName(DDLStatementParser.TypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#typeName}.
	 * @param ctx the parse tree
	 */
	void exitTypeName(DDLStatementParser.TypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#simpleTypeName}.
	 * @param ctx the parse tree
	 */
	void enterSimpleTypeName(DDLStatementParser.SimpleTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#simpleTypeName}.
	 * @param ctx the parse tree
	 */
	void exitSimpleTypeName(DDLStatementParser.SimpleTypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#exprList}.
	 * @param ctx the parse tree
	 */
	void enterExprList(DDLStatementParser.ExprListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#exprList}.
	 * @param ctx the parse tree
	 */
	void exitExprList(DDLStatementParser.ExprListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#extractList}.
	 * @param ctx the parse tree
	 */
	void enterExtractList(DDLStatementParser.ExtractListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#extractList}.
	 * @param ctx the parse tree
	 */
	void exitExtractList(DDLStatementParser.ExtractListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#extractArg}.
	 * @param ctx the parse tree
	 */
	void enterExtractArg(DDLStatementParser.ExtractArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#extractArg}.
	 * @param ctx the parse tree
	 */
	void exitExtractArg(DDLStatementParser.ExtractArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#genericType}.
	 * @param ctx the parse tree
	 */
	void enterGenericType(DDLStatementParser.GenericTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#genericType}.
	 * @param ctx the parse tree
	 */
	void exitGenericType(DDLStatementParser.GenericTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#typeModifiers}.
	 * @param ctx the parse tree
	 */
	void enterTypeModifiers(DDLStatementParser.TypeModifiersContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#typeModifiers}.
	 * @param ctx the parse tree
	 */
	void exitTypeModifiers(DDLStatementParser.TypeModifiersContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#numeric}.
	 * @param ctx the parse tree
	 */
	void enterNumeric(DDLStatementParser.NumericContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#numeric}.
	 * @param ctx the parse tree
	 */
	void exitNumeric(DDLStatementParser.NumericContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#constDatetime}.
	 * @param ctx the parse tree
	 */
	void enterConstDatetime(DDLStatementParser.ConstDatetimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#constDatetime}.
	 * @param ctx the parse tree
	 */
	void exitConstDatetime(DDLStatementParser.ConstDatetimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#timezone}.
	 * @param ctx the parse tree
	 */
	void enterTimezone(DDLStatementParser.TimezoneContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#timezone}.
	 * @param ctx the parse tree
	 */
	void exitTimezone(DDLStatementParser.TimezoneContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#character}.
	 * @param ctx the parse tree
	 */
	void enterCharacter(DDLStatementParser.CharacterContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#character}.
	 * @param ctx the parse tree
	 */
	void exitCharacter(DDLStatementParser.CharacterContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#characterWithLength}.
	 * @param ctx the parse tree
	 */
	void enterCharacterWithLength(DDLStatementParser.CharacterWithLengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#characterWithLength}.
	 * @param ctx the parse tree
	 */
	void exitCharacterWithLength(DDLStatementParser.CharacterWithLengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#characterWithoutLength}.
	 * @param ctx the parse tree
	 */
	void enterCharacterWithoutLength(DDLStatementParser.CharacterWithoutLengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#characterWithoutLength}.
	 * @param ctx the parse tree
	 */
	void exitCharacterWithoutLength(DDLStatementParser.CharacterWithoutLengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#characterClause}.
	 * @param ctx the parse tree
	 */
	void enterCharacterClause(DDLStatementParser.CharacterClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#characterClause}.
	 * @param ctx the parse tree
	 */
	void exitCharacterClause(DDLStatementParser.CharacterClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#optFloat}.
	 * @param ctx the parse tree
	 */
	void enterOptFloat(DDLStatementParser.OptFloatContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#optFloat}.
	 * @param ctx the parse tree
	 */
	void exitOptFloat(DDLStatementParser.OptFloatContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#attrs}.
	 * @param ctx the parse tree
	 */
	void enterAttrs(DDLStatementParser.AttrsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#attrs}.
	 * @param ctx the parse tree
	 */
	void exitAttrs(DDLStatementParser.AttrsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#attrName}.
	 * @param ctx the parse tree
	 */
	void enterAttrName(DDLStatementParser.AttrNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#attrName}.
	 * @param ctx the parse tree
	 */
	void exitAttrName(DDLStatementParser.AttrNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#colLable}.
	 * @param ctx the parse tree
	 */
	void enterColLable(DDLStatementParser.ColLableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#colLable}.
	 * @param ctx the parse tree
	 */
	void exitColLable(DDLStatementParser.ColLableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#bit}.
	 * @param ctx the parse tree
	 */
	void enterBit(DDLStatementParser.BitContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#bit}.
	 * @param ctx the parse tree
	 */
	void exitBit(DDLStatementParser.BitContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#bitWithLength}.
	 * @param ctx the parse tree
	 */
	void enterBitWithLength(DDLStatementParser.BitWithLengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#bitWithLength}.
	 * @param ctx the parse tree
	 */
	void exitBitWithLength(DDLStatementParser.BitWithLengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#bitWithoutLength}.
	 * @param ctx the parse tree
	 */
	void enterBitWithoutLength(DDLStatementParser.BitWithoutLengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#bitWithoutLength}.
	 * @param ctx the parse tree
	 */
	void exitBitWithoutLength(DDLStatementParser.BitWithoutLengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#constInterval}.
	 * @param ctx the parse tree
	 */
	void enterConstInterval(DDLStatementParser.ConstIntervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#constInterval}.
	 * @param ctx the parse tree
	 */
	void exitConstInterval(DDLStatementParser.ConstIntervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#optInterval}.
	 * @param ctx the parse tree
	 */
	void enterOptInterval(DDLStatementParser.OptIntervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#optInterval}.
	 * @param ctx the parse tree
	 */
	void exitOptInterval(DDLStatementParser.OptIntervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#optArrayBounds}.
	 * @param ctx the parse tree
	 */
	void enterOptArrayBounds(DDLStatementParser.OptArrayBoundsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#optArrayBounds}.
	 * @param ctx the parse tree
	 */
	void exitOptArrayBounds(DDLStatementParser.OptArrayBoundsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#intervalSecond}.
	 * @param ctx the parse tree
	 */
	void enterIntervalSecond(DDLStatementParser.IntervalSecondContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#intervalSecond}.
	 * @param ctx the parse tree
	 */
	void exitIntervalSecond(DDLStatementParser.IntervalSecondContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#unicodeNormalForm}.
	 * @param ctx the parse tree
	 */
	void enterUnicodeNormalForm(DDLStatementParser.UnicodeNormalFormContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#unicodeNormalForm}.
	 * @param ctx the parse tree
	 */
	void exitUnicodeNormalForm(DDLStatementParser.UnicodeNormalFormContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#trimList}.
	 * @param ctx the parse tree
	 */
	void enterTrimList(DDLStatementParser.TrimListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#trimList}.
	 * @param ctx the parse tree
	 */
	void exitTrimList(DDLStatementParser.TrimListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#overlayList}.
	 * @param ctx the parse tree
	 */
	void enterOverlayList(DDLStatementParser.OverlayListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#overlayList}.
	 * @param ctx the parse tree
	 */
	void exitOverlayList(DDLStatementParser.OverlayListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#overlayPlacing}.
	 * @param ctx the parse tree
	 */
	void enterOverlayPlacing(DDLStatementParser.OverlayPlacingContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#overlayPlacing}.
	 * @param ctx the parse tree
	 */
	void exitOverlayPlacing(DDLStatementParser.OverlayPlacingContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#substrFrom}.
	 * @param ctx the parse tree
	 */
	void enterSubstrFrom(DDLStatementParser.SubstrFromContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#substrFrom}.
	 * @param ctx the parse tree
	 */
	void exitSubstrFrom(DDLStatementParser.SubstrFromContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#substrFor}.
	 * @param ctx the parse tree
	 */
	void enterSubstrFor(DDLStatementParser.SubstrForContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#substrFor}.
	 * @param ctx the parse tree
	 */
	void exitSubstrFor(DDLStatementParser.SubstrForContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#positionList}.
	 * @param ctx the parse tree
	 */
	void enterPositionList(DDLStatementParser.PositionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#positionList}.
	 * @param ctx the parse tree
	 */
	void exitPositionList(DDLStatementParser.PositionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#substrList}.
	 * @param ctx the parse tree
	 */
	void enterSubstrList(DDLStatementParser.SubstrListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#substrList}.
	 * @param ctx the parse tree
	 */
	void exitSubstrList(DDLStatementParser.SubstrListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#xmlAttributes}.
	 * @param ctx the parse tree
	 */
	void enterXmlAttributes(DDLStatementParser.XmlAttributesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#xmlAttributes}.
	 * @param ctx the parse tree
	 */
	void exitXmlAttributes(DDLStatementParser.XmlAttributesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#xmlAttributeList}.
	 * @param ctx the parse tree
	 */
	void enterXmlAttributeList(DDLStatementParser.XmlAttributeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#xmlAttributeList}.
	 * @param ctx the parse tree
	 */
	void exitXmlAttributeList(DDLStatementParser.XmlAttributeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#xmlAttributeEl}.
	 * @param ctx the parse tree
	 */
	void enterXmlAttributeEl(DDLStatementParser.XmlAttributeElContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#xmlAttributeEl}.
	 * @param ctx the parse tree
	 */
	void exitXmlAttributeEl(DDLStatementParser.XmlAttributeElContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#xmlExistsArgument}.
	 * @param ctx the parse tree
	 */
	void enterXmlExistsArgument(DDLStatementParser.XmlExistsArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#xmlExistsArgument}.
	 * @param ctx the parse tree
	 */
	void exitXmlExistsArgument(DDLStatementParser.XmlExistsArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#xmlPassingMech}.
	 * @param ctx the parse tree
	 */
	void enterXmlPassingMech(DDLStatementParser.XmlPassingMechContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#xmlPassingMech}.
	 * @param ctx the parse tree
	 */
	void exitXmlPassingMech(DDLStatementParser.XmlPassingMechContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#documentOrContent}.
	 * @param ctx the parse tree
	 */
	void enterDocumentOrContent(DDLStatementParser.DocumentOrContentContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#documentOrContent}.
	 * @param ctx the parse tree
	 */
	void exitDocumentOrContent(DDLStatementParser.DocumentOrContentContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#xmlWhitespaceOption}.
	 * @param ctx the parse tree
	 */
	void enterXmlWhitespaceOption(DDLStatementParser.XmlWhitespaceOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#xmlWhitespaceOption}.
	 * @param ctx the parse tree
	 */
	void exitXmlWhitespaceOption(DDLStatementParser.XmlWhitespaceOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#xmlRootVersion}.
	 * @param ctx the parse tree
	 */
	void enterXmlRootVersion(DDLStatementParser.XmlRootVersionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#xmlRootVersion}.
	 * @param ctx the parse tree
	 */
	void exitXmlRootVersion(DDLStatementParser.XmlRootVersionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#xmlRootStandalone}.
	 * @param ctx the parse tree
	 */
	void enterXmlRootStandalone(DDLStatementParser.XmlRootStandaloneContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#xmlRootStandalone}.
	 * @param ctx the parse tree
	 */
	void exitXmlRootStandalone(DDLStatementParser.XmlRootStandaloneContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#rowsFromItem}.
	 * @param ctx the parse tree
	 */
	void enterRowsFromItem(DDLStatementParser.RowsFromItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#rowsFromItem}.
	 * @param ctx the parse tree
	 */
	void exitRowsFromItem(DDLStatementParser.RowsFromItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#rowsFromList}.
	 * @param ctx the parse tree
	 */
	void enterRowsFromList(DDLStatementParser.RowsFromListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#rowsFromList}.
	 * @param ctx the parse tree
	 */
	void exitRowsFromList(DDLStatementParser.RowsFromListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#columnDefList}.
	 * @param ctx the parse tree
	 */
	void enterColumnDefList(DDLStatementParser.ColumnDefListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#columnDefList}.
	 * @param ctx the parse tree
	 */
	void exitColumnDefList(DDLStatementParser.ColumnDefListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#tableFuncElementList}.
	 * @param ctx the parse tree
	 */
	void enterTableFuncElementList(DDLStatementParser.TableFuncElementListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#tableFuncElementList}.
	 * @param ctx the parse tree
	 */
	void exitTableFuncElementList(DDLStatementParser.TableFuncElementListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#tableFuncElement}.
	 * @param ctx the parse tree
	 */
	void enterTableFuncElement(DDLStatementParser.TableFuncElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#tableFuncElement}.
	 * @param ctx the parse tree
	 */
	void exitTableFuncElement(DDLStatementParser.TableFuncElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#collateClause}.
	 * @param ctx the parse tree
	 */
	void enterCollateClause(DDLStatementParser.CollateClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#collateClause}.
	 * @param ctx the parse tree
	 */
	void exitCollateClause(DDLStatementParser.CollateClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#anyName}.
	 * @param ctx the parse tree
	 */
	void enterAnyName(DDLStatementParser.AnyNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#anyName}.
	 * @param ctx the parse tree
	 */
	void exitAnyName(DDLStatementParser.AnyNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#aliasClause}.
	 * @param ctx the parse tree
	 */
	void enterAliasClause(DDLStatementParser.AliasClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#aliasClause}.
	 * @param ctx the parse tree
	 */
	void exitAliasClause(DDLStatementParser.AliasClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#nameList}.
	 * @param ctx the parse tree
	 */
	void enterNameList(DDLStatementParser.NameListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#nameList}.
	 * @param ctx the parse tree
	 */
	void exitNameList(DDLStatementParser.NameListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#funcAliasClause}.
	 * @param ctx the parse tree
	 */
	void enterFuncAliasClause(DDLStatementParser.FuncAliasClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#funcAliasClause}.
	 * @param ctx the parse tree
	 */
	void exitFuncAliasClause(DDLStatementParser.FuncAliasClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#tablesampleClause}.
	 * @param ctx the parse tree
	 */
	void enterTablesampleClause(DDLStatementParser.TablesampleClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#tablesampleClause}.
	 * @param ctx the parse tree
	 */
	void exitTablesampleClause(DDLStatementParser.TablesampleClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#repeatableClause}.
	 * @param ctx the parse tree
	 */
	void enterRepeatableClause(DDLStatementParser.RepeatableClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#repeatableClause}.
	 * @param ctx the parse tree
	 */
	void exitRepeatableClause(DDLStatementParser.RepeatableClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#allOrDistinct}.
	 * @param ctx the parse tree
	 */
	void enterAllOrDistinct(DDLStatementParser.AllOrDistinctContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#allOrDistinct}.
	 * @param ctx the parse tree
	 */
	void exitAllOrDistinct(DDLStatementParser.AllOrDistinctContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#sortClause}.
	 * @param ctx the parse tree
	 */
	void enterSortClause(DDLStatementParser.SortClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#sortClause}.
	 * @param ctx the parse tree
	 */
	void exitSortClause(DDLStatementParser.SortClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#sortbyList}.
	 * @param ctx the parse tree
	 */
	void enterSortbyList(DDLStatementParser.SortbyListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#sortbyList}.
	 * @param ctx the parse tree
	 */
	void exitSortbyList(DDLStatementParser.SortbyListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#sortby}.
	 * @param ctx the parse tree
	 */
	void enterSortby(DDLStatementParser.SortbyContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#sortby}.
	 * @param ctx the parse tree
	 */
	void exitSortby(DDLStatementParser.SortbyContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#nullsOrder}.
	 * @param ctx the parse tree
	 */
	void enterNullsOrder(DDLStatementParser.NullsOrderContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#nullsOrder}.
	 * @param ctx the parse tree
	 */
	void exitNullsOrder(DDLStatementParser.NullsOrderContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#distinctClause}.
	 * @param ctx the parse tree
	 */
	void enterDistinctClause(DDLStatementParser.DistinctClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#distinctClause}.
	 * @param ctx the parse tree
	 */
	void exitDistinctClause(DDLStatementParser.DistinctClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#distinct}.
	 * @param ctx the parse tree
	 */
	void enterDistinct(DDLStatementParser.DistinctContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#distinct}.
	 * @param ctx the parse tree
	 */
	void exitDistinct(DDLStatementParser.DistinctContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#overClause}.
	 * @param ctx the parse tree
	 */
	void enterOverClause(DDLStatementParser.OverClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#overClause}.
	 * @param ctx the parse tree
	 */
	void exitOverClause(DDLStatementParser.OverClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#windowSpecification}.
	 * @param ctx the parse tree
	 */
	void enterWindowSpecification(DDLStatementParser.WindowSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#windowSpecification}.
	 * @param ctx the parse tree
	 */
	void exitWindowSpecification(DDLStatementParser.WindowSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#windowName}.
	 * @param ctx the parse tree
	 */
	void enterWindowName(DDLStatementParser.WindowNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#windowName}.
	 * @param ctx the parse tree
	 */
	void exitWindowName(DDLStatementParser.WindowNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#partitionClause}.
	 * @param ctx the parse tree
	 */
	void enterPartitionClause(DDLStatementParser.PartitionClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#partitionClause}.
	 * @param ctx the parse tree
	 */
	void exitPartitionClause(DDLStatementParser.PartitionClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#indexParams}.
	 * @param ctx the parse tree
	 */
	void enterIndexParams(DDLStatementParser.IndexParamsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#indexParams}.
	 * @param ctx the parse tree
	 */
	void exitIndexParams(DDLStatementParser.IndexParamsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#indexElemOptions}.
	 * @param ctx the parse tree
	 */
	void enterIndexElemOptions(DDLStatementParser.IndexElemOptionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#indexElemOptions}.
	 * @param ctx the parse tree
	 */
	void exitIndexElemOptions(DDLStatementParser.IndexElemOptionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#indexElem}.
	 * @param ctx the parse tree
	 */
	void enterIndexElem(DDLStatementParser.IndexElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#indexElem}.
	 * @param ctx the parse tree
	 */
	void exitIndexElem(DDLStatementParser.IndexElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#collate}.
	 * @param ctx the parse tree
	 */
	void enterCollate(DDLStatementParser.CollateContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#collate}.
	 * @param ctx the parse tree
	 */
	void exitCollate(DDLStatementParser.CollateContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#optClass}.
	 * @param ctx the parse tree
	 */
	void enterOptClass(DDLStatementParser.OptClassContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#optClass}.
	 * @param ctx the parse tree
	 */
	void exitOptClass(DDLStatementParser.OptClassContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#reloptions}.
	 * @param ctx the parse tree
	 */
	void enterReloptions(DDLStatementParser.ReloptionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#reloptions}.
	 * @param ctx the parse tree
	 */
	void exitReloptions(DDLStatementParser.ReloptionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#reloptionList}.
	 * @param ctx the parse tree
	 */
	void enterReloptionList(DDLStatementParser.ReloptionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#reloptionList}.
	 * @param ctx the parse tree
	 */
	void exitReloptionList(DDLStatementParser.ReloptionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#reloptionElem}.
	 * @param ctx the parse tree
	 */
	void enterReloptionElem(DDLStatementParser.ReloptionElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#reloptionElem}.
	 * @param ctx the parse tree
	 */
	void exitReloptionElem(DDLStatementParser.ReloptionElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#defArg}.
	 * @param ctx the parse tree
	 */
	void enterDefArg(DDLStatementParser.DefArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#defArg}.
	 * @param ctx the parse tree
	 */
	void exitDefArg(DDLStatementParser.DefArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#funcType}.
	 * @param ctx the parse tree
	 */
	void enterFuncType(DDLStatementParser.FuncTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#funcType}.
	 * @param ctx the parse tree
	 */
	void exitFuncType(DDLStatementParser.FuncTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#selectWithParens}.
	 * @param ctx the parse tree
	 */
	void enterSelectWithParens(DDLStatementParser.SelectWithParensContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#selectWithParens}.
	 * @param ctx the parse tree
	 */
	void exitSelectWithParens(DDLStatementParser.SelectWithParensContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterDataType(DDLStatementParser.DataTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitDataType(DDLStatementParser.DataTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dataTypeName}.
	 * @param ctx the parse tree
	 */
	void enterDataTypeName(DDLStatementParser.DataTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dataTypeName}.
	 * @param ctx the parse tree
	 */
	void exitDataTypeName(DDLStatementParser.DataTypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dataTypeLength}.
	 * @param ctx the parse tree
	 */
	void enterDataTypeLength(DDLStatementParser.DataTypeLengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dataTypeLength}.
	 * @param ctx the parse tree
	 */
	void exitDataTypeLength(DDLStatementParser.DataTypeLengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#characterSet}.
	 * @param ctx the parse tree
	 */
	void enterCharacterSet(DDLStatementParser.CharacterSetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#characterSet}.
	 * @param ctx the parse tree
	 */
	void exitCharacterSet(DDLStatementParser.CharacterSetContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#ignoredIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterIgnoredIdentifier(DDLStatementParser.IgnoredIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#ignoredIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitIgnoredIdentifier(DDLStatementParser.IgnoredIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#ignoredIdentifiers}.
	 * @param ctx the parse tree
	 */
	void enterIgnoredIdentifiers(DDLStatementParser.IgnoredIdentifiersContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#ignoredIdentifiers}.
	 * @param ctx the parse tree
	 */
	void exitIgnoredIdentifiers(DDLStatementParser.IgnoredIdentifiersContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#signedIconst}.
	 * @param ctx the parse tree
	 */
	void enterSignedIconst(DDLStatementParser.SignedIconstContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#signedIconst}.
	 * @param ctx the parse tree
	 */
	void exitSignedIconst(DDLStatementParser.SignedIconstContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#booleanOrString}.
	 * @param ctx the parse tree
	 */
	void enterBooleanOrString(DDLStatementParser.BooleanOrStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#booleanOrString}.
	 * @param ctx the parse tree
	 */
	void exitBooleanOrString(DDLStatementParser.BooleanOrStringContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#nonReservedWord}.
	 * @param ctx the parse tree
	 */
	void enterNonReservedWord(DDLStatementParser.NonReservedWordContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#nonReservedWord}.
	 * @param ctx the parse tree
	 */
	void exitNonReservedWord(DDLStatementParser.NonReservedWordContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#colNameKeyword}.
	 * @param ctx the parse tree
	 */
	void enterColNameKeyword(DDLStatementParser.ColNameKeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#colNameKeyword}.
	 * @param ctx the parse tree
	 */
	void exitColNameKeyword(DDLStatementParser.ColNameKeywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#databaseName}.
	 * @param ctx the parse tree
	 */
	void enterDatabaseName(DDLStatementParser.DatabaseNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#databaseName}.
	 * @param ctx the parse tree
	 */
	void exitDatabaseName(DDLStatementParser.DatabaseNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#roleSpec}.
	 * @param ctx the parse tree
	 */
	void enterRoleSpec(DDLStatementParser.RoleSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#roleSpec}.
	 * @param ctx the parse tree
	 */
	void exitRoleSpec(DDLStatementParser.RoleSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#varName}.
	 * @param ctx the parse tree
	 */
	void enterVarName(DDLStatementParser.VarNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#varName}.
	 * @param ctx the parse tree
	 */
	void exitVarName(DDLStatementParser.VarNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#varList}.
	 * @param ctx the parse tree
	 */
	void enterVarList(DDLStatementParser.VarListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#varList}.
	 * @param ctx the parse tree
	 */
	void exitVarList(DDLStatementParser.VarListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#varValue}.
	 * @param ctx the parse tree
	 */
	void enterVarValue(DDLStatementParser.VarValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#varValue}.
	 * @param ctx the parse tree
	 */
	void exitVarValue(DDLStatementParser.VarValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#zoneValue}.
	 * @param ctx the parse tree
	 */
	void enterZoneValue(DDLStatementParser.ZoneValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#zoneValue}.
	 * @param ctx the parse tree
	 */
	void exitZoneValue(DDLStatementParser.ZoneValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#numericOnly}.
	 * @param ctx the parse tree
	 */
	void enterNumericOnly(DDLStatementParser.NumericOnlyContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#numericOnly}.
	 * @param ctx the parse tree
	 */
	void exitNumericOnly(DDLStatementParser.NumericOnlyContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#isoLevel}.
	 * @param ctx the parse tree
	 */
	void enterIsoLevel(DDLStatementParser.IsoLevelContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#isoLevel}.
	 * @param ctx the parse tree
	 */
	void exitIsoLevel(DDLStatementParser.IsoLevelContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#columnDef}.
	 * @param ctx the parse tree
	 */
	void enterColumnDef(DDLStatementParser.ColumnDefContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#columnDef}.
	 * @param ctx the parse tree
	 */
	void exitColumnDef(DDLStatementParser.ColumnDefContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#colConstraint}.
	 * @param ctx the parse tree
	 */
	void enterColConstraint(DDLStatementParser.ColConstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#colConstraint}.
	 * @param ctx the parse tree
	 */
	void exitColConstraint(DDLStatementParser.ColConstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#constraintAttr}.
	 * @param ctx the parse tree
	 */
	void enterConstraintAttr(DDLStatementParser.ConstraintAttrContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#constraintAttr}.
	 * @param ctx the parse tree
	 */
	void exitConstraintAttr(DDLStatementParser.ConstraintAttrContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#colConstraintElem}.
	 * @param ctx the parse tree
	 */
	void enterColConstraintElem(DDLStatementParser.ColConstraintElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#colConstraintElem}.
	 * @param ctx the parse tree
	 */
	void exitColConstraintElem(DDLStatementParser.ColConstraintElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#parenthesizedSeqOptList}.
	 * @param ctx the parse tree
	 */
	void enterParenthesizedSeqOptList(DDLStatementParser.ParenthesizedSeqOptListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#parenthesizedSeqOptList}.
	 * @param ctx the parse tree
	 */
	void exitParenthesizedSeqOptList(DDLStatementParser.ParenthesizedSeqOptListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#seqOptList}.
	 * @param ctx the parse tree
	 */
	void enterSeqOptList(DDLStatementParser.SeqOptListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#seqOptList}.
	 * @param ctx the parse tree
	 */
	void exitSeqOptList(DDLStatementParser.SeqOptListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#seqOptElem}.
	 * @param ctx the parse tree
	 */
	void enterSeqOptElem(DDLStatementParser.SeqOptElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#seqOptElem}.
	 * @param ctx the parse tree
	 */
	void exitSeqOptElem(DDLStatementParser.SeqOptElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#optColumnList}.
	 * @param ctx the parse tree
	 */
	void enterOptColumnList(DDLStatementParser.OptColumnListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#optColumnList}.
	 * @param ctx the parse tree
	 */
	void exitOptColumnList(DDLStatementParser.OptColumnListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#generatedWhen}.
	 * @param ctx the parse tree
	 */
	void enterGeneratedWhen(DDLStatementParser.GeneratedWhenContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#generatedWhen}.
	 * @param ctx the parse tree
	 */
	void exitGeneratedWhen(DDLStatementParser.GeneratedWhenContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#noInherit}.
	 * @param ctx the parse tree
	 */
	void enterNoInherit(DDLStatementParser.NoInheritContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#noInherit}.
	 * @param ctx the parse tree
	 */
	void exitNoInherit(DDLStatementParser.NoInheritContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#consTableSpace}.
	 * @param ctx the parse tree
	 */
	void enterConsTableSpace(DDLStatementParser.ConsTableSpaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#consTableSpace}.
	 * @param ctx the parse tree
	 */
	void exitConsTableSpace(DDLStatementParser.ConsTableSpaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#definition}.
	 * @param ctx the parse tree
	 */
	void enterDefinition(DDLStatementParser.DefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#definition}.
	 * @param ctx the parse tree
	 */
	void exitDefinition(DDLStatementParser.DefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#defList}.
	 * @param ctx the parse tree
	 */
	void enterDefList(DDLStatementParser.DefListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#defList}.
	 * @param ctx the parse tree
	 */
	void exitDefList(DDLStatementParser.DefListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#defElem}.
	 * @param ctx the parse tree
	 */
	void enterDefElem(DDLStatementParser.DefElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#defElem}.
	 * @param ctx the parse tree
	 */
	void exitDefElem(DDLStatementParser.DefElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#colLabel}.
	 * @param ctx the parse tree
	 */
	void enterColLabel(DDLStatementParser.ColLabelContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#colLabel}.
	 * @param ctx the parse tree
	 */
	void exitColLabel(DDLStatementParser.ColLabelContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#keyActions}.
	 * @param ctx the parse tree
	 */
	void enterKeyActions(DDLStatementParser.KeyActionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#keyActions}.
	 * @param ctx the parse tree
	 */
	void exitKeyActions(DDLStatementParser.KeyActionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#keyDelete}.
	 * @param ctx the parse tree
	 */
	void enterKeyDelete(DDLStatementParser.KeyDeleteContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#keyDelete}.
	 * @param ctx the parse tree
	 */
	void exitKeyDelete(DDLStatementParser.KeyDeleteContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#keyUpdate}.
	 * @param ctx the parse tree
	 */
	void enterKeyUpdate(DDLStatementParser.KeyUpdateContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#keyUpdate}.
	 * @param ctx the parse tree
	 */
	void exitKeyUpdate(DDLStatementParser.KeyUpdateContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#keyAction}.
	 * @param ctx the parse tree
	 */
	void enterKeyAction(DDLStatementParser.KeyActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#keyAction}.
	 * @param ctx the parse tree
	 */
	void exitKeyAction(DDLStatementParser.KeyActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#keyMatch}.
	 * @param ctx the parse tree
	 */
	void enterKeyMatch(DDLStatementParser.KeyMatchContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#keyMatch}.
	 * @param ctx the parse tree
	 */
	void exitKeyMatch(DDLStatementParser.KeyMatchContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#createGenericOptions}.
	 * @param ctx the parse tree
	 */
	void enterCreateGenericOptions(DDLStatementParser.CreateGenericOptionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#createGenericOptions}.
	 * @param ctx the parse tree
	 */
	void exitCreateGenericOptions(DDLStatementParser.CreateGenericOptionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#genericOptionList}.
	 * @param ctx the parse tree
	 */
	void enterGenericOptionList(DDLStatementParser.GenericOptionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#genericOptionList}.
	 * @param ctx the parse tree
	 */
	void exitGenericOptionList(DDLStatementParser.GenericOptionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#replicaIdentity}.
	 * @param ctx the parse tree
	 */
	void enterReplicaIdentity(DDLStatementParser.ReplicaIdentityContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#replicaIdentity}.
	 * @param ctx the parse tree
	 */
	void exitReplicaIdentity(DDLStatementParser.ReplicaIdentityContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#operArgtypes}.
	 * @param ctx the parse tree
	 */
	void enterOperArgtypes(DDLStatementParser.OperArgtypesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#operArgtypes}.
	 * @param ctx the parse tree
	 */
	void exitOperArgtypes(DDLStatementParser.OperArgtypesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#funcArg}.
	 * @param ctx the parse tree
	 */
	void enterFuncArg(DDLStatementParser.FuncArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#funcArg}.
	 * @param ctx the parse tree
	 */
	void exitFuncArg(DDLStatementParser.FuncArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#argClass}.
	 * @param ctx the parse tree
	 */
	void enterArgClass(DDLStatementParser.ArgClassContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#argClass}.
	 * @param ctx the parse tree
	 */
	void exitArgClass(DDLStatementParser.ArgClassContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#funcArgsList}.
	 * @param ctx the parse tree
	 */
	void enterFuncArgsList(DDLStatementParser.FuncArgsListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#funcArgsList}.
	 * @param ctx the parse tree
	 */
	void exitFuncArgsList(DDLStatementParser.FuncArgsListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#nonReservedWordOrSconst}.
	 * @param ctx the parse tree
	 */
	void enterNonReservedWordOrSconst(DDLStatementParser.NonReservedWordOrSconstContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#nonReservedWordOrSconst}.
	 * @param ctx the parse tree
	 */
	void exitNonReservedWordOrSconst(DDLStatementParser.NonReservedWordOrSconstContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#fileName}.
	 * @param ctx the parse tree
	 */
	void enterFileName(DDLStatementParser.FileNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#fileName}.
	 * @param ctx the parse tree
	 */
	void exitFileName(DDLStatementParser.FileNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#roleList}.
	 * @param ctx the parse tree
	 */
	void enterRoleList(DDLStatementParser.RoleListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#roleList}.
	 * @param ctx the parse tree
	 */
	void exitRoleList(DDLStatementParser.RoleListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#setResetClause}.
	 * @param ctx the parse tree
	 */
	void enterSetResetClause(DDLStatementParser.SetResetClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#setResetClause}.
	 * @param ctx the parse tree
	 */
	void exitSetResetClause(DDLStatementParser.SetResetClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#setRest}.
	 * @param ctx the parse tree
	 */
	void enterSetRest(DDLStatementParser.SetRestContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#setRest}.
	 * @param ctx the parse tree
	 */
	void exitSetRest(DDLStatementParser.SetRestContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#transactionModeList}.
	 * @param ctx the parse tree
	 */
	void enterTransactionModeList(DDLStatementParser.TransactionModeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#transactionModeList}.
	 * @param ctx the parse tree
	 */
	void exitTransactionModeList(DDLStatementParser.TransactionModeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#transactionModeItem}.
	 * @param ctx the parse tree
	 */
	void enterTransactionModeItem(DDLStatementParser.TransactionModeItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#transactionModeItem}.
	 * @param ctx the parse tree
	 */
	void exitTransactionModeItem(DDLStatementParser.TransactionModeItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#setRestMore}.
	 * @param ctx the parse tree
	 */
	void enterSetRestMore(DDLStatementParser.SetRestMoreContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#setRestMore}.
	 * @param ctx the parse tree
	 */
	void exitSetRestMore(DDLStatementParser.SetRestMoreContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#encoding}.
	 * @param ctx the parse tree
	 */
	void enterEncoding(DDLStatementParser.EncodingContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#encoding}.
	 * @param ctx the parse tree
	 */
	void exitEncoding(DDLStatementParser.EncodingContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#genericSet}.
	 * @param ctx the parse tree
	 */
	void enterGenericSet(DDLStatementParser.GenericSetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#genericSet}.
	 * @param ctx the parse tree
	 */
	void exitGenericSet(DDLStatementParser.GenericSetContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#variableResetStmt}.
	 * @param ctx the parse tree
	 */
	void enterVariableResetStmt(DDLStatementParser.VariableResetStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#variableResetStmt}.
	 * @param ctx the parse tree
	 */
	void exitVariableResetStmt(DDLStatementParser.VariableResetStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#resetRest}.
	 * @param ctx the parse tree
	 */
	void enterResetRest(DDLStatementParser.ResetRestContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#resetRest}.
	 * @param ctx the parse tree
	 */
	void exitResetRest(DDLStatementParser.ResetRestContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#genericReset}.
	 * @param ctx the parse tree
	 */
	void enterGenericReset(DDLStatementParser.GenericResetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#genericReset}.
	 * @param ctx the parse tree
	 */
	void exitGenericReset(DDLStatementParser.GenericResetContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#relationExprList}.
	 * @param ctx the parse tree
	 */
	void enterRelationExprList(DDLStatementParser.RelationExprListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#relationExprList}.
	 * @param ctx the parse tree
	 */
	void exitRelationExprList(DDLStatementParser.RelationExprListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#relationExpr}.
	 * @param ctx the parse tree
	 */
	void enterRelationExpr(DDLStatementParser.RelationExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#relationExpr}.
	 * @param ctx the parse tree
	 */
	void exitRelationExpr(DDLStatementParser.RelationExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#commonFuncOptItem}.
	 * @param ctx the parse tree
	 */
	void enterCommonFuncOptItem(DDLStatementParser.CommonFuncOptItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#commonFuncOptItem}.
	 * @param ctx the parse tree
	 */
	void exitCommonFuncOptItem(DDLStatementParser.CommonFuncOptItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#functionSetResetClause}.
	 * @param ctx the parse tree
	 */
	void enterFunctionSetResetClause(DDLStatementParser.FunctionSetResetClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#functionSetResetClause}.
	 * @param ctx the parse tree
	 */
	void exitFunctionSetResetClause(DDLStatementParser.FunctionSetResetClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#rowSecurityCmd}.
	 * @param ctx the parse tree
	 */
	void enterRowSecurityCmd(DDLStatementParser.RowSecurityCmdContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#rowSecurityCmd}.
	 * @param ctx the parse tree
	 */
	void exitRowSecurityCmd(DDLStatementParser.RowSecurityCmdContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#event}.
	 * @param ctx the parse tree
	 */
	void enterEvent(DDLStatementParser.EventContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#event}.
	 * @param ctx the parse tree
	 */
	void exitEvent(DDLStatementParser.EventContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#typeNameList}.
	 * @param ctx the parse tree
	 */
	void enterTypeNameList(DDLStatementParser.TypeNameListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#typeNameList}.
	 * @param ctx the parse tree
	 */
	void exitTypeNameList(DDLStatementParser.TypeNameListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#notExistClause}.
	 * @param ctx the parse tree
	 */
	void enterNotExistClause(DDLStatementParser.NotExistClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#notExistClause}.
	 * @param ctx the parse tree
	 */
	void exitNotExistClause(DDLStatementParser.NotExistClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#existClause}.
	 * @param ctx the parse tree
	 */
	void enterExistClause(DDLStatementParser.ExistClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#existClause}.
	 * @param ctx the parse tree
	 */
	void exitExistClause(DDLStatementParser.ExistClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#insert}.
	 * @param ctx the parse tree
	 */
	void enterInsert(DDLStatementParser.InsertContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#insert}.
	 * @param ctx the parse tree
	 */
	void exitInsert(DDLStatementParser.InsertContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#insertTarget}.
	 * @param ctx the parse tree
	 */
	void enterInsertTarget(DDLStatementParser.InsertTargetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#insertTarget}.
	 * @param ctx the parse tree
	 */
	void exitInsertTarget(DDLStatementParser.InsertTargetContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#insertRest}.
	 * @param ctx the parse tree
	 */
	void enterInsertRest(DDLStatementParser.InsertRestContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#insertRest}.
	 * @param ctx the parse tree
	 */
	void exitInsertRest(DDLStatementParser.InsertRestContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#overrideKind}.
	 * @param ctx the parse tree
	 */
	void enterOverrideKind(DDLStatementParser.OverrideKindContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#overrideKind}.
	 * @param ctx the parse tree
	 */
	void exitOverrideKind(DDLStatementParser.OverrideKindContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#insertColumnList}.
	 * @param ctx the parse tree
	 */
	void enterInsertColumnList(DDLStatementParser.InsertColumnListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#insertColumnList}.
	 * @param ctx the parse tree
	 */
	void exitInsertColumnList(DDLStatementParser.InsertColumnListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#insertColumnItem}.
	 * @param ctx the parse tree
	 */
	void enterInsertColumnItem(DDLStatementParser.InsertColumnItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#insertColumnItem}.
	 * @param ctx the parse tree
	 */
	void exitInsertColumnItem(DDLStatementParser.InsertColumnItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#optOnConflict}.
	 * @param ctx the parse tree
	 */
	void enterOptOnConflict(DDLStatementParser.OptOnConflictContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#optOnConflict}.
	 * @param ctx the parse tree
	 */
	void exitOptOnConflict(DDLStatementParser.OptOnConflictContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#optConfExpr}.
	 * @param ctx the parse tree
	 */
	void enterOptConfExpr(DDLStatementParser.OptConfExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#optConfExpr}.
	 * @param ctx the parse tree
	 */
	void exitOptConfExpr(DDLStatementParser.OptConfExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#update}.
	 * @param ctx the parse tree
	 */
	void enterUpdate(DDLStatementParser.UpdateContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#update}.
	 * @param ctx the parse tree
	 */
	void exitUpdate(DDLStatementParser.UpdateContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#setClauseList}.
	 * @param ctx the parse tree
	 */
	void enterSetClauseList(DDLStatementParser.SetClauseListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#setClauseList}.
	 * @param ctx the parse tree
	 */
	void exitSetClauseList(DDLStatementParser.SetClauseListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#setClause}.
	 * @param ctx the parse tree
	 */
	void enterSetClause(DDLStatementParser.SetClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#setClause}.
	 * @param ctx the parse tree
	 */
	void exitSetClause(DDLStatementParser.SetClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#setTarget}.
	 * @param ctx the parse tree
	 */
	void enterSetTarget(DDLStatementParser.SetTargetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#setTarget}.
	 * @param ctx the parse tree
	 */
	void exitSetTarget(DDLStatementParser.SetTargetContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#setTargetList}.
	 * @param ctx the parse tree
	 */
	void enterSetTargetList(DDLStatementParser.SetTargetListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#setTargetList}.
	 * @param ctx the parse tree
	 */
	void exitSetTargetList(DDLStatementParser.SetTargetListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#returningClause}.
	 * @param ctx the parse tree
	 */
	void enterReturningClause(DDLStatementParser.ReturningClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#returningClause}.
	 * @param ctx the parse tree
	 */
	void exitReturningClause(DDLStatementParser.ReturningClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#delete}.
	 * @param ctx the parse tree
	 */
	void enterDelete(DDLStatementParser.DeleteContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#delete}.
	 * @param ctx the parse tree
	 */
	void exitDelete(DDLStatementParser.DeleteContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#relationExprOptAlias}.
	 * @param ctx the parse tree
	 */
	void enterRelationExprOptAlias(DDLStatementParser.RelationExprOptAliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#relationExprOptAlias}.
	 * @param ctx the parse tree
	 */
	void exitRelationExprOptAlias(DDLStatementParser.RelationExprOptAliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#usingClause}.
	 * @param ctx the parse tree
	 */
	void enterUsingClause(DDLStatementParser.UsingClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#usingClause}.
	 * @param ctx the parse tree
	 */
	void exitUsingClause(DDLStatementParser.UsingClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#select}.
	 * @param ctx the parse tree
	 */
	void enterSelect(DDLStatementParser.SelectContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#select}.
	 * @param ctx the parse tree
	 */
	void exitSelect(DDLStatementParser.SelectContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#selectNoParens}.
	 * @param ctx the parse tree
	 */
	void enterSelectNoParens(DDLStatementParser.SelectNoParensContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#selectNoParens}.
	 * @param ctx the parse tree
	 */
	void exitSelectNoParens(DDLStatementParser.SelectNoParensContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#selectClauseN}.
	 * @param ctx the parse tree
	 */
	void enterSelectClauseN(DDLStatementParser.SelectClauseNContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#selectClauseN}.
	 * @param ctx the parse tree
	 */
	void exitSelectClauseN(DDLStatementParser.SelectClauseNContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#simpleSelect}.
	 * @param ctx the parse tree
	 */
	void enterSimpleSelect(DDLStatementParser.SimpleSelectContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#simpleSelect}.
	 * @param ctx the parse tree
	 */
	void exitSimpleSelect(DDLStatementParser.SimpleSelectContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#withClause}.
	 * @param ctx the parse tree
	 */
	void enterWithClause(DDLStatementParser.WithClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#withClause}.
	 * @param ctx the parse tree
	 */
	void exitWithClause(DDLStatementParser.WithClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#intoClause}.
	 * @param ctx the parse tree
	 */
	void enterIntoClause(DDLStatementParser.IntoClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#intoClause}.
	 * @param ctx the parse tree
	 */
	void exitIntoClause(DDLStatementParser.IntoClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#optTempTableName}.
	 * @param ctx the parse tree
	 */
	void enterOptTempTableName(DDLStatementParser.OptTempTableNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#optTempTableName}.
	 * @param ctx the parse tree
	 */
	void exitOptTempTableName(DDLStatementParser.OptTempTableNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#cteList}.
	 * @param ctx the parse tree
	 */
	void enterCteList(DDLStatementParser.CteListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#cteList}.
	 * @param ctx the parse tree
	 */
	void exitCteList(DDLStatementParser.CteListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#commonTableExpr}.
	 * @param ctx the parse tree
	 */
	void enterCommonTableExpr(DDLStatementParser.CommonTableExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#commonTableExpr}.
	 * @param ctx the parse tree
	 */
	void exitCommonTableExpr(DDLStatementParser.CommonTableExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#optMaterialized}.
	 * @param ctx the parse tree
	 */
	void enterOptMaterialized(DDLStatementParser.OptMaterializedContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#optMaterialized}.
	 * @param ctx the parse tree
	 */
	void exitOptMaterialized(DDLStatementParser.OptMaterializedContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#optNameList}.
	 * @param ctx the parse tree
	 */
	void enterOptNameList(DDLStatementParser.OptNameListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#optNameList}.
	 * @param ctx the parse tree
	 */
	void exitOptNameList(DDLStatementParser.OptNameListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#preparableStmt}.
	 * @param ctx the parse tree
	 */
	void enterPreparableStmt(DDLStatementParser.PreparableStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#preparableStmt}.
	 * @param ctx the parse tree
	 */
	void exitPreparableStmt(DDLStatementParser.PreparableStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#forLockingClause}.
	 * @param ctx the parse tree
	 */
	void enterForLockingClause(DDLStatementParser.ForLockingClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#forLockingClause}.
	 * @param ctx the parse tree
	 */
	void exitForLockingClause(DDLStatementParser.ForLockingClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#forLockingItems}.
	 * @param ctx the parse tree
	 */
	void enterForLockingItems(DDLStatementParser.ForLockingItemsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#forLockingItems}.
	 * @param ctx the parse tree
	 */
	void exitForLockingItems(DDLStatementParser.ForLockingItemsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#forLockingItem}.
	 * @param ctx the parse tree
	 */
	void enterForLockingItem(DDLStatementParser.ForLockingItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#forLockingItem}.
	 * @param ctx the parse tree
	 */
	void exitForLockingItem(DDLStatementParser.ForLockingItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#nowaitOrSkip}.
	 * @param ctx the parse tree
	 */
	void enterNowaitOrSkip(DDLStatementParser.NowaitOrSkipContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#nowaitOrSkip}.
	 * @param ctx the parse tree
	 */
	void exitNowaitOrSkip(DDLStatementParser.NowaitOrSkipContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#forLockingStrength}.
	 * @param ctx the parse tree
	 */
	void enterForLockingStrength(DDLStatementParser.ForLockingStrengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#forLockingStrength}.
	 * @param ctx the parse tree
	 */
	void exitForLockingStrength(DDLStatementParser.ForLockingStrengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#lockedRelsList}.
	 * @param ctx the parse tree
	 */
	void enterLockedRelsList(DDLStatementParser.LockedRelsListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#lockedRelsList}.
	 * @param ctx the parse tree
	 */
	void exitLockedRelsList(DDLStatementParser.LockedRelsListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#qualifiedNameList}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedNameList(DDLStatementParser.QualifiedNameListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#qualifiedNameList}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedNameList(DDLStatementParser.QualifiedNameListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#selectLimit}.
	 * @param ctx the parse tree
	 */
	void enterSelectLimit(DDLStatementParser.SelectLimitContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#selectLimit}.
	 * @param ctx the parse tree
	 */
	void exitSelectLimit(DDLStatementParser.SelectLimitContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#valuesClause}.
	 * @param ctx the parse tree
	 */
	void enterValuesClause(DDLStatementParser.ValuesClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#valuesClause}.
	 * @param ctx the parse tree
	 */
	void exitValuesClause(DDLStatementParser.ValuesClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#limitClause}.
	 * @param ctx the parse tree
	 */
	void enterLimitClause(DDLStatementParser.LimitClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#limitClause}.
	 * @param ctx the parse tree
	 */
	void exitLimitClause(DDLStatementParser.LimitClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#offsetClause}.
	 * @param ctx the parse tree
	 */
	void enterOffsetClause(DDLStatementParser.OffsetClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#offsetClause}.
	 * @param ctx the parse tree
	 */
	void exitOffsetClause(DDLStatementParser.OffsetClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#selectLimitValue}.
	 * @param ctx the parse tree
	 */
	void enterSelectLimitValue(DDLStatementParser.SelectLimitValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#selectLimitValue}.
	 * @param ctx the parse tree
	 */
	void exitSelectLimitValue(DDLStatementParser.SelectLimitValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#selectOffsetValue}.
	 * @param ctx the parse tree
	 */
	void enterSelectOffsetValue(DDLStatementParser.SelectOffsetValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#selectOffsetValue}.
	 * @param ctx the parse tree
	 */
	void exitSelectOffsetValue(DDLStatementParser.SelectOffsetValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#selectFetchFirstValue}.
	 * @param ctx the parse tree
	 */
	void enterSelectFetchFirstValue(DDLStatementParser.SelectFetchFirstValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#selectFetchFirstValue}.
	 * @param ctx the parse tree
	 */
	void exitSelectFetchFirstValue(DDLStatementParser.SelectFetchFirstValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#rowOrRows}.
	 * @param ctx the parse tree
	 */
	void enterRowOrRows(DDLStatementParser.RowOrRowsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#rowOrRows}.
	 * @param ctx the parse tree
	 */
	void exitRowOrRows(DDLStatementParser.RowOrRowsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#firstOrNext}.
	 * @param ctx the parse tree
	 */
	void enterFirstOrNext(DDLStatementParser.FirstOrNextContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#firstOrNext}.
	 * @param ctx the parse tree
	 */
	void exitFirstOrNext(DDLStatementParser.FirstOrNextContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#targetList}.
	 * @param ctx the parse tree
	 */
	void enterTargetList(DDLStatementParser.TargetListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#targetList}.
	 * @param ctx the parse tree
	 */
	void exitTargetList(DDLStatementParser.TargetListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#targetEl}.
	 * @param ctx the parse tree
	 */
	void enterTargetEl(DDLStatementParser.TargetElContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#targetEl}.
	 * @param ctx the parse tree
	 */
	void exitTargetEl(DDLStatementParser.TargetElContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#groupClause}.
	 * @param ctx the parse tree
	 */
	void enterGroupClause(DDLStatementParser.GroupClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#groupClause}.
	 * @param ctx the parse tree
	 */
	void exitGroupClause(DDLStatementParser.GroupClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#groupByList}.
	 * @param ctx the parse tree
	 */
	void enterGroupByList(DDLStatementParser.GroupByListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#groupByList}.
	 * @param ctx the parse tree
	 */
	void exitGroupByList(DDLStatementParser.GroupByListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#groupByItem}.
	 * @param ctx the parse tree
	 */
	void enterGroupByItem(DDLStatementParser.GroupByItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#groupByItem}.
	 * @param ctx the parse tree
	 */
	void exitGroupByItem(DDLStatementParser.GroupByItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#emptyGroupingSet}.
	 * @param ctx the parse tree
	 */
	void enterEmptyGroupingSet(DDLStatementParser.EmptyGroupingSetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#emptyGroupingSet}.
	 * @param ctx the parse tree
	 */
	void exitEmptyGroupingSet(DDLStatementParser.EmptyGroupingSetContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#rollupClause}.
	 * @param ctx the parse tree
	 */
	void enterRollupClause(DDLStatementParser.RollupClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#rollupClause}.
	 * @param ctx the parse tree
	 */
	void exitRollupClause(DDLStatementParser.RollupClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#cubeClause}.
	 * @param ctx the parse tree
	 */
	void enterCubeClause(DDLStatementParser.CubeClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#cubeClause}.
	 * @param ctx the parse tree
	 */
	void exitCubeClause(DDLStatementParser.CubeClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#groupingSetsClause}.
	 * @param ctx the parse tree
	 */
	void enterGroupingSetsClause(DDLStatementParser.GroupingSetsClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#groupingSetsClause}.
	 * @param ctx the parse tree
	 */
	void exitGroupingSetsClause(DDLStatementParser.GroupingSetsClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#windowClause}.
	 * @param ctx the parse tree
	 */
	void enterWindowClause(DDLStatementParser.WindowClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#windowClause}.
	 * @param ctx the parse tree
	 */
	void exitWindowClause(DDLStatementParser.WindowClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#windowDefinitionList}.
	 * @param ctx the parse tree
	 */
	void enterWindowDefinitionList(DDLStatementParser.WindowDefinitionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#windowDefinitionList}.
	 * @param ctx the parse tree
	 */
	void exitWindowDefinitionList(DDLStatementParser.WindowDefinitionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#windowDefinition}.
	 * @param ctx the parse tree
	 */
	void enterWindowDefinition(DDLStatementParser.WindowDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#windowDefinition}.
	 * @param ctx the parse tree
	 */
	void exitWindowDefinition(DDLStatementParser.WindowDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#existingWindowName}.
	 * @param ctx the parse tree
	 */
	void enterExistingWindowName(DDLStatementParser.ExistingWindowNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#existingWindowName}.
	 * @param ctx the parse tree
	 */
	void exitExistingWindowName(DDLStatementParser.ExistingWindowNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#optWindowExclusionClause}.
	 * @param ctx the parse tree
	 */
	void enterOptWindowExclusionClause(DDLStatementParser.OptWindowExclusionClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#optWindowExclusionClause}.
	 * @param ctx the parse tree
	 */
	void exitOptWindowExclusionClause(DDLStatementParser.OptWindowExclusionClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void enterFromClause(DDLStatementParser.FromClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void exitFromClause(DDLStatementParser.FromClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#fromList}.
	 * @param ctx the parse tree
	 */
	void enterFromList(DDLStatementParser.FromListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#fromList}.
	 * @param ctx the parse tree
	 */
	void exitFromList(DDLStatementParser.FromListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#tableReference}.
	 * @param ctx the parse tree
	 */
	void enterTableReference(DDLStatementParser.TableReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#tableReference}.
	 * @param ctx the parse tree
	 */
	void exitTableReference(DDLStatementParser.TableReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#joinedTable}.
	 * @param ctx the parse tree
	 */
	void enterJoinedTable(DDLStatementParser.JoinedTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#joinedTable}.
	 * @param ctx the parse tree
	 */
	void exitJoinedTable(DDLStatementParser.JoinedTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#joinType}.
	 * @param ctx the parse tree
	 */
	void enterJoinType(DDLStatementParser.JoinTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#joinType}.
	 * @param ctx the parse tree
	 */
	void exitJoinType(DDLStatementParser.JoinTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#joinOuter}.
	 * @param ctx the parse tree
	 */
	void enterJoinOuter(DDLStatementParser.JoinOuterContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#joinOuter}.
	 * @param ctx the parse tree
	 */
	void exitJoinOuter(DDLStatementParser.JoinOuterContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#joinQual}.
	 * @param ctx the parse tree
	 */
	void enterJoinQual(DDLStatementParser.JoinQualContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#joinQual}.
	 * @param ctx the parse tree
	 */
	void exitJoinQual(DDLStatementParser.JoinQualContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void enterWhereClause(DDLStatementParser.WhereClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void exitWhereClause(DDLStatementParser.WhereClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#whereOrCurrentClause}.
	 * @param ctx the parse tree
	 */
	void enterWhereOrCurrentClause(DDLStatementParser.WhereOrCurrentClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#whereOrCurrentClause}.
	 * @param ctx the parse tree
	 */
	void exitWhereOrCurrentClause(DDLStatementParser.WhereOrCurrentClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#havingClause}.
	 * @param ctx the parse tree
	 */
	void enterHavingClause(DDLStatementParser.HavingClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#havingClause}.
	 * @param ctx the parse tree
	 */
	void exitHavingClause(DDLStatementParser.HavingClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#doStatement}.
	 * @param ctx the parse tree
	 */
	void enterDoStatement(DDLStatementParser.DoStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#doStatement}.
	 * @param ctx the parse tree
	 */
	void exitDoStatement(DDLStatementParser.DoStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dostmtOptList}.
	 * @param ctx the parse tree
	 */
	void enterDostmtOptList(DDLStatementParser.DostmtOptListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dostmtOptList}.
	 * @param ctx the parse tree
	 */
	void exitDostmtOptList(DDLStatementParser.DostmtOptListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#dostmtOptItem}.
	 * @param ctx the parse tree
	 */
	void enterDostmtOptItem(DDLStatementParser.DostmtOptItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#dostmtOptItem}.
	 * @param ctx the parse tree
	 */
	void exitDostmtOptItem(DDLStatementParser.DostmtOptItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#lock}.
	 * @param ctx the parse tree
	 */
	void enterLock(DDLStatementParser.LockContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#lock}.
	 * @param ctx the parse tree
	 */
	void exitLock(DDLStatementParser.LockContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#lockType}.
	 * @param ctx the parse tree
	 */
	void enterLockType(DDLStatementParser.LockTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#lockType}.
	 * @param ctx the parse tree
	 */
	void exitLockType(DDLStatementParser.LockTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#checkpoint}.
	 * @param ctx the parse tree
	 */
	void enterCheckpoint(DDLStatementParser.CheckpointContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#checkpoint}.
	 * @param ctx the parse tree
	 */
	void exitCheckpoint(DDLStatementParser.CheckpointContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#copy}.
	 * @param ctx the parse tree
	 */
	void enterCopy(DDLStatementParser.CopyContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#copy}.
	 * @param ctx the parse tree
	 */
	void exitCopy(DDLStatementParser.CopyContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#copyOptions}.
	 * @param ctx the parse tree
	 */
	void enterCopyOptions(DDLStatementParser.CopyOptionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#copyOptions}.
	 * @param ctx the parse tree
	 */
	void exitCopyOptions(DDLStatementParser.CopyOptionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#copyGenericOptList}.
	 * @param ctx the parse tree
	 */
	void enterCopyGenericOptList(DDLStatementParser.CopyGenericOptListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#copyGenericOptList}.
	 * @param ctx the parse tree
	 */
	void exitCopyGenericOptList(DDLStatementParser.CopyGenericOptListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#copyGenericOptElem}.
	 * @param ctx the parse tree
	 */
	void enterCopyGenericOptElem(DDLStatementParser.CopyGenericOptElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#copyGenericOptElem}.
	 * @param ctx the parse tree
	 */
	void exitCopyGenericOptElem(DDLStatementParser.CopyGenericOptElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#copyGenericOptArg}.
	 * @param ctx the parse tree
	 */
	void enterCopyGenericOptArg(DDLStatementParser.CopyGenericOptArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#copyGenericOptArg}.
	 * @param ctx the parse tree
	 */
	void exitCopyGenericOptArg(DDLStatementParser.CopyGenericOptArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#copyGenericOptArgList}.
	 * @param ctx the parse tree
	 */
	void enterCopyGenericOptArgList(DDLStatementParser.CopyGenericOptArgListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#copyGenericOptArgList}.
	 * @param ctx the parse tree
	 */
	void exitCopyGenericOptArgList(DDLStatementParser.CopyGenericOptArgListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#copyGenericOptArgListItem}.
	 * @param ctx the parse tree
	 */
	void enterCopyGenericOptArgListItem(DDLStatementParser.CopyGenericOptArgListItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#copyGenericOptArgListItem}.
	 * @param ctx the parse tree
	 */
	void exitCopyGenericOptArgListItem(DDLStatementParser.CopyGenericOptArgListItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#copyOptList}.
	 * @param ctx the parse tree
	 */
	void enterCopyOptList(DDLStatementParser.CopyOptListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#copyOptList}.
	 * @param ctx the parse tree
	 */
	void exitCopyOptList(DDLStatementParser.CopyOptListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#copyOptItem}.
	 * @param ctx the parse tree
	 */
	void enterCopyOptItem(DDLStatementParser.CopyOptItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#copyOptItem}.
	 * @param ctx the parse tree
	 */
	void exitCopyOptItem(DDLStatementParser.CopyOptItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#copyDelimiter}.
	 * @param ctx the parse tree
	 */
	void enterCopyDelimiter(DDLStatementParser.CopyDelimiterContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#copyDelimiter}.
	 * @param ctx the parse tree
	 */
	void exitCopyDelimiter(DDLStatementParser.CopyDelimiterContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#fetch}.
	 * @param ctx the parse tree
	 */
	void enterFetch(DDLStatementParser.FetchContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#fetch}.
	 * @param ctx the parse tree
	 */
	void exitFetch(DDLStatementParser.FetchContext ctx);
	/**
	 * Enter a parse tree produced by {@link DDLStatementParser#fetchArgs}.
	 * @param ctx the parse tree
	 */
	void enterFetchArgs(DDLStatementParser.FetchArgsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DDLStatementParser#fetchArgs}.
	 * @param ctx the parse tree
	 */
	void exitFetchArgs(DDLStatementParser.FetchArgsContext ctx);
}