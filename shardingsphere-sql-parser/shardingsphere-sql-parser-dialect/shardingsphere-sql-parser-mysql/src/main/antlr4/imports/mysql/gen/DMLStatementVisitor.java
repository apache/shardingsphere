// Generated from /home/guimy/github/shardingsphere/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-mysql/src/main/antlr4/imports/mysql/DMLStatement.g4 by ANTLR 4.9.1
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
	 * Visit a parse tree produced by {@link DMLStatementParser#insertSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertSpecification(DMLStatementParser.InsertSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#insertValuesClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertValuesClause(DMLStatementParser.InsertValuesClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#fields}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFields(DMLStatementParser.FieldsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#insertIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertIdentifier(DMLStatementParser.InsertIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#tableWild}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableWild(DMLStatementParser.TableWildContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#insertSelectClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertSelectClause(DMLStatementParser.InsertSelectClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#onDuplicateKeyClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOnDuplicateKeyClause(DMLStatementParser.OnDuplicateKeyClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#valueReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValueReference(DMLStatementParser.ValueReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#derivedColumns}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDerivedColumns(DMLStatementParser.DerivedColumnsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#replace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReplace(DMLStatementParser.ReplaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#replaceSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReplaceSpecification(DMLStatementParser.ReplaceSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#replaceValuesClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReplaceValuesClause(DMLStatementParser.ReplaceValuesClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#replaceSelectClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReplaceSelectClause(DMLStatementParser.ReplaceSelectClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#update}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdate(DMLStatementParser.UpdateContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#updateSpecification_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdateSpecification_(DMLStatementParser.UpdateSpecification_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(DMLStatementParser.AssignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#setAssignmentsClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetAssignmentsClause(DMLStatementParser.SetAssignmentsClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#assignmentValues}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentValues(DMLStatementParser.AssignmentValuesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#assignmentValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentValue(DMLStatementParser.AssignmentValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#blobValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlobValue(DMLStatementParser.BlobValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#delete}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDelete(DMLStatementParser.DeleteContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#deleteSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeleteSpecification(DMLStatementParser.DeleteSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#singleTableClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleTableClause(DMLStatementParser.SingleTableClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#multipleTablesClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultipleTablesClause(DMLStatementParser.MultipleTablesClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#select}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect(DMLStatementParser.SelectContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#selectWithInto}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectWithInto(DMLStatementParser.SelectWithIntoContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#queryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryExpression(DMLStatementParser.QueryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#queryExpressionBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryExpressionBody(DMLStatementParser.QueryExpressionBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#queryExpressionParens}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryExpressionParens(DMLStatementParser.QueryExpressionParensContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#queryPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryPrimary(DMLStatementParser.QueryPrimaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#querySpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuerySpecification(DMLStatementParser.QuerySpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#call}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCall(DMLStatementParser.CallContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#doStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDoStatement(DMLStatementParser.DoStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#handlerStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHandlerStatement(DMLStatementParser.HandlerStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#handlerOpenStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHandlerOpenStatement(DMLStatementParser.HandlerOpenStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#handlerReadIndexStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHandlerReadIndexStatement(DMLStatementParser.HandlerReadIndexStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#handlerReadStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHandlerReadStatement(DMLStatementParser.HandlerReadStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#handlerCloseStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHandlerCloseStatement(DMLStatementParser.HandlerCloseStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#importStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportStatement(DMLStatementParser.ImportStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#loadStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoadStatement(DMLStatementParser.LoadStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#loadDataStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoadDataStatement(DMLStatementParser.LoadDataStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#loadXmlStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoadXmlStatement(DMLStatementParser.LoadXmlStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#explicitTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplicitTable(DMLStatementParser.ExplicitTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#tableValueConstructor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableValueConstructor(DMLStatementParser.TableValueConstructorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#rowConstructorList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowConstructorList(DMLStatementParser.RowConstructorListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#withClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithClause(DMLStatementParser.WithClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#cteClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCteClause(DMLStatementParser.CteClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#selectSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectSpecification(DMLStatementParser.SelectSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#duplicateSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDuplicateSpecification(DMLStatementParser.DuplicateSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#projections}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProjections(DMLStatementParser.ProjectionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#projection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProjection(DMLStatementParser.ProjectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#unqualifiedShorthand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnqualifiedShorthand(DMLStatementParser.UnqualifiedShorthandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#qualifiedShorthand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedShorthand(DMLStatementParser.QualifiedShorthandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#fromClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromClause(DMLStatementParser.FromClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#tableReferences}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableReferences(DMLStatementParser.TableReferencesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#escapedTableReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEscapedTableReference(DMLStatementParser.EscapedTableReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#tableReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableReference(DMLStatementParser.TableReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#tableFactor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableFactor(DMLStatementParser.TableFactorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#partitionNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionNames(DMLStatementParser.PartitionNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#indexHintList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexHintList(DMLStatementParser.IndexHintListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#indexHint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexHint(DMLStatementParser.IndexHintContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#joinedTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinedTable(DMLStatementParser.JoinedTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#innerJoinType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInnerJoinType(DMLStatementParser.InnerJoinTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#outerJoinType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOuterJoinType(DMLStatementParser.OuterJoinTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#naturalJoinType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNaturalJoinType(DMLStatementParser.NaturalJoinTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#joinSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinSpecification(DMLStatementParser.JoinSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#whereClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhereClause(DMLStatementParser.WhereClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#groupByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupByClause(DMLStatementParser.GroupByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#havingClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingClause(DMLStatementParser.HavingClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#limitClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimitClause(DMLStatementParser.LimitClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#limitRowCount}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimitRowCount(DMLStatementParser.LimitRowCountContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#limitOffset}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimitOffset(DMLStatementParser.LimitOffsetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#windowClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowClause(DMLStatementParser.WindowClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#windowItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowItem(DMLStatementParser.WindowItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#subquery}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubquery(DMLStatementParser.SubqueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#selectLinesInto}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectLinesInto(DMLStatementParser.SelectLinesIntoContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#selectFieldsInto}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectFieldsInto(DMLStatementParser.SelectFieldsIntoContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#selectIntoExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectIntoExpression(DMLStatementParser.SelectIntoExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#lockClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLockClause(DMLStatementParser.LockClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#lockClauseList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLockClauseList(DMLStatementParser.LockClauseListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#lockStrength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLockStrength(DMLStatementParser.LockStrengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#lockedRowAction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLockedRowAction(DMLStatementParser.LockedRowActionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#tableLockingList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableLockingList(DMLStatementParser.TableLockingListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#tableIdentOptWild}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableIdentOptWild(DMLStatementParser.TableIdentOptWildContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#tableAliasRefList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableAliasRefList(DMLStatementParser.TableAliasRefListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#parameterMarker}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterMarker(DMLStatementParser.ParameterMarkerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#customKeyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCustomKeyword(DMLStatementParser.CustomKeywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#literals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiterals(DMLStatementParser.LiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#string_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitString_(DMLStatementParser.String_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#stringLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringLiterals(DMLStatementParser.StringLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#numberLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberLiterals(DMLStatementParser.NumberLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#temporalLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTemporalLiterals(DMLStatementParser.TemporalLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#hexadecimalLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHexadecimalLiterals(DMLStatementParser.HexadecimalLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#bitValueLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitValueLiterals(DMLStatementParser.BitValueLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#booleanLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanLiterals(DMLStatementParser.BooleanLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#nullValueLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullValueLiterals(DMLStatementParser.NullValueLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#collationName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollationName(DMLStatementParser.CollationNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(DMLStatementParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#identifierKeywordsUnambiguous}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierKeywordsUnambiguous(DMLStatementParser.IdentifierKeywordsUnambiguousContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#identifierKeywordsAmbiguous1RolesAndLabels}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierKeywordsAmbiguous1RolesAndLabels(DMLStatementParser.IdentifierKeywordsAmbiguous1RolesAndLabelsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#identifierKeywordsAmbiguous2Labels}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierKeywordsAmbiguous2Labels(DMLStatementParser.IdentifierKeywordsAmbiguous2LabelsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#identifierKeywordsAmbiguous3Roles}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierKeywordsAmbiguous3Roles(DMLStatementParser.IdentifierKeywordsAmbiguous3RolesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#identifierKeywordsAmbiguous4SystemVariables}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierKeywordsAmbiguous4SystemVariables(DMLStatementParser.IdentifierKeywordsAmbiguous4SystemVariablesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#textOrIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTextOrIdentifier(DMLStatementParser.TextOrIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable(DMLStatementParser.VariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#userVariable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUserVariable(DMLStatementParser.UserVariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#systemVariable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSystemVariable(DMLStatementParser.SystemVariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#setSystemVariable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetSystemVariable(DMLStatementParser.SetSystemVariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#optionType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptionType(DMLStatementParser.OptionTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#internalVariableName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInternalVariableName(DMLStatementParser.InternalVariableNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#setExprOrDefault}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetExprOrDefault(DMLStatementParser.SetExprOrDefaultContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#transactionCharacteristics}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransactionCharacteristics(DMLStatementParser.TransactionCharacteristicsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#isolationLevel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsolationLevel(DMLStatementParser.IsolationLevelContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#isolationTypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsolationTypes(DMLStatementParser.IsolationTypesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#transactionAccessMode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransactionAccessMode(DMLStatementParser.TransactionAccessModeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#schemaName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaName(DMLStatementParser.SchemaNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#schemaNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaNames(DMLStatementParser.SchemaNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#charsetName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharsetName(DMLStatementParser.CharsetNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#schemaPairs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaPairs(DMLStatementParser.SchemaPairsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#schemaPair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaPair(DMLStatementParser.SchemaPairContext ctx);
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
	 * Visit a parse tree produced by {@link DMLStatementParser#indexName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexName(DMLStatementParser.IndexNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#userIdentifierOrText}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUserIdentifierOrText(DMLStatementParser.UserIdentifierOrTextContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#userName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUserName(DMLStatementParser.UserNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#eventName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEventName(DMLStatementParser.EventNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#serverName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitServerName(DMLStatementParser.ServerNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#wrapperName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWrapperName(DMLStatementParser.WrapperNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#functionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionName(DMLStatementParser.FunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#viewName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitViewName(DMLStatementParser.ViewNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#owner}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOwner(DMLStatementParser.OwnerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlias(DMLStatementParser.AliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName(DMLStatementParser.NameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#tableList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableList(DMLStatementParser.TableListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#viewNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitViewNames(DMLStatementParser.ViewNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#columnNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNames(DMLStatementParser.ColumnNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#groupName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupName(DMLStatementParser.GroupNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#routineName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoutineName(DMLStatementParser.RoutineNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#shardLibraryName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShardLibraryName(DMLStatementParser.ShardLibraryNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#componentName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComponentName(DMLStatementParser.ComponentNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#pluginName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPluginName(DMLStatementParser.PluginNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#hostName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHostName(DMLStatementParser.HostNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#port}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPort(DMLStatementParser.PortContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#cloneInstance}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCloneInstance(DMLStatementParser.CloneInstanceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#cloneDir}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCloneDir(DMLStatementParser.CloneDirContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#channelName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChannelName(DMLStatementParser.ChannelNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#logName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogName(DMLStatementParser.LogNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#roleName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoleName(DMLStatementParser.RoleNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#roleIdentifierOrText}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoleIdentifierOrText(DMLStatementParser.RoleIdentifierOrTextContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#engineRef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEngineRef(DMLStatementParser.EngineRefContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#triggerName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerName(DMLStatementParser.TriggerNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#triggerTime}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerTime(DMLStatementParser.TriggerTimeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#tableOrTables}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableOrTables(DMLStatementParser.TableOrTablesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#userOrRole}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUserOrRole(DMLStatementParser.UserOrRoleContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#partitionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionName(DMLStatementParser.PartitionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#identifierList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierList(DMLStatementParser.IdentifierListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#allOrPartitionNameList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAllOrPartitionNameList(DMLStatementParser.AllOrPartitionNameListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#triggerEvent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerEvent(DMLStatementParser.TriggerEventContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#triggerOrder}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerOrder(DMLStatementParser.TriggerOrderContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(DMLStatementParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#logicalOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOperator(DMLStatementParser.LogicalOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#notOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotOperator(DMLStatementParser.NotOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#booleanPrimary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanPrimary(DMLStatementParser.BooleanPrimaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOperator(DMLStatementParser.ComparisonOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate(DMLStatementParser.PredicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#bitExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitExpr(DMLStatementParser.BitExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#simpleExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleExpr(DMLStatementParser.SimpleExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#columnRef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnRef(DMLStatementParser.ColumnRefContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#columnRefList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnRefList(DMLStatementParser.ColumnRefListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCall(DMLStatementParser.FunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#aggregationFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregationFunction(DMLStatementParser.AggregationFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#aggregationFunctionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregationFunctionName(DMLStatementParser.AggregationFunctionNameContext ctx);
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
	 * Visit a parse tree produced by {@link DMLStatementParser#windowSpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowSpecification(DMLStatementParser.WindowSpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#frameClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrameClause(DMLStatementParser.FrameClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#frameStart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrameStart(DMLStatementParser.FrameStartContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#frameEnd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrameEnd(DMLStatementParser.FrameEndContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#frameBetween}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrameBetween(DMLStatementParser.FrameBetweenContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#specialFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecialFunction(DMLStatementParser.SpecialFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#currentUserFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCurrentUserFunction(DMLStatementParser.CurrentUserFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#groupConcatFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupConcatFunction(DMLStatementParser.GroupConcatFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#windowFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowFunction(DMLStatementParser.WindowFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#windowingClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowingClause(DMLStatementParser.WindowingClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#leadLagInfo}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLeadLagInfo(DMLStatementParser.LeadLagInfoContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#nullTreatment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullTreatment(DMLStatementParser.NullTreatmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#checkType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCheckType(DMLStatementParser.CheckTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#repairType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRepairType(DMLStatementParser.RepairTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#castFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCastFunction(DMLStatementParser.CastFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#convertFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConvertFunction(DMLStatementParser.ConvertFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#castType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCastType(DMLStatementParser.CastTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#nchar}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNchar(DMLStatementParser.NcharContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#positionFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPositionFunction(DMLStatementParser.PositionFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#substringFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubstringFunction(DMLStatementParser.SubstringFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#extractFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtractFunction(DMLStatementParser.ExtractFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#charFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharFunction(DMLStatementParser.CharFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#trimFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrimFunction(DMLStatementParser.TrimFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#valuesFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValuesFunction(DMLStatementParser.ValuesFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#weightStringFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWeightStringFunction(DMLStatementParser.WeightStringFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#levelClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLevelClause(DMLStatementParser.LevelClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#levelInWeightListElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLevelInWeightListElement(DMLStatementParser.LevelInWeightListElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#regularFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRegularFunction(DMLStatementParser.RegularFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#shorthandRegularFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShorthandRegularFunction(DMLStatementParser.ShorthandRegularFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#completeRegularFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompleteRegularFunction(DMLStatementParser.CompleteRegularFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#regularFunctionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRegularFunctionName(DMLStatementParser.RegularFunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#matchExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatchExpression(DMLStatementParser.MatchExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#matchSearchModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatchSearchModifier(DMLStatementParser.MatchSearchModifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#caseExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseExpression(DMLStatementParser.CaseExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#datetimeExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDatetimeExpr(DMLStatementParser.DatetimeExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#binaryLogFileIndexNumber}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryLogFileIndexNumber(DMLStatementParser.BinaryLogFileIndexNumberContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#caseWhen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseWhen(DMLStatementParser.CaseWhenContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#caseElse}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseElse(DMLStatementParser.CaseElseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#intervalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalExpression(DMLStatementParser.IntervalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#intervalValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalValue(DMLStatementParser.IntervalValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#intervalUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalUnit(DMLStatementParser.IntervalUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#orderByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByClause(DMLStatementParser.OrderByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#orderByItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByItem(DMLStatementParser.OrderByItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataType(DMLStatementParser.DataTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#stringList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringList(DMLStatementParser.StringListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#textString}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTextString(DMLStatementParser.TextStringContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#textStringHash}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTextStringHash(DMLStatementParser.TextStringHashContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#fieldOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldOptions(DMLStatementParser.FieldOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#precision}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecision(DMLStatementParser.PrecisionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#typeDatetimePrecision}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeDatetimePrecision(DMLStatementParser.TypeDatetimePrecisionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#charsetWithOptBinary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharsetWithOptBinary(DMLStatementParser.CharsetWithOptBinaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#ascii}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAscii(DMLStatementParser.AsciiContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#unicode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnicode(DMLStatementParser.UnicodeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#charset}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharset(DMLStatementParser.CharsetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#defaultCollation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefaultCollation(DMLStatementParser.DefaultCollationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#defaultEncryption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefaultEncryption(DMLStatementParser.DefaultEncryptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#defaultCharset}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefaultCharset(DMLStatementParser.DefaultCharsetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#signedLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSignedLiteral(DMLStatementParser.SignedLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#now}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNow(DMLStatementParser.NowContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#columnFormat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnFormat(DMLStatementParser.ColumnFormatContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#storageMedia}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStorageMedia(DMLStatementParser.StorageMediaContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#direction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirection(DMLStatementParser.DirectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#keyOrIndex}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyOrIndex(DMLStatementParser.KeyOrIndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#fieldLength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldLength(DMLStatementParser.FieldLengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#characterSet}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterSet(DMLStatementParser.CharacterSetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#collateClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollateClause(DMLStatementParser.CollateClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#fieldOrVarSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldOrVarSpec(DMLStatementParser.FieldOrVarSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#notExistClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotExistClause(DMLStatementParser.NotExistClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#existClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExistClause(DMLStatementParser.ExistClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#connectionId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConnectionId(DMLStatementParser.ConnectionIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#labelName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLabelName(DMLStatementParser.LabelNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#cursorName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCursorName(DMLStatementParser.CursorNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#conditionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditionName(DMLStatementParser.ConditionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#unionOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnionOption(DMLStatementParser.UnionOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#noWriteToBinLog}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNoWriteToBinLog(DMLStatementParser.NoWriteToBinLogContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#channelOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChannelOption(DMLStatementParser.ChannelOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#preparedStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPreparedStatement(DMLStatementParser.PreparedStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#executeStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExecuteStatement(DMLStatementParser.ExecuteStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DMLStatementParser#executeVarList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExecuteVarList(DMLStatementParser.ExecuteVarListContext ctx);
}