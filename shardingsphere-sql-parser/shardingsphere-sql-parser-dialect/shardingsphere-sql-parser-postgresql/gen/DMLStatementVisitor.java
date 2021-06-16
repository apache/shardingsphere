// Generated from /home/totalo/code/shardingsphere/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-postgresql/src/main/antlr4/imports/postgresql/DMLStatement.g4 by ANTLR 4.9.1
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link DMLStatementParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface DMLStatementVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#insert}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert(DMLStatementParser.InsertContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#insertTarget}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertTarget(DMLStatementParser.InsertTargetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#insertRest}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertRest(DMLStatementParser.InsertRestContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#overrideKind}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOverrideKind(DMLStatementParser.OverrideKindContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#insertColumnList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertColumnList(DMLStatementParser.InsertColumnListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#insertColumnItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertColumnItem(DMLStatementParser.InsertColumnItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#optOnConflict}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptOnConflict(DMLStatementParser.OptOnConflictContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#optConfExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptConfExpr(DMLStatementParser.OptConfExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#update}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdate(DMLStatementParser.UpdateContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#setClauseList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetClauseList(DMLStatementParser.SetClauseListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#setClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetClause(DMLStatementParser.SetClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#setTarget}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetTarget(DMLStatementParser.SetTargetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#setTargetList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetTargetList(DMLStatementParser.SetTargetListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#returningClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturningClause(DMLStatementParser.ReturningClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#delete}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDelete(DMLStatementParser.DeleteContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#relationExprOptAlias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationExprOptAlias(DMLStatementParser.RelationExprOptAliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#usingClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUsingClause(DMLStatementParser.UsingClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#select}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect(DMLStatementParser.SelectContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#selectWithParens}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectWithParens(DMLStatementParser.SelectWithParensContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#selectNoParens}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectNoParens(DMLStatementParser.SelectNoParensContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#selectClauseN}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectClauseN(DMLStatementParser.SelectClauseNContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#simpleSelect}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleSelect(DMLStatementParser.SimpleSelectContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#withClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithClause(DMLStatementParser.WithClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#intoClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntoClause(DMLStatementParser.IntoClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#optTempTableName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptTempTableName(DMLStatementParser.OptTempTableNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#cteList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCteList(DMLStatementParser.CteListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#commonTableExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommonTableExpr(DMLStatementParser.CommonTableExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#optMaterialized}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptMaterialized(DMLStatementParser.OptMaterializedContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#optNameList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptNameList(DMLStatementParser.OptNameListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#preparableStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPreparableStmt(DMLStatementParser.PreparableStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#forLockingClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForLockingClause(DMLStatementParser.ForLockingClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#forLockingItems}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForLockingItems(DMLStatementParser.ForLockingItemsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#forLockingItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForLockingItem(DMLStatementParser.ForLockingItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#nowaitOrSkip}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNowaitOrSkip(DMLStatementParser.NowaitOrSkipContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#forLockingStrength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForLockingStrength(DMLStatementParser.ForLockingStrengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#lockedRelsList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLockedRelsList(DMLStatementParser.LockedRelsListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#qualifiedNameList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedNameList(DMLStatementParser.QualifiedNameListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#qualifiedName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedName(DMLStatementParser.QualifiedNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#selectLimit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectLimit(DMLStatementParser.SelectLimitContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#valuesClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValuesClause(DMLStatementParser.ValuesClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#limitClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimitClause(DMLStatementParser.LimitClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#offsetClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOffsetClause(DMLStatementParser.OffsetClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#selectLimitValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectLimitValue(DMLStatementParser.SelectLimitValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#selectOffsetValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectOffsetValue(DMLStatementParser.SelectOffsetValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#selectFetchFirstValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectFetchFirstValue(DMLStatementParser.SelectFetchFirstValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#rowOrRows}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowOrRows(DMLStatementParser.RowOrRowsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#firstOrNext}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFirstOrNext(DMLStatementParser.FirstOrNextContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#targetList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTargetList(DMLStatementParser.TargetListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#targetEl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTargetEl(DMLStatementParser.TargetElContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#groupClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupClause(DMLStatementParser.GroupClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#groupByList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupByList(DMLStatementParser.GroupByListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#groupByItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupByItem(DMLStatementParser.GroupByItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#emptyGroupingSet}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmptyGroupingSet(DMLStatementParser.EmptyGroupingSetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#rollupClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRollupClause(DMLStatementParser.RollupClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#cubeClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCubeClause(DMLStatementParser.CubeClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#groupingSetsClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupingSetsClause(DMLStatementParser.GroupingSetsClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#windowClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowClause(DMLStatementParser.WindowClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#windowDefinitionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowDefinitionList(DMLStatementParser.WindowDefinitionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#windowDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowDefinition(DMLStatementParser.WindowDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#windowSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowSpecification(DMLStatementParser.WindowSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#existingWindowName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExistingWindowName(DMLStatementParser.ExistingWindowNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#partitionClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionClause(DMLStatementParser.PartitionClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#frameClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrameClause(DMLStatementParser.FrameClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#frameExtent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrameExtent(DMLStatementParser.FrameExtentContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#frameBound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrameBound(DMLStatementParser.FrameBoundContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#optWindowExclusionClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptWindowExclusionClause(DMLStatementParser.OptWindowExclusionClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlias(DMLStatementParser.AliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#fromClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromClause(DMLStatementParser.FromClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#fromList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromList(DMLStatementParser.FromListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#tableReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableReference(DMLStatementParser.TableReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#joinedTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinedTable(DMLStatementParser.JoinedTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#joinType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinType(DMLStatementParser.JoinTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#joinOuter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinOuter(DMLStatementParser.JoinOuterContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#joinQual}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinQual(DMLStatementParser.JoinQualContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#relationExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationExpr(DMLStatementParser.RelationExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#whereClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhereClause(DMLStatementParser.WhereClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#whereOrCurrentClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhereOrCurrentClause(DMLStatementParser.WhereOrCurrentClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#havingClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingClause(DMLStatementParser.HavingClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#doStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDoStatement(DMLStatementParser.DoStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#dostmtOptList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDostmtOptList(DMLStatementParser.DostmtOptListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#dostmtOptItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDostmtOptItem(DMLStatementParser.DostmtOptItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#lock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLock(DMLStatementParser.LockContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#lockType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLockType(DMLStatementParser.LockTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#checkpoint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCheckpoint(DMLStatementParser.CheckpointContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#copy}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopy(DMLStatementParser.CopyContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#copyOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopyOptions(DMLStatementParser.CopyOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#copyGenericOptList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopyGenericOptList(DMLStatementParser.CopyGenericOptListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#copyGenericOptElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopyGenericOptElem(DMLStatementParser.CopyGenericOptElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#copyGenericOptArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopyGenericOptArg(DMLStatementParser.CopyGenericOptArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#copyGenericOptArgList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopyGenericOptArgList(DMLStatementParser.CopyGenericOptArgListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#copyGenericOptArgListItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopyGenericOptArgListItem(DMLStatementParser.CopyGenericOptArgListItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#copyOptList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopyOptList(DMLStatementParser.CopyOptListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#copyOptItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopyOptItem(DMLStatementParser.CopyOptItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#copyDelimiter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopyDelimiter(DMLStatementParser.CopyDelimiterContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#fetch}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetch(DMLStatementParser.FetchContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#fetchArgs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetchArgs(DMLStatementParser.FetchArgsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#parameterMarker}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterMarker(DMLStatementParser.ParameterMarkerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#reservedKeyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReservedKeyword(DMLStatementParser.ReservedKeywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#numberLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberLiterals(DMLStatementParser.NumberLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#literalsType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteralsType(DMLStatementParser.LiteralsTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(DMLStatementParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#unicodeEscapes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnicodeEscapes(DMLStatementParser.UnicodeEscapesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#uescape}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUescape(DMLStatementParser.UescapeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#unreservedWord}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnreservedWord(DMLStatementParser.UnreservedWordContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#typeFuncNameKeyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeFuncNameKeyword(DMLStatementParser.TypeFuncNameKeywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#schemaName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaName(DMLStatementParser.SchemaNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#tableName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableName(DMLStatementParser.TableNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#columnName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnName(DMLStatementParser.ColumnNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#owner}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOwner(DMLStatementParser.OwnerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName(DMLStatementParser.NameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#tableNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableNames(DMLStatementParser.TableNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#columnNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNames(DMLStatementParser.ColumnNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#collationName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollationName(DMLStatementParser.CollationNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#indexName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexName(DMLStatementParser.IndexNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#primaryKey}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryKey(DMLStatementParser.PrimaryKeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#logicalOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOperator(DMLStatementParser.LogicalOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOperator(DMLStatementParser.ComparisonOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#patternMatchingOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPatternMatchingOperator(DMLStatementParser.PatternMatchingOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#cursorName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCursorName(DMLStatementParser.CursorNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#aExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAExpr(DMLStatementParser.AExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#bExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBExpr(DMLStatementParser.BExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#cExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCExpr(DMLStatementParser.CExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#indirection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndirection(DMLStatementParser.IndirectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#optIndirection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptIndirection(DMLStatementParser.OptIndirectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#indirectionEl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndirectionEl(DMLStatementParser.IndirectionElContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#sliceBound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSliceBound(DMLStatementParser.SliceBoundContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#inExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInExpr(DMLStatementParser.InExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#caseExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseExpr(DMLStatementParser.CaseExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#whenClauseList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhenClauseList(DMLStatementParser.WhenClauseListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#whenClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhenClause(DMLStatementParser.WhenClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#caseDefault}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseDefault(DMLStatementParser.CaseDefaultContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#caseArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseArg(DMLStatementParser.CaseArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#columnref}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnref(DMLStatementParser.ColumnrefContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#qualOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualOp(DMLStatementParser.QualOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#subqueryOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubqueryOp(DMLStatementParser.SubqueryOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#allOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAllOp(DMLStatementParser.AllOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#op}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOp(DMLStatementParser.OpContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#mathOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathOperator(DMLStatementParser.MathOperatorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonExtract}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonExtract(DMLStatementParser.JsonExtractContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonExtractText}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonExtractText(DMLStatementParser.JsonExtractTextContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonPathExtract}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonPathExtract(DMLStatementParser.JsonPathExtractContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonPathExtractText}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonPathExtractText(DMLStatementParser.JsonPathExtractTextContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbContainRight}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbContainRight(DMLStatementParser.JsonbContainRightContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbContainLeft}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbContainLeft(DMLStatementParser.JsonbContainLeftContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbContainTopKey}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbContainTopKey(DMLStatementParser.JsonbContainTopKeyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbContainAnyTopKey}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbContainAnyTopKey(DMLStatementParser.JsonbContainAnyTopKeyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbContainAllTopKey}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbContainAllTopKey(DMLStatementParser.JsonbContainAllTopKeyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbConcat}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbConcat(DMLStatementParser.JsonbConcatContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbDelete}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbDelete(DMLStatementParser.JsonbDeleteContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbPathDelete}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbPathDelete(DMLStatementParser.JsonbPathDeleteContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbPathContainAnyValue}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbPathContainAnyValue(DMLStatementParser.JsonbPathContainAnyValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jsonbPathPredicateCheck}
	 * labeled alternative in {@link DMLStatementParser#jsonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonbPathPredicateCheck(DMLStatementParser.JsonbPathPredicateCheckContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#qualAllOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualAllOp(DMLStatementParser.QualAllOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#ascDesc}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAscDesc(DMLStatementParser.AscDescContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#anyOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnyOperator(DMLStatementParser.AnyOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#windowExclusionClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowExclusionClause(DMLStatementParser.WindowExclusionClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#row}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRow(DMLStatementParser.RowContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#explicitRow}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplicitRow(DMLStatementParser.ExplicitRowContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#implicitRow}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImplicitRow(DMLStatementParser.ImplicitRowContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#subType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubType(DMLStatementParser.SubTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#arrayExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayExpr(DMLStatementParser.ArrayExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#arrayExprList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayExprList(DMLStatementParser.ArrayExprListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#funcArgList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncArgList(DMLStatementParser.FuncArgListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#paramName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParamName(DMLStatementParser.ParamNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#funcArgExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncArgExpr(DMLStatementParser.FuncArgExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#typeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeList(DMLStatementParser.TypeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#funcApplication}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncApplication(DMLStatementParser.FuncApplicationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#funcName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncName(DMLStatementParser.FuncNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#aexprConst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAexprConst(DMLStatementParser.AexprConstContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#colId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColId(DMLStatementParser.ColIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#typeFunctionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeFunctionName(DMLStatementParser.TypeFunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#functionTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionTable(DMLStatementParser.FunctionTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#xmlTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlTable(DMLStatementParser.XmlTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#xmlTableColumnList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlTableColumnList(DMLStatementParser.XmlTableColumnListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#xmlTableColumnEl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlTableColumnEl(DMLStatementParser.XmlTableColumnElContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#xmlTableColumnOptionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlTableColumnOptionList(DMLStatementParser.XmlTableColumnOptionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#xmlTableColumnOptionEl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlTableColumnOptionEl(DMLStatementParser.XmlTableColumnOptionElContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#xmlNamespaceList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlNamespaceList(DMLStatementParser.XmlNamespaceListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#xmlNamespaceEl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlNamespaceEl(DMLStatementParser.XmlNamespaceElContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#funcExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncExpr(DMLStatementParser.FuncExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#withinGroupClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithinGroupClause(DMLStatementParser.WithinGroupClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#filterClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFilterClause(DMLStatementParser.FilterClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#functionExprWindowless}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionExprWindowless(DMLStatementParser.FunctionExprWindowlessContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#ordinality}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrdinality(DMLStatementParser.OrdinalityContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#functionExprCommonSubexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionExprCommonSubexpr(DMLStatementParser.FunctionExprCommonSubexprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#typeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeName(DMLStatementParser.TypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#simpleTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleTypeName(DMLStatementParser.SimpleTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#exprList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprList(DMLStatementParser.ExprListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#extractList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtractList(DMLStatementParser.ExtractListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#extractArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtractArg(DMLStatementParser.ExtractArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#genericType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericType(DMLStatementParser.GenericTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#typeModifiers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeModifiers(DMLStatementParser.TypeModifiersContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#numeric}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumeric(DMLStatementParser.NumericContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#constDatetime}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstDatetime(DMLStatementParser.ConstDatetimeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#timezone}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimezone(DMLStatementParser.TimezoneContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#character}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacter(DMLStatementParser.CharacterContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#characterWithLength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterWithLength(DMLStatementParser.CharacterWithLengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#characterWithoutLength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterWithoutLength(DMLStatementParser.CharacterWithoutLengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#characterClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterClause(DMLStatementParser.CharacterClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#optFloat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptFloat(DMLStatementParser.OptFloatContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#attrs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttrs(DMLStatementParser.AttrsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#attrName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttrName(DMLStatementParser.AttrNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#colLable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColLable(DMLStatementParser.ColLableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#bit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBit(DMLStatementParser.BitContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#bitWithLength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitWithLength(DMLStatementParser.BitWithLengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#bitWithoutLength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitWithoutLength(DMLStatementParser.BitWithoutLengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#constInterval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstInterval(DMLStatementParser.ConstIntervalContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#optInterval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptInterval(DMLStatementParser.OptIntervalContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#optArrayBounds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptArrayBounds(DMLStatementParser.OptArrayBoundsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#intervalSecond}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalSecond(DMLStatementParser.IntervalSecondContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#unicodeNormalForm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnicodeNormalForm(DMLStatementParser.UnicodeNormalFormContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#trimList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrimList(DMLStatementParser.TrimListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#overlayList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOverlayList(DMLStatementParser.OverlayListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#overlayPlacing}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOverlayPlacing(DMLStatementParser.OverlayPlacingContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#substrFrom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubstrFrom(DMLStatementParser.SubstrFromContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#substrFor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubstrFor(DMLStatementParser.SubstrForContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#positionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPositionList(DMLStatementParser.PositionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#substrList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubstrList(DMLStatementParser.SubstrListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#xmlAttributes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlAttributes(DMLStatementParser.XmlAttributesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#xmlAttributeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlAttributeList(DMLStatementParser.XmlAttributeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#xmlAttributeEl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlAttributeEl(DMLStatementParser.XmlAttributeElContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#xmlExistsArgument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlExistsArgument(DMLStatementParser.XmlExistsArgumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#xmlPassingMech}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlPassingMech(DMLStatementParser.XmlPassingMechContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#documentOrContent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocumentOrContent(DMLStatementParser.DocumentOrContentContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#xmlWhitespaceOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlWhitespaceOption(DMLStatementParser.XmlWhitespaceOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#xmlRootVersion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlRootVersion(DMLStatementParser.XmlRootVersionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#xmlRootStandalone}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlRootStandalone(DMLStatementParser.XmlRootStandaloneContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#rowsFromItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowsFromItem(DMLStatementParser.RowsFromItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#rowsFromList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowsFromList(DMLStatementParser.RowsFromListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#columnDefList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnDefList(DMLStatementParser.ColumnDefListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#tableFuncElementList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableFuncElementList(DMLStatementParser.TableFuncElementListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#tableFuncElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableFuncElement(DMLStatementParser.TableFuncElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#collateClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollateClause(DMLStatementParser.CollateClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#anyName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnyName(DMLStatementParser.AnyNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#aliasClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAliasClause(DMLStatementParser.AliasClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#nameList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNameList(DMLStatementParser.NameListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#funcAliasClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncAliasClause(DMLStatementParser.FuncAliasClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#tablesampleClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablesampleClause(DMLStatementParser.TablesampleClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#repeatableClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRepeatableClause(DMLStatementParser.RepeatableClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#allOrDistinct}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAllOrDistinct(DMLStatementParser.AllOrDistinctContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#sortClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSortClause(DMLStatementParser.SortClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#sortbyList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSortbyList(DMLStatementParser.SortbyListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#sortby}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSortby(DMLStatementParser.SortbyContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#nullsOrder}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullsOrder(DMLStatementParser.NullsOrderContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#distinctClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDistinctClause(DMLStatementParser.DistinctClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#distinct}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDistinct(DMLStatementParser.DistinctContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#overClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOverClause(DMLStatementParser.OverClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#windowName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowName(DMLStatementParser.WindowNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#indexParams}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexParams(DMLStatementParser.IndexParamsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#indexElemOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexElemOptions(DMLStatementParser.IndexElemOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#indexElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexElem(DMLStatementParser.IndexElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#collate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollate(DMLStatementParser.CollateContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#optClass}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptClass(DMLStatementParser.OptClassContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#reloptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReloptions(DMLStatementParser.ReloptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#reloptionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReloptionList(DMLStatementParser.ReloptionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#reloptionElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReloptionElem(DMLStatementParser.ReloptionElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#defArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefArg(DMLStatementParser.DefArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#funcType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncType(DMLStatementParser.FuncTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataType(DMLStatementParser.DataTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#dataTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataTypeName(DMLStatementParser.DataTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#dataTypeLength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataTypeLength(DMLStatementParser.DataTypeLengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#characterSet}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterSet(DMLStatementParser.CharacterSetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#ignoredIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIgnoredIdentifier(DMLStatementParser.IgnoredIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#ignoredIdentifiers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIgnoredIdentifiers(DMLStatementParser.IgnoredIdentifiersContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#signedIconst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSignedIconst(DMLStatementParser.SignedIconstContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#booleanOrString}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanOrString(DMLStatementParser.BooleanOrStringContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#nonReservedWord}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonReservedWord(DMLStatementParser.NonReservedWordContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#colNameKeyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColNameKeyword(DMLStatementParser.ColNameKeywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#databaseName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDatabaseName(DMLStatementParser.DatabaseNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#roleSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoleSpec(DMLStatementParser.RoleSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#varName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarName(DMLStatementParser.VarNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#varList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarList(DMLStatementParser.VarListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#varValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarValue(DMLStatementParser.VarValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#zoneValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitZoneValue(DMLStatementParser.ZoneValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#numericOnly}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericOnly(DMLStatementParser.NumericOnlyContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#isoLevel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsoLevel(DMLStatementParser.IsoLevelContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#columnDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnDef(DMLStatementParser.ColumnDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#colQualList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColQualList(DMLStatementParser.ColQualListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#colConstraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColConstraint(DMLStatementParser.ColConstraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#constraintAttr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintAttr(DMLStatementParser.ConstraintAttrContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#colConstraintElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColConstraintElem(DMLStatementParser.ColConstraintElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#parenthesizedSeqOptList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenthesizedSeqOptList(DMLStatementParser.ParenthesizedSeqOptListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#seqOptList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSeqOptList(DMLStatementParser.SeqOptListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#seqOptElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSeqOptElem(DMLStatementParser.SeqOptElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#optColumnList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptColumnList(DMLStatementParser.OptColumnListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#columnElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnElem(DMLStatementParser.ColumnElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#columnList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnList(DMLStatementParser.ColumnListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#generatedWhen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGeneratedWhen(DMLStatementParser.GeneratedWhenContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#noInherit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNoInherit(DMLStatementParser.NoInheritContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#consTableSpace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConsTableSpace(DMLStatementParser.ConsTableSpaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefinition(DMLStatementParser.DefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#defList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefList(DMLStatementParser.DefListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#defElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefElem(DMLStatementParser.DefElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#colLabel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColLabel(DMLStatementParser.ColLabelContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#keyActions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyActions(DMLStatementParser.KeyActionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#keyDelete}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyDelete(DMLStatementParser.KeyDeleteContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#keyUpdate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyUpdate(DMLStatementParser.KeyUpdateContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#keyAction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyAction(DMLStatementParser.KeyActionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#keyMatch}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyMatch(DMLStatementParser.KeyMatchContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#createGenericOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateGenericOptions(DMLStatementParser.CreateGenericOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#genericOptionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericOptionList(DMLStatementParser.GenericOptionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#genericOptionElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericOptionElem(DMLStatementParser.GenericOptionElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#genericOptionArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericOptionArg(DMLStatementParser.GenericOptionArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#genericOptionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericOptionName(DMLStatementParser.GenericOptionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#replicaIdentity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReplicaIdentity(DMLStatementParser.ReplicaIdentityContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#operArgtypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperArgtypes(DMLStatementParser.OperArgtypesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#funcArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncArg(DMLStatementParser.FuncArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#argClass}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgClass(DMLStatementParser.ArgClassContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#funcArgsList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncArgsList(DMLStatementParser.FuncArgsListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#nonReservedWordOrSconst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonReservedWordOrSconst(DMLStatementParser.NonReservedWordOrSconstContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#fileName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFileName(DMLStatementParser.FileNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#roleList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoleList(DMLStatementParser.RoleListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#setResetClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetResetClause(DMLStatementParser.SetResetClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#setRest}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetRest(DMLStatementParser.SetRestContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#transactionModeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransactionModeList(DMLStatementParser.TransactionModeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#transactionModeItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransactionModeItem(DMLStatementParser.TransactionModeItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#setRestMore}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetRestMore(DMLStatementParser.SetRestMoreContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#encoding}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEncoding(DMLStatementParser.EncodingContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#genericSet}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericSet(DMLStatementParser.GenericSetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#variableResetStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableResetStmt(DMLStatementParser.VariableResetStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#resetRest}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResetRest(DMLStatementParser.ResetRestContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#genericReset}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericReset(DMLStatementParser.GenericResetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#relationExprList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationExprList(DMLStatementParser.RelationExprListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#commonFuncOptItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommonFuncOptItem(DMLStatementParser.CommonFuncOptItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#functionSetResetClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionSetResetClause(DMLStatementParser.FunctionSetResetClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#rowSecurityCmd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowSecurityCmd(DMLStatementParser.RowSecurityCmdContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#event}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEvent(DMLStatementParser.EventContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#typeNameList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeNameList(DMLStatementParser.TypeNameListContext ctx);
}