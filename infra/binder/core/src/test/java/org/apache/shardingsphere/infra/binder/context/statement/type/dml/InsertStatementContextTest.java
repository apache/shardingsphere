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

package org.apache.shardingsphere.infra.binder.context.statement.type.dml;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.ParameterMarkerType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.RandomAccess;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InsertStatementContextTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertExposeInsertConflictCondition() {
        TableNameSegment table = new TableNameSegment(0, 2, new IdentifierValue("tbl"));
        table.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        ColumnSegment column = new ColumnSegment(80, 81, new IdentifierValue("id"));
        WhereSegment where = new WhereSegment(74, 85, new BinaryOperationExpression(80, 85, column, new LiteralExpressionSegment(85, 85, 1), "=", "id = 1"));
        InsertStatement statement = InsertStatement.builder().databaseType(databaseType).table(new SimpleTableSegment(table))
                .insertColumns(new InsertColumnsSegment(4, 10, Collections.singletonList(new ColumnSegment(5, 6, new IdentifierValue("id")))))
                .values(Collections.singletonList(new InsertValuesSegment(20, 22, Collections.singletonList(new LiteralExpressionSegment(21, 21, 1)))))
                .where(where).build();
        InsertStatementContext actual = createInsertStatementContext(statement);
        assertThat(actual.getWhereSegments(), is(Collections.singletonList(where)));
        assertThat(actual.getColumnSegments(), is(Collections.singletonList(column)));
    }
    
    @ParameterizedTest(name = "parameterizedSource={0}, literalFirstRow={1}, attachWith={2}")
    @CsvSource({"false,false,true", "true,false,true", "false,true,true", "true,true,true", "false,false,false", "true,false,false", "false,true,false", "true,true,false"})
    void assertKeepWithParametersOutsideValueRows(final boolean parameterizedSource, final boolean literalFirstRow, final boolean attachWith) {
        int offset = parameterizedSource ? 1 : 0;
        ParameterMarkerExpressionSegment sourceMarker = new ParameterMarkerExpressionSegment(15, 15, 0);
        ExpressionSegment sourceValue = parameterizedSource ? sourceMarker : new LiteralExpressionSegment(15, 15, 9);
        ProjectionsSegment sourceProjections = new ProjectionsSegment(15, 15);
        sourceProjections.getProjections().add(new ExpressionProjectionSegment(15, 15, parameterizedSource ? "?" : "9", sourceValue));
        SelectStatement source = SelectStatement.builder().databaseType(databaseType).projections(sourceProjections).build();
        CommonTableExpressionSegment common = new CommonTableExpressionSegment(5, 20, new AliasSegment(5, 10, new IdentifierValue("source")), new SubquerySegment(12, 20, source, ""), null);
        TableNameSegment table = new TableNameSegment(35, 37, new IdentifierValue("tbl"));
        table.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        List<ParameterMarkerSegment> markers = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        if (parameterizedSource) {
            markers.add(sourceMarker);
            source.addParameterMarkers(Collections.singleton(sourceMarker));
            params.add(9);
        }
        List<ExpressionSegment> first = new ArrayList<>();
        if (literalFirstRow) {
            first.add(new LiteralExpressionSegment(40, 40, 1));
            first.add(new LiteralExpressionSegment(42, 46, "Tom"));
        } else {
            ParameterMarkerExpressionSegment id = new ParameterMarkerExpressionSegment(40, 40, offset);
            ParameterMarkerExpressionSegment name = new ParameterMarkerExpressionSegment(42, 42, offset + 1);
            first.addAll(Arrays.asList(id, name));
            markers.addAll(Arrays.asList(id, name));
            params.addAll(Arrays.asList(1, "Tom"));
        }
        first.add(new LiteralExpressionSegment(48, 53, "init"));
        int nextIndex = params.size();
        ParameterMarkerExpressionSegment secondId = new ParameterMarkerExpressionSegment(60, 60, nextIndex);
        ParameterMarkerExpressionSegment secondName = new ParameterMarkerExpressionSegment(62, 62, nextIndex + 1);
        markers.addAll(Arrays.asList(secondId, secondName));
        params.addAll(Arrays.asList(2, "Jerry"));
        InsertStatement statement = InsertStatement.builder().databaseType(databaseType).table(new SimpleTableSegment(table))
                .with(attachWith ? new WithSegment(0, 20, Collections.singleton(common), false) : null).values(Arrays.asList(new InsertValuesSegment(39, 54, first),
                        new InsertValuesSegment(59, 70, Arrays.asList(secondId, secondName, new LiteralExpressionSegment(64, 69, "init")))))
                .build();
        statement.addParameterMarkers(markers);
        InsertStatementContext actual = createInsertStatementContext(statement);
        actual.bindParameters(params);
        if (attachWith) {
            assertThat(actual.getBaseContext().getSubqueryContexts().size(), is(1));
            assertThat(actual.getBaseContext().getSubqueryContexts().get(12).getSubqueryType(), is(org.apache.shardingsphere.sql.parser.statement.core.enums.SubqueryType.WITH));
        } else {
            assertTrue(actual.getBaseContext().getSubqueryContexts().isEmpty());
        }
        assertThat(actual.getGroupedParameters(), is(Arrays.asList(literalFirstRow ? Collections.emptyList() : Arrays.<Object>asList(1, "Tom"), Arrays.<Object>asList(2, "Jerry"))));
        assertThat(actual.getInsertValueContexts().get(0).getParametersOffset(), is(offset));
        assertThat(actual.getInsertValueContexts().get(1).getParametersOffset(), is(offset + (literalFirstRow ? 0 : 2)));
    }
    
    @Test
    void assertInsertStatementContextWithColumnNames() {
        InsertStatement insertStatement = mock(InsertStatement.class);
        when(insertStatement.getDatabaseType()).thenReturn(databaseType);
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("tbl"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        SimpleTableSegment tableSegment = new SimpleTableSegment(tableNameSegment);
        tableSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("foo_db".toUpperCase())));
        when(insertStatement.getTable()).thenReturn(Optional.of(tableSegment));
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0, Arrays.asList(
                new ColumnSegment(0, 0, new IdentifierValue("id")), new ColumnSegment(0, 0, new IdentifierValue("name")), new ColumnSegment(0, 0, new IdentifierValue("status"))));
        when(insertStatement.getInsertColumns()).thenReturn(Optional.of(insertColumnsSegment));
        setUpInsertValues(insertStatement);
        InsertStatementContext actual = createInsertStatementContext(insertStatement);
        actual.bindParameters(Arrays.asList(1, "Tom", 2, "Jerry"));
        assertInsertStatementContext(actual);
    }
    
    private InsertStatementContext createInsertStatementContext(final InsertStatement insertStatement) {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.getName()).thenReturn("foo_db");
        when(schema.getVisibleColumnNames(new IdentifierValue("tbl"))).thenReturn(Arrays.asList("id", "name", "status"));
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, mock(), mock(), Collections.singleton(schema), new ConfigurationProperties(new Properties()));
        return new InsertStatementContext(insertStatement, new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock()), "foo_db");
    }
    
    @Test
    void assertInsertStatementContextWithoutColumnNames() {
        InsertStatement insertStatement = mock(InsertStatement.class);
        when(insertStatement.getDatabaseType()).thenReturn(databaseType);
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("tbl"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        when(insertStatement.getTable()).thenReturn(Optional.of(new SimpleTableSegment(tableNameSegment)));
        setUpInsertValues(insertStatement);
        InsertStatementContext actual = createInsertStatementContext(insertStatement);
        actual.bindParameters(Arrays.asList(1, "Tom", 2, "Jerry"));
        assertInsertStatementContext(actual);
    }
    
    @Test
    void assertInsertValueContextsSupportRandomAccess() {
        InsertStatement insertStatement = mock(InsertStatement.class);
        when(insertStatement.getDatabaseType()).thenReturn(databaseType);
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("tbl"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        when(insertStatement.getTable()).thenReturn(Optional.of(new SimpleTableSegment(tableNameSegment)));
        setUpInsertValues(insertStatement);
        InsertStatementContext actual = createInsertStatementContext(insertStatement);
        actual.bindParameters(Arrays.asList(1, "Tom", 2, "Jerry"));
        List<InsertValueContext> actualInsertValueContexts = actual.getInsertValueContexts();
        assertThat(actualInsertValueContexts.size(), is(2));
        assertTrue(actualInsertValueContexts instanceof RandomAccess);
    }
    
    @Test
    void assertGetGroupedParametersWithoutOnDuplicateParameter() {
        InsertStatement insertStatement = mock(InsertStatement.class);
        when(insertStatement.getDatabaseType()).thenReturn(databaseType);
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("tbl"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        when(insertStatement.getTable()).thenReturn(Optional.of(new SimpleTableSegment(tableNameSegment)));
        setUpInsertValues(insertStatement);
        InsertStatementContext actual = createInsertStatementContext(insertStatement);
        actual.bindParameters(Arrays.asList(1, "Tom", 2, "Jerry"));
        assertThat(actual.getGroupedParameters().size(), is(2));
        assertNull(actual.getOnDuplicateKeyUpdateValueContext());
        assertTrue(actual.getOnDuplicateKeyUpdateParameters().isEmpty());
    }
    
    @Test
    void assertGetGroupedParametersWithOnDuplicateParameters() {
        InsertStatement insertStatement = mock(InsertStatement.class);
        when(insertStatement.getDatabaseType()).thenReturn(databaseType);
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("tbl"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        when(insertStatement.getTable()).thenReturn(Optional.of(new SimpleTableSegment(tableNameSegment)));
        setUpInsertValues(insertStatement);
        setUpOnDuplicateValues(insertStatement);
        InsertStatementContext actual = createInsertStatementContext(insertStatement);
        actual.bindParameters(Arrays.asList(1, "Tom", 2, "Jerry", "onDuplicateKeyUpdateColumnValue"));
        assertThat(actual.getGroupedParameters().size(), is(2));
        assertThat(actual.getOnDuplicateKeyUpdateValueContext().getColumns().size(), is(2));
        assertThat(actual.getOnDuplicateKeyUpdateParameters().size(), is(1));
    }
    
    @Test
    void assertInsertSelect() {
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).projections(new ProjectionsSegment(0, 0)).build();
        selectStatement.addParameterMarkers(Collections.singleton(new ParameterMarkerExpressionSegment(0, 0, 0, ParameterMarkerType.QUESTION)));
        SubquerySegment insertSelect = new SubquerySegment(0, 0, selectStatement, "");
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("tbl"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        InsertStatement insertStatement = InsertStatement.builder().databaseType(databaseType).insertSelect(insertSelect).table(new SimpleTableSegment(tableNameSegment)).build();
        InsertStatementContext actual = createInsertStatementContext(insertStatement);
        actual.bindParameters(Collections.singletonList("param"));
        assertThat(actual.getInsertSelectContext().getSelectStatementContext().getSqlStatement().getParameterCount(), is(1));
        assertThat(actual.getGroupedParameters().size(), is(0));
    }
    
    @Test
    void assertAddParameterMarkersWithDuplicates() {
        Collection<ParameterMarkerSegment> segments = new ArrayList<>();
        ParameterMarkerExpressionSegment parameterMarkerExpressionSegment = new ParameterMarkerExpressionSegment(1, 0, 1);
        segments.add(parameterMarkerExpressionSegment);
        segments.add(parameterMarkerExpressionSegment);
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).build();
        selectStatement.addParameterMarkers(segments);
        assertThat(selectStatement.getParameterCount(), is(1));
        assertThat(selectStatement.getParameterMarkers().size(), is(1));
    }
    
    private void setUpInsertValues(final InsertStatement insertStatement) {
        when(insertStatement.getValues()).thenReturn(Arrays.asList(
                new InsertValuesSegment(0, 0, Arrays.asList(
                        new ParameterMarkerExpressionSegment(0, 0, 0), new ParameterMarkerExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, "init"))),
                new InsertValuesSegment(0, 0, Arrays.asList(
                        new ParameterMarkerExpressionSegment(0, 0, 2), new ParameterMarkerExpressionSegment(0, 0, 3), new LiteralExpressionSegment(0, 0, "init")))));
    }
    
    private void setUpOnDuplicateValues(final InsertStatement insertStatement) {
        List<ColumnSegment> parameterMarkerExpressionAssignmentColumns = new LinkedList<>();
        parameterMarkerExpressionAssignmentColumns.add(new ColumnSegment(0, 0, new IdentifierValue("on_duplicate_key_update_column_1")));
        ColumnAssignmentSegment parameterMarkerExpressionAssignment = new ColumnAssignmentSegment(0, 0, parameterMarkerExpressionAssignmentColumns,
                new ParameterMarkerExpressionSegment(0, 0, 4));
        List<ColumnSegment> literalExpressionAssignmentColumns = new LinkedList<>();
        literalExpressionAssignmentColumns.add(new ColumnSegment(0, 0, new IdentifierValue("on_duplicate_key_update_column_2")));
        ColumnAssignmentSegment literalExpressionAssignment = new ColumnAssignmentSegment(0, 0, literalExpressionAssignmentColumns,
                new LiteralExpressionSegment(0, 0, 5));
        OnDuplicateKeyColumnsSegment onDuplicateKeyColumnsSegment = new OnDuplicateKeyColumnsSegment(0, 0, Arrays.asList(parameterMarkerExpressionAssignment, literalExpressionAssignment));
        when(insertStatement.getOnDuplicateKeyColumns()).thenReturn(Optional.of(onDuplicateKeyColumnsSegment));
    }
    
    private void assertInsertStatementContext(final InsertStatementContext actual) {
        assertThat(actual.getTablesContext().getTableNames(), is(new HashSet<>(Collections.singleton("tbl"))));
        assertThat(actual.getTablesContext().getSimpleTables().size(), is(1));
        SimpleTableSegment simpleTableSegment = actual.getTablesContext().getSimpleTables().iterator().next();
        assertThat(simpleTableSegment.getTableName().getStartIndex(), is(0));
        assertThat(simpleTableSegment.getTableName().getStopIndex(), is(0));
        assertThat(simpleTableSegment.getTableName().getIdentifier().getValue(), is("tbl"));
        List<String> columnNames = new ArrayList<>(3);
        actual.getDescendingColumnNames().forEachRemaining(columnNames::add);
        assertThat(columnNames, is(Arrays.asList("status", "name", "id")));
        assertThat(actual.getGeneratedKeyContext(), is(Optional.empty()));
        assertThat(actual.getColumnNames(), is(Arrays.asList("id", "name", "status")));
        assertThat(actual.getInsertValueContexts().size(), is(2));
        assertTrue(actual.getInsertValueContexts().get(0).getLiteralValue(0).isPresent());
        assertTrue(actual.getInsertValueContexts().get(0).getLiteralValue(1).isPresent());
        assertTrue(actual.getInsertValueContexts().get(0).getLiteralValue(2).isPresent());
        assertTrue(actual.getInsertValueContexts().get(1).getLiteralValue(0).isPresent());
        assertTrue(actual.getInsertValueContexts().get(1).getLiteralValue(1).isPresent());
        assertTrue(actual.getInsertValueContexts().get(1).getLiteralValue(2).isPresent());
        assertThat(actual.getInsertValueContexts().get(0).getLiteralValue(0).get(), is(1));
        assertThat(actual.getInsertValueContexts().get(0).getLiteralValue(1).get(), is("Tom"));
        assertThat(actual.getInsertValueContexts().get(0).getLiteralValue(2).get(), is("init"));
        assertThat(actual.getInsertValueContexts().get(1).getLiteralValue(0).get(), is(2));
        assertThat(actual.getInsertValueContexts().get(1).getLiteralValue(1).get(), is("Jerry"));
        assertThat(actual.getInsertValueContexts().get(1).getLiteralValue(2).get(), is("init"));
    }
    
    @Test
    void assertContainsInsertColumns() {
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("col"))));
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue(""));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        InsertStatement insertStatement = InsertStatement.builder().databaseType(databaseType).insertColumns(insertColumnsSegment).table(new SimpleTableSegment(tableNameSegment)).build();
        InsertStatementContext insertStatementContext = createInsertStatementContext(insertStatement);
        assertTrue(insertStatementContext.containsInsertColumns());
    }
    
    @Test
    void assertNotContainsInsertColumns() {
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue(""));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        InsertStatement insertStatement = InsertStatement.builder().databaseType(databaseType).table(new SimpleTableSegment(tableNameSegment)).build();
        InsertStatementContext insertStatementContext = createInsertStatementContext(insertStatement);
        assertFalse(insertStatementContext.containsInsertColumns());
    }
    
    @Test
    void assertContainsInsertColumnsWithSetAssignment() {
        InsertStatement insertStatement = mock(InsertStatement.class);
        when(insertStatement.getDatabaseType()).thenReturn(databaseType);
        when(insertStatement.getSetAssignment()).thenReturn(Optional.of(new SetAssignmentSegment(0, 0, Collections.emptyList())));
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue(""));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        when(insertStatement.getTable()).thenReturn(Optional.of(new SimpleTableSegment(tableNameSegment)));
        InsertStatementContext insertStatementContext = createInsertStatementContext(insertStatement);
        assertTrue(insertStatementContext.containsInsertColumns());
    }
    
    @Test
    void assertGetValueListCountWithValues() {
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue(""));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        InsertStatement insertStatement = InsertStatement.builder().databaseType(databaseType).table(new SimpleTableSegment(tableNameSegment))
                .values(Arrays.asList(new InsertValuesSegment(0, 0, Collections.singletonList(new LiteralExpressionSegment(0, 0, 1))),
                        new InsertValuesSegment(0, 0, Collections.singletonList(new LiteralExpressionSegment(0, 0, 2)))))
                .build();
        InsertStatementContext insertStatementContext = createInsertStatementContext(insertStatement);
        assertThat(insertStatementContext.getValueListCount(), is(2));
    }
    
    @Test
    void assertGetValueListCountWithSetAssignment() {
        InsertStatement insertStatement = mock(InsertStatement.class);
        when(insertStatement.getDatabaseType()).thenReturn(databaseType);
        List<ColumnSegment> columns = new LinkedList<>();
        columns.add(new ColumnSegment(0, 0, new IdentifierValue("col")));
        ColumnAssignmentSegment insertStatementAssignment = new ColumnAssignmentSegment(0, 0, columns, new LiteralExpressionSegment(0, 0, 1));
        when(insertStatement.getSetAssignment()).thenReturn(Optional.of(new SetAssignmentSegment(0, 0, Collections.singletonList(insertStatementAssignment))));
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue(""));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        when(insertStatement.getTable()).thenReturn(Optional.of(new SimpleTableSegment(tableNameSegment)));
        InsertStatementContext insertStatementContext = createInsertStatementContext(insertStatement);
        assertThat(insertStatementContext.getValueListCount(), is(1));
    }
    
    @Test
    void assertGetInsertColumnNamesForInsertColumns() {
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("col"))));
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue(""));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        InsertStatement insertStatement = InsertStatement.builder().databaseType(databaseType).insertColumns(insertColumnsSegment).table(new SimpleTableSegment(tableNameSegment)).build();
        InsertStatementContext insertStatementContext = createInsertStatementContext(insertStatement);
        List<String> columnNames = insertStatementContext.getInsertColumnNames();
        assertThat(columnNames.size(), is(1));
        assertThat(columnNames.iterator().next(), is("col"));
    }
    
    @Test
    void assertGetInsertColumnNamesForSetAssignment() {
        InsertStatement insertStatement = mock(InsertStatement.class);
        when(insertStatement.getDatabaseType()).thenReturn(databaseType);
        List<ColumnSegment> columns = new LinkedList<>();
        columns.add(new ColumnSegment(0, 0, new IdentifierValue("col")));
        ColumnAssignmentSegment insertStatementAssignment = new ColumnAssignmentSegment(0, 0, columns, new LiteralExpressionSegment(0, 0, 1));
        when(insertStatement.getSetAssignment()).thenReturn(Optional.of(new SetAssignmentSegment(0, 0, Collections.singletonList(insertStatementAssignment))));
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue(""));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        when(insertStatement.getTable()).thenReturn(Optional.of(new SimpleTableSegment(tableNameSegment)));
        InsertStatementContext insertStatementContext = createInsertStatementContext(insertStatement);
        List<String> columnNames = insertStatementContext.getInsertColumnNames();
        assertThat(columnNames.size(), is(1));
        assertThat(columnNames.iterator().next(), is("col"));
    }
}
