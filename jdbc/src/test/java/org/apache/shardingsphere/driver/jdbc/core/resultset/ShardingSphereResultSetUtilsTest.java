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
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.DerivedProjection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingSphereResultSetUtilsTest {
    
    @Test
    void assertCreateColumnLabelAndIndexMapWithAggregationDistinctSchemaDrift() throws SQLException {
        DatabaseType databaseType = mock(DatabaseType.class);
        AggregationDistinctProjection count = new AggregationDistinctProjection(0, 0, AggregationType.COUNT,
                new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(DISTINCT user_id)"),
                new IdentifierValue("AGGREGATION_DISTINCT_DERIVED_0"), "user_id", databaseType);
        AggregationDistinctProjection sum = new AggregationDistinctProjection(0, 0, AggregationType.SUM,
                new AggregationProjectionSegment(0, 0, AggregationType.SUM, "SUM(DISTINCT user_id)"),
                new IdentifierValue("AGGREGATION_DISTINCT_DERIVED_1"), "user_id", databaseType);
        SelectStatementContext context = mock(SelectStatementContext.class);
        when(context.containsDerivedProjections()).thenReturn(true);
        List<Projection> projections = new ArrayList<>(3);
        projections.add(new ColumnProjection(null, "user_id", null, databaseType));
        projections.add(count);
        projections.add(sum);
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, projections);
        projectionsContext.getProjections().add(new DerivedProjection("status", new IdentifierValue("GROUP_BY_DERIVED_0"), mock(SQLSegment.class)));
        when(context.getProjectionsContext()).thenReturn(projectionsContext);
        ResultSetMetaData returnedMetadata = mock(ResultSetMetaData.class);
        when(returnedMetadata.getColumnCount()).thenReturn(5);
        when(returnedMetadata.getColumnLabel(1)).thenReturn("add_test");
        when(returnedMetadata.getColumnLabel(2)).thenReturn("user_id");
        when(returnedMetadata.getColumnLabel(3)).thenReturn("aggregation_distinct_derived_0");
        when(returnedMetadata.getColumnLabel(4)).thenReturn("AGGREGATION_DISTINCT_DERIVED_1");
        when(returnedMetadata.getColumnLabel(5)).thenReturn("GROUP_BY_DERIVED_0");
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(context, returnedMetadata);
        Map<String, Integer> expected = new HashMap<>(4, 1F);
        expected.put("add_test", 1);
        expected.put("user_id", 2);
        expected.put("COUNT(DISTINCT user_id)", 3);
        expected.put("SUM(DISTINCT user_id)", 4);
        assertThat(actual, is(expected));
        assertThat(actual.get("count(distinct user_id)"), is(3));
    }
    
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
        AggregationProjection avgProjection = new AggregationProjection(AggregationType.AVG,
                new AggregationProjectionSegment(0, 0, AggregationType.AVG, "AVG(col1)"), new IdentifierValue("avg"), mock(DatabaseType.class));
        appendAverageDerivedProjections(avgProjection);
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.singletonList(avgProjection));
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(3);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("avg");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("AVG_DERIVED_COUNT_0");
        when(resultSetMetaData.getColumnLabel(3)).thenReturn("AVG_DERIVED_SUM_0");
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, resultSetMetaData);
        assertThat(actual, is(Collections.singletonMap("avg", 1)));
    }
    
    @Test
    void assertCreateColumnLabelAndIndexMapFallsBackToResultSetMetaDataWhenNonRewritingAggregationProjectionMismatches() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
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
    void assertCreateColumnLabelAndIndexMapUsesProjectionsWhenDerivedColumnAppended() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        List<Projection> projections = new ArrayList<>(2);
        projections.add(new ColumnProjection(null, "col1", null, mock(DatabaseType.class)));
        projections.add(new ColumnProjection(null, "col2", null, mock(DatabaseType.class)));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, projections);
        projectionsContext.getProjections().add(new DerivedProjection("col1", new IdentifierValue("ORDER_BY_DERIVED_0"), mock(SQLSegment.class)));
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(3);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("col1");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("col2");
        when(resultSetMetaData.getColumnLabel(3)).thenReturn("ORDER_BY_DERIVED_0");
        Map<String, Integer> expected = new HashMap<>(2, 1F);
        expected.put("col1", 1);
        expected.put("col2", 2);
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, resultSetMetaData);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertCreateColumnLabelAndIndexMapUsesProjectionsWhenDerivedColumnAppendedInDifferentCase() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        List<Projection> projections = new ArrayList<>(2);
        projections.add(new ColumnProjection(null, "col1", null, mock(DatabaseType.class)));
        projections.add(new ColumnProjection(null, "col2", null, mock(DatabaseType.class)));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, projections);
        projectionsContext.getProjections().add(new DerivedProjection("col1", new IdentifierValue("ORDER_BY_DERIVED_0"), mock(SQLSegment.class)));
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(3);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("col1");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("col2");
        when(resultSetMetaData.getColumnLabel(3)).thenReturn("order_by_derived_0");
        Map<String, Integer> expected = new HashMap<>(2, 1F);
        expected.put("col1", 1);
        expected.put("col2", 2);
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, resultSetMetaData);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertCreateColumnLabelAndIndexMapFallsBackToResultSetMetaDataWhenDerivedColumnsNotAppended() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        List<Projection> projections = new ArrayList<>(2);
        projections.add(new ColumnProjection(null, "col1", null, mock(DatabaseType.class)));
        projections.add(new ColumnProjection(null, "col2", null, mock(DatabaseType.class)));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, projections);
        projectionsContext.getProjections().add(new DerivedProjection("col1", new IdentifierValue("ORDER_BY_DERIVED_0"), mock(SQLSegment.class)));
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
    void assertCreateColumnLabelAndIndexMapFallsBackToResultSetMetaDataWhenAggregationDerivedColumnsNotAppended() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        AggregationProjection avgProjection = new AggregationProjection(AggregationType.AVG,
                new AggregationProjectionSegment(0, 0, AggregationType.AVG, "AVG(col1)"), new IdentifierValue("avg"), mock(DatabaseType.class));
        appendAverageDerivedProjections(avgProjection);
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.singletonList(avgProjection));
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(2);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("avg");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("added_col");
        Map<String, Integer> expected = new HashMap<>(2, 1F);
        expected.put("avg", 1);
        expected.put("added_col", 2);
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, resultSetMetaData);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertCreateColumnLabelAndIndexMapUsesProjectionsWhenGroupByDerivedColumnAppended() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        List<Projection> projections = new ArrayList<>(2);
        projections.add(new ColumnProjection(null, "col1", null, mock(DatabaseType.class)));
        projections.add(new ColumnProjection(null, "col2", null, mock(DatabaseType.class)));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, projections);
        projectionsContext.getProjections().add(new DerivedProjection("col1", new IdentifierValue("GROUP_BY_DERIVED_0"), mock(SQLSegment.class)));
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(3);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("col1");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("col2");
        when(resultSetMetaData.getColumnLabel(3)).thenReturn("GROUP_BY_DERIVED_0");
        Map<String, Integer> expected = new HashMap<>(2, 1F);
        expected.put("col1", 1);
        expected.put("col2", 2);
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, resultSetMetaData);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertCreateColumnLabelAndIndexMapFallsBackToResultSetMetaDataWhenDerivedColumnAppendedWithSchemaDrift() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        List<Projection> projections = new ArrayList<>(2);
        projections.add(new ColumnProjection(null, "col1", null, mock(DatabaseType.class)));
        projections.add(new ColumnProjection(null, "col2", null, mock(DatabaseType.class)));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, projections);
        projectionsContext.getProjections().add(new DerivedProjection("col1", new IdentifierValue("ORDER_BY_DERIVED_0"), mock(SQLSegment.class)));
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(4);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("col1");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("added_col");
        when(resultSetMetaData.getColumnLabel(3)).thenReturn("col2");
        when(resultSetMetaData.getColumnLabel(4)).thenReturn("ORDER_BY_DERIVED_0");
        Map<String, Integer> expected = new HashMap<>(3, 1F);
        expected.put("col1", 1);
        expected.put("added_col", 2);
        expected.put("col2", 3);
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, resultSetMetaData);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertCreateColumnLabelAndIndexMapFallsBackToResultSetMetaDataWhenDerivedColumnAppendedWithReorderedColumns() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        List<Projection> projections = new ArrayList<>(2);
        projections.add(new ColumnProjection(null, "col1", null, mock(DatabaseType.class)));
        projections.add(new ColumnProjection(null, "col2", null, mock(DatabaseType.class)));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, projections);
        projectionsContext.getProjections().add(new DerivedProjection("col1", new IdentifierValue("ORDER_BY_DERIVED_0"), mock(SQLSegment.class)));
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(3);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("col2");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("col1");
        when(resultSetMetaData.getColumnLabel(3)).thenReturn("ORDER_BY_DERIVED_0");
        Map<String, Integer> expected = new HashMap<>(2, 1F);
        expected.put("col2", 1);
        expected.put("col1", 2);
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, resultSetMetaData);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertCreateColumnLabelAndIndexMapFallsBackToResultSetMetaDataWhenAggregationDerivedColumnsAppendedWithSchemaDrift() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        List<Projection> projections = new ArrayList<>(2);
        projections.add(new ColumnProjection(null, "col1", null, mock(DatabaseType.class)));
        AggregationProjection avgProjection = new AggregationProjection(AggregationType.AVG,
                new AggregationProjectionSegment(0, 0, AggregationType.AVG, "AVG(col1)"), new IdentifierValue("avg"), mock(DatabaseType.class));
        appendAverageDerivedProjections(avgProjection);
        projections.add(avgProjection);
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, projections);
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(5);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("col1");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("added_col");
        when(resultSetMetaData.getColumnLabel(3)).thenReturn("avg");
        when(resultSetMetaData.getColumnLabel(4)).thenReturn("AVG_DERIVED_COUNT_0");
        when(resultSetMetaData.getColumnLabel(5)).thenReturn("AVG_DERIVED_SUM_0");
        Map<String, Integer> expected = new HashMap<>(3, 1F);
        expected.put("col1", 1);
        expected.put("added_col", 2);
        expected.put("avg", 3);
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, resultSetMetaData);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertCreateColumnLabelAndIndexMapFallsBackToResultSetMetaDataWhenDerivedColumnAppendedWithRenamedColumn() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        List<Projection> projections = new ArrayList<>(3);
        projections.add(new ColumnProjection(null, "col1", null, mock(DatabaseType.class)));
        projections.add(new ColumnProjection(null, "col2", null, mock(DatabaseType.class)));
        projections.add(new ColumnProjection(null, "col3", null, mock(DatabaseType.class)));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, projections);
        projectionsContext.getProjections().add(new DerivedProjection("col1", new IdentifierValue("ORDER_BY_DERIVED_0"), mock(SQLSegment.class)));
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(4);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("col1");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("renamed_col");
        when(resultSetMetaData.getColumnLabel(3)).thenReturn("col3");
        when(resultSetMetaData.getColumnLabel(4)).thenReturn("ORDER_BY_DERIVED_0");
        Map<String, Integer> expected = new HashMap<>(3, 1F);
        expected.put("col1", 1);
        expected.put("renamed_col", 2);
        expected.put("col3", 3);
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, resultSetMetaData);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertCreateColumnLabelAndIndexMapKeepsDerivedNamedColumnWhenNoDerivedProjections() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(false);
        List<Projection> projections = new ArrayList<>(1);
        projections.add(new ColumnProjection(null, "col1", null, mock(DatabaseType.class)));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, projections);
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(2);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("col1");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("order_by_derived_0");
        Map<String, Integer> expected = new HashMap<>(2, 1F);
        expected.put("col1", 1);
        expected.put("order_by_derived_0", 2);
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, resultSetMetaData);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertCreateColumnLabelAndIndexMapWithNonSelectStatement() throws SQLException {
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(2);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("col1");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("order_by_derived_0");
        Map<String, Integer> expected = new HashMap<>(2, 1F);
        expected.put("col1", 1);
        expected.put("order_by_derived_0", 2);
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(mock(SQLStatementContext.class), resultSetMetaData);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertCreateColumnLabelAndIndexMapKeepsColumnWhoseNameCollidesWithDerivedAliasUnderSchemaDrift() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        List<Projection> projections = new ArrayList<>(2);
        projections.add(new ColumnProjection(null, "col1", null, mock(DatabaseType.class)));
        projections.add(new ColumnProjection(null, "order_by_derived_0", null, mock(DatabaseType.class)));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, projections);
        projectionsContext.getProjections().add(new DerivedProjection("col1", new IdentifierValue("ORDER_BY_DERIVED_0"), mock(SQLSegment.class)));
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(4);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("col1");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("add_test");
        when(resultSetMetaData.getColumnLabel(3)).thenReturn("order_by_derived_0");
        when(resultSetMetaData.getColumnLabel(4)).thenReturn("ORDER_BY_DERIVED_0");
        Map<String, Integer> expected = new HashMap<>(3, 1F);
        expected.put("col1", 1);
        expected.put("add_test", 2);
        expected.put("order_by_derived_0", 3);
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, resultSetMetaData);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertCreateColumnLabelAndIndexMapKeepsRealDerivedNamedColumnWhenDerivedColumnAppended() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        List<Projection> projections = new ArrayList<>(2);
        projections.add(new ColumnProjection(null, "order_id", null, mock(DatabaseType.class)));
        projections.add(new ColumnProjection(null, "ORDER_BY_DERIVED_9", null, mock(DatabaseType.class)));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, projections);
        projectionsContext.getProjections().add(new DerivedProjection("sort_key", new IdentifierValue("ORDER_BY_DERIVED_0"), mock(SQLSegment.class)));
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(3);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("order_id");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("ORDER_BY_DERIVED_9");
        when(resultSetMetaData.getColumnLabel(3)).thenReturn("ORDER_BY_DERIVED_0");
        Map<String, Integer> expected = new HashMap<>(2, 1F);
        expected.put("order_id", 1);
        expected.put("ORDER_BY_DERIVED_9", 2);
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, resultSetMetaData);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertCreateColumnLabelAndIndexMapKeepsRealDerivedNamedColumnWhenDerivedColumnAppendedWithSchemaDrift() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        List<Projection> projections = new ArrayList<>(2);
        projections.add(new ColumnProjection(null, "order_id", null, mock(DatabaseType.class)));
        projections.add(new ColumnProjection(null, "ORDER_BY_DERIVED_9", null, mock(DatabaseType.class)));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, projections);
        projectionsContext.getProjections().add(new DerivedProjection("sort_key", new IdentifierValue("ORDER_BY_DERIVED_0"), mock(SQLSegment.class)));
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(4);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("order_id");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("add_test");
        when(resultSetMetaData.getColumnLabel(3)).thenReturn("ORDER_BY_DERIVED_9");
        when(resultSetMetaData.getColumnLabel(4)).thenReturn("ORDER_BY_DERIVED_0");
        Map<String, Integer> expected = new HashMap<>(3, 1F);
        expected.put("order_id", 1);
        expected.put("add_test", 2);
        expected.put("ORDER_BY_DERIVED_9", 3);
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, resultSetMetaData);
        assertThat(actual, is(expected));
    }
}
