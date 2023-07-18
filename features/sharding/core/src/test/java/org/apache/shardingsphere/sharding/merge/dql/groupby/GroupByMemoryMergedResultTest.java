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

package org.apache.shardingsphere.sharding.merge.dql.groupby;

import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.merge.dql.ShardingDQLResultMerger;
import org.apache.shardingsphere.sql.parser.sql.common.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.NullsOrderType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GroupByMemoryMergedResultTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test
    void assertNextForResultSetsAllEmpty() throws SQLException {
        when(database.getName()).thenReturn("db_schema");
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        MergedResult actual = resultMerger.merge(Arrays.asList(createQueryResult(), createQueryResult(), createQueryResult()), createSelectStatementContext(), database, mock(ConnectionContext.class));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(0));
        assertNull(actual.getValue(2, Object.class));
        assertFalse(actual.next());
    }
    
    @Test
    void assertNextForSomeResultSetsEmpty() throws SQLException {
        when(database.getName()).thenReturn("db_schema");
        QueryResult queryResult1 = createQueryResult();
        when(queryResult1.next()).thenReturn(true, false);
        when(queryResult1.getValue(1, Object.class)).thenReturn(20);
        when(queryResult1.getValue(2, Object.class)).thenReturn(0);
        when(queryResult1.getValue(3, Object.class)).thenReturn(2);
        when(queryResult1.getValue(4, Object.class)).thenReturn(2);
        when(queryResult1.getValue(5, Object.class)).thenReturn(20);
        QueryResult queryResult2 = createQueryResult();
        QueryResult queryResult3 = createQueryResult();
        when(queryResult3.next()).thenReturn(true, true, false);
        when(queryResult3.getValue(1, Object.class)).thenReturn(20, 30);
        when(queryResult3.getValue(2, Object.class)).thenReturn(0);
        when(queryResult3.getValue(3, Object.class)).thenReturn(2, 3);
        when(queryResult3.getValue(4, Object.class)).thenReturn(2, 2, 3);
        when(queryResult3.getValue(5, Object.class)).thenReturn(20, 20, 30);
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        MergedResult actual = resultMerger.merge(Arrays.asList(queryResult1, queryResult2, queryResult3), createSelectStatementContext(), database, mock(ConnectionContext.class));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(30)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat(actual.getValue(3, Object.class), is(3));
        assertThat(actual.getValue(4, Object.class), is(new BigDecimal(3)));
        assertThat(actual.getValue(5, Object.class), is(new BigDecimal(30)));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(40)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat(actual.getValue(3, Object.class), is(2));
        assertThat(actual.getValue(4, Object.class), is(new BigDecimal(4)));
        assertThat(actual.getValue(5, Object.class), is(new BigDecimal(40)));
        assertFalse(actual.next());
    }
    
    private SelectStatementContext createSelectStatementContext() {
        SelectStatement selectStatement = new MySQLSelectStatement();
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "(*)"));
        projectionsSegment.getProjections().add(new AggregationProjectionSegment(0, 0, AggregationType.AVG, "(num)"));
        selectStatement.setProjections(projectionsSegment);
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.ASC, NullsOrderType.FIRST))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setProjections(projectionsSegment);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        return new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
    }
    
    private SelectStatementContext createSelectStatementContext(final ShardingSphereDatabase database) {
        SelectStatement selectStatement = new MySQLSelectStatement();
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.setDistinctRow(true);
        projectionsSegment.getProjections().add(new ShorthandProjectionSegment(0, 0));
        selectStatement.setProjections(projectionsSegment);
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.DESC, NullsOrderType.FIRST))));
        selectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        selectStatement.setProjections(projectionsSegment);
        return new SelectStatementContext(createShardingSphereMetaData(database), Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
    }
    
    private ShardingSphereMetaData createShardingSphereMetaData(final ShardingSphereDatabase database) {
        return new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database), mock(ShardingSphereResourceMetaData.class),
                mock(ShardingSphereRuleMetaData.class), mock(ConfigurationProperties.class));
    }
    
    @Test
    void assertNextForAggregationResultSetsEmpty() throws SQLException {
        when(database.getName()).thenReturn("db_schema");
        QueryResult queryResult1 = createQueryResult();
        when(queryResult1.next()).thenReturn(true, false);
        when(queryResult1.getValue(1, Object.class)).thenReturn(20);
        when(queryResult1.getValue(2, Object.class)).thenReturn(0);
        when(queryResult1.getValue(3, Object.class)).thenReturn(2);
        when(queryResult1.getValue(4, Object.class)).thenReturn(2);
        when(queryResult1.getValue(5, Object.class)).thenReturn(20);
        QueryResult queryResult2 = createQueryResult();
        QueryResult queryResult3 = createQueryResult();
        when(queryResult3.next()).thenReturn(true, true, false);
        when(queryResult3.getValue(1, Object.class)).thenReturn(20, 30);
        when(queryResult3.getValue(2, Object.class)).thenReturn(0);
        when(queryResult3.getValue(3, Object.class)).thenReturn(2, 3);
        when(queryResult3.getValue(4, Object.class)).thenReturn(2, 2, 3);
        when(queryResult3.getValue(5, Object.class)).thenReturn(20, 20, 30);
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        MergedResult actual = resultMerger.merge(Arrays.asList(queryResult1, queryResult2, queryResult3), createSelectStatementContext(), database, mock(ConnectionContext.class));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(30)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat(actual.getValue(3, Object.class), is(3));
        assertThat(actual.getValue(4, Object.class), is(new BigDecimal(3)));
        assertThat(actual.getValue(5, Object.class), is(new BigDecimal(30)));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(40)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat(actual.getValue(3, Object.class), is(2));
        assertThat(actual.getValue(4, Object.class), is(new BigDecimal(4)));
        assertThat(actual.getValue(5, Object.class), is(new BigDecimal(40)));
        assertFalse(actual.next());
    }
    
    private QueryResult createQueryResult() throws SQLException {
        QueryResult result = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getColumnCount()).thenReturn(5);
        when(result.getMetaData().getColumnLabel(1)).thenReturn("COUNT(*)");
        when(result.getMetaData().getColumnLabel(2)).thenReturn("AVG(num)");
        when(result.getMetaData().getColumnLabel(3)).thenReturn("id");
        when(result.getMetaData().getColumnLabel(4)).thenReturn("AVG_DERIVED_COUNT_0");
        when(result.getMetaData().getColumnLabel(5)).thenReturn("AVG_DERIVED_SUM_0");
        return result;
    }
    
    @Test
    void assertNextForDistinctShorthandResultSetsEmpty() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(queryResult.getMetaData().getColumnCount()).thenReturn(2);
        when(queryResult.getMetaData().getColumnLabel(1)).thenReturn("order_id");
        when(queryResult.getMetaData().getColumnLabel(2)).thenReturn("content");
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(schema.getTable("t_order")).thenReturn(table);
        when(schema.containsTable("t_order")).thenReturn(true);
        when(table.getColumnValues()).thenReturn(Collections.emptyList());
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(schema);
        when(database.getSchemas()).thenReturn(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema));
        when(database.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        ShardingDQLResultMerger merger = new ShardingDQLResultMerger(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        MergedResult actual = merger.merge(Arrays.asList(queryResult, queryResult, queryResult), createSelectStatementContext(database), database, mock(ConnectionContext.class));
        assertFalse(actual.next());
    }
}
