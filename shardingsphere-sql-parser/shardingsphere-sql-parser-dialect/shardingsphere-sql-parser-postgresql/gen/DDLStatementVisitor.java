// Generated from /home/totalo/code/shardingsphere/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-postgresql/src/main/antlr4/imports/postgresql/DDLStatement.g4 by ANTLR 4.9.1
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link DDLStatementParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface DDLStatementVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTable(DDLStatementParser.CreateTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#executeParamClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExecuteParamClause(DDLStatementParser.ExecuteParamClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#partitionBoundSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionBoundSpec(DDLStatementParser.PartitionBoundSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#hashPartbound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHashPartbound(DDLStatementParser.HashPartboundContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#hashPartboundElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHashPartboundElem(DDLStatementParser.HashPartboundElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#typedTableElementList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypedTableElementList(DDLStatementParser.TypedTableElementListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#typedTableElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypedTableElement(DDLStatementParser.TypedTableElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#columnOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnOptions(DDLStatementParser.ColumnOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#colQualList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColQualList(DDLStatementParser.ColQualListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#withData}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithData(DDLStatementParser.WithDataContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#tableSpace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableSpace(DDLStatementParser.TableSpaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#onCommitOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOnCommitOption(DDLStatementParser.OnCommitOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#withOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithOption(DDLStatementParser.WithOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#tableAccessMethodClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableAccessMethodClause(DDLStatementParser.TableAccessMethodClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#accessMethod}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAccessMethod(DDLStatementParser.AccessMethodContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createIndex}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateIndex(DDLStatementParser.CreateIndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#include}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInclude(DDLStatementParser.IncludeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#indexIncludingParams}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexIncludingParams(DDLStatementParser.IndexIncludingParamsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#accessMethodClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAccessMethodClause(DDLStatementParser.AccessMethodClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createDatabase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateDatabase(DDLStatementParser.CreateDatabaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createView}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateView(DDLStatementParser.CreateViewContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#columnList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnList(DDLStatementParser.ColumnListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#columnElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnElem(DDLStatementParser.ColumnElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropDatabase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropDatabase(DDLStatementParser.DropDatabaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createDatabaseSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateDatabaseSpecification(DDLStatementParser.CreateDatabaseSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createdbOptName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatedbOptName(DDLStatementParser.CreatedbOptNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTable(DDLStatementParser.AlterTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterIndex}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterIndex(DDLStatementParser.AlterIndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropTable(DDLStatementParser.DropTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropTableOpt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropTableOpt(DDLStatementParser.DropTableOptContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropIndex}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropIndex(DDLStatementParser.DropIndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropIndexOpt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropIndexOpt(DDLStatementParser.DropIndexOptContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#truncateTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTruncateTable(DDLStatementParser.TruncateTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#restartSeqs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRestartSeqs(DDLStatementParser.RestartSeqsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createTableSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTableSpecification(DDLStatementParser.CreateTableSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createDefinitionClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateDefinitionClause(DDLStatementParser.CreateDefinitionClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateDefinition(DDLStatementParser.CreateDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#columnDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnDefinition(DDLStatementParser.ColumnDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#columnConstraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnConstraint(DDLStatementParser.ColumnConstraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#constraintClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintClause(DDLStatementParser.ConstraintClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#columnConstraintOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnConstraintOption(DDLStatementParser.ColumnConstraintOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#checkOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCheckOption(DDLStatementParser.CheckOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#defaultExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefaultExpr(DDLStatementParser.DefaultExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#sequenceOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSequenceOptions(DDLStatementParser.SequenceOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#sequenceOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSequenceOption(DDLStatementParser.SequenceOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#indexParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexParameters(DDLStatementParser.IndexParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#action}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAction(DDLStatementParser.ActionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#constraintOptionalParam}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintOptionalParam(DDLStatementParser.ConstraintOptionalParamContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#likeOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLikeOption(DDLStatementParser.LikeOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#tableConstraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableConstraint(DDLStatementParser.TableConstraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#tableConstraintOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableConstraintOption(DDLStatementParser.TableConstraintOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#exclusionWhereClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExclusionWhereClause(DDLStatementParser.ExclusionWhereClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#exclusionConstraintList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExclusionConstraintList(DDLStatementParser.ExclusionConstraintListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#exclusionConstraintElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExclusionConstraintElem(DDLStatementParser.ExclusionConstraintElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#inheritClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInheritClause(DDLStatementParser.InheritClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#partitionSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionSpec(DDLStatementParser.PartitionSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#partParams}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartParams(DDLStatementParser.PartParamsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#partElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartElem(DDLStatementParser.PartElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#funcExprWindowless}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncExprWindowless(DDLStatementParser.FuncExprWindowlessContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#partStrategy}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartStrategy(DDLStatementParser.PartStrategyContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createIndexSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateIndexSpecification(DDLStatementParser.CreateIndexSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#concurrentlyClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConcurrentlyClause(DDLStatementParser.ConcurrentlyClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#onlyClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOnlyClause(DDLStatementParser.OnlyClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#asteriskClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsteriskClause(DDLStatementParser.AsteriskClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterDefinitionClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterDefinitionClause(DDLStatementParser.AlterDefinitionClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#partitionCmd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionCmd(DDLStatementParser.PartitionCmdContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterIndexDefinitionClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterIndexDefinitionClause(DDLStatementParser.AlterIndexDefinitionClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#indexPartitionCmd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexPartitionCmd(DDLStatementParser.IndexPartitionCmdContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#renameIndexSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRenameIndexSpecification(DDLStatementParser.RenameIndexSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterIndexDependsOnExtension}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterIndexDependsOnExtension(DDLStatementParser.AlterIndexDependsOnExtensionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterIndexSetTableSpace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterIndexSetTableSpace(DDLStatementParser.AlterIndexSetTableSpaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#tableNamesClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableNamesClause(DDLStatementParser.TableNamesClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#tableNameClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableNameClause(DDLStatementParser.TableNameClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterTableActions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTableActions(DDLStatementParser.AlterTableActionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterTableAction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTableAction(DDLStatementParser.AlterTableActionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#addColumnSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddColumnSpecification(DDLStatementParser.AddColumnSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropColumnSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropColumnSpecification(DDLStatementParser.DropColumnSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#modifyColumnSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModifyColumnSpecification(DDLStatementParser.ModifyColumnSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#modifyColumn}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModifyColumn(DDLStatementParser.ModifyColumnContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterColumnSetOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterColumnSetOption(DDLStatementParser.AlterColumnSetOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#attributeOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeOptions(DDLStatementParser.AttributeOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#attributeOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributeOption(DDLStatementParser.AttributeOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#addConstraintSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddConstraintSpecification(DDLStatementParser.AddConstraintSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#tableConstraintUsingIndex}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableConstraintUsingIndex(DDLStatementParser.TableConstraintUsingIndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#storageParameterWithValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStorageParameterWithValue(DDLStatementParser.StorageParameterWithValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#storageParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStorageParameter(DDLStatementParser.StorageParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#renameColumnSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRenameColumnSpecification(DDLStatementParser.RenameColumnSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#renameConstraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRenameConstraint(DDLStatementParser.RenameConstraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#renameTableSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRenameTableSpecification(DDLStatementParser.RenameTableSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#indexNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexNames(DDLStatementParser.IndexNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterDatabase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterDatabase(DDLStatementParser.AlterDatabaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterDatabaseClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterDatabaseClause(DDLStatementParser.AlterDatabaseClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createdbOptItems}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatedbOptItems(DDLStatementParser.CreatedbOptItemsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createdbOptItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatedbOptItem(DDLStatementParser.CreatedbOptItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterTableCmds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTableCmds(DDLStatementParser.AlterTableCmdsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterTableCmd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTableCmd(DDLStatementParser.AlterTableCmdContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#constraintAttributeSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintAttributeSpec(DDLStatementParser.ConstraintAttributeSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#constraintAttributeElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintAttributeElem(DDLStatementParser.ConstraintAttributeElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterGenericOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterGenericOptions(DDLStatementParser.AlterGenericOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterGenericOptionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterGenericOptionList(DDLStatementParser.AlterGenericOptionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterGenericOptionElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterGenericOptionElem(DDLStatementParser.AlterGenericOptionElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#genericOptionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericOptionName(DDLStatementParser.GenericOptionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropBehavior}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropBehavior(DDLStatementParser.DropBehaviorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterUsing}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterUsing(DDLStatementParser.AlterUsingContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#setData}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetData(DDLStatementParser.SetDataContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterIdentityColumnOptionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterIdentityColumnOptionList(DDLStatementParser.AlterIdentityColumnOptionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterIdentityColumnOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterIdentityColumnOption(DDLStatementParser.AlterIdentityColumnOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterColumnDefault}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterColumnDefault(DDLStatementParser.AlterColumnDefaultContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterOperator(DDLStatementParser.AlterOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterOperatorClass}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterOperatorClass(DDLStatementParser.AlterOperatorClassContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterOperatorClassClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterOperatorClassClauses(DDLStatementParser.AlterOperatorClassClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterOperatorFamily}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterOperatorFamily(DDLStatementParser.AlterOperatorFamilyContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterOperatorFamilyClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterOperatorFamilyClauses(DDLStatementParser.AlterOperatorFamilyClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#opclassItemList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpclassItemList(DDLStatementParser.OpclassItemListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#opclassItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpclassItem(DDLStatementParser.OpclassItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#opclassPurpose}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpclassPurpose(DDLStatementParser.OpclassPurposeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterOperatorClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterOperatorClauses(DDLStatementParser.AlterOperatorClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#operatorDefList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperatorDefList(DDLStatementParser.OperatorDefListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#operatorDefElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperatorDefElem(DDLStatementParser.OperatorDefElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#operatorDefArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperatorDefArg(DDLStatementParser.OperatorDefArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#operatorWithArgtypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperatorWithArgtypes(DDLStatementParser.OperatorWithArgtypesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterAggregate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterAggregate(DDLStatementParser.AlterAggregateContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#aggregateSignature}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregateSignature(DDLStatementParser.AggregateSignatureContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#aggrArgs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggrArgs(DDLStatementParser.AggrArgsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#aggrArgsList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggrArgsList(DDLStatementParser.AggrArgsListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#aggrArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggrArg(DDLStatementParser.AggrArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterAggregateDefinitionClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterAggregateDefinitionClause(DDLStatementParser.AlterAggregateDefinitionClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterCollation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterCollation(DDLStatementParser.AlterCollationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterCollationClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterCollationClause(DDLStatementParser.AlterCollationClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterConversion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterConversion(DDLStatementParser.AlterConversionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterConversionClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterConversionClause(DDLStatementParser.AlterConversionClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterDefaultPrivileges}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterDefaultPrivileges(DDLStatementParser.AlterDefaultPrivilegesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#defACLAction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefACLAction(DDLStatementParser.DefACLActionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#grantGrantOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrantGrantOption(DDLStatementParser.GrantGrantOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#granteeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGranteeList(DDLStatementParser.GranteeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#grantee}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrantee(DDLStatementParser.GranteeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#defaclPrivilegeTarget}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefaclPrivilegeTarget(DDLStatementParser.DefaclPrivilegeTargetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#privileges}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivileges(DDLStatementParser.PrivilegesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#privilegeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivilegeList(DDLStatementParser.PrivilegeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#privilege}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivilege(DDLStatementParser.PrivilegeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#defACLOptionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefACLOptionList(DDLStatementParser.DefACLOptionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#defACLOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefACLOption(DDLStatementParser.DefACLOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#schemaNameList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaNameList(DDLStatementParser.SchemaNameListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterDomain}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterDomain(DDLStatementParser.AlterDomainContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterDomainClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterDomainClause(DDLStatementParser.AlterDomainClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#constraintName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintName(DDLStatementParser.ConstraintNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterEventTrigger}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterEventTrigger(DDLStatementParser.AlterEventTriggerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterEventTriggerClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterEventTriggerClause(DDLStatementParser.AlterEventTriggerClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#tiggerName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTiggerName(DDLStatementParser.TiggerNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterExtension}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterExtension(DDLStatementParser.AlterExtensionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterExtensionClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterExtensionClauses(DDLStatementParser.AlterExtensionClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#functionWithArgtypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionWithArgtypes(DDLStatementParser.FunctionWithArgtypesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#funcArgs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncArgs(DDLStatementParser.FuncArgsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#aggregateWithArgtypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregateWithArgtypes(DDLStatementParser.AggregateWithArgtypesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterExtensionOptList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterExtensionOptList(DDLStatementParser.AlterExtensionOptListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterExtensionOptItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterExtensionOptItem(DDLStatementParser.AlterExtensionOptItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterForeignDataWrapper}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterForeignDataWrapper(DDLStatementParser.AlterForeignDataWrapperContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterForeignDataWrapperClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterForeignDataWrapperClauses(DDLStatementParser.AlterForeignDataWrapperClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#genericOptionElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericOptionElem(DDLStatementParser.GenericOptionElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#genericOptionArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericOptionArg(DDLStatementParser.GenericOptionArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#fdwOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFdwOptions(DDLStatementParser.FdwOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#fdwOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFdwOption(DDLStatementParser.FdwOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#handlerName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHandlerName(DDLStatementParser.HandlerNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterGroup}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterGroup(DDLStatementParser.AlterGroupContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterGroupClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterGroupClauses(DDLStatementParser.AlterGroupClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterLanguage}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterLanguage(DDLStatementParser.AlterLanguageContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterLargeObject}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterLargeObject(DDLStatementParser.AlterLargeObjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterMaterializedView}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterMaterializedView(DDLStatementParser.AlterMaterializedViewContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterMaterializedViewClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterMaterializedViewClauses(DDLStatementParser.AlterMaterializedViewClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#declare}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclare(DDLStatementParser.DeclareContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#cursorOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCursorOptions(DDLStatementParser.CursorOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#cursorOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCursorOption(DDLStatementParser.CursorOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#executeStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExecuteStmt(DDLStatementParser.ExecuteStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createMaterializedView}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateMaterializedView(DDLStatementParser.CreateMaterializedViewContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createMvTarget}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateMvTarget(DDLStatementParser.CreateMvTargetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#refreshMatViewStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRefreshMatViewStmt(DDLStatementParser.RefreshMatViewStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterPolicy}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterPolicy(DDLStatementParser.AlterPolicyContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterPolicyClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterPolicyClauses(DDLStatementParser.AlterPolicyClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterProcedure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterProcedure(DDLStatementParser.AlterProcedureContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterProcedureClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterProcedureClauses(DDLStatementParser.AlterProcedureClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterfuncOptList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterfuncOptList(DDLStatementParser.AlterfuncOptListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterFunction(DDLStatementParser.AlterFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterFunctionClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterFunctionClauses(DDLStatementParser.AlterFunctionClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterPublication}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterPublication(DDLStatementParser.AlterPublicationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterRoutine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterRoutine(DDLStatementParser.AlterRoutineContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterRule}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterRule(DDLStatementParser.AlterRuleContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterSequence}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterSequence(DDLStatementParser.AlterSequenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterSequenceClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterSequenceClauses(DDLStatementParser.AlterSequenceClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterServer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterServer(DDLStatementParser.AlterServerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#foreignServerVersion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForeignServerVersion(DDLStatementParser.ForeignServerVersionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterStatistics}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterStatistics(DDLStatementParser.AlterStatisticsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterSubscription}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterSubscription(DDLStatementParser.AlterSubscriptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#publicationNameList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPublicationNameList(DDLStatementParser.PublicationNameListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#publicationNameItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPublicationNameItem(DDLStatementParser.PublicationNameItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterSystem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterSystem(DDLStatementParser.AlterSystemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterTablespace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTablespace(DDLStatementParser.AlterTablespaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterTextSearchConfiguration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTextSearchConfiguration(DDLStatementParser.AlterTextSearchConfigurationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterTextSearchConfigurationClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTextSearchConfigurationClauses(DDLStatementParser.AlterTextSearchConfigurationClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#anyNameList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnyNameList(DDLStatementParser.AnyNameListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterTextSearchDictionary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTextSearchDictionary(DDLStatementParser.AlterTextSearchDictionaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterTextSearchParser}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTextSearchParser(DDLStatementParser.AlterTextSearchParserContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterTextSearchTemplate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTextSearchTemplate(DDLStatementParser.AlterTextSearchTemplateContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterTrigger}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTrigger(DDLStatementParser.AlterTriggerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterType(DDLStatementParser.AlterTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterTypeClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTypeClauses(DDLStatementParser.AlterTypeClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterTypeCmds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTypeCmds(DDLStatementParser.AlterTypeCmdsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterTypeCmd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterTypeCmd(DDLStatementParser.AlterTypeCmdContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterUserMapping}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterUserMapping(DDLStatementParser.AlterUserMappingContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#authIdent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAuthIdent(DDLStatementParser.AuthIdentContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterView}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterView(DDLStatementParser.AlterViewContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterViewClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterViewClauses(DDLStatementParser.AlterViewClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#close}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClose(DDLStatementParser.CloseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#cluster}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCluster(DDLStatementParser.ClusterContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#clusterIndexSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClusterIndexSpecification(DDLStatementParser.ClusterIndexSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#comment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComment(DDLStatementParser.CommentContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#commentClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommentClauses(DDLStatementParser.CommentClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#objectTypeNameOnAnyName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectTypeNameOnAnyName(DDLStatementParser.ObjectTypeNameOnAnyNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#objectTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectTypeName(DDLStatementParser.ObjectTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropTypeName(DDLStatementParser.DropTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#objectTypeAnyName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectTypeAnyName(DDLStatementParser.ObjectTypeAnyNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#commentText}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommentText(DDLStatementParser.CommentTextContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createAccessMethod}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateAccessMethod(DDLStatementParser.CreateAccessMethodContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createAggregate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateAggregate(DDLStatementParser.CreateAggregateContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#oldAggrDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOldAggrDefinition(DDLStatementParser.OldAggrDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#oldAggrList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOldAggrList(DDLStatementParser.OldAggrListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#oldAggrElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOldAggrElem(DDLStatementParser.OldAggrElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createCast}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateCast(DDLStatementParser.CreateCastContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#castContext}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCastContext(DDLStatementParser.CastContextContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createCollation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateCollation(DDLStatementParser.CreateCollationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createConversion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateConversion(DDLStatementParser.CreateConversionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createDomain}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateDomain(DDLStatementParser.CreateDomainContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createEventTrigger}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateEventTrigger(DDLStatementParser.CreateEventTriggerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#eventTriggerWhenList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEventTriggerWhenList(DDLStatementParser.EventTriggerWhenListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#eventTriggerWhenItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEventTriggerWhenItem(DDLStatementParser.EventTriggerWhenItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#eventTriggerValueList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEventTriggerValueList(DDLStatementParser.EventTriggerValueListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createExtension}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateExtension(DDLStatementParser.CreateExtensionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createExtensionOptList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateExtensionOptList(DDLStatementParser.CreateExtensionOptListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createExtensionOptItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateExtensionOptItem(DDLStatementParser.CreateExtensionOptItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createForeignDataWrapper}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateForeignDataWrapper(DDLStatementParser.CreateForeignDataWrapperContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createForeignTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateForeignTable(DDLStatementParser.CreateForeignTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createForeignTableClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateForeignTableClauses(DDLStatementParser.CreateForeignTableClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#tableElementList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableElementList(DDLStatementParser.TableElementListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#tableElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableElement(DDLStatementParser.TableElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#tableLikeClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableLikeClause(DDLStatementParser.TableLikeClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#tableLikeOptionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableLikeOptionList(DDLStatementParser.TableLikeOptionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#tableLikeOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableLikeOption(DDLStatementParser.TableLikeOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateFunction(DDLStatementParser.CreateFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#tableFuncColumnList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableFuncColumnList(DDLStatementParser.TableFuncColumnListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#tableFuncColumn}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableFuncColumn(DDLStatementParser.TableFuncColumnContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createfuncOptList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatefuncOptList(DDLStatementParser.CreatefuncOptListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createfuncOptItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatefuncOptItem(DDLStatementParser.CreatefuncOptItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#transformTypeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformTypeList(DDLStatementParser.TransformTypeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#funcAs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncAs(DDLStatementParser.FuncAsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#funcReturn}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncReturn(DDLStatementParser.FuncReturnContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#funcArgsWithDefaults}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncArgsWithDefaults(DDLStatementParser.FuncArgsWithDefaultsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#funcArgsWithDefaultsList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncArgsWithDefaultsList(DDLStatementParser.FuncArgsWithDefaultsListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#funcArgWithDefault}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncArgWithDefault(DDLStatementParser.FuncArgWithDefaultContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createLanguage}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateLanguage(DDLStatementParser.CreateLanguageContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#transformElementList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformElementList(DDLStatementParser.TransformElementListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#validatorClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValidatorClause(DDLStatementParser.ValidatorClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createPolicy}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatePolicy(DDLStatementParser.CreatePolicyContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createProcedure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateProcedure(DDLStatementParser.CreateProcedureContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createPublication}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatePublication(DDLStatementParser.CreatePublicationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#publicationForTables}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPublicationForTables(DDLStatementParser.PublicationForTablesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createRule}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateRule(DDLStatementParser.CreateRuleContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#ruleActionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRuleActionList(DDLStatementParser.RuleActionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#ruleActionStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRuleActionStmt(DDLStatementParser.RuleActionStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#ruleActionMulti}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRuleActionMulti(DDLStatementParser.RuleActionMultiContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#notifyStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotifyStmt(DDLStatementParser.NotifyStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createTrigger}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTrigger(DDLStatementParser.CreateTriggerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#triggerEvents}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerEvents(DDLStatementParser.TriggerEventsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#triggerOneEvent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerOneEvent(DDLStatementParser.TriggerOneEventContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#triggerActionTime}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerActionTime(DDLStatementParser.TriggerActionTimeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#triggerFuncArgs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerFuncArgs(DDLStatementParser.TriggerFuncArgsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#triggerFuncArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerFuncArg(DDLStatementParser.TriggerFuncArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#triggerWhen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerWhen(DDLStatementParser.TriggerWhenContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#triggerForSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerForSpec(DDLStatementParser.TriggerForSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#triggerReferencing}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerReferencing(DDLStatementParser.TriggerReferencingContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#triggerTransitions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerTransitions(DDLStatementParser.TriggerTransitionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#triggerTransition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerTransition(DDLStatementParser.TriggerTransitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#transitionRelName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransitionRelName(DDLStatementParser.TransitionRelNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#transitionRowOrTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransitionRowOrTable(DDLStatementParser.TransitionRowOrTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#transitionOldOrNew}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransitionOldOrNew(DDLStatementParser.TransitionOldOrNewContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createSequence}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateSequence(DDLStatementParser.CreateSequenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#tempOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTempOption(DDLStatementParser.TempOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createServer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateServer(DDLStatementParser.CreateServerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createStatistics}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateStatistics(DDLStatementParser.CreateStatisticsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createSubscription}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateSubscription(DDLStatementParser.CreateSubscriptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createTablespace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTablespace(DDLStatementParser.CreateTablespaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createTextSearch}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTextSearch(DDLStatementParser.CreateTextSearchContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createTransform}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTransform(DDLStatementParser.CreateTransformContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateType(DDLStatementParser.CreateTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createTypeClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTypeClauses(DDLStatementParser.CreateTypeClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#enumValList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumValList(DDLStatementParser.EnumValListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createUserMapping}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateUserMapping(DDLStatementParser.CreateUserMappingContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#discard}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDiscard(DDLStatementParser.DiscardContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropAccessMethod}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropAccessMethod(DDLStatementParser.DropAccessMethodContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropAggregate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropAggregate(DDLStatementParser.DropAggregateContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#aggregateWithArgtypesList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregateWithArgtypesList(DDLStatementParser.AggregateWithArgtypesListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropCast}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropCast(DDLStatementParser.DropCastContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropCollation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropCollation(DDLStatementParser.DropCollationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropConversion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropConversion(DDLStatementParser.DropConversionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropDomain}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropDomain(DDLStatementParser.DropDomainContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropEventTrigger}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropEventTrigger(DDLStatementParser.DropEventTriggerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropExtension}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropExtension(DDLStatementParser.DropExtensionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropForeignDataWrapper}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropForeignDataWrapper(DDLStatementParser.DropForeignDataWrapperContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropForeignTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropForeignTable(DDLStatementParser.DropForeignTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropFunction(DDLStatementParser.DropFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#functionWithArgtypesList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionWithArgtypesList(DDLStatementParser.FunctionWithArgtypesListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropLanguage}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropLanguage(DDLStatementParser.DropLanguageContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropMaterializedView}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropMaterializedView(DDLStatementParser.DropMaterializedViewContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropOperator(DDLStatementParser.DropOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#operatorWithArgtypesList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperatorWithArgtypesList(DDLStatementParser.OperatorWithArgtypesListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropOperatorClass}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropOperatorClass(DDLStatementParser.DropOperatorClassContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropOperatorFamily}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropOperatorFamily(DDLStatementParser.DropOperatorFamilyContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropOwned}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropOwned(DDLStatementParser.DropOwnedContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropPolicy}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropPolicy(DDLStatementParser.DropPolicyContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropProcedure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropProcedure(DDLStatementParser.DropProcedureContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropPublication}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropPublication(DDLStatementParser.DropPublicationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropRoutine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropRoutine(DDLStatementParser.DropRoutineContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropRule}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropRule(DDLStatementParser.DropRuleContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropSequence}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropSequence(DDLStatementParser.DropSequenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropServer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropServer(DDLStatementParser.DropServerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropStatistics}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropStatistics(DDLStatementParser.DropStatisticsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropSubscription}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropSubscription(DDLStatementParser.DropSubscriptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropTablespace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropTablespace(DDLStatementParser.DropTablespaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropTextSearch}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropTextSearch(DDLStatementParser.DropTextSearchContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropTransform}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropTransform(DDLStatementParser.DropTransformContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropTrigger}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropTrigger(DDLStatementParser.DropTriggerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropType(DDLStatementParser.DropTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropUserMapping}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropUserMapping(DDLStatementParser.DropUserMappingContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dropView}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropView(DDLStatementParser.DropViewContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#importForeignSchema}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportForeignSchema(DDLStatementParser.ImportForeignSchemaContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#importQualification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportQualification(DDLStatementParser.ImportQualificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#importQualificationType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportQualificationType(DDLStatementParser.ImportQualificationTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#listen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListen(DDLStatementParser.ListenContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#move}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMove(DDLStatementParser.MoveContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#prepare}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrepare(DDLStatementParser.PrepareContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#deallocate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeallocate(DDLStatementParser.DeallocateContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#prepTypeClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrepTypeClause(DDLStatementParser.PrepTypeClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#refreshMaterializedView}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRefreshMaterializedView(DDLStatementParser.RefreshMaterializedViewContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#reIndex}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReIndex(DDLStatementParser.ReIndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#reIndexClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReIndexClauses(DDLStatementParser.ReIndexClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#reindexOptionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReindexOptionList(DDLStatementParser.ReindexOptionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#reindexOptionElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReindexOptionElem(DDLStatementParser.ReindexOptionElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#reindexTargetMultitable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReindexTargetMultitable(DDLStatementParser.ReindexTargetMultitableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#reindexTargetType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReindexTargetType(DDLStatementParser.ReindexTargetTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterForeignTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterForeignTable(DDLStatementParser.AlterForeignTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alterForeignTableClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterForeignTableClauses(DDLStatementParser.AlterForeignTableClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateOperator(DDLStatementParser.CreateOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createOperatorClass}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateOperatorClass(DDLStatementParser.CreateOperatorClassContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createOperatorFamily}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateOperatorFamily(DDLStatementParser.CreateOperatorFamilyContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#securityLabelStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSecurityLabelStmt(DDLStatementParser.SecurityLabelStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#securityLabel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSecurityLabel(DDLStatementParser.SecurityLabelContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#securityLabelClausces}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSecurityLabelClausces(DDLStatementParser.SecurityLabelClauscesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#unlisten}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnlisten(DDLStatementParser.UnlistenContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#parameterMarker}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterMarker(DDLStatementParser.ParameterMarkerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#reservedKeyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReservedKeyword(DDLStatementParser.ReservedKeywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#numberLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberLiterals(DDLStatementParser.NumberLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#literalsType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteralsType(DDLStatementParser.LiteralsTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(DDLStatementParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#unicodeEscapes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnicodeEscapes(DDLStatementParser.UnicodeEscapesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#uescape}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUescape(DDLStatementParser.UescapeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#unreservedWord}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnreservedWord(DDLStatementParser.UnreservedWordContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#typeFuncNameKeyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeFuncNameKeyword(DDLStatementParser.TypeFuncNameKeywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#schemaName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaName(DDLStatementParser.SchemaNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#tableName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableName(DDLStatementParser.TableNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#columnName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnName(DDLStatementParser.ColumnNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#owner}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOwner(DDLStatementParser.OwnerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName(DDLStatementParser.NameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#tableNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableNames(DDLStatementParser.TableNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#columnNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNames(DDLStatementParser.ColumnNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#collationName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollationName(DDLStatementParser.CollationNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#indexName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexName(DDLStatementParser.IndexNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlias(DDLStatementParser.AliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#primaryKey}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryKey(DDLStatementParser.PrimaryKeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#logicalOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOperator(DDLStatementParser.LogicalOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOperator(DDLStatementParser.ComparisonOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#patternMatchingOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPatternMatchingOperator(DDLStatementParser.PatternMatchingOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#cursorName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCursorName(DDLStatementParser.CursorNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#aExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAExpr(DDLStatementParser.AExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#bExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBExpr(DDLStatementParser.BExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#cExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCExpr(DDLStatementParser.CExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#indirection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndirection(DDLStatementParser.IndirectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#optIndirection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptIndirection(DDLStatementParser.OptIndirectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#indirectionEl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndirectionEl(DDLStatementParser.IndirectionElContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#sliceBound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSliceBound(DDLStatementParser.SliceBoundContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#inExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInExpr(DDLStatementParser.InExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#caseExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseExpr(DDLStatementParser.CaseExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#whenClauseList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhenClauseList(DDLStatementParser.WhenClauseListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#whenClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhenClause(DDLStatementParser.WhenClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#caseDefault}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseDefault(DDLStatementParser.CaseDefaultContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#caseArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseArg(DDLStatementParser.CaseArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#columnref}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnref(DDLStatementParser.ColumnrefContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#qualOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualOp(DDLStatementParser.QualOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#subqueryOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubqueryOp(DDLStatementParser.SubqueryOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#allOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAllOp(DDLStatementParser.AllOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#op}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOp(DDLStatementParser.OpContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#mathOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathOperator(DDLStatementParser.MathOperatorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonExtract}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonExtract(DDLStatementParser.JsonExtractContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonExtractText}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonExtractText(DDLStatementParser.JsonExtractTextContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonPathExtract}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonPathExtract(DDLStatementParser.JsonPathExtractContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonPathExtractText}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonPathExtractText(DDLStatementParser.JsonPathExtractTextContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbContainRight}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbContainRight(DDLStatementParser.JsonbContainRightContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbContainLeft}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbContainLeft(DDLStatementParser.JsonbContainLeftContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbContainTopKey}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbContainTopKey(DDLStatementParser.JsonbContainTopKeyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbContainAnyTopKey}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbContainAnyTopKey(DDLStatementParser.JsonbContainAnyTopKeyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbContainAllTopKey}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbContainAllTopKey(DDLStatementParser.JsonbContainAllTopKeyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbConcat}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbConcat(DDLStatementParser.JsonbConcatContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbDelete}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbDelete(DDLStatementParser.JsonbDeleteContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbPathDelete}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbPathDelete(DDLStatementParser.JsonbPathDeleteContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbPathContainAnyValue}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbPathContainAnyValue(DDLStatementParser.JsonbPathContainAnyValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbPathPredicateCheck}
	 * labeled alternative in {@link DDLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbPathPredicateCheck(DDLStatementParser.JsonbPathPredicateCheckContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#qualAllOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualAllOp(DDLStatementParser.QualAllOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#ascDesc}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAscDesc(DDLStatementParser.AscDescContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#anyOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnyOperator(DDLStatementParser.AnyOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#frameClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrameClause(DDLStatementParser.FrameClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#frameExtent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrameExtent(DDLStatementParser.FrameExtentContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#frameBound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrameBound(DDLStatementParser.FrameBoundContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#windowExclusionClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowExclusionClause(DDLStatementParser.WindowExclusionClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#row}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRow(DDLStatementParser.RowContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#explicitRow}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplicitRow(DDLStatementParser.ExplicitRowContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#implicitRow}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImplicitRow(DDLStatementParser.ImplicitRowContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#subType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubType(DDLStatementParser.SubTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#arrayExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayExpr(DDLStatementParser.ArrayExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#arrayExprList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayExprList(DDLStatementParser.ArrayExprListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#funcArgList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncArgList(DDLStatementParser.FuncArgListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#paramName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParamName(DDLStatementParser.ParamNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#funcArgExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncArgExpr(DDLStatementParser.FuncArgExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#typeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeList(DDLStatementParser.TypeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#funcApplication}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncApplication(DDLStatementParser.FuncApplicationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#funcName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncName(DDLStatementParser.FuncNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#aexprConst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAexprConst(DDLStatementParser.AexprConstContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#qualifiedName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedName(DDLStatementParser.QualifiedNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#colId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColId(DDLStatementParser.ColIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#typeFunctionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeFunctionName(DDLStatementParser.TypeFunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#functionTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionTable(DDLStatementParser.FunctionTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#xmlTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlTable(DDLStatementParser.XmlTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#xmlTableColumnList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlTableColumnList(DDLStatementParser.XmlTableColumnListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#xmlTableColumnEl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlTableColumnEl(DDLStatementParser.XmlTableColumnElContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#xmlTableColumnOptionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlTableColumnOptionList(DDLStatementParser.XmlTableColumnOptionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#xmlTableColumnOptionEl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlTableColumnOptionEl(DDLStatementParser.XmlTableColumnOptionElContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#xmlNamespaceList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlNamespaceList(DDLStatementParser.XmlNamespaceListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#xmlNamespaceEl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlNamespaceEl(DDLStatementParser.XmlNamespaceElContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#funcExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncExpr(DDLStatementParser.FuncExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#withinGroupClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithinGroupClause(DDLStatementParser.WithinGroupClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#filterClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFilterClause(DDLStatementParser.FilterClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#functionExprWindowless}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionExprWindowless(DDLStatementParser.FunctionExprWindowlessContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#ordinality}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrdinality(DDLStatementParser.OrdinalityContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#functionExprCommonSubexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionExprCommonSubexpr(DDLStatementParser.FunctionExprCommonSubexprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#typeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeName(DDLStatementParser.TypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#simpleTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleTypeName(DDLStatementParser.SimpleTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#exprList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprList(DDLStatementParser.ExprListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#extractList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtractList(DDLStatementParser.ExtractListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#extractArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtractArg(DDLStatementParser.ExtractArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#genericType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericType(DDLStatementParser.GenericTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#typeModifiers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeModifiers(DDLStatementParser.TypeModifiersContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#numeric}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumeric(DDLStatementParser.NumericContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#constDatetime}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstDatetime(DDLStatementParser.ConstDatetimeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#timezone}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimezone(DDLStatementParser.TimezoneContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#character}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacter(DDLStatementParser.CharacterContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#characterWithLength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterWithLength(DDLStatementParser.CharacterWithLengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#characterWithoutLength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterWithoutLength(DDLStatementParser.CharacterWithoutLengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#characterClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterClause(DDLStatementParser.CharacterClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#optFloat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptFloat(DDLStatementParser.OptFloatContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#attrs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttrs(DDLStatementParser.AttrsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#attrName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttrName(DDLStatementParser.AttrNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#colLable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColLable(DDLStatementParser.ColLableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#bit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBit(DDLStatementParser.BitContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#bitWithLength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitWithLength(DDLStatementParser.BitWithLengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#bitWithoutLength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitWithoutLength(DDLStatementParser.BitWithoutLengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#constInterval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstInterval(DDLStatementParser.ConstIntervalContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#optInterval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptInterval(DDLStatementParser.OptIntervalContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#optArrayBounds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptArrayBounds(DDLStatementParser.OptArrayBoundsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#intervalSecond}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalSecond(DDLStatementParser.IntervalSecondContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#unicodeNormalForm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnicodeNormalForm(DDLStatementParser.UnicodeNormalFormContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#trimList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrimList(DDLStatementParser.TrimListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#overlayList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOverlayList(DDLStatementParser.OverlayListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#overlayPlacing}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOverlayPlacing(DDLStatementParser.OverlayPlacingContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#substrFrom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubstrFrom(DDLStatementParser.SubstrFromContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#substrFor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubstrFor(DDLStatementParser.SubstrForContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#positionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPositionList(DDLStatementParser.PositionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#substrList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubstrList(DDLStatementParser.SubstrListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#xmlAttributes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlAttributes(DDLStatementParser.XmlAttributesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#xmlAttributeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlAttributeList(DDLStatementParser.XmlAttributeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#xmlAttributeEl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlAttributeEl(DDLStatementParser.XmlAttributeElContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#xmlExistsArgument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlExistsArgument(DDLStatementParser.XmlExistsArgumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#xmlPassingMech}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlPassingMech(DDLStatementParser.XmlPassingMechContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#documentOrContent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocumentOrContent(DDLStatementParser.DocumentOrContentContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#xmlWhitespaceOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlWhitespaceOption(DDLStatementParser.XmlWhitespaceOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#xmlRootVersion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlRootVersion(DDLStatementParser.XmlRootVersionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#xmlRootStandalone}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlRootStandalone(DDLStatementParser.XmlRootStandaloneContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#rowsFromItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowsFromItem(DDLStatementParser.RowsFromItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#rowsFromList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowsFromList(DDLStatementParser.RowsFromListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#columnDefList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnDefList(DDLStatementParser.ColumnDefListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#tableFuncElementList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableFuncElementList(DDLStatementParser.TableFuncElementListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#tableFuncElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableFuncElement(DDLStatementParser.TableFuncElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#collateClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollateClause(DDLStatementParser.CollateClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#anyName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnyName(DDLStatementParser.AnyNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#aliasClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAliasClause(DDLStatementParser.AliasClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#nameList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNameList(DDLStatementParser.NameListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#funcAliasClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncAliasClause(DDLStatementParser.FuncAliasClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#tablesampleClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablesampleClause(DDLStatementParser.TablesampleClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#repeatableClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRepeatableClause(DDLStatementParser.RepeatableClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#allOrDistinct}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAllOrDistinct(DDLStatementParser.AllOrDistinctContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#sortClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSortClause(DDLStatementParser.SortClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#sortbyList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSortbyList(DDLStatementParser.SortbyListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#sortby}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSortby(DDLStatementParser.SortbyContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#nullsOrder}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullsOrder(DDLStatementParser.NullsOrderContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#distinctClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDistinctClause(DDLStatementParser.DistinctClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#distinct}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDistinct(DDLStatementParser.DistinctContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#overClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOverClause(DDLStatementParser.OverClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#windowSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowSpecification(DDLStatementParser.WindowSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#windowName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowName(DDLStatementParser.WindowNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#partitionClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionClause(DDLStatementParser.PartitionClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#indexParams}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexParams(DDLStatementParser.IndexParamsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#indexElemOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexElemOptions(DDLStatementParser.IndexElemOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#indexElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexElem(DDLStatementParser.IndexElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#collate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollate(DDLStatementParser.CollateContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#optClass}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptClass(DDLStatementParser.OptClassContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#reloptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReloptions(DDLStatementParser.ReloptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#reloptionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReloptionList(DDLStatementParser.ReloptionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#reloptionElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReloptionElem(DDLStatementParser.ReloptionElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#defArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefArg(DDLStatementParser.DefArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#funcType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncType(DDLStatementParser.FuncTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#selectWithParens}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectWithParens(DDLStatementParser.SelectWithParensContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataType(DDLStatementParser.DataTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dataTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataTypeName(DDLStatementParser.DataTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dataTypeLength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataTypeLength(DDLStatementParser.DataTypeLengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#characterSet}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterSet(DDLStatementParser.CharacterSetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#ignoredIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIgnoredIdentifier(DDLStatementParser.IgnoredIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#ignoredIdentifiers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIgnoredIdentifiers(DDLStatementParser.IgnoredIdentifiersContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#signedIconst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSignedIconst(DDLStatementParser.SignedIconstContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#booleanOrString}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanOrString(DDLStatementParser.BooleanOrStringContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#nonReservedWord}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonReservedWord(DDLStatementParser.NonReservedWordContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#colNameKeyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColNameKeyword(DDLStatementParser.ColNameKeywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#databaseName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDatabaseName(DDLStatementParser.DatabaseNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#roleSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoleSpec(DDLStatementParser.RoleSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#varName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarName(DDLStatementParser.VarNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#varList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarList(DDLStatementParser.VarListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#varValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarValue(DDLStatementParser.VarValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#zoneValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitZoneValue(DDLStatementParser.ZoneValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#numericOnly}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericOnly(DDLStatementParser.NumericOnlyContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#isoLevel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsoLevel(DDLStatementParser.IsoLevelContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#columnDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnDef(DDLStatementParser.ColumnDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#colConstraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColConstraint(DDLStatementParser.ColConstraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#constraintAttr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintAttr(DDLStatementParser.ConstraintAttrContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#colConstraintElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColConstraintElem(DDLStatementParser.ColConstraintElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#parenthesizedSeqOptList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenthesizedSeqOptList(DDLStatementParser.ParenthesizedSeqOptListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#seqOptList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSeqOptList(DDLStatementParser.SeqOptListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#seqOptElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSeqOptElem(DDLStatementParser.SeqOptElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#optColumnList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptColumnList(DDLStatementParser.OptColumnListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#generatedWhen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGeneratedWhen(DDLStatementParser.GeneratedWhenContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#noInherit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNoInherit(DDLStatementParser.NoInheritContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#consTableSpace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConsTableSpace(DDLStatementParser.ConsTableSpaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefinition(DDLStatementParser.DefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#defList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefList(DDLStatementParser.DefListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#defElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefElem(DDLStatementParser.DefElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#colLabel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColLabel(DDLStatementParser.ColLabelContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#keyActions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyActions(DDLStatementParser.KeyActionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#keyDelete}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyDelete(DDLStatementParser.KeyDeleteContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#keyUpdate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyUpdate(DDLStatementParser.KeyUpdateContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#keyAction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyAction(DDLStatementParser.KeyActionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#keyMatch}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyMatch(DDLStatementParser.KeyMatchContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#createGenericOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateGenericOptions(DDLStatementParser.CreateGenericOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#genericOptionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericOptionList(DDLStatementParser.GenericOptionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#replicaIdentity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReplicaIdentity(DDLStatementParser.ReplicaIdentityContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#operArgtypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperArgtypes(DDLStatementParser.OperArgtypesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#funcArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncArg(DDLStatementParser.FuncArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#argClass}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgClass(DDLStatementParser.ArgClassContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#funcArgsList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncArgsList(DDLStatementParser.FuncArgsListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#nonReservedWordOrSconst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonReservedWordOrSconst(DDLStatementParser.NonReservedWordOrSconstContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#fileName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFileName(DDLStatementParser.FileNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#roleList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoleList(DDLStatementParser.RoleListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#setResetClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetResetClause(DDLStatementParser.SetResetClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#setRest}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetRest(DDLStatementParser.SetRestContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#transactionModeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransactionModeList(DDLStatementParser.TransactionModeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#transactionModeItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransactionModeItem(DDLStatementParser.TransactionModeItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#setRestMore}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetRestMore(DDLStatementParser.SetRestMoreContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#encoding}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEncoding(DDLStatementParser.EncodingContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#genericSet}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericSet(DDLStatementParser.GenericSetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#variableResetStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableResetStmt(DDLStatementParser.VariableResetStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#resetRest}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResetRest(DDLStatementParser.ResetRestContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#genericReset}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericReset(DDLStatementParser.GenericResetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#relationExprList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationExprList(DDLStatementParser.RelationExprListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#relationExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationExpr(DDLStatementParser.RelationExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#commonFuncOptItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommonFuncOptItem(DDLStatementParser.CommonFuncOptItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#functionSetResetClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionSetResetClause(DDLStatementParser.FunctionSetResetClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#rowSecurityCmd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowSecurityCmd(DDLStatementParser.RowSecurityCmdContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#event}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEvent(DDLStatementParser.EventContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#typeNameList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeNameList(DDLStatementParser.TypeNameListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#notExistClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotExistClause(DDLStatementParser.NotExistClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#existClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExistClause(DDLStatementParser.ExistClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#insert}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert(DDLStatementParser.InsertContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#insertTarget}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertTarget(DDLStatementParser.InsertTargetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#insertRest}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertRest(DDLStatementParser.InsertRestContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#overrideKind}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOverrideKind(DDLStatementParser.OverrideKindContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#insertColumnList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertColumnList(DDLStatementParser.InsertColumnListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#insertColumnItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertColumnItem(DDLStatementParser.InsertColumnItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#optOnConflict}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptOnConflict(DDLStatementParser.OptOnConflictContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#optConfExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptConfExpr(DDLStatementParser.OptConfExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#update}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdate(DDLStatementParser.UpdateContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#setClauseList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetClauseList(DDLStatementParser.SetClauseListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#setClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetClause(DDLStatementParser.SetClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#setTarget}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetTarget(DDLStatementParser.SetTargetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#setTargetList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetTargetList(DDLStatementParser.SetTargetListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#returningClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturningClause(DDLStatementParser.ReturningClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#delete}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDelete(DDLStatementParser.DeleteContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#relationExprOptAlias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationExprOptAlias(DDLStatementParser.RelationExprOptAliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#usingClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUsingClause(DDLStatementParser.UsingClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#select}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect(DDLStatementParser.SelectContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#selectNoParens}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectNoParens(DDLStatementParser.SelectNoParensContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#selectClauseN}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectClauseN(DDLStatementParser.SelectClauseNContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#simpleSelect}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleSelect(DDLStatementParser.SimpleSelectContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#withClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithClause(DDLStatementParser.WithClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#intoClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntoClause(DDLStatementParser.IntoClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#optTempTableName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptTempTableName(DDLStatementParser.OptTempTableNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#cteList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCteList(DDLStatementParser.CteListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#commonTableExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommonTableExpr(DDLStatementParser.CommonTableExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#optMaterialized}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptMaterialized(DDLStatementParser.OptMaterializedContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#optNameList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptNameList(DDLStatementParser.OptNameListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#preparableStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPreparableStmt(DDLStatementParser.PreparableStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#forLockingClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForLockingClause(DDLStatementParser.ForLockingClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#forLockingItems}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForLockingItems(DDLStatementParser.ForLockingItemsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#forLockingItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForLockingItem(DDLStatementParser.ForLockingItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#nowaitOrSkip}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNowaitOrSkip(DDLStatementParser.NowaitOrSkipContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#forLockingStrength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForLockingStrength(DDLStatementParser.ForLockingStrengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#lockedRelsList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLockedRelsList(DDLStatementParser.LockedRelsListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#qualifiedNameList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedNameList(DDLStatementParser.QualifiedNameListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#selectLimit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectLimit(DDLStatementParser.SelectLimitContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#valuesClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValuesClause(DDLStatementParser.ValuesClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#limitClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimitClause(DDLStatementParser.LimitClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#offsetClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOffsetClause(DDLStatementParser.OffsetClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#selectLimitValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectLimitValue(DDLStatementParser.SelectLimitValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#selectOffsetValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectOffsetValue(DDLStatementParser.SelectOffsetValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#selectFetchFirstValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectFetchFirstValue(DDLStatementParser.SelectFetchFirstValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#rowOrRows}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowOrRows(DDLStatementParser.RowOrRowsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#firstOrNext}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFirstOrNext(DDLStatementParser.FirstOrNextContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#targetList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTargetList(DDLStatementParser.TargetListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#targetEl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTargetEl(DDLStatementParser.TargetElContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#groupClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupClause(DDLStatementParser.GroupClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#groupByList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupByList(DDLStatementParser.GroupByListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#groupByItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupByItem(DDLStatementParser.GroupByItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#emptyGroupingSet}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmptyGroupingSet(DDLStatementParser.EmptyGroupingSetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#rollupClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRollupClause(DDLStatementParser.RollupClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#cubeClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCubeClause(DDLStatementParser.CubeClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#groupingSetsClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupingSetsClause(DDLStatementParser.GroupingSetsClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#windowClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowClause(DDLStatementParser.WindowClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#windowDefinitionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowDefinitionList(DDLStatementParser.WindowDefinitionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#windowDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowDefinition(DDLStatementParser.WindowDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#existingWindowName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExistingWindowName(DDLStatementParser.ExistingWindowNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#optWindowExclusionClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptWindowExclusionClause(DDLStatementParser.OptWindowExclusionClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#fromClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromClause(DDLStatementParser.FromClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#fromList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromList(DDLStatementParser.FromListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#tableReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableReference(DDLStatementParser.TableReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#joinedTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinedTable(DDLStatementParser.JoinedTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#joinType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinType(DDLStatementParser.JoinTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#joinOuter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinOuter(DDLStatementParser.JoinOuterContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#joinQual}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinQual(DDLStatementParser.JoinQualContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#whereClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhereClause(DDLStatementParser.WhereClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#whereOrCurrentClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhereOrCurrentClause(DDLStatementParser.WhereOrCurrentClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#havingClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingClause(DDLStatementParser.HavingClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#doStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDoStatement(DDLStatementParser.DoStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dostmtOptList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDostmtOptList(DDLStatementParser.DostmtOptListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#dostmtOptItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDostmtOptItem(DDLStatementParser.DostmtOptItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#lock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLock(DDLStatementParser.LockContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#lockType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLockType(DDLStatementParser.LockTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#checkpoint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCheckpoint(DDLStatementParser.CheckpointContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#copy}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopy(DDLStatementParser.CopyContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#copyOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopyOptions(DDLStatementParser.CopyOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#copyGenericOptList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopyGenericOptList(DDLStatementParser.CopyGenericOptListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#copyGenericOptElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopyGenericOptElem(DDLStatementParser.CopyGenericOptElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#copyGenericOptArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopyGenericOptArg(DDLStatementParser.CopyGenericOptArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#copyGenericOptArgList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopyGenericOptArgList(DDLStatementParser.CopyGenericOptArgListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#copyGenericOptArgListItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopyGenericOptArgListItem(DDLStatementParser.CopyGenericOptArgListItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#copyOptList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopyOptList(DDLStatementParser.CopyOptListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#copyOptItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopyOptItem(DDLStatementParser.CopyOptItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#copyDelimiter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopyDelimiter(DDLStatementParser.CopyDelimiterContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#fetch}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetch(DDLStatementParser.FetchContext ctx);
	/**
	 * Visit a parse tree produced by {@link DDLStatementParser#fetchArgs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetchArgs(DDLStatementParser.FetchArgsContext ctx);
}