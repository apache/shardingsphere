/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.mcp.core.tool.handler.execute;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.api.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.exception.MCPQueryFailedException;
import org.apache.shardingsphere.mcp.api.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPBannedSQLStatementException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPUnsupportedSQLStatementException;
import org.apache.shardingsphere.mcp.core.session.MCPSessionExecutionCoordinator;
import org.apache.shardingsphere.mcp.core.session.MCPSessionNotExistedException;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.database.capability.SchemaExecutionSemantics;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.support.database.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.support.database.exception.StatementClassNotSupportedException;
import org.apache.shardingsphere.mcp.support.database.tool.request.SQLExecutionRequest;
import org.apache.shardingsphere.mcp.support.database.tool.result.SQLExecutionResult;
import org.apache.shardingsphere.sql.parser.spi.SQLStatementVisitorFacade;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.CombineType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.FunctionNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.datetime.DatetimeExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.exec.ExecSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CollateExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionWithParamsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.IntervalExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.IntervalUnit;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.KeyValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.QuantifySubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.RowExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.UnaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ValuesExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.interval.IntervalDayToSecondExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.DatetimeProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.IntervalExpressionProjection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.merge.MergeWhenAndThenSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.ExpressionLimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HavingSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HierarchicalQuerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableConditionalIntoElseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableConditionalIntoSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableConditionalIntoThenSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableConditionalIntoWhenThenSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableInsertIntoSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableInsertType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlElementFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlPiFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlQueryAndExistsFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlSerializeFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlTableColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlTableFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlTableOptionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ModelColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ModelSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.match.MatchAgainstExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.CollectionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.EmptyStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.AlterDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MCPSQLExecutionFacadeTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource({"parsedNamespaceStatements", "parsedExpressionStatements", "parsedXMLStatements"})
    void assertExecuteWithParsedCrossSchemaObjects(final String name, final String databaseType, final String sql, final SQLStatement statement, final String expectedObject) {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY, SupportedMCPStatement.DML, SupportedMCPStatement.DDL, SupportedMCPStatement.DCL));
        when(capability.getDatabaseType()).thenReturn(databaseType);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        mockSessionLock(coordinator);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor);
        try (
                MockedConstruction<ShardingSphereSQLParserEngine> ignored = mockConstruction(ShardingSphereSQLParserEngine.class,
                        (mock, context) -> when(mock.parse(anyString(), eq(false))).thenReturn(statement))) {
            MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> facade.execute(createExecutionRequest(sql)));
            assertThat(actual.getMessage(), is(String.format("Cross-schema SQL is not supported for database `logic_db`: `%s`.", expectedObject)));
            verifyNoInteractions(transactionExecutor, statementExecutor);
        }
    }
    
    private static Stream<Arguments> parsedNamespaceStatements() {
        DatabaseType oracle = TypedSPILoader.getService(DatabaseType.class, "Oracle");
        CreateMaterializedViewStatement materializedView = new CreateMaterializedViewStatement(oracle);
        materializedView.setView(createTable("logic_db", "foo_view"));
        materializedView.setSelect(selectExpression(oracle, createForeignFunction()));
        IndexSegment index = new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("foo_index")));
        index.setOwner(new OwnerSegment(0, 0, new IdentifierValue("other_db")));
        DatabaseType postgresql = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        AlterDatabaseStatement alterDatabase = new AlterDatabaseStatement(postgresql);
        alterDatabase.setDatabaseName("other_db");
        alterDatabase.setRenameDatabaseName("foo_new_database");
        AlterViewStatement renameView = new AlterViewStatement(postgresql);
        renameView.setView(createTable("other_db", "foo_view"));
        renameView.setRenameView(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_new_view"))));
        DatabaseType sqlserver = TypedSPILoader.getService(DatabaseType.class, "SQLServer");
        AlterViewStatement redefineView = new AlterViewStatement(sqlserver);
        redefineView.setView(createTable("logic_db", "foo_view"));
        redefineView.setSelect(SelectStatement.builder().databaseType(sqlserver).projections(starProjection()).from(createTable("other_db", "foo_table")).build());
        DeleteMultiTableSegment deleteTables = new DeleteMultiTableSegment();
        deleteTables.getActualDeleteTables().add(createTable("logic_db", "foo_orders"));
        JoinTableSegment relation = new JoinTableSegment();
        relation.setLeft(createTable("logic_db", "foo_orders"));
        DatabaseType mysql = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        relation.setRight(new SubqueryTableSegment(0, 0, new SubquerySegment(0, 0,
                SelectStatement.builder().databaseType(mysql).projections(starProjection()).from(createTable("other_db", "foo_table")).build(), "SELECT * FROM other_db.foo_table")));
        relation.setCondition(new LiteralExpressionSegment(0, 0, true));
        deleteTables.setRelationTable(relation);
        IndexSegment renameIndex = new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("foo_new_index")));
        return Stream.of(
                Arguments.of("materialized view body", "Oracle", "CREATE MATERIALIZED VIEW logic_db.foo_view AS SELECT other_db.foo_read() FROM dual", materializedView, "other_db.foo_read"),
                Arguments.of("alter and rename index", "PostgreSQL", "ALTER INDEX other_db.foo_index RENAME TO foo_new_index", new AlterIndexStatement(postgresql, index, renameIndex, null),
                        "other_db.foo_index"),
                Arguments.of("alter and rename database", "PostgreSQL", "ALTER DATABASE other_db RENAME TO foo_new_database", alterDatabase, "other_db"),
                Arguments.of("drop database", "PostgreSQL", "DROP DATABASE other_db", new DropDatabaseStatement(postgresql, "other_db", false), "other_db"),
                Arguments.of("rename view", "PostgreSQL", "ALTER VIEW other_db.foo_view RENAME TO foo_new_view", renameView, "other_db.foo_view"),
                Arguments.of("redefine view", "SQLServer", "ALTER VIEW logic_db.foo_view AS SELECT * FROM other_db.foo_table", redefineView, "other_db.foo_table"),
                Arguments.of("delete via subquery relation", "MySQL", "DELETE logic_db.foo_orders FROM logic_db.foo_orders JOIN (SELECT * FROM other_db.foo_table) foo_source ON 1=1",
                        DeleteStatement.builder().databaseType(mysql).table(deleteTables).build(), "other_db.foo_table"));
    }
    
    private static SimpleTableSegment createTable(final String owner, final String name) {
        SimpleTableSegment result = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(name)));
        result.setOwner(new OwnerSegment(0, 0, new IdentifierValue(owner)));
        return result;
    }
    
    private static SelectStatement selectExpression(final DatabaseType databaseType, final ExpressionSegment expression) {
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ExpressionProjectionSegment(0, 0, expression.getText(), expression));
        return SelectStatement.builder().databaseType(databaseType).projections(projections).build();
    }
    
    private static FunctionSegment createForeignFunction() {
        FunctionSegment result = new FunctionSegment(0, 0, "foo_read", "other_db.foo_read()");
        result.setOwner(new OwnerSegment(0, 0, new IdentifierValue("other_db")));
        return result;
    }
    
    private static ProjectionsSegment starProjection() {
        ProjectionsSegment result = new ProjectionsSegment(0, 0);
        result.getProjections().add(new ShorthandProjectionSegment(0, 0));
        return result;
    }
    
    private static Stream<Arguments> parsedExpressionStatements() {
        DatabaseType oracle = TypedSPILoader.getService(DatabaseType.class, "Oracle");
        FunctionSegment function = createForeignFunction();
        KeyValueSegment keyValue = new KeyValueSegment(0, 0, "'foo':other_db.foo_read()");
        keyValue.setKey(new LiteralExpressionSegment(0, 0, "foo"));
        keyValue.setValue(function);
        FunctionSegment json = new FunctionSegment(0, 0, "JSON_OBJECT", "JSON_OBJECT('foo':other_db.foo_read())");
        json.getParameters().add(keyValue);
        MatchAgainstExpression match = new MatchAgainstExpression(0, 0, function, "", "MATCH(foo_content) AGAINST(other_db.foo_read())");
        match.getColumns().add(new ColumnSegment(0, 0, new IdentifierValue("foo_content")));
        SubquerySegment subquery = new SubquerySegment(0, 0, selectExpression(oracle, function), "SELECT other_db.foo_read() FROM dual");
        ProjectionsSegment subqueryProjections = new ProjectionsSegment(0, 0);
        subqueryProjections.getProjections().add(new SubqueryProjectionSegment(subquery, "(SELECT other_db.foo_read() FROM dual)"));
        LiteralExpressionSegment literal = new LiteralExpressionSegment(0, 0, 1);
        ListExpression list = new ListExpression(0, 0);
        list.getItems().add(function);
        RowExpression row = new RowExpression(0, 0, "(other_db.foo_read(), 1)");
        row.getItems().add(function);
        row.getItems().add(literal);
        RowExpression comparisonRow = new RowExpression(0, 0, "(1, 1)");
        comparisonRow.getItems().addAll(List.of(literal, literal));
        FunctionSegment datetimeFunction = new FunctionSegment(0, 0, "COALESCE", "COALESCE(other_db.foo_read() AT TIME ZONE 'UTC', NULL)");
        datetimeFunction.getParameters().add(new DatetimeExpression(0, 0, function, new ExpressionProjectionSegment(0, 0, "'UTC'", new LiteralExpressionSegment(0, 0, "UTC")), "AT TIME ZONE"));
        datetimeFunction.getParameters().add(new LiteralExpressionSegment(0, 0, null));
        ValuesExpression values = new ValuesExpression(0, 0);
        values.getRowConstructorList().add(new InsertValuesSegment(0, 0, List.of(function)));
        DatabaseType postgresql = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        DatabaseType mysql = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        DatabaseType sqlserver = TypedSPILoader.getService(DatabaseType.class, "SQLServer");
        return Stream.of(
                Arguments.of("JSON key value", "SQLServer", "SELECT JSON_OBJECT('foo':other_db.foo_read())", selectExpression(sqlserver, json), "other_db.foo_read"),
                Arguments.of("match expression", "MySQL", "SELECT MATCH(foo_content) AGAINST(other_db.foo_read()) FROM foo_documents", selectExpression(mysql, match), "other_db.foo_read"),
                Arguments.of("scalar subquery projection", "Oracle", "SELECT (SELECT other_db.foo_read() FROM dual) FROM dual",
                        SelectStatement.builder().databaseType(oracle).projections(subqueryProjections).build(), "other_db.foo_read"),
                Arguments.of("scalar subquery expression", "Oracle", "SELECT 1 + (SELECT other_db.foo_read() FROM dual) FROM dual",
                        selectExpression(oracle, new BinaryOperationExpression(0, 0, new LiteralExpressionSegment(0, 0, 1), new SubqueryExpressionSegment(subquery), "+", "1 + subquery")),
                        "other_db.foo_read"),
                Arguments.of("collection table expression", "Oracle", "SELECT * FROM TABLE(other_db.foo_read())",
                        SelectStatement.builder().databaseType(oracle).projections(starProjection()).from(new CollectionTableSegment(function)).build(), "other_db.foo_read"),
                Arguments.of("between expression", "PostgreSQL", "SELECT 1 BETWEEN other_db.foo_read() AND 3",
                        selectExpression(postgresql, new BetweenExpression(0, 0, literal, function, literal, false)), "other_db.foo_read"),
                Arguments.of("in expression", "PostgreSQL", "SELECT 1 IN (other_db.foo_read())", selectExpression(postgresql, new InExpression(0, 0, literal, list, false)), "other_db.foo_read"),
                Arguments.of("not expression", "PostgreSQL", "SELECT NOT other_db.foo_read()", selectExpression(postgresql, new NotExpression(0, 0, function, false)), "other_db.foo_read"),
                Arguments.of("type cast expression", "PostgreSQL", "SELECT CAST(other_db.foo_read() AS INTEGER)",
                        selectExpression(postgresql, new TypeCastExpression(0, 0, "CAST", function, "INTEGER")), "other_db.foo_read"),
                Arguments.of("collate expression", "MySQL", "SELECT other_db.foo_read() COLLATE utf8_bin",
                        selectExpression(mysql, new CollateExpression(0, 0, new LiteralExpressionSegment(0, 0, "utf8_bin"), function)), "other_db.foo_read"),
                Arguments.of("datetime expression", "Oracle", "SELECT COALESCE(other_db.foo_read() AT TIME ZONE 'UTC', NULL) FROM dual",
                        selectExpression(oracle, datetimeFunction), "other_db.foo_read"),
                Arguments.of("interval expression", "MySQL", "SELECT INTERVAL other_db.foo_read() DAY", selectExpression(mysql, new IntervalExpression(0, 0, function, IntervalUnit.DAY, "INTERVAL")),
                        "other_db.foo_read"),
                Arguments.of("unary expression", "MySQL", "SELECT -other_db.foo_read()", selectExpression(mysql, new UnaryOperationExpression(0, 0, function, "-", "-other_db.foo_read()")),
                        "other_db.foo_read"),
                Arguments.of("row expression", "MySQL", "SELECT (other_db.foo_read(), 1) = (1, 1)",
                        selectExpression(mysql, new BinaryOperationExpression(0, 0, row, comparisonRow, "=", "tuple comparison")), "other_db.foo_read"),
                Arguments.of("values expression", "MySQL", "SELECT * FROM (VALUES ROW(other_db.foo_read())) foo_values",
                        SelectStatement.builder().databaseType(mysql).projections(starProjection()).from(new SubqueryTableSegment(0, 0,
                                new SubquerySegment(0, 0, selectExpression(mysql, values), "VALUES ROW(other_db.foo_read())"))).build(),
                        "other_db.foo_read"),
                Arguments.of("case expression", "PostgreSQL", "SELECT CASE other_db.foo_read() WHEN 1 THEN 1 ELSE 1 END",
                        selectExpression(postgresql, new CaseWhenExpression(0, 0, function, List.of(literal), List.of(literal), literal, "CASE")), "other_db.foo_read"),
                Arguments.of("quantified subquery", "MySQL", "SELECT 1 = ANY (SELECT other_db.foo_read())",
                        selectExpression(mysql, new BinaryOperationExpression(0, 0, literal,
                                new QuantifySubqueryExpression(0, 0, new SubquerySegment(0, 0, selectExpression(mysql, function), "SELECT other_db.foo_read()"), "ANY"), "=", "quantified comparison")),
                        "other_db.foo_read"));
    }
    
    private static Stream<Arguments> parsedXMLStatements() {
        DatabaseType oracle = TypedSPILoader.getService(DatabaseType.class, "Oracle");
        FunctionSegment function = createForeignFunction();
        LiteralExpressionSegment literal = new LiteralExpressionSegment(0, 0, "foo");
        XmlElementFunctionSegment element = new XmlElementFunctionSegment(0, 0, "XMLELEMENT", new IdentifierValue("foo_node"), "XMLELEMENT");
        element.getXmlAttributes().add(function);
        element.getParameters().add(literal);
        XmlQueryAndExistsFunctionSegment query = new XmlQueryAndExistsFunctionSegment(0, 0, "XMLQUERY", "'/root'", "XMLQUERY");
        query.getParameters().add(function);
        XmlTableOptionsSegment options = new XmlTableOptionsSegment(0, 0, "PASSING other_db.foo_read() COLUMNS foo_value VARCHAR2(100) PATH '/root' DEFAULT 'foo'");
        options.getParameters().add(function);
        options.getXmlTableColumnSegments().add(new XmlTableColumnSegment(0, 0, "foo_value", "VARCHAR2(100)", "'/root'", literal, "foo_value"));
        XmlTableFunctionSegment table = new XmlTableFunctionSegment(0, 0, "XMLTABLE", null, "'/root'", options, "XMLTABLE");
        return Stream.of(
                Arguments.of("XML attributes", "Oracle", "SELECT XMLELEMENT(foo_node, XMLATTRIBUTES(other_db.foo_read()), 'foo') FROM dual", selectExpression(oracle, element), "other_db.foo_read"),
                Arguments.of("XML evaluated name", "Oracle", "SELECT XMLPI(EVALNAME other_db.foo_read(), 'foo') FROM dual",
                        selectExpression(oracle, new XmlPiFunctionSegment(0, 0, "XMLPI", function, literal, "XMLPI")), "other_db.foo_read"),
                Arguments.of("XML static name", "Oracle", "SELECT XMLPI(NAME foo_node, other_db.foo_read()) FROM dual",
                        selectExpression(oracle, new XmlPiFunctionSegment(0, 0, "XMLPI", "foo_node", function, "XMLPI")), "other_db.foo_read"),
                Arguments.of("XML query parameters", "Oracle", "SELECT XMLQUERY('/root' PASSING other_db.foo_read() RETURNING CONTENT) FROM dual", selectExpression(oracle, query),
                        "other_db.foo_read"),
                Arguments.of("XML serialize parameters", "Oracle", "SELECT XMLSERIALIZE(CONTENT other_db.foo_read() AS VARCHAR2(100)) FROM dual",
                        selectExpression(oracle, new XmlSerializeFunctionSegment(0, 0, "XMLSERIALIZE", function, "VARCHAR2(100)", null, null, null, "XMLSERIALIZE")), "other_db.foo_read"),
                Arguments.of("XML table parameters", "Oracle", "SELECT * FROM XMLTABLE('/root' PASSING other_db.foo_read() COLUMNS foo_value VARCHAR2(100) PATH '/root' DEFAULT 'foo')",
                        SelectStatement.builder().databaseType(oracle).projections(starProjection()).from(new FunctionTableSegment(0, 0, table)).build(), "other_db.foo_read"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource({"parsedSelectClauses", "parsedDMLClauses", "parsedMergeClauses"})
    void assertExecuteWithParsedClauseReferences(final String name, final String databaseType, final String sql, final SQLStatement statement, final Set<String> expectedObjects) {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY, SupportedMCPStatement.DML, SupportedMCPStatement.DDL));
        when(capability.getDatabaseType()).thenReturn(databaseType);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        mockSessionLock(coordinator);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, mock(MCPJdbcTransactionStatementExecutor.class), statementExecutor);
        SQLExecutionRequest request = createExecutionRequest(sql);
        SQLExecutionResult expectedResult = mock(SQLExecutionResult.class);
        when(statementExecutor.execute(eq(request), any(ClassificationResult.class), eq(capability))).thenReturn(expectedResult);
        try (
                MockedConstruction<ShardingSphereSQLParserEngine> ignored = mockConstruction(ShardingSphereSQLParserEngine.class,
                        (mock, context) -> when(mock.parse(anyString(), eq(false))).thenReturn(statement))) {
            assertThat(facade.execute(request), sameInstance(expectedResult));
            ArgumentCaptor<ClassificationResult> actual = ArgumentCaptor.forClass(ClassificationResult.class);
            verify(statementExecutor).execute(eq(request), actual.capture(), eq(capability));
            assertThat(actual.getValue().getReferencedObjects().stream().map(SQLStatementObjectName::getObjectName).collect(Collectors.toSet()), hasItems(expectedObjects.toArray(new String[0])));
        }
    }
    
    private static Stream<Arguments> parsedSelectClauses() {
        WindowItemSegment item = new WindowItemSegment(0, 0);
        item.setPartitionListSegments(List.of(createCurrentFunction("foo_partition")));
        item.setOrderBySegment(expressionOrder(createCurrentFunction("foo_order")));
        item.setFrameClause(new CommonExpressionSegment(0, 0, "ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW", List.of()));
        AggregationProjectionSegment aggregation = new AggregationProjectionSegment(0, 0, AggregationType.SUM, "SUM(logic_db.foo_value())");
        aggregation.getParameters().add(createCurrentFunction("foo_value"));
        aggregation.setWindow(item);
        ProjectionsSegment aggregationProjections = new ProjectionsSegment(0, 0);
        aggregationProjections.getProjections().add(aggregation);
        WindowSegment window = new WindowSegment(0, 0);
        window.getItemSegments().add(item);
        DatabaseType oracle = TypedSPILoader.getService(DatabaseType.class, "Oracle");
        ModelSegment model = new ModelSegment(0, 0);
        model.getReferenceModelSelects().add(new SubquerySegment(0, 0, selectExpression(oracle, createCurrentFunction("foo_reference")), "SELECT logic_db.foo_reference() FROM dual"));
        model.getCellAssignmentSelects().add(new SubquerySegment(0, 0, selectExpression(oracle, createCurrentFunction("foo_cell")), "SELECT logic_db.foo_cell() FROM dual"));
        model.getOrderBySegments().add(expressionOrder(createCurrentFunction("foo_model_order")));
        model.getPartitionColumns().add(new ModelColumnSegment(0, 0, createCurrentFunction("foo_model_partition")));
        model.getDimensionColumns().add(new ModelColumnSegment(0, 0, createCurrentFunction("foo_dimension")));
        model.getMeasureColumns().add(new ModelColumnSegment(0, 0, createCurrentFunction("foo_measure")));
        model.getCellAssignmentColumns().add(new ColumnSegment(0, 0, new IdentifierValue("foo_measure")));
        ProjectionsSegment datetime = new ProjectionsSegment(0, 0);
        datetime.getProjections().add(new DatetimeProjectionSegment(0, 0, createCurrentFunction("foo_datetime"),
                new ExpressionProjectionSegment(0, 0, "'UTC'", new LiteralExpressionSegment(0, 0, "UTC")), "AT TIME ZONE"));
        ProjectionsSegment interval = new ProjectionsSegment(0, 0);
        IntervalExpressionProjection intervalProjection = new IntervalExpressionProjection(0, 0, createCurrentFunction("foo_interval_left"), null,
                createCurrentFunction("foo_interval_right"), "INTERVAL DAY TO SECOND");
        intervalProjection.setDayToSecondExpression(new IntervalDayToSecondExpression(0, 0, "DAY", "TO", "SECOND"));
        interval.getProjections().add(intervalProjection);
        DatabaseType postgresql = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        LimitSegment limit = new LimitSegment(0, 0, new ExpressionLimitValueSegment(0, 0, createCurrentFunction("foo_offset")),
                new ExpressionLimitValueSegment(0, 0, createCurrentFunction("foo_limit")));
        return Stream.of(
                Arguments.of("aggregate window", "PostgreSQL",
                        "SELECT SUM(logic_db.foo_value()) OVER (PARTITION BY logic_db.foo_partition() ORDER BY logic_db.foo_order() ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)",
                        SelectStatement.builder().databaseType(postgresql).projections(aggregationProjections).build(), Set.of("logic_db.foo_value", "logic_db.foo_partition", "logic_db.foo_order")),
                Arguments.of("window clauses", "PostgreSQL",
                        "SELECT 1 WINDOW foo_window AS (PARTITION BY logic_db.foo_partition() ORDER BY logic_db.foo_order() ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)",
                        SelectStatement.builder().databaseType(postgresql).projections(starProjection()).window(window).build(), Set.of("logic_db.foo_partition", "logic_db.foo_order")),
                Arguments.of("pagination expressions", "PostgreSQL", "SELECT 1 LIMIT logic_db.foo_limit() OFFSET logic_db.foo_offset()",
                        SelectStatement.builder().databaseType(postgresql).projections(starProjection()).limit(limit).build(), Set.of("logic_db.foo_offset", "logic_db.foo_limit")),
                Arguments.of("model clauses", "Oracle", "SELECT * FROM dual MODEL REFERENCE foo_ref ON (SELECT logic_db.foo_reference() FROM dual) DIMENSION BY (foo_key) MEASURES (foo_value) "
                        + "MAIN foo_main PARTITION BY (logic_db.foo_model_partition()) DIMENSION BY (logic_db.foo_dimension() foo_key) MEASURES (logic_db.foo_measure() foo_measure) "
                        + "RULES (foo_measure[FOR foo_key IN (SELECT logic_db.foo_cell() FROM dual)] ORDER BY logic_db.foo_model_order() = 1)",
                        SelectStatement.builder().databaseType(oracle).projections(starProjection()).model(model).build(),
                        Set.of("logic_db.foo_reference", "logic_db.foo_cell", "logic_db.foo_model_order", "logic_db.foo_model_partition", "logic_db.foo_dimension", "logic_db.foo_measure")),
                Arguments.of("datetime projections", "Oracle", "SELECT logic_db.foo_datetime() AT TIME ZONE 'UTC' FROM dual",
                        SelectStatement.builder().databaseType(oracle).projections(datetime).build(), Set.of("logic_db.foo_datetime")),
                Arguments.of("interval projections", "Oracle", "SELECT (logic_db.foo_interval_left() - logic_db.foo_interval_right()) DAY TO SECOND FROM dual",
                        SelectStatement.builder().databaseType(oracle).projections(interval).build(), Set.of("logic_db.foo_interval_left", "logic_db.foo_interval_right")));
    }
    
    private static FunctionSegment createCurrentFunction(final String name) {
        FunctionSegment result = new FunctionSegment(0, 0, name, "logic_db." + name + "()");
        result.setOwner(new OwnerSegment(0, 0, new IdentifierValue("logic_db")));
        return result;
    }
    
    private static OrderBySegment expressionOrder(final ExpressionSegment expression) {
        return new OrderBySegment(0, 0, List.of(new ExpressionOrderByItemSegment(0, 0, expression.getText(), OrderDirection.ASC, NullsOrderType.FIRST, expression)));
    }
    
    private static Stream<Arguments> parsedDMLClauses() {
        DatabaseType mysql = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        DatabaseType postgresql = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        DatabaseType sqlserver = TypedSPILoader.getService(DatabaseType.class, "SQLServer");
        DatabaseType oracle = TypedSPILoader.getService(DatabaseType.class, "Oracle");
        SimpleTableSegment table = createTable("logic_db", "foo_orders");
        InsertStatement insertAssignment = InsertStatement.builder().databaseType(mysql).table(table).setAssignment(assignment("foo_insert"))
                .onDuplicateKeyColumns(new OnDuplicateKeyColumnsSegment(0, 0, assignment("foo_duplicate").getAssignments())).build();
        InsertStatement insertReturning = InsertStatement.builder().databaseType(postgresql).table(table).values(List.of(new InsertValuesSegment(0, 0, List.of(createCurrentFunction("foo_value")))))
                .returning(new ReturningSegment(0, 0, expressionProjection(createCurrentFunction("foo_returning")))).build();
        ExecSegment exec = new ExecSegment(0, 0);
        exec.setProcedureName(new FunctionNameSegment(0, 0, new IdentifierValue("foo_procedure")));
        exec.getExpressionSegments().add(new LiteralExpressionSegment(0, 0, 1));
        InsertStatement insertExec = InsertStatement.builder().databaseType(sqlserver).table(table).exec(exec).output(output("foo_output")).build();
        MultiTableInsertIntoSegment multi = new MultiTableInsertIntoSegment(0, 0);
        multi.getInsertStatements().add(InsertStatement.builder().databaseType(oracle).table(table).values(List.of(new InsertValuesSegment(0, 0, List.of(createCurrentFunction("foo_all"))))).build());
        MultiTableConditionalIntoSegment conditional = new MultiTableConditionalIntoSegment(0, 0);
        conditional.getWhenThenSegments().add(new MultiTableConditionalIntoWhenThenSegment(0, 0, createCurrentFunction("foo_when"), new MultiTableConditionalIntoThenSegment(0, 0,
                List.of(InsertStatement.builder().databaseType(oracle).table(table).values(List.of(new InsertValuesSegment(0, 0, List.of(createCurrentFunction("foo_then"))))).build()))));
        conditional.setElseSegment(new MultiTableConditionalIntoElseSegment(0, 0,
                List.of(InsertStatement.builder().databaseType(oracle).table(table).values(List.of(new InsertValuesSegment(0, 0, List.of(createCurrentFunction("foo_else"))))).build())));
        UpdateStatement updateReturning = UpdateStatement.builder().databaseType(postgresql).table(table).setAssignment(assignment("foo_update"))
                .from(createTable("logic_db", "foo_source")).returning(new ReturningSegment(0, 0, expressionProjection(createCurrentFunction("foo_returning")))).build();
        UpdateStatement updateOrdered = UpdateStatement.builder().databaseType(mysql).table(table).setAssignment(assignment("foo_update"))
                .where(new WhereSegment(0, 0, createCurrentFunction("foo_filter"))).orderBy(expressionOrder(createCurrentFunction("foo_order"))).build();
        return Stream.of(
                Arguments.of("insert assignments", "MySQL", "INSERT INTO logic_db.foo_orders SET foo_value=logic_db.foo_insert() ON DUPLICATE KEY UPDATE foo_value=logic_db.foo_duplicate()",
                        insertAssignment, Set.of("logic_db.foo_orders", "logic_db.foo_insert", "logic_db.foo_duplicate")),
                Arguments.of("insert returning", "PostgreSQL", "INSERT INTO logic_db.foo_orders VALUES (logic_db.foo_value()) RETURNING logic_db.foo_returning()",
                        insertReturning, Set.of("logic_db.foo_orders", "logic_db.foo_value", "logic_db.foo_returning")),
                Arguments.of("insert exec output", "SQLServer", "INSERT INTO logic_db.foo_orders OUTPUT logic_db.foo_output() EXEC foo_procedure 1",
                        insertExec, Set.of("logic_db.foo_orders", "logic_db.foo_output")),
                Arguments.of("insert into all", "Oracle", "INSERT ALL INTO logic_db.foo_orders VALUES (logic_db.foo_all()) SELECT 1 FROM dual",
                        InsertStatement.builder().databaseType(oracle).multiTableInsertType(MultiTableInsertType.ALL).multiTableInsertInto(multi)
                                .insertSelect(new SubquerySegment(0, 0, selectExpression(oracle, new LiteralExpressionSegment(0, 0, 1)), "SELECT 1 FROM dual")).build(),
                        Set.of("logic_db.foo_all")),
                Arguments.of("conditional insert into", "Oracle", "INSERT FIRST WHEN logic_db.foo_when()=1 THEN INTO logic_db.foo_orders VALUES (logic_db.foo_then()) "
                        + "ELSE INTO logic_db.foo_orders VALUES (logic_db.foo_else()) SELECT 1 FROM dual",
                        InsertStatement.builder().databaseType(oracle).multiTableInsertType(MultiTableInsertType.FIRST).multiTableConditionalInto(conditional)
                                .insertSelect(new SubquerySegment(0, 0, selectExpression(oracle, new LiteralExpressionSegment(0, 0, 1)), "SELECT 1 FROM dual")).build(),
                        Set.of("logic_db.foo_when", "logic_db.foo_then", "logic_db.foo_else")),
                Arguments.of("update from returning", "PostgreSQL", "UPDATE logic_db.foo_orders SET foo_value=logic_db.foo_update() FROM logic_db.foo_source RETURNING logic_db.foo_returning()",
                        updateReturning, Set.of("logic_db.foo_orders", "logic_db.foo_source", "logic_db.foo_update", "logic_db.foo_returning")),
                Arguments.of("update ordered", "MySQL", "UPDATE logic_db.foo_orders SET foo_value=logic_db.foo_update() WHERE logic_db.foo_filter()=1 ORDER BY logic_db.foo_order()", updateOrdered,
                        Set.of("logic_db.foo_orders", "logic_db.foo_update", "logic_db.foo_filter", "logic_db.foo_order")),
                Arguments.of("update output", "SQLServer", "UPDATE logic_db.foo_orders SET foo_value=logic_db.foo_update() OUTPUT logic_db.foo_output()",
                        UpdateStatement.builder().databaseType(sqlserver).table(table).setAssignment(assignment("foo_update")).output(output("foo_output")).build(),
                        Set.of("logic_db.foo_orders", "logic_db.foo_update", "logic_db.foo_output")),
                Arguments.of("delete ordered", "MySQL", "DELETE FROM logic_db.foo_orders ORDER BY logic_db.foo_order()",
                        DeleteStatement.builder().databaseType(mysql).table(table).orderBy(expressionOrder(createCurrentFunction("foo_order"))).build(),
                        Set.of("logic_db.foo_orders", "logic_db.foo_order")),
                Arguments.of("delete returning", "PostgreSQL", "DELETE FROM logic_db.foo_orders RETURNING logic_db.foo_returning()",
                        DeleteStatement.builder().databaseType(postgresql).table(table).returning(new ReturningSegment(0, 0, expressionProjection(createCurrentFunction("foo_returning")))).build(),
                        Set.of("logic_db.foo_orders", "logic_db.foo_returning")),
                Arguments.of("delete output", "SQLServer", "DELETE FROM logic_db.foo_orders OUTPUT logic_db.foo_output()",
                        DeleteStatement.builder().databaseType(sqlserver).table(table).output(output("foo_output")).build(), Set.of("logic_db.foo_orders", "logic_db.foo_output")));
    }
    
    private static SetAssignmentSegment assignment(final String functionName) {
        return new SetAssignmentSegment(0, 0, List.of(new ColumnAssignmentSegment(0, 0, List.of(new ColumnSegment(0, 0, new IdentifierValue("foo_value"))), createCurrentFunction(functionName))));
    }
    
    private static ProjectionsSegment expressionProjection(final ExpressionSegment expression) {
        ProjectionsSegment result = new ProjectionsSegment(0, 0);
        result.getProjections().add(new ExpressionProjectionSegment(0, 0, expression.getText(), expression));
        return result;
    }
    
    private static OutputSegment output(final String functionName) {
        OutputSegment result = new OutputSegment(0, 0);
        result.setOutputColumns(expressionProjection(createCurrentFunction(functionName)));
        return result;
    }
    
    private static Stream<Arguments> parsedMergeClauses() {
        DatabaseType oracle = TypedSPILoader.getService(DatabaseType.class, "Oracle");
        DatabaseType sqlserver = TypedSPILoader.getService(DatabaseType.class, "SQLServer");
        SimpleTableSegment target = createTable("logic_db", "foo_orders");
        SimpleTableSegment source = createTable("logic_db", "foo_source");
        UpdateStatement update = UpdateStatement.builder().databaseType(oracle).setAssignment(assignment("foo_update"))
                .deleteWhere(new WhereSegment(0, 0, createCurrentFunction("foo_delete"))).build();
        InsertStatement insert = InsertStatement.builder().databaseType(oracle).values(List.of(new InsertValuesSegment(0, 0, List.of(createCurrentFunction("foo_insert")))))
                .where(new WhereSegment(0, 0, createCurrentFunction("foo_where"))).build();
        MergeWhenAndThenSegment updateClause = new MergeWhenAndThenSegment(0, 0, "WHEN MATCHED");
        updateClause.setAndExpr(createCurrentFunction("foo_matched"));
        updateClause.setUpdate(UpdateStatement.builder().databaseType(sqlserver).setAssignment(assignment("foo_update")).build());
        MergeWhenAndThenSegment insertClause = new MergeWhenAndThenSegment(0, 0, "WHEN NOT MATCHED");
        insertClause.setAndExpr(createCurrentFunction("foo_unmatched"));
        insertClause.setInsert(InsertStatement.builder().databaseType(sqlserver).values(List.of(new InsertValuesSegment(0, 0, List.of(createCurrentFunction("foo_insert"))))).build());
        DatabaseType postgresql = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        CombineSegment combine = new CombineSegment(0, 0, new SubquerySegment(0, 0, selectExpression(postgresql, createCurrentFunction("foo_left")), "SELECT logic_db.foo_left()"),
                CombineType.UNION, new SubquerySegment(0, 0, selectExpression(postgresql, createCurrentFunction("foo_right")), "SELECT logic_db.foo_right()"));
        HierarchicalQuerySegment hierarchical = new HierarchicalQuerySegment(0, 0);
        hierarchical.setStartWith(createCurrentFunction("foo_start"));
        hierarchical.setConnectBy(createCurrentFunction("foo_connect"));
        return Stream.of(
                Arguments.of("merge update and insert", "Oracle", "MERGE INTO logic_db.foo_orders USING logic_db.foo_source ON (logic_db.foo_on()=1) "
                        + "WHEN MATCHED THEN UPDATE SET foo_value=logic_db.foo_update() DELETE WHERE logic_db.foo_delete()=1 "
                        + "WHEN NOT MATCHED THEN INSERT (foo_value) VALUES (logic_db.foo_insert()) WHERE logic_db.foo_where()=1",
                        MergeStatement.builder().databaseType(oracle).target(target).source(source).expression(new ExpressionWithParamsSegment(0, 0, createCurrentFunction("foo_on")))
                                .update(update).insert(insert).build(),
                        Set.of("logic_db.foo_orders", "logic_db.foo_source", "logic_db.foo_on", "logic_db.foo_update", "logic_db.foo_delete", "logic_db.foo_insert", "logic_db.foo_where")),
                Arguments.of("merge conditional clauses", "SQLServer", "MERGE INTO logic_db.foo_orders USING logic_db.foo_source ON logic_db.foo_on()=1 "
                        + "WHEN MATCHED AND logic_db.foo_matched()=1 THEN UPDATE SET foo_value=logic_db.foo_update() "
                        + "WHEN NOT MATCHED AND logic_db.foo_unmatched()=1 THEN INSERT (foo_value) VALUES (logic_db.foo_insert()) OUTPUT logic_db.foo_output()",
                        MergeStatement.builder().databaseType(sqlserver).target(target).source(source).expression(new ExpressionWithParamsSegment(0, 0, createCurrentFunction("foo_on")))
                                .whenAndThens(List.of(updateClause, insertClause)).output(output("foo_output")).build(),
                        Set.of("logic_db.foo_orders", "logic_db.foo_source", "logic_db.foo_on", "logic_db.foo_matched", "logic_db.foo_unmatched", "logic_db.foo_update", "logic_db.foo_insert",
                                "logic_db.foo_output")),
                Arguments.of("combined selects", "PostgreSQL", "SELECT logic_db.foo_left() UNION SELECT logic_db.foo_right()",
                        SelectStatement.builder().databaseType(postgresql).projections(expressionProjection(createCurrentFunction("foo_left"))).combine(combine).build(),
                        Set.of("logic_db.foo_left", "logic_db.foo_right")),
                Arguments.of("hierarchical select", "Oracle", "SELECT * FROM dual START WITH logic_db.foo_start()=1 CONNECT BY logic_db.foo_connect()=1",
                        SelectStatement.builder().databaseType(oracle).projections(starProjection()).hierarchicalQuery(hierarchical).build(), Set.of("logic_db.foo_start", "logic_db.foo_connect")),
                Arguments.of("select clause references", "PostgreSQL", "SELECT * FROM logic_db.foo_orders WHERE logic_db.foo_where()=1 GROUP BY logic_db.foo_group() "
                        + "HAVING logic_db.foo_having()=1 ORDER BY logic_db.foo_order()",
                        SelectStatement.builder().databaseType(postgresql).projections(starProjection()).from(target).where(new WhereSegment(0, 0, createCurrentFunction("foo_where")))
                                .groupBy(new GroupBySegment(0, 0, expressionOrder(createCurrentFunction("foo_group")).getOrderByItems()))
                                .having(new HavingSegment(0, 0, createCurrentFunction("foo_having"))).orderBy(expressionOrder(createCurrentFunction("foo_order"))).build(),
                        Set.of("logic_db.foo_orders", "logic_db.foo_where", "logic_db.foo_group", "logic_db.foo_having", "logic_db.foo_order")));
    }
    
    @Test
    void assertExecuteWithMissingStatementVisitor() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPDatabaseCapability capability = mock(MCPDatabaseCapability.class);
        when(capability.getDatabaseType()).thenReturn("MySQL");
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        mockSessionLock(coordinator);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, mock(MCPJdbcTransactionStatementExecutor.class), mock(MCPJdbcStatementExecutor.class));
        try (MockedStatic<DatabaseTypedSPILoader> mocked = mockStatic(DatabaseTypedSPILoader.class, CALLS_REAL_METHODS)) {
            mocked.when(() -> DatabaseTypedSPILoader.findService(SQLStatementVisitorFacade.class, databaseType)).thenReturn(Optional.empty());
            MCPUnsupportedException actual = assertThrows(MCPUnsupportedException.class, () -> facade.execute(createExecutionRequest("SELECT 1")));
            assertThat(actual.getMessage(), is("SQL parser is not available for database type `MySQL`."));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("malformedDelimiterInputs")
    void assertExecuteWithMalformedDelimiter(final String name, final String databaseType, final String sql) {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPDatabaseCapability capability = mock(MCPDatabaseCapability.class);
        when(capability.getDatabaseType()).thenReturn(databaseType);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        mockSessionLock(coordinator);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, mock(MCPJdbcTransactionStatementExecutor.class), mock(MCPJdbcStatementExecutor.class));
        try (MockedConstruction<ShardingSphereSQLParserEngine> actual = mockConstruction(ShardingSphereSQLParserEngine.class)) {
            assertThrows(MCPUnsupportedSQLStatementException.class, () -> facade.execute(createExecutionRequest(sql)));
            assertTrue(actual.constructed().isEmpty());
        }
    }
    
    private static Stream<Arguments> malformedDelimiterInputs() {
        return Stream.of(
                Arguments.of("bare string quote", "MySQL", "SELECT '"),
                Arguments.of("bare identifier quote", "MySQL", "SELECT `"),
                Arguments.of("bare ANSI quote", "PostgreSQL", "SELECT \""),
                Arguments.of("unterminated fallback block comment", "ClickHouse", "SELECT 1 /* foo"));
    }
    
    @Test
    void assertExecuteWithEmptyParsedStatement() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPDatabaseCapability capability = mock(MCPDatabaseCapability.class);
        when(capability.getDatabaseType()).thenReturn("PostgreSQL");
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        mockSessionLock(coordinator);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, mock(MCPJdbcTransactionStatementExecutor.class), statementExecutor);
        try (
                MockedConstruction<ShardingSphereSQLParserEngine> ignored = mockConstruction(ShardingSphereSQLParserEngine.class,
                        (mock, context) -> when(mock.parse(anyString(), eq(false))).thenReturn(new EmptyStatement(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"))))) {
            assertThrows(MCPUnsupportedSQLStatementException.class, () -> facade.execute(createExecutionRequest("/**/")));
            verifyNoInteractions(statementExecutor);
        }
    }
    
    @Test
    void assertExecuteWithParenthesizedStatement() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPDatabaseCapability capability = mock(MCPDatabaseCapability.class);
        when(capability.getDatabaseType()).thenReturn("MySQL");
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        mockSessionLock(coordinator);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, mock(MCPJdbcTransactionStatementExecutor.class), statementExecutor);
        SQLStatement statement = selectExpression(TypedSPILoader.getService(DatabaseType.class, "MySQL"), new LiteralExpressionSegment(0, 0, 1));
        try (
                MockedConstruction<ShardingSphereSQLParserEngine> ignored = mockConstruction(ShardingSphereSQLParserEngine.class,
                        (mock, context) -> when(mock.parse(anyString(), eq(false))).thenReturn(statement))) {
            assertThrows(MCPUnsupportedSQLStatementException.class, () -> facade.execute(createExecutionRequest("(SELECT 1)")));
            verifyNoInteractions(statementExecutor);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("parserFailureInputs")
    void assertExecuteWithParserFailure(final String name, final String databaseType, final String sql) {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPDatabaseCapability capability = mock(MCPDatabaseCapability.class);
        when(capability.getDatabaseType()).thenReturn(databaseType);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        mockSessionLock(coordinator);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, mock(MCPJdbcTransactionStatementExecutor.class), statementExecutor);
        IllegalArgumentException expectedCause = new IllegalArgumentException("foo_invalid_sql");
        try (
                MockedConstruction<ShardingSphereSQLParserEngine> actual = mockConstruction(ShardingSphereSQLParserEngine.class,
                        (mock, context) -> when(mock.parse(anyString(), eq(false))).thenThrow(expectedCause))) {
            MCPUnsupportedSQLStatementException actualException = assertThrows(MCPUnsupportedSQLStatementException.class, () -> facade.execute(createExecutionRequest(sql)));
            assertThat(actualException.getCause(), sameInstance(expectedCause));
            assertThat(actual.constructed().size(), is(1));
            verify(actual.constructed().get(0)).parse(sql, false);
            verifyNoInteractions(statementExecutor);
        }
    }
    
    private static Stream<Arguments> parserFailureInputs() {
        return Stream.of(
                Arguments.of("fallback block comment masks semicolons", "ClickHouse", "SELECT 1 /* ; SELECT 2 */"),
                Arguments.of("fallback line comment masks semicolons", "ClickHouse", "SELECT 1 -- ; SELECT 2\n +2"),
                Arguments.of("fallback line comment without newline", "ClickHouse", "SELECT 1 -- ; SELECT 2"),
                Arguments.of("incomplete division", "MySQL", "SELECT 1 /"),
                Arguments.of("spaced comment delimiter", "MySQL", "SELECT 1 / *"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("supportedLexicalQueries")
    void assertExecuteWithSupportedLexicalQuery(final String name, final String databaseType, final String sql) {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY));
        when(capability.getDatabaseType()).thenReturn(databaseType);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        mockSessionLock(coordinator);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        SQLExecutionRequest request = createExecutionRequest(sql);
        SQLExecutionResult expectedResult = mock(SQLExecutionResult.class);
        when(statementExecutor.execute(eq(request), any(), eq(capability))).thenReturn(expectedResult);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, mock(MCPJdbcTransactionStatementExecutor.class), statementExecutor);
        SQLStatement statement = selectExpression(TypedSPILoader.getService(DatabaseType.class, databaseType), new LiteralExpressionSegment(0, 0, 1));
        try (
                MockedConstruction<ShardingSphereSQLParserEngine> ignored = mockConstruction(ShardingSphereSQLParserEngine.class,
                        (mock, context) -> when(mock.parse(anyString(), eq(false))).thenReturn(statement))) {
            assertThat(facade.execute(request), sameInstance(expectedResult));
            ArgumentCaptor<ClassificationResult> actual = ArgumentCaptor.forClass(ClassificationResult.class);
            verify(statementExecutor).execute(eq(request), actual.capture(), eq(capability));
            assertThat(actual.getValue().getStatementClass(), is(SupportedMCPStatement.QUERY));
            assertThat(actual.getValue().getNormalizedSql(), is(sql));
        }
    }
    
    private static Stream<Arguments> supportedLexicalQueries() {
        return Stream.of(
                Arguments.of("Oracle trailing fullwidth whitespace", "Oracle", "SELECT 1 FROM dual　"),
                Arguments.of("Oracle leading fullwidth whitespace", "Oracle", "　SELECT 1 FROM dual"),
                Arguments.of("division expression", "MySQL", "SELECT 1 / 2"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("quotedExplainPrefixes")
    void assertExecuteExplainWithQuotedKeyword(final String name, final String databaseType, final String sql) {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.EXPLAIN));
        when(capability.getDatabaseType()).thenReturn(databaseType);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        mockSessionLock(coordinator);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        ClassificationResult classification = new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT 1", "", List.of(), false);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, mock(MCPJdbcTransactionStatementExecutor.class), statementExecutor, createMCPStatementAnalyzer(classification));
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> facade.executeExplain(createExecutionRequest(sql), "SELECT 1"));
        assertThat(actual.getMessage(), is("explain_sql must start with EXPLAIN."));
        verifyNoInteractions(statementExecutor);
    }
    
    private static Stream<Arguments> quotedExplainPrefixes() {
        return Stream.of(
                Arguments.of("ANSI quoted keyword", "PostgreSQL", "\"EXPLAIN\" SELECT 1"),
                Arguments.of("backtick quoted keyword", "MySQL", "`EXPLAIN` SELECT 1"),
                Arguments.of("bracket quoted keyword", "SQLServer", "[EXPLAIN] SELECT 1"));
    }
    
    @Test
    void assertExecuteWithMissingSession() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor);
        SQLExecutionRequest request = createExecutionRequest("SELECT 1");
        when(coordinator.executeWithSessionLock(eq("session-1"), any())).thenThrow(new MCPSessionNotExistedException());
        MCPSessionNotExistedException actual = assertThrows(MCPSessionNotExistedException.class, () -> facade.execute(request));
        assertThat(actual.getMessage(), is("Session does not exist."));
        verifyNoInteractions(capabilityProvider, transactionExecutor, statementExecutor);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertExecuteDefersWorkUntilSessionLock() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPStatementAnalyzer statementAnalyzer = mock(MCPStatementAnalyzer.class);
        SQLExecutionRequest request = createExecutionRequest("SELECT 1");
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY));
        ClassificationResult classification = new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT 1", "", List.of(), false);
        SQLExecutionResult expectedResult = mock(SQLExecutionResult.class);
        ArgumentCaptor<Supplier<SQLExecutionResult>> lockedExecution = ArgumentCaptor.forClass(Supplier.class);
        when(coordinator.executeWithSessionLock(eq("session-1"), lockedExecution.capture())).thenReturn(expectedResult);
        when(statementAnalyzer.analyze("SELECT 1", capability)).thenReturn(classification);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        when(statementExecutor.execute(request, classification, capability)).thenReturn(expectedResult);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, statementAnalyzer);
        assertThat(facade.execute(request), is(expectedResult));
        verifyNoInteractions(capabilityProvider, transactionExecutor, statementExecutor, statementAnalyzer);
        assertThat(lockedExecution.getValue().get(), is(expectedResult));
        verify(statementAnalyzer).analyze("SELECT 1", capability);
        verify(capabilityProvider).provide("logic_db");
        verify(statementExecutor).execute(request, classification, capability);
        verifyNoInteractions(transactionExecutor);
    }
    
    @Test
    void assertExecuteWithUnknownCapability() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor);
        SQLExecutionRequest request = createExecutionRequest("SELECT 1");
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.empty());
        DatabaseCapabilityNotFoundException actual = assertThrows(DatabaseCapabilityNotFoundException.class, () -> facade.execute(request));
        assertThat(actual.getMessage(), is("Database capability does not exist."));
        verifyNoInteractions(transactionExecutor, statementExecutor);
    }
    
    @Test
    void assertExecuteWithUnsupportedStatement() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPStatementAnalyzer statementAnalyzer = mock(MCPStatementAnalyzer.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY));
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        when(statementAnalyzer.analyze(anyString(), any())).thenThrow(new MCPBannedSQLStatementException());
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, statementAnalyzer);
        SQLExecutionRequest request = createExecutionRequest("SELECT 1");
        MCPBannedSQLStatementException actual = assertThrows(MCPBannedSQLStatementException.class, () -> facade.execute(request));
        assertThat(actual.getMessage(), is("Statement is banned by the MCP contract."));
        verifyNoInteractions(transactionExecutor, statementExecutor);
    }
    
    @Test
    void assertExecuteWithUnexpectedClassifierFailure() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPStatementAnalyzer statementAnalyzer = mock(MCPStatementAnalyzer.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY));
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        when(statementAnalyzer.analyze(anyString(), any())).thenThrow(new IllegalArgumentException("Malformed SQL."));
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, statementAnalyzer);
        SQLExecutionRequest request = createExecutionRequest("SELECT 1");
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> facade.execute(request));
        assertThat(actual.getMessage(), is("Malformed SQL."));
        verifyNoInteractions(transactionExecutor, statementExecutor);
    }
    
    @Test
    void assertExecuteWithUnsupportedStatementClass() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        SQLExecutionRequest request = createExecutionRequest("SELECT 1");
        ClassificationResult classification = new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT 1", "",
                List.of(SQLStatementObjectName.fromNormalizedName("orders")), false);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, createMCPStatementAnalyzer(classification));
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.DML));
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        StatementClassNotSupportedException actual = assertThrows(StatementClassNotSupportedException.class, () -> facade.execute(request));
        assertThat(actual.getMessage(), is("Statement class is not supported."));
        verifyNoInteractions(transactionExecutor, statementExecutor);
    }
    
    @Test
    void assertExecuteTransactionStatement() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.TRANSACTION_CONTROL));
        ClassificationResult classification = new ClassificationResult(SupportedMCPStatement.TRANSACTION_CONTROL, "BEGIN", "BEGIN", "", List.of(), false);
        SQLExecutionResult expectedResult = mock(SQLExecutionResult.class);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        when(transactionExecutor.execute("session-1", "logic_db", capability, classification)).thenReturn(expectedResult);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, createMCPStatementAnalyzer(classification));
        SQLExecutionRequest request = createExecutionRequest("BEGIN");
        SQLExecutionResult actual = facade.execute(request);
        assertThat(actual, is(expectedResult));
        verify(transactionExecutor).execute("session-1", "logic_db", capability, classification);
        verifyNoInteractions(statementExecutor);
    }
    
    @Test
    void assertExecuteQueryStatement() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY));
        ClassificationResult classification = new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT 1", "",
                List.of(SQLStatementObjectName.fromNormalizedName("orders")), false);
        SQLExecutionResult expectedResult = mock(SQLExecutionResult.class);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        SQLExecutionRequest request = createExecutionRequest("SELECT 1");
        when(statementExecutor.execute(request, classification, capability)).thenReturn(expectedResult);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, createMCPStatementAnalyzer(classification));
        SQLExecutionResult actual = facade.execute(request);
        assertThat(actual, is(expectedResult));
        verify(statementExecutor).execute(request, classification, capability);
        verifyNoInteractions(transactionExecutor);
    }
    
    @Test
    void assertExecuteWithCrossSchemaQueryDisabled() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY));
        SQLExecutionRequest request = createExecutionRequest("SELECT * FROM other_db.orders");
        ClassificationResult classification = new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT * FROM other_db.orders", "",
                List.of(SQLStatementObjectName.fromNormalizedName("other_db.orders")), false);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, createMCPStatementAnalyzer(classification));
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> facade.execute(request));
        assertThat(actual.getMessage(), is("Cross-schema SQL is not supported for database `logic_db`: `other_db.orders`."));
        verifyNoInteractions(transactionExecutor, statementExecutor);
    }
    
    @Test
    void assertExecuteWithCrossSchemaSubqueryDisabled() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY));
        SQLExecutionRequest request = createExecutionRequest("SELECT * FROM logic_db.orders WHERE EXISTS (SELECT 1 FROM other_db.items)");
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> facade.execute(request));
        assertThat(actual.getMessage(), is("Cross-schema SQL is not supported for database `logic_db`: `other_db.items`."));
        verifyNoInteractions(transactionExecutor, statementExecutor);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertExecuteWithCrossSchemaReferencesDisabledCases")
    void assertExecuteWithCrossSchemaReferencesDisabled(final String name, final String sql, final String expectedObjectName) {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY, SupportedMCPStatement.DML, SupportedMCPStatement.DDL, SupportedMCPStatement.DCL));
        SQLExecutionRequest request = createExecutionRequest(sql);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> facade.execute(request));
        assertThat(actual.getMessage(), is(String.format("Cross-schema SQL is not supported for database `logic_db`: `%s`.", expectedObjectName)));
        verifyNoInteractions(transactionExecutor, statementExecutor);
    }
    
    @Test
    void assertExecuteWithCrossSchemaDMLDisabled() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.DML));
        SQLExecutionRequest request = createExecutionRequest("UPDATE other_db.orders SET status = 'DONE'");
        ClassificationResult classification = new ClassificationResult(SupportedMCPStatement.DML, "UPDATE", "UPDATE other_db.orders SET status = 'DONE'", "",
                List.of(SQLStatementObjectName.fromNormalizedName("other_db.orders")), false);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, createMCPStatementAnalyzer(classification));
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> facade.execute(request));
        assertThat(actual.getMessage(), is("Cross-schema SQL is not supported for database `logic_db`: `other_db.orders`."));
        verifyNoInteractions(transactionExecutor, statementExecutor);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertExecuteWithQualifiedCurrentDatabaseCases")
    void assertExecuteWithQualifiedCurrentDatabase(final String name, final String sql, final SupportedMCPStatement supportedStatement, final String expectedStatementType) {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPDatabaseCapability capability = createCapability(Set.of(supportedStatement));
        SQLExecutionResult expectedResult = mock(SQLExecutionResult.class);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        SQLExecutionRequest request = createExecutionRequest(sql);
        when(statementExecutor.execute(eq(request), any(), eq(capability))).thenReturn(expectedResult);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor);
        SQLExecutionResult actual = facade.execute(request);
        assertThat(actual, sameInstance(expectedResult));
        ArgumentCaptor<ClassificationResult> actualClassification = ArgumentCaptor.forClass(ClassificationResult.class);
        verify(statementExecutor).execute(eq(request), actualClassification.capture(), eq(capability));
        assertThat(actualClassification.getValue().getStatementClass(), is(supportedStatement));
        assertThat(actualClassification.getValue().getStatementType(), is(expectedStatementType));
        assertThat(actualClassification.getValue().getNormalizedSql(), is(sql));
        verifyNoInteractions(transactionExecutor);
    }
    
    @Test
    void assertExecuteWithCaseInsensitiveQualifiedCurrentDatabase() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY));
        SQLExecutionResult expectedResult = mock(SQLExecutionResult.class);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        SQLExecutionRequest request = createExecutionRequest("SELECT * FROM Logic_DB.orders");
        when(statementExecutor.execute(eq(request), any(), eq(capability))).thenReturn(expectedResult);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, mock(MCPJdbcTransactionStatementExecutor.class), statementExecutor);
        assertThat(facade.execute(request), is(expectedResult));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertExecuteWithNonMatchingCurrentDatabaseIdentifierCases")
    void assertExecuteWithNonMatchingCurrentDatabaseIdentifier(final String name, final String sql, final IdentifierCasePolicySet identifierCasePolicySet) {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY));
        when(capability.getIdentifierContext()).thenReturn(new DatabaseIdentifierContext(identifierCasePolicySet));
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, mock(MCPJdbcTransactionStatementExecutor.class), mock(MCPJdbcStatementExecutor.class));
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> facade.execute(createExecutionRequest(sql)));
        assertThat(actual.getMessage(), is("Cross-schema SQL is not supported for database `logic_db`: `Logic_DB.orders`."));
    }
    
    @Test
    void assertExecuteWithCrossSchemaSqlEnabled() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY), SchemaExecutionSemantics.BEST_EFFORT);
        ClassificationResult classification = new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT * FROM other_db.orders", "",
                List.of(SQLStatementObjectName.fromNormalizedName("other_db.orders")), false);
        SQLExecutionResult expectedResult = mock(SQLExecutionResult.class);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        SQLExecutionRequest request = createExecutionRequest("SELECT * FROM other_db.orders");
        when(statementExecutor.execute(request, classification, capability)).thenReturn(expectedResult);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, createMCPStatementAnalyzer(classification));
        SQLExecutionResult actual = facade.execute(request);
        assertThat(actual, is(expectedResult));
        verify(statementExecutor).execute(request, classification, capability);
        verifyNoInteractions(transactionExecutor);
    }
    
    @Test
    void assertExecuteWithExecutorFailure() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.QUERY));
        SQLExecutionRequest request = createExecutionRequest("SELECT 1");
        ClassificationResult classification = new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT 1", "",
                List.of(SQLStatementObjectName.fromNormalizedName("orders")), false);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        when(statementExecutor.execute(request, classification, capability)).thenThrow(new MCPInvalidRequestException("bad query"));
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, createMCPStatementAnalyzer(classification));
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> facade.execute(request));
        assertThat(actual.getMessage(), is("bad query"));
        verifyNoInteractions(transactionExecutor);
    }
    
    @Test
    void assertExecuteExplain() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.EXPLAIN));
        SQLExecutionRequest request = createExecutionRequest("EXPLAIN SELECT * FROM orders");
        SQLExecutionResult expectedResult = mock(SQLExecutionResult.class);
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        when(statementExecutor.execute(eq(request), any(), eq(capability))).thenReturn(expectedResult);
        MCPJdbcTransactionStatementExecutor transactionExecutor = mock(MCPJdbcTransactionStatementExecutor.class);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor);
        SQLExecutionResult actual = facade.executeExplain(request, "SELECT * FROM orders");
        assertThat(actual, is(expectedResult));
        ArgumentCaptor<ClassificationResult> classificationCaptor = ArgumentCaptor.forClass(ClassificationResult.class);
        verify(statementExecutor).execute(eq(request), classificationCaptor.capture(), eq(capability));
        assertThat(classificationCaptor.getValue().getStatementClass(), is(SupportedMCPStatement.EXPLAIN));
        assertThat(classificationCaptor.getValue().getReferencedObjects().stream().map(SQLStatementObjectName::getObjectName).toList(), contains("orders"));
        verifyNoInteractions(transactionExecutor);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertExecuteExplainWithSyntaxFailureCases")
    void assertExecuteExplainWithSyntaxFailure(final String name, final RuntimeException executionFailure) {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.EXPLAIN));
        SQLExecutionRequest request = createExecutionRequest("EXPLAIN SELECT * FROM orders");
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        when(statementExecutor.execute(eq(request), any(), eq(capability))).thenThrow(executionFailure);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, mock(MCPJdbcTransactionStatementExecutor.class), statementExecutor);
        ExplainSQLSyntaxException actual = assertThrows(ExplainSQLSyntaxException.class, () -> facade.executeExplain(request, "SELECT * FROM orders"));
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getSchema(), is("public"));
        assertThat(actual.getSql(), is("SELECT * FROM orders"));
        assertThat(actual.getExplainSql(), is("EXPLAIN SELECT * FROM orders"));
    }
    
    @Test
    void assertExecuteExplainWithNonSyntaxFailure() {
        MCPDatabaseCapabilityProvider capabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        MCPSessionExecutionCoordinator coordinator = mock(MCPSessionExecutionCoordinator.class);
        MCPDatabaseCapability capability = createCapability(Set.of(SupportedMCPStatement.EXPLAIN));
        SQLExecutionRequest request = createExecutionRequest("EXPLAIN SELECT * FROM missing_orders");
        mockSessionLock(coordinator);
        when(capabilityProvider.provide("logic_db")).thenReturn(Optional.of(capability));
        MCPJdbcStatementExecutor statementExecutor = mock(MCPJdbcStatementExecutor.class);
        MCPQueryFailedException executionFailure = new MCPQueryFailedException("missing table", new SQLException("missing table", "42P01"));
        when(statementExecutor.execute(eq(request), any(), eq(capability))).thenThrow(executionFailure);
        MCPSQLExecutionFacade facade = createFacade(capabilityProvider, coordinator, mock(MCPJdbcTransactionStatementExecutor.class), statementExecutor);
        assertThat(assertThrows(MCPQueryFailedException.class, () -> facade.executeExplain(request, "SELECT * FROM missing_orders")), is(executionFailure));
    }
    
    private MCPSQLExecutionFacade createFacade(final MCPDatabaseCapabilityProvider capabilityProvider, final MCPSessionExecutionCoordinator coordinator,
                                               final MCPJdbcTransactionStatementExecutor transactionExecutor, final MCPJdbcStatementExecutor statementExecutor) {
        return createFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, new MCPStatementAnalyzer());
    }
    
    private MCPSQLExecutionFacade createFacade(final MCPDatabaseCapabilityProvider capabilityProvider, final MCPSessionExecutionCoordinator coordinator,
                                               final MCPJdbcTransactionStatementExecutor transactionExecutor, final MCPJdbcStatementExecutor statementExecutor,
                                               final MCPStatementAnalyzer statementAnalyzer) {
        return new MCPSQLExecutionFacade(capabilityProvider, coordinator, transactionExecutor, statementExecutor, statementAnalyzer);
    }
    
    @SuppressWarnings("unchecked")
    private void mockSessionLock(final MCPSessionExecutionCoordinator coordinator) {
        when(coordinator.executeWithSessionLock(eq("session-1"), any())).thenAnswer(invocation -> ((Supplier<SQLExecutionResult>) invocation.getArgument(1, Supplier.class)).get());
    }
    
    private MCPDatabaseCapability createCapability(final Set<SupportedMCPStatement> supportedStatementClasses) {
        return createCapability(supportedStatementClasses, SchemaExecutionSemantics.FIXED_TO_DATABASE);
    }
    
    private MCPDatabaseCapability createCapability(final Set<SupportedMCPStatement> supportedStatementClasses, final SchemaExecutionSemantics schemaExecutionSemantics) {
        MCPDatabaseCapability result = mock(MCPDatabaseCapability.class);
        when(result.getSupportedStatementClasses()).thenReturn(supportedStatementClasses);
        when(result.getSchemaExecutionSemantics()).thenReturn(schemaExecutionSemantics);
        when(result.getIdentifierContext()).thenReturn(new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newInsensitivePolicySet()));
        when(result.getDatabaseType()).thenReturn("MySQL");
        return result;
    }
    
    private MCPStatementAnalyzer createMCPStatementAnalyzer(final ClassificationResult classificationResult) {
        MCPStatementAnalyzer result = mock(MCPStatementAnalyzer.class);
        when(result.analyze(anyString(), any())).thenReturn(classificationResult);
        return result;
    }
    
    private static Stream<Arguments> assertExecuteWithCrossSchemaReferencesDisabledCases() {
        return Stream.of(
                Arguments.of("query object list", "SELECT * FROM logic_db.orders, other_db.items", "other_db.items"),
                Arguments.of("cte object list", "WITH query_result AS (SELECT * FROM logic_db.orders, other_db.items) SELECT * FROM query_result", "other_db.items"),
                Arguments.of("unused cte reference", "WITH unused_result AS (SELECT * FROM other_db.items) SELECT * FROM logic_db.orders", "other_db.items"),
                Arguments.of("insert select object list", "INSERT INTO logic_db.orders_archive SELECT * FROM logic_db.orders, other_db.items", "other_db.items"),
                Arguments.of("update target object list", "UPDATE logic_db.orders o, other_db.items i SET o.status = 'DONE'", "other_db.items"),
                Arguments.of("delete target before from", "DELETE other_db.orders FROM logic_db.orders JOIN other_db.items ON 1 = 1", "other_db.orders"),
                Arguments.of("create view object list", "CREATE VIEW logic_db.active_orders AS SELECT * FROM logic_db.orders, other_db.items", "other_db.items"),
                Arguments.of("create table like", "CREATE TABLE logic_db.orders_archive LIKE other_db.orders", "other_db.orders"),
                Arguments.of("create table select without as", "CREATE TABLE logic_db.orders_archive SELECT * FROM other_db.orders", "other_db.orders"),
                Arguments.of("create table parenthesized select", "CREATE TABLE logic_db.orders_archive AS (SELECT * FROM other_db.orders)", "other_db.orders"),
                Arguments.of("create table table query", "CREATE TABLE logic_db.orders_archive AS TABLE other_db.orders", "other_db.orders"),
                Arguments.of("create table quoted table query", "CREATE TABLE logic_db.orders_archive AS TABLE `other_db`.`orders`", "other_db.orders"),
                Arguments.of("create table ANSI quoted table query", "CREATE TABLE logic_db.orders_archive AS TABLE \"other_db\".\"orders\"", "other_db.orders"),
                Arguments.of("create table foreign key reference", "CREATE TABLE logic_db.order_items (order_id INT REFERENCES other_db.orders(id))", "other_db.orders"),
                Arguments.of("alter table", "ALTER TABLE other_db.orders ADD COLUMN status VARCHAR(10)", "other_db.orders"),
                Arguments.of("alter table foreign key reference",
                        "ALTER TABLE logic_db.order_items ADD CONSTRAINT order_fk FOREIGN KEY (order_id) REFERENCES other_db.orders(id)", "other_db.orders"),
                Arguments.of("grant table", "GRANT SELECT ON other_db.orders TO PUBLIC", "other_db.orders"),
                Arguments.of("revoke table", "REVOKE SELECT ON other_db.orders FROM PUBLIC", "other_db.orders"),
                Arguments.of("grant database wildcard", "GRANT SELECT ON other_db.* TO PUBLIC", "other_db.*"),
                Arguments.of("grant global wildcard", "GRANT SELECT ON *.* TO PUBLIC", "*.*"),
                Arguments.of("qualified function", "SELECT other_db.foo_refresh_orders()", "other_db.foo_refresh_orders"),
                Arguments.of("create database target", "CREATE DATABASE other_db", "other_db"),
                Arguments.of("commented create database target", "/* guard */ CREATE DATABASE other_db", "other_db"),
                Arguments.of("create schema target", "CREATE SCHEMA other_db", "other_db"),
                Arguments.of("drop table object list", "DROP TABLE IF EXISTS logic_db.orders, other_db.items", "other_db.items"),
                Arguments.of("truncate table", "TRUNCATE TABLE other_db.items", "other_db.items"));
    }
    
    private static Stream<Arguments> assertExecuteWithNonMatchingCurrentDatabaseIdentifierCases() {
        return Stream.of(
                Arguments.of("case-sensitive unquoted identifier", "SELECT * FROM Logic_DB.orders", IdentifierCasePolicyFactory.newSensitivePolicySet()),
                Arguments.of("quoted identifier exact match", "SELECT * FROM \"Logic_DB\".orders", IdentifierCasePolicyFactory.newInsensitivePolicySet()));
    }
    
    private static Stream<Arguments> assertExecuteWithQualifiedCurrentDatabaseCases() {
        return Stream.of(
                Arguments.of("table", "SELECT * FROM logic_db.orders", SupportedMCPStatement.QUERY, "SELECT"),
                Arguments.of("function", "SELECT logic_db.foo_refresh_orders()", SupportedMCPStatement.QUERY, "SELECT"),
                Arguments.of("grant", "GRANT SELECT ON logic_db.orders TO PUBLIC", SupportedMCPStatement.DCL, "GRANT"),
                Arguments.of("create function", "CREATE FUNCTION logic_db.foo_function() RETURNS INT RETURN 1", SupportedMCPStatement.DDL, "CREATE"),
                Arguments.of("create procedure", "CREATE PROCEDURE logic_db.foo_procedure() SELECT 1", SupportedMCPStatement.DDL, "CREATE"),
                Arguments.of("create trigger", "CREATE TRIGGER logic_db.foo_trigger BEFORE INSERT ON logic_db.foo_table FOR EACH ROW SET @foo_value=1", SupportedMCPStatement.DDL, "CREATE"));
    }
    
    private static Stream<Arguments> assertExecuteExplainWithSyntaxFailureCases() {
        return Stream.of(
                Arguments.of("JDBC syntax exception", new MCPInvalidRequestException("bad explain", new SQLSyntaxErrorException("bad explain"))),
                Arguments.of("SQLState syntax exception", new MCPQueryFailedException("bad explain", new SQLException("bad explain", "42601"))),
                Arguments.of("MySQL vendor syntax exception", new MCPQueryFailedException("bad explain", new SQLException("bad explain", "42000", 1064))));
    }
    
    private SQLExecutionRequest createExecutionRequest(final String sql) {
        return new SQLExecutionRequest("session-1", "logic_db", "public", sql, 1, 1000);
    }
}
