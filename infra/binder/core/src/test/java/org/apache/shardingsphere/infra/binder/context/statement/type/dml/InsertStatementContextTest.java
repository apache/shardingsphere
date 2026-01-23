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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InsertStatementContextTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
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
        when(schema.getVisibleColumnNames("tbl")).thenReturn(Arrays.asList("id", "name", "status"));
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, mock(), mock(), Collections.singleton(schema));
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
        InsertStatement insertStatement = new InsertStatement(databaseType);
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.addParameterMarkers(Collections.singleton(new ParameterMarkerExpressionSegment(0, 0, 0, ParameterMarkerType.QUESTION)));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SubquerySegment insertSelect = new SubquerySegment(0, 0, selectStatement, "");
        insertStatement.setInsertSelect(insertSelect);
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("tbl"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        insertStatement.setTable(new SimpleTableSegment(tableNameSegment));
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
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.addParameterMarkers(segments);
        assertThat(selectStatement.getParameterCount(), is(1));
        assertThat(selectStatement.getParameterMarkers().size(), is(1));
    }
    
    private void setUpInsertValues(final InsertStatement insertStatement) {
        when(insertStatement.getValues()).thenReturn(Arrays.asList(
                new InsertValuesSegment(0, 0, Arrays.asList(
                        new ParameterMarkerExpressionSegment(0, 0, 1), new ParameterMarkerExpressionSegment(0, 0, 2), new LiteralExpressionSegment(0, 0, "init"))),
                new InsertValuesSegment(0, 0, Arrays.asList(
                        new ParameterMarkerExpressionSegment(0, 0, 3), new ParameterMarkerExpressionSegment(0, 0, 4), new LiteralExpressionSegment(0, 0, "init")))));
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
        InsertStatement insertStatement = new InsertStatement(databaseType);
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("col"))));
        insertStatement.setInsertColumns(insertColumnsSegment);
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue(""));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        insertStatement.setTable(new SimpleTableSegment(tableNameSegment));
        InsertStatementContext insertStatementContext = createInsertStatementContext(insertStatement);
        assertTrue(insertStatementContext.containsInsertColumns());
    }
    
    @Test
    void assertNotContainsInsertColumns() {
        InsertStatement insertStatement = new InsertStatement(databaseType);
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue(""));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        insertStatement.setTable(new SimpleTableSegment(tableNameSegment));
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
        InsertStatement insertStatement = new InsertStatement(databaseType);
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.singletonList(new LiteralExpressionSegment(0, 0, 1))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.singletonList(new LiteralExpressionSegment(0, 0, 2))));
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue(""));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        insertStatement.setTable(new SimpleTableSegment(tableNameSegment));
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
        InsertStatement insertStatement = new InsertStatement(databaseType);
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("col"))));
        insertStatement.setInsertColumns(insertColumnsSegment);
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue(""));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        insertStatement.setTable(new SimpleTableSegment(tableNameSegment));
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
