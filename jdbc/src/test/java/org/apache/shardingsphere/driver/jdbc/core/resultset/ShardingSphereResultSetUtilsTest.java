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
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.DerivedProjection;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
        List<Projection> projections = new ArrayList<>(2);
        projections.add(new ColumnProjection(null, "col1", null, mock(DatabaseType.class)));
        projections.add(new ColumnProjection(null, "col2", null, mock(DatabaseType.class)));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, projections);
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(2);
        Map<String, Integer> expected = new HashMap<>(2, 1F);
        expected.put("col1", 1);
        expected.put("col2", 2);
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, resultSetMetaData);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertCreateColumnLabelAndIndexMapFallsBackToResultSetMetaDataWhenProjectionCountMismatches() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        List<Projection> projections = new ArrayList<>(2);
        projections.add(new ColumnProjection(null, "col1", null, mock(DatabaseType.class)));
        projections.add(new ColumnProjection(null, "col2", null, mock(DatabaseType.class)));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, projections);
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(3);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("col1");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("added_col");
        when(resultSetMetaData.getColumnLabel(3)).thenReturn("col2");
        Map<String, Integer> expected = new HashMap<>(3, 1F);
        expected.put("col1", 1);
        expected.put("added_col", 2);
        expected.put("col2", 3);
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, resultSetMetaData);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertCreateColumnLabelAndIndexMapUsesProjectionsWhenAggregationProjectionMismatches() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        // AVG is rewritten to SUM and COUNT on the backend, so the backend column count is larger than the expanded projection count.
        AggregationProjection avgProjection = new AggregationProjection(AggregationType.AVG,
                new AggregationProjectionSegment(0, 0, AggregationType.AVG, "AVG(col1)"), new IdentifierValue("avg"), mock(DatabaseType.class));
        appendAverageDerivedProjections(avgProjection);
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.singletonList(avgProjection));
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(2);
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, resultSetMetaData);
        assertThat(actual, is(Collections.singletonMap("avg", 1)));
    }
    
    @Test
    void assertCreateColumnLabelAndIndexMapFallsBackToResultSetMetaDataWhenNonRewritingAggregationProjectionMismatches() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        // COUNT is not rewritten, so the backend column count still has to match the expanded projection count.
        AggregationProjection countProjection = new AggregationProjection(AggregationType.COUNT,
                new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(*)"), new IdentifierValue("cnt"), mock(DatabaseType.class));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.singletonList(countProjection));
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(2);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("col1");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("cnt");
        Map<String, Integer> expected = new HashMap<>(2, 1F);
        expected.put("col1", 1);
        expected.put("cnt", 2);
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, resultSetMetaData);
        assertThat(actual, is(expected));
    }
    
    private void appendAverageDerivedProjections(final AggregationProjection averageProjection) {
        averageProjection.getDerivedAggregationProjections().add(new AggregationProjection(AggregationType.COUNT,
                new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(col1)"), new IdentifierValue("AVG_DERIVED_COUNT_0"), mock(DatabaseType.class)));
        averageProjection.getDerivedAggregationProjections().add(new AggregationProjection(AggregationType.SUM,
                new AggregationProjectionSegment(0, 0, AggregationType.SUM, "SUM(col1)"), new IdentifierValue("AVG_DERIVED_SUM_0"), mock(DatabaseType.class)));
    }
    
    @Test
    void assertCreateColumnLabelAndIndexMapUsesProjectionsWhenDerivedProjectionMismatches() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        // The order-by column is appended to the rewritten SQL, so the backend column count is larger than the expanded projection count.
        List<Projection> projections = new ArrayList<>(2);
        projections.add(new ColumnProjection(null, "col1", null, mock(DatabaseType.class)));
        projections.add(new ColumnProjection(null, "col2", null, mock(DatabaseType.class)));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, projections);
        projectionsContext.getProjections().add(new DerivedProjection("col1", new IdentifierValue("ORDER_BY_DERIVED_0"), mock(SQLSegment.class)));
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(3);
        Map<String, Integer> expected = new HashMap<>(2, 1F);
        expected.put("col1", 1);
        expected.put("col2", 2);
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, resultSetMetaData);
        assertThat(actual, is(expected));
    }
}
