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

package org.apache.shardingsphere.core.optimize.api.statement;

import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class InsertOptimizeStatementTest {
    
    @Test
    public void assertInsertOptimizedStatementWithColumnNames() {
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.getAllSQLSegments().add(new TableSegment(0, 0, "tbl"));
        insertStatement.getColumns().addAll(Arrays.asList(new ColumnSegment(0, 0, "id"), new ColumnSegment(0, 0, "name"), new ColumnSegment(0, 0, "status")));
        setUpInsertValues(insertStatement);
        InsertOptimizedStatement actual = new InsertOptimizedStatement(mock(TableMetas.class), Arrays.<Object>asList(1, "Tom", 2, "Jerry"), insertStatement);
        assertInsertOptimizeStatement(actual);
    }
    
    @Test
    public void assertInsertOptimizedStatementWithoutColumnNames() {
        TableMetas tableMetas = mock(TableMetas.class);
        when(tableMetas.getAllColumnNames("tbl")).thenReturn(Arrays.asList("id", "name", "status"));
        InsertStatement insertStatement = new InsertStatement();
        insertStatement.getAllSQLSegments().add(new TableSegment(0, 0, "tbl"));
        setUpInsertValues(insertStatement);
        InsertOptimizedStatement actual = new InsertOptimizedStatement(tableMetas, Arrays.<Object>asList(1, "Tom", 2, "Jerry"), insertStatement);
        assertInsertOptimizeStatement(actual);
    }
    
    private void setUpInsertValues(final InsertStatement insertStatement) {
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Arrays.<ExpressionSegment>asList(
                new ParameterMarkerExpressionSegment(0, 0, 1), new ParameterMarkerExpressionSegment(0, 0, 2), new LiteralExpressionSegment(0, 0, "init"))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Arrays.<ExpressionSegment>asList(
                new ParameterMarkerExpressionSegment(0, 0, 3), new ParameterMarkerExpressionSegment(0, 0, 4), new LiteralExpressionSegment(0, 0, "init"))));
    }
    
    private void assertInsertOptimizeStatement(final InsertOptimizedStatement actual) {
        assertThat(actual.getTables().getSingleTableName(), is("tbl"));
        assertThat(actual.getColumnNames(), is(Arrays.asList("id", "name", "status")));
        assertThat(actual.getInsertValues().size(), is(2));
        assertThat(actual.getInsertValues().get(0).getValue(0), is((Object) 1));
        assertThat(actual.getInsertValues().get(0).getValue(1), is((Object) "Tom"));
        assertThat(actual.getInsertValues().get(0).getValue(2), is((Object) "init"));
        assertThat(actual.getInsertValues().get(1).getValue(0), is((Object) 2));
        assertThat(actual.getInsertValues().get(1).getValue(1), is((Object) "Jerry"));
        assertThat(actual.getInsertValues().get(1).getValue(2), is((Object) "init"));
    }
}
