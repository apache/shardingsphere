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

package org.apache.shardingsphere.sql.parser.binder.statement.impl;

import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.value.identifier.IdentifierValue;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class InsertStatementContextTest {
    
    @Test
    public void assertInsertStatementContextWithColumnNames() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("tbl")));
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0, Arrays.asList(
                new ColumnSegment(0, 0, new IdentifierValue("id")), new ColumnSegment(0, 0, new IdentifierValue("name")), new ColumnSegment(0, 0, new IdentifierValue("status"))));
        insertStatement.setInsertColumns(insertColumnsSegment);
        setUpInsertValues(insertStatement);
        InsertStatementContext actual = new InsertStatementContext(mock(SchemaMetaData.class), Arrays.asList(1, "Tom", 2, "Jerry"), insertStatement);
        assertInsertStatementContext(actual);
    }
    
    @Test
    public void assertInsertStatementContextWithoutColumnNames() {
        SchemaMetaData schemaMetaData = mock(SchemaMetaData.class);
        when(schemaMetaData.getAllColumnNames("tbl")).thenReturn(Arrays.asList("id", "name", "status"));
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("tbl")));
        setUpInsertValues(insertStatement);
        InsertStatementContext actual = new InsertStatementContext(schemaMetaData, Arrays.asList(1, "Tom", 2, "Jerry"), insertStatement);
        assertInsertStatementContext(actual);
    }
    
    @Test
    public void assertGetGroupedParametersWithoutOnDuplicateParameter() {
        SchemaMetaData schemaMetaData = mock(SchemaMetaData.class);
        when(schemaMetaData.getAllColumnNames("tbl")).thenReturn(Arrays.asList("id", "name", "status"));
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("tbl")));
        setUpInsertValues(insertStatement);
        InsertStatementContext actual = new InsertStatementContext(schemaMetaData, Arrays.asList(1, "Tom", 2, "Jerry"), insertStatement);
        assertThat(actual.getGroupedParameters().size(), is(2));
        assertNull(actual.getOnDuplicateKeyUpdateValueContext());
        assertThat(actual.getOnDuplicateKeyUpdateParameters().size(), is(0));
    }
    
    @Test
    public void assertGetGroupedParametersWithOnDuplicateParameters() {
        SchemaMetaData schemaMetaData = mock(SchemaMetaData.class);
        when(schemaMetaData.getAllColumnNames("tbl")).thenReturn(Arrays.asList("id", "name", "status"));
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("tbl")));
        setUpInsertValues(insertStatement);
        setUpOnDuplicateValues(insertStatement);
        InsertStatementContext actual = new InsertStatementContext(schemaMetaData, Arrays.asList(1, "Tom", 2, "Jerry", "onDuplicateKeyUpdateColumnValue"), insertStatement);
        assertThat(actual.getGroupedParameters().size(), is(2));
        assertThat(actual.getOnDuplicateKeyUpdateValueContext().getColumns().size(), is(2));
        assertThat(actual.getOnDuplicateKeyUpdateParameters().size(), is(1));
    }
    
    private void setUpInsertValues(final InsertStatement insertStatement) {
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Arrays.asList(
                new ParameterMarkerExpressionSegment(0, 0, 1), new ParameterMarkerExpressionSegment(0, 0, 2), new LiteralExpressionSegment(0, 0, "init"))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Arrays.asList(
                new ParameterMarkerExpressionSegment(0, 0, 3), new ParameterMarkerExpressionSegment(0, 0, 4), new LiteralExpressionSegment(0, 0, "init"))));
    }
    
    private void setUpOnDuplicateValues(final InsertStatement insertStatement) {
        AssignmentSegment parameterMarkerExpressionAssignment = new AssignmentSegment(0, 0,
                new ColumnSegment(0, 0, new IdentifierValue("on_duplicate_key_update_column_1")),
                new ParameterMarkerExpressionSegment(0, 0, 4)
        );
        AssignmentSegment literalExpressionAssignment = new AssignmentSegment(0, 0,
                new ColumnSegment(0, 0, new IdentifierValue("on_duplicate_key_update_column_2")),
                new LiteralExpressionSegment(0, 0, 5)
        );
        OnDuplicateKeyColumnsSegment onDuplicateKeyColumnsSegment = new OnDuplicateKeyColumnsSegment(0, 0, Arrays.asList(
                parameterMarkerExpressionAssignment, literalExpressionAssignment
        ));
        insertStatement.setOnDuplicateKeyColumns(onDuplicateKeyColumnsSegment);
    }
    
    private void assertInsertStatementContext(final InsertStatementContext actual) {
        assertThat(actual.getColumnNames(), is(Arrays.asList("id", "name", "status")));
        assertThat(actual.getInsertValueContexts().size(), is(2));
        assertThat(actual.getInsertValueContexts().get(0).getValue(0), is(1));
        assertThat(actual.getInsertValueContexts().get(0).getValue(1), is("Tom"));
        assertThat(actual.getInsertValueContexts().get(0).getValue(2), is("init"));
        assertThat(actual.getInsertValueContexts().get(1).getValue(0), is(2));
        assertThat(actual.getInsertValueContexts().get(1).getValue(1), is("Jerry"));
        assertThat(actual.getInsertValueContexts().get(1).getValue(2), is("init"));
    }
}
