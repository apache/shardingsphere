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
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VisibleColumnMappingTest {
    
    @Test
    void assertResolveWithUnexpandedShorthand() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        ShorthandProjection shorthand = new ShorthandProjection(null, Collections.emptyList());
        List<Projection> projections = new ArrayList<>(1);
        projections.add(shorthand);
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, projections);
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        ResultSetMetaData physicalMetaData = mock(ResultSetMetaData.class);
        when(physicalMetaData.getColumnCount()).thenReturn(3);
        when(physicalMetaData.getColumnLabel(1)).thenReturn("id");
        when(physicalMetaData.getColumnLabel(2)).thenReturn("name");
        when(physicalMetaData.getColumnLabel(3)).thenReturn("age");
        VisibleColumnMapping mapping = VisibleColumnMapping.resolve(selectStatementContext, physicalMetaData);
        assertThat(mapping.getColumnCount(), is(3));
        assertThat(mapping.getColumnLabelAndIndexMap().get("id"), is(1));
        assertThat(mapping.getColumnLabelAndIndexMap().get("NAME"), is(2));
        assertThat(mapping.getColumnLabelAndIndexMap().get("Age"), is(3));
        assertNull(mapping.findProjection(1));
    }
    
    @Test
    void assertResolveWithExpandedProjections() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        List<Projection> projections = new ArrayList<>(2);
        ColumnProjection col1 = new ColumnProjection(null, "col1", null, mock(DatabaseType.class));
        ColumnProjection col2 = new ColumnProjection(null, "col2", null, mock(DatabaseType.class));
        projections.add(col1);
        projections.add(col2);
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, projections);
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        VisibleColumnMapping mapping = VisibleColumnMapping.resolve(selectStatementContext, null);
        assertThat(mapping.getColumnCount(), is(2));
        assertThat(mapping.getColumnLabelAndIndexMap().get("col1"), is(1));
        assertThat(mapping.getColumnLabelAndIndexMap().get("COL2"), is(2));
        assertThat(mapping.getExpandProjections().size(), is(2));
    }
    
    @Test
    void assertResolveIfExpandedReturnsNullForUnexpandedShorthand() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        ShorthandProjection shorthand = new ShorthandProjection(null, Collections.emptyList());
        List<Projection> projections = new ArrayList<>(1);
        projections.add(shorthand);
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, projections);
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        assertNull(VisibleColumnMapping.resolveIfExpanded(selectStatementContext));
    }
    
    @Test
    void assertResolveIfExpandedReturnsMappingForExpandedProjections() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        List<Projection> projections = new ArrayList<>(1);
        projections.add(new ColumnProjection(null, "col1", null, mock(DatabaseType.class)));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, projections);
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        VisibleColumnMapping mapping = VisibleColumnMapping.resolveIfExpanded(selectStatementContext);
        assertThat(mapping.getColumnCount(), is(1));
        assertThat(mapping.getColumnLabelAndIndexMap().get("col1"), is(1));
    }
    
    @Test
    void assertFindProjectionWithExpandedProjections() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        List<Projection> projections = new ArrayList<>(2);
        ColumnProjection col1 = new ColumnProjection(null, "col1", null, mock(DatabaseType.class));
        ColumnProjection col2 = new ColumnProjection(null, "col2", null, mock(DatabaseType.class));
        projections.add(col1);
        projections.add(col2);
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, projections);
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        VisibleColumnMapping mapping = VisibleColumnMapping.resolve(selectStatementContext, null);
        assertThat(mapping.findProjection(1), is(col1));
        assertThat(mapping.findProjection(2), is(col2));
    }
    
    @Test
    void assertFindProjectionReturnsNullWhenPhysicalMetaDataIsAuthoritative() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        ShorthandProjection shorthand = new ShorthandProjection(null, Collections.emptyList());
        List<Projection> projections = new ArrayList<>(1);
        projections.add(shorthand);
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, projections);
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        ResultSetMetaData physicalMetaData = mock(ResultSetMetaData.class);
        when(physicalMetaData.getColumnCount()).thenReturn(1);
        when(physicalMetaData.getColumnLabel(1)).thenReturn("id");
        VisibleColumnMapping mapping = VisibleColumnMapping.resolve(selectStatementContext, physicalMetaData);
        assertNull(mapping.findProjection(1));
    }
}
