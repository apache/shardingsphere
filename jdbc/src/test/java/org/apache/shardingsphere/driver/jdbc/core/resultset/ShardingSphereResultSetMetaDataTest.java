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
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        // AVG is rewritten to SUM and COUNT on the backend, so a multi-route rewrite appends them and their derived aliases show up in the returned metadata.
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
        // The multi-route rewrite appended the derived order-by column and the backend schema drifted at the same time:
        // the derived column is hidden, the newly added user column is visible with its label and index in the returned metadata.
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
        // Without derived projections nothing appends derived columns, so a derived-looking label is a genuine column.
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(2);
        stubBackendColumnLabels(resultSetMetaData, "col1", "order_by_derived_0");
        ShardingSphereResultSetMetaData metaData = new ShardingSphereResultSetMetaData(resultSetMetaData, null, sqlStatementContext);
        assertThat(metaData.getColumnCount(), is(2));
        assertThat(metaData.getColumnLabel(2), is("order_by_derived_0"));
    }
    
    @Test
    void assertGetColumnLabelTranslatesVisibleOrdinalWhenDerivedColumnAppendedMidPosition() throws SQLException {
        SelectStatementContext sqlStatementContext = createSelectStatementContext(Arrays.asList("order_id", "user_id"));
        // The projection token generators append derived columns as a trailing block, so this mid-position layout cannot
        // occur today; it pins that a client-visible ordinal is translated to the matching index of the returned metadata.
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(4);
        stubBackendColumnLabels(resultSetMetaData, "order_id", "ORDER_BY_DERIVED_0", "user_id", "add_test");
        ShardingSphereResultSetMetaData metaData = new ShardingSphereResultSetMetaData(resultSetMetaData, null, sqlStatementContext);
        assertThat(metaData.getColumnCount(), is(3));
        assertThat(metaData.getColumnLabel(1), is("order_id"));
        assertThat(metaData.getColumnLabel(2), is("user_id"));
        assertThat(metaData.getColumnLabel(3), is("add_test"));
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
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("t_order"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).projections(projectionsSegment).from(new SimpleTableSegment(tableNameSegment)).build();
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
