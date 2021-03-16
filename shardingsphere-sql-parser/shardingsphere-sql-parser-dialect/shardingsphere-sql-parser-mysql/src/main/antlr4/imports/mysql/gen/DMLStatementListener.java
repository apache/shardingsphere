// Generated from /home/guimy/github/shardingsphere/shardingsphere-sql-parser/shardingsphere-sql-parser-dialect/shardingsphere-sql-parser-mysql/src/main/antlr4/imports/mysql/DMLStatement.g4 by ANTLR 4.9.1
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
	 * Enter a parse tree produced by {@link DMLStatementParser#insertSpecification}.
	 * @param ctx the parse tree
	 */
	void enterInsertSpecification(DMLStatementParser.InsertSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#insertSpecification}.
	 * @param ctx the parse tree
	 */
	void exitInsertSpecification(DMLStatementParser.InsertSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#insertValuesClause}.
	 * @param ctx the parse tree
	 */
	void enterInsertValuesClause(DMLStatementParser.InsertValuesClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#insertValuesClause}.
	 * @param ctx the parse tree
	 */
	void exitInsertValuesClause(DMLStatementParser.InsertValuesClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#fields}.
	 * @param ctx the parse tree
	 */
	void enterFields(DMLStatementParser.FieldsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#fields}.
	 * @param ctx the parse tree
	 */
	void exitFields(DMLStatementParser.FieldsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#insertIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterInsertIdentifier(DMLStatementParser.InsertIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#insertIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitInsertIdentifier(DMLStatementParser.InsertIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#tableWild}.
	 * @param ctx the parse tree
	 */
	void enterTableWild(DMLStatementParser.TableWildContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#tableWild}.
	 * @param ctx the parse tree
	 */
	void exitTableWild(DMLStatementParser.TableWildContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#insertSelectClause}.
	 * @param ctx the parse tree
	 */
	void enterInsertSelectClause(DMLStatementParser.InsertSelectClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#insertSelectClause}.
	 * @param ctx the parse tree
	 */
	void exitInsertSelectClause(DMLStatementParser.InsertSelectClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#onDuplicateKeyClause}.
	 * @param ctx the parse tree
	 */
	void enterOnDuplicateKeyClause(DMLStatementParser.OnDuplicateKeyClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#onDuplicateKeyClause}.
	 * @param ctx the parse tree
	 */
	void exitOnDuplicateKeyClause(DMLStatementParser.OnDuplicateKeyClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#valueReference}.
	 * @param ctx the parse tree
	 */
	void enterValueReference(DMLStatementParser.ValueReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#valueReference}.
	 * @param ctx the parse tree
	 */
	void exitValueReference(DMLStatementParser.ValueReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#derivedColumns}.
	 * @param ctx the parse tree
	 */
	void enterDerivedColumns(DMLStatementParser.DerivedColumnsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#derivedColumns}.
	 * @param ctx the parse tree
	 */
	void exitDerivedColumns(DMLStatementParser.DerivedColumnsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#replace}.
	 * @param ctx the parse tree
	 */
	void enterReplace(DMLStatementParser.ReplaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#replace}.
	 * @param ctx the parse tree
	 */
	void exitReplace(DMLStatementParser.ReplaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#replaceSpecification}.
	 * @param ctx the parse tree
	 */
	void enterReplaceSpecification(DMLStatementParser.ReplaceSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#replaceSpecification}.
	 * @param ctx the parse tree
	 */
	void exitReplaceSpecification(DMLStatementParser.ReplaceSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#replaceValuesClause}.
	 * @param ctx the parse tree
	 */
	void enterReplaceValuesClause(DMLStatementParser.ReplaceValuesClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#replaceValuesClause}.
	 * @param ctx the parse tree
	 */
	void exitReplaceValuesClause(DMLStatementParser.ReplaceValuesClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#replaceSelectClause}.
	 * @param ctx the parse tree
	 */
	void enterReplaceSelectClause(DMLStatementParser.ReplaceSelectClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#replaceSelectClause}.
	 * @param ctx the parse tree
	 */
	void exitReplaceSelectClause(DMLStatementParser.ReplaceSelectClauseContext ctx);
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
	 * Enter a parse tree produced by {@link DMLStatementParser#updateSpecification_}.
	 * @param ctx the parse tree
	 */
	void enterUpdateSpecification_(DMLStatementParser.UpdateSpecification_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#updateSpecification_}.
	 * @param ctx the parse tree
	 */
	void exitUpdateSpecification_(DMLStatementParser.UpdateSpecification_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(DMLStatementParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(DMLStatementParser.AssignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#setAssignmentsClause}.
	 * @param ctx the parse tree
	 */
	void enterSetAssignmentsClause(DMLStatementParser.SetAssignmentsClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#setAssignmentsClause}.
	 * @param ctx the parse tree
	 */
	void exitSetAssignmentsClause(DMLStatementParser.SetAssignmentsClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#assignmentValues}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentValues(DMLStatementParser.AssignmentValuesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#assignmentValues}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentValues(DMLStatementParser.AssignmentValuesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentValue(DMLStatementParser.AssignmentValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#assignmentValue}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentValue(DMLStatementParser.AssignmentValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#blobValue}.
	 * @param ctx the parse tree
	 */
	void enterBlobValue(DMLStatementParser.BlobValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#blobValue}.
	 * @param ctx the parse tree
	 */
	void exitBlobValue(DMLStatementParser.BlobValueContext ctx);
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
	 * Enter a parse tree produced by {@link DMLStatementParser#deleteSpecification}.
	 * @param ctx the parse tree
	 */
	void enterDeleteSpecification(DMLStatementParser.DeleteSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#deleteSpecification}.
	 * @param ctx the parse tree
	 */
	void exitDeleteSpecification(DMLStatementParser.DeleteSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#singleTableClause}.
	 * @param ctx the parse tree
	 */
	void enterSingleTableClause(DMLStatementParser.SingleTableClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#singleTableClause}.
	 * @param ctx the parse tree
	 */
	void exitSingleTableClause(DMLStatementParser.SingleTableClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#multipleTablesClause}.
	 * @param ctx the parse tree
	 */
	void enterMultipleTablesClause(DMLStatementParser.MultipleTablesClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#multipleTablesClause}.
	 * @param ctx the parse tree
	 */
	void exitMultipleTablesClause(DMLStatementParser.MultipleTablesClauseContext ctx);
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
	 * Enter a parse tree produced by {@link DMLStatementParser#selectWithInto}.
	 * @param ctx the parse tree
	 */
	void enterSelectWithInto(DMLStatementParser.SelectWithIntoContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#selectWithInto}.
	 * @param ctx the parse tree
	 */
	void exitSelectWithInto(DMLStatementParser.SelectWithIntoContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#queryExpression}.
	 * @param ctx the parse tree
	 */
	void enterQueryExpression(DMLStatementParser.QueryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#queryExpression}.
	 * @param ctx the parse tree
	 */
	void exitQueryExpression(DMLStatementParser.QueryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#queryExpressionBody}.
	 * @param ctx the parse tree
	 */
	void enterQueryExpressionBody(DMLStatementParser.QueryExpressionBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#queryExpressionBody}.
	 * @param ctx the parse tree
	 */
	void exitQueryExpressionBody(DMLStatementParser.QueryExpressionBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#queryExpressionParens}.
	 * @param ctx the parse tree
	 */
	void enterQueryExpressionParens(DMLStatementParser.QueryExpressionParensContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#queryExpressionParens}.
	 * @param ctx the parse tree
	 */
	void exitQueryExpressionParens(DMLStatementParser.QueryExpressionParensContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#queryPrimary}.
	 * @param ctx the parse tree
	 */
	void enterQueryPrimary(DMLStatementParser.QueryPrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#queryPrimary}.
	 * @param ctx the parse tree
	 */
	void exitQueryPrimary(DMLStatementParser.QueryPrimaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#querySpecification}.
	 * @param ctx the parse tree
	 */
	void enterQuerySpecification(DMLStatementParser.QuerySpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#querySpecification}.
	 * @param ctx the parse tree
	 */
	void exitQuerySpecification(DMLStatementParser.QuerySpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#call}.
	 * @param ctx the parse tree
	 */
	void enterCall(DMLStatementParser.CallContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#call}.
	 * @param ctx the parse tree
	 */
	void exitCall(DMLStatementParser.CallContext ctx);
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
	 * Enter a parse tree produced by {@link DMLStatementParser#handlerStatement}.
	 * @param ctx the parse tree
	 */
	void enterHandlerStatement(DMLStatementParser.HandlerStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#handlerStatement}.
	 * @param ctx the parse tree
	 */
	void exitHandlerStatement(DMLStatementParser.HandlerStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#handlerOpenStatement}.
	 * @param ctx the parse tree
	 */
	void enterHandlerOpenStatement(DMLStatementParser.HandlerOpenStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#handlerOpenStatement}.
	 * @param ctx the parse tree
	 */
	void exitHandlerOpenStatement(DMLStatementParser.HandlerOpenStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#handlerReadIndexStatement}.
	 * @param ctx the parse tree
	 */
	void enterHandlerReadIndexStatement(DMLStatementParser.HandlerReadIndexStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#handlerReadIndexStatement}.
	 * @param ctx the parse tree
	 */
	void exitHandlerReadIndexStatement(DMLStatementParser.HandlerReadIndexStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#handlerReadStatement}.
	 * @param ctx the parse tree
	 */
	void enterHandlerReadStatement(DMLStatementParser.HandlerReadStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#handlerReadStatement}.
	 * @param ctx the parse tree
	 */
	void exitHandlerReadStatement(DMLStatementParser.HandlerReadStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#handlerCloseStatement}.
	 * @param ctx the parse tree
	 */
	void enterHandlerCloseStatement(DMLStatementParser.HandlerCloseStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#handlerCloseStatement}.
	 * @param ctx the parse tree
	 */
	void exitHandlerCloseStatement(DMLStatementParser.HandlerCloseStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#importStatement}.
	 * @param ctx the parse tree
	 */
	void enterImportStatement(DMLStatementParser.ImportStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#importStatement}.
	 * @param ctx the parse tree
	 */
	void exitImportStatement(DMLStatementParser.ImportStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#loadStatement}.
	 * @param ctx the parse tree
	 */
	void enterLoadStatement(DMLStatementParser.LoadStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#loadStatement}.
	 * @param ctx the parse tree
	 */
	void exitLoadStatement(DMLStatementParser.LoadStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#loadDataStatement}.
	 * @param ctx the parse tree
	 */
	void enterLoadDataStatement(DMLStatementParser.LoadDataStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#loadDataStatement}.
	 * @param ctx the parse tree
	 */
	void exitLoadDataStatement(DMLStatementParser.LoadDataStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#loadXmlStatement}.
	 * @param ctx the parse tree
	 */
	void enterLoadXmlStatement(DMLStatementParser.LoadXmlStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#loadXmlStatement}.
	 * @param ctx the parse tree
	 */
	void exitLoadXmlStatement(DMLStatementParser.LoadXmlStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#explicitTable}.
	 * @param ctx the parse tree
	 */
	void enterExplicitTable(DMLStatementParser.ExplicitTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#explicitTable}.
	 * @param ctx the parse tree
	 */
	void exitExplicitTable(DMLStatementParser.ExplicitTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#tableValueConstructor}.
	 * @param ctx the parse tree
	 */
	void enterTableValueConstructor(DMLStatementParser.TableValueConstructorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#tableValueConstructor}.
	 * @param ctx the parse tree
	 */
	void exitTableValueConstructor(DMLStatementParser.TableValueConstructorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#rowConstructorList}.
	 * @param ctx the parse tree
	 */
	void enterRowConstructorList(DMLStatementParser.RowConstructorListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#rowConstructorList}.
	 * @param ctx the parse tree
	 */
	void exitRowConstructorList(DMLStatementParser.RowConstructorListContext ctx);
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
	 * Enter a parse tree produced by {@link DMLStatementParser#cteClause}.
	 * @param ctx the parse tree
	 */
	void enterCteClause(DMLStatementParser.CteClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#cteClause}.
	 * @param ctx the parse tree
	 */
	void exitCteClause(DMLStatementParser.CteClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#selectSpecification}.
	 * @param ctx the parse tree
	 */
	void enterSelectSpecification(DMLStatementParser.SelectSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#selectSpecification}.
	 * @param ctx the parse tree
	 */
	void exitSelectSpecification(DMLStatementParser.SelectSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#duplicateSpecification}.
	 * @param ctx the parse tree
	 */
	void enterDuplicateSpecification(DMLStatementParser.DuplicateSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#duplicateSpecification}.
	 * @param ctx the parse tree
	 */
	void exitDuplicateSpecification(DMLStatementParser.DuplicateSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#projections}.
	 * @param ctx the parse tree
	 */
	void enterProjections(DMLStatementParser.ProjectionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#projections}.
	 * @param ctx the parse tree
	 */
	void exitProjections(DMLStatementParser.ProjectionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#projection}.
	 * @param ctx the parse tree
	 */
	void enterProjection(DMLStatementParser.ProjectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#projection}.
	 * @param ctx the parse tree
	 */
	void exitProjection(DMLStatementParser.ProjectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#unqualifiedShorthand}.
	 * @param ctx the parse tree
	 */
	void enterUnqualifiedShorthand(DMLStatementParser.UnqualifiedShorthandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#unqualifiedShorthand}.
	 * @param ctx the parse tree
	 */
	void exitUnqualifiedShorthand(DMLStatementParser.UnqualifiedShorthandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#qualifiedShorthand}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedShorthand(DMLStatementParser.QualifiedShorthandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#qualifiedShorthand}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedShorthand(DMLStatementParser.QualifiedShorthandContext ctx);
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
	 * Enter a parse tree produced by {@link DMLStatementParser#tableReferences}.
	 * @param ctx the parse tree
	 */
	void enterTableReferences(DMLStatementParser.TableReferencesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#tableReferences}.
	 * @param ctx the parse tree
	 */
	void exitTableReferences(DMLStatementParser.TableReferencesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#escapedTableReference}.
	 * @param ctx the parse tree
	 */
	void enterEscapedTableReference(DMLStatementParser.EscapedTableReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#escapedTableReference}.
	 * @param ctx the parse tree
	 */
	void exitEscapedTableReference(DMLStatementParser.EscapedTableReferenceContext ctx);
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
	 * Enter a parse tree produced by {@link DMLStatementParser#tableFactor}.
	 * @param ctx the parse tree
	 */
	void enterTableFactor(DMLStatementParser.TableFactorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#tableFactor}.
	 * @param ctx the parse tree
	 */
	void exitTableFactor(DMLStatementParser.TableFactorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#partitionNames}.
	 * @param ctx the parse tree
	 */
	void enterPartitionNames(DMLStatementParser.PartitionNamesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#partitionNames}.
	 * @param ctx the parse tree
	 */
	void exitPartitionNames(DMLStatementParser.PartitionNamesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#indexHintList}.
	 * @param ctx the parse tree
	 */
	void enterIndexHintList(DMLStatementParser.IndexHintListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#indexHintList}.
	 * @param ctx the parse tree
	 */
	void exitIndexHintList(DMLStatementParser.IndexHintListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#indexHint}.
	 * @param ctx the parse tree
	 */
	void enterIndexHint(DMLStatementParser.IndexHintContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#indexHint}.
	 * @param ctx the parse tree
	 */
	void exitIndexHint(DMLStatementParser.IndexHintContext ctx);
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
	 * Enter a parse tree produced by {@link DMLStatementParser#innerJoinType}.
	 * @param ctx the parse tree
	 */
	void enterInnerJoinType(DMLStatementParser.InnerJoinTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#innerJoinType}.
	 * @param ctx the parse tree
	 */
	void exitInnerJoinType(DMLStatementParser.InnerJoinTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#outerJoinType}.
	 * @param ctx the parse tree
	 */
	void enterOuterJoinType(DMLStatementParser.OuterJoinTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#outerJoinType}.
	 * @param ctx the parse tree
	 */
	void exitOuterJoinType(DMLStatementParser.OuterJoinTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#naturalJoinType}.
	 * @param ctx the parse tree
	 */
	void enterNaturalJoinType(DMLStatementParser.NaturalJoinTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#naturalJoinType}.
	 * @param ctx the parse tree
	 */
	void exitNaturalJoinType(DMLStatementParser.NaturalJoinTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#joinSpecification}.
	 * @param ctx the parse tree
	 */
	void enterJoinSpecification(DMLStatementParser.JoinSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#joinSpecification}.
	 * @param ctx the parse tree
	 */
	void exitJoinSpecification(DMLStatementParser.JoinSpecificationContext ctx);
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
	 * Enter a parse tree produced by {@link DMLStatementParser#groupByClause}.
	 * @param ctx the parse tree
	 */
	void enterGroupByClause(DMLStatementParser.GroupByClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#groupByClause}.
	 * @param ctx the parse tree
	 */
	void exitGroupByClause(DMLStatementParser.GroupByClauseContext ctx);
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
	 * Enter a parse tree produced by {@link DMLStatementParser#limitRowCount}.
	 * @param ctx the parse tree
	 */
	void enterLimitRowCount(DMLStatementParser.LimitRowCountContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#limitRowCount}.
	 * @param ctx the parse tree
	 */
	void exitLimitRowCount(DMLStatementParser.LimitRowCountContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#limitOffset}.
	 * @param ctx the parse tree
	 */
	void enterLimitOffset(DMLStatementParser.LimitOffsetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#limitOffset}.
	 * @param ctx the parse tree
	 */
	void exitLimitOffset(DMLStatementParser.LimitOffsetContext ctx);
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
	 * Enter a parse tree produced by {@link DMLStatementParser#windowItem}.
	 * @param ctx the parse tree
	 */
	void enterWindowItem(DMLStatementParser.WindowItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#windowItem}.
	 * @param ctx the parse tree
	 */
	void exitWindowItem(DMLStatementParser.WindowItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#subquery}.
	 * @param ctx the parse tree
	 */
	void enterSubquery(DMLStatementParser.SubqueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#subquery}.
	 * @param ctx the parse tree
	 */
	void exitSubquery(DMLStatementParser.SubqueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#selectLinesInto}.
	 * @param ctx the parse tree
	 */
	void enterSelectLinesInto(DMLStatementParser.SelectLinesIntoContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#selectLinesInto}.
	 * @param ctx the parse tree
	 */
	void exitSelectLinesInto(DMLStatementParser.SelectLinesIntoContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#selectFieldsInto}.
	 * @param ctx the parse tree
	 */
	void enterSelectFieldsInto(DMLStatementParser.SelectFieldsIntoContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#selectFieldsInto}.
	 * @param ctx the parse tree
	 */
	void exitSelectFieldsInto(DMLStatementParser.SelectFieldsIntoContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#selectIntoExpression}.
	 * @param ctx the parse tree
	 */
	void enterSelectIntoExpression(DMLStatementParser.SelectIntoExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#selectIntoExpression}.
	 * @param ctx the parse tree
	 */
	void exitSelectIntoExpression(DMLStatementParser.SelectIntoExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#lockClause}.
	 * @param ctx the parse tree
	 */
	void enterLockClause(DMLStatementParser.LockClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#lockClause}.
	 * @param ctx the parse tree
	 */
	void exitLockClause(DMLStatementParser.LockClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#lockClauseList}.
	 * @param ctx the parse tree
	 */
	void enterLockClauseList(DMLStatementParser.LockClauseListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#lockClauseList}.
	 * @param ctx the parse tree
	 */
	void exitLockClauseList(DMLStatementParser.LockClauseListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#lockStrength}.
	 * @param ctx the parse tree
	 */
	void enterLockStrength(DMLStatementParser.LockStrengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#lockStrength}.
	 * @param ctx the parse tree
	 */
	void exitLockStrength(DMLStatementParser.LockStrengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#lockedRowAction}.
	 * @param ctx the parse tree
	 */
	void enterLockedRowAction(DMLStatementParser.LockedRowActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#lockedRowAction}.
	 * @param ctx the parse tree
	 */
	void exitLockedRowAction(DMLStatementParser.LockedRowActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#tableLockingList}.
	 * @param ctx the parse tree
	 */
	void enterTableLockingList(DMLStatementParser.TableLockingListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#tableLockingList}.
	 * @param ctx the parse tree
	 */
	void exitTableLockingList(DMLStatementParser.TableLockingListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#tableIdentOptWild}.
	 * @param ctx the parse tree
	 */
	void enterTableIdentOptWild(DMLStatementParser.TableIdentOptWildContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#tableIdentOptWild}.
	 * @param ctx the parse tree
	 */
	void exitTableIdentOptWild(DMLStatementParser.TableIdentOptWildContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#tableAliasRefList}.
	 * @param ctx the parse tree
	 */
	void enterTableAliasRefList(DMLStatementParser.TableAliasRefListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#tableAliasRefList}.
	 * @param ctx the parse tree
	 */
	void exitTableAliasRefList(DMLStatementParser.TableAliasRefListContext ctx);
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
	 * Enter a parse tree produced by {@link DMLStatementParser#customKeyword}.
	 * @param ctx the parse tree
	 */
	void enterCustomKeyword(DMLStatementParser.CustomKeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#customKeyword}.
	 * @param ctx the parse tree
	 */
	void exitCustomKeyword(DMLStatementParser.CustomKeywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#literals}.
	 * @param ctx the parse tree
	 */
	void enterLiterals(DMLStatementParser.LiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#literals}.
	 * @param ctx the parse tree
	 */
	void exitLiterals(DMLStatementParser.LiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#string_}.
	 * @param ctx the parse tree
	 */
	void enterString_(DMLStatementParser.String_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#string_}.
	 * @param ctx the parse tree
	 */
	void exitString_(DMLStatementParser.String_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#stringLiterals}.
	 * @param ctx the parse tree
	 */
	void enterStringLiterals(DMLStatementParser.StringLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#stringLiterals}.
	 * @param ctx the parse tree
	 */
	void exitStringLiterals(DMLStatementParser.StringLiteralsContext ctx);
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
	 * Enter a parse tree produced by {@link DMLStatementParser#temporalLiterals}.
	 * @param ctx the parse tree
	 */
	void enterTemporalLiterals(DMLStatementParser.TemporalLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#temporalLiterals}.
	 * @param ctx the parse tree
	 */
	void exitTemporalLiterals(DMLStatementParser.TemporalLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#hexadecimalLiterals}.
	 * @param ctx the parse tree
	 */
	void enterHexadecimalLiterals(DMLStatementParser.HexadecimalLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#hexadecimalLiterals}.
	 * @param ctx the parse tree
	 */
	void exitHexadecimalLiterals(DMLStatementParser.HexadecimalLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#bitValueLiterals}.
	 * @param ctx the parse tree
	 */
	void enterBitValueLiterals(DMLStatementParser.BitValueLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#bitValueLiterals}.
	 * @param ctx the parse tree
	 */
	void exitBitValueLiterals(DMLStatementParser.BitValueLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#booleanLiterals}.
	 * @param ctx the parse tree
	 */
	void enterBooleanLiterals(DMLStatementParser.BooleanLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#booleanLiterals}.
	 * @param ctx the parse tree
	 */
	void exitBooleanLiterals(DMLStatementParser.BooleanLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#nullValueLiterals}.
	 * @param ctx the parse tree
	 */
	void enterNullValueLiterals(DMLStatementParser.NullValueLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#nullValueLiterals}.
	 * @param ctx the parse tree
	 */
	void exitNullValueLiterals(DMLStatementParser.NullValueLiteralsContext ctx);
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
	 * Enter a parse tree produced by {@link DMLStatementParser#identifierKeywordsUnambiguous}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierKeywordsUnambiguous(DMLStatementParser.IdentifierKeywordsUnambiguousContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#identifierKeywordsUnambiguous}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierKeywordsUnambiguous(DMLStatementParser.IdentifierKeywordsUnambiguousContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#identifierKeywordsAmbiguous1RolesAndLabels}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierKeywordsAmbiguous1RolesAndLabels(DMLStatementParser.IdentifierKeywordsAmbiguous1RolesAndLabelsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#identifierKeywordsAmbiguous1RolesAndLabels}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierKeywordsAmbiguous1RolesAndLabels(DMLStatementParser.IdentifierKeywordsAmbiguous1RolesAndLabelsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#identifierKeywordsAmbiguous2Labels}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierKeywordsAmbiguous2Labels(DMLStatementParser.IdentifierKeywordsAmbiguous2LabelsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#identifierKeywordsAmbiguous2Labels}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierKeywordsAmbiguous2Labels(DMLStatementParser.IdentifierKeywordsAmbiguous2LabelsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#identifierKeywordsAmbiguous3Roles}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierKeywordsAmbiguous3Roles(DMLStatementParser.IdentifierKeywordsAmbiguous3RolesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#identifierKeywordsAmbiguous3Roles}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierKeywordsAmbiguous3Roles(DMLStatementParser.IdentifierKeywordsAmbiguous3RolesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#identifierKeywordsAmbiguous4SystemVariables}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierKeywordsAmbiguous4SystemVariables(DMLStatementParser.IdentifierKeywordsAmbiguous4SystemVariablesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#identifierKeywordsAmbiguous4SystemVariables}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierKeywordsAmbiguous4SystemVariables(DMLStatementParser.IdentifierKeywordsAmbiguous4SystemVariablesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#textOrIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterTextOrIdentifier(DMLStatementParser.TextOrIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#textOrIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitTextOrIdentifier(DMLStatementParser.TextOrIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#variable}.
	 * @param ctx the parse tree
	 */
	void enterVariable(DMLStatementParser.VariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#variable}.
	 * @param ctx the parse tree
	 */
	void exitVariable(DMLStatementParser.VariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#userVariable}.
	 * @param ctx the parse tree
	 */
	void enterUserVariable(DMLStatementParser.UserVariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#userVariable}.
	 * @param ctx the parse tree
	 */
	void exitUserVariable(DMLStatementParser.UserVariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#systemVariable}.
	 * @param ctx the parse tree
	 */
	void enterSystemVariable(DMLStatementParser.SystemVariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#systemVariable}.
	 * @param ctx the parse tree
	 */
	void exitSystemVariable(DMLStatementParser.SystemVariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#setSystemVariable}.
	 * @param ctx the parse tree
	 */
	void enterSetSystemVariable(DMLStatementParser.SetSystemVariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#setSystemVariable}.
	 * @param ctx the parse tree
	 */
	void exitSetSystemVariable(DMLStatementParser.SetSystemVariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#optionType}.
	 * @param ctx the parse tree
	 */
	void enterOptionType(DMLStatementParser.OptionTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#optionType}.
	 * @param ctx the parse tree
	 */
	void exitOptionType(DMLStatementParser.OptionTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#internalVariableName}.
	 * @param ctx the parse tree
	 */
	void enterInternalVariableName(DMLStatementParser.InternalVariableNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#internalVariableName}.
	 * @param ctx the parse tree
	 */
	void exitInternalVariableName(DMLStatementParser.InternalVariableNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#setExprOrDefault}.
	 * @param ctx the parse tree
	 */
	void enterSetExprOrDefault(DMLStatementParser.SetExprOrDefaultContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#setExprOrDefault}.
	 * @param ctx the parse tree
	 */
	void exitSetExprOrDefault(DMLStatementParser.SetExprOrDefaultContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#transactionCharacteristics}.
	 * @param ctx the parse tree
	 */
	void enterTransactionCharacteristics(DMLStatementParser.TransactionCharacteristicsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#transactionCharacteristics}.
	 * @param ctx the parse tree
	 */
	void exitTransactionCharacteristics(DMLStatementParser.TransactionCharacteristicsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#isolationLevel}.
	 * @param ctx the parse tree
	 */
	void enterIsolationLevel(DMLStatementParser.IsolationLevelContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#isolationLevel}.
	 * @param ctx the parse tree
	 */
	void exitIsolationLevel(DMLStatementParser.IsolationLevelContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#isolationTypes}.
	 * @param ctx the parse tree
	 */
	void enterIsolationTypes(DMLStatementParser.IsolationTypesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#isolationTypes}.
	 * @param ctx the parse tree
	 */
	void exitIsolationTypes(DMLStatementParser.IsolationTypesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#transactionAccessMode}.
	 * @param ctx the parse tree
	 */
	void enterTransactionAccessMode(DMLStatementParser.TransactionAccessModeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#transactionAccessMode}.
	 * @param ctx the parse tree
	 */
	void exitTransactionAccessMode(DMLStatementParser.TransactionAccessModeContext ctx);
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
	 * Enter a parse tree produced by {@link DMLStatementParser#schemaNames}.
	 * @param ctx the parse tree
	 */
	void enterSchemaNames(DMLStatementParser.SchemaNamesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#schemaNames}.
	 * @param ctx the parse tree
	 */
	void exitSchemaNames(DMLStatementParser.SchemaNamesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#charsetName}.
	 * @param ctx the parse tree
	 */
	void enterCharsetName(DMLStatementParser.CharsetNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#charsetName}.
	 * @param ctx the parse tree
	 */
	void exitCharsetName(DMLStatementParser.CharsetNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#schemaPairs}.
	 * @param ctx the parse tree
	 */
	void enterSchemaPairs(DMLStatementParser.SchemaPairsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#schemaPairs}.
	 * @param ctx the parse tree
	 */
	void exitSchemaPairs(DMLStatementParser.SchemaPairsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#schemaPair}.
	 * @param ctx the parse tree
	 */
	void enterSchemaPair(DMLStatementParser.SchemaPairContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#schemaPair}.
	 * @param ctx the parse tree
	 */
	void exitSchemaPair(DMLStatementParser.SchemaPairContext ctx);
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
	 * Enter a parse tree produced by {@link DMLStatementParser#userIdentifierOrText}.
	 * @param ctx the parse tree
	 */
	void enterUserIdentifierOrText(DMLStatementParser.UserIdentifierOrTextContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#userIdentifierOrText}.
	 * @param ctx the parse tree
	 */
	void exitUserIdentifierOrText(DMLStatementParser.UserIdentifierOrTextContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#userName}.
	 * @param ctx the parse tree
	 */
	void enterUserName(DMLStatementParser.UserNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#userName}.
	 * @param ctx the parse tree
	 */
	void exitUserName(DMLStatementParser.UserNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#eventName}.
	 * @param ctx the parse tree
	 */
	void enterEventName(DMLStatementParser.EventNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#eventName}.
	 * @param ctx the parse tree
	 */
	void exitEventName(DMLStatementParser.EventNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#serverName}.
	 * @param ctx the parse tree
	 */
	void enterServerName(DMLStatementParser.ServerNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#serverName}.
	 * @param ctx the parse tree
	 */
	void exitServerName(DMLStatementParser.ServerNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#wrapperName}.
	 * @param ctx the parse tree
	 */
	void enterWrapperName(DMLStatementParser.WrapperNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#wrapperName}.
	 * @param ctx the parse tree
	 */
	void exitWrapperName(DMLStatementParser.WrapperNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#functionName}.
	 * @param ctx the parse tree
	 */
	void enterFunctionName(DMLStatementParser.FunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#functionName}.
	 * @param ctx the parse tree
	 */
	void exitFunctionName(DMLStatementParser.FunctionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#viewName}.
	 * @param ctx the parse tree
	 */
	void enterViewName(DMLStatementParser.ViewNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#viewName}.
	 * @param ctx the parse tree
	 */
	void exitViewName(DMLStatementParser.ViewNameContext ctx);
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
	 * Enter a parse tree produced by {@link DMLStatementParser#tableList}.
	 * @param ctx the parse tree
	 */
	void enterTableList(DMLStatementParser.TableListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#tableList}.
	 * @param ctx the parse tree
	 */
	void exitTableList(DMLStatementParser.TableListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#viewNames}.
	 * @param ctx the parse tree
	 */
	void enterViewNames(DMLStatementParser.ViewNamesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#viewNames}.
	 * @param ctx the parse tree
	 */
	void exitViewNames(DMLStatementParser.ViewNamesContext ctx);
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
	 * Enter a parse tree produced by {@link DMLStatementParser#groupName}.
	 * @param ctx the parse tree
	 */
	void enterGroupName(DMLStatementParser.GroupNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#groupName}.
	 * @param ctx the parse tree
	 */
	void exitGroupName(DMLStatementParser.GroupNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#routineName}.
	 * @param ctx the parse tree
	 */
	void enterRoutineName(DMLStatementParser.RoutineNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#routineName}.
	 * @param ctx the parse tree
	 */
	void exitRoutineName(DMLStatementParser.RoutineNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#shardLibraryName}.
	 * @param ctx the parse tree
	 */
	void enterShardLibraryName(DMLStatementParser.ShardLibraryNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#shardLibraryName}.
	 * @param ctx the parse tree
	 */
	void exitShardLibraryName(DMLStatementParser.ShardLibraryNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#componentName}.
	 * @param ctx the parse tree
	 */
	void enterComponentName(DMLStatementParser.ComponentNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#componentName}.
	 * @param ctx the parse tree
	 */
	void exitComponentName(DMLStatementParser.ComponentNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#pluginName}.
	 * @param ctx the parse tree
	 */
	void enterPluginName(DMLStatementParser.PluginNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#pluginName}.
	 * @param ctx the parse tree
	 */
	void exitPluginName(DMLStatementParser.PluginNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#hostName}.
	 * @param ctx the parse tree
	 */
	void enterHostName(DMLStatementParser.HostNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#hostName}.
	 * @param ctx the parse tree
	 */
	void exitHostName(DMLStatementParser.HostNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#port}.
	 * @param ctx the parse tree
	 */
	void enterPort(DMLStatementParser.PortContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#port}.
	 * @param ctx the parse tree
	 */
	void exitPort(DMLStatementParser.PortContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#cloneInstance}.
	 * @param ctx the parse tree
	 */
	void enterCloneInstance(DMLStatementParser.CloneInstanceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#cloneInstance}.
	 * @param ctx the parse tree
	 */
	void exitCloneInstance(DMLStatementParser.CloneInstanceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#cloneDir}.
	 * @param ctx the parse tree
	 */
	void enterCloneDir(DMLStatementParser.CloneDirContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#cloneDir}.
	 * @param ctx the parse tree
	 */
	void exitCloneDir(DMLStatementParser.CloneDirContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#channelName}.
	 * @param ctx the parse tree
	 */
	void enterChannelName(DMLStatementParser.ChannelNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#channelName}.
	 * @param ctx the parse tree
	 */
	void exitChannelName(DMLStatementParser.ChannelNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#logName}.
	 * @param ctx the parse tree
	 */
	void enterLogName(DMLStatementParser.LogNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#logName}.
	 * @param ctx the parse tree
	 */
	void exitLogName(DMLStatementParser.LogNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#roleName}.
	 * @param ctx the parse tree
	 */
	void enterRoleName(DMLStatementParser.RoleNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#roleName}.
	 * @param ctx the parse tree
	 */
	void exitRoleName(DMLStatementParser.RoleNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#roleIdentifierOrText}.
	 * @param ctx the parse tree
	 */
	void enterRoleIdentifierOrText(DMLStatementParser.RoleIdentifierOrTextContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#roleIdentifierOrText}.
	 * @param ctx the parse tree
	 */
	void exitRoleIdentifierOrText(DMLStatementParser.RoleIdentifierOrTextContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#engineRef}.
	 * @param ctx the parse tree
	 */
	void enterEngineRef(DMLStatementParser.EngineRefContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#engineRef}.
	 * @param ctx the parse tree
	 */
	void exitEngineRef(DMLStatementParser.EngineRefContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#triggerName}.
	 * @param ctx the parse tree
	 */
	void enterTriggerName(DMLStatementParser.TriggerNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#triggerName}.
	 * @param ctx the parse tree
	 */
	void exitTriggerName(DMLStatementParser.TriggerNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#triggerTime}.
	 * @param ctx the parse tree
	 */
	void enterTriggerTime(DMLStatementParser.TriggerTimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#triggerTime}.
	 * @param ctx the parse tree
	 */
	void exitTriggerTime(DMLStatementParser.TriggerTimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#tableOrTables}.
	 * @param ctx the parse tree
	 */
	void enterTableOrTables(DMLStatementParser.TableOrTablesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#tableOrTables}.
	 * @param ctx the parse tree
	 */
	void exitTableOrTables(DMLStatementParser.TableOrTablesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#userOrRole}.
	 * @param ctx the parse tree
	 */
	void enterUserOrRole(DMLStatementParser.UserOrRoleContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#userOrRole}.
	 * @param ctx the parse tree
	 */
	void exitUserOrRole(DMLStatementParser.UserOrRoleContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#partitionName}.
	 * @param ctx the parse tree
	 */
	void enterPartitionName(DMLStatementParser.PartitionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#partitionName}.
	 * @param ctx the parse tree
	 */
	void exitPartitionName(DMLStatementParser.PartitionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#identifierList}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierList(DMLStatementParser.IdentifierListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#identifierList}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierList(DMLStatementParser.IdentifierListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#allOrPartitionNameList}.
	 * @param ctx the parse tree
	 */
	void enterAllOrPartitionNameList(DMLStatementParser.AllOrPartitionNameListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#allOrPartitionNameList}.
	 * @param ctx the parse tree
	 */
	void exitAllOrPartitionNameList(DMLStatementParser.AllOrPartitionNameListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#triggerEvent}.
	 * @param ctx the parse tree
	 */
	void enterTriggerEvent(DMLStatementParser.TriggerEventContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#triggerEvent}.
	 * @param ctx the parse tree
	 */
	void exitTriggerEvent(DMLStatementParser.TriggerEventContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#triggerOrder}.
	 * @param ctx the parse tree
	 */
	void enterTriggerOrder(DMLStatementParser.TriggerOrderContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#triggerOrder}.
	 * @param ctx the parse tree
	 */
	void exitTriggerOrder(DMLStatementParser.TriggerOrderContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(DMLStatementParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(DMLStatementParser.ExprContext ctx);
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
	 * Enter a parse tree produced by {@link DMLStatementParser#notOperator}.
	 * @param ctx the parse tree
	 */
	void enterNotOperator(DMLStatementParser.NotOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#notOperator}.
	 * @param ctx the parse tree
	 */
	void exitNotOperator(DMLStatementParser.NotOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#booleanPrimary}.
	 * @param ctx the parse tree
	 */
	void enterBooleanPrimary(DMLStatementParser.BooleanPrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#booleanPrimary}.
	 * @param ctx the parse tree
	 */
	void exitBooleanPrimary(DMLStatementParser.BooleanPrimaryContext ctx);
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
	 * Enter a parse tree produced by {@link DMLStatementParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterPredicate(DMLStatementParser.PredicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitPredicate(DMLStatementParser.PredicateContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#bitExpr}.
	 * @param ctx the parse tree
	 */
	void enterBitExpr(DMLStatementParser.BitExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#bitExpr}.
	 * @param ctx the parse tree
	 */
	void exitBitExpr(DMLStatementParser.BitExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#simpleExpr}.
	 * @param ctx the parse tree
	 */
	void enterSimpleExpr(DMLStatementParser.SimpleExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#simpleExpr}.
	 * @param ctx the parse tree
	 */
	void exitSimpleExpr(DMLStatementParser.SimpleExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#columnRef}.
	 * @param ctx the parse tree
	 */
	void enterColumnRef(DMLStatementParser.ColumnRefContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#columnRef}.
	 * @param ctx the parse tree
	 */
	void exitColumnRef(DMLStatementParser.ColumnRefContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#columnRefList}.
	 * @param ctx the parse tree
	 */
	void enterColumnRefList(DMLStatementParser.ColumnRefListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#columnRefList}.
	 * @param ctx the parse tree
	 */
	void exitColumnRefList(DMLStatementParser.ColumnRefListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCall(DMLStatementParser.FunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCall(DMLStatementParser.FunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#aggregationFunction}.
	 * @param ctx the parse tree
	 */
	void enterAggregationFunction(DMLStatementParser.AggregationFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#aggregationFunction}.
	 * @param ctx the parse tree
	 */
	void exitAggregationFunction(DMLStatementParser.AggregationFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#aggregationFunctionName}.
	 * @param ctx the parse tree
	 */
	void enterAggregationFunctionName(DMLStatementParser.AggregationFunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#aggregationFunctionName}.
	 * @param ctx the parse tree
	 */
	void exitAggregationFunctionName(DMLStatementParser.AggregationFunctionNameContext ctx);
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
	 * Enter a parse tree produced by {@link DMLStatementParser#frameStart}.
	 * @param ctx the parse tree
	 */
	void enterFrameStart(DMLStatementParser.FrameStartContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#frameStart}.
	 * @param ctx the parse tree
	 */
	void exitFrameStart(DMLStatementParser.FrameStartContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#frameEnd}.
	 * @param ctx the parse tree
	 */
	void enterFrameEnd(DMLStatementParser.FrameEndContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#frameEnd}.
	 * @param ctx the parse tree
	 */
	void exitFrameEnd(DMLStatementParser.FrameEndContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#frameBetween}.
	 * @param ctx the parse tree
	 */
	void enterFrameBetween(DMLStatementParser.FrameBetweenContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#frameBetween}.
	 * @param ctx the parse tree
	 */
	void exitFrameBetween(DMLStatementParser.FrameBetweenContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#specialFunction}.
	 * @param ctx the parse tree
	 */
	void enterSpecialFunction(DMLStatementParser.SpecialFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#specialFunction}.
	 * @param ctx the parse tree
	 */
	void exitSpecialFunction(DMLStatementParser.SpecialFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#currentUserFunction}.
	 * @param ctx the parse tree
	 */
	void enterCurrentUserFunction(DMLStatementParser.CurrentUserFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#currentUserFunction}.
	 * @param ctx the parse tree
	 */
	void exitCurrentUserFunction(DMLStatementParser.CurrentUserFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#groupConcatFunction}.
	 * @param ctx the parse tree
	 */
	void enterGroupConcatFunction(DMLStatementParser.GroupConcatFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#groupConcatFunction}.
	 * @param ctx the parse tree
	 */
	void exitGroupConcatFunction(DMLStatementParser.GroupConcatFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#windowFunction}.
	 * @param ctx the parse tree
	 */
	void enterWindowFunction(DMLStatementParser.WindowFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#windowFunction}.
	 * @param ctx the parse tree
	 */
	void exitWindowFunction(DMLStatementParser.WindowFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#windowingClause}.
	 * @param ctx the parse tree
	 */
	void enterWindowingClause(DMLStatementParser.WindowingClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#windowingClause}.
	 * @param ctx the parse tree
	 */
	void exitWindowingClause(DMLStatementParser.WindowingClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#leadLagInfo}.
	 * @param ctx the parse tree
	 */
	void enterLeadLagInfo(DMLStatementParser.LeadLagInfoContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#leadLagInfo}.
	 * @param ctx the parse tree
	 */
	void exitLeadLagInfo(DMLStatementParser.LeadLagInfoContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#nullTreatment}.
	 * @param ctx the parse tree
	 */
	void enterNullTreatment(DMLStatementParser.NullTreatmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#nullTreatment}.
	 * @param ctx the parse tree
	 */
	void exitNullTreatment(DMLStatementParser.NullTreatmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#checkType}.
	 * @param ctx the parse tree
	 */
	void enterCheckType(DMLStatementParser.CheckTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#checkType}.
	 * @param ctx the parse tree
	 */
	void exitCheckType(DMLStatementParser.CheckTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#repairType}.
	 * @param ctx the parse tree
	 */
	void enterRepairType(DMLStatementParser.RepairTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#repairType}.
	 * @param ctx the parse tree
	 */
	void exitRepairType(DMLStatementParser.RepairTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#castFunction}.
	 * @param ctx the parse tree
	 */
	void enterCastFunction(DMLStatementParser.CastFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#castFunction}.
	 * @param ctx the parse tree
	 */
	void exitCastFunction(DMLStatementParser.CastFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#convertFunction}.
	 * @param ctx the parse tree
	 */
	void enterConvertFunction(DMLStatementParser.ConvertFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#convertFunction}.
	 * @param ctx the parse tree
	 */
	void exitConvertFunction(DMLStatementParser.ConvertFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#castType}.
	 * @param ctx the parse tree
	 */
	void enterCastType(DMLStatementParser.CastTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#castType}.
	 * @param ctx the parse tree
	 */
	void exitCastType(DMLStatementParser.CastTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#nchar}.
	 * @param ctx the parse tree
	 */
	void enterNchar(DMLStatementParser.NcharContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#nchar}.
	 * @param ctx the parse tree
	 */
	void exitNchar(DMLStatementParser.NcharContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#positionFunction}.
	 * @param ctx the parse tree
	 */
	void enterPositionFunction(DMLStatementParser.PositionFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#positionFunction}.
	 * @param ctx the parse tree
	 */
	void exitPositionFunction(DMLStatementParser.PositionFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#substringFunction}.
	 * @param ctx the parse tree
	 */
	void enterSubstringFunction(DMLStatementParser.SubstringFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#substringFunction}.
	 * @param ctx the parse tree
	 */
	void exitSubstringFunction(DMLStatementParser.SubstringFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#extractFunction}.
	 * @param ctx the parse tree
	 */
	void enterExtractFunction(DMLStatementParser.ExtractFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#extractFunction}.
	 * @param ctx the parse tree
	 */
	void exitExtractFunction(DMLStatementParser.ExtractFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#charFunction}.
	 * @param ctx the parse tree
	 */
	void enterCharFunction(DMLStatementParser.CharFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#charFunction}.
	 * @param ctx the parse tree
	 */
	void exitCharFunction(DMLStatementParser.CharFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#trimFunction}.
	 * @param ctx the parse tree
	 */
	void enterTrimFunction(DMLStatementParser.TrimFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#trimFunction}.
	 * @param ctx the parse tree
	 */
	void exitTrimFunction(DMLStatementParser.TrimFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#valuesFunction}.
	 * @param ctx the parse tree
	 */
	void enterValuesFunction(DMLStatementParser.ValuesFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#valuesFunction}.
	 * @param ctx the parse tree
	 */
	void exitValuesFunction(DMLStatementParser.ValuesFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#weightStringFunction}.
	 * @param ctx the parse tree
	 */
	void enterWeightStringFunction(DMLStatementParser.WeightStringFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#weightStringFunction}.
	 * @param ctx the parse tree
	 */
	void exitWeightStringFunction(DMLStatementParser.WeightStringFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#levelClause}.
	 * @param ctx the parse tree
	 */
	void enterLevelClause(DMLStatementParser.LevelClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#levelClause}.
	 * @param ctx the parse tree
	 */
	void exitLevelClause(DMLStatementParser.LevelClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#levelInWeightListElement}.
	 * @param ctx the parse tree
	 */
	void enterLevelInWeightListElement(DMLStatementParser.LevelInWeightListElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#levelInWeightListElement}.
	 * @param ctx the parse tree
	 */
	void exitLevelInWeightListElement(DMLStatementParser.LevelInWeightListElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#regularFunction}.
	 * @param ctx the parse tree
	 */
	void enterRegularFunction(DMLStatementParser.RegularFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#regularFunction}.
	 * @param ctx the parse tree
	 */
	void exitRegularFunction(DMLStatementParser.RegularFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#shorthandRegularFunction}.
	 * @param ctx the parse tree
	 */
	void enterShorthandRegularFunction(DMLStatementParser.ShorthandRegularFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#shorthandRegularFunction}.
	 * @param ctx the parse tree
	 */
	void exitShorthandRegularFunction(DMLStatementParser.ShorthandRegularFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#completeRegularFunction}.
	 * @param ctx the parse tree
	 */
	void enterCompleteRegularFunction(DMLStatementParser.CompleteRegularFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#completeRegularFunction}.
	 * @param ctx the parse tree
	 */
	void exitCompleteRegularFunction(DMLStatementParser.CompleteRegularFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#regularFunctionName}.
	 * @param ctx the parse tree
	 */
	void enterRegularFunctionName(DMLStatementParser.RegularFunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#regularFunctionName}.
	 * @param ctx the parse tree
	 */
	void exitRegularFunctionName(DMLStatementParser.RegularFunctionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#matchExpression}.
	 * @param ctx the parse tree
	 */
	void enterMatchExpression(DMLStatementParser.MatchExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#matchExpression}.
	 * @param ctx the parse tree
	 */
	void exitMatchExpression(DMLStatementParser.MatchExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#matchSearchModifier}.
	 * @param ctx the parse tree
	 */
	void enterMatchSearchModifier(DMLStatementParser.MatchSearchModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#matchSearchModifier}.
	 * @param ctx the parse tree
	 */
	void exitMatchSearchModifier(DMLStatementParser.MatchSearchModifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#caseExpression}.
	 * @param ctx the parse tree
	 */
	void enterCaseExpression(DMLStatementParser.CaseExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#caseExpression}.
	 * @param ctx the parse tree
	 */
	void exitCaseExpression(DMLStatementParser.CaseExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#datetimeExpr}.
	 * @param ctx the parse tree
	 */
	void enterDatetimeExpr(DMLStatementParser.DatetimeExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#datetimeExpr}.
	 * @param ctx the parse tree
	 */
	void exitDatetimeExpr(DMLStatementParser.DatetimeExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#binaryLogFileIndexNumber}.
	 * @param ctx the parse tree
	 */
	void enterBinaryLogFileIndexNumber(DMLStatementParser.BinaryLogFileIndexNumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#binaryLogFileIndexNumber}.
	 * @param ctx the parse tree
	 */
	void exitBinaryLogFileIndexNumber(DMLStatementParser.BinaryLogFileIndexNumberContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#caseWhen}.
	 * @param ctx the parse tree
	 */
	void enterCaseWhen(DMLStatementParser.CaseWhenContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#caseWhen}.
	 * @param ctx the parse tree
	 */
	void exitCaseWhen(DMLStatementParser.CaseWhenContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#caseElse}.
	 * @param ctx the parse tree
	 */
	void enterCaseElse(DMLStatementParser.CaseElseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#caseElse}.
	 * @param ctx the parse tree
	 */
	void exitCaseElse(DMLStatementParser.CaseElseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#intervalExpression}.
	 * @param ctx the parse tree
	 */
	void enterIntervalExpression(DMLStatementParser.IntervalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#intervalExpression}.
	 * @param ctx the parse tree
	 */
	void exitIntervalExpression(DMLStatementParser.IntervalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#intervalValue}.
	 * @param ctx the parse tree
	 */
	void enterIntervalValue(DMLStatementParser.IntervalValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#intervalValue}.
	 * @param ctx the parse tree
	 */
	void exitIntervalValue(DMLStatementParser.IntervalValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#intervalUnit}.
	 * @param ctx the parse tree
	 */
	void enterIntervalUnit(DMLStatementParser.IntervalUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#intervalUnit}.
	 * @param ctx the parse tree
	 */
	void exitIntervalUnit(DMLStatementParser.IntervalUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#orderByClause}.
	 * @param ctx the parse tree
	 */
	void enterOrderByClause(DMLStatementParser.OrderByClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#orderByClause}.
	 * @param ctx the parse tree
	 */
	void exitOrderByClause(DMLStatementParser.OrderByClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#orderByItem}.
	 * @param ctx the parse tree
	 */
	void enterOrderByItem(DMLStatementParser.OrderByItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#orderByItem}.
	 * @param ctx the parse tree
	 */
	void exitOrderByItem(DMLStatementParser.OrderByItemContext ctx);
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
	 * Enter a parse tree produced by {@link DMLStatementParser#stringList}.
	 * @param ctx the parse tree
	 */
	void enterStringList(DMLStatementParser.StringListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#stringList}.
	 * @param ctx the parse tree
	 */
	void exitStringList(DMLStatementParser.StringListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#textString}.
	 * @param ctx the parse tree
	 */
	void enterTextString(DMLStatementParser.TextStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#textString}.
	 * @param ctx the parse tree
	 */
	void exitTextString(DMLStatementParser.TextStringContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#textStringHash}.
	 * @param ctx the parse tree
	 */
	void enterTextStringHash(DMLStatementParser.TextStringHashContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#textStringHash}.
	 * @param ctx the parse tree
	 */
	void exitTextStringHash(DMLStatementParser.TextStringHashContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#fieldOptions}.
	 * @param ctx the parse tree
	 */
	void enterFieldOptions(DMLStatementParser.FieldOptionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#fieldOptions}.
	 * @param ctx the parse tree
	 */
	void exitFieldOptions(DMLStatementParser.FieldOptionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#precision}.
	 * @param ctx the parse tree
	 */
	void enterPrecision(DMLStatementParser.PrecisionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#precision}.
	 * @param ctx the parse tree
	 */
	void exitPrecision(DMLStatementParser.PrecisionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#typeDatetimePrecision}.
	 * @param ctx the parse tree
	 */
	void enterTypeDatetimePrecision(DMLStatementParser.TypeDatetimePrecisionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#typeDatetimePrecision}.
	 * @param ctx the parse tree
	 */
	void exitTypeDatetimePrecision(DMLStatementParser.TypeDatetimePrecisionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#charsetWithOptBinary}.
	 * @param ctx the parse tree
	 */
	void enterCharsetWithOptBinary(DMLStatementParser.CharsetWithOptBinaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#charsetWithOptBinary}.
	 * @param ctx the parse tree
	 */
	void exitCharsetWithOptBinary(DMLStatementParser.CharsetWithOptBinaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#ascii}.
	 * @param ctx the parse tree
	 */
	void enterAscii(DMLStatementParser.AsciiContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#ascii}.
	 * @param ctx the parse tree
	 */
	void exitAscii(DMLStatementParser.AsciiContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#unicode}.
	 * @param ctx the parse tree
	 */
	void enterUnicode(DMLStatementParser.UnicodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#unicode}.
	 * @param ctx the parse tree
	 */
	void exitUnicode(DMLStatementParser.UnicodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#charset}.
	 * @param ctx the parse tree
	 */
	void enterCharset(DMLStatementParser.CharsetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#charset}.
	 * @param ctx the parse tree
	 */
	void exitCharset(DMLStatementParser.CharsetContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#defaultCollation}.
	 * @param ctx the parse tree
	 */
	void enterDefaultCollation(DMLStatementParser.DefaultCollationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#defaultCollation}.
	 * @param ctx the parse tree
	 */
	void exitDefaultCollation(DMLStatementParser.DefaultCollationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#defaultEncryption}.
	 * @param ctx the parse tree
	 */
	void enterDefaultEncryption(DMLStatementParser.DefaultEncryptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#defaultEncryption}.
	 * @param ctx the parse tree
	 */
	void exitDefaultEncryption(DMLStatementParser.DefaultEncryptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#defaultCharset}.
	 * @param ctx the parse tree
	 */
	void enterDefaultCharset(DMLStatementParser.DefaultCharsetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#defaultCharset}.
	 * @param ctx the parse tree
	 */
	void exitDefaultCharset(DMLStatementParser.DefaultCharsetContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#signedLiteral}.
	 * @param ctx the parse tree
	 */
	void enterSignedLiteral(DMLStatementParser.SignedLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#signedLiteral}.
	 * @param ctx the parse tree
	 */
	void exitSignedLiteral(DMLStatementParser.SignedLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#now}.
	 * @param ctx the parse tree
	 */
	void enterNow(DMLStatementParser.NowContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#now}.
	 * @param ctx the parse tree
	 */
	void exitNow(DMLStatementParser.NowContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#columnFormat}.
	 * @param ctx the parse tree
	 */
	void enterColumnFormat(DMLStatementParser.ColumnFormatContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#columnFormat}.
	 * @param ctx the parse tree
	 */
	void exitColumnFormat(DMLStatementParser.ColumnFormatContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#storageMedia}.
	 * @param ctx the parse tree
	 */
	void enterStorageMedia(DMLStatementParser.StorageMediaContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#storageMedia}.
	 * @param ctx the parse tree
	 */
	void exitStorageMedia(DMLStatementParser.StorageMediaContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#direction}.
	 * @param ctx the parse tree
	 */
	void enterDirection(DMLStatementParser.DirectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#direction}.
	 * @param ctx the parse tree
	 */
	void exitDirection(DMLStatementParser.DirectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#keyOrIndex}.
	 * @param ctx the parse tree
	 */
	void enterKeyOrIndex(DMLStatementParser.KeyOrIndexContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#keyOrIndex}.
	 * @param ctx the parse tree
	 */
	void exitKeyOrIndex(DMLStatementParser.KeyOrIndexContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#fieldLength}.
	 * @param ctx the parse tree
	 */
	void enterFieldLength(DMLStatementParser.FieldLengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#fieldLength}.
	 * @param ctx the parse tree
	 */
	void exitFieldLength(DMLStatementParser.FieldLengthContext ctx);
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
	 * Enter a parse tree produced by {@link DMLStatementParser#fieldOrVarSpec}.
	 * @param ctx the parse tree
	 */
	void enterFieldOrVarSpec(DMLStatementParser.FieldOrVarSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#fieldOrVarSpec}.
	 * @param ctx the parse tree
	 */
	void exitFieldOrVarSpec(DMLStatementParser.FieldOrVarSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#notExistClause}.
	 * @param ctx the parse tree
	 */
	void enterNotExistClause(DMLStatementParser.NotExistClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#notExistClause}.
	 * @param ctx the parse tree
	 */
	void exitNotExistClause(DMLStatementParser.NotExistClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#existClause}.
	 * @param ctx the parse tree
	 */
	void enterExistClause(DMLStatementParser.ExistClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#existClause}.
	 * @param ctx the parse tree
	 */
	void exitExistClause(DMLStatementParser.ExistClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#connectionId}.
	 * @param ctx the parse tree
	 */
	void enterConnectionId(DMLStatementParser.ConnectionIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#connectionId}.
	 * @param ctx the parse tree
	 */
	void exitConnectionId(DMLStatementParser.ConnectionIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#labelName}.
	 * @param ctx the parse tree
	 */
	void enterLabelName(DMLStatementParser.LabelNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#labelName}.
	 * @param ctx the parse tree
	 */
	void exitLabelName(DMLStatementParser.LabelNameContext ctx);
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
	 * Enter a parse tree produced by {@link DMLStatementParser#conditionName}.
	 * @param ctx the parse tree
	 */
	void enterConditionName(DMLStatementParser.ConditionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#conditionName}.
	 * @param ctx the parse tree
	 */
	void exitConditionName(DMLStatementParser.ConditionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#unionOption}.
	 * @param ctx the parse tree
	 */
	void enterUnionOption(DMLStatementParser.UnionOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#unionOption}.
	 * @param ctx the parse tree
	 */
	void exitUnionOption(DMLStatementParser.UnionOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#noWriteToBinLog}.
	 * @param ctx the parse tree
	 */
	void enterNoWriteToBinLog(DMLStatementParser.NoWriteToBinLogContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#noWriteToBinLog}.
	 * @param ctx the parse tree
	 */
	void exitNoWriteToBinLog(DMLStatementParser.NoWriteToBinLogContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#channelOption}.
	 * @param ctx the parse tree
	 */
	void enterChannelOption(DMLStatementParser.ChannelOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#channelOption}.
	 * @param ctx the parse tree
	 */
	void exitChannelOption(DMLStatementParser.ChannelOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#preparedStatement}.
	 * @param ctx the parse tree
	 */
	void enterPreparedStatement(DMLStatementParser.PreparedStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#preparedStatement}.
	 * @param ctx the parse tree
	 */
	void exitPreparedStatement(DMLStatementParser.PreparedStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#executeStatement}.
	 * @param ctx the parse tree
	 */
	void enterExecuteStatement(DMLStatementParser.ExecuteStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#executeStatement}.
	 * @param ctx the parse tree
	 */
	void exitExecuteStatement(DMLStatementParser.ExecuteStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DMLStatementParser#executeVarList}.
	 * @param ctx the parse tree
	 */
	void enterExecuteVarList(DMLStatementParser.ExecuteVarListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DMLStatementParser#executeVarList}.
	 * @param ctx the parse tree
	 */
	void exitExecuteVarList(DMLStatementParser.ExecuteVarListContext ctx);
}