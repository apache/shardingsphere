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

package org.apache.shardingsphere.driver.jdbc.core.resultset;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.junit.jupiter.api.Test;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingSphereResultSetUtilsTest {
    
    @Test
    void assertCreateColumnLabelAndIndexMapWithSelectWithoutExpandProjections() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getProjectionsContext()).thenReturn(new ProjectionsContext(0, 0, false, Collections.emptyList()));
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("label");
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, resultSetMetaData);
        assertThat(actual, is(Collections.singletonMap("label", 1)));
    }
    
    @Test
    void assertCreateColumnLabelAndIndexMapWithSelectWithExpandProjections() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        Collection<Projection> projections = new ArrayList<>(2);
        projections.add(new ColumnProjection(null, "col1", null, mock(DatabaseType.class)));
        projections.add(new ColumnProjection(null, "col2", null, mock(DatabaseType.class)));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, projections);
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        Map<String, Integer> expected = new HashMap<>(2, 1F);
        expected.put("col1", 1);
        expected.put("col2", 2);
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, null);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertCreateColumnLabelAndIndexMapWithUnexpandedShorthand() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        ShorthandProjection shorthand = new ShorthandProjection(null, Collections.emptyList());
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.singletonList(shorthand));
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(2);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("id");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("name");
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, resultSetMetaData);
        Map<String, Integer> expected = new HashMap<>(2, 1F);
        expected.put("id", 1);
        expected.put("name", 2);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertGetVisibleColumnCountWithDerivedProjections() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        Collection<Projection> projections = new ArrayList<>(2);
        projections.add(new ColumnProjection(null, "col1", null, mock(DatabaseType.class)));
        projections.add(new ColumnProjection(null, "col2", null, mock(DatabaseType.class)));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, projections);
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        assertThat(ShardingSphereResultSetUtils.getVisibleColumnCount(selectStatementContext, null), is(2));
    }
    
    @Test
    void assertGetVisibleColumnCountWithoutDerivedProjections() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(5);
        assertThat(ShardingSphereResultSetUtils.getVisibleColumnCount(selectStatementContext, resultSetMetaData), is(5));
    }
}
