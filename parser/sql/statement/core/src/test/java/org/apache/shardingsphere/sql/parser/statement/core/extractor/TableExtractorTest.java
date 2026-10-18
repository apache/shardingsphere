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

package org.apache.shardingsphere.sql.parser.statement.core.extractor;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.CombineType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.ValidStatementSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionWithParamsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.UnaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.merge.MergeWhenAndThenSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CommentStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.fixture.database.MockedDatabaseType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TableExtractorTest {
    
    private final TableExtractor tableExtractor = new TableExtractor();
    
    @Test
    void assertExtractUnaryExpressionTableOwner() {
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(30, 38, new IdentifierValue("foo_table")));
        ColumnSegment column = new ColumnSegment(14, 15, new IdentifierValue("id"));
        column.setOwner(new OwnerSegment(4, 12, new IdentifierValue("foo_table")));
        ProjectionsSegment projections = new ProjectionsSegment(0, 15);
        projections.getProjections().add(new ExpressionProjectionSegment(3, 15, "~foo_table.id", new UnaryOperationExpression(3, 15, column, "~", "~foo_table.id")));
        tableExtractor.extractTablesFromSelect(SelectStatement.builder().projections(projections).from(table).build());
        assertThat(tableExtractor.getRewriteTables().size(), is(2));
        Iterator<SimpleTableSegment> actualTables = tableExtractor.getRewriteTables().iterator();
        assertThat(actualTables.next(), is(table));
        SimpleTableSegment actualOwner = actualTables.next();
        assertThat(actualOwner.getStartIndex(), is(4));
        assertThat(actualOwner.getStopIndex(), is(12));
        assertThat(actualOwner.getTableName().getIdentifier().getValue(), is("foo_table"));
    }
    
    @Test
    void assertExtractUnaryExpressionTableAlias() {
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(30, 38, new IdentifierValue("foo_table")));
        table.setAlias(new AliasSegment(40, 40, new IdentifierValue("f")));
        ColumnSegment column = new ColumnSegment(6, 7, new IdentifierValue("id"));
        column.setOwner(new OwnerSegment(4, 4, new IdentifierValue("f")));
        ProjectionsSegment projections = new ProjectionsSegment(0, 7);
        projections.getProjections().add(new ExpressionProjectionSegment(3, 7, "~f.id", new UnaryOperationExpression(3, 7, column, "~", "~f.id")));
        tableExtractor.extractTablesFromSelect(SelectStatement.builder().projections(projections).from(table).build());
        assertThat(tableExtractor.getRewriteTables(), contains(table));
    }
    
    @Test
    void assertExtractNestedUnarySubqueryTables() {
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(30, 39, new IdentifierValue("foo_nested")));
        SelectStatement subquery = SelectStatement.builder().projections(new ProjectionsSegment(0, 0)).from(table).build();
        SubqueryExpressionSegment subqueryExpression = new SubqueryExpressionSegment(new SubquerySegment(5, 40, subquery, "SELECT id FROM foo_nested"));
        UnaryOperationExpression inner = new UnaryOperationExpression(4, 41, subqueryExpression, "~", "~(SELECT id FROM foo_nested)");
        UnaryOperationExpression expression = new UnaryOperationExpression(3, 41, inner, "~", "~~(SELECT id FROM foo_nested)");
        ProjectionsSegment projections = new ProjectionsSegment(0, 41);
        projections.getProjections().add(new ExpressionProjectionSegment(3, 41, expression.getText(), expression));
        tableExtractor.extractTablesFromSelect(SelectStatement.builder().projections(projections).build());
        assertThat(tableExtractor.getRewriteTables(), contains(table));
    }
    
    @Test
    void assertExtractCommonExpressionTableOrder() {
        SimpleTableSegment first = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_first")));
        SimpleTableSegment second = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_second")));
        CommonExpressionSegment indexed = new CommonExpressionSegment(0, 0, "ARRAY[(SELECT id FROM foo_first)][(SELECT id FROM foo_second)]",
                Arrays.asList(createSubqueryExpression(first), new CommonExpressionSegment(0, 0, "(SELECT id FROM foo_second)", Collections.singletonList(createSubqueryExpression(second)))));
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ExpressionProjectionSegment(0, 0, "", indexed));
        tableExtractor.extractTablesFromSelect(SelectStatement.builder().projections(projections).build());
        assertThat(tableExtractor.getRewriteTables(), contains(first, second));
    }
    
    private static SubqueryExpressionSegment createSubqueryExpression(final SimpleTableSegment table) {
        SelectStatement statement = SelectStatement.builder().projections(new ProjectionsSegment(0, 0)).from(table).build();
        return new SubqueryExpressionSegment(new SubquerySegment(0, 0, statement, ""));
    }
    
    @Test
    void assertExtractLegacyCommonExpressionTables() {
        ProjectionsSegment projections = new ProjectionsSegment(0, 10);
        projections.getProjections().add(new ExpressionProjectionSegment(0, 10, "legacy text", new CommonExpressionSegment(0, 10, "legacy text", Collections.emptyList())));
        tableExtractor.extractTablesFromSelect(SelectStatement.builder().projections(projections).build());
        assertTrue(tableExtractor.getRewriteTables().isEmpty());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDataModificationArguments")
    void assertExtractTablesFromDataModification(final String name, final DMLStatement body, final int expectedTableCount) {
        CommonTableExpressionSegment commonTable = new CommonTableExpressionSegment(0, 0, new AliasSegment(0, 0, new IdentifierValue("foo_changed")), null, body);
        WithSegment with = new WithSegment(0, 0, Collections.singletonList(commonTable));
        tableExtractor.extractTablesFromSelect(SelectStatement.builder().projections(new ProjectionsSegment(0, 0)).with(with).build());
        assertThat(tableExtractor.getRewriteTables().size(), is(expectedTableCount));
        assertThat(tableExtractor.getRewriteTables().iterator().next().getTableName().getIdentifier().getValue(), is("foo_target"));
    }
    
    private static Stream<Arguments> getDataModificationArguments() {
        SimpleTableSegment target = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_target")));
        SimpleTableSegment source = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_source")));
        SelectStatement select = SelectStatement.builder().projections(new ProjectionsSegment(0, 0)).from(source).build();
        return Stream.of(Arguments.of("insert", InsertStatement.builder().table(target).build(), 1),
                Arguments.of("insert select", InsertStatement.builder().table(target).insertSelect(new SubquerySegment(0, 0, select, "")).build(), 2),
                Arguments.of("update", UpdateStatement.builder().table(target).setAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList())).build(), 1),
                Arguments.of("update from", UpdateStatement.builder().table(target).from(source).setAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList())).build(), 2),
                Arguments.of("delete", DeleteStatement.builder().table(target).build(), 1),
                Arguments.of("merge", MergeStatement.builder().target(target).source(source).build(), 2));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getMergeArguments")
    void assertExtractTablesFromMerge(final String name, final MergeStatement mergeStatement, final Collection<SimpleTableSegment> expected) {
        tableExtractor.extractTablesFromMerge(mergeStatement);
        assertThat(tableExtractor.getRewriteTables().size(), is(expected.size()));
        Iterator<SimpleTableSegment> actual = tableExtractor.getRewriteTables().iterator();
        for (SimpleTableSegment each : expected) {
            assertTableSegment(actual.next(), each.getStartIndex(), each.getStopIndex(), each.getTableName().getIdentifier().getValue());
        }
    }
    
    private static Stream<Arguments> getMergeArguments() {
        SimpleTableSegment nested = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_nested")));
        SelectStatement nestedSelect = SelectStatement.builder().projections(new ProjectionsSegment(0, 0)).from(nested).build();
        ExistsSubqueryExpression expression = new ExistsSubqueryExpression(0, 0, new SubquerySegment(0, 0, nestedSelect, ""));
        SimpleTableSegment updateConditionTable = createScopeTable("foo_update_condition", 100);
        SimpleTableSegment firstUpdateTable = createScopeTable("foo_first_update", 150);
        SimpleTableSegment secondUpdateTable = createScopeTable("foo_second_update", 200);
        MergeWhenAndThenSegment update = new MergeWhenAndThenSegment(0, 0, "WHEN MATCHED AND EXISTS (...) THEN UPDATE SET id = (...), value = (...)");
        update.setAndExpr(new ExistsSubqueryExpression(0, 0, createSubqueryExpression(updateConditionTable).getSubquery()));
        update.setUpdate(UpdateStatement.builder().setAssignment(new SetAssignmentSegment(0, 0, Arrays.asList(
                new ColumnAssignmentSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("id"))), createSubqueryExpression(firstUpdateTable)),
                new ColumnAssignmentSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("value"))), createSubqueryExpression(secondUpdateTable))))).build());
        SimpleTableSegment insertConditionTable = createScopeTable("foo_insert_condition", 250);
        SimpleTableSegment firstInsertTable = createScopeTable("foo_first_insert", 300);
        SimpleTableSegment secondInsertTable = createScopeTable("foo_second_insert", 350);
        MergeWhenAndThenSegment insert = new MergeWhenAndThenSegment(0, 0, "WHEN NOT MATCHED AND EXISTS (...) THEN INSERT VALUES ((...), (...))");
        insert.setAndExpr(new ExistsSubqueryExpression(0, 0, createSubqueryExpression(insertConditionTable).getSubquery()));
        insert.setInsert(InsertStatement.builder().values(Collections.singletonList(new InsertValuesSegment(0, 0,
                Arrays.asList(createSubqueryExpression(firstInsertTable), createSubqueryExpression(secondInsertTable))))).build());
        SimpleTableSegment commonTableSource = createScopeTable("foo_common_source", 450);
        CommonTableExpressionSegment commonTable = new CommonTableExpressionSegment(0, 0, new AliasSegment(0, 0, new IdentifierValue("foo_source")),
                createSubqueryExpression(commonTableSource).getSubquery(), null);
        MergeWhenAndThenSegment delete = new MergeWhenAndThenSegment(0, 0, "WHEN MATCHED AND EXISTS (...) THEN DELETE");
        delete.setAndExpr(expression);
        delete.setDelete(DeleteStatement.builder().build());
        MergeWhenAndThenSegment doNothing = new MergeWhenAndThenSegment(0, 0, "WHEN NOT MATCHED THEN DO NOTHING");
        ExpressionWithParamsSegment onExpression = new ExpressionWithParamsSegment(0, 0, new LiteralExpressionSegment(0, 0, true));
        MergeWhenAndThenSegment defaultInsert = new MergeWhenAndThenSegment(0, 0, "WHEN NOT MATCHED THEN INSERT DEFAULT VALUES");
        defaultInsert.setInsert(InsertStatement.builder().build());
        SimpleTableSegment innerTarget = createScopeTable("foo_inner_target", 10);
        MergeStatement innerMerge = MergeStatement.builder().target(innerTarget).source(nested).expression(onExpression).whenAndThens(Collections.singletonList(doNothing)).build();
        CommonTableExpressionSegment mergingCommonTable = new CommonTableExpressionSegment(0, 0, new AliasSegment(0, 0, new IdentifierValue("foo_merged")), null, innerMerge);
        WithSegment mergingWith = new WithSegment(0, 0, Arrays.asList(mergingCommonTable, commonTable));
        ReturningSegment defaultReturning = new ReturningSegment(0, 0, createScopeProjections(
                new ColumnProjectionSegment(createScopeColumn("old", 70)), new ColumnProjectionSegment(createScopeColumn("new", 90))));
        ReturningSegment aliasedReturning = new ReturningSegment(0, 0, createScopeProjections(
                new ColumnProjectionSegment(createScopeColumn("old_row", 70)), new ColumnProjectionSegment(createScopeColumn("new_row", 90))));
        aliasedReturning.setOldAlias(new AliasSegment(0, 0, new IdentifierValue("old_row")));
        aliasedReturning.setNewAlias(new AliasSegment(0, 0, new IdentifierValue("new_row")));
        SimpleTableSegment shadowingTable = createScopeTable("old_row", 150);
        SelectStatement shadowingSelect = SelectStatement.builder().from(shadowingTable)
                .projections(createScopeProjections(new ColumnProjectionSegment(createScopeColumn("old_row", 180)))).build();
        ReturningSegment shadowingReturning = new ReturningSegment(0, 0, createScopeProjections(
                new ColumnProjectionSegment(createScopeColumn("old_row", 70)), new SubqueryProjectionSegment(new SubquerySegment(0, 0, shadowingSelect, ""), "")));
        shadowingReturning.setOldAlias(new AliasSegment(0, 0, new IdentifierValue("old_row")));
        SimpleTableSegment oldTable = createScopeTable("old", 10);
        SimpleTableSegment newTable = createScopeTable("new", 30);
        SelectStatement correlatedSelect = SelectStatement.builder().projections(createScopeProjections(new ColumnProjectionSegment(createScopeColumn("old", 150)))).build();
        SubqueryTableSegment derivedTable = new SubqueryTableSegment(0, 0, new SubquerySegment(0, 0, correlatedSelect, ""));
        derivedTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("x")));
        SelectStatement derivedSelect = SelectStatement.builder().from(derivedTable).projections(createScopeProjections(new ColumnProjectionSegment(createScopeColumn("x", 180)))).build();
        ReturningSegment derivedReturning = new ReturningSegment(0, 0,
                createScopeProjections(new SubqueryProjectionSegment(new SubquerySegment(0, 0, derivedSelect, ""), "")));
        SimpleTableSegment target = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_target")));
        SimpleTableSegment source = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_source")));
        MergeStatement mergeStatement = MergeStatement.builder().target(target).source(source).expression(new ExpressionWithParamsSegment(0, 0, expression)).build();
        SimpleTableSegment left = createScopeTable("foo_left", 40);
        SimpleTableSegment right = createScopeTable("bar_right", 60);
        MergeStatement usingAliasMerge = MergeStatement.builder().databaseType(new MockedDatabaseType()).target(target).source(createUsingAliasJoin(left, right))
                .expression(new ExpressionWithParamsSegment(20, 32, createScopeColumn("foo_outer", 20))).build();
        DatabaseType databaseType = new MockedDatabaseType();
        SimpleTableSegment returningTable = createScopeTable("foo_returning", 400);
        ReturningSegment returning = new ReturningSegment(0, 0, createScopeProjections(new SubqueryProjectionSegment(createSubqueryExpression(returningTable).getSubquery(), "")));
        MergeStatement allClauses = MergeStatement.builder().databaseType(databaseType).target(target).source(source).expression(new ExpressionWithParamsSegment(0, 0, expression))
                .whenAndThens(Arrays.asList(update, insert)).returning(returning).with(new WithSegment(0, 0, Collections.singletonList(commonTable))).build();
        ReturningSegment usingAliasReturning = new ReturningSegment(0, 0, createScopeProjections(
                new ColumnProjectionSegment(createScopeColumn("foo_outer", 70)), new ColumnProjectionSegment(createScopeColumn("old", 90))));
        return Stream.of(
                Arguments.of("ON subquery", mergeStatement, Arrays.asList(target, source, nested)),
                Arguments.of("Source USING alias", usingAliasMerge, Arrays.asList(target, left, right)),
                Arguments.of("Without ON expression", MergeStatement.builder().target(target).source(source).build(), Arrays.asList(target, source)),
                Arguments.of("WHEN actions and traversal order", allClauses,
                        Arrays.asList(target, source, nested, updateConditionTable, firstUpdateTable, secondUpdateTable, insertConditionTable, firstInsertTable, secondInsertTable,
                                returningTable, commonTableSource)),
                Arguments.of("DELETE and DO NOTHING", MergeStatement.builder().target(target).source(source).expression(onExpression).whenAndThens(Arrays.asList(delete, doNothing)).build(),
                        Arrays.asList(target, source, nested)),
                Arguments.of("INSERT DEFAULT VALUES", MergeStatement.builder().target(target).source(source).expression(onExpression).whenAndThens(Collections.singletonList(defaultInsert)).build(),
                        Arrays.asList(target, source)),
                Arguments.of("MERGE and SELECT CTE bodies", MergeStatement.builder().target(target).source(source).expression(onExpression).whenAndThens(Collections.singletonList(doNothing))
                        .with(mergingWith).build(), Arrays.asList(target, source, innerTarget, nested, commonTableSource)),
                Arguments.of("Default RETURNING row aliases", MergeStatement.builder().databaseType(databaseType).target(target).source(source).returning(defaultReturning).build(),
                        Arrays.asList(target, source)),
                Arguments.of("Explicit RETURNING row aliases", MergeStatement.builder().databaseType(databaseType).target(target).source(source).returning(aliasedReturning).build(),
                        Arrays.asList(target, source)),
                Arguments.of("Local table shadows RETURNING row alias", MergeStatement.builder().databaseType(databaseType).target(target).source(source).returning(shadowingReturning).build(),
                        Arrays.asList(target, source, shadowingTable, createScopeTable("old_row", 180))),
                Arguments.of("Physical tables mask default RETURNING aliases",
                        MergeStatement.builder().databaseType(databaseType).target(oldTable).source(newTable).returning(defaultReturning).build(),
                        Arrays.asList(oldTable, newTable, createScopeTable("old", 70), createScopeTable("new", 90))),
                Arguments.of("Derived query inherits RETURNING aliases", MergeStatement.builder().databaseType(databaseType).target(target).source(source).returning(derivedReturning).build(),
                        Arrays.asList(target, source)),
                Arguments.of("RETURNING respects source USING alias",
                        MergeStatement.builder().databaseType(databaseType).target(target).source(createUsingAliasJoin(left, right)).returning(usingAliasReturning).build(),
                        Arrays.asList(target, left, right)));
    }
    
    @Test
    void assertExtractTablesFromSelectProjects() {
        AggregationProjectionSegment aggregationProjection = new AggregationProjectionSegment(10, 20, AggregationType.SUM, "SUM(t_order.id)");
        ColumnSegment columnSegment = new ColumnSegment(133, 136, new IdentifierValue("id"));
        columnSegment.setOwner(new OwnerSegment(130, 132, new IdentifierValue("t_order")));
        aggregationProjection.getParameters().add(columnSegment);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(10, 20);
        projectionsSegment.getProjections().add(aggregationProjection);
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        tableExtractor.extractTablesFromSelect(selectStatement);
        assertThat(tableExtractor.getRewriteTables().size(), is(1));
        Iterator<SimpleTableSegment> tableSegmentIterator = tableExtractor.getRewriteTables().iterator();
        assertTableSegment(tableSegmentIterator.next(), 130, 132, "t_order");
    }
    
    @Test
    void assertExtractTablesFromSelectProjectsWithFunctionWithSubQuery() {
        FunctionSegment functionSegment = new FunctionSegment(0, 0, "", "");
        SelectStatement subQuerySegment = mock(SelectStatement.class);
        when(subQuerySegment.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")))));
        SubquerySegment subquerySegment = new SubquerySegment(0, 0, subQuerySegment, "");
        SubqueryExpressionSegment subqueryExpressionSegment = new SubqueryExpressionSegment(subquerySegment);
        functionSegment.getParameters().add(subqueryExpressionSegment);
        ExpressionProjectionSegment expressionProjectionSegment = new ExpressionProjectionSegment(0, 0, "", functionSegment);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(expressionProjectionSegment);
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getProjections()).thenReturn(projectionsSegment);
        tableExtractor.extractTablesFromSelect(selectStatement);
        assertThat(tableExtractor.getRewriteTables().size(), is(1));
    }
    
    @Test
    void assertExtractTablesFromSelectLockWithValue() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        LockSegment lockSegment = new LockSegment(108, 154);
        when(selectStatement.getLock()).thenReturn(Optional.of(lockSegment));
        lockSegment.getTables().add(new SimpleTableSegment(new TableNameSegment(122, 128, new IdentifierValue("t_order"))));
        lockSegment.getTables().add(new SimpleTableSegment(new TableNameSegment(143, 154, new IdentifierValue("t_order_item"))));
        tableExtractor.extractTablesFromSelect(selectStatement);
        assertThat(tableExtractor.getRewriteTables().size(), is(2));
        Iterator<SimpleTableSegment> tableSegmentIterator = tableExtractor.getRewriteTables().iterator();
        assertTableSegment(tableSegmentIterator.next(), 122, 128, "t_order");
        assertTableSegment(tableSegmentIterator.next(), 143, 154, "t_order_item");
    }
    
    @Test
    void assertExtractTablesFromInsert() {
        InsertStatement insertStatement = mock(InsertStatement.class);
        when(insertStatement.getTable()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(122, 128, new IdentifierValue("t_order")))));
        Collection<ColumnAssignmentSegment> assignmentSegments = new LinkedList<>();
        ColumnSegment columnSegment = new ColumnSegment(133, 136, new IdentifierValue("id"));
        columnSegment.setOwner(new OwnerSegment(130, 132, new IdentifierValue("t_order")));
        assignmentSegments.add(new ColumnAssignmentSegment(130, 140, Collections.singletonList(columnSegment), new LiteralExpressionSegment(141, 142, 1)));
        when(insertStatement.getOnDuplicateKeyColumns()).thenReturn(Optional.of(new OnDuplicateKeyColumnsSegment(130, 140, assignmentSegments)));
        tableExtractor.extractTablesFromInsert(insertStatement);
        assertThat(tableExtractor.getRewriteTables().size(), is(2));
        Iterator<SimpleTableSegment> tableSegmentIterator = tableExtractor.getRewriteTables().iterator();
        assertTableSegment(tableSegmentIterator.next(), 122, 128, "t_order");
        assertTableSegment(tableSegmentIterator.next(), 130, 132, "t_order");
    }
    
    @Test
    void assertNotExistTableFromRoutineBody() {
        RoutineBodySegment routineBodySegment = new RoutineBodySegment(0, 3);
        ValidStatementSegment validStatement = new ValidStatementSegment(0, 1);
        validStatement.setSqlStatement(mock(SQLStatement.class));
        routineBodySegment.getValidStatements().add(validStatement);
        ValidStatementSegment newValidStatement = new ValidStatementSegment(0, 1);
        validStatement.setSqlStatement(mock(CreateTableStatement.class));
        routineBodySegment.getValidStatements().add(newValidStatement);
        Collection<SimpleTableSegment> nonExistingTables = tableExtractor.extractNotExistTableFromRoutineBody(routineBodySegment);
        assertThat(nonExistingTables.size(), is(1));
    }
    
    private void assertTableSegment(final SimpleTableSegment actual, final int expectedStartIndex, final int expectedStopIndex, final String expectedTableName) {
        assertThat(actual.getStartIndex(), is(expectedStartIndex));
        assertThat(actual.getStopIndex(), is(expectedStopIndex));
        Optional<String> actualTableName = Optional.ofNullable(actual.getTableName()).map(TableNameSegment::getIdentifier).map(IdentifierValue::getValue);
        assertTrue(actualTableName.isPresent());
        assertThat(actualTableName.get(), is(expectedTableName));
    }
    
    @Test
    void assertExtractTablesFromCombineSegment() {
        SelectStatement selectStatement = createSelectStatement("t_order");
        SubquerySegment left = new SubquerySegment(0, 0, createSelectStatement("t_order"), "");
        SubquerySegment right = new SubquerySegment(0, 0, createSelectStatement("t_order_item"), "");
        when(selectStatement.getCombine()).thenReturn(Optional.of(new CombineSegment(0, 0, left, CombineType.UNION, right)));
        tableExtractor.extractTablesFromSelect(selectStatement);
        Collection<SimpleTableSegment> actual = tableExtractor.getRewriteTables();
        assertThat(actual.size(), is(2));
        Iterator<SimpleTableSegment> iterator = actual.iterator();
        assertTableSegment(iterator.next(), 0, 0, "t_order");
        assertTableSegment(iterator.next(), 0, 0, "t_order_item");
    }
    
    private SelectStatement createSelectStatement(final String tableName) {
        SelectStatement result = mock(SelectStatement.class);
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ShorthandProjectionSegment(0, 0));
        when(result.getProjections()).thenReturn(projections);
        when(result.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(tableName)))));
        return result;
    }
    
    @Test
    void assertExtractTablesFromCombineSegmentWithColumnProjection() {
        SelectStatement selectStatement = createSelectStatementWithColumnProjection("t_order");
        SubquerySegment left = new SubquerySegment(0, 0, createSelectStatementWithColumnProjection("t_order"), "");
        SubquerySegment right = new SubquerySegment(0, 0, createSelectStatementWithColumnProjection("t_order_item"), "");
        when(selectStatement.getCombine()).thenReturn(Optional.of(new CombineSegment(0, 0, left, CombineType.UNION, right)));
        tableExtractor.extractTablesFromSelect(selectStatement);
        Collection<SimpleTableSegment> actual = tableExtractor.getRewriteTables();
        assertThat(actual.size(), is(2));
        Iterator<SimpleTableSegment> iterator = actual.iterator();
        assertTableSegment(iterator.next(), 0, 0, "t_order");
        assertTableSegment(iterator.next(), 0, 0, "t_order_item");
    }
    
    private SelectStatement createSelectStatementWithColumnProjection(final String tableName) {
        SelectStatement result = mock(SelectStatement.class);
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("id"));
        columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("a")));
        projections.getProjections().add(new ColumnProjectionSegment(columnSegment));
        when(result.getProjections()).thenReturn(projections);
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(tableName)));
        tableSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("a")));
        when(result.getFrom()).thenReturn(Optional.of(tableSegment));
        return result;
    }
    
    @Test
    void assertExtractTablesFromCombineWithSubQueryProjection() {
        SelectStatement selectStatement = createSelectStatementWithSubQueryProjection("t_order");
        SubquerySegment left = new SubquerySegment(0, 0, createSelectStatementWithSubQueryProjection("t_order"), "");
        SubquerySegment right = new SubquerySegment(0, 0, createSelectStatementWithSubQueryProjection("t_order_item"), "");
        when(selectStatement.getCombine()).thenReturn(Optional.of(new CombineSegment(0, 0, left, CombineType.UNION, right)));
        tableExtractor.extractTablesFromSelect(selectStatement);
        Collection<SimpleTableSegment> actual = tableExtractor.getRewriteTables();
        assertThat(actual.size(), is(2));
        Iterator<SimpleTableSegment> iterator = actual.iterator();
        assertTableSegment(iterator.next(), 0, 0, "t_order");
        assertTableSegment(iterator.next(), 0, 0, "t_order_item");
    }
    
    private SelectStatement createSelectStatementWithSubQueryProjection(final String tableName) {
        SelectStatement result = mock(SelectStatement.class);
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new SubqueryProjectionSegment(new SubquerySegment(0, 0, createSelectStatement(tableName), ""), ""));
        when(result.getProjections()).thenReturn(projections);
        return result;
    }
    
    @Test
    void assertExtractJoinTableSegmentsFromSelect() {
        JoinTableSegment joinTableSegment = new JoinTableSegment();
        joinTableSegment.setLeft(new SimpleTableSegment(new TableNameSegment(16, 22, new IdentifierValue("t_order"))));
        joinTableSegment.setRight(new SimpleTableSegment(new TableNameSegment(37, 48, new IdentifierValue("t_order_item"))));
        joinTableSegment.setJoinType("INNER");
        joinTableSegment.setCondition(new BinaryOperationExpression(56, 79, new ColumnSegment(56, 65, new IdentifierValue("order_id")),
                new ColumnSegment(69, 79, new IdentifierValue("order_id")), "=", "oi.order_id = o.order_id"));
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.of(joinTableSegment));
        tableExtractor.extractTablesFromSelect(selectStatement);
        assertThat(tableExtractor.getJoinTables().size(), is(1));
        assertThat(tableExtractor.getJoinTables().iterator().next(), is(joinTableSegment));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getUsingAliasArguments")
    void assertExtractTablesFromSelectUsingAlias(final String name, final String owner) {
        SimpleTableSegment left = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_first")));
        left.setAlias(new AliasSegment(0, 0, new IdentifierValue("f")));
        SimpleTableSegment right = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_second")));
        right.setAlias(new AliasSegment(0, 0, new IdentifierValue("b")));
        JoinTableSegment joinTable = new JoinTableSegment();
        joinTable.setLeft(left);
        joinTable.setRight(right);
        joinTable.setUsing(Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("id"))));
        joinTable.setUsingAlias(new AliasSegment(0, 0, new IdentifierValue("joined")));
        ColumnSegment column = new ColumnSegment(0, 0, new IdentifierValue("id"));
        column.setOwner(new OwnerSegment(0, 0, new IdentifierValue(owner)));
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ColumnProjectionSegment(column));
        tableExtractor.extractTablesFromSelect(SelectStatement.builder().databaseType(new MockedDatabaseType()).projections(projections).from(joinTable).build());
        assertThat(tableExtractor.getRewriteTables(), contains(left, right));
    }
    
    private static Stream<Arguments> getUsingAliasArguments() {
        return Stream.of(Arguments.of("USING alias", "joined"), Arguments.of("Left table alias", "f"), Arguments.of("Right table alias", "b"));
    }
    
    @Test
    void assertExtractTablesFromSelectWithCorrelatedDerivedUsingAlias() {
        SimpleTableSegment left = createScopeTable("foo_left", 20);
        SimpleTableSegment right = createScopeTable("bar_right", 40);
        SelectStatement correlated = SelectStatement.builder().projections(createScopeProjections(new ColumnProjectionSegment(createScopeColumn("foo_outer", 80)))).build();
        SubqueryTableSegment derived = new SubqueryTableSegment(70, 110, new SubquerySegment(70, 110, correlated, ""));
        derived.setAlias(new AliasSegment(115, 115, new IdentifierValue("x")));
        JoinTableSegment from = new JoinTableSegment();
        from.setLeft(createUsingAliasJoin(left, right));
        from.setRight(derived);
        tableExtractor.extractTablesFromSelect(SelectStatement.builder().databaseType(new MockedDatabaseType()).from(from).projections(new ProjectionsSegment(0, 0)).build());
        assertThat(tableExtractor.getRewriteTables(), contains(left, right));
    }
    
    @Test
    void assertExtractTablesFromSelectWithNestedUsingAlias() {
        JoinTableSegment innerJoin = new JoinTableSegment();
        innerJoin.setLeft(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_inner_left"))));
        innerJoin.setRight(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_inner_right"))));
        innerJoin.setUsing(Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("id"))));
        innerJoin.setUsingAlias(new AliasSegment(0, 0, new IdentifierValue("foo_outer")));
        SelectStatement innerSelect = SelectStatement.builder().databaseType(new MockedDatabaseType()).projections(new ProjectionsSegment(0, 0)).from(innerJoin).build();
        SubqueryTableSegment subqueryTable = new SubqueryTableSegment(0, 0, new SubquerySegment(0, 0, innerSelect, ""));
        subqueryTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("foo_nested")));
        JoinTableSegment outerJoin = new JoinTableSegment();
        outerJoin.setLeft(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_outer"))));
        outerJoin.setRight(subqueryTable);
        ColumnSegment column = new ColumnSegment(0, 0, new IdentifierValue("id"));
        column.setOwner(new OwnerSegment(0, 0, new IdentifierValue("foo_outer")));
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ColumnProjectionSegment(column));
        tableExtractor.extractTablesFromSelect(SelectStatement.builder().projections(projections).from(outerJoin).build());
        Collection<String> actualTableNames = tableExtractor.getRewriteTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList());
        assertThat(actualTableNames, contains("foo_outer", "foo_inner_left", "foo_inner_right", "foo_outer"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getTableScopeArguments")
    void assertExtractTablesFromSelectScope(final String name, final SelectStatement selectStatement, final Collection<TableSegment> inheritedTables, final Collection<String> expectedRewriteTables,
                                            final Collection<TableSegment> expectedTableContext, final Collection<JoinTableSegment> expectedJoinTables) {
        tableExtractor.getTableContext().addAll(inheritedTables);
        tableExtractor.extractTablesFromSelect(selectStatement);
        Collection<String> actualRewriteTables = tableExtractor.getRewriteTables().stream()
                .map(each -> each.getTableName().getIdentifier().getValue() + "@" + each.getStartIndex() + ":" + each.getStopIndex()).collect(Collectors.toList());
        assertThat(actualRewriteTables, is(expectedRewriteTables));
        assertThat(tableExtractor.getTableContext(), is(expectedTableContext));
        assertThat(tableExtractor.getJoinTables(), is(expectedJoinTables));
    }
    
    private static Stream<Arguments> getTableScopeArguments() {
        SimpleTableSegment parentLeft = createScopeTable("foo_parent", 150);
        SimpleTableSegment parentRight = createScopeTable("bar_parent", 170);
        JoinTableSegment parentJoin = createUsingAliasJoin(parentLeft, parentRight);
        ProjectionsSegment childProjections = createScopeProjections(new ColumnProjectionSegment(createScopeColumn("foo_outer", 120)));
        SimpleTableSegment tailTable = createScopeTable("foo_tail", 200);
        JoinTableSegment leftUsingJoin = new JoinTableSegment();
        leftUsingJoin.setLeft(parentJoin);
        leftUsingJoin.setRight(tailTable);
        JoinTableSegment rightUsingJoin = new JoinTableSegment();
        rightUsingJoin.setLeft(tailTable);
        rightUsingJoin.setRight(parentJoin);
        SelectStatement leftUsingSelect = SelectStatement.builder().databaseType(new MockedDatabaseType()).projections(childProjections).from(leftUsingJoin).build();
        SelectStatement rightUsingSelect = SelectStatement.builder().databaseType(new MockedDatabaseType()).projections(childProjections).from(rightUsingJoin).build();
        SelectStatement correlated = SelectStatement.builder().projections(childProjections).build();
        SimpleTableSegment localPhysical = createScopeTable("foo_outer", 100);
        SelectStatement physicalShadow = SelectStatement.builder().projections(childProjections).from(localPhysical).build();
        SimpleTableSegment localAlias = createScopeTable("foo_local", 80);
        localAlias.setAlias(new AliasSegment(90, 98, new IdentifierValue("foo_outer")));
        SelectStatement aliasShadow = SelectStatement.builder().projections(childProjections).from(localAlias).build();
        FunctionTableSegment localFunction = new FunctionTableSegment(80, 95, new FunctionSegment(80, 95, "foo_function", "foo_function()"));
        localFunction.setAlias(new AliasSegment(100, 108, new IdentifierValue("foo_outer")));
        SelectStatement functionShadow = SelectStatement.builder().projections(childProjections).from(localFunction).build();
        SelectStatement derivedSelect = SelectStatement.builder().projections(createScopeProjections(new ShorthandProjectionSegment(0, 0))).from(localPhysical).build();
        SubqueryTableSegment localSubquery = new SubqueryTableSegment(80, 115, new SubquerySegment(80, 115, derivedSelect, ""));
        localSubquery.setAlias(new AliasSegment(116, 124, new IdentifierValue("foo_outer")));
        SelectStatement subqueryShadow = SelectStatement.builder().projections(childProjections).from(localSubquery).build();
        SubquerySegment grandchild = new SubquerySegment(100, 115, derivedSelect, "");
        WhereSegment grandchildExists = new WhereSegment(95, 115, new ExistsSubqueryExpression(95, 115, grandchild));
        SelectStatement grandchildMetadata = SelectStatement.builder().projections(childProjections).where(grandchildExists).build();
        SimpleTableSegment middlePhysical = createScopeTable("foo_outer", 80);
        SelectStatement grandchildWithLocal = SelectStatement.builder().projections(childProjections).from(middlePhysical).where(grandchildExists).build();
        SimpleTableSegment innerLeft = createScopeTable("foo_left", 40);
        SimpleTableSegment innerRight = createScopeTable("bar_right", 60);
        JoinTableSegment innerJoin = createUsingAliasJoin(innerLeft, innerRight);
        SelectStatement innerSelect = SelectStatement.builder().databaseType(new MockedDatabaseType())
                .projections(createScopeProjections(new ExpressionProjectionSegment(0, 0, "true", new LiteralExpressionSegment(0, 0, true))))
                .from(innerJoin).build();
        SubquerySegment innerSubquery = new SubquerySegment(30, 90, innerSelect, "");
        SelectStatement combine = SelectStatement.builder().combine(new CombineSegment(0, 130, innerSubquery, CombineType.UNION, new SubquerySegment(95, 130, physicalShadow, ""))).build();
        CommonTableExpressionSegment firstCTE = new CommonTableExpressionSegment(0, 90, new AliasSegment(0, 0, new IdentifierValue("foo_first")), innerSubquery, null);
        CommonTableExpressionSegment secondCTE = new CommonTableExpressionSegment(95, 130, new AliasSegment(95, 95, new IdentifierValue("bar_second")),
                new SubquerySegment(95, 130, physicalShadow, ""), null);
        SelectStatement commonTables = SelectStatement.builder().projections(new ProjectionsSegment(0, 0)).with(new WithSegment(0, 130, Arrays.asList(firstCTE, secondCTE), false)).build();
        SimpleTableSegment updateTarget = createScopeTable("foo_target", 5);
        SetAssignmentSegment assignments = new SetAssignmentSegment(0, 0, Collections.singletonList(new ColumnAssignmentSegment(0, 0,
                Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("id"))), new LiteralExpressionSegment(0, 0, 1))));
        UpdateStatement firstUpdate = UpdateStatement.builder().databaseType(new MockedDatabaseType()).table(updateTarget).from(innerJoin).setAssignment(assignments).build();
        UpdateStatement secondUpdate = UpdateStatement.builder().table(localPhysical).setAssignment(assignments)
                .where(new WhereSegment(120, 134, new BinaryOperationExpression(120, 134, createScopeColumn("foo_outer", 120), new LiteralExpressionSegment(134, 134, 1), "=", ""))).build();
        CommonTableExpressionSegment firstUpdateCTE = new CommonTableExpressionSegment(0, 90, new AliasSegment(0, 0, new IdentifierValue("foo_updated")), null, firstUpdate);
        CommonTableExpressionSegment secondUpdateCTE = new CommonTableExpressionSegment(95, 134, new AliasSegment(95, 95, new IdentifierValue("bar_updated")), null, secondUpdate);
        SelectStatement modifyingCommonTables = SelectStatement.builder().projections(new ProjectionsSegment(0, 0))
                .with(new WithSegment(0, 134, Arrays.asList(firstUpdateCTE, secondUpdateCTE), false)).build();
        SelectStatement scopedModifyingCommonTable = SelectStatement.builder().databaseType(new MockedDatabaseType()).projections(new ProjectionsSegment(0, 0)).from(parentJoin)
                .with(new WithSegment(95, 134, Collections.singletonList(secondUpdateCTE), false)).build();
        InsertStatement firstInsert = InsertStatement.builder().databaseType(new MockedDatabaseType()).table(updateTarget).insertSelect(innerSubquery).build();
        CommonTableExpressionSegment firstInsertCTE = new CommonTableExpressionSegment(0, 90, new AliasSegment(0, 0, new IdentifierValue("foo_inserted")), null, firstInsert);
        SelectStatement insertingCommonTables = SelectStatement.builder().projections(new ProjectionsSegment(0, 0))
                .with(new WithSegment(0, 134, Arrays.asList(firstInsertCTE, secondUpdateCTE), false)).build();
        SimpleTableSegment inheritedAlias = createScopeTable("foo_inherited", 200);
        inheritedAlias.setAlias(new AliasSegment(214, 214, new IdentifierValue("f")));
        ProjectionsSegment inheritedProjections = createScopeProjections(new ColumnProjectionSegment(createScopeColumn("f", 20)));
        SelectStatement inheritedUsingSelect = SelectStatement.builder().databaseType(new MockedDatabaseType()).projections(inheritedProjections).from(innerJoin).build();
        SimpleTableSegment inheritedPhysical = createScopeTable("f", 150);
        SimpleTableSegment descendantAlias = createScopeTable("foo_shadow", 100);
        descendantAlias.setAlias(new AliasSegment(114, 114, new IdentifierValue("f")));
        SelectStatement descendantSelect = SelectStatement.builder().projections(createScopeProjections(new ShorthandProjectionSegment(0, 0))).from(descendantAlias).build();
        SelectStatement inheritedPhysicalSelect = SelectStatement.builder().databaseType(new MockedDatabaseType()).projections(inheritedProjections).from(innerJoin)
                .where(new WhereSegment(95, 115, new ExistsSubqueryExpression(95, 115, new SubquerySegment(100, 115, descendantSelect, "")))).build();
        SimpleTableSegment differentlyAliased = createScopeTable("foo_outer", 100);
        differentlyAliased.setAlias(new AliasSegment(112, 122, new IdentifierValue("local_alias")));
        SelectStatement hiddenPhysical = SelectStatement.builder().projections(childProjections).from(differentlyAliased).build();
        SelectStatement unresolved = SelectStatement.builder().projections(createScopeProjections(new SubqueryProjectionSegment(new SubquerySegment(0, 130, correlated, ""), ""))).build();
        Collection<TableSegment> parentContext = Arrays.asList(parentJoin, parentLeft, parentRight);
        Collection<String> parentRewrites = Arrays.asList("foo_parent@150:159", "bar_parent@170:179");
        Collection<JoinTableSegment> parentJoins = Collections.singletonList(parentJoin);
        Collection<String> outwardRewrites = Arrays.asList("foo_outer@10:18", "foo_left@40:47", "bar_right@60:68", "foo_outer@20:28");
        SimpleTableSegment outerTable = createScopeTable("foo_outer", 10);
        Collection<TableSegment> outwardContext = Arrays.asList(outerTable, innerLeft, innerRight);
        Collection<JoinTableSegment> innerJoins = Collections.singletonList(innerJoin);
        ColumnSegment outerColumn = createScopeColumn("foo_outer", 20);
        ProjectionsSegment outerProjections = createScopeProjections(new ColumnProjectionSegment(outerColumn));
        SelectStatement exists = SelectStatement.builder().projections(outerProjections).from(outerTable).where(new WhereSegment(30, 90, new ExistsSubqueryExpression(30, 90, innerSubquery))).build();
        SelectStatement scalar = SelectStatement.builder().projections(outerProjections).from(outerTable).where(new WhereSegment(30, 90, new SubqueryExpressionSegment(innerSubquery))).build();
        SelectStatement rawSubquery = SelectStatement.builder().projections(outerProjections).from(outerTable).where(new WhereSegment(30, 90, innerSubquery)).build();
        SelectStatement projection = SelectStatement.builder().projections(createScopeProjections(new SubqueryProjectionSegment(innerSubquery, ""), new ColumnProjectionSegment(outerColumn)))
                .from(outerTable).build();
        return Stream.of(
                Arguments.of("EXISTS alias isolation", exists, Collections.emptyList(), outwardRewrites, outwardContext, innerJoins),
                Arguments.of("Scalar alias isolation", scalar, Collections.emptyList(), outwardRewrites, outwardContext, innerJoins),
                Arguments.of("Subquery node alias isolation", rawSubquery, Collections.emptyList(), outwardRewrites, outwardContext, innerJoins),
                Arguments.of("Projection alias isolation", projection, Collections.emptyList(), outwardRewrites, outwardContext, innerJoins),
                Arguments.of("Left join subtree USING alias", leftUsingSelect, Collections.emptyList(), Arrays.asList("foo_parent@150:159", "bar_parent@170:179", "foo_tail@200:207"),
                        Arrays.asList(parentJoin, parentLeft, parentRight, tailTable), Arrays.asList(leftUsingJoin, parentJoin)),
                Arguments.of("Right join subtree USING alias", rightUsingSelect, Collections.emptyList(), Arrays.asList("foo_tail@200:207", "foo_parent@150:159", "bar_parent@170:179"),
                        Arrays.asList(tailTable, parentJoin, parentLeft, parentRight), Arrays.asList(rightUsingJoin, parentJoin)),
                Arguments.of("Correlated USING alias", createScopeOuterSelect(parentJoin, correlated), Collections.emptyList(), parentRewrites, parentContext, parentJoins),
                Arguments.of("Correlated table alias", createScopeOuterSelect(localAlias, correlated), Collections.emptyList(), Collections.singletonList("foo_local@80:98"),
                        Collections.singletonList(localAlias), Collections.emptyList()),
                Arguments.of("Local physical shadow", createScopeOuterSelect(parentJoin, physicalShadow), Collections.emptyList(),
                        Arrays.asList("foo_parent@150:159", "bar_parent@170:179", "foo_outer@100:108", "foo_outer@120:128"),
                        Arrays.asList(parentJoin, parentLeft, parentRight, localPhysical), parentJoins),
                Arguments.of("Local table alias", createScopeOuterSelect(parentJoin, aliasShadow), Collections.emptyList(),
                        Arrays.asList("foo_parent@150:159", "bar_parent@170:179", "foo_local@80:98"), Arrays.asList(parentJoin, parentLeft, parentRight, localAlias), parentJoins),
                Arguments.of("Local function alias", createScopeOuterSelect(parentJoin, functionShadow), Collections.emptyList(), parentRewrites,
                        Arrays.asList(parentJoin, parentLeft, parentRight, localFunction), parentJoins),
                Arguments.of("Local derived alias", createScopeOuterSelect(parentJoin, subqueryShadow), Collections.emptyList(),
                        Arrays.asList("foo_parent@150:159", "bar_parent@170:179", "foo_outer@100:108"), Arrays.asList(parentJoin, parentLeft, parentRight, localSubquery), parentJoins),
                Arguments.of("Grandchild metadata is not local", createScopeOuterSelect(parentJoin, grandchildMetadata), Collections.emptyList(),
                        Arrays.asList("foo_parent@150:159", "bar_parent@170:179", "foo_outer@100:108"), Arrays.asList(parentJoin, parentLeft, parentRight, localPhysical), parentJoins),
                Arguments.of("Local physical shadows with grandchild", createScopeOuterSelect(parentJoin, grandchildWithLocal), Collections.emptyList(),
                        Arrays.asList("foo_parent@150:159", "bar_parent@170:179", "foo_outer@80:88", "foo_outer@100:108", "foo_outer@120:128"),
                        Arrays.asList(parentJoin, parentLeft, parentRight, middlePhysical, localPhysical), parentJoins),
                Arguments.of("Combine sibling isolation", combine, Collections.emptyList(), Arrays.asList("foo_left@40:47", "bar_right@60:68", "foo_outer@100:108", "foo_outer@120:128"),
                        Arrays.asList(innerLeft, innerRight, localPhysical), innerJoins),
                Arguments.of("CTE sibling isolation", commonTables, Collections.emptyList(), Arrays.asList("foo_left@40:47", "bar_right@60:68", "foo_outer@100:108", "foo_outer@120:128"),
                        Arrays.asList(innerLeft, innerRight, localPhysical), innerJoins),
                Arguments.of("DML CTE sibling isolation", modifyingCommonTables, Collections.emptyList(),
                        Arrays.asList("foo_target@5:14", "foo_left@40:47", "bar_right@60:68", "foo_outer@100:108", "foo_outer@120:128"),
                        Arrays.asList(updateTarget, innerLeft, innerRight, localPhysical), innerJoins),
                Arguments.of("DML CTE below USING source", scopedModifyingCommonTable, Collections.emptyList(),
                        Arrays.asList("foo_parent@150:159", "bar_parent@170:179", "foo_outer@100:108", "foo_outer@120:128"),
                        Arrays.asList(parentJoin, parentLeft, parentRight, localPhysical), parentJoins),
                Arguments.of("INSERT SELECT CTE sibling isolation", insertingCommonTables, Collections.emptyList(),
                        Arrays.asList("foo_target@5:14", "foo_left@40:47", "bar_right@60:68", "foo_outer@100:108", "foo_outer@120:128"),
                        Arrays.asList(updateTarget, innerLeft, innerRight, localPhysical), innerJoins),
                Arguments.of("Inherited alias prefix", createScopeOuterSelect(inheritedAlias, inheritedUsingSelect), Collections.emptyList(),
                        Arrays.asList("foo_inherited@200:214", "foo_left@40:47", "bar_right@60:68"), Arrays.asList(inheritedAlias, innerLeft, innerRight), innerJoins),
                Arguments.of("Inherited prefix excludes descendants", createScopeOuterSelect(inheritedPhysical, inheritedPhysicalSelect), Collections.emptyList(),
                        Arrays.asList("f@150:150", "foo_left@40:47", "bar_right@60:68", "foo_shadow@100:114", "f@20:20"),
                        Arrays.asList(inheritedPhysical, innerLeft, innerRight, descendantAlias), innerJoins),
                Arguments.of("Different alias hides local physical name", createScopeOuterSelect(parentJoin, hiddenPhysical), Collections.emptyList(),
                        Arrays.asList("foo_parent@150:159", "bar_parent@170:179", "foo_outer@100:122"),
                        Arrays.asList(parentJoin, parentLeft, parentRight, differentlyAliased), parentJoins),
                Arguments.of("Unresolved parent lookup", unresolved, Collections.emptyList(), Collections.singletonList("foo_outer@120:128"), Collections.emptyList(), Collections.emptyList()),
                Arguments.of("Inherited correlated alias", unresolved, Collections.singletonList(localAlias), Collections.emptyList(), Collections.singletonList(localAlias), Collections.emptyList()));
    }
    
    private static SimpleTableSegment createScopeTable(final String tableName, final int startIndex) {
        return new SimpleTableSegment(new TableNameSegment(startIndex, startIndex + tableName.length() - 1, new IdentifierValue(tableName)));
    }
    
    private static JoinTableSegment createUsingAliasJoin(final SimpleTableSegment left, final SimpleTableSegment right) {
        JoinTableSegment result = new JoinTableSegment();
        result.setLeft(left);
        result.setRight(right);
        result.setUsing(Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("id"))));
        result.setUsingAlias(new AliasSegment(0, 0, new IdentifierValue("foo_outer")));
        return result;
    }
    
    private static ColumnSegment createScopeColumn(final String owner, final int startIndex) {
        ColumnSegment result = new ColumnSegment(startIndex + owner.length() + 1, startIndex + owner.length() + 2, new IdentifierValue("id"));
        result.setOwner(new OwnerSegment(startIndex, startIndex + owner.length() - 1, new IdentifierValue(owner)));
        return result;
    }
    
    private static ProjectionsSegment createScopeProjections(final ProjectionSegment... projections) {
        ProjectionsSegment result = new ProjectionsSegment(0, 0);
        result.getProjections().addAll(Arrays.asList(projections));
        return result;
    }
    
    private static SelectStatement createScopeOuterSelect(final TableSegment from, final SelectStatement child) {
        return SelectStatement.builder().databaseType(new MockedDatabaseType())
                .projections(createScopeProjections(new SubqueryProjectionSegment(new SubquerySegment(0, 130, child, ""), ""))).from(from).build();
    }
    
    @Test
    void assertExtractTablesFromCreateTableStatement() {
        CreateTableStatement createTableStatement = mock(CreateTableStatement.class);
        when(createTableStatement.getTable()).thenReturn(createSimpleTableSegmentForDDLStatement());
        assertSimpleTableSegmentFromDDLStatement(createTableStatement);
    }
    
    @Test
    void assertExtractTablesFromAlterTableStatement() {
        AlterTableStatement alterTableStatement = mock(AlterTableStatement.class);
        when(alterTableStatement.getTable()).thenReturn(createSimpleTableSegmentForDDLStatement());
        assertSimpleTableSegmentFromDDLStatement(alterTableStatement);
    }
    
    @Test
    void assertExtractTablesFromCommentStatement() {
        CommentStatement commentStatement = mock(CommentStatement.class);
        when(commentStatement.getTable()).thenReturn(createSimpleTableSegmentForDDLStatement());
        assertSimpleTableSegmentFromDDLStatement(commentStatement);
    }
    
    @Test
    void assertExtractTablesFromCreateIndexStatement() {
        CreateIndexStatement createIndexStatement = mock(CreateIndexStatement.class);
        when(createIndexStatement.getTable()).thenReturn(createSimpleTableSegmentForDDLStatement());
        assertSimpleTableSegmentFromDDLStatement(createIndexStatement);
    }
    
    @Test
    void assertExtractTablesFromDropIndexStatement() {
        DropIndexStatement dropIndexStatement = mock(DropIndexStatement.class);
        when(dropIndexStatement.getSimpleTable()).thenReturn(Optional.of(createSimpleTableSegmentForDDLStatement()));
        assertSimpleTableSegmentFromDDLStatement(dropIndexStatement);
    }
    
    private SimpleTableSegment createSimpleTableSegmentForDDLStatement() {
        return new SimpleTableSegment(new TableNameSegment(30, 40, new IdentifierValue("t_order")));
    }
    
    private void assertSimpleTableSegmentFromDDLStatement(final DDLStatement statement) {
        tableExtractor.extractTablesFromSQLStatement(statement);
        assertThat(tableExtractor.getRewriteTables().size(), is(1));
        assertTableSegment(tableExtractor.getRewriteTables().iterator().next(), 30, 40, "t_order");
    }
}
