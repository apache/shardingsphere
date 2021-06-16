// Generated from /home/totalo/code/shardingsphere/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-postgresql/src/main/antlr4/imports/postgresql/DMLStatement.g4 by ANTLR 4.9.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DMLStatementParser}.
 */
public interface DMLStatementListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#insert}.
	 * @param ctx the parse tree
	 */
	void enterInsert(DMLStatementParser.InsertContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#insert}.
	 * @param ctx the parse tree
	 */
	void exitInsert(DMLStatementParser.InsertContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#insertTarget}.
	 * @param ctx the parse tree
	 */
	void enterInsertTarget(DMLStatementParser.InsertTargetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#insertTarget}.
	 * @param ctx the parse tree
	 */
	void exitInsertTarget(DMLStatementParser.InsertTargetContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#insertRest}.
	 * @param ctx the parse tree
	 */
	void enterInsertRest(DMLStatementParser.InsertRestContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#insertRest}.
	 * @param ctx the parse tree
	 */
	void exitInsertRest(DMLStatementParser.InsertRestContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#overrideKind}.
	 * @param ctx the parse tree
	 */
	void enterOverrideKind(DMLStatementParser.OverrideKindContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#overrideKind}.
	 * @param ctx the parse tree
	 */
	void exitOverrideKind(DMLStatementParser.OverrideKindContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#insertColumnList}.
	 * @param ctx the parse tree
	 */
	void enterInsertColumnList(DMLStatementParser.InsertColumnListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#insertColumnList}.
	 * @param ctx the parse tree
	 */
	void exitInsertColumnList(DMLStatementParser.InsertColumnListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#insertColumnItem}.
	 * @param ctx the parse tree
	 */
	void enterInsertColumnItem(DMLStatementParser.InsertColumnItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#insertColumnItem}.
	 * @param ctx the parse tree
	 */
	void exitInsertColumnItem(DMLStatementParser.InsertColumnItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#optOnConflict}.
	 * @param ctx the parse tree
	 */
	void enterOptOnConflict(DMLStatementParser.OptOnConflictContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#optOnConflict}.
	 * @param ctx the parse tree
	 */
	void exitOptOnConflict(DMLStatementParser.OptOnConflictContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#optConfExpr}.
	 * @param ctx the parse tree
	 */
	void enterOptConfExpr(DMLStatementParser.OptConfExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#optConfExpr}.
	 * @param ctx the parse tree
	 */
	void exitOptConfExpr(DMLStatementParser.OptConfExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#update}.
	 * @param ctx the parse tree
	 */
	void enterUpdate(DMLStatementParser.UpdateContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#update}.
	 * @param ctx the parse tree
	 */
	void exitUpdate(DMLStatementParser.UpdateContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#setClauseList}.
	 * @param ctx the parse tree
	 */
	void enterSetClauseList(DMLStatementParser.SetClauseListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#setClauseList}.
	 * @param ctx the parse tree
	 */
	void exitSetClauseList(DMLStatementParser.SetClauseListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#setClause}.
	 * @param ctx the parse tree
	 */
	void enterSetClause(DMLStatementParser.SetClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#setClause}.
	 * @param ctx the parse tree
	 */
	void exitSetClause(DMLStatementParser.SetClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#setTarget}.
	 * @param ctx the parse tree
	 */
	void enterSetTarget(DMLStatementParser.SetTargetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#setTarget}.
	 * @param ctx the parse tree
	 */
	void exitSetTarget(DMLStatementParser.SetTargetContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#setTargetList}.
	 * @param ctx the parse tree
	 */
	void enterSetTargetList(DMLStatementParser.SetTargetListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#setTargetList}.
	 * @param ctx the parse tree
	 */
	void exitSetTargetList(DMLStatementParser.SetTargetListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#returningClause}.
	 * @param ctx the parse tree
	 */
	void enterReturningClause(DMLStatementParser.ReturningClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#returningClause}.
	 * @param ctx the parse tree
	 */
	void exitReturningClause(DMLStatementParser.ReturningClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#delete}.
	 * @param ctx the parse tree
	 */
	void enterDelete(DMLStatementParser.DeleteContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#delete}.
	 * @param ctx the parse tree
	 */
	void exitDelete(DMLStatementParser.DeleteContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#relationExprOptAlias}.
	 * @param ctx the parse tree
	 */
	void enterRelationExprOptAlias(DMLStatementParser.RelationExprOptAliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#relationExprOptAlias}.
	 * @param ctx the parse tree
	 */
	void exitRelationExprOptAlias(DMLStatementParser.RelationExprOptAliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#usingClause}.
	 * @param ctx the parse tree
	 */
	void enterUsingClause(DMLStatementParser.UsingClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#usingClause}.
	 * @param ctx the parse tree
	 */
	void exitUsingClause(DMLStatementParser.UsingClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#select}.
	 * @param ctx the parse tree
	 */
	void enterSelect(DMLStatementParser.SelectContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#select}.
	 * @param ctx the parse tree
	 */
	void exitSelect(DMLStatementParser.SelectContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#selectWithParens}.
	 * @param ctx the parse tree
	 */
	void enterSelectWithParens(DMLStatementParser.SelectWithParensContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#selectWithParens}.
	 * @param ctx the parse tree
	 */
	void exitSelectWithParens(DMLStatementParser.SelectWithParensContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#selectNoParens}.
	 * @param ctx the parse tree
	 */
	void enterSelectNoParens(DMLStatementParser.SelectNoParensContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#selectNoParens}.
	 * @param ctx the parse tree
	 */
	void exitSelectNoParens(DMLStatementParser.SelectNoParensContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#selectClauseN}.
	 * @param ctx the parse tree
	 */
	void enterSelectClauseN(DMLStatementParser.SelectClauseNContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#selectClauseN}.
	 * @param ctx the parse tree
	 */
	void exitSelectClauseN(DMLStatementParser.SelectClauseNContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#simpleSelect}.
	 * @param ctx the parse tree
	 */
	void enterSimpleSelect(DMLStatementParser.SimpleSelectContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#simpleSelect}.
	 * @param ctx the parse tree
	 */
	void exitSimpleSelect(DMLStatementParser.SimpleSelectContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#withClause}.
	 * @param ctx the parse tree
	 */
	void enterWithClause(DMLStatementParser.WithClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#withClause}.
	 * @param ctx the parse tree
	 */
	void exitWithClause(DMLStatementParser.WithClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#intoClause}.
	 * @param ctx the parse tree
	 */
	void enterIntoClause(DMLStatementParser.IntoClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#intoClause}.
	 * @param ctx the parse tree
	 */
	void exitIntoClause(DMLStatementParser.IntoClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#optTempTableName}.
	 * @param ctx the parse tree
	 */
	void enterOptTempTableName(DMLStatementParser.OptTempTableNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#optTempTableName}.
	 * @param ctx the parse tree
	 */
	void exitOptTempTableName(DMLStatementParser.OptTempTableNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#cteList}.
	 * @param ctx the parse tree
	 */
	void enterCteList(DMLStatementParser.CteListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#cteList}.
	 * @param ctx the parse tree
	 */
	void exitCteList(DMLStatementParser.CteListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#commonTableExpr}.
	 * @param ctx the parse tree
	 */
	void enterCommonTableExpr(DMLStatementParser.CommonTableExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#commonTableExpr}.
	 * @param ctx the parse tree
	 */
	void exitCommonTableExpr(DMLStatementParser.CommonTableExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#optMaterialized}.
	 * @param ctx the parse tree
	 */
	void enterOptMaterialized(DMLStatementParser.OptMaterializedContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#optMaterialized}.
	 * @param ctx the parse tree
	 */
	void exitOptMaterialized(DMLStatementParser.OptMaterializedContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#optNameList}.
	 * @param ctx the parse tree
	 */
	void enterOptNameList(DMLStatementParser.OptNameListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#optNameList}.
	 * @param ctx the parse tree
	 */
	void exitOptNameList(DMLStatementParser.OptNameListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#preparableStmt}.
	 * @param ctx the parse tree
	 */
	void enterPreparableStmt(DMLStatementParser.PreparableStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#preparableStmt}.
	 * @param ctx the parse tree
	 */
	void exitPreparableStmt(DMLStatementParser.PreparableStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#forLockingClause}.
	 * @param ctx the parse tree
	 */
	void enterForLockingClause(DMLStatementParser.ForLockingClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#forLockingClause}.
	 * @param ctx the parse tree
	 */
	void exitForLockingClause(DMLStatementParser.ForLockingClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#forLockingItems}.
	 * @param ctx the parse tree
	 */
	void enterForLockingItems(DMLStatementParser.ForLockingItemsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#forLockingItems}.
	 * @param ctx the parse tree
	 */
	void exitForLockingItems(DMLStatementParser.ForLockingItemsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#forLockingItem}.
	 * @param ctx the parse tree
	 */
	void enterForLockingItem(DMLStatementParser.ForLockingItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#forLockingItem}.
	 * @param ctx the parse tree
	 */
	void exitForLockingItem(DMLStatementParser.ForLockingItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#nowaitOrSkip}.
	 * @param ctx the parse tree
	 */
	void enterNowaitOrSkip(DMLStatementParser.NowaitOrSkipContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#nowaitOrSkip}.
	 * @param ctx the parse tree
	 */
	void exitNowaitOrSkip(DMLStatementParser.NowaitOrSkipContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#forLockingStrength}.
	 * @param ctx the parse tree
	 */
	void enterForLockingStrength(DMLStatementParser.ForLockingStrengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#forLockingStrength}.
	 * @param ctx the parse tree
	 */
	void exitForLockingStrength(DMLStatementParser.ForLockingStrengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#lockedRelsList}.
	 * @param ctx the parse tree
	 */
	void enterLockedRelsList(DMLStatementParser.LockedRelsListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#lockedRelsList}.
	 * @param ctx the parse tree
	 */
	void exitLockedRelsList(DMLStatementParser.LockedRelsListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#qualifiedNameList}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedNameList(DMLStatementParser.QualifiedNameListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#qualifiedNameList}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedNameList(DMLStatementParser.QualifiedNameListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedName(DMLStatementParser.QualifiedNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedName(DMLStatementParser.QualifiedNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#selectLimit}.
	 * @param ctx the parse tree
	 */
	void enterSelectLimit(DMLStatementParser.SelectLimitContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#selectLimit}.
	 * @param ctx the parse tree
	 */
	void exitSelectLimit(DMLStatementParser.SelectLimitContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#valuesClause}.
	 * @param ctx the parse tree
	 */
	void enterValuesClause(DMLStatementParser.ValuesClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#valuesClause}.
	 * @param ctx the parse tree
	 */
	void exitValuesClause(DMLStatementParser.ValuesClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#limitClause}.
	 * @param ctx the parse tree
	 */
	void enterLimitClause(DMLStatementParser.LimitClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#limitClause}.
	 * @param ctx the parse tree
	 */
	void exitLimitClause(DMLStatementParser.LimitClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#offsetClause}.
	 * @param ctx the parse tree
	 */
	void enterOffsetClause(DMLStatementParser.OffsetClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#offsetClause}.
	 * @param ctx the parse tree
	 */
	void exitOffsetClause(DMLStatementParser.OffsetClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#selectLimitValue}.
	 * @param ctx the parse tree
	 */
	void enterSelectLimitValue(DMLStatementParser.SelectLimitValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#selectLimitValue}.
	 * @param ctx the parse tree
	 */
	void exitSelectLimitValue(DMLStatementParser.SelectLimitValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#selectOffsetValue}.
	 * @param ctx the parse tree
	 */
	void enterSelectOffsetValue(DMLStatementParser.SelectOffsetValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#selectOffsetValue}.
	 * @param ctx the parse tree
	 */
	void exitSelectOffsetValue(DMLStatementParser.SelectOffsetValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#selectFetchFirstValue}.
	 * @param ctx the parse tree
	 */
	void enterSelectFetchFirstValue(DMLStatementParser.SelectFetchFirstValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#selectFetchFirstValue}.
	 * @param ctx the parse tree
	 */
	void exitSelectFetchFirstValue(DMLStatementParser.SelectFetchFirstValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#rowOrRows}.
	 * @param ctx the parse tree
	 */
	void enterRowOrRows(DMLStatementParser.RowOrRowsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#rowOrRows}.
	 * @param ctx the parse tree
	 */
	void exitRowOrRows(DMLStatementParser.RowOrRowsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#firstOrNext}.
	 * @param ctx the parse tree
	 */
	void enterFirstOrNext(DMLStatementParser.FirstOrNextContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#firstOrNext}.
	 * @param ctx the parse tree
	 */
	void exitFirstOrNext(DMLStatementParser.FirstOrNextContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#targetList}.
	 * @param ctx the parse tree
	 */
	void enterTargetList(DMLStatementParser.TargetListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#targetList}.
	 * @param ctx the parse tree
	 */
	void exitTargetList(DMLStatementParser.TargetListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#targetEl}.
	 * @param ctx the parse tree
	 */
	void enterTargetEl(DMLStatementParser.TargetElContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#targetEl}.
	 * @param ctx the parse tree
	 */
	void exitTargetEl(DMLStatementParser.TargetElContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#groupClause}.
	 * @param ctx the parse tree
	 */
	void enterGroupClause(DMLStatementParser.GroupClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#groupClause}.
	 * @param ctx the parse tree
	 */
	void exitGroupClause(DMLStatementParser.GroupClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#groupByList}.
	 * @param ctx the parse tree
	 */
	void enterGroupByList(DMLStatementParser.GroupByListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#groupByList}.
	 * @param ctx the parse tree
	 */
	void exitGroupByList(DMLStatementParser.GroupByListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#groupByItem}.
	 * @param ctx the parse tree
	 */
	void enterGroupByItem(DMLStatementParser.GroupByItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#groupByItem}.
	 * @param ctx the parse tree
	 */
	void exitGroupByItem(DMLStatementParser.GroupByItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#emptyGroupingSet}.
	 * @param ctx the parse tree
	 */
	void enterEmptyGroupingSet(DMLStatementParser.EmptyGroupingSetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#emptyGroupingSet}.
	 * @param ctx the parse tree
	 */
	void exitEmptyGroupingSet(DMLStatementParser.EmptyGroupingSetContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#rollupClause}.
	 * @param ctx the parse tree
	 */
	void enterRollupClause(DMLStatementParser.RollupClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#rollupClause}.
	 * @param ctx the parse tree
	 */
	void exitRollupClause(DMLStatementParser.RollupClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#cubeClause}.
	 * @param ctx the parse tree
	 */
	void enterCubeClause(DMLStatementParser.CubeClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#cubeClause}.
	 * @param ctx the parse tree
	 */
	void exitCubeClause(DMLStatementParser.CubeClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#groupingSetsClause}.
	 * @param ctx the parse tree
	 */
	void enterGroupingSetsClause(DMLStatementParser.GroupingSetsClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#groupingSetsClause}.
	 * @param ctx the parse tree
	 */
	void exitGroupingSetsClause(DMLStatementParser.GroupingSetsClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#windowClause}.
	 * @param ctx the parse tree
	 */
	void enterWindowClause(DMLStatementParser.WindowClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#windowClause}.
	 * @param ctx the parse tree
	 */
	void exitWindowClause(DMLStatementParser.WindowClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#windowDefinitionList}.
	 * @param ctx the parse tree
	 */
	void enterWindowDefinitionList(DMLStatementParser.WindowDefinitionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#windowDefinitionList}.
	 * @param ctx the parse tree
	 */
	void exitWindowDefinitionList(DMLStatementParser.WindowDefinitionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#windowDefinition}.
	 * @param ctx the parse tree
	 */
	void enterWindowDefinition(DMLStatementParser.WindowDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#windowDefinition}.
	 * @param ctx the parse tree
	 */
	void exitWindowDefinition(DMLStatementParser.WindowDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#windowSpecification}.
	 * @param ctx the parse tree
	 */
	void enterWindowSpecification(DMLStatementParser.WindowSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#windowSpecification}.
	 * @param ctx the parse tree
	 */
	void exitWindowSpecification(DMLStatementParser.WindowSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#existingWindowName}.
	 * @param ctx the parse tree
	 */
	void enterExistingWindowName(DMLStatementParser.ExistingWindowNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#existingWindowName}.
	 * @param ctx the parse tree
	 */
	void exitExistingWindowName(DMLStatementParser.ExistingWindowNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#partitionClause}.
	 * @param ctx the parse tree
	 */
	void enterPartitionClause(DMLStatementParser.PartitionClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#partitionClause}.
	 * @param ctx the parse tree
	 */
	void exitPartitionClause(DMLStatementParser.PartitionClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#frameClause}.
	 * @param ctx the parse tree
	 */
	void enterFrameClause(DMLStatementParser.FrameClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#frameClause}.
	 * @param ctx the parse tree
	 */
	void exitFrameClause(DMLStatementParser.FrameClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#frameExtent}.
	 * @param ctx the parse tree
	 */
	void enterFrameExtent(DMLStatementParser.FrameExtentContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#frameExtent}.
	 * @param ctx the parse tree
	 */
	void exitFrameExtent(DMLStatementParser.FrameExtentContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#frameBound}.
	 * @param ctx the parse tree
	 */
	void enterFrameBound(DMLStatementParser.FrameBoundContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#frameBound}.
	 * @param ctx the parse tree
	 */
	void exitFrameBound(DMLStatementParser.FrameBoundContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#optWindowExclusionClause}.
	 * @param ctx the parse tree
	 */
	void enterOptWindowExclusionClause(DMLStatementParser.OptWindowExclusionClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#optWindowExclusionClause}.
	 * @param ctx the parse tree
	 */
	void exitOptWindowExclusionClause(DMLStatementParser.OptWindowExclusionClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#alias}.
	 * @param ctx the parse tree
	 */
	void enterAlias(DMLStatementParser.AliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#alias}.
	 * @param ctx the parse tree
	 */
	void exitAlias(DMLStatementParser.AliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void enterFromClause(DMLStatementParser.FromClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void exitFromClause(DMLStatementParser.FromClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#fromList}.
	 * @param ctx the parse tree
	 */
	void enterFromList(DMLStatementParser.FromListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#fromList}.
	 * @param ctx the parse tree
	 */
	void exitFromList(DMLStatementParser.FromListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#tableReference}.
	 * @param ctx the parse tree
	 */
	void enterTableReference(DMLStatementParser.TableReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#tableReference}.
	 * @param ctx the parse tree
	 */
	void exitTableReference(DMLStatementParser.TableReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#joinedTable}.
	 * @param ctx the parse tree
	 */
	void enterJoinedTable(DMLStatementParser.JoinedTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#joinedTable}.
	 * @param ctx the parse tree
	 */
	void exitJoinedTable(DMLStatementParser.JoinedTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#joinType}.
	 * @param ctx the parse tree
	 */
	void enterJoinType(DMLStatementParser.JoinTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#joinType}.
	 * @param ctx the parse tree
	 */
	void exitJoinType(DMLStatementParser.JoinTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#joinOuter}.
	 * @param ctx the parse tree
	 */
	void enterJoinOuter(DMLStatementParser.JoinOuterContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#joinOuter}.
	 * @param ctx the parse tree
	 */
	void exitJoinOuter(DMLStatementParser.JoinOuterContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#joinQual}.
	 * @param ctx the parse tree
	 */
	void enterJoinQual(DMLStatementParser.JoinQualContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#joinQual}.
	 * @param ctx the parse tree
	 */
	void exitJoinQual(DMLStatementParser.JoinQualContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#relationExpr}.
	 * @param ctx the parse tree
	 */
	void enterRelationExpr(DMLStatementParser.RelationExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#relationExpr}.
	 * @param ctx the parse tree
	 */
	void exitRelationExpr(DMLStatementParser.RelationExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void enterWhereClause(DMLStatementParser.WhereClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void exitWhereClause(DMLStatementParser.WhereClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#whereOrCurrentClause}.
	 * @param ctx the parse tree
	 */
	void enterWhereOrCurrentClause(DMLStatementParser.WhereOrCurrentClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#whereOrCurrentClause}.
	 * @param ctx the parse tree
	 */
	void exitWhereOrCurrentClause(DMLStatementParser.WhereOrCurrentClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#havingClause}.
	 * @param ctx the parse tree
	 */
	void enterHavingClause(DMLStatementParser.HavingClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#havingClause}.
	 * @param ctx the parse tree
	 */
	void exitHavingClause(DMLStatementParser.HavingClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#doStatement}.
	 * @param ctx the parse tree
	 */
	void enterDoStatement(DMLStatementParser.DoStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#doStatement}.
	 * @param ctx the parse tree
	 */
	void exitDoStatement(DMLStatementParser.DoStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#dostmtOptList}.
	 * @param ctx the parse tree
	 */
	void enterDostmtOptList(DMLStatementParser.DostmtOptListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#dostmtOptList}.
	 * @param ctx the parse tree
	 */
	void exitDostmtOptList(DMLStatementParser.DostmtOptListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#dostmtOptItem}.
	 * @param ctx the parse tree
	 */
	void enterDostmtOptItem(DMLStatementParser.DostmtOptItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#dostmtOptItem}.
	 * @param ctx the parse tree
	 */
	void exitDostmtOptItem(DMLStatementParser.DostmtOptItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#lock}.
	 * @param ctx the parse tree
	 */
	void enterLock(DMLStatementParser.LockContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#lock}.
	 * @param ctx the parse tree
	 */
	void exitLock(DMLStatementParser.LockContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#lockType}.
	 * @param ctx the parse tree
	 */
	void enterLockType(DMLStatementParser.LockTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#lockType}.
	 * @param ctx the parse tree
	 */
	void exitLockType(DMLStatementParser.LockTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#checkpoint}.
	 * @param ctx the parse tree
	 */
	void enterCheckpoint(DMLStatementParser.CheckpointContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#checkpoint}.
	 * @param ctx the parse tree
	 */
	void exitCheckpoint(DMLStatementParser.CheckpointContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#copy}.
	 * @param ctx the parse tree
	 */
	void enterCopy(DMLStatementParser.CopyContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#copy}.
	 * @param ctx the parse tree
	 */
	void exitCopy(DMLStatementParser.CopyContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#copyOptions}.
	 * @param ctx the parse tree
	 */
	void enterCopyOptions(DMLStatementParser.CopyOptionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#copyOptions}.
	 * @param ctx the parse tree
	 */
	void exitCopyOptions(DMLStatementParser.CopyOptionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#copyGenericOptList}.
	 * @param ctx the parse tree
	 */
	void enterCopyGenericOptList(DMLStatementParser.CopyGenericOptListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#copyGenericOptList}.
	 * @param ctx the parse tree
	 */
	void exitCopyGenericOptList(DMLStatementParser.CopyGenericOptListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#copyGenericOptElem}.
	 * @param ctx the parse tree
	 */
	void enterCopyGenericOptElem(DMLStatementParser.CopyGenericOptElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#copyGenericOptElem}.
	 * @param ctx the parse tree
	 */
	void exitCopyGenericOptElem(DMLStatementParser.CopyGenericOptElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#copyGenericOptArg}.
	 * @param ctx the parse tree
	 */
	void enterCopyGenericOptArg(DMLStatementParser.CopyGenericOptArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#copyGenericOptArg}.
	 * @param ctx the parse tree
	 */
	void exitCopyGenericOptArg(DMLStatementParser.CopyGenericOptArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#copyGenericOptArgList}.
	 * @param ctx the parse tree
	 */
	void enterCopyGenericOptArgList(DMLStatementParser.CopyGenericOptArgListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#copyGenericOptArgList}.
	 * @param ctx the parse tree
	 */
	void exitCopyGenericOptArgList(DMLStatementParser.CopyGenericOptArgListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#copyGenericOptArgListItem}.
	 * @param ctx the parse tree
	 */
	void enterCopyGenericOptArgListItem(DMLStatementParser.CopyGenericOptArgListItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#copyGenericOptArgListItem}.
	 * @param ctx the parse tree
	 */
	void exitCopyGenericOptArgListItem(DMLStatementParser.CopyGenericOptArgListItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#copyOptList}.
	 * @param ctx the parse tree
	 */
	void enterCopyOptList(DMLStatementParser.CopyOptListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#copyOptList}.
	 * @param ctx the parse tree
	 */
	void exitCopyOptList(DMLStatementParser.CopyOptListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#copyOptItem}.
	 * @param ctx the parse tree
	 */
	void enterCopyOptItem(DMLStatementParser.CopyOptItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#copyOptItem}.
	 * @param ctx the parse tree
	 */
	void exitCopyOptItem(DMLStatementParser.CopyOptItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#copyDelimiter}.
	 * @param ctx the parse tree
	 */
	void enterCopyDelimiter(DMLStatementParser.CopyDelimiterContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#copyDelimiter}.
	 * @param ctx the parse tree
	 */
	void exitCopyDelimiter(DMLStatementParser.CopyDelimiterContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#fetch}.
	 * @param ctx the parse tree
	 */
	void enterFetch(DMLStatementParser.FetchContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#fetch}.
	 * @param ctx the parse tree
	 */
	void exitFetch(DMLStatementParser.FetchContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#fetchArgs}.
	 * @param ctx the parse tree
	 */
	void enterFetchArgs(DMLStatementParser.FetchArgsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#fetchArgs}.
	 * @param ctx the parse tree
	 */
	void exitFetchArgs(DMLStatementParser.FetchArgsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#parameterMarker}.
	 * @param ctx the parse tree
	 */
	void enterParameterMarker(DMLStatementParser.ParameterMarkerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#parameterMarker}.
	 * @param ctx the parse tree
	 */
	void exitParameterMarker(DMLStatementParser.ParameterMarkerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#reservedKeyword}.
	 * @param ctx the parse tree
	 */
	void enterReservedKeyword(DMLStatementParser.ReservedKeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#reservedKeyword}.
	 * @param ctx the parse tree
	 */
	void exitReservedKeyword(DMLStatementParser.ReservedKeywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#numberLiterals}.
	 * @param ctx the parse tree
	 */
	void enterNumberLiterals(DMLStatementParser.NumberLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#numberLiterals}.
	 * @param ctx the parse tree
	 */
	void exitNumberLiterals(DMLStatementParser.NumberLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#literalsType}.
	 * @param ctx the parse tree
	 */
	void enterLiteralsType(DMLStatementParser.LiteralsTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#literalsType}.
	 * @param ctx the parse tree
	 */
	void exitLiteralsType(DMLStatementParser.LiteralsTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(DMLStatementParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(DMLStatementParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#unicodeEscapes}.
	 * @param ctx the parse tree
	 */
	void enterUnicodeEscapes(DMLStatementParser.UnicodeEscapesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#unicodeEscapes}.
	 * @param ctx the parse tree
	 */
	void exitUnicodeEscapes(DMLStatementParser.UnicodeEscapesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#uescape}.
	 * @param ctx the parse tree
	 */
	void enterUescape(DMLStatementParser.UescapeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#uescape}.
	 * @param ctx the parse tree
	 */
	void exitUescape(DMLStatementParser.UescapeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#unreservedWord}.
	 * @param ctx the parse tree
	 */
	void enterUnreservedWord(DMLStatementParser.UnreservedWordContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#unreservedWord}.
	 * @param ctx the parse tree
	 */
	void exitUnreservedWord(DMLStatementParser.UnreservedWordContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#typeFuncNameKeyword}.
	 * @param ctx the parse tree
	 */
	void enterTypeFuncNameKeyword(DMLStatementParser.TypeFuncNameKeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#typeFuncNameKeyword}.
	 * @param ctx the parse tree
	 */
	void exitTypeFuncNameKeyword(DMLStatementParser.TypeFuncNameKeywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#schemaName}.
	 * @param ctx the parse tree
	 */
	void enterSchemaName(DMLStatementParser.SchemaNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#schemaName}.
	 * @param ctx the parse tree
	 */
	void exitSchemaName(DMLStatementParser.SchemaNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#tableName}.
	 * @param ctx the parse tree
	 */
	void enterTableName(DMLStatementParser.TableNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#tableName}.
	 * @param ctx the parse tree
	 */
	void exitTableName(DMLStatementParser.TableNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#columnName}.
	 * @param ctx the parse tree
	 */
	void enterColumnName(DMLStatementParser.ColumnNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#columnName}.
	 * @param ctx the parse tree
	 */
	void exitColumnName(DMLStatementParser.ColumnNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#owner}.
	 * @param ctx the parse tree
	 */
	void enterOwner(DMLStatementParser.OwnerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#owner}.
	 * @param ctx the parse tree
	 */
	void exitOwner(DMLStatementParser.OwnerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#name}.
	 * @param ctx the parse tree
	 */
	void enterName(DMLStatementParser.NameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#name}.
	 * @param ctx the parse tree
	 */
	void exitName(DMLStatementParser.NameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#tableNames}.
	 * @param ctx the parse tree
	 */
	void enterTableNames(DMLStatementParser.TableNamesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#tableNames}.
	 * @param ctx the parse tree
	 */
	void exitTableNames(DMLStatementParser.TableNamesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#columnNames}.
	 * @param ctx the parse tree
	 */
	void enterColumnNames(DMLStatementParser.ColumnNamesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#columnNames}.
	 * @param ctx the parse tree
	 */
	void exitColumnNames(DMLStatementParser.ColumnNamesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#collationName}.
	 * @param ctx the parse tree
	 */
	void enterCollationName(DMLStatementParser.CollationNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#collationName}.
	 * @param ctx the parse tree
	 */
	void exitCollationName(DMLStatementParser.CollationNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#indexName}.
	 * @param ctx the parse tree
	 */
	void enterIndexName(DMLStatementParser.IndexNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#indexName}.
	 * @param ctx the parse tree
	 */
	void exitIndexName(DMLStatementParser.IndexNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#primaryKey}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryKey(DMLStatementParser.PrimaryKeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#primaryKey}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryKey(DMLStatementParser.PrimaryKeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#logicalOperator}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOperator(DMLStatementParser.LogicalOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#logicalOperator}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOperator(DMLStatementParser.LogicalOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOperator(DMLStatementParser.ComparisonOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOperator(DMLStatementParser.ComparisonOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#patternMatchingOperator}.
	 * @param ctx the parse tree
	 */
	void enterPatternMatchingOperator(DMLStatementParser.PatternMatchingOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#patternMatchingOperator}.
	 * @param ctx the parse tree
	 */
	void exitPatternMatchingOperator(DMLStatementParser.PatternMatchingOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#cursorName}.
	 * @param ctx the parse tree
	 */
	void enterCursorName(DMLStatementParser.CursorNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#cursorName}.
	 * @param ctx the parse tree
	 */
	void exitCursorName(DMLStatementParser.CursorNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#aExpr}.
	 * @param ctx the parse tree
	 */
	void enterAExpr(DMLStatementParser.AExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#aExpr}.
	 * @param ctx the parse tree
	 */
	void exitAExpr(DMLStatementParser.AExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#bExpr}.
	 * @param ctx the parse tree
	 */
	void enterBExpr(DMLStatementParser.BExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#bExpr}.
	 * @param ctx the parse tree
	 */
	void exitBExpr(DMLStatementParser.BExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#cExpr}.
	 * @param ctx the parse tree
	 */
	void enterCExpr(DMLStatementParser.CExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#cExpr}.
	 * @param ctx the parse tree
	 */
	void exitCExpr(DMLStatementParser.CExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#indirection}.
	 * @param ctx the parse tree
	 */
	void enterIndirection(DMLStatementParser.IndirectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#indirection}.
	 * @param ctx the parse tree
	 */
	void exitIndirection(DMLStatementParser.IndirectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#optIndirection}.
	 * @param ctx the parse tree
	 */
	void enterOptIndirection(DMLStatementParser.OptIndirectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#optIndirection}.
	 * @param ctx the parse tree
	 */
	void exitOptIndirection(DMLStatementParser.OptIndirectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#indirectionEl}.
	 * @param ctx the parse tree
	 */
	void enterIndirectionEl(DMLStatementParser.IndirectionElContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#indirectionEl}.
	 * @param ctx the parse tree
	 */
	void exitIndirectionEl(DMLStatementParser.IndirectionElContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#sliceBound}.
	 * @param ctx the parse tree
	 */
	void enterSliceBound(DMLStatementParser.SliceBoundContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#sliceBound}.
	 * @param ctx the parse tree
	 */
	void exitSliceBound(DMLStatementParser.SliceBoundContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#inExpr}.
	 * @param ctx the parse tree
	 */
	void enterInExpr(DMLStatementParser.InExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#inExpr}.
	 * @param ctx the parse tree
	 */
	void exitInExpr(DMLStatementParser.InExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#caseExpr}.
	 * @param ctx the parse tree
	 */
	void enterCaseExpr(DMLStatementParser.CaseExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#caseExpr}.
	 * @param ctx the parse tree
	 */
	void exitCaseExpr(DMLStatementParser.CaseExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#whenClauseList}.
	 * @param ctx the parse tree
	 */
	void enterWhenClauseList(DMLStatementParser.WhenClauseListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#whenClauseList}.
	 * @param ctx the parse tree
	 */
	void exitWhenClauseList(DMLStatementParser.WhenClauseListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#whenClause}.
	 * @param ctx the parse tree
	 */
	void enterWhenClause(DMLStatementParser.WhenClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#whenClause}.
	 * @param ctx the parse tree
	 */
	void exitWhenClause(DMLStatementParser.WhenClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#caseDefault}.
	 * @param ctx the parse tree
	 */
	void enterCaseDefault(DMLStatementParser.CaseDefaultContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#caseDefault}.
	 * @param ctx the parse tree
	 */
	void exitCaseDefault(DMLStatementParser.CaseDefaultContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#caseArg}.
	 * @param ctx the parse tree
	 */
	void enterCaseArg(DMLStatementParser.CaseArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#caseArg}.
	 * @param ctx the parse tree
	 */
	void exitCaseArg(DMLStatementParser.CaseArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#columnref}.
	 * @param ctx the parse tree
	 */
	void enterColumnref(DMLStatementParser.ColumnrefContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#columnref}.
	 * @param ctx the parse tree
	 */
	void exitColumnref(DMLStatementParser.ColumnrefContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#qualOp}.
	 * @param ctx the parse tree
	 */
	void enterQualOp(DMLStatementParser.QualOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#qualOp}.
	 * @param ctx the parse tree
	 */
	void exitQualOp(DMLStatementParser.QualOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#subqueryOp}.
	 * @param ctx the parse tree
	 */
	void enterSubqueryOp(DMLStatementParser.SubqueryOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#subqueryOp}.
	 * @param ctx the parse tree
	 */
	void exitSubqueryOp(DMLStatementParser.SubqueryOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#allOp}.
	 * @param ctx the parse tree
	 */
	void enterAllOp(DMLStatementParser.AllOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#allOp}.
	 * @param ctx the parse tree
	 */
	void exitAllOp(DMLStatementParser.AllOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#op}.
	 * @param ctx the parse tree
	 */
	void enterOp(DMLStatementParser.OpContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#op}.
	 * @param ctx the parse tree
	 */
	void exitOp(DMLStatementParser.OpContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#mathOperator}.
	 * @param ctx the parse tree
	 */
	void enterMathOperator(DMLStatementParser.MathOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#mathOperator}.
	 * @param ctx the parse tree
	 */
	void exitMathOperator(DMLStatementParser.MathOperatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonExtract}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonExtract(DMLStatementParser.JsonExtractContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonExtract}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonExtract(DMLStatementParser.JsonExtractContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonExtractText}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonExtractText(DMLStatementParser.JsonExtractTextContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonExtractText}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonExtractText(DMLStatementParser.JsonExtractTextContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonPathExtract}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonPathExtract(DMLStatementParser.JsonPathExtractContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonPathExtract}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonPathExtract(DMLStatementParser.JsonPathExtractContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonPathExtractText}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonPathExtractText(DMLStatementParser.JsonPathExtractTextContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonPathExtractText}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonPathExtractText(DMLStatementParser.JsonPathExtractTextContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbContainRight}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbContainRight(DMLStatementParser.JsonbContainRightContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbContainRight}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbContainRight(DMLStatementParser.JsonbContainRightContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbContainLeft}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbContainLeft(DMLStatementParser.JsonbContainLeftContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbContainLeft}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbContainLeft(DMLStatementParser.JsonbContainLeftContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbContainTopKey}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbContainTopKey(DMLStatementParser.JsonbContainTopKeyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbContainTopKey}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbContainTopKey(DMLStatementParser.JsonbContainTopKeyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbContainAnyTopKey}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbContainAnyTopKey(DMLStatementParser.JsonbContainAnyTopKeyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbContainAnyTopKey}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbContainAnyTopKey(DMLStatementParser.JsonbContainAnyTopKeyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbContainAllTopKey}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbContainAllTopKey(DMLStatementParser.JsonbContainAllTopKeyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbContainAllTopKey}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbContainAllTopKey(DMLStatementParser.JsonbContainAllTopKeyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbConcat}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbConcat(DMLStatementParser.JsonbConcatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbConcat}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbConcat(DMLStatementParser.JsonbConcatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbDelete}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbDelete(DMLStatementParser.JsonbDeleteContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbDelete}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbDelete(DMLStatementParser.JsonbDeleteContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbPathDelete}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbPathDelete(DMLStatementParser.JsonbPathDeleteContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbPathDelete}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbPathDelete(DMLStatementParser.JsonbPathDeleteContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbPathContainAnyValue}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbPathContainAnyValue(DMLStatementParser.JsonbPathContainAnyValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbPathContainAnyValue}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbPathContainAnyValue(DMLStatementParser.JsonbPathContainAnyValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code jsonbPathPredicateCheck}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void enterJsonbPathPredicateCheck(DMLStatementParser.JsonbPathPredicateCheckContext ctx);
	/**
	 * Exit a parse tree produced by the {@code jsonbPathPredicateCheck}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 */
	void exitJsonbPathPredicateCheck(DMLStatementParser.JsonbPathPredicateCheckContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#qualAllOp}.
	 * @param ctx the parse tree
	 */
	void enterQualAllOp(DMLStatementParser.QualAllOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#qualAllOp}.
	 * @param ctx the parse tree
	 */
	void exitQualAllOp(DMLStatementParser.QualAllOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#ascDesc}.
	 * @param ctx the parse tree
	 */
	void enterAscDesc(DMLStatementParser.AscDescContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#ascDesc}.
	 * @param ctx the parse tree
	 */
	void exitAscDesc(DMLStatementParser.AscDescContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#anyOperator}.
	 * @param ctx the parse tree
	 */
	void enterAnyOperator(DMLStatementParser.AnyOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#anyOperator}.
	 * @param ctx the parse tree
	 */
	void exitAnyOperator(DMLStatementParser.AnyOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#windowExclusionClause}.
	 * @param ctx the parse tree
	 */
	void enterWindowExclusionClause(DMLStatementParser.WindowExclusionClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#windowExclusionClause}.
	 * @param ctx the parse tree
	 */
	void exitWindowExclusionClause(DMLStatementParser.WindowExclusionClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#row}.
	 * @param ctx the parse tree
	 */
	void enterRow(DMLStatementParser.RowContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#row}.
	 * @param ctx the parse tree
	 */
	void exitRow(DMLStatementParser.RowContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#explicitRow}.
	 * @param ctx the parse tree
	 */
	void enterExplicitRow(DMLStatementParser.ExplicitRowContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#explicitRow}.
	 * @param ctx the parse tree
	 */
	void exitExplicitRow(DMLStatementParser.ExplicitRowContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#implicitRow}.
	 * @param ctx the parse tree
	 */
	void enterImplicitRow(DMLStatementParser.ImplicitRowContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#implicitRow}.
	 * @param ctx the parse tree
	 */
	void exitImplicitRow(DMLStatementParser.ImplicitRowContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#subType}.
	 * @param ctx the parse tree
	 */
	void enterSubType(DMLStatementParser.SubTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#subType}.
	 * @param ctx the parse tree
	 */
	void exitSubType(DMLStatementParser.SubTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#arrayExpr}.
	 * @param ctx the parse tree
	 */
	void enterArrayExpr(DMLStatementParser.ArrayExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#arrayExpr}.
	 * @param ctx the parse tree
	 */
	void exitArrayExpr(DMLStatementParser.ArrayExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#arrayExprList}.
	 * @param ctx the parse tree
	 */
	void enterArrayExprList(DMLStatementParser.ArrayExprListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#arrayExprList}.
	 * @param ctx the parse tree
	 */
	void exitArrayExprList(DMLStatementParser.ArrayExprListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#funcArgList}.
	 * @param ctx the parse tree
	 */
	void enterFuncArgList(DMLStatementParser.FuncArgListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#funcArgList}.
	 * @param ctx the parse tree
	 */
	void exitFuncArgList(DMLStatementParser.FuncArgListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#paramName}.
	 * @param ctx the parse tree
	 */
	void enterParamName(DMLStatementParser.ParamNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#paramName}.
	 * @param ctx the parse tree
	 */
	void exitParamName(DMLStatementParser.ParamNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#funcArgExpr}.
	 * @param ctx the parse tree
	 */
	void enterFuncArgExpr(DMLStatementParser.FuncArgExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#funcArgExpr}.
	 * @param ctx the parse tree
	 */
	void exitFuncArgExpr(DMLStatementParser.FuncArgExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#typeList}.
	 * @param ctx the parse tree
	 */
	void enterTypeList(DMLStatementParser.TypeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#typeList}.
	 * @param ctx the parse tree
	 */
	void exitTypeList(DMLStatementParser.TypeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#funcApplication}.
	 * @param ctx the parse tree
	 */
	void enterFuncApplication(DMLStatementParser.FuncApplicationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#funcApplication}.
	 * @param ctx the parse tree
	 */
	void exitFuncApplication(DMLStatementParser.FuncApplicationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#funcName}.
	 * @param ctx the parse tree
	 */
	void enterFuncName(DMLStatementParser.FuncNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#funcName}.
	 * @param ctx the parse tree
	 */
	void exitFuncName(DMLStatementParser.FuncNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#aexprConst}.
	 * @param ctx the parse tree
	 */
	void enterAexprConst(DMLStatementParser.AexprConstContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#aexprConst}.
	 * @param ctx the parse tree
	 */
	void exitAexprConst(DMLStatementParser.AexprConstContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#colId}.
	 * @param ctx the parse tree
	 */
	void enterColId(DMLStatementParser.ColIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#colId}.
	 * @param ctx the parse tree
	 */
	void exitColId(DMLStatementParser.ColIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#typeFunctionName}.
	 * @param ctx the parse tree
	 */
	void enterTypeFunctionName(DMLStatementParser.TypeFunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#typeFunctionName}.
	 * @param ctx the parse tree
	 */
	void exitTypeFunctionName(DMLStatementParser.TypeFunctionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#functionTable}.
	 * @param ctx the parse tree
	 */
	void enterFunctionTable(DMLStatementParser.FunctionTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#functionTable}.
	 * @param ctx the parse tree
	 */
	void exitFunctionTable(DMLStatementParser.FunctionTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#xmlTable}.
	 * @param ctx the parse tree
	 */
	void enterXmlTable(DMLStatementParser.XmlTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#xmlTable}.
	 * @param ctx the parse tree
	 */
	void exitXmlTable(DMLStatementParser.XmlTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#xmlTableColumnList}.
	 * @param ctx the parse tree
	 */
	void enterXmlTableColumnList(DMLStatementParser.XmlTableColumnListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#xmlTableColumnList}.
	 * @param ctx the parse tree
	 */
	void exitXmlTableColumnList(DMLStatementParser.XmlTableColumnListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#xmlTableColumnEl}.
	 * @param ctx the parse tree
	 */
	void enterXmlTableColumnEl(DMLStatementParser.XmlTableColumnElContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#xmlTableColumnEl}.
	 * @param ctx the parse tree
	 */
	void exitXmlTableColumnEl(DMLStatementParser.XmlTableColumnElContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#xmlTableColumnOptionList}.
	 * @param ctx the parse tree
	 */
	void enterXmlTableColumnOptionList(DMLStatementParser.XmlTableColumnOptionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#xmlTableColumnOptionList}.
	 * @param ctx the parse tree
	 */
	void exitXmlTableColumnOptionList(DMLStatementParser.XmlTableColumnOptionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#xmlTableColumnOptionEl}.
	 * @param ctx the parse tree
	 */
	void enterXmlTableColumnOptionEl(DMLStatementParser.XmlTableColumnOptionElContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#xmlTableColumnOptionEl}.
	 * @param ctx the parse tree
	 */
	void exitXmlTableColumnOptionEl(DMLStatementParser.XmlTableColumnOptionElContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#xmlNamespaceList}.
	 * @param ctx the parse tree
	 */
	void enterXmlNamespaceList(DMLStatementParser.XmlNamespaceListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#xmlNamespaceList}.
	 * @param ctx the parse tree
	 */
	void exitXmlNamespaceList(DMLStatementParser.XmlNamespaceListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#xmlNamespaceEl}.
	 * @param ctx the parse tree
	 */
	void enterXmlNamespaceEl(DMLStatementParser.XmlNamespaceElContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#xmlNamespaceEl}.
	 * @param ctx the parse tree
	 */
	void exitXmlNamespaceEl(DMLStatementParser.XmlNamespaceElContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#funcExpr}.
	 * @param ctx the parse tree
	 */
	void enterFuncExpr(DMLStatementParser.FuncExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#funcExpr}.
	 * @param ctx the parse tree
	 */
	void exitFuncExpr(DMLStatementParser.FuncExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#withinGroupClause}.
	 * @param ctx the parse tree
	 */
	void enterWithinGroupClause(DMLStatementParser.WithinGroupClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#withinGroupClause}.
	 * @param ctx the parse tree
	 */
	void exitWithinGroupClause(DMLStatementParser.WithinGroupClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#filterClause}.
	 * @param ctx the parse tree
	 */
	void enterFilterClause(DMLStatementParser.FilterClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#filterClause}.
	 * @param ctx the parse tree
	 */
	void exitFilterClause(DMLStatementParser.FilterClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#functionExprWindowless}.
	 * @param ctx the parse tree
	 */
	void enterFunctionExprWindowless(DMLStatementParser.FunctionExprWindowlessContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#functionExprWindowless}.
	 * @param ctx the parse tree
	 */
	void exitFunctionExprWindowless(DMLStatementParser.FunctionExprWindowlessContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#ordinality}.
	 * @param ctx the parse tree
	 */
	void enterOrdinality(DMLStatementParser.OrdinalityContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#ordinality}.
	 * @param ctx the parse tree
	 */
	void exitOrdinality(DMLStatementParser.OrdinalityContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#functionExprCommonSubexpr}.
	 * @param ctx the parse tree
	 */
	void enterFunctionExprCommonSubexpr(DMLStatementParser.FunctionExprCommonSubexprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#functionExprCommonSubexpr}.
	 * @param ctx the parse tree
	 */
	void exitFunctionExprCommonSubexpr(DMLStatementParser.FunctionExprCommonSubexprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#typeName}.
	 * @param ctx the parse tree
	 */
	void enterTypeName(DMLStatementParser.TypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#typeName}.
	 * @param ctx the parse tree
	 */
	void exitTypeName(DMLStatementParser.TypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#simpleTypeName}.
	 * @param ctx the parse tree
	 */
	void enterSimpleTypeName(DMLStatementParser.SimpleTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#simpleTypeName}.
	 * @param ctx the parse tree
	 */
	void exitSimpleTypeName(DMLStatementParser.SimpleTypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#exprList}.
	 * @param ctx the parse tree
	 */
	void enterExprList(DMLStatementParser.ExprListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#exprList}.
	 * @param ctx the parse tree
	 */
	void exitExprList(DMLStatementParser.ExprListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#extractList}.
	 * @param ctx the parse tree
	 */
	void enterExtractList(DMLStatementParser.ExtractListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#extractList}.
	 * @param ctx the parse tree
	 */
	void exitExtractList(DMLStatementParser.ExtractListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#extractArg}.
	 * @param ctx the parse tree
	 */
	void enterExtractArg(DMLStatementParser.ExtractArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#extractArg}.
	 * @param ctx the parse tree
	 */
	void exitExtractArg(DMLStatementParser.ExtractArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#genericType}.
	 * @param ctx the parse tree
	 */
	void enterGenericType(DMLStatementParser.GenericTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#genericType}.
	 * @param ctx the parse tree
	 */
	void exitGenericType(DMLStatementParser.GenericTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#typeModifiers}.
	 * @param ctx the parse tree
	 */
	void enterTypeModifiers(DMLStatementParser.TypeModifiersContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#typeModifiers}.
	 * @param ctx the parse tree
	 */
	void exitTypeModifiers(DMLStatementParser.TypeModifiersContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#numeric}.
	 * @param ctx the parse tree
	 */
	void enterNumeric(DMLStatementParser.NumericContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#numeric}.
	 * @param ctx the parse tree
	 */
	void exitNumeric(DMLStatementParser.NumericContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#constDatetime}.
	 * @param ctx the parse tree
	 */
	void enterConstDatetime(DMLStatementParser.ConstDatetimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#constDatetime}.
	 * @param ctx the parse tree
	 */
	void exitConstDatetime(DMLStatementParser.ConstDatetimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#timezone}.
	 * @param ctx the parse tree
	 */
	void enterTimezone(DMLStatementParser.TimezoneContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#timezone}.
	 * @param ctx the parse tree
	 */
	void exitTimezone(DMLStatementParser.TimezoneContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#character}.
	 * @param ctx the parse tree
	 */
	void enterCharacter(DMLStatementParser.CharacterContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#character}.
	 * @param ctx the parse tree
	 */
	void exitCharacter(DMLStatementParser.CharacterContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#characterWithLength}.
	 * @param ctx the parse tree
	 */
	void enterCharacterWithLength(DMLStatementParser.CharacterWithLengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#characterWithLength}.
	 * @param ctx the parse tree
	 */
	void exitCharacterWithLength(DMLStatementParser.CharacterWithLengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#characterWithoutLength}.
	 * @param ctx the parse tree
	 */
	void enterCharacterWithoutLength(DMLStatementParser.CharacterWithoutLengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#characterWithoutLength}.
	 * @param ctx the parse tree
	 */
	void exitCharacterWithoutLength(DMLStatementParser.CharacterWithoutLengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#characterClause}.
	 * @param ctx the parse tree
	 */
	void enterCharacterClause(DMLStatementParser.CharacterClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#characterClause}.
	 * @param ctx the parse tree
	 */
	void exitCharacterClause(DMLStatementParser.CharacterClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#optFloat}.
	 * @param ctx the parse tree
	 */
	void enterOptFloat(DMLStatementParser.OptFloatContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#optFloat}.
	 * @param ctx the parse tree
	 */
	void exitOptFloat(DMLStatementParser.OptFloatContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#attrs}.
	 * @param ctx the parse tree
	 */
	void enterAttrs(DMLStatementParser.AttrsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#attrs}.
	 * @param ctx the parse tree
	 */
	void exitAttrs(DMLStatementParser.AttrsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#attrName}.
	 * @param ctx the parse tree
	 */
	void enterAttrName(DMLStatementParser.AttrNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#attrName}.
	 * @param ctx the parse tree
	 */
	void exitAttrName(DMLStatementParser.AttrNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#colLable}.
	 * @param ctx the parse tree
	 */
	void enterColLable(DMLStatementParser.ColLableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#colLable}.
	 * @param ctx the parse tree
	 */
	void exitColLable(DMLStatementParser.ColLableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#bit}.
	 * @param ctx the parse tree
	 */
	void enterBit(DMLStatementParser.BitContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#bit}.
	 * @param ctx the parse tree
	 */
	void exitBit(DMLStatementParser.BitContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#bitWithLength}.
	 * @param ctx the parse tree
	 */
	void enterBitWithLength(DMLStatementParser.BitWithLengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#bitWithLength}.
	 * @param ctx the parse tree
	 */
	void exitBitWithLength(DMLStatementParser.BitWithLengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#bitWithoutLength}.
	 * @param ctx the parse tree
	 */
	void enterBitWithoutLength(DMLStatementParser.BitWithoutLengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#bitWithoutLength}.
	 * @param ctx the parse tree
	 */
	void exitBitWithoutLength(DMLStatementParser.BitWithoutLengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#constInterval}.
	 * @param ctx the parse tree
	 */
	void enterConstInterval(DMLStatementParser.ConstIntervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#constInterval}.
	 * @param ctx the parse tree
	 */
	void exitConstInterval(DMLStatementParser.ConstIntervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#optInterval}.
	 * @param ctx the parse tree
	 */
	void enterOptInterval(DMLStatementParser.OptIntervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#optInterval}.
	 * @param ctx the parse tree
	 */
	void exitOptInterval(DMLStatementParser.OptIntervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#optArrayBounds}.
	 * @param ctx the parse tree
	 */
	void enterOptArrayBounds(DMLStatementParser.OptArrayBoundsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#optArrayBounds}.
	 * @param ctx the parse tree
	 */
	void exitOptArrayBounds(DMLStatementParser.OptArrayBoundsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#intervalSecond}.
	 * @param ctx the parse tree
	 */
	void enterIntervalSecond(DMLStatementParser.IntervalSecondContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#intervalSecond}.
	 * @param ctx the parse tree
	 */
	void exitIntervalSecond(DMLStatementParser.IntervalSecondContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#unicodeNormalForm}.
	 * @param ctx the parse tree
	 */
	void enterUnicodeNormalForm(DMLStatementParser.UnicodeNormalFormContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#unicodeNormalForm}.
	 * @param ctx the parse tree
	 */
	void exitUnicodeNormalForm(DMLStatementParser.UnicodeNormalFormContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#trimList}.
	 * @param ctx the parse tree
	 */
	void enterTrimList(DMLStatementParser.TrimListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#trimList}.
	 * @param ctx the parse tree
	 */
	void exitTrimList(DMLStatementParser.TrimListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#overlayList}.
	 * @param ctx the parse tree
	 */
	void enterOverlayList(DMLStatementParser.OverlayListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#overlayList}.
	 * @param ctx the parse tree
	 */
	void exitOverlayList(DMLStatementParser.OverlayListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#overlayPlacing}.
	 * @param ctx the parse tree
	 */
	void enterOverlayPlacing(DMLStatementParser.OverlayPlacingContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#overlayPlacing}.
	 * @param ctx the parse tree
	 */
	void exitOverlayPlacing(DMLStatementParser.OverlayPlacingContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#substrFrom}.
	 * @param ctx the parse tree
	 */
	void enterSubstrFrom(DMLStatementParser.SubstrFromContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#substrFrom}.
	 * @param ctx the parse tree
	 */
	void exitSubstrFrom(DMLStatementParser.SubstrFromContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#substrFor}.
	 * @param ctx the parse tree
	 */
	void enterSubstrFor(DMLStatementParser.SubstrForContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#substrFor}.
	 * @param ctx the parse tree
	 */
	void exitSubstrFor(DMLStatementParser.SubstrForContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#positionList}.
	 * @param ctx the parse tree
	 */
	void enterPositionList(DMLStatementParser.PositionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#positionList}.
	 * @param ctx the parse tree
	 */
	void exitPositionList(DMLStatementParser.PositionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#substrList}.
	 * @param ctx the parse tree
	 */
	void enterSubstrList(DMLStatementParser.SubstrListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#substrList}.
	 * @param ctx the parse tree
	 */
	void exitSubstrList(DMLStatementParser.SubstrListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#xmlAttributes}.
	 * @param ctx the parse tree
	 */
	void enterXmlAttributes(DMLStatementParser.XmlAttributesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#xmlAttributes}.
	 * @param ctx the parse tree
	 */
	void exitXmlAttributes(DMLStatementParser.XmlAttributesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#xmlAttributeList}.
	 * @param ctx the parse tree
	 */
	void enterXmlAttributeList(DMLStatementParser.XmlAttributeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#xmlAttributeList}.
	 * @param ctx the parse tree
	 */
	void exitXmlAttributeList(DMLStatementParser.XmlAttributeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#xmlAttributeEl}.
	 * @param ctx the parse tree
	 */
	void enterXmlAttributeEl(DMLStatementParser.XmlAttributeElContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#xmlAttributeEl}.
	 * @param ctx the parse tree
	 */
	void exitXmlAttributeEl(DMLStatementParser.XmlAttributeElContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#xmlExistsArgument}.
	 * @param ctx the parse tree
	 */
	void enterXmlExistsArgument(DMLStatementParser.XmlExistsArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#xmlExistsArgument}.
	 * @param ctx the parse tree
	 */
	void exitXmlExistsArgument(DMLStatementParser.XmlExistsArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#xmlPassingMech}.
	 * @param ctx the parse tree
	 */
	void enterXmlPassingMech(DMLStatementParser.XmlPassingMechContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#xmlPassingMech}.
	 * @param ctx the parse tree
	 */
	void exitXmlPassingMech(DMLStatementParser.XmlPassingMechContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#documentOrContent}.
	 * @param ctx the parse tree
	 */
	void enterDocumentOrContent(DMLStatementParser.DocumentOrContentContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#documentOrContent}.
	 * @param ctx the parse tree
	 */
	void exitDocumentOrContent(DMLStatementParser.DocumentOrContentContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#xmlWhitespaceOption}.
	 * @param ctx the parse tree
	 */
	void enterXmlWhitespaceOption(DMLStatementParser.XmlWhitespaceOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#xmlWhitespaceOption}.
	 * @param ctx the parse tree
	 */
	void exitXmlWhitespaceOption(DMLStatementParser.XmlWhitespaceOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#xmlRootVersion}.
	 * @param ctx the parse tree
	 */
	void enterXmlRootVersion(DMLStatementParser.XmlRootVersionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#xmlRootVersion}.
	 * @param ctx the parse tree
	 */
	void exitXmlRootVersion(DMLStatementParser.XmlRootVersionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#xmlRootStandalone}.
	 * @param ctx the parse tree
	 */
	void enterXmlRootStandalone(DMLStatementParser.XmlRootStandaloneContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#xmlRootStandalone}.
	 * @param ctx the parse tree
	 */
	void exitXmlRootStandalone(DMLStatementParser.XmlRootStandaloneContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#rowsFromItem}.
	 * @param ctx the parse tree
	 */
	void enterRowsFromItem(DMLStatementParser.RowsFromItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#rowsFromItem}.
	 * @param ctx the parse tree
	 */
	void exitRowsFromItem(DMLStatementParser.RowsFromItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#rowsFromList}.
	 * @param ctx the parse tree
	 */
	void enterRowsFromList(DMLStatementParser.RowsFromListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#rowsFromList}.
	 * @param ctx the parse tree
	 */
	void exitRowsFromList(DMLStatementParser.RowsFromListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#columnDefList}.
	 * @param ctx the parse tree
	 */
	void enterColumnDefList(DMLStatementParser.ColumnDefListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#columnDefList}.
	 * @param ctx the parse tree
	 */
	void exitColumnDefList(DMLStatementParser.ColumnDefListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#tableFuncElementList}.
	 * @param ctx the parse tree
	 */
	void enterTableFuncElementList(DMLStatementParser.TableFuncElementListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#tableFuncElementList}.
	 * @param ctx the parse tree
	 */
	void exitTableFuncElementList(DMLStatementParser.TableFuncElementListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#tableFuncElement}.
	 * @param ctx the parse tree
	 */
	void enterTableFuncElement(DMLStatementParser.TableFuncElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#tableFuncElement}.
	 * @param ctx the parse tree
	 */
	void exitTableFuncElement(DMLStatementParser.TableFuncElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#collateClause}.
	 * @param ctx the parse tree
	 */
	void enterCollateClause(DMLStatementParser.CollateClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#collateClause}.
	 * @param ctx the parse tree
	 */
	void exitCollateClause(DMLStatementParser.CollateClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#anyName}.
	 * @param ctx the parse tree
	 */
	void enterAnyName(DMLStatementParser.AnyNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#anyName}.
	 * @param ctx the parse tree
	 */
	void exitAnyName(DMLStatementParser.AnyNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#aliasClause}.
	 * @param ctx the parse tree
	 */
	void enterAliasClause(DMLStatementParser.AliasClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#aliasClause}.
	 * @param ctx the parse tree
	 */
	void exitAliasClause(DMLStatementParser.AliasClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#nameList}.
	 * @param ctx the parse tree
	 */
	void enterNameList(DMLStatementParser.NameListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#nameList}.
	 * @param ctx the parse tree
	 */
	void exitNameList(DMLStatementParser.NameListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#funcAliasClause}.
	 * @param ctx the parse tree
	 */
	void enterFuncAliasClause(DMLStatementParser.FuncAliasClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#funcAliasClause}.
	 * @param ctx the parse tree
	 */
	void exitFuncAliasClause(DMLStatementParser.FuncAliasClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#tablesampleClause}.
	 * @param ctx the parse tree
	 */
	void enterTablesampleClause(DMLStatementParser.TablesampleClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#tablesampleClause}.
	 * @param ctx the parse tree
	 */
	void exitTablesampleClause(DMLStatementParser.TablesampleClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#repeatableClause}.
	 * @param ctx the parse tree
	 */
	void enterRepeatableClause(DMLStatementParser.RepeatableClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#repeatableClause}.
	 * @param ctx the parse tree
	 */
	void exitRepeatableClause(DMLStatementParser.RepeatableClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#allOrDistinct}.
	 * @param ctx the parse tree
	 */
	void enterAllOrDistinct(DMLStatementParser.AllOrDistinctContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#allOrDistinct}.
	 * @param ctx the parse tree
	 */
	void exitAllOrDistinct(DMLStatementParser.AllOrDistinctContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#sortClause}.
	 * @param ctx the parse tree
	 */
	void enterSortClause(DMLStatementParser.SortClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#sortClause}.
	 * @param ctx the parse tree
	 */
	void exitSortClause(DMLStatementParser.SortClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#sortbyList}.
	 * @param ctx the parse tree
	 */
	void enterSortbyList(DMLStatementParser.SortbyListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#sortbyList}.
	 * @param ctx the parse tree
	 */
	void exitSortbyList(DMLStatementParser.SortbyListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#sortby}.
	 * @param ctx the parse tree
	 */
	void enterSortby(DMLStatementParser.SortbyContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#sortby}.
	 * @param ctx the parse tree
	 */
	void exitSortby(DMLStatementParser.SortbyContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#nullsOrder}.
	 * @param ctx the parse tree
	 */
	void enterNullsOrder(DMLStatementParser.NullsOrderContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#nullsOrder}.
	 * @param ctx the parse tree
	 */
	void exitNullsOrder(DMLStatementParser.NullsOrderContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#distinctClause}.
	 * @param ctx the parse tree
	 */
	void enterDistinctClause(DMLStatementParser.DistinctClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#distinctClause}.
	 * @param ctx the parse tree
	 */
	void exitDistinctClause(DMLStatementParser.DistinctClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#distinct}.
	 * @param ctx the parse tree
	 */
	void enterDistinct(DMLStatementParser.DistinctContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#distinct}.
	 * @param ctx the parse tree
	 */
	void exitDistinct(DMLStatementParser.DistinctContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#overClause}.
	 * @param ctx the parse tree
	 */
	void enterOverClause(DMLStatementParser.OverClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#overClause}.
	 * @param ctx the parse tree
	 */
	void exitOverClause(DMLStatementParser.OverClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#windowName}.
	 * @param ctx the parse tree
	 */
	void enterWindowName(DMLStatementParser.WindowNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#windowName}.
	 * @param ctx the parse tree
	 */
	void exitWindowName(DMLStatementParser.WindowNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#indexParams}.
	 * @param ctx the parse tree
	 */
	void enterIndexParams(DMLStatementParser.IndexParamsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#indexParams}.
	 * @param ctx the parse tree
	 */
	void exitIndexParams(DMLStatementParser.IndexParamsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#indexElemOptions}.
	 * @param ctx the parse tree
	 */
	void enterIndexElemOptions(DMLStatementParser.IndexElemOptionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#indexElemOptions}.
	 * @param ctx the parse tree
	 */
	void exitIndexElemOptions(DMLStatementParser.IndexElemOptionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#indexElem}.
	 * @param ctx the parse tree
	 */
	void enterIndexElem(DMLStatementParser.IndexElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#indexElem}.
	 * @param ctx the parse tree
	 */
	void exitIndexElem(DMLStatementParser.IndexElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#collate}.
	 * @param ctx the parse tree
	 */
	void enterCollate(DMLStatementParser.CollateContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#collate}.
	 * @param ctx the parse tree
	 */
	void exitCollate(DMLStatementParser.CollateContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#optClass}.
	 * @param ctx the parse tree
	 */
	void enterOptClass(DMLStatementParser.OptClassContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#optClass}.
	 * @param ctx the parse tree
	 */
	void exitOptClass(DMLStatementParser.OptClassContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#reloptions}.
	 * @param ctx the parse tree
	 */
	void enterReloptions(DMLStatementParser.ReloptionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#reloptions}.
	 * @param ctx the parse tree
	 */
	void exitReloptions(DMLStatementParser.ReloptionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#reloptionList}.
	 * @param ctx the parse tree
	 */
	void enterReloptionList(DMLStatementParser.ReloptionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#reloptionList}.
	 * @param ctx the parse tree
	 */
	void exitReloptionList(DMLStatementParser.ReloptionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#reloptionElem}.
	 * @param ctx the parse tree
	 */
	void enterReloptionElem(DMLStatementParser.ReloptionElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#reloptionElem}.
	 * @param ctx the parse tree
	 */
	void exitReloptionElem(DMLStatementParser.ReloptionElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#defArg}.
	 * @param ctx the parse tree
	 */
	void enterDefArg(DMLStatementParser.DefArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#defArg}.
	 * @param ctx the parse tree
	 */
	void exitDefArg(DMLStatementParser.DefArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#funcType}.
	 * @param ctx the parse tree
	 */
	void enterFuncType(DMLStatementParser.FuncTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#funcType}.
	 * @param ctx the parse tree
	 */
	void exitFuncType(DMLStatementParser.FuncTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterDataType(DMLStatementParser.DataTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitDataType(DMLStatementParser.DataTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#dataTypeName}.
	 * @param ctx the parse tree
	 */
	void enterDataTypeName(DMLStatementParser.DataTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#dataTypeName}.
	 * @param ctx the parse tree
	 */
	void exitDataTypeName(DMLStatementParser.DataTypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#dataTypeLength}.
	 * @param ctx the parse tree
	 */
	void enterDataTypeLength(DMLStatementParser.DataTypeLengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#dataTypeLength}.
	 * @param ctx the parse tree
	 */
	void exitDataTypeLength(DMLStatementParser.DataTypeLengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#characterSet}.
	 * @param ctx the parse tree
	 */
	void enterCharacterSet(DMLStatementParser.CharacterSetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#characterSet}.
	 * @param ctx the parse tree
	 */
	void exitCharacterSet(DMLStatementParser.CharacterSetContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#ignoredIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterIgnoredIdentifier(DMLStatementParser.IgnoredIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#ignoredIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitIgnoredIdentifier(DMLStatementParser.IgnoredIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#ignoredIdentifiers}.
	 * @param ctx the parse tree
	 */
	void enterIgnoredIdentifiers(DMLStatementParser.IgnoredIdentifiersContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#ignoredIdentifiers}.
	 * @param ctx the parse tree
	 */
	void exitIgnoredIdentifiers(DMLStatementParser.IgnoredIdentifiersContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#signedIconst}.
	 * @param ctx the parse tree
	 */
	void enterSignedIconst(DMLStatementParser.SignedIconstContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#signedIconst}.
	 * @param ctx the parse tree
	 */
	void exitSignedIconst(DMLStatementParser.SignedIconstContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#booleanOrString}.
	 * @param ctx the parse tree
	 */
	void enterBooleanOrString(DMLStatementParser.BooleanOrStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#booleanOrString}.
	 * @param ctx the parse tree
	 */
	void exitBooleanOrString(DMLStatementParser.BooleanOrStringContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#nonReservedWord}.
	 * @param ctx the parse tree
	 */
	void enterNonReservedWord(DMLStatementParser.NonReservedWordContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#nonReservedWord}.
	 * @param ctx the parse tree
	 */
	void exitNonReservedWord(DMLStatementParser.NonReservedWordContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#colNameKeyword}.
	 * @param ctx the parse tree
	 */
	void enterColNameKeyword(DMLStatementParser.ColNameKeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#colNameKeyword}.
	 * @param ctx the parse tree
	 */
	void exitColNameKeyword(DMLStatementParser.ColNameKeywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#databaseName}.
	 * @param ctx the parse tree
	 */
	void enterDatabaseName(DMLStatementParser.DatabaseNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#databaseName}.
	 * @param ctx the parse tree
	 */
	void exitDatabaseName(DMLStatementParser.DatabaseNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#roleSpec}.
	 * @param ctx the parse tree
	 */
	void enterRoleSpec(DMLStatementParser.RoleSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#roleSpec}.
	 * @param ctx the parse tree
	 */
	void exitRoleSpec(DMLStatementParser.RoleSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#varName}.
	 * @param ctx the parse tree
	 */
	void enterVarName(DMLStatementParser.VarNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#varName}.
	 * @param ctx the parse tree
	 */
	void exitVarName(DMLStatementParser.VarNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#varList}.
	 * @param ctx the parse tree
	 */
	void enterVarList(DMLStatementParser.VarListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#varList}.
	 * @param ctx the parse tree
	 */
	void exitVarList(DMLStatementParser.VarListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#varValue}.
	 * @param ctx the parse tree
	 */
	void enterVarValue(DMLStatementParser.VarValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#varValue}.
	 * @param ctx the parse tree
	 */
	void exitVarValue(DMLStatementParser.VarValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#zoneValue}.
	 * @param ctx the parse tree
	 */
	void enterZoneValue(DMLStatementParser.ZoneValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#zoneValue}.
	 * @param ctx the parse tree
	 */
	void exitZoneValue(DMLStatementParser.ZoneValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#numericOnly}.
	 * @param ctx the parse tree
	 */
	void enterNumericOnly(DMLStatementParser.NumericOnlyContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#numericOnly}.
	 * @param ctx the parse tree
	 */
	void exitNumericOnly(DMLStatementParser.NumericOnlyContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#isoLevel}.
	 * @param ctx the parse tree
	 */
	void enterIsoLevel(DMLStatementParser.IsoLevelContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#isoLevel}.
	 * @param ctx the parse tree
	 */
	void exitIsoLevel(DMLStatementParser.IsoLevelContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#columnDef}.
	 * @param ctx the parse tree
	 */
	void enterColumnDef(DMLStatementParser.ColumnDefContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#columnDef}.
	 * @param ctx the parse tree
	 */
	void exitColumnDef(DMLStatementParser.ColumnDefContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#colQualList}.
	 * @param ctx the parse tree
	 */
	void enterColQualList(DMLStatementParser.ColQualListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#colQualList}.
	 * @param ctx the parse tree
	 */
	void exitColQualList(DMLStatementParser.ColQualListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#colConstraint}.
	 * @param ctx the parse tree
	 */
	void enterColConstraint(DMLStatementParser.ColConstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#colConstraint}.
	 * @param ctx the parse tree
	 */
	void exitColConstraint(DMLStatementParser.ColConstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#constraintAttr}.
	 * @param ctx the parse tree
	 */
	void enterConstraintAttr(DMLStatementParser.ConstraintAttrContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#constraintAttr}.
	 * @param ctx the parse tree
	 */
	void exitConstraintAttr(DMLStatementParser.ConstraintAttrContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#colConstraintElem}.
	 * @param ctx the parse tree
	 */
	void enterColConstraintElem(DMLStatementParser.ColConstraintElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#colConstraintElem}.
	 * @param ctx the parse tree
	 */
	void exitColConstraintElem(DMLStatementParser.ColConstraintElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#parenthesizedSeqOptList}.
	 * @param ctx the parse tree
	 */
	void enterParenthesizedSeqOptList(DMLStatementParser.ParenthesizedSeqOptListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#parenthesizedSeqOptList}.
	 * @param ctx the parse tree
	 */
	void exitParenthesizedSeqOptList(DMLStatementParser.ParenthesizedSeqOptListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#seqOptList}.
	 * @param ctx the parse tree
	 */
	void enterSeqOptList(DMLStatementParser.SeqOptListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#seqOptList}.
	 * @param ctx the parse tree
	 */
	void exitSeqOptList(DMLStatementParser.SeqOptListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#seqOptElem}.
	 * @param ctx the parse tree
	 */
	void enterSeqOptElem(DMLStatementParser.SeqOptElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#seqOptElem}.
	 * @param ctx the parse tree
	 */
	void exitSeqOptElem(DMLStatementParser.SeqOptElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#optColumnList}.
	 * @param ctx the parse tree
	 */
	void enterOptColumnList(DMLStatementParser.OptColumnListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#optColumnList}.
	 * @param ctx the parse tree
	 */
	void exitOptColumnList(DMLStatementParser.OptColumnListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#columnElem}.
	 * @param ctx the parse tree
	 */
	void enterColumnElem(DMLStatementParser.ColumnElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#columnElem}.
	 * @param ctx the parse tree
	 */
	void exitColumnElem(DMLStatementParser.ColumnElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#columnList}.
	 * @param ctx the parse tree
	 */
	void enterColumnList(DMLStatementParser.ColumnListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#columnList}.
	 * @param ctx the parse tree
	 */
	void exitColumnList(DMLStatementParser.ColumnListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#generatedWhen}.
	 * @param ctx the parse tree
	 */
	void enterGeneratedWhen(DMLStatementParser.GeneratedWhenContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#generatedWhen}.
	 * @param ctx the parse tree
	 */
	void exitGeneratedWhen(DMLStatementParser.GeneratedWhenContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#noInherit}.
	 * @param ctx the parse tree
	 */
	void enterNoInherit(DMLStatementParser.NoInheritContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#noInherit}.
	 * @param ctx the parse tree
	 */
	void exitNoInherit(DMLStatementParser.NoInheritContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#consTableSpace}.
	 * @param ctx the parse tree
	 */
	void enterConsTableSpace(DMLStatementParser.ConsTableSpaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#consTableSpace}.
	 * @param ctx the parse tree
	 */
	void exitConsTableSpace(DMLStatementParser.ConsTableSpaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#definition}.
	 * @param ctx the parse tree
	 */
	void enterDefinition(DMLStatementParser.DefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#definition}.
	 * @param ctx the parse tree
	 */
	void exitDefinition(DMLStatementParser.DefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#defList}.
	 * @param ctx the parse tree
	 */
	void enterDefList(DMLStatementParser.DefListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#defList}.
	 * @param ctx the parse tree
	 */
	void exitDefList(DMLStatementParser.DefListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#defElem}.
	 * @param ctx the parse tree
	 */
	void enterDefElem(DMLStatementParser.DefElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#defElem}.
	 * @param ctx the parse tree
	 */
	void exitDefElem(DMLStatementParser.DefElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#colLabel}.
	 * @param ctx the parse tree
	 */
	void enterColLabel(DMLStatementParser.ColLabelContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#colLabel}.
	 * @param ctx the parse tree
	 */
	void exitColLabel(DMLStatementParser.ColLabelContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#keyActions}.
	 * @param ctx the parse tree
	 */
	void enterKeyActions(DMLStatementParser.KeyActionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#keyActions}.
	 * @param ctx the parse tree
	 */
	void exitKeyActions(DMLStatementParser.KeyActionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#keyDelete}.
	 * @param ctx the parse tree
	 */
	void enterKeyDelete(DMLStatementParser.KeyDeleteContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#keyDelete}.
	 * @param ctx the parse tree
	 */
	void exitKeyDelete(DMLStatementParser.KeyDeleteContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#keyUpdate}.
	 * @param ctx the parse tree
	 */
	void enterKeyUpdate(DMLStatementParser.KeyUpdateContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#keyUpdate}.
	 * @param ctx the parse tree
	 */
	void exitKeyUpdate(DMLStatementParser.KeyUpdateContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#keyAction}.
	 * @param ctx the parse tree
	 */
	void enterKeyAction(DMLStatementParser.KeyActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#keyAction}.
	 * @param ctx the parse tree
	 */
	void exitKeyAction(DMLStatementParser.KeyActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#keyMatch}.
	 * @param ctx the parse tree
	 */
	void enterKeyMatch(DMLStatementParser.KeyMatchContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#keyMatch}.
	 * @param ctx the parse tree
	 */
	void exitKeyMatch(DMLStatementParser.KeyMatchContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#createGenericOptions}.
	 * @param ctx the parse tree
	 */
	void enterCreateGenericOptions(DMLStatementParser.CreateGenericOptionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#createGenericOptions}.
	 * @param ctx the parse tree
	 */
	void exitCreateGenericOptions(DMLStatementParser.CreateGenericOptionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#genericOptionList}.
	 * @param ctx the parse tree
	 */
	void enterGenericOptionList(DMLStatementParser.GenericOptionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#genericOptionList}.
	 * @param ctx the parse tree
	 */
	void exitGenericOptionList(DMLStatementParser.GenericOptionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#genericOptionElem}.
	 * @param ctx the parse tree
	 */
	void enterGenericOptionElem(DMLStatementParser.GenericOptionElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#genericOptionElem}.
	 * @param ctx the parse tree
	 */
	void exitGenericOptionElem(DMLStatementParser.GenericOptionElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#genericOptionArg}.
	 * @param ctx the parse tree
	 */
	void enterGenericOptionArg(DMLStatementParser.GenericOptionArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#genericOptionArg}.
	 * @param ctx the parse tree
	 */
	void exitGenericOptionArg(DMLStatementParser.GenericOptionArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#genericOptionName}.
	 * @param ctx the parse tree
	 */
	void enterGenericOptionName(DMLStatementParser.GenericOptionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#genericOptionName}.
	 * @param ctx the parse tree
	 */
	void exitGenericOptionName(DMLStatementParser.GenericOptionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#replicaIdentity}.
	 * @param ctx the parse tree
	 */
	void enterReplicaIdentity(DMLStatementParser.ReplicaIdentityContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#replicaIdentity}.
	 * @param ctx the parse tree
	 */
	void exitReplicaIdentity(DMLStatementParser.ReplicaIdentityContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#operArgtypes}.
	 * @param ctx the parse tree
	 */
	void enterOperArgtypes(DMLStatementParser.OperArgtypesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#operArgtypes}.
	 * @param ctx the parse tree
	 */
	void exitOperArgtypes(DMLStatementParser.OperArgtypesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#funcArg}.
	 * @param ctx the parse tree
	 */
	void enterFuncArg(DMLStatementParser.FuncArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#funcArg}.
	 * @param ctx the parse tree
	 */
	void exitFuncArg(DMLStatementParser.FuncArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#argClass}.
	 * @param ctx the parse tree
	 */
	void enterArgClass(DMLStatementParser.ArgClassContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#argClass}.
	 * @param ctx the parse tree
	 */
	void exitArgClass(DMLStatementParser.ArgClassContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#funcArgsList}.
	 * @param ctx the parse tree
	 */
	void enterFuncArgsList(DMLStatementParser.FuncArgsListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#funcArgsList}.
	 * @param ctx the parse tree
	 */
	void exitFuncArgsList(DMLStatementParser.FuncArgsListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#nonReservedWordOrSconst}.
	 * @param ctx the parse tree
	 */
	void enterNonReservedWordOrSconst(DMLStatementParser.NonReservedWordOrSconstContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#nonReservedWordOrSconst}.
	 * @param ctx the parse tree
	 */
	void exitNonReservedWordOrSconst(DMLStatementParser.NonReservedWordOrSconstContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#fileName}.
	 * @param ctx the parse tree
	 */
	void enterFileName(DMLStatementParser.FileNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#fileName}.
	 * @param ctx the parse tree
	 */
	void exitFileName(DMLStatementParser.FileNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#roleList}.
	 * @param ctx the parse tree
	 */
	void enterRoleList(DMLStatementParser.RoleListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#roleList}.
	 * @param ctx the parse tree
	 */
	void exitRoleList(DMLStatementParser.RoleListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#setResetClause}.
	 * @param ctx the parse tree
	 */
	void enterSetResetClause(DMLStatementParser.SetResetClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#setResetClause}.
	 * @param ctx the parse tree
	 */
	void exitSetResetClause(DMLStatementParser.SetResetClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#setRest}.
	 * @param ctx the parse tree
	 */
	void enterSetRest(DMLStatementParser.SetRestContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#setRest}.
	 * @param ctx the parse tree
	 */
	void exitSetRest(DMLStatementParser.SetRestContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#transactionModeList}.
	 * @param ctx the parse tree
	 */
	void enterTransactionModeList(DMLStatementParser.TransactionModeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#transactionModeList}.
	 * @param ctx the parse tree
	 */
	void exitTransactionModeList(DMLStatementParser.TransactionModeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#transactionModeItem}.
	 * @param ctx the parse tree
	 */
	void enterTransactionModeItem(DMLStatementParser.TransactionModeItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#transactionModeItem}.
	 * @param ctx the parse tree
	 */
	void exitTransactionModeItem(DMLStatementParser.TransactionModeItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#setRestMore}.
	 * @param ctx the parse tree
	 */
	void enterSetRestMore(DMLStatementParser.SetRestMoreContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#setRestMore}.
	 * @param ctx the parse tree
	 */
	void exitSetRestMore(DMLStatementParser.SetRestMoreContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#encoding}.
	 * @param ctx the parse tree
	 */
	void enterEncoding(DMLStatementParser.EncodingContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#encoding}.
	 * @param ctx the parse tree
	 */
	void exitEncoding(DMLStatementParser.EncodingContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#genericSet}.
	 * @param ctx the parse tree
	 */
	void enterGenericSet(DMLStatementParser.GenericSetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#genericSet}.
	 * @param ctx the parse tree
	 */
	void exitGenericSet(DMLStatementParser.GenericSetContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#variableResetStmt}.
	 * @param ctx the parse tree
	 */
	void enterVariableResetStmt(DMLStatementParser.VariableResetStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#variableResetStmt}.
	 * @param ctx the parse tree
	 */
	void exitVariableResetStmt(DMLStatementParser.VariableResetStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#resetRest}.
	 * @param ctx the parse tree
	 */
	void enterResetRest(DMLStatementParser.ResetRestContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#resetRest}.
	 * @param ctx the parse tree
	 */
	void exitResetRest(DMLStatementParser.ResetRestContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#genericReset}.
	 * @param ctx the parse tree
	 */
	void enterGenericReset(DMLStatementParser.GenericResetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#genericReset}.
	 * @param ctx the parse tree
	 */
	void exitGenericReset(DMLStatementParser.GenericResetContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#relationExprList}.
	 * @param ctx the parse tree
	 */
	void enterRelationExprList(DMLStatementParser.RelationExprListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#relationExprList}.
	 * @param ctx the parse tree
	 */
	void exitRelationExprList(DMLStatementParser.RelationExprListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#commonFuncOptItem}.
	 * @param ctx the parse tree
	 */
	void enterCommonFuncOptItem(DMLStatementParser.CommonFuncOptItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#commonFuncOptItem}.
	 * @param ctx the parse tree
	 */
	void exitCommonFuncOptItem(DMLStatementParser.CommonFuncOptItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#functionSetResetClause}.
	 * @param ctx the parse tree
	 */
	void enterFunctionSetResetClause(DMLStatementParser.FunctionSetResetClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#functionSetResetClause}.
	 * @param ctx the parse tree
	 */
	void exitFunctionSetResetClause(DMLStatementParser.FunctionSetResetClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#rowSecurityCmd}.
	 * @param ctx the parse tree
	 */
	void enterRowSecurityCmd(DMLStatementParser.RowSecurityCmdContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#rowSecurityCmd}.
	 * @param ctx the parse tree
	 */
	void exitRowSecurityCmd(DMLStatementParser.RowSecurityCmdContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#event}.
	 * @param ctx the parse tree
	 */
	void enterEvent(DMLStatementParser.EventContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#event}.
	 * @param ctx the parse tree
	 */
	void exitEvent(DMLStatementParser.EventContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#typeNameList}.
	 * @param ctx the parse tree
	 */
	void enterTypeNameList(DMLStatementParser.TypeNameListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#typeNameList}.
	 * @param ctx the parse tree
	 */
	void exitTypeNameList(DMLStatementParser.TypeNameListContext ctx);
}