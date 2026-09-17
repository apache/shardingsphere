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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.DerivedProjection;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingSphereResultSetMetaDataTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
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
    void assertGetColumnCountFallsBackToResultSetMetaDataWhenProjectionCountMismatches() throws SQLException {
        SelectStatementContext sqlStatementContext = createSelectStatementContext(Arrays.asList("order_id", "user_id", "status", "create_time"));
        assertTrue(sqlStatementContext.containsDerivedProjections());
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(5);
        stubBackendColumnLabels(resultSetMetaData, "order_id", "add_test", "user_id", "status", "create_time");
        ShardingSphereResultSetMetaData metaData = new ShardingSphereResultSetMetaData(resultSetMetaData, null, sqlStatementContext);
        assertThat(metaData.getColumnCount(), is(5));
    }
    
    @Test
    void assertGetColumnNameAndLabelFallBackToResultSetMetaDataWhenProjectionCountMismatches() throws SQLException {
        SelectStatementContext sqlStatementContext = createSelectStatementContext(Arrays.asList("order_id", "user_id", "status", "create_time"));
        assertTrue(sqlStatementContext.containsDerivedProjections());
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(5);
        stubBackendColumnLabels(resultSetMetaData, "order_id", "add_test", "user_id", "status", "create_time");
        when(resultSetMetaData.getColumnName(2)).thenReturn("add_test");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("add_test");
        ShardingSphereResultSetMetaData metaData = new ShardingSphereResultSetMetaData(resultSetMetaData, null, sqlStatementContext);
        assertThat(metaData.getColumnName(2), is("add_test"));
        assertThat(metaData.getColumnLabel(2), is("add_test"));
    }
    
    @Test
    void assertGetColumnNameAndLabelUseExpandedProjectionsWhenProjectionCountMatches() throws SQLException {
        SelectStatementContext sqlStatementContext = createSelectStatementContext(Collections.singletonList("user_id"));
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnName(1)).thenReturn("cipher_user_id");
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("cipher_user_id");
        ShardingSphereResultSetMetaData metaData = new ShardingSphereResultSetMetaData(resultSetMetaData, null, sqlStatementContext);
        assertThat(metaData.getColumnName(1), is("user_id"));
        assertThat(metaData.getColumnLabel(1), is("user_id"));
    }
    
    @Test
    void assertGetColumnCountAndLabelUseExpandedProjectionsWhenAggregationProjectionMismatches() throws SQLException {
        AggregationProjectionSegment avgSegment = new AggregationProjectionSegment(0, 0, AggregationType.AVG, "AVG(user_id)");
        avgSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("user_id_avg")));
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(avgSegment);
        SelectStatementContext sqlStatementContext = createSelectStatementContext(projectionsSegment);
        assertTrue(sqlStatementContext.containsDerivedProjections());
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(3);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("user_id_avg");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("AVG_DERIVED_COUNT_0");
        when(resultSetMetaData.getColumnLabel(3)).thenReturn("AVG_DERIVED_SUM_0");
        ShardingSphereResultSetMetaData metaData = new ShardingSphereResultSetMetaData(resultSetMetaData, null, sqlStatementContext);
        assertThat(metaData.getColumnCount(), is(1));
        assertThat(metaData.getColumnLabel(1), is("user_id_avg"));
    }
    
    @Test
    void assertGetColumnCountFallsBackToResultSetMetaDataWhenNonRewritingAggregationProjectionMismatches() throws SQLException {
        SelectStatementContext sqlStatementContext = createSelectStatementContext(Arrays.asList("order_id", "user_id", "status", "create_time"),
                new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(*)"));
        assertTrue(sqlStatementContext.containsDerivedProjections());
        assertTrue(sqlStatementContext.getProjectionsContext().getProjections().stream()
                .filter(AggregationProjection.class::isInstance).map(AggregationProjection.class::cast)
                .allMatch(each -> each.getDerivedAggregationProjections().isEmpty()));
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(6);
        stubBackendColumnLabels(resultSetMetaData, "order_id", "user_id", "status", "create_time", "COUNT(*)", "add_test");
        ShardingSphereResultSetMetaData metaData = new ShardingSphereResultSetMetaData(resultSetMetaData, null, sqlStatementContext);
        assertThat(metaData.getColumnCount(), is(6));
    }
    
    @Test
    void assertGetColumnCountUsesExpandedProjectionsWhenNonRewritingAggregationProjectionMatches() throws SQLException {
        SelectStatementContext sqlStatementContext = createSelectStatementContext(Arrays.asList("order_id", "user_id", "status", "create_time"),
                new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(*)"));
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(5);
        when(resultSetMetaData.getColumnLabel(5)).thenReturn("count_from_backend");
        ShardingSphereResultSetMetaData metaData = new ShardingSphereResultSetMetaData(resultSetMetaData, null, sqlStatementContext);
        assertThat(metaData.getColumnCount(), is(5));
        assertThat(metaData.getColumnLabel(5), is("COUNT(*)"));
    }
    
    @Test
    void assertGetColumnCountAndLabelUseExpandedProjectionsWhenAggregationDistinctProjectionRewritten() throws SQLException {
        AggregationDistinctProjectionSegment countDistinctSegment =
                new AggregationDistinctProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(DISTINCT user_id)", "user_id");
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(countDistinctSegment);
        GroupBySegment groupBySegment = new GroupBySegment(0, 0, Collections.singletonList(
                new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("status")), OrderDirection.ASC, NullsOrderType.LAST)));
        SelectStatementContext sqlStatementContext = createSelectStatementContext(projectionsSegment, groupBySegment);
        assertTrue(sqlStatementContext.containsDerivedProjections());
        assertTrue(sqlStatementContext.getProjectionsContext().getProjections().stream().anyMatch(DerivedProjection.class::isInstance));
        assertThat(sqlStatementContext.getProjectionsContext().getExpandProjections().size(), is(1));
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(2);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("AGGREGATION_DISTINCT_DERIVED_0");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("GROUP_BY_DERIVED_0");
        ShardingSphereResultSetMetaData metaData = new ShardingSphereResultSetMetaData(resultSetMetaData, null, sqlStatementContext);
        assertThat(metaData.getColumnCount(), is(1));
        assertThat(metaData.getColumnLabel(1), is("COUNT(DISTINCT user_id)"));
    }
    
    @Test
    void assertColumnIndexOutOfRangeWhenProjectionCountMatches() throws SQLException {
        SelectStatementContext sqlStatementContext = createSelectStatementContext(Collections.singletonList("user_id"));
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        ShardingSphereResultSetMetaData metaData = new ShardingSphereResultSetMetaData(resultSetMetaData, null, sqlStatementContext);
        assertThrows(SQLException.class, () -> metaData.getColumnName(2));
    }
    
    @Test
    void assertGetColumnCountAndLabelFallBackToResultSetMetaDataWhenDerivedColumnAppendedWithSchemaDrift() throws SQLException {
        SelectStatementContext sqlStatementContext = createSelectStatementContext(Arrays.asList("order_id", "user_id", "status", "create_time"));
        assertTrue(sqlStatementContext.containsDerivedProjections());
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(6);
        stubBackendColumnLabels(resultSetMetaData, "order_id", "add_test", "user_id", "status", "create_time", "ORDER_BY_DERIVED_0");
        when(resultSetMetaData.getColumnName(2)).thenReturn("add_test");
        ShardingSphereResultSetMetaData metaData = new ShardingSphereResultSetMetaData(resultSetMetaData, null, sqlStatementContext);
        assertThat(metaData.getColumnCount(), is(5));
        assertThat(metaData.getColumnLabel(2), is("add_test"));
        assertThat(metaData.getColumnLabel(5), is("create_time"));
        assertThat(metaData.getColumnName(2), is("add_test"));
        assertThrows(SQLException.class, () -> metaData.getColumnLabel(6));
    }
    
    @Test
    void assertGetColumnCountKeepsDerivedNamedColumnWhenNoDerivedProjections() throws SQLException {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.containsDerivedProjections()).thenReturn(false);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(2);
        stubBackendColumnLabels(resultSetMetaData, "col1", "order_by_derived_0");
        ShardingSphereResultSetMetaData metaData = new ShardingSphereResultSetMetaData(resultSetMetaData, null, sqlStatementContext);
        assertThat(metaData.getColumnCount(), is(2));
        assertThat(metaData.getColumnLabel(2), is("order_by_derived_0"));
    }
    
    @Test
    void assertReadingAllColumnHeadersReconcilesReturnedMetadataOnce() throws SQLException {
        SelectStatementContext sqlStatementContext = createSelectStatementContext(Arrays.asList("order_id", "user_id", "status", "create_time"));
        assertTrue(sqlStatementContext.containsDerivedProjections());
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        AtomicInteger countReadCount = new AtomicInteger();
        String[] backendColumnLabels = {"order_id", "add_test", "user_id", "status", "create_time", "ORDER_BY_DERIVED_0"};
        when(resultSetMetaData.getColumnCount()).thenAnswer(invocation -> {
            countReadCount.incrementAndGet();
            return backendColumnLabels.length;
        });
        AtomicInteger labelReadCount = new AtomicInteger();
        when(resultSetMetaData.getColumnLabel(anyInt())).thenAnswer(invocation -> {
            labelReadCount.incrementAndGet();
            return backendColumnLabels[invocation.getArgument(0, Integer.class) - 1];
        });
        ShardingSphereResultSetMetaData metaData = new ShardingSphereResultSetMetaData(resultSetMetaData, null, sqlStatementContext);
        int columnCount = metaData.getColumnCount();
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            metaData.getColumnName(columnIndex);
            metaData.getColumnLabel(columnIndex);
        }
        assertThat(columnCount, is(5));
        assertThat(countReadCount.get(), lessThanOrEqualTo(backendColumnLabels.length));
        assertThat(labelReadCount.get(), lessThanOrEqualTo(4 * backendColumnLabels.length));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("aggregationDistinctColumns")
    void assertAggregationDistinctMetadataWithSchemaDrift(final String name, final AggregationType type, final String alias,
                                                          final String returnedLabel, final String expectedLabel) throws SQLException {
        AggregationDistinctProjectionSegment aggregation = new AggregationDistinctProjectionSegment(0, 0, type, type.name() + "(DISTINCT user_id)", "user_id");
        if (!alias.isEmpty()) {
            aggregation.setAlias(new AliasSegment(0, 0, new IdentifierValue(alias)));
        }
        ShorthandProjectionSegment shorthand = new ShorthandProjectionSegment(0, 0);
        shorthand.getActualProjectionSegments().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("order_id"))));
        shorthand.getActualProjectionSegments().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id"))));
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(shorthand);
        projections.getProjections().add(aggregation);
        GroupBySegment groupBy = new GroupBySegment(0, 0, Collections.singletonList(new ExpressionOrderByItemSegment(0, 0, "user_id + 0", OrderDirection.ASC, NullsOrderType.LAST)));
        SelectStatementContext context = createSelectStatementContext(projections, groupBy);
        assertTrue(context.getProjectionsContext().getProjections().stream().anyMatch(DerivedProjection.class::isInstance));
        ResultSetMetaData returnedMetadata = mock(ResultSetMetaData.class);
        String[] returnedLabels = AggregationType.AVG == type
                ? new String[]{"order_id", "add_test", "user_id", returnedLabel, "AVG_DERIVED_COUNT_0", "AVG_DERIVED_SUM_0", "GROUP_BY_DERIVED_0"}
                : new String[]{"order_id", "add_test", "user_id", returnedLabel, "GROUP_BY_DERIVED_0"};
        when(returnedMetadata.getColumnCount()).thenReturn(returnedLabels.length);
        stubBackendColumnLabels(returnedMetadata, returnedLabels);
        when(returnedMetadata.getColumnName(1)).thenReturn("order_id");
        when(returnedMetadata.getColumnName(2)).thenReturn("add_test");
        when(returnedMetadata.getColumnName(3)).thenReturn("user_id");
        when(returnedMetadata.getColumnName(4)).thenReturn("user_id");
        ShardingSphereResultSetMetaData actual = new ShardingSphereResultSetMetaData(returnedMetadata, null, context);
        assertThat(actual.getColumnCount(), is(4));
        String[] expected = {"order_id", "add_test", "user_id", expectedLabel};
        for (int columnIndex = 1; columnIndex <= expected.length; columnIndex++) {
            assertThat(actual.getColumnLabel(columnIndex), is(expected[columnIndex - 1]));
            assertThat(actual.getColumnName(columnIndex), is(expected[columnIndex - 1]));
        }
        assertThrows(SQLException.class, () -> actual.getColumnLabel(5));
    }
    
    private static Collection<Arguments> aggregationDistinctColumns() {
        return Arrays.asList(
                Arguments.of("count", AggregationType.COUNT, "", "AGGREGATION_DISTINCT_DERIVED_0", "COUNT(DISTINCT user_id)"),
                Arguments.of("folded count", AggregationType.COUNT, "", "aggregation_distinct_derived_0", "COUNT(DISTINCT user_id)"),
                Arguments.of("aliased count", AggregationType.COUNT, "foo_count", "foo_count", "foo_count"),
                Arguments.of("sum", AggregationType.SUM, "", "AGGREGATION_DISTINCT_DERIVED_0", "SUM(DISTINCT user_id)"),
                Arguments.of("average", AggregationType.AVG, "", "AGGREGATION_DISTINCT_DERIVED_0", "AVG(DISTINCT user_id)"));
    }
    
    private void stubBackendColumnLabels(final ResultSetMetaData resultSetMetaData, final String... columnLabels) throws SQLException {
        for (int columnIndex = 1; columnIndex <= columnLabels.length; columnIndex++) {
            when(resultSetMetaData.getColumnLabel(columnIndex)).thenReturn(columnLabels[columnIndex - 1]);
        }
    }
    
    private SelectStatementContext createSelectStatementContext(final Collection<String> shorthandColumns) {
        return createSelectStatementContext(shorthandColumns, null);
    }
    
    private SelectStatementContext createSelectStatementContext(final Collection<String> shorthandColumns, final ProjectionSegment extraProjectionSegment) {
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 0);
        for (String each : shorthandColumns) {
            shorthandProjectionSegment.getActualProjectionSegments().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue(each))));
        }
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(shorthandProjectionSegment);
        if (null != extraProjectionSegment) {
            projectionsSegment.getProjections().add(extraProjectionSegment);
        }
        return createSelectStatementContext(projectionsSegment);
    }
    
    private SelectStatementContext createSelectStatementContext(final ProjectionsSegment projectionsSegment) {
        return createSelectStatementContext(projectionsSegment, null);
    }
    
    private SelectStatementContext createSelectStatementContext(final ProjectionsSegment projectionsSegment, final GroupBySegment groupBySegment) {
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("t_order"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).projections(projectionsSegment)
                .from(new SimpleTableSegment(tableNameSegment)).groupBy(groupBySegment).build();
        ShardingSphereMetaData metaData =
                new ShardingSphereMetaData(Collections.singleton(mockDatabase()), mock(ResourceMetaData.class), mock(RuleMetaData.class), new ConfigurationProperties(new Properties()));
        return new SelectStatementContext(selectStatement, metaData, "foo_db", Collections.emptyList());
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("foo_db");
        TableMapperRuleAttribute ruleAttribute = mock(TableMapperRuleAttribute.class, RETURNS_DEEP_STUBS);
        when(ruleAttribute.getEnhancedTableNames().contains("t_order")).thenReturn(true);
        when(result.getRuleMetaData().getAttributes(TableMapperRuleAttribute.class)).thenReturn(Collections.singleton(ruleAttribute));
        return result;
    }
}
