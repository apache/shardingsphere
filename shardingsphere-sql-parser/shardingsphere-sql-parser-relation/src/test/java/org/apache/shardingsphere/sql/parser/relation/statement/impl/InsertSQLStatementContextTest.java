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

package org.apache.shardingsphere.sql.parser.relation.statement.impl;

import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class InsertSQLStatementContextTest {
    
    @Test
    public void assertInsertSQLStatementContextWithColumnNames() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.getAllSQLSegments().add(new TableSegment(0, 0, "tbl"));
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0);
        insertColumnsSegment.getColumns().addAll(Arrays.asList(new ColumnSegment(0, 0, "id"), new ColumnSegment(0, 0, "name"), new ColumnSegment(0, 0, "status")));
        insertStatement.setColumns(insertColumnsSegment);
        setUpInsertValues(insertStatement);
        InsertSQLStatementContext actual = new InsertSQLStatementContext(mock(RelationMetas.class), Arrays.<Object>asList(1, "Tom", 2, "Jerry"), insertStatement);
        assertInsertSQLStatementContext(actual);
    }
    
    @Test
    public void assertInsertSQLStatementContextWithoutColumnNames() {
        RelationMetas relationMetas = mock(RelationMetas.class);
        when(relationMetas.getAllColumnNames("tbl")).thenReturn(Arrays.asList("id", "name", "status"));
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.getAllSQLSegments().add(new TableSegment(0, 0, "tbl"));
        setUpInsertValues(insertStatement);
        InsertSQLStatementContext actual = new InsertSQLStatementContext(relationMetas, Arrays.<Object>asList(1, "Tom", 2, "Jerry"), insertStatement);
        assertInsertSQLStatementContext(actual);
    }
    
    @Test
    public void assertGetGroupedParameters() {
        RelationMetas relationMetas = mock(RelationMetas.class);
        when(relationMetas.getAllColumnNames("tbl")).thenReturn(Arrays.asList("id", "name", "status"));
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.getAllSQLSegments().add(new TableSegment(0, 0, "tbl"));
        setUpInsertValues(insertStatement);
        InsertSQLStatementContext actual = new InsertSQLStatementContext(relationMetas, Arrays.<Object>asList(1, "Tom", 2, "Jerry"), insertStatement);
        assertThat(actual.getGroupedParameters().size(), is(2));
    }
    
    private void setUpInsertValues(final InsertStatement insertStatement) {
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Arrays.<ExpressionSegment>asList(
                new ParameterMarkerExpressionSegment(0, 0, 1), new ParameterMarkerExpressionSegment(0, 0, 2), new LiteralExpressionSegment(0, 0, "init"))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Arrays.<ExpressionSegment>asList(
                new ParameterMarkerExpressionSegment(0, 0, 3), new ParameterMarkerExpressionSegment(0, 0, 4), new LiteralExpressionSegment(0, 0, "init"))));
    }
    
    private void assertInsertSQLStatementContext(final InsertSQLStatementContext actual) {
        assertThat(actual.getTablesContext().getSingleTableName(), is("tbl"));
        assertThat(actual.getColumnNames(), is(Arrays.asList("id", "name", "status")));
        assertThat(actual.getInsertValueContexts().size(), is(2));
        assertThat(actual.getInsertValueContexts().get(0).getValue(0), is((Object) 1));
        assertThat(actual.getInsertValueContexts().get(0).getValue(1), is((Object) "Tom"));
        assertThat(actual.getInsertValueContexts().get(0).getValue(2), is((Object) "init"));
        assertThat(actual.getInsertValueContexts().get(1).getValue(0), is((Object) 2));
        assertThat(actual.getInsertValueContexts().get(1).getValue(1), is((Object) "Jerry"));
        assertThat(actual.getInsertValueContexts().get(1).getValue(2), is((Object) "init"));
    }
}
