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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingSphereResultSetMetaDataTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Test
    void assertGetTableNameWithNullDatabase() throws SQLException {
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getTableName(1)).thenReturn("t_order");
        String actualTableName = new ShardingSphereResultSetMetaData(resultSetMetaData, null, null).getTableName(1);
        assertThat(actualTableName, is("t_order"));
    }
    
    @Test
    void assertGetTableNameWithDataNodeRuleAttribute() throws SQLException {
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getTableName(1)).thenReturn("t_order_0");
        DataNodeRuleAttribute ruleAttribute = mock(DataNodeRuleAttribute.class);
        when(ruleAttribute.findLogicTableByActualTable("t_order_0")).thenReturn(Optional.of("t_order"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData().getAttributes(DataNodeRuleAttribute.class)).thenReturn(Collections.singleton(ruleAttribute));
        String actualTableName = new ShardingSphereResultSetMetaData(resultSetMetaData, database, null).getTableName(1);
        assertThat(actualTableName, is("t_order"));
    }
    
    @Test
    void assertGetColumnCountWithExpandedProjections() throws SQLException {
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        Collection<Projection> projections = new ArrayList<>(2);
        projections.add(new ColumnProjection(null, "col1", null, databaseType));
        projections.add(new ColumnProjection(null, "col2", null, databaseType));
        when(selectStatementContext.getProjectionsContext()).thenReturn(new ProjectionsContext(0, 0, false, projections));
        assertThat(new ShardingSphereResultSetMetaData(resultSetMetaData, null, selectStatementContext).getColumnCount(), is(2));
    }
    
    @Test
    void assertGetColumnCountWithUnexpandedShorthand() throws SQLException {
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(3);
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        ShorthandProjection shorthand = new ShorthandProjection(null, Collections.emptyList());
        when(selectStatementContext.getProjectionsContext()).thenReturn(new ProjectionsContext(0, 0, false, Collections.singletonList(shorthand)));
        assertThat(new ShardingSphereResultSetMetaData(resultSetMetaData, null, selectStatementContext).getColumnCount(), is(3));
    }
    
    @Test
    void assertGetColumnCountWithoutDerivedProjections() throws SQLException {
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(5);
        assertThat(new ShardingSphereResultSetMetaData(resultSetMetaData, null, null).getColumnCount(), is(5));
    }
    
    @Test
    void assertGetColumnLabelWithExpandedProjections() throws SQLException {
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        Collection<Projection> projections = new ArrayList<>(1);
        projections.add(new ColumnProjection(null, "col1", "alias1", databaseType));
        when(selectStatementContext.getProjectionsContext()).thenReturn(new ProjectionsContext(0, 0, false, projections));
        assertThat(new ShardingSphereResultSetMetaData(resultSetMetaData, null, selectStatementContext).getColumnLabel(1), is("alias1"));
    }
    
    @Test
    void assertGetColumnLabelWithUnexpandedShorthandFallsBackToMetaData() throws SQLException {
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("physical_label");
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        ShorthandProjection shorthand = new ShorthandProjection(null, Collections.emptyList());
        when(selectStatementContext.getProjectionsContext()).thenReturn(new ProjectionsContext(0, 0, false, Collections.singletonList(shorthand)));
        assertThat(new ShardingSphereResultSetMetaData(resultSetMetaData, null, selectStatementContext).getColumnLabel(1), is("physical_label"));
    }
    
    @Test
    void assertGetColumnNameWithExpandedProjections() throws SQLException {
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        Collection<Projection> projections = new ArrayList<>(1);
        projections.add(new ColumnProjection(null, "col1", null, databaseType));
        when(selectStatementContext.getProjectionsContext()).thenReturn(new ProjectionsContext(0, 0, false, projections));
        assertThat(new ShardingSphereResultSetMetaData(resultSetMetaData, null, selectStatementContext).getColumnName(1), is("col1"));
    }
}
